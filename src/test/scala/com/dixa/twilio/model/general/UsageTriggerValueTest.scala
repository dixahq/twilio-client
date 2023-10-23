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
