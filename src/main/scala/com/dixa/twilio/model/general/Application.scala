package com.dixa.twilio.model.general

import com.dixa.twilio.model.{HttpMethod, SidAbstract}
import com.dixa.twilio.model.SidAbstract.{Prefix, SidCompanionObject}
import com.dixa.twilio.model.callback.CallbackUrl.{
  ApplicationStatusCallback,
  MessageStatusCallback,
  SmsFallbackUrl,
  SmsStatusCallback,
  SmsUrl,
  VoiceFallbackUrl,
  VoiceUrl
}
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
  * @see
  *   https://www.twilio.com/docs/usage/api/applications#create-an-application-resource
  */
final case class Application(
    accountSid: TwilioAccount.Sid,
    dateCreated: Instant,
    dateUpdated: Instant,
    friendlyName: Application.FriendlyName,
    messageStatusCallback: MessageStatusCallback,
    sid: Application.Sid,
    smsFallbackMethod: HttpMethod,
    smsFallbackUrl: SmsFallbackUrl,
    smsMethod: HttpMethod,
    smsStatusCallback: SmsStatusCallback,
    smsUrl: SmsUrl,
    statusCallback: ApplicationStatusCallback,
    statusCallbackMethod: HttpMethod,
    voiceCallerIdLookup: Boolean,
    voiceFallbackMethod: HttpMethod,
    voiceFallbackUrl: VoiceFallbackUrl,
    voiceMethod: HttpMethod,
    voiceUrl: VoiceUrl,
    publicApplicationConnectEnabled: Boolean
) {}

object Application {

  /** Represent a Twilio Application SID
    *
    * Input must apply to the format that Twilio specify as a Application SID: "It is a 34 character
    * string that starts with AP"
    *
    * @see
    *   https://support.twilio.com/hc/en-us/articles/223136607-What-is-an-Application-SID-
    */
  final case class Sid private[Application] (override val toString: String) extends SidAbstract

  object Sid extends SidCompanionObject(List(Prefix("AP")), new Sid(_))

  final case class FriendlyName(override val toString: String) extends AnyVal
}
