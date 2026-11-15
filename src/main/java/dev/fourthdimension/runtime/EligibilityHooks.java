package dev.fourthdimension.runtime;

import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class EligibilityHooks {
  private EligibilityHooks() {}

  // Client copies are replaced on full snapshots and keyed by level lifetime.
  public static final Map<Level, FoundationData> CLIENT =
      Collections.synchronizedMap(new WeakHashMap<>());

  public static FoundationData data(Level level) {
    return level instanceof ServerLevel server ? FoundationData.get(server) : CLIENT.get(level);
  }

  public static boolean allows(SubLevel chassis, Level level, GraphHit hit) {
    if (hit == null) return false;
    var data = data(level);
    if (data == null) return true;
    var vehicle = data.vehicles.get(chassis.getUniqueId());
    var markers = FamilyControllers.find(chassis);
    if (vehicle != null && (markers.size() != 1 || !markers.contains(vehicle.family))) return false;
    // An unbound or mixed-family assembly is not an ordinary recovery cart.
    if (!markers.isEmpty()
        && (vehicle == null || markers.size() != 1 || !markers.contains(vehicle.family)))
      return false;
    var rail =
        data.rails.stream()
            .filter(r -> r.matches(hit.edge().from(), hit.edge().to()))
            .findFirst()
            .orElse(null);
    if (vehicle == null) {
      var recovery =
          data.vehicles.values().stream()
              .filter(v -> chassis.getUniqueId().equals(v.recoveryCart))
              .toList();
      if (!recovery.isEmpty())
        return rail != null
            && rail.valid
            && dev.fourthdimension.track.OrdinaryGearCertificate.fits(chassis)
            && recovery.stream().allMatch(v -> v.variant.equals(rail.variant));
      if (rail == null) return true;
      return rail.valid && dev.fourthdimension.track.OrdinaryGearCertificate.fits(chassis);
    }
    if (!dev.fourthdimension.vehicle.ConsistRules.eligible(level, chassis, vehicle)) return false;
    if (dev.fourthdimension.vehicle.WingVehicle.is(vehicle))
      return dev.fourthdimension.vehicle.WingTrack.rejection(level, hit.edge()) == null;
    if (dev.fourthdimension.vehicle.RollerBallVehicle.is(vehicle) && dev.fourthdimension.vehicle.RollerBallTrack.rejection(level,hit.edge()) != null) return false;
    if (dev.fourthdimension.vehicle.AxisVehicle.is(vehicle) && dev.fourthdimension.vehicle.AxisTrack.rejection(level,hit.edge()) != null) return false;
    if (dev.fourthdimension.vehicle.FlyingVehicle.is(vehicle) && dev.fourthdimension.vehicle.FlyingTrack.rejection(level,hit.edge()) != null) return false;
    if (dev.fourthdimension.vehicle.NewGenerationFlyingVehicle.is(vehicle) && dev.fourthdimension.vehicle.NewGenerationFlyingTrack.rejection(level,hit.edge()) != null) return false;
    if (dev.fourthdimension.vehicle.FixedInvertedVehicle.is(vehicle)
        && dev.fourthdimension.vehicle.FixedInvertedTrack.rejection(level, hit.edge()) != null) return false;
    if (dev.fourthdimension.vehicle.SuspendedVehicle.is(vehicle)
        && dev.fourthdimension.vehicle.SuspendedTrack.rejection(level, hit.edge()) != null) return false;
    if (dev.fourthdimension.vehicle.SpinningVehicle.is(vehicle)
        && dev.fourthdimension.vehicle.SpinningTrack.rejection(level, hit.edge()) != null) return false;
    if (rail == null
        || !rail.valid
        || rail.family() != vehicle.family
        || !rail.variant.equals(vehicle.variant)) return false;
    if (vehicle.boundFrom != null
        && !rail.matches(vehicle.boundFrom, vehicle.boundTo)
        && !rail.from().equals(vehicle.boundFrom)
        && !rail.from().equals(vehicle.boundTo)
        && !rail.to().equals(vehicle.boundFrom)
        && !rail.to().equals(vehicle.boundTo)) return false;
    return true;
  }

  public static void bindAccepted(SubLevel chassis, ServerLevel level, GraphHit hit) {
    if (!allows(chassis, level, hit)) return;
    var data = FoundationData.get(level);
    var vehicle = data.vehicles.get(chassis.getUniqueId());
    if (vehicle == null) return;
    if (dev.fourthdimension.vehicle.WingVehicle.is(vehicle)) {
      if (!hit.edge().from().equals(vehicle.boundFrom) || !hit.edge().to().equals(vehicle.boundTo)) {
        vehicle.boundFrom = hit.edge().from();
        vehicle.boundTo = hit.edge().to();
        data.setDirty();
      }
      return;
    }
    var rail =
        data.rails.stream()
            .filter(r -> r.matches(hit.edge().from(), hit.edge().to()))
            .findFirst()
            .orElse(null);
    if (rail != null && !rail.matches(vehicle.boundFrom, vehicle.boundTo)) {
      vehicle.boundFrom = rail.from;
      vehicle.boundTo = rail.to;
      data.setDirty();
    }
  }
}
