package com.dixa.twilio.model.messaging

import org.scalatest.wordspec.AnyWordSpec

final class MediaSidTest extends AnyWordSpec {

  classOf[MediaSid].getSimpleName should {

    "return its wrapped value in it's toString" in {
      val wrapped = "SomeSid"
      val a       = MediaSid(wrapped)
      assert(a.toString === wrapped)
    }
  }
}
