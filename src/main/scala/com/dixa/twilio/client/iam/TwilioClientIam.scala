package com.dixa.twilio.client.iam

trait TwilioClientIam {

  /** Fetch a single account for an account sid.
    */
  def accountFetch: AccountFetchRequestExecutor

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
  def accountRead: ReadAllAccountsRequestExecutor

  /** Create a secondary auth token.
    *
    * Note that you do not provide any account sid, so the secondary token will be created on the
    * account belonging to the used credentials. Unlike most other request, this makes it impossible
    * to use root account credentials, when doing this on a sub account.
    *
    * @return
    *   SecondaryAuthTokenCreateRequestExecutor
    */
  def secondaryAuthTokenCreate: SecondaryAuthTokenCreateRequestExecutor
}
