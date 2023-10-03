package com.dixa.twilio.model.iam
import com.dixa.twilio.client.TwilioTestConstants
import org.scalatest.wordspec.AnyWordSpec

final class AuthTokenTest extends AnyWordSpec {

  classOf[AuthToken].getSimpleName should {

    "have a primary subclass that is also a instance of KnownType" in {
      val primary: AuthToken.Primary = AuthToken.Primary("primaryToken")
      assert(primary.asString === "primaryToken")
      assert(primary.isInstanceOf[AuthToken.KnownType])
      assert(primary.isInstanceOf[AuthToken])
    }

    "have a secondary subclass that is also a instance of KnownType" in {
      val secondary: AuthToken.Secondary = AuthToken.Secondary("secondaryToken")
      assert(secondary.asString === "secondaryToken")
      assert(secondary.isInstanceOf[AuthToken.KnownType])
      assert(secondary.isInstanceOf[AuthToken])
    }

    "have a UnknownType subclass, that is not an instance of KnownType" in {
      val unknownType: AuthToken.UnknownType = AuthToken.UnknownType("anAuthToken")
      assert(unknownType.asString === "anAuthToken")
      assertTypeError("val as: AuthToken.KnownType = unknownType")
      assert(unknownType.isInstanceOf[AuthToken])
    }

    "be extractable as a AuthToken when its a Primary instance" in {
      val primary: AuthToken.Primary = AuthToken.Primary("primaryToken")
      primary match {
        case AuthToken(s) => assert(s === "primaryToken")
        case _            => fail()
      }
    }

    "be extractable as a Primary instance when that is what it is" in {
      val primary: AuthToken.Primary = AuthToken.Primary("primaryToken")
      primary match {
        case AuthToken.Primary(s) => assert(s === "primaryToken")
        case _                    => fail()
      }
    }

    "be extractable as a AuthToken when its a Secondary instance" in {
      val secondary: AuthToken.Secondary = AuthToken.Secondary("secondaryToken")
      secondary match {
        case AuthToken(s) => assert(s === "secondaryToken")
        case _            => fail()
      }
    }

    "be extracable as a Secondary instance when that is what it is" in {
      val secondary: AuthToken.Secondary = AuthToken.Secondary("secondaryToken")
      secondary match {
        case AuthToken.Secondary(s) => assert(s === "secondaryToken")
        case _                      => fail()
      }
    }

    "be extractable as a AuthToken when its a UnknownType instance" in {
      val unknownType: AuthToken.UnknownType = AuthToken.UnknownType("anAuthToken")
      unknownType match {
        case AuthToken(s) => assert(s === "anAuthToken")
        case _            => fail()
      }
    }

    "be extracable as a UnknownType instance when that is what it is" in {
      val unknownType: AuthToken.UnknownType = AuthToken.UnknownType("anAuthToken")
      unknownType match {
        case AuthToken.UnknownType(s) => assert(s === "anAuthToken")
        case _                        => fail()
      }
    }

    "hide the actual value in its toString when it is a Primary instance" in {
      val primary = AuthToken.Primary("primaryToken")
      assert(primary.toString === "AuthToken.Primary(***)")
    }

    "hide the actual value in its toString when it is a Secondary instance" in {
      val secondary = AuthToken.Secondary("secondaryToken")
      assert(secondary.toString === "AuthToken.Secondary(***)")
    }

    "hide the actual value in its toString when it is a UnknownType instance" in {
      val unknownType = AuthToken.UnknownType("secondaryToken")
      assert(unknownType.toString === "AuthToken.UnknownType(***)")
    }

    "have inner type representing meta information" in {
      val m: AuthToken.MetaData = AuthToken.MetaData(
        accountSid = TwilioTestConstants.accountSid,
        createdTime = TwilioTestConstants.createdTime,
        updatedTime = TwilioTestConstants.updatedTime
      )
      assert(m.accountSid === TwilioTestConstants.accountSid)
      assert(m.createdTime === TwilioTestConstants.createdTime)
      assert(m.updatedTime === TwilioTestConstants.updatedTime)
    }

    "have a wrapper class containing an auth token and it's metadata" in {
      val t: AuthToken.Primary = AuthToken.Primary("token")
      val m: AuthToken.MetaData = AuthToken.MetaData(
        accountSid = TwilioTestConstants.accountSid,
        createdTime = TwilioTestConstants.createdTime,
        updatedTime = TwilioTestConstants.updatedTime
      )
      val w: AuthToken.AuthTokenAndMetaData[AuthToken.Primary] =
        AuthToken.AuthTokenAndMetaData(t, m)
      assert(w.authToken === t)
      assert(w.metaData === m)
    }
  }
}
