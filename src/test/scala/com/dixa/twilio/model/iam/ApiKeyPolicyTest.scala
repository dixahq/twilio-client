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

final class ApiKeyPolicyTest extends AnyWordSpec {
  "ApiKeyPolicy" should {

    "correctly parse LookupPhoneNumbersRead policy" in {
      val policy = ApiKeyPolicy.fromTwilioString("/twilio/lookup/phone-numbers/read")
      assert(policy === Some(ApiKeyPolicy.LookupPhoneNumbersRead))
    }

    "correctly parse IamAccountOauthAppsAll policy" in {
      val policy = ApiKeyPolicy.fromTwilioString("/twilio/iam/account-oauth-apps/*")
      assert(policy === Some(ApiKeyPolicy.IamAccountOauthAppsAll))
    }

    "correctly parse StudioFlowsAll policy" in {
      val policy = ApiKeyPolicy.fromTwilioString("/twilio/studio/flows/*")
      assert(policy === Some(ApiKeyPolicy.StudioFlowsAll))
    }

    "correctly parse StudioExecutionsStepsContextRead policy" in {
      val policy =
        ApiKeyPolicy.fromTwilioString("/twilio/studio/executions.steps.context/read")
      assert(policy === Some(ApiKeyPolicy.StudioExecutionsStepsContextRead))
    }

    "correctly parse PhoneNumbersRegulatoryComplianceSupportingDocumentsAll policy" in {
      val policy =
        ApiKeyPolicy.fromTwilioString(
          "/twilio/phone-numbers/regulatory-compliance.supporting-documents/*"
        )
      assert(
        policy === Some(ApiKeyPolicy.PhoneNumbersRegulatoryComplianceSupportingDocumentsAll)
      )
    }

    "correctly parse PhoneNumbersRegulatoryComplianceBundlesReplaceItemsUpdate policy" in {
      val policy =
        ApiKeyPolicy.fromTwilioString(
          "/twilio/phone-numbers/regulatory-compliance.bundles.replace-items/update"
        )
      assert(
        policy === Some(
          ApiKeyPolicy.PhoneNumbersRegulatoryComplianceBundlesReplaceItemsUpdate
        )
      )
    }

    "correctly parse PhoneNumbersActiveNumbersAll policy" in {
      val policy =
        ApiKeyPolicy.fromTwilioString("/twilio/phone-numbers/active-numbers/*")
      assert(policy === Some(ApiKeyPolicy.PhoneNumbersActiveNumbersAll))
    }

    "correctly parse BillingUsageRead policy" in {
      val policy = ApiKeyPolicy.fromTwilioString("/twilio/billing/usage/read")
      assert(policy === Some(ApiKeyPolicy.BillingUsageRead))
    }

    "correctly parse MonitorEventsList policy" in {
      val policy = ApiKeyPolicy.fromTwilioString("/twilio/monitor/events/list")
      assert(policy === Some(ApiKeyPolicy.MonitorEventsList))
    }

    "correctly parse MonitorEventsRead policy" in {
      val policy = ApiKeyPolicy.fromTwilioString("/twilio/monitor/events/read")
      assert(policy === Some(ApiKeyPolicy.MonitorEventsRead))
    }

    "correctly parse MonitorAlertsList policy" in {
      val policy = ApiKeyPolicy.fromTwilioString("/twilio/monitor/alerts/list")
      assert(policy === Some(ApiKeyPolicy.MonitorAlertsList))
    }

    "correctly parse MonitorAlertsRead policy" in {
      val policy = ApiKeyPolicy.fromTwilioString("/twilio/monitor/alerts/read")
      assert(policy === Some(ApiKeyPolicy.MonitorAlertsRead))
    }

    "correctly parse EventStreamsSchemaRead policy" in {
      val policy = ApiKeyPolicy.fromTwilioString("/twilio/event-streams/schema/read")
      assert(policy === Some(ApiKeyPolicy.EventStreamsSchemaRead))
    }

    "correctly parse EventStreamsSubscriptionSubscribedEventAll policy" in {
      val policy =
        ApiKeyPolicy.fromTwilioString(
          "/twilio/event-streams/subscription.subscribed-event/*"
        )
      assert(policy === Some(ApiKeyPolicy.EventStreamsSubscriptionSubscribedEventAll))
    }

    "correctly parse EventStreamsSinkTestCreate policy" in {
      val policy = ApiKeyPolicy.fromTwilioString("/twilio/event-streams/sink.test/create")
      assert(policy === Some(ApiKeyPolicy.EventStreamsSinkTestCreate))
    }

    "correctly parse FlexInsightsHistoricalReportsRead policy" in {
      val policy =
        ApiKeyPolicy.fromTwilioString("/twilio/flex/insights.historical-reports/read")
      assert(policy === Some(ApiKeyPolicy.FlexInsightsHistoricalReportsRead))
    }

    "correctly parse FlexScvCertificateCreate policy" in {
      val policy = ApiKeyPolicy.fromTwilioString("/twilio/flex/scv-certificate/create")
      assert(policy === Some(ApiKeyPolicy.FlexScvCertificateCreate))
    }

    "correctly parse MicrovisorDeviceCertCreate policy" in {
      val policy = ApiKeyPolicy.fromTwilioString("/twilio/microvisor/device-cert/create")
      assert(policy === Some(ApiKeyPolicy.MicrovisorDeviceCertCreate))
    }

    "correctly parse VideoRecordingsRecordingSettingsRead policy" in {
      val policy =
        ApiKeyPolicy.fromTwilioString("/twilio/video/recordings.recording-settings/read")
      assert(policy === Some(ApiKeyPolicy.VideoRecordingsRecordingSettingsRead))
    }

    "correctly parse VideoRoomsParticipantsPublishedTracksList policy" in {
      val policy =
        ApiKeyPolicy.fromTwilioString(
          "/twilio/video/rooms.participants.published-tracks/list"
        )
      assert(policy === Some(ApiKeyPolicy.VideoRoomsParticipantsPublishedTracksList))
    }

    "correctly parse VideoCompositionsCompositionHooksAll policy" in {
      val policy =
        ApiKeyPolicy.fromTwilioString("/twilio/video/compositions.composition-hooks/*")
      assert(policy === Some(ApiKeyPolicy.VideoCompositionsCompositionHooksAll))
    }
  }
}
