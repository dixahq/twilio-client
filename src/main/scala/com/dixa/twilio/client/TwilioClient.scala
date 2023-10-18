package com.dixa.twilio.client

import akka.actor.ClassicActorSystemProvider
import com.dixa.twilio.client.general.TwilioClientGeneral
import com.dixa.twilio.client.iam.TwilioClientIam
import com.dixa.twilio.client.impl.TwilioClientImpl
import com.dixa.twilio.client.messaging.TwilioClientMessaging
import com.dixa.twilio.client.phonenumber.TwilioClientPhoneNumber
import com.dixa.twilio.client.voice.TwilioClientVoice

/** Main entry point for communicating with twilio.
  *
  * You can get an instance of this via [[TwilioClient.defaultImpl]]
  *
  * It does not in itself provide any twilio calls directly, but instead provide access to a sub
  * client for each of Twilios sub APIs.
  *
  * The sub clients then provide methods for performing calls against twilio. Such request can be of
  * two different types:
  *
  * 1) A standard request. A method for this will return an implementation of
  * [[SingleRequestExecutor]]. Look at the documentation of that trait for more details.
  *
  * 2) A paging request. At time of writing, we have build a proper abstraction for representing
  * these, but for now they just return a Source[ResponseType, NotUser]. But this is subject to
  * change, as we should find a general abstraction for these type of calls, that both handles
  * errors properly, and also gives the client the possibility to actually use paging if needed.
  */
trait TwilioClient {

  /** Return sub client for the Twilio IAM API */
  def iam: TwilioClientIam

  /** Return sub client for the Twilio voice API */
  def voice: TwilioClientVoice

  /** Return sub client for the Twilio messaging API
    */
  def messaging: TwilioClientMessaging

  /** Return sub client for the Twilio phoneNumber API */
  def phoneNumber: TwilioClientPhoneNumber

  /** Return sub client for the Twilio general API */
  def general: TwilioClientGeneral

}

object TwilioClient {
  def defaultImpl()(
      implicit actorSystem: ClassicActorSystemProvider
  ): TwilioClient = new TwilioClientImpl()
}
