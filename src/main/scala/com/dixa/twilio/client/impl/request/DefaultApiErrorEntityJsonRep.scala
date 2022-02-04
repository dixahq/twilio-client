package com.dixa.twilio.client.impl.request

/** Json representation of the standard error entity that Twilio will send back on none 200
  * responses.
  *
  * More details can be found here: https://www.twilio.com/docs/api/errors
  */
private[request] final case class DefaultApiErrorEntityJsonRep(
    code: Long,
    message: String,
    more_info: String,
    status: Int
)
