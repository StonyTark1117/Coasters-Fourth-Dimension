package dev.fourthdimension.integration.additions;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.integration.create.RideInformation;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.plushivr.coasterfins.block.StationBlockEntity;
import dev.plushivr.coasterfins.logic.FinSpanSegment;
import dev.plushivr.coasterfins.registry.CFBlocks;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/** Native scans of linked, ambiguous, mixed and partial station spans. */
public final class AdditionsConsistCheck {
  private AdditionsConsistCheck() {}

  public static void run(GameTestHelper helper) {
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    var from = new BlockPos(400, 150, -850);
    var to = from.offset(0, 0, 60);
    for (int x = (from.getX() - 16) >> 4; x <= (from.getX() + 16) >> 4; x++)
      for (int z = (from.getZ() - 16) >> 4; z <= (to.getZ() + 16) >> 4; z++)
        level.setChunkForced(x, z, true);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000402"), "FDAdditionsConsist"));
    player.setGameMode(GameType.CREATIVE);
    for (var pos : List.of(from, to))
      level.setBlockAndUpdate(
          pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
    player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
    var rail =
        TrackPlacement.connect(
            level, from, to, TrackDesign.ARROW, player, player.getMainHandItem());
    var cars = new ArrayList<FoundationData.Vehicle>();
    var riders = new ArrayList<net.minecraft.world.entity.animal.Pig>();
    var station = new StationBlockEntity[1];
    var extra = new ServerSubLevel[1];
    var finPos = from.offset(-10, 0, 24);
    var starts = new org.joml.Vector3d[2];
    var full = List.of(new FinSpanSegment(from, to, 0, 1, true));

    helper.runAtTickTime(
        15,
        () -> {
          for (double distance : new double[] {18, 12}) cars.add(create(level, rail, distance));
        });
    helper.runAtTickTime(
        80,
        () -> {
          helper.assertTrue(
              cars.stream().allMatch(v -> Boarding.ready(level, v)),
              "Linked station cars not aligned");
          helper.assertTrue(
              CoasterCartTrainLinkConstraint.linkCarts(
                  level,
                  (ServerSubLevel) container.getSubLevel(cars.get(0).chassis),
                  (ServerSubLevel) container.getSubLevel(cars.get(1).chassis),
                  6),
              "Native station train coupling failed");
          var pos = from.offset(-8, 0, 24);
          level.setBlockAndUpdate(pos, CFBlocks.STATION.get().defaultBlockState());
          station[0] = (StationBlockEntity) level.getBlockEntity(pos);
          station[0].setSpan(full);
          for (int channel = 0; channel < 4; channel++) {
            station[0].setFrequency(
                channel, 0, net.minecraft.world.item.Items.AMETHYST_SHARD.getDefaultInstance());
            station[0].setFrequency(
                channel,
                1,
                List.of(
                        net.minecraft.world.item.Items.DIAMOND,
                        net.minecraft.world.item.Items.EMERALD,
                        net.minecraft.world.item.Items.REDSTONE,
                        net.minecraft.world.item.Items.GOLD_INGOT)
                    .get(channel)
                    .getDefaultInstance());
          }
          station[0].setDispatch(false, 6, 1, 0);
          level.setBlockAndUpdate(
              finPos,
              net.minecraft.core.registries.BuiltInRegistries.BLOCK
                  .get(net.minecraft.resources.ResourceLocation.parse("coasterfins:coaster_fin"))
                  .defaultBlockState());
          finMode(level, finPos, "boost", full);
        });
    helper.runAtTickTime(
        105,
        () -> {
          ((com.simibubi.create.foundation.blockEntity.SmartBlockEntity)
                  level.getBlockEntity(finPos))
              .getBehaviour(
                  com.simibubi
                      .create
                      .foundation
                      .blockEntity
                      .behaviour
                      .scrollValue
                      .ScrollValueBehaviour
                      .TYPE)
              .setValue(10);
          var t = ((RideInformation.Source) station[0]).telemetry();
          helper.assertTrue(
              t.available() && t.cars() == 2 && t.capacity() == 8 && t.occupied() == 0,
              "Native scanner did not identify whole linked train: " + t);
          for (var v : cars) {
            var pig = net.minecraft.world.entity.EntityType.PIG.create(level);
            pig.setNoAi(true);
            pig.setPos(from.getX(), 151, from.getZ());
            level.addFreshEntity(pig);
            var c = v.carriers.getLast();
            SeatBlock.sitDown(
                level,
                container.getSubLevel(c.body).getPlot().getCenterBlock().offset(c.seats.getLast()),
                pig);
            helper.assertTrue(pig.isPassenger(), "Linked station rider did not board");
            riders.add(pig);
          }
          VehicleOperations.restraints(level, cars.getFirst(), true);
          station[0].setDispatch(true, 6, 1, 0);
        });
    helper.runAtTickTime(
        145,
        () -> {
          var t = ((RideInformation.Source) station[0]).telemetry();
          helper.assertTrue(
              t.cars() == 2
                  && t.occupied() == 2
                  && t.capacity() == 8
                  && station[0].channel(1).power() == 5,
              "Linked carrier occupancy/weight was not aggregated: " + t);
          held(helper, cars, "Weight threshold dispatched the train");
          // Another complete but uncoupled 4D car shares the same native span.
          var v = create(level, rail, 45);
          extra[0] = (ServerSubLevel) container.getSubLevel(v.chassis);
          station[0].setDispatch(true, 5, 1, 0);
        });
    helper.runAtTickTime(
        195,
        () -> {
          rejectedSpan(helper, station[0], cars, "uncoupled 4D car");
          container.removeSubLevel(extra[0], SubLevelRemovalReason.REMOVED);
          var edge = TrackGeometry.resolve(level, rail);
          var hit =
              new CoasterPathTrackFrame.GraphHit(
                  CoasterPathGraphManager.sampleEdgePosition(level, edge, .75), edge, .75);
          var orientation =
              CoasterPathTrackFrame.quaternionFromTrackBasis(
                  CoasterPathTrackFrame.trackBasisAtHit(level, hit));
          extra[0] =
              CoasterCartSpawner.spawnMinimalContraption(
                  level,
                  CoasterCartLinkPlacement.plotOriginForBearingCenter(
                      CoasterPathTrackFrame.pathAnchorForSnappedBlock(level, hit), orientation),
                  orientation,
                  null,
                  false);
          helper.assertTrue(extra[0] != null, "Ordinary mixed-span cart was not allocated");
          helper.assertTrue(
              CoasterCartRailSnapEligibility.filterHitForBearing(extra[0], level, hit) != null,
              "Ordinary mixed-span fixture is ineligible for the route");
        });
    helper.runAtTickTime(
        245,
        () -> {
          rejectedSpan(helper, station[0], cars, "ordinary cart");
          container.removeSubLevel(extra[0], SubLevelRemovalReason.REMOVED);
          // Rear car is within the short span; the linked front car is outside it.
          station[0].setSpan(List.of(new FinSpanSegment(from, to, 0, .25, true)));
        });
    helper.runAtTickTime(
        285,
        () -> {
          held(helper, cars, "Partial linked train dispatched");
          helper.assertTrue(
              !((RideInformation.Source) station[0]).telemetry().available(),
              "Partial train retained complete-consist telemetry");
          station[0].setDispatch(true, 5, 3, 5);
          station[0].setSpan(full);
        });
    helper.runAtTickTime(370, () -> held(helper, cars, "Arrival cooldown was ignored"));
    helper.runAtTickTime(
        405,
        () -> {
          held(helper, cars, "Ready delay was ignored after cooldown");
          VehicleOperations.retention(level, cars.getFirst(), true);
        });
    helper.runAtTickTime(
        430,
        () -> {
          held(helper, cars, "Maintenance retention was bypassed");
          VehicleOperations.retention(level, cars.getFirst(), false);
        });
    helper.runAtTickTime(
        475, () -> held(helper, cars, "Readiness timer accumulated across retention"));
    helper.runAtTickTime(
        515,
        () -> {
          helper.assertTrue(
              cars.stream().noneMatch(v -> v.stopping), "Validated linked auto-dispatch failed");
          helper.assertTrue(
              station[0].channel(0).power() == 0 && station[0].channel(2).power() == 15,
              "Successful linked dispatch did not release occupied while retaining bars");
          helper.assertTrue(
              riders.stream().allMatch(net.minecraft.world.entity.Entity::isPassenger),
              "Linked dispatch lost rider");
        });
    helper.runAtTickTime(
        550,
        () -> {
          helper.assertTrue(
              cars.stream().noneMatch(v -> v.stopping),
              "Station recaptured dispatched train before span clear");
          System.out.println(
              "FD_ADDITIONS_CONSIST linked=true ambiguousRejected=true mixedRejected=true"
                  + " partialRejected=true threshold=5 cooldown=true continuousDelay=true"
                  + " departureLatch=true telemetry="
                  + ((RideInformation.Source) station[0]).telemetry());
          for (int i = 0; i < cars.size(); i++)
            starts[i] =
                new org.joml.Vector3d(
                    CoasterCartResolve.cartCenterWorld(container.getSubLevel(cars.get(i).chassis)));
          level.setBlockAndUpdate(
              finPos.below(),
              net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK.defaultBlockState());
        });
    helper.runAtTickTime(
        570,
        () -> {
          for (int i = 0; i < cars.size(); i++) {
            double speed = velocity(level, cars.get(i)).z;
            double travel =
                CoasterCartResolve.cartCenterWorld(container.getSubLevel(cars.get(i).chassis))
                    .distance(starts[i]);
            helper.assertTrue(
                speed > 1 && travel > .3,
                "Linked BOOST did not propel both chassis: " + speed + " / " + travel);
            System.out.println(
                "FD_ADDITIONS_LINKED_FIN mode=BOOST car="
                    + i
                    + " speed="
                    + speed
                    + " travel="
                    + travel);
          }
          finMode(level, finPos, "brake", full);
        });
    helper.runAtTickTime(
        610,
        () -> {
          for (int i = 0; i < cars.size(); i++) {
            double speed = velocity(level, cars.get(i)).z;
            helper.assertTrue(
                Math.abs(speed - 3) < .3, "Linked BRAKE did not regulate 3 blocks/s: " + speed);
            System.out.println("FD_ADDITIONS_LINKED_FIN mode=BRAKE car=" + i + " speed=" + speed);
          }
          finMode(level, finPos, "brake", List.of(new FinSpanSegment(from, to, 0, 1, false)));
        });
    helper.runAtTickTime(
        660,
        () -> {
          for (int i = 0; i < cars.size(); i++) {
            double speed = velocity(level, cars.get(i)).z;
            helper.assertTrue(
                Math.abs(speed + 3) < .3,
                "Linked reversed BRAKE did not regulate -3 blocks/s: " + speed);
            System.out.println("FD_ADDITIONS_LINKED_FIN mode=REVERSE car=" + i + " speed=" + speed);
          }
          finMode(level, finPos, "stop", full);
        });
    helper.runAtTickTime(
        720,
        () -> {
          helper.assertTrue(
              cars.stream().allMatch(v -> !v.stopping && v.restraints && v.fault.isBlank()),
              "STOP result came from a 4D interlock or fault instead of the native fin");
          for (int i = 0; i < cars.size(); i++) {
            double speed = velocity(level, cars.get(i)).length();
            helper.assertTrue(speed < .15, "Linked STOP did not hold both chassis: " + speed);
            System.out.println("FD_ADDITIONS_LINKED_FIN mode=STOP car=" + i + " speed=" + speed);
          }
          helper.assertTrue(
              CoasterCartTrainLinkConstraint.areLinkedCarts(
                  container.getSubLevel(cars.get(0).chassis),
                  container.getSubLevel(cars.get(1).chassis)),
              "Fin modes broke native train coupling");
          helper.assertTrue(
              riders.stream().allMatch(net.minecraft.world.entity.Entity::isPassenger),
              "Fin modes lost linked rider");
          level.removeBlock(finPos.below(), false);
          station[0].clearSpan();
          VehicleOperations.capture(level, cars.getFirst());
        });
    helper.runAtTickTime(
        790,
        () -> {
          VehicleOperations.restraints(level, cars.getFirst(), false);
          for (var rider : riders) {
            rider.stopRiding();
            rider.discard();
          }
          for (var v : cars)
            container.removeSubLevel(
                container.getSubLevel(v.chassis), SubLevelRemovalReason.REMOVED);
          helper.succeed();
        });
  }

  private static org.joml.Vector3d velocity(
      net.minecraft.server.level.ServerLevel level, FoundationData.Vehicle v) {
    var c = SubLevelContainer.getContainer(level);
    return c.physicsSystem()
        .getPipeline()
        .getLinearVelocity((ServerSubLevel) c.getSubLevel(v.chassis), new org.joml.Vector3d());
  }

  private static void finMode(
      net.minecraft.server.level.ServerLevel level,
      BlockPos pos,
      String mode,
      List<FinSpanSegment> span) {
    var fin = level.getBlockEntity(pos);
    var tag = fin.saveWithoutMetadata(level.registryAccess());
    tag.putString("SpanMode", mode);
    var segments = new net.minecraft.nbt.ListTag();
    span.forEach(s -> segments.add(s.write()));
    tag.put("SpanSegments", segments);
    fin.loadWithComponents(tag, level.registryAccess());
  }

  private static FoundationData.Vehicle create(
      net.minecraft.server.level.ServerLevel level, FoundationData.Rail rail, double distance) {
    var edge = TrackGeometry.resolve(level, rail);
    double t = TrackGeometry.table(level, edge).parameterAt(distance);
    return VehicleFactory.create(
        level,
        TrackDesign.ARROW,
        new CoasterPathTrackFrame.GraphHit(
            CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t));
  }

  private static void held(
      GameTestHelper helper, List<FoundationData.Vehicle> cars, String reason) {
    helper.assertTrue(
        cars.stream().allMatch(v -> v.stopping && v.restraints && v.fault.isBlank()), reason);
  }

  private static void rejectedSpan(
      GameTestHelper helper,
      StationBlockEntity station,
      List<FoundationData.Vehicle> cars,
      String kind) {
    held(helper, cars, "Ambiguous span dispatched with " + kind);
    var t = ((RideInformation.Source) station).telemetry();
    helper.assertTrue(
        !t.available() && t.dispatchDenial().contains("exactly one complete"),
        "Mixed-span rejection missing for " + kind + ": " + t);
    helper.assertTrue(station.channel(0).power() == 15, "Ambiguous span released occupied output");
    System.out.println("FD_ADDITIONS_REJECT kind=" + kind + " telemetry=" + t);
  }
}
