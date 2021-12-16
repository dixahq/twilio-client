package com.dixa.twilio.client

import java.util
import cats.effect.IO
import com.dixa.thrift.generated.{PhoneNumber => ThriftPhoneNumber, RequestMeta, TelephonyAccount}
import com.dixa.twilio.client.model.{
  InProgress,
  Paused,
  PhoneNumber,
  RecordingStatus,
  TRecordings,
  TwilioOutboundVerified,
  UpdateRecordingResponse
}
import com.twilio.sdk.TwilioRestException
import com.twilio.sdk.resource.instance._
import io.circe.generic.auto._
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair
import org.http4s.Method.{GET, POST}
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.{BasicCredentials, EntityDecoder, MediaType, Uri, UrlForm}
import org.slf4j.{Logger, LoggerFactory}
import org.scalactic.TypeCheckedTripleEquals._
import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import org.http4s.client.dsl.io._
import org.http4s.headers._
import org.http4s.client._

import scala.util.Try
import scala.util.control.NonFatal

//todo: write tests
class TwilioClientImpl(factory: TwilioRestClientFactory, client: Client[IO], apiUrl: String)(
    implicit ioEc: ExecutionContext
) extends TwilioClient {

  protected def log: Logger = LoggerFactory.getLogger(getClass)

  override def createSimultaneousRingCall(
      account: TelephonyAccount,
      from: ThriftPhoneNumber,
      to: ThriftPhoneNumber,
      url: String,
      statusCallbackUrl: String,
      fallbackUrl: Option[String] = None,
      username: Option[String] = None,
      password: Option[String] = None,
      timeout: Option[Duration]
  ): IO[Call] = {

    val callTypeString = "?call_type=simultaneous_ringing"
    val passwordEntry = password.map { password =>
      "SipAuthPassword" -> password
    }

    val usernameEntry = username.map { username =>
      "SipAuthUsername" -> username
    }

    val actualFallbackUrl = fallbackUrl.getOrElse(s"$apiUrl/v1/twilio/completed$callTypeString")

    val param = Map[String, String](
      "From"                -> from.e164Format,
      "To"                  -> to.e164Format,
      "Url"                 -> url,
      "Method"              -> "POST",
      "FallbackUrl"         -> actualFallbackUrl,
      "FallbackMethod"      -> "POST",
      "StatusCallback"      -> statusCallbackUrl,
      "StatusCallbackEvent" -> "completed",
      "Timeout"             -> timeout.map(_.toSeconds).getOrElse(30).toString
    ) ++ usernameEntry ++ passwordEntry

    for {
      call <- factory.resource(account).use { client =>
        IO.blocking(client.getAccount.getCallFactory.create(param.asJava))
      }
    } yield call
  }

  def createCall(
      account: TelephonyAccount,
      from: ThriftPhoneNumber,
      to: ThriftPhoneNumber,
      url: String,
      csid: Long,
      endConferenceOnExit: Boolean,
      muted: Boolean,
      startConferenceOnEnter: Boolean,
      fallbackUrl: Option[String],
      timeout: Option[Duration],
      callType: Option[String],
      username: Option[String],
      password: Option[String],
      callbackEvents: Seq[String] = Seq("completed"),
      toUserId: Option[String] = None
  ): IO[Call] = {
    val callTypeString = callType.map(s => "?call_type=" + s).getOrElse("")
    val toUserString   = toUserId.map(userId => s"&userId=$userId").getOrElse("")
    val statusCallback = s"$apiUrl/v1/twilio/completed$callTypeString$toUserString"
    val actualFallbackUrl = fallbackUrl
      .getOrElse(
        s"$apiUrl/v1/twilio/conference/join/$csid?endConferenceOnExit=$endConferenceOnExit&muted=$muted&startConferenceOnEnter=$startConferenceOnEnter"
      )
    val passwordEntry = password.map { password =>
      "SipAuthPassword" -> password
    }

    val usernameEntry = username.map { username =>
      "SipAuthUsername" -> username
    }

    val param = Map(
      "From"                 -> from.e164Format,
      "To"                   -> to.e164Format,
      "Url"                  -> url,
      "Method"               -> "POST",
      "FallbackUrl"          -> actualFallbackUrl,
      "FallbackMethod"       -> "POST",
      "StatusCallback"       -> statusCallback,
      "StatusCallbackMethod" -> "POST",
      "Timeout"              -> timeout.map(_.toSeconds).getOrElse(30).toString
    ) ++ usernameEntry ++ passwordEntry

    // Because StatusCallbackEvents cannot be sent as an array and maps have an unique key constraint,
    // we go around that by sending a list of tuples as parameters instead
    val paramList = (param.map(entry => new BasicNameValuePair(entry._1, entry._2)) ++
      callbackEvents.map(new BasicNameValuePair("StatusCallbackEvent", _))).asJavaCollection

    for {
      call <- factory.resource(account).use { client =>
        IO.blocking(
          client.getAccount.getCallFactory.create(new util.ArrayList[NameValuePair](paramList))
        )
      }
    } yield call
  }

  override def redirectCall(account: TelephonyAccount, callSid: String, url: String): IO[Unit] = {
    for {
      _ <- factory.resource(account).use { client =>
        IO.blocking(
          client.getAccount
            .getCall(callSid)
            .redirect(url, "POST")
        ).void
          .handleError {
            // This error occurs often during the shutdown of a call where we attempt to play a tone to the agent.
            // If the call has already been terminated Twilio will send this exception to us.
            // We should not care, thus we are swallowing it.
            case e: TwilioRestException
                if e.getErrorMessage.contains("Call is not in-progress. Cannot redirect.") => {}
          }
      }
    } yield ()
  }

  override def kickAllParticipants(
      account: TelephonyAccount,
      csid: Long,
      organizationId: String
  ): IO[Unit] = {
    val params = Map(
      "FriendlyName" -> toFriendlyName(csid, organizationId),
      "Status"       -> "in-progress"
    )
    for {
      _ <- factory.resource(account).use { client =>
        IO.blocking {
          client.getAccount
            .getConferences(params.asJava)
            .asScala
            .flatMap(_.getParticipants.asScala)
            .foreach(_.kick())
        }.handleError {
          // This error will occur all the time because we are being very thorough when shutting down calls
          // Twilio is telling us that the participant has already disconnected.
          // That is what we expect, so we swallow the "error".
          case e: TwilioRestException
              if e.getErrorMessage.contains("Participant is not connected")
                || e.getErrorMessage.contains("Cannot fetch") => {}
        }
      }
    } yield ()
  }

  override def putParticipantsOnHold(
      account: TelephonyAccount,
      csid: Long,
      orgId: String,
      callSids: List[String],
      hold: Boolean,
      holdSound: Option[String]
  ): IO[Unit] = {
    val holdSoundString = holdSound.getOrElse("dial-sound")
    val params = if (hold) {
      Map(
        "HoldUrl"    -> s"$apiUrl/v1/twilio/$holdSoundString",
        "Hold"       -> hold.toString,
        "HoldMethod" -> "POST"
      )
    } else {
      Map(
        "Hold" -> hold.toString
      )
    }

    for {
      _ <- factory.resource(account).use { client =>
        IO.blocking {
          client.getAccount
            .getConferences(Map("FriendlyName" -> toFriendlyName(csid, orgId)).asJava)
            .asScala
            .flatMap(
              _.getParticipants.asScala
                .filter(participant => callSids.contains(participant.getCallSid))
            )
            .foreach { participant => participant.update(params.asJava) }
        }
      }
    } yield ()
  }

  override def putAllOffHold(account: TelephonyAccount, csid: Long, orgId: String): IO[Unit] = {
    val params = Map("Hold" -> "false")
    for {
      _ <- factory.resource(account).use { client =>
        IO.blocking {
          client.getAccount
            .getConferences(Map("FriendlyName" -> toFriendlyName(csid, orgId)).asJava)
            .asScala
            .flatMap(_.getParticipants.asScala)
            .foreach { participant => participant.update(params.asJava) }
        }
      }
    } yield ()
  }

  override def hangupCall(
      meta: RequestMeta,
      account: TelephonyAccount,
      callSid: String*
  ): IO[Unit] = {
    for {
      _ <- factory
        .resource(account)
        .use { client =>
          IO.blocking {
            client.getAccount
          }.map(account => callSid.toList.foreach(account.getCall(_).hangup()))
        }
        .handleError {
          case t: TwilioRestException if t.getErrorMessage.endsWith("was not found") =>
            log.error(
              s"${meta.id} - unable to end call leg $callSid for organization ${account.twilioAccountSid}",
              t
            )
            Future.successful({})
        }
    } yield ()
  }

  override def deleteCallRecording(account: TelephonyAccount, url: String): IO[Boolean] = {
    for {
      result <- factory.resource(account).use { client =>
        IO.blocking {
          client.getAccount.getRecording(url.split("/").last).delete()
        }
      }
    } yield result
  }

  private def updateRecording(
      account: TelephonyAccount,
      conferenceSid: String,
      recordingSid: String,
      status: RecordingStatus
  ): IO[Either[String, UpdateRecordingResponse]] = {
    val route =
      s"https://api.twilio.com/2010-04-01/Accounts/${account.twilioAccountSid}/Conferences/$conferenceSid/Recordings/$recordingSid.json"

    val requestOrParseError = Uri.fromString(route).right.map { url =>
      POST(
        url,
        Authorization(BasicCredentials(account.twilioAccountSid, account.twilioAuthToken))
      ).withEntity(UrlForm("Status" -> status.toTwilioString))
    }
    for {
      res <- requestOrParseError match {
        case Left(error) =>
          log.error(s"Unable to send request to malformed url", error)
          IO(Left("Unable to update call recording due to unexpected error."))
        case Right(req) =>
          client.expect[UpdateRecordingResponse](req).map(Right(_))
      }
    } yield res
  }

  private def findRecordingFromConferenceSid(
      account: TelephonyAccount,
      conferenceSid: String
  ): IO[Option[TRecordings]] = {
    val route =
      s"https://api.twilio.com/2010-04-01/Accounts/${account.twilioAccountSid}/Conferences/$conferenceSid/Recordings.json"
    val urlOrParseError = Uri.fromString(route)

    urlOrParseError match {
      case Left(error) =>
        log.error(s"Unable to send request to malformed url", error)
        IO(None)
      case Right(url) =>
        executeGetRequest[TRecordings](account, url)
    }
  }

  private def updateConferenceRecording(
      account: TelephonyAccount,
      csid: Long,
      organizationId: String,
      status: RecordingStatus
  ): IO[Either[String, UpdateRecordingResponse]] = {
    log.info(s"updateRecording($account, $csid, $organizationId)")
    for {
      conference <- factory.resource(account).use { client =>
        IO(
          client.getAccount
            .getConferences(Map("FriendlyName" -> toFriendlyName(csid, organizationId)).asJava)
            .iterator()
            .asScala
            .toStream
            .headOption
        )
      }

      updateResult <-
        for {
          recordingListing <- conference match {
            case Some(conf) =>
              findRecordingFromConferenceSid(account, conf.getSid)
            case None =>
              IO(None)
          }
          recording = recordingListing.flatMap(_.recordings.headOption)

          updateRequestResult <- (conference, recording) match {
            case (Some(conf), Some(rec)) =>
              updateRecording(account, conf.getSid, rec.sid, status)
            case (_, None) =>
              IO(Left("Unable to find call recording, please try again in a short while."))
            case _ =>
              IO(Left("Unable to update call recording due to unexpected error."))
          }
        } yield updateRequestResult
    } yield updateResult
  }

  override def pauseCallRecording(
      account: TelephonyAccount,
      csid: Long,
      organizationId: String
  ): IO[Either[String, UpdateRecordingResponse]] = {
    log.info(s"pauseCallRecording($account, $csid, $organizationId)")
    updateConferenceRecording(account, csid, organizationId, Paused)
      .handleError { case NonFatal(e) =>
        log.error(s"pauseCallRecording($csid, $organizationId) failed", e)
        Left("Unable to pause pause call recording due to unexpected error.")
      }
  }

  override def resumeCallRecording(
      account: TelephonyAccount,
      csid: Long,
      organizationId: String
  ): IO[Either[String, UpdateRecordingResponse]] = {
    log.info(s"resumeCallRecording($account, $csid, $organizationId)")
    updateConferenceRecording(account, csid, organizationId, InProgress)
      .handleError { case NonFatal(e) =>
        log.error(s"resumeCallRecording($csid, $organizationId) failed", e)
        Left("Unable to resume call recording due to unexpected error.")
      }
  }

  private def executeGetRequest[A](account: TelephonyAccount, uri: Uri)(
      implicit encoder: EntityDecoder[IO, A]
  ): IO[Option[A]] = {
    val req = GET(
      uri,
      Authorization(BasicCredentials(account.twilioAccountSid, account.twilioAuthToken)),
      Accept(MediaType.application.json)
    )
    for {
      res <- client.expect[A](req).map(Option.apply)
    } yield res
  }

  private def toFriendlyName(csid: Long, orgId: String) = s"${csid}_$orgId"

  // usageTrigger handling to be implemented?
  override def getApplication(
      account: TelephonyAccount,
      params: Map[String, String]
  ): IO[Option[com.twilio.sdk.resource.instance.Application]] = {
    for {
      result <- factory.resource(account).use { client =>
        IO {
          client.getAccount
            .getApplications(params.asJava)
            .iterator()
            .asScala
            .toList
            .headOption
        }
      }
    } yield result
  }

  override def isTwilioSuspended(account: TelephonyAccount): IO[Boolean] = {
    factory.resource(account).use { twilioClient =>
      IO.blocking {
        val status = Try {
          twilioClient.getAccount.getStatus
        }.getOrElse("suspended")
        status match {
          case "active" => false
          case _        => true
        }
      }
    }
  }

  override def isNumberOutboundVerified(
      phoneNumber: PhoneNumber,
      account: TelephonyAccount
  ): IO[TwilioOutboundVerified] = {
    val twilioRespIo = factory.resource(account).use { twilioClient =>
      IO.blocking {
        val filter = Map("PhoneNumber" -> phoneNumber.inFormatE164).asJava
        // toList is for force copy it in blocking context, as the returned type is a pretty
        // complex one, that could lazy fetch stuff later.
        twilioClient.getAccount.getOutgoingCallerIds(filter).asScala.toList
      }
    }
    val validatedPhoneNumbersIo = twilioRespIo.map { twilioResp =>
      twilioResp.map { on =>
        PhoneNumber.fromE123OrE164(on.getPhoneNumber)
      }
    }
    validatedPhoneNumbersIo.map { validatedPhoneNumbers =>
      TwilioOutboundVerified.fromBoolean(validatedPhoneNumbers.exists(_ === phoneNumber))
    }
  }

}
