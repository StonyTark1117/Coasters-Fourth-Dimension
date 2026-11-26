package dev.fourthdimension.mixin;
import dev.fourthdimension.moving.MovingNativeAdapter;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraph;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=CoasterPathGraphManager.class,remap=false)
public abstract class MovingNativeGraphMixin {
  @Inject(method="get",at=@At("RETURN"),require=1)
  private static void fourthdimension$registeredDockGraph(ServerLevel level,CallbackInfoReturnable<CoasterPathGraph> result){
    MovingNativeAdapter.reconcileGraph(level,result.getReturnValue());
  }
}
