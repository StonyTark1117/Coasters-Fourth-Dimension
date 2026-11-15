package dev.fourthdimension.integration;

import dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;

/** Shared graph membership is not a running-gear certificate or a transfer bridge. */
public final class CoexistenceGuards {
  private CoexistenceGuards() {}
  public static boolean waterparked(Level level,CoasterPathEdge edge) {
    var material=CtnbTracks.material(level,edge);
    return material!=null&&material.id.getNamespace().equals("create_waterparked");
  }
  public static void requireCoreAnchor(Level level,BlockPos pos) {
    if(BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).getNamespace().equals("create_waterparked"))
      throw new IllegalArgumentException("Waterparked anchors remain slide-only; use ordinary Coasters anchors for 4D track");
  }
}
