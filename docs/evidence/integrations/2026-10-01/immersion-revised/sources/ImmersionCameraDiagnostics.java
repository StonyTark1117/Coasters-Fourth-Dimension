package dev.fourthdimension.integration.immersion;

import com.mojang.logging.LogUtils;
import dev.mito.coastersimmersion.client.camera.BodyTravel;
import dev.mito.coastersimmersion.config.ImmersionClientConfig;
import dev.mito.coastersimmersion.smooth.CameraFilter;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3d;

/** Opt-in measurements of the rendered view; never changes the camera, rider or physics. */
public final class ImmersionCameraDiagnostics {
  private static long previous;

  private ImmersionCameraDiagnostics() {}

  public static void sample(RenderLevelStageEvent event) {
    long now = System.nanoTime();
    if (now - previous < 250_000_000L) return;
    previous = now;
    var mc = Minecraft.getInstance();
    if (mc.player == null || mc.level == null) return;
    var body = ImmersionCameraContext.carrier();
    var raw = CameraFilter.freshOrientation();
    var pose = body == null ? null : body.renderPose().orientation();
    double error = -1;
    if (raw != null && pose != null) {
      double norm = Math.sqrt(raw.lengthSquared() * pose.lengthSquared());
      double dot = raw.x * pose.x() + raw.y * pose.y() + raw.z * pose.z() + raw.w * pose.w();
      error = norm > 0 ? 2 * Math.acos(Math.min(1, Math.abs(dot) / norm)) : Double.NaN;
    }
    var up = pose == null ? new Vector3d(0, 1, 0) : new Vector3d(0, 1, 0).rotate(pose);
    var camera = event.getCamera();
    LogUtils.getLogger()
        .info(
            "FD_IMMERSION metric=normalized-v1 body={} riding={} enabled={} processing={}"
                + " rawError={} carrierUp={} cameraUp={} yaw={} pitch={} travel={} inertia={}"
                + " perspective={} camera={}",
            body == null ? "none" : body.getUniqueId(),
            mc.player.isPassenger(),
            ImmersionClientConfig.cinematicEnabled(),
            ImmersionClientConfig.cameraProcessingActive(),
            error,
            up.y,
            camera.getUpVector().y,
            mc.player.getYRot(),
            mc.player.getXRot(),
            BodyTravel.offset(),
            ImmersionClientConfig.headInertiaSeconds(),
            mc.options.getCameraType(),
            camera.rotation());
  }
}
