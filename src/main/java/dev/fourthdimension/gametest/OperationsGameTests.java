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

  @GameTest(template = "empty", timeoutTicks = 250)
  public static void fiveFamilySupportLossFailsClosedWithoutRebindingOrDuplicatingRiders(
      GameTestHelper helper) {
    var level = helper.getLevel();
    for (int x = -58; x <= -50; x++)
      for (int z = -33; z <= -29; z++) level.setChunkForced(x, z, true);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000902"), "FDSupportLoss"));
    player.setGameMode(GameType.CREATIVE);
    var data = FoundationData.get(level);
    var container = SubLevelContainer.getContainer(level);
    var rails = new ArrayList<FoundationData.Rail>();
    var cars = new ArrayList<FoundationData.Vehicle>();
    var riders = new ArrayList<net.minecraft.world.entity.animal.Pig>();
    var originalBindings = new HashMap<UUID, List<BlockPos>>();
    for (var design : TrackDesign.values()) {
      // Keep this recovery test away from the family matrix's fixed fixture coordinates.
      // Simulated Coasters' native physics also requires this fixture to remain within 1024 blocks.
      var from = new BlockPos(-900 + design.ordinal() * 24, 120, -500);
      var to = from.offset(0, 0, 32);
      for (var anchor : List.of(from, to))
        level.setBlockAndUpdate(
            anchor, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
      player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
      rails.add(TrackPlacement.connect(level, from, to, design, player, player.getMainHandItem()));
    }
    helper.runAtTickTime(
        20,
        () -> {
          for (int i = 0; i < rails.size(); i++) {
            var rail = rails.get(i);
            var edge = TrackGeometry.resolve(level, rail);
            var hit =
                new CoasterPathTrackFrame.GraphHit(
                    CoasterPathGraphManager.sampleEdgePosition(level, edge, .5), edge, .5);
            cars.add(VehicleFactory.create(level, TrackDesign.values()[i], hit));
          }
        });
    helper.runAtTickTime(
        90,
        () -> {
          for (var car : cars) {
            helper.assertTrue(Boarding.ready(level, car), "Support-loss fixture did not reach boarding");
            var carrier = (ServerSubLevel) container.getSubLevel(car.carriers.getFirst().body);
            var rider = net.minecraft.world.entity.EntityType.PIG.create(level);
            rider.setNoAi(true);
            rider.setPos(carrier.logicalPose().position().x, carrier.logicalPose().position().y, carrier.logicalPose().position().z);
            level.addFreshEntity(rider);
            com.simibubi.create.content.contraptions.actors.seat.SeatBlock.sitDown(
                level, carrier.getPlot().getCenterBlock().offset(car.carriers.getFirst().seats.getFirst()), rider);
            helper.assertTrue(rider.isPassenger(), "Support-loss fixture rider did not mount");
            riders.add(rider);
            VehicleOperations.restraints(level, car, true);
            originalBindings.put(car.chassis, List.of(car.boundFrom, car.boundTo));
          }
          for (var rail : rails)
            ((CoasterAnchorpointBlockEntity) level.getBlockEntity(rail.from))
                .removeAnchorPeerCurve(level, rail.to);
        });
    helper.runAtTickTime(
        140,
        () -> {
          helper.assertTrue(
              riders.size() == 5 && riders.stream().allMatch(net.minecraft.world.entity.Entity::isPassenger),
              "Support loss displaced or duplicated a mounted rider");
          for (var car : cars) {
            helper.assertTrue(
                car.stopping
                    && car.restraints
                    && car.fault.equals("Matching track support lost")
                    && List.of(car.boundFrom, car.boundTo).equals(originalBindings.get(car.chassis)),
                "Support loss did not preserve a fail-closed functional binding for " + car.variant);
            helper.assertTrue(
                data.vehicles.get(car.chassis) == car
                    && car.carriers.stream()
                        .allMatch(
                            carrier ->
                                carrier.capture && container.getSubLevel(carrier.body) != null),
                "Support loss removed or released a carrier for " + car.variant);
          }
          var restored =
              FoundationData.load(
                  data.save(new net.minecraft.nbt.CompoundTag(), level.registryAccess()),
                  level.registryAccess());
          for (var car : cars) {
            var loaded = restored.vehicles.get(car.chassis);
            helper.assertTrue(
                loaded != null
                    && loaded.stopping
                    && loaded.restraints
                    && loaded.fault.equals("Matching track support lost")
                    && List.of(loaded.boundFrom, loaded.boundTo).equals(originalBindings.get(car.chassis))
                    && loaded.carriers.stream().allMatch(carrier -> carrier.capture),
                "Support-loss state did not survive save/load for " + car.variant);
          }
          System.out.println(
              "FD_SUPPORT_LOSS families="
                  + cars.stream().map(car -> car.family).toList()
                  + " riders="
                  + riders.size()
                  + " bindingsPreserved=true savedStatePreserved=true failClosed=true");
        });
    helper.runAtTickTime(
        155,
        () -> {
          // A real carrier removal must recover every rider and permanently remove its entire
          // vehicle group. Do this after support loss so disassembly cannot silently rebind a
          // stopped vehicle to another route.
          for (var car : cars) {
            var body = container.getSubLevel(car.carriers.getFirst().body);
            helper.assertTrue(body != null, "Disassembly fixture lost carrier before removal");
            container.removeSubLevel(body, SubLevelRemovalReason.REMOVED);
          }
        });
    helper.runAtTickTime(
        195,
        () -> {
          helper.assertTrue(
              riders.stream().noneMatch(net.minecraft.world.entity.Entity::isPassenger),
              "Permanent disassembly stranded a rider");
          for (var car : cars) {
            helper.assertTrue(
                !data.vehicles.containsKey(car.chassis),
                "Permanent disassembly retained vehicle state for " + car.variant);
            helper.assertTrue(
                container.getSubLevel(car.chassis) == null
                    && car.carriers.stream().allMatch(c -> container.getSubLevel(c.body) == null),
                "Permanent disassembly left an orphan native body for " + car.variant);
          }
          var saved =
              FoundationData.load(
                  data.save(new net.minecraft.nbt.CompoundTag(), level.registryAccess()),
                  level.registryAccess());
          helper.assertTrue(
              cars.stream().noneMatch(car -> saved.vehicles.containsKey(car.chassis)),
              "Permanent disassembly left stale vehicle state in the saved record");
          System.out.println(
              "FD_PERMANENT_DISASSEMBLY families="
                  + cars.stream().map(car -> car.family).toList()
                  + " ridersRecovered="
                  + riders.size()
                  + " bodiesRemoved=true savedStateRemoved=true");
        });
    helper.runAtTickTime(
        215,
        () -> {
          for (var rider : riders) {
            rider.discard();
          }
          for (var car : cars) {
            for (var carrier : car.carriers) {
              var body = container.getSubLevel(carrier.body);
              if (body != null) container.removeSubLevel(body, SubLevelRemovalReason.REMOVED);
            }
            var chassis = container.getSubLevel(car.chassis);
            if (chassis != null) container.removeSubLevel(chassis, SubLevelRemovalReason.REMOVED);
            data.vehicles.remove(car.chassis);
          }
          for (var rail : rails) {
            data.rails.remove(rail);
            level.removeBlock(rail.from, false);
            level.removeBlock(rail.to, false);
          }
          helper.succeed();
        });
  }

  @GameTest(template = "empty", timeoutTicks = 450)
  public static void fiveFamilySupportRepairRequiresExactTrackAndRealignment(
      GameTestHelper helper) {
    var level = helper.getLevel();
    // Native plots and their carrier bodies must remain in loaded chunks for the full recovery
    // sequence. This corridor follows the already-proven support-loss fixture layout but remains
    // clear of it and every concurrent fixture.
    for (int x = -65; x <= -56; x++)
      for (int z = -33; z <= -29; z++) level.setChunkForced(x, z, true);
    var player = FakePlayerFactory.get(
        level,
        new com.mojang.authlib.GameProfile(
            UUID.fromString("fdfdfdfd-0000-0000-0000-000000000909"), "FDSupportRepair"));
    player.setGameMode(GameType.CREATIVE);
    var data = FoundationData.get(level);
    var container = SubLevelContainer.getContainer(level);
    var rails = new ArrayList<FoundationData.Rail>();
    var cars = new ArrayList<FoundationData.Vehicle>();
    var riders = new ArrayList<net.minecraft.world.entity.animal.Pig>();
    for (var design : TrackDesign.values()) {
      // Use a separate chunk-loaded corridor; native carrier clearance extends beyond each car's
      // center and cannot overlap concurrently running fixtures.
      var from = new BlockPos(-1020 + design.ordinal() * 24, 120, -500);
      var to = from.offset(0, 0, 32);
      for (var anchor : List.of(from, to))
        level.setBlockAndUpdate(
            anchor, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
      player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
      rails.add(TrackPlacement.connect(level, from, to, design, player, player.getMainHandItem()));
    }
    helper.runAtTickTime(
        20,
        () -> {
          for (var rail : rails) {
            var edge = TrackGeometry.resolve(level, rail);
            var hit =
                new CoasterPathTrackFrame.GraphHit(
                    CoasterPathGraphManager.sampleEdgePosition(level, edge, .5), edge, .5);
            cars.add(VehicleFactory.create(level, TrackDesign.byVariant(rail.variant), hit));
          }
        });
    helper.runAtTickTime(
        85,
        () -> {
          for (var car : cars) {
            helper.assertTrue(Boarding.ready(level, car), "Repair fixture did not reach boarding");
            var carrier = (ServerSubLevel) container.getSubLevel(car.carriers.getFirst().body);
            var rider = net.minecraft.world.entity.EntityType.PIG.create(level);
            rider.setNoAi(true);
            rider.setPos(
                carrier.logicalPose().position().x,
                carrier.logicalPose().position().y,
                carrier.logicalPose().position().z);
            level.addFreshEntity(rider);
            com.simibubi.create.content.contraptions.actors.seat.SeatBlock.sitDown(
                level,
                carrier.getPlot().getCenterBlock().offset(car.carriers.getFirst().seats.getFirst()),
                rider);
            helper.assertTrue(rider.isPassenger(), "Repair fixture rider did not mount");
            riders.add(rider);
            VehicleOperations.restraints(level, car, true);
            var rail =
                data.rails.stream()
                    .filter(candidate -> candidate.matches(car.boundFrom, car.boundTo))
                    .findFirst()
                    .orElseThrow();
            ((CoasterAnchorpointBlockEntity) level.getBlockEntity(rail.from))
                .removeAnchorPeerCurve(level, rail.to);
          }
        });
    helper.runAtTickTime(
        115,
        () -> {
          helper.assertTrue(
              riders.stream().allMatch(net.minecraft.world.entity.Entity::isPassenger),
              "Support loss displaced a rider before repair");
          for (var car : cars) {
            helper.assertTrue(
                car.stopping
                    && car.restraints
                    && VehicleOperations.MATCHING_SUPPORT_LOST.equals(car.fault)
                    && car.carriers.stream().allMatch(carrier -> carrier.capture),
                "Support loss did not retain a serviceable captured car for " + car.variant);
            boolean rejected = false;
            try {
              VehicleOperations.dispatch(level, car);
            } catch (IllegalArgumentException expected) {
              rejected = true;
            }
            helper.assertTrue(rejected, "Support-loss vehicle dispatched before explicit repair");
          }
          for (var rail : rails) {
            player.setPos(rail.to.getX() + 2, rail.to.getY() + 1, rail.to.getZ());
            player.setItemInHand(
                InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
            TrackPlacement.connect(
                level,
                rail.from,
                rail.to,
                TrackDesign.byVariant(rail.variant),
                player,
                player.getMainHandItem());
          }
        });
    helper.runAtTickTime(
        135,
        () -> {
          for (var car : cars) VehicleOperations.repairMatchingSupport(level, car);
          for (var car : cars) {
            helper.assertTrue(
                car.stopping
                    && car.restraints
                    && car.fault.isEmpty()
                    && car.alignmentRequired
                    && car.carriers.stream().allMatch(carrier -> carrier.capture),
                "Repair did not retain a stopped car pending realignment for " + car.variant);
            var rail =
                data.rails.stream()
                    .filter(candidate -> candidate.matches(car.boundFrom, car.boundTo))
                    .findFirst()
                    .orElseThrow();
            helper.assertTrue(
                rail.valid && TrackGeometry.resolve(level, rail) != null,
                "Exact matching support did not become valid again for " + car.variant);
            boolean rejected = false;
            try {
              VehicleOperations.dispatch(level, car);
            } catch (IllegalArgumentException expected) {
              rejected = expected.getMessage().contains("Realign matching track");
            }
            helper.assertTrue(
                rejected, "Repaired vehicle dispatched before native realignment for " + car.variant);
          }
        });
    helper.runAtTickTime(
        155,
        () -> {
          for (var car : cars) {
            VehicleOperations.realignMatchingSupport(level, car);
            helper.assertTrue(
                car.stopping
                    && car.restraints
                    && car.fault.isEmpty()
                    && !car.alignmentRequired
                    && car.carriers.stream().allMatch(c -> c.capture),
                "Realignment changed retained safety state for " + car.variant);
          }
          helper.assertTrue(
              riders.stream().allMatch(net.minecraft.world.entity.Entity::isPassenger),
              "Realignment duplicated or displaced a retained rider");
        });
    helper.runAtTickTime(
        235,
        () -> {
          for (var car : cars) {
            var chassis = (ServerSubLevel) container.getSubLevel(car.chassis);
            var bearingPlot = CoasterCartTrackSnap.representativeBearingPlotPos(chassis);
            helper.assertTrue(bearingPlot != null, "Realigned vehicle lost its native bearing");
            var bearing =
                chassis
                    .logicalPose()
                    .transformPosition(net.minecraft.world.phys.Vec3.atCenterOf(bearingPlot));
            var rail =
                data.rails.stream()
                    .filter(candidate -> candidate.matches(car.boundFrom, car.boundTo))
                    .findFirst()
                    .orElseThrow();
            var hit = CoasterPathTrackFrame.closestHitOnEdge(level, TrackGeometry.resolve(level, rail), bearing);
            helper.assertTrue(
                hit != null,
                "Native bearing has no matching-track point after realignment for " + car.variant);
            if (hit == null) return;
            var basis = CoasterPathTrackFrame.trackBasisAtHit(level, hit);
            var expectedBearing = hit.point().add(basis.up().normalize().scale(.3125));
            helper.assertTrue(
                hit != null && expectedBearing.distanceToSqr(bearing) < .0025,
                "Native bearing did not physically realign to matching track for " + car.variant);
            helper.assertTrue(
                Boarding.ready(level, car),
                "Realignment did not leave a stopped ready vehicle for " + car.variant);
            if (car.family == dev.fourthdimension.contract.Family.PAX) {
              boolean reachedWinchGate = false;
              try {
                VehicleOperations.dispatch(level, car);
              } catch (IllegalArgumentException expected) {
                reachedWinchGate = expected.getMessage().contains("powered shuttle winch");
              }
              helper.assertTrue(
                  reachedWinchGate,
                  "Realigned PAX did not pass matching-track dispatch interlocks");
            } else {
              VehicleOperations.dispatch(level, car);
              helper.assertTrue(
                  !car.stopping
                      && car.fault.isEmpty()
                      && car.carriers.stream().noneMatch(c -> c.capture),
                  "Realigned vehicle did not dispatch normally for " + car.variant);
            }
          }
          helper.assertTrue(
              riders.stream().allMatch(net.minecraft.world.entity.Entity::isPassenger),
              "Realignment duplicated or displaced a retained rider");
          // The dispatch assertion above proves that physical realignment clears the native
          // interlock. Recapture before the lifecycle boundary so holding-store reload checks
          // the safe, stopped condition operators are expected to preserve during service.
          for (var car : cars)
            if (car.family != dev.fourthdimension.contract.Family.PAX)
              VehicleOperations.capture(level, car);
        });
    helper.runAtTickTime(
        300,
        () -> {
          for (var car : cars) {
            helper.assertTrue(
                Boarding.ready(level, car)
                    && car.stopping
                    && car.restraints
                    && car.fault.isEmpty()
                    && !car.alignmentRequired,
                "Recaptured realigned vehicle was not ready for holding reload " + car.variant);
            var saved =
                FoundationData.load(
                        data.save(new net.minecraft.nbt.CompoundTag(), level.registryAccess()),
                        level.registryAccess())
                    .vehicles
                    .get(car.chassis);
            helper.assertTrue(
                saved != null
                    && saved.stopping
                    && saved.restraints
                    && saved.fault.isEmpty()
                    && !saved.alignmentRequired,
                "Realigned support state did not survive save before holding reload " + car.variant);
            var chassis = (ServerSubLevel) container.getSubLevel(car.chassis);
            helper.assertTrue(chassis != null, "Realigned chassis missing before holding reload");
            var members = new ArrayList<java.util.UUID>();
            members.add(car.chassis);
            car.carriers.forEach(carrier -> members.add(carrier.body));
            container
                .getHoldingChunkMap()
                .moveToUnloaded(
                    chassis,
                    new net.minecraft.world.level.ChunkPos(
                        net.minecraft.core.BlockPos.containing(chassis.logicalPose().position().x,
                            chassis.logicalPose().position().y, chassis.logicalPose().position().z)));
            helper.assertTrue(
                members.stream()
                    .allMatch(
                        id ->
                            container.getSubLevel(id) == null
                                && container.getHoldingChunkMap().getHoldingSubLevel(id) != null),
                "Realigned vehicle did not enter the native holding store " + car.variant);
          }
        });
    helper.runAtTickTime(
        370,
        () -> {
          for (var car : cars) {
            helper.assertTrue(
                container.getSubLevel(car.chassis) != null
                    && car.carriers.stream().allMatch(carrier -> container.getSubLevel(carrier.body) != null)
                    && Boarding.ready(level, car)
                    && car.stopping
                    && car.restraints
                    && car.fault.isEmpty()
                    && !car.alignmentRequired,
                "Realigned vehicle did not restore its safe native state " + car.variant);
          }
          helper.assertTrue(
              riders.stream().allMatch(net.minecraft.world.entity.Entity::isPassenger),
              "Holding reload displaced a rider after physical realignment");
          System.out.println(
              "FD_SUPPORT_REPAIR families="
                  + cars.stream().map(car -> car.family).toList()
                  + " riders="
                  + riders.size()
                  + " exactGeometryOnly=true noAutoResume=true realignmentRequiredBeforeDispatch=true"
                  + " nativeReengagement=true normalDispatch=true postRepairHoldingReload=true");
          helper.succeed();
        });
  }
}
