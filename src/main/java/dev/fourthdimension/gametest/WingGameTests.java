package dev.fourthdimension.gametest;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.cart.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

@GameTestHolder("fourthdimension_fd1101")
@PrefixGameTestTemplate(false)
public final class WingGameTests {
  @GameTest(template="empty", timeoutTicks=220)
  public static void fixedSeatsCouplingAndPersistence(GameTestHelper helper) {
    var level = helper.getLevel();
    var from = new BlockPos(400,100,400);
    var to = from.offset(0,0,80);
    for (int x=24;x<=26;x++) for (int z=24;z<=31;z++) level.setChunkForced(x,z,true);
    for (var p : java.util.List.of(from,to))
      level.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var edge = WingExamples.connect(level,from,to,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
    var cars = new FoundationData.Vehicle[2];
    var initial = new Vector3d();
    helper.runAtTickTime(10,()-> {
      helper.assertTrue(WingTrack.rejection(level,edge)==null,"Ordinary native track rejected");
      cars[0]=WingVehicle.create(level,WingExamples.sample(level,edge,20));
      cars[1]=WingVehicle.create(level,WingExamples.sample(level,edge,26));
      for (var car:cars) {
        helper.assertTrue(car.carriers.size()==1 && car.carriers.getFirst().body.equals(car.chassis),"Fixed group allocated another body");
        helper.assertTrue(car.carriers.getFirst().seats.equals(WingVehicle.SEATS),"Fixed seats changed");
      }
    });
    helper.runAtTickTime(50,()-> {
      var container=SubLevelContainer.getContainer(level);
      var a=(ServerSubLevel)container.getSubLevel(cars[0].chassis);
      var b=(ServerSubLevel)container.getSubLevel(cars[1].chassis);
      helper.assertTrue(Boarding.ready(level,cars[0]) && Boarding.ready(level,cars[1]),"Stopped fixed seats not ready: "+cars[0].fault);
      helper.assertTrue(!ConsistRules.canCouple(level,a,b,5),"Wrong coupling distance accepted");
      helper.assertTrue(CoasterCartTrainLinkConstraint.linkCarts(level,a,b,6),"Native Wing coupling rejected");
      boolean denied=false;
      try {VehicleOperations.dispatch(level,cars[0]);} catch(IllegalArgumentException expected){denied=true;}
      helper.assertTrue(denied,"Open restraint dispatch allowed");
      VehicleOperations.restraints(level,cars[0],true);
      VehicleOperations.dispatch(level,cars[0]);
      initial.set(CoasterCartResolve.cartCenterWorld(a));
      container.physicsSystem().getPipeline().addLinearAndAngularVelocity(a,new Vector3d(0,0,2),new Vector3d());
      container.physicsSystem().getPipeline().addLinearAndAngularVelocity(b,new Vector3d(0,0,2),new Vector3d());
    });
    helper.runAtTickTime(90,()-> {
      var container=SubLevelContainer.getContainer(level);
      var a=container.getSubLevel(cars[0].chassis);
      helper.assertTrue(CoasterCartResolve.cartCenterWorld(a).distance(initial)>.2,"Fixed native chassis did not move");
      helper.assertTrue(cars[0].carriers.getFirst().angle==0 && cars[0].carriers.getFirst().omega==0,"Fixed seats acquired rotation");
      helper.assertTrue(cars[0].fault.isEmpty(),"Wing motion fault: "+cars[0].fault);
      VehicleOperations.capture(level,cars[0]);
    });
    helper.runAtTickTime(160,()-> {
      helper.assertTrue(Boarding.ready(level,cars[0]),"Wing stop did not settle");
      VehicleOperations.restraints(level,cars[0],false);
      var data=FoundationData.get(level);
      var saved=data.save(new CompoundTag(),level.registryAccess());
      var loaded=FoundationData.load(saved,level.registryAccess());
      var fixed=loaded.vehicles.get(cars[0].chassis);
      helper.assertTrue(fixed.family==Family.WING && fixed.carriers.getFirst().body.equals(fixed.chassis)
          && fixed.carriers.getFirst().seats.equals(WingVehicle.SEATS),"Fixed identity did not roundtrip");
      var probe=WingExamples.sample(level,edge,50);
      var obstacle=BlockPos.containing(probe.point().add(3.5,2,0));
      level.setBlockAndUpdate(obstacle,Blocks.STONE.defaultBlockState());
      helper.assertTrue(WingTrack.obstruction(level,probe)!=null,"Wide rider obstruction missed");
      level.setBlockAndUpdate(obstacle,Blocks.AIR.defaultBlockState());
      System.out.println("FD_WING_NATIVE fixedBody=true seats=4 coupled=2 motion=true stop=true schema6=true obstructionRejected=true");
      helper.succeed();
    });
  }
}
