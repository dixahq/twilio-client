package com.dixa.twilio.client.impl.phonenumber

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.HttpMethods
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString, TwilioPagingFlow}
import com.dixa.twilio.client.impl.TwilioUri.TwilioPath
import com.dixa.twilio.client.impl.phonenumber.ActivePhoneNumberListRequest._
import com.dixa.twilio.model.phonenumber.{TwilioActivePhoneNumber, TwilioPhoneNumberSid}
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

/** Represents a request to fetch a list of active phone numbers, optionally filtering by a specific
  * phone number SID.
  *
  * See
  * https://www.twilio.com/docs/phone-numbers/global-catalog/api/active-numbers?code-sample=code-get-activenumbers-instance&code-language=curl&code-sdk-version=default
  */
private[impl] final class ActivePhoneNumberListRequest()(
    implicit http: HttpExt,
    materializer: Materializer,
    executionContext: ExecutionContext
) {

  def apply(
      connSettings: TwilioConnectionSettings,
      phoneNumberSid: Option[TwilioPhoneNumberSid]
  ): Source[TwilioActivePhoneNumber, NotUsed] = {
    val pathAsString =
      s"/Numbers/ActiveNumbers/${phoneNumberSid.map(_.asString).getOrElse("")}?PageSize=1000"

    TwilioPagingFlow
      .createPagingSrc(
        connSettings,
        TwilioPath(ApiSubDomain.Preview, HttpMethods.GET, pathAsString)
      )
      .map(entityToModel)
      .mapConcat(identity)
  }
}

private object ActivePhoneNumberListRequest {
  private final case class OuterJsonRep(items: List[ActivePhoneNumberJsonRep])

  private def entityToModel(entity: HttpEntityString): List[TwilioActivePhoneNumber] = {
    val decoded = entity.parseUnsafe[OuterJsonRep]()
    decoded.items.map(_.toModel)
  }
}
