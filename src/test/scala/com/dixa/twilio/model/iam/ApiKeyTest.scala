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

package com.dixa.twilio.model.iam

import org.scalatest.wordspec.AnyWordSpec

final class ApiKeyTest extends AnyWordSpec {
  private val dateCreated = java.time.Instant.EPOCH.plusSeconds(234526438)
  private val dateUpdated = dateCreated.plusSeconds(60)

  "ApiKey" should {
    "preserve HasFlags trait when calling withSecret" in {
      val sid                     = ApiKey.Sid("SK123")
      val name                    = ApiKey.FriendlyName("name")
      val secret                  = ApiKey.Secret("secret")
      val flags: Set[ApiKey.Flag] = Set(ApiKey.Flag.Restricted)

      val key: ApiKey with ApiKey.HasFlags with ApiKey.HasSecret =
        ApiKey(sid, name, dateCreated, dateUpdated)
          .withFlags(flags)
          .withSecret(secret)

      assert(key.flags === flags)
      assert(key.secret === secret)
      assert(key.dateCreated === dateCreated)
      assert(key.dateUpdated === dateUpdated)
    }

    "preserve HasSecret trait when calling withFlags" in {
      val sid                     = ApiKey.Sid("SK123")
      val name                    = ApiKey.FriendlyName("name")
      val secret                  = ApiKey.Secret("secret")
      val flags: Set[ApiKey.Flag] = Set(ApiKey.Flag.Restricted)

      val key: ApiKey with ApiKey.HasSecret with ApiKey.HasFlags =
        ApiKey(sid, name, dateCreated, dateUpdated)
          .withSecret(secret)
          .withFlags(flags)

      assert(key.flags === flags)
      assert(key.secret === secret)
      assert(key.dateCreated === dateCreated)
      assert(key.dateUpdated === dateUpdated)
    }

    "preserve HasFlags and HasSecret traits when calling withPolicyAllow" in {
      val sid                       = ApiKey.Sid("SK123")
      val name                      = ApiKey.FriendlyName("name")
      val secret                    = ApiKey.Secret("secret")
      val flags: Set[ApiKey.Flag]   = Set(ApiKey.Flag.Restricted)
      val policy: Set[ApiKeyPolicy] = Set(ApiKeyPolicy.ConferencesRead)

      val key: ApiKey with ApiKey.HasFlags with ApiKey.HasSecret with ApiKey.HasPolicyAllow =
        ApiKey(sid, name, dateCreated, dateUpdated)
          .withFlags(flags)
          .withSecret(secret)
          .withPolicyAllow(policy)

      assert(key.flags === flags)
      assert(key.secret === secret)
      assert(key.policyAllow === policy)
      assert(key.dateCreated === dateCreated)
      assert(key.dateUpdated === dateUpdated)
    }

    "preserve HasPolicyAllow trait when calling withFlags and withSecret" in {
      val sid                       = ApiKey.Sid("SK123")
      val name                      = ApiKey.FriendlyName("name")
      val secret                    = ApiKey.Secret("secret")
      val flags: Set[ApiKey.Flag]   = Set(ApiKey.Flag.Restricted)
      val policy: Set[ApiKeyPolicy] = Set(ApiKeyPolicy.ConferencesRead)

      val key: ApiKey with ApiKey.HasPolicyAllow with ApiKey.HasFlags with ApiKey.HasSecret =
        ApiKey(sid, name, dateCreated, dateUpdated)
          .withPolicyAllow(policy)
          .withFlags(flags)
          .withSecret(secret)

      assert(key.flags === flags)
      assert(key.secret === secret)
      assert(key.policyAllow === policy)
      assert(key.dateCreated === dateCreated)
      assert(key.dateUpdated === dateUpdated)
    }

    "correctly implement equals, hashCode and toString" in {
      val sid                     = ApiKey.Sid("SK123")
      val secret                  = ApiKey.Secret("secret")
      val name                    = ApiKey.FriendlyName("name")
      val flags: Set[ApiKey.Flag] = Set(ApiKey.Flag.Restricted)
      val dateCreated             = java.time.Instant.parse("2023-01-01T00:00:00Z")
      val dateUpdated             = java.time.Instant.parse("2023-01-01T00:01:00Z")

      val key1 = ApiKey(sid, name, dateCreated, dateUpdated).withSecret(secret)
      val key2 = ApiKey(sid, name, dateCreated, dateUpdated).withSecret(secret)
      val key3 = ApiKey(sid, name, dateCreated, dateUpdated).withSecret(secret).withFlags(flags)
      val key4 = ApiKey(sid, name, dateCreated, dateUpdated).withSecret(secret).withFlags(flags)

      assert(key1 === key2)
      assert(key1.hashCode() === key2.hashCode())
      assert(
        key1.toString === s"ApiKey(sid=$sid, secretOpt=Some($secret), friendlyName=$name, flagsOpt=None, policyAllowOpt=None, dateCreated=$dateCreated, dateUpdated=$dateUpdated)"
      )

      assert(key3 === key4)
      assert(key3.hashCode() === key4.hashCode())
      assert(
        key3.toString === s"ApiKey(sid=$sid, secretOpt=Some($secret), friendlyName=$name, flagsOpt=Some($flags), policyAllowOpt=None, dateCreated=$dateCreated, dateUpdated=$dateUpdated)"
      )

      assert(key1 !== key3)
      assert(key1.hashCode() !== key3.hashCode())

      val keyNoSecret = ApiKey(sid, name, dateCreated, dateUpdated)
      assert(keyNoSecret.secretOpt === None)
      assert(
        keyNoSecret.toString === s"ApiKey(sid=$sid, secretOpt=None, friendlyName=$name, flagsOpt=None, policyAllowOpt=None, dateCreated=$dateCreated, dateUpdated=$dateUpdated)"
      )
    }
  }
}
