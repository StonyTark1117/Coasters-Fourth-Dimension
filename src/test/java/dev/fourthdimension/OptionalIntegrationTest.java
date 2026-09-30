package dev.fourthdimension;

import static org.junit.jupiter.api.Assertions.*;

import dev.fourthdimension.integration.OptionalIntegrations;
import dev.fourthdimension.integration.TrackRenderOwnership;
import java.util.EnumMap;
import org.junit.jupiter.api.Test;

class OptionalIntegrationTest {
  @Test
  void onlyAuditedExactReleasesCanResolveOptionalClasses() {
    assertFalse(
        OptionalIntegrations.classify(OptionalIntegrations.TRACK_STYLES, null).mayInspect());
    assertFalse(
        OptionalIntegrations.classify(OptionalIntegrations.TRACK_STYLES, "1.1.0").mayInspect());
    assertFalse(
        OptionalIntegrations.classify(OptionalIntegrations.TRACK_STYLES, "1.2.1").mayInspect());
    assertFalse(OptionalIntegrations.classify("unknown_provider", "1.2.0").mayInspect());
    assertTrue(
        OptionalIntegrations.classify(OptionalIntegrations.TRACK_STYLES, "1.2.0").mayInspect());
    assertTrue(
        OptionalIntegrations.classify(OptionalIntegrations.ENGINEERED, "1.1.1").mayInspect());
  }

  @Test
  void decorationCannotReplaceMechanicalRenderOwners() {
    var appearance = TrackRenderOwnership.decoration("example:cosmetic");
    assertFalse(appearance.originalOwns(TrackRenderOwnership.Component.SPINE));
    assertFalse(appearance.originalOwns(TrackRenderOwnership.Component.TIES));
    for (var component :
        new TrackRenderOwnership.Component[] {
          TrackRenderOwnership.Component.RUNNING_RAILS,
          TrackRenderOwnership.Component.CONTROL_RAILS,
          TrackRenderOwnership.Component.MAGNETIC_EQUIPMENT
        }) {
      assertTrue(appearance.originalOwns(component));
      var illegal = new EnumMap<>(appearance.owners());
      illegal.put(component, "example:cosmetic");
      assertThrows(IllegalArgumentException.class, () -> new TrackRenderOwnership(illegal));
    }
    var incomplete = new EnumMap<>(appearance.owners());
    incomplete.remove(TrackRenderOwnership.Component.SPINE);
    assertThrows(IllegalArgumentException.class, () -> new TrackRenderOwnership(incomplete));
    assertThrows(UnsupportedOperationException.class, () -> appearance.owners().clear());
    for (var component : TrackRenderOwnership.Component.values())
      assertTrue(TrackRenderOwnership.original().originalOwns(component));
  }
}
