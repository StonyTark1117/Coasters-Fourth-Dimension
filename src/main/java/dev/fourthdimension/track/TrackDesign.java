package dev.fourthdimension.track;

import dev.fourthdimension.contract.*;
import dev.fourthdimension.contract.TrackContract.*;
import dev.fourthdimension.physics.RotationProfile;
import java.util.Set;

/** Original game-scale interfaces, measured in blocks relative to the main-rail centerline. */
public enum TrackDesign {
  POWERED_INVERTED_YAW(Family.POWERED_INVERTED, "upright_yaw_v1", 0x2d94ae),
  DIVE(Family.DIVE, "six_abreast_v1", 0xd8ab31),
  PIPELINE(Family.PIPELINE, "seated_side_rail_v1", 0x327fa8),
  ARROW(Family.ARROW, "arrow_four_rail_v1", 0xb43b48),
  LEGACY(Family.ZACSPIN, "zacspin_legacy_v1", 0xd8a32b),
  FREE_SPIN(Family.FREE_SPIN, "sns_magnetic_v1", 0x377fbe),
  REVISED(Family.ZACSPIN_REVISED, "zacspin_revised_v1", 0x9650c7),
  PAX(Family.PAX, "pax_shuttle_v1", 0x3b9b88),
  S2_SPINNING(Family.SPINNING, "launched_inverting_v1", 0xb645a3),
  SPINNING(Family.SPINNING, "conventional_v1", 0xe16b30),
  SUSPENDED(Family.SUSPENDED, "lateral_swing_v1", 0xe88132),
  FIXED_INVERTED(Family.FIXED_INVERTED, "four_abreast_v1", 0x8558ac),
  AXIS(Family.AXIS, "longitudinal_roll_v1", 0x25b7ba),
  FLYING(Family.FLYING, "station_changeover_v1", 0x379ba6),
  NEW_GENERATION_FLYING(Family.FLYING, "route_changeover_yaw_v1", 0x705ed6),
  ROLLER_BALL(Family.ROLLER_BALL, "vertical_zigzag_v1", 0x92b83d);
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

  public boolean overhead() { return this == POWERED_INVERTED_YAW || this == SUSPENDED || this == FIXED_INVERTED || this == FLYING || this == NEW_GENERATION_FLYING; }

  public NativeTrackFit nativeFit() {
    if (this == POWERED_INVERTED_YAW) return dev.fourthdimension.vehicle.PoweredInvertedYawVehicle.FIT;
    if (this == DIVE) return dev.fourthdimension.vehicle.DiveVehicle.FIT;
    if (this == PIPELINE) return dev.fourthdimension.vehicle.PipelineVehicle.FIT;
    if (this == S2_SPINNING) return dev.fourthdimension.vehicle.S2SpinningVehicle.FIT;
    if (this == NEW_GENERATION_FLYING) return dev.fourthdimension.vehicle.NewGenerationFlyingVehicle.FIT;
    if (this == ROLLER_BALL) return dev.fourthdimension.vehicle.RollerBallVehicle.FIT;
    if (this == AXIS) return dev.fourthdimension.vehicle.AxisVehicle.FIT;
    if (this == FLYING) return dev.fourthdimension.vehicle.FlyingVehicle.FIT;
    if (this == FIXED_INVERTED) return dev.fourthdimension.vehicle.FixedInvertedVehicle.FIT;
    if (overhead()) return new NativeTrackFit(NativeTrackFit.N_CONTACTS, NativeTrackFit.OVERHEAD,
        new NativeTrackFit.Envelope(3, 1.10, 4.70, 1.75));
    return new NativeTrackFit(NativeTrackFit.N_CONTACTS, NativeTrackFit.ABOVE,
        (this == SPINNING || this == S2_SPINNING) ? new NativeTrackFit.Envelope(2.25, 3.65, .95, 2.25)
            : new NativeTrackFit.Envelope(5, rotorRadius()+2.3125,
                rotorRadius()+(pairedRows()?3:0), rotorRadius()+(pairedRows()?3:0)));
  }

  public TrackContract contract() {
    return new TrackContract(
        "fourthdimension:original",
        family,
        variant,
        new Gear(this == PIPELINE ? 3 : 2 * HALF_GAUGE, WHEEL_RADIUS, this == PIPELINE ? 1.75625 : TREAD_OUTER, WHEEL_ROW_SPACING),
        this == POWERED_INVERTED_YAW ? Set.of(Mechanism.ROTARY_CLEARANCE,Mechanism.STATION_CAPTURE,Mechanism.OVERHEAD_MOUNT,Mechanism.YAW,Mechanism.LONGITUDINAL_DRIVE) : (this == DIVE || this == PIPELINE) ? Set.of(Mechanism.STATION_CAPTURE) : this == S2_SPINNING ? Set.of(Mechanism.ROTARY_CLEARANCE,Mechanism.STATION_CAPTURE,Mechanism.MAGNET_INTERFACE,Mechanism.LONGITUDINAL_DRIVE)
            : this == ROLLER_BALL ? Set.of(Mechanism.ROTARY_CLEARANCE, Mechanism.STATION_CAPTURE, Mechanism.LIFT_LOCK)
            : this == NEW_GENERATION_FLYING ? Set.of(Mechanism.ROTARY_CLEARANCE, Mechanism.STATION_CAPTURE, Mechanism.OVERHEAD_MOUNT, Mechanism.POSTURE_LOCK, Mechanism.YAW)
            : this == AXIS ? Set.of(Mechanism.ROTARY_CLEARANCE, Mechanism.STATION_CAPTURE, Mechanism.ROTATION_DRIVE, Mechanism.LIFT_LOCK)
            : this == FLYING ? Set.of(Mechanism.ROTARY_CLEARANCE, Mechanism.STATION_CAPTURE, Mechanism.OVERHEAD_MOUNT, Mechanism.POSTURE_LOCK)
            : this == FIXED_INVERTED ? Set.of(Mechanism.OVERHEAD_MOUNT, Mechanism.STATION_CAPTURE)
            : overhead() ? Set.of(Mechanism.ROTARY_CLEARANCE,Mechanism.STATION_CAPTURE,Mechanism.OVERHEAD_MOUNT,Mechanism.SWING_LIMITS,Mechanism.LIFT_LOCK)
            : (this == SPINNING || this == S2_SPINNING) ? Set.of(Mechanism.ROTARY_CLEARANCE, Mechanism.STATION_CAPTURE)
            : family.driven()
            ? Set.of(Mechanism.ROTARY_CLEARANCE, Mechanism.CONTROL_RAILS)
            : family == Family.FREE_SPIN
                ? Set.of(Mechanism.ROTARY_CLEARANCE, Mechanism.MAGNET_INTERFACE)
                : Set.of(Mechanism.ROTARY_CLEARANCE),
        this == POWERED_INVERTED_YAW ? new Clearance(1.5,1.1,4.5) : this == DIVE ? new Clearance(3.25,3.65,.95) : this == PIPELINE ? new Clearance(2.05,1,2.5) : this == ROLLER_BALL ? new Clearance(4.5,4.75,.75) : this == NEW_GENERATION_FLYING ? new Clearance(2.75,1.10,5.25) : this == AXIS ? new Clearance(6.5,4,1) : this == FLYING ? new Clearance(2.25,1.10,4.50) : this == FIXED_INVERTED ? new Clearance(2.25, 1.10, 4.50) : overhead() ? new Clearance(3, 1.10, 4.70) : (this == SPINNING || this == S2_SPINNING) ? new Clearance(2.25, 3.65, .95)
            : new Clearance(5, rotorRadius() + 2.3125, rotorRadius() + (pairedRows() ? 3 : 0)));
  }

  public boolean pairedRows() {
    return this == FREE_SPIN || this == REVISED;
  }

  public int groups() {
    return (this == DIVE || this == PIPELINE || this == ROLLER_BALL || this == SPINNING || this == S2_SPINNING || this == AXIS || overhead()) ? 1 : pairedRows() ? 4 : 2;
  }

  public double childPivotOffset() {
    return this == REVISED ? -.5 : 0;
  }

  public double rotorRadius() {
    return (this == SPINNING || this == S2_SPINNING) ? 2 : this == REVISED ? 3.2 : 2.7;
  }

  public double axleSpacing() {
    if(this == POWERED_INVERTED_YAW)return 6;
    return (this == DIVE || this == S2_SPINNING || this == PIPELINE || overhead() || this == AXIS) ? 8 : this == REVISED ? 14 : this == FREE_SPIN ? 12 : 6;
  }

  public boolean canLink() {
    return this == DIVE || this == S2_SPINNING || this == PIPELINE || this == ARROW || this == PAX || this == AXIS || overhead();
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
