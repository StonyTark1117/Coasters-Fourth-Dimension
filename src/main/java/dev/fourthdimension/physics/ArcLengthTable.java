package dev.fourthdimension.physics;

import java.util.Arrays;
import java.util.function.DoubleFunction;

/** Immutable distance/parameter map. Sampling occurs once per geometry revision. */
public final class ArcLengthTable {
  public record Point(double x, double y, double z) {
    public Point {
      if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z))
        throw new IllegalArgumentException("Non-finite curve point");
    }

    double distance(Point other) {
      return Math.hypot(Math.hypot(x - other.x, y - other.y), z - other.z);
    }
  }

  private final double[] distances;

  public ArcLengthTable(DoubleFunction<Point> curve, int samples) {
    if (samples < 16 || samples > 16384)
      throw new IllegalArgumentException("Curve resolution out of bounds");
    distances = new double[samples + 1];
    Point previous = curve.apply(0);
    for (int i = 1; i <= samples; i++) {
      Point next = curve.apply((double) i / samples);
      distances[i] = distances[i - 1] + previous.distance(next);
      previous = next;
    }
    if (!Double.isFinite(length()) || length() < 1e-6)
      throw new IllegalArgumentException("Degenerate curve");
  }

  public double length() {
    return distances[distances.length - 1];
  }

  public double distanceAt(double parameter) {
    requireFinite(parameter);
    double index = Math.clamp(parameter, 0, 1) * (distances.length - 1);
    int lo = Math.min((int) index, distances.length - 2);
    return distances[lo] + (distances[lo + 1] - distances[lo]) * (index - lo);
  }

  public double parameterAt(double distance) {
    requireFinite(distance);
    if (distance <= 0) return 0;
    if (distance >= length()) return 1;
    int found = Arrays.binarySearch(distances, distance);
    if (found >= 0) return (double) found / (distances.length - 1);
    int hi = -found - 1, lo = hi - 1;
    return (lo + (distance - distances[lo]) / (distances[hi] - distances[lo]))
        / (distances.length - 1);
  }

  private static void requireFinite(double value) {
    if (!Double.isFinite(value)) throw new IllegalArgumentException("Non-finite curve coordinate");
  }
}
