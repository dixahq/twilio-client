package com.dixa.twilio.client.impl

/** Custom Upickle Pickler settings that in generel suit this client
  */
private[client] object TwilioClientPickler extends upickle.AttributeTagged {

  /** The default is to treat option as json collections of max 1 element. However twilio sends us a
    * lot of attributes where the values could be null, and we would like to read these in as
    * options, instead of relying on attributes actually being set to null (the upickle default for
    * null values).
    */
  override implicit def OptionReader[T: Reader]: Reader[Option[T]] = {
    new Reader.Delegate[Any, Option[T]](implicitly[Reader[T]].map(Some(_))) {
      override def visitNull(index: Int) = None

    }
  }
}
