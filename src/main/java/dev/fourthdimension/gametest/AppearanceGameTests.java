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
    for (int x = 46; x <= 48; x++)
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
    var snapshot = new FoundationData();
    snapshot.rails.add(rail);
    var saved = snapshot.save(new CompoundTag(), level.registryAccess());
    var restored = FoundationData.load(saved, level.registryAccess()).rails.getFirst();
    helper.assertTrue(
        restored.id.equals(rail.id)
            && restored.appearance.equals(rail.appearance)
            && restored.profile.keys().equals(rail.profile.keys()),
        "Appearance NBT changed identities or profile");
    for (int schema = 1; schema <= 4; schema++) {
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
        "FD_APPEARANCE schema=5 oldSchemas=1..4 missingIntentPreserved=true"
            + " mechanicsUnchanged=true");
    data.rails.remove(rail);
    for (var p : List.of(from, to)) level.removeBlock(p, false);
    helper.succeed();
  }
}
