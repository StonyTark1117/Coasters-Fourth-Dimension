package dev.fourthdimension.mixin;

import com.simibubi.create.content.trains.track.BezierConnection;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackPlacement;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CoasterAnchorpointBlockEntity.class, remap = false)
public abstract class TrackMutationMixin {
  @Inject(
      method = {"setJointBankRadians", "setLiftBlocks", "setOpenEndExtensionBlocks"},
      at = @At("HEAD"),
      cancellable = true)
  private void fourthdimension$guardAnchor(float value, CallbackInfo ci) {
    if (!Float.isFinite(value) || !fourthdimension$editAnchor()) ci.cancel();
  }

  @Inject(
      method = {"clearLift", "clearOpenEndExtension"},
      at = @At("HEAD"),
      cancellable = true)
  private void fourthdimension$guardReset(CallbackInfo ci) {
    if (!fourthdimension$editAnchor()) ci.cancel();
  }

  @org.spongepowered.asm.mixin.Unique
  private boolean fourthdimension$editAnchor() {
    var self = (CoasterAnchorpointBlockEntity) (Object) this;
    if (!(self.getLevel() instanceof ServerLevel level)) return true;
    var data = FoundationData.get(level);
    var affected =
        data.rails.stream()
            .filter(
                r ->
                    r.original()
                        && (r.from.equals(self.getBlockPos()) || r.to.equals(self.getBlockPos())))
            .toList();
    if (affected.stream().anyMatch(r -> TrackPlacement.occupied(data, r))) return false;
    for (var rail : affected) {
      rail.valid = false;
      rail.revision++;
    }
    if (!affected.isEmpty()) data.setDirty();
    return true;
  }

  @Inject(method = "putAnchorPeerCurve", at = @At("HEAD"), cancellable = true)
  private void fourthdimension$guardEdit(
      ServerLevel level, BlockPos peer, BezierConnection curve, CallbackInfo ci) {
    var self = (CoasterAnchorpointBlockEntity) (Object) this;
    var data = FoundationData.get(level);
    for (var rail : data.rails)
      if (rail.original() && rail.matches(self.getBlockPos(), peer)) {
        if (TrackPlacement.occupied(data, rail)) {
          ci.cancel();
          return;
        }
        rail.valid = false;
        rail.revision++;
        data.setDirty();
      }
  }

  @Inject(method = "removeAnchorPeerCurve", at = @At("HEAD"))
  private void fourthdimension$invalidateRemoved(
      ServerLevel level, BlockPos peer, CallbackInfo ci) {
    var self = (CoasterAnchorpointBlockEntity) (Object) this;
    var data = FoundationData.get(level);
    for (var rail : data.rails)
      if (rail.original() && rail.matches(self.getBlockPos(), peer)) {
        rail.valid = false;
        rail.revision++;
        data.setDirty();
      }
  }
}
