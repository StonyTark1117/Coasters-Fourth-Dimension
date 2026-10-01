package dev.fourthdimension.gametest;

import dev.fourthdimension.integration.*;
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
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;

/** Exercises native factory/snap paths for every original family, including locked carriers. */
@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class FamilyEligibilityGameTests {
  private static final List<TrackAppearance> APPEARANCES =
      List.of(
          TrackAppearance.ORIGINAL,
          new TrackAppearance(
              AppearanceProviders.TRACK_STYLES, "coasterssimulatedextratypes:arrow_4d"),
          new TrackAppearance(
              AppearanceProviders.TRACK_STYLES, "coasterssimulatedextratypes:arrow_4d_dbl_spine"),
          new TrackAppearance("missing:appearance", "missing:style"));
  private static final List<String> UNAVAILABLE_PROVIDERS =
      List.of("missing:functional", AppearanceProviders.TRACK_STYLES);

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
    var neighbors = new ArrayList<List<FoundationData.Rail>>();
    var allRails = new ArrayList<FoundationData.Rail>();
    var hits = new ArrayList<CoasterPathTrackFrame.GraphHit>();
    var vehicles = new ArrayList<FoundationData.Vehicle>();
    for (var design : TrackDesign.values()) {
      var from = new BlockPos(-800 + design.ordinal() * 24, 120, -640);
      var to = from.offset(0, 0, 32);
      var lower = from.offset(0, 0, -16);
      var upper = to.offset(0, 0, 16);
      for (var anchor : List.of(lower, from, to, upper))
        level.setBlockAndUpdate(
            anchor, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
      player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
      rails.add(TrackPlacement.connect(level, from, to, design, player, player.getMainHandItem()));
      var adjacent = new ArrayList<FoundationData.Rail>();
      for (var pair : List.of(List.of(lower, from), List.of(to, upper))) {
        var end = pair.getLast();
        player.setPos(end.getX() + 2, end.getY() + 1, end.getZ());
        adjacent.add(
            TrackPlacement.connect(
                level, pair.getFirst(), end, design, player, player.getMainHandItem()));
      }
      neighbors.add(adjacent);
      allRails.add(rails.getLast());
      allRails.addAll(adjacent);
    }
    rails.get(TrackDesign.FREE_SPIN.ordinal()).magnets =
        List.of(
            new dev.fourthdimension.physics.MagneticSpan(
                2, 28, dev.fourthdimension.physics.MagneticSpan.Mode.DAMP, 1, 5, .1, 3));
    helper.runAtTickTime(
        20,
        () -> {
          for (var rail : rails) {
            var edge = TrackGeometry.resolve(level, rail);
            hits.add(
                new CoasterPathTrackFrame.GraphHit(
                    CoasterPathGraphManager.sampleEdgePosition(level, edge, .5), edge, .5));
          }
          // Exercise removal/misidentification before any matching vehicle occupies the site.
          // Otherwise an unrelated axle-clearance rejection could hide a provider bypass.
          for (int i = 0; i < rails.size(); i++) {
            var design = TrackDesign.values()[i];
            var rail = rails.get(i);
            var hit = hits.get(i);
            int index = data.rails.indexOf(rail);
            for (var provider : UNAVAILABLE_PROVIDERS) {
              data.rails.set(index, withProvider(rail, provider));
              try {
                int count = data.vehicles.size();
                int bodies = container.getAllSubLevels().size();
                EditorGameTests.rejects(
                    helper,
                    () -> VehicleFactory.create(level, design, hit),
                    "Factory accepted unavailable functional provider "
                        + provider
                        + " for "
                        + design);
                helper.assertTrue(
                    data.vehicles.size() == count && container.getAllSubLevels().size() == bodies,
                    "Unavailable provider allocated a vehicle or native body");
                itemPlacement(helper, player, design, hit, false);
              } finally {
                data.rails.set(index, rail);
              }
            }
            data.rails.remove(index);
            try {
              EditorGameTests.rejects(
                  helper,
                  () -> VehicleFactory.create(level, design, hit),
                  "Ordinary native geometry granted 4D factory access");
              itemPlacement(helper, player, design, hit, false);
            } finally {
              data.rails.add(index, rail);
            }
          }
          for (var appearance : APPEARANCES) {
            setAppearance(helper, rails, appearance);
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
                itemPlacement(helper, player, design, wrong, false);
              }
            }
          }
          setAppearance(helper, rails, TrackAppearance.ORIGINAL);
          for (int i = 0; i < TrackDesign.values().length; i++) {
            itemPlacement(helper, player, TrackDesign.values()[i], hits.get(i), true);
            var rail = rails.get(i);
            vehicles.add(
                data.vehicles.values().stream()
                    .filter(v -> rail.matches(v.boundFrom, v.boundTo))
                    .findFirst()
                    .orElseThrow());
          }
        });
    helper.runAtTickTime(
        100,
        () -> {
          int decisions = 0;
          for (var vehicle : vehicles) {
            helper.assertTrue(
                Boarding.ready(level, vehicle), "Matrix vehicle failed boarding capture");
            VehicleOperations.restraints(level, vehicle, true);
          }
          for (var appearance : APPEARANCES) {
            setAppearance(helper, rails, appearance);
            for (int i = 0; i < vehicles.size(); i++) {
              var vehicle = vehicles.get(i);
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
          }
          connectedMatrix(helper, rails, neighbors, allRails, vehicles, hits);
          for (int i = 0; i < vehicles.size(); i++) {
            var vehicle = vehicles.get(i);
            var body = (ServerSubLevel) container.getSubLevel(vehicle.chassis);
            var rail = rails.get(i);
            int index = data.rails.indexOf(rail);
            for (var provider : UNAVAILABLE_PROVIDERS) {
              data.rails.set(index, withProvider(rail, provider));
              try {
                denied(helper, body, vehicle, hits.get(i));
              } finally {
                data.rails.set(index, rail);
              }
            }
            data.rails.remove(index);
            try {
              denied(helper, body, vehicle, hits.get(i));
            } finally {
              data.rails.add(index, rail);
            }
            CoasterCartTrackSnap.applyInitialSnapAfterSpawn(body, level, hits.get(i));
          }
          setAppearance(helper, rails, TrackAppearance.ORIGINAL);
          System.out.println(
              "FD_FAMILY_MATRIX decisions="
                  + decisions
                  + " factoryRejects=80 lockedNativeInitialSnapRejects=80 bindingPreserved=true");
          System.out.println(
              "FD_APPEARANCE_MATRIX phases=4 families=5 unavailableProviderFactoryRejects=10"
                  + " ordinaryFactoryRejects=5 unavailableProviderNativeRejects=10"
                  + " ordinaryNativeRejects=5 survivalItemRejects=95 survivalItemPlacements=5"
                  + " savedMechanicsPreserved=true stylesAvailable="
                  + (AppearanceProviders.choices(TrackDesign.ARROW.family).size() == 3));
          for (var vehicle : vehicles) {
            for (var carrier : vehicle.carriers)
              container.removeSubLevel(
                  container.getSubLevel(carrier.body), SubLevelRemovalReason.REMOVED);
            container.removeSubLevel(
                container.getSubLevel(vehicle.chassis), SubLevelRemovalReason.REMOVED);
            data.vehicles.remove(vehicle.chassis);
          }
          for (var rail : allRails) {
            ((dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity)
                    level.getBlockEntity(rail.from))
                .removeAnchorPeerCurve(level, rail.to);
            data.rails.remove(rail);
          }
          for (var rail : allRails) {
            level.removeBlock(rail.from, false);
            level.removeBlock(rail.to, false);
          }
          helper.succeed();
        });
  }

  private static void connectedMatrix(
      GameTestHelper helper,
      List<FoundationData.Rail> rails,
      List<List<FoundationData.Rail>> neighbors,
      List<FoundationData.Rail> allRails,
      List<FoundationData.Vehicle> vehicles,
      List<CoasterPathTrackFrame.GraphHit> homeHits) {
    var level = helper.getLevel();
    var data = FoundationData.get(level);
    var container = SubLevelContainer.getContainer(level);
    int accepted = 0, rejected = 0;
    for (var appearance : APPEARANCES) {
      setAppearance(helper, allRails, appearance);
      for (int i = 0; i < vehicles.size(); i++) {
        var vehicle = vehicles.get(i);
        var body = (ServerSubLevel) container.getSubLevel(vehicle.chassis);
        var home = rails.get(i);
        helper.assertTrue(
            home.matches(vehicle.boundFrom, vehicle.boundTo),
            "Connected matrix lost its initial binding");
        for (int side = 0; side < 2; side++) {
          var original = neighbors.get(i).get(side);
          var shared = side == 0 ? home.from : home.to;
          helper.assertTrue(
              original.from.equals(shared) || original.to.equals(shared),
              "Family probe is disconnected from its original rail");
          var edge = TrackGeometry.resolve(level, original);
          var hit =
              new CoasterPathTrackFrame.GraphHit(
                  CoasterPathGraphManager.sampleEdgePosition(level, edge, .5), edge, .5);
          int index = data.rails.indexOf(original);
          for (var design : TrackDesign.values()) {
            // Model a persisted or externally supplied mixed junction. Normal track placement
            // refuses mixed-family junctions; the native re-engagement guard must also reject them.
            var candidate =
                new FoundationData.Rail(
                    original.from,
                    original.to,
                    design.family,
                    original.profile,
                    original.id,
                    design.variant,
                    original.geometryHash,
                    original.revision,
                    true,
                    original.provider);
            candidate.appearance = appearance;
            data.rails.set(index, candidate);
            try {
              helper.assertTrue(
                  TrackProviders.available(candidate)
                      && TrackGeometry.resolve(level, candidate) != null,
                  "Connected probe has an unavailable functional provider or geometry");
              boolean matching = vehicle.family == design.family;
              helper.assertTrue(
                  EligibilityHooks.allows(body, level, hit) == matching,
                  "Connected family eligibility mismatch " + vehicle.family + "/" + design.family);
              helper.assertTrue(
                  (CoasterCartRailSnapEligibility.filterHitForBearing(body, level, hit) != null)
                      == matching,
                  "Connected native filter mismatch " + vehicle.family + "/" + design.family);
              var before = new org.joml.Vector3d(body.logicalPose().position());
              EligibilityHooks.bindAccepted(body, level, hit);
              CoasterCartTrackSnap.applyInitialSnapAfterSpawn(body, level, hit);
              if (matching) {
                var bearing = CoasterCartTrackSnap.representativeBearingPlotPos(body);
                helper.assertTrue(
                    candidate.matches(vehicle.boundFrom, vehicle.boundTo)
                        && bearing != null
                        && level.getBlockState(bearing).getValue(CoasterCartBlock.SNAP_ENGAGED),
                    "Matching connected snap failed to advance the native binding");
                accepted++;
              } else {
                helper.assertTrue(
                    home.matches(vehicle.boundFrom, vehicle.boundTo)
                        && CoasterCartTrackSnap.isBearingDisengaged(body)
                        && before.distance(body.logicalPose().position()) < 1e-8,
                    "Wrong-family connected snap changed binding, pose or engagement");
                rejected++;
              }
              System.out.println(
                  "FD_CONNECTED_FAMILY car="
                      + vehicle.family.id()
                      + " track="
                      + design.family.id()
                      + " side="
                      + (side == 0 ? "from" : "to")
                      + " appearance="
                      + appearance.style()
                      + " accepted="
                      + matching);
            } finally {
              data.rails.set(index, original);
              EligibilityHooks.bindAccepted(body, level, homeHits.get(i));
              CoasterCartTrackSnap.applyInitialSnapAfterSpawn(body, level, homeHits.get(i));
            }
            helper.assertTrue(
                home.matches(vehicle.boundFrom, vehicle.boundTo),
                "Connected probe failed to restore its original binding");
          }
        }
      }
    }
    setAppearance(helper, allRails, TrackAppearance.ORIGINAL);
    helper.assertTrue(accepted == 40 && rejected == 160, "Incomplete connected family matrix");
    System.out.println(
        "FD_CONNECTED_MATRIX cases=200 matchingAccepted="
            + accepted
            + " wrongFamilyRejected="
            + rejected
            + " endpoints=2 appearances=4 bindingRestored=true");
  }

  private static void itemPlacement(
      GameTestHelper helper,
      net.neoforged.neoforge.common.util.FakePlayer player,
      TrackDesign design,
      CoasterPathTrackFrame.GraphHit hit,
      boolean allowed) {
    var item =
        switch (design) {
          case ARROW -> TrackItems.ARROW_CAR.get();
          case LEGACY -> TrackItems.LEGACY_CAR.get();
          case FREE_SPIN -> TrackItems.FREE_SPIN_CAR.get();
          case REVISED -> TrackItems.REVISED_CAR.get();
          case PAX -> TrackItems.PAX_CAR.get();
        };
    var level = helper.getLevel();
    int vehicles = FoundationData.get(level).vehicles.size();
    int bodies = SubLevelContainer.getContainer(level).getAllSubLevels().size();
    player.setGameMode(GameType.SURVIVAL);
    player.setPos(hit.point().x + 2, hit.point().y + 1, hit.point().z);
    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item, 2));
    var result =
        item.useOn(
            new UseOnContext(
                player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                    hit.point(), Direction.UP, BlockPos.containing(hit.point()), false)));
    helper.assertTrue(
        result == (allowed ? InteractionResult.SUCCESS : InteractionResult.FAIL),
        "Survival item placement result mismatch for " + design);
    helper.assertTrue(
        player.getMainHandItem().getCount() == (allowed ? 1 : 2)
            && FoundationData.get(level).vehicles.size() == vehicles + (allowed ? 1 : 0),
        "Survival item consumption or vehicle allocation mismatch");
    if (!allowed)
      helper.assertTrue(
          SubLevelContainer.getContainer(level).getAllSubLevels().size() == bodies,
          "Rejected item placement leaked native bodies");
    player.setGameMode(GameType.CREATIVE);
  }

  private static FoundationData.Rail withProvider(FoundationData.Rail rail, String provider) {
    var copy =
        new FoundationData.Rail(
            rail.from,
            rail.to,
            rail.family,
            rail.profile,
            rail.id,
            rail.variant,
            rail.geometryHash,
            rail.revision,
            rail.valid,
            provider);
    copy.magnets = rail.magnets;
    copy.appearance = APPEARANCES.get(1);
    return copy;
  }

  private static void denied(
      GameTestHelper helper,
      ServerSubLevel body,
      FoundationData.Vehicle vehicle,
      CoasterPathTrackFrame.GraphHit hit) {
    var level = helper.getLevel();
    var from = vehicle.boundFrom;
    var to = vehicle.boundTo;
    helper.assertTrue(
        !EligibilityHooks.allows(body, level, hit)
            && CoasterCartRailSnapEligibility.filterHitForBearing(body, level, hit) == null,
        "Unavailable functional track passed native snap filter");
    EligibilityHooks.bindAccepted(body, level, hit);
    CoasterCartTrackSnap.applyInitialSnapAfterSpawn(body, level, hit);
    helper.assertTrue(
        CoasterCartTrackSnap.isBearingDisengaged(body)
            && from.equals(vehicle.boundFrom)
            && to.equals(vehicle.boundTo),
        "Unavailable functional track changed binding or accepted initial snap");
  }

  private static void setAppearance(
      GameTestHelper helper, List<FoundationData.Rail> rails, TrackAppearance appearance) {
    var fixture = new FoundationData();
    fixture.rails.addAll(rails);
    var registries = helper.getLevel().registryAccess();
    var before = fixture.save(new CompoundTag(), registries);
    for (var rail : rails) {
      rail.appearance = appearance;
      var definition = AppearanceProviders.definition(appearance);
      var expected =
          definition != null && definition.families().contains(rail.family)
              ? appearance
              : TrackAppearance.ORIGINAL;
      helper.assertTrue(
          appearance.effective(rail.family).equals(expected),
          "Unavailable or wrong-family appearance failed to fall back");
    }
    var saved = fixture.save(new CompoundTag(), registries);
    var restored = FoundationData.load(saved, registries);
    helper.assertTrue(
        saved.equals(restored.save(new CompoundTag(), registries)),
        "Appearance round trip changed saved rail data");
    var originalRails = before.getList("rails", Tag.TAG_COMPOUND);
    var changedRails = saved.getList("rails", Tag.TAG_COMPOUND);
    for (int i = 0; i < originalRails.size(); i++) {
      var original = originalRails.getCompound(i);
      var changed = changedRails.getCompound(i);
      for (var key : List.of("appearance_provider", "appearance_style")) {
        original.remove(key);
        changed.remove(key);
      }
      helper.assertTrue(original.equals(changed), "Appearance changed functional/mechanical data");
    }
  }
}
