package dev.fourthdimension;

import static org.junit.jupiter.api.Assertions.*;

import dev.fourthdimension.physics.RiderLook;
import org.joml.Quaterniond;
import org.junit.jupiter.api.Test;

class RiderLookTest {
  @Test
  void turnedCarrierRestoresWorldBoardingInput() {
    var look = RiderLook.toWorld(90, -5, new Quaterniond().rotateY(Math.PI));
    assertEquals(-90, look.yaw(), .0001);
    assertEquals(-5, look.pitch(), .0001);
    look = RiderLook.toWorld(0, 20, new Quaterniond().rotateY(Math.PI / 2));
    assertEquals(-90, look.yaw(), .0001);
    assertEquals(20, look.pitch(), .0001);
  }

  @Test
  void inversionAndIdentityPreserveLocalMeaning() {
    var look = RiderLook.toWorld(0, 30, new Quaterniond().rotateZ(Math.PI));
    assertEquals(0, look.yaw(), .0001);
    assertEquals(-30, look.pitch(), .0001);
    look = RiderLook.toWorld(-135, -42, new Quaterniond());
    assertEquals(-135, look.yaw(), .0001);
    assertEquals(-42, look.pitch(), .0001);
  }
}
