package dev.fourthdimension.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import dev.fourthdimension.runtime.EligibilityHooks;
import dev.fourthdimension.runtime.FoundationData;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.mixinterface.camera.camera_zoom.CameraZoomExtension;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/** Read-only evidence for native orbit-camera collision and connected-carrier membership. */
public final class CarrierCameraDiagnostics {
  private static long previous;

  private CarrierCameraDiagnostics() {}

  public static void sample(RenderLevelStageEvent event) {
    long now = System.nanoTime();
    if (now - previous < 250_000_000L) return;
    previous = now;
    var mc = Minecraft.getInstance();
    if (mc.level == null || mc.player == null || mc.player.getVehicle() == null) return;
    var data = EligibilityHooks.CLIENT.get(mc.level);
    if (data == null) return;
    if (!(Sable.HELPER.getContaining(mc.player.getVehicle()) instanceof ClientSubLevel body))
      return;
    var vehicle =
        data.vehicles.values().stream()
            .filter(v -> !FoundationData.VARIANT.equals(v.variant))
            .filter(v -> v.carriers.stream().anyMatch(c -> c.body.equals(body.getUniqueId())))
            .findFirst()
            .orElse(null);
    if (vehicle == null) return;
    var camera = event.getCamera();
    var position = camera.getPosition();
    var center = body.renderPose().position();
    var json = new JsonObject();
    json.addProperty("player", mc.player.getName().getString());
    json.addProperty("body", body.getUniqueId().toString());
    json.addProperty("chassis", vehicle.chassis.toString());
    json.addProperty("mode", mc.options.getCameraType().name());
    json.addProperty("zoom", ((CameraZoomExtension) camera).sable$getZoomAmount());
    json.addProperty(
        "distance",
        position.distanceTo(new net.minecraft.world.phys.Vec3(center.x(), center.y(), center.z())));
    json.addProperty("cameraX", position.x);
    json.addProperty("cameraY", position.y);
    json.addProperty("cameraZ", position.z);
    json.addProperty("centerX", center.x());
    json.addProperty("centerY", center.y());
    json.addProperty("centerZ", center.z());
    json.addProperty("yaw", mc.player.getYRot());
    json.addProperty("pitch", mc.player.getXRot());
    var group = new JsonArray();
    SubLevelHelper.getConnectedChain(body).stream()
        .map(b -> b.getUniqueId().toString())
        .sorted()
        .forEach(group::add);
    json.add("group", group);
    var expected = new JsonArray();
    expected.add(vehicle.chassis.toString());
    vehicle.carriers.forEach(c -> expected.add(c.body.toString()));
    json.add("vehicleBodies", expected);
    LogUtils.getLogger().info("FD_CAMERA_GROUP {}", json);
  }
}
