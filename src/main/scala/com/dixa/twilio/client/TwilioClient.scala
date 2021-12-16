package com.dixa.twilio.client

import cats.effect.IO
import cats.effect.kernel.Resource
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
import com.twilio.sdk.resource.instance._
import org.http4s.blaze.client.BlazeClientBuilder
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext

object TwilioClient {
  def resource(apiUrl: String)(clientFactory: TwilioRestClientFactory)(
      implicit ioEc: ExecutionContext
  ): Resource[IO, TwilioClientImpl] = {
    BlazeClientBuilder[IO](ioEc).resource
      .map { client =>
        new TwilioClientImpl(clientFactory, client, apiUrl)
      }
  }
}

trait TwilioClient {
  def createSimultaneousRingCall(
      account: TelephonyAccount,
      from: ThriftPhoneNumber,
      to: ThriftPhoneNumber,
      url: String,
      statusCallbackUrl: String,
      fallbackUrl: Option[String] = None,
      username: Option[String] = None,
      password: Option[String] = None,
      timeout: Option[Duration]
  ): IO[Call]

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
  ): IO[Call]

  def redirectCall(account: TelephonyAccount, callSid: String, url: String): IO[Unit]

  def kickAllParticipants(
      account: TelephonyAccount,
      csid: Long,
      organizationId: String
  ): IO[Unit]

  def putParticipantsOnHold(
      account: TelephonyAccount,
      csid: Long,
      orgId: String,
      callSids: List[String],
      hold: Boolean,
      holdSound: Option[String]
  ): IO[Unit]

  def putAllOffHold(account: TelephonyAccount, csid: Long, orgId: String): IO[Unit]

  def hangupCall(meta: RequestMeta, account: TelephonyAccount, callSid: String*): IO[Unit]
  def deleteCallRecording(account: TelephonyAccount, url: String): IO[Boolean]
  def pauseCallRecording(
      account: TelephonyAccount,
      csid: Long,
      organizationId: String
  ): IO[Either[String, UpdateRecordingResponse]]

  def resumeCallRecording(
      account: TelephonyAccount,
      csid: Long,
      organizationId: String
  ): IO[Either[String, UpdateRecordingResponse]]

  def getApplication(
      account: TelephonyAccount,
      params: Map[String, String]
  ): IO[Option[com.twilio.sdk.resource.instance.Application]]

  def isTwilioSuspended(account: TelephonyAccount): IO[Boolean]

  // todo pr look into a unit test for this, but it may be hard to do, as long as we are using the old twilio sdk and not our own twilio client.
  /** Checks if provided number is outbound verified at Twilio
    */
  def isNumberOutboundVerified(
      phoneNumber: PhoneNumber,
      account: TelephonyAccount
  ): IO[TwilioOutboundVerified]

}
