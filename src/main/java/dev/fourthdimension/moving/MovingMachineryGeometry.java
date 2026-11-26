package dev.fourthdimension.moving;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.joml.Quaterniond;
import org.joml.Vector3d;

/** Original real-pose load paths. Native constraints remain the sole motion authority. */
public final class MovingMachineryGeometry {
  public record Beam(Vector3d from,Vector3d to,double width,String material){
    public Beam {from=new Vector3d(from);to=new Vector3d(to);}
    public AABB bounds(){return new AABB(from.x,from.y,from.z,to.x,to.y,to.z).inflate(width*.5);}
  }
  public record Race(Vector3d center,Vector3d axis,Vector3d radial,double inner,double outer,double width){}
  public record Geometry(List<Beam> fixed,List<Beam> moving,List<Race> races,Map<BlockPos,MovingPartBlock.Kind> feet){}
  private MovingMachineryGeometry(){}
  public static boolean overhead(MovingAssemblyState state){var config=MovingCapabilityCatalog.Configuration.byId(state.configuration);return config!=MovingCapabilityCatalog.Configuration.WING&&config!=MovingCapabilityCatalog.Configuration.NATIVE_SERVICE&&dev.fourthdimension.track.TrackDesign.valueOf(config.name()).overhead();}
  public static double mountY(MovingAssemblyState state){return state.configuration.equals("pipeline")?-4.05:overhead(state)?.85:-1.05;}
  private static Vector3d point(MovingAssemblyState.Pose pose,double x,double y,double z){return new Vector3d(x,y,z).rotate(pose.orientation()).add(pose.position());}
  public static MovingAssemblyState.Pose compose(MovingAssemblyState.Pose origin,MovingProgram.Pose local){var r=local.quaternion();var q=origin.orientation().mul(new Quaterniond(r[0],r[1],r[2],r[3]));var p=point(origin,local.x(),local.y(),local.z());return new MovingAssemblyState.Pose(p.x,p.y,p.z,q.x,q.y,q.z,q.w);}
  private static Vector3d foot(Map<BlockPos,MovingPartBlock.Kind> feet,Vector3d contact,MovingPartBlock.Kind kind){
    var pos=BlockPos.containing(contact.x,contact.y,contact.z);feet.put(pos,kind);return new Vector3d(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5);
  }
  public static MovingProgram.Bounds movingBounds(MovingAssemblyState state){
    var m=MovingMechanism.byId(state.mechanism);double y=mountY(state);double half=state.deckLength/2.0;
    boolean linear=m==MovingMechanism.TRANSFER||m==MovingMechanism.DROP||m==MovingMechanism.ELEVATOR||m==MovingMechanism.SIDE_DROP;
    double radius=m==MovingMechanism.TURNTABLE||m==MovingMechanism.COMPOUND?half+3:6;
    return linear?new MovingProgram.Bounds(-1.65,y-.3,-half-2.5,1.65,y+.3,half+2.5):new MovingProgram.Bounds(-radius-.3,Math.min(y,0)-.3,-.35,radius+.3,Math.max(y,0)+.3,.35);
  }
  public static List<AABB> fixedBounds(MovingAssemblyState state){
    var g=geometry(state,compose(state.origin,state.program.build().start()));var boxes=new ArrayList<AABB>();g.fixed.forEach(b->boxes.add(b.bounds()));
    if(state.program.mechanism()==MovingMechanism.TURNTABLE||state.program.mechanism()==MovingMechanism.COMPOUND){
      var center=state.origin.position();double radius=state.deckLength/2.0+3;
      for(int i=0;i<64;i++){double a=i*Math.PI/32,b=(i+1)*Math.PI/32;var first=new Vector3d(center).add(radius*Math.cos(a),0,radius*Math.sin(a));var last=new Vector3d(center).add(radius*Math.cos(b),0,radius*Math.sin(b));boxes.add(new Beam(first,last,.6,"machined").bounds());}
    }return List.copyOf(boxes);
  }
  public static Geometry geometry(MovingAssemblyState state,MovingAssemblyState.Pose actual){
    var fixed=new ArrayList<Beam>();var moving=new ArrayList<Beam>();var races=new ArrayList<Race>();var feet=new LinkedHashMap<BlockPos,MovingPartBlock.Kind>();
    var mechanism=MovingMechanism.byId(state.mechanism);int half=state.deckLength/2;double y=mountY(state);
    var start=compose(state.origin,state.program.build().start());var end=compose(state.origin,state.program.build().end());
    if(mechanism==MovingMechanism.TRANSFER||mechanism==MovingMechanism.DROP||mechanism==MovingMechanism.ELEVATOR||mechanism==MovingMechanism.SIDE_DROP){
      for(int side:new int[]{-1,1})for(int tail:new int[]{-1,1}){
        var a=point(start,side*1.4,y,tail*(half+2.0));var b=point(end,side*1.4,y,tail*(half+2.0));
        fixed.add(new Beam(a,b,.24,"structural"));
        for(var endpoint:List.of(a,b)){
          var f=foot(feet,new Vector3d(endpoint).add(new Vector3d(0,0,tail*2).rotate(state.origin.orientation())).add(0,overhead(state)?1:-1,0),MovingPartBlock.Kind.GUIDE);
          fixed.add(new Beam(f,endpoint,.22,"structural"));
          fixed.add(new Beam(f,foot(feet,new Vector3d(f).add(side,0,0),MovingPartBlock.Kind.LOCK),.2,"structural"));
          fixed.add(new Beam(f,foot(feet,new Vector3d(f).add(0,0,tail),MovingPartBlock.Kind.SOCKET),.2,"structural"));
        }
        var at=point(actual,side*1.4,y,tail*(half+2.0));var mount=point(actual,side*1.4,y,tail*(half-.1));
        moving.add(new Beam(mount,at,.18,"machined"));
        // A retained sleeve visibly straddles the fixed guide at the native body's actual position.
        var direction=new Vector3d(b).sub(a);if(direction.lengthSquared()>1e-10){direction.normalize().mul(.35);moving.add(new Beam(new Vector3d(at).sub(direction),new Vector3d(at).add(direction),.36,"paint_cyan"));}
      }
    }else{
      boolean yaw=mechanism==MovingMechanism.TURNTABLE||mechanism==MovingMechanism.COMPOUND;double radius=yaw?half+3.0:6.0;
      var center=state.origin.position();var right=new Vector3d(1,0,0).rotate(actual.orientation());var up=new Vector3d(0,1,0);
      if(yaw){
        races.add(new Race(new Vector3d(center),up,new Vector3d(1,0,0),radius-.25,radius+.25,.3));
        for(int i=0;i<4;i++){double angle=i*Math.PI/2;var p=new Vector3d(center).add(radius*Math.cos(angle),0,radius*Math.sin(angle));var f=foot(feet,new Vector3d(p).add(0,-half-4,0),MovingPartBlock.Kind.PIVOT);fixed.add(new Beam(f,p,.3,"structural"));foot(feet,new Vector3d(f).add(0,-1,0),MovingPartBlock.Kind.GUIDE);fixed.add(new Beam(f,foot(feet,new Vector3d(f).add(1,0,0),MovingPartBlock.Kind.LOCK),.2,"structural"));fixed.add(new Beam(f,foot(feet,new Vector3d(f).add(-1,0,0),MovingPartBlock.Kind.SOCKET),.2,"structural"));}
      }
      for(int side:new int[]{-1,1}){
        var axis=new Vector3d(center).fma(side*radius,right);var inner=point(actual,side*1.4,0,0);var mount=point(actual,side*1.4,y,0);
        moving.add(new Beam(mount,inner,.22,"structural"));moving.add(new Beam(inner,axis,.20,"machined"));
        races.add(new Race(axis,new Vector3d(right),up,.16,.28,.45));
        if(!yaw){var f=foot(feet,new Vector3d(axis).add(0,-half-4,0),MovingPartBlock.Kind.PIVOT);fixed.add(new Beam(f,axis,.3,"structural"));foot(feet,new Vector3d(f).add(0,-1,0),MovingPartBlock.Kind.GUIDE);fixed.add(new Beam(f,foot(feet,new Vector3d(f).add(side,0,0),MovingPartBlock.Kind.LOCK),.2,"structural"));fixed.add(new Beam(f,foot(feet,new Vector3d(f).add(-side,0,0),MovingPartBlock.Kind.SOCKET),.2,"structural"));}
      }
    }
    return new Geometry(List.copyOf(fixed),List.copyOf(moving),List.copyOf(races),Collections.unmodifiableMap(feet));
  }
}
