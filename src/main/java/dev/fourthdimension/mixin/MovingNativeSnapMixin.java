package dev.fourthdimension.mixin;
import dev.fourthdimension.moving.MovingNativeAdapter;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrackSnap;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
/** One pose owner: only this native snap loop omits authenticated physically retained bodies. */
@Mixin(value=CoasterCartTrackSnap.class,remap=false)
public abstract class MovingNativeSnapMixin {
  @Redirect(method="onPrePhysicsTick",at=@At(value="INVOKE",target="Ldev/ryanhcode/sable/api/sublevel/ServerSubLevelContainer;getAllSubLevels()Ljava/util/List;"),require=1)
  private static List<ServerSubLevel> fourthdimension$retainedSupport(ServerSubLevelContainer container) {
    dev.fourthdimension.moving.MovingNativeRestoreSupport.prepareAll(container.getLevel());
    return container.getAllSubLevels().stream().filter(b->!dev.fourthdimension.moving.MovingNativeRestoreSupport.held(container.getLevel(),b.getUniqueId())
        &&!MovingNativeAdapter.retained(container.getLevel(),b.getUniqueId())
        &&dev.fourthdimension.runtime.FoundationData.get(container.getLevel()).moving.retaining(b.getUniqueId())==null).toList();
  }
}
