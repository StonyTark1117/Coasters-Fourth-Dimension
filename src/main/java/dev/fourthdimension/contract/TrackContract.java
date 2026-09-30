package dev.fourthdimension.contract;

import java.util.Objects;
import java.util.Set;

/** Appearance IDs have deliberately no part in eligibility. A provider must declare mechanics. */
public record TrackContract(
    String provider,
    Family family,
    String variant,
    Gear mainRails,
    Set<Mechanism> mechanisms,
    Clearance clearance) {
  public enum Mechanism {
    CONTROL_RAILS,
    ROTARY_CLEARANCE,
    MAGNET_INTERFACE,
    SHUTTLE_CAPTURE
  }

  public TrackContract {
    Objects.requireNonNull(family);
    Objects.requireNonNull(mainRails);
    Objects.requireNonNull(clearance);
    if (provider == null || provider.isBlank() || variant == null || variant.isBlank())
      throw new IllegalArgumentException("Missing functional identity");
    mechanisms = Set.copyOf(mechanisms);
  }

  public record Gear(double gauge, double runningRadius, double sideContact, double upstopContact) {
    public Gear {
      finitePositive(gauge);
      finitePositive(runningRadius);
      finitePositive(sideContact);
      finitePositive(upstopContact);
    }

    public boolean fits(Gear other) {
      return close(gauge, other.gauge)
          && close(runningRadius, other.runningRadius)
          && close(sideContact, other.sideContact)
          && close(upstopContact, other.upstopContact);
    }

    private static boolean close(double a, double b) {
      return Math.abs(a - b) <= 1e-6;
    }
  }

  public record Clearance(double halfWidth, double aboveRail, double belowRail) {
    public Clearance {
      finitePositive(halfWidth);
      finitePositive(aboveRail);
      finitePositive(belowRail);
    }

    public boolean contains(Clearance envelope) {
      return halfWidth >= envelope.halfWidth
          && aboveRail >= envelope.aboveRail
          && belowRail >= envelope.belowRail;
    }
  }

  private static void finitePositive(double x) {
    if (!Double.isFinite(x) || x <= 0)
      throw new IllegalArgumentException("Expected finite positive dimension");
  }
}
