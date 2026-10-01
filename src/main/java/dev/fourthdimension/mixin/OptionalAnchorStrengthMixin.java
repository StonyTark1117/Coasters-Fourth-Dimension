package dev.fourthdimension.mixin;

import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import dev.fourthdimension.runtime.FoundationData;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Preserve inactive Magnetized strength on our anchors across optional-mod removal saves. */
@Mixin(value = CoasterAnchorpointBlockEntity.class, remap = false)
public abstract class OptionalAnchorStrengthMixin {
  @Unique private Integer fourthdimension$inactiveStrength;

  @Inject(method = "read", at = @At("TAIL"))
  private void fourthdimension$rememberStrength(
      CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
    // Absence of the field clears the remembered value, including intentional data removal.
    int strength = tag.getInt("ScrollValue");
    fourthdimension$inactiveStrength =
        tag.contains("ScrollValue", Tag.TAG_INT) && strength >= 0 && strength <= 100
            ? strength
            : null;
  }

  @Inject(method = "write", at = @At("TAIL"))
  private void fourthdimension$retainStrength(
      CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
    var anchor = (CoasterAnchorpointBlockEntity) (Object) this;
    if (clientPacket
        || fourthdimension$inactiveStrength == null
        || ModList.get().isLoaded("coastersmagnetized")
        || anchor.getBehaviour(ScrollValueBehaviour.TYPE) != null
        || tag.contains("ScrollValue")
        || !(anchor.getLevel() instanceof ServerLevel level)) return;
    var pos = anchor.getBlockPos();
    if (FoundationData.get(level).rails.stream()
        .anyMatch(rail -> rail.from.equals(pos) || rail.to.equals(pos)))
      tag.putInt("ScrollValue", fourthdimension$inactiveStrength);
  }
}
