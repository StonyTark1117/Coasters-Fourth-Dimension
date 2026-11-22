package dev.fourthdimension.gametest;

import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.integration.RideAuthority;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

/** Disclosed native fixture; real Chain propulsion and real rider mass, no motion injection. */
@GameTestHolder("fourthdimension_fd2204")
@PrefixGameTestTemplate(false)
public final class FlyingBikeGameTests {
 @GameTest(template="empty",timeoutTicks=2000)
 public static void twoNativeLaunchesPowerLossRecoveryAndDock(GameTestHelper h){
  var l=h.getLevel();var origin=new BlockPos(500,110,-600);
  for(int x=30;x<=38;x++)for(int z=-39;z<=-29;z++)l.setChunkForced(x,z,true);
  var rig=new FlyingBikeExamples.Example[1];var first=new FlyingBikeGovernorEntity[1];var second=new FlyingBikeGovernorEntity[1];
  var source=new CreativeMotorBlockEntity[1];double[] speed={0,0},mass={0},previous={0},acceleration={0};boolean[] observed={false,false};
  h.runAtTickTime(5,()->{var fresh=new FoundationData.Vehicle(java.util.UUID.randomUUID(),dev.fourthdimension.contract.Family.FLYING_BIKE);
   h.assertTrue(fresh.authority.owner==null&&!RideAuthority.deniesProvider(l,fresh,RideAuthority.NATIVE_CHAIN),"Unowned native Chain denied");
   for(var provider:java.util.List.of(RideAuthority.ENGINEERED,RideAuthority.MAGNETIZED,RideAuthority.CTNB,"external_probe"))h.assertTrue(RideAuthority.deniesProvider(l,fresh,provider),"Unowned specialty accepted "+provider);
   rig[0]=FlyingBikeExamples.create(l,origin,true);first[0]=(FlyingBikeGovernorEntity)l.getBlockEntity(rig[0].firstGovernor());second[0]=(FlyingBikeGovernorEntity)l.getBlockEntity(rig[0].secondGovernor());source[0]=(CreativeMotorBlockEntity)l.getBlockEntity(rig[0].firstGovernor().east());});
  h.runAtTickTime(35,()->{var v=rig[0].vehicle();first[0].bind(v);first[0].link(second[0]);h.assertTrue(first[0].availability()==null,"Two supplied native launches unavailable");h.assertTrue(first[0].getGeneratedSpeed()==0&&second[0].getGeneratedSpeed()==0,"Launch governors must not generate energy");});
  h.runAtTickTime(65,()->{var v=rig[0].vehicle();h.assertTrue(Boarding.ready(l,v),"Prone dock must settle: "+v.fault);boolean denied=false;try{VehicleOperations.dispatch(l,v);}catch(IllegalArgumentException expected){denied=true;}h.assertTrue(denied&&v.stopping&&RideAuthority.holds(l,v),"Open restraints dispatched or released authority");var b=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.chassis);mass[0]=b.getMassTracker().getMass();var pig=EntityType.PIG.create(l);pig.setNoAi(true);l.addFreshEntity(pig);SeatBlock.sitDown(l,b.getPlot().getCenterBlock().offset(FlyingBikeVehicle.SEATS.getFirst()),pig);});
  h.runAtTickTime(70,()->{var v=rig[0].vehicle();var missing=origin.offset(4,-3,38);var floor=l.getBlockState(missing);l.removeBlock(missing,false);h.assertTrue(!Boarding.ready(l,v),"Missing prone exit platform accepted");boolean denied=false;try{FlyingBikeRuntime.rebindDock(l,v,rig[0].station());}catch(IllegalArgumentException expected){denied=true;}h.assertTrue(denied,"Missing exit platform accepted by dock binding");l.setBlockAndUpdate(missing,floor);h.assertTrue(Boarding.ready(l,v),"Restored prone platform failed");});
  h.runAtTickTime(80,()->{var v=rig[0].vehicle();var b=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.chassis);h.assertTrue(Math.abs(b.getMassTracker().getMass()-mass[0]-.8)<1e-5,"Exactly one native rider mass missing");VehicleOperations.restraints(l,v,true);VehicleOperations.dispatch(l,v);});
  h.runAtTickTime(150,()->{var v=rig[0].vehicle();h.assertTrue(speed[0]>5,"First launch did not physically accelerate: "+speed[0]+" "+v.fault);source[0].generatedSpeed.setValue(0);});
  h.runAtTickTime(200,()->{var v=rig[0].vehicle();h.assertTrue(v.stopping&&v.restraints&&RideAuthority.holds(l,v)&&!v.fault.isEmpty(),"Power loss failed retained hold");h.assertTrue(first[0].outputRpm()==0&&second[0].outputRpm()==0,"Power loss left either transmission active");var saved=FoundationData.get(l).save(new CompoundTag(),l.registryAccess());var restored=FoundationData.load(saved,l.registryAccess()).vehicles.get(v.chassis);h.assertTrue(restored.flyingBike.resumeRequired&&restored.flyingBike.governor.equals(first[0].getBlockPos())&&restored.carriers.getFirst().body.equals(v.chassis),"Saved interrupted rigid identity/intent changed");source[0].generatedSpeed.setValue(192);});
  h.runAtTickTime(240,()->{var v=rig[0].vehicle();h.assertTrue(v.stopping&&RideAuthority.holds(l,v)&&first[0].outputRpm()==0,"Power restoration auto-launched");VehicleOperations.capture(l,v);});
  h.runAtTickTime(255,()->FlyingBikeRuntime.resumeToDock(l,rig[0].vehicle()));
  for(int t=81;t<1950;t++){final int tick=t;h.runAtTickTime(t,()->{
   var v=rig[0].vehicle();var c=SubLevelContainer.getContainer(l);var b=(ServerSubLevel)c.getSubLevel(v.chassis);double actual=c.physicsSystem().getPipeline().getLinearVelocity(b,new Vector3d()).length();
   if(!v.stopping&&tick>82&&tick!=256){double a=Math.abs(actual-previous[0])*20;acceleration[0]=Math.max(acceleration[0],a);h.assertTrue(a<=3.25,"Native launch acceleration exceeded bound at tick "+tick+": "+a+" phase="+v.flyingBike.phase+" previous="+previous[0]+" actual="+actual);}
   previous[0]=actual;
   if(v.flyingBike.phase.equals("LAUNCH_1")){speed[0]=Math.max(speed[0],actual);observed[0]|=actual>5;}
   if(v.flyingBike.phase.equals("LAUNCH_2")){speed[1]=Math.max(speed[1],actual);observed[1]|=actual>9;}
   if(tick%50==0)System.out.println("FD2204_NATIVE tick="+tick+" phase="+v.flyingBike.phase+" speed="+actual+" first="+first[0].status()+" second="+second[0].status()+" fault="+v.fault+" center="+dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve.cartCenterWorld(b));
  });}
  h.succeedWhen(()->{h.assertTrue(h.getTick()>300,"Wait for interruption/recovery");var v=rig[0].vehicle();h.assertTrue(observed[0]&&observed[1],"Both actual native launch speeds not yet observed");h.assertTrue(v.flyingBike.launch==2&&v.flyingBike.phase.equals("DOCK")&&Boarding.ready(l,v),"Full physical route has not returned to dock: "+v.flyingBike.phase+" "+v.fault);h.assertTrue(first[0].outputRpm()==0&&second[0].outputRpm()==0,"Final dock left drive active");VehicleOperations.restraints(l,v,false);System.out.println("FD2204_NATIVE PASS firstMax="+speed[0]+" secondMax="+speed[1]+" maxPhysicalLaunchAcceleration="+acceleration[0]+" realNativeRiderMass=true powerLossHold=true restoredPowerNoStart=true explicitResume=true serializedIntent=true fullCircuit=true finalDockOpen=true; not occupied player camera or cold-process proof");});
 }
 @GameTest(template="empty",timeoutTicks=150)
 public static void invertedNativeEvacuationExitsAndIntent(GameTestHelper h){
  var l=h.getLevel();var origin=new BlockPos(-500,110,-600);var car=new FoundationData.Vehicle[1];var floors=new java.util.ArrayList<BlockPos>();
  for(int x=-33;x<=-30;x++)for(int z=-39;z<=-32;z++)l.setChunkForced(x,z,true);
  h.runAtTickTime(5,()->{
    var end=origin.south(64);
    for(var pos:java.util.List.of(origin,end)){l.setBlockAndUpdate(pos,dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());((dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity)l.getBlockEntity(pos)).setJointBankRadians((float)Math.PI);}
    var forward=new net.minecraft.world.phys.Vec3(0,0,1);
    var n=dev.silvergold.simulatedcoasters.track.CoasterTrackPlacement.anchorFaceNormalForAxis(l,origin,forward);
    var edge=FlyingBikeExamples.connect(l,origin,end,forward,forward.scale(-1),n,n);
    var profile=new dev.fourthdimension.physics.RotationProfile(java.util.List.of(new dev.fourthdimension.physics.RotationProfile.Key(0,0),new dev.fourthdimension.physics.RotationProfile.Key(64,0)));
    var rail=new FoundationData.Rail(origin,end,dev.fourthdimension.contract.Family.FLYING_BIKE,profile,java.util.UUID.randomUUID(),FlyingBikeVehicle.VARIANT,dev.fourthdimension.track.TrackGeometry.fingerprint(l,edge),0,true);
    dev.fourthdimension.track.TrackPlacement.captureOriginalCurve(rail,edge.bezier());FoundationData.get(l).rails.add(rail);
    car[0]=FlyingBikeVehicle.create(l,FlyingBikeExamples.sample(l,edge,32));car[0].dock=origin.west(5);car[0].flyingBike.dockFrom=origin;car[0].flyingBike.dockTo=end;car[0].flyingBike.dockGeometry=rail.geometryHash;car[0].restraints=true;
  });
  h.runAtTickTime(35,()->{
    var v=car[0];var b=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.chassis);h.assertTrue(new Vector3d(0,1,0).rotate(b.logicalPose().orientation()).y<-.999,"Native inverted support fixture did not invert body");
    var plot=b.getPlot().getCenterBlock();
    for(var seat:FlyingBikeVehicle.SEATS){var p=plot.offset(seat);var w=b.logicalPose().transformPosition(new Vector3d(p.getX()+.5,p.getY()+.7,p.getZ()-.35));var floor=BlockPos.containing(w.x-Math.signum(seat.getX())*3,w.y,w.z).below();floors.add(floor);l.setBlockAndUpdate(floor,net.minecraft.world.level.block.Blocks.SMOOTH_STONE.defaultBlockState());}
  });
  h.runAtTickTime(50,()->{
    var v=car[0];var b=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.chassis);var original=v.dock;var phase=v.flyingBike.phase;var station=origin.south(32).east(5).above(2);
    l.setBlockAndUpdate(station,dev.fourthdimension.station.StationContent.STATION.get().defaultBlockState());
    FlyingBikeRuntime.rebindDock(l,v,station);((dev.fourthdimension.station.BoardingStationEntity)l.getBlockEntity(station)).vehicle=v.chassis;
    h.assertTrue(FlyingBikeRuntime.evacuation(v)&&v.dock.equals(original)&&v.flyingBike.phase.equals(phase),"Evacuation changed original dock or phase");
    h.assertTrue(SubLevelContainer.getContainer(l).physicsSystem().getPipeline().getAngularVelocity(b,new Vector3d()).length()<=.01&&v.flyingBike.evacuationStoppedTicks>=5,"Evacuation accepted angular motion before five settled ticks");
    h.assertTrue(Boarding.ready(l,v),"Inverted native evacuation exits unavailable: "+v.fault);
    VehicleOperations.restraints(l,v,false);
    for(var seat:FlyingBikeVehicle.SEATS){var p=b.getPlot().getCenterBlock().offset(seat);h.assertTrue(!Boarding.canBoard(l,p)&&FlyingBikeRuntime.exit(l,v,p)!=null,"Evacuation allowed new boarding or lost bounded exit");}
    boolean dispatch=false,resume=false;try{VehicleOperations.dispatch(l,v);}catch(IllegalArgumentException expected){dispatch=true;}try{FlyingBikeRuntime.resumeToDock(l,v);}catch(IllegalArgumentException expected){resume=true;}h.assertTrue(dispatch&&resume,"Evacuation allowed motion");
    var restored=FoundationData.load(FoundationData.get(l).save(new CompoundTag(),l.registryAccess()),l.registryAccess()).vehicles.get(v.chassis);
    h.assertTrue(FlyingBikeRuntime.evacuation(restored)&&restored.dock.equals(original)&&restored.flyingBike.resumeRequired,"Evacuation intent/dock not preserved");
    for(var floor:floors)l.removeBlock(floor,false);h.assertTrue(!Boarding.ready(l,v),"Missing evacuation platforms accepted");
    System.out.println("FD2204_EVAC PASS actualNativeRoll180=true boundedDryHorizontalExits=true originalDockAndPhasePreserved=true newBoardingDenied=true dispatchResumeDenied=true savedConservativeIntent=true missingPlatformDenied=true; no player unload claim");h.succeed();
  });
 }

}
