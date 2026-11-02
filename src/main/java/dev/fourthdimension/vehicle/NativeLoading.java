package dev.fourthdimension.vehicle;

import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import dev.fourthdimension.runtime.FoundationData;
import dev.ryanhcode.sable.api.physics.mass.MassTracker;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;

/** Sable 2.0.3 merges blocks and contraptions, not seat passengers. Add their mass exactly once. */
public final class NativeLoading {
  // Game-scale convention: one native mass unit represents 100 kg of declared load.
  public static final double MASS_PER_RIDER = .8;

  private static final class Applied {
    final MassTracker tracker;
    final Map<BlockPos, Double> masses = new HashMap<>();
    long sampledTick = Long.MIN_VALUE;
    double vehicleBallast = Double.NaN;
    double carrierBallast = Double.NaN;

    Applied(MassTracker tracker) {
      this.tracker = tracker;
    }
  }

  private static final Map<ServerSubLevel, Applied> APPLIED = new WeakHashMap<>();

  private NativeLoading() {}

  public static void update(
      ServerSubLevel child, FoundationData.Vehicle vehicle, FoundationData.Carrier carrier) {
    var level = child.getLevel();
    var tracker = child.getSelfMassTracker();
    var applied = APPLIED.get(child);
    if (applied == null || applied.tracker != tracker) {
      applied = new Applied(tracker);
      APPLIED.put(child, applied);
    }
    // Entity mounts and ballast edits occur on the server tick. Physics may invoke this callback
    // more than once within that tick, so repeat neither the seat entity queries nor mass rebuild.
    long gameTick = level.getGameTime();
    if (applied.sampledTick == gameTick
        && Double.compare(applied.vehicleBallast, vehicle.ballast) == 0
        && Double.compare(applied.carrierBallast, carrier.ballast) == 0) return;
    var occupants = new ArrayList<UUID>();
    var desired = new HashMap<BlockPos, Double>();
    for (var offset : carrier.seats) {
      var seat = child.getPlot().getCenterBlock().offset(offset);
      var passengers =
          level.getEntitiesOfClass(SeatEntity.class, new AABB(seat)).stream()
              .flatMap(s -> s.getPassengers().stream())
              .toList();
      for (var passenger : passengers) {
        occupants.add(passenger.getUUID());
        if (passenger instanceof net.minecraft.server.level.ServerPlayer)
          carrier.riderSeats.put(passenger.getUUID(), offset);
      }
      // Equal declared torso mass 1.5 blocks above the seat block origin; never inspect player
      // characteristics.
      desired.put(
          seat.above(),
          MASS_PER_RIDER * passengers.size()
              + (vehicle.ballast + carrier.ballast) / (100 * carrier.seats.size()));
    }
    var positions = new HashSet<>(applied.masses.keySet());
    positions.addAll(desired.keySet());
    boolean changed = false;
    for (var pos : positions) {
      double delta = desired.getOrDefault(pos, 0.0) - applied.masses.getOrDefault(pos, 0.0);
      if (Math.abs(delta) > 1e-9) {
        tracker.addBlockMass(
            level, Blocks.IRON_BLOCK.defaultBlockState(), pos, delta, new Vec3(.12, .06, .12));
        changed = true;
      }
    }
    applied.masses.clear();
    applied.masses.putAll(desired);
    applied.sampledTick = gameTick;
    applied.vehicleBallast = vehicle.ballast;
    applied.carrierBallast = carrier.ballast;
    if (changed) child.updateMergedMassData(1);
    carrier.occupants = List.copyOf(occupants);
  }
}
