package dev.fourthdimension.client;

import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import dev.silvergold.simulatedcoasters.client.track.ChainLiftAnchorOutlineClient;
import dev.silvergold.simulatedcoasters.client.track.graph.CoasterPathGraphManagerClient;
import dev.silvergold.simulatedcoasters.track.anchor.ChainLiftClientIndex;
import dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftSpanOverlap;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphSpans;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphSpans.SpanPath;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** On-demand client inspection; never places track, sends input or changes selection. */
@EventBusSubscriber(modid = "fourthdimension", value = Dist.CLIENT)
public final class ChainLiftDiagnostics {
  private static int ticks;

  private ChainLiftDiagnostics() {}

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    if (!Boolean.getBoolean("fourthdimension.validation") || ++ticks % 20 != 0) return;
    var mc = Minecraft.getInstance();
    if (mc.level == null || mc.player == null) return;
    var request = mc.gameDirectory.toPath().resolve("fd-chain-diagnostic.request");
    if (!Files.isRegularFile(request)) return;
    var result = new LinkedHashMap<String, Object>();
    result.put("at", java.time.Instant.now().toString());
    result.put("gameTime", mc.level.getGameTime());
    try {
      String probe = Files.readString(request).trim();
      Files.delete(request);
      var selected = (BlockPos) read("selectedAnchor");
      var cached = (SpanPath) read("spanPreviewCachedPath");
      result.put("selectedAnchor", position(selected));
      result.put("cachedKey", String.valueOf(read("spanPreviewCacheKey")));
      result.put("cachedStatus", String.valueOf(read("spanPreviewCachedStatus")));
      result.put("cachedPath", path(cached));
      result.put("heldItem", mc.player.getMainHandItem().toString());
      result.put("hit", String.valueOf(mc.hitResult));
      if (mc.hitResult instanceof BlockHitResult hit) {
        result.put("hitBlock", position(hit.getBlockPos()));
        result.put("hitState", mc.level.getBlockState(hit.getBlockPos()).toString());
        var entity = mc.level.getBlockEntity(hit.getBlockPos());
        result.put("hitBlockEntity", entity == null ? null : entity.getClass().getName());
      }
      SpanPath requested = null;
      BlockPos probeStart = null;
      if (!probe.isEmpty()) {
        String[] parts = probe.split("\\s+");
        if (parts.length != 6)
          throw new IllegalArgumentException("Expected blank request or six endpoint coordinates");
        probeStart = new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
            Integer.parseInt(parts[2]));
        var end = new BlockPos(Integer.parseInt(parts[3]), Integer.parseInt(parts[4]),
            Integer.parseInt(parts[5]));
        requested = CoasterPathGraphSpans.spanPathBetweenAnchors(mc.level,
            CoasterPathGraphManagerClient.get(mc.level), probeStart, end, 0);
        result.put("probeStart", position(probeStart));
        result.put("probeEnd", position(end));
        result.put("probePath", path(requested));
      }
      var anchors = new ArrayList<Map<String, Object>>();
      for (var anchor : ChainLiftClientIndex.snapshotAnchors()) {
        if (anchor.isRemoved() || anchor.getLevel() != mc.level) continue;
        var row = new LinkedHashMap<String, Object>();
        row.put("anchor", position(anchor.getBlockPos()));
        row.put("currentBlockEntity", mc.level.getBlockEntity(anchor.getBlockPos()) == anchor);
        row.put("hasShaft", anchor.hasChainLiftShaft());
        row.put("hasSpan", anchor.hasChainLiftSpan());
        row.put("speed", anchor.getSpeed());
        row.put("committedSegments", anchor.getChainLiftCommittedSegments().toString());
        // Resolve from this anchor's synchronized data, without trusting a prior path-cache entry.
        var existing = anchor.resolveChainLiftSpanPath(0);
        row.put("resolvedPath", path(existing));
        row.put("overlapsCached", existing != null && cached != null
            && !anchor.getBlockPos().equals(selected)
            && ChainLiftSpanOverlap.spansOverlap(cached, existing));
        row.put("overlapsProbe", existing != null && requested != null
            && !anchor.getBlockPos().equals(probeStart)
            && ChainLiftSpanOverlap.spansOverlap(requested, existing));
        anchors.add(row);
      }
      result.put("indexedAnchors", anchors);
    } catch (Exception failure) {
      result.put("error", failure.toString());
    }
    try {
      var output = mc.gameDirectory.toPath().resolve("fd-chain-diagnostic.json");
      Files.writeString(output,
          new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(result) + "\n");
      LogUtils.getLogger().info("FD_CHAIN_DIAGNOSTIC output={} error={}", output, result.get("error"));
    } catch (Exception failure) {
      LogUtils.getLogger().warn("FD_CHAIN_DIAGNOSTIC could not write snapshot", failure);
    }
  }

  private static Object read(String name) throws ReflectiveOperationException {
    var field = ChainLiftAnchorOutlineClient.class.getDeclaredField(name);
    field.setAccessible(true);
    return field.get(null);
  }

  private static Object position(BlockPos pos) {
    return pos == null ? null : new int[] {pos.getX(), pos.getY(), pos.getZ()};
  }

  private static Object path(SpanPath path) {
    if (path == null) return null;
    return Map.of("length", path.totalLength(), "segments", path.segments().stream()
        .map(s -> Map.of("from", position(s.edge().from()), "to", position(s.edge().to()),
            "tStart", s.tStart(), "tEnd", s.tEnd())).toList());
  }
}
