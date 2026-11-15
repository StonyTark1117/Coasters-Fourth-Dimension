package dev.fourthdimension.physics;

/** Five distinct server ticks, never five physics callbacks, qualify a stopped dock latch. */
public final class DockCapture {
  public static final double MAX_SPEED = .35, MAX_ACCELERATION = .5;
  public static final double ANGLE_TOLERANCE = Math.toRadians(2), OMEGA_TOLERANCE = .05;
  private long lastTick = Long.MIN_VALUE;
  private int settled;

  public boolean update(long tick, boolean dock, double error, double omega, double travel) {
    boolean valid = dock && Double.isFinite(error) && Math.abs(error) <= ANGLE_TOLERANCE
        && Double.isFinite(omega) && Math.abs(omega) <= OMEGA_TOLERANCE
        && Double.isFinite(travel) && Math.abs(travel) <= .05;
    if (!valid) { settled = 0; lastTick = tick; return false; }
    if (lastTick != tick) {
      settled = lastTick == tick - 1 ? Math.min(5, settled + 1) : 1;
      lastTick = tick;
    }
    return settled >= 5;
  }
}
