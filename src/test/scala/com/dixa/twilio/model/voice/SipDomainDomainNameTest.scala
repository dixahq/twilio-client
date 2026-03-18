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

final class SipDomainDomainNameTest extends AnyWordSpec {

  classOf[SipDomain.DomainName].getSimpleName should {

    "return an an Left if created with null value" in {

      val result = SipDomain.DomainName.safe(null)
      assert(result == Left(SipDomain.DomainName.NullValueException()))
    }

    "or throw an exception if created with null via the unsafe variant" in {
      intercept[SipDomain.DomainName.NullValueException](
        SipDomain.DomainName.unsafe(null)
      )
    }

    "return an Left if created with a value that is not ending in .sip.twilio.com" in {
      val result = SipDomain.DomainName.safe("validChars.but.wrong.suffix")
      assert(
        result == Left(
          SipDomain.DomainName
            .InvalidSuffixException("validChars.but.wrong.suffix", ".sip.twilio.com")
        )
      )
    }

    "or throw an created with a value that is not ending in via the unsafe method" in {
      intercept[SipDomain.DomainName.InvalidSuffixException](
        SipDomain.DomainName.unsafe("validChars.but.wrong.suffix")
      )
    }

    "return an Left if created with a value that contains invalid chars" in {
      val result = SipDomain.DomainName.safe("invalidChar?.sip.twilio.com")
      assert(
        result == Left(
          SipDomain.DomainName
            .InvalidCharException("invalidChar?.sip.twilio.com", SipDomain.DomainName.validChars)
        )
      )
    }

    "or throw an created with a value containing invalid chars via the unsafe method" in {
      intercept[SipDomain.DomainName.InvalidCharException](
        SipDomain.DomainName.unsafe("invalidChar?.sip.twilio.com")
      )
    }

    "return Right if created with with valid input" in {
      val result                          = SipDomain.DomainName.safe("valid.sip.twilio.com")
      val unwrapped: SipDomain.DomainName =
        result.getOrElse(fail("Expected success result here"))
      assert(unwrapped.toString == "valid.sip.twilio.com")
    }

    "return instance if created with valid input via unsafe method" in {
      val result: SipDomain.DomainName = SipDomain.DomainName.unsafe("valid.sip.twilio.com")
      assert(result.toString == "valid.sip.twilio.com")
    }

    "do not allow instances to be created with the default apply method of cases classes" in {
      assertTypeError("""SipDomain.DomainName("This should not be possible")""")
    }

    "Do not allow to use copy on instances, as that would be a way to create a instance with invalid length" in {
      assertTypeError(
        """val instance = SipDomain.DomainName.unsafe("valid.sip.twilio.com")
          |instance.copy(toString = "I should disallow this")
          |""".stripMargin
      )
    }
  }

}
