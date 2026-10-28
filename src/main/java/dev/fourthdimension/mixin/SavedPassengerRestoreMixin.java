package dev.fourthdimension.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.fourthdimension.vehicle.Boarding;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** EntityType has one startRiding call: reconstruction of a saved Passengers relation. */
@Mixin(EntityType.class)
public abstract class SavedPassengerRestoreMixin {
  @WrapOperation(
      method = "*",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/entity/Entity;startRiding(Lnet/minecraft/world/entity/Entity;Z)Z"),
      require = 1,
      allow = 1)
  private static boolean fourthdimension$restoreSavedPassenger(
      Entity passenger, Entity seat, boolean force, Operation<Boolean> original) {
    return Boarding.restoreSavedPassenger(
        passenger, seat, () -> original.call(passenger, seat, force));
  }
}
