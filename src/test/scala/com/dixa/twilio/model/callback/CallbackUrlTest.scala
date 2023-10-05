package com.dixa.twilio.model.callback

import com.dixa.twilio.model.callback.CallbackUrl.MessagingStatusCallback
import org.scalatest.wordspec.AnyWordSpec

import java.net.URL

final class CallbackUrlTest extends AnyWordSpec {

  classOf[CallbackUrl].getSimpleName should {

    "support extracting url as String in pattern matching" in {

      val s        = "http://localhost/some/callback/url"
      val instance = CallbackUrl(s)
      instance match {
        case CallbackUrl(s2) =>
          assert(s2 == s)
        case _ => fail("wrong type")
      }
    }

    "toString should just return the represented url" in {
      val s        = "http://localhost/some/callback/url"
      val instance = CallbackUrl(s)
      assert(instance.toString == s)
    }

    "MessagingStatusCallback subtype should print the urls string representation in it's toString" in {
      val urlAsString = "http://localhost:8347/CallbackPath"
      val a           = MessagingStatusCallback(new URL(urlAsString))
      assert(a.toString === urlAsString)
    }
  }

}
