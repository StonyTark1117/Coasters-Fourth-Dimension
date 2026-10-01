package dev.fourthdimension.mixin.optional;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import dev.fourthdimension.integration.RideOperations;
import dev.fourthdimension.integration.RideTelemetry;
import dev.fourthdimension.integration.additions.AdditionsStation;
import dev.fourthdimension.integration.create.RideInformation;
import java.util.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.plushivr.coasterfins.block.StationBlockEntity", remap = false)
public abstract class AdditionsStationMixin
    implements AdditionsStation.Access,
        RideInformation.Source,
        IHaveGoggleInformation,
        RideOperations.SpanController {
  @Unique private final AdditionsStation fourthdimension$state = new AdditionsStation();

  public AdditionsStation fourthdimension$station() {
    return fourthdimension$state;
  }

  public RideTelemetry telemetry() {
    return fourthdimension$state.telemetry;
  }

  public boolean authorizesRide(UUID chassis) {
    return fourthdimension$state.authorizes(chassis);
  }

  public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
    return RideInformation.goggles(tooltip, telemetry());
  }

  @Inject(method = "write", at = @At("TAIL"))
  private void fourthdimension$write(
      CompoundTag tag, HolderLookup.Provider registries, boolean packet, CallbackInfo ci) {
    if (packet) tag.put("fourthdimension_ride", telemetry().save());
  }

  @Inject(method = "read", at = @At("TAIL"))
  private void fourthdimension$read(
      CompoundTag tag, HolderLookup.Provider registries, boolean packet, CallbackInfo ci) {
    if (packet)
      fourthdimension$state.telemetry = RideTelemetry.load(tag.getCompound("fourthdimension_ride"));
  }
}
