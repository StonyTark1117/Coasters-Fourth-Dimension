package dev.fourthdimension.integration.additions;

import dev.fourthdimension.integration.ExternalDrivePolicy;
import dev.fourthdimension.runtime.FoundationData;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import org.joml.*;

public final class AdditionsForces {
  private AdditionsForces() {}

  public static boolean owns(ServerLevel level, UUID body) {
    return FoundationData.get(level).vehicles.values().stream()
        .filter(v -> !FoundationData.VARIANT.equals(v.variant))
        .anyMatch(
            v -> v.chassis.equals(body) || v.carriers.stream().anyMatch(c -> c.body.equals(body)));
  }

  /** Apply the native velocity increment only to this vehicle's real joint-connected bodies. */
  public static boolean apply(
      SubLevelPhysicsSystem physics,
      ServerSubLevelContainer container,
      ServerSubLevel chassis,
      Vector3dc delta) {
    var level = physics.getLevel();
    var vehicle = FoundationData.get(level).vehicles.get(chassis.getUniqueId());
    if (vehicle == null || FoundationData.VARIANT.equals(vehicle.variant)) return false;
    if (!ExternalDrivePolicy.allows(level, vehicle.chassis) || !delta.isFinite()) return true;
    var bodies = new ArrayList<ServerSubLevel>();
    bodies.add(chassis);
    for (var carrier : vehicle.carriers) {
      if (!(container.getSubLevel(carrier.body) instanceof ServerSubLevel body) || body.isRemoved())
        return true;
      bodies.add(body);
    }
    var pipeline = physics.getPipeline();
    for (var body : bodies) {
      pipeline.wakeUp(body);
      pipeline.addLinearAndAngularVelocity(body, delta, new Vector3d());
    }
    return true;
  }
}
