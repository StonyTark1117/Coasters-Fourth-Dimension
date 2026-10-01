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
          var savedSeat = new net.minecraft.nbt.CompoundTag();
          helper.assertTrue(
              rider.getVehicle().save(savedSeat), "Native occupied seat did not serialize");
          var replay =
              net.minecraft.world.entity.EntityType.loadEntityRecursive(
                  savedSeat, level, entity -> entity);
          helper.assertTrue(
              replay != null
                  && replay.getPassengers().size() == 1
                  && replay.getFirstPassenger().getUUID().equals(rider.getUUID())
                  && !replay.isAddedToLevel()
                  && !replay.getFirstPassenger().isAddedToLevel(),
              "Saved passenger was rejected by closed restraints during deserialization");
          var unknownSeat = savedSeat.copy();
          unknownSeat
              .getList("Passengers", net.minecraft.nbt.Tag.TAG_COMPOUND)
              .getCompound(0)
              .putUUID("UUID", java.util.UUID.randomUUID());
          var rejected =
              net.minecraft.world.entity.EntityType.loadEntityRecursive(
                  unknownSeat, level, entity -> entity);
          helper.assertTrue(
              rejected != null && rejected.getPassengers().isEmpty(),
              "Unknown serialized passenger bypassed closed restraints");
          helper.assertTrue(rider.isPassenger(), "Deserialization probe disturbed the live rider");
          System.out.println(
              "FD_SAVED_PASSENGER nativeReplay=true unknownRejected=true liveUnchanged=true");
          rider.stopRiding();
          helper.assertTrue(rider.isPassenger(), "Moving restraint can be bypassed with dismount");
          v.restraints = false;
          rider.stopRiding();
          delayedPassengerLoad(helper, body, v, c, savedSeat, rider.getUUID(), masses[0]);
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

  /** Exercise the native PENDING -> LOADED boundary without depending on disk I/O timing. */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private static void delayedPassengerLoad(
      GameTestHelper helper,
      ServerSubLevel body,
      FoundationData.Vehicle vehicle,
      FoundationData.Carrier carrier,
      net.minecraft.nbt.CompoundTag savedSeat,
      UUID rider,
      double emptyMass) {
    var level = helper.getLevel();
    var prior = new HashMap<Long, Object>();
    it.unimi.dsi.fastutil.longs.Long2ObjectMap<Object> statuses;
    try {
      var field =
          net.minecraft.world.level.entity.PersistentEntitySectionManager.class.getDeclaredField(
              "chunkLoadStatuses");
      field.setAccessible(true);
      var manager = net.minecraft.server.level.ServerLevel.class.getDeclaredField("entityManager");
      manager.setAccessible(true);
      statuses = (it.unimi.dsi.fastutil.longs.Long2ObjectMap<Object>) field.get(manager.get(level));
    } catch (ReflectiveOperationException ex) {
      throw new IllegalStateException("Cannot control native entity loading for regression", ex);
    }
    try {
      for (var offset : carrier.seats) {
        long chunk =
            new net.minecraft.world.level.ChunkPos(body.getPlot().getCenterBlock().offset(offset))
                .toLong();
        helper.assertTrue(level.areEntitiesLoaded(chunk), "Carrier entity chunk was not loaded");
        prior.putIfAbsent(chunk, statuses.get(chunk));
      }
      for (var entry : prior.entrySet())
        statuses.put(
            entry.getKey().longValue(),
            Enum.valueOf((Class) entry.getValue().getClass(), "PENDING"));
      vehicle.restraints = true;
      NativeLoading.update(body, vehicle, carrier);
      helper.assertTrue(
          carrier.occupants.contains(rider), "Pending entity load erased saved passenger identity");
      helper.assertTrue(
          Math.abs(body.getMassTracker().getMass() - emptyMass - NativeLoading.MASS_PER_RIDER)
              < 1e-5,
          "Pending entity load removed passenger mass");
      var replay =
          net.minecraft.world.entity.EntityType.loadEntityRecursive(
              savedSeat, level, entity -> entity);
      helper.assertTrue(
          replay != null
              && replay.getPassengers().size() == 1
              && replay.getFirstPassenger().getUUID().equals(rider),
          "Delayed saved passenger could not restore behind closed restraints");
    } finally {
      prior.forEach((chunk, status) -> statuses.put(chunk.longValue(), status));
      vehicle.restraints = false;
    }
    NativeLoading.update(body, vehicle, carrier);
    NativeLoading.update(body, vehicle, carrier);
    helper.assertTrue(carrier.occupants.isEmpty(), "Loaded empty seats retained stale occupants");
    helper.assertTrue(
        Math.abs(body.getMassTracker().getMass() - emptyMass) < 1e-5,
        "Loaded empty seats did not remove passenger mass exactly once");
    System.out.println(
        "FD_DELAYED_PASSENGER pendingIdentity=true pendingMass=true delayedReplay=true"
            + " loadedEmpty=true");
  }
}
