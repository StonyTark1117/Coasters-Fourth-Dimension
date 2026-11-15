package dev.fourthdimension.mixin;

import com.simibubi.create.content.trains.track.TrackMaterial;
import dev.fourthdimension.integration.CtnbTracks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets="dev.notzyvex.coasters_extras.track.TrackRepaint",remap=false)
public abstract class CtnbRepaintMixin {
  @Inject(method="repaint",at=@At("HEAD"),cancellable=true)
  private static void guard(ServerLevel l,BlockPos a,BlockPos b,TrackMaterial material,CallbackInfoReturnable<Boolean> ci) {
    if(!CtnbTracks.mayRepaint(l,a,b,material))ci.setReturnValue(false);
  }
  @Inject(method="repaint",at=@At("RETURN"))
  private static void changed(ServerLevel l,BlockPos a,BlockPos b,TrackMaterial material,CallbackInfoReturnable<Boolean> ci) {
    if(ci.getReturnValue())CtnbTracks.repainted(l,a,b);
  }
}
