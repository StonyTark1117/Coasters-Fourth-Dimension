package dev.fourthdimension.physics;

/** Reference harness only. Native carriers use Sable gravity/inertia, never this torque as well. */
public final class FreeRotor {
  private FreeRotor() {}

  public record State(double angle, double omega) {
    public State {
      if (!Double.isFinite(angle) || !Double.isFinite(omega))
        throw new IllegalArgumentException("Non-finite state");
    }
  }

  public record Loading(double mass, double firstMoment, double inertia) {
    public Loading {
      if (!Double.isFinite(mass)
          || !Double.isFinite(firstMoment)
          || !Double.isFinite(inertia)
          || mass <= 0
          || inertia <= 0) throw new IllegalArgumentException("Invalid mass properties");
    }

    public Loading withRider(double riderMass, double pivotDistance) {
      if (!Double.isFinite(riderMass) || !Double.isFinite(pivotDistance) || riderMass < 0)
        throw new IllegalArgumentException("Invalid rider");
      return new Loading(
          mass + riderMass,
          firstMoment + riderMass * pivotDistance,
          inertia + riderMass * pivotDistance * pivotDistance);
    }
  }

  public static State step(
      State state, Loading loading, double gravity, double damping, double torque, double dt) {
    if (!Double.isFinite(gravity)
        || !Double.isFinite(damping)
        || !Double.isFinite(torque)
        || !Double.isFinite(dt)
        || dt <= 0
        || dt > 0.05
        || damping < 0) throw new IllegalArgumentException("Invalid step");
    // Kick-drift-kick reduces timestep dependence. Loading is supplied once by the caller.
    double half =
        state.omega
            + (-gravity * loading.firstMoment * Math.sin(state.angle) + torque)
                / loading.inertia
                * dt
                / 2;
    half *= Math.exp(-damping * dt / 2);
    double angle = state.angle + half * dt;
    double omega =
        (half
                + (-gravity * loading.firstMoment * Math.sin(angle) + torque)
                    / loading.inertia
                    * dt
                    / 2)
            * Math.exp(-damping * dt / 2);
    return new State(angle, omega);
  }

  public static double energy(State state, Loading loading, double gravity) {
    return 0.5 * loading.inertia * state.omega * state.omega
        + gravity * loading.firstMoment * (1 - Math.cos(state.angle));
  }

  public static double unwrap(double previous, double wrapped) {
    return previous + Math.atan2(Math.sin(wrapped - previous), Math.cos(wrapped - previous));
  }
}
