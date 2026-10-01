package dev.fourthdimension.integration.magnetized;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.magnetic.MagneticRuntime;
import dev.fourthdimension.physics.MagneticSpan;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.antopfr.coastersmagnetized.magnet.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.joml.Vector3d;

public final class MagnetizedRideCheck {
  private MagnetizedRideCheck() {}

  public static void setEquipment(
      ServerLevel level, FoundationData.Rail rail, AnchorMode mode, int percent) {
    MagnetizedAnchors.setMode(level, rail.from, mode);
    var anchor = (SmartBlockEntity) level.getBlockEntity(rail.from);
    anchor.getBehaviour(ScrollValueBehaviour.TYPE).setValue(percent);
    level.setBlockAndUpdate(rail.from.below(), Blocks.REDSTONE_BLOCK.defaultBlockState());
  }

  private static void velocity(ServerLevel level, FoundationData.Vehicle v, double z) {
    var container = SubLevelContainer.getContainer(level);
    var physics = container.physicsSystem().getPipeline();
    var ids = new ArrayList<UUID>();
    ids.add(v.chassis);
    v.carriers.forEach(c -> ids.add(c.body));
    for (var id : ids) {
      var body = (ServerSubLevel) container.getSubLevel(id);
      physics.addLinearAndAngularVelocity(
          body,
          new Vector3d(0, 0, z).sub(physics.getLinearVelocity(body, new Vector3d())),
          new Vector3d());
    }
  }

  public static void run(GameTestHelper helper) {
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    var physics = container.physicsSystem().getPipeline();
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000402"), "FDMagnetizedTest"));
    player.setGameMode(GameType.CREATIVE);
    player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
    var rails = new ArrayList<FoundationData.Rail>();
    var cars = new ArrayList<FoundationData.Vehicle>();
    var riders = new ArrayList<net.minecraft.world.entity.animal.Pig>();
    for (var design : TrackDesign.values()) {
      var from = new BlockPos(-900 + design.ordinal() * 32, 160, -200);
      var to = from.offset(0, 0, 60);
      for (int x = (from.getX() - 16) >> 4; x <= (from.getX() + 16) >> 4; x++)
        for (int z = (from.getZ() - 16) >> 4; z <= (to.getZ() + 16) >> 4; z++)
          level.setChunkForced(x, z, true);
      for (var pos : List.of(from, to))
        level.setBlockAndUpdate(
            pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
      var rail = TrackPlacement.connect(level, from, to, design, player, player.getMainHandItem());
      if (design == TrackDesign.FREE_SPIN)
        rail.magnets = List.of(new MagneticSpan(0, 60, MagneticSpan.Mode.INDUCE, 1, 2, .1, 1));
      rails.add(rail);
    }
    helper.runAtTickTime(
        15,
        () -> {
          for (var design : TrackDesign.values()) {
            var rail = rails.get(design.ordinal());
            var edge = TrackGeometry.resolve(level, rail);
            cars.add(
                VehicleFactory.create(
                    level,
                    design,
                    new CoasterPathTrackFrame.GraphHit(
                        CoasterPathGraphManager.sampleEdgePosition(level, edge, .5), edge, .5)));
            setEquipment(level, rail, AnchorMode.MAGNET, 10);
          }
        });
    helper.runAtTickTime(
        85,
        () -> {
          for (int i = 0; i < cars.size(); i++) {
            var v = cars.get(i);
            var edge = TrackGeometry.resolve(level, rails.get(i));
            helper.assertTrue(
                Boarding.ready(level, v), "Magnetized held fixture not ready: " + v.family);
            helper.assertTrue(
                MagnetBoost.accelerationFor(level, edge) > 0, "Magnetized anchor not active");
            var body = (ServerSubLevel) container.getSubLevel(v.chassis);
            var hit =
                new CoasterPathTrackFrame.GraphHit(
                    CoasterPathGraphManager.sampleEdgePosition(level, edge, .5), edge, .5);
            var basis = CoasterPathTrackFrame.trackBasisAtHit(level, hit);
            // Probe each invalid combination synchronously, restoring all state before the next
            // tick.
            boolean stopping = v.stopping, restraints = v.restraints, retention = v.maintenance;
            String fault = v.fault;
            for (int invalid = 0; invalid < 4; invalid++) {
              v.stopping = invalid == 0;
              v.restraints = invalid != 1;
              v.maintenance = invalid == 2;
              v.fault = invalid == 3 ? "Integration guard probe" : "";
              velocity(level, v, .75);
              var before = physics.getLinearVelocity(body, new Vector3d());
              CoasterCartChainLiftDrive.applySpeedMatch(
                  body, level, container.physicsSystem(), hit, basis, 1);
              helper.assertTrue(
                  physics.getLinearVelocity(body, new Vector3d()).distance(before) < 1e-8,
                  "Magnetized bypassed interlock " + invalid + " for " + v.family);
            }
            v.stopping = stopping;
            v.restraints = restraints;
            v.maintenance = retention;
            v.fault = fault;
            velocity(level, v, 0);
            var pig = net.minecraft.world.entity.EntityType.PIG.create(level);
            pig.setNoAi(true);
            pig.setPos(rails.get(i).from.getX(), 163, -170);
            level.addFreshEntity(pig);
            var c = v.carriers.getFirst();
            com.simibubi.create.content.contraptions.actors.seat.SeatBlock.sitDown(
                level,
                container.getSubLevel(c.body).getPlot().getCenterBlock().offset(c.seats.getFirst()),
                pig);
            helper.assertTrue(pig.isPassenger(), "Magnetized rider failed to board");
            riders.add(pig);
            VehicleOperations.restraints(level, v, true);
            if (v.family != Family.PAX) {
              VehicleOperations.dispatch(level, v);
              velocity(level, v, 1);
              var before = physics.getLinearVelocity(body, new Vector3d());
              CoasterCartChainLiftDrive.applySpeedMatch(
                  body, level, container.physicsSystem(), hit, basis, .1);
              helper.assertTrue(
                  physics.getLinearVelocity(body, new Vector3d()).distance(before) > .001,
                  "Valid drive did not apply its velocity increment; negative probes would be"
                      + " inconclusive");
              velocity(level, v, 1);
            }
          }
        });
    helper.runAtTickTime(
        125,
        () -> {
          for (int i = 0; i < cars.size(); i++) {
            var v = cars.get(i);
            double speed =
                physics.getLinearVelocity(
                        (ServerSubLevel) container.getSubLevel(v.chassis), new Vector3d())
                    .z;
            System.out.println(
                "FD_MAGNETIZED_FORWARD family="
                    + v.family
                    + " speed="
                    + speed
                    + " fault="
                    + v.fault);
            helper.assertTrue(
                v.family == Family.PAX ? Math.abs(speed) < .01 : speed > 1.1,
                "Native Magnetized acceleration failed: " + v.family);
            setEquipment(level, rails.get(i), AnchorMode.BRAKE, 100);
          }
        });
    helper.runAtTickTime(
        175,
        () -> {
          for (int i = 0; i < cars.size(); i++) {
            var v = cars.get(i);
            double speed =
                physics
                    .getLinearVelocity(
                        (ServerSubLevel) container.getSubLevel(v.chassis), new Vector3d())
                    .length();
            System.out.println("FD_MAGNETIZED_BRAKE family=" + v.family + " speed=" + speed);
            helper.assertTrue(speed < .15, "Magnetized forward brake failed: " + v.family);
            setEquipment(level, rails.get(i), AnchorMode.MAGNET, 10);
            if (v.family != Family.PAX) velocity(level, v, -1);
          }
        });
    helper.runAtTickTime(
        215,
        () -> {
          for (int i = 0; i < cars.size(); i++) {
            var v = cars.get(i);
            double speed =
                physics.getLinearVelocity(
                        (ServerSubLevel) container.getSubLevel(v.chassis), new Vector3d())
                    .z;
            System.out.println(
                "FD_MAGNETIZED_ROLLBACK family="
                    + v.family
                    + " speed="
                    + speed
                    + " fault="
                    + v.fault);
            helper.assertTrue(
                v.family == Family.PAX ? Math.abs(speed) < .01 : speed < -1.1,
                "Magnetized rollback acceleration failed: " + v.family);
            setEquipment(level, rails.get(i), AnchorMode.BRAKE, 100);
          }
        });
    helper.runAtTickTime(
        265,
        () -> {
          for (int i = 0; i < cars.size(); i++) {
            var v = cars.get(i);
            double speed =
                physics
                    .getLinearVelocity(
                        (ServerSubLevel) container.getSubLevel(v.chassis), new Vector3d())
                    .length();
            helper.assertTrue(speed < .15, "Magnetized rollback brake failed: " + v.family);
            helper.assertTrue(
                v.fault.isBlank() && riders.get(i).isPassenger(),
                "Magnetized run lost rider or matching track");
            VehicleOperations.capture(level, v);
          }
        });
    helper.runAtTickTime(
        430,
        () -> {
          for (int i = 0; i < cars.size(); i++) {
            var v = cars.get(i);
            helper.assertTrue(
                Boarding.ready(level, v), "Magnetized ride failed recapture: " + v.family);
            if (v.family == Family.FREE_SPIN)
              for (int c = 0; c < v.carriers.size(); c++) {
                var audit =
                    MagneticRuntime.audit(
                        (ServerSubLevel) container.getSubLevel(v.carriers.get(c).body));
                System.out.println(
                    "FD_MAGNETIZED_SEAT group="
                        + c
                        + " impulses="
                        + audit.impulses
                        + " energy="
                        + audit.energyChange);
                helper.assertTrue(
                    c == 0 ? audit.impulses > 0 : audit.impulses == 0,
                    "Magnetized changed seat-magnet target ownership");
                helper.assertTrue(
                    audit.energyChange <= 1e-6, "Combined magnetic equipment added energy");
              }
            VehicleOperations.restraints(level, v, false);
            riders.get(i).stopRiding();
            riders.get(i).discard();
            container.removeSubLevel(
                container.getSubLevel(v.chassis), SubLevelRemovalReason.REMOVED);
          }
          helper.succeed();
        });
  }
}
