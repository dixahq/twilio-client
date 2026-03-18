// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.model.general

import org.scalatest.wordspec.AnyWordSpec

final class UsageTriggerFriendlyNameTest extends AnyWordSpec {

  private val length64String = "This is a 64-character Scala string example to test the limit..."
  private val length65String = "This is a 65-character Scala string example to test the limit...."

  classOf[UsageTrigger.FriendlyName].getSimpleName should {

    "return an an Left if created with null value" in {

      val result = UsageTrigger.FriendlyName.safe(null)
      assert(result == Left(UsageTrigger.FriendlyName.NullValueException()))
    }

    "or throw an exception if created with null via the unsafe variant" in {
      intercept[UsageTrigger.FriendlyName.NullValueException](
        UsageTrigger.FriendlyName.unsafe(null)
      )
    }

    "return an Left if created with a value that is to long" in {
      val result = UsageTrigger.FriendlyName.safe(length65String)
      assert(result == Left(UsageTrigger.FriendlyName.ToLongException(length65String, 64)))
    }

    "or throw an created with to long of a value via the unsafe method" in {
      intercept[UsageTrigger.FriendlyName.ToLongException](
        UsageTrigger.FriendlyName.unsafe(length65String)
      )
    }

    "return Right if created with the max length of 64 chars" in {
      val result                               = UsageTrigger.FriendlyName.safe(length64String)
      val unwrapped: UsageTrigger.FriendlyName =
        result.getOrElse(fail("Expected success result here"))
      assert(unwrapped.toString == length64String)
    }

    "return instance if created with the max length of 64 chars via unsafe method" in {
      val result: UsageTrigger.FriendlyName = UsageTrigger.FriendlyName.unsafe(length64String)
      assert(result.toString == length64String)
    }

    "do not allow instances to be created with the default apply method of cases classes" in {
      assertTypeError("""UsageTrigger.FriendlyName("This should not be possible")""")
    }

    "Do not allow to use copy on instances, as that would be a way to create a instance with invalid length" in {
      assertTypeError(
        """val instance = UsageTrigger.FriendlyName.unsafe("Hello there")
          |instance.copy(toString = "I should disallow this")
          |""".stripMargin
      )
    }
  }

}
