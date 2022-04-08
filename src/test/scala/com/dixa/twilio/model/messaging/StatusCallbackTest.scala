package com.dixa.twilio.model.messaging

import org.scalatest.wordspec.AnyWordSpec

import java.net.URL

final class StatusCallbackTest extends AnyWordSpec {

  classOf[StatusCallback].getSimpleName should {

    "should print the urls string representation in it's toString" in {
      val urlAsString = "http://localhost:8347/CallbackPath"
      val a           = StatusCallback(new URL(urlAsString))
      assert(a.toString === urlAsString)
    }
  }
}
