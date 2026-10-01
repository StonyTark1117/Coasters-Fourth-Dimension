package dev.fourthdimension.physics;

import org.joml.Quaterniondc;
import org.joml.Vector3d;

/** Converts saved carrier-local mouse angles to the world-space input expected on boarding. */
public record RiderLook(float yaw, float pitch) {
  public static RiderLook toWorld(float localYaw, float localPitch, Quaterniondc carrier) {
    double yaw = Math.toRadians(localYaw), pitch = Math.toRadians(localPitch);
    var direction =
        new Vector3d(
                -Math.sin(yaw) * Math.cos(pitch), -Math.sin(pitch), Math.cos(yaw) * Math.cos(pitch))
            .rotate(carrier);
    return new RiderLook(
        (float) Math.toDegrees(Math.atan2(-direction.x, direction.z)),
        (float) Math.toDegrees(Math.atan2(-direction.y, Math.hypot(direction.x, direction.z))));
  }
}
