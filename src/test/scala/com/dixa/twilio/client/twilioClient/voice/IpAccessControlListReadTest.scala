package com.dixa.twilio.client.twilioClient.voice

import akka.NotUsed
import akka.stream.scaladsl.{Sink, Source}
import com.dixa.twilio.CommonFixtures
import com.dixa.twilio.client.messaging.TwilioClientMessaging
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.voice.IpAccessControlListReadRequestExecutor
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.voice.IpAccessControlList
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, absent, equalTo}

import java.time.{ZoneOffset, ZonedDateTime}

final class IpAccessControlListReadTest extends TwilioClientTest {
  private val baseUrl =
    s"/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/IpAccessControlLists.json"

  classOf[TwilioClientMessaging].getSimpleName when {

    "ask to read all IpAccessControlLists" should {
      "returned all of Twilios paginated results as a stream" in {

        val request = IpAccessControlListReadRequestExecutor.IpAccessControlListReadRequest.build(
          _.withAccountSid(CommonFixtures.accountSid1)
            .build()
        )

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(
                baseUrl
              )
            )
            .withQueryParam("PageToken", absent())
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse1)
            )
        )
        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(
                baseUrl
              )
            )
            .withQueryParam("PageToken", equalTo(nextPageToken))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse2)
            )
        )

        val expected = Seq(
          IpAccessControlList(
            CommonFixtures.accountSid1,
            IpAccessControlList.Sid.unsafe("ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1"),
            Some(IpAccessControlList.FriendlyName.unsafe("aaaa")),
            ZonedDateTime.of(2015, 7, 17, 21, 25, 15, 0, ZoneOffset.UTC).toInstant,
            ZonedDateTime.of(2015, 7, 17, 21, 25, 15, 0, ZoneOffset.UTC).toInstant
          ),
          IpAccessControlList(
            CommonFixtures.accountSid1,
            IpAccessControlList.Sid.unsafe("ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2"),
            Some(IpAccessControlList.FriendlyName.unsafe("aaaa")),
            ZonedDateTime.of(2015, 7, 17, 21, 25, 15, 0, ZoneOffset.UTC).toInstant,
            ZonedDateTime.of(2015, 7, 17, 21, 25, 15, 0, ZoneOffset.UTC).toInstant
          )
        )

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: IpAccessControlListReadRequestExecutor =
          TwilioClient.defaultImpl().voice.ipAccessControlListRead
        val resultSource: Source[
          Either[
            IpAccessControlListReadRequestExecutor.IpAccessControlListReadException,
            IpAccessControlList
          ],
          NotUsed
        ] = {
          instance.source(connSettings, request)
        }
        resultSource.runWith(Sink.seq).map { result =>
          val succResult = result.map(subResult =>
            subResult.getOrElse {
              val e = subResult.left.getOrElse(fail("No success or either, how can that happen :D"))
              fail("expected successfully result here", e)
            }
          )
          assert(succResult === expected)
        }
      }
    }
  }

  def nextPageToken = "PAAP43d194ab52aefee77ec51e4e185f3e38"

  private def twilioResponse1NextPageUri =
    s"$baseUrl?PageSize=1&Page=1&PageToken=$nextPageToken"

  private def twilioResponse1 =
    s"""{
       |  "first_page_uri": "$baseUrl?PageSize=1&Page=0",
       |  "end": 0,
       |  "previous_page_uri": null,
       |  "uri": "$baseUrl?PageSize=1&Page=0",
       |  "page_size": 1,
       |  "page": 0,
       |  "domains": [
       |    {
       |      "account_sid": "${CommonFixtures.accountSid1}",
       |      "date_created": "Fri, 17 Jul 2015 21:25:15 +0000",
       |      "date_updated": "Fri, 17 Jul 2015 21:25:15 +0000",
       |      "friendly_name": "aaaa",
       |      "sid": "ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1",
       |      "subresource_uris": {
       |        "ip_addresses": "/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/IpAccessControlLists/ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/IpAddresses.json"
       |      },
       |      "uri": "/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/IpAccessControlLists/ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1.json"
       |    }
       |  ],
       |  "next_page_uri": "$twilioResponse1NextPageUri",
       |  "start": 0
       |}
       |""".stripMargin

  private def twilioResponse2 =
    s"""{
       |  "first_page_uri": "$baseUrl?PageSize=1&Page=0",
       |  "end": 1,
       |  "previous_page_uri": "$baseUrl?PageSize=1&Page=0&PageToken=PBAP33f1531a9dadcd439bb8d2f06b0ebd1f",
       |  "uri": "$twilioResponse1NextPageUri",
       |  "page_size": 1,
       |  "page": 1,
       |  "domains": [
       |    {
       |      "account_sid": "${CommonFixtures.accountSid1}",
       |      "date_created": "Fri, 17 Jul 2015 21:25:15 +0000",
       |      "date_updated": "Fri, 17 Jul 2015 21:25:15 +0000",
       |      "friendly_name": "aaaa",
       |      "sid": "ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2",
       |      "subresource_uris": {
       |        "ip_addresses": "/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/IpAccessControlLists/ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2/IpAddresses.json"
       |      },
       |      "uri": "/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/IpAccessControlLists/ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2.json"
       |    }
       |  ],
       |  "next_page_uri": null,
       |  "start": 1
       |}
       |""".stripMargin

}
