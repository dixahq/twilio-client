package com.dixa.twilio.client.twilioClient.messaging

import akka.stream.scaladsl.Sink
import com.dixa.twilio.CommonFixtures.AccountSid
import com.dixa.twilio.client.impl.messaging.MediaResourceUrlFactory
import com.dixa.twilio.client.messaging.{MessageResourceReadSource, TwilioClientMessaging}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioConnectionSettings, TwilioTestConstants}
import com.dixa.twilio.model.Iso4127CountryCode
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.messaging.{
  MediaResourceReference,
  MediaResourceUrl,
  MediaSid,
  MessageBody,
  MessageDirection,
  MessageNumSegments,
  MessagePrice,
  MessageResource,
  MessageSender,
  MessageSid,
  MessageStatus,
  ServiceSid
}
import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import org.scalatest.matchers.should.Matchers

import java.time._

final class MessageResourceListTest extends TwilioClientTest with Matchers {

  import MessageResourceListTest._

  classOf[TwilioClientMessaging].getSimpleName when {

    val connectionSettings = connSettings(wireMockServer.port())
    val accountSid         = connectionSettings.accountSid

    "mediaResourceList" should {

      "no message resources should turn into an empty list" in {

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(path(accountSid))
            )
            .withBasicAuth(connectionSettings.accountSid.toString, "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(messageResourceListResp(accountSid, List.empty))
            )
        )

        val instance = TwilioClient.defaultImpl().messaging
        val result =
          instance.messageResourceRead.source(connectionSettings, req(accountSid)).runWith(Sink.seq)
        result.map { result =>
          println(s"result: $result")
          result.isEmpty shouldBe true
        }
      }

      "lists a single media resource" in {

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(path(accountSid))
            )
            .withBasicAuth(connectionSettings.accountSid.toString, "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(messageResourceListResp(accountSid, List(messageResource(accountSid))))
            )
        )

        val expected = messageResource(accountSid)

        val instance = TwilioClient.defaultImpl().messaging
        val result =
          instance.messageResourceRead.source(connectionSettings, req(accountSid)).runWith(Sink.seq)
        result.map { result =>
          println(result)
          result.size shouldBe 1
          result.head.isRight shouldBe true
          result.head.right.get shouldBe expected
          result.head.right.get.dateCreated.toString shouldBe "2022-02-01T13:44:20Z"
          result.head.right.get.dateUpdated.toString shouldBe "2022-02-02T15:42:20Z"
          result.head.right.get.body.toString shouldBe "testing"
        }
      }

//      "lists multiple of media resources" in {
//        val sid2 = MediaSid("Sid2")
//        val sid3 = MediaSid("Sid3")
//        wireMockServer.stubFor(
//          WireMock
//            .get(
//              WireMock.urlPathEqualTo(path)
//            )
//            .withBasicAuth(connSettings.accountSid.toString, "testPassword")
//            .willReturn(
//              aResponse()
//                .withStatus(200)
//                .withBody(
//                  messageResourceListResp(
//                    connSettings.accountSid,
//                    messageSid,
//                    List(sid, sid2, sid3)
//                  )
//                )
//            )
//        )
//
//        val expected = MediaResourceReference(
//          sid,
//          connSettings.accountSid,
//          messageSid,
//          contentType = "image/jpeg",
//          dateCreated = createdAtInstant,
//          dateUpdated = updatedAtInstant,
//          MediaResourceUrl(
//            s"http://localhost/2010-04-01/Accounts/${connSettings.accountSid}/Messages/$messageSid/Media/$sid"
//          )
//        )
//
//        val expected2 = MediaResourceReference(
//          sid2,
//          connSettings.accountSid,
//          messageSid,
//          contentType = "image/jpeg",
//          dateCreated = createdAtInstant,
//          dateUpdated = updatedAtInstant,
//          MediaResourceUrl(
//            s"http://localhost/2010-04-01/Accounts/${connSettings.accountSid}/Messages/$messageSid/Media/$sid2"
//          )
//        )
//
//        val expected3 = MediaResourceReference(
//          sid3,
//          connSettings.accountSid,
//          messageSid,
//          contentType = "image/jpeg",
//          dateCreated = createdAtInstant,
//          dateUpdated = updatedAtInstant,
//          MediaResourceUrl(
//            s"http://localhost/2010-04-01/Accounts/${connSettings.accountSid}/Messages/$messageSid/Media/$sid3"
//          )
//        )
//
//        val instance = TwilioClient.defaultImpl().messaging
//        val result =
//          instance.mediaResourceRead(connSettings, req).runWith(Sink.seq)
//        result.map { result =>
//          result.size shouldBe 3
//          result should contain theSameElementsAs List(expected, expected2, expected3)
//        }
//      }
    }
  }
}

private object MessageResourceListTest {
  private def connSettings(port: Int) = TwilioTestConstants.connSettings(port)

  private def path(accountSid: TwilioAccount.Sid) =
    s"/2010-04-01/Accounts/$accountSid/Messages.json"

  private val messageSid = MessageSid("MM9c8a124127702f0c7084b373cb06157a")

  val filter = MessageResourceReadSource.MessageResourcesReadRequestFilter(
    to = None,
    from = None,
    dateSentAfter = None,
    dateSentBefore = None,
    pageSize = 1000
  )
  def req(accountSid: TwilioAccount.Sid) = MessageResourceReadSource.MessageResourceReadRequest(
    accountSid = accountSid,
    filter = filter
  )

  private val createdAt = "Tue, 01 Feb 2022 13:44:20 +0000"
  private val updatedAt = "Wed, 02 Feb 2022 15:42:20 +0000"
  private val dateSent  = "Wed, 02 Feb 2022 16:42:20 +0000"

  private val createdAtInstant = Instant.from(
    OffsetDateTime.of(
      LocalDateTime.of(LocalDate.of(2022, 2, 1), LocalTime.of(13, 44, 20)),
      ZoneOffset.UTC
    )
  )
  private val updatedAtInstant = Instant.from(
    OffsetDateTime.of(
      LocalDateTime.of(LocalDate.of(2022, 2, 2), LocalTime.of(15, 42, 20)),
      ZoneOffset.UTC
    )
  )
  private val dateSentInstant = Instant.from(
    OffsetDateTime.of(
      LocalDateTime.of(LocalDate.of(2022, 2, 2), LocalTime.of(16, 42, 20)),
      ZoneOffset.UTC
    )
  )

  private val direction      = MessageDirection.OutboundApi
  private val sender         = MessageSender.Alphanumeric("+12019235161")
  private val serviceSid     = ServiceSid("MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
  private val numberSegments = MessageNumSegments.apply(1)
  private val price = MessagePrice(
    BigDecimal(0.234324),
    Iso4127CountryCode("DKK")
  )
  private val receiver = PhoneNumberE164("+12019235100")

  private def messageResource(accountSid: TwilioAccount.Sid) = MessageResource(
    sid = messageSid,
    dateCreated = Some(createdAtInstant),
    dateUpdated = Some(updatedAtInstant),
    dateSent = Some(dateSentInstant),
    accountSid = accountSid,
    to = receiver,
    from = sender,
    messagingServiceSid = Some(serviceSid),
    body = MessageBody("testing"),
    status = MessageStatus.Sent,
    numSegments = numberSegments,
    numMedia = 0,
    direction = direction,
    price = Some(price),
    error = None
  )

  def messageResourceReferenceResp(
      accountSid: TwilioAccount.Sid,
      messageResource: MessageResource
  ): String = {
    s"""{
       |  "account_sid": "${messageResource.accountSid}",
       |  "api_version": "2010-04-01",
       |  "body": "${messageResource.body}",
       |  "date_created": "${messageResource.dateCreated}",
       |  "date_sent": "${messageResource.dateSent}",
       |  "date_updated": "${messageResource.dateUpdated}",
       |  "direction": "${messageResource.direction.twilioString}",
       |  "from": "${messageResource.from}",
       |  "messaging_service_sid": "${messageResource.messagingServiceSid}",
       |  "num_media": "${messageResource.numMedia}",
       |  "num_segments": "${messageResource.numSegments}",
       |  "price": "${messageResource.price.map(_.amount)}",
       |  "price_unit": "${messageResource.price.map(_.unit)}",
       |  "sid": "${messageResource.sid}",
       |  "status": "${messageResource.status.twilioString}",
       |  "to": "${messageResource.to}",
       |  "uri": "/2010-04-01/Accounts/$accountSid/Messages/${messageResource.sid}.json"
       |}""".stripMargin
  }

  private def messageResourceListResp(
      accountSid: TwilioAccount.Sid,
      messages: List[MessageResource]
  ): String =
    s"""{
       |    "first_page_uri": "/2010-04-01/Accounts/$accountSid/Messages.json?PageSize=1000&Page=0",
       |    "end": 0,
       |    "messages": [
       |         ${messages.map(messageResourceReferenceResp(accountSid, _)).mkString(", ")}
       |    ],
       |    "previous_page_uri": null,
       |    "uri": "/2010-04-01/Accounts/$accountSid/Messages.json?PageSize=1000&Page=0",
       |    "page_size": 1000,
       |    "start": 0,
       |    "next_page_uri": null,
       |    "page": 0
       |}
       |""".stripMargin
}
