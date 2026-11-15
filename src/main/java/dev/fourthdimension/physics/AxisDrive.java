package dev.fourthdimension.physics;
/** Bounded unwrapped Axis command state, independent of native physics and persistence. */
public class AxisDrive {
  public double command, commandRate;
  public boolean initialized;
  public double advance(double goal,double actual,double dt,double speed,double acceleration) {
    if(!initialized){command=actual;commandRate=0;initialized=true;}
    if(!Double.isFinite(goal)||!Double.isFinite(actual)||!Double.isFinite(dt)||dt<=0||dt>.25
        ||!Double.isFinite(speed)||speed<=0||!Double.isFinite(acceleration)||acceleration<=0)
      throw new IllegalArgumentException("Invalid Axis servo step");
    double error=goal-command;
    double desired=Math.copySign(Math.min(speed,Math.sqrt(2*acceleration*Math.abs(error)+Math.pow(acceleration*dt,2))-acceleration*dt),error);
    commandRate+=Math.clamp(desired-commandRate,-acceleration*dt,acceleration*dt);
    double next=command+commandRate*dt;
    if((goal-command)*(goal-next)<=0&&Math.abs(commandRate)<=acceleration*dt){next=goal;commandRate=0;}
    command=Math.clamp(next,actual-.25,actual+.25);
    return command;
  }
}
