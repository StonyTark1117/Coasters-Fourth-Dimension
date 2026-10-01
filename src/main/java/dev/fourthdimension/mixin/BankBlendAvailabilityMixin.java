package dev.fourthdimension.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackGeometry;
import dev.silvergold.simulatedcoasters.track.BezierBankBlend;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Bank interpolation may inspect a long chain, but must not load its inactive 4D anchors. */
@Mixin(value = BezierBankBlend.class, remap = false)
public abstract class BankBlendAvailabilityMixin {
  @WrapOperation(
      method = {"jointBankRadiansAt", "neighbourCoasterCurveAtAnchor"},
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
  private static BlockEntity fourthdimension$residentBank(
      Level level, BlockPos pos, Operation<BlockEntity> original) {
    if (level instanceof ServerLevel server
        && !TrackGeometry.ticking(server, pos)
        && FoundationData.get(server).rails.stream()
            .anyMatch(r -> r.from.equals(pos) || r.to.equals(pos))) {
      var chunk = server.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
      // Read existing objects only; do not instantiate an entity in a departing chunk.
      var anchor = chunk == null ? null : chunk.getBlockEntities().get(pos);
      return anchor == null || anchor.isRemoved() ? null : anchor;
    }
    return original.call(level, pos);
  }
}
