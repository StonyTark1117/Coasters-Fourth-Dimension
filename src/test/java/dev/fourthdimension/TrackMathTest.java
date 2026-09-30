package dev.fourthdimension;

import static org.junit.jupiter.api.Assertions.*;

import dev.fourthdimension.physics.*;
import dev.fourthdimension.track.TrackDesign;
import java.util.List;
import org.junit.jupiter.api.Test;

class TrackMathTest {
  @Test
  void nativeBearingContactPlanesAndTreadOverlap() {
    double upper = .3125 + .1875 + .1;
    double lower = upper - .55;
    assertEquals(upper - .2, TrackDesign.MAIN_RAIL_HEIGHT + TrackDesign.RAIL_HALF_HEIGHT, 1e-9);
    assertEquals(lower + .2, TrackDesign.MAIN_RAIL_HEIGHT - TrackDesign.RAIL_HALF_HEIGHT, 1e-9);
    assertTrue(TrackDesign.HALF_GAUGE - TrackDesign.RAIL_HALF_WIDTH >= 18.0 / 16 - .5);
    assertTrue(TrackDesign.HALF_GAUGE + TrackDesign.RAIL_HALF_WIDTH <= 23.0 / 16 - .5);
    assertTrue(TrackDesign.ARROW.controlHeight(RotationProfile.MAX_ANGLE) + .09 < lower - .2);
  }

  @Test
  void servoKeepsFullTurnsAndRetracesWithoutQuaternionShortcut() {
    var servo = new RotationServo(0);
    double actual = 0;
    for (int i = 0; i < 160; i++) {
      double next = servo.advance(4 * Math.PI, actual, .05);
      assertTrue(next >= actual - 1e-8 && next - actual <= RotationServo.MAX_SPEED * .05 + 1e-8);
      actual = next;
    }
    assertEquals(4 * Math.PI, actual, 1e-8);
    assertEquals(actual, servo.advance(actual, actual, .05), 1e-8);
    for (int i = 0; i < 160; i++) actual = servo.advance(0, actual, .05);
    assertEquals(0, actual, 1e-8);
    assertThrows(IllegalArgumentException.class, () -> servo.advance(0, 0, Double.NaN));
  }

  @Test
  void curvedDistanceIsNotChordOrParameter() {
    var circle =
        new ArcLengthTable(
            t -> new ArcLengthTable.Point(Math.cos(t * Math.PI / 2), Math.sin(t * Math.PI / 2), 0),
            1024);
    assertEquals(Math.PI / 2, circle.length(), 1e-6);
    var varying = new ArcLengthTable(t -> new ArcLengthTable.Point(10 * t * t, 0, 0), 1024);
    assertEquals(2.5, varying.distanceAt(.5), 1e-8);
    assertEquals(.5, varying.parameterAt(2.5), 1e-8);
    assertThrows(IllegalArgumentException.class, () -> varying.distanceAt(Double.NaN));
    assertThrows(
        IllegalArgumentException.class,
        () -> new ArcLengthTable(t -> new ArcLengthTable.Point(0, 0, 0), 16));
  }

  @Test
  void explicitTangentsAreContinuousWithoutOvershoot() {
    var p =
        new RotationProfile(
            List.of(new RotationProfile.Key(0, 0, 1), new RotationProfile.Key(2, 2, 1)));
    assertEquals(1, p.angleAt(1), 1e-10);
    assertEquals(1, p.slopeAt(0), 1e-10);
    assertEquals(1, p.slopeAt(2), 1e-10);
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new RotationProfile(
                List.of(new RotationProfile.Key(0, 0, 3), new RotationProfile.Key(2, 1, 0))));
    assertThrows(
        IllegalArgumentException.class, () -> new RotationProfile.Key(1, Double.POSITIVE_INFINITY));
  }

  @Test
  void mechanicalContinuityDoesNotWrapFullTurns() {
    var p =
        new RotationProfile(
            List.of(new RotationProfile.Key(0, 0), new RotationProfile.Key(10, 4 * Math.PI)));
    assertFalse(p.joins(p));
    assertTrue(p.joins(p.reversed()));
    assertEquals(p.angleAt(3), p.reversed().angleAt(7), 1e-10);
    assertEquals(-p.angleAt(3), p.mirrored().angleAt(3), 1e-10);
    assertEquals(p.angleAt(3), p.scaledTo(20).angleAt(6), 1e-10);
    assertTrue(
        TrackDesign.ARROW.controlHeight(4 * Math.PI)
            > TrackDesign.ARROW.controlHeight(2 * Math.PI));
    assertEquals(
        .5,
        TrackDesign.ARROW.controlHeight(RotationProfile.MAX_ANGLE)
            - TrackDesign.CONTROL_BASE_HEIGHT,
        1e-10);
    assertThrows(
        IllegalArgumentException.class, () -> TrackDesign.ARROW.controlHeight(9 * Math.PI));
  }
}
