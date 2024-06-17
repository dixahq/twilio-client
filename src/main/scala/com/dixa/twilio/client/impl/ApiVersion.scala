package com.dixa.twilio.client.impl

import com.dixa.twilio.model.EnumWithTwilioString

import scala.collection.immutable

/** Twilio use different subdomains for different APIs. This enum specifies them. */
private[client] sealed abstract class ApiVersion(
    override val toString: String,
) extends EnumWithTwilioString.EnumEntry

private[client] object ApiVersion extends EnumWithTwilioString[ApiVersion] {

  override val values: immutable.IndexedSeq[ApiVersion] = findValues

  case object `2010-04-01` extends ApiVersion("2010-04-01")

  case object V2 extends ApiVersion("v2")
}
