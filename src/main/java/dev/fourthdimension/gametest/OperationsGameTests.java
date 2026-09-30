package dev.fourthdimension.gametest;

import dev.fourthdimension.integration.*;
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
          var before = RideTelemetry.sample(level, v.chassis);
          helper.assertTrue(
              before.available() && before.boardingReady() && !before.dispatchReady(),
              "Open restraints reported dispatch ready");
          long revision = v.revision;
          EditorGameTests.rejects(
              helper,
              () ->
                  RideOperations.request(
                      level, stationPos, v.chassis, RideOperations.Request.DISPATCH),
              "Shared controller bypassed open restraints");
          helper.assertTrue(
              v.revision == revision && v.stopping,
              "Read-only telemetry or rejected dispatch mutated vehicle");
          EditorGameTests.rejects(
              helper,
              () ->
                  RideOperations.request(
                      level, stationPos.offset(1, 0, 0), v.chassis, RideOperations.Request.CAPTURE),
              "Unbound controller operated a ride");
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
          helper.assertTrue(
              RideTelemetry.sample(level, v.chassis).dispatchReady(),
              "Closed aligned ride did not report dispatch ready");
          var sources =
              com.simibubi.create.api.behaviour.display.DisplaySource.getAll(level, stationPos);
          helper.assertTrue(
              sources.stream()
                      .filter(
                          s ->
                              s instanceof dev.fourthdimension.integration.create.RideDisplaySource)
                      .count()
                  == 6,
              "Station Display Link sources not registered");
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
          var telemetry = RideTelemetry.sample(level, v.chassis);
          helper.assertTrue(
              telemetry.retained()
                  && !telemetry.dispatchReady()
                  && telemetry.occupied() == 1
                  && telemetry.capacity() == 4,
              "Consist telemetry lost occupancy or retention interlock");
          var panel = (BoardingStationEntity) level.getBlockEntity(stationPos);
          helper.assertTrue(
              !panel.telemetry().dispatchReady(), "Station cached stale dispatch readiness");
          helper.assertTrue(
              panel.getUpdateTag(level.registryAccess()).contains("ride_telemetry"),
              "Goggle diagnostics were not synchronized");
          var remotePos = stationPos.offset(24, 0, 0);
          level.setBlockAndUpdate(remotePos, StationContent.STATION.get().defaultBlockState());
          var remote = (BoardingStationEntity) level.getBlockEntity(remotePos);
          remote.vehicle = v.chassis;
          helper.runAfterDelay(
              6,
              () -> {
                helper.assertTrue(
                    remote.telemetry().available()
                        && remote.telemetry().occupied() == 1
                        && !remote.telemetry().dispatchReady()
                        && remote
                            .telemetry()
                            .dispatchDenial()
                            .equals("Vehicle outside station range"),
                    "Station lost bound ride telemetry outside its operation range");
                var linkPos = remotePos.above();
                var signPos = remotePos.east(2);
                level.setBlockAndUpdate(signPos.below(), Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(signPos, Blocks.OAK_SIGN.defaultBlockState());
                level.setBlockAndUpdate(
                    linkPos,
                    com.simibubi.create.AllBlocks.DISPLAY_LINK
                        .getDefaultState()
                        .setValue(
                            com.simibubi
                                .create
                                .content
                                .redstone
                                .displayLink
                                .DisplayLinkBlock
                                .FACING,
                            Direction.UP));
                var link =
                    (com.simibubi.create.content.redstone.displayLink.DisplayLinkBlockEntity)
                        level.getBlockEntity(linkPos);
                link.target(signPos);
                link.activeSource =
                    com.simibubi.create.api.behaviour.display.DisplaySource.get(
                        net.minecraft.resources.ResourceLocation.parse(
                            "fourthdimension:ride_occupancy"));
                link.updateGatheredData();
                var sign =
                    (net.minecraft.world.level.block.entity.SignBlockEntity)
                        level.getBlockEntity(signPos);
                helper.assertTrue(
                    sign.getFrontText().getMessage(0, false).getString().equals("1 / 4"),
                    "Create Display Link did not transfer occupied ride data to its target");
                level.removeBlock(linkPos, false);
                level.removeBlock(signPos, false);
                level.removeBlock(signPos.below(), false);
                level.removeBlock(remotePos, false);
              });
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
