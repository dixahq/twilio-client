package com.dixa.twilio.client.implDetails.request.messaging

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, StatusCodes}
import akka.stream.Materializer
import com.dixa.twilio.client.implDetails.TwilioUri.TwilioPath
import com.dixa.twilio.client.implDetails.request.messaging.ServiceCreateRequest.createPostParamString
import com.dixa.twilio.client.implDetails.{ApiSubDomain, HttpEntityString}
import com.dixa.twilio.client.model.messaging.TwilioMessagingService
import com.dixa.twilio.client.{TwilioClientMessaging, TwilioConnectionSettings}
import org.scalactic.TypeCheckedTripleEquals._
import io.circe.generic.auto._

import java.net.URLEncoder
import scala.concurrent.{ExecutionContext, Future}

private[implDetails] final class ServiceCreateRequest()(
    implicit http: HttpExt,
    materializer: Materializer,
    executionContext: ExecutionContext
) {

  def apply(
      connSettings: TwilioConnectionSettings,
      toCreate: TwilioClientMessaging.ServiceCreateRequest
  ): Future[TwilioMessagingService] = {
    val postParams = createPostParamString(toCreate)
    val req = TwilioPath(
      ApiSubDomain.Messaging,
      HttpMethods.POST,
      "/v1/Services"
    )
      .createHttpRequest(connSettings)
      .withEntity(HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, postParams))
    http.singleRequest(req).flatMap { resp =>
      if (resp.status !== StatusCodes.Created) {
        throw new RuntimeException(
          s"Could not create service: $toCreate, due to getting status code ${resp.status} from Twilio"
        )
      }
      resp.entity.toStrict(connSettings.timeouts.requestEntityTimeout).map { entity =>
        val entityString = HttpEntityString(entity.data.utf8String)
        val decoded      = entityString.parseUnsafe[MessagingServiceJsonRep]()
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
