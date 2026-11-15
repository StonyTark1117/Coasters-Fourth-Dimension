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

@GameTestHolder("fourthdimension_fd1501")
@PrefixGameTestTemplate(false)
public final class SpinningGameTests {
  @GameTest(template="empty",timeoutTicks=1000)
  public static void nativeYLoadingCaptureAndPersistence(GameTestHelper h) {
    var level=h.getLevel();var from=new BlockPos(400,100,400);var to=from.offset(0,0,80);
    for(int x=24;x<=26;x++)for(int z=24;z<=31;z++)level.setChunkForced(x,z,true);
    for(var p:List.of(from,to))level.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var edge=WingExamples.connect(level,from,to,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
    var data=FoundationData.get(level);
    var rail=new FoundationData.Rail(edge.from(),edge.to(),Family.SPINNING,
        new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(TrackGeometry.table(level,edge).length(),0))),
        UUID.randomUUID(),SpinningVehicle.VARIANT,TrackGeometry.fingerprint(level,edge),0,true);
    data.rails.add(rail);TrackPlacement.captureOriginalCurve(rail,edge.bezier());
    var cars=new FoundationData.Vehicle[2];var empty=new double[2];var start=new double[2];
    h.runAtTickTime(10,()->{
      h.assertTrue(SpinningTrack.rejection(level,edge)==null,"S1 track rejected");
      for(int i=0;i<2;i++) {
        cars[i]=SpinningVehicle.create(level,WingExamples.sample(level,edge,20+i*25));
        var pos=from.offset(5,0,20+i*25);
        level.setBlockAndUpdate(pos,StationContent.STATION.get().defaultBlockState());
        ((BoardingStationEntity)level.getBlockEntity(pos)).vehicle=cars[i].chassis;cars[i].dock=pos;
      }
    });
    h.runAtTickTime(50,()->{
      var container=SubLevelContainer.getContainer(level);
      for(int i=0;i<2;i++) {
        var v=cars[i];var c=v.carriers.getFirst();var body=(ServerSubLevel)container.getSubLevel(c.body);
        h.assertTrue(Boarding.ready(level,v),"Initial S1 dock not latched: "+v.fault+" angle="+c.angle+" omega="+c.omega);
        empty[i]=body.getMassTracker().getMass();
        System.out.println("FD_S1_EMPTY car="+i+" mass="+empty[i]+" com="+body.getMassTracker().getCenterOfMass()+" inertia="+body.getMassTracker().getInertiaTensor());
        for(int seatIndex:i==0?new int[]{0,3}:new int[]{1,3}) {
          var seat=body.getPlot().getCenterBlock().offset(c.seats.get(seatIndex));
          var pig=EntityType.PIG.create(level);pig.setNoAi(true);level.addFreshEntity(pig);SeatBlock.sitDown(level,seat,pig);
        }
      }
    });
    h.runAtTickTime(65,()->{
      var container=SubLevelContainer.getContainer(level);var pipeline=container.physicsSystem().getPipeline();
      for(int i=0;i<2;i++) {
        var v=cars[i];var c=v.carriers.getFirst();var body=(ServerSubLevel)container.getSubLevel(c.body);
        h.assertTrue(Math.abs(body.getMassTracker().getMass()-empty[i]-1.6)<1e-5,"Rider loading not exactly 1.6");
        VehicleOperations.restraints(level,v,true);VehicleOperations.dispatch(level,v);start[i]=c.angle;
        pipeline.addLinearAndAngularVelocity((ServerSubLevel)container.getSubLevel(v.chassis),new Vector3d(0,0,6),new Vector3d());
        System.out.println("FD_S1_LOADED car="+i+" com="+body.getMassTracker().getCenterOfMass()+" inertiaY="+SpinningRuntime.jointInertia(body,c));
      }
    });
    h.runAtTickTime(85,()->{
      double balanced=cars[0].carriers.getFirst().angle-start[0];
      double asymmetric=cars[1].carriers.getFirst().angle-start[1];
      System.out.println("FD_S1_LOAD_RESPONSE balanced="+balanced+" asymmetric="+asymmetric);
      h.assertTrue(Math.abs(asymmetric)>Math.abs(balanced)+.0001,"Asymmetric native load did not change Y response");
      var container=SubLevelContainer.getContainer(level);var pipeline=container.physicsSystem().getPipeline();
      for(var v:cars) {
        var c=v.carriers.getFirst();var body=(ServerSubLevel)container.getSubLevel(c.body);
        pipeline.addLinearAndAngularVelocity(body,new Vector3d(),new Vector3d(0,.8,0));
      }
    });
    h.runAtTickTime(100,()->{
      for(var v:cars) {
        var c=v.carriers.getFirst();
        h.assertTrue(Math.abs(c.omega)>.1,"Native local Y joint not rotating");
        h.assertTrue(!Boarding.ready(level,v),"Free carrier allowed boarding");
        VehicleOperations.capture(level,v);
      }
    });
    h.runAtTickTime(150,()->{
      var c=cars[1].carriers.getFirst();
      var saved=data.save(new CompoundTag(),level.registryAccess());
      for (var raw : saved.getList("vehicles", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
        var tag=(CompoundTag) raw;
        if(tag.getUUID("chassis").equals(cars[1].chassis))
          tag.getList("carriers",net.minecraft.nbt.Tag.TAG_COMPOUND).getCompound(0).putDouble("angle",c.angle+4*Math.PI);
      }
      var loaded=FoundationData.load(saved,level.registryAccess());
      var copy=loaded.vehicles.get(cars[1].chassis).carriers.getFirst();
      h.assertTrue(Math.abs(copy.angle-c.angle-4*Math.PI)<1e-9 && Math.abs(copy.omega-c.omega)<1e-9
          && copy.seats.equals(SpinningVehicle.SEATS) && copy.childPivotX==-.5,"S1 state did not roundtrip");
      // Remove a designated dock during capture: physical pose remains, unload stays denied.
      level.removeBlock(cars[1].dock,false);
      h.assertTrue(!Boarding.ready(level,cars[1]),"Missing dock allowed unload");
      level.setBlockAndUpdate(cars[1].dock,StationContent.STATION.get().defaultBlockState());
      ((BoardingStationEntity)level.getBlockEntity(cars[1].dock)).vehicle=cars[1].chassis;

    });
    h.runAtTickTime(850,()->{
      for(var v:cars) {
        var c=v.carriers.getFirst();
        h.assertTrue(Boarding.ready(level,v),"S1 capture failed: "+v.fault+" angle="+c.angle+" target="+c.captureTarget+" omega="+c.omega);
        VehicleOperations.restraints(level,v,false);
      }
      System.out.println("FD_S1_NATIVE axis=Y nativeLoad=true asymmetric=true free=true boundedCapture=true dockLoss=true schema7=true");
      h.succeed();
    });
  }
}
