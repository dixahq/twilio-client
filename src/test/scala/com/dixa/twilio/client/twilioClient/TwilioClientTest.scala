package com.dixa.twilio.client.twilioClient

import com.dixa.twilio.client.TestActorSystem
import org.scalatest.wordspec.AnyWordSpec

/** Shared base class for all tests of functionality in the TwilioClientX classes.
  *
  * This has several purposes:
  *   1. Make the these test a little consisten 2. Bring down compile time, as there is a lot of
  *      test classes, and they all need to mix in the same trait. And is is faster to extends an
  *      absract class, than to mix in a trait.
  */
abstract class TwilioClientTest extends AnyWordSpec with WireMockTest with TestActorSystem {}
