package com.dixa.twilio.client.implDetails.request.messaging

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.HttpMethods
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.implDetails.TwilioUri.TwilioPath
import com.dixa.twilio.client.implDetails.{ApiSubDomain, HttpEntityString, TwilioPagingFlow}
import com.dixa.twilio.client.model.messaging.TwilioMessagingService
import io.circe.generic.auto._

private[implDetails] final class ServicesReadRequest()(
    implicit httpExt: HttpExt,
    materializer: Materializer
) {

  import ServicesReadRequest._

  def apply(
      conSettings: TwilioConnectionSettings
  ): Source[TwilioMessagingService, NotUsed] = {
    TwilioPagingFlow
      .createPagingSrc(
        conSettings,
        TwilioPath(ApiSubDomain.Messaging, HttpMethods.GET, "/v1/Services?PageSize=1000")
      )
      .map(entityToServiceList)
      .mapConcat(identity)
  }
}

private object ServicesReadRequest {

  private final case class OuterJsonRep(services: List[MessagingServiceJsonRep])

  private def entityToServiceList(entity: HttpEntityString) = {
    val decoded = entity.parseUnsafe[OuterJsonRep]()
    decoded.services.map(_.toTwilioMessagingService)
  }
}
