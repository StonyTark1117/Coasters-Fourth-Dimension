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
import org.joml.Vector3d;

@GameTestHolder("fourthdimension_fd1802")
@PrefixGameTestTemplate(false)
public final class NewGenerationFlyingGameTests {
 @GameTest(template="empty",timeoutTicks=1100)
 public static void nativeCompound(GameTestHelper h){
  var l=h.getLevel();var from=new BlockPos(-800,120,400);var to=from.offset(0,0,100);
  for(int x=-51;x<=-49;x++)for(int z=24;z<=32;z++)l.setChunkForced(x,z,true);
  for(var p:List.of(from,to))l.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
  var edge=FlyingExamples.connect(l,from,to,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
  var data=FoundationData.get(l);double length=TrackGeometry.table(l,edge).length();
  var rail=new FoundationData.Rail(from,to,Family.FLYING,new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(length,0))),UUID.randomUUID(),NewGenerationFlyingVehicle.VARIANT,TrackGeometry.fingerprint(l,edge),0,true);
  data.rails.add(rail);TrackPlacement.captureOriginalCurve(rail,edge.bezier());rail.yaw.geometry=rail.geometryHash;
  // A constant section first exercises both native motors simultaneously at a held track position.
  rail.yaw.intervals=List.of(new YawSection.Interval(UUID.randomUUID(),"Compound",0,length,YawState.Mode.PROFILE,0,Math.PI/2,Math.PI/2,new RotationProfile(List.of(new RotationProfile.Key(0,Math.PI),new RotationProfile.Key(length,Math.PI))),null,null,"",YawState.Winding.NEAREST,.6,.8,"",0));
  var anchor=(CoasterAnchorpointBlockEntity)l.getBlockEntity(from);anchor.commitChainLift(Direction.Axis.X,ChainLiftEndRef.anchor(to),new CoasterPathGraphSpans.SpanPath(List.of(new CoasterPathGraphSpans.PathSegment(edge,0,1)),length));
  for(int n=1;n<=2;n++)l.setBlockAndUpdate(from.east(n),AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS,Direction.Axis.X));
  l.setBlockAndUpdate(from.east(3),AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.WEST));
  ((CreativeMotorBlockEntity)l.getBlockEntity(from.east(3))).generatedSpeed.setValue(32*anchor.getChainLiftTravelSign());
  FoundationData.Vehicle[] car={null};double[] baseline={0},previous={0,0},peaks={0,0,0,0},maximumGap={0};
  h.runAtTickTime(10,()->{
   var v=NewGenerationFlyingVehicle.create(l,FlyingExamples.sample(l,edge,30));car[0]=v;v.yaw.owner=UUID.randomUUID();rail.yaw.owner=v.yaw.owner;
   var dock=from.offset(4,-4,30);l.setBlockAndUpdate(dock,StationContent.STATION.get().defaultBlockState());((BoardingStationEntity)l.getBlockEntity(dock)).vehicle=v.chassis;v.dock=dock;v.flying.dockFrom=from;v.flying.dockTo=to;v.flying.dockGeometry=rail.geometryHash;
  });
  h.runAtTickTime(50,()->{var v=car[0];NewGenerationFlyingVehicle.validate(v);h.assertTrue(Boarding.ready(l,v),"Initial physical seated/yaw zero capture: "+v.fault);var child=body(h,v.carriers.getFirst().body);baseline[0]=child.getMassTracker().getMass();mount(h,v,0);});
  h.runAtTickTime(65,()->{var v=car[0];h.assertTrue(Math.abs(body(h,v.carriers.getFirst().body).getMassTracker().getMass()-baseline[0]-.8)<1e-6,"One rider adds exactly .8 to inner native body");mount(h,v,1);});
  h.runAtTickTime(80,()->{var v=car[0];h.assertTrue(Math.abs(body(h,v.carriers.getFirst().body).getMassTracker().getMass()-baseline[0]-1.6)<1e-6,"Full two-seat mass");NewGenerationFlyingRuntime.restraints(l,v,true,true);NewGenerationFlyingRuntime.restraints(l,v,false,true);NewGenerationFlyingRuntime.dispatch(l,v);NewGenerationFlyingRuntime.capture(l,v);});
  for(int tick=81;tick<=1000;tick++){final int t=tick;h.runAtTickTime(t,()->{
   var v=car[0];var c=v.carriers.getFirst();double[] rates={v.yaw.velocity,c.omega};
   for(int i=0;i<2;i++){peaks[i]=Math.max(peaks[i],Math.abs(rates[i]));if(t>82){double acceleration=Math.abs(rates[i]-previous[i])*20;if(acceleration>peaks[i+2]){peaks[i+2]=acceleration;System.out.println("FD_NG_PEAK tick="+t+" axis="+i+" acceleration="+acceleration+" rate="+rates[i]+" previous="+previous[i]+" mode="+v.yaw.mode+" lock="+v.flying.lock+" phiRef="+v.yaw.postureReference);}}previous[i]=rates[i];}
   maximumGap[0]=Math.max(maximumGap[0],hinge(h,v));
   if(t%20==0)System.out.println("FD_NG_STEP tick="+t+" psi="+v.yaw.angle+" phi="+(-c.angle)+" omega="+v.yaw.velocity+" phiDot="+(-c.omega)+" ref="+v.yaw.reference+" phiRef="+v.yaw.postureReference+" lock="+v.flying.lock+" gap="+hinge(h,v)+" fault="+v.fault);
  });}
  h.runAtTickTime(400,()->{var v=car[0];h.assertTrue(v.fault.isEmpty(),"No compound fault: "+v.fault);h.assertTrue(Math.abs(v.yaw.angle-Math.PI)<Math.toRadians(2),"Actual yaw reaches π");h.assertTrue(v.flying.lock==FlyingState.Lock.PRONE,"Actual prone endpoint lock");
   var loaded=FoundationData.load(data.save(new CompoundTag(),l.registryAccess()),l.registryAccess()).vehicles.get(v.chassis);h.assertTrue(loaded.yaw.deck.equals(v.yaw.deck)&&Math.abs(loaded.yaw.angle-v.yaw.angle)<1e-6&&loaded.flying.retained(),"Hierarchy and actual state roundtrip");
   NewGenerationFlyingRuntime.fault(l,v,"TEST_MARKER_LOSS");
  });
  h.runAtTickTime(440,()->{var v=car[0];h.assertTrue(Math.abs(v.yaw.velocity)<.05&&v.flying.retained(),"Fault brakes without releasing");NewGenerationFlyingRuntime.retry(l,v);NewGenerationFlyingRuntime.unwind(l,v);});
  h.runAtTickTime(1001,()->{var v=car[0];h.assertTrue(v.fault.isEmpty(),"Recovery no fault: "+v.fault);h.assertTrue(Boarding.ready(l,v),"Return actual winding zero and seated capture");
   h.assertTrue(peaks[0]<=.6+.01&&peaks[1]<=.25+.005,"Actual speed limits: "+Arrays.toString(peaks));h.assertTrue(peaks[2]<=.8+.02&&peaks[3]<=.35+.02,"Actual acceleration limits: "+Arrays.toString(peaks));h.assertTrue(maximumGap[0]<.01,"Both physical hinges stay coincident: "+maximumGap[0]);
   System.out.println("FD_NG_COMPOUND PASS massEmpty="+baseline[0]+" full="+(baseline[0]+1.6)+" peaks="+Arrays.toString(peaks)+" hinge="+maximumGap[0]);h.succeed();});
 }
 private static ServerSubLevel body(GameTestHelper h,UUID id){return (ServerSubLevel)SubLevelContainer.getContainer(h.getLevel()).getSubLevel(id);}
 private static void mount(GameTestHelper h,FoundationData.Vehicle v,int index){var pig=EntityType.PIG.create(h.getLevel());pig.setNoAi(true);h.getLevel().addFreshEntity(pig);var child=body(h,v.carriers.getFirst().body);SeatBlock.sitDown(h.getLevel(),child.getPlot().getCenterBlock().offset(v.carriers.getFirst().seats.get(index)),pig);}
 private static double hinge(GameTestHelper h,FoundationData.Vehicle v){var parent=body(h,v.chassis);var deck=body(h,v.yaw.deck);var inner=body(h,v.carriers.getFirst().body);var p=parent.getPlot().getCenterBlock();var d=deck.getPlot().getCenterBlock();var c=inner.getPlot().getCenterBlock();var a=parent.logicalPose().transformPosition(new Vector3d(p.getX()+.5,p.getY()+.5+NewGenerationFlyingVehicle.PIVOT,p.getZ()+.5));var b=deck.logicalPose().transformPosition(new Vector3d(d.getX()+.5,d.getY()+.5,d.getZ()+.5));var q=inner.logicalPose().transformPosition(new Vector3d(c.getX(),c.getY()+.5,c.getZ()+.5));return Math.max(a.distance(b),b.distance(q));}
}
