package dev.fourthdimension.gametest;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.station.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;
import java.util.*;
@GameTestHolder("fourthdimension_fd1301")
@PrefixGameTestTemplate(false)
public final class SuspendedGameTests {
 @GameTest(template="empty",timeoutTicks=650)
 public static void nativeLimitsAndCapture(GameTestHelper h) {
  limitsAndCapture(h,600);
 }
 @GameTest(template="empty",timeoutTicks=650)
 public static void nativeRepairedJournal(GameTestHelper h) { limitsAndCapture(h,-700); }
 private static void limitsAndCapture(GameTestHelper h,int originX) {
  var level=h.getLevel();var from=new BlockPos(originX,100,400);var to=from.offset(0,0,80);
  for(int x=(originX>>4)-1;x<=(originX>>4)+1;x++)for(int z=24;z<=31;z++)level.setChunkForced(x,z,true);
  for(var p:List.of(from,to))level.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
  var edge=WingExamples.connect(level,from,to,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
  var data=FoundationData.get(level);
  var rail=new FoundationData.Rail(edge.from(),edge.to(),Family.SUSPENDED,
    new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(TrackGeometry.table(level,edge).length(),0))),
    UUID.randomUUID(),SuspendedVehicle.VARIANT,TrackGeometry.fingerprint(level,edge),0,true);
  data.rails.add(rail);TrackPlacement.captureOriginalCurve(rail,edge.bezier());
  var cars=new FoundationData.Vehicle[2];var empty=new double[2];var peak=new double[2];var reaction=new double[2];
  h.runAtTickTime(10,()->{
   h.assertTrue(SuspendedTrack.rejection(level,edge)==null,"Overhead track rejected");
   for(int i=0;i<2;i++){
    cars[i]=SuspendedVehicle.create(level,WingExamples.sample(level,edge,20+i*25));
    var pos=from.offset(5,-3,20+i*25);level.setBlockAndUpdate(pos,StationContent.STATION.get().defaultBlockState());
    ((BoardingStationEntity)level.getBlockEntity(pos)).vehicle=cars[i].chassis;cars[i].dock=pos;
   }
  });
  h.runAtTickTime(55,()->{
   var container=SubLevelContainer.getContainer(level);
   for(int i=0;i<2;i++){
    var v=cars[i];var c=v.carriers.getFirst();var body=(ServerSubLevel)container.getSubLevel(c.body);
    h.assertTrue(Boarding.ready(level,v),"Initial dock not ready: "+v.fault+" a="+c.angle+" w="+c.omega);
    h.assertTrue(level.getBlockState(body.getPlot().getCenterBlock().below()).is(VehicleParts.SUSPENDED_LOCK.get()),"Connected journal split off");
    empty[i]=body.getMassTracker().getMass();
    h.assertTrue(Math.abs(empty[i]-5)<1e-6,"Connected journal mass must be 5: "+empty[i]);
    System.out.println("FD_SUSPENDED_EMPTY car="+i+" mass="+empty[i]+" com="+body.getMassTracker().getCenterOfMass()+" inertia="+SuspendedRuntime.jointInertia(body,c));
    for(int s:i==0?new int[]{0,1}:new int[]{1}){
     var pig=EntityType.PIG.create(level);pig.setNoAi(true);level.addFreshEntity(pig);
     SeatBlock.sitDown(level,body.getPlot().getCenterBlock().offset(c.seats.get(s)),pig);
    }
   }
  });
  h.runAtTickTime(75,()->{
   var container=SubLevelContainer.getContainer(level);
   for(int i=0;i<2;i++){
    var v=cars[i];var c=v.carriers.getFirst();var body=(ServerSubLevel)container.getSubLevel(c.body);
    h.assertTrue(Math.abs(body.getMassTracker().getMass()-empty[i]-(i==0?1.6:.8))<1e-5,"Native rider mass incorrect");
    VehicleOperations.restraints(level,v,true);VehicleOperations.dispatch(level,v);
   }
  });
  h.runAtTickTime(130,()->{
   var container=SubLevelContainer.getContainer(level);var pipeline=container.physicsSystem().getPipeline();
   for(int i=0;i<2;i++){
    var v=cars[i];var c=v.carriers.getFirst();var body=(ServerSubLevel)container.getSubLevel(c.body);
    var parent=(ServerSubLevel)container.getSubLevel(v.chassis);var p=parent.getPlot().getCenterBlock();
    var pivot=parent.logicalPose().transformPosition(new Vector3d(p.getX()+.5,p.getY()+.5+c.pivotHeight,p.getZ()+.5));
    // Disclosed limit fixture: start at ±40 degrees before a measured sustained outward angular impulse.
    double sign=i==0?1:-1;var q=new org.joml.Quaterniond(parent.logicalPose().orientation()).rotateZ(sign*Math.toRadians(40));
    body.logicalPose().position().set(pivot);body.logicalPose().orientation().set(q);body.updateLastPose();
    pipeline.teleport(body,pivot,q);pipeline.resetVelocity(body);
    pipeline.addLinearAndAngularVelocity(body,new Vector3d(),new Vector3d(0,0,0).rotate(parent.logicalPose().orientation()));
   }
  });
  for(int tick=131;tick<=170;tick++)h.runAtTickTime(tick,()->{
   var container=SubLevelContainer.getContainer(level);var pipeline=container.physicsSystem().getPipeline();
   for(int i=0;i<2;i++){
    var c=cars[i].carriers.getFirst();var body=(ServerSubLevel)container.getSubLevel(c.body);
    var parent=(ServerSubLevel)container.getSubLevel(cars[i].chassis);
    double inertia=SuspendedRuntime.jointInertia(body,c), sign=i==0?1:-1;
    pipeline.applyLinearAndAngularImpulse(body,new Vector3d(),new Vector3d(0,0,sign*inertia*20*.05).rotate(parent.logicalPose().orientation()),true);
    reaction[i]=Math.max(reaction[i],CarrierRuntime.jointAngularImpulseForValidation(body).length());
   }
  });
  for(int tick=140;tick<=220;tick++)h.runAtTickTime(tick,()->{
   for(int i=0;i<2;i++){
    var c=cars[i].carriers.getFirst();peak[i]=Math.max(peak[i],Math.abs(c.angle));
    h.assertTrue(Math.abs(c.angle)<=Math.PI/4+.025,"Hard limit exceeded: "+c.angle);
    h.assertTrue(!Boarding.ready(level,cars[i]),"Moving/faulted cabin allowed boarding");
   }
  });
  h.runAtTickTime(230,()->{
   System.out.println("FD_SUSPENDED_LIMIT peaks="+Arrays.toString(peak)+" reaction="+Arrays.toString(reaction));
   for(int i=0;i<2;i++)h.assertTrue(peak[i]>Math.toRadians(44)&&Double.isFinite(reaction[i]),"Outward-force boundary probe invalid: "+peak[i]+" / "+reaction[i]);
   for(var v:cars)VehicleOperations.capture(level,v);
  });
  h.runAtTickTime(550,()->{
   for(var v:cars){var c=v.carriers.getFirst();h.assertTrue(Boarding.ready(level,v),"Capture failed "+v.fault+" a="+c.angle+" w="+c.omega+" latched="+c.latched+" held="+c.liftHeld);}
   var saved=data.save(new CompoundTag(),level.registryAccess());var loaded=FoundationData.load(saved,level.registryAccess());
   for(var v:cars){var c=v.carriers.getFirst();var copy=loaded.vehicles.get(v.chassis).carriers.getFirst();
    h.assertTrue(copy.seats.equals(SuspendedVehicle.SEATS)&&copy.pivotHeight==SuspendedVehicle.PIVOT&&Math.abs(copy.angle-c.angle)<1e-9,"Z topology/pose roundtrip failed");
    VehicleOperations.restraints(level,v,false);
   }
   System.out.println("FD_SUSPENDED_NATIVE axis=Z outwardTorque20I=true nativeReaction=true hardLimit45=true capture=true persistence=true");h.succeed();
  });
 }
 @GameTest(template="empty",timeoutTicks=1200)
 public static void nativeFourCarLiftAndInterruption(GameTestHelper h) {
  var level=h.getLevel();var from=new BlockPos(-400,100,400);var middle=from.offset(0,0,80);var end=from.offset(0,0,160);
  for(int x=-26;x<=-24;x++)for(int z=24;z<=36;z++)level.setChunkForced(x,z,true);
  for(var p:List.of(from,middle,end))level.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
  var first=WingExamples.connect(level,from,middle,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
  var second=WingExamples.connect(level,middle,end,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
  var data=FoundationData.get(level);var rails=new ArrayList<FoundationData.Rail>();
  for(var edge:List.of(first,second)) {
   var rail=new FoundationData.Rail(edge.from(),edge.to(),Family.SUSPENDED,
     new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(TrackGeometry.table(level,edge).length(),0))),
     UUID.randomUUID(),SuspendedVehicle.VARIANT,TrackGeometry.fingerprint(level,edge),0,true);
   rail.liftLock=edge==first;data.rails.add(rail);rails.add(rail);TrackPlacement.captureOriginalCurve(rail,edge.bezier());
  }
  var cars=new ArrayList<FoundationData.Vehicle>();
  var motors=new ArrayList<com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity>();
  for(var edge:List.of(first,second)) {
   var anchor=(dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity)level.getBlockEntity(edge.from());
   var path=new dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphSpans.SpanPath(
     List.of(new dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphSpans.PathSegment(edge,0,1)),TrackGeometry.table(level,edge).length());
   anchor.commitChainLift(net.minecraft.core.Direction.Axis.X,dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftEndRef.anchor(edge.to()),path);
   var motor=edge.from().east();level.setBlockAndUpdate(motor,com.simibubi.create.AllBlocks.CREATIVE_MOTOR.getDefaultState()
     .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING,net.minecraft.core.Direction.WEST));
   var be=(com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity)level.getBlockEntity(motor);
   be.generatedSpeed.setValue(32*anchor.getChainLiftTravelSign());motors.add(be);
  }
  h.runAtTickTime(10,()->{
   for(int i=0;i<4;i++){
    var v=SuspendedVehicle.create(level,WingExamples.sample(level,first,48+8*i));cars.add(v);
    var dock=from.offset(5,-3,48+8*i);level.setBlockAndUpdate(dock,StationContent.STATION.get().defaultBlockState());
    ((BoardingStationEntity)level.getBlockEntity(dock)).vehicle=v.chassis;v.dock=dock;
   }
  });
  h.runAtTickTime(60,()->{
   var container=SubLevelContainer.getContainer(level);
   for(var v:cars)h.assertTrue(Boarding.ready(level,v),"Four-car initial capture failed: "+v.fault);
   for(int i=0;i<3;i++)h.assertTrue(dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint.linkCarts(level,
     (ServerSubLevel)container.getSubLevel(cars.get(i).chassis),(ServerSubLevel)container.getSubLevel(cars.get(i+1).chassis),8),"Eight-block coupling rejected");
   h.assertTrue(VehicleOperations.consist(level,cars.getFirst()).size()==4,"Four-car native train missing");
   VehicleOperations.restraints(level,cars.getFirst(),true);VehicleOperations.dispatch(level,cars.getFirst());
  });
  h.runAtTickTime(100,()->{
   for(var v:cars)h.assertTrue(v.carriers.getFirst().liftHeld && !v.stopping,"Lift lock not retained: "+v.fault);
   motors.getFirst().generatedSpeed.setValue(0);
  });
  h.runAtTickTime(170,()->{
   for(var v:cars)h.assertTrue(v.stopping && v.restraints && v.carriers.getFirst().liftHeld,"Power loss released train lock");
   var copy=FoundationData.load(data.save(new CompoundTag(),level.registryAccess()),level.registryAccess());
   for(var v:cars)h.assertTrue(copy.vehicles.get(v.chassis).carriers.getFirst().liftHeld,"Saved lift lock lost");
   var anchor=(dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity)level.getBlockEntity(from);
   motors.getFirst().generatedSpeed.setValue(32*anchor.getChainLiftTravelSign());
  });
  h.runAtTickTime(220,()->SuspendedRuntime.resumeToDock(level,cars.getFirst()));
  h.runAtTickTime(700,()->{
   for(var v:cars)h.assertTrue(!v.stopping && !v.carriers.getFirst().liftHeld,"Rear-clear release failed: "+v.fault+" held="+v.carriers.getFirst().liftHeld);
   var dock=from.offset(5,-3,110);level.setBlockAndUpdate(dock,StationContent.STATION.get().defaultBlockState());
   ((BoardingStationEntity)level.getBlockEntity(dock)).vehicle=cars.getFirst().chassis;
   for(var v:cars)v.dock=dock;
   VehicleOperations.capture(level,cars.getFirst());
  });
  h.runAtTickTime(1000,()->{
   for(var v:cars)h.assertTrue(Boarding.ready(level,v),"Four-car recapture failed: "+v.fault);
   System.out.println("FD_SUSPENDED_TRAIN cars=4 distance=8 nativeChain=true retainedPowerLoss=true explicitResume=true rearClearRelease=true recapture=true");h.succeed();
  });
 }

 @GameTest(template="empty",timeoutTicks=140)
 public static void nativePendingLiftInterruption(GameTestHelper h) {
  var level=h.getLevel();var from=new BlockPos(800,100,400);var to=from.offset(0,0,64);
  for(int x=49;x<=51;x++)for(int z=24;z<=30;z++)level.setChunkForced(x,z,true);
  for(var pos:List.of(from,to))level.setBlockAndUpdate(pos,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
  var edge=WingExamples.connect(level,from,to,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
  var data=FoundationData.get(level);double length=TrackGeometry.table(level,edge).length();
  var rail=new FoundationData.Rail(edge.from(),edge.to(),Family.SUSPENDED,new RotationProfile(List.of(
    new RotationProfile.Key(0,0),new RotationProfile.Key(length,0))),UUID.randomUUID(),SuspendedVehicle.VARIANT,TrackGeometry.fingerprint(level,edge),0,true);
  data.rails.add(rail);TrackPlacement.captureOriginalCurve(rail,edge.bezier());
  var anchor=(dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity)level.getBlockEntity(from);
  anchor.commitChainLift(net.minecraft.core.Direction.Axis.X,dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftEndRef.anchor(to),
    new dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphSpans.SpanPath(List.of(
      new dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphSpans.PathSegment(edge,0,1)),length));
  level.setBlockAndUpdate(from.east(),com.simibubi.create.AllBlocks.CREATIVE_MOTOR.getDefaultState()
    .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING,net.minecraft.core.Direction.WEST));
  ((com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity)level.getBlockEntity(from.east())).generatedSpeed.setValue(32*anchor.getChainLiftTravelSign());
  var car=new FoundationData.Vehicle[1];
  h.runAtTickTime(10,()->{
    car[0]=SuspendedVehicle.create(level,WingExamples.sample(level,edge,20));var dock=from.offset(5,-3,20);
    level.setBlockAndUpdate(dock,StationContent.STATION.get().defaultBlockState());
    ((BoardingStationEntity)level.getBlockEntity(dock)).vehicle=car[0].chassis;car[0].dock=dock;
  });
  h.runAtTickTime(55,()->{
    var v=car[0];h.assertTrue(Boarding.ready(level,v),"Pending-entry fixture not initially ready: "+v.fault);
    var c=v.carriers.getFirst();var container=SubLevelContainer.getContainer(level);
    var parent=(ServerSubLevel)container.getSubLevel(v.chassis);var child=(ServerSubLevel)container.getSubLevel(c.body);
    var p=parent.getPlot().getCenterBlock();var pivot=parent.logicalPose().transformPosition(new Vector3d(p.getX()+.5,p.getY()+.5+c.pivotHeight,p.getZ()+.5));
    var q=new org.joml.Quaterniond(parent.logicalPose().orientation()).rotateZ(Math.toRadians(30));
    child.logicalPose().position().set(pivot);child.logicalPose().orientation().set(q);child.updateLastPose();
    container.physicsSystem().getPipeline().teleport(child,pivot,q);
    rail.liftLock=true;c.latched=false;c.capture=false;v.restraints=true;v.stopping=false;
  });
  h.runAtTickTime(58,()->{
    var v=car[0];var c=v.carriers.getFirst();
    h.assertTrue(v.stopping&&v.restraints&&c.liftPending&&!c.liftHeld,"Unsafe pending entry acquired lock or released restraints");
    var loaded=FoundationData.load(data.save(new CompoundTag(),level.registryAccess()),level.registryAccess()).vehicles.get(v.chassis);
    var copy=loaded.carriers.getFirst();
    h.assertTrue(copy.liftPending&&!copy.liftHeld&&Math.abs(copy.angle-c.angle)<1e-9,"Pending interruption did not preserve unlocked pose");
    System.out.println("FD_SUSPENDED_PENDING stopped=true restrained=true pending=true held=false persisted=true angle="+c.angle);h.succeed();
  });
 }

 @GameTest(template="empty",timeoutTicks=600)
 public static void retainedNativeCapture(GameTestHelper h) {
  var level=h.getLevel();var data=FoundationData.get(level);
  var cars=data.vehicles.values().stream().filter(v->SuspendedVehicle.is(v)&&v.boundFrom.getX()==600).toList();
  h.assertTrue(cars.size()==2,"Retained limit fixture missing");
  h.runAtTickTime(5,()->{
   for(var v:cars){var c=v.carriers.getFirst();
    var body=(ServerSubLevel)SubLevelContainer.getContainer(level).getSubLevel(c.body);var p=body.getPlot().getCenterBlock();
    var arm=new Vector3d(body.getMassTracker().getCenterOfMass()).sub(p.getX()+.5+c.childPivotX,p.getY()+.5+c.childPivotOffset,p.getZ()+.5+c.childPivotZ);
    double radius=Math.sqrt(arm.x*arm.x+arm.y*arm.y),mass=body.getMassTracker().getMass(),inertia=SuspendedRuntime.jointInertia(body,c);
    double gravityBound=mass*9.81*radius,brakeBound=mass*6*radius,motorBound=inertia*10;
    System.out.println("FD_SUSPENDED_FORCE_BUDGET mass="+mass+" I="+inertia+" radius="+radius+" applied="+(20*inertia)+" gravityMax="+gravityBound+" brakeMax="+brakeBound+" motorMax="+motorBound);
    h.assertTrue(20*inertia>gravityBound+brakeBound+motorBound,"Outward load did not dominate possible non-limit torques");
    System.out.println("FD_SUSPENDED_CAPTURE_START angle="+c.angle+" omega="+c.omega+" fault="+v.fault);VehicleOperations.capture(level,v);
   }
  });
  h.runAtTickTime(500,()->{
   for(var v:cars){var c=v.carriers.getFirst();h.assertTrue(Boarding.ready(level,v),"Retained capture failed: "+v.fault+" angle="+c.angle+" omega="+c.omega+" latched="+c.latched);}
   var copy=FoundationData.load(data.save(new CompoundTag(),level.registryAccess()),level.registryAccess());
   for(var v:cars){var c=v.carriers.getFirst();var saved=copy.vehicles.get(v.chassis).carriers.getFirst();h.assertTrue(saved.seats.equals(SuspendedVehicle.SEATS)&&saved.angle==c.angle&&saved.omega==c.omega,"Capture save mismatch");VehicleOperations.restraints(level,v,false);}
   System.out.println("FD_SUSPENDED_RETAINED_CAPTURE dockReady=true open=true poseAndSeatSave=true");h.succeed();
  });
 }

 @GameTest(template="empty",timeoutTicks=180)
 public static void retainedDockRebind(GameTestHelper h) {
  var level=h.getLevel();var data=FoundationData.get(level);
  var cars=data.vehicles.values().stream().filter(v->SuspendedVehicle.is(v)&&v.boundFrom.getX()==-400).toList();
  h.assertTrue(cars.size()==4,"Retained four-car fixture missing");
  h.runAtTickTime(20,()->{
   var selected=cars.getFirst();var oldDock=selected.dock;var oldIds=List.copyOf(selected.suspendedConsist);
   selected.stopping=false;
   boolean rejected=false;
   try {SuspendedRuntime.rebindDock(level,selected,oldDock);}catch(IllegalArgumentException expected){rejected=true;}
   selected.stopping=true;
   h.assertTrue(rejected&&selected.suspendedConsist.equals(oldIds)&&selected.dock.equals(oldDock),"Moving rebind mutated state");
   for(var v:cars){v.suspendedConsist=List.of(v.chassis,UUID.randomUUID());v.fault="Suspended coupler/consist changed; stop and rebind at dock";}
   SuspendedRuntime.rebindDock(level,selected,oldDock);
   var ids=new HashSet<>(cars.stream().map(v->v.chassis).toList());
   for(var v:cars)h.assertTrue(new HashSet<>(v.suspendedConsist).equals(ids)&&v.fault.isEmpty(),"Explicit stopped rebind did not recover changed consist");
  });
  h.runAtTickTime(100,()->{
   for(var v:cars)h.assertTrue(Boarding.ready(level,v),"Rebound train not ready: "+v.fault);
   System.out.println("FD_SUSPENDED_REBIND movingDenied=true unchangedOnReject=true stoppedSnapshotRepaired=true ready=true");h.succeed();
  });
 }

}
