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

  private record Applied(MassTracker tracker, Map<BlockPos, Double> masses) {}

  private static final Map<ServerSubLevel, Applied> APPLIED = new WeakHashMap<>();

  private NativeLoading() {}

  public static void update(
      ServerSubLevel child, FoundationData.Vehicle vehicle, FoundationData.Carrier carrier) {
    var level = child.getLevel();
    // Sable can expose the plot before Minecraft finishes loading its entities. An empty
    // query then is not an empty seat: overwriting saved UUIDs would reject later passenger NBT.
    // Wait for every seat chunk before changing either occupancy or the applied rider mass.
    for (var offset : carrier.seats) {
      var seat = child.getPlot().getCenterBlock().offset(offset);
      if (!level.areEntitiesLoaded(new net.minecraft.world.level.ChunkPos(seat).toLong())) return;
    }
    var tracker = child.getSelfMassTracker();
    var applied = APPLIED.get(child);
    if (applied == null || applied.tracker != tracker) {
      applied = new Applied(tracker, new HashMap<>());
      APPLIED.put(child, applied);
    }
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
    if (changed) child.updateMergedMassData(1);
    carrier.occupants = List.copyOf(occupants);
  }
}
