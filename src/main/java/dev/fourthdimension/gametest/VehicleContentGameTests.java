package dev.fourthdimension.gametest;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class VehicleContentGameTests {
  @GameTest(template = "empty", timeoutTicks = 140)
  public static void distinctPlayersRetainDistinctCarrierSeats(GameTestHelper helper) {
    var level = helper.getLevel();
    for (int x = -10; x <= -7; x++) for (int z = 10; z <= 14; z++) level.setChunkForced(x, z, true);
    var builder =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000105"), "FDSeatBuilder"));
    builder.setGameMode(GameType.CREATIVE);
    var firstProfile =
        new com.mojang.authlib.GameProfile(
            UUID.fromString("fdfdfdfd-0000-0000-0000-000000000106"), "FDSeatFirst");
    var secondProfile =
        new com.mojang.authlib.GameProfile(
            UUID.fromString("fdfdfdfd-0000-0000-0000-000000000107"), "FDSeatSecond");
    var first =
        new net.minecraft.server.level.ServerPlayer(
            level.getServer(),
            level,
            firstProfile,
            net.minecraft.server.level.ClientInformation.createDefault());
    var second =
        new net.minecraft.server.level.ServerPlayer(
            level.getServer(),
            level,
            secondProfile,
            net.minecraft.server.level.ClientInformation.createDefault());
    first.connection = FakePlayerFactory.get(level, firstProfile).connection;
    second.connection = FakePlayerFactory.get(level, secondProfile).connection;
    first.setGameMode(GameType.CREATIVE);
    second.setGameMode(GameType.CREATIVE);
    var from = new BlockPos(-148, 96, 173);
    var to = from.offset(0, 0, 36);
    for (var point : List.of(from, to))
      level.setBlockAndUpdate(
          point, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    builder.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
    builder.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
    var rail = TrackPlacement.connect(level, from, to, TrackDesign.ARROW, builder, builder.getMainHandItem());
    final FoundationData.Vehicle[] vehicle = new FoundationData.Vehicle[1];
    var container = SubLevelContainer.getContainer(level);
    helper.runAtTickTime(
        10,
        () -> {
          var edge =
              CoasterPathGraphManager.get(level).edges().stream()
                  .filter(candidate -> rail.matches(candidate.from(), candidate.to()))
                  .findFirst()
                  .orElseThrow();
          var point = CoasterPathGraphManager.sampleEdgePosition(level, edge, .4);
          vehicle[0] =
              VehicleFactory.create(
                  level, TrackDesign.ARROW, new CoasterPathTrackFrame.GraphHit(point, edge, .4));
        });
    helper.runAtTickTime(
        80,
        () -> {
          var car = vehicle[0];
          helper.assertTrue(Boarding.ready(level, car), "Carrier did not settle before boarding");
          var carrier = car.carriers.getFirst();
          var body = (ServerSubLevel) container.getSubLevel(carrier.body);
          var firstSeat = body.getPlot().getCenterBlock().offset(carrier.seats.get(0));
          var secondSeat = body.getPlot().getCenterBlock().offset(carrier.seats.get(1));
          first.setPos(firstSeat.getX(), firstSeat.getY(), firstSeat.getZ());
          second.setPos(secondSeat.getX(), secondSeat.getY(), secondSeat.getZ());
          SeatBlock.sitDown(level, firstSeat, first);
          SeatBlock.sitDown(level, secondSeat, second);
          helper.assertTrue(first.isPassenger() && second.isPassenger(), "Distinct players failed to mount");
          helper.assertTrue(
              first.getVehicle() != second.getVehicle(), "Distinct players shared one native seat entity");
          helper.assertTrue(
              carrier.riderSeats.size() == 2
                  && carrier.riderSeats.get(first.getUUID()).equals(carrier.seats.get(0))
                  && carrier.riderSeats.get(second.getUUID()).equals(carrier.seats.get(1)),
              "Distinct player-to-seat assignments were not retained");
        });
    helper.runAtTickTime(
        100,
        () -> {
          var carrier = vehicle[0].carriers.getFirst();
          helper.assertTrue(
              first.isPassenger()
                  && second.isPassenger()
                  && carrier.riderSeats.keySet().containsAll(List.of(first.getUUID(), second.getUUID())),
              "Separate player assignments did not survive a native update");
          first.stopRiding();
          second.stopRiding();
          for (var carrierToRemove : vehicle[0].carriers)
            container.removeSubLevel(
                container.getSubLevel(carrierToRemove.body), SubLevelRemovalReason.REMOVED);
          container.removeSubLevel(
              container.getSubLevel(vehicle[0].chassis), SubLevelRemovalReason.REMOVED);
          FoundationData.get(level).vehicles.remove(vehicle[0].chassis);
          ((CoasterAnchorpointBlockEntity) level.getBlockEntity(rail.from))
              .removeAnchorPeerCurve(level, rail.to);
          level.removeBlock(rail.from, false);
          level.removeBlock(rail.to, false);
          FoundationData.get(level).rails.remove(rail);
          helper.succeed();
        });
  }

  @GameTest(template = "empty", timeoutTicks = 260)
  public static void originalVehiclesLoadingAndCapture(GameTestHelper helper) {
    var level = helper.getLevel();
    for (int x = -8; x <= -5; x++) for (int z = 12; z <= 16; z++) level.setChunkForced(x, z, true);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000104"), "FDLoadTest"));
    player.setGameMode(GameType.CREATIVE);
    var rails = new ArrayList<FoundationData.Rail>();
    for (var design : TrackDesign.values()) {
      var from = new BlockPos(-116 + design.ordinal() * 18, 96, 205);
      var to = from.offset(0, 0, 36);
      for (var p : List.of(from, to))
        level.setBlockAndUpdate(
            p, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
      var item = design == TrackDesign.ARROW ? TrackItems.ARROW.get() : TrackItems.LEGACY.get();
      player.setItemInHand(InteractionHand.MAIN_HAND, item.getDefaultInstance());
      rails.add(TrackPlacement.connect(level, from, to, design, player, player.getMainHandItem()));
    }
    var vehicles = new ArrayList<FoundationData.Vehicle>();
    var masses = new double[2];
    var excursion = new double[2];
    var released = new java.util.concurrent.atomic.AtomicBoolean(false);
    helper.onEachTick(
        () -> {
          if (released.get()) {
            var v = vehicles.get(1);
            excursion[0] =
                Math.max(excursion[0], Math.abs(v.carriers.getFirst().angle - masses[1]));
            excursion[1] =
                Math.max(
                    excursion[1],
                    Math.abs(v.carriers.getFirst().angle - v.carriers.getLast().angle));
          }
        });
    var rider = net.minecraft.world.entity.EntityType.PIG.create(level);
    rider.setNoAi(true);
    rider.setPos(-98, 98, 220);
    level.addFreshEntity(rider);
    var container = SubLevelContainer.getContainer(level);
    helper.runAtTickTime(
        10,
        () -> {
          for (int i = 0; i < 2; i++) {
            var rail = rails.get(i);
            var edge =
                CoasterPathGraphManager.get(level).edges().stream()
                    .filter(e -> rail.matches(e.from(), e.to()))
                    .findFirst()
                    .orElseThrow();
            var point = CoasterPathGraphManager.sampleEdgePosition(level, edge, .4);
            var v =
                VehicleFactory.create(
                    level,
                    TrackDesign.values()[i],
                    new CoasterPathTrackFrame.GraphHit(point, edge, .4));
            vehicles.add(v);
            helper.assertTrue(
                v.carriers.stream().mapToInt(c -> c.seats.size()).sum() == (i == 0 ? 4 : 8),
                "Vehicle seat topology is wrong");
          }
        });
    helper.runAtTickTime(
        80,
        () -> {
          for (var v : vehicles)
            helper.assertTrue(Boarding.ready(level, v), "Initial carrier alignment did not settle");
          var v = vehicles.get(1);
          var c = v.carriers.getFirst();
          var body = (ServerSubLevel) container.getSubLevel(c.body);
          masses[0] = body.getMassTracker().getMass();
          SeatBlock.sitDown(
              level, body.getPlot().getCenterBlock().offset(c.seats.getFirst()), rider);
          helper.assertTrue(rider.isPassenger(), "Real seat entity did not mount the passenger");
        });
    helper.runAtTickTime(
        90,
        () -> {
          var v = vehicles.get(1);
          var c = v.carriers.getFirst();
          var body = (ServerSubLevel) container.getSubLevel(c.body);
          helper.assertTrue(
              Math.abs(body.getMassTracker().getMass() - masses[0] - NativeLoading.MASS_PER_RIDER)
                  < 1e-5,
              "Native passenger mass missing or counted twice");
          helper.assertTrue(
              c.occupants.contains(rider.getUUID()),
              "Carrier occupancy is not synchronized with passenger entities");
          v.restraints = true;
          rider.stopRiding();
          helper.assertTrue(rider.isPassenger(), "Moving restraint can be bypassed with dismount");
          v.restraints = false;
          rider.stopRiding();
          v.carriers.forEach(group -> group.capture = false);
          container
              .physicsSystem()
              .getPipeline()
              .addLinearAndAngularVelocity(body, new Vector3d(), new Vector3d(1.5, 0, 0));
          masses[1] = c.angle;
          released.set(true);
        });
    helper.runAtTickTime(
        140,
        () -> {
          var v = vehicles.get(1);
          var c = v.carriers.getFirst();
          var body = (ServerSubLevel) container.getSubLevel(c.body);
          helper.assertTrue(
              Math.abs(body.getMassTracker().getMass() - masses[0]) < 1e-5,
              "Passenger mass was not removed exactly once");
          System.out.println(
              "FD_CONTENT_FREE excursions="
                  + java.util.Arrays.toString(excursion)
                  + " angle="
                  + c.angle
                  + " omega="
                  + c.omega
                  + " mass="
                  + body.getMassTracker().getMass());
          helper.assertTrue(
              excursion[0] > .15, "Free carrier did not respond to native angular impulse");
          helper.assertTrue(excursion[1] > .1, "Independent groups lost separate states");
          v.carriers.forEach(group -> group.capture = true);
        });
    helper.runAtTickTime(
        240,
        () -> {
          helper.assertTrue(
              Boarding.ready(level, vehicles.get(1)),
              "Free carrier did not settle into boarding capture");
          rider.discard();
          for (var v : vehicles) {
            for (var c : v.carriers)
              container.removeSubLevel(
                  container.getSubLevel(c.body), SubLevelRemovalReason.REMOVED);
            container.removeSubLevel(
                container.getSubLevel(v.chassis), SubLevelRemovalReason.REMOVED);
            FoundationData.get(level).vehicles.remove(v.chassis);
          }
          for (var r : rails) {
            ((CoasterAnchorpointBlockEntity) level.getBlockEntity(r.from))
                .removeAnchorPeerCurve(level, r.to);
            level.removeBlock(r.from, false);
            level.removeBlock(r.to, false);
            FoundationData.get(level).rails.remove(r);
          }
          helper.succeed();
        });
  }
}
