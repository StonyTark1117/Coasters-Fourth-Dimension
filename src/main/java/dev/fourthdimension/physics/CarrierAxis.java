package dev.fourthdimension.physics;

import org.joml.Quaterniondc;
import org.joml.Vector3d;

/** Native plot-local joint axes. The same definition owns creation, sampling and restore. */
public enum CarrierAxis {
  X(1, 0, 0), Y(0, 1, 0);

  private final double x, y, z;

  CarrierAxis(double x, double y, double z) {
    if (Math.abs(x*x + y*y + z*z - 1) > 1e-12)
      throw new IllegalArgumentException("Joint axis must be a finite unit vector");
    this.x = x; this.y = y; this.z = z;
  }

  public Vector3d vector() { return new Vector3d(x, y, z); }

  /** Project the relative quaternion onto this joint's twist, retaining unwrapped turns. */
  public double angle(double previous, Quaterniondc relative) {
    return FreeRotor.unwrap(previous,
        2 * Math.atan2(x * relative.x() + y * relative.y() + z * relative.z(), relative.w()));
  }
}
