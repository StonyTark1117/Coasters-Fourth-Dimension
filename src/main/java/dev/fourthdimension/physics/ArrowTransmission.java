package dev.fourthdimension.physics;

/** Shared dimensions and finite spring travel for the authored rail-following transmission. */
public final class ArrowTransmission {
  public static final double RATIO=8;
  public static final double RACK_PINION_RADIUS=1/(2*Math.PI);
  /** +/-22.5 degrees at the slow spring, equivalent to +/-pi at the actual seat axle. */
  public static final double MAX_AXLE_ERROR=Math.PI;
  public static final double ENGAGE_AXLE_ERROR=Math.PI-.15;
  public static final String OVERTRAVEL="Arrow control coupling overtravel: capture and inspect the profile";
  private ArrowTransmission() {}

  public static double slowAngle(double unwrappedAxleAngle) { return unwrappedAxleAngle/RATIO; }
  public static boolean withinSpring(double input,double actual) {
    return Double.isFinite(input)&&Double.isFinite(actual)&&Math.abs(input-actual)<=MAX_AXLE_ERROR;
  }
  public static boolean mayEngage(double input,double actual) {
    return Double.isFinite(input)&&Double.isFinite(actual)&&Math.abs(input-actual)<=ENGAGE_AXLE_ERROR;
  }
}
