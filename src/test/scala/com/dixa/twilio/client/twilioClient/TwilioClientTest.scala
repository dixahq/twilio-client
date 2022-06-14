package com.dixa.twilio.client.twilioClient

import com.dixa.twilio.client.TestActorSystem
import org.scalatest.wordspec.AsyncWordSpec

/** Shared base class for all tests of functionality in the TwilioClientX classes.
  *
  * This has several purposes:
  *   1. Make the these test a little consistent. 2. Bring down compile time, as there is a lot of
  *      test classes, and they all need to mix in the same trait. And it is faster to extend an
  *      abstract class, than to mix in a trait.
  */
abstract class TwilioClientTest extends AsyncWordSpec with WireMockTest with TestActorSystem {

  protected val expectedPageSizeForStreams = "1000"
}
