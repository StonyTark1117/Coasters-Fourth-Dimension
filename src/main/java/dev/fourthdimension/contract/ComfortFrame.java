package dev.fourthdimension.contract;

import org.joml.Quaterniond;
import org.joml.Vector3d;

/** Presentation-only orientation math; no physical pose mutation. */
public final class ComfortFrame {
  private ComfortFrame() {}
  /** A finite upright frame even when activation occurs at vertical or inverted pitch. */
  public static Quaterniond uprightFrame(org.joml.Quaterniondc orientation) {
    var forward = new Vector3d(0, 0, 1).rotate(orientation);
    double yaw;
    if (forward.x * forward.x + forward.z * forward.z > 1e-6)
      yaw = Math.atan2(forward.x, forward.z);
    else {
      var right = new Vector3d(1, 0, 0).rotate(orientation);
      yaw = Math.atan2(-right.z, right.x);
    }
    return new Quaterniond().rotationY(yaw);
  }
}
