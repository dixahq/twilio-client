package com.dixa.twilio.client.impl.phonenumber

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.HttpMethods
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString, TwilioPagingFlow}
import com.dixa.twilio.client.impl.TwilioUri.TwilioPath
import com.dixa.twilio.client.impl.phonenumber.IncomingPhoneNumberListRequest._
import com.dixa.twilio.model.phonenumber.TwilioIncomingPhoneNumber
import io.circe.generic.auto._

import java.net.URLEncoder
import scala.concurrent.ExecutionContext

private[impl] final class IncomingPhoneNumberListRequest()(
    implicit http: HttpExt,
    materializer: Materializer,
    executionContext: ExecutionContext
) {

  def apply(
      connSettings: TwilioConnectionSettings,
      filter: Option[TwilioIncomingPhoneNumber.PhoneNumberFilter]
  ): Source[TwilioIncomingPhoneNumber, NotUsed] = {
    val filterQueryParam = filter
      .map { f =>
        val filterEscaped = URLEncoder.encode(f.toString, "UTF-8")
        s"&PhoneNumber=$filterEscaped"
      }
      .getOrElse("")
    val pathAsString =
      s"/2010-04-01/Accounts/${connSettings.accountSid}/IncomingPhoneNumbers.json?PageSize=1000$filterQueryParam"
    TwilioPagingFlow
      .createPagingSrc(
        connSettings,
        TwilioPath(ApiSubDomain.Api, HttpMethods.GET, pathAsString)
      )
      .map(entityToIncomingPhoneNumberSeq)
      .mapConcat(identity)
  }
}

private object IncomingPhoneNumberListRequest {
  private final case class OuterJsonRep(incoming_phone_numbers: List[IncomingPhoneNumberJsonRep])

  private def entityToIncomingPhoneNumberSeq(entity: HttpEntityString) = {
    val decoded = entity.parseUnsafe[OuterJsonRep]()
    decoded.incoming_phone_numbers.map(_.toModel)
  }
}
