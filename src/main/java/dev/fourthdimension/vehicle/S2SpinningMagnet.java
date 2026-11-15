package dev.fourthdimension.vehicle;

import dev.fourthdimension.physics.MagneticSpan;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackGeometry;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

/** External local-Y spin field. It applies torque only; longitudinal Chain remains independent. */
public final class S2SpinningMagnet {
  private record Sample(UUID rail,long revision,double distance){}
  private static final Map<ServerSubLevel,Sample> PREVIOUS=new WeakHashMap<>();
  public static final class Audit {public long impulses;public double peakCommandAlpha,damperWork;}
  private static final Map<ServerSubLevel,Audit> AUDIT=new WeakHashMap<>();
  private S2SpinningMagnet(){}
  public static Audit audit(ServerSubLevel body){return AUDIT.computeIfAbsent(body,k->new Audit());}
  public static void update(ServerLevel level,ServerSubLevel parent,ServerSubLevel child,
      FoundationData.Vehicle v,FoundationData.Carrier c,FoundationData.Rail rail,double dt){
    if(rail==null)return;
    var edge=TrackGeometry.resolve(level,rail);var center=CoasterCartResolve.cartCenterWorld(parent);
    if(edge==null||center==null)return;
    var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
    if(hit==null)return;
    double distance=TrackGeometry.distance(level,edge,rail,hit.t());
    var previous=PREVIOUS.put(child,new Sample(rail.id,rail.revision,distance));
    if(c.capture||c.liftHeld||v.stopping||v.maintenance||previous==null)return;
    double alpha=0;
    for(var span:rail.magnets){
      if(!span.targets(0))continue;
      double delta=previous.rail.equals(rail.id)&&previous.revision==rail.revision?Math.abs(distance-previous.distance):0;
      double weight=delta>1e-8?span.weightedLength(previous.distance,distance)/delta:span.weight(distance);
      alpha+=dev.fourthdimension.physics.S2SpinningField.acceleration(span,c.omega,weight,dt);
    }
    alpha=Math.clamp(alpha,-1,1);
    if(Math.abs(c.omega)>=3)alpha=-Math.signum(c.omega)*Math.min(Math.abs(alpha),Math.abs(c.omega)/dt);
    if(alpha==0)return;
    double inertia=SpinningRuntime.jointInertia(child,c);
    if(!Double.isFinite(inertia)||inertia<=0)return;
    var axis=new Vector3d(0,1,0).rotate(parent.logicalPose().orientation());
    // Rapier's native force/torque API in impulse mode; the pivot's parallel-axis inertia
    // accounts for asymmetric seats. No angle/velocity assignment and no travel impulse.
    SubLevelContainer.getContainer(level).physicsSystem().getPipeline()
        .applyLinearAndAngularImpulse(child,new Vector3d(),axis.mul(inertia*alpha*dt),true);
    var a=audit(child);a.impulses++;a.peakCommandAlpha=Math.max(a.peakCommandAlpha,Math.abs(alpha));
    if(alpha*c.omega<0)a.damperWork+=inertia*alpha*c.omega*dt;
  }
}
