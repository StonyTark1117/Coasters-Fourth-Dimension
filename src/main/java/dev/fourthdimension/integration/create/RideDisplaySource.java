package dev.fourthdimension.integration.create;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import dev.fourthdimension.integration.RideTelemetry;
import net.minecraft.network.chat.*;

public final class RideDisplaySource extends SingleLineDisplaySource {
  public enum Field {
    STATUS,
    OCCUPANCY,
    RESTRAINTS,
    DISPATCH,
    PHASE,
    FAULT
  }

  private final Field field;

  public RideDisplaySource(Field field) {
    this.field = field;
  }

  public MutableComponent text(RideTelemetry ride) {
    if (!ride.available()) return Component.literal(ride.fault());
    return switch (field) {
      case STATUS ->
          !ride.fault().isBlank()
              ? Component.literal(ride.fault())
              : ride.dispatchReady()
                  ? Component.translatable("fourthdimension.telemetry.ready")
                  : RideInformation.phase(ride).copy();
      case OCCUPANCY -> Component.literal(ride.occupied() + " / " + ride.capacity());
      case RESTRAINTS ->
          Component.translatable(
              "fourthdimension.telemetry.restraints",
              RideInformation.yesNo(ride.restraintsClosed()));
      case DISPATCH ->
          ride.dispatchReady()
              ? Component.translatable("fourthdimension.telemetry.ready")
              : Component.literal(ride.dispatchDenial());
      case PHASE -> RideInformation.phase(ride).copy();
      case FAULT -> Component.literal(ride.fault());
    };
  }

  @Override
  public Component getName() {
    return Component.translatable(
        "display_source.fourthdimension.ride_" + field.name().toLowerCase(java.util.Locale.ROOT));
  }

  @Override
  protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
    return context.getSourceBlockEntity() instanceof RideInformation.Source source
        ? text(source.telemetry())
        : Component.translatable("fourthdimension.telemetry.unavailable");
  }

  @Override
  protected boolean allowsLabeling(DisplayLinkContext context) {
    return true;
  }

  @Override
  public int getPassiveRefreshTicks() {
    return 10;
  }
}
