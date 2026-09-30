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

  public TrackAppearance effective() {
    return AppearanceProviders.definition(this) == null ? ORIGINAL : this;
  }

  public TrackAppearance effective(dev.fourthdimension.contract.Family family) {
    var definition = AppearanceProviders.definition(this);
    return definition != null && definition.families().contains(family) ? this : ORIGINAL;
  }

  public Map<Component, String> renderOwners() {
    var owners = new EnumMap<Component, String>(Component.class);
    for (var component : Component.values()) owners.put(component, TrackProviders.ORIGINAL);
    var definition = AppearanceProviders.definition(this);
    if (definition != null)
      for (var component : definition.components()) owners.put(component, provider);
    return Map.copyOf(owners);
  }
}
