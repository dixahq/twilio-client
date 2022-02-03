package com.dixa.twilio.client.impl.request

private[request] final case class DefaultApiErrorEntityJsonRep(
    code: Long,
    message: String,
    more_info: String,
    status: Int
)
