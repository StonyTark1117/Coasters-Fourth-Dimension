package dev.fourthdimension.gametest;

import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
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
public final class EditorGameTests {
  @GameTest(template = "empty", timeoutTicks = 40)
  public static void maximumFunctionalSnapshotFitsNetworkBudget(GameTestHelper helper) {
    var data = new FoundationData();
    for (int i = 0; i < 256; i++) {
      int count = i < 7 ? 1024 : i == 7 ? 528 : 2;
      var keys = new ArrayList<dev.fourthdimension.physics.RotationProfile.Key>();
      for (int k = 0; k < count; k++)
        keys.add(new dev.fourthdimension.physics.RotationProfile.Key(20.0 * k / (count - 1), 0));
      var from = new BlockPos(i, 100, 0);
      data.rails.add(
          new FoundationData.Rail(
              from,
              from.offset(0, 0, 20),
              TrackDesign.FREE_SPIN.family,
              new dev.fourthdimension.physics.RotationProfile(keys),
              UUID.randomUUID(),
              TrackDesign.FREE_SPIN.variant,
              0,
              0,
              true));
    }
    for (int i = 0; i < 64; i++)
      data.rails.get(i).magnets =
          java.util.stream.IntStream.range(0, 8)
              .mapToObj(
                  n ->
                      new dev.fourthdimension.physics.MagneticSpan(
                          n,
                          n + .5,
                          dev.fourthdimension.physics.MagneticSpan.Mode.INDUCE,
                          1,
                          4,
                          .2,
                          15))
              .toList();
    data.checkProfileCapacity(0);
    rejects(helper, () -> data.checkProfileCapacity(1), "Global profile budget exceeded");
    for (int i = 0; i < 64; i++) {
      var v = new FoundationData.Vehicle(UUID.randomUUID(), TrackDesign.FREE_SPIN.family);
      v.variant = TrackDesign.FREE_SPIN.variant;
      v.fault = "x".repeat(256);
      for (int side : new int[] {-3, 3}) {
        for (int row : new int[] {-3, 3}) {
          var c = new FoundationData.Carrier(UUID.randomUUID(), side);
          c.longitudinal = row;
          c.seats = List.of(new BlockPos(0, -1, 0), new BlockPos(1, -1, 0));
          c.seats.forEach(seat -> c.riderSeats.put(UUID.randomUUID(), seat));
          c.occupants = List.copyOf(c.riderSeats.keySet());
          v.carriers.add(c);
        }
      }
      data.vehicles.put(v.chassis, v);
    }
    var buf =
        new net.minecraft.network.RegistryFriendlyByteBuf(
            io.netty.buffer.Unpooled.buffer(), helper.getLevel().registryAccess());
    try {
      dev.fourthdimension.runtime.FoundationSync.Snapshot.CODEC.encode(
          buf,
          new dev.fourthdimension.runtime.FoundationSync.Snapshot(
              data.save(new CompoundTag(), helper.getLevel().registryAccess())));
      System.out.println("FD_SNAPSHOT_MAX bytes=" + buf.readableBytes());
      helper.assertTrue(
          buf.readableBytes() < 1_048_576, "Valid snapshot exceeds Minecraft custom-payload limit");
      var copy =
          FoundationData.load(
              dev.fourthdimension.runtime.FoundationSync.Snapshot.CODEC.decode(buf).tag(),
              helper.getLevel().registryAccess());
      helper.assertTrue(
          copy.rails.size() == 256
              && copy.vehicles.size() == 64
              && copy.vehicles.values().iterator().next().carriers.getFirst().riderSeats.size() == 2
              && copy.rails.stream().mapToInt(r -> r.magnets.size()).sum() == 512,
          "Network round-trip lost identities or rider assignments");
    } finally {
      buf.release();
    }
    helper.succeed();
  }

  @GameTest(template = "empty", timeoutTicks = 60)
  public static void editsAreAtomicValidatedAndPersistent(GameTestHelper helper) {
    var level = helper.getLevel();
    for (int x = 30; x <= 32; x++) for (int z = 6; z <= 11; z++) level.setChunkForced(x, z, true);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000105"), "FDEditTest"));
    player.setGameMode(GameType.CREATIVE);
    var from = new BlockPos(500, 110, 110);
    var middle = from.offset(0, 0, 24);
    var end = middle.offset(0, 0, 24);
    for (var p : List.of(from, middle, end))
      level.setBlockAndUpdate(
          p, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
    player.setPos(middle.getX() + 2, middle.getY() + 1, middle.getZ());
    var rail =
        TrackPlacement.connect(
            level, from, middle, TrackDesign.ARROW, player, player.getMainHandItem());
    player.setPos(end.getX() + 2, end.getY() + 1, end.getZ());
    var next =
        TrackPlacement.connect(
            level, middle, end, TrackDesign.ARROW, player, player.getMainHandItem());
    player.setPos(from.getX() + 2, from.getY() + 1, from.getZ());
    var edit = new EditorNetwork.Edit(rail.id, rail.revision, 0, "0:0;12:360;24:0");
    EditorNetwork.apply(player, edit);
    helper.assertTrue(
        Math.abs(rail.profile.angleAt(12) - 2 * Math.PI) < 1e-8, "Editor erased full turn");
    rejects(helper, () -> EditorNetwork.apply(player, edit), "Stale revision accepted");
    for (String invalid :
        List.of(
            "0:NaN;24:0",
            "0:Infinity;24:0",
            "0:0;0:10",
            "0:0;24:2000",
            "0:0;24:0;",
            "x".repeat(16385),
            "0:0;24:0:999"))
      rejects(
          helper,
          () ->
              EditorNetwork.apply(
                  player, new EditorNetwork.Edit(rail.id, rail.revision, 0, invalid)),
          "Malformed profile accepted");
    long revision = rail.revision;
    rejects(
        helper,
        () ->
            EditorNetwork.apply(
                player, new EditorNetwork.Edit(rail.id, rail.revision, 0, "0:0;24:360")),
        "Discontinuous junction accepted");
    helper.assertTrue(rail.revision == revision, "Rejected edit mutated revision");
    player.setGameMode(GameType.ADVENTURE);
    rejects(
        helper,
        () -> EditorNetwork.apply(player, new EditorNetwork.Edit(rail.id, rail.revision, 2, "")),
        "Permission bypass");
    player.setGameMode(GameType.CREATIVE);
    player.setPos(0, 100, 0);
    rejects(
        helper,
        () -> EditorNetwork.apply(player, new EditorNetwork.Edit(rail.id, rail.revision, 2, "")),
        "Reach bypass");
    player.setPos(from.getX() + 2, from.getY() + 1, from.getZ());
    var data = FoundationData.get(level);
    var occupied = new FoundationData.Vehicle(UUID.randomUUID(), TrackDesign.ARROW.family);
    occupied.boundFrom = next.from;
    occupied.boundTo = next.to;
    data.vehicles.put(occupied.chassis, occupied);
    rejects(
        helper,
        () ->
            EditorNetwork.apply(
                player,
                new EditorNetwork.Edit(
                    rail.id, rail.revision, 5, "fourthdimension:family_default")),
        "Occupied approach accepted appearance edit");
    rejects(
        helper,
        () -> EditorNetwork.apply(player, new EditorNetwork.Edit(rail.id, rail.revision, 2, "")),
        "Occupied approach accepted edit");
    data.vehicles.remove(occupied.chassis);
    EditorNetwork.apply(player, new EditorNetwork.Edit(rail.id, rail.revision, 2, ""));
    helper.assertTrue(rail.profile.angleAt(12) < -6, "Mirror failed");
    rail.valid = false;
    rejects(
        helper,
        () ->
            EditorNetwork.apply(
                player, new EditorNetwork.Edit(rail.id, rail.revision, 0, edit.value())),
        "Invalid geometry accepted profile");
    EditorNetwork.apply(player, new EditorNetwork.Edit(rail.id, rail.revision, 1, ""));
    helper.assertTrue(
        rail.valid && rail.profile.angleAt(12) < -6, "Explicit rebind erased profile");
    var profileBefore = rail.profile;
    var magnetsBefore = rail.magnets;
    long geometryBefore = rail.geometryHash;
    var familyBefore = rail.family;
    String providerBefore = rail.provider, variantBefore = rail.variant;
    boolean stylesInstalled =
        dev.fourthdimension.integration.OptionalIntegrations.supported(
            "coasterssimulatedextratypes", Set.of("1.2.0"));
    helper.assertTrue(
        dev.fourthdimension.integration.AppearanceProviders.choices(rail.family).size()
            == (stylesInstalled ? 3 : 1),
        "Exact-version appearance registration failed");
    var unsupported =
        new dev.fourthdimension.integration.TrackAppearance(
            dev.fourthdimension.integration.AppearanceProviders.TRACK_STYLES,
            "coasterssimulatedextratypes:arrow_4d");
    helper.assertTrue(
        unsupported
            .effective(TrackDesign.FREE_SPIN.family)
            .equals(dev.fourthdimension.integration.TrackAppearance.ORIGINAL),
        "Arrow appearance activated for a different family");
    System.out.println(
        "FD_APPEARANCE styles="
            + stylesInstalled
            + " choices="
            + dev.fourthdimension.integration.AppearanceProviders.choices(rail.family).size());
    for (var style : dev.fourthdimension.integration.AppearanceProviders.choices(rail.family)) {
      long beforeRevision = rail.revision;
      EditorNetwork.apply(
          player, new EditorNetwork.Edit(rail.id, rail.revision, 5, style.appearance().style()));
      helper.assertTrue(
          rail.appearance.equals(style.appearance()) && rail.revision == beforeRevision + 1,
          "Appearance edit was not synchronized through the track revision");
      helper.assertTrue(
          rail.profile == profileBefore
              && rail.magnets == magnetsBefore
              && rail.geometryHash == geometryBefore
              && rail.valid
              && rail.family == familyBefore
              && rail.provider.equals(providerBefore)
              && rail.variant.equals(variantBefore),
          "Appearance changed functional data");
      for (var component : dev.fourthdimension.integration.TrackAppearance.Component.values())
        if (component != dev.fourthdimension.integration.TrackAppearance.Component.SPINE)
          helper.assertTrue(
              style
                  .appearance()
                  .renderOwners()
                  .get(component)
                  .equals(dev.fourthdimension.integration.TrackProviders.ORIGINAL),
              "Spine appearance took ownership of mechanical rails or equipment");
    }
    long beforeRejectedStyle = rail.revision;
    rejects(
        helper,
        () ->
            EditorNetwork.apply(
                player, new EditorNetwork.Edit(rail.id, rail.revision, 5, "missing:style")),
        "Unavailable appearance accepted");
    helper.assertTrue(rail.revision == beforeRejectedStyle, "Rejected appearance changed revision");
    var saved =
        FoundationData.load(
            data.save(new CompoundTag(), level.registryAccess()), level.registryAccess());
    var restored = saved.rails.stream().filter(r -> r.id.equals(rail.id)).findFirst().orElseThrow();
    helper.assertTrue(
        restored.revision == rail.revision
            && restored.valid
            && restored.profile.angleAt(12) < -6
            && restored.appearance.equals(rail.appearance),
        "Edited profile did not survive save");
    for (var r : List.of(rail, next)) {
      ((CoasterAnchorpointBlockEntity) level.getBlockEntity(r.from))
          .removeAnchorPeerCurve(level, r.to);
      data.rails.remove(r);
    }
    for (var p : List.of(from, middle, end)) level.removeBlock(p, false);
    helper.succeed();
  }

  static void rejects(GameTestHelper helper, Runnable action, String message) {
    boolean rejected = false;
    try {
      action.run();
    } catch (IllegalArgumentException | IllegalStateException expected) {
      rejected = true;
    }
    helper.assertTrue(rejected, message);
  }
}
