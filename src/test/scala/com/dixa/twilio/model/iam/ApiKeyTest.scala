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
      val sid                             = ApiKey.Sid("SK123")
      val name                            = ApiKey.FriendlyName("name")
      val secret                          = ApiKey.Secret("secret")
      val flags: Set[ApiKey.Flag]         = Set(ApiKey.Flag.Restricted)
      val policy: Set[ApiKey.PolicyAllow] = Set(ApiKey.PolicyAllow.ConferencesRead)

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
      val sid                             = ApiKey.Sid("SK123")
      val name                            = ApiKey.FriendlyName("name")
      val secret                          = ApiKey.Secret("secret")
      val flags: Set[ApiKey.Flag]         = Set(ApiKey.Flag.Restricted)
      val policy: Set[ApiKey.PolicyAllow] = Set(ApiKey.PolicyAllow.ConferencesRead)

      val key: ApiKey with ApiKey.HasPolicyAllow with ApiKey.HasFlags with ApiKey.HasSecret =
        ApiKey(sid, name)
          .withPolicyAllow(policy)
          .withFlags(flags)
          .withSecret(secret)

      assert(key.flags === flags)
      assert(key.secret === secret)
      assert(key.policyAllow === policy)
    }

    "correctly parse LookupPhoneNumbersRead policy" in {
      val policy = ApiKey.PolicyAllow.fromTwilioString("/twilio/lookup/phone-numbers/read")
      assert(policy === Some(ApiKey.PolicyAllow.LookupPhoneNumbersRead))
    }

    "correctly parse IamAccountOauthAppsAll policy" in {
      val policy = ApiKey.PolicyAllow.fromTwilioString("/twilio/iam/account-oauth-apps/*")
      assert(policy === Some(ApiKey.PolicyAllow.IamAccountOauthAppsAll))
    }

    "correctly parse StudioFlowsAll policy" in {
      val policy = ApiKey.PolicyAllow.fromTwilioString("/twilio/studio/flows/*")
      assert(policy === Some(ApiKey.PolicyAllow.StudioFlowsAll))
    }

    "correctly parse StudioExecutionsStepsContextRead policy" in {
      val policy =
        ApiKey.PolicyAllow.fromTwilioString("/twilio/studio/executions.steps.context/read")
      assert(policy === Some(ApiKey.PolicyAllow.StudioExecutionsStepsContextRead))
    }

    "correctly parse PhoneNumbersRegulatoryComplianceSupportingDocumentsAll policy" in {
      val policy =
        ApiKey.PolicyAllow.fromTwilioString(
          "/twilio/phone-numbers/regulatory-compliance.supporting-documents/*"
        )
      assert(
        policy === Some(ApiKey.PolicyAllow.PhoneNumbersRegulatoryComplianceSupportingDocumentsAll)
      )
    }

    "correctly parse PhoneNumbersRegulatoryComplianceBundlesReplaceItemsUpdate policy" in {
      val policy =
        ApiKey.PolicyAllow.fromTwilioString(
          "/twilio/phone-numbers/regulatory-compliance.bundles.replace-items/update"
        )
      assert(
        policy === Some(
          ApiKey.PolicyAllow.PhoneNumbersRegulatoryComplianceBundlesReplaceItemsUpdate
        )
      )
    }

    "correctly parse PhoneNumbersActiveNumbersAll policy" in {
      val policy =
        ApiKey.PolicyAllow.fromTwilioString("/twilio/phone-numbers/active-numbers/*")
      assert(policy === Some(ApiKey.PolicyAllow.PhoneNumbersActiveNumbersAll))
    }

    "correctly parse BillingUsageRead policy" in {
      val policy = ApiKey.PolicyAllow.fromTwilioString("/twilio/billing/usage/read")
      assert(policy === Some(ApiKey.PolicyAllow.BillingUsageRead))
    }

    "correctly parse MonitorEventsList policy" in {
      val policy = ApiKey.PolicyAllow.fromTwilioString("/twilio/monitor/events/list")
      assert(policy === Some(ApiKey.PolicyAllow.MonitorEventsList))
    }

    "correctly parse MonitorEventsRead policy" in {
      val policy = ApiKey.PolicyAllow.fromTwilioString("/twilio/monitor/events/read")
      assert(policy === Some(ApiKey.PolicyAllow.MonitorEventsRead))
    }

    "correctly parse MonitorAlertsList policy" in {
      val policy = ApiKey.PolicyAllow.fromTwilioString("/twilio/monitor/alerts/list")
      assert(policy === Some(ApiKey.PolicyAllow.MonitorAlertsList))
    }

    "correctly parse MonitorAlertsRead policy" in {
      val policy = ApiKey.PolicyAllow.fromTwilioString("/twilio/monitor/alerts/read")
      assert(policy === Some(ApiKey.PolicyAllow.MonitorAlertsRead))
    }
  }
}
