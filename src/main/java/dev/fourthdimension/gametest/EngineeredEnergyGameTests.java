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
import net.minecraft.world.level.GameType;
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
    if (previous == null) return;
    requirement.set(previous);
    System.out.println("FD_ENGINEERED_FE restored requireEnergy=" + previous);
    previous = null;
    requirement = null;
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
  @GameTest(template = "empty", batch = BATCH, timeoutTicks = 480)
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
    for (int x = -55; x <= -52; x++)
      for (int z = -45; z <= -40; z++) level.setChunkForced(x, z, true);
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
    Vector3d[] brakedStart = {null};
    Vector3d[] towingStart = {null};
    int[] suppliedEnergy = {0};
    int[] brakeEnergy = {0};
    double[] brakedDistance = {0};
    double[] starvedTowDistance = {0};
    helper.runAtTickTime(
        100,
        () -> {
          var car = recovery.car();
          helper.assertTrue(Boarding.ready(level, car), "Recovery FE fixture failed initial alignment");
          VehicleOperations.restraints(level, car, true);
          VehicleOperations.retention(level, car, true);
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
          configureMotor(helper, rail, controllerPos, 10, "FORWARD");
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
        360,
        () -> {
          double towingDistance =
              CoasterCartResolve.cartCenterWorld(recovery.bearing()).distance(towingStart[0]);
          helper.assertTrue(towingDistance > .5, "Supplied FE did not tow the recovery group: " + towingDistance);
          helper.assertTrue(
              energy[0].getEnergyStored() < suppliedEnergy[0],
              "Explicit FE recovery tow did not consume native anchor energy");
          helper.assertTrue(rider[0].isPassenger(), "FE recovery tow lost the native rider mount");
          RecoveryOperations.brake(level, recovery.car());
          brakeEnergy[0] = energy[0].getEnergyStored();
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
        440,
        () -> {
          helper.assertTrue(recovery.car().stopping, "Recovery FE brake did not retain the stop state");
          helper.assertTrue(
              energy[0].getEnergyStored() >= brakeEnergy[0],
              "Braked recovery motor continued consuming FE after Brake tow");
          helper.assertTrue(rider[0].isPassenger(), "FE recovery brake lost the native rider mount");
          System.out.println(
              "FD_ENGINEERED_RECOVERY_FE brakedEnergy="
                  + energy[0].getEnergyStored()
                  + " noBrakeConsumption=true explicitStart=true");
          helper.succeed();
        });
  }
}
