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
    boolean firstSample = applied == null || applied.tracker != tracker;
    if (firstSample) {
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
              .filter(s -> !(vehicle.family.specialty()||PoweredInvertedYawVehicle.is(vehicle)||DiveVehicle.is(vehicle)||PipelineVehicle.is(vehicle)||FixedInvertedVehicle.is(vehicle)) || s.blockPosition().equals(seat))
              .flatMap(s -> s.getPassengers().stream().filter(p -> p.getVehicle() == s))
              .toList();
      for (var passenger : passengers) {
        occupants.add(passenger.getUUID());
        if (passenger instanceof net.minecraft.server.level.ServerPlayer)
          bindLiveRider(level, carrier, passenger.getUUID(), offset);
      }
      // Equal declared torso mass 1.5 blocks above the seat block origin; never inspect player
      // characteristics.
      desired.put(
          seat.above(),
          MASS_PER_RIDER * passengers.size()
              + (vehicle.ballast + carrier.ballast) / (100 * carrier.seats.size()));
    }
    if(RollerBallVehicle.is(vehicle)) desired.put(child.getPlot().getCenterBlock(),1.0);
    if(AxisVehicle.is(vehicle)) desired.put(child.getPlot().getCenterBlock().offset(0,-1,0),1.0);
    if(SuspendedVehicle.is(vehicle)) desired.put(child.getPlot().getCenterBlock().offset(0,-2,0),1.0);
    var positions = new HashSet<>(applied.masses.keySet());
    positions.addAll(desired.keySet());
    boolean changed = false;
    for (var pos : positions) {
      double delta = desired.getOrDefault(pos, 0.0) - applied.masses.getOrDefault(pos, 0.0);
      if (Math.abs(delta) > 1e-9) {
        tracker.addBlockMass(
            level, StandingVehicle.is(vehicle) ? StandingContent.SEAT.get().defaultBlockState().setValue(StandingSeatBlock.RIGHT,pos.getX()>child.getPlot().getCenterBlock().getX()).setValue(StandingSeatBlock.SURF,StandingVehicle.surf(vehicle)) : FlyingBikeVehicle.is(vehicle) ? FlyingBikeContent.SEAT.get().defaultBlockState().setValue(FlyingBikeSeatBlock.LOAD_MARKER,true) : MotorbikeVehicle.is(vehicle) ? MotorbikeContent.SEAT.get().defaultBlockState() : PoweredInvertedYawVehicle.is(vehicle) ? PoweredInvertedYawContent.SEAT.get().defaultBlockState() : DiveVehicle.is(vehicle) ? DiveContent.SEAT.get().defaultBlockState() : PipelineVehicle.is(vehicle) ? VehicleParts.PIPELINE_SEAT.get().defaultBlockState().setValue(PipelineSeatBlock.RIGHT,pos.getX()>child.getPlot().getCenterBlock().getX()).setValue(PipelineSeatBlock.REAR,pos.getZ()>child.getPlot().getCenterBlock().getZ()) : AxisVehicle.is(vehicle) && pos.equals(child.getPlot().getCenterBlock().offset(0,-1,0))
                ? VehicleParts.AXIS_KEEL.get().defaultBlockState() : RollerBallVehicle.is(vehicle)
                ? (pos.equals(child.getPlot().getCenterBlock()) ? VehicleParts.ROLLER_BALL_BALLAST.get().defaultBlockState() : VehicleParts.ROLLER_BALL_SEAT.get().defaultBlockState().setValue(RollerBallSeatBlock.LEFT,pos.getX()<child.getPlot().getCenterBlock().getX())) : NewGenerationFlyingVehicle.is(vehicle)
                ? VehicleParts.NEW_GENERATION_FLYING_SEAT.get().defaultBlockState() : FlyingVehicle.is(vehicle)
                ? VehicleParts.FLYING_SEAT.get().defaultBlockState() : SuspendedVehicle.is(vehicle) && pos.equals(child.getPlot().getCenterBlock().offset(0,-2,0))
                ? VehicleParts.SUSPENDED_KEEL.get().defaultBlockState() : FixedInvertedVehicle.is(vehicle)
                ? VehicleParts.FIXED_INVERTED_SEAT.get().defaultBlockState() : S2SpinningVehicle.is(vehicle)
                ? S2SpinningSeatBlock.state(pos.below().subtract(child.getPlot().getCenterBlock())) : SpinningVehicle.is(vehicle)
                ? SpinningSeatBlock.state(pos.below().subtract(child.getPlot().getCenterBlock()))
                : Blocks.IRON_BLOCK.defaultBlockState(), pos, delta, new Vec3(.12, .06, .12));
        changed = true;
      }
    }
    applied.masses.clear();
    applied.masses.putAll(desired);
    applied.sampledTick = gameTick;
    applied.vehicleBallast = vehicle.ballast;
    applied.carrierBallast = carrier.ballast;
    if (changed || firstSample) child.updateMergedMassData(1);
    if (firstSample && !changed) {
      // Sable restores the saved pose origin, but rebuilds mass without offline passengers.
      // Its merged-mass upload skips equal native mass/COM values, leaving that saved origin
      // stale. Rebase coordinates once without moving any world-space block or changing angle.
      var pose = child.logicalPose();
      var center = child.getMassTracker().getCenterOfMass();
      if (center != null && pose.rotationPoint().distanceSquared(center) > 1e-12) {
        var physics = dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(level).physicsSystem();
        physics.updatePose(child);
        var position = new org.joml.Vector3d(center).sub(pose.rotationPoint())
            .rotate(pose.orientation()).add(pose.position());
        pose.rotationPoint().set(center);
        physics.getPipeline().teleport(child, position, pose.orientation());
      }
    }
    carrier.occupants = List.copyOf(occupants);
  }

  public static void bindLiveRider(
      net.minecraft.server.level.ServerLevel level,
      FoundationData.Carrier carrier,
      UUID rider,
      BlockPos offset) {
    var data = FoundationData.get(level);
    // A saved or previously occupied car can retain an old assignment while the rider is
    // mounted in this seat. Keep the live native mount authoritative across all cars.
    for (var vehicle : data.vehicles.values())
      for (var other : vehicle.carriers)
        if (other != carrier && other.riderSeats.remove(rider) != null) data.setDirty();
    if (!offset.equals(carrier.riderSeats.put(rider, offset))) data.setDirty();
  }
}
