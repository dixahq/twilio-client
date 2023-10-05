package com.dixa.twilio.client.general

trait TwilioClientGeneral {

  /** Read all Usage Triggers.
    *
    * @see
    *   https://www.twilio.com/docs/usage/api/usage-trigger#read-multiple-usagetrigger-resources
    */
  def usageTriggerRead: UsageTriggerReadRequestExecutor
}
