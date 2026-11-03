package dev.fourthdimension.gametest;

import com.simibubi.create.content.trains.track.TrackMaterial;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import java.util.List;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class TrackContentGameTests {
  @GameTest(template = "empty", timeoutTicks = 80)
  public static void originalTrackItemsAndPersistence(GameTestHelper helper) {
    var level = helper.getLevel();
    for (int x = -6; x <= -3; x++) for (int z = 7; z <= 10; z++) level.setChunkForced(x, z, true);
    var player = FakePlayerFactory.getMinecraft(level);
    player.setGameMode(GameType.CREATIVE);
    var from = new BlockPos(-80, 90, 120);
    var to = from.offset(0, 0, 24);
    var other = from.offset(18, 4, 0);
    var end = other.offset(0, 0, 24);
    for (var p : List.of(from, to, other, end))
      level.setBlockAndUpdate(
          p, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    place(helper, player, TrackItems.ARROW.get(), from, to);
    place(helper, player, TrackItems.LEGACY.get(), other, end);
    var data = FoundationData.get(level);
    var rail = data.rails.stream().filter(r -> r.matches(from, to)).findFirst().orElseThrow();
    helper.assertTrue(
        rail.original() && rail.variant.equals(TrackDesign.ARROW.variant),
        "Track item lost its mechanical identity");
    helper.assertTrue(
        TrackGeometry.resolve(level, rail) != null, "Item did not create a native track curve");
    var copy =
        FoundationData.load(
            data.save(new CompoundTag(), level.registryAccess()), level.registryAccess());
    var saved = copy.rails.stream().filter(r -> r.id.equals(rail.id)).findFirst().orElseThrow();
    helper.assertTrue(
        saved.variant.equals(rail.variant) && saved.geometryHash == rail.geometryHash,
        "Track identity did not survive serialization");
    long originalRevision = rail.revision;
    // Model an intact pre-provenance save without changing its functional binding.
    rail.originalCurve = null;
    var legacy = data.rails.stream().filter(r -> r.matches(other, end)).findFirst().orElseThrow();
    var wrongMaterialCurve = TrackGeometry.resolve(level, legacy).bezier().clone();
    wrongMaterialCurve.setMaterial(TrackMaterial.ANDESITE);
    var legacyAnchor = (CoasterAnchorpointBlockEntity) level.getBlockEntity(other);
    legacyAnchor.removeAnchorPeerCurve(level, end);
    legacyAnchor.putAnchorPeerCurve(level, end, wrongMaterialCurve);
    TrackGeometry.invalidate(level);
    var wrongMaterialEdge = TrackGeometry.resolve(level, legacy);
    helper.assertTrue(
        wrongMaterialEdge != null
            && TrackMaterial.ANDESITE.id.equals(wrongMaterialEdge.bezier().getMaterial().id),
        "Non-Coasters material control did not retain the substituted native curve");
    // Replacing the native curve can change the sampled basis. Model an older, still-valid rail
    // whose saved binding matches this foreign curve so material is the only backfill gate.
    legacy.geometryHash = TrackGeometry.fingerprint(level, wrongMaterialEdge);
    long foreignGeometry = legacy.geometryHash;
    legacy.originalCurve = null;
    helper.runAtTickTime(
        20,
        () -> {
          helper.assertTrue(
              rail.valid, "Unedited track invalidated itself after graph synchronization");
          helper.assertTrue(
              rail.originalCurve != null && rail.revision == originalRevision,
              "Intact legacy rail did not acquire its exact native curve without rebinding");
          var backfilled =
              FoundationData.load(
                      data.save(new CompoundTag(), level.registryAccess()), level.registryAccess())
                  .rails.stream()
                  .filter(r -> r.id.equals(rail.id))
                  .findFirst()
                  .orElseThrow();
          helper.assertTrue(
              backfilled.originalCurve != null && backfilled.geometryHash == rail.geometryHash,
              "Backfilled legacy curve was not persisted with its original binding");
          var currentForeignEdge = TrackGeometry.resolve(level, legacy);
          var currentForeignMaterial =
              currentForeignEdge == null || currentForeignEdge.bezier() == null
                  ? null
                  : currentForeignEdge.bezier().getMaterial();
          long currentForeignHash =
              currentForeignEdge == null ? Long.MIN_VALUE : TrackGeometry.fingerprint(level, currentForeignEdge);
          helper.assertTrue(
              legacy.valid
                  && legacy.originalCurve == null
                  && currentForeignMaterial != null
                  && TrackMaterial.ANDESITE.id.equals(currentForeignMaterial.id)
                  && currentForeignHash == foreignGeometry,
              "Foreign material rail changed after native graph synchronization: valid="
                  + legacy.valid
                  + ", snapshot="
                  + (legacy.originalCurve != null)
                  + ", material="
                  + currentForeignMaterial
                  + ", savedHash="
                  + foreignGeometry
                  + ", nativeHash="
                  + currentForeignHash);
          // A changed fingerprint and a missing native connection must never get provenance.
          legacy.originalCurve = null;
          legacy.geometryHash++;
          rail.originalCurve = null;
          ((CoasterAnchorpointBlockEntity) level.getBlockEntity(from))
              .removeAnchorPeerCurve(level, to);
        });
    helper.runAtTickTime(
        40,
        () -> {
          helper.assertTrue(!rail.valid, "Removed geometry retained its active mechanical profile");
          helper.assertTrue(
              rail.originalCurve == null && !legacy.valid && legacy.originalCurve == null,
              "Missing or changed legacy geometry acquired an exact-curve snapshot");
          for (var p : List.of(from, to, other, end)) level.removeBlock(p, false);
          data.rails.removeIf(r -> r.matches(from, to) || r.matches(other, end));
          helper.succeed();
        });
  }

  private static void place(
      GameTestHelper helper,
      net.minecraft.server.level.ServerPlayer player,
      Item item,
      BlockPos from,
      BlockPos to) {
    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item, 64));
    for (var p : List.of(from, to)) {
      player.setPos(p.getX() + 2, p.getY() + 1, p.getZ());
      var result =
          item.useOn(
              new UseOnContext(
                  player,
                  InteractionHand.MAIN_HAND,
                  new BlockHitResult(p.getCenter(), Direction.UP, p, false)));
      helper.assertTrue(result.consumesAction(), "Track item rejected legal anchor placement");
    }
    helper.assertTrue(
        !player.getMainHandItem().has(TrackItems.SELECTION.get()),
        "Second click did not finish placement");
  }
}
