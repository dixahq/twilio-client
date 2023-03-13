package com.dixa.twilio.client.impl
import com.dixa.twilio.model.TwilioStringValue

import java.net.URLEncoder

/** Helper class for easy building of multiple query params, into one single String, that can just
  * be added to the end of a url
  */
private[impl] final class QueryParamBuilder private (private val paramStrings: List[String]) {

  def withParam(key: String, value: String): QueryParamBuilder = {
    val encodedKey   = URLEncoder.encode(key, "utf-8")
    val encodedValue = URLEncoder.encode(value, "utf-8")
    val toAdd        = s"$encodedKey=$encodedValue"
    new QueryParamBuilder(toAdd +: paramStrings)
  }

  def withParam(key: String, value: TwilioStringValue): QueryParamBuilder =
    withParam(key, value.twilioString)

  def withOptionalParam(
      key: String,
      valueOpt: Option[TwilioStringValue]
  ): QueryParamBuilder = valueOpt match {
    case Some(value) => withParam(key, value.twilioString)
    case None        => this
  }

  def build: String = paramStrings match {
    case Nil                            => ""
    case ::(head, tail) if tail.isEmpty => s"?$head"
    case ::(head, tail)                 => s"?$head${tail.mkString("&", "&", "")}"
  }

  def buildForPostParams: String = paramStrings match {
    case Nil                            => ""
    case ::(head, tail) if tail.isEmpty => s"$head"
    case ::(head, tail)                 => s"$head${tail.mkString("&", "&", "")}"
  }
}

private[impl] object QueryParamBuilder {
  val empty = new QueryParamBuilder(List.empty)
}
