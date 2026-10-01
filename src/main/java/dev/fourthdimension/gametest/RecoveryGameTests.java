package dev.fourthdimension.gametest;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

/** Operator path, actual native chain propulsion, actual seats and native pair disconnection. */
@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class RecoveryGameTests {
  private static final class Case {
    final TrackDesign design;
    FoundationData.Rail rail;
    FoundationData.Vehicle car;
    ServerSubLevel ordinary;
    CreativeMotorBlockEntity motor;
    BlockPos powerLever;
    Vector3d start;
    double maxAngle, maxSpacing;
    final List<net.minecraft.world.entity.animal.Pig> riders = new ArrayList<>();

    Case(TrackDesign design) {
      this.design = design;
    }
  }

  @GameTest(template = "empty", timeoutTicks = 700)
  public static void guardedRecoveryUsesNativeChainDriveAcrossFiveFamilies(GameTestHelper helper) {
    var level = helper.getLevel();
    var data = FoundationData.get(level);
    var container = SubLevelContainer.getContainer(level);
    for (int x = -57; x <= -49; x++)
      for (int z = 24; z <= 29; z++) level.setChunkForced(x, z, true);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000402"), "FDRecovery"));
    player.setGameMode(GameType.CREATIVE);
    var cases = new ArrayList<Case>();
    for (var design : TrackDesign.values()) {
      var c = new Case(design);
      cases.add(c);
      var from = new BlockPos(-900 + 24 * design.ordinal(), 160, 400);
      var example = RecoveryExamples.create(level, from, design);
      c.car = example.car();
      c.ordinary = example.bearing();
      c.motor = example.motor();
      c.powerLever = example.powerLever();
      c.rail =
          data.rails.stream()
              .filter(r -> r.matches(c.car.boundFrom, c.car.boundTo))
              .findFirst()
              .orElseThrow();
    }
    helper.runAtTickTime(
        80,
        () -> {
          for (var c : cases) {
            helper.assertTrue(
                Boarding.ready(level, c.car), "Recovery car failed initial alignment");
            c.motor.generatedSpeed.setValue(
                8
                    * ((CoasterAnchorpointBlockEntity) level.getBlockEntity(c.rail.from))
                        .getChainLiftTravelSign());
            for (var carrier : c.car.carriers)
              for (var offset : carrier.seats) {
                var body = container.getSubLevel(carrier.body);
                var pig = EntityType.PIG.create(level);
                pig.setNoAi(true);
                pig.setPos(c.rail.from.getX(), c.rail.from.getY(), c.rail.from.getZ());
                level.addFreshEntity(pig);
                SeatBlock.sitDown(level, body.getPlot().getCenterBlock().offset(offset), pig);
                helper.assertTrue(pig.isPassenger(), "Recovery native rider failed to mount");
                c.riders.add(pig);
              }
            var ordinaryCenter = CoasterCartResolve.cartCenterWorld(c.ordinary);
            player.setPos(ordinaryCenter.x + 1, ordinaryCenter.y + 1, ordinaryCenter.z);
            player.setItemInHand(
                InteractionHand.MAIN_HAND, TrackItems.ROTATION_TOOL.get().getDefaultInstance());
            player.setShiftKeyDown(true);
            var result =
                player
                    .getMainHandItem()
                    .useOn(
                        new UseOnContext(
                            player,
                            InteractionHand.MAIN_HAND,
                            new BlockHitResult(
                                new Vec3(ordinaryCenter.x, ordinaryCenter.y, ordinaryCenter.z),
                                Direction.UP,
                                c.ordinary.getPlot().getCenterBlock(),
                                false)));
            player.setShiftKeyDown(false);
            helper.assertTrue(result.consumesAction(), "Normal tool recovery selection failed");
            position(player, container.getSubLevel(c.car.chassis));
            reject(helper, () -> edit(player, c.car, 20), "Unretained recovery was admitted");
            helper.assertTrue(c.car.recoveryCart == null, "Rejected recovery left partial state");
            edit(player, c.car, 11);
            edit(player, c.car, 14);
            reject(
                helper,
                () ->
                    EditorNetwork.apply(
                        player, new EditorNetwork.Edit(c.car.chassis, c.car.revision - 1, 20, "")),
                "Stale editor recovery request was admitted");
            edit(player, c.car, 20);
            helper.assertTrue(
                c.ordinary.getUniqueId().equals(c.car.recoveryCart)
                    && CoasterCartTrainLinkConstraint.areLinkedCarts(
                        container.getSubLevel(c.car.chassis), c.ordinary),
                "Guarded native coupling failed " + c.design);
            for (var other : cases)
              if (other.design != c.design) {
                var wrongEdge = TrackGeometry.resolve(level, other.rail);
                var wrongHit =
                    new CoasterPathTrackFrame.GraphHit(
                        CoasterPathGraphManager.sampleEdgePosition(level, wrongEdge, .5),
                        wrongEdge,
                        .5);
                var originalBody = (ServerSubLevel) container.getSubLevel(c.car.chassis);
                var beforeCar = new Vector3d(CoasterCartResolve.cartCenterWorld(originalBody));
                var beforeOrdinary = new Vector3d(CoasterCartResolve.cartCenterWorld(c.ordinary));
                CoasterCartTrackSnap.applyInitialSnapAfterSpawn(originalBody, level, wrongHit);
                CoasterCartTrackSnap.applyInitialSnapAfterSpawn(c.ordinary, level, wrongHit);
                helper.assertTrue(
                    beforeCar.distance(CoasterCartResolve.cartCenterWorld(originalBody)) < .0001
                        && beforeOrdinary.distance(CoasterCartResolve.cartCenterWorld(c.ordinary))
                            < .0001,
                    "Recovery re-snapped onto wrong family " + c.design + " -> " + other.design);
              }
            reject(helper, () -> edit(player, c.car, 13), "Recovery dispatched a ride");
            reject(helper, () -> edit(player, c.car, 15), "Recovery released carrier retention");
            reject(helper, () -> edit(player, c.car, 12), "Recovery opened occupied restraints");
          }
        });
    helper.runAtTickTime(
        140,
        () -> {
          for (var c : cases) {
            position(player, container.getSubLevel(c.car.chassis));
            helper.assertTrue(
                Boarding.ready(level, c.car),
                "Coupled recovery did not settle " + c.design + ": " + c.car.fault);
            c.start =
                new Vector3d(
                    CoasterCartResolve.cartCenterWorld(container.getSubLevel(c.car.chassis)));
            helper.assertTrue(
                ((CoasterAnchorpointBlockEntity) level.getBlockEntity(c.rail.from)).getSpeed() == 0
                    && Math.abs(c.motor.generatedSpeed.getValue()) == 8
                    && level
                        .getBlockState(c.powerLever)
                        .getValue(
                            net.minecraft.world.level.block.state.properties.BlockStateProperties
                                .POWERED),
                "Initial recovery clutch transmitted power with its stop lever on " + c.design);
            edit(player, c.car, 21);
            helper.assertTrue(
                level
                    .getBlockState(c.powerLever)
                    .useWithoutItem(
                        level,
                        player,
                        new BlockHitResult(
                            Vec3.atCenterOf(c.powerLever), Direction.UP, c.powerLever, false))
                    .consumesAction(),
                "Normal recovery power lever interaction failed");
          }
        });
    helper.onEachTick(
        () -> {
          for (var c : cases)
            if (c.start != null && !c.ordinary.isRemoved()) {
              var parent = container.getSubLevel(c.car.chassis);
              c.maxSpacing =
                  Math.max(
                      c.maxSpacing,
                      Math.abs(
                          CoasterCartResolve.cartCenterWorld(parent)
                                  .distance(CoasterCartResolve.cartCenterWorld(c.ordinary))
                              - c.design.axleSpacing()));
              c.maxAngle =
                  Math.max(
                      c.maxAngle,
                      c.car.carriers.stream()
                          .mapToDouble(group -> Math.abs(group.angle))
                          .max()
                          .orElse(0));
            }
        });
    helper.runAtTickTime(
        380,
        () -> {
          for (var c : cases) {
            double travel =
                CoasterCartResolve.cartCenterWorld(container.getSubLevel(c.car.chassis))
                    .distance(c.start);
            System.out.println(
                "FD_RECOVERY_NATIVE family="
                    + c.design
                    + " travel="
                    + travel
                    + " riders="
                    + c.riders.size()
                    + " maxSpacing="
                    + c.maxSpacing
                    + " maxAngle="
                    + c.maxAngle
                    + " fault="
                    + c.car.fault);
            helper.assertTrue(
                travel > 3 && c.car.fault.isEmpty() && c.maxSpacing < .15 && c.maxAngle < .04,
                "Native recovery propulsion failed " + c.design + ": " + c.car.fault);
            helper.assertTrue(
                c.riders.stream().allMatch(p -> p.isPassenger())
                    && !data.vehicles.containsKey(c.ordinary.getUniqueId()),
                "Native recovery lost passengers or ordinary classification");
            position(player, container.getSubLevel(c.car.chassis));
            reject(helper, () -> edit(player, c.car, 23), "Moving recovery unlink accepted");
            edit(player, c.car, 22);
            level
                .getBlockState(c.powerLever)
                .useWithoutItem(
                    level,
                    player,
                    new BlockHitResult(
                        Vec3.atCenterOf(c.powerLever), Direction.UP, c.powerLever, false));
          }
        });
    helper.runAtTickTime(
        430,
        () -> {
          var restored =
              FoundationData.load(
                  data.save(new CompoundTag(), level.registryAccess()), level.registryAccess());
          var legacyTag = data.save(new CompoundTag(), level.registryAccess());
          legacyTag.putInt("schema", 5);
          for (var raw : legacyTag.getList("vehicles", net.minecraft.nbt.Tag.TAG_COMPOUND))
            ((CompoundTag) raw).remove("recovery_cart");
          var legacy = FoundationData.load(legacyTag, level.registryAccess());
          helper.assertTrue(
              legacy.vehicles.values().stream().allMatch(v -> v.recoveryCart == null)
                  && legacy.vehicles.keySet().equals(data.vehicles.keySet()),
              "Schema 5 migration inferred recovery or lost identity");
          var invalid = data.save(new CompoundTag(), level.registryAccess());
          var invalidVehicle =
              invalid.getList("vehicles", net.minecraft.nbt.Tag.TAG_COMPOUND).getCompound(0);
          invalidVehicle.putUUID("recovery_cart", invalidVehicle.getUUID("chassis"));
          reject(
              helper,
              () -> FoundationData.load(invalid, level.registryAccess()),
              "4D recovery partner accepted on load");
          for (var c : cases) {
            helper.assertTrue(Boarding.ready(level, c.car), "Recovery braking failed " + c.design);
            var saved = restored.vehicles.get(c.car.chassis);
            helper.assertTrue(
                c.car.recoveryCart.equals(saved.recoveryCart)
                    && saved.stopping
                    && saved.maintenance
                    && saved.restraints,
                "Recovery save lost its identity or retained state");
            helper.assertTrue(
                ((CoasterAnchorpointBlockEntity) level.getBlockEntity(c.rail.from)).getSpeed() == 0
                    && Math.abs(c.motor.generatedSpeed.getValue()) == 8
                    && level
                        .getBlockState(c.powerLever)
                        .getValue(
                            net.minecraft.world.level.block.state.properties.BlockStateProperties
                                .POWERED),
                "Recovery lever failed to cut chain power without removing or zeroing the motor "
                    + c.design);
            var members = new ArrayList<UUID>();
            members.add(c.ordinary.getUniqueId());
            members.add(c.car.chassis);
            c.car.carriers.forEach(carrier -> members.add(carrier.body));
            container
                .getHoldingChunkMap()
                .moveToUnloaded(
                    c.ordinary,
                    new net.minecraft.world.level.ChunkPos(
                        BlockPos.containing(
                            c.ordinary.logicalPose().position().x,
                            c.ordinary.logicalPose().position().y,
                            c.ordinary.logicalPose().position().z)));
            helper.assertTrue(
                members.stream()
                    .allMatch(
                        id ->
                            container.getSubLevel(id) == null
                                && container.getHoldingChunkMap().getHoldingSubLevel(id) != null),
                "Recovery native loading dependency missed a component " + c.design);
            helper.assertTrue(
                !c.car.recoveryCartRemoved, "Native holding falsely marked permanent bearing loss");
            reject(
                helper,
                () -> RecoveryOperations.detach(level, c.car),
                "Held bearing allowed orphaned recovery disconnect");
            reject(
                helper,
                () -> RecoveryOperations.start(level, c.car),
                "Unavailable recovery resumed");
          }
          System.out.println(
              "FD_RECOVERY_POWER fiveFamilies=true initialPowerCut=true normalLeverStart=true"
                  + " normalLeverStop=true motorRetainedAt8RPM=true anchorStopped=true");
          RecoveryOperations.tick(level, .05);
          helper.assertTrue(
              cases.stream()
                  .allMatch(
                      c ->
                          c.car.stopping
                              && RecoveryOperations.retained(c.car)
                              && !c.car.fault.isEmpty()),
              "Native holding failed to brake and retain recovery");
          System.out.println(
              "FD_RECOVERY_HOLD fiveFamilies=true allComponentsHeld=true"
                  + " missingResumeRejected=true dormantDisconnectRejected=true"
                  + " permanentRemoval=false");
        });
    helper.runAtTickTime(
        500,
        () -> {
          for (var c : cases) {
            c.ordinary = (ServerSubLevel) container.getSubLevel(c.car.recoveryCart);
            helper.assertTrue(
                c.ordinary != null
                    && container.getSubLevel(c.car.chassis) != null
                    && c.car.carriers.stream()
                        .allMatch(carrier -> container.getSubLevel(carrier.body) != null)
                    && CoasterCartTrainLinkConstraint.areLinkedCarts(
                        container.getSubLevel(c.car.chassis), c.ordinary)
                    && Boarding.ready(level, c.car)
                    && c.car.stopping
                    && RecoveryOperations.retained(c.car)
                    && !c.car.recoveryCartRemoved,
                "Native recovery did not restore braked original components and coupling "
                    + c.design);
            var riders = c.riders.stream().map(p -> level.getEntity(p.getUUID())).toList();
            helper.assertTrue(
                riders.stream()
                    .allMatch(
                        p -> p instanceof net.minecraft.world.entity.animal.Pig && p.isPassenger()),
                "Native recovery restoration lost an occupied seat " + c.design);
            c.riders.clear();
            riders.forEach(p -> c.riders.add((net.minecraft.world.entity.animal.Pig) p));
            position(player, container.getSubLevel(c.car.chassis));
            edit(player, c.car, 21);
            helper.assertTrue(
                !c.car.stopping && c.car.fault.isEmpty(),
                "Explicit operator resume failed after native restoration " + c.design);
            edit(player, c.car, 22);
            if (c.design == TrackDesign.PAX)
              container.removeSubLevel(
                  c.ordinary, dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason.REMOVED);
            else CoasterCartTrainLinkConstraint.clearAllLinks(level, c.ordinary);
          }
          System.out.println(
              "FD_RECOVERY_HOLD fiveFamiliesRestored=true ridersRestored=32"
                  + " nativeCouplingsPreserved=true autoResume=false explicitResume=true");
        });
    helper.runAtTickTime(
        550,
        () -> {
          for (var c : cases) {
            helper.assertTrue(
                c.car.stopping && !c.car.fault.isEmpty() && c.car.recoveryCart != null,
                "Broken coupler failed to retain recovery state");
            position(player, container.getSubLevel(c.car.chassis));
            reject(helper, () -> edit(player, c.car, 21), "Broken recovery resumed");
            var savedAfterLoss =
                FoundationData.load(
                        data.save(new CompoundTag(), level.registryAccess()),
                        level.registryAccess())
                    .vehicles
                    .get(c.car.chassis);
            helper.assertTrue(
                savedAfterLoss.recoveryCartRemoved == c.car.recoveryCartRemoved
                    && savedAfterLoss.recoveryCart.equals(c.car.recoveryCart),
                "Recovery loss record did not persist");
            if (c.design == TrackDesign.PAX)
              helper.assertTrue(
                  c.car.recoveryCartRemoved,
                  "Permanent ordinary removal lost its durable identity");
            edit(player, c.car, 23);
            helper.assertTrue(
                c.car.recoveryCart == null && c.car.stopping && c.car.maintenance,
                "Recovery detach released retention");
            c.motor.generatedSpeed.setValue(0);
            edit(player, c.car, 12);
            c.riders.forEach(p -> p.stopRiding());
            helper.assertTrue(
                c.riders.stream().noneMatch(p -> p.isPassenger()),
                "Recovery failed normal unloading");
          }
          System.out.println(
              "FD_RECOVERY_NATIVE fiveFamilies=true actualChainDrive=true"
                  + " wrongFamilyRecoverySnapsRejected=40 movingUnlinkRejected=true"
                  + " staleEditRejected=true saveRoundTrip=true brokenLinkRetained=true"
                  + " normalDetachAndUnload=true permanentBearingRemoval=true");
          helper.succeed();
        });
  }

  @GameTest(template = "empty", timeoutTicks = 300)
  public static void paxRecoveryHandsOverTheActualStationaryCarriage(GameTestHelper helper) {
    var level = helper.getLevel();
    for (int x = -58; x <= -54; x++)
      for (int z = -30; z <= -12; z++) level.setChunkForced(x, z, true);
    var example =
        dev.fourthdimension.shuttle.ShuttleExamples.create(
            level, new BlockPos(-900, 180, -400), false);
    var winch = example.winch();
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000403"), "FDRecoveryPax"));
    player.setGameMode(GameType.CREATIVE);
    var container = SubLevelContainer.getContainer(level);
    UUID[] carriage = {null};
    helper.runAtTickTime(
        80, () -> dev.fourthdimension.shuttle.ShuttleExamples.assemble(level, example));
    helper.runAtTickTime(
        100,
        () -> {
          var car = example.cars().getFirst();
          player.setPos(
              winch.getBlockPos().getX() + 1,
              winch.getBlockPos().getY() + 1,
              winch.getBlockPos().getZ());
          reject(helper, () -> edit(player, car, 24), "Unretained shuttle detached");
          VehicleOperations.restraints(level, car, true);
          winch.start();
          reject(helper, () -> edit(player, car, 24), "Moving shuttle handover accepted");
        });
    helper.runAtTickTime(
        120,
        () -> {
          helper.assertTrue(
              winch.carriage != null && container.getSubLevel(winch.carriage) != null,
              "Powered native carriage was not created");
          carriage[0] = winch.carriage;
          var car = example.cars().getFirst();
          position(player, container.getSubLevel(car.chassis));
          edit(player, car, 10);
          edit(player, car, 14);
        });
    helper.runAtTickTime(
        210,
        () -> {
          var car = example.cars().getFirst();
          helper.assertTrue(
              example.cars().stream()
                  .allMatch(v -> Boarding.ready(level, v) && RecoveryOperations.retained(v)),
              "Interrupted PAX train did not settle for handover");
          player.setPos(
              winch.getBlockPos().getX() + 1,
              winch.getBlockPos().getY() + 1,
              winch.getBlockPos().getZ());
          edit(player, car, 24);
          helper.assertTrue(
              winch.vehicle == null
                  && winch.carriage == null
                  && winch.cars.isEmpty()
                  && container.getSubLevel(carriage[0]) == null,
              "Handover retained powered catch ownership or its carriage");
          helper.assertTrue(
              example.cars().stream()
                  .allMatch(
                      v ->
                          container.getSubLevel(v.chassis) != null
                              && v.carriers.stream()
                                  .allMatch(c -> container.getSubLevel(c.body) != null)),
              "Handover removed original train components");
          helper.assertTrue(
              CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(
                          container.getSubLevel(car.chassis))
                      .size()
                  == 4,
              "Handover destroyed original PAX couplings");
        });
    helper.runAtTickTime(
        250,
        () -> {
          helper.assertTrue(
              winch.vehicle == null
                  && winch.carriage == null
                  && example.cars().stream().allMatch(v -> Boarding.ready(level, v)),
              "Detached winch rebuilt a catch or moved the retained train");
          System.out.println(
              "FD_RECOVERY_PAX physicalCarriageHandover=true activeHandoverRejected=true"
                  + " originalCarsAndCarriersPreserved=true originalCouplings=4"
                  + " replacementCarriage=false");
          helper.succeed();
        });
  }

  private static void position(ServerPlayer player, dev.ryanhcode.sable.sublevel.SubLevel body) {
    var center = CoasterCartResolve.cartCenterWorld(body);
    player.setPos(center.x + 1, center.y + 1, center.z);
  }

  private static void edit(ServerPlayer player, FoundationData.Vehicle car, int action) {
    EditorNetwork.apply(player, new EditorNetwork.Edit(car.chassis, car.revision, action, ""));
  }

  private static void reject(GameTestHelper helper, Runnable action, String message) {
    boolean rejected = false;
    try {
      action.run();
    } catch (IllegalArgumentException expected) {
      rejected = true;
    }
    helper.assertTrue(rejected, message);
  }
}
