package dev.fourthdimension.integration;

import java.util.Map;
import java.util.Objects;

/** Cosmetic providers may own decoration; contact/control/magnetic geometry stays authoritative. */
public record TrackRenderOwnership(Map<Component, String> owners) {
  public static final String ORIGINAL = "fourthdimension:original";

  public enum Component {
    SPINE,
    TIES,
    RUNNING_RAILS,
    RUNNING_SUPPORTS,
    CONTROL_RAILS,
    MAGNETIC_EQUIPMENT
  }

  public TrackRenderOwnership {
    owners = Map.copyOf(owners);
    for (var component : Component.values()) {
      String owner = owners.get(component);
      if (owner == null || owner.isBlank())
        throw new IllegalArgumentException("Every rendered component requires exactly one owner");
      if (component != Component.SPINE && component != Component.TIES && !ORIGINAL.equals(owner))
        throw new IllegalArgumentException("An appearance provider cannot own mechanical geometry");
    }
  }

  public static TrackRenderOwnership original() {
    return decoration(ORIGINAL);
  }

  public static TrackRenderOwnership decoration(String provider) {
    Objects.requireNonNull(provider);
    return new TrackRenderOwnership(
        Map.of(
            Component.SPINE, provider,
            Component.TIES, provider,
            Component.RUNNING_RAILS, ORIGINAL,
            Component.RUNNING_SUPPORTS, ORIGINAL,
            Component.CONTROL_RAILS, ORIGINAL,
            Component.MAGNETIC_EQUIPMENT, ORIGINAL));
  }

  public boolean originalOwns(Component component) {
    return ORIGINAL.equals(owners.get(component));
  }
}
