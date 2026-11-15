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

/** Opt-in read-only native geometry and occupied motion observations. */
@EventBusSubscriber(modid="fourthdimension")
public final class AxisDiagnostics {
 private static final Gson JSON=new Gson();
 @SubscribeEvent public static void tick(LevelTickEvent.Post event){
  if(!Boolean.getBoolean("fourthdimension.validation")||!(event.getLevel() instanceof ServerLevel level))return;
  var container=SubLevelContainer.getContainer(level);var rows=new ArrayList<Map<String,Object>>();
  for(var v:FoundationData.get(level).vehicles.values()){
   if(!AxisVehicle.is(v))continue;var c=v.carriers.getFirst();
   if(!(container.getSubLevel(v.chassis) instanceof ServerSubLevel parent)||!(container.getSubLevel(c.body) instanceof ServerSubLevel child))continue;
   if(v.stopping&&c.occupants.isEmpty()&&level.getGameTime()%20!=0)continue;
   var center=CoasterCartResolve.cartCenterWorld(parent);if(center==null)continue;var p=child.getPlot().getCenterBlock();var q=parent.getPlot().getCenterBlock();
   var row=new LinkedHashMap<String,Object>();row.put("id",v.chassis.toString());row.put("body",c.body.toString());
   row.put("center",vector(new Vector3d(center)));row.put("angle",c.angle);row.put("omega",c.omega);row.put("mode",v.axis.mode);
   row.put("stopping",v.stopping);row.put("fault",v.fault);row.put("ready",v.boardingReady);row.put("restraints",v.restraints);row.put("capture",c.capture);row.put("occupants",c.occupants);
   row.put("parentUp",vector(new Vector3d(0,1,0).rotate(parent.logicalPose().orientation())));row.put("podUp",vector(new Vector3d(0,1,0).rotate(child.logicalPose().orientation())));
   row.put("parentHinge",vector(parent.logicalPose().transformPosition(new Vector3d(q.getX()+.5+c.side,q.getY()+.5+c.pivotHeight,q.getZ()+.5+c.longitudinal))));
   row.put("childHinge",vector(child.logicalPose().transformPosition(new Vector3d(p.getX()+.5+c.childPivotX,p.getY()+.5+c.childPivotOffset,p.getZ()+.5+c.childPivotZ))));
   row.put("mass",child.getMassTracker().getMass());var seats=new ArrayList<double[]>();
   for(var s:c.seats)seats.add(vector(child.logicalPose().transformPosition(new Vector3d(p.getX()+s.getX()+.5,p.getY()+s.getY()+.5,p.getZ()+s.getZ()+.5))));
   row.put("hips",seats);rows.add(row);
  }
  if(rows.isEmpty())return;
  try{Files.writeString(Path.of("fd-axis-trace.jsonl"),JSON.toJson(Map.of("tick",level.getGameTime(),"cars",rows))+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);}
  catch(java.io.IOException failure){throw new IllegalStateException("Cannot record Axis validation trace",failure);}
 }
 private static double[] vector(Vector3d v){return new double[]{v.x,v.y,v.z};}
}
