package com.dixa.twilio.client.impl.messaging

import com.dixa.twilio.client.impl.messaging.ChannelSenderVerificationConfigurationJsonRep.ConfigurationJsonRep
import com.dixa.twilio.client.impl.messaging.ChannelSenderVerificationConfigurationJsonRep._
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString}
import com.dixa.twilio.client.messaging.ChannelSenderVerificationRequestExecutor
import com.dixa.twilio.client.messaging.ChannelSenderVerificationRequestExecutor.ChannelSenderVerificationException
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import upickle.default._

import scala.concurrent.ExecutionContext

private[impl] class ChannelSenderVerificationRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ChannelSenderVerificationRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Messaging

  override protected def method: org.apache.pekko.http.scaladsl.model.HttpMethod = HttpMethods.POST

  override def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ChannelSenderVerificationRequestExecutor.ChannelSenderVerificationRequest
  ): Either[ChannelSenderVerificationException, HttpRequest] = {
    createHttpRequestFor(
      s"/${ApiVersion.V2}/Channels/Senders/${req.senderSid}",
      connSettings
    ).map(
      _.withEntity(
        HttpEntity(
          ContentTypes.`application/json`,
          write[ChannelSenderVerificationConfigurationJsonRep](
            ChannelSenderVerificationConfigurationJsonRep(
              ConfigurationJsonRep(req.verificationCode.verificationCode)
            )
          )
        )
      )
    )

  }

  override protected def mapApiException(
      apiException: ApiException
  ): ChannelSenderVerificationException.Api =
    ChannelSenderVerificationException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): ChannelSenderVerificationException.Unspecified =
    ChannelSenderVerificationException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      request: ChannelSenderVerificationRequestExecutor.ChannelSenderVerificationRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[ChannelSenderVerificationException, Unit] = Right(())
}
