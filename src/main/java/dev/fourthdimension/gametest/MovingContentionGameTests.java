package dev.fourthdimension.gametest;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.moving.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;

/** Two physical production tables and trains share one real fixed receiving span. */
@GameTestHolder("fourthdimension_fd2007_contention")
@PrefixGameTestTemplate(false)
public final class MovingContentionGameTests {
  @GameTest(template="empty",timeoutTicks=850)
  public static void independentTablesCannotClaimSharedExit(GameTestHelper h){
    var level=h.getLevel();var origins=new BlockPos[]{new BlockPos(-640,130,-640),new BlockPos(-640,130,-600)};
    for(int x=-42;x<=-38;x++)for(int z=-43;z<=-35;z++)level.setChunkForced(x,z,true);
    var players=new net.minecraft.server.level.ServerPlayer[2];var states=new MovingAssemblyState[2];
    var cars=new FoundationData.Vehicle[2];var world=FoundationData.get(level).moving;
    var snapshot=new CarrierRuntime.PerformanceSnapshot[1];var moved=new boolean[1];var completed=new boolean[1];
    for(int i=0;i<2;i++){
      var origin=origins[i];var controller=origin.west(14);
      players[i]=FakePlayerFactory.get(level,new GameProfile(new UUID(2007,i+1),"FDTable"+i));
      players[i].setGameMode(GameType.CREATIVE);players[i].setPos(controller.getX()+.5,130,controller.getZ()+2.5);
      level.setBlockAndUpdate(controller,MovingContent.CONTROLLERS.get(MovingMechanism.TRANSFER).get().defaultBlockState().setValue(BlockStateProperties.FACING,Direction.EAST));
      level.setBlockAndUpdate(controller.west(),AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.EAST));
      ((CreativeMotorBlockEntity)level.getBlockEntity(controller.west())).generatedSpeed.setValue(64);
      for(int x:new int[]{0,8})for(int z:new int[]{-8,8})level.setBlockAndUpdate(origin.offset(x,0,z),SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    }
    h.runAtTickTime(5,()->{
      for(int i=0;i<2;i++)states[i]=MovingConstruction.build(players[i],origins[i].west(14),new MovingConstruction.ConfigureRequest("wing",16,origins[i],Direction.SOUTH,new MovingProgramSpec(MovingMechanism.TRANSFER,8,0,0,false)));
      for(String name:List.of("start","end")){
        var a=states[0].docks.get(name);var b=states[1].docks.get(name);
        h.assertTrue(a!=null&&b!=null,"Expected source and destination docks");
        var before=a.from().offset(0,0,-24);var after=b.to().offset(0,0,24);
        level.setBlockAndUpdate(before,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
        level.setBlockAndUpdate(after,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
        connect(level,before,a.from());connect(level,a.to(),b.from());connect(level,b.to(),after);
      }
    });
    h.runAtTickTime(50,()->{
      for(int i=0;i<2;i++){
        h.assertTrue(states[i].locksEngaged,"Table source failed to lock: "+states[i].fault);
        cars[i]=WingVehicle.create(level,MotorbikeExamples.sample(level,MovingNativeAdapter.liveDockEdge(level,states[i]),8));
      }
    });
    h.runAtTickTime(110,()->{
      for(int i=0;i<2;i++){
        h.assertTrue(Boarding.ready(level,cars[i]),"Wing not normally ready: "+cars[i].fault);
        VehicleOperations.restraints(level,cars[i],true);
      }
      var a=states[0];var b=states[1];
      operate(players[0],a,MovingRuntime.Action.CAPTURE);
      var claims=world.reservedBy(a.id);var pose=SubLevelContainer.getContainer(level).getSubLevel(cars[1].chassis).logicalPose().position();
      var before=new org.joml.Vector3d(pose);String denial="";
      try{operate(players[1],b,MovingRuntime.Action.CAPTURE);}catch(IllegalArgumentException expected){denial=expected.getMessage();}
      h.assertTrue(denial.equals("Exit is reserved by another moving assembly"),"Expected actual shared reservation denial, got: "+denial);
      h.assertTrue(claims.equals(world.reservedBy(a.id))&&world.reservedBy(b.id).isEmpty(),"Denial changed ownership or left partial claims");
      h.assertTrue(!b.retained()&&b.cars.isEmpty()&&b.locksEngaged&&MovingNativeAdapter.liveDockEdge(level,b)!=null,"Denied table lost ordinary support");
      h.assertTrue(FoundationData.get(level).vehicles.get(cars[1].chassis)==cars[1]&&cars[1].restraints&&before.distance(pose)<1e-9,"Denial changed train identity, restraints or pose");
      var shared=MovingReservations.key(a.docks.get("end").to(),b.docks.get("end").from());
      h.assertTrue(a.id.equals(world.receivingOwner(shared)),"Shared physical receiving edge was not claimed");
      System.out.println("FD2007_CONTENTION_DENIAL first="+a.id+" second="+b.id+" shared="+shared+" claims="+claims+" reason="+denial);
      snapshot[0]=CarrierRuntime.performanceSnapshot();operate(players[0],a,MovingRuntime.Action.MOVE);moved[0]=true;
    });
    for(int tick=120;tick<=750;tick+=10){final int time=tick;h.runAtTickTime(tick,()->{
      if(!moved[0]||completed[0])return;var a=states[0];var b=states[1];
      h.assertTrue(a.phase!=MovingAssemblyState.Phase.FAULT_HOLD,"Moving winner faulted: "+a.fault);
      h.assertTrue(b.locksEngaged&&MovingNativeAdapter.liveDockEdge(level,b)!=null,"Losing table lost source support");
      if(a.phase==MovingAssemblyState.Phase.RECONNECTED){
        var end=CarrierRuntime.performanceSnapshot();long steps=end.steps()-snapshot[0].steps(),nanos=end.nanos()-snapshot[0].nanos();
        h.assertTrue(steps>0&&nanos>0,"Missing actual runtime workload sample");
        h.assertTrue(world.reservedBy(b.id).isEmpty(),"Losing table acquired a claim during winner motion");
        System.out.println("FD2007_CONTENTION_PASS tick="+time+" tables=2 trains=2 moving=1 retainedBodies="+a.bodies.size()+" callbacks="+steps+" addonNanos="+nanos+" averageAddonCallbackNanos="+(nanos/steps)+" source="+a.origin+" destination="+a.actualPose);
        completed[0]=true;h.succeed();
      }
    });}
  }
  private static void operate(net.minecraft.server.level.ServerPlayer p,MovingAssemblyState s,MovingRuntime.Action a){MovingRuntime.operate(p,s.controller,a,s.id,s.epoch,s.sequence,"end");}
  private static void connect(net.minecraft.server.level.ServerLevel l,BlockPos a,BlockPos b){MotorbikeExamples.connect(l,a,b,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));}
}
