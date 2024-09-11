package com.dixa.twilio.client.impl.messaging

import com.dixa.twilio.client.impl.messaging.ChannelsSendersVerificationConfigurationJsonRep.ConfigurationJsonRep
import com.dixa.twilio.client.impl.messaging.ChannelsSendersVerificationConfigurationJsonRep._
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString}
import com.dixa.twilio.client.messaging.ChannelsSendersVerificationRequestExecutor
import com.dixa.twilio.client.messaging.ChannelsSendersVerificationRequestExecutor.ChannelSenderVerificationException
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import upickle.default._

import scala.concurrent.ExecutionContext

private[impl] class ChannelsSendersVerificationRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ChannelsSendersVerificationRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Messaging

  override protected def method: org.apache.pekko.http.scaladsl.model.HttpMethod = HttpMethods.POST

  override def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ChannelsSendersVerificationRequestExecutor.ChannelSenderVerificationRequest
  ): Either[ChannelSenderVerificationException, HttpRequest] = {
    createHttpRequestFor(
      s"/${ApiVersion.V2}/Channels/Senders/${req.senderSid}",
      connSettings
    ).map(
      _.withEntity(
        HttpEntity(
          ContentTypes.`application/json`,
          write[ChannelsSendersVerificationConfigurationJsonRep](
            ChannelsSendersVerificationConfigurationJsonRep(
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
      request: ChannelsSendersVerificationRequestExecutor.ChannelSenderVerificationRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[ChannelSenderVerificationException, Unit] = Right(())
}
