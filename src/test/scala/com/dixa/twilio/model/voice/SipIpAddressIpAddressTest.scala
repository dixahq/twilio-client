package com.dixa.twilio.model.voice

import org.scalatest.wordspec.AnyWordSpec

final class SipIpAddressIpAddressTest extends AnyWordSpec {

  classOf[SipIpAddress.IpAddress].getSimpleName should {

    "return valid instance when created from a valid ipv4 value" in {
      val in       = "192.168.1.187"
      val instance = SipIpAddress.IpAddress.unsafe(in)
      assert(instance.toString == in)
    }

    "return error when value where last group is to high" in {
      val in     = "192.168.1.256"
      val result = SipIpAddress.IpAddress.safe(in)
      val e      = result.swap.getOrElse(fail("Expected error here"))
      assert(e == SipIpAddress.IpAddress.NotIpv4Exception(in))
    }
  }

}
