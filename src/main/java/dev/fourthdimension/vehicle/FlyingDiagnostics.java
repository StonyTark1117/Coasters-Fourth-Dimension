package dev.fourthdimension.vehicle;

import com.google.gson.Gson;
import dev.fourthdimension.runtime.FoundationData;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import java.nio.file.*;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.joml.Vector3d;

/** Opt-in read-only actual posture/body/rider observation. */
@EventBusSubscriber(modid="fourthdimension")
public final class FlyingDiagnostics {
 private static final Gson JSON=new Gson();
 @SubscribeEvent public static void tick(LevelTickEvent.Post event){
  if(!Boolean.getBoolean("fourthdimension.validation")||!(event.getLevel() instanceof ServerLevel level))return;
  var container=SubLevelContainer.getContainer(level);var rows=new ArrayList<Map<String,Object>>();
  for(var v:FoundationData.get(level).vehicles.values()){
   if(!FlyingVehicle.is(v))continue;var c=v.carriers.getFirst();
   if(!(container.getSubLevel(v.chassis) instanceof ServerSubLevel parent)||!(container.getSubLevel(c.body) instanceof ServerSubLevel child))continue;
   if(v.stopping&&c.occupants.isEmpty()&&!v.flying.transitioning()&&level.getGameTime()%20!=0)continue;
   var center=CoasterCartResolve.cartCenterWorld(parent);if(center==null)continue;var s=v.flying;var p=child.getPlot().getCenterBlock();
   var row=new LinkedHashMap<String,Object>();row.put("id",v.chassis.toString());row.put("body",c.body.toString());
   row.put("center",new double[]{center.x,center.y,center.z});row.put("phi",-c.angle);row.put("phiDot",-c.omega);
   row.put("phase",s.phase.name());row.put("lock",s.lock.name());row.put("target",s.target);row.put("progress",s.progress);
   row.put("bodyRetained",s.body);row.put("legsRetained",s.legs);row.put("stopping",v.stopping);row.put("fault",v.fault);
   row.put("interruption",s.interruption);row.put("ready",v.boardingReady);row.put("occupants",c.occupants);
   row.put("parentUp",vector(new Vector3d(0,1,0).rotate(parent.logicalPose().orientation())));
   row.put("torsoAxis",vector(new Vector3d(0,1,0).rotate(child.logicalPose().orientation())));
   row.put("motor",FlyingRuntime.motorObservation(child));row.put("captureAngle",c.captureTarget);row.put("holdAngle",s.holdAngle);
   row.put("mass",child.getMassTracker().getMass());row.put("inertia",FlyingRuntime.jointInertia(child,c));
   var seats=new ArrayList<double[]>();var torso=new ArrayList<double[]>();
   for(var offset:c.seats){var hip=new Vector3d(p.getX()+offset.getX()+.5,p.getY()+offset.getY()+.5,p.getZ()+offset.getZ()+.5);
    seats.add(vector(child.logicalPose().transformPosition(new Vector3d(hip))));torso.add(vector(child.logicalPose().transformPosition(hip.add(0,.625,0))));}
   var parentPoint=parent.getPlot().getCenterBlock();
   row.put("parentHinge",vector(parent.logicalPose().transformPosition(new Vector3d(parentPoint.getX()+.5+c.side,parentPoint.getY()+.5+c.pivotHeight,parentPoint.getZ()+.5+c.longitudinal))));
   row.put("childHinge",vector(child.logicalPose().transformPosition(new Vector3d(p.getX()+.5+c.childPivotX,p.getY()+.5+c.childPivotOffset,p.getZ()+.5+c.childPivotZ))));
   row.put("rotationPoint",vector(new Vector3d(child.logicalPose().rotationPoint())));
   row.put("centerOfMass",vector(new Vector3d(child.getMassTracker().getCenterOfMass())));
   row.put("hips",seats);row.put("torsoLoads",torso);rows.add(row);
  }
  if(rows.isEmpty())return;
  try{Files.writeString(Path.of("fd-flying-trace.jsonl"),JSON.toJson(Map.of("tick",level.getGameTime(),"cars",rows))+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);}
  catch(java.io.IOException failure){throw new IllegalStateException("Cannot record Flying validation trace",failure);}
 }
 private static double[] vector(Vector3d v){return new double[]{v.x,v.y,v.z};}
}
