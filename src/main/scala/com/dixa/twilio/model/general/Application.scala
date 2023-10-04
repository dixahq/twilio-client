package com.dixa.twilio.model.general

import com.dixa.twilio.model.iam.TwilioAccount

import java.time.Instant

/** Represent an Application resource. Also sometimes called "TwiML Application" or "TwiML App"
  *
  * An Application Resource (also referred to as a "TwiML Application" or "TwiML App") represents a
  * collection of endpoints that return TwiML instructions to Twilio. TwiML Applications are most
  * commonly used for the Voice SDKs to handle outbound calls, but can also be used to configure
  * multiple phone numbers with the same set of TwiML endpoints.
  *
  * The Applications list resource represents the set of an account's Twilio applications. You can
  * POST to the list resource to create a new application. Note that accounts can contain at most
  * 1000 applications.
  *
  * Applications are useful for encapsulating configuration information that you need to distribute
  * across multiple phone numbers. You can assign an ApplicationSid to an IncomingPhoneNumber to
  * tell Twilio to use the application's URLs instead of the ones set directly on the
  * IncomingPhoneNumber. So if you create an application with its VoiceUrl set to
  * http://myapp.com/answer, you can assign that application to all of your phone numbers and Twilio
  * will make a request to that URL whenever a call comes in.
  *
  * @see https://www.twilio.com/docs/usage/api/applications#create-an-application-resource
  */
final case class Application(
    accountSid: TwilioAccount.Sid,
    dateCreated: Instant,
    dateUpdated: Instant,
    friendlyName: Application.FriendlyName
                            ) {}

object Application {

  final case class FriendlyName(override val toString: String) extends AnyVal
}