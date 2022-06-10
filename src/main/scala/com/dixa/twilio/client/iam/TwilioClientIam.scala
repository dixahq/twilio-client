package com.dixa.twilio.client.iam

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.model.iam.TwilioAccount

trait TwilioClientIam {

  /** Fetch a single account for an account sid.
    */
  def accountFetch: AccountFetchRequestExecutor

  /** Fetch accounts with optional status filter
    */
  def accountReadV2: ReadAllAccountsRequestExecutor

  /** Read all Twilio accounts
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
  @deprecated("Use accountReadV2 instead", "0.11.0")
  def accountRead(
      connSettings: TwilioConnectionSettings,
      status: Option[TwilioAccount.Status] = None
  ): Source[TwilioAccount, NotUsed]
}
