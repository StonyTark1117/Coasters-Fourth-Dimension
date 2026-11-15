package dev.fourthdimension.runtime;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Local preference only; never synchronized to or consulted by the server. */
public final class RiderComfortSettings {
  public static final ModConfigSpec SPEC;
  public static final ModConfigSpec.BooleanValue ENABLED;
  static {
    var builder = new ModConfigSpec.Builder();
    ENABLED = builder.comment("Keep a world-upright camera heading during a ride; mouse look remains free.",
        "K toggles this local preference. Physical riders and vehicles still follow the ride.")
        .define("worldStableRiderView", false);
    SPEC = builder.build();
  }
  private RiderComfortSettings() {}
}
