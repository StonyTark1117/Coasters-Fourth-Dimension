package dev.fourthdimension.vehicle;

import dev.fourthdimension.runtime.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.graph.*;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;

public final class BoundaryGuard {
  private BoundaryGuard() {}

  public static void check(
      ServerLevel level,
      ServerSubLevel parent,
      FoundationData.Vehicle vehicle,
      FoundationData.Rail rail) {
    check(level, parent, vehicle, rail, new VehicleTrackSample(level, parent, rail));
  }

  /** The caller may reuse the projection before advancing physics or changing geometry. */
  public static void check(
      ServerLevel level,
      ServerSubLevel parent,
      FoundationData.Vehicle vehicle,
      FoundationData.Rail rail,
      VehicleTrackSample sample) {
    if (rail == null || !rail.valid) return;
    if (!ConsistRules.eligible(level, parent, vehicle)) {
      stop(vehicle, "Incompatible vehicle in linked consist");
      return;
    }
    // The validated shuttle controller guards both spikes and captures the complete train.
    if (dev.fourthdimension.shuttle.ShuttleRuntime.running(level, vehicle.chassis)) return;
    if (!sample.hasRunningGear()) {
      stop(vehicle, "Running gear unavailable");
      return;
    }
    var edge = sample.edge();
    if (edge == null) {
      stop(vehicle, "Engaged track unavailable");
      return;
    }
    var hit = sample.hit();
    if (hit == null) return;
    var velocity =
        SubLevelContainer.getContainer(level)
            .physicsSystem()
            .getPipeline()
            .getLinearVelocity(parent, new Vector3d());
    var tangent =
        CoasterPathGraphManager.sampleEdgePosition(level, edge, Math.min(1, hit.t() + .001))
            .subtract(
                CoasterPathGraphManager.sampleEdgePosition(
                    level, edge, Math.max(0, hit.t() - .001)))
            .normalize();
    double speed = velocity.dot(tangent.x, tangent.y, tangent.z);
    if (Math.abs(speed) < .01) return;
    var endpoint = speed > 0 ? edge.to() : edge.from();
    var table = dev.fourthdimension.track.TrackGeometry.table(level, edge);
    double remaining =
        speed > 0 ? table.length() - table.distanceAt(hit.t()) : table.distanceAt(hit.t());
    if (remaining > speed * speed / 12 + 2.5) return;
    var data = FoundationData.get(level);
    boolean supported =
        data.rails.stream()
            .anyMatch(
                next ->
                    next != rail
                        && next.valid
                        && next.family() == vehicle.family
                        && next.variant.equals(vehicle.variant)
                        && dev.fourthdimension.integration.TrackProviders.available(next)
                        && (next.from.equals(endpoint) || next.to.equals(endpoint)));
    if (!supported) stop(vehicle, "End of compatible track; capture and inspect the route");
  }

  private static void stop(FoundationData.Vehicle vehicle, String reason) {
    vehicle.stopping = true;
    vehicle.restraints = true;
    vehicle.fault = reason;
  }
}
