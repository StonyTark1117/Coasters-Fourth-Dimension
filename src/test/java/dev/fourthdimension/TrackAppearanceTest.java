package dev.fourthdimension;

import static org.junit.jupiter.api.Assertions.*;

import dev.fourthdimension.integration.TrackAppearance;
import java.util.List;
import org.junit.jupiter.api.Test;

class TrackAppearanceTest {
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
