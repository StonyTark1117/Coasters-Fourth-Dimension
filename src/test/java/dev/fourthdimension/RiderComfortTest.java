package dev.fourthdimension;

import dev.fourthdimension.contract.ComfortFrame;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RiderComfortTest {
  @Test void activationAtBanksVerticalsAndInversionsKeepsWorldUpWithoutChangingSeatPose() {
    for (double pitch : new double[] {0, Math.PI / 2, Math.PI, -Math.PI / 2})
      for (double bank : new double[] {0, .5, Math.PI / 2, Math.PI}) {
        var seat = new Quaterniond().rotateY(.7).rotateX(pitch).rotateZ(bank);
        var before = new Quaterniond(seat);
        var frame = ComfortFrame.uprightFrame(seat);
        assertTrue(Double.isFinite(frame.w));
        assertEquals(1, frame.lengthSquared(), 1e-12);
        assertEquals(0, new Vector3d(0,1,0).rotate(frame).distance(new Vector3d(0,1,0)), 1e-12);
        assertEquals(before, seat);
        var leftLook = new Vector3d(0,0,1).rotate(new Quaterniond(frame).rotateY(.3));
        var rightLook = new Vector3d(0,0,1).rotate(new Quaterniond(frame).rotateY(-.3));
        assertTrue(leftLook.distance(rightLook) > .5);
      }
  }
}
