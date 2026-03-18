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

final class UsageTriggerValueTest extends AnyWordSpec {

  classOf[UsageTrigger.TriggerValue].getSimpleName should {

    "Successfully return value, when constructed from a decimal like value" in {
      val instance: Either[UsageTrigger.TriggerValue.CreationException, UsageTrigger.TriggerValue] =
        UsageTrigger.TriggerValue.safe("2342.33")
      val unwrappedResult = instance.getOrElse(fail("expected success here"))
      assert(unwrappedResult.twilioString == "2342.33")
    }

    "Expose value as a BigDecimal" in {
      val instance = UsageTrigger.TriggerValue.unsafe("64.22")
      val expected = BigDecimal("64.22")
      assert(instance.toBigDecimal == expected)
    }

    "return a left if created value is decimal like, but have two dots" in {
      val instance: Either[UsageTrigger.TriggerValue.CreationException, UsageTrigger.TriggerValue] =
        UsageTrigger.TriggerValue.safe("2342.33.22")
      assert(instance == Left(UsageTrigger.TriggerValue.NotDecimalException("2342.33.22")))
    }

    "return a left if created value non decimal like string" in {
      val instance: Either[UsageTrigger.TriggerValue.CreationException, UsageTrigger.TriggerValue] =
        UsageTrigger.TriggerValue.safe("Hello")
      assert(instance == Left(UsageTrigger.TriggerValue.NotDecimalException("Hello")))
    }

    "do not allow instance to be created with the constructor" in {
      assertTypeError("""new UsageTrigger.TriggerValue("bla")""")
    }

    "do not allow instances to be created with the default apply method of cases classes" in {
      assertTypeError("""UsageTrigger.TriggerValue("34234.234")""")
    }

    "Do not allow to use copy on instances, as that would be a way to create a instance with invalid length" in {
      assertTypeError(
        """val instance = UsageTrigger.TriggerValue.unsafe("2342.33")
          |instance.copy(toString = "hello there")
          |""".stripMargin
      )
    }

  }

}
