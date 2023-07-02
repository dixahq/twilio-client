package com.dixa.twilio.model

import org.scalatest.wordspec.AnyWordSpec

import scala.reflect.{classTag, ClassTag}

/** Abstract base class for tests of subclasses of SidAbstract.
  *
  * All you need to do to have a working test is to create a test class that extends this class, and
  * nothing else. You should not declare any tests directly in that class. If you need more tests
  * than provided by this class, then create a separate test class for that.
  *
  * This test will only work if the companion object of the SidAbstract implementation is also
  * extending the SidAbstract.SidCompanionObject class, but that is something you really should do
  * anyway when implementing a Sid class.
  *
  * @param companionObject
  *   The companion object of the SidAbstract implementation.
  * @param notAllowDirectCallToConstructorTest
  *   A Sid implementation should have a private constructor, so that it's not possible to bypass
  *   the validation checks of the input. So we would like to test that it does not compile to
  *   create an instance with the `new` keyword. Unfortunately scala-test `assertDoesNotCompile`
  *   function requires a plain string literal as input, and that means that we cannot construct the
  *   line dynamically in this class. So you have to provide that test as a function in this
  *   argument. If as an example you are testing the Call.Sid class, the input would be: {
  *   assertDoesNotCompile("""new com.dixa.twilio.model.voice.Call.Sid("NotValidInput")""") }
  * @tparam S
  *   The SidAbstract implementation type to test.
  * @tparam C
  *   The type of the companion object of the SidAbstract implementation to test.
  */
abstract class SidAbstractTest[
    S <: SidAbstract: ClassTag,
    C <: SidAbstract.SidCompanionObject[S],
](companionObject: C, notAllowDirectCallToConstructorTest: => Any)
    extends AnyWordSpec {

  private val entityName = classTag[S].runtimeClass.getName

  entityName when {

    "constructed with apply method" should {
      // It looks like call sids are always CA and then 32 HEX characters. But the
      // official documentation says nothing else than:
      // "It is a 34 character string that starts with CA"
      // So lets enforce exactly that.
      // https://support.twilio.com/hc/en-us/articles/223180488-What-is-a-Call-SID-

      "not accept empty strings as input" in {
        intercept[companionObject.ArgumentEmptyException] {
          companionObject.unsafe("")
        }
      }

      s"not accept input that does not start with ${companionObject.prefix}" in {
        intercept[companionObject.ArgumentMissingPrefixException] {
          val prefixTotry = if (companionObject.prefix.toString == "AC") "CA" else "AC"
          companionObject.unsafe(s"${prefixTotry}XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
        }
      }

      s"not accept input that is not 31 chars long (Should always be ${companionObject.prefix} plus 32 chars" in {
        intercept[companionObject.ArgumentLengthException] {
          println("-------------------------")
          println(companionObject.prefix)
          companionObject.unsafe(s"${companionObject.prefix}XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
        }
      }

      s"not accept input that is not 33 chars long (Should always be ${companionObject.prefix} plus 32 chars" in {
        intercept[companionObject.ArgumentLengthException] {
          companionObject.unsafe(s"${companionObject.prefix}XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
        }
      }
    }

    "constructed with safe method" should {

      "return right on valid input" in {
        val asString = s"${companionObject.prefix}xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
        val resultE: Either[companionObject.CreationException, S] = companionObject.safe(asString)
        val result = resultE.getOrElse(fail(s"Expected right side here but was: $resultE"))
        assert(result.toString === asString)
      }

      "return Left on empty strings as input" in {
        val resultE  = companionObject.safe("")
        val expected = Left(companionObject.ArgumentEmptyException())
        assert(resultE === expected)
      }

      s"not accept input that does not start with ${companionObject.prefix}" in {
        val prefixTotry = if (companionObject.prefix.toString == "AC") "CA" else "AC"
        val asString    = s"${prefixTotry}XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
        val resultE     = companionObject.safe(asString)
        val expected    = Left(companionObject.ArgumentMissingPrefixException(asString))
        assert(resultE === expected)
      }

      s"not accept input that is not 31 chars long (Should always be ${companionObject.prefix} plus 32 chars" in {
        val asString = s"${companionObject.prefix}XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
        val resultE  = companionObject.safe(asString)
        val expected = Left(companionObject.ArgumentLengthException(asString))
        assert(resultE === expected)
      }

      s"not accept input that is not 33 chars long (Should always be ${companionObject.prefix} plus 32 chars" in {
        val asString = s"${companionObject.prefix}XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
        val resultE  = companionObject.safe(asString)
        val expected = Left(companionObject.ArgumentLengthException(asString))
        assert(resultE === expected)
      }
    }

    "not allow using the constructor, to bypass validation" in notAllowDirectCallToConstructorTest
  }

}
