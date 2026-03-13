package com.dixa.twilio.model.iam

import com.dixa.twilio.model.TwilioStringValue

/** A short-lived JSON Web Token (JWT) used to authenticate Twilio client-side SDKs. It encodes the
  * target Twilio account, region, user identity, and a set of grants that define which Twilio
  * products and actions the token holder is permitted to use. Tokens have a maximum lifetime of 24
  * hours and must be signed with a Twilio API Key. More info:
  * https://www.twilio.com/docs/iam/access-tokens
  */
final case class AccessToken(token: String) extends TwilioStringValue {
  override def twilioString: String = token
}
