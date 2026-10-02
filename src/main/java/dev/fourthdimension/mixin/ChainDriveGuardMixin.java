package dev.fourthdimension.mixin;

import dev.fourthdimension.integration.OptionalIntegrations;
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
    // With the inspected addon, its HEAD callback must run its native reports.
    // EngineeredMotorGuardMixin still cancels before its force/parking commands.
    if (!OptionalIntegrations.installed(OptionalIntegrations.ENGINEERED).mayInspect()
        && PropulsionInterlock.holds(sub, level)) ci.cancel();
  }

  @Inject(
      method = "applySpeedMatch",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Ldev/silvergold/simulatedcoasters/track/chainlift/ChainLiftSpanQuery;findAtGraphHit(Lnet/minecraft/world/level/Level;Ldev/silvergold/simulatedcoasters/track/graph/CoasterPathTrackFrame$GraphHit;D)Ldev/silvergold/simulatedcoasters/track/chainlift/ChainLiftSpanQuery$ChainLiftSpanHit;"),
      cancellable = true)
  private static void fourthdimension$holdAfterEngineeredReports(
      ServerSubLevel sub,
      ServerLevel level,
      SubLevelPhysicsSystem physics,
      CoasterPathTrackFrame.GraphHit hit,
      CoasterPathTrackFrame.TrackBasis basis,
      double dt,
      CallbackInfo ci) {
    // This is the first native chain query, before any chain speed matching.
    if (OptionalIntegrations.installed(OptionalIntegrations.ENGINEERED).mayInspect()
        && PropulsionInterlock.holds(sub, level)) ci.cancel();
  }
}
