package com.dixa.twilio.model

import com.dixa.twilio.model.callback.CallbackUrl

/** Encapsulate a Optional CallbackUrl and a none optional method to use for it.
  *
  * This is a common pattern used in many Twilio resources, where the url for a callback, and the
  * method to use for it is two different attributes, where the url is optional, but the method is
  * always there.
  */
final case class CallbackUrlOptionalAndRequiredMethod[URL <: CallbackUrl](
    urlOption: Option[URL],
    method: HttpMethod
)
