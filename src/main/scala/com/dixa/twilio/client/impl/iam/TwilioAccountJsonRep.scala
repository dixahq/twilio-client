package com.dixa.twilio.client.impl.iam

import com.dixa.twilio.client.impl.Formatter
import com.dixa.twilio.model.iam.{AuthToken, TwilioAccount}

import java.time.Instant

/** Mimic the JSON Twilio API returns when asked for accounts.
  *
  * Certain fields like all the sub resource urls are omitted, as these are not used in this
  * library. Users can get sub resources by calling the respective methods for fetching them
  * instead.
  *
  * Example Twilio JSON response:
  *
  * {{{
  * {
  *    "status": "active",
  *    "date_updated": "Wed, 23 Feb 2022 17:13:40 +0000",
  *    "auth_token": "AVerySecretValueThatShouldBeXXXX",
  *    "friendly_name": "account friendly name",
  *    "owner_account_sid": "AC5fc6c53ce58165d0712d4a56fa29e23a",
  *    "uri": "/2010-04-01/Accounts/ACf6c9aa4f8756c258be45a6d2637cfa15.json",
  *    "sid": "ACf6c9aa4f2754c258aa45a6d2637cfa15",
  *    "date_created": "Mon, 26 Oct 2015 11:40:54 +0000",
  *    "type": "Full",
  *    "subresource_uris": {
  *      "addresses": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/Addresses.json",
  *      "conferences": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/Conferences.json",
  *      "signing_keys": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/SigningKeys.json",
  *      "transcriptions": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/Transcriptions.json",
  *      "connect_apps": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/ConnectApps.json",
  *      "sip": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/SIP.json",
  *      "authorized_connect_apps": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/AuthorizedConnectApps.json",
  *      "usage": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/Usage.json",
  *      "keys": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/Keys.json",
  *      "applications": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/Applications.json",
  *      "recordings": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/Recordings.json",
  *      "short_codes": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/SMS/ShortCodes.json",
  *      "calls": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/Calls.json",
  *      "notifications": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/Notifications.json",
  *      "incoming_phone_numbers": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/IncomingPhoneNumbers.json",
  *      "queues": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/Queues.json",
  *      "messages": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/Messages.json",
  *      "outgoing_caller_ids": "/2010-04-01/Accounts/ACf6c9aa4f8756c258be45a6d2637cfa15/OutgoingCallerIds.json",
  *      "available_phone_numbers": "/2010-04-01/Accounts/ACf6c9aa4f8756c258be45a6d2637cfa15/AvailablePhoneNumbers.json",
  *      "balance": "/2010-04-01/Accounts/ACf6c9aa4f8756c258be45a6d2637cfa15/Balance.json"
  *    }
  * }
  * }}}
  */
private[iam] final case class TwilioAccountJsonRep(
    status: String,
    date_updated: String,
    auth_token: String,
    friendly_name: String,
    owner_account_sid: String,
    sid: String,
    date_created: String,
    `type`: String
) {

  private[iam] def toModel: TwilioAccount = {
    TwilioAccount(
      name = TwilioAccount.Name(friendly_name),
      sid = TwilioAccount.Sid(sid),
      status = TwilioAccount.Status.fromTwilioStringUnsafe(status),
      ownerAccountSid = TwilioAccount.Sid(owner_account_sid),
      authToken = AuthToken.Primary(auth_token),
      accountType = TwilioAccount.Type.fromTwilioStringUnsafe(`type`),
      timeCreated = Instant.from(Formatter.dateTime.parse(date_created)),
      timeUpdated = Instant.from(Formatter.dateTime.parse(date_updated))
    )
  }

  override def toString =
    s"TwilioAccountJsonRep($status, $date_updated, ***, $friendly_name, $owner_account_sid, $sid, $date_created, ${`type`})"
}
