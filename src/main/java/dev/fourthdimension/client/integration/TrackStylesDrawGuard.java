package dev.fourthdimension.client.integration;

/** Ensures an optional-provider draw cannot leave caller-owned render state pushed. */
final class TrackStylesDrawGuard {
  private TrackStylesDrawGuard() {}

  static boolean draw(Runnable push, Runnable draw, Runnable pop) {
    push.run();
    try {
      draw.run();
      return true;
    } catch (RuntimeException | LinkageError unavailable) {
      return false;
    } finally {
      pop.run();
    }
  }
}
