package com.dixa.twilio.client

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.model.TwilioAccount

trait TwilioClientAccount {

  /** Fetch all Twilio accounts
    *
    * @param connSettings
    *   Connection settings to use. The returned accounts, will be the main account connected to,
    *   and all of its sub accounts.
    * @param status
    *   If Some(status) then only fetch accounts of that status. If None no status filtering is
    *   applied.
    * @return
    *   Source of the requested TwilioAccount objects. Twilio does not support streaming, so it will
    *   make multiple request behind the scene.
    */
  def fetchAllAccounts(
      connSettings: TwilioConnectionSettings,
      status: Option[TwilioAccount.Status] = None
  ): Source[TwilioAccount, NotUsed]
}
