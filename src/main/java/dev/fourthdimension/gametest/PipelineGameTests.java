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
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;
import java.util.*;

@GameTestHolder("fourthdimension_fd1401")
@PrefixGameTestTemplate(false)
public final class PipelineGameTests {
 @GameTest(template="empty",timeoutTicks=320)
 public static void pipelineNativeContract(GameTestHelper h){
  var level=h.getLevel();var from=new BlockPos(-1000,100,400);var to=from.offset(0,0,80);
  for(int x=(from.getX()>>4)-1;x<=(from.getX()>>4)+1;x++)for(int z=24;z<=31;z++)level.setChunkForced(x,z,true);
  for(var p:List.of(from,to))level.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
  var edge=PipelineExamples.connect(level,from,to,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
  var data=FoundationData.get(level);var length=TrackGeometry.table(level,edge).length();
  var rail=new FoundationData.Rail(from,to,Family.PIPELINE,new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(length,0))),UUID.randomUUID(),PipelineVehicle.VARIANT,TrackGeometry.fingerprint(level,edge),0,true);
  h.assertTrue(PipelineTrack.rejection(level,edge)!=null,"Ordinary track accepted as overhead");
  data.rails.add(rail);TrackPlacement.captureOriginalCurve(rail,edge.bezier());
  var cars=new ArrayList<FoundationData.Vehicle>();var mass=new double[1];var pigIds=new ArrayList<UUID>();
  h.runAtTickTime(10,()->{
   for(int i=0;i<2;i++){
    var v=PipelineVehicle.create(level,PipelineExamples.sample(level,edge,20+8*i));cars.add(v);
    var dock=from.offset(4,-3,20+8*i);level.setBlockAndUpdate(dock,StationContent.STATION.get().defaultBlockState());
    ((BoardingStationEntity)level.getBlockEntity(dock)).vehicle=v.chassis;
    v.dock=dock;
   }
  });
  h.runAtTickTime(55,()->{
   var container=SubLevelContainer.getContainer(level);var v=cars.getFirst();var body=(ServerSubLevel)container.getSubLevel(v.chassis);
   for(var car:cars){System.out.println("FD_FIXED_SETTLED center="+dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve.cartCenterWorld(container.getSubLevel(car.chassis))+" dock="+car.dock+" fault="+car.fault);PipelineRuntime.rebindDock(level,car,car.dock);h.assertTrue(Boarding.ready(level,car),"Fixed dock unavailable "+car.fault);PipelineVehicle.validate(car);}
  });
  h.runAtTickTime(65,()->{
   var container=SubLevelContainer.getContainer(level);var v=cars.getFirst();var body=(ServerSubLevel)container.getSubLevel(v.chassis);
   for(var car:cars)h.assertTrue(car.boardingReady,"Waited dock readiness cache false: "+car.fault);
   mass[0]=body.getMassTracker().getMass();
   for(int i=0;i<1;i++)h.assertTrue(CoasterCartTrainLinkConstraint.linkCarts(level,(ServerSubLevel)container.getSubLevel(cars.get(i).chassis),(ServerSubLevel)container.getSubLevel(cars.get(i+1).chassis),8),"Eight-block coupling failed");
   var dock=v.dock;PipelineRuntime.rebindDock(level,v,dock);
   for(var seat:v.carriers.getFirst().seats.subList(0,1)){var pig=EntityType.PIG.create(level);pig.setNoAi(true);level.addFreshEntity(pig);pigIds.add(pig.getUUID());SeatBlock.sitDown(level,body.getPlot().getCenterBlock().offset(seat),pig);}
   System.out.println("FD_FIXED_EMPTY mass="+mass[0]+" com="+body.getMassTracker().getCenterOfMass()+" nativeBodies=2 fixedSeats=8");
  });
  h.runAtTickTime(72,()->{
   var v=cars.getFirst();var body=(ServerSubLevel)SubLevelContainer.getContainer(level).getSubLevel(v.chassis);
   h.assertTrue(Math.abs(body.getMassTracker().getMass()-mass[0]-.8)<1e-5,"One asymmetric passenger must add .8 once");
   for(var seat:v.carriers.getFirst().seats.subList(1,4)){var pig=EntityType.PIG.create(level);pig.setNoAi(true);level.addFreshEntity(pig);pigIds.add(pig.getUUID());SeatBlock.sitDown(level,body.getPlot().getCenterBlock().offset(seat),pig);}
  });
  h.runAtTickTime(80,()->{
   var container=SubLevelContainer.getContainer(level);var v=cars.getFirst();var body=(ServerSubLevel)container.getSubLevel(v.chassis);
   h.assertTrue(Math.abs(body.getMassTracker().getMass()-mass[0]-3.2)<1e-5,"Four riders must add3.2 once: "+body.getMassTracker().getMass());
   h.assertTrue(v.carriers.getFirst().occupants.size()==4,"Adjacent fractional seat overlap double-counted");
   VehicleOperations.restraints(level,v,true);VehicleOperations.dispatch(level,v);
   h.assertTrue(cars.stream().noneMatch(c->c.stopping),"Whole train did not dispatch");
   var fakeDock=v.fixedDockFrom;v.fixedDockFrom=fakeDock.offset(1,0,0);v.stopping=true;
   h.assertTrue(!Boarding.ready(level,v),"Stopped non-dock location allowed unload");v.fixedDockFrom=fakeDock;
   VehicleOperations.capture(level,v);
   System.out.println("FD_FIXED_LOAD exactDelta=3.2 occupants=4 fixedOwner=true dispatch4=true wrongDockDenied=true");
  });
  h.runAtTickTime(120,()->{
   var v=cars.getFirst();var snapshot=data.snapshot(new CompoundTag(),level.registryAccess());var copy=FoundationData.load(snapshot,level.registryAccess());
   var c=copy.vehicles.get(v.chassis);PipelineVehicle.validate(c);
   h.assertTrue(c.fixedConsist.size()==2&&c.fixedDockFrom.equals(v.fixedDockFrom),"Fixed dock/consist roundtrip failed");
   var body=(ServerSubLevel)SubLevelContainer.getContainer(level).getSubLevel(v.chassis);
   var pos=body.getPlot().getCenterBlock().offset(PipelineVehicle.FRAME.getFirst());
   level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
  });
  h.runAtTickTime(145,()->{
   h.assertTrue(cars.stream().allMatch(c->c.stopping&&c.restraints&&!c.fault.isEmpty()),"Missing frame failed to retain whole train");
   System.out.println("FD_FIXED_NATIVE PASS twoCars8=true exactSeats=true equalLoads=true dockGuard=true serialization=true frameLossTrainHold=true");h.succeed();
  });
 }
}
