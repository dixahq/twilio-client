package com.dixa.twilio.model.voice

import org.scalatest.wordspec.AnyWordSpec

final class QueueMaxSizeTest extends AnyWordSpec {

  classOf[Queue.MaxSize].getSimpleName should {

    "Tell that it is valid if value is between 1 and 5000 both inclusive" in {
      (1 to 5000) foreach { i =>
        val instance = Queue.MaxSize(i)
        assert(instance.isValid)
        assert(instance.asInt == i)
      }
    }

    "Tell that it is not valid if value is 0" in {
      val instance = Queue.MaxSize(0)
      assert(!instance.isValid)
    }

    "Tell that it is not valid if value is 5001" in {
      val instance = Queue.MaxSize(5001)
      assert(!instance.isValid)
    }

    "Return corresponding valid value instance" in {
      val instance = Queue.MaxSize(2348)
      assert(instance.asValidValue == Some(Queue.MaxSize.ValidValues.`2348`))
    }

    "Return None if ask for corresponding valid value for an instance that is not valid" in {
      val instance = Queue.MaxSize(9988333)
      assert(instance.asValidValue == None)
    }

    "Be creatable from a valid value" in {
      val instance: Queue.MaxSize = Queue.MaxSize.fromValidValue(Queue.MaxSize.ValidValues.`3521`)
      assert(instance.asInt == 3521)
    }
  }

}
