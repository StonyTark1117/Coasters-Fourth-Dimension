package dev.fourthdimension.gametest;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.track.TrackDesign;
import java.util.List;

/** Stable inputs/spacing for the original-five fixtures; expanded designs have their own cases. */
final class OriginalFamilyCases {
  private static final List<TrackDesign> DESIGNS = List.of(
      TrackDesign.ARROW, TrackDesign.LEGACY, TrackDesign.FREE_SPIN,
      TrackDesign.REVISED, TrackDesign.PAX);
  private OriginalFamilyCases() {}
  static TrackDesign[] designs() { return DESIGNS.toArray(TrackDesign[]::new); }
  static Family[] families() { return DESIGNS.stream().map(d -> d.family).toArray(Family[]::new); }
  static int index(TrackDesign design) {
    int index = DESIGNS.indexOf(design);
    if (index < 0) throw new IllegalArgumentException("Not an original-five fixture: " + design);
    return index;
  }
}
