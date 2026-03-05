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
      val sid                              = ApiKey.Sid("SK123")
      val name                             = ApiKey.FriendlyName("name")
      val secret                           = ApiKey.Secret("secret")
      val flags: Set[ApiKey.Flag]          = Set(ApiKey.Flag.Restricted)
      val policy: Set[ApiKey.ApiKeyPolicy] = Set(ApiKey.ApiKeyPolicy.ConferencesRead)

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
      val sid                              = ApiKey.Sid("SK123")
      val name                             = ApiKey.FriendlyName("name")
      val secret                           = ApiKey.Secret("secret")
      val flags: Set[ApiKey.Flag]          = Set(ApiKey.Flag.Restricted)
      val policy: Set[ApiKey.ApiKeyPolicy] = Set(ApiKey.ApiKeyPolicy.ConferencesRead)

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
      val policy = ApiKey.ApiKeyPolicy.fromTwilioString("/twilio/lookup/phone-numbers/read")
      assert(policy === Some(ApiKey.ApiKeyPolicy.LookupPhoneNumbersRead))
    }

    "correctly parse IamAccountOauthAppsAll policy" in {
      val policy = ApiKey.ApiKeyPolicy.fromTwilioString("/twilio/iam/account-oauth-apps/*")
      assert(policy === Some(ApiKey.ApiKeyPolicy.IamAccountOauthAppsAll))
    }

    "correctly parse StudioFlowsAll policy" in {
      val policy = ApiKey.ApiKeyPolicy.fromTwilioString("/twilio/studio/flows/*")
      assert(policy === Some(ApiKey.ApiKeyPolicy.StudioFlowsAll))
    }

    "correctly parse StudioExecutionsStepsContextRead policy" in {
      val policy =
        ApiKey.ApiKeyPolicy.fromTwilioString("/twilio/studio/executions.steps.context/read")
      assert(policy === Some(ApiKey.ApiKeyPolicy.StudioExecutionsStepsContextRead))
    }

    "correctly parse PhoneNumbersRegulatoryComplianceSupportingDocumentsAll policy" in {
      val policy =
        ApiKey.ApiKeyPolicy.fromTwilioString(
          "/twilio/phone-numbers/regulatory-compliance.supporting-documents/*"
        )
      assert(
        policy === Some(ApiKey.ApiKeyPolicy.PhoneNumbersRegulatoryComplianceSupportingDocumentsAll)
      )
    }

    "correctly parse PhoneNumbersRegulatoryComplianceBundlesReplaceItemsUpdate policy" in {
      val policy =
        ApiKey.ApiKeyPolicy.fromTwilioString(
          "/twilio/phone-numbers/regulatory-compliance.bundles.replace-items/update"
        )
      assert(
        policy === Some(
          ApiKey.ApiKeyPolicy.PhoneNumbersRegulatoryComplianceBundlesReplaceItemsUpdate
        )
      )
    }

    "correctly parse PhoneNumbersActiveNumbersAll policy" in {
      val policy =
        ApiKey.ApiKeyPolicy.fromTwilioString("/twilio/phone-numbers/active-numbers/*")
      assert(policy === Some(ApiKey.ApiKeyPolicy.PhoneNumbersActiveNumbersAll))
    }

    "correctly parse BillingUsageRead policy" in {
      val policy = ApiKey.ApiKeyPolicy.fromTwilioString("/twilio/billing/usage/read")
      assert(policy === Some(ApiKey.ApiKeyPolicy.BillingUsageRead))
    }

    "correctly parse MonitorEventsList policy" in {
      val policy = ApiKey.ApiKeyPolicy.fromTwilioString("/twilio/monitor/events/list")
      assert(policy === Some(ApiKey.ApiKeyPolicy.MonitorEventsList))
    }

    "correctly parse MonitorEventsRead policy" in {
      val policy = ApiKey.ApiKeyPolicy.fromTwilioString("/twilio/monitor/events/read")
      assert(policy === Some(ApiKey.ApiKeyPolicy.MonitorEventsRead))
    }

    "correctly parse MonitorAlertsList policy" in {
      val policy = ApiKey.ApiKeyPolicy.fromTwilioString("/twilio/monitor/alerts/list")
      assert(policy === Some(ApiKey.ApiKeyPolicy.MonitorAlertsList))
    }

    "correctly parse MonitorAlertsRead policy" in {
      val policy = ApiKey.ApiKeyPolicy.fromTwilioString("/twilio/monitor/alerts/read")
      assert(policy === Some(ApiKey.ApiKeyPolicy.MonitorAlertsRead))
    }

    "correctly parse EventStreamsSchemaRead policy" in {
      val policy = ApiKey.ApiKeyPolicy.fromTwilioString("/twilio/event-streams/schema/read")
      assert(policy === Some(ApiKey.ApiKeyPolicy.EventStreamsSchemaRead))
    }

    "correctly parse EventStreamsSubscriptionSubscribedEventAll policy" in {
      val policy =
        ApiKey.ApiKeyPolicy.fromTwilioString(
          "/twilio/event-streams/subscription.subscribed-event/*"
        )
      assert(policy === Some(ApiKey.ApiKeyPolicy.EventStreamsSubscriptionSubscribedEventAll))
    }

    "correctly parse EventStreamsSinkTestCreate policy" in {
      val policy = ApiKey.ApiKeyPolicy.fromTwilioString("/twilio/event-streams/sink.test/create")
      assert(policy === Some(ApiKey.ApiKeyPolicy.EventStreamsSinkTestCreate))
    }

    "correctly parse FlexInsightsHistoricalReportsRead policy" in {
      val policy =
        ApiKey.ApiKeyPolicy.fromTwilioString("/twilio/flex/insights.historical-reports/read")
      assert(policy === Some(ApiKey.ApiKeyPolicy.FlexInsightsHistoricalReportsRead))
    }

    "correctly parse FlexScvCertificateCreate policy" in {
      val policy = ApiKey.ApiKeyPolicy.fromTwilioString("/twilio/flex/scv-certificate/create")
      assert(policy === Some(ApiKey.ApiKeyPolicy.FlexScvCertificateCreate))
    }

    "correctly parse MicrovisorDeviceCertCreate policy" in {
      val policy = ApiKey.ApiKeyPolicy.fromTwilioString("/twilio/microvisor/device-cert/create")
      assert(policy === Some(ApiKey.ApiKeyPolicy.MicrovisorDeviceCertCreate))
    }

    "correctly parse VideoRecordingsRecordingSettingsRead policy" in {
      val policy =
        ApiKey.ApiKeyPolicy.fromTwilioString("/twilio/video/recordings.recording-settings/read")
      assert(policy === Some(ApiKey.ApiKeyPolicy.VideoRecordingsRecordingSettingsRead))
    }

    "correctly parse VideoRoomsParticipantsPublishedTracksList policy" in {
      val policy =
        ApiKey.ApiKeyPolicy.fromTwilioString(
          "/twilio/video/rooms.participants.published-tracks/list"
        )
      assert(policy === Some(ApiKey.ApiKeyPolicy.VideoRoomsParticipantsPublishedTracksList))
    }

    "correctly parse VideoCompositionsCompositionHooksAll policy" in {
      val policy =
        ApiKey.ApiKeyPolicy.fromTwilioString("/twilio/video/compositions.composition-hooks/*")
      assert(policy === Some(ApiKey.ApiKeyPolicy.VideoCompositionsCompositionHooksAll))
    }
  }
}
