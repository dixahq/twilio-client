package com.dixa.twilio.client.twilioClient.messaging

import akka.stream.scaladsl.Sink
import com.dixa.twilio.client.messaging.TwilioClientMessaging
import com.dixa.twilio.client.model.iam.TwilioAccount
import com.dixa.twilio.client.model.messaging.MediaResourceUrl.buildMediaResourcePath
import com.dixa.twilio.client.model.messaging.{MediaResourceReference, MediaSid, MessageSid}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import org.scalatest.matchers.should.Matchers

import java.time.Instant
import java.time.format.DateTimeFormatter

final class MediaResourceListTest extends TwilioClientTest with Matchers {

  private val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
  private val messageSid   = MessageSid("MM9c8a124127702f0c7084b373cb06157a")
  private val sid          = MediaSid("ME9ec380c03268689d63e8fc5e97bba86e")
  private val req = TwilioClientMessaging.MediaResourceReadRequest(
    messageSid = messageSid
  )
  private val path = buildMediaResourcePath(
    connSettings.accountSid,
    messageSid
  )

  private val createdAt = "Tue, 01 Feb 2022 13:44:20 +0000"
  private val updatedAt = "Wed, 02 Feb 2022 15:42:20 +0000"

  private val formatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss Z")

  private val createdAtInstant = Instant.from(formatter.parse(createdAt))
  private val updatedAtInstant = Instant.from(formatter.parse(updatedAt))

  classOf[TwilioClientMessaging].getSimpleName when {

    "mediaResourceList" should {

      "no media resources should turn into an empty list" in {

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(path)
            )
            .withBasicAuth(connSettings.accountSid.toString, "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(mediaResourceListResp(connSettings.accountSid, messageSid, List.empty))
            )
        )

        val instance = TwilioClient.defaultImpl().messaging
        val result =
          instance.mediaResourceRead(connSettings, req).runWith(Sink.seq)
        result.map { result =>
          result.isEmpty shouldBe true
        }
      }

      "lists a single media resource" in {
        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(path)
            )
            .withBasicAuth(connSettings.accountSid.toString, "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(mediaResourceListResp(connSettings.accountSid, messageSid, List(sid)))
            )
        )

        val expected = MediaResourceReference(
          sid,
          connSettings.accountSid,
          messageSid,
          contentType = "image/jpeg",
          dateCreated = createdAtInstant,
          dateUpdated = updatedAtInstant
        )

        val instance = TwilioClient.defaultImpl().messaging
        val result =
          instance.mediaResourceRead(connSettings, req).runWith(Sink.seq)
        result.map { result =>
          result.size shouldBe 1
          result.head shouldBe expected
          result.head.dateCreated.toString shouldBe "2022-02-01T13:44:20Z"
          result.head.dateUpdated.toString shouldBe "2022-02-02T15:42:20Z"
        }
      }

      "lists multiple of media resources" in {
        val sid2 = MediaSid("Sid2")
        val sid3 = MediaSid("Sid3")
        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(path)
            )
            .withBasicAuth(connSettings.accountSid.toString, "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(
                  mediaResourceListResp(connSettings.accountSid, messageSid, List(sid, sid2, sid3))
                )
            )
        )

        val expected = MediaResourceReference(
          sid,
          connSettings.accountSid,
          messageSid,
          contentType = "image/jpeg",
          dateCreated = createdAtInstant,
          dateUpdated = updatedAtInstant
        )

        val expected2 = MediaResourceReference(
          sid2,
          connSettings.accountSid,
          messageSid,
          contentType = "image/jpeg",
          dateCreated = createdAtInstant,
          dateUpdated = updatedAtInstant
        )

        val expected3 = MediaResourceReference(
          sid3,
          connSettings.accountSid,
          messageSid,
          contentType = "image/jpeg",
          dateCreated = createdAtInstant,
          dateUpdated = updatedAtInstant
        )

        val instance = TwilioClient.defaultImpl().messaging
        val result =
          instance.mediaResourceRead(connSettings, req).runWith(Sink.seq)
        result.map { result =>
          result.size shouldBe 3
          result should contain theSameElementsAs List(expected, expected2, expected3)
        }
      }
    }
  }

  def mediaResourceReferenceResp(
      accountSid: TwilioAccount.Sid,
      messageSid: MessageSid,
      sid: MediaSid
  ): String = {
    s"""{
       |             "sid": "$sid",
       |             "account_sid": "$accountSid",
       |             "parent_sid": "$messageSid",
       |             "content_type": "image/jpeg",
       |             "date_created": "$createdAt",
       |             "date_updated": "$updatedAt",
       |             "uri": "/2010-04-01/Accounts/$accountSid/Messages/$messageSid/Media/$sid.json"
       |         }""".stripMargin
  }

  private def mediaResourceListResp(
      accountSid: TwilioAccount.Sid,
      messageSid: MessageSid,
      sids: List[MediaSid]
  ) =
    s"""{
       |    "first_page_uri": "/2010-04-01/Accounts/$accountSid/Messages/$messageSid/Media.json?PageSize=1000&Page=0",
       |    "end": 0,
       |    "media_list": [
       |         ${sids.map(mediaResourceReferenceResp(accountSid, messageSid, _)).mkString(", ")}
       |    ],
       |    "previous_page_uri": null,
       |    "uri": "/2010-04-01/Accounts/$accountSid/Messages/$messageSid/Media.json?PageSize=1000&Page=0",
       |    "page_size": 1000,
       |    "start": 0,
       |    "next_page_uri": null,
       |    "page": 0
       |}
       |""".stripMargin
}
