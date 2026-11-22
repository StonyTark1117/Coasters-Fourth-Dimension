package dev.fourthdimension.vehicle;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.FoundationData;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Replace saved paired cars' old transverse center cells with continuous spine cells. */
@EventBusSubscriber(modid = "fourthdimension")
public final class PairedSpineMigration {
  private PairedSpineMigration() {}

  @SubscribeEvent
  public static void tick(LevelTickEvent.Post event) {
    if (!(event.getLevel() instanceof ServerLevel level) || level.getGameTime() % 40 != 0) return;
    var container = SubLevelContainer.getContainer(level);
    if (container == null) return;
    for (var vehicle : FoundationData.get(level).vehicles.values()) {
      if (FoundationData.VARIANT.equals(vehicle.variant) ||
          vehicle.family != Family.FREE_SPIN && vehicle.family != Family.ZACSPIN_REVISED) continue;
      var body = container.getSubLevel(vehicle.chassis);
      if (body == null) continue; // Try after a retained body is loaded.
      var embedded = body.getPlot().getEmbeddedLevelAccessor();
      for (int z = -3; z <= 3; z++) {
        var pos = new BlockPos(0, 2, z);
        if (!embedded.getBlockState(pos).is(VehicleParts.STRUT.get())) continue;
        var replacement = z == 0 || vehicle.family == Family.FREE_SPIN && Math.abs(z) == 3
            ? VehicleParts.PAIRED_SPINE_CROSS.get() : VehicleParts.PAIRED_SPINE.get();
        embedded.setBlock(pos, replacement.defaultBlockState(), 3);
      }
    }
  }
}
