package dev.fourthdimension.physics;

/** Bounded, unwrapped constraint target; the native engine still owns actual angular motion. */
public final class RotationServo {
  public static final double MAX_SPEED = 3, MAX_ACCELERATION = 6;
  private double angle, velocity;

  public RotationServo(double angle) {
    if (!Double.isFinite(angle)) throw new IllegalArgumentException("Invalid initial angle");
    this.angle = angle;
  }

  public double advance(double goal, double actual, double dt) {
    if (!Double.isFinite(goal)
        || !Double.isFinite(actual)
        || !Double.isFinite(dt)
        || dt <= 0
        || dt > .25) throw new IllegalArgumentException("Invalid servo step");
    double error = goal - angle;
    double brakingSpeed = Math.sqrt(2 * MAX_ACCELERATION * Math.abs(error));
    double desired = Math.copySign(Math.min(MAX_SPEED, brakingSpeed), error);
    velocity += Math.clamp(desired - velocity, -MAX_ACCELERATION * dt, MAX_ACCELERATION * dt);
    double next = angle + velocity * dt;
    if ((goal - angle) * (goal - next) <= 0) {
      next = goal;
      velocity = 0;
    }
    // Keep the next quaternion target near the actual body so no skipped full turn can take a
    // shortcut.
    next = Math.clamp(next, actual - .35, actual + .35);
    angle = next;
    return angle;
  }

  public double velocity() {
    return velocity;
  }
}
