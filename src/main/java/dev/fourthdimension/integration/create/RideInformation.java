package dev.fourthdimension.integration.create;

import dev.fourthdimension.integration.RideTelemetry;
import java.util.List;
import net.minecraft.network.chat.Component;

public final class RideInformation {
  private RideInformation() {}

  public interface Source {
    RideTelemetry telemetry();
  }

  public static boolean goggles(List<Component> tooltip, RideTelemetry ride) {
    line(tooltip, Component.translatable("fourthdimension.telemetry.title"));
    if (!ride.available()) {
      line(tooltip, Component.literal("  " + ride.fault()));
      return true;
    }
    line(
        tooltip,
        Component.translatable(
            "fourthdimension.telemetry.occupancy", ride.occupied(), ride.capacity(), ride.cars()));
    line(tooltip, Component.translatable("fourthdimension.telemetry.phase", phase(ride)));
    line(
        tooltip,
        Component.translatable(
            "fourthdimension.telemetry.restraints", yesNo(ride.restraintsClosed())));
    line(
        tooltip,
        Component.translatable("fourthdimension.telemetry.aligned", yesNo(ride.boardingReady())));
    line(
        tooltip,
        Component.translatable("fourthdimension.telemetry.retained", yesNo(ride.retained())));
    line(
        tooltip,
        Component.translatable("fourthdimension.telemetry.dispatch", yesNo(ride.dispatchReady())));
    if (!ride.fault().isBlank()) line(tooltip, Component.literal("  " + ride.fault()));
    if (!ride.dispatchDenial().isBlank() && !ride.dispatchDenial().equals(ride.fault()))
      line(tooltip, Component.literal("  " + ride.dispatchDenial()));
    return true;
  }

  private static void line(List<Component> tooltip, Component text) {
    tooltip.add(Component.literal("    ").append(text));
  }

  public static Component yesNo(boolean value) {
    return Component.translatable("fourthdimension.telemetry." + (value ? "yes" : "no"));
  }

  public static Component phase(RideTelemetry ride) {
    return Component.translatable(
        "fourthdimension.phase." + ride.phase().toLowerCase(java.util.Locale.ROOT));
  }
}
