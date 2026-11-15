package dev.fourthdimension.runtime;

import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import dev.fourthdimension.physics.FreeRotor;
import dev.ryanhcode.sable.api.physics.constraint.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public final class CarrierRuntime {
  /** Target-host FD904 limits; they cover this addon's timing only, never full server ticks or FPS. */
  public static final long TARGET_AVERAGE_INVOCATION_NANOS = 120_000;
  public static final long TARGET_AVERAGE_CARRIER_NANOS = 6_000;

  private CarrierRuntime() {}

  private static final Map<ServerSubLevel, RotaryConstraintHandle> JOINTS = new WeakHashMap<>();
  private static final Map<ServerSubLevel, dev.fourthdimension.physics.RotationServo> SERVOS =
      new WeakHashMap<>();
  public static long nanos, steps, vehicleUpdates, carrierUpdates, carrierNanos, constraintsRebuilt;

  /** Server-side cost of this addon runtime only; it intentionally excludes Sable and renderer time. */
  /**
   * Server-side callback accounting. Carrier time is the work inside one live carrier body's
   * update; invocation time also includes shared shuttle, recovery, vehicle and magnetic work.
   */
  public record PerformanceSnapshot(
      long steps,
      long nanos,
      long vehicleUpdates,
      long carrierUpdates,
      long carrierNanos,
      long constraintsRebuilt) {
    public long averageNanos() {
      return steps == 0 ? 0 : nanos / steps;
    }

    public long averageCarrierNanos() {
      return carrierUpdates == 0 ? 0 : carrierNanos / carrierUpdates;
    }
  }

  public static PerformanceSnapshot performanceSnapshot() {
    return new PerformanceSnapshot(
        steps, nanos, vehicleUpdates, carrierUpdates, carrierNanos, constraintsRebuilt);
  }

  public static boolean meetsPerformanceTarget(PerformanceSnapshot snapshot) {
    return snapshot.averageNanos() <= TARGET_AVERAGE_INVOCATION_NANOS
        && snapshot.averageCarrierNanos() <= TARGET_AVERAGE_CARRIER_NANOS;
  }

  public static void resetPerformanceSnapshot() {
    nanos = 0;
    steps = 0;
    vehicleUpdates = 0;
    carrierUpdates = 0;
    carrierNanos = 0;
    constraintsRebuilt = 0;
  }

  /** Take the same physical instant as Sable's save, including impulses since the last step. */
  public static void refreshForSave(
      FoundationData data, net.minecraft.server.level.ServerLevel level) {
    var container = SubLevelContainer.getContainer(level);
    if (container == null) return;
    var physics = container.physicsSystem();
    var pipeline = physics.getPipeline();
    for (var vehicle : data.vehicles.values()) {
      if (!(container.getSubLevel(vehicle.chassis) instanceof ServerSubLevel parent)
          || parent.isRemoved()) continue;
      physics.updatePose(parent);
      for (var carrier : vehicle.carriers) {
        if (!(container.getSubLevel(carrier.body) instanceof ServerSubLevel child)
            || child.isRemoved()) continue;
        var joint = JOINTS.get(child);
        // Do not overwrite retained low-speed state before its joint has been restored.
        if (joint == null || !joint.isValid()) continue;
        physics.updatePose(child);
        var relative =
            new Quaterniond(parent.logicalPose().orientation())
                .conjugate()
                .mul(child.logicalPose().orientation());
        carrier.angle = vehicle.family.axis().angle(carrier.angle, relative);
        var axis = vehicle.family.axis().vector().rotate(parent.logicalPose().orientation());
        carrier.omega =
            pipeline
                .getAngularVelocity(child, new Vector3d())
                .sub(pipeline.getAngularVelocity(parent, new Vector3d()))
                .dot(axis);
      }
    }
  }

  public static void tick(ForgeSablePrePhysicsTickEvent event) {
    long start = System.nanoTime();
    var physics = event.getPhysicsSystem();
    var level = physics.getLevel();
    var container = SubLevelContainer.getContainer(level);
    if (container == null) return;
    var data = FoundationData.get(level);
    var pipeline = physics.getPipeline();
    // Every active car checks its bound rail once per physics callback. The PAX
    // shuttle has four cars, so avoid allocating two stream pipelines for each
    // one; the direct searches preserve the previous first-match behavior.
    var graph = CoasterPathGraphManager.get(level);
    // A missing active PAX support must fault the winch before its guide advances in this
    // callback. The ordinary per-vehicle check below still covers every other vehicle.
    for (var winch : dev.fourthdimension.shuttle.ShuttleRuntime.winches(level)) {
      if (winch.phase == dev.fourthdimension.shuttle.ShuttleWinchEntity.Phase.BOARDING
          || winch.phase == dev.fourthdimension.shuttle.ShuttleWinchEntity.Phase.FAULT)
        continue;
      for (var id : winch.cars) {
        var car = data.vehicles.get(id);
        if (car == null || !(container.getSubLevel(id) instanceof ServerSubLevel parent)
            || parent.isRemoved()) continue;
        var rail = matchingRail(data, car);
        if (rail == null || !rail.valid
            || dev.fourthdimension.track.TrackGeometry.resolve(level, rail) == null) {
          dev.fourthdimension.vehicle.VehicleOperations.failForLostSupport(level, car);
          break;
        }
      }
    }
    dev.fourthdimension.shuttle.ShuttleRuntime.tick(level, event.getTimeStep());
    dev.fourthdimension.vehicle.RecoveryOperations.tick(level, event.getTimeStep());
    for (var vehicle : data.vehicles.values()) {
      var parentRaw = container.getSubLevel(vehicle.chassis);
      if (!(parentRaw instanceof ServerSubLevel parent) || parent.isRemoved()) continue;
      vehicleUpdates++;
      physics.updatePose(parent);
      if (dev.fourthdimension.vehicle.WingVehicle.is(vehicle)) {
        dev.fourthdimension.vehicle.WingRuntime.tick(level, parent, vehicle, event.getTimeStep());
        continue;
      }
      FoundationData.Rail rail = matchingRail(data, vehicle);
      boolean content = !FoundationData.VARIANT.equals(vehicle.variant);
      boolean supportLost =
          content
              && (rail == null
                  || !rail.valid
                  || dev.fourthdimension.track.TrackGeometry.resolve(level, rail) == null);
      if (supportLost) dev.fourthdimension.vehicle.VehicleOperations.failForLostSupport(level, vehicle);
      if (content && !supportLost)
        dev.fourthdimension.vehicle.BoundaryGuard.check(level, parent, vehicle, rail);
      if (content && (vehicle.stopping || vehicle.maintenance && vehicle.recoveryCart == null))
        dev.fourthdimension.vehicle.VehicleOperations.brake(level, vehicle, event.getTimeStep());
      double target = Double.NaN;
      if (rail != null && rail.valid && vehicle.family.driven()) {
        var edge = matchingEdge(graph, rail);
        if (edge != null && CoasterCartResolve.cartCenterWorld(parent) != null) {
          var center = CoasterCartResolve.cartCenterWorld(parent);
          var hit =
              CoasterPathTrackFrame.closestHitOnEdge(
                  level, edge, new Vec3(center.x, center.y, center.z));
          if (hit != null) {
            target =
                rail.profile()
                    .angleAt(
                        dev.fourthdimension.track.TrackGeometry.distance(
                            level, edge, rail, hit.t()));
          }
        }
      }
      for (var carrier : vehicle.carriers) {
        var childRaw = container.getSubLevel(carrier.body);
        if (!(childRaw instanceof ServerSubLevel child) || child.isRemoved()) {
          if (dev.fourthdimension.vehicle.SpinningVehicle.is(vehicle))
            dev.fourthdimension.vehicle.SpinningRuntime.fault(vehicle, "S1 carrier body unavailable");
          continue;
        }
        long carrierStart = System.nanoTime();
        carrierUpdates++;
        physics.updatePose(child);
        var handle = JOINTS.get(child);
        if (handle == null || !handle.isValid()) {
          constraintsRebuilt++;
          var p = parent.getPlot().getCenterBlock();
          var c = child.getPlot().getCenterBlock();
          handle =
              pipeline.addConstraint(
                  parent,
                  child,
                  new RotaryConstraintConfiguration(
                      new Vector3d(
                          p.getX() + .5 + carrier.side,
                          p.getY() + .5 + carrier.pivotHeight,
                          p.getZ() + .5 + carrier.longitudinal),
                      new Vector3d(
                          c.getX() + .5 + carrier.childPivotX, c.getY() + .5 + carrier.childPivotOffset, c.getZ() + .5 + carrier.childPivotZ),
                      vehicle.family.axis().vector(),
                      vehicle.family.axis().vector()));
          handle.setContactsEnabled(false);
          JOINTS.put(child, handle);
          // Sable omits very small angular speeds from its native save. Restore
          // our exact relative speed once when rebuilding the joint, not per tick.
          var restoredAxis = vehicle.family.axis().vector().rotate(parent.logicalPose().orientation());
          var desired =
              pipeline
                  .getAngularVelocity(parent, new Vector3d())
                  .add(restoredAxis.mul(carrier.omega));
          pipeline.addLinearAndAngularVelocity(
              child,
              new Vector3d(),
              desired.sub(pipeline.getAngularVelocity(child, new Vector3d())));
        }
        var relative =
            new Quaterniond(parent.logicalPose().orientation())
                .conjugate()
                .mul(child.logicalPose().orientation());
        carrier.angle = vehicle.family.axis().angle(carrier.angle, relative);
        var axis = vehicle.family.axis().vector().rotate(parent.logicalPose().orientation());
        carrier.omega =
            pipeline
                .getAngularVelocity(child, new Vector3d())
                .sub(pipeline.getAngularVelocity(parent, new Vector3d()))
                .dot(axis);
        if (dev.fourthdimension.vehicle.SpinningVehicle.is(vehicle)) {
          dev.fourthdimension.vehicle.NativeLoading.update(child, vehicle, carrier);
          dev.fourthdimension.vehicle.SpinningRuntime.update(level, parent, child, vehicle, carrier,
              rail, handle, event.getTimeStep());
          carrierNanos += System.nanoTime() - carrierStart;
          continue;
        }
        boolean drive = vehicle.family.driven() || carrier.capture;
        double goal =
            carrier.capture
                ? Math.rint(carrier.angle / (2 * Math.PI)) * 2 * Math.PI
                : Double.isFinite(target) ? target : carrier.angle;
        if (content) {
          var servo =
              SERVOS.computeIfAbsent(
                  child, k -> new dev.fourthdimension.physics.RotationServo(carrier.angle));
          goal = servo.advance(goal, carrier.angle, event.getTimeStep());
        }
        handle.setMotor(
            RotaryConstraintHandle.DEFAULT_AXIS,
            Math.atan2(Math.sin(goal), Math.cos(goal)),
            drive ? 30000 : 0,
            drive ? 2500 : vehicle.damping,
            true,
            80000);
        if (!FoundationData.VARIANT.equals(vehicle.variant)) {
          dev.fourthdimension.vehicle.NativeLoading.update(child, vehicle, carrier);
        } else {
          BlockPos seat = child.getPlot().getCenterBlock().below();
          carrier.occupants =
              level.getEntitiesOfClass(SeatEntity.class, new AABB(seat)).stream()
                  .flatMap(s -> s.getPassengers().stream())
                  .map(Entity::getUUID)
                  .toList();
        }
        carrierNanos += System.nanoTime() - carrierStart;
      }
      dev.fourthdimension.magnetic.MagneticRuntime.tick(
          level, vehicle, parent, event.getTimeStep());
      vehicle.boardingReady = dev.fourthdimension.vehicle.Boarding.ready(level, vehicle);
    }
    if (!data.vehicles.isEmpty()) data.setDirty();
    nanos += System.nanoTime() - start;
    steps++;
  }

  private static FoundationData.Rail matchingRail(
      FoundationData data, FoundationData.Vehicle vehicle) {
    for (var rail : data.rails)
      if (rail.matches(vehicle.boundFrom, vehicle.boundTo)) return rail;
    return null;
  }

  private static CoasterPathEdge matchingEdge(
      CoasterPathGraph graph, FoundationData.Rail rail) {
    for (var edge : graph.edges()) if (rail.matches(edge.from(), edge.to())) return edge;
    return null;
  }
}
