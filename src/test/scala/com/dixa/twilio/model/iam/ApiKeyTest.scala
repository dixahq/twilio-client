package com.dixa.twilio.model.iam

import org.scalatest.wordspec.AnyWordSpec

final class ApiKeyTest extends AnyWordSpec {
  "ApiKey" should {
    "preserve HasFlags trait when calling withSecret" in {
      val sid                     = ApiKey.Sid("SK123")
      val name                    = ApiKey.FriendlyName("name")
      val secret                  = ApiKey.Secret("secret")
      val flags: Set[ApiKey.Flag] = Set(ApiKey.Flag.Restricted)

      val key: ApiKey with ApiKey.HasFlags with ApiKey.HasSecret =
        ApiKey(sid, name)
          .withFlags(flags)
          .withSecret(secret)

      assert(key.flags === flags)
      assert(key.secret === secret)
    }

    "preserve HasSecret trait when calling withFlags" in {
      val sid                     = ApiKey.Sid("SK123")
      val name                    = ApiKey.FriendlyName("name")
      val secret                  = ApiKey.Secret("secret")
      val flags: Set[ApiKey.Flag] = Set(ApiKey.Flag.Restricted)

      val key: ApiKey with ApiKey.HasSecret with ApiKey.HasFlags =
        ApiKey(sid, name)
          .withSecret(secret)
          .withFlags(flags)

      assert(key.flags === flags)
      assert(key.secret === secret)
    }

    "preserve HasFlags and HasSecret traits when calling withPolicyAllow" in {
      val sid                       = ApiKey.Sid("SK123")
      val name                      = ApiKey.FriendlyName("name")
      val secret                    = ApiKey.Secret("secret")
      val flags: Set[ApiKey.Flag]   = Set(ApiKey.Flag.Restricted)
      val policy: Set[ApiKeyPolicy] = Set(ApiKeyPolicy.ConferencesRead)

      val key: ApiKey with ApiKey.HasFlags with ApiKey.HasSecret with ApiKey.HasPolicyAllow =
        ApiKey(sid, name)
          .withFlags(flags)
          .withSecret(secret)
          .withPolicyAllow(policy)

      assert(key.flags === flags)
      assert(key.secret === secret)
      assert(key.policyAllow === policy)
    }

    "preserve HasPolicyAllow trait when calling withFlags and withSecret" in {
      val sid                       = ApiKey.Sid("SK123")
      val name                      = ApiKey.FriendlyName("name")
      val secret                    = ApiKey.Secret("secret")
      val flags: Set[ApiKey.Flag]   = Set(ApiKey.Flag.Restricted)
      val policy: Set[ApiKeyPolicy] = Set(ApiKeyPolicy.ConferencesRead)

      val key: ApiKey with ApiKey.HasPolicyAllow with ApiKey.HasFlags with ApiKey.HasSecret =
        ApiKey(sid, name)
          .withPolicyAllow(policy)
          .withFlags(flags)
          .withSecret(secret)

      assert(key.flags === flags)
      assert(key.secret === secret)
      assert(key.policyAllow === policy)
    }
  }
}
