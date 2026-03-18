// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.model

import org.scalatest.wordspec.AnyWordSpec

final class PublicEdgeLocationTest extends AnyWordSpec {

  "PublicEdgeLocation.fromLegacyRegion" should {

    "map every LegacyRegion value to a PublicEdgeLocation that contains it" in {
      PublicEdgeLocation.LegacyRegion.values.foreach { legacyRegion =>
        val location = PublicEdgeLocation.fromLegacyRegion(legacyRegion)
        assert(
          location.legacyRegionId.contains(legacyRegion),
          s"fromLegacyRegion($legacyRegion) returned $location, which does not contain $legacyRegion"
        )
      }
    }
  }

  "PublicEdgeLocation.fromLegacyRegionCode" should {

    "map every LegacyRegion id to a Some(PublicEdgeLocation) that contains the corresponding LegacyRegion" in {
      PublicEdgeLocation.LegacyRegion.values.foreach { legacyRegion =>
        val result = PublicEdgeLocation.fromLegacyRegionCode(legacyRegion.id)
        assert(result.isDefined, s"fromLegacyRegionCode(${legacyRegion.id}) returned None")
        assert(
          result.exists(_.legacyRegionId.contains(legacyRegion)),
          s"fromLegacyRegionCode(${legacyRegion.id}) returned $result, which does not contain $legacyRegion"
        )
      }
    }

    "return None for an unknown code" in {
      assert(PublicEdgeLocation.fromLegacyRegionCode("unknown") == None)
    }
  }
}
