package dev.fourthdimension.integration;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.neoforged.fml.ModList;

/** Do not resolve optional classes until both mod presence and exact artifact version match. */
public final class OptionalIntegrations {
  private OptionalIntegrations() {}

  public static boolean supported(String modId, Set<String> versions) {
    return ModList.get()
        .getModContainerById(modId)
        .map(mod -> versions.contains(mod.getModInfo().getVersion().toString()))
        .orElse(false);
  }

  public static <T> Optional<T> load(String modId, Set<String> versions, Supplier<T> adapter) {
    return supported(modId, versions) ? Optional.of(adapter.get()) : Optional.empty();
  }
}
