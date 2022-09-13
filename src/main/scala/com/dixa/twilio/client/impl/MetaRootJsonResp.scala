package com.dixa.twilio.client.impl

//  Full meta json object looks like this, but for now we only need the nex_page_url:
//  "meta": {
//    "page": 1,
//    "page_size": 2,
//    "first_page_url": "https://messaging.twilio.com/v1/Services?PageSize=2&Page=0",
//    "previous_page_url": "https://messaging.twilio.com/v1/Services?PageSize=2&Page=0&PageToken=PTMGd8410e59416697cb4455c87eba98a6d0",
//    "url": "https://messaging.twilio.com/v1/Services?PageSize=2&Page=1&PageToken=PTMGf9a4a36b7b901e4a5d325ff1d92c6dcd",
//    "next_page_url": null,
//    "key": "services"
//  }
//
// There is no direct Twilio documentation on this, as it is documented under each resource using it.
// But an example of such a resource is the Service in the messaging API: https://www.twilio.com/docs/messaging/services/api#

private[client] final case class MetaRootJsonResp(meta: MetaRootJsonResp.MetaJsonRep)

private[client] object MetaRootJsonResp {
  final case class MetaJsonRep(next_page_url: Option[String])
}
