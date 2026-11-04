package dev.fourthdimension.gametest;

import dev.fourthdimension.integration.*;
import dev.fourthdimension.physics.*;
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
import net.minecraft.nbt.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class ProviderGameTests {
  @GameTest(template = "empty", timeoutTicks = 40)
  public static void providerPersistencePreservesUnknownMechanicsAndRecovery(
      GameTestHelper helper) {
    var access = helper.getLevel().registryAccess();
    var data = new FoundationData();
    for (var design : TrackDesign.values()) {
      var rail =
          new FoundationData.Rail(
              new BlockPos(design.ordinal() * 24, 180, 900),
              new BlockPos(design.ordinal() * 24, 180, 932),
              design.family,
              new RotationProfile(
                  List.of(new RotationProfile.Key(0, 0, 0), new RotationProfile.Key(32, 0, 0))),
              UUID.randomUUID(),
              design.variant,
              1234,
              7,
              true,
              "missing:functional");
      rail.appearance = new TrackAppearance("missing:decoration", 0x12ab34);
      if (design == TrackDesign.FREE_SPIN)
        rail.magnets = List.of(new MagneticSpan(2, 8, MagneticSpan.Mode.INDUCE, 1, 4, .2, 15));
      data.rails.add(rail);
    }
    var saved = data.save(new CompoundTag(), access);
    var loaded = FoundationData.load(saved, access);
    helper.assertTrue(
        saved.equals(loaded.save(new CompoundTag(), access)),
        "Unknown provider roundtrip changed mechanical, appearance or magnetic data");
    for (var rail : loaded.rails)
      helper.assertTrue(
          !rail.original()
              && !TrackProviders.available(rail)
              && TrackGeometry.resolve(helper.getLevel(), rail) == null,
          "Unknown functional identity acquired original ownership or mechanics");
    var buf =
        new net.minecraft.network.RegistryFriendlyByteBuf(
            io.netty.buffer.Unpooled.buffer(), access);
    try {
      FoundationSync.Snapshot.CODEC.encode(
          buf, new FoundationSync.Snapshot(helper.getLevel().dimension().location(), saved));
      var snapshot = FoundationSync.Snapshot.CODEC.decode(buf);
      helper.assertTrue(
          saved.equals(FoundationData.load(snapshot.tag(), access).save(new CompoundTag(), access)),
          "Client snapshot lost unknown provider identity");
    } finally {
      buf.release();
    }
    for (int schema = 1; schema <= 6; schema++) {
      var legacy = saved.copy();
      legacy.putInt("schema", schema);
      for (var raw : legacy.getList("rails", Tag.TAG_COMPOUND)) {
        var tag = (CompoundTag) raw;
        tag.remove("provider");
      }
      var migrated = FoundationData.load(legacy, access);
      helper.assertTrue(
          migrated.rails.stream().allMatch(r -> r.original() && TrackProviders.available(r)),
          "Legacy native provider defaults changed: schema " + schema);
      helper.assertTrue(
          migrated.rails.get(2).magnets.equals(data.rails.get(2).magnets),
          "Legacy magnetic data changed");
    }
    for (var id :
        List.of(
            "bad identity", "missing_namespace", "x:" + "a".repeat(256), FoundationData.PROVIDER)) {
      var invalid = saved.copy();
      invalid.getList("rails", Tag.TAG_COMPOUND).getCompound(0).putString("provider", id);
      EditorGameTests.rejects(
          helper, () -> FoundationData.load(invalid, access), "Invalid identity accepted: " + id);
    }
    helper.assertTrue(
        ProviderFixtures.absentSkipped
            && ProviderFixtures.versionSkipped
            && ProviderFixtures.exactAccepted
            && ProviderFixtures.factories == 2
            && ProviderFixtures.failedFactorySkipped
            && ProviderFixtures.duplicateRejected
            && ProviderFixtures.reservedRejected,
        "Optional release gate loaded a rejected factory or registration checks failed");
    boolean frozen = false;
    try {
      TrackProviders.register("fdtest:late", null);
    } catch (IllegalStateException expected) {
      frozen = true;
    }
    helper.assertTrue(frozen, "Registry remained mutable after startup");
    System.out.println(
        "FD_PROVIDER persistence families=5 unknownPreserved=true magnetic=true appearance=true"
            + " packet=true legacySchemas=6 invalidRejected=4 exactFactoryCalls=2"
            + " failedFactoryIsolated=true frozen=true");
    helper.succeed();
  }

  @GameTest(template = "empty", batch = "fourthdimension_providers", timeoutTicks = 180)
  public static void registeredProvidersGateNativePlacementAndSnapping(GameTestHelper helper) {
    var level = helper.getLevel();
    var data = FoundationData.get(level);
    var container = SubLevelContainer.getContainer(level);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000419"), "FDProviders"));
    player.setGameMode(GameType.CREATIVE);
    player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
    var rails = new ArrayList<FoundationData.Rail>();
    var vehicles = new ArrayList<FoundationData.Vehicle>();
    var hits = new ArrayList<CoasterPathTrackFrame.GraphHit>();
    for (var design : TrackDesign.values()) {
      var from = new BlockPos(-850 + 24 * design.ordinal(), 180, 900);
      var to = from.offset(0, 0, 32);
      for (var pos : List.of(from, to)) {
        level.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
        level.setBlockAndUpdate(
            pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      }
      player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
      var nativeRail =
          TrackPlacement.connect(level, from, to, design, player, player.getMainHandItem());
      var rail = withProvider(nativeRail, ProviderFixtures.GOOD);
      data.rails.set(data.rails.indexOf(nativeRail), rail);
      rails.add(rail);
    }
    helper.runAtTickTime(
        20,
        () -> {
          for (int i = 0; i < rails.size(); i++) {
            var rail = rails.get(i);
            var edge = TrackGeometry.resolve(level, rail);
            helper.assertTrue(
                edge != null && !rail.original(),
                "Registered provider resolution/ownership failed");
            var hit =
                new CoasterPathTrackFrame.GraphHit(
                    CoasterPathGraphManager.sampleEdgePosition(level, edge, .5), edge, .5);
            hits.add(hit);
            vehicles.add(VehicleFactory.create(level, TrackDesign.values()[i], hit));
          }
        });
    helper.runAtTickTime(
        110,
        () -> {
          int denied = 0;
          for (int i = 0; i < rails.size(); i++) {
            var rail = rails.get(i);
            var vehicle = vehicles.get(i);
            var design = TrackDesign.values()[i];
            var body = (ServerSubLevel) container.getSubLevel(vehicle.chassis);
            var hit = hits.get(i);
            helper.assertTrue(
                Boarding.ready(level, vehicle), "Registered provider car failed capture");
            VehicleOperations.restraints(level, vehicle, true);
            helper.assertTrue(
                CoasterCartRailSnapEligibility.filterHitForBearing(body, level, hit) != null,
                "Registered provider native snap rejected");
            if (design != TrackDesign.PAX) VehicleOperations.validateDispatch(level, vehicle);
            var recovery = UUID.randomUUID();
            vehicle.recoveryCart = recovery;
            var saved = data.save(new CompoundTag(), level.registryAccess());
            var loaded = FoundationData.load(saved, level.registryAccess());
            helper.assertTrue(
                recovery.equals(loaded.vehicles.get(vehicle.chassis).recoveryCart),
                "Provider save lost newer recovery state");
            vehicle.recoveryCart = null;
            var modes = new ArrayList<String>(List.of(ProviderFixtures.BAD));
            modes.add("absent");
            for (var mode : modes) {
              var rejected = withProvider(rail, "fdtest:" + mode);
              int index = data.rails.indexOf(rail);
              data.rails.set(index, rejected);
              try {
                helper.assertTrue(
                    TrackGeometry.resolve(level, rejected) == null,
                    "Invalid provider acquired geometry: " + mode);
                helper.assertTrue(
                    CoasterCartRailSnapEligibility.filterHitForBearing(body, level, hit) == null,
                    "Native snap accepted invalid provider: " + mode);
                EditorGameTests.rejects(
                    helper,
                    () -> VehicleFactory.create(level, design, hit),
                    "Factory accepted invalid provider: " + mode);
                if (design != TrackDesign.PAX)
                  EditorGameTests.rejects(
                      helper,
                      () -> VehicleOperations.validateDispatch(level, vehicle),
                      "Dispatch accepted invalid provider: " + mode);
                var roundtrip =
                    FoundationData.load(
                        data.save(new CompoundTag(), level.registryAccess()),
                        level.registryAccess());
                helper.assertTrue(
                    roundtrip.rails.stream()
                        .anyMatch(
                            r ->
                                r.id.equals(rail.id)
                                    && r.provider.equals(rejected.provider)
                                    && r.valid),
                    "Unavailable provider lost identity or geometry intent");
                denied++;
              } finally {
                data.rails.set(index, rail);
              }
            }
            helper.assertTrue(
                CoasterCartRailSnapEligibility.filterHitForBearing(body, level, hit) != null,
                "Restored provider did not restore native eligibility");
            System.out.println(
                "FD_PROVIDER native family="
                    + vehicle.family
                    + " placement=true snap=true rejected=10 restored=true recovery=true");
          }
          helper.assertTrue(denied == 50, "Provider rejection matrix incomplete");
          for (var v : vehicles) {
            Boarding.recover(level, v);
            for (var c : v.carriers)
              container.removeSubLevel(
                  container.getSubLevel(c.body), SubLevelRemovalReason.REMOVED);
            container.removeSubLevel(
                container.getSubLevel(v.chassis), SubLevelRemovalReason.REMOVED);
            data.vehicles.remove(v.chassis);
          }
          for (var rail : rails) {
            data.rails.remove(rail);
            level.removeBlock(rail.from, false);
            level.removeBlock(rail.to, false);
            for (var pos : List.of(rail.from, rail.to))
              level.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, false);
          }
          System.out.println("FD_PROVIDER COMPLETE nativeFamilies=5 rejected=50");
          helper.succeed();
        });
  }

  private static FoundationData.Rail withProvider(FoundationData.Rail rail, String provider) {
    var result =
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
    result.appearance = rail.appearance;
    result.magnets = rail.magnets;
    return result;
  }
}
