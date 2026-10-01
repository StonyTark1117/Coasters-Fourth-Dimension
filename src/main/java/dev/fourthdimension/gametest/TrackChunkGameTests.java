package dev.fourthdimension.gametest;

import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
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

@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class TrackChunkGameTests {
  private record Route(
      TrackDesign design,
      boolean banked,
      List<BlockPos> anchors,
      List<FoundationData.Rail> rails,
      CoasterAnchorpointBlockEntity end) {}

  @GameTest(template = "empty", batch = "fourthdimension_track_chunks", timeoutTicks = 1800)
  public static void connectedRoutesPreserveBankAndEndpointsAcrossPartialUnload(
      GameTestHelper helper) {
    var level = helper.getLevel();
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000406"), "FDTrackChunks"));
    player.setGameMode(GameType.CREATIVE);
    player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
    var routes = new ArrayList<Route>();
    for (var design : TrackDesign.values()) {
      for (boolean banked : List.of(false, true)) {
        var a = new BlockPos(300 + routes.size() * 24, 220, -1000);
        var c = a.offset(0, 0, 360);
        for (int x = (a.getX() - 16) >> 4; x <= (a.getX() + 16) >> 4; x++)
          for (int z = (a.getZ() - 16) >> 4; z <= (c.getZ() + 16) >> 4; z++)
            level.setChunkForced(x, z, true);
        var anchors =
            java.util.stream.IntStream.rangeClosed(0, 6)
                .mapToObj(i -> a.offset(0, 0, i * 60))
                .toList();
        for (int i = 0; i < anchors.size(); i++) {
          var pos = anchors.get(i);
          level.setBlockAndUpdate(
              pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
          if (banked)
            ((CoasterAnchorpointBlockEntity) level.getBlockEntity(pos))
                .setJointBankRadians(new float[] {0, .2f, -.1f, .35f, -.2f, .3f, .15f}[i]);
        }
        var rails = new ArrayList<FoundationData.Rail>();
        for (int i = 0; i < anchors.size() - 1; i++) {
          player.setPos(anchors.get(i + 1).getCenter().add(2, 1, 0));
          rails.add(
              TrackPlacement.connect(
                  level,
                  anchors.get(i),
                  anchors.get(i + 1),
                  design,
                  player,
                  player.getMainHandItem()));
        }
        // Adding a neighbor can change the native bank interpolation; bind the finished route.
        for (var rail : rails) {
          player.setPos(rail.from.getCenter().add(2, 1, 0));
          ProfileEditor.rebind(player, rail.id, rail.revision);
        }
        routes.add(
            new Route(
                design,
                banked,
                anchors,
                rails,
                (CoasterAnchorpointBlockEntity) level.getBlockEntity(c)));
      }
    }
    helper.runAtTickTime(
        30,
        () -> {
          for (var route : routes) {
            helper.assertTrue(
                route.rails.stream().allMatch(r -> r.valid),
                "Connected track invalid before interruption");
            var a = route.anchors.getFirst();
            var b = route.anchors.get(1);
            var c = route.anchors.getLast();
            for (int x = (a.getX() - 16) >> 4; x <= (a.getX() + 16) >> 4; x++)
              for (int z = (b.getZ() >> 4) + 2; z <= (c.getZ() + 16) >> 4; z++)
                level.setChunkForced(x, z, false);
          }
        });
    helper.runAtTickTime(
        200,
        () -> {
          // Drain native pending unload/save work even when the accelerated GameTest loop has no
          // spare budget.
          level.getChunkSource().save(true);
          System.out.println("FD_TRACK_PARTIAL_FLUSH native=true");
        });
    long startTick = level.getGameTime();
    long[] reloadAt = {-1};
    helper.onEachTick(
        () -> {
          if (reloadAt[0] >= 0 || level.getGameTime() - startTick < 200) return;
          if (routes.stream()
              .anyMatch(
                  route -> {
                    var c = route.anchors.getLast();
                    return level.getChunkSource().getChunkNow(c.getX() >> 4, c.getZ() >> 4) != null
                        || !route.end.isRemoved();
                  })) return;
          reloadAt[0] = level.getGameTime();
          System.out.println("FD_TRACK_PARTIAL_WAITED ticks=" + (reloadAt[0] - startTick));
          for (var route : routes) {
            var c = route.anchors.getLast();
            helper.assertTrue(
                TrackGeometry.ticking(level, route.anchors.getFirst())
                    && TrackGeometry.ticking(level, route.anchors.get(1)),
                "Neighbor section stopped ticking");
            helper.assertTrue(
                level.getChunkSource().getChunkNow(c.getX() >> 4, c.getZ() >> 4) == null
                    && route.end.isRemoved(),
                "Connected end did not unload: " + route.design + " banked=" + route.banked);
            helper.assertTrue(
                route.rails.stream().allMatch(r -> r.valid),
                "Partial unload invalidated mechanical geometry");
          }
          var graph = CoasterPathGraphManager.get(level);
          var refs =
              TrackEndpointAvailability.sample(
                  level,
                  graph,
                  CoasterPathTraceEndpoints.allEdgeIndices(graph),
                  CoasterPathTraceEndpoints::pathOpenEndRefs);
          for (var route : routes) {
            var matching =
                refs.stream()
                    .filter(
                        ref ->
                            route.rails.stream()
                                .anyMatch(
                                    r ->
                                        r.matches(
                                            graph.edges().get(ref.edgeIndex()).from(),
                                            graph.edges().get(ref.edgeIndex()).to())))
                    .toList();
            helper.assertTrue(
                matching.size() == 1, "Partial route exposed an inactive or false endpoint");
            var ref = matching.getFirst();
            var edge = graph.edges().get(ref.edgeIndex());
            helper.assertTrue(
                (ref.atFrom() ? edge.from() : edge.to()).equals(route.anchors.getFirst()),
                "Partial route endpoint moved to its junction");
            var c = route.anchors.getLast();
            helper.assertTrue(
                level.getChunkSource().getChunkNow(c.getX() >> 4, c.getZ() >> 4) == null,
                "Endpoint sampling reloaded the inactive continuation");
            System.out.println(
                "FD_TRACK_PARTIAL family="
                    + route.design.family
                    + " banked="
                    + route.banked
                    + " evicted=true valid=true endpoints=1");
          }
          // Check every route before reloading any; adjacent routes share chunk neighborhoods.
          for (var route : routes) {
            var c = route.anchors.getLast();
            var a = route.anchors.getFirst();
            for (int x = (a.getX() - 16) >> 4; x <= (a.getX() + 16) >> 4; x++)
              for (int z = (a.getZ() - 16) >> 4; z <= (c.getZ() + 16) >> 4; z++)
                level.setChunkForced(x, z, true);
            level.getChunkAt(c);
          }
        });
    helper.onEachTick(
        () -> {
          if (reloadAt[0] < 0 || level.getGameTime() - reloadAt[0] < 70) return;
          var graph = CoasterPathGraphManager.get(level);
          var refs =
              TrackEndpointAvailability.sample(
                  level,
                  graph,
                  CoasterPathTraceEndpoints.allEdgeIndices(graph),
                  CoasterPathTraceEndpoints::pathOpenEndRefs);
          for (var route : routes) {
            var c = route.anchors.getLast();
            var end = (CoasterAnchorpointBlockEntity) level.getBlockEntity(c);
            helper.assertTrue(
                end != route.end && end.getJointBankRadians() == (route.banked ? .15f : 0),
                "Connected reload changed native bank");
            for (var rail : route.rails)
              helper.assertTrue(
                  rail.valid
                      && TrackGeometry.fingerprint(level, TrackGeometry.resolve(level, rail))
                          == rail.geometryHash,
                  "Connected reload changed mechanical geometry");
            long count =
                refs.stream()
                    .filter(
                        ref ->
                            route.rails.stream()
                                .anyMatch(
                                    r ->
                                        r.matches(
                                            graph.edges().get(ref.edgeIndex()).from(),
                                            graph.edges().get(ref.edgeIndex()).to())))
                    .count();
            helper.assertTrue(count == 2, "Connected reload lost an open endpoint");
            System.out.println(
                "FD_TRACK_PARTIAL_RELOADED family="
                    + route.design.family
                    + " banked="
                    + route.banked
                    + " fresh=true geometry=true endpoints=2");
          }
          helper.succeed();
        });
    helper.runAtTickTime(
        1700,
        () -> {
          for (var route : routes) {
            var c = route.anchors.getLast();
            System.out.println(
                "FD_TRACK_PARTIAL_PENDING family="
                    + route.design.family
                    + " banked="
                    + route.banked
                    + " ticking="
                    + TrackGeometry.ticking(level, c)
                    + " accessible="
                    + level.hasChunkAt(c)
                    + " resident="
                    + (level.getChunkSource().getChunkNow(c.getX() >> 4, c.getZ() >> 4) != null)
                    + " removed="
                    + route.end.isRemoved());
          }
          helper.fail("Connected route eviction/recovery did not complete");
        });
  }
}
