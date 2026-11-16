package dev.fourthdimension.mixin;

import dev.fourthdimension.runtime.ManagedPlotScan;
import dev.ryanhcode.sable.sublevel.plot.LevelPlot;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartPlotScan;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CoasterCartPlotScan.class, remap = false)
public abstract class ManagedPlotScanMixin {
  @Redirect(method = "scanBearingCells", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;hasOnlyAir()Z"))
  private static boolean fourthdimension$skipAbsent(
      LevelChunkSection section, LevelPlot plot, Block requested) {
    return ManagedPlotScan.skip(section, plot, requested);
  }
}
