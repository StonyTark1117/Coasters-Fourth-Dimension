package dev.fourthdimension.gametest;

import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;

/** Exercises native factory/snap paths for every original family, including locked carriers. */
@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class FamilyEligibilityGameTests {
  @GameTest(template = "empty", timeoutTicks = 220)
  public static void fiveFamilyFactoryLockedSnapAndBindingMatrix(GameTestHelper helper) {
    var level = helper.getLevel();
    for (int x = -51; x <= -43; x++)
      for (int z = -41; z <= -37; z++) level.setChunkForced(x, z, true);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000901"), "FDFamilyMatrix"));
    player.setGameMode(GameType.CREATIVE);
    var data = FoundationData.get(level);
    var container = SubLevelContainer.getContainer(level);
    var rails = new ArrayList<FoundationData.Rail>();
    var hits = new ArrayList<CoasterPathTrackFrame.GraphHit>();
    var vehicles = new ArrayList<FoundationData.Vehicle>();
    for (var design : TrackDesign.values()) {
      var from = new BlockPos(-800 + design.ordinal() * 24, 120, -640);
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
            hits.add(
                new CoasterPathTrackFrame.GraphHit(
                    CoasterPathGraphManager.sampleEdgePosition(level, edge, .5), edge, .5));
          }
          for (int i = 0; i < TrackDesign.values().length; i++) {
            var design = TrackDesign.values()[i];
            for (int j = 0; j < hits.size(); j++) {
              if (i == j) continue;
              var wrong = hits.get(j);
              int count = data.vehicles.size();
              EditorGameTests.rejects(
                  helper,
                  () -> VehicleFactory.create(level, design, wrong),
                  "Wrong-family factory accepted " + design + " on " + TrackDesign.values()[j]);
              helper.assertTrue(
                  data.vehicles.size() == count, "Rejected factory leaked vehicle state");
            }
            vehicles.add(VehicleFactory.create(level, design, hits.get(i)));
          }
        });
    helper.runAtTickTime(
        100,
        () -> {
          int decisions = 0;
          for (int i = 0; i < vehicles.size(); i++) {
            var vehicle = vehicles.get(i);
            helper.assertTrue(
                Boarding.ready(level, vehicle), "Matrix vehicle failed boarding capture");
            VehicleOperations.restraints(level, vehicle, true);
            var body = (ServerSubLevel) container.getSubLevel(vehicle.chassis);
            var from = vehicle.boundFrom;
            var to = vehicle.boundTo;
            for (int j = 0; j < hits.size(); j++) {
              boolean matching = i == j;
              var hit = hits.get(j);
              helper.assertTrue(
                  EligibilityHooks.allows(body, level, hit) == matching,
                  "Locked family eligibility mismatch " + i + "/" + j);
              helper.assertTrue(
                  (CoasterCartRailSnapEligibility.filterHitForBearing(body, level, hit) != null)
                      == matching,
                  "Native snap filter mismatch " + i + "/" + j);
              EligibilityHooks.bindAccepted(body, level, hit);
              helper.assertTrue(
                  vehicle.boundFrom.equals(from) && vehicle.boundTo.equals(to),
                  "Rejected hit stole the persisted track binding");
              if (!matching) {
                CoasterCartTrackSnap.applyInitialSnapAfterSpawn(body, level, hit);
                helper.assertTrue(
                    CoasterCartTrackSnap.isBearingDisengaged(body),
                    "Native initial snap bypassed family restriction " + i + "/" + j);
              }
              decisions++;
            }
            CoasterCartTrackSnap.applyInitialSnapAfterSpawn(body, level, hits.get(i));
            // The public initial-snap method sets the block immediately; its internal
            // disengaged cache is cleared by the next native pre-physics tick.
            var bearing = CoasterCartTrackSnap.representativeBearingPlotPos(body);
            helper.assertTrue(
                bearing != null
                    && level.getBlockState(bearing).getValue(CoasterCartBlock.SNAP_ENGAGED),
                "Matching initial snap failed after rejects");
          }
          int mixedConsistRejects = 0;
          for (int i = 0; i < vehicles.size(); i++) {
            var first = vehicles.get(i);
            var firstBody = (ServerSubLevel) container.getSubLevel(first.chassis);
            var firstFrom = first.boundFrom;
            var firstTo = first.boundTo;
            for (int j = 0; j < vehicles.size(); j++) {
              if (i == j) continue;
              var second = vehicles.get(j);
              var secondBody = (ServerSubLevel) container.getSubLevel(second.chassis);
              var secondFrom = second.boundFrom;
              var secondTo = second.boundTo;
              helper.assertTrue(
                  !CoasterCartTrainLinkConstraint.linkCarts(level, firstBody, secondBody, 6),
                  "Mixed-family native coupling accepted " + first.family + " -> " + second.family);
              helper.assertTrue(
                  !CoasterCartTrainLinkConstraint.areLinkedCarts(firstBody, secondBody)
                      && first.boundFrom.equals(firstFrom)
                      && first.boundTo.equals(firstTo)
                      && second.boundFrom != null
                      && second.boundTo != null
                      && second.boundFrom.equals(secondFrom)
                      && second.boundTo.equals(secondTo),
                  "Rejected mixed coupling changed a vehicle state");
              mixedConsistRejects++;
            }
          }
          System.out.println(
              "FD_FAMILY_MATRIX decisions="
                  + decisions
                  + " factoryRejects=20 lockedNativeInitialSnapRejects=20 mixedConsistRejects="
                  + mixedConsistRejects
                  + " bindingPreserved=true");
          for (var vehicle : vehicles) {
            for (var carrier : vehicle.carriers)
              container.removeSubLevel(
                  container.getSubLevel(carrier.body), SubLevelRemovalReason.REMOVED);
            container.removeSubLevel(
                container.getSubLevel(vehicle.chassis), SubLevelRemovalReason.REMOVED);
            data.vehicles.remove(vehicle.chassis);
          }
          for (var rail : rails) {
            ((dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity)
                    level.getBlockEntity(rail.from))
                .removeAnchorPeerCurve(level, rail.to);
            data.rails.remove(rail);
            level.removeBlock(rail.from, false);
            level.removeBlock(rail.to, false);
          }
          helper.succeed();
        });
  }
}
