package dev.fourthdimension.vehicle;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.FoundationData;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public final class VehicleVisualState {
  private VehicleVisualState() {}

  public static void tick(LevelTickEvent.Post event) {
    if (!(event.getLevel() instanceof ServerLevel level)) return;
    var container = SubLevelContainer.getContainer(level);
    if (container == null) return;
    for (var v : FoundationData.get(level).vehicles.values())
      if (!FoundationData.VARIANT.equals(v.variant) && !(FlyingVehicle.is(v) || NewGenerationFlyingVehicle.is(v))) {
        for (var c : v.carriers) {
          var body = container.getSubLevel(c.body);
          if (body == null) continue;
          if (v.family == Family.ZACSPIN) {
            int side = c.side < 0 ? -1 : 1;
            for (var relative : java.util.List.of(BlockPos.ZERO, new BlockPos(side, 0, 0))) {
              var pos = body.getPlot().getCenterBlock().offset(relative);
              var state = level.getBlockState(pos);
              if (state.is(VehicleParts.AXLE.get()))
                level.setBlock(pos, VehicleParts.LEGACY_AXLE.get().defaultBlockState(), 3);
            }
          }
          for (var relative : c.seats) {
            var pos = body.getPlot().getCenterBlock().offset(relative);
            var state = level.getBlockState(pos);
            dev.fourthdimension.integration.RotatingSeatIntegration.project(level,pos,v.restraints);
            if (state.getBlock() instanceof CarrierSeatBlock
                && state.getValue(CarrierSeatBlock.RESTRAINED) != v.restraints)
              level.setBlock(pos, state.setValue(CarrierSeatBlock.RESTRAINED, v.restraints), 3);
          }
        }
      }
  }
}
