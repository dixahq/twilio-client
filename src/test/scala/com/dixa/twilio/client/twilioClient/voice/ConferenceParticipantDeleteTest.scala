package com.dixa.twilio.client.twilioClient.voice

import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.voice.ConferenceParticipantDeleteRequestExecutor.ConferenceParticipantDeleteRequest
import com.dixa.twilio.client.voice.TwilioClientVoice
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.Funit
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.{Call, Conference}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import org.scalatest.matchers.should.Matchers

final class ConferenceParticipantDeleteTest extends TwilioClientTest with Matchers {
  classOf[TwilioClientVoice].getSimpleName when {

    "conferenceParticipantDelete" should {
      "safely delete a single participant from a conference" in {
        wireMockServer.stubFor(
          WireMock
            .delete(
              WireMock.urlPathEqualTo(
                "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Conference/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Participant/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json"
              )
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(204)
            )
        )

        val twilioConnectionSetting     = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: TwilioClientVoice = TwilioClient.defaultImpl().voice
        val req =
          ConferenceParticipantDeleteRequest.build(
            _.withAccountSid(TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"))
              .withConferenceSid(Conference.Sid.unsafe("CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"))
              .withCallSid(Call.Sid.unsafe("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"))
              .build()
          )

        val resultFut = instance.conferenceParticipantDelete.run(twilioConnectionSetting, req)

        resultFut.map(res => assert(res === Right(Funit)))
      }
    }
  }
}
