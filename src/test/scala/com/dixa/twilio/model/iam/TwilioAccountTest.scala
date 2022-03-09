package com.dixa.twilio.model.iam

import com.dixa.twilio.CommonFixtures
import org.scalatest.wordspec.AnyWordSpec

final class TwilioAccountTest extends AnyWordSpec {

  classOf[TwilioAccount].getSimpleName when {

    "isActive is called" should {
      "say true if status is active" in {
        val f = new Fixture
        import f._
        val instance = account1.copy(status = TwilioAccount.Status.Active)
        assert(instance.isActive)
      }
      "say false if status is closed" in {
        val f = new Fixture
        import f._
        val instance = account1.copy(status = TwilioAccount.Status.Closed)
        assert(!instance.isActive)
      }
      "say false if status is suspended" in {
        val f = new Fixture
        import f._
        val instance = account1.copy(status = TwilioAccount.Status.Suspended)
        assert(!instance.isActive)
      }
    }
  }

  private final class Fixture extends CommonFixtures.Account
}
