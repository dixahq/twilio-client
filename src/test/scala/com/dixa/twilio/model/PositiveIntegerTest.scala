package com.dixa.twilio.model

import org.scalatest.wordspec.AnyWordSpec

final class PositiveIntegerTest extends AnyWordSpec {

  classOf[PositiveInteger].getSimpleName when {

    "constructed with the unsafe factory" should {

      "throw an exception if called with 0" in {
        intercept[PositiveInteger.Err.NotPositive] {
          PositiveInteger.unsafe(0)
        }
      }

      "throw an exception if called with -1" in {
        intercept[PositiveInteger.Err.NotPositive] {
          PositiveInteger.unsafe(-1)
        }
      }

      "throw an exception if called with -3456345" in {
        intercept[PositiveInteger.Err.NotPositive] {
          PositiveInteger.unsafe(-3456345)
        }
      }

      "throw an exception if called with Int.MinValue" in {
        intercept[PositiveInteger.Err.NotPositive] {
          PositiveInteger.unsafe(Int.MinValue)
        }
      }

      "return a instance if called with 1" in {
        val instance = PositiveInteger.unsafe(1)
        assert(instance.int == 1)
      }

      "return a instance if called with 45635" in {
        val instance = PositiveInteger.unsafe(45635)
        assert(instance.int == 45635)
      }

      "return a instance if called with Int.MaxValue" in {
        val instance = PositiveInteger.unsafe(Int.MaxValue)
        assert(instance.int == Int.MaxValue)
      }
    }

    "constructed with the safe factory" should {

      "return a left if called with 0" in {
        assert(PositiveInteger.safe(0) == Left(PositiveInteger.Err.NotPositive(0)))
      }

      "throw an exception if called with -1" in {
        assert(PositiveInteger.safe(-1) == Left(PositiveInteger.Err.NotPositive(-1)))
      }

      "throw an exception if called with -2637755" in {
        assert(PositiveInteger.safe(-2637755) == Left(PositiveInteger.Err.NotPositive(-2637755)))
      }

      "throw an exception if called with Int.MinValue" in {
        assert(
          PositiveInteger.safe(Int.MinValue) == Left(PositiveInteger.Err.NotPositive(Int.MinValue))
        )
      }

      "return a instance if called with 1" in {
        val instance: Either[PositiveInteger.Err, PositiveInteger] = PositiveInteger.safe(1)
        assert(instance.isRight)
        assert(instance.right.get.int == 1)
      }

      "return a instance if called with 547484" in {
        val instance: Either[PositiveInteger.Err, PositiveInteger] = PositiveInteger.safe(547484)
        assert(instance.isRight)
        assert(instance.right.get.int == 547484)
      }

      "return a instance if called with Int.MaxValue" in {
        val instance: Either[PositiveInteger.Err, PositiveInteger] =
          PositiveInteger.safe(Int.MaxValue)
        assert(instance.isRight)
        assert(instance.right.get.int == Int.MaxValue)
      }
    }

    "tried constructed directly via apply method" should {
      "not compile" in {
        assertDoesNotCompile("PositiveInteger(3)")
      }
    }

    "tried constructed directly via constructor" should {
      "not compile" in {
        assertDoesNotCompile("new PositiveInteger(3)")
      }
    }
  }
}
