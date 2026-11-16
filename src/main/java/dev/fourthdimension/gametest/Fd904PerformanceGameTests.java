package dev.fourthdimension.gametest;

import com.simibubi.create.content.trains.track.BezierConnection;
import dev.fourthdimension.runtime.ManagedPlotScan;
import dev.fourthdimension.track.TrackDesign;
import dev.fourthdimension.vehicle.RecoveryExamples;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.plot.LevelPlot;
import dev.silvergold.simulatedcoasters.CoasterTrackMaterials;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.CoasterPeerCurveHandleLengths;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartPlotScan;
import java.util.*;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/** Exercise the native scan and curve APIs with their production mixins applied. */
@GameTestHolder("fourthdimension_fd904_performance")
@PrefixGameTestTemplate(false)
public final class Fd904PerformanceGameTests {
  @GameTest(template = "empty", timeoutTicks = 160)
  public static void livePalettePreservesSameTickEdits(GameTestHelper h) {
    var level = h.getLevel();
    var origin = new BlockPos(-700, 120, -700);
    for (int x = -45; x <= -43; x++)
      for (int z = -45; z <= -40; z++) level.setChunkForced(x, z, true);
    var fixture = RecoveryExamples.create(level, origin, TrackDesign.ARROW);
    h.runAtTickTime(40, () -> {
      var container = SubLevelContainer.getContainer(level);
      var chassis = (ServerSubLevel) container.getSubLevel(fixture.car().chassis);
      var carrier = (ServerSubLevel) container.getSubLevel(fixture.car().carriers.getFirst().body);
      h.assertTrue(ManagedPlotScan.managed(chassis.getPlot()), "Chassis not managed");
      h.assertTrue(ManagedPlotScan.managed(carrier.getPlot()), "Carrier not managed");
      h.assertTrue(!ManagedPlotScan.managed(fixture.bearing().getPlot()), "Ordinary bearing claimed");
      int checks = 0;
      for (var body : List.of(chassis, carrier, fixture.bearing())) {
        var plot = body.getPlot();
        var chunk = plot.getLoadedChunks().iterator().next().getChunk();
        var section = Arrays.stream(chunk.getSections()).filter(s -> !s.hasOnlyAir()).findFirst().orElseThrow();
        var beforeA = section.getBlockState(14, 14, 14);
        var beforeB = section.getBlockState(15, 13, 14);
        try {
          for (var requested : List.of(SimulatedCoastersBlocks.COASTER_CART_BLOCK.get(),
              SimulatedCoastersBlocks.RIVET.get())) {
            section.setBlockState(14, 14, 14, Blocks.STONE.defaultBlockState());
            section.setBlockState(15, 13, 14, Blocks.STONE.defaultBlockState());
            compare(h, plot, requested); checks++;
            section.setBlockState(14, 14, 14, requested.defaultBlockState());
            compare(h, plot, requested); checks++;
            section.setBlockState(15, 13, 14, requested.defaultBlockState());
            compare(h, plot, requested); checks++;
            section.setBlockState(14, 14, 14, Blocks.STONE.defaultBlockState());
            section.setBlockState(15, 13, 14, Blocks.STONE.defaultBlockState());
            compare(h, plot, requested); checks++;
          }
        } finally {
          section.setBlockState(14, 14, 14, beforeA);
          section.setBlockState(15, 13, 14, beforeB);
        }
      }
      System.out.println("FD904_PERFORMANCE native scan checks=" + checks + " sameTick=" + level.getGameTime());
      h.succeed();
    });
  }

  private static void compare(GameTestHelper h, LevelPlot plot, Block block) {
    var expected = new ArrayList<BlockPos>();
    for (var holder : plot.getLoadedChunks()) {
      var chunk = holder.getChunk();
      if (chunk == null) continue;
      for (int i = 0; i < chunk.getSectionsCount(); i++) {
        var section = chunk.getSection(i);
        if (section == null) continue;
        for (int x = 0; x < 16; x++) for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++)
          if (section.getBlockState(x, y, z).is(block))
            expected.add(new BlockPos(chunk.getPos().getMinBlockX() + x,
                chunk.getSectionYFromSectionIndex(i) * 16 + y, chunk.getPos().getMinBlockZ() + z));
      }
    }
    var actual = CoasterCartPlotScan.scanBearingCells(plot, block);
    h.assertTrue(actual.bearingCount() == expected.size() && actual.cells().equals(expected),
        "Native scan omitted or reordered " + block + " cells");
    var representative = expected.stream().min(Comparator.<BlockPos>comparingInt(BlockPos::getY)
        .thenComparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ)).orElse(null);
    h.assertTrue(Objects.equals(actual.representative(), representative), "Native representative changed");
  }

  @GameTest(template = "empty", timeoutTicks = 20)
  public static void endpointKeysTrackMutationWithoutCachingHandles(GameTestHelper h) {
    var a = new BlockPos.MutableBlockPos(-20, 90, 15);
    var b = new BlockPos.MutableBlockPos(-20, 90, 55);
    var curve = new BezierConnection(Couple.create(a, b),
        Couple.create(Vec3.atCenterOf(a), Vec3.atCenterOf(b)),
        Couple.create(new Vec3(0, 0, 1), new Vec3(0, 0, -1)),
        Couple.create(new Vec3(0, 1, 0), new Vec3(0, 1, 0)), true, false, CoasterTrackMaterials.COASTER);
    String first = CoasterPeerCurveHandleLengths.edgeKey(curve);
    h.assertTrue(first == CoasterPeerCurveHandleLengths.edgeKey(curve), "Unchanged key not reused");
    h.assertTrue(first.equals(CoasterPeerCurveHandleLengths.edgeKeyFromEndpoints(a, b)), "Wrong initial key");
    a.set(-21, 91, 16);
    String edited = CoasterPeerCurveHandleLengths.edgeKey(curve);
    h.assertTrue(!first.equals(edited) && edited.equals(CoasterPeerCurveHandleLengths.edgeKeyFromEndpoints(a, b)),
        "In-place endpoint mutation reused stale key");
    h.assertTrue(edited.equals(CoasterPeerCurveHandleLengths.edgeKey(curve.secondary())), "Reverse key differs");
    CoasterPeerCurveHandleLengths.putCanonical(curve, 7, 9);
    var old = CoasterPeerCurveHandleLengths.getCanonicalLengths(curve);
    CoasterPeerCurveHandleLengths.putCanonical(curve, 11, 13);
    var changed = CoasterPeerCurveHandleLengths.getCanonicalLengths(curve);
    h.assertTrue(!old.equals(changed) && changed.getFirst() == 11 && changed.getSecond() == 13,
        "Handle edit hidden by key reuse");
    CoasterPeerCurveHandleLengths.removeEdge(curve);
    System.out.println("FD904_PERFORMANCE endpoint mutation/reversal and native handle edit passed");
    h.succeed();
  }
}
