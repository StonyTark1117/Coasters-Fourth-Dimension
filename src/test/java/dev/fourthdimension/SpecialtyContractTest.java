package dev.fourthdimension;

import dev.fourthdimension.contract.*;
import dev.fourthdimension.contract.TrackContract.*;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpecialtyContractTest {
  private static final Gear GEAR = new Gear(1.544, .2, .9375, .55);
  private static final Clearance ENVELOPE = new Clearance(2.25, 4, 4.5);

  @Test void standingVariantsCannotExchangeTrackOrLoseCapture() {
    var car = new VehicleContract(Family.STANDING, "standing_surf_v1", GEAR, ENVELOPE, Set.of(), 1);
    assertEquals(VehicleContract.Eligibility.FIXED_SEAT, car.evaluate(track(Family.STANDING, "standing_surf_v1", Set.of(Mechanism.STATION_CAPTURE))));
    assertEquals(VehicleContract.Eligibility.WRONG_FAMILY, car.evaluate(track(Family.STANDING, "standing_standard_v1", Set.of(Mechanism.STATION_CAPTURE))));
    assertEquals(VehicleContract.Eligibility.WRONG_FAMILY, car.evaluate(track(Family.PIPELINE, "seated_side_rail_v1", Set.of(Mechanism.STATION_CAPTURE))));
    assertEquals(VehicleContract.Eligibility.MISSING_MECHANISM, car.evaluate(track(Family.STANDING, "standing_surf_v1", Set.of())));
  }

  @Test void flyingBikeNeedsItsOwnMountAndLaunchAuthority() {
    var car = new VehicleContract(Family.FLYING_BIKE, "prone_multilaunch_v1", GEAR, ENVELOPE, Set.of(), 1);
    var required = Set.of(Mechanism.STATION_CAPTURE, Mechanism.OVERHEAD_MOUNT, Mechanism.LONGITUDINAL_DRIVE);
    assertEquals(VehicleContract.Eligibility.FIXED_SEAT, car.evaluate(track(Family.FLYING_BIKE, "prone_multilaunch_v1", required)));
    for (var missing : required) {
      var incomplete = new java.util.HashSet<>(required);
      incomplete.remove(missing);
      assertEquals(VehicleContract.Eligibility.MISSING_MECHANISM, car.evaluate(track(Family.FLYING_BIKE, "prone_multilaunch_v1", incomplete)));
    }
    assertEquals(VehicleContract.Eligibility.WRONG_FAMILY, car.evaluate(track(Family.FLYING, "station_changeover_v1", required)));
  }

  private static TrackContract track(Family family, String variant, Set<Mechanism> mechanisms) {
    return new TrackContract("fourthdimension:original", family, variant, GEAR, mechanisms, ENVELOPE);
  }
}
