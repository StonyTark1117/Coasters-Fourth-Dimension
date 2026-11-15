package dev.fourthdimension.mixin;

import dev.fourthdimension.integration.RotatingSeatIntegration;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** FD's normal mount listener owns restraints, including recovery before native removal. */
@Pseudo
@Mixin(targets="dev.plushivr.coasterfins.event.SeatDismountHandler",remap=false)
public abstract class RotatingSeatDismountMixin {
  @Inject(method="onMountChange",at=@At("HEAD"),cancellable=true,require=0)
  private void fourthdimension$oneOwner(EntityMountEvent event,CallbackInfo ci) {
    var seat=event.getEntityBeingMounted();
    if(!seat.level().isClientSide&&RotatingSeatIntegration.ownsSeat(seat))ci.cancel();
  }
}
