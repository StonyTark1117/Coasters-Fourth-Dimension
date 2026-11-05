package dev.fourthdimension.vehicle;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RecoveryRiderClearanceTest {
  @Test
  void everyCertifiedLayerWithClearHandsIsAccepted() {
    assertTrue(RecoveryClearancePolicy.certifiedEquipment(true, true, true, true, true, true));
  }

  @Test
  void eitherHeldItemFailsClosed() {
    assertFalse(RecoveryClearancePolicy.certifiedEquipment(false, true, true, true, true, true));
    assertFalse(RecoveryClearancePolicy.certifiedEquipment(true, false, true, true, true, true));
  }

  @Test
  void anyUncertifiedEquipmentSlotFailsClosed() {
    for (int slot = 0; slot < 4; slot++) {
      var certified = new boolean[] {true, true, true, true};
      certified[slot] = false;
      assertFalse(
          RecoveryClearancePolicy.certifiedEquipment(
              true, true, certified[0], certified[1], certified[2], certified[3]));
    }
  }
}
