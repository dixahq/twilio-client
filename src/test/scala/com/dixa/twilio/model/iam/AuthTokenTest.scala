package com.dixa.twilio.model.iam
import org.scalatest.wordspec.AnyWordSpec

final class AuthTokenTest extends AnyWordSpec {

  classOf[AuthToken].getSimpleName should {

    "have a primary subclass" in {
      val primary: AuthToken.Primary = AuthToken.Primary("primaryToken")
      assert(primary.asString === "primaryToken")
      assert(primary.isInstanceOf[AuthToken])
    }

    "have a secondary subclass" in {
      val secondary: AuthToken.Secondary = AuthToken.Secondary("secondaryToken")
      assert(secondary.asString === "secondaryToken")
      assert(secondary.isInstanceOf[AuthToken])
    }

    "be extractable as a AuthToken when its a Primary instance" in {
      val primary: AuthToken.Primary = AuthToken.Primary("primaryToken")
      primary match {
        case AuthToken(s) => assert(s === "primaryToken")
      }
    }

    "be extractable as a Primary instance when that is what it is" in {
      val primary: AuthToken.Primary = AuthToken.Primary("primaryToken")
      primary match {
        case AuthToken.Primary(s) => assert(s === "primaryToken")
      }
    }

    "be extractable as a AuthToken when its a Secondary instance" in {
      val secondary: AuthToken.Secondary = AuthToken.Secondary("secondaryToken")
      secondary match {
        case AuthToken(s) => assert(s === "secondaryToken")
      }
    }

    "be extracable as a Secondary instance when that is what it is" in {
      val secondary: AuthToken.Secondary = AuthToken.Secondary("secondaryToken")
      secondary match {
        case AuthToken.Secondary(s) => assert(s === "secondaryToken")
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
  }
}
