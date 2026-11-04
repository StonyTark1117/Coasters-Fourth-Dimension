package dev.fourthdimension.integration;

import com.mojang.logging.LogUtils;
import dev.fourthdimension.contract.TrackContract;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackDesign;
import dev.fourthdimension.track.TrackGeometry;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

/** Register on both physical sides before load-complete. Appearance never grants mechanics. */
public final class TrackProviders {
  public static final String ORIGINAL = "fourthdimension:original";

  public interface Provider {
    TrackContract contract(String variant);

    /** Return the native edge for these exact endpoints, or null while it is unavailable. */
    CoasterPathEdge resolve(Level level, FoundationData.Rail rail);
  }

  private static final Map<String, Provider> PROVIDERS = new LinkedHashMap<>();
  private static final Set<String> REPORTED = new HashSet<>();
  private static volatile boolean frozen;

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
      throw new IllegalArgumentException("Invalid functional provider identity");
    return id;
  }

  public static synchronized void register(String id, Provider provider) {
    identity(id);
    if (frozen) throw new IllegalStateException("Track provider registration is closed");
    if (FoundationData.PROVIDER.equals(id)
        || PROVIDERS.putIfAbsent(id, Objects.requireNonNull(provider)) != null)
      throw new IllegalArgumentException("Duplicate or reserved track provider: " + id);
  }

  /** The factory is never evaluated for an absent or different release. No optional API imports. */
  public static synchronized boolean registerOptional(
      String id, String modId, String exactVersion, Supplier<? extends Provider> factory) {
    identity(id);
    Objects.requireNonNull(factory);
    if (frozen) throw new IllegalStateException("Track provider registration is closed");
    if (modId == null || modId.isBlank() || exactVersion == null || exactVersion.isBlank())
      throw new IllegalArgumentException("An optional provider requires an exact mod release");
    var installed = ModList.get().getModContainerById(modId);
    if (installed.isEmpty()
        || !exactVersion.equals(installed.get().getModInfo().getVersion().toString())) return false;
    final Provider adapter;
    try {
      adapter = Objects.requireNonNull(factory.get());
    } catch (RuntimeException | LinkageError failure) {
      report(id, failure);
      return false;
    }
    register(id, adapter);
    return true;
  }

  public static synchronized void freeze() {
    frozen = true;
  }

  public static boolean available(FoundationData.Rail rail) {
    if (FoundationData.VARIANT.equals(rail.variant))
      return FoundationData.PROVIDER.equals(rail.provider);
    // The built-in immutable contract is fixed by Rail's validated family/variant constructor.
    // Avoid rebuilding its dimensions and mechanism sets in every native snap callback.
    if (rail.original()) return true;
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
    } catch (RuntimeException | LinkageError failure) {
      report(rail.provider, failure);
      return false;
    }
  }

  public static CoasterPathEdge resolve(Level level, FoundationData.Rail rail) {
    if (!available(rail) || !level.hasChunkAt(rail.from) || !level.hasChunkAt(rail.to)) return null;
    try {
      var edge =
          FoundationData.PROVIDER.equals(rail.provider)
              ? TrackGeometry.resolveNative(level, rail)
              : PROVIDERS.get(rail.provider).resolve(level, rail);
      return edge != null && rail.matches(edge.from(), edge.to()) ? edge : null;
    } catch (RuntimeException | LinkageError failure) {
      report(rail.provider, failure);
      return null;
    }
  }

  private static synchronized void report(String provider, Throwable failure) {
    if (REPORTED.add(provider))
      LogUtils.getLogger().warn("Functional track provider {} unavailable", provider, failure);
  }
}
