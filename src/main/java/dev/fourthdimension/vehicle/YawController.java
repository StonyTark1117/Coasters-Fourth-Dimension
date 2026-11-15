package dev.fourthdimension.vehicle;

import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/** Pure projection, winding and bounded reference mathematics; native bodies remain authoritative. */
public final class YawController {
  public static final double LIMIT=8*Math.PI;
  private YawController(){}
  public static double projected(Quaterniondc nativeChassis,Vector3dc pivot,Vector3dc marker){
    var design=new Quaterniond(nativeChassis).rotateY(Math.PI);
    var d=new Vector3d(marker).sub(pivot).rotate(design.conjugate());
    return Math.hypot(d.x,d.z)<.05?Double.NaN:Math.atan2(d.x,d.z);
  }
  public static double resolve(double theta,double actual,YawState.Winding policy){
    double chosen=Double.NaN,best=Double.POSITIVE_INFINITY;
    for(int k=-5;k<=5;k++){double c=theta+2*Math.PI*k,d=c-actual;if(c< -LIMIT-1e-10||c>LIMIT+1e-10||policy==YawState.Winding.POSITIVE&&d< -1e-9||policy==YawState.Winding.NEGATIVE&&d>1e-9)continue;
      if(Math.abs(d)<best-1e-9||Math.abs(Math.abs(d)-best)<1e-9&&d>0){chosen=Math.clamp(c,-LIMIT,LIMIT);best=Math.abs(d);}}
    return chosen;
  }
  public record Reference(double angle,double velocity){}
  public static Reference advance(double reference,double velocity,double target,double speed,double acceleration,double dt){
    if(!Double.isFinite(target)||!Double.isFinite(dt)||dt<=0)throw new IllegalArgumentException("Invalid yaw reference input");
    double error=target-reference;
    double wanted=Math.copySign(Math.min(speed,Math.sqrt(2*acceleration*Math.abs(error)+acceleration*acceleration*dt*dt)-acceleration*dt),error);
    if(Math.abs(error)<1e-9)wanted=0;
    double next=velocity+Math.clamp(wanted-velocity,-acceleration*dt,acceleration*dt);
    // Conservative discrete stopping speed avoids crossing a hard endpoint while braking.
    double angle=reference+next*dt;
    if((target-reference)*(target-angle)<=0&&Math.abs(next)<=acceleration*dt){angle=target;next=0;}
    return new Reference(angle,next);
  }
  public static Reference brake(double angle,double velocity,double acceleration,double dt){double v=velocity+Math.clamp(-velocity,-acceleration*dt,acceleration*dt);return new Reference(angle+(velocity+v)*dt*.5,v);}
}
