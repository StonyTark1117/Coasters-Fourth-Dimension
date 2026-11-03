package dev.fourthdimension.client.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TrackStylesFallbackStateTest {
  @Test
  void drawFailureQuarantinesOnlyItsRailUntilTheRailOrModelChanges() {
    var state = new TrackStylesFallbackState();
    var failed = UUID.randomUUID();
    var unaffected = UUID.randomUUID();

    state.quarantine(failed, 9, 12);

    assertTrue(state.quarantines(failed, 9, 12));
    assertFalse(state.quarantines(unaffected, 9, 12));
    assertFalse(state.quarantines(failed, 10, 12));
    assertFalse(state.quarantines(failed, 9, 13));
  }

  @Test
  void discardedRailDoesNotRemainQuarantined() {
    var state = new TrackStylesFallbackState();
    var rail = UUID.randomUUID();
    state.quarantine(rail, 3, 4);

    state.retain(Set.of());

    assertFalse(state.quarantines(rail, 3, 4));
  }
}
