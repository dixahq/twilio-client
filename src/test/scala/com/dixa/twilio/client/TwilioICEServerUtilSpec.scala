package com.dixa.twilio.client

import cats.effect.unsafe.implicits.global
import com.dixa.thrift.generated.{ICEServer, TelephonyAccount}
import com.twilio.sdk.TwilioRestClient
import com.twilio.sdk.resource.instance.{Account, Token}
import com.twilio.sdk.resource.list.TokenList
import org.apache.http.NameValuePair
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.{global => globalEc}
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Try

class TwilioICEServerUtilSpec extends AnyWordSpec with Matchers with MockFactory {
  def await[A](f: Future[A]): A = scala.concurrent.Await.result(f, 5 seconds)

  "TwilioICEServerUtil" should {
    "Create a token and filter out non-tcp-443 servers" in {
      val telephonyAccount = TelephonyAccount("sid", "token")
      val clientFactory    = mock[TwilioRestClientFactory]
      (clientFactory
        .createClient(_: TelephonyAccount))
        .expects(telephonyAccount)
        .returning {
          new TwilioRestClient(
            telephonyAccount.twilioAccountSid,
            telephonyAccount.twilioAuthToken
          ) {
            private val self: TwilioRestClient = this
            private val iceServers: java.util.List[java.util.Map[String, String]] = Seq(
              Map(
                "url" -> "stun:global.stun.twilio.com:3478?transport=udp"
              ).asJava,
              Map(
                "url"        -> "turn:global.turn.twilio.com:3478?transport=tcp",
                "username"   -> "username",
                "credential" -> "credential"
              ).asJava,
              Map(
                "url"        -> "turn:global.turn.twilio.com:443?transport=tcp",
                "username"   -> "tcp-username",
                "credential" -> "tcp-password"
              ).asJava
            ).asJava

            private val tokenProperties: Map[String, Object] = Map(
              "username"    -> "481d2195b16752e96c03f2f07ed36dbae6c19998978a0787df92f8a574796cd9",
              "credential"  -> "iTKdKBgMQsKfUgP41OGUqJWd5XuXT2HaBYwjxxNWaeM",
              "ice_servers" -> iceServers
            )

            override def getAccount: Account =
              new Account(self, new java.util.HashMap[String, Object]) {
                override def getTokenFactory: TokenList = new TokenList(self) {
                  override def create(params: java.util.List[NameValuePair]): Token =
                    new Token(self, tokenProperties.asJava)
                }
              }
          }
        }

      val util      = new TwilioICEServerUtil(clientFactory)
      val serverSet = await(util.createTurnServerCredentials(telephonyAccount).unsafeToFuture())

      serverSet.size shouldBe 1
      serverSet.headOption shouldEqual Some(
        ICEServer(
          url = "turn:global.turn.twilio.com:443?transport=tcp",
          username = Some("tcp-username"),
          password = Some("tcp-password")
        )
      )
    }

    "Not break on null credentials" in {
      val telephonyAccount = TelephonyAccount("sid", "token")
      val clientFactory    = mock[TwilioRestClientFactory]
      (clientFactory
        .createClient(_: TelephonyAccount))
        .expects(telephonyAccount)
        .returning {
          new TwilioRestClient(
            telephonyAccount.twilioAccountSid,
            telephonyAccount.twilioAuthToken
          ) {
            private val self: TwilioRestClient = this
            private val iceServers: java.util.List[java.util.Map[String, String]] = Seq(
              Map(
                "url"        -> "turn:url:443?transport=tcp",
                "username"   -> null,
                "credential" -> "credential"
              ).asJava,
              Map(
                "url"        -> "turn:url:443?transport=tcp",
                "username"   -> "username",
                "credential" -> null
              ).asJava
            ).asJava

            private val tokenProperties: Map[String, Object] = Map(
              "username"    -> "username",
              "credential"  -> "credential",
              "ice_servers" -> iceServers
            )

            override def getAccount: Account =
              new Account(self, new java.util.HashMap[String, Object]) {
                override def getTokenFactory: TokenList = new TokenList(self) {
                  override def create(params: java.util.List[NameValuePair]): Token =
                    new Token(self, tokenProperties.asJava)
                }
              }
          }
        }

      val util      = new TwilioICEServerUtil(clientFactory)
      val serverSet = await(util.createTurnServerCredentials(telephonyAccount).unsafeToFuture())

      serverSet.size shouldBe 2
      serverSet should contain allElementsOf Seq(
        ICEServer(
          url = "turn:url:443?transport=tcp",
          username = None,
          password = Some("credential")
        ),
        ICEServer(
          url = "turn:url:443?transport=tcp",
          username = Some("username"),
          password = None
        )
      )
    }

  }

  "GetServers from Token" should {
    "Map each each map entry to ICEServer" in {

      // given
      val mockToken = stub[Token]
      val rawToken = List(
        Map("url" -> "url123", "username" -> "user123", "credential" -> "credential123123").asJava
      ).asJava
      (mockToken.getObject _).when("ice_servers").returns(rawToken)

      // when
      val servers = TwilioICEServerUtil.getServers(mockToken)

      // then
      servers shouldBe Set(ICEServer("url123", Some("user123"), Some("credential123123")))
    }

    "correctly deal with null username and credentials values" in {
      // given
      val mockToken = stub[Token]
      val rawToken =
        List(Map("url" -> "url123", "username" -> null, "credential" -> null).asJava).asJava
      (mockToken.getObject _).when("ice_servers").returns(rawToken)

      // when
      val servers = TwilioICEServerUtil.getServers(mockToken)

      // then
      servers shouldBe Set(ICEServer("url123", None, None))
    }

    "throws an IllegalArgumentException if url server is not present or null" in {
      // given
      val mockToken = stub[Token]
      val tokenWithEmptyServerUrl =
        List(Map("username" -> null, "credential" -> null).asJava).asJava
      val tokenWithNullServerUrl = List(Map("username" -> null, "credential" -> null).asJava).asJava
      (mockToken.getObject _).when("ice_servers").returns(tokenWithEmptyServerUrl)
      (mockToken.getObject _).when("ice_servers").returns(tokenWithNullServerUrl)

      // when
      val emptyUrlServersTry = Try(TwilioICEServerUtil.getServers(mockToken))
      val nullUrlServersTry  = Try(TwilioICEServerUtil.getServers(mockToken))

      // then
      emptyUrlServersTry.isFailure shouldBe true
      emptyUrlServersTry.failed.get shouldBe an[IllegalArgumentException]

      // and
      nullUrlServersTry.isFailure shouldBe true
      nullUrlServersTry.failed.get shouldBe an[IllegalArgumentException]
    }
  }
}
