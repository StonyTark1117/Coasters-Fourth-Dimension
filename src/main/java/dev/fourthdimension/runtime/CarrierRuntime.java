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
    for (var vehicle : data.vehicles.values()) {
      var parentRaw = container.getSubLevel(vehicle.chassis);
      if (!(parentRaw instanceof ServerSubLevel parent) || parent.isRemoved()) continue;
      physics.updatePose(parent);
      FoundationData.Rail rail =
          data.rails.stream()
              .filter(r -> r.matches(vehicle.boundFrom, vehicle.boundTo))
              .findFirst()
              .orElse(null);
      double target = 0;
      if (rail != null && vehicle.family.driven()) {
        var edge =
            CoasterPathGraphManager.get(level).edges().stream()
                .filter(e -> rail.matches(e.from(), e.to()))
                .findFirst()
                .orElse(null);
        if (edge != null) {
          var center = CoasterCartResolve.cartCenterWorld(parent);
          var hit =
              CoasterPathTrackFrame.closestHitOnEdge(
                  level, edge, new Vec3(center.x, center.y, center.z));
          if (hit != null) {
            double t = edge.from().equals(rail.from()) ? hit.t() : 1 - hit.t();
            // Foundation fixture is straight; general arc-length sampling is FD102.
            double length = rail.from().getCenter().distanceTo(rail.to().getCenter());
            target = rail.profile().angleAt(t * length);
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
                      new Vector3d(p.getX() + .5 + carrier.side, p.getY() + 2.5, p.getZ() + .5),
                      new Vector3d(c.getX() + .5, c.getY() + .5, c.getZ() + .5),
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
        double goal = carrier.capture ? 0 : target;
        handle.setMotor(
            RotaryConstraintHandle.DEFAULT_AXIS,
            Math.atan2(Math.sin(goal), Math.cos(goal)),
            drive ? 30000 : 0,
            drive ? 2500 : 5,
            true,
            80000);
        BlockPos seat = child.getPlot().getCenterBlock().below();
        carrier.occupants =
            level.getEntitiesOfClass(SeatEntity.class, new AABB(seat)).stream()
                .flatMap(s -> s.getPassengers().stream())
                .map(Entity::getUUID)
                .toList();
      }
    }
    if (!data.vehicles.isEmpty()) data.setDirty();
    nanos += System.nanoTime() - start;
    steps++;
  }
}
