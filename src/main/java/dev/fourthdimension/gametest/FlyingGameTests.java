package dev.fourthdimension.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
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
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftEndRef;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphSpans;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension_fd1801")
@PrefixGameTestTemplate(false)
public final class FlyingGameTests {
 @GameTest(template="empty",timeoutTicks=850)
 public static void flyingNativePosture(GameTestHelper h){
  var level=h.getLevel();var from=new BlockPos(-800,120,400);var to=from.offset(0,0,80);
  for(int x=-51;x<=-49;x++)for(int z=24;z<=31;z++)level.setChunkForced(x,z,true);
  for(var p:List.of(from,to))level.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
  var edge=FlyingExamples.connect(level,from,to,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
  var data=FoundationData.get(level);var length=TrackGeometry.table(level,edge).length();
  var rail=new FoundationData.Rail(from,to,Family.FLYING,new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(length,0))),UUID.randomUUID(),FlyingVehicle.VARIANT,TrackGeometry.fingerprint(level,edge),0,true);
  h.assertTrue(FlyingTrack.rejection(level,edge)!=null,"Ordinary track accepted for Flying");
  data.rails.add(rail);TrackPlacement.captureOriginalCurve(rail,edge.bezier());
  var anchor=(CoasterAnchorpointBlockEntity)level.getBlockEntity(from);
  anchor.commitChainLift(Direction.Axis.X,ChainLiftEndRef.anchor(to),new CoasterPathGraphSpans.SpanPath(List.of(new CoasterPathGraphSpans.PathSegment(edge,0,1)),length));
  var motorPos=from.east();level.setBlockAndUpdate(motorPos,AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.WEST));
  var motor=(CreativeMotorBlockEntity)level.getBlockEntity(motorPos);int rpm=64*anchor.getChainLiftTravelSign();motor.generatedSpeed.setValue(rpm);
  var cars=new ArrayList<FoundationData.Vehicle>();double[] mass={0},held={0},peaks={0,0},previous=new double[4];String[] previousState=new String[4];
  h.runAtTickTime(10,()->{
   for(int i=0;i<4;i++){
    var v=FlyingVehicle.create(level,FlyingExamples.sample(level,edge,20+8*i));cars.add(v);
    var dock=from.offset(4,-3,20+8*i);level.setBlockAndUpdate(dock,StationContent.STATION.get().defaultBlockState());
    ((BoardingStationEntity)level.getBlockEntity(dock)).vehicle=v.chassis;v.dock=dock;
    v.flying.dockFrom=from;v.flying.dockTo=to;v.flying.dockGeometry=rail.geometryHash;
   }
  });
  h.runAtTickTime(50,()->{
   for(var v:cars){FlyingRuntime.rebindDock(level,v,v.dock);FlyingVehicle.validate(v);assertHinge(h,v);h.assertTrue(Boarding.ready(level,v),"Seated five-tick dock latch: "+v.fault+" / "+v.flying.phase);}
  });
  h.runAtTickTime(65,()->{
   var container=SubLevelContainer.getContainer(level);var v=cars.getFirst();var c=v.carriers.getFirst();var child=(ServerSubLevel)container.getSubLevel(c.body);
   for(int i=0;i<3;i++)h.assertTrue(CoasterCartTrainLinkConstraint.linkCarts(level,(ServerSubLevel)container.getSubLevel(cars.get(i).chassis),(ServerSubLevel)container.getSubLevel(cars.get(i+1).chassis),8),"Eight-block Flying coupling");
   FlyingRuntime.rebindDock(level,v,v.dock);mass[0]=child.getMassTracker().getMass();
   var pig=EntityType.PIG.create(level);pig.setNoAi(true);level.addFreshEntity(pig);SeatBlock.sitDown(level,child.getPlot().getCenterBlock().offset(c.seats.getFirst()),pig);
   System.out.println("FD_FLYING_EMPTY mass="+mass[0]+" com="+child.getMassTracker().getCenterOfMass()+" inertia="+FlyingRuntime.jointInertia(child,c)+" gravity="+dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData.getGravity(level));
  });
  h.runAtTickTime(80,()->{
   var v=cars.getFirst();var c=v.carriers.getFirst();var child=(ServerSubLevel)SubLevelContainer.getContainer(level).getSubLevel(c.body);
   h.assertTrue(Math.abs(child.getMassTracker().getMass()-mass[0]-.8)<1e-6,"Asymmetric one rider equals .8 native mass");
   for(int i=1;i<4;i++){var pig=EntityType.PIG.create(level);pig.setNoAi(true);level.addFreshEntity(pig);SeatBlock.sitDown(level,child.getPlot().getCenterBlock().offset(c.seats.get(i)),pig);}
  });
  h.runAtTickTime(95,()->{
   var v=cars.getFirst();var c=v.carriers.getFirst();var child=(ServerSubLevel)SubLevelContainer.getContainer(level).getSubLevel(c.body);
   assertHinge(h,v);h.assertTrue(Math.abs(child.getMassTracker().getMass()-mass[0]-3.2)<1e-6&&c.occupants.size()==4,"Four rider loading exactly3.2");
   FlyingRuntime.restraints(level,v,true,true);
   try{FlyingRuntime.requestPosture(level,v,true,false);h.fail("Prone accepted with open leg restraint");}catch(IllegalArgumentException expected){}
   FlyingRuntime.restraints(level,v,false,true);
   try{FlyingRuntime.validateDispatch(level,v);h.fail("Seated dispatch accepted");}catch(IllegalArgumentException expected){}
   FlyingRuntime.requestPosture(level,v,true,false);
  });
  for(int tick=96;tick<780;tick++){final int t=tick;h.runAtTickTime(t,()->{
   var v=cars.getFirst();var c=v.carriers.getFirst();
   for(int i=0;i<cars.size();i++){var car=cars.get(i);var carrier=car.carriers.getFirst();
    if(t>=96){
     peaks[0]=Math.max(peaks[0],Math.abs(carrier.omega));double acceleration=Math.abs(carrier.omega-previous[i])*20;
     if(acceleration>peaks[1]){peaks[1]=acceleration;
      var body=(ServerSubLevel)SubLevelContainer.getContainer(level).getSubLevel(carrier.body);
      System.out.println("FD_FLYING_PEAK tick="+t+" car="+i+" acceleration="+acceleration+" previousOmega="+previous[i]+" omega="+carrier.omega+" previous="+previousState[i]+" current="+car.flying.phase+"/"+car.flying.lock+" capture="+carrier.captureTarget+" motor="+FlyingRuntime.motorObservation(body));
     }
    }previous[i]=carrier.omega;previousState[i]=car.flying.phase+"/"+car.flying.lock;
   }
   if(t%20==0)System.out.println("FD_FLYING_STEP tick="+t+" phi="+(-c.angle)+" rate="+(-c.omega)+" state="+v.flying.phase+" lock="+v.flying.lock+" fault="+v.fault);
  });}
  h.runAtTickTime(150,()->motor.generatedSpeed.setValue(0));
  h.runAtTickTime(175,()->{
   var v=cars.getFirst();var c=v.carriers.getFirst();
   h.assertTrue(v.flying.phase==FlyingState.Phase.INTERRUPTED_TRANSITION&&v.flying.lock==FlyingState.Lock.NONE&&v.restraints&&v.stopping,"Power interruption preserved pose/state: "+v.flying.phase);
   h.assertTrue(-c.angle>.1&&-c.angle<1.4,"Interrupted actual intermediate angle");held[0]=c.angle;
   var copy=FoundationData.load(data.snapshot(new CompoundTag(),level.registryAccess()),level.registryAccess()).vehicles.get(v.chassis);
   FlyingVehicle.validate(copy);h.assertTrue(copy.flying.phase==v.flying.phase&&Math.abs(copy.carriers.getFirst().angle-c.angle)<.001&&copy.flying.target==Math.PI/2,"Same-instant interrupted state roundtrip");
  });
  h.runAtTickTime(205,()->{
   var v=cars.getFirst();h.assertTrue(Math.abs(v.carriers.getFirst().angle-held[0])<.035,"Passive interruption brake drift");
   motor.generatedSpeed.setValue(rpm);
  });
  h.runAtTickTime(225,()->FlyingRuntime.requestPosture(level,cars.getFirst(),true,true));
  h.runAtTickTime(405,()->{
   for(var v:cars)h.assertTrue(v.flying.lock==FlyingState.Lock.PRONE&&Math.abs(v.carriers.getFirst().angle+Math.PI/2)<Math.toRadians(2),"All cars prone locked: "+v.flying.phase+" phi="+(-v.carriers.getFirst().angle)+" rate="+v.carriers.getFirst().omega+" / "+v.fault);
   for(var car:cars)assertHinge(h,car);
   var v=cars.getFirst();FlyingRuntime.validateDispatch(level,v);
   try{FlyingRuntime.restraints(level,v,false,false);h.fail("Prone leg release accepted");}catch(IllegalArgumentException expected){}
   FlyingRuntime.dispatch(level,v);
   try{FlyingRuntime.requestPosture(level,v,false,false);h.fail("In-route seated transition accepted");}catch(IllegalArgumentException expected){}
   FlyingRuntime.capture(level,v);
  });
  h.runAtTickTime(435,()->FlyingRuntime.requestPosture(level,cars.getFirst(),false,false));
  h.runAtTickTime(660,()->{
   var v=cars.getFirst();for(var car:cars)h.assertTrue(car.flying.lock==FlyingState.Lock.SEATED&&Boarding.ready(level,car),"Complete seated return: "+car.fault);
   FlyingRuntime.restraints(level,v,true,false);FlyingRuntime.restraints(level,v,false,false);
   h.assertTrue(!v.restraints&&!v.flying.body&&!v.flying.legs,"Separate restraints open after seated lock");
   System.out.println("FD_FLYING_NATIVE peaks rate="+peaks[0]+" acceleration="+peaks[1]);
   h.assertTrue(peaks[0]<=.25+.002,"Physical posture rate above .25: "+peaks[0]);
   h.assertTrue(peaks[1]<=.35+.01,"Physical posture acceleration above .35: "+peaks[1]);
   System.out.println("FD_FLYING_NATIVE PASS fourCars8=true nativePosture=true equalAsymmetricFullLoads=true separateRetention=true powerInterrupt=true explicitResume=true sameInstantSnapshot=true returnSeated=true");h.succeed();
  });
 }
 private static void assertHinge(GameTestHelper h,FoundationData.Vehicle v){
  var container=SubLevelContainer.getContainer(h.getLevel());var c=v.carriers.getFirst();
  var parent=(ServerSubLevel)container.getSubLevel(v.chassis);var child=(ServerSubLevel)container.getSubLevel(c.body);
  var p=parent.getPlot().getCenterBlock();var b=child.getPlot().getCenterBlock();
  var a=parent.logicalPose().transformPosition(new org.joml.Vector3d(p.getX()+.5+c.side,p.getY()+.5+c.pivotHeight,p.getZ()+.5+c.longitudinal));
  var z=child.logicalPose().transformPosition(new org.joml.Vector3d(b.getX()+.5+c.childPivotX,b.getY()+.5+c.childPivotOffset,b.getZ()+.5+c.childPivotZ));
  var edge=TrackGeometry.resolve(h.getLevel(),FlyingRuntime.rail(h.getLevel(),v));
  var hit=dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.closestHitOnEdge(h.getLevel(),edge,new Vec3(a.x,a.y,a.z));
  double height=a.y-hit.point().y;
  System.out.println("FD_FLYING_HINGE phase="+v.flying.phase+" gap="+a.distance(z)+" graphHeight="+height+" parent="+a+" child="+z+" com="+child.getMassTracker().getCenterOfMass()+" rotationPoint="+child.logicalPose().rotationPoint());
  h.assertTrue(a.distance(z)<.002,"Native/rendered hinge separation: "+a.distance(z));
  h.assertTrue(Math.abs(height+2.5)<.002,"Graph-relative hip height: "+height);
 }
}
