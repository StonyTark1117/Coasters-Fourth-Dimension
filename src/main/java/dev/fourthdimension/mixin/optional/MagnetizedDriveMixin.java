package dev.fourthdimension.mixin.optional;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.fourthdimension.integration.ExternalDrivePolicy;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartChainLiftDrive;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;

/** Wraps the full drive, including Magnetized's injected HEAD callback, regardless of ordering. */
@Mixin(value = CoasterCartChainLiftDrive.class, remap = false)
public abstract class MagnetizedDriveMixin {
  @WrapMethod(method = "applySpeedMatch")
  private static void fourthdimension$externalInterlocks(
      ServerSubLevel body,
      ServerLevel level,
      SubLevelPhysicsSystem physics,
      CoasterPathTrackFrame.GraphHit hit,
      CoasterPathTrackFrame.TrackBasis basis,
      double dt,
      Operation<Void> original) {
    if (ExternalDrivePolicy.allows(level, body.getUniqueId()))
      original.call(body, level, physics, hit, basis, dt);
  }
}
