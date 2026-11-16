package dev.fourthdimension.runtime;

import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.plot.LevelPlot;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunkSection;

/** Exclude absent blocks using the live palette; retain the native scan for every possible match. */
public final class ManagedPlotScan {
  private ManagedPlotScan() {}

  public static boolean managed(LevelPlot plot) {
    if (!(plot.getSubLevel() instanceof ServerSubLevel body)) return false;
    var data = FoundationData.get(body.getLevel());
    var id = body.getUniqueId();
    if (data.vehicles.containsKey(id)) return true;
    for (var vehicle : data.vehicles.values()) {
      if (id.equals(vehicle.recoveryCart) || id.equals(vehicle.yaw.deck)) return true;
      for (var carrier : vehicle.carriers) if (id.equals(carrier.body)) return true;
    }
    return false;
  }

  public static boolean skip(LevelChunkSection section, LevelPlot plot, Block requested) {
    return section.hasOnlyAir()
        || managed(plot) && !section.maybeHas(state -> state.is(requested));
  }
}
