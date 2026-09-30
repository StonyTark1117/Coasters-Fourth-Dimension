package dev.fourthdimension.track;

import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;

/** Certification of the pinned, unmodified Coasters bearing; added bodies need another survey. */
public final class OrdinaryGearCertificate {
  private OrdinaryGearCertificate() {}

  public static boolean fits(SubLevel chassis) {
    var bounds = chassis.getPlot().getBoundingBox();
    if (bounds == null
        || bounds.maxX() - bounds.minX() > 1
        || bounds.maxY() - bounds.minY() > 1
        || bounds.maxZ() - bounds.minZ() > 1) return false;
    int bearings = 0;
    for (var holder : chassis.getPlot().getLoadedChunks())
      for (var section : holder.getChunk().getSections()) {
        if (section == null || section.hasOnlyAir()) continue;
        for (int x = 0; x < 16; x++)
          for (int y = 0; y < 16; y++)
            for (int z = 0; z < 16; z++) {
              var state = section.getBlockState(x, y, z);
              if (state.isAir()) continue;
              if (!state.is(SimulatedCoastersBlocks.COASTER_CART_BLOCK.get()) || ++bearings > 1)
                return false;
            }
      }
    return bearings == 1;
  }
}
