package dev.fourthdimension.gametest;

import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.station.*;
import dev.fourthdimension.track.TrackGeometry;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;

/** The changed compound consist guard and canonical reverse path; no family sweep. */
@GameTestHolder("fourthdimension_fd1802_consist")
@PrefixGameTestTemplate(false)
public final class NewGenerationFlyingConsistGameTests {
 @GameTest(template="empty",timeoutTicks=1500)
 public static void fourCarCanonicalReverse(GameTestHelper h){
  var l=h.getLevel();var cars=new ArrayList<FoundationData.Vehicle>();
  NewGenerationFlyingRouteGameTests.Rig[] rig={null};double[] forward={0},reverse={0};int[] rpm={0};boolean[] crossed={false};
  h.runAtTickTime(1,()->{
   var r=NewGenerationFlyingRouteGameTests.create(h,0);rig[0]=r;cars.add(r.vehicle());r.vehicle().yaw.mode=YawState.Mode.STATION_CAPTURE;
   var edge=TrackGeometry.resolve(l,r.rail());
   for(int i=1;i<=4;i++){
    var v=NewGenerationFlyingVehicle.create(l,FlyingExamples.sample(l,edge,i==4?48:40-8*i));v.yaw.owner=r.owner();cars.add(v);
    var dock=r.rail().from.offset(4,-4,i==4?48:40-8*i);l.setBlockAndUpdate(dock,StationContent.STATION.get().defaultBlockState());((BoardingStationEntity)l.getBlockEntity(dock)).vehicle=v.chassis;v.dock=dock;v.flying.dockFrom=r.rail().from;v.flying.dockTo=r.rail().to;v.flying.dockGeometry=r.rail().geometryHash;
   }
   r.rail().yaw.intervals=List.of(interval("Transition",0,70,0,Math.PI/2,0,Math.PI/2),interval("Prone",70,100,Math.PI/2,Math.PI/2,Math.PI/2,Math.PI/2));
  });
  h.runAtTickTime(55,()->{
   for(var v:cars)h.assertTrue(Boarding.ready(l,v),"All compound cars initially ready: "+v.fault);
   for(int i=0;i<3;i++)h.assertTrue(CoasterCartTrainLinkConstraint.linkCarts(l,body(h,cars.get(i).chassis),body(h,cars.get(i+1).chassis),8),"Native eight-block compound coupling");
   h.assertTrue(!ConsistRules.canCouple(l,body(h,cars.getFirst().chassis),body(h,cars.getLast().chassis),8),"Fifth compound car rejected");
   var extra=cars.removeLast();var container=SubLevelContainer.getContainer(l);for(var id:List.of(extra.carriers.getFirst().body,extra.yaw.deck,extra.chassis))container.removeSubLevel(container.getSubLevel(id),SubLevelRemovalReason.REMOVED);FoundationData.get(l).vehicles.remove(extra.chassis);
   NewGenerationFlyingRuntime.rebindDock(l,cars.getFirst(),rig[0].vehicle().dock);
   NewGenerationFlyingRuntime.restraints(l,cars.getFirst(),true,true);NewGenerationFlyingRuntime.restraints(l,cars.getFirst(),false,true);
   NewGenerationFlyingRuntime.dispatch(l,cars.getFirst());
   rpm[0]=((CreativeMotorBlockEntity)l.getBlockEntity(rig[0].rail().from.east())).generatedSpeed.getValue();
  });
  for(int tick=56;tick<1100;tick++){final int t=tick;h.runAtTickTime(t,()->{
   for(var v:cars){h.assertTrue(v.fault.isEmpty(),"Compound consist fault: "+v.fault);if(v.yaw.lastDistance>70.1){crossed[0]=true;h.assertTrue(v.flying.lock==FlyingState.Lock.PRONE,"Endpoint captured before crossing: "+v.yaw.lastDistance+" / "+v.flying.lock);}}
   if(t%50==0)System.out.println("FD_NG_CONSIST tick="+t+" distances="+cars.stream().map(v->v.yaw.lastDistance).toList()+" stopped="+cars.stream().map(v->v.stopping).toList()+" phi="+cars.stream().map(v->-v.carriers.getFirst().angle).toList()+" directions="+cars.stream().map(v->v.yaw.direction).toList());
  });}
  h.runAtTickTime(455,()->{forward[0]=cars.getFirst().yaw.lastDistance;h.assertTrue(crossed[0]&&forward[0]>70,"Four-car travel passed transition exit: "+forward[0]);NewGenerationFlyingRuntime.capture(l,cars.getFirst());});
  h.runAtTickTime(485,()->{((CreativeMotorBlockEntity)l.getBlockEntity(rig[0].rail().from.east())).generatedSpeed.setValue(-rpm[0]);});
  h.runAtTickTime(505,()->NewGenerationFlyingRuntime.dispatch(l,cars.getFirst()));
  h.runAtTickTime(855,()->{
   var v=cars.getFirst();reverse[0]=v.yaw.lastDistance;h.assertTrue(reverse[0]<forward[0]-20,"Actual reversed native travel: "+forward[0]+" -> "+reverse[0]);
   for(var car:cars){h.assertTrue(car.yaw.direction==-1,"Canonical direction reverses");var i=rig[0].rail().yaw.at(car.yaw.lastDistance);h.assertTrue(Math.abs(car.yaw.lastTarget-i.profile().angleAt(car.yaw.lastDistance-i.start()))<.001,"Same canonical yaw profile retraced");h.assertTrue(Math.abs(car.carriers.getFirst().angle+i.posture(car.yaw.lastDistance))<.06,"Actual posture retraced");}
   NewGenerationFlyingRuntime.capture(l,v);
  });
  h.runAtTickTime(890,()->NewGenerationFlyingRuntime.unwind(l,cars.getFirst()));
  h.runAtTickTime(1250,()->{for(var v:cars)h.assertTrue(Boarding.ready(l,v),"All cars settle seated/zero at dock: "+v.fault);System.out.println("FD_NG_CONSIST PASS nativeCars=4 spacing=8 fifthRejected=true endpointBeforeExit=true canonicalReverse=true forward="+forward[0]+" reverse="+reverse[0]);h.succeed();});
 }
 private static YawSection.Interval interval(String name,double start,double end,double a,double b,double yawA,double yawB){return new YawSection.Interval(UUID.randomUUID(),name,start,end,YawState.Mode.PROFILE,0,a,b,new RotationProfile(List.of(new RotationProfile.Key(0,yawA),new RotationProfile.Key(end-start,yawB))),null,null,"",YawState.Winding.NEAREST,.6,.8,"",0);}
 private static ServerSubLevel body(GameTestHelper h,UUID id){return (ServerSubLevel)SubLevelContainer.getContainer(h.getLevel()).getSubLevel(id);}
}
