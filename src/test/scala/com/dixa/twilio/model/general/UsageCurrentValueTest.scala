package com.dixa.twilio.model.general

import org.scalatest.wordspec.AnyWordSpec

final class UsageCurrentValueTest extends AnyWordSpec {

  classOf[UsageTrigger.CurrentValue].getSimpleName should {

    "Successfully return value, when constructed from a decimal like value" in {
      val instance: Either[UsageTrigger.CurrentValue.CreationException, UsageTrigger.CurrentValue] =
        UsageTrigger.CurrentValue.safe("2342.33")
      val unwrappedResult = instance.getOrElse(fail("expected success here"))
      assert(unwrappedResult.twilioString == "2342.33")
    }

    "return a left if created value is decimal like, but have two dots" in {
      val instance: Either[UsageTrigger.CurrentValue.CreationException, UsageTrigger.CurrentValue] =
        UsageTrigger.CurrentValue.safe("2342.33.22")
      assert(instance == Left(UsageTrigger.CurrentValue.NotDecimalException("2342.33.22")))
    }

    "return a left if created value non decimal like string" in {
      val instance: Either[UsageTrigger.CurrentValue.CreationException, UsageTrigger.CurrentValue] =
        UsageTrigger.CurrentValue.safe("Hello")
      assert(instance == Left(UsageTrigger.CurrentValue.NotDecimalException("Hello")))
    }

    "do not allow instance to be created with the constructor" in {
      assertTypeError("""new UsageTrigger.CurrentValue("bla")""")
    }

    "do not allow instances to be created with the default apply method of cases classes" in {
      assertTypeError("""UsageTrigger.CurrentValue("34234.234")""")
    }

    "Do not allow to use copy on instances, as that would be a way to create a instance with invalid length" in {
      assertTypeError(
        """val instance = UsageTrigger.CurrentValue.unsafe("2342.33")
          |instance.copy(toString = "hello there")
          |""".stripMargin
      )
    }

  }

}
