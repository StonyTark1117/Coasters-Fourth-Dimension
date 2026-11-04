package dev.fourthdimension.gametest;

import static dev.fourthdimension.gametest.EngineeredGameTests.*;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.integration.OptionalIntegrations;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

/** Native FE capability and motor accounting, isolated from the default-energy test batch. */
@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class EngineeredEnergyGameTests {
  private static final String BATCH = "fourthdimension_engineered_energy";
  private static ModConfigSpec.BooleanValue requirement;
  private static Boolean previous;
  private static Runnable recoveryCleanup;

  @BeforeBatch(batch = BATCH)
  public static void enableEnergy(ServerLevel level) {
    if (!OptionalIntegrations.installed(OptionalIntegrations.ENGINEERED).mayInspect()) return;
    try {
      requirement =
          (ModConfigSpec.BooleanValue)
              type("CoastersEngineeredConfig").getField("REQUIRE_ENERGY").get(null);
      previous = requirement.get();
      requirement.set(true);
      if (!(boolean) invoke(type("TrackEnergy"), "required"))
        throw new IllegalStateException("Pinned Engineered energy requirement did not activate");
      System.out.println("FD_ENGINEERED_FE isolatedBatch requireEnergy=true previous=" + previous);
    } catch (ReflectiveOperationException failure) {
      throw new IllegalStateException("Pinned Engineered FE config unavailable", failure);
    }
  }

  @AfterBatch(batch = BATCH)
  public static void restoreEnergy(ServerLevel level) {
    try {
      // Batch teardown also runs when the recovery test fails or times out.
      if (recoveryCleanup != null) recoveryCleanup.run();
    } finally {
      recoveryCleanup = null;
      if (previous != null) {
        requirement.set(previous);
        System.out.println("FD_ENGINEERED_FE restored requireEnergy=" + previous);
        previous = null;
        requirement = null;
      }
    }
  }

  private static final class Fixture {
    FoundationData.Rail rail;
    FoundationData.Vehicle vehicle;
    ServerSubLevel body;
    BlockPos controllerPos;
    Object controller;
    IEnergyStorage energy;
    Pig rider;
    double startZ;
    int beforeBrake;
    int peakBrakeEnergy;
    int retainedEnergy;

    String label() {
      return vehicle == null ? "ordinary" : vehicle.family.id();
    }

    boolean pax() {
      return vehicle != null && vehicle.family == Family.PAX;
    }
  }

  @GameTest(template = "empty", batch = BATCH, timeoutTicks = 440)
  public static void nativeMotorSpanAndEnergySurviveBlockEntityReload(GameTestHelper helper) {
    if (!OptionalIntegrations.installed(OptionalIntegrations.ENGINEERED).mayInspect()) {
      System.out.println("FD_ENGINEERED_FE_RELOAD absent/uninspected: scenario not exercised");
      helper.succeed();
      return;
    }
    helper.assertTrue((boolean) invoke(type("TrackEnergy"), "required"), "FE requirement disabled");
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    for (int x = -45; x <= -43; x++)
      for (int z = -39; z <= -35; z++) level.setChunkForced(x, z, true);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000415"), "FDEngineeredReload"));
    player.setGameMode(GameType.CREATIVE);
    player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
    var from = new BlockPos(-704, 120, -608);
    var to = from.offset(0, 0, 48);
    for (var pos : List.of(from, to))
      level.setBlockAndUpdate(
          pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
    var rail =
        TrackPlacement.connect(
            level, from, to, TrackDesign.ARROW, player, player.getMainHandItem());
    var controllerPos = from.offset(-6, 0, 0);
    level.setBlockAndUpdate(
        controllerPos,
        BuiltInRegistries.BLOCK
            .get(ResourceLocation.parse("coastersengineered:linear_motor_controller"))
            .defaultBlockState());
    FoundationData.Vehicle[] vehicle = {null};
    Pig[] rider = {null};
    Object[] controller = {null};
    IEnergyStorage[] energy = {null};
    double[] heldZ = {0};
    int[] supplied = {0};

    helper.runAtTickTime(
        15,
        () -> {
          controller[0] = configureMotor(helper, rail, controllerPos, 10, "FORWARD");
          var edge = TrackGeometry.resolve(level, rail);
          double t = edge.from().equals(from) ? .25 : .75;
          var hit =
              new CoasterPathTrackFrame.GraphHit(
                  CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t);
          vehicle[0] = VehicleFactory.create(level, TrackDesign.ARROW, hit);
        });
    helper.runAtTickTime(
        80,
        () -> {
          var v = vehicle[0];
          helper.assertTrue(Boarding.ready(level, v), "Reload fixture not ready to board");
          var chassis = (ServerSubLevel) container.getSubLevel(v.chassis);
          var carrier = v.carriers.getFirst();
          var body = container.getSubLevel(carrier.body);
          rider[0] = EntityType.PIG.create(level);
          rider[0].setNoAi(true);
          rider[0].setPos(
              chassis.logicalPose().position().x(), 123, chassis.logicalPose().position().z());
          level.addFreshEntity(rider[0]);
          SeatBlock.sitDown(
              level, body.getPlot().getCenterBlock().offset(carrier.seats.getFirst()), rider[0]);
          helper.assertTrue(rider[0].isPassenger(), "Reload fixture failed native boarding");
          VehicleOperations.restraints(level, v, true);
          VehicleOperations.retention(level, v, true);
          energy[0] = level.getCapability(Capabilities.EnergyStorage.BLOCK, from, Direction.UP);
          helper.assertTrue(energy[0] != null, "Motor anchor exposes no FE capability");
          supplied[0] = energy[0].receiveEnergy(40000, false);
          helper.assertTrue(supplied[0] == 40000, "Motor anchor failed FE supply");
          heldZ[0] = chassis.logicalPose().position().z();
          helper.assertTrue(
              (boolean) invoke(controller[0], "isLinked"), "Motor unlinked before reload");
          reloadBlockEntity(helper, from);
          reloadBlockEntity(helper, controllerPos);
        });
    helper.runAtTickTime(
        140,
        () -> {
          var v = vehicle[0];
          var chassis = (ServerSubLevel) container.getSubLevel(v.chassis);
          controller[0] = level.getBlockEntity(controllerPos);
          energy[0] = level.getCapability(Capabilities.EnergyStorage.BLOCK, from, Direction.UP);
          helper.assertTrue(energy[0] != null, "Reload lost motor FE capability");
          helper.assertTrue(energy[0].getEnergyStored() == supplied[0], "Reload lost stored FE");
          helper.assertTrue(
              (boolean) invoke(controller[0], "isLinked"), "Reload lost controller link");
          helper.assertTrue(
              invoke(controller[0], "boundAnchor").equals(from), "Reload changed controller anchor");
          helper.assertTrue(
              invoke(controller[0], "commandMode").toString().equals("LAUNCH")
                  && ((Number) invoke(controller[0], "commandValue")).intValue() == 10,
              "Reload changed motor step");
          helper.assertTrue(
              Math.abs(chassis.logicalPose().position().z() - heldZ[0]) < .1,
              "Reloaded motor bypassed maintenance retention");
          helper.assertTrue(
              rider[0].isPassenger() && v.restraints, "Reload lost occupied restraints");
          assertMass(helper, v);
          System.out.println(
              "FD_ENGINEERED_FE_RELOAD linked=true stored="
                  + energy[0].getEnergyStored()
                  + " retained=true rider=true");
          VehicleOperations.retention(level, v, false);
          VehicleOperations.dispatch(level, v);
        });
    helper.runAtTickTime(
        250,
        () -> {
          var chassis = (ServerSubLevel) container.getSubLevel(vehicle[0].chassis);
          double distance = Math.abs(chassis.logicalPose().position().z() - heldZ[0]);
          helper.assertTrue(distance > 3, "Reloaded powered motor did not launch Arrow car");
          helper.assertTrue(
              energy[0].getEnergyStored() < supplied[0], "Reloaded motor spent no FE on launch");
          helper.assertTrue(rider[0].isPassenger(), "Reloaded launch lost passenger");
          setMode(controller[0], "STOP", 0);
          System.out.println(
              "FD_ENGINEERED_FE_RELOAD launched distance="
                  + distance
                  + " remaining="
                  + energy[0].getEnergyStored());
        });
    helper.runAtTickTime(
        370,
        () -> {
          var v = vehicle[0];
          var chassis = (ServerSubLevel) container.getSubLevel(v.chassis);
          double speed =
              container
                  .physicsSystem()
                  .getPipeline()
                  .getLinearVelocity(chassis, new Vector3d())
                  .length();
          helper.assertTrue(speed < .1, "Reloaded motor STOP failed");
          VehicleOperations.capture(level, v);
          helper.assertTrue(Boarding.ready(level, v), "Reloaded ride failed carrier recapture");
          VehicleOperations.restraints(level, v, false);
          rider[0].stopRiding();
          helper.assertTrue(!rider[0].isPassenger(), "Reloaded ride could not unload");
          rider[0].discard();
          for (var c : v.carriers)
            container.removeSubLevel(container.getSubLevel(c.body), SubLevelRemovalReason.REMOVED);
          container.removeSubLevel(chassis, SubLevelRemovalReason.REMOVED);
          FoundationData.get(level).vehicles.remove(v.chassis);
          ((CoasterAnchorpointBlockEntity) level.getBlockEntity(from))
              .removeAnchorPeerCurve(level, to);
          FoundationData.get(level).rails.remove(rail);
          for (var pos : List.of(from, to, controllerPos)) level.removeBlock(pos, false);
          System.out.println("FD_ENGINEERED_FE_RELOAD stopped=true recaptured=true unloaded=true");
          helper.succeed();
        });
  }

  private static void reloadBlockEntity(GameTestHelper helper, BlockPos pos) {
    var level = helper.getLevel();
    var original = level.getBlockEntity(pos);
    helper.assertTrue(original != null, "Reload target missing at " + pos);
    var saved = original.saveWithFullMetadata(level.registryAccess());
    var state = level.getBlockState(pos);
    level.removeBlockEntity(pos);
    var restored = BlockEntity.loadStatic(pos, state, saved, level.registryAccess());
    helper.assertTrue(restored != null, "Native block entity failed reload at " + pos);
    level.setBlockEntity(restored);
  }

  @GameTest(template = "empty", batch = BATCH, timeoutTicks = 650)
  public static void nativeEnergyStarvationSupplyBrakeAndRetention(GameTestHelper helper) {
    if (!OptionalIntegrations.installed(OptionalIntegrations.ENGINEERED).mayInspect()) {
      System.out.println("FD_ENGINEERED_FE absent/uninspected: energy scenario not exercised");
      helper.succeed();
      return;
    }
    helper.assertTrue((boolean) invoke(type("TrackEnergy"), "required"), "FE requirement disabled");
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    for (int x = -45; x <= -35; x++)
      for (int z = -32; z <= -28; z++) level.setChunkForced(x, z, true);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000414"), "FDEngineeredEnergy"));
    player.setGameMode(GameType.CREATIVE);
    player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
    var fixtures = new ArrayList<Fixture>();
    boolean[] braking = {false};
    helper.onEachTick(
        () -> {
          if (!braking[0]) return;
          for (var f : fixtures)
            f.peakBrakeEnergy = Math.max(f.peakBrakeEnergy, f.energy.getEnergyStored());
        });
    var controllerBlock =
        BuiltInRegistries.BLOCK.get(
            ResourceLocation.parse("coastersengineered:linear_motor_controller"));
    for (int i = 0; i <= TrackDesign.values().length; i++) {
      var fixture = new Fixture();
      var from = new BlockPos(-700 + i * 24, 120, -500);
      var to = from.offset(0, 0, 48);
      for (var pos : List.of(from, to))
        level.setBlockAndUpdate(
            pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
      var design = i == TrackDesign.values().length ? TrackDesign.ARROW : TrackDesign.values()[i];
      fixture.rail =
          TrackPlacement.connect(level, from, to, design, player, player.getMainHandItem());
      fixture.controllerPos = from.offset(-6, 0, 0);
      level.setBlockAndUpdate(fixture.controllerPos, controllerBlock.defaultBlockState());
      fixtures.add(fixture);
    }
    helper.runAtTickTime(
        15,
        () -> {
          for (int i = 0; i < fixtures.size(); i++) {
            var f = fixtures.get(i);
            f.controller = configureMotor(helper, f.rail, f.controllerPos, 10, "FORWARD");
            f.energy =
                level.getCapability(Capabilities.EnergyStorage.BLOCK, f.rail.from, Direction.UP);
            helper.assertTrue(f.energy != null, "Motor anchor exposes no native FE capability");
            helper.assertTrue(f.energy.getEnergyStored() == 0, "Fresh anchor was not empty");
            var edge = TrackGeometry.resolve(level, f.rail);
            double t = edge.from().equals(f.rail.from) ? .25 : .75;
            var hit =
                new CoasterPathTrackFrame.GraphHit(
                    CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t);
            if (i < TrackDesign.values().length) {
              f.vehicle = VehicleFactory.create(level, TrackDesign.values()[i], hit);
              f.body = (ServerSubLevel) container.getSubLevel(f.vehicle.chassis);
            } else {
              var q =
                  CoasterPathTrackFrame.quaternionFromTrackBasis(
                      CoasterPathTrackFrame.trackBasisAtHit(level, hit));
              var origin =
                  CoasterCartLinkPlacement.plotOriginForBearingCenter(
                      CoasterPathTrackFrame.pathAnchorForSnappedBlock(level, hit), q);
              f.body = CoasterCartSpawner.spawnMinimalContraption(level, origin, q, hit, true);
              helper.assertTrue(f.body != null, "Ordinary FE fixture failed assembly");
            }
          }
        });
    helper.runAtTickTime(
        80,
        () -> {
          for (var f : fixtures) {
            f.startZ = f.body.logicalPose().position().z();
            if (f.vehicle == null) continue;
            helper.assertTrue(Boarding.ready(level, f.vehicle), "FE fixture not ready to board");
            var carrier = f.vehicle.carriers.getFirst();
            var body = container.getSubLevel(carrier.body);
            f.rider = EntityType.PIG.create(level);
            f.rider.setNoAi(true);
            f.rider.setPos(f.body.logicalPose().position().x(), 123, f.startZ);
            level.addFreshEntity(f.rider);
            SeatBlock.sitDown(
                level, body.getPlot().getCenterBlock().offset(carrier.seats.getFirst()), f.rider);
            helper.assertTrue(f.rider.isPassenger(), "FE fixture failed normal native boarding");
            VehicleOperations.restraints(level, f.vehicle, true);
            if (f.pax()) VehicleOperations.retention(level, f.vehicle, true);
            else VehicleOperations.dispatch(level, f.vehicle);
            assertMass(helper, f.vehicle);
          }
        });
    helper.runAtTickTime(
        180,
        () -> {
          for (var f : fixtures) {
            double drift = Math.abs(f.body.logicalPose().position().z() - f.startZ);
            helper.assertTrue(drift < .1, "Empty native FE buffer still propelled " + f.label());
            helper.assertTrue(f.energy.getEnergyStored() == 0, "Empty buffer created energy");
            int received = f.energy.receiveEnergy(Integer.MAX_VALUE, false);
            helper.assertTrue(
                received == f.energy.getMaxEnergyStored() && received > 0,
                "Native FE capability did not fill the buffer");
            System.out.println(
                "FD_ENGINEERED_FE starved "
                    + f.label()
                    + " drift="
                    + drift
                    + " supplied="
                    + received);
          }
        });
    helper.runAtTickTime(
        280,
        () -> {
          for (var f : fixtures) {
            double distance = Math.abs(f.body.logicalPose().position().z() - f.startZ);
            double speed =
                container
                    .physicsSystem()
                    .getPipeline()
                    .getLinearVelocity(f.body, new Vector3d())
                    .length();
            f.beforeBrake = f.energy.getEnergyStored();
            f.peakBrakeEnergy = f.beforeBrake;
            if (f.pax()) {
              helper.assertTrue(distance < .1, "Supplied FE bypassed PAX propulsion ownership");
              helper.assertTrue(
                  f.beforeBrake == f.energy.getMaxEnergyStored(), "Held PAX motor consumed FE");
            } else {
              helper.assertTrue(distance > 3 && speed > 1, "FE supply did not launch " + f.label());
              helper.assertTrue(
                  f.beforeBrake < f.energy.getMaxEnergyStored(),
                  "Native launch did not consume FE");
              setMode(f.controller, "STOP", 0);
            }
            if (f.vehicle != null) {
              helper.assertTrue(f.rider.isPassenger(), "FE launch lost its passenger");
              helper.assertTrue(
                  f.vehicle.restraints && f.vehicle.fault.isEmpty(), "FE corrupted state");
              assertMass(helper, f.vehicle);
            } else {
              helper.assertTrue(
                  !FoundationData.get(level).vehicles.containsKey(f.body.getUniqueId()),
                  "Ordinary FE motor gained 4D state");
            }
            System.out.println(
                "FD_ENGINEERED_FE supplied "
                    + f.label()
                    + " distance="
                    + distance
                    + " speed="
                    + speed
                    + " remaining="
                    + f.beforeBrake);
          }
          braking[0] = true;
        });
    helper.runAtTickTime(
        410,
        () -> {
          braking[0] = false;
          for (var f : fixtures) {
            double speed =
                container
                    .physicsSystem()
                    .getPipeline()
                    .getLinearVelocity(f.body, new Vector3d())
                    .length();
            helper.assertTrue(speed < .1, "FE-enabled STOP failed " + f.label());
            System.out.println(
                "FD_ENGINEERED_FE stopped "
                    + f.label()
                    + " speed="
                    + speed
                    + " recovered="
                    + (f.energy.getEnergyStored() - f.beforeBrake)
                    + " peakRecovered="
                    + (f.peakBrakeEnergy - f.beforeBrake));
            if (!f.pax())
              helper.assertTrue(
                  f.peakBrakeEnergy > f.beforeBrake, "Native STOP did not regenerate FE");
            if (f.vehicle != null && !f.pax()) VehicleOperations.capture(level, f.vehicle);
          }
        });
    helper.runAtTickTime(
        520,
        () -> {
          for (var f : fixtures) {
            if (f.vehicle == null) continue;
            helper.assertTrue(Boarding.ready(level, f.vehicle), "FE ride failed carrier recapture");
            VehicleOperations.restraints(level, f.vehicle, false);
            f.rider.stopRiding();
            helper.assertTrue(!f.rider.isPassenger(), "FE ride could not unload");
            f.rider.discard();
            VehicleOperations.retention(level, f.vehicle, true);
            setMode(f.controller, "LAUNCH", 20);
            f.energy.receiveEnergy(Integer.MAX_VALUE, false);
            f.retainedEnergy = f.energy.getEnergyStored();
            f.startZ = f.body.logicalPose().position().z();
          }
        });
    helper.runAtTickTime(
        620,
        () -> {
          for (var f : fixtures) {
            if (f.vehicle != null) {
              double drift = Math.abs(f.body.logicalPose().position().z() - f.startZ);
              helper.assertTrue(drift < .1, "Supplied FE bypassed maintenance retention");
              helper.assertTrue(
                  f.energy.getEnergyStored() == f.retainedEnergy, "Retained motor consumed FE");
              System.out.println(
                  "FD_ENGINEERED_FE retained "
                      + f.label()
                      + " drift="
                      + drift
                      + " energy="
                      + f.retainedEnergy);
              for (var c : f.vehicle.carriers)
                container.removeSubLevel(
                    container.getSubLevel(c.body), SubLevelRemovalReason.REMOVED);
              FoundationData.get(level).vehicles.remove(f.vehicle.chassis);
            }
            container.removeSubLevel(f.body, SubLevelRemovalReason.REMOVED);
            ((CoasterAnchorpointBlockEntity) level.getBlockEntity(f.rail.from))
                .removeAnchorPeerCurve(level, f.rail.to);
            FoundationData.get(level).rails.remove(f.rail);
            for (var pos : List.of(f.rail.from, f.rail.to, f.controllerPos))
              level.removeBlock(pos, false);
          }
          System.out.println(
              "FD_ENGINEERED_FE installed=1.1.1 nativeCapability=true unloaded=true");
          helper.succeed();
        });
  }

  /**
   * Exercises the installed FE capability against the ordinary bearing in a production recovery
   * group. The production brake must hold both motion and consumption until an explicit start.
   */
  @GameTest(template = "empty", batch = BATCH, timeoutTicks = 520)
  public static void nativeEnergyRespectsRecoveryBrakeAndExplicitTow(GameTestHelper helper) {
    if (!OptionalIntegrations.installed(OptionalIntegrations.ENGINEERED).mayInspect()) {
      System.out.println("FD_ENGINEERED_RECOVERY_FE absent/uninspected: scenario not exercised");
      helper.succeed();
      return;
    }
    helper.assertTrue((boolean) invoke(type("TrackEnergy"), "required"), "FE requirement disabled");
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    var origin = new BlockPos(-850, 120, -700);
    var forcedChunks = new ArrayList<ChunkPos>();
    for (int x = -55; x <= -52; x++)
      for (int z = -45; z <= -40; z++) {
        var chunk = new ChunkPos(x, z);
        if (!level.getForcedChunks().contains(chunk.toLong())) {
          level.setChunkForced(x, z, true);
          forcedChunks.add(chunk);
        }
      }
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000417"),
                "FDRecoveryEngineeredEnergy"));
    player.setGameMode(GameType.CREATIVE);
    var recovery = RecoveryExamples.create(level, origin, TrackDesign.ARROW);
    var rail =
        FoundationData.get(level).rails.stream()
            .filter(candidate -> candidate.matches(recovery.car().boundFrom, recovery.car().boundTo))
            .findFirst()
            .orElseThrow();
    var controllerPos = origin.west(6);
    level.setBlockAndUpdate(
        controllerPos,
        BuiltInRegistries.BLOCK
            .get(ResourceLocation.parse("coastersengineered:linear_motor_controller"))
            .defaultBlockState());
    IEnergyStorage[] energy = {null};
    Pig[] rider = {null};
    var nativeIds = new ArrayList<UUID>();
    for (var carrier : recovery.car().carriers) nativeIds.add(carrier.body);
    nativeIds.add(recovery.car().chassis);
    nativeIds.add(recovery.bearing().getUniqueId());
    recoveryCleanup =
        () -> {
          recovery.car().stopping = true;
          if (rider[0] != null) rider[0].discard();
          for (var id : nativeIds) {
            var body = container.getSubLevel(id);
            if (body != null && !body.isRemoved())
              container.removeSubLevel(body, SubLevelRemovalReason.REMOVED);
          }
          var data = FoundationData.get(level);
          data.vehicles.remove(recovery.car().chassis);
          var anchor = level.getBlockEntity(rail.from);
          if (anchor instanceof CoasterAnchorpointBlockEntity coasterAnchor)
            coasterAnchor.removeAnchorPeerCurve(level, rail.to);
          data.rails.remove(rail);
          for (var pos : BlockPos.betweenClosed(origin.offset(-8, -1, -4), origin.offset(8, 12, 52)))
            if (!level.getBlockState(pos).isAir()) level.removeBlock(pos, false);
          for (var chunk : forcedChunks) level.setChunkForced(chunk.x, chunk.z, false);
          data.setDirty();
          System.out.println(
              "FD_ENGINEERED_RECOVERY_FE cleanup vehiclePresent="
                  + data.vehicles.containsKey(recovery.car().chassis)
                  + " remainingVehicles="
                  + data.vehicles.size());
        };
    Vector3d[] brakedStart = {null};
    Vector3d[] towingStart = {null};
    int[] suppliedEnergy = {0};
    int[] brakeEnergy = {0};
    int[] lowestBrakeEnergy = {Integer.MAX_VALUE};
    double[] brakedDistance = {0};
    double[] starvedTowDistance = {0};
    boolean[] braking = {false};
    helper.onEachTick(
        () -> {
          if (braking[0])
            lowestBrakeEnergy[0] = Math.min(lowestBrakeEnergy[0], energy[0].getEnergyStored());
        });
    helper.runAtTickTime(
        100,
        () -> {
          var car = recovery.car();
          helper.assertTrue(Boarding.ready(level, car), "Recovery FE fixture failed initial alignment");
          var chassis = (ServerSubLevel) container.getSubLevel(car.chassis);
          var bearing = recovery.bearing();
          helper.assertTrue(
              Math.abs(CoasterCartResolve.cartCenterWorld(chassis).distance(CoasterCartResolve.cartCenterWorld(bearing))
                      - TrackDesign.ARROW.axleSpacing())
                  < .25,
              "Recovery FE bearing was not at certified Arrow spacing");
          var carrier = car.carriers.getFirst();
          rider[0] = EntityType.PIG.create(level);
          rider[0].setNoAi(true);
          var center = CoasterCartResolve.cartCenterWorld(chassis);
          rider[0].setPos(center.x, center.y, center.z);
          level.addFreshEntity(rider[0]);
          SeatBlock.sitDown(
              level,
              container.getSubLevel(carrier.body).getPlot().getCenterBlock().offset(carrier.seats.getFirst()),
              rider[0]);
          helper.assertTrue(rider[0].isPassenger(), "Recovery FE fixture failed native rider mount");
          VehicleOperations.restraints(level, car, true);
          VehicleOperations.retention(level, car, true);
          player.setPos(center.x, center.y, center.z);
          RecoveryOperations.select(player, bearing);
          RecoveryOperations.attach(player, car);
          helper.assertTrue(
              car.stopping
                  && car.recoveryCart.equals(bearing.getUniqueId())
                  && CoasterCartTrainLinkConstraint.areLinkedCarts(chassis, bearing),
              "Recovery FE fixture did not form the braked native group");
        });
    helper.runAtTickTime(
        130,
        () -> {
          // LAUNCH has a minimum target of 1 block/s, above recovery's 0.75-block/s cap.
          // Keep the healthy-towing assertions: a fault is a failed scenario, not a pass.
          configureMotor(helper, rail, controllerPos, 1, "FORWARD");
          energy[0] = level.getCapability(Capabilities.EnergyStorage.BLOCK, rail.from, Direction.UP);
          helper.assertTrue(energy[0] != null, "Recovery motor anchor exposes no native FE capability");
          helper.assertTrue(energy[0].getEnergyStored() == 0, "Recovery FE buffer was not empty");
          brakedStart[0] = new Vector3d(CoasterCartResolve.cartCenterWorld(recovery.bearing()));
        });
    helper.runAtTickTime(
        160,
        () -> {
          brakedDistance[0] =
              CoasterCartResolve.cartCenterWorld(recovery.bearing()).distance(brakedStart[0]);
          helper.assertTrue(
              brakedDistance[0] < .01,
              "Empty FE recovery bearing moved while braked: " + brakedDistance[0]);
          RecoveryOperations.start(level, recovery.car());
          towingStart[0] = new Vector3d(CoasterCartResolve.cartCenterWorld(recovery.bearing()));
        });
    helper.runAtTickTime(
        200,
        () -> {
          starvedTowDistance[0] =
              CoasterCartResolve.cartCenterWorld(recovery.bearing()).distance(towingStart[0]);
          helper.assertTrue(
              starvedTowDistance[0] < .01,
              "Empty FE buffer bypassed recovery starvation after Start tow: " + starvedTowDistance[0]);
          helper.assertTrue(
              energy[0].getEnergyStored() == 0,
              "Starved recovery motor created FE after Start tow");
          RecoveryOperations.brake(level, recovery.car());
          int received = energy[0].receiveEnergy(Integer.MAX_VALUE, false);
          helper.assertTrue(
              received == energy[0].getMaxEnergyStored() && received > 0,
              "Recovery motor FE buffer did not fill through its native capability");
          suppliedEnergy[0] = energy[0].getEnergyStored();
          brakedStart[0] = new Vector3d(CoasterCartResolve.cartCenterWorld(recovery.bearing()));
        });
    helper.runAtTickTime(
        240,
        () -> {
          double suppliedBrakedDistance =
              CoasterCartResolve.cartCenterWorld(recovery.bearing()).distance(brakedStart[0]);
          helper.assertTrue(
              suppliedBrakedDistance < .01,
              "Supplied FE bypassed the production recovery brake: " + suppliedBrakedDistance);
          helper.assertTrue(
              energy[0].getEnergyStored() == suppliedEnergy[0],
              "Braked recovery motor consumed supplied FE before Start tow");
          RecoveryOperations.start(level, recovery.car());
          towingStart[0] = new Vector3d(CoasterCartResolve.cartCenterWorld(recovery.bearing()));
        });
    helper.runAtTickTime(
        400,
        () -> {
          double towingDistance =
              CoasterCartResolve.cartCenterWorld(recovery.bearing()).distance(towingStart[0]);
          helper.assertTrue(towingDistance > .05, "Supplied FE did not tow the recovery group: " + towingDistance);
          helper.assertTrue(
              energy[0].getEnergyStored() < suppliedEnergy[0],
              "Explicit FE recovery tow did not consume native anchor energy");
          helper.assertTrue(rider[0].isPassenger(), "FE recovery tow lost the native rider mount");
          helper.assertTrue(
              !recovery.car().stopping && recovery.car().fault.isEmpty(),
              "Recovery FE tow faulted instead of remaining actively healthy: " + recovery.car().fault);
          double speed =
              container
                  .physicsSystem()
                  .getPipeline()
                  .getLinearVelocity(recovery.bearing(), new Vector3d())
                  .length();
          helper.assertTrue(
              speed <= RecoveryOperations.MAX_SPEED + .05,
              "Recovery FE tow exceeded the guarded speed cap: " + speed);
          RecoveryOperations.brake(level, recovery.car());
          brakeEnergy[0] = energy[0].getEnergyStored();
          lowestBrakeEnergy[0] = brakeEnergy[0];
          braking[0] = true;
          System.out.println(
              "FD_ENGINEERED_RECOVERY_FE towingDistance="
                  + towingDistance
                  + " brakedDistance="
                  + brakedDistance[0]
                  + " starvedTowDistance="
                  + starvedTowDistance[0]
                  + " suppliedEnergy="
                  + suppliedEnergy[0]
                  + " activeEnergy="
                  + brakeEnergy[0]
                  + " riderMounted=true");
        });
    helper.runAtTickTime(
        480,
        () -> {
          braking[0] = false;
          helper.assertTrue(recovery.car().stopping, "Recovery FE brake did not retain the stop state");
          helper.assertTrue(
              lowestBrakeEnergy[0] >= brakeEnergy[0],
              "Braked recovery motor depleted FE after Brake tow");
          helper.assertTrue(rider[0].isPassenger(), "FE recovery brake lost the native rider mount");
          System.out.println(
              "FD_ENGINEERED_RECOVERY_FE brakedEnergy="
                  + energy[0].getEnergyStored()
                  + " minimumBrakedEnergy="
                  + lowestBrakeEnergy[0]
                  + " noBrakeDepletion=true explicitStart=true");
          recoveryCleanup.run();
          recoveryCleanup = null;
        });
    helper.runAtTickTime(
        483,
        () -> {
          helper.assertTrue(
              !FoundationData.get(level).vehicles.containsKey(recovery.car().chassis),
              "Recovery FE fixture leaked its vehicle slot");
          helper.assertTrue(
              !FoundationData.get(level).rails.contains(rail), "Recovery FE fixture leaked its rail");
          for (var id : nativeIds)
            helper.assertTrue(
                container.getSubLevel(id) == null, "Recovery FE fixture leaked a native body");
          helper.assertTrue(rider[0].isRemoved(), "Recovery FE fixture leaked its rider");
          System.out.println("FD_ENGINEERED_RECOVERY_FE cleanupVerified=true");
          helper.succeed();
        });
  }
}
