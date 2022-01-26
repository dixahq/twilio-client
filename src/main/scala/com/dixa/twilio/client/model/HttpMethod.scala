package com.dixa.twilio.client.model

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

sealed abstract class HttpMethod extends EnumEntry {}

object HttpMethod extends Enum[HttpMethod] {
  override val values: immutable.IndexedSeq[HttpMethod] = findValues

  case object Get  extends HttpMethod
  case object Post extends HttpMethod

  // Put and Delete are deliberately not here, as they seem to be not used at all in the Twilio API.
}
