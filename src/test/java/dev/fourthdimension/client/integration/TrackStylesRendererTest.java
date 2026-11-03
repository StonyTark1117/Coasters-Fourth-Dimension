package dev.fourthdimension.client.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class TrackStylesRendererTest {
  @Test
  void providerDrawFailureBalancesThePoseCallbacks() {
    var depth = new AtomicInteger();

    assertFalse(
        TrackStylesDrawGuard.draw(
            depth::incrementAndGet,
            () -> {
              throw new LinkageError("provider model disappeared");
            },
            depth::decrementAndGet));

    assertEquals(0, depth.get());
  }

  @Test
  void successfulProviderDrawBalancesThePoseCallbacks() {
    var depth = new AtomicInteger();

    assertTrue(
        TrackStylesDrawGuard.draw(
            depth::incrementAndGet, () -> assertEquals(1, depth.get()), depth::decrementAndGet));

    assertEquals(0, depth.get());
  }
}
