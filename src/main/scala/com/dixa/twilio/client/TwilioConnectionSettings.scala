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

package com.dixa.twilio.client

import com.dixa.twilio.client
import com.dixa.twilio.client.TwilioConnectionSettings.TwilioEndpoint
import com.dixa.twilio.client.impl.ApiSubDomain
import com.dixa.twilio.model.{PublicEdgeLocation, Region}
import com.dixa.twilio.model.iam.{ApiKey, AuthToken, TwilioAccount}
import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable
import scala.concurrent.duration.{DurationInt, FiniteDuration}

/** Connection settings to use when communicating with Twilio
  *
  * @param endpoint
  *   The base host name and port to connect to without the sup domain part. For production this
  *   would be `twilio.com:443,` and then request would end up being made against the respective
  *   subdomains with the region and edge location mixed in like `api.dublin.ie1.twilio.com` and
  *   `messaging.ashburn.us1.twilio.com` depending on the request. The Exception is localhost or
  *   127.0.0.1, if that is set, then no subdomain will be added, no matter what the request is.
  *   Should be "twilio.com" for the production Twilio API.
  * @param region
  *   The region to connect to.
  * @param publicEdgeLocation
  *   The public edge location to connect through. It's generally recommended to pick the one that
  *   is closest to where your request originates from so that you reach the Twilio network as early
  *   as possible. From reading the Twilio documentation, you could think that this param should be
  *   optional, because some older documentation claims that Twilio will then pick the optimal edge
  *   location for you, based on your network geo position. But after introducing real regions, the
  *   FQDN requires both an edge location and a region. Only the US1 region will work without it,
  *   because it is the default region. But according to their own documentation, if the default is
  *   used, then it's the us1 region, and always ashburn as the edge location. So in reality it's
  *   not optional, it only has a default on the US region due to backward compatability. The
  *   documentation that states this can be found here:
  *   https://www.twilio.com/docs/global-infrastructure/create-an-outbound-call-via-rest-api-in-a-non-us-twilio-region#the-twilio-apis-fqdn-format
  * @param protocol
  *   Protocol to use for connecting to Twilio. Should be Https for production Twilio API.
  * @param credentials
  *   The credentials to use for authenticating requests. Use
  *   [[TwilioConnectionSettings.Credentials.AuthTokenCredentials]] to authenticate with an auth
  *   token, or [[TwilioConnectionSettings.Credentials.ApiKeyCredentials]] to authenticate with an
  *   API key.
  * @param parallelFactor
  *   Some request supports doing some work in parallel. This value will in such cases be used for
  *   that. Note that not all requests support running in parallel, and in the once that does, only
  *   some part of it may. So this is more of a guideline to the library, rather than a law.
  * @param timeouts
  *   Timeouts to use in the clients.
  */
final case class TwilioConnectionSettings(
    endpoint: TwilioEndpoint,
    region: Region,
    publicEdgeLocation: PublicEdgeLocation,
    protocol: client.TwilioConnectionSettings.Protocol,
    credentials: TwilioConnectionSettings.Credentials,
    parallelFactor: TwilioConnectionSettings.ParallelFactor,
    timeouts: TwilioConnectionSettings.Timeouts
) {

  // Tiny optimization. Precalculate if it's localhost, so that we don't have to do it on every call to hostNameFor.
  private val isLocalHost =
    endpoint.baseHostName == "localhost" || endpoint.baseHostName == "127.0.0.1"

  // Tiny optimization, but pregenerate the possible hostnames so that we don't have to generate a new string on every call to hostNameFor.
  private val accountHost   = s"${ApiSubDomain.Api}.$baseHostNameWithRegionAndEdge"
  private val apiHost       = s"${ApiSubDomain.Api}.$baseHostNameWithRegionAndEdge"
  private val iamHost       = s"${ApiSubDomain.Iam}.$baseHostNameWithRegionAndEdge"
  private val messagingHost = s"${ApiSubDomain.Messaging}.$baseHostNameWithRegionAndEdge"
  private val previewHost   = s"${ApiSubDomain.Preview}.$baseHostNameWithRegionAndEdge"
  private val routesHost    = s"${ApiSubDomain.Routes}.$baseHostNameWithRegionAndEdge"

  private def baseHostNameWithRegionAndEdge =
    s"${publicEdgeLocation.edgeId}.${region.twilioString}.${endpoint.baseHostName}"

  /** Return the hostname for a specific API subdomain.
    *
    * Twilio is using different subdomains, for different parts of their API, so the
    * TwilioConnectionSetting is only setting what base hostname to connect against, and it is then
    * up to each request to specify what subdomain they should use and use this method for
    * constructing the full hostname.
    *
    * If the base hostname is localhost or 127.0.0.1, then this method will just return that without
    * changing it. This is to make it possible to stub request in unit test with a tool like
    * Wiremock, where the request should just go to localhost without a subdomain appended.
    */
  private[client] def hostNameFor(subDomain: ApiSubDomain): String = {
    if (isLocalHost) endpoint.baseHostName
    else {
      subDomain match {
        case ApiSubDomain.Accounts  => accountHost
        case ApiSubDomain.Api       => apiHost
        case ApiSubDomain.Iam       => iamHost
        case ApiSubDomain.Messaging => messagingHost
        case ApiSubDomain.Preview   => previewHost
        case ApiSubDomain.Routes    => routesHost
      }
    }

  }

}

object TwilioConnectionSettings {

  /** Credentials used to authenticate HTTP requests to the Twilio API. */
  sealed trait Credentials

  object Credentials {

    /** Authenticate using an account auth token.
      *
      * Uses Basic Auth with the account SID as the username and the auth token as the password.
      */
    final case class AuthTokenCredentials(accountSid: TwilioAccount.Sid, authToken: AuthToken)
        extends Credentials

    /** Authenticate using a Twilio API key.
      *
      * Uses Basic Auth with the API key SID as the username and the API key secret as the password.
      * Preferred over auth tokens where possible, as API keys can be revoked independently without
      * affecting the account auth token.
      */
    final case class ApiKeyCredentials(apiKeySid: ApiKey.Sid, apiKeySecret: ApiKey.Secret)
        extends Credentials
  }

  sealed abstract class Protocol(override val toString: String) extends EnumEntry
  object Protocol                                               extends Enum[Protocol] {
    override val values: immutable.IndexedSeq[Protocol] = findValues
    case object Http  extends Protocol("http")
    case object Https extends Protocol("https")
  }

  /** Represent a parallel factor for request to run at. */
  final case class ParallelFactor(asInt: Int)

  object ParallelFactor {

    /** Parallel factor corosponding to half of the cpu cores, or 1 if there only is one cpu core.
      *
      * A good default, if you system is also doing other things, than communicating with Twilio.
      */
    lazy val halfCpuCores: ParallelFactor = {
      val halfCpu = Runtime.getRuntime.availableProcessors() match {
        case numberOfCores if numberOfCores == 1 => 1
        case numberOfCores                       => numberOfCores / 2
      }
      ParallelFactor(halfCpu)
    }
  }

  final case class TwilioEndpoint private (baseHostName: String, port: Int)

  object TwilioEndpoint {

    /** The default endpoint for Twilio - twilio.com:443 */
    val default: TwilioEndpoint = new TwilioEndpoint("twilio.com", 443)

    def apply(baseHostName: String, port: Int): TwilioEndpoint = {
      // return already existing default instance if settings match the default settings.
      // Minor optimization that will save to identical object from existing, in the cases
      // where clients read settings from config, or for some other reason end up constructing
      // this object manually with default settings. And default settings is what would
      // end up being used in almost any other case than tests.
      if (baseHostName == default.baseHostName && port == default.port) default
      else new TwilioEndpoint(baseHostName, port)
    }
  }

  /** Specify the different timeouts to be used by this client.
    *
    * @param requestEntityTimeout
    *   The max time to use on fetching an entity of a single request against Twilio.
    */
  final case class Timeouts(requestEntityTimeout: FiniteDuration)

  object Timeouts {

    lazy val default: Timeouts = Timeouts(
      requestEntityTimeout = 30.seconds
    )
  }
}
