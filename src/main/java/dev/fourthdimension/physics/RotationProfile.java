package dev.fourthdimension.physics;

import java.util.List;

/** Distance in blocks, angle in radians; values stay unwrapped across complete revolutions. */
public record RotationProfile(List<Key> keys) {
  public record Key(double distance, double angle) {
    public Key {
      if (!Double.isFinite(distance) || !Double.isFinite(angle))
        throw new IllegalArgumentException("Non-finite profile");
    }
  }

  public RotationProfile {
    keys = List.copyOf(keys);
    if (keys.size() < 2 || keys.size() > 1024)
      throw new IllegalArgumentException("Expected 2..1024 keys");
    for (int i = 1; i < keys.size(); i++)
      if (keys.get(i).distance <= keys.get(i - 1).distance)
        throw new IllegalArgumentException("Distances must increase");
  }

  public double angleAt(double distance) {
    if (!Double.isFinite(distance)) throw new IllegalArgumentException("Non-finite distance");
    if (distance <= keys.getFirst().distance) return keys.getFirst().angle;
    for (int i = 1; i < keys.size(); i++) {
      Key a = keys.get(i - 1), b = keys.get(i);
      if (distance <= b.distance) {
        double t = (distance - a.distance) / (b.distance - a.distance);
        double smooth = t * t * (3 - 2 * t);
        return a.angle + smooth * (b.angle - a.angle);
      }
    }
    return keys.getLast().angle;
  }
}
