package dev.fourthdimension;

import static org.junit.jupiter.api.Assertions.*;

import dev.fourthdimension.contract.*;
import dev.fourthdimension.contract.TrackContract.*;
import dev.fourthdimension.physics.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class FoundationMathTest {
  private final Gear gear = new Gear(1, .1, .5, .2);
  private final Clearance clearance = new Clearance(4, 4, 4);

  @Test
  void fullTurnsAndRollbackAreDistanceBased() {
    var p =
        new RotationProfile(
            List.of(new RotationProfile.Key(0, 0), new RotationProfile.Key(10, 4 * Math.PI)));
    assertEquals(4 * Math.PI, p.angleAt(10));
    assertEquals(2 * Math.PI, p.angleAt(5));
    double outbound = p.angleAt(3);
    p.angleAt(8);
    assertEquals(outbound, p.angleAt(3));
    assertEquals(4 * Math.PI + .1, FreeRotor.unwrap(4 * Math.PI - .1, .1), 1e-10);
  }

  @Test
  void allFiveFamiliesAreStrictAndProvidersMayBeEquivalent() {
    for (Family a : java.util.Arrays.stream(Family.values()).filter(Family::rotary).toList())
      for (Family b : java.util.Arrays.stream(Family.values()).filter(Family::rotary).toList()) {
        var cart = new VehicleContract(a, "v1", gear, clearance, Set.of(), 2);
        var track =
            new TrackContract(
                "external:verified", b, "v1", gear, EnumSet.allOf(Mechanism.class), clearance);
        assertEquals(a == b, cart.evaluate(track).allowed());
      }
  }

  @Test
  void ordinaryAccessHasNoFourDEffectsAndGaugeAloneIsInsufficient() {
    var track = new TrackContract("fd:original", Family.ARROW, "v1", gear, Set.of(), clearance);
    var cart = new VehicleContract(null, "ordinary", gear, clearance, Set.of(), 0);
    assertEquals(VehicleContract.Eligibility.ORDINARY_RAIL_ACCESS, cart.evaluate(track));
    var bad =
        new VehicleContract(null, "ordinary", new Gear(1, .1, .5, .3), clearance, Set.of(), 0);
    assertEquals(VehicleContract.Eligibility.CONTACT_MISMATCH, bad.evaluate(track));
    assertEquals(
        VehicleContract.Eligibility.CLEARANCE_MISMATCH,
        new VehicleContract(null, "ordinary", gear, new Clearance(5, 4, 4), Set.of(), 0)
            .evaluate(track));
    assertEquals(
        VehicleContract.Eligibility.MISSING_MECHANISM,
        new VehicleContract(Family.ARROW, "v1", gear, clearance, Set.of(Mechanism.CONTROL_RAILS), 2)
            .evaluate(track));
  }

  @Test
  void loadingAddsMassAndParallelAxisInertiaExactlyOnce() {
    var loaded = new FreeRotor.Loading(100, 20, 40).withRider(80, .5);
    assertEquals(180, loaded.mass());
    assertEquals(60, loaded.firstMoment());
    assertEquals(60, loaded.inertia());
  }

  @Test
  void freeMotionRetainsVelocityAcrossTranslationReversal() {
    var s = new FreeRotor.State(1, 2);
    var next = FreeRotor.step(s, new FreeRotor.Loading(10, 0, 4), 9.81, 0, 0, .01);
    assertEquals(2, next.omega());
    assertEquals(1.02, next.angle(), 1e-12);
  }

  @Test
  void gravityAndTimestepAreStable() {
    var loading = new FreeRotor.Loading(10, 5, 5);
    var a = new FreeRotor.State(.8, 0);
    var b = a;
    double initialEnergy = FreeRotor.energy(a, loading, 9.81);
    for (int i = 0; i < 1000; i++) a = FreeRotor.step(a, loading, 9.81, 0, 0, .01);
    for (int i = 0; i < 2000; i++) b = FreeRotor.step(b, loading, 9.81, 0, 0, .005);
    assertEquals(a.angle(), b.angle(), .002);
    assertEquals(initialEnergy, FreeRotor.energy(a, loading, 9.81), .02);
    var first = FreeRotor.step(new FreeRotor.State(Math.PI / 2, 0), loading, 9.81, 0, 0, .001);
    assertEquals(-9.81 * .001, first.omega(), 1e-7);
  }

  @Test
  void invalidInputsFailClosed() {
    assertThrows(IllegalArgumentException.class, () -> Family.byId("future_unknown"));
    assertThrows(IllegalArgumentException.class, () -> new Gear(Double.NaN, 1, 1, 1));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new RotationProfile(
                List.of(new RotationProfile.Key(0, 0), new RotationProfile.Key(0, 1))));
    assertThrows(
        IllegalArgumentException.class, () -> new FreeRotor.State(0, Double.POSITIVE_INFINITY));
  }
}
