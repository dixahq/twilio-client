package com.dixa.twilio.client.impl.messaging

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, StatusCodes}
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.impl.messaging.ServiceCreateRequest.createPostParamString
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString, TwilioUri}
import com.dixa.twilio.client.messaging.TwilioClientMessaging
import com.dixa.twilio.model.messaging.TwilioMessagingService

import java.net.URLEncoder
import scala.concurrent.{ExecutionContext, Future}

private[impl] final class ServiceCreateRequest()(
    implicit http: HttpExt,
    materializer: Materializer,
    executionContext: ExecutionContext
) {

  def apply(
      connSettings: TwilioConnectionSettings,
      req: TwilioClientMessaging.ServiceCreateRequest
  ): Future[TwilioMessagingService] = {
    val postParams = createPostParamString(req)
    val httpReq    = TwilioUri
      .createPathUnsafe(
        ApiSubDomain.Messaging,
        HttpMethods.POST,
        s"/${ApiVersion.V1}/Services"
      )
      .createHttpRequestUnsafe(connSettings)
      .withEntity(HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, postParams))
    http.singleRequest(httpReq).flatMap { httpResp =>
      if (httpResp.status != StatusCodes.Created) {
        throw new RuntimeException(
          s"Could not create service: $req, due to getting status code ${httpResp.status} from Twilio"
        )
      }
      httpResp.entity.toStrict(connSettings.timeouts.requestEntityTimeout).map { entity =>
        val entityString = HttpEntityString(entity.data.utf8String)
        val decoded      = entityString.parse[MessagingServiceJsonRep]().toTry.get
        decoded.toTwilioMessagingService
      }
    }
  }

}

private object ServiceCreateRequest {
  private def createPostParamString(t: TwilioClientMessaging.ServiceCreateRequest): String = {
    val sb = new StringBuilder

    if (t.friendlyName.toString.nonEmpty)
      sb.append(s"FriendlyName=${encode(t.friendlyName)}&")

    t.statusCallback.foreach(statusCallback =>
      sb.append(s"StatusCallback=${encode(statusCallback)}&")
    )

    t.fallbackWebhook.foreach { fallbackHook =>
      sb.append(s"FallbackUrl=${encode(fallbackHook.url)}&")
      sb.append(s"FallbackMethod=${encode(fallbackHook.method)}&")
    }

    t.inboundRequestWebhook.foreach { inboundHook =>
      sb.append(s"InboundRequestUrl=${encode(inboundHook.url)}&")
      sb.append(s"InboundMethod=${encode(inboundHook.method)}&")
    }

    sb.append(s"UseInboundWebhookOnNumber=${encode(t.useInboundWebhookOnNumber)}")

    sb.toString()
  }

  private[this] def encode(s: Any): String = URLEncoder.encode(s.toString, "UTF-8")
}
