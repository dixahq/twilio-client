package com.dixa.twilio.client.impl

/** This package contains a object per request that TwilioClient is offering.
  *
  * TwilioClient offers a nice overview, of what we are currently able to do with Twilios api in a
  * single place. However implementing it all in a single implementation class, would result in a
  * huge file, that would be very cluttered and a hard to maintain. So therefore each method in the
  * client, is implemented by an object in this package. TwilioClientImpl therefore only need to
  * pick the matching object, and call its apply method.
  *
  * The Request objects should be grouped in futher sub packages, representing the domain in Twilio
  * that the touch, this could be things like voice, account etc.
  *
  * These object should contain all the logic necessary for performing that call. But whatever they
  * need to have in common, such as shared classes for representing Twilio JSON, can be placed in
  * either there domain sub package, or in this package, depending on the needed scope for it. But
  * remember everything placed like that will be shared by multiple request object, so they should
  * be documented to clearly describe what they are usable for.
  *
  * Request can also be grouped in sub package, and then have the code in common in that sub package
  * package. This should be done, so that it resembles how Twilios API is structured, as it limits
  * the scope of where this shared logic is available. See
  * [[com.dixa.twilio.client.impl.request.voice]] for an example of that.
  */
package object request {}
