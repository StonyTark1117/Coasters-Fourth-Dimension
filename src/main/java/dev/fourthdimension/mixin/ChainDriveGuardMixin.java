package dev.fourthdimension.mixin;

import dev.fourthdimension.integration.PropulsionInterlock;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartChainLiftDrive;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CoasterCartChainLiftDrive.class, remap = false)
public abstract class ChainDriveGuardMixin {
  @Inject(method = "applySpeedMatch", at = @At("HEAD"), cancellable = true)
  private static void fourthdimension$stationHold(
      ServerSubLevel sub,
      ServerLevel level,
      SubLevelPhysicsSystem physics,
      CoasterPathTrackFrame.GraphHit hit,
      CoasterPathTrackFrame.TrackBasis basis,
      double dt,
      CallbackInfo ci) {
    if (PropulsionInterlock.holds(sub, level)) ci.cancel();
  }
}
