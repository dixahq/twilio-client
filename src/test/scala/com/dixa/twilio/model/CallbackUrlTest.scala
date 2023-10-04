package com.dixa.twilio.model

import com.dixa.twilio.model.callback.CallbackUrl
import org.scalatest.wordspec.AnyWordSpec

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
  }

}
