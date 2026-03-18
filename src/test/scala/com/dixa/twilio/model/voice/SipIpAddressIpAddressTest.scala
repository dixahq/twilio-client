// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
