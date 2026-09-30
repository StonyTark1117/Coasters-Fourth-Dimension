package dev.fourthdimension.vehicle;

import dev.fourthdimension.runtime.FoundationData;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public final class VehicleVisualState {
  private VehicleVisualState() {}

  public static void tick(LevelTickEvent.Post event) {
    if (!(event.getLevel() instanceof ServerLevel level)) return;
    var container = SubLevelContainer.getContainer(level);
    if (container == null) return;
    for (var v : FoundationData.get(level).vehicles.values())
      if (!FoundationData.VARIANT.equals(v.variant))
        for (var c : v.carriers) {
          var body = container.getSubLevel(c.body);
          if (body == null) continue;
          for (var relative : c.seats) {
            var pos = body.getPlot().getCenterBlock().offset(relative);
            var state = level.getBlockState(pos);
            if (state.getBlock() instanceof CarrierSeatBlock
                && state.getValue(CarrierSeatBlock.RESTRAINED) != v.restraints)
              level.setBlock(pos, state.setValue(CarrierSeatBlock.RESTRAINED, v.restraints), 3);
          }
        }
  }
}
