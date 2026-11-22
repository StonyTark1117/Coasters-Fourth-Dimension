package dev.fourthdimension.mixin;

import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.FlyingBikeVehicle;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartChainLiftDrive;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Bound the existing powered native matcher itself; transmission ramp alone cannot bound entry. */
@Mixin(CoasterCartChainLiftDrive.class)
public abstract class FlyingBikeChainRampMixin {
  @ModifyConstant(method="applySpeedMatch",constant={@Constant(doubleValue=64.0),@Constant(doubleValue=32.0)},require=2)
  private static double fourthdimension$boundedProneLaunch(double original,ServerSubLevel body,ServerLevel level,
      SubLevelPhysicsSystem physics,CoasterPathTrackFrame.GraphHit hit,CoasterPathTrackFrame.TrackBasis basis,double dt){
    var v=FoundationData.get(level).vehicles.get(body.getUniqueId());
    return v!=null&&FlyingBikeVehicle.is(v)?3:original;
  }
}
