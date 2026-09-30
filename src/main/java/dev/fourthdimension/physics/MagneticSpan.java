package dev.fourthdimension.physics;

/** Original passive magnetic interface, in canonical section arc-length coordinates. */
public record MagneticSpan(
    double start,
    double end,
    Mode mode,
    int direction,
    double strength,
    double falloff,
    int targets) {
  public enum Mode {
    INDUCE,
    DAMP
  }

  public MagneticSpan {
    if (!Double.isFinite(start)
        || !Double.isFinite(end)
        || start < 0
        || end - start < .05
        || end > 256
        || mode == null
        || (direction != -1 && direction != 1)
        || !Double.isFinite(strength)
        || strength < 0
        || strength > 20
        || !Double.isFinite(falloff)
        || falloff < 0
        || falloff > .5
        || targets < 1
        || targets > 15)
      throw new IllegalArgumentException(
          "Magnet: span >= 0.05, strength 0..20, falloff 0..0.5, direction +/-1, targets 1..15");
  }

  public boolean targets(int index) {
    return (targets & (1 << index)) != 0;
  }

  public double weight(double distance) {
    if (distance < start || distance > end) return 0;
    double ramp = (end - start) * falloff;
    return ramp == 0 ? 1 : Math.min(1, Math.min(distance - start, end - distance) / ramp);
  }

  private double primitive(double distance) {
    double length = end - start, x = Math.max(0, Math.min(length, distance - start));
    double ramp = length * falloff;
    if (ramp == 0) return x;
    if (x < ramp) return x * x / (2 * ramp);
    if (x <= length - ramp) return x - ramp / 2;
    double tail = length - x;
    return length - ramp - tail * tail / (2 * ramp);
  }

  public double weightedLength(double from, double to) {
    if (!Double.isFinite(from) || !Double.isFinite(to))
      throw new IllegalArgumentException("Invalid sweep");
    return Math.abs(primitive(to) - primitive(from));
  }

  public MagneticSpan scaled(double factor) {
    return new MagneticSpan(
        start * factor, end * factor, mode, direction, strength, falloff, targets);
  }
}
