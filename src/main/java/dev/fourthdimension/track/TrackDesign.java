package dev.fourthdimension.track;

import dev.fourthdimension.contract.*;
import dev.fourthdimension.contract.TrackContract.*;
import dev.fourthdimension.physics.RotationProfile;
import java.util.Set;

/** Original game-scale interfaces, measured in blocks relative to the main-rail centerline. */
public enum TrackDesign {
  ARROW(Family.ARROW, "arrow_four_rail_v1", 0xb43b48),
  LEGACY(Family.ZACSPIN, "zacspin_legacy_v1", 0xd8a32b),
  FREE_SPIN(Family.FREE_SPIN, "sns_magnetic_v1", 0x377fbe),
  REVISED(Family.ZACSPIN_REVISED, "zacspin_revised_v1", 0x9650c7),
  PAX(Family.PAX, "pax_shuttle_v1", 0x3b9b88),
  SPINNING(Family.SPINNING, "conventional_v1", 0xe16b30);
  // Coasters 0.1.5 wheel renderer: path anchor .3125 + compensation .1875 + axle .1.
  // Main and upstop wheel rows are .55 apart, each with nominal radius .2.
  public static final double HALF_GAUGE = .772;
  public static final double MAIN_RAIL_HEIGHT = .325;
  public static final double RAIL_HALF_WIDTH = .13125, RAIL_HALF_HEIGHT = .075;
  public static final double WHEEL_RADIUS = .2, WHEEL_ROW_SPACING = .55;
  public static final double TREAD_INNER = .625, TREAD_OUTER = .9375;
  public static final double SPINE_HEIGHT = -.55;
  public static final double RUNNING_CROSSBAR_RADIUS = .06;
  // The native cart renders a lower bearing through the main-rail centreline.
  // Keep decoration outboard of its full wheel envelope and join it below.
  public static final double RUNNING_OUTBOARD_HALF_GAUGE = 1.12;
  public static final double RUNNING_UNDERBRACE_HEIGHT = -.70;
  public static final double RUNNING_MOUNT_RADIUS = .08;
  public static final double CONTROL_SUPPORT_RADIUS = .055;
  public static final double MAGNET_HALF_GAUGE = 1.6, MAGNET_HEIGHT = .65, MAGNET_RADIUS = .12;
  public static final double MAGNET_SUPPORT_HEIGHT = -.55, MAGNET_SUPPORT_RADIUS = .055;
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

  public NativeTrackFit nativeFit() {
    return new NativeTrackFit(NativeTrackFit.N_CONTACTS, NativeTrackFit.ABOVE,
        this == SPINNING ? new NativeTrackFit.Envelope(2.25, 3.65, .95, 2.25)
            : new NativeTrackFit.Envelope(5, rotorRadius()+2.3125,
                rotorRadius()+(pairedRows()?3:0), rotorRadius()+(pairedRows()?3:0)));
  }

  public TrackContract contract() {
    return new TrackContract(
        "fourthdimension:original",
        family,
        variant,
        new Gear(2 * HALF_GAUGE, WHEEL_RADIUS, TREAD_OUTER, WHEEL_ROW_SPACING),
        this == SPINNING ? Set.of(Mechanism.ROTARY_CLEARANCE, Mechanism.STATION_CAPTURE)
            : family.driven()
            ? Set.of(Mechanism.ROTARY_CLEARANCE, Mechanism.CONTROL_RAILS)
            : family == Family.FREE_SPIN
                ? Set.of(Mechanism.ROTARY_CLEARANCE, Mechanism.MAGNET_INTERFACE)
                : Set.of(Mechanism.ROTARY_CLEARANCE),
        this == SPINNING ? new Clearance(2.25, 3.65, .95)
            : new Clearance(5, rotorRadius() + 2.3125, rotorRadius() + (pairedRows() ? 3 : 0)));
  }

  public boolean pairedRows() {
    return this == FREE_SPIN || this == REVISED;
  }

  public int groups() {
    return this == SPINNING ? 1 : pairedRows() ? 4 : 2;
  }

  public double childPivotOffset() {
    return this == REVISED ? -.5 : 0;
  }

  public double rotorRadius() {
    return this == SPINNING ? 2 : this == REVISED ? 3.2 : 2.7;
  }

  public double axleSpacing() {
    return this == REVISED ? 14 : this == FREE_SPIN ? 12 : 6;
  }

  public boolean canLink() {
    return this == ARROW || this == PAX;
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
