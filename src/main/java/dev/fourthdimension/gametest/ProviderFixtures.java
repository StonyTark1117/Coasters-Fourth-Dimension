package dev.fourthdimension.gametest;

import dev.fourthdimension.contract.*;
import dev.fourthdimension.integration.TrackProviders;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge;
import java.util.Set;
import net.minecraft.world.level.Level;

/** Synthetic providers, registered only with the explicit providerFixtures launch property. */
public final class ProviderFixtures {
  public static final String GOOD = "fdtest:compatible";
  public static final String[] BAD = {
    "identity",
    "family",
    "variant",
    "gear",
    "clearance",
    "mechanisms",
    "contract_error",
    "resolve_error",
    "wrong_edge"
  };
  public static boolean absentSkipped,
      versionSkipped,
      exactAccepted,
      duplicateRejected,
      reservedRejected;
  public static int factories;
  public static boolean failedFactorySkipped;

  private ProviderFixtures() {}

  public static void register() {
    absentSkipped =
        !TrackProviders.registerOptional(
            "fdtest:absent",
            "fdtest_missing_mod",
            "1",
            () -> {
              factories++;
              return provider("fdtest:absent", "good");
            });
    versionSkipped =
        !TrackProviders.registerOptional(
            "fdtest:unverified",
            "create",
            "0.unverified",
            () -> {
              factories++;
              return provider("fdtest:unverified", "good");
            });
    exactAccepted =
        TrackProviders.registerOptional(
            GOOD,
            "create",
            "6.0.10",
            () -> {
              factories++;
              return provider(GOOD, "good");
            });
    failedFactorySkipped =
        !TrackProviders.registerOptional(
            "fdtest:failed_factory",
            "create",
            "6.0.10",
            () -> {
              factories++;
              throw new NoClassDefFoundError("Synthetic unavailable adapter initializer");
            });
    for (var mode : BAD)
      TrackProviders.register("fdtest:" + mode, provider("fdtest:" + mode, mode));
    try {
      TrackProviders.register(GOOD, provider(GOOD, "good"));
    } catch (IllegalArgumentException expected) {
      duplicateRejected = true;
    }
    try {
      TrackProviders.register(FoundationData.PROVIDER, provider(FoundationData.PROVIDER, "good"));
    } catch (IllegalArgumentException expected) {
      reservedRejected = true;
    }
  }

  private static TrackProviders.Provider provider(String id, String mode) {
    return new TrackProviders.Provider() {
      public TrackContract contract(String variant) {
        if (mode.equals("contract_error"))
          throw new NoClassDefFoundError("Synthetic absent optional API");
        var base = TrackDesign.byVariant(variant).contract();
        return new TrackContract(
            mode.equals("identity") ? "fdtest:other" : id,
            mode.equals("family")
                ? (base.family() == Family.ARROW ? Family.PAX : Family.ARROW)
                : base.family(),
            mode.equals("variant") ? "wrong_variant" : variant,
            mode.equals("gear") ? new TrackContract.Gear(999, 1, 1, 1) : base.mainRails(),
            mode.equals("mechanisms") ? Set.of() : base.mechanisms(),
            mode.equals("clearance") ? new TrackContract.Clearance(.1, .1, .1) : base.clearance());
      }

      public CoasterPathEdge resolve(Level level, FoundationData.Rail rail) {
        if (mode.equals("resolve_error"))
          throw new IllegalStateException("Synthetic failed optional resolver");
        if (mode.equals("wrong_edge"))
          return TrackGeometry.resolveNative(
              level,
              new FoundationData.Rail(
                  rail.from.offset(24, 0, 0), rail.to.offset(24, 0, 0), rail.family, rail.profile));
        return TrackGeometry.resolveNative(level, rail);
      }
    };
  }
}
