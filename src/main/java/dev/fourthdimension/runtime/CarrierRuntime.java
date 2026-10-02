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
  private CarrierRuntime() {}

  private static final Map<ServerSubLevel, RotaryConstraintHandle> JOINTS = new WeakHashMap<>();
  private static final Map<ServerSubLevel, dev.fourthdimension.physics.RotationServo> SERVOS =
      new WeakHashMap<>();
  public static long nanos, steps;

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
        carrier.angle = FreeRotor.unwrap(carrier.angle, 2 * Math.atan2(relative.x, relative.w));
        var axis = new Vector3d(1, 0, 0).rotate(parent.logicalPose().orientation());
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
    dev.fourthdimension.shuttle.ShuttleRuntime.tick(level, event.getTimeStep());
    dev.fourthdimension.vehicle.RecoveryOperations.tick(level, event.getTimeStep());
    for (var vehicle : data.vehicles.values()) {
      var parentRaw = container.getSubLevel(vehicle.chassis);
      if (!(parentRaw instanceof ServerSubLevel parent) || parent.isRemoved()) continue;
      physics.updatePose(parent);
      FoundationData.Rail rail =
          data.rails.stream()
              .filter(r -> r.matches(vehicle.boundFrom, vehicle.boundTo))
              .findFirst()
              .orElse(null);
      boolean content = !FoundationData.VARIANT.equals(vehicle.variant);
      boolean supportLost =
          content
              && (rail == null
                  || !rail.valid
                  || dev.fourthdimension.track.TrackGeometry.resolve(level, rail) == null);
      if (supportLost) {
        vehicle.stopping = true;
        vehicle.restraints = true;
        vehicle.fault = "Matching track support lost";
      }
      if (content && !supportLost)
        dev.fourthdimension.vehicle.BoundaryGuard.check(level, parent, vehicle, rail);
      if (content && (vehicle.stopping || vehicle.maintenance && vehicle.recoveryCart == null))
        dev.fourthdimension.vehicle.VehicleOperations.brake(level, vehicle, event.getTimeStep());
      double target = Double.NaN;
      if (rail != null && rail.valid && vehicle.family.driven()) {
        var edge =
            CoasterPathGraphManager.get(level).edges().stream()
                .filter(e -> rail.matches(e.from(), e.to()))
                .findFirst()
                .orElse(null);
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
        if (!(childRaw instanceof ServerSubLevel child) || child.isRemoved()) continue;
        physics.updatePose(child);
        var handle = JOINTS.get(child);
        if (handle == null || !handle.isValid()) {
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
                          c.getX() + .5, c.getY() + .5 + carrier.childPivotOffset, c.getZ() + .5),
                      new Vector3d(1, 0, 0),
                      new Vector3d(1, 0, 0)));
          handle.setContactsEnabled(false);
          JOINTS.put(child, handle);
          // Sable omits very small angular speeds from its native save. Restore
          // our exact relative speed once when rebuilding the joint, not per tick.
          var restoredAxis = new Vector3d(1, 0, 0).rotate(parent.logicalPose().orientation());
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
        carrier.angle = FreeRotor.unwrap(carrier.angle, 2 * Math.atan2(relative.x, relative.w));
        var axis = new Vector3d(1, 0, 0).rotate(parent.logicalPose().orientation());
        carrier.omega =
            pipeline
                .getAngularVelocity(child, new Vector3d())
                .sub(pipeline.getAngularVelocity(parent, new Vector3d()))
                .dot(axis);
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
      }
      dev.fourthdimension.magnetic.MagneticRuntime.tick(
          level, vehicle, parent, event.getTimeStep());
      vehicle.boardingReady = dev.fourthdimension.vehicle.Boarding.ready(level, vehicle);
    }
    if (!data.vehicles.isEmpty()) data.setDirty();
    nanos += System.nanoTime() - start;
    steps++;
  }
}
