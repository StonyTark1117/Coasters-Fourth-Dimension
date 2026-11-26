package dev.fourthdimension.mixin;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrackSnap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
/** Clear the pinned native guide and nearest-hit caches before physical table retention. */
@Mixin(value=CoasterCartTrackSnap.class,remap=false)
public interface MovingNativeSnapAccess {
  @Invoker("tearDownAllRailCouplingForSub")
  static void fourthdimension$tearDownRailCoupling(ServerSubLevel body){throw new AssertionError("Mixin not applied");}
}
