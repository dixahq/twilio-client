// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.client.iam

trait TwilioClientIam {

  /** Create a new Account instance resource as a subaccount of the one used to make the request.
    *
    * @see
    *   https://www.twilio.com/docs/iam/api/account#create-an-account-resource
    */
  def accountCreate: AccountCreateRequestExecutor

  /** Fetch a single account for an account sid.
    *
    * @see
    *   https://www.twilio.com/docs/iam/api/account#fetch-an-account-resource
    */
  def accountFetch: AccountFetchRequestExecutor

  /** Read all Twilio accounts
    *
    * @see
    *   https://www.twilio.com/docs/iam/api/account#read-multiple-account-resources
    */
  def accountRead: ReadAllAccountsRequestExecutor

  /** Allows you to modify the properties of an account.
    *
    * @see
    *   https://www.twilio.com/docs/iam/api/account#update-an-account-resource
    */
  def accountUpdate: AccountUpdateRequestExecutor

  /** Create a secondary auth token.
    *
    * Note that you do not provide any account sid, so the secondary token will be created on the
    * account belonging to the used credentials. Unlike most other request, this makes it impossible
    * to use root account credentials, when doing this on a sub account.
    *
    * Twilio documentation: [[https://www.twilio.com/docs/iam/api/secondary_authtoken]]
    */
  def authTokenSecondaryCreate: AuthTokenSecondaryCreateRequestExecutor

  /** Delete a secondary auth token.
    *
    * Note that you do not provide any account sid, so the secondary token will be deleted on the
    * account belonging to the used credentials. Unlike most other request, this makes it impossible
    * to use root account credentials, when doing this on a sub account.
    *
    * Twilio documentation: [[https://www.twilio.com/docs/iam/api/secondary_authtoken]]
    */
  def authTokenSecondaryDelete: AuthTokenSecondaryDeleteRequestExecutor

  /** Promote the secondary auth token to primary.
    *
    * This will:
    *
    *   - Delete the current primary token
    *   - Promote the current secondary token to be the new primary token
    *   - Leave no account without a secondary token
    *
    * Twilio documentation: [[https://www.twilio.com/docs/iam/api/authtoken]]
    */
  def authTokenPromote: AuthTokenPromoteRequestExecutor

  /** Create an Access Token for client-side SDKs.
    */
  def accessTokenCreate: AccessTokenCreateRequestExecutor

  /** Create a new Twilio API key for a given account.
    *
    * The returned [[com.dixa.twilio.model.iam.ApiKey]] includes the secret, which is only available
    * at creation time. Store it securely immediately.
    *
    * @see
    *   https://www.twilio.com/docs/iam/api-keys/key-resource-v1
    */
  def apiKeyCreate: ApiKeyCreateRequestExecutor

  /** List all Twilio API keys for a given account.
    *
    * Note: the key secret is not returned in list responses.
    *
    * @see
    *   https://www.twilio.com/docs/iam/api-keys/key-resource-v1
    */
  def apiKeyRead: ApiKeyReadRequestExecutor

  /** Delete a Twilio API key.
    *
    * @see
    *   https://www.twilio.com/docs/iam/api-keys/key-resource-v1
    */
  def apiKeyDelete: ApiKeyDeleteRequestExecutor
}
