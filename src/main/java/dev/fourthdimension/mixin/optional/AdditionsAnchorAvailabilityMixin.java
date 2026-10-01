package dev.fourthdimension.mixin.optional;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackGeometry;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Additions polls every anchor from physics, including anchors with no configured fin span. */
@Pseudo
@Mixin(targets = "dev.plushivr.coasterfins.behaviour.AnchorSpanBehaviour", remap = false)
public abstract class AdditionsAnchorAvailabilityMixin {
  @Inject(method = "getSignal", at = @At("HEAD"), cancellable = true)
  private void fourthdimension$inactiveAnchor(CallbackInfoReturnable<Integer> cir) {
    var behaviour = (BlockEntityBehaviour) (Object) this;
    if (!(behaviour.getWorld() instanceof ServerLevel level)) return;
    var pos = behaviour.getPos();
    if (!TrackGeometry.ticking(level, pos)
        && FoundationData.get(level).rails.stream()
            .anyMatch(rail -> rail.from.equals(pos) || rail.to.equals(pos))) cir.setReturnValue(0);
  }
}
