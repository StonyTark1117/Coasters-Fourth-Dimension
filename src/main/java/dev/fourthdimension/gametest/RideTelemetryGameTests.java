package dev.fourthdimension.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.redstone.displayLink.*;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import dev.fourthdimension.integration.*;
import dev.fourthdimension.integration.create.*;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.shuttle.*;
import dev.fourthdimension.station.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;

/** Native station/winch snapshots, real Display Link sources, and fresh server authority. */
@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class RideTelemetryGameTests {
  @GameTest(template = "empty", batch = "fourthdimension_telemetry", timeoutTicks = 260)
  public static void sharedTelemetryRespectsConsistRecoveryAndControllerAuthority(
      GameTestHelper helper) {
    var level = helper.getLevel();
    var data = FoundationData.get(level);
    var container = SubLevelContainer.getContainer(level);
    for (int x = -55; x <= -35; x++)
      for (int z = 35; z <= 47; z++) {
        level.getChunk(x, z);
        level.setChunkForced(x, z, true);
      }
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000418"), "FDTelemetry"));
    player.setGameMode(GameType.CREATIVE);
    player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
    var rails = new ArrayList<FoundationData.Rail>();
    for (int i = 0; i < 4; i++) {
      var from = new BlockPos(-850 + i * 24, 180, 650);
      var to = from.offset(0, 0, 64);
      for (var pos : List.of(from, to))
        level.setBlockAndUpdate(
            pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
      rails.add(
          TrackPlacement.connect(
              level, from, to, TrackDesign.values()[i], player, player.getMainHandItem()));
    }
    var pax = ShuttleExamples.create(level, new BlockPos(-600, 180, 650), false);
    var groups = new ArrayList<List<FoundationData.Vehicle>>();
    var stations = new ArrayList<BoardingStationEntity>();
    var riders = new ArrayList<Pig>();
    var blocks = new ArrayList<BlockPos>();
    helper.runAtTickTime(
        15,
        () -> {
          for (int i = 0; i < rails.size(); i++) {
            var rail = rails.get(i);
            var edge = TrackGeometry.resolve(level, rail);
            var cars = new ArrayList<FoundationData.Vehicle>();
            for (double distance : i == 0 ? new double[] {12, 6} : new double[] {12}) {
              double t = edge.from().equals(rail.from) ? distance / 64 : 1 - distance / 64;
              cars.add(
                  VehicleFactory.create(
                      level,
                      TrackDesign.values()[i],
                      new CoasterPathTrackFrame.GraphHit(
                          CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t)));
            }
            groups.add(cars);
            var pos = rail.from.offset(-6, 0, 12);
            stations.add(station(helper, pos, cars.getFirst().chassis));
            blocks.add(pos);
            blocks.add(pos.above());
          }
          groups.add(pax.cars());
          var pos = new BlockPos(-594, 180, 658);
          stations.add(station(helper, pos, pax.cars().getFirst().chassis));
          blocks.add(pos);
          blocks.add(pos.above());
          var winchLink = pax.winch().getBlockPos().above();
          displayLink(helper, winchLink);
          blocks.add(winchLink);
        });
    helper.runAtTickTime(
        80,
        () -> {
          var arrow = groups.getFirst();
          helper.assertTrue(
              CoasterCartTrainLinkConstraint.linkCarts(
                  level,
                  (ServerSubLevel) container.getSubLevel(arrow.get(0).chassis),
                  (ServerSubLevel) container.getSubLevel(arrow.get(1).chassis),
                  6),
              "Arrow native coupling failed");
          ShuttleExamples.assemble(level, pax);
        });
    helper.runAtTickTime(
        100,
        () -> {
          for (var group : groups) {
            var open = RideTelemetry.sample(level, group.getFirst().chassis);
            helper.assertTrue(
                open.available()
                    && open.occupied() == 0
                    && open.boardingReady()
                    && !open.restraintsClosed()
                    && !open.dispatchReady(),
                "Open snapshot incorrect: " + open);
            for (var car : group) {
              var carrier = car.carriers.getFirst();
              var body = container.getSubLevel(carrier.body);
              var rider = EntityType.PIG.create(level);
              rider.setNoAi(true);
              var center = CoasterCartResolve.cartCenterWorld(container.getSubLevel(car.chassis));
              rider.setPos(center.x, center.y, center.z);
              level.addFreshEntity(rider);
              SeatBlock.sitDown(
                  level, body.getPlot().getCenterBlock().offset(carrier.seats.getFirst()), rider);
              helper.assertTrue(rider.isPassenger(), "Native telemetry boarding failed");
              riders.add(rider);
            }
            VehicleOperations.restraints(level, group.getFirst(), true);
          }
        });
    helper.runAtTickTime(
        120,
        () -> {
          var saved = data.save(new CompoundTag(), level.registryAccess());
          var winchSaved = pax.winch().saveWithoutMetadata(level.registryAccess());
          for (int repeat = 0; repeat < 20; repeat++)
            for (var group : groups) RideTelemetry.sample(level, group.getFirst().chassis);
          helper.assertTrue(
              saved.equals(data.save(new CompoundTag(), level.registryAccess()))
                  && winchSaved.equals(pax.winch().saveWithoutMetadata(level.registryAccess())),
              "Read-only telemetry changed mechanical or shuttle state");
          for (int i = 0; i < groups.size(); i++) {
            var lead = groups.get(i).getFirst();
            var snapshot = stations.get(i).telemetry();
            int cars = i == 0 ? 2 : i == 4 ? 4 : 1, capacity = i == 4 ? 16 : 8;
            helper.assertTrue(
                snapshot.available()
                    && snapshot.cars() == cars
                    && snapshot.capacity() == capacity
                    && snapshot.occupied() == cars
                    && snapshot.restraintsClosed()
                    && snapshot.boardingReady(),
                "Consist snapshot mismatch: " + snapshot);
            helper.assertTrue(
                snapshot.dispatchReady() == (i != 4),
                "Station advertised wrong dispatch authority: " + snapshot);
            if (i == 4)
              helper.assertTrue(
                  snapshot.dispatchDenial().contains("bound shuttle winch"),
                  "PAX station denial lost ownership");
            outputs(helper, stations.get(i), snapshot);
            var clone =
                new BoardingStationEntity(
                    stations.get(i).getBlockPos(), stations.get(i).getBlockState());
            clone.handleUpdateTag(
                stations.get(i).getUpdateTag(level.registryAccess()), level.registryAccess());
            helper.assertTrue(
                clone.telemetry().equals(snapshot), "Station update tag lost snapshot");
            System.out.println(
                "FD_TELEMETRY family="
                    + lead.family
                    + " cars="
                    + snapshot.cars()
                    + " occupied="
                    + snapshot.occupied()
                    + " capacity="
                    + snapshot.capacity()
                    + " stationDispatch="
                    + snapshot.dispatchReady()
                    + " sources=6 packet=true");
          }
          var winch = pax.winch();
          var snapshot = winch.telemetry();
          helper.assertTrue(
              snapshot.dispatchReady()
                  && snapshot.cars() == 4
                  && snapshot.occupied() == 4
                  && snapshot.capacity() == 16
                  && snapshot.phase().equals("BOARDING"),
              "Powered PAX winch not ready: " + snapshot);
          outputs(helper, winch, snapshot);
          helper.assertTrue(
              RideTelemetry.load(
                      winch.getUpdateTag(level.registryAccess()).getCompound("ride_telemetry"))
                  .equals(snapshot),
              "Winch update tag lost snapshot");
          var arrow = groups.getFirst();
          var lead = arrow.getFirst();
          var station = stations.getFirst();
          arrow.getLast().restraints = false;
          denied(
              helper,
              () ->
                  RideOperations.request(
                      level, station.getBlockPos(), lead.chassis, RideOperations.Request.DISPATCH),
              "restraints");
          helper.assertTrue(
              !RideTelemetry.atController(level, station.getBlockPos(), lead.chassis)
                  .dispatchReady(),
              "Open peer advertised dispatch");
          arrow.getLast().restraints = true;
          lead.recoveryCart = UUID.randomUUID();
          var recovery = RideTelemetry.sample(level, lead.chassis);
          helper.assertTrue(
              !recovery.dispatchReady()
                  && recovery.dispatchDenial().contains("Disconnect recovery"),
              "Recovery dispatch guard lost in telemetry");
          denied(
              helper,
              () ->
                  RideOperations.request(
                      level, station.getBlockPos(), lead.chassis, RideOperations.Request.DISPATCH),
              "Disconnect recovery");
          lead.recoveryCart = null;
          VehicleOperations.retention(level, lead, true);
          helper.assertTrue(
              RideTelemetry.sample(level, lead.chassis).retained(),
              "Retention absent from telemetry");
          denied(
              helper,
              () ->
                  RideOperations.request(
                      level, station.getBlockPos(), lead.chassis, RideOperations.Request.DISPATCH),
              "maintenance");
          VehicleOperations.retention(level, lead, false);
          denied(
              helper,
              () ->
                  RideOperations.request(
                      level,
                      stations.get(1).getBlockPos(),
                      lead.chassis,
                      RideOperations.Request.DISPATCH),
              "not bound");
          var far = station(helper, new BlockPos(-895, 180, 662), lead.chassis);
          blocks.add(far.getBlockPos());
          blocks.add(far.getBlockPos().above());
          helper.assertTrue(
              RideTelemetry.atController(level, far.getBlockPos(), lead.chassis)
                  .dispatchDenial()
                  .contains("outside station range"),
              "Range absent from controller snapshot");
          denied(
              helper,
              () ->
                  RideOperations.request(
                      level, far.getBlockPos(), lead.chassis, RideOperations.Request.DISPATCH),
              "outside station range");
          denied(
              helper,
              () ->
                  RideOperations.request(
                      level,
                      stations.get(4).getBlockPos(),
                      winch.vehicle,
                      RideOperations.Request.DISPATCH),
              "bound shuttle winch");
          var stale = RideTelemetry.atController(level, station.getBlockPos(), lead.chassis);
          helper.assertTrue(stale.dispatchReady(), "Healthy station did not recover readiness");
          VehicleOperations.restraints(level, lead, false);
          denied(
              helper,
              () ->
                  RideOperations.request(
                      level, station.getBlockPos(), lead.chassis, RideOperations.Request.DISPATCH),
              "restraints");
          helper.assertTrue(
              stale.dispatchReady() && lead.stopping,
              "Stale snapshot granted authority or mutated");
          VehicleOperations.restraints(level, lead, true);
          System.out.println(
              "FD_TELEMETRY guards=7 readOnly=true recoveryPreserved=true"
                  + " staleSnapshotRejected=true");
        });
    helper.runAtTickTime(
        140,
        () -> {
          for (int i = 0; i < 4; i++)
            RideOperations.request(
                level,
                stations.get(i).getBlockPos(),
                groups.get(i).getFirst().chassis,
                RideOperations.Request.DISPATCH);
          RideOperations.request(
              level,
              pax.winch().getBlockPos(),
              pax.winch().vehicle,
              RideOperations.Request.DISPATCH);
          helper.assertTrue(
              pax.winch().phase == ShuttleWinchEntity.Phase.LIFTING,
              "Shared request bypassed shuttle state machine");
        });
    helper.runAtTickTime(
        155,
        () -> {
          helper.assertTrue(
              pax.winch().telemetry().phase().equals("LIFTING")
                  && !pax.winch().telemetry().dispatchReady(),
              "Winch telemetry did not follow lifting");
          outputs(helper, pax.winch(), pax.winch().telemetry());
          RideOperations.request(
              level,
              pax.winch().getBlockPos(),
              pax.winch().vehicle,
              RideOperations.Request.CAPTURE);
          for (int i = 0; i < 4; i++)
            RideOperations.request(
                level,
                stations.get(i).getBlockPos(),
                groups.get(i).getFirst().chassis,
                RideOperations.Request.CAPTURE);
        });
    helper.runAtTickTime(
        180,
        () -> {
          var state = pax.winch().telemetry();
          helper.assertTrue(
              state.phase().equals("FAULT")
                  && !state.dispatchReady()
                  && state.fault().contains("emergency capture"),
              "Shuttle fault not exposed");
          outputs(helper, pax.winch(), state);
          System.out.println(
              "FD_TELEMETRY paxPhases=BOARDING,LIFTING,FAULT winchAuthority=true"
                  + " faultVisible=true");
        });
    helper.runAtTickTime(
        210,
        () -> {
          for (var group : groups) for (var car : group) Boarding.recover(level, car);
          for (var rider : riders) rider.discard();
          for (var group : groups)
            for (var car : group) {
              for (var carrier : car.carriers) {
                var body = container.getSubLevel(carrier.body);
                if (body != null) container.removeSubLevel(body, SubLevelRemovalReason.REMOVED);
              }
              var body = container.getSubLevel(car.chassis);
              if (body != null) container.removeSubLevel(body, SubLevelRemovalReason.REMOVED);
              data.vehicles.remove(car.chassis);
            }
          var owned =
              data.rails.stream()
                  .filter(
                      r ->
                          r.from.getZ() >= 590
                              && r.from.getZ() < 750
                              && r.from.getX() >= -850
                              && r.from.getX() <= -600)
                  .toList();
          for (var rail : owned) {
            data.rails.remove(rail);
            level.removeBlock(rail.from, false);
            level.removeBlock(rail.to, false);
          }
          for (var pos : blocks) level.removeBlock(pos, false);
          level.removeBlock(pax.winch().getBlockPos(), false);
          level.removeBlock(pax.winch().getBlockPos().west(), false);
          for (int x = -55; x <= -35; x++)
            for (int z = 35; z <= 47; z++) level.setChunkForced(x, z, false);
          helper.succeed();
          System.out.println("FD_TELEMETRY COMPLETE");
        });
  }

  private static BoardingStationEntity station(GameTestHelper helper, BlockPos pos, UUID id) {
    var level = helper.getLevel();
    level.setBlockAndUpdate(pos, StationContent.STATION.get().defaultBlockState());
    var station = (BoardingStationEntity) level.getBlockEntity(pos);
    station.vehicle = id;
    displayLink(helper, pos.above());
    return station;
  }

  private static void displayLink(GameTestHelper helper, BlockPos pos) {
    helper
        .getLevel()
        .setBlockAndUpdate(
            pos,
            AllBlocks.DISPLAY_LINK
                .getDefaultState()
                .setValue(DisplayLinkBlock.FACING, Direction.UP));
  }

  private static void outputs(
      GameTestHelper helper,
      net.minecraft.world.level.block.entity.BlockEntity block,
      RideTelemetry snapshot) {
    var level = helper.getLevel();
    helper.assertTrue(
        DisplaySource.getAll(level, block.getBlockPos()).stream()
                .filter(s -> s instanceof RideDisplaySource)
                .count()
            == 6,
        "Create did not discover six sources");
    var context =
        new DisplayLinkContext(
            level, (DisplayLinkBlockEntity) level.getBlockEntity(block.getBlockPos().above()));
    helper.assertTrue(
        context.getSourceBlockEntity() == block, "Native Display Link source selection failed");
    for (var field : RideDisplaySource.Field.values()) {
      var source =
          (RideDisplaySource)
              DisplaySource.get(
                  ResourceLocation.fromNamespaceAndPath(
                      "fourthdimension", "ride_" + field.name().toLowerCase(Locale.ROOT)));
      var text = source.provideText(context, new DisplayTargetStats(1, 128, null));
      helper.assertTrue(
          text.size() == 1 && text.getFirst().getString().equals(source.text(snapshot).getString()),
          "Native Display Link field mismatch: " + field);
    }
    var tooltip = new ArrayList<Component>();
    // Create's kinetic overlay uses client formatting. Verify our shared data formatter here;
    // exercise the actual kinetic overlay and client application in a live client checkpoint.
    boolean shown =
        block instanceof ShuttleWinchEntity
            ? RideInformation.goggles(tooltip, snapshot)
            : ((com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation) block)
                .addToGoggleTooltip(tooltip, false);
    helper.assertTrue(shown && tooltip.size() >= 7, "Goggle snapshot missing");
    helper.assertTrue(
        snapshot.equals(RideTelemetry.load(snapshot.save())), "Telemetry roundtrip lost fields");
    helper.assertTrue(
        !snapshot.save().toString().contains("riderSeats")
            && !snapshot.save().toString().contains("occupants"),
        "Snapshot exposed passenger identities");
  }

  private static void denied(GameTestHelper helper, Runnable operation, String reason) {
    try {
      operation.run();
      throw new AssertionError("Unsafe operation accepted: " + reason);
    } catch (IllegalArgumentException expected) {
      helper.assertTrue(
          expected.getMessage().contains(reason),
          "Wrong operation denial: " + expected.getMessage());
    }
  }
}
