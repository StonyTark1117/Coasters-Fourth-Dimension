package dev.fourthdimension.gametest;

import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.integration.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class AppearanceGameTests {
  @GameTest(template = "empty", timeoutTicks = 60)
  public static void cosmeticTransactionsMigrationAndMissingProviderPersistence(
      GameTestHelper helper) {
    var level = helper.getLevel();
    for (int x = 46; x <= 55; x++)
      for (int z = -47; z <= -44; z++) level.setChunkForced(x, z, true);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000401"), "FDAppearance"));
    player.setGameMode(GameType.CREATIVE);
    player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
    var from = new BlockPos(752, 120, -736);
    var to = from.offset(0, 0, 32);
    for (var p : List.of(from, to))
      level.setBlockAndUpdate(
          p, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
    var rail =
        TrackPlacement.connect(
            level, from, to, TrackDesign.ARROW, player, player.getMainHandItem());
    var data = FoundationData.get(level);
    var originalProfile = rail.profile;
    long originalGeometry = rail.geometryHash;
    var apply =
        new EditorNetwork.Edit(rail.id, rail.revision, 5, "fourthdimension:original;#12ABCD");
    EditorNetwork.apply(player, apply);
    helper.assertTrue(
        rail.appearance.tint() == 0x12abcd && rail.revision == apply.revision() + 1,
        "Appearance transaction failed");
    helper.assertTrue(
        rail.profile == originalProfile
            && rail.geometryHash == originalGeometry
            && rail.family == TrackDesign.ARROW.family
            && rail.variant.equals(TrackDesign.ARROW.variant),
        "Appearance changed mechanics or binding");
    EditorGameTests.rejects(
        helper, () -> EditorNetwork.apply(player, apply), "Stale appearance edit accepted");
    long revision = rail.revision;
    EditorGameTests.rejects(
        helper,
        () ->
            EditorNetwork.apply(
                player, new EditorNetwork.Edit(rail.id, revision, 5, "unknown:style;#112233")),
        "Uninspected appearance accepted");
    if (!OptionalIntegrations.installed(OptionalIntegrations.TRACK_STYLES).mayInspect())
      EditorGameTests.rejects(
          helper,
          () ->
              EditorNetwork.apply(
                  player,
                  new EditorNetwork.Edit(
                      rail.id, revision, 5, TrackAppearance.CANDIDATES.getFirst() + ";")),
          "Absent provider was selectable");
    player.setGameMode(GameType.ADVENTURE);
    EditorGameTests.rejects(
        helper,
        () ->
            EditorNetwork.apply(
                player, new EditorNetwork.Edit(rail.id, revision, 5, "fourthdimension:original;")),
        "Appearance edit ignored permissions");
    player.setGameMode(GameType.CREATIVE);
    helper.assertTrue(
        rail.revision == revision && rail.appearance.tint() == 0x12abcd,
        "Rejected appearance edit mutated state");
    for (var design : TrackDesign.values()) {
      var start = from.offset(24 + design.ordinal() * 24, 0, 0);
      var finish = start.offset(0, 0, 32);
      for (var pos : List.of(start, finish))
        level.setBlockAndUpdate(
            pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      player.setPos(finish.getX() + 2, finish.getY() + 1, finish.getZ());
      var candidate =
          TrackPlacement.connect(level, start, finish, design, player, player.getMainHandItem());
      var profile = candidate.profile;
      for (var id : TrackAppearance.CANDIDATES) {
        long before = candidate.revision;
        var edit = new EditorNetwork.Edit(candidate.id, before, 5, id + ";#001122");
        if (design == TrackDesign.ARROW
            && OptionalIntegrations.installed(OptionalIntegrations.TRACK_STYLES).mayInspect()) {
          EditorNetwork.apply(player, edit);
          helper.assertTrue(
              candidate.appearance.id().equals(id), "Installed Arrow style not selected");
          var selected = candidate.appearance;
          EditorNetwork.apply(
              player,
              new EditorNetwork.Edit(candidate.id, candidate.revision, 0, "0:0;16:1080;32:0"));
          long highRevision = candidate.revision;
          helper.assertTrue(
              !selected.supportsProfile(candidate.profile)
                  && candidate.appearance.equals(selected)
                  && Math.abs(candidate.profile.angleAt(16) - 6 * Math.PI) < 1e-9,
              "High-stroke profile was clamped or lost appearance intent");
          EditorGameTests.rejects(
              helper,
              () ->
                  EditorNetwork.apply(
                      player, new EditorNetwork.Edit(candidate.id, highRevision, 5, id + ";")),
              "Style selection accepted a conflicting control-rail stroke");
          helper.assertTrue(
              candidate.revision == highRevision, "Rejected stroke edit mutated revision");
          var strokeSnapshot = new FoundationData();
          strokeSnapshot.rails.add(candidate);
          var strokeReload =
              FoundationData.load(
                      strokeSnapshot.save(new CompoundTag(), level.registryAccess()),
                      level.registryAccess())
                  .rails
                  .getFirst();
          helper.assertTrue(
              strokeReload.appearance.equals(selected)
                  && strokeReload.profile.keys().equals(candidate.profile.keys())
                  && !strokeReload.appearance.supportsProfile(strokeReload.profile),
              "High-stroke fallback did not retain saved profile/intent");
          ProfileEditor.apply(player, candidate.id, candidate.revision, profile, false);
          helper.assertTrue(
              selected.supportsProfile(candidate.profile),
              "Lower stroke did not restore style eligibility");
        } else {
          EditorGameTests.rejects(
              helper, () -> EditorNetwork.apply(player, edit), "Unverified style/family accepted");
          helper.assertTrue(candidate.revision == before, "Rejected style/family mutated revision");
        }
        helper.assertTrue(
            candidate.profile == profile
                && candidate.family == design.family
                && candidate.variant.equals(design.variant),
            "Styling changed mechanical family");
      }
      ((dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity)
              level.getBlockEntity(start))
          .removeAnchorPeerCurve(level, finish);
      data.rails.remove(candidate);
      level.removeBlock(start, false);
      level.removeBlock(finish, false);
    }
    var snapshot = new FoundationData();
    snapshot.rails.add(rail);
    var saved = snapshot.save(new CompoundTag(), level.registryAccess());
    var restored = FoundationData.load(saved, level.registryAccess()).rails.getFirst();
    helper.assertTrue(
        restored.id.equals(rail.id)
            && restored.appearance.equals(rail.appearance)
            && restored.profile.keys().equals(rail.profile.keys()),
        "Appearance NBT changed identities or profile");
    for (int schema = 1; schema <= 5; schema++) {
      var old = saved.copy();
      old.putInt("schema", schema);
      old.getList("rails", net.minecraft.nbt.Tag.TAG_COMPOUND).getCompound(0).remove("appearance");
      var migrated = FoundationData.load(old, level.registryAccess()).rails.getFirst();
      helper.assertTrue(
          migrated.appearance.equals(TrackAppearance.DEFAULT)
              && migrated.id.equals(rail.id)
              && migrated.profile.keys().equals(rail.profile.keys()),
          "Older schema did not retain original appearance/profile");
    }
    rail.appearance = new TrackAppearance("future:missing_provider", 0x223344);
    var missing =
        FoundationData.load(
                snapshot.save(new CompoundTag(), level.registryAccess()), level.registryAccess())
            .rails
            .getFirst();
    helper.assertTrue(
        missing.appearance.equals(rail.appearance)
            && missing.family == rail.family
            && missing.variant.equals(rail.variant),
        "Provider removal erased cosmetic intent or mechanics");
    System.out.println(
        "FD_APPEARANCE schema="
            + FoundationData.SCHEMA
            + " oldSchemas=1..5 missingIntentPreserved=true"
            + " mechanicsUnchanged=true styleFamilyCases=10 strokeCases="
            + (OptionalIntegrations.installed(OptionalIntegrations.TRACK_STYLES).mayInspect()
                ? 2
                : 0));
    data.rails.remove(rail);
    for (var p : List.of(from, to)) level.removeBlock(p, false);
    helper.succeed();
  }
}
