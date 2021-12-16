package com.dixa.twilio.client

import cats.effect.{IO, Resource}
import com.dixa.thrift.generated.TelephonyAccount
import com.twilio.sdk.TwilioRestClient

import scala.concurrent.ExecutionContext

/** For testing purposes, this trait is used for creating a live or mocked Twilio rest client
  */
trait TwilioRestClientFactory {
  def createClient(account: TelephonyAccount): TwilioRestClient =
    createClient(account.twilioAccountSid, account.twilioAuthToken)

  def createClient(sid: String, token: String): TwilioRestClient

  def resource(account: TelephonyAccount): Resource[IO, TwilioRestClient]
}

/** Default factory for production usage
  */
private[client] class TwilioRestClientFactoryImpl()(
    implicit ioEC: ExecutionContext
) extends TwilioRestClientFactory {
  override def createClient(sid: String, token: String): TwilioRestClient = {
    new TwilioRestClient(sid, token)
  }

  @annotation.nowarn // getConnectionManager has been deprecated by apache http client, but there is not a better way to shut down this client from a very old and outdated Twilio SDK. We should fix this by replacing or updating the TwilioSDK: https://dixa-product.atlassian.net/browse/TEAMS-6374
  override def resource(account: TelephonyAccount): Resource[IO, TwilioRestClient] = {
    Resource.make {
      IO(createClient(account.twilioAccountSid, account.twilioAuthToken))
    } { cient => IO(cient.getHttpClient.getConnectionManager.shutdown()) }
  }

}
