package dev.fourthdimension.vehicle;

import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.*;
import org.joml.Vector3d;

/** Builds original four-seat controlled cars or eight-seat legacy offset-pivot cars. */
public final class VehicleFactory {
  private VehicleFactory() {}

  public static FoundationData.Vehicle create(
      ServerLevel level, TrackDesign design, CoasterPathTrackFrame.GraphHit hit) {
    var data = FoundationData.get(level);
    if (data.vehicles.size() >= 64) throw new IllegalArgumentException("Vehicle capacity reached");
    var rail =
        data.rails.stream()
            .filter(r -> r.matches(hit.edge().from(), hit.edge().to()))
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "A 4D car requires its matching functional track"));
    if (!rail.valid || !rail.variant.equals(design.variant))
      throw new IllegalArgumentException("Vehicle and track families do not match");
    if (CoasterPathGraphManager.edgeHostedOnSubLevel(level, hit.edge()))
      throw new IllegalArgumentException("Place vehicles on fixed world tracks");
    var orientation =
        CoasterPathTrackFrame.quaternionFromTrackBasis(
            CoasterPathTrackFrame.trackBasisAtHit(level, hit));
    Vec3 bearing = CoasterPathTrackFrame.pathAnchorForSnappedBlock(level, hit);
    // Reserve the entire swept envelope, including riders, before allocating bodies.
    for (var existing : data.vehicles.values()) {
      var body = SubLevelContainer.getContainer(level).getSubLevel(existing.chassis);
      var center = body == null ? null : CoasterCartResolve.cartCenterWorld(body);
      if (center != null
          && center.distanceSquared(new Vector3d(bearing.x, bearing.y, bearing.z)) < 35.99)
        throw new IllegalArgumentException("Leave at least six blocks between axles");
    }
    var basis = CoasterPathTrackFrame.trackBasisAtHit(level, hit);
    for (double side : new double[] {-3.5, 3.5}) {
      var center = hit.point().add(basis.up().scale(2.3125)).add(basis.right().scale(side));
      if (SweptClearance.obstructed(
          level, center, basis.right(), basis.up(), basis.forward(), 1.5, 2.7, 2.7))
        throw new IllegalArgumentException(
            "Clear the full wing and rider rotation envelope before placing this car");
    }
    var origin = CoasterCartLinkPlacement.plotOriginForBearingCenter(bearing, orientation);
    var chassis =
        CoasterCartSpawner.spawnMinimalContraption(level, origin, orientation, null, false);
    if (chassis == null) throw new IllegalStateException("Native chassis allocation failed");
    var container = SubLevelContainer.getContainer(level);
    var pipeline = container.physicsSystem().getPipeline();
    var vehicle = new FoundationData.Vehicle(chassis.getUniqueId(), design.family);
    vehicle.variant = design.variant;
    vehicle.stopping = true;
    vehicle.boundFrom = rail.from;
    vehicle.boundTo = rail.to;
    vehicle.damping = design.family.driven() ? 5 : .8;
    data.vehicles.put(vehicle.chassis, vehicle);
    try {
      var embedded = chassis.getPlot().getEmbeddedLevelAccessor();
      embedded.setBlock(
          new BlockPos(0, 1, 0),
          FamilyControllers.CONTROLLERS.get(design.family).get().defaultBlockState(),
          3);
      for (int x = -2; x <= 2; x++)
        embedded.setBlock(new BlockPos(x, 2, 0), VehicleParts.STRUT.get().defaultBlockState(), 3);
      for (int sign : new int[] {-1, 1}) {
        double side = 3 * sign;
        var parentCenter = chassis.getPlot().getCenterBlock();
        var world =
            chassis
                .logicalPose()
                .transformPosition(
                    new Vector3d(
                        parentCenter.getX() + .5 + side,
                        parentCenter.getY() + 2.5,
                        parentCenter.getZ() + .5));
        var pose = new Pose3d();
        pose.position().set(world);
        pose.orientation().set(orientation);
        var body = (ServerSubLevel) container.allocateNewSubLevel(pose);
        var carrier = new FoundationData.Carrier(body.getUniqueId(), side);
        carrier.capture = true;
        vehicle.carriers.add(carrier);
        carrier.seats =
            design.family.driven()
                ? List.of(new BlockPos(0, -1, 0), new BlockPos(sign, -1, 0))
                : List.of(
                    new BlockPos(0, -1, -1),
                    new BlockPos(sign, -1, -1),
                    new BlockPos(0, -1, 1),
                    new BlockPos(sign, -1, 1));
        var plot = body.getPlot();
        plot.newEmptyChunk(plot.getCenterChunk());
        var blocks = plot.getEmbeddedLevelAccessor();
        blocks.setBlock(BlockPos.ZERO, VehicleParts.AXLE.get().defaultBlockState(), 3);
        blocks.setBlock(new BlockPos(sign, 0, 0), VehicleParts.AXLE.get().defaultBlockState(), 3);
        for (var seat : carrier.seats) {
          var block =
              design.family.driven()
                  ? VehicleParts.ARROW_SEAT.get()
                  : VehicleParts.LEGACY_SEAT.get();
          blocks.setBlock(
              seat,
              block
                  .defaultBlockState()
                  .setValue(
                      CarrierSeatBlock.FACING, seat.getZ() > 0 ? Direction.SOUTH : Direction.NORTH),
              3);
        }
        var center = plot.getCenterBlock();
        body.logicalPose()
            .rotationPoint()
            .set(center.getX() + .5, center.getY() + .5, center.getZ() + .5);
        body.logicalPose().position().set(world);
        body.logicalPose().orientation().set(orientation);
        body.updateLastPose();
        pipeline.onStatsChanged(body);
        pipeline.teleport(body, body.logicalPose().position(), body.logicalPose().orientation());
      }
      CoasterCartTrackSnap.applyInitialSnapAfterSpawn(chassis, level, hit);
      data.setDirty();
      FoundationSync.broadcast(level);
      return vehicle;
    } catch (RuntimeException failure) {
      for (var carrier : vehicle.carriers) {
        var body = container.getSubLevel(carrier.body);
        if (body != null) container.removeSubLevel(body, SubLevelRemovalReason.REMOVED);
      }
      container.removeSubLevel(chassis, SubLevelRemovalReason.REMOVED);
      data.vehicles.remove(vehicle.chassis);
      throw failure;
    }
  }
}
