package dev.fourthdimension.physics;

import java.util.List;

/** Distance in blocks; unwrapped radians and radians/block. Monotone cubic Hermite segments. */
public record RotationProfile(List<Key> keys) {
  public static final double MAX_ANGLE = 8 * Math.PI;

  public record Key(double distance, double angle, double slope) {
    public Key(double distance, double angle) {
      this(distance, angle, 0);
    }

    public Key {
      if (!Double.isFinite(distance)
          || distance < 0
          || distance > 65536
          || !Double.isFinite(angle)
          || Math.abs(angle) > MAX_ANGLE
          || !Double.isFinite(slope)
          || Math.abs(slope) > Math.PI)
        throw new IllegalArgumentException("Profile key outside finite mechanical bounds");
    }
  }

  public RotationProfile {
    keys = List.copyOf(keys);
    if (keys.size() < 2 || keys.size() > 1024)
      throw new IllegalArgumentException("Expected 2..1024 keys");
    for (int i = 1; i < keys.size(); i++) {
      Key a = keys.get(i - 1), b = keys.get(i);
      double length = b.distance - a.distance;
      if (length <= 0) throw new IllegalArgumentException("Distances must increase");
      double secant = (b.angle - a.angle) / length;
      // Sufficient monotonicity constraint: no intermediate overshoot beyond rail stroke.
      if (Math.abs(secant) < 1e-12) {
        if (a.slope != 0 || b.slope != 0)
          throw new IllegalArgumentException("Flat span must have flat tangents");
      } else {
        double alpha = a.slope / secant, beta = b.slope / secant;
        if (alpha < 0 || beta < 0 || alpha * alpha + beta * beta > 9 + 1e-12)
          throw new IllegalArgumentException("Profile tangents overshoot mechanical stroke");
      }
    }
  }

  public double angleAt(double distance) {
    return evaluate(distance, false);
  }

  public double slopeAt(double distance) {
    return evaluate(distance, true);
  }

  private double evaluate(double distance, boolean derivative) {
    if (!Double.isFinite(distance)) throw new IllegalArgumentException("Non-finite distance");
    if (distance < keys.getFirst().distance) return derivative ? 0 : keys.getFirst().angle;
    for (int i = 1; i < keys.size(); i++) {
      Key a = keys.get(i - 1), b = keys.get(i);
      if (distance <= b.distance) {
        double h = b.distance - a.distance, t = (distance - a.distance) / h;
        if (derivative)
          return ((6 * t * t - 6 * t) * a.angle + (-6 * t * t + 6 * t) * b.angle) / h
              + (3 * t * t - 4 * t + 1) * a.slope
              + (3 * t * t - 2 * t) * b.slope;
        return (2 * t * t * t - 3 * t * t + 1) * a.angle
            + (-2 * t * t * t + 3 * t * t) * b.angle
            + (t * t * t - 2 * t * t + t) * h * a.slope
            + (t * t * t - t * t) * h * b.slope;
      }
    }
    return derivative ? 0 : keys.getLast().angle;
  }

  public boolean joins(RotationProfile next) {
    Key end = keys.getLast(), start = next.keys.getFirst();
    return Math.abs(end.angle - start.angle) < 1e-7 && Math.abs(end.slope - start.slope) < 1e-7;
  }

  public RotationProfile mirrored() {
    return new RotationProfile(
        keys.stream().map(k -> new Key(k.distance, -k.angle, -k.slope)).toList());
  }

  public RotationProfile reversed() {
    double length = keys.getLast().distance;
    return new RotationProfile(
        keys.reversed().stream()
            .map(k -> new Key(length - k.distance, k.angle, -k.slope))
            .toList());
  }

  public RotationProfile scaledTo(double length) {
    double old = keys.getLast().distance - keys.getFirst().distance;
    if (!Double.isFinite(length) || length <= 0)
      throw new IllegalArgumentException("Invalid profile length");
    return new RotationProfile(
        keys.stream()
            .map(
                k ->
                    new Key(
                        (k.distance - keys.getFirst().distance) * length / old,
                        k.angle,
                        k.slope * old / length))
            .toList());
  }
}
