package dev.fourthdimension.gametest;

import dev.fourthdimension.moving.*;
import dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent;
import java.nio.file.*;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3d;

/** Validation-only observation at every native physics callback; never writes a body or actuator. */
final class MovingNativeMeasurements {
  private final ServerLevel level;
  private final MovingAssemblyState state;
  private final String label;
  private final Consumer<ForgeSablePrePhysicsTickEvent> listener=this::sample;
  private final StringBuilder csv=new StringBuilder("seconds,dt,phase,x,y,z,qx,qy,qz,qw,vx,vy,vz,wx,wy,wz,linear_acceleration,angular_acceleration,linear_jerk,angular_jerk,braking\n");
  private Vector3d previousV,previousW,previousA,previousAlpha;
  private MovingAssemblyState.Pose brakeStart;
  private double elapsed,minDt=Double.POSITIVE_INFINITY,maxDt;
  private final double[] motion=new double[6],braking=new double[6];
  private double stopExcursion,angularExcursion,stopSeconds=-1,brakeTime;
  private int samples;
  private boolean active=true,brakeFinished;

  MovingNativeMeasurements(ServerLevel level,MovingAssemblyState state,String label){
    if(!Boolean.getBoolean("fourthdimension.validation"))throw new IllegalStateException("Validation disabled");
    this.level=level;this.state=state;this.label=label;NeoForge.EVENT_BUS.addListener(listener);
    var container=dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(level);
    var ids=new java.util.LinkedHashSet<java.util.UUID>(state.bodies.keySet());ids.add(state.bed);
    double totalMass=0;
    for(var id:ids)if(container.getSubLevel(id) instanceof dev.ryanhcode.sable.sublevel.ServerSubLevel body)
      totalMass+=body.getMassTracker().getMass();
    System.out.println("FD2001_MEASURE_LOAD label="+label+" bodies="+ids.size()+" nativeTotalMass="+totalMass
        +" perLinearAxisForceCap="+Math.max(1,totalMass)*24+" perAngularAxisTorqueCap="+Math.max(1,totalMass)*240);
  }
  void interrupt(){MovingNativeAdapter.readback(level,state);brakeStart=state.actualPose;brakeTime=elapsed;}
  void resumed(){brakeFinished=true;}
  private void sample(ForgeSablePrePhysicsTickEvent event){
    if(!active||event.getPhysicsSystem().getLevel()!=level)return;
    double dt=event.getTimeStep();if(!(dt>0))return;
    MovingNativeAdapter.readback(level,state);elapsed+=dt;samples++;minDt=Math.min(minDt,dt);maxDt=Math.max(maxDt,dt);
    var v=new Vector3d(state.linearVelocity);var w=new Vector3d(state.angularVelocity);
    var a=previousV==null?new Vector3d():new Vector3d(v).sub(previousV).div(dt);
    var alpha=previousW==null?new Vector3d():new Vector3d(w).sub(previousW).div(dt);
    var j=previousA==null?new Vector3d():new Vector3d(a).sub(previousA).div(dt);
    var ja=previousAlpha==null?new Vector3d():new Vector3d(alpha).sub(previousAlpha).div(dt);
    boolean stopping=brakeStart!=null&&!brakeFinished;
    double[] max=stopping?braking:motion;double[] values={v.length(),w.length(),a.length(),alpha.length(),j.length(),ja.length()};
    for(int i=0;i<max.length;i++)max[i]=Math.max(max[i],values[i]);
    if(stopping){
      stopExcursion=Math.max(stopExcursion,state.actualPose.position().distance(brakeStart.position()));
      angularExcursion=Math.max(angularExcursion,MovingNativeAdapter.angularError(state.actualPose.orientation(),brakeStart.orientation()));
      if(stopSeconds<0&&v.length()<=.025&&w.length()<=Math.toRadians(.1))stopSeconds=elapsed-brakeTime;
    }
    var p=state.actualPose;
    csv.append(elapsed).append(',').append(dt).append(',').append(state.phase).append(',')
        .append(p.x()).append(',').append(p.y()).append(',').append(p.z()).append(',')
        .append(p.qx()).append(',').append(p.qy()).append(',').append(p.qz()).append(',').append(p.qw()).append(',')
        .append(v.x).append(',').append(v.y).append(',').append(v.z).append(',')
        .append(w.x).append(',').append(w.y).append(',').append(w.z).append(',')
        .append(values[2]).append(',').append(values[3]).append(',').append(values[4]).append(',').append(values[5]).append(',').append(stopping).append('\n');
    previousV=v;previousW=w;previousA=a;previousAlpha=alpha;
  }
  void finish(){
    active=false;NeoForge.EVENT_BUS.unregister(listener);
    var base=Path.of("/mnt/NVME4/fourthdimension-runs/fd15-moving-native-20261122");
    try{Files.writeString(base.resolve(label+"-physics.csv"),csv);}catch(java.io.IOException e){throw new IllegalStateException(e);}
    System.out.println("FD2001_MEASURE label="+label+" samples="+samples+" dt="+minDt+".."+maxDt
        +" motionMax[v,w,a,alpha,j,angularJerk]="+java.util.Arrays.toString(motion)
        +" brakeMax="+java.util.Arrays.toString(braking)+" stopExcursion="+stopExcursion
        +" angularExcursion="+angularExcursion+" firstStoppedSeconds="+stopSeconds);
  }
}
