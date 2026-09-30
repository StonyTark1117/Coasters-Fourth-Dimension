package dev.fourthdimension.physics;

/**
 * Two generalized velocity directions with their true kinetic mass matrix, including cross term.
 */
public final class PassiveCoupler {
  private PassiveCoupler() {}

  public static final double RADIUS = .35;

  public record Result(double translation, double rotation, double energyChange) {}

  public static Result impulse(
      double mass,
      double inertia,
      double cross,
      double linearMomentum,
      double angularMomentum,
      MagneticSpan span,
      double exposure) {
    double determinant = mass * inertia - cross * cross;
    if (!Double.isFinite(determinant)
        || mass <= 0
        || inertia <= 0
        || determinant <= 1e-9
        || !Double.isFinite(linearMomentum)
        || !Double.isFinite(angularMomentum)
        || !Double.isFinite(exposure)
        || exposure < 0
        || exposure > .1)
      throw new IllegalArgumentException("Invalid magnetic mass matrix or exposure");
    double g0 = span.mode() == MagneticSpan.Mode.INDUCE ? 1 : 0;
    double g1 = span.mode() == MagneticSpan.Mode.INDUCE ? -span.direction() * RADIUS : 1;
    double a = (inertia * g0 - cross * g1) / determinant;
    double b = (mass * g1 - cross * g0) / determinant;
    double slip = a * linearMomentum + b * angularMomentum;
    if (slip == 0 || exposure == 0) return new Result(0, 0, 0);
    double inverseMass = g0 * a + g1 * b;
    double j = slip / inverseMass * -Math.expm1(-span.strength() * exposure);
    j = Math.copySign(Math.min(Math.abs(j), 100 * exposure), j);
    double dv = -a * j, dw = -b * j;
    double energy =
        linearMomentum * dv
            + angularMomentum * dw
            + .5 * (mass * dv * dv + 2 * cross * dv * dw + inertia * dw * dw);
    return new Result(dv, dw, energy);
  }
}
