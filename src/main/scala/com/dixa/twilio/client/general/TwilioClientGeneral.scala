package com.dixa.twilio.client.general

trait TwilioClientGeneral {

  /** Create a Usage Trigger.
    *
    * @see
    *   https://www.twilio.com/docs/usage/api/usage-trigger#create-a-usagetrigger-resource
    */
  def usageTriggerCreate: UsageTriggerCreateRequestExecutor

  /** Read all Usage Triggers.
    *
    * @see
    *   https://www.twilio.com/docs/usage/api/usage-trigger#read-multiple-usagetrigger-resources
    */
  def usageTriggerRead: UsageTriggerReadRequestExecutor

  /** Delete Usage Triggers.
    *
    * @see
    *   https://www.twilio.com/docs/usage/api/usage-trigger#delete-a-usagetrigger-resource
    */
  def usageTriggerDelete: UsageTriggerDeleteRequestExecutor

  /** Create an Application (TwimlApp).
    *
    * @see
    *   https://www.twilio.com/docs/usage/api/applications#create-an-application-resource
    */
  def applicationCreate: ApplicationCreateRequestExecutor

  /** Delete an Application (TwimlApp)
    *
    * If this application's sid is assigned to any IncomingPhoneNumber resources as a
    * VoiceApplicationSid or SmsApplicationSid it will be removed.
    *
    * @see
    *   https://www.twilio.com/docs/usage/api/applications#delete-an-application-resource
    */
  def applicationDelete: ApplicationDeleteRequestExecutor

  /** Read all applications (TwimlApps) from a subaccount.
    *
    * @see
    *   https://www.twilio.com/docs/usage/api/applications#read-multiple-application-resources
    */
  def applicationRead: ApplicationReadRequestExecutor
}
