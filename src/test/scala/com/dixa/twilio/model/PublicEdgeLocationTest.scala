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
