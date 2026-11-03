package dev.fourthdimension;

import static org.junit.jupiter.api.Assertions.*;

import dev.fourthdimension.physics.*;
import org.junit.jupiter.api.Test;

class MagneticMathTest {
  private MagneticSpan span(MagneticSpan.Mode mode) {
    return new MagneticSpan(2, 2.2, mode, 1, 4, .25, 15);
  }

  @Test
  void sweptFalloffIncludesNarrowAndReversePassesExactlyOnce() {
    var s = span(MagneticSpan.Mode.INDUCE);
    assertEquals(.15, s.weightedLength(0, 8), 1e-12);
    assertEquals(.15, s.weightedLength(8, 0), 1e-12);
    assertEquals(0, s.weightedLength(2.1, 2.1), 0);
    double integral = 0;
    for (int i = 0; i < 800; i++) integral += s.weightedLength(i * .01, (i + 1) * .01);
    assertEquals(s.weightedLength(0, 8), integral, 1e-12);
  }

  @Test
  void passiveInteractionNeverAddsKineticEnergyIncludingCrossCoupling() {
    var random = new java.util.Random(202);
    for (int n = 0; n < 10000; n++) {
      double m = 1 + random.nextDouble() * 40,
          i = 1 + random.nextDouble() * 15,
          b = (random.nextDouble() - .5) * Math.sqrt(m * i);
      double v = random.nextDouble() * 30 - 15, w = random.nextDouble() * 20 - 10;
      for (var mode : MagneticSpan.Mode.values()) {
        var r = PassiveCoupler.impulse(m, i, b, m * v + b * w, b * v + i * w, span(mode), .05);
        double before = .5 * (m * v * v + 2 * b * v * w + i * w * w);
        double after =
            .5
                * (m * Math.pow(v + r.translation(), 2)
                    + 2 * b * (v + r.translation()) * (w + r.rotation())
                    + i * Math.pow(w + r.rotation(), 2));
        assertTrue(after <= before + 1e-9);
        assertEquals(after - before, r.energyChange(), 1e-9);
      }
    }
  }

  @Test
  void stationaryCannotPowerAndReverseReversesInduction() {
    var s = span(MagneticSpan.Mode.INDUCE);
    assertEquals(
        new PassiveCoupler.Result(0, 0, 0), PassiveCoupler.impulse(20, 4, 0, 0, 0, s, .05));
    var forward = PassiveCoupler.impulse(20, 4, 0, 40, 0, s, .05);
    var reverse = PassiveCoupler.impulse(20, 4, 0, -40, 0, s, .05);
    assertTrue(forward.rotation() > 0);
    assertTrue(forward.translation() < 0);
    assertEquals(-forward.rotation(), reverse.rotation(), 1e-12);
    assertEquals(-forward.translation(), reverse.translation(), 1e-12);
  }

  @Test
  void damperRemovesFreeAngularSpeedWithoutCaptureOrDirectionInput() {
    var forward = new MagneticSpan(2, 6, MagneticSpan.Mode.DAMP, 1, 20, .25, 1);
    var reverse = new MagneticSpan(2, 6, MagneticSpan.Mode.DAMP, -1, 20, .25, 1);
    double initialOmega = 3;
    var damped = PassiveCoupler.impulse(20, 4, 0, 160, 4 * initialOmega, forward, .05);
    assertEquals(0, damped.translation(), 1e-12);
    assertTrue(damped.rotation() < 0);
    assertTrue(Math.abs(initialOmega + damped.rotation()) < Math.abs(initialOmega));
    assertTrue(damped.energyChange() < 0);
    assertEquals(damped, PassiveCoupler.impulse(20, 4, 0, 160, 4 * initialOmega, reverse, .05));
    assertEquals(
        new PassiveCoupler.Result(0, 0, 0),
        PassiveCoupler.impulse(20, 4, 0, 160, 0, forward, .05));
  }

  @Test
  void equipmentBracketsClearRunningGearAndWingEnvelope() {
    var rail = dev.fourthdimension.track.TrackDesign.FREE_SPIN;
    assertTrue(rail.MAGNET_HALF_GAUGE - rail.MAGNET_RADIUS > rail.TREAD_OUTER);
    assertTrue(rail.MAGNET_HALF_GAUGE + rail.MAGNET_RADIUS < 2);
    assertTrue(rail.MAGNET_SUPPORT_HEIGHT + rail.MAGNET_SUPPORT_RADIUS < .05 - rail.WHEEL_RADIUS);
  }

  @Test
  void rejectsNonFiniteInvalidSpansAndNonphysicalMatrices() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new MagneticSpan(0, Double.NaN, MagneticSpan.Mode.INDUCE, 1, 4, .2, 15));
    assertThrows(
        IllegalArgumentException.class,
        () -> new MagneticSpan(0, 1, MagneticSpan.Mode.INDUCE, 0, 4, .2, 15));
    assertThrows(
        IllegalArgumentException.class,
        () -> PassiveCoupler.impulse(1, 1, 2, 0, 0, span(MagneticSpan.Mode.DAMP), .05));
  }
}
