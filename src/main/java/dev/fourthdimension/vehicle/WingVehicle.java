package dev.fourthdimension.vehicle;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.TrackDesign;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;

/** Fixed seats are native chassis blocks, never a zero-angle rotary mechanism. */
public final class WingVehicle {
  public static final String VARIANT = "wing_fixed_v1";
  public static final double SPACING = 6;
  public static final List<BlockPos> SEATS = List.of(
      new BlockPos(-4, 1, 0), new BlockPos(-3, 1, 0),
      new BlockPos(3, 1, 0), new BlockPos(4, 1, 0));

  private WingVehicle() {}

  public static boolean is(FoundationData.Vehicle vehicle) {
    return vehicle.family == Family.WING && VARIANT.equals(vehicle.variant);
  }

  public static double spacing(FoundationData.Vehicle vehicle) {
    return is(vehicle) || FoundationData.VARIANT.equals(vehicle.variant)
        ? SPACING : TrackDesign.byVariant(vehicle.variant).axleSpacing();
  }

  public static FoundationData.Vehicle create(ServerLevel level, CoasterPathTrackFrame.GraphHit hit) {
    String rejection = WingTrack.rejection(level, hit.edge());
    if (rejection != null) throw new IllegalArgumentException(rejection);
    var data = FoundationData.get(level);
    if (data.vehicles.size() >= 64) throw new IllegalArgumentException("Vehicle capacity reached");
    var obstruction = WingTrack.obstruction(level, hit);
    if (obstruction != null)
      throw new IllegalArgumentException("Clear/load the Wing rider envelope at " + obstruction.toShortString());
    var orientation = CoasterPathTrackFrame.quaternionFromTrackBasis(
        CoasterPathTrackFrame.trackBasisAtHit(level, hit));
    var bearing = CoasterPathTrackFrame.pathAnchorForSnappedBlock(level, hit);
    var container = SubLevelContainer.getContainer(level);
    for (var existing : data.vehicles.values()) {
      var body = container.getSubLevel(existing.chassis);
      var center = body == null ? null : CoasterCartResolve.cartCenterWorld(body);
      if (center != null && center.distanceSquared(new Vector3d(bearing.x, bearing.y, bearing.z))
          < Math.pow(Math.max(SPACING, spacing(existing)), 2) - .01)
        throw new IllegalArgumentException("Leave six blocks or the other car's larger spacing between bearings");
    }
    var origin = CoasterCartLinkPlacement.plotOriginForBearingCenter(bearing, orientation);
    var chassis = CoasterCartSpawner.spawnMinimalContraption(level, origin, orientation, null, false);
    if (chassis == null) throw new IllegalStateException("Native chassis allocation failed");
    var vehicle = new FoundationData.Vehicle(chassis.getUniqueId(), Family.WING);
    vehicle.variant = VARIANT;
    vehicle.boundFrom = hit.edge().from();
    vehicle.boundTo = hit.edge().to();
    vehicle.stopping = true;
    vehicle.damping = 0;
    var seats = new FoundationData.Carrier(vehicle.chassis, 0);
    seats.seats = SEATS;
    seats.pivotHeight = 0;
    seats.capture = true;
    vehicle.carriers.add(seats);
    data.vehicles.put(vehicle.chassis, vehicle);
    try {
      var blocks = chassis.getPlot().getEmbeddedLevelAccessor();
      for (int x = -4; x <= 4; x++)
        blocks.setBlock(new BlockPos(x, 1, 0), VehicleParts.WING_BEAM.get().defaultBlockState(), 3);
      blocks.setBlock(new BlockPos(0, 1, 0),
          FamilyControllers.CONTROLLERS.get(Family.WING).get().defaultBlockState(), 3);
      for (var seat : SEATS)
        blocks.setBlock(seat, VehicleParts.WING_SEAT.get().defaultBlockState()
            .setValue(CarrierSeatBlock.FACING, Direction.NORTH), 3);
      CoasterCartTrackSnap.applyInitialSnapAfterSpawn(chassis, level, hit);
      data.setDirty();
      FoundationSync.broadcast(level);
      return vehicle;
    } catch (RuntimeException failure) {
      data.vehicles.remove(vehicle.chassis);
      container.removeSubLevel(chassis, SubLevelRemovalReason.REMOVED);
      throw failure;
    }
  }
}
