package dev.fourthdimension.vehicle;

import com.google.gson.Gson;
import dev.fourthdimension.runtime.FoundationData;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import java.nio.file.*;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.joml.Vector3d;

/** Opt-in observation of physical chassis/seat transforms; never changes ride state or input. */
@EventBusSubscriber(modid="fourthdimension")
public final class FixedInvertedDiagnostics {
 private static final Gson JSON=new Gson();
 @SubscribeEvent public static void tick(LevelTickEvent.Post event){
  if(!Boolean.getBoolean("fourthdimension.validation")||!(event.getLevel() instanceof ServerLevel level))return;
  var data=FoundationData.get(level);var container=SubLevelContainer.getContainer(level);
  var cars=new ArrayList<Map<String,Object>>();
  for(var v:data.vehicles.values()){
   if(!FixedInvertedVehicle.is(v))continue;
   if(!(container.getSubLevel(v.chassis) instanceof dev.ryanhcode.sable.sublevel.ServerSubLevel body))continue;
   var center=CoasterCartResolve.cartCenterWorld(body);if(center==null)continue;
   if(v.stopping&&v.carriers.getFirst().occupants.isEmpty()&&level.getGameTime()%20!=0)continue;
   var p=body.getPlot().getCenterBlock();var q=body.logicalPose().orientation();var up=new Vector3d(0,1,0).rotate(q);
   var row=new LinkedHashMap<String,Object>();row.put("id",v.chassis.toString());row.put("center",new double[]{center.x,center.y,center.z});
   row.put("q",new double[]{q.x,q.y,q.z,q.w});row.put("upY",up.y);row.put("stopping",v.stopping);row.put("restraints",v.restraints);
   row.put("fault",v.fault);row.put("ready",v.boardingReady);row.put("occupants",v.carriers.getFirst().occupants);
   row.put("mass",body.getMassTracker().getMass());row.put("sameSeatOwner",v.carriers.getFirst().body.equals(v.chassis));
   var seats=new ArrayList<double[]>();for(var s:FixedInvertedVehicle.SEATS){var a=body.logicalPose().transformPosition(new Vector3d(p.getX()+s.getX(),p.getY()+s.getY()+.9375,p.getZ()+s.getZ()+.5));seats.add(new double[]{a.x,a.y,a.z});}
   row.put("seats",seats);cars.add(row);
  }
  if(cars.isEmpty())return;
  try{Files.writeString(Path.of("fd-fixed-trace.jsonl"),JSON.toJson(Map.of("tick",level.getGameTime(),"dimension",level.dimension().location().toString(),"cars",cars))+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);}
  catch(java.io.IOException failure){throw new IllegalStateException("Cannot record Fixed Inverted validation trace",failure);}
 }
}
