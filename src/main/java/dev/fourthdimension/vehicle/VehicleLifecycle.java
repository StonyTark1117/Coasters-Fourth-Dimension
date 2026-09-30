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
                for (var vehicle : FoundationData.get(level).vehicles.values()) {
                  if (FoundationData.VARIANT.equals(vehicle.variant)) continue;
                  if (vehicle.chassis.equals(body.getUniqueId())
                      || vehicle.carriers.stream()
                          .anyMatch(c -> c.body.equals(body.getUniqueId()))) {
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
