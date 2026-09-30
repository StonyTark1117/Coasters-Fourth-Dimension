package dev.fourthdimension.integration;

import java.util.*;

/** Saved appearance never grants a functional contract. Missing appearances use our full mesh. */
public record TrackAppearance(String provider, String style) {
  public static final TrackAppearance ORIGINAL =
      new TrackAppearance(TrackProviders.ORIGINAL, "fourthdimension:family_default");

  public enum Component {
    SPINE,
    TIES,
    RUNNING_RAILS,
    CONTROL_RAILS,
    MAGNETIC_EQUIPMENT
  }

  public TrackAppearance {
    TrackProviders.identity(provider);
    TrackProviders.identity(style);
  }

  // External appearances are retained in saves but remain inactive until an adapter is certified.
  public TrackAppearance effective() {
    return ORIGINAL;
  }

  public Map<Component, String> renderOwners() {
    var owners = new EnumMap<Component, String>(Component.class);
    for (var component : Component.values()) owners.put(component, TrackProviders.ORIGINAL);
    return Map.copyOf(owners);
  }
}
