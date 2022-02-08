package com.dixa.twilio.client.impl.messaging

import akka.Done
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpMethods, HttpResponse, ResponseEntity, StatusCodes}
import akka.stream.Materializer
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.TwilioConnectionSettings.Timeouts
import com.dixa.twilio.client.impl.TwilioUri.TwilioPath
import com.dixa.twilio.client.impl.{ApiSubDomain, DefaultApiErrorEntityJsonRep, HttpEntityString}
import com.dixa.twilio.client.messaging.PhoneNumberDeleteRequestExecutor
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}

private[impl] final class PhoneNumberDeleteRequestExecutorImpl()(
    implicit http: HttpExt,
    materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends PhoneNumberDeleteRequestExecutor {

  import PhoneNumberDeleteRequestExecutor._

  override def run(
      connSettings: TwilioConnectionSettings,
      req: PhoneNumberDeleteRequestExecutor.PhoneNumberDeleteRequest
  ): Future[Either[PhoneNumberDeleteRequestExecutor.PhoneNumberDeleteException, Done]] = {
    val httpReq = TwilioPath(
      ApiSubDomain.Messaging,
      HttpMethods.DELETE,
      s"/v1/Services/${req.serviceSid}/PhoneNumbers/${req.phoneNumberSid}"
    )
      .createHttpRequest(connSettings)
    http
      .singleRequest(httpReq)
      .flatMap { httpResp =>
        httpResp.status match {
          case StatusCodes.OK =>
            httpResp.entity.discardBytes()
            Future.successful(Right(Done))
          case StatusCodes.NotFound =>
            buildResultForNotFoundResponse(httpResp.entity, connSettings.timeouts)
          case _ => buildOtherStatusCodeErrorResponse(req, httpResp, connSettings.timeouts)
        }
      }
      .recover { case e: Exception =>
        Left(new PhoneNumberDeleteException.UnspecifiedError(e))
      }
  }

  private def buildOtherStatusCodeErrorResponse(
      req: PhoneNumberDeleteRequestExecutor.PhoneNumberDeleteRequest,
      resp: HttpResponse,
      timeouts: Timeouts
  ) = {
    resp.entity.toStrict(timeouts.requestEntityTimeout).map { entity =>
      val entityAsString = entity.data.utf8String
      val msg = s"Could not perform: $req, due to getting status code ${resp.status}. " +
        s"Full entity is: $entityAsString"
      Left(new PhoneNumberDeleteException.UnspecifiedError(msg))
    }
  }

  private def buildResultForNotFoundResponse(
      entity: ResponseEntity,
      timeouts: TwilioConnectionSettings.Timeouts
  ) = {
    entity.toStrict(timeouts.requestEntityTimeout).map { entity =>
      val entityString = HttpEntityString(entity.data.utf8String)
      val decoded      = entityString.parseUnsafe[DefaultApiErrorEntityJsonRep]()
      Left(PhoneNumberDeleteException.NotFound(decoded.message))
    }
  }

}
