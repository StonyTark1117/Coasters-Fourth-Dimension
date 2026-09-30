package dev.fourthdimension.track;

import dev.fourthdimension.contract.*;
import dev.fourthdimension.contract.TrackContract.*;
import dev.fourthdimension.physics.RotationProfile;
import java.util.Set;

/** Original game-scale interfaces, measured in blocks relative to the main-rail centerline. */
public enum TrackDesign {
  ARROW(Family.ARROW, "arrow_four_rail_v1", 0xb43b48),
  LEGACY(Family.ZACSPIN, "zacspin_legacy_v1", 0xd8a32b);
  // Coasters 0.1.5 wheel renderer: path anchor .3125 + compensation .1875 + axle .1.
  // Main and upstop wheel rows are .55 apart, each with nominal radius .2.
  public static final double HALF_GAUGE = .772;
  public static final double MAIN_RAIL_HEIGHT = .325;
  public static final double RAIL_HALF_WIDTH = .13125, RAIL_HALF_HEIGHT = .075;
  public static final double WHEEL_RADIUS = .2, WHEEL_ROW_SPACING = .55;
  public static final double TREAD_INNER = .625, TREAD_OUTER = .9375;
  public static final double CONTROL_HALF_GAUGE = 1.35, CONTROL_BASE_HEIGHT = -.85;
  // Direct rack approximation: 8 pi radians use .5 blocks of signed stroke.
  public static final double BLOCKS_PER_RADIAN = .5 / RotationProfile.MAX_ANGLE;
  public final Family family;
  public final String variant;
  public final int color;

  TrackDesign(Family family, String variant, int color) {
    this.family = family;
    this.variant = variant;
    this.color = color;
  }

  public TrackContract contract() {
    return new TrackContract(
        "fourthdimension:original",
        family,
        variant,
        new Gear(2 * HALF_GAUGE, WHEEL_RADIUS, TREAD_OUTER, WHEEL_ROW_SPACING),
        family.driven()
            ? Set.of(Mechanism.ROTARY_CLEARANCE, Mechanism.CONTROL_RAILS)
            : Set.of(Mechanism.ROTARY_CLEARANCE),
        new Clearance(5, 5, 3));
  }

  public double controlHeight(double angle) {
    if (!Double.isFinite(angle) || Math.abs(angle) > RotationProfile.MAX_ANGLE + 1e-9)
      throw new IllegalArgumentException("Control rail stroke exceeded");
    return CONTROL_BASE_HEIGHT + angle * BLOCKS_PER_RADIAN;
  }

  public static TrackDesign byVariant(String id) {
    for (var value : values()) if (value.variant.equals(id)) return value;
    throw new IllegalArgumentException("Unknown original track: " + id);
  }
}
