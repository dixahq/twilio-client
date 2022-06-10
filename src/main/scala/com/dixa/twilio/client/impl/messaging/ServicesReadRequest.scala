package com.dixa.twilio.client.impl.messaging

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.HttpMethods
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.impl.TwilioUri.TwilioPath
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString, TwilioPagingFlow}
import com.dixa.twilio.model.messaging.TwilioMessagingService
import io.circe.generic.auto._

import scala.annotation.nowarn

@deprecated("Use ServicesReadRequestExecutor instead", "0.11.0")
private[impl] final class ServicesReadRequest()(
    implicit httpExt: HttpExt,
    materializer: Materializer
) {

  import ServicesReadRequest._

  def apply(
      connSettings: TwilioConnectionSettings
  ): Source[TwilioMessagingService, NotUsed] = {
    TwilioPagingFlow
      .createPagingSrc(
        connSettings,
        TwilioPath(ApiSubDomain.Messaging, HttpMethods.GET, "/v1/Services?PageSize=1000")
      )
      .map(entityToServiceList)
      .mapConcat(identity)
  }
}

private object ServicesReadRequest {

  private final case class OuterJsonRep(services: List[MessagingServiceJsonRep])

  private def entityToServiceList(entity: HttpEntityString) = {
    val decoded = entity.parse[OuterJsonRep]().toTry.get
    decoded.services.map(_.toTwilioMessagingService)
  }
}
