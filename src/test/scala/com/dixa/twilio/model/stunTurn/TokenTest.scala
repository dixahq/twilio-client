package com.dixa.twilio.model.stunTurn

import com.dixa.twilio.CommonFixtures
import com.dixa.twilio.model.PositiveInteger
import org.scalatest.wordspec.AnyWordSpec

import java.time.Instant

final class TokenTest extends AnyWordSpec {

  classOf[Token].getSimpleName should {

    "not expose password in it's toString" in {

      val outerPasswordValue = "2ffe553b-76d4-4284-aad6-4e0296500515"
      val iceServerPassword  = "b3dd189c-c5ea-4436-945d-6b358c6e0f94"
      val instance = Token(
        Token.Username("testTokenUsername"),
        Token.Password(outerPasswordValue),
        PositiveInteger.unsafe(234),
        CommonFixtures.accountSid1,
        Seq(
          Token.IceServer(
            Token.IceServerUrl("some:testIceServer.url"),
            None,
            Some(Token.IceServerCredential(iceServerPassword))
          )
        ),
        Instant.EPOCH,
        Instant.EPOCH
      )
      val toStringOutput = instance.toString
      assert(!toStringOutput.contains(outerPasswordValue))
      assert(!toStringOutput.contains(iceServerPassword))
    }
  }
}
