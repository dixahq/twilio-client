package com.dixa.twilio.client.iam

trait TwilioClientIam {

  /** Fetch a single account for an account sid.
    */
  def accountFetch: AccountFetchRequestExecutor

  /** Read all Twilio accounts */
  def accountRead: ReadAllAccountsRequestExecutor

  /** Create a secondary auth token.
    *
    * Note that you do not provide any account sid, so the secondary token will be created on the
    * account belonging to the used credentials. Unlike most other request, this makes it impossible
    * to use root account credentials, when doing this on a sub account.
    */
  def authTokenSecondaryCreate: AuthTokenSecondaryCreateRequestExecutor

  /** Delete a secondary auth token.
    *
    * Note that you do not provide any account sid, so the secondary token will be deleted on the
    * account belonging to the used credentials. Unlike most other request, this makes it impossible
    * to use root account credentials, when doing this on a sub account.
    */
  def authTokenSecondaryDelete: AuthTokenSecondaryDeleteRequestExecutor
}
