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
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;
import java.util.*;
@GameTestHolder("fourthdimension_fd2201")
@PrefixGameTestTemplate(false)
public final class StandingGameTests {
 @GameTest(template="empty",timeoutTicks=420)
 public static void standingPhysicalContract(GameTestHelper h){
  var l=h.getLevel();var data=FoundationData.get(l);var cars=new ArrayList<FoundationData.Vehicle>();var edges=new ArrayList<dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge>();var origins=new ArrayList<BlockPos>();var held=new double[1];var mass=new double[1];
  for(int i=0;i<2;i++){var from=new BlockPos(-1800+i*100,110,400);origins.add(from);var to=from.offset(0,0,80);for(int x=(from.getX()>>4)-1;x<=(from.getX()>>4)+1;x++)for(int z=24;z<=31;z++)l.setChunkForced(x,z,true);for(var p:List.of(from,to))l.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());var e=PipelineExamples.connect(l,from,to,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));edges.add(e);var length=TrackGeometry.table(l,e).length();var r=new FoundationData.Rail(from,to,Family.STANDING,new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(length,0))),UUID.randomUUID(),i==0?StandingVehicle.STANDARD:StandingVehicle.SURF,TrackGeometry.fingerprint(l,e),0,true);data.rails.add(r);TrackPlacement.captureOriginalCurve(r,e.bezier());for(int side:new int[]{-1,1})for(var p:BlockPos.betweenClosed(from.offset(side*3,1,5),from.offset(side*6,1,75)))l.setBlockAndUpdate(p,Blocks.SMOOTH_STONE.defaultBlockState());}
  h.runAtTickTime(10,()->{for(int i=0;i<2;i++){var d=i==0?TrackDesign.STANDING_STANDARD:TrackDesign.STANDING_SURF;h.assertTrue(StandingTrack.rejection(l,edges.get(i),i==0?TrackDesign.STANDING_SURF:TrackDesign.STANDING_STANDARD)!=null,"Wrong standing variant accepted");var v=StandingVehicle.create(l,PipelineExamples.sample(l,edges.get(i),20),d);cars.add(v);var dock=origins.get(i).offset(-4,1,20);l.setBlockAndUpdate(dock,StationContent.STATION.get().defaultBlockState());((BoardingStationEntity)l.getBlockEntity(dock)).vehicle=v.chassis;v.dock=dock;}});
  h.runAtTickTime(65,()->{for(var v:cars){StandingRuntime.rebindDock(l,v,v.dock);StandingVehicle.validate(v);}});
  h.runAtTickTime(80,()->{for(var car:cars)h.assertTrue(Boarding.ready(l,car),"Standing bound dock unavailable: "+car.variant+" "+car.fault+" q="+car.standing.travel);
   var v=cars.getLast();var child=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.carriers.getFirst().body);mass[0]=child.getMassTracker().getMass();var pig=EntityType.PIG.create(l);pig.setNoAi(true);l.addFreshEntity(pig);SeatBlock.sitDown(l,child.getPlot().getCenterBlock().offset(StandingVehicle.SEATS.getFirst()),pig);h.assertTrue(pig.isPassenger(),"Fixture mount rejected after home recognition");
  });
  h.runAtTickTime(95,()->{var v=cars.getLast();var child=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.carriers.getFirst().body);h.assertTrue(Math.abs(child.getMassTracker().getMass()-mass[0]-.8)<1e-5,"Standing passenger mass not added exactly once: empty="+mass[0]+" now="+child.getMassTracker().getMass()+" occupants="+v.carriers.getFirst().occupants);for(var car:cars){VehicleOperations.restraints(l,car,true);VehicleOperations.dispatch(l,car);}System.out.println("FD2201_NATIVE start standardOwner="+cars.getFirst().carriers.getFirst().body+" surfParent="+v.chassis+" surfChild="+child.getUniqueId()+" massDelta="+(child.getMassTracker().getMass()-mass[0]));});
  h.runAtTickTime(145,()->{var v=cars.getLast();h.assertTrue(v.standing.travel>.05&&v.standing.travel<.44,"Native Surf translation did not move within stroke: "+v.standing.travel+" fault="+v.fault);h.assertTrue(cars.getFirst().standing.travel==0&&cars.getFirst().carriers.getFirst().body.equals(cars.getFirst().chassis),"Standard must remain rigid");held[0]=v.standing.travel;StandingRuntime.fault(l,v,"FD2201 disclosed interruption");System.out.println("FD2201_NATIVE measuredMoving q="+held[0]+" qdot="+v.standing.velocity);});
  h.runAtTickTime(175,()->{var v=cars.getLast();h.assertTrue(v.stopping&&v.restraints&&v.standing.interrupted&&!Boarding.ready(l,v),"Mid-stroke interruption failed to retain/deny unload");h.assertTrue(Math.abs(v.standing.travel-held[0])<.035,"Interrupted native latch moved: "+v.standing.travel);var saved=data.snapshot(new CompoundTag(),l.registryAccess());var copy=FoundationData.load(saved,l.registryAccess()).vehicles.get(v.chassis);StandingVehicle.validate(copy);h.assertTrue(copy.standing.interrupted&&copy.stopping&&copy.restraints&&Math.abs(copy.standing.heldTravel-v.standing.heldTravel)<1e-8,"Travel interruption serialization changed");StandingRuntime.resumeToDock(l,v);h.assertTrue(!v.stopping&&!v.standing.interrupted,"Explicit validated Resume failed");});
  h.runAtTickTime(200,()->{for(var v:cars)VehicleOperations.capture(l,v);});
  for(int tick:new int[]{205,220,240,260,280,299})h.runAtTickTime(tick,()->System.out.println("FD2201_CAPTURE tick="+tick+" "+StandingRuntime.captureDiagnostic(l,cars.getLast())));
  h.runAtTickTime(300,()->{for(var v:cars){h.assertTrue(Boarding.ready(l,v),"Standing return home capture failed q="+v.standing.travel+" fault="+v.fault);VehicleOperations.restraints(l,v,false);}var v=cars.getLast();var parent=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.chassis);l.setBlockAndUpdate(parent.getPlot().getCenterBlock().offset(StandingVehicle.FRAME.getFirst()),Blocks.AIR.defaultBlockState());});
  h.runAtTickTime(325,()->{var v=cars.getLast();h.assertTrue(v.stopping&&v.restraints&&!v.fault.isBlank(),"Missing fixed guide support did not retain");System.out.println("FD2201_NATIVE PASS rigidStandard=true nativeSurfMoved=true physicalStroke=true exactRiderMass=true interruptedLatch=true saveLoadState=true explicitResume=true dockHome=true missingSupportHold=true; cold process and occupied standing view remain separate");h.succeed();});
 }
}
