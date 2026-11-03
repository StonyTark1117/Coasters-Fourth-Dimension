package dev.fourthdimension.client.integration;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

/** Per-rail quarantine for a provider draw failure, reset by a rail or resource-model revision. */
public final class TrackStylesFallbackState {
  private record Failure(long generation, long revision) {}

  private final HashMap<UUID, Failure> failures = new HashMap<>();

  public boolean quarantines(UUID rail, long generation, long revision) {
    return new Failure(generation, revision).equals(failures.get(rail));
  }

  public void quarantine(UUID rail, long generation, long revision) {
    failures.put(rail, new Failure(generation, revision));
  }

  public void retain(Set<UUID> rails) {
    failures.keySet().retainAll(rails);
  }

  public void clear() {
    failures.clear();
  }
}
