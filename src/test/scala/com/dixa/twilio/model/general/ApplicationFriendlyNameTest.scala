package com.dixa.twilio.model.general

import org.scalatest.wordspec.AnyWordSpec

final class ApplicationFriendlyNameTest extends AnyWordSpec {

  private val length64String = "This is a 64-character Scala string example to test the limit..."
  private val length65String = "This is a 65-character Scala string example to test the limit...."

  classOf[Application.FriendlyName].getSimpleName should {

    "return an an Left if created with null value" in {

      val result = Application.FriendlyName.safe(null)
      assert(result == Left(Application.FriendlyName.NullValueException()))
    }

    "or throw an exception if created with null via the unsafe variant" in {
      intercept[Application.FriendlyName.NullValueException](
        Application.FriendlyName.unsafe(null)
      )
    }

    "return an Left if created with a value that is to long" in {
      val result = Application.FriendlyName.safe(length65String)
      assert(result == Left(Application.FriendlyName.ToLongException(length65String, 64)))
    }

    "or throw an created with to long of a value via the unsafe method" in {
      intercept[Application.FriendlyName.ToLongException](
        Application.FriendlyName.unsafe(length65String)
      )
    }

    "return Right if created with the max length of 64 chars" in {
      val result                              = Application.FriendlyName.safe(length64String)
      val unwrapped: Application.FriendlyName =
        result.getOrElse(fail("Expected success result here"))
      assert(unwrapped.toString == length64String)
    }

    "return instance if created with the max length of 64 chars via unsafe method" in {
      val result: Application.FriendlyName = Application.FriendlyName.unsafe(length64String)
      assert(result.toString == length64String)
    }

    "do not allow instances to be created with the default apply method of cases classes" in {
      assertTypeError("""Application.FriendlyName("This should not be possible")""")
    }

    "Do not allow to use copy on instances, as that would be a way to create a instance with invalid length" in {
      assertTypeError(
        """val instance = Application.FriendlyName.unsafe("Hello there")
          |instance.copy(toString = "I should disallow this")
          |""".stripMargin
      )
    }
  }

}
