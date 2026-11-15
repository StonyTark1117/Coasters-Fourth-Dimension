package dev.fourthdimension.physics;

/** Bounded, unwrapped constraint target; the native engine still owns actual angular motion. */
public final class RotationServo {
  public static final double MAX_SPEED = 3, MAX_ACCELERATION = 6;
  private double angle, velocity;
  private final double maxSpeed, maxAcceleration;
  private final boolean boundedStop;

  public RotationServo(double angle) {
    this(angle, MAX_SPEED, MAX_ACCELERATION, false);
  }

  public RotationServo(double angle, double maxSpeed, double maxAcceleration) {
    this(angle, maxSpeed, maxAcceleration, true);
  }

  private RotationServo(double angle, double maxSpeed, double maxAcceleration, boolean boundedStop) {
    if (!Double.isFinite(angle)) throw new IllegalArgumentException("Invalid initial angle");
    if (!Double.isFinite(maxSpeed) || maxSpeed <= 0
        || !Double.isFinite(maxAcceleration) || maxAcceleration <= 0)
      throw new IllegalArgumentException("Invalid servo limits");
    this.angle = angle;
    this.maxSpeed = maxSpeed;
    this.maxAcceleration = maxAcceleration;
    this.boundedStop = boundedStop;
  }

  public double advance(double goal, double actual, double dt) {
    if (!Double.isFinite(goal)
        || !Double.isFinite(actual)
        || !Double.isFinite(dt)
        || dt <= 0
        || dt > .25) throw new IllegalArgumentException("Invalid servo step");
    double error = goal - angle;
    double brakingSpeed = boundedStop
        ? Math.sqrt(2 * maxAcceleration * Math.abs(error) + Math.pow(maxAcceleration * dt, 2))
            - maxAcceleration * dt
        : Math.sqrt(2 * maxAcceleration * Math.abs(error));
    double desired = Math.copySign(Math.min(maxSpeed, brakingSpeed), error);
    velocity += Math.clamp(desired - velocity, -maxAcceleration * dt, maxAcceleration * dt);
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
