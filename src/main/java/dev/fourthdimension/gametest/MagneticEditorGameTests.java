package dev.fourthdimension.gametest;

import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.magnetic.*;
import dev.fourthdimension.physics.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class MagneticEditorGameTests {
  private static void rejects(GameTestHelper helper, Runnable action, String message) {
    boolean failed = false;
    try {
      action.run();
    } catch (IllegalArgumentException e) {
      failed = true;
    }
    helper.assertTrue(failed, message);
  }

  @GameTest(template = "empty", timeoutTicks = 130)
  public static void actualEquipmentInventoryAtomicEditingAndEligibility(GameTestHelper helper) {
    var level = helper.getLevel();
    for (int x = 46; x <= 50; x++)
      for (int z = -20; z <= -16; z++) level.setChunkForced(x, z, true);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000203"), "FDMagneticEditor"));
    player.setGameMode(GameType.CREATIVE);
    var from = new BlockPos(750, 140, -300);
    var to = from.offset(0, 0, 32);
    for (var p : List.of(from, to))
      level.setBlockAndUpdate(
          p, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
    player.setItemInHand(
        InteractionHand.MAIN_HAND, TrackItems.FREE_SPIN.get().getDefaultInstance());
    var rail =
        TrackPlacement.connect(
            level, from, to, TrackDesign.FREE_SPIN, player, player.getMainHandItem());
    player.setPos(from.getX() + 2, from.getY() + 1, from.getZ());
    player.setGameMode(GameType.SURVIVAL);
    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TrackItems.INDUCER.get(), 3));
    var context =
        new UseOnContext(
            player,
            InteractionHand.MAIN_HAND,
            new BlockHitResult(from.getCenter(), Direction.UP, from, false));
    helper.assertTrue(
        TrackItems.INDUCER.get().useOn(context).consumesAction(),
        "Equipment item failed to install");
    helper.assertTrue(
        rail.magnets.size() == 1 && player.getMainHandItem().getCount() == 2,
        "Equipment did not consume exactly one item");
    long revision = rail.revision;
    var before = rail.magnets;
    var changed = List.of(new MagneticSpan(6, 10, MagneticSpan.Mode.INDUCE, -1, 3, .3, 5));
    EditorNetwork.apply(
        player, new EditorNetwork.Edit(rail.id, revision, 4, MagneticData.format(changed)));
    helper.assertTrue(
        rail.magnets.equals(changed) && player.getMainHandItem().getCount() == 2,
        "Editing existing equipment charged another item");
    rejects(
        helper,
        () ->
            EditorNetwork.apply(
                player, new EditorNetwork.Edit(rail.id, revision, 4, MagneticData.format(before))),
        "Stale magnet packet accepted");
    rejects(
        helper, () -> MagneticData.parse("0:2:induce:1:NaN:.2:15"), "Non-finite strength accepted");
    rejects(
        helper, () -> MagneticData.parse("x".repeat(4097)), "Oversized magnet payload accepted");
    rejects(
        helper,
        () ->
            MagneticData.apply(
                player,
                rail.id,
                rail.revision,
                List.of(new MagneticSpan(0, 40, MagneticSpan.Mode.INDUCE, 1, 4, .2, 15))),
        "Off-section equipment accepted");
    rejects(
        helper,
        () ->
            MagneticData.apply(
                player,
                rail.id,
                rail.revision,
                List.of(new MagneticSpan(0, 4, MagneticSpan.Mode.DAMP, 1, 4, .2, 15))),
        "Missing equipment inventory was ignored");
    helper.assertTrue(rail.magnets.equals(changed), "Failed edit partially changed equipment");
    player.setGameMode(GameType.ADVENTURE);
    rejects(
        helper,
        () -> MagneticData.apply(player, rail.id, rail.revision, List.of()),
        "Permission bypass accepted");
    player.setGameMode(GameType.SURVIVAL);
    player.setPos(750, 140, -200);
    rejects(
        helper,
        () -> MagneticData.apply(player, rail.id, rail.revision, List.of()),
        "Distant magnet edit accepted");
    player.setPos(from.getX() + 2, from.getY() + 1, from.getZ());
    MagneticData.apply(player, rail.id, rail.revision, List.of());
    helper.assertTrue(
        player.getInventory().items.stream()
                .filter(s -> s.is(TrackItems.INDUCER.get()))
                .mapToInt(ItemStack::getCount)
                .sum()
            == 3,
        "Removing equipment did not refund one item");
    player.setGameMode(GameType.CREATIVE);
    MagneticData.apply(player, rail.id, rail.revision, changed);
    var copy =
        FoundationData.load(
            FoundationData.get(level).save(new CompoundTag(), level.registryAccess()),
            level.registryAccess());
    helper.assertTrue(
        copy.rails.stream()
            .filter(r -> r.id.equals(rail.id))
            .findFirst()
            .orElseThrow()
            .magnets
            .equals(changed),
        "Edited magnets did not survive reload");
    FoundationData.Vehicle[] vehicle = {null};
    helper.runAtTickTime(
        15,
        () -> {
          var edge = TrackGeometry.resolve(level, rail);
          var hit =
              new CoasterPathTrackFrame.GraphHit(
                  CoasterPathGraphManager.sampleEdgePosition(level, edge, .5), edge, .5);
          rejects(
              helper,
              () -> VehicleFactory.create(level, TrackDesign.ARROW, hit),
              "Arrow car accepted Free Spin track");
          rejects(
              helper,
              () -> VehicleFactory.create(level, TrackDesign.LEGACY, hit),
              "Legacy car accepted Free Spin track");
          vehicle[0] = VehicleFactory.create(level, TrackDesign.FREE_SPIN, hit);
          rejects(
              helper,
              () -> MagneticData.apply(player, rail.id, rail.revision, List.of()),
              "Occupied magnetic section was editable");
        });
    helper.runAtTickTime(
        100,
        () -> {
          var v = vehicle[0];
          var container = SubLevelContainer.getContainer(level);
          var body = container.getSubLevel(v.chassis);
          VehicleOperations.restraints(level, v, true);
          helper.assertTrue(
              !ConsistRules.canCouple(level, body, body, 6),
              "Free Spin train coupling bypassed topology");
          for (var other : FoundationData.get(level).rails)
            if (other.family != v.family && other.original()) {
              var edge = TrackGeometry.resolve(level, other);
              if (edge == null) continue;
              var hit =
                  new CoasterPathTrackFrame.GraphHit(
                      CoasterPathGraphManager.sampleEdgePosition(level, edge, .5), edge, .5);
              helper.assertTrue(
                  !EligibilityHooks.allows(body, level, hit),
                  "Locked Free Spin accepted unrelated track");
            }
          container.removeSubLevel(body, SubLevelRemovalReason.REMOVED);
          helper.succeed();
        });
  }
}
