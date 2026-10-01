package dev.fourthdimension;

import static org.junit.jupiter.api.Assertions.*;

import dev.fourthdimension.integration.TrackAppearance;
import java.util.List;
import org.junit.jupiter.api.Test;

class TrackAppearanceTest {
  @Test
  void decorativeStrokeFallbackDoesNotClampOrEraseFullTurns() {
    for (var id : TrackAppearance.CANDIDATES) {
      var appearance = new TrackAppearance(id, 0x123456);
      for (double peak : new double[] {-8 * Math.PI, 0, 2 * Math.PI, 4 * Math.PI}) {
        var profile = profile(peak);
        assertTrue(appearance.supportsProfile(profile));
      }
      for (double peak : new double[] {Math.nextUp(4 * Math.PI), 6 * Math.PI, 8 * Math.PI}) {
        var profile = profile(peak);
        assertFalse(appearance.supportsProfile(profile));
        assertEquals(peak, profile.angleAt(16));
        assertTrue(TrackAppearance.DEFAULT.supportsProfile(profile));
        assertEquals(appearance, TrackAppearance.parse(id + ";#123456"));
      }
    }
  }

  private static dev.fourthdimension.physics.RotationProfile profile(double peak) {
    return new dev.fourthdimension.physics.RotationProfile(
        List.of(
            new dev.fourthdimension.physics.RotationProfile.Key(0, 0),
            new dev.fourthdimension.physics.RotationProfile.Key(16, peak),
            new dev.fourthdimension.physics.RotationProfile.Key(32, 0)));
  }

  @Test
  void arrowDecorationDoesNotClaimOtherFamilyGeometry() {
    for (var family : dev.fourthdimension.contract.Family.values()) {
      assertTrue(TrackAppearance.DEFAULT.supports(family));
      for (var id : TrackAppearance.CANDIDATES)
        assertEquals(
            family == dev.fourthdimension.contract.Family.ARROW,
            new TrackAppearance(id, -1).supports(family));
      assertFalse(new TrackAppearance("future:missing", -1).supports(family));
    }
  }

  @Test
  void missingProviderIntentAndRgbAreBoundedAndPreserved() {
    var missing = TrackAppearance.parse("future:spine;#12aBCd");
    assertEquals("future:spine", missing.id());
    assertEquals(0x12abcd, missing.tint());
    assertEquals("#12ABCD", missing.formattedColor());
    assertEquals(missing, TrackAppearance.parse(missing.id() + ";" + missing.formattedColor()));
    assertFalse(missing.original());
    assertEquals(TrackAppearance.DEFAULT, TrackAppearance.parse("fourthdimension:original;"));
    for (String invalid :
        List.of(
            "future:style",
            "future:style;#12345",
            "future:style;#1234567",
            "future:style;#12GG00",
            "future:style;NaN",
            "future:style;;",
            "INVALID:style;",
            "future:" + "x".repeat(96) + ";"))
      assertThrows(IllegalArgumentException.class, () -> TrackAppearance.parse(invalid));
    assertThrows(IllegalArgumentException.class, () -> new TrackAppearance("future:spine", -2));
    assertThrows(
        IllegalArgumentException.class, () -> new TrackAppearance("future:spine", 0x1000000));
  }
}
