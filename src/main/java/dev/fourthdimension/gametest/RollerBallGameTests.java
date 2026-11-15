package dev.fourthdimension.gametest;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.contract.*;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.station.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

/** Native assertions; fixture animals and direct construction do not claim normal player input. */
@GameTestHolder("fourthdimension_fd1502")
@PrefixGameTestTemplate(false)
public final class RollerBallGameTests {
 @GameTest(template="empty",timeoutTicks=350)
 public static void fourSeatNativeHingeAndLoad(GameTestHelper h){
  var l=h.getLevel();var a=new BlockPos(-700,100,-700);var b=a.offset(0,0,40);
  for(int x=-45;x<=-43;x++)for(int z=-45;z<=-40;z++)l.setChunkForced(x,z,true);
  for(var p:List.of(a,b))l.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
  var edge=AxisExamples.connect(l,a,b,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
  var d=FoundationData.get(l);double length=TrackGeometry.table(l,edge).length();
  var r=new FoundationData.Rail(a,b,Family.ROLLER_BALL,new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(length,0))),UUID.randomUUID(),RollerBallVehicle.VARIANT,TrackGeometry.fingerprint(l,edge),0,true);
  r.rollerSection=RollerBallState.Section.DOCK;d.rails.add(r);TrackPlacement.captureOriginalCurve(r,edge.bezier());
  var v=new FoundationData.Vehicle[1];double[] mass={0};
  h.runAtTickTime(10,()->{v[0]=RollerBallVehicle.create(l,AxisExamples.sample(l,edge,12));var pos=a.offset(-6,1,12);l.setBlockAndUpdate(pos,StationContent.STATION.get().defaultBlockState());((BoardingStationEntity)l.getBlockEntity(pos)).vehicle=v[0].chassis;v[0].dock=pos;});
  h.runAtTickTime(40,()->RollerBallRuntime.rebindDock(l,v[0],v[0].dock));
  h.runAtTickTime(100,()->{
   var car=v[0];var c=car.carriers.getFirst();var child=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(c.body);mass[0]=child.getMassTracker().getMass();
   hinge(h,car);
   var origin=child.getPlot().getCenterBlock();
   for(var seat:RollerBallVehicle.SEATS){var state=child.getPlot().getEmbeddedLevelAccessor().getBlockState(seat);
     h.assertTrue(Math.abs(Math.abs(seat.getX()+RollerBallSeatBlock.hipX(state)-.5)-3.25)<1e-9,"Declared transverse hip offsets");}
   h.assertTrue(Math.abs(child.getMassTracker().getCenterOfMass().x()-origin.getX()-.5)<1e-6,"Empty transverse mass symmetry");
   h.assertTrue(Boarding.ready(l,car),"Initial Roller Ball dock: "+car.fault+" angle="+c.angle+" omega="+c.omega);RollerBallVehicle.validate(car);
   System.out.println("FD_ROLLER_EMPTY mass="+mass[0]+" inertia="+RollerBallRuntime.inertia(child,c)+" com="+child.getMassTracker().getCenterOfMass());
   var pig=EntityType.PIG.create(l);pig.setNoAi(true);l.addFreshEntity(pig);SeatBlock.sitDown(l,child.getPlot().getCenterBlock().offset(c.seats.getFirst()),pig);
  });
  h.runAtTickTime(150,()->{
   var car=v[0];var c=car.carriers.getFirst();var child=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(c.body);
   h.assertTrue(Math.abs(child.getMassTracker().getMass()-mass[0]-.8)<1e-6,"One declared rider must add .8");hinge(h,car);
   for(int i=1;i<4;i++){var pig=EntityType.PIG.create(l);pig.setNoAi(true);l.addFreshEntity(pig);SeatBlock.sitDown(l,child.getPlot().getCenterBlock().offset(c.seats.get(i)),pig);}
  });
  h.runAtTickTime(220,()->{
   var car=v[0];var c=car.carriers.getFirst();var child=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(c.body);
   h.assertTrue(Math.abs(child.getMassTracker().getMass()-mass[0]-3.2)<1e-6,"Four declared riders must add3.2");h.assertTrue(c.occupants.size()==4,"All four native seats share one carrier");hinge(h,car);
   var round=FoundationData.load(d.save(new CompoundTag(),l.registryAccess()),l.registryAccess());var copy=round.vehicles.get(car.chassis);
   h.assertTrue(copy.carriers.getFirst().body.equals(c.body)&&copy.carriers.getFirst().angle==c.angle&&copy.roller.phase==car.roller.phase,"Exact saved carrier/lift phase");
   var ordinary=TrackDesign.ROLLER_BALL.contract();h.assertTrue(!ordinary.mechanisms().contains(TrackContract.Mechanism.ROTATION_DRIVE),"Roller Ball is passive, not route spin-drive");
   System.out.println("FD_ROLLER_LOADED mass="+child.getMassTracker().getMass()+" inertia="+RollerBallRuntime.inertia(child,c)+" angle="+c.angle+" omega="+c.omega);h.succeed();
  });
 }
 private static void hinge(GameTestHelper h,FoundationData.Vehicle v){
  var container=SubLevelContainer.getContainer(h.getLevel());var c=v.carriers.getFirst();var p=(ServerSubLevel)container.getSubLevel(v.chassis);var b=(ServerSubLevel)container.getSubLevel(c.body);
  container.physicsSystem().updatePose(p);container.physicsSystem().updatePose(b);var po=p.getPlot().getCenterBlock();var bo=b.getPlot().getCenterBlock();
  var x=p.logicalPose().transformPosition(new Vector3d(po.getX()+.5,po.getY()+.5+c.pivotHeight,po.getZ()+.5));
  var y=b.logicalPose().transformPosition(new Vector3d(bo.getX()+.5,bo.getY()+1.5,bo.getZ()+.5));
  System.out.println("FD_ROLLER_HINGE gap="+x.distance(y)+" pivot="+x);h.assertTrue(x.distance(y)<.03,"Physical Roller Ball hinge "+x.distance(y));
 }
 @GameTest(template="empty",timeoutTicks=80)
 public static void smoothVerticalRouteGeometry(GameTestHelper h){
  var l=h.getLevel();var o=new BlockPos(-500,80,-500);for(int x=-32;x<=-30;x++)for(int z=-34;z<=-20;z++)l.setChunkForced(x,z,true);
  h.runAtTickTime(5,()->{var ex=RollerBallExamples.create(l,o,false);h.assertTrue(ex.edges().size()==22,"Finite closed route sections");
   for(var e:ex.edges()){h.assertTrue(RollerBallTrack.rejection(l,e)==null,"Declared curvature/plane");System.out.println("FD_ROLLER_ROUTE from="+e.from()+" to="+e.to()+" length="+TrackGeometry.table(l,e).length());}h.succeed();});
 }
}
