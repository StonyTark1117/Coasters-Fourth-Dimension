package dev.fourthdimension.integration.immersion;

import dev.fourthdimension.runtime.EligibilityHooks;
import dev.fourthdimension.runtime.FoundationData;
import dev.mito.coastersimmersion.config.ImmersionClientConfig;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import org.joml.Quaterniond;

/** Client-only adapter state. Sable remains the sole owner of the physical camera transform. */
public final class ImmersionCameraContext {
  private static Level previousLevel;
  private static UUID previousCarrier;
  private static UUID previousRider;
  private static UUID previousSeat;
  private static boolean previousEnabled;

  private ImmersionCameraContext() {}

  public static ClientSubLevel carrier() {
    var mc = Minecraft.getInstance();
    var rider = mc.getCameraEntity();
    if (mc.level == null || rider == null || rider != mc.player || rider.getVehicle() == null)
      return null;
    var body = Sable.HELPER.getContaining(rider.getVehicle());
    if (!(body instanceof ClientSubLevel client)) return null;
    var data = EligibilityHooks.CLIENT.get(mc.level);
    if (data == null) return null;
    for (var vehicle : data.vehicles.values()) {
      if (FoundationData.VARIANT.equals(vehicle.variant)) continue;
      for (var carrier : vehicle.carriers)
        if (carrier.body.equals(body.getUniqueId())) return client;
    }
    return null;
  }

  /**
   * Raw authoritative carrier pose, independent of mouse look, camera smoothing and comfort
   * options.
   */
  public static Quaterniond orientation() {
    var body = carrier();
    return body == null ? null : new Quaterniond(body.renderPose().orientation());
  }

  /** Reset transient addon effects on dismount, carrier/world change, or camera master toggle. */
  public static boolean changed() {
    var mc = Minecraft.getInstance();
    var body = carrier();
    UUID id = body == null ? null : body.getUniqueId();
    UUID rider = mc.player == null ? null : mc.player.getUUID();
    UUID seat =
        mc.player == null || mc.player.getVehicle() == null
            ? null
            : mc.player.getVehicle().getUUID();
    boolean enabled = ImmersionClientConfig.cinematicEnabled();
    boolean reset =
        (id != null || previousCarrier != null)
            && (mc.level != previousLevel
                || !java.util.Objects.equals(id, previousCarrier)
                || !java.util.Objects.equals(rider, previousRider)
                || !java.util.Objects.equals(seat, previousSeat)
                || enabled != previousEnabled);
    previousLevel = mc.level;
    previousCarrier = id;
    previousRider = rider;
    previousSeat = seat;
    previousEnabled = enabled;
    return reset;
  }
}
