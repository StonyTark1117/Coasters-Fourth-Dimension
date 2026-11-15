package dev.fourthdimension.vehicle;

import dev.fourthdimension.runtime.*;
import dev.ryanhcode.sable.api.sublevel.*;
import dev.ryanhcode.sable.neoforge.event.ForgeSableSubLevelContainerReadyEvent;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Permanent body destruction recovers riders and removes the remainder; unloads retain state. */
public final class VehicleLifecycle {
  private static final Map<ServerLevel, Set<UUID>> REMOVED = new WeakHashMap<>();

  private VehicleLifecycle() {}

  public static void ready(ForgeSableSubLevelContainerReadyEvent event) {
    if (!(event.getLevel() instanceof ServerLevel level)) return;
    event
        .getContainer()
        .addObserver(
            new SubLevelObserver() {
              @Override
              public void onSubLevelRemoved(SubLevel body, SubLevelRemovalReason reason) {
                if (reason != SubLevelRemovalReason.REMOVED) return;
                dev.fourthdimension.shuttle.ShuttleRuntime.onBodyRemoved(level, body.getUniqueId());
                for (var vehicle : FoundationData.get(level).vehicles.values()) {
                  if (body.getUniqueId().equals(vehicle.recoveryCart)) {
                    vehicle.recoveryCartRemoved = true;
                    vehicle.stopping = true;
                    vehicle.restraints = true;
                    vehicle.carriers.forEach(c -> c.capture = true);
                    vehicle.fault =
                        "Recovery bearing permanently removed: brake and disconnect recovery";
                    vehicle.revision++;
                    FoundationData.get(level).setDirty();
                  }
                  if (FoundationData.VARIANT.equals(vehicle.variant)) continue;
                  if (vehicle.chassis.equals(body.getUniqueId())
                      || NewGenerationFlyingVehicle.is(vehicle) && body.getUniqueId().equals(vehicle.yaw.deck)
                      || vehicle.carriers.stream()
                          .anyMatch(c -> c.body.equals(body.getUniqueId()))) {
                    if(PoweredInvertedYawVehicle.is(vehicle)){
                      PoweredInvertedYawRuntime.fault(level,vehicle,"Powered Inverted Yaw native body removed; surviving train retained");
                      FoundationData.get(level).setDirty();continue;
                    }
                    if(RollerBallVehicle.is(vehicle)) {
                      RollerBallRuntime.fault(level,vehicle,"Roller Ball body removed; retain surviving state");
                      FoundationData.get(level).setDirty();continue;
                    }
                    if(AxisVehicle.is(vehicle)) {
                      AxisRuntime.fault(level,vehicle,"Axis native body removed; surviving train retained");
                      FoundationData.get(level).setDirty();continue;
                    }
                    if(NewGenerationFlyingVehicle.is(vehicle)) {
                      NewGenerationFlyingRuntime.fault(level,vehicle,"Compound Flying body removed; retained for recovery");
                      FoundationData.get(level).setDirty();continue;
                    }
                    if(FlyingVehicle.is(vehicle)) {
                      FlyingRuntime.fault(level,vehicle,"Flying native body removed; surviving train retained");
                      FoundationData.get(level).setDirty();
                    }
                    if(FixedInvertedVehicle.is(vehicle))
                      FixedInvertedRuntime.fault(level,vehicle,"Fixed Inverted native body removed; surviving train retained");
                    if(SuspendedVehicle.is(vehicle)) {
                      SuspendedRuntime.fault(level,vehicle,"Suspended native body removed; train retained");
                      FoundationData.get(level).setDirty();continue;
                    }
                    if (SpinningVehicle.is(vehicle)) {
                      SpinningRuntime.fault(vehicle, "S1 native body permanently removed; surviving pose retained");
                      FoundationData.get(level).setDirty();
                      continue;
                    }
                    if (REMOVED.computeIfAbsent(level, k -> new HashSet<>()).add(vehicle.chassis))
                      Boarding.recover(level, vehicle);
                  }
                }
              }
            });
  }

  public static void tick(LevelTickEvent.Post event) {
    if (!(event.getLevel() instanceof ServerLevel level)) return;
    var pending = REMOVED.remove(level);
    if (pending == null) return;
    var data = FoundationData.get(level);
    var container = SubLevelContainer.getContainer(level);
    for (var id : pending) {
      var vehicle = data.vehicles.remove(id);
      if (vehicle == null) continue;
      var bodies = new ArrayList<UUID>();
      bodies.add(vehicle.chassis);
      vehicle.carriers.forEach(c -> bodies.add(c.body));
      for (var bodyId : bodies) {
        var body = container.getSubLevel(bodyId);
        if (body != null && !body.isRemoved())
          container.removeSubLevel(body, SubLevelRemovalReason.REMOVED);
      }
    }
    data.setDirty();
    FoundationSync.broadcast(level);
  }
}
