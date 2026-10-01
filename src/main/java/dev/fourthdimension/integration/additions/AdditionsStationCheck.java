package dev.fourthdimension.integration.additions;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.integration.*;
import dev.fourthdimension.integration.create.RideInformation;
import dev.fourthdimension.runtime.FamilyControllers;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.plushivr.coasterfins.block.StationBlockEntity;
import dev.plushivr.coasterfins.logic.*;
import dev.plushivr.coasterfins.registry.CFBlocks;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.joml.Vector3d;

/** Runs actual upstream station scanning, wireless channels and powered fin physics. */
public final class AdditionsStationCheck {
  private AdditionsStationCheck() {}

  private static void rejects(GameTestHelper helper, Runnable action, String message) {
    boolean rejected = false;
    try {
      action.run();
    } catch (IllegalArgumentException expected) {
      rejected = true;
    }
    helper.assertTrue(rejected, message);
  }

  public static void run(GameTestHelper helper) {
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000401"), "FDAdditionsTest"));
    player.setGameMode(GameType.CREATIVE);
    var rails = new ArrayList<FoundationData.Rail>();
    var vehicles = new ArrayList<FoundationData.Vehicle>();
    var stations = new ArrayList<StationBlockEntity>();
    var fins = new ArrayList<BlockPos>();
    var held = new ArrayList<Vector3d>();
    var riders = new ArrayList<net.minecraft.world.entity.animal.Pig>();
    for (var design : TrackDesign.values()) {
      var from = new BlockPos(100 + design.ordinal() * 32, 150, -850);
      var to = from.offset(0, 0, 60);
      for (int x = (from.getX() - 16) >> 4; x <= (from.getX() + 16) >> 4; x++)
        for (int z = (from.getZ() - 16) >> 4; z <= (to.getZ() + 16) >> 4; z++)
          level.setChunkForced(x, z, true);
      for (var pos : List.of(from, to))
        level.setBlockAndUpdate(
            pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
      player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
      rails.add(TrackPlacement.connect(level, from, to, design, player, player.getMainHandItem()));
    }
    helper.runAtTickTime(
        15,
        () -> {
          for (var design : TrackDesign.values()) {
            var rail = rails.get(design.ordinal());
            var edge = TrackGeometry.resolve(level, rail);
            var v =
                VehicleFactory.create(
                    level,
                    design,
                    new CoasterPathTrackFrame.GraphHit(
                        CoasterPathGraphManager.sampleEdgePosition(level, edge, .3), edge, .3));
            vehicles.add(v);
            var span = List.of(new FinSpanSegment(rail.from, rail.to, 0, 1, true));
            var stationPos = rail.from.offset(-8, 0, 24);
            level.setBlockAndUpdate(stationPos, CFBlocks.STATION.get().defaultBlockState());
            var station = (StationBlockEntity) level.getBlockEntity(stationPos);
            station.setSpan(span);
            var frequencies =
                List.of(
                    net.minecraft.world.item.Items.DIAMOND,
                    net.minecraft.world.item.Items.EMERALD,
                    net.minecraft.world.item.Items.REDSTONE,
                    net.minecraft.world.item.Items.GOLD_INGOT);
            for (int channel = 0; channel < 4; channel++) {
              station.setFrequency(
                  channel,
                  0,
                  FamilyControllers.CONTROLLERS
                      .get(design.family)
                      .get()
                      .asItem()
                      .getDefaultInstance());
              station.setFrequency(channel, 1, frequencies.get(channel).getDefaultInstance());
            }
            station.setDispatch(true, 1, 1, 0);
            stations.add(station);
            var finPos = rail.from.offset(-10, 0, 24);
            level.setBlockAndUpdate(
                finPos,
                BuiltInRegistries.BLOCK
                    .get(ResourceLocation.parse("coasterfins:coaster_fin"))
                    .defaultBlockState());
            var fin = level.getBlockEntity(finPos);
            var tag = fin.saveWithoutMetadata(level.registryAccess());
            tag.putString("SpanMode", "boost");
            var segments = new ListTag();
            span.forEach(s -> segments.add(s.write()));
            tag.put("SpanSegments", segments);
            fin.loadWithComponents(tag, level.registryAccess());
            level.setBlockAndUpdate(finPos.below(), Blocks.REDSTONE_BLOCK.defaultBlockState());
            fins.add(finPos);
          }
        });
    helper.runAtTickTime(
        30,
        () -> {
          for (var pos : fins) {
            var fin =
                (com.simibubi.create.foundation.blockEntity.SmartBlockEntity)
                    level.getBlockEntity(pos);
            fin.getBehaviour(
                    com.simibubi
                        .create
                        .foundation
                        .blockEntity
                        .behaviour
                        .scrollValue
                        .ScrollValueBehaviour
                        .TYPE)
                .setValue(10);
          }
        });
    helper.runAtTickTime(
        85,
        () -> {
          for (int i = 0; i < vehicles.size(); i++) {
            var v = vehicles.get(i);
            var station = stations.get(i);
            helper.assertTrue(
                Boarding.ready(level, v), "Additions fixture not captured: " + v.family);
            var snapshot = ((RideInformation.Source) station).telemetry();
            helper.assertTrue(
                snapshot.available()
                    && snapshot.capacity()
                        == v.carriers.stream().mapToInt(c -> c.seats.size()).sum()
                    && !snapshot.dispatchReady(),
                "Upstream station did not expose 4D telemetry: " + v.family + " " + snapshot);
            helper.assertTrue(
                ((SpanController) level.getBlockEntity(fins.get(i))).isForceActive(),
                "Native fin not powered");
            helper.assertTrue(
                station.channel(0).power() == 15 && station.channel(2).power() == 0,
                "Open restraint signals wrong");
            helper.assertTrue(
                com.simibubi.create.api.behaviour.display.DisplaySource.getAll(
                            level, station.getBlockPos())
                        .stream()
                        .filter(
                            s ->
                                s
                                    instanceof
                                    dev.fourthdimension.integration.create.RideDisplaySource)
                        .count()
                    == 6,
                "Additions Display Link sources missing");
            helper.assertTrue(
                station.getUpdateTag(level.registryAccess()).contains("fourthdimension_ride"),
                "Additions telemetry not synchronized");
            held.add(
                new Vector3d(CoasterCartResolve.cartCenterWorld(container.getSubLevel(v.chassis))));
            var pig = net.minecraft.world.entity.EntityType.PIG.create(level);
            pig.setNoAi(true);
            pig.setPos(station.getBlockPos().getX(), 150, station.getBlockPos().getZ());
            level.addFreshEntity(pig);
            var c = v.carriers.getFirst();
            SeatBlock.sitDown(
                level,
                container.getSubLevel(c.body).getPlot().getCenterBlock().offset(c.seats.getFirst()),
                pig);
            helper.assertTrue(pig.isPassenger(), "Additions passenger failed to board");
            riders.add(pig);
            VehicleOperations.restraints(level, v, true);
            VehicleOperations.retention(level, v, true);
            rejects(
                helper,
                () ->
                    RideOperations.request(
                        level, station.getBlockPos(), v.chassis, RideOperations.Request.DISPATCH),
                "Cached station granted operation outside native scan");
          }
        });
    helper.runAtTickTime(
        135,
        () -> {
          for (int i = 0; i < vehicles.size(); i++) {
            var v = vehicles.get(i);
            var station = stations.get(i);
            var snapshot = ((RideInformation.Source) station).telemetry();
            int expected = 1 + Math.round(14f / snapshot.capacity());
            helper.assertTrue(
                snapshot.occupied() == 1
                    && station.channel(1).power() == expected
                    && station.channel(2).power() == 15,
                "Carrier occupancy/restraint signal missing: " + snapshot);
            helper.assertTrue(
                v.stopping && !snapshot.dispatchReady() && station.channel(0).power() == 15,
                "Auto-dispatch bypassed maintenance or PAX ownership");
            helper.assertTrue(
                CoasterCartResolve.cartCenterWorld(container.getSubLevel(v.chassis))
                        .distance(held.get(i))
                    < .2,
                "Fins pulled held car");
            VehicleOperations.retention(level, v, false);
          }
        });
    helper.runAtTickTime(
        145,
        () -> {
          for (var v : vehicles) helper.assertTrue(v.stopping, "Auto-dispatch skipped ready delay");
        });
    helper.runAtTickTime(
        175,
        () -> {
          for (int i = 0; i < vehicles.size(); i++) {
            var v = vehicles.get(i);
            var station = stations.get(i);
            double traveled =
                CoasterCartResolve.cartCenterWorld(container.getSubLevel(v.chassis))
                    .distance(held.get(i));
            System.out.println(
                "FD_ADDITIONS family="
                    + v.family
                    + " travel="
                    + traveled
                    + " stopped="
                    + v.stopping
                    + " telemetry="
                    + ((RideInformation.Source) station).telemetry());
            if (v.family == Family.PAX) {
              helper.assertTrue(
                  v.stopping && traveled < .2 && station.channel(0).power() == 15,
                  "Additions overrode PAX mechanical control");
            } else {
              helper.assertTrue(
                  !v.stopping && traveled > 1 && station.channel(0).power() == 0,
                  "Validated dispatch/fin propulsion failed: " + v.family);
            }
            helper.assertTrue(
                riders.get(i).isPassenger() && v.fault.isBlank(),
                "Additions lost passenger or faulted");
            var fin = level.getBlockEntity(fins.get(i));
            var tag = fin.saveWithoutMetadata(level.registryAccess());
            tag.putString("SpanMode", "stop");
            fin.loadWithComponents(tag, level.registryAccess());
            station.clearSpan();
          }
        });
    helper.runAtTickTime(
        280,
        () -> {
          for (int i = 0; i < vehicles.size(); i++) {
            var v = vehicles.get(i);
            double speed =
                container
                    .physicsSystem()
                    .getPipeline()
                    .getLinearVelocity(
                        (dev.ryanhcode.sable.sublevel.ServerSubLevel)
                            container.getSubLevel(v.chassis),
                        new Vector3d())
                    .length();
            System.out.println("FD_ADDITIONS_STOP family=" + v.family + " speed=" + speed);
            helper.assertTrue(speed < .15, "Native STOP fin failed to hold car: " + v.family);
            level.removeBlock(fins.get(i).below(), false);
            VehicleOperations.capture(level, v);
          }
        });
    helper.runAtTickTime(
        430,
        () -> {
          for (int i = 0; i < vehicles.size(); i++) {
            var v = vehicles.get(i);
            helper.assertTrue(
                !((RideInformation.Source) stations.get(i)).telemetry().available(),
                "Cleared station retained stale telemetry");
            helper.assertTrue(
                Boarding.ready(level, v), "Additions ride failed recapture: " + v.family);
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
