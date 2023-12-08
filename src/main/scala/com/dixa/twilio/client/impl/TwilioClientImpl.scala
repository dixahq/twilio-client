package com.dixa.twilio.client.impl

import org.apache.pekko.actor.ClassicActorSystemProvider
import org.apache.pekko.http.scaladsl.{Http, HttpExt}
import com.dixa.twilio.client.TwilioClient
import com.dixa.twilio.client.general.TwilioClientGeneral
import com.dixa.twilio.client.iam.TwilioClientIam
import com.dixa.twilio.client.impl.general.TwilioClientGeneralImpl
import com.dixa.twilio.client.impl.iam.TwilioClientIamImpl
import com.dixa.twilio.client.impl.messaging.TwilioClientMessagingImpl
import com.dixa.twilio.client.impl.phonenumber.TwilioClientPhoneNumberImpl
import com.dixa.twilio.client.impl.stunTurn.TwilioClientStunTurnImpl
import com.dixa.twilio.client.impl.voice.TwilioClientVoiceImpl
import com.dixa.twilio.client.messaging.TwilioClientMessaging
import com.dixa.twilio.client.phonenumber.TwilioClientPhoneNumber
import com.dixa.twilio.client.stunTurn.TwilioClientStunTurn
import com.dixa.twilio.client.voice.TwilioClientVoice

import scala.concurrent.ExecutionContext

/** Default implementation of a TwilioClient. */
private[client] final class TwilioClientImpl()(
    implicit actorSystem: ClassicActorSystemProvider
) extends TwilioClient {

  private implicit val executionContext: ExecutionContext = actorSystem.classicSystem.dispatcher

  private implicit val http: HttpExt = Http()

  override val iam: TwilioClientIam = new TwilioClientIamImpl()

  override val voice: TwilioClientVoice = new TwilioClientVoiceImpl()

  override val messaging: TwilioClientMessaging = new TwilioClientMessagingImpl()

  override val phoneNumber: TwilioClientPhoneNumber = new TwilioClientPhoneNumberImpl()

  override val general: TwilioClientGeneral = new TwilioClientGeneralImpl()

  override val stunTurn: TwilioClientStunTurn = new TwilioClientStunTurnImpl()
}
