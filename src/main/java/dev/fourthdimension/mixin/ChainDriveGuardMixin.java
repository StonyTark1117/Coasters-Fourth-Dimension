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

// Let the pinned optional HEAD callbacks run their independently guarded motor first.
// Cancelling native Chain before those callbacks also cancelled the authorized Engineered drive.
@Mixin(value = CoasterCartChainLiftDrive.class, priority = 1100, remap = false)
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
    var v = dev.fourthdimension.runtime.FoundationData.get(level).vehicles.get(sub.getUniqueId());
    if(v!=null && dev.fourthdimension.integration.RideAuthority.MAGNETIZED.equals(v.authority.provider))
      dev.fourthdimension.integration.OptionalDrive.magnetized(sub,level,physics,hit,basis,dt);
    if(v!=null && dev.fourthdimension.integration.RideAuthority.CTNB.equals(v.authority.provider))
      dev.fourthdimension.integration.CtnbDrive.apply(sub,level,physics,hit,basis,dt);
    if (v != null && dev.fourthdimension.vehicle.DiveVehicle.is(v) && dev.fourthdimension.vehicle.DiveRuntime.blocksChain(level,v) || PropulsionInterlock.holds(sub, level) || v != null && dev.fourthdimension.integration.RideAuthority.deniesProvider(level,v,dev.fourthdimension.integration.RideAuthority.NATIVE_CHAIN)) ci.cancel();
  }
}
