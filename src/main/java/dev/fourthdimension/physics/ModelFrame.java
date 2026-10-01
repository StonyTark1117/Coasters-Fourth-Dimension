package dev.fourthdimension.physics;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector4f;

/**
 * Right-handed model +Z frame. Coaster travel/right conventions need not share model handedness.
 */
public final class ModelFrame {
  private ModelFrame() {}

  public static Matrix4f at(double x, double y, double z, Vector3d forward, Vector3d up) {
    if (!Double.isFinite(x)
        || !Double.isFinite(y)
        || !Double.isFinite(z)
        || !forward.isFinite()
        || !up.isFinite()
        || forward.lengthSquared() < 1e-12)
      throw new IllegalArgumentException("Invalid model frame");
    var f = new Vector3d(forward).normalize();
    var r = new Vector3d(up).cross(f);
    if (r.lengthSquared() < 1e-12)
      throw new IllegalArgumentException("Model up is parallel to forward");
    r.normalize();
    var u = new Vector3d(f).cross(r).normalize();
    return new Matrix4f()
        .setColumn(0, new Vector4f((float) r.x, (float) r.y, (float) r.z, 0))
        .setColumn(1, new Vector4f((float) u.x, (float) u.y, (float) u.z, 0))
        .setColumn(2, new Vector4f((float) f.x, (float) f.y, (float) f.z, 0))
        .setColumn(3, new Vector4f((float) x, (float) y, (float) z, 1));
  }
}
