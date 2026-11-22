package dev.fourthdimension.contract;

import dev.fourthdimension.contract.TrackContract.*;
import java.util.Objects;
import java.util.Set;

public record VehicleContract(
    Family family,
    String variant,
    Gear runningGear,
    Clearance sweptEnvelope,
    Set<Mechanism> requiredMechanisms,
    int carrierGroups) {
  public VehicleContract {
    Objects.requireNonNull(variant);
    Objects.requireNonNull(runningGear);
    Objects.requireNonNull(sweptEnvelope);
    requiredMechanisms = Set.copyOf(requiredMechanisms);
    if (carrierGroups < 0
        || (family == null && (carrierGroups != 0 || !requiredMechanisms.isEmpty()))
        || (family != null && carrierGroups == 0))
      throw new IllegalArgumentException("Invalid vehicle role");
  }

  public boolean ordinary() {
    return family == null;
  }

  public Eligibility evaluate(TrackContract track) {
    if (family == Family.WING) {
      if (track.family() != null) return Eligibility.WRONG_FAMILY;
      if (!runningGear.fits(track.mainRails())) return Eligibility.CONTACT_MISMATCH;
      if (!track.clearance().contains(sweptEnvelope)) return Eligibility.CLEARANCE_MISMATCH;
      return Eligibility.FIXED_SEAT;
    }
    if (!ordinary() && (family != track.family() || !variant.equals(track.variant())))
      return Eligibility.WRONG_FAMILY;
    if (!runningGear.fits(track.mainRails())) return Eligibility.CONTACT_MISMATCH;
    if (!track.clearance().contains(sweptEnvelope)) return Eligibility.CLEARANCE_MISMATCH;
    if (!ordinary() && family.rotary() && !track.mechanisms().contains(Mechanism.ROTARY_CLEARANCE))
      return Eligibility.MISSING_MECHANISM;
    if (family == Family.ARROW && !track.mechanisms().contains(Mechanism.CONTROL_RAILS))
      return Eligibility.MISSING_MECHANISM;
    if ((family == Family.SPINNING || family == Family.SUSPENDED || family == Family.FIXED_INVERTED) && !track.mechanisms().contains(Mechanism.STATION_CAPTURE))
      return Eligibility.MISSING_MECHANISM;
    if (family != null && family.specialty() && !track.mechanisms().contains(Mechanism.STATION_CAPTURE))
      return Eligibility.MISSING_MECHANISM;
    if (family == Family.FLYING_BIKE && !track.mechanisms().containsAll(Set.of(Mechanism.OVERHEAD_MOUNT, Mechanism.LONGITUDINAL_DRIVE)))
      return Eligibility.MISSING_MECHANISM;
    if (family == Family.FIXED_INVERTED && !track.mechanisms().contains(Mechanism.OVERHEAD_MOUNT))
      return Eligibility.MISSING_MECHANISM;
    if (family == Family.FLYING && "route_changeover_yaw_v1".equals(variant)
        && !track.mechanisms().contains(Mechanism.YAW))
      return Eligibility.MISSING_MECHANISM;
    if (family == Family.FLYING && !track.mechanisms().containsAll(Set.of(Mechanism.POSTURE_LOCK, Mechanism.OVERHEAD_MOUNT, Mechanism.STATION_CAPTURE)))
      return Eligibility.MISSING_MECHANISM;
    if (family == Family.AXIS && !track.mechanisms().containsAll(Set.of(Mechanism.ROTATION_DRIVE, Mechanism.STATION_CAPTURE, Mechanism.LIFT_LOCK)))
      return Eligibility.MISSING_MECHANISM;
    if (!track.mechanisms().containsAll(requiredMechanisms)) return Eligibility.MISSING_MECHANISM;
    return ordinary() ? Eligibility.ORDINARY_RAIL_ACCESS : !family.rotary() ? Eligibility.FIXED_SEAT : Eligibility.FOUR_D;
  }

  public enum Eligibility {
    WRONG_FAMILY,
    CONTACT_MISMATCH,
    CLEARANCE_MISMATCH,
    MISSING_MECHANISM,
    FIXED_SEAT,
    ORDINARY_RAIL_ACCESS,
    FOUR_D;

    public boolean allowed() {
      return this == ORDINARY_RAIL_ACCESS || this == FOUR_D || this == FIXED_SEAT;
    }
  }
}
