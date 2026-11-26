package dev.fourthdimension.moving;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Finite server-time physical targets, not an animation or docking authority.
 * Native readback/locks must establish completion; a finished sample never permits release.
 * Units: blocks, seconds, radians. Local X is lateral, Y up, Z longitudinal.
 */
public final class MovingProgram {
  public static final int VERSION = 1;
  // Command-coordinate caps. Native solver/braking observations are recorded separately.
  public static final Limits LEVEL_LIMITS = new Limits(2, 1, 2, Math.PI / 12, Math.PI / 24, Math.PI / 12);
  public static final Limits EFFECT_LIMITS = new Limits(4, 2, 4, Math.PI / 12, Math.PI / 24, Math.PI / 12);
  // Maxima of the derivatives of 35u^4-84u^5+70u^6-20u^7, u in [0,1].
  private static final double MAX_D1 = 35.0 / 16, MAX_D2 = 84 / (5 * Math.sqrt(5)), MAX_D3 = 52.5;
  private final MovingMechanism mechanism;
  private final Limits limits;
  private final List<Segment> segments;
  private final double duration;

  public record Point(double x, double y, double z) {
    public Point { finite(x, y, z); }
  }

  /** Active rotation R_y(yaw) R_x(pitch), followed by translation; angles may be unwrapped. */
  public record Pose(double x, double y, double z, double yaw, double pitch) {
    public static final Pose ZERO = new Pose(0, 0, 0, 0, 0);
    public Pose { finite(x, y, z, yaw, pitch); }
    public Point transform(Point p) {
      double cp = Math.cos(pitch), sp = Math.sin(pitch), cy = Math.cos(yaw), sy = Math.sin(yaw);
      double py = cp * p.y - sp * p.z, pz = sp * p.y + cp * p.z;
      return new Point(x + cy * p.x + sy * pz, y + py, z - sy * p.x + cy * pz);
    }
    public double[] quaternion() {
      double sy = Math.sin(yaw / 2), cy = Math.cos(yaw / 2);
      double sp = Math.sin(pitch / 2), cp = Math.cos(pitch / 2);
      return new double[]{cy * sp, sy * cp, -sy * sp, cy * cp}; // x,y,z,w
    }
    private Pose scale(double s) { return new Pose(x*s, y*s, z*s, yaw*s, pitch*s); }
    private Pose plus(Pose p) { return new Pose(x+p.x, y+p.y, z+p.z, yaw+p.yaw, pitch+p.pitch); }
    private Pose minus(Pose p) { return plus(p.scale(-1)); }
    public double linearMagnitude() { return Math.sqrt(x*x + y*y + z*z); }
  }

  /** Velocity/acceleration/jerk are coordinate derivatives, not composed angular vectors. */
  public record Sample(Pose pose, Pose velocity, Pose acceleration, Pose jerk, boolean targetFinished) {
    /** Spatial angular velocity for native rigid-body control, unlike Euler coordinate rates. */
    public Point angularVelocity() {
      double c=Math.cos(pose.yaw),s=Math.sin(pose.yaw);
      return new Point(c*velocity.pitch,velocity.yaw,-s*velocity.pitch);
    }
    public Point angularAcceleration() {
      double c=Math.cos(pose.yaw),s=Math.sin(pose.yaw), y=velocity.yaw,p=velocity.pitch;
      return new Point(c*acceleration.pitch-s*y*p,acceleration.yaw,-s*acceleration.pitch-c*y*p);
    }
    public Point angularJerk() {
      double c=Math.cos(pose.yaw),s=Math.sin(pose.yaw), y=velocity.yaw,p=velocity.pitch;
      double cross=acceleration.yaw*p+2*y*acceleration.pitch;
      return new Point(c*(jerk.pitch-y*y*p)-s*cross,jerk.yaw,-s*(jerk.pitch-y*y*p)-c*cross);
    }
  }
  public record Limits(double linearSpeed, double linearAcceleration, double linearJerk,
                       double angularSpeed, double angularAcceleration, double angularJerk) {
    public Limits {
      for (double v : new double[]{linearSpeed,linearAcceleration,linearJerk,angularSpeed,angularAcceleration,angularJerk})
        if (!Double.isFinite(v) || v <= 0) throw new IllegalArgumentException("Positive finite motion limits required");
    }
  }

  /** Bounds include every retained car, coupler, rider, carrier and moving support relative to pivot. */
  public record Bounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
    public Bounds {
      finite(minX,minY,minZ,maxX,maxY,maxZ);
      if (minX > maxX || minY > maxY || minZ > maxZ) throw new IllegalArgumentException("Inverted bounds");
    }
    public boolean contains(Point p) {
      return p.x >= minX && p.x <= maxX && p.y >= minY && p.y <= maxY && p.z >= minZ && p.z <= maxZ;
    }
    public List<Point> corners() {
      var result = new ArrayList<Point>();
      for (double x : new double[]{minX,maxX}) for (double y : new double[]{minY,maxY})
        for (double z : new double[]{minZ,maxZ}) result.add(new Point(x,y,z));
      return List.copyOf(result);
    }
    public Bounds union(Bounds b) {
      return new Bounds(Math.min(minX,b.minX),Math.min(minY,b.minY),Math.min(minZ,b.minZ),
          Math.max(maxX,b.maxX),Math.max(maxY,b.maxY),Math.max(maxZ,b.maxZ));
    }
    public Bounds inflate(double amount) {
      if (!Double.isFinite(amount) || amount < 0) throw new IllegalArgumentException("Invalid sweep margin");
      return new Bounds(minX-amount,minY-amount,minZ-amount,maxX+amount,maxY+amount,maxZ+amount);
    }
    public Bounds transformed(Pose pose) {
      Bounds result = null;
      for (var c : corners()) {
        var p = pose.transform(c);
        var b = new Bounds(p.x,p.y,p.z,p.x,p.y,p.z);
        result = result == null ? b : result.union(b);
      }
      return result;
    }
    public double radius() {
      double x = Math.max(Math.abs(minX),Math.abs(maxX)), y = Math.max(Math.abs(minY),Math.abs(maxY)),
          z = Math.max(Math.abs(minZ),Math.abs(maxZ));
      return Math.sqrt(x*x+y*y+z*z);
    }
  }

  public record Segment(Pose from, Pose to, double seconds) {
    public Segment {
      Objects.requireNonNull(from); Objects.requireNonNull(to);
      if (!Double.isFinite(seconds) || seconds <= 0) throw new IllegalArgumentException("Invalid segment duration");
    }
    private Pose atProgress(double s) { return from.plus(to.minus(from).scale(s)); }
  }

  private MovingProgram(MovingMechanism mechanism, Limits limits, List<Pose> targets) {
    this.mechanism = Objects.requireNonNull(mechanism);
    this.limits = Objects.requireNonNull(limits);
    var result = new ArrayList<Segment>();
    double total = 0;
    for (int i=1; i<targets.size(); i++) {
      Pose from=targets.get(i-1), to=targets.get(i), d=to.minus(from);
      double angular=Math.max(Math.abs(d.yaw),Math.abs(d.pitch));
      double seconds=Math.max(durationFor(d.linearMagnitude(),limits.linearSpeed,limits.linearAcceleration,limits.linearJerk),
          durationFor(angular,limits.angularSpeed,limits.angularAcceleration,limits.angularJerk));
      seconds=Math.max(.05,seconds);
      result.add(new Segment(from,to,seconds)); total+=seconds;
    }
    if (result.isEmpty()) throw new IllegalArgumentException("Motion requires endpoints");
    this.segments=List.copyOf(result); this.duration=total;
  }

  private static double durationFor(double distance,double v,double a,double j) {
    return Math.max(distance*MAX_D1/v,Math.max(Math.sqrt(distance*MAX_D2/a),Math.cbrt(distance*MAX_D3/j)));
  }
  public MovingMechanism mechanism() { return mechanism; }
  public Limits limits() { return limits; }
  public List<Segment> segments() { return segments; }
  public double durationSeconds() { return duration; }
  public Pose start() { return segments.getFirst().from; }
  public Pose end() { return segments.getLast().to; }
  public double maxAbsPitch() {
    return segments.stream().mapToDouble(s -> Math.max(Math.abs(s.from.pitch),Math.abs(s.to.pitch))).max().orElse(0);
  }

  public Sample sample(double seconds) {
    if (!Double.isFinite(seconds)) throw new IllegalArgumentException("Nonfinite server time");
    if (seconds<=0) return new Sample(start(),Pose.ZERO,Pose.ZERO,Pose.ZERO,false);
    if (seconds>=duration) return new Sample(end(),Pose.ZERO,Pose.ZERO,Pose.ZERO,true);
    for (var segment : segments) {
      if (seconds>segment.seconds) { seconds-=segment.seconds; continue; }
      double u=seconds/segment.seconds, t=segment.seconds, w=1-u;
      double s=u*u*u*u*(35+u*(-84+u*(70-20*u)));
      double d1=140*u*u*u*w*w*w;
      double d2=420*u*u*w*w*(1-2*u);
      double d3=840*u*w*(1-5*u+5*u*u);
      var delta=segment.to.minus(segment.from);
      return new Sample(segment.atProgress(s),delta.scale(d1/t),delta.scale(d2/(t*t)),delta.scale(d3/(t*t*t)),false);
    }
    throw new IllegalStateException("Program time partition failed");
  }

  /** Return/reset follows the same prescribed path; effect reset uses the slower powered limits. */
  public MovingProgram reverse() {
    var targets=new ArrayList<Pose>(); targets.add(end());
    for (int i=segments.size()-1;i>=0;i--) targets.add(segments.get(i).from);
    return new MovingProgram(mechanism,LEVEL_LIMITS,targets);
  }

  /** Replan from a physically stopped on-path sample, never resume a nonzero old derivative. */
  public MovingProgram remainingFrom(double seconds) { return recoveryFrom(seconds,false).program(); }
  /** Controlled return traverses the prescribed legs in reverse at powered-service limits. */
  public MovingProgram returnFrom(double seconds) { return recoveryFrom(seconds,true).program(); }

  public record RecoverySample(Sample motion,double originalSeconds) {}
  private record Mapping(int originalIndex,double fromProgress,double toProgress,double originalOffset) {}
  /** Derived, immutable cursor. Persist original preset/progress/direction/elapsed, not these legs. */
  public static final class Recovery {
    private final MovingProgram original, program;
    private final List<Mapping> mapping;
    private Recovery(MovingProgram original,MovingProgram program,List<Mapping> mapping) {
      this.original=original;this.program=program;this.mapping=List.copyOf(mapping);
    }
    public MovingProgram program(){return program;}
    public RecoverySample sample(double elapsed){
      if(!Double.isFinite(elapsed)||elapsed<0)
        throw new IllegalArgumentException("Recovery time outside fragment");
      if(elapsed>=program.duration){
        var last=mapping.getLast();
        double time=last.originalOffset+last.toProgress*original.segments.get(last.originalIndex).seconds;
        return new RecoverySample(program.sample(elapsed),Math.clamp(time,0,original.duration));
      }
      double remaining=elapsed;
      for(int i=0;i<mapping.size();i++){
        var leg=program.segments.get(i);
        if(remaining>leg.seconds && i<mapping.size()-1){remaining-=leg.seconds;continue;}
        var m=mapping.get(i);
        double p=m.fromProgress+(m.toProgress-m.fromProgress)*smooth(Math.clamp(remaining/leg.seconds,0,1));
        double originalSeconds=m.originalOffset+inverseSmooth(p)*original.segments.get(m.originalIndex).seconds;
        return new RecoverySample(program.sample(elapsed),Math.clamp(originalSeconds,0,original.duration));
      }
      throw new IllegalStateException("Recovery mapping exhausted");
    }
  }
  /** Always call on the original preset program, using previous RecoverySample.originalSeconds(). */
  public Recovery recoveryFrom(double seconds,boolean returning){
    int index=segmentAt(seconds);
    double[] offsets=new double[segments.size()];
    for(int i=1;i<offsets.length;i++) offsets[i]=offsets[i-1]+segments.get(i-1).seconds;
    double progress=smooth(Math.clamp((seconds-offsets[index])/segments.get(index).seconds,0,1));
    var targets=new ArrayList<Pose>();targets.add(sample(seconds).pose());
    var mapping=new ArrayList<Mapping>();
    if(returning){
      for(int i=index;i>=0;i--){targets.add(segments.get(i).from);mapping.add(new Mapping(i,i==index?progress:1,0,offsets[i]));}
    }else{
      for(int i=index;i<segments.size();i++){targets.add(segments.get(i).to);mapping.add(new Mapping(i,i==index?progress:0,1,offsets[i]));}
    }
    return new Recovery(this,new MovingProgram(mechanism,returning?LEVEL_LIMITS:limits,targets),mapping);
  }
  public record Projection(double originalSeconds,Pose pose,double positionError,double angularError) {}
  /**
   * Find a measured stopped pose only on the saved original leg and within explicit along-path
   * limits. Quaternion comparison preserves unwrapped show/teeter branch identity via that leg
   * and a <=90-degree search window. This is readback diagnosis, never a teleport or release.
   */
  public Projection projectStoppedPose(double savedSeconds,Point measuredPosition,double[] measuredQuaternion,
      double maximumTranslationCorrection,double maximumAngularCorrection,double positionTolerance,double angularTolerance){
    int index=segmentAt(savedSeconds);Objects.requireNonNull(measuredPosition);
    if(measuredQuaternion==null||measuredQuaternion.length!=4)throw new IllegalArgumentException("Quaternion xyzw required");
    finite(maximumTranslationCorrection,maximumAngularCorrection,positionTolerance,angularTolerance);
    if(maximumTranslationCorrection<0||maximumAngularCorrection<0||maximumAngularCorrection>Math.PI/2
        ||positionTolerance<=0||angularTolerance<=0)throw new IllegalArgumentException("Bounded positive projection tolerances required");
    double norm=0;for(double value:measuredQuaternion){finite(value);norm+=value*value;}
    if(Math.abs(norm-1)>.001)throw new IllegalArgumentException("Normalized measured orientation required");
    final double[] q=measuredQuaternion.clone();double qnorm=Math.sqrt(norm);for(int i=0;i<4;i++)q[i]/=qnorm;
    double offset=0;for(int i=0;i<index;i++)offset+=segments.get(i).seconds;
    var segment=segments.get(index);var delta=segment.to.minus(segment.from);
    double saved=smooth(Math.clamp((savedSeconds-offset)/segment.seconds,0,1)),window=1;
    if(delta.linearMagnitude()>1e-12)window=Math.min(window,maximumTranslationCorrection/delta.linearMagnitude());
    double angular=Math.abs(delta.yaw)+Math.abs(delta.pitch);
    if(angular>1e-12)window=Math.min(window,maximumAngularCorrection/angular);
    double lo=Math.max(0,saved-window),hi=Math.min(1,saved+window);
    // The bounded interval avoids another revolution; sample then refine the best local minimum.
    double best=lo,bestScore=Double.POSITIVE_INFINITY,step=(hi-lo)/256;
    for(int i=0;i<=256;i++){double t=lo+step*i;double score=projectionScore(segment.atProgress(t),measuredPosition,q,positionTolerance,angularTolerance);
      if(score<bestScore){best=t;bestScore=score;}}
    double a=Math.max(lo,best-step),b=Math.min(hi,best+step);
    for(int i=0;i<70;i++){double left=a+(b-a)/3,right=b-(b-a)/3;
      if(projectionScore(segment.atProgress(left),measuredPosition,q,positionTolerance,angularTolerance)
          <=projectionScore(segment.atProgress(right),measuredPosition,q,positionTolerance,angularTolerance))b=right;else a=left;}
    double refined=(a+b)/2;
    if(projectionScore(segment.atProgress(refined),measuredPosition,q,positionTolerance,angularTolerance)<bestScore)best=refined;
    var pose=segment.atProgress(best);double distance=positionDistance(pose,measuredPosition),angle=quaternionDistance(pose.quaternion(),q);
    if(distance>positionTolerance||angle>angularTolerance)throw new IllegalArgumentException("Stopped native pose is outside the saved path leg; retain and diagnose");
    return new Projection(offset+inverseSmooth(best)*segment.seconds,pose,distance,angle);
  }
  private static double projectionScore(Pose pose,Point p,double[] q,double pt,double at){
    double distance=positionDistance(pose,p)/pt,angle=quaternionDistance(pose.quaternion(),q)/at;return distance*distance+angle*angle;
  }
  private static double positionDistance(Pose pose,Point p){return Math.sqrt(Math.pow(pose.x-p.x,2)+Math.pow(pose.y-p.y,2)+Math.pow(pose.z-p.z,2));}
  private static double quaternionDistance(double[] a,double[] b){double dot=0;for(int i=0;i<4;i++)dot+=a[i]*b[i];return 2*Math.acos(Math.clamp(Math.abs(dot),0,1));}

  private static double smooth(double u){return u*u*u*u*(35+u*(-84+u*(70-20*u)));}
  private static double inverseSmooth(double s){
    if(s<=0)return 0;if(s>=1)return 1;
    double lo=0,hi=1;
    for(int i=0;i<60;i++){double mid=(lo+hi)/2;if(smooth(mid)<s)lo=mid;else hi=mid;}
    return (lo+hi)/2;
  }
  private int segmentAt(double seconds) {
    if(!Double.isFinite(seconds)||seconds<0||seconds>duration)
      throw new IllegalArgumentException("Recovery time must lie within this program");
    for(int i=0;i<segments.size()-1;i++) {
      if(seconds<=segments.get(i).seconds)return i;
      seconds-=segments.get(i).seconds;
    }
    return segments.size()-1;
  }

  public static MovingProgram transfer(int lateralBlocks) {
    preset(lateralBlocks,4,8,16);
    return path(MovingMechanism.TRANSFER,LEVEL_LIMITS,new Pose(lateralBlocks,0,0,0,0));
  }
  public static MovingProgram turntable(int fromPort,int toPort,boolean fullShowTurn) {
    port(fromPort);port(toPort);
    double start=Math.toRadians(fromPort), yaw=shortestYaw(fromPort,toPort);
    // A full retained show is one 360-degree turn plus a separate shortest route selection.
    if(fullShowTurn) return new MovingProgram(MovingMechanism.TURNTABLE,LEVEL_LIMITS,List.of(
        new Pose(0,0,0,start,0),new Pose(0,0,0,start+2*Math.PI,0),
        new Pose(0,0,0,start+2*Math.PI+shortestYaw(fromPort,toPort),0)));
    return new MovingProgram(MovingMechanism.TURNTABLE,LEVEL_LIMITS,List.of(new Pose(0,0,0,start,0),new Pose(0,0,0,start+yaw,0)));
  }
  public static MovingProgram drop(int height) {
    preset(height,4,8,12);return path(MovingMechanism.DROP,EFFECT_LIMITS,new Pose(0,-height,0,0,0));
  }
  public static MovingProgram tilt(int downwardDegrees) {
    preset(downwardDegrees,30,90);return path(MovingMechanism.TILT,LEVEL_LIMITS,new Pose(0,0,0,0,Math.toRadians(downwardDegrees)));
  }
  public static MovingProgram elevator(int height) {
    preset(height,4,8,16,32);return path(MovingMechanism.ELEVATOR,LEVEL_LIMITS,new Pose(0,height,0,0,0));
  }
  public static MovingProgram teeter(int cycles) {
    preset(cycles,1,2,3);var targets=new ArrayList<Pose>();targets.add(Pose.ZERO);
    for(int i=0;i<cycles;i++){targets.add(new Pose(0,0,0,0,Math.PI/12));targets.add(new Pose(0,0,0,0,-Math.PI/12));}
    targets.add(Pose.ZERO);return new MovingProgram(MovingMechanism.TEETER,LEVEL_LIMITS,targets);
  }
  public static MovingProgram sideDrop(int offset) {
    preset(offset,4,8);return path(MovingMechanism.SIDE_DROP,EFFECT_LIMITS,new Pose(offset,-offset,0,0,0));
  }
  public static MovingProgram compound(int fromPort,int toPort,int pitchDegrees) {
    port(fromPort);port(toPort);preset(pitchDegrees,-15,15);
    double start=Math.toRadians(fromPort), turn=shortestYaw(fromPort,toPort);
    if(turn==0) throw new IllegalArgumentException("Compound needs a different yaw port");
    return new MovingProgram(MovingMechanism.COMPOUND,LEVEL_LIMITS,List.of(new Pose(0,0,0,start,0),
        new Pose(0,0,0,start+turn/2,Math.toRadians(pitchDegrees)),new Pose(0,0,0,start+turn,0)));
  }
  /** Positive yaw is the declared clockwise tie-break when viewed from above along -Y. */
  public static double shortestYaw(int fromPort,int toPort) {
    port(fromPort);port(toPort);int degrees=Math.floorMod(toPort-fromPort,360);
    return Math.toRadians(degrees>180?degrees-360:degrees);
  }
  private static MovingProgram path(MovingMechanism m,Limits limits,Pose end) {
    return new MovingProgram(m,limits,List.of(Pose.ZERO,end));
  }

  /**
   * Conservative continuous swept cells. End-corner AABBs are padded by a proved angular
   * displacement bound r*(|dyaw|+|dpitch|)/(2*n), not a claim that endpoint/sample tests alone
   * find the sweep. Linear translation is inside endpoint hulls. Native collision queries must
   * test these volumes against the world, excluding only authenticated assembly members.
   */
  public List<Bounds> sweptVolumes(Bounds local,double maximumPadding) {
    Objects.requireNonNull(local);
    if(!Double.isFinite(maximumPadding)||maximumPadding<=0) throw new IllegalArgumentException("Positive sweep tolerance required");
    var result=new ArrayList<Bounds>(); double radius=local.radius();
    for(var s:segments){
      double angle=Math.abs(s.to.yaw-s.from.yaw)+Math.abs(s.to.pitch-s.from.pitch);
      // Translation subdivisions keep a diagonal cell from reserving its entire surrounding box.
      double travel=s.to.minus(s.from).linearMagnitude()+radius*angle;
      double count=Math.max(1,Math.ceil(travel/(2*maximumPadding)));
      if(count>100_000)throw new IllegalArgumentException("Sweep exceeds bounded query budget; increase tolerance or reduce envelope");
      int n=(int)count;
      for(int i=0;i<n;i++){
        Bounds a=local.transformed(s.atProgress((double)i/n)),b=local.transformed(s.atProgress((double)(i+1)/n));
        // Interpolated rotation differs from endpoint-hull interpolation by <= radius*angle/(2*n).
        result.add(a.union(b).inflate(radius*angle/(2*n)+1e-9));
      }
    }
    return List.copyOf(result);
  }
  public Bounds sweptEnvelope(Bounds local) {
    return sweptVolumes(local,.125).stream().reduce(Bounds::union).orElseThrow();
  }
  private static void port(int value){preset(value,0,90,180,270);}
  private static void preset(int value,int...allowed){for(int a:allowed)if(value==a)return;throw new IllegalArgumentException("Unsupported motion preset: "+value);}
  private static void finite(double...values){for(double v:values)if(!Double.isFinite(v))throw new IllegalArgumentException("Finite coordinates required");}
}
