package dev.fourthdimension.mixin.optional;

import dev.fourthdimension.integration.ExternalDrivePolicy;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.jwaterfall.coastersengineered.MotorDrive", remap = false)
public abstract class EngineeredDriveMixin {
  @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
  private static void fourthdimension$respectInterlocks(
      ServerSubLevel sub,
      ServerLevel level,
      SubLevelPhysicsSystem physics,
      CoasterPathTrackFrame.GraphHit hit,
      CoasterPathTrackFrame.TrackBasis basis,
      double dt,
      CallbackInfo ci) {
    if (!ExternalDrivePolicy.allows(level, sub.getUniqueId())) ci.cancel();
  }
}
