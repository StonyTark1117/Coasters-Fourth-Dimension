package dev.fourthdimension.physics;

/** Bounded local-axis field request; native torque application owns the physical response. */
public final class S2SpinningField {
  private S2SpinningField(){}
  public static double acceleration(MagneticSpan span,double omega,double weight,double dt){
    if(!Double.isFinite(omega)||!Double.isFinite(weight)||dt<=0)return 0;
    double bound=Math.min(1,span.strength()/20)*Math.clamp(weight,0,1);
    if(span.mode()==MagneticSpan.Mode.DAMP||Math.abs(omega)>=3)
      return -Math.signum(omega)*Math.min(bound,Math.abs(omega)/dt);
    return span.direction()*bound;
  }
}
