package dev.fourthdimension.mixin.optional;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import dev.fourthdimension.integration.additions.AdditionsStation;
import dev.plushivr.coasterfins.block.StationBlockEntity;
import dev.ryanhcode.sable.sublevel.SubLevel;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Pseudo
@Mixin(targets = "dev.plushivr.coasterfins.event.StationHandler", remap = false)
public abstract class AdditionsStationHandlerMixin {
  @WrapOperation(
      method = "apply",
      at =
          @At(
              value = "INVOKE",
              target = "Ldev/plushivr/coasterfins/block/StationBlockEntity;setOccupied(ZIZ)V",
              ordinal = 2))
  private static void fourthdimension$station(
      StationBlockEntity station,
      boolean occupied,
      int weight,
      boolean preWaiting,
      Operation<Void> original,
      @Local(name = "counted") java.util.Set<SubLevel> counted) {
    if (!((AdditionsStation.Access) station)
        .fourthdimension$station()
        .update(station, counted, preWaiting)) original.call(station, occupied, weight, preWaiting);
  }

  @WrapOperation(
      method = "apply",
      at = {
        @At(
            value = "INVOKE",
            target = "Ldev/plushivr/coasterfins/block/StationBlockEntity;setOccupied(ZIZ)V",
            ordinal = 0),
        @At(
            value = "INVOKE",
            target = "Ldev/plushivr/coasterfins/block/StationBlockEntity;setOccupied(ZIZ)V",
            ordinal = 1)
      })
  private static void fourthdimension$clear(
      StationBlockEntity station,
      boolean occupied,
      int weight,
      boolean preWaiting,
      Operation<Void> original) {
    ((AdditionsStation.Access) station).fourthdimension$station().reset(station);
    original.call(station, occupied, weight, preWaiting);
  }
}
