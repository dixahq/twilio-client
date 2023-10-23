package com.dixa.twilio.client.twilioClient.voice

import com.dixa.twilio.CommonFixtures
import com.dixa.twilio.client.messaging.TwilioClientMessaging
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.voice.IpAccessControlListCreateRequestExecutor
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.voice.IpAccessControlList
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.time.{ZoneOffset, ZonedDateTime}
import scala.concurrent.Future

final class IpAccessControlListCreateTest extends TwilioClientTest {
  classOf[TwilioClientMessaging].getSimpleName when {

    "ask to create an IpAccessControlList" should {

      "ask twilio to create it, and return the IpAccessControlList it gets back from Twilio" in {

        val request =
          IpAccessControlListCreateRequestExecutor.IpAccessControlListCreateRequest.build(
            _.withAccountSid(CommonFixtures.accountSid1)
              .withFriendlyName(IpAccessControlList.FriendlyName.unsafe("Unit test ACL"))
              .build()
          )

        wireMockServer.stubFor(
          WireMock
            .post(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/IpAccessControlLists.json"
              )
            )
            .withRequestBody(WireMock.containing(s"FriendlyName=Unit+test+ACL"))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
            .willReturn(
              aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse1)
            )
        )
        val expected = IpAccessControlList(
          CommonFixtures.accountSid1,
          IpAccessControlList.Sid.unsafe("ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
          Some(IpAccessControlList.FriendlyName.unsafe("Unit test ACL")),
          ZonedDateTime.of(2015, 7, 17, 21, 25, 15, 0, ZoneOffset.UTC).toInstant,
          ZonedDateTime.of(2015, 7, 17, 22, 25, 15, 0, ZoneOffset.UTC).toInstant
        )

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: IpAccessControlListCreateRequestExecutor =
          TwilioClient.defaultImpl().voice.ipAccessControlListCreate
        val resultFut: Future[
          Either[
            IpAccessControlListCreateRequestExecutor.IpAccessControlListCreateException,
            IpAccessControlList
          ]
        ] = {
          instance.run(connSettings, request)
        }
        resultFut.map { result =>
          val succResult = result.getOrElse {
            val e = result.left.getOrElse(fail("No success or either, how can that happen :D"))
            fail("expected successfully result here", e)
          }
          assert(succResult === expected)
        }
      }
    }
  }

  private def twilioResponse1 =
    s"""{
       |  "account_sid": "${CommonFixtures.accountSid1}",
       |  "date_created": "Fri, 17 Jul 2015 21:25:15 +0000",
       |  "date_updated": "Fri, 17 Jul 2015 22:25:15 +0000",
       |  "friendly_name": "Unit test ACL",
       |  "sid": "ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
       |  "subresource_uris": {
       |    "ip_addresses": "/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/IpAccessControlLists/ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/IpAddresses.json"
       |  },
       |  "uri": "/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/IpAccessControlLists/ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json"
       |}
       |""".stripMargin
}
