package dev.fourthdimension.vehicle;

import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.*;
import java.util.*;
import net.minecraft.server.level.*;
import org.joml.Vector3d;

public final class VehicleOperations {
  private VehicleOperations() {}

  public static FoundationData.Vehicle accessible(ServerPlayer player, UUID id) {
    var v = FoundationData.get(player.serverLevel()).vehicles.get(id);
    if (v == null || FoundationData.VARIANT.equals(v.variant))
      throw new IllegalArgumentException("Select an original 4D vehicle");
    var parent = SubLevelContainer.getContainer(player.level()).getSubLevel(id);
    var center = parent == null ? null : CoasterCartResolve.cartCenterWorld(parent);
    if (center == null
        || center.distanceSquared(new Vector3d(player.getX(), player.getY(), player.getZ()))
            > 12 * 12
        || !player
            .serverLevel()
            .mayInteract(
                player, net.minecraft.core.BlockPos.containing(center.x, center.y, center.z))
        || !player.mayBuild())
      throw new IllegalArgumentException("Vehicle is out of reach or cannot be edited");
    return v;
  }

  public static List<FoundationData.Vehicle> consist(
      ServerLevel level, FoundationData.Vehicle vehicle) {
    var container = SubLevelContainer.getContainer(level);
    var parent = container.getSubLevel(vehicle.chassis);
    if (parent == null) throw new IllegalArgumentException("Vehicle chassis is unavailable");
    var ids = CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(parent);
    var result = new ArrayList<FoundationData.Vehicle>();
    for (var id : ids) {
      var v = FoundationData.get(level).vehicles.get(id);
      if (v == null || !v.variant.equals(vehicle.variant))
        throw new IllegalArgumentException("Incompatible or missing vehicle in consist");
      result.add(v);
    }
    if (result.isEmpty()) result.add(vehicle);
    return result;
  }

  public static void capture(ServerLevel level, FoundationData.Vehicle vehicle) {
    var winch = dev.fourthdimension.shuttle.ShuttleRuntime.owner(level, vehicle.chassis);
    if (winch != null
        && winch.phase != dev.fourthdimension.shuttle.ShuttleWinchEntity.Phase.BOARDING) {
      winch.fail("Operator emergency capture: inspect the shuttle before recovery");
      return;
    }
    for (var v : consist(level, vehicle)) {
      v.stopping = true;
      v.carriers.forEach(c -> c.capture = true);
      v.revision++;
    }
    FoundationData.get(level).setDirty();
  }

  public static void restraints(ServerLevel level, FoundationData.Vehicle vehicle, boolean closed) {
    if (!closed && dev.fourthdimension.shuttle.ShuttleRuntime.running(level, vehicle.chassis))
      throw new IllegalArgumentException(
          "Shuttle restraints remain closed until final station capture");
    var consist = consist(level, vehicle);
    for (var v : consist)
      if (!Boarding.ready(level, v))
        throw new IllegalArgumentException(
            "Stop and align every carrier before operating restraints");
    for (var v : consist) {
      v.restraints = closed;
      v.revision++;
    }
    FoundationData.get(level).setDirty();
  }

  public static void dispatch(ServerLevel level, FoundationData.Vehicle vehicle) {
    if (vehicle.family == dev.fourthdimension.contract.Family.PAX) {
      var winch = dev.fourthdimension.shuttle.ShuttleRuntime.owner(level, vehicle.chassis);
      if (winch == null)
        throw new IllegalArgumentException("Bind a powered shuttle winch before PAX dispatch");
      winch.start();
      return;
    }
    var data = FoundationData.get(level);
    var consist = consist(level, vehicle);
    for (var v : consist) {
      if (!v.restraints || !Boarding.ready(level, v))
        throw new IllegalArgumentException(
            "All carriers must be captured and restraints closed before dispatch");
      if (v.maintenance)
        throw new IllegalArgumentException("Release maintenance retention before dispatch");
      var rail =
          data.rails.stream()
              .filter(r -> r.matches(v.boundFrom, v.boundTo))
              .findFirst()
              .orElse(null);
      if (rail == null
          || !rail.valid
          || !rail.variant.equals(v.variant)
          || TrackGeometry.resolve(level, rail) == null)
        throw new IllegalArgumentException("Vehicle has lost its matching track support");
    }
    for (var v : consist) {
      v.stopping = false;
      v.fault = "";
      v.carriers.forEach(c -> c.capture = false);
      v.revision++;
    }
    data.setDirty();
  }

  public static void tune(
      ServerLevel level, FoundationData.Vehicle vehicle, double damping, double ballast) {
    if (vehicle.family.driven()
        || !Boarding.ready(level, vehicle)
        || vehicle.carriers.stream().anyMatch(c -> !c.occupants.isEmpty()))
      throw new IllegalArgumentException("Tune an empty, stopped, captured free-spin vehicle");
    if (!Double.isFinite(damping)
        || damping < 0
        || damping > 250
        || !Double.isFinite(ballast)
        || ballast < 0
        || ballast > 240)
      throw new IllegalArgumentException("Damping must be 0..250 and ballast 0..240 per carrier");
    vehicle.damping = damping;
    vehicle.ballast = ballast;
    vehicle.carriers.forEach(c -> c.ballast = 0);
    vehicle.revision++;
    FoundationData.get(level).setDirty();
  }

  public static void retention(
      ServerLevel level, FoundationData.Vehicle vehicle, boolean retained) {
    var cars = consist(level, vehicle);
    if (!retained && cars.stream().anyMatch(v -> !Boarding.ready(level, v)))
      throw new IllegalArgumentException("Capture every vehicle before releasing retention");
    if (retained) capture(level, vehicle);
    for (var v : cars) {
      v.maintenance = retained;
      v.revision++;
    }
    FoundationData.get(level).setDirty();
  }

  public static void loadingPreset(ServerLevel level, FoundationData.Vehicle vehicle, int preset) {
    if (vehicle.family != dev.fourthdimension.contract.Family.FREE_SPIN || preset < 0 || preset > 2)
      throw new IllegalArgumentException("Select a Free Spin loading preset");
    tune(level, vehicle, vehicle.damping, 0);
    for (int i = 0; i < vehicle.carriers.size(); i++)
      vehicle.carriers.get(i).ballast = preset == 1 || (preset == 2 && i == 0) ? 160 : 0;
    FoundationData.get(level).setDirty();
  }

  public static void brake(ServerLevel level, FoundationData.Vehicle vehicle, double dt) {
    var container = SubLevelContainer.getContainer(level);
    var pipeline = container.physicsSystem().getPipeline();
    if (!(container.getSubLevel(vehicle.chassis) instanceof ServerSubLevel parent)) return;
    var velocity = pipeline.getLinearVelocity(parent, new Vector3d());
    double speed = velocity.length();
    if (speed == 0) return;
    var reduction = velocity.mul(-Math.min(1, 6 * dt / speed));
    var ids = new ArrayList<UUID>();
    ids.add(vehicle.chassis);
    vehicle.carriers.forEach(c -> ids.add(c.body));
    for (var id : ids)
      if (container.getSubLevel(id) instanceof ServerSubLevel body) {
        // Brake the common train translation once per body. Braking only the light
        // chassis lets the loaded carrier joints feed momentum back into it.
        pipeline.addLinearAndAngularVelocity(body, reduction, new Vector3d());
      }
  }
}
