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
    run(helper, false);
  }

  public static void runLinked(GameTestHelper helper) {
    run(helper, true);
  }

  private static void run(GameTestHelper helper, boolean linked) {
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    var physics = container.physicsSystem().getPipeline();
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString(
                    linked
                        ? "fdfdfdfd-0000-0000-0000-000000000409"
                        : "fdfdfdfd-0000-0000-0000-000000000402"),
                "FDMagnetizedTest"));
    player.setGameMode(GameType.CREATIVE);
    player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
    var designs = linked ? List.of(TrackDesign.ARROW) : List.of(TrackDesign.values());
    String marker = linked ? "FD_MAGNETIZED_LINKED" : "FD_MAGNETIZED";
    var rails = new ArrayList<FoundationData.Rail>();
    var carRails = new ArrayList<FoundationData.Rail>();
    var cars = new ArrayList<FoundationData.Vehicle>();
    var riders = new ArrayList<net.minecraft.world.entity.animal.Pig>();
    for (var design : designs) {
      var from = new BlockPos(-900 + design.ordinal() * 32, 160, linked ? 200 : -200);
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
          for (var design : designs) {
            var rail = rails.get(design.ordinal());
            var edge = TrackGeometry.resolve(level, rail);
            for (double t : linked ? new double[] {.5, .4} : new double[] {.5}) {
              cars.add(
                  VehicleFactory.create(
                      level,
                      design,
                      new CoasterPathTrackFrame.GraphHit(
                          CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t)));
              carRails.add(rail);
            }
            setEquipment(level, rail, AnchorMode.MAGNET, 10);
          }
        });
    helper.runAtTickTime(
        85,
        () -> {
          if (linked)
            helper.assertTrue(
                CoasterCartTrainLinkConstraint.linkCarts(
                    level,
                    (ServerSubLevel) container.getSubLevel(cars.getFirst().chassis),
                    (ServerSubLevel) container.getSubLevel(cars.getLast().chassis),
                    6),
                "Magnetized Arrow train link failed");
          for (int i = 0; i < cars.size(); i++) {
            var v = cars.get(i);
            var edge = TrackGeometry.resolve(level, carRails.get(i));
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
            pig.setPos(carRails.get(i).from.getX(), 163, carRails.get(i).from.getZ() + 30);
            level.addFreshEntity(pig);
            var c = v.carriers.getFirst();
            com.simibubi.create.content.contraptions.actors.seat.SeatBlock.sitDown(
                level,
                container.getSubLevel(c.body).getPlot().getCenterBlock().offset(c.seats.getFirst()),
                pig);
            helper.assertTrue(pig.isPassenger(), "Magnetized rider failed to board");
            riders.add(pig);
          }
          for (var v : cars) VehicleOperations.restraints(level, v, true);
          if (linked) {
            var lead = cars.getFirst();
            var peer = cars.getLast();
            var body = (ServerSubLevel) container.getSubLevel(lead.chassis);
            var edge = TrackGeometry.resolve(level, carRails.getFirst());
            var hit =
                new CoasterPathTrackFrame.GraphHit(
                    CoasterPathGraphManager.sampleEdgePosition(level, edge, .5), edge, .5);
            var basis = CoasterPathTrackFrame.trackBasisAtHit(level, hit);
            for (var v : cars) v.stopping = false;
            for (int invalid = 0; invalid < 4; invalid++) {
              peer.stopping = invalid == 0;
              peer.restraints = invalid != 1;
              peer.maintenance = invalid == 2;
              peer.fault = invalid == 3 ? "Integration peer guard probe" : "";
              velocity(level, lead, .75);
              var before = physics.getLinearVelocity(body, new Vector3d());
              CoasterCartChainLiftDrive.applySpeedMatch(
                  body, level, container.physicsSystem(), hit, basis, .1);
              helper.assertTrue(
                  physics.getLinearVelocity(body, new Vector3d()).distance(before) < 1e-8,
                  "Magnetized drove a train with unsafe trailing car " + invalid);
              peer.stopping = false;
              peer.restraints = true;
              peer.maintenance = false;
              peer.fault = "";
              CoasterCartChainLiftDrive.applySpeedMatch(
                  body, level, container.physicsSystem(), hit, basis, .1);
              double healthyDelta =
                  physics.getLinearVelocity(body, new Vector3d()).distance(before);
              helper.assertTrue(
                  healthyDelta > .001,
                  "Healthy trailing car did not restore native propulsion for probe " + invalid);
              System.out.println(
                  marker
                      + "_PEER_GUARD invalid="
                      + invalid
                      + " unchanged=true healthyDelta="
                      + healthyDelta);
            }
            peer.restraints = true;
            peer.maintenance = false;
            peer.fault = "";
            for (var v : cars) {
              v.stopping = true;
              velocity(level, v, 0);
            }
          }
          for (int i = 0; i < cars.size(); i++) {
            var v = cars.get(i);
            if (v.family != Family.PAX) {
              if (v.stopping) VehicleOperations.dispatch(level, v);
              var body = (ServerSubLevel) container.getSubLevel(v.chassis);
              var edge = TrackGeometry.resolve(level, carRails.get(i));
              var hit =
                  new CoasterPathTrackFrame.GraphHit(
                      CoasterPathGraphManager.sampleEdgePosition(level, edge, .5), edge, .5);
              var basis = CoasterPathTrackFrame.trackBasisAtHit(level, hit);
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
                marker + "_FORWARD family=" + v.family + " speed=" + speed + " fault=" + v.fault);
            helper.assertTrue(
                v.family == Family.PAX ? Math.abs(speed) < .01 : speed > 1.1,
                "Native Magnetized acceleration failed: " + v.family);
            setEquipment(level, carRails.get(i), AnchorMode.BRAKE, 100);
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
            System.out.println(marker + "_BRAKE family=" + v.family + " speed=" + speed);
            helper.assertTrue(speed < .15, "Magnetized forward brake failed: " + v.family);
            setEquipment(level, carRails.get(i), AnchorMode.MAGNET, 10);
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
                marker + "_ROLLBACK family=" + v.family + " speed=" + speed + " fault=" + v.fault);
            helper.assertTrue(
                v.family == Family.PAX ? Math.abs(speed) < .01 : speed < -1.1,
                "Magnetized rollback acceleration failed: " + v.family);
            setEquipment(level, carRails.get(i), AnchorMode.BRAKE, 100);
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
            System.out.println(marker + "_REVERSE_BRAKE car=" + i + " speed=" + speed);
            if (linked)
              helper.assertTrue(
                  VehicleOperations.consist(level, v).size() == 2,
                  "Magnetized motion lost the Arrow coupling");
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
                    marker
                        + "_SEAT group="
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
            if (linked)
              helper.assertTrue(
                  VehicleOperations.consist(level, v).size() == 2,
                  "Magnetized capture lost the Arrow coupling");
            System.out.println(
                marker + "_CAPTURE car=" + i + " ready=true rider=" + riders.get(i).isPassenger());
          }
          for (int i = 0; i < cars.size(); i++) {
            var v = cars.get(i);
            VehicleOperations.restraints(level, v, false);
            riders.get(i).stopRiding();
            riders.get(i).discard();
          }
          for (var v : cars)
            container.removeSubLevel(
                container.getSubLevel(v.chassis), SubLevelRemovalReason.REMOVED);
          helper.succeed();
        });
  }
}
