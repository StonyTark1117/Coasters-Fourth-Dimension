package dev.fourthdimension.gametest;

import dev.fourthdimension.runtime.*;
import dev.fourthdimension.station.*;
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
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class OperationsGameTests {
  @GameTest(template = "empty", timeoutTicks = 280)
  public static void stationRetentionSupportLossAndBodyRecovery(GameTestHelper helper) {
    var level = helper.getLevel();
    for (int x = 42; x <= 47; x++) for (int z = 6; z <= 11; z++) level.setChunkForced(x, z, true);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000106"), "FDOperationsTest"));
    player.setGameMode(GameType.CREATIVE);
    var rails = new ArrayList<FoundationData.Rail>();
    for (var design : TrackDesign.values()) {
      var from = new BlockPos(700 + design.ordinal() * 24, 120, 110);
      var to = from.offset(0, 0, 40);
      for (var p : List.of(from, to))
        level.setBlockAndUpdate(
            p, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
      player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
      rails.add(TrackPlacement.connect(level, from, to, design, player, player.getMainHandItem()));
    }
    var vehicles = new ArrayList<FoundationData.Vehicle>();
    var container = SubLevelContainer.getContainer(level);
    var stationPos = rails.getFirst().from.offset(-6, 0, 12);
    var input = stationPos.south();
    var rider = net.minecraft.world.entity.EntityType.PIG.create(level);
    rider.setNoAi(true);
    rider.setPos(700, 120, 120);
    level.addFreshEntity(rider);
    helper.runAtTickTime(
        15,
        () -> {
          for (int i = 0; i < 2; i++) {
            var rail = rails.get(i);
            var edge = TrackGeometry.resolve(level, rail);
            var hit =
                new CoasterPathTrackFrame.GraphHit(
                    CoasterPathGraphManager.sampleEdgePosition(level, edge, .3), edge, .3);
            var obstacle = rail.from.offset(3, 2, 12);
            level.setBlockAndUpdate(obstacle, Blocks.STONE.defaultBlockState());
            int count = FoundationData.get(level).vehicles.size();
            int index = i;
            EditorGameTests.rejects(
                helper,
                () -> VehicleFactory.create(level, TrackDesign.values()[index], hit),
                "Swept obstruction accepted");
            helper.assertTrue(
                count == FoundationData.get(level).vehicles.size(),
                "Failed placement leaked a vehicle");
            level.removeBlock(obstacle, false);
            vehicles.add(VehicleFactory.create(level, TrackDesign.values()[i], hit));
          }
          level.setBlockAndUpdate(stationPos, StationContent.STATION.get().defaultBlockState());
          ((BoardingStationEntity) level.getBlockEntity(stationPos)).vehicle =
              vehicles.getFirst().chassis;
        });
    helper.runAtTickTime(
        80,
        () -> {
          var v = vehicles.getFirst();
          helper.assertTrue(Boarding.ready(level, v), "Vehicle not ready at station");
          var c = v.carriers.getFirst();
          var carrier = container.getSubLevel(c.body);
          var dependencies =
              dev.ryanhcode.sable.api.SubLevelHelper.getLoadingDependencyChain(
                  (ServerSubLevel) carrier);
          helper.assertTrue(
              dependencies.contains(container.getSubLevel(v.chassis))
                  && v.carriers.stream()
                      .allMatch(group -> dependencies.contains(container.getSubLevel(group.body))),
              "Native loading dependencies split a vehicle");
          com.simibubi.create.content.contraptions.actors.seat.SeatBlock.sitDown(
              level, carrier.getPlot().getCenterBlock().offset(c.seats.getFirst()), rider);
          helper.assertTrue(rider.isPassenger(), "Recovery fixture passenger could not board");
          var state = level.getBlockState(stationPos);
          helper.assertTrue(
              state.getValue(BoardingStation.READY)
                  && level.getSignal(stationPos, Direction.SOUTH) == 15,
              "Front ready output missing");
          helper.assertTrue(
              level.getSignal(stationPos, Direction.NORTH) == 0,
              "Ready signal leaked into rear input");
          VehicleOperations.restraints(level, v, true);
          var wrong = TrackGeometry.resolve(level, rails.getLast());
          var hit =
              new CoasterPathTrackFrame.GraphHit(
                  CoasterPathGraphManager.sampleEdgePosition(level, wrong, .3), wrong, .3);
          var chassis = container.getSubLevel(v.chassis);
          helper.assertTrue(
              !EligibilityHooks.allows(chassis, level, hit), "Locked Arrow accepted legacy track");
          helper.assertTrue(
              !CoasterCartTrainLinkConstraint.linkCarts(
                  level,
                  (ServerSubLevel) chassis,
                  (ServerSubLevel) container.getSubLevel(vehicles.getLast().chassis),
                  6),
              "Mixed family coupling accepted");
          var anchor = (CoasterAnchorpointBlockEntity) level.getBlockEntity(rails.getFirst().from);
          float bank = anchor.getJointBankRadians();
          anchor.setJointBankRadians(1);
          helper.assertTrue(
              anchor.getJointBankRadians() == bank && rails.getFirst().valid,
              "Occupied anchor bank changed");
          level.setBlockAndUpdate(input, Blocks.REDSTONE_BLOCK.defaultBlockState());
        });
    helper.runAtTickTime(
        100,
        () -> {
          helper.assertTrue(
              level.getSignal(stationPos, Direction.WEST) == 15, "Right restraint output missing");
          level.removeBlock(input, false);
        });
    helper.runAtTickTime(
        125,
        () -> {
          var v = vehicles.getFirst();
          helper.assertTrue(
              !v.stopping && v.carriers.stream().noneMatch(c -> c.capture),
              "Rear falling edge failed dispatch");
          EditorGameTests.rejects(
              helper,
              () -> VehicleOperations.restraints(level, v, false),
              "Uncaptured restraints released");
          level.setBlockAndUpdate(input, Blocks.REDSTONE_BLOCK.defaultBlockState());
        });
    helper.runAtTickTime(
        180,
        () -> {
          var v = vehicles.getFirst();
          helper.assertTrue(Boarding.ready(level, v), "Station capture did not restore boarding");
          VehicleOperations.retention(level, v, true);
          level.removeBlock(input, false);
        });
    helper.runAtTickTime(
        205,
        () -> {
          var v = vehicles.getFirst();
          helper.assertTrue(v.maintenance && v.stopping, "Redstone bypassed maintenance retention");
          ((CoasterAnchorpointBlockEntity) level.getBlockEntity(rails.getFirst().from))
              .removeAnchorPeerCurve(level, rails.getFirst().to);
        });
    helper.runAtTickTime(
        225,
        () -> {
          var v = vehicles.getFirst();
          helper.assertTrue(
              v.stopping && v.restraints && !v.fault.isBlank(), "Removed support failed closed");
          container.removeSubLevel(
              container.getSubLevel(v.carriers.getFirst().body), SubLevelRemovalReason.REMOVED);
        });
    helper.runAtTickTime(
        245,
        () -> {
          var v = vehicles.getFirst();
          helper.assertTrue(!rider.isPassenger(), "Destroyed body stranded its passenger");
          helper.assertTrue(
              !FoundationData.get(level).vehicles.containsKey(v.chassis),
              "Destroyed vehicle retained live state");
          helper.assertTrue(
              container.getSubLevel(v.chassis) == null
                  && v.carriers.stream().allMatch(c -> container.getSubLevel(c.body) == null),
              "Destroyed vehicle left orphan bodies");
          rider.discard();
          var other = vehicles.getLast();
          container.removeSubLevel(
              container.getSubLevel(other.chassis), SubLevelRemovalReason.REMOVED);
          for (var rail : rails) {
            ((CoasterAnchorpointBlockEntity) level.getBlockEntity(rail.from))
                .removeAnchorPeerCurve(level, rail.to);
            FoundationData.get(level).rails.remove(rail);
            level.removeBlock(rail.from, false);
            level.removeBlock(rail.to, false);
          }
          level.removeBlock(stationPos, false);
          level.removeBlock(input, false);
          helper.succeed();
        });
  }
}
