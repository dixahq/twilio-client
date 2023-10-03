package com.dixa.twilio.model.iam

/** Encapsulate an account sid and a auth token in one type.
  *
  * This is not representing a Twilio entity as such, but is a combination often used in system to
  * represent credentials for accessing Twilio.
  *
  * @tparam A
  *   Type of AuthToken to encapsulate. If you don't care what type it is, then set this value to
  *   the base type of `AuthToken`
  */
final case class AccountSidAndAuthToken[+A <: AuthToken](
    accountSid: TwilioAccount.Sid,
    authToken: A
)
