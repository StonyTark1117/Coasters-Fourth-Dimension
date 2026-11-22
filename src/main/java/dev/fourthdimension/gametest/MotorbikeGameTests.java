package dev.fourthdimension.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.station.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftEndRef;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphSpans;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

/** Native production-path guards; occupied player/visual acceptance belongs to the private client. */
@GameTestHolder("fourthdimension_fd2202")
@PrefixGameTestTemplate(false)
public final class MotorbikeGameTests {
 @GameTest(template="empty",timeoutTicks=260)
 public static void motorbikeDockDriveAndRestore(GameTestHelper h){
  var state=new MotorbikeState();state.running=true;state.directions.put(new BlockPos(4,20,-6),-1);
  var loaded=MotorbikeState.read(state.save());h.assertTrue(loaded.running&&loaded.restored&&loaded.directions.equals(state.directions),"Motion intent roundtrip");
  var bad=new CompoundTag();bad.putInt("version",2);
  try{MotorbikeState.read(bad);h.fail("Unknown state version accepted");}catch(IllegalArgumentException expected){}
  var duplicate=new ListTag();bad=new MotorbikeState().save();
  for(int sign:new int[]{1,-1}){var entry=new CompoundTag();entry.putLong("pos",BlockPos.ZERO.asLong());entry.putInt("sign",sign);duplicate.add(entry);}
  bad.put("directions",duplicate);try{MotorbikeState.read(bad);h.fail("Duplicate drive identity accepted");}catch(IllegalArgumentException expected){}
  duplicate.remove(1);duplicate.getCompound(0).putInt("sign",0);try{MotorbikeState.read(bad);h.fail("Zero drive direction accepted");}catch(IllegalArgumentException expected){}
  var l=h.getLevel();var from=new BlockPos(-480,120,-480);var to=from.offset(0,0,80);
  for(int x=-31;x<=-29;x++)for(int z=-31;z<=-24;z++)l.setChunkForced(x,z,true);
  for(var p:List.of(from,to))l.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
  var edge=MotorbikeExamples.connect(l,from,to,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
  h.assertTrue(MotorbikeTrack.rejection(l,edge)!=null,"Unbound native track must not count as Motorbike track");
  var data=FoundationData.get(l);double length=TrackGeometry.table(l,edge).length();
  var rail=new FoundationData.Rail(from,to,Family.MOTORBIKE,new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(length,0))),UUID.randomUUID(),MotorbikeVehicle.VARIANT,TrackGeometry.fingerprint(l,edge),0,true);
  TrackPlacement.captureOriginalCurve(rail,edge.bezier());data.rails.add(rail);
  var a=(CoasterAnchorpointBlockEntity)l.getBlockEntity(from);
  a.commitChainLift(Direction.Axis.X,ChainLiftEndRef.anchor(to),new CoasterPathGraphSpans.SpanPath(List.of(new CoasterPathGraphSpans.PathSegment(edge,0,1)),length));
  for(int i=1;i<=2;i++)l.setBlockAndUpdate(from.east(i),AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS,Direction.Axis.X));
  l.setBlockAndUpdate(from.east(3),AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.WEST));
  var motor=(CreativeMotorBlockEntity)l.getBlockEntity(from.east(3));int rpm=64*a.getChainLiftTravelSign();motor.generatedSpeed.setValue(rpm);
  var stationPos=from.offset(-4,1,20);l.setBlockAndUpdate(stationPos,StationContent.STATION.get().defaultBlockState());
  for(int side:new int[]{-1,1})for(var p:BlockPos.betweenClosed(from.offset(side*3,0,10),from.offset(side*5,0,50)))l.setBlockAndUpdate(p,Blocks.SMOOTH_STONE.defaultBlockState());
  FoundationData.Vehicle[] car={null};Vector3d[] at={null};
  h.runAtTickTime(10,()->{car[0]=MotorbikeVehicle.create(l,MotorbikeExamples.sample(l,edge,20));((BoardingStationEntity)l.getBlockEntity(stationPos)).vehicle=car[0].chassis;});
  h.runAtTickTime(45,()->{
   var v=car[0];MotorbikeVehicle.validate(v);var body=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.chassis);
   h.assertTrue(!Boarding.ready(l,v),"Unbound dock admitted riders");System.out.println("FD2202_DOCK "+MotorbikeRuntime.dockDiagnostic(l,body,v,stationPos));MotorbikeRuntime.rebindDock(l,v,stationPos);
   h.assertTrue(Boarding.ready(l,v),"Bound level Motorbike dock not ready: "+v.fault);
   var old=l.getBlockState(from.offset(3,0,20));l.setBlockAndUpdate(from.offset(3,0,20),Blocks.AIR.defaultBlockState());
   // This platform cell lies under the near-right exit; source geometry determines either side sign.
   h.assertTrue(!Boarding.ready(l,v),"Missing exit floor admitted riders");l.setBlockAndUpdate(from.offset(3,0,20),old);
   try{VehicleOperations.dispatch(l,v);h.fail("Open hold-down dispatched");}catch(IllegalArgumentException expected){}
   VehicleOperations.restraints(l,v,true);at[0]=new Vector3d(body.logicalPose().position());VehicleOperations.dispatch(l,v);
   var copy=FoundationData.load(data.snapshot(new CompoundTag(),l.registryAccess()),l.registryAccess()).vehicles.get(v.chassis);
   h.assertTrue(copy.motorbike.running&&copy.motorbike.restored&&copy.motorbike.directions.size()==1&&copy.carriers.getFirst().body.equals(v.chassis),"Motorbike saved motion/drive/topology roundtrip");
  });
  h.runAtTickTime(85,()->{
   var v=car[0];var body=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.chassis);
   h.assertTrue(body.logicalPose().position().distance(at[0])>2,"Native Motorbike did not travel: "+v.fault);
   motor.generatedSpeed.setValue(0);
  });
  h.runAtTickTime(115,()->{
   var v=car[0];h.assertTrue(v.stopping&&v.restraints&&v.carriers.getFirst().capture&&v.fault.contains("drive"),"Drive loss did not retain closed Motorbike: stop="+v.stopping+" restraint="+v.restraints+" capture="+v.carriers.getFirst().capture+" fault="+v.fault);
   try{MotorbikeRuntime.resumeToDock(l,v);h.fail("Powerless Resume accepted");}catch(IllegalArgumentException expected){}
   motor.generatedSpeed.setValue(rpm);
  });
  h.runAtTickTime(140,()->{var v=car[0];h.assertTrue(v.stopping,"Restored power automatically released");MotorbikeRuntime.resumeToDock(l,v);});
  h.runAtTickTime(165,()->motor.generatedSpeed.setValue(-rpm));
  h.runAtTickTime(195,()->{
   var v=car[0];h.assertTrue(v.stopping&&v.restraints&&v.fault.contains("reversed"),"Drive reversal did not retain: "+v.fault);
   motor.generatedSpeed.setValue(rpm);data.rails.remove(rail);
  });
  h.runAtTickTime(220,()->{
   var v=car[0];h.assertTrue(v.stopping&&v.restraints&&v.fault.contains("Track"),"Support loss did not retain: "+v.fault);
   data.rails.add(rail);System.out.println("FD2202_NATIVE_PASS car="+v.chassis+" dock="+v.dock+" seats="+v.carriers.getFirst().seats+" directions="+v.motorbike.directions);
   h.succeed();
  });
 }
}
