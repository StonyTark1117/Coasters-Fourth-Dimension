package dev.fourthdimension.integration;

import dev.fourthdimension.contract.TrackContract;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackDesign;
import dev.fourthdimension.track.TrackGeometry;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/** Functional providers must be registered before worlds load, on both physical sides. */
public final class TrackProviders {
  public static final String ORIGINAL = "fourthdimension:original";

  public interface Provider {
    TrackContract contract(String variant);

    CoasterPathEdge resolve(Level level, FoundationData.Rail rail);
  }

  private static final Map<String, Provider> PROVIDERS = new LinkedHashMap<>();
  private static boolean frozen;

  static {
    register(
        ORIGINAL,
        new Provider() {
          public TrackContract contract(String variant) {
            return TrackDesign.byVariant(variant).contract();
          }

          public CoasterPathEdge resolve(Level level, FoundationData.Rail rail) {
            return TrackGeometry.resolveNative(level, rail);
          }
        });
  }

  private TrackProviders() {}

  public static String identity(String id) {
    if (id == null
        || id.length() > 256
        || !id.contains(":")
        || ResourceLocation.tryParse(id) == null)
      throw new IllegalArgumentException("Invalid provider or style identity");
    return id;
  }

  public static synchronized void register(String id, Provider provider) {
    identity(id);
    if (frozen) throw new IllegalStateException("Track provider registration is closed");
    if (FoundationData.PROVIDER.equals(id)
        || PROVIDERS.putIfAbsent(id, Objects.requireNonNull(provider)) != null)
      throw new IllegalArgumentException("Duplicate or reserved track provider: " + id);
  }

  public static synchronized void freeze() {
    frozen = true;
  }

  public static boolean available(FoundationData.Rail rail) {
    if (FoundationData.VARIANT.equals(rail.variant))
      return FoundationData.PROVIDER.equals(rail.provider);
    var provider = PROVIDERS.get(rail.provider);
    if (provider == null) return false;
    try {
      var actual = provider.contract(rail.variant);
      var expected = TrackDesign.byVariant(rail.variant).contract();
      return actual != null
          && actual.provider().equals(rail.provider)
          && actual.family() == rail.family
          && actual.variant().equals(rail.variant)
          && actual.mainRails().fits(expected.mainRails())
          && actual.clearance().contains(expected.clearance())
          && actual.mechanisms().containsAll(expected.mechanisms());
    } catch (IllegalArgumentException unsupported) {
      return false;
    }
  }

  public static CoasterPathEdge resolve(Level level, FoundationData.Rail rail) {
    if (!available(rail)) return null;
    return FoundationData.PROVIDER.equals(rail.provider)
        ? TrackGeometry.resolveNative(level, rail)
        : PROVIDERS.get(rail.provider).resolve(level, rail);
  }
}
