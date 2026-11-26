package dev.fourthdimension.gametest;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.moving.*;
import dev.fourthdimension.runtime.FoundationData;
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
import org.joml.Vector3d;

/** Real production coordinator with disclosed Creative world construction and fake operator setup. */
@GameTestHolder("fourthdimension_fd2001_production")
@PrefixGameTestTemplate(false)
public final class MovingNativeProductionGameTests {
  @GameTest(template="empty",timeoutTicks=800)
  public static void wingProductionCaptureTranslateAndRelease(GameTestHelper h){
    var level=h.getLevel();var origin=new BlockPos(-420,130,-420);var controller=origin.west(14);
    for(int x=-29;x<=-24;x++)for(int z=-29;z<=-24;z++)level.setChunkForced(x,z,true);
    var player=FakePlayerFactory.get(level,new GameProfile(UUID.fromString("fdfdfdfd-0000-0000-0000-000000002001"),"FDMoving"));
    player.setGameMode(GameType.CREATIVE);player.setPos(controller.getX()+.5,controller.getY(),controller.getZ()+2.5);
    level.setBlockAndUpdate(controller,MovingContent.CONTROLLERS.get(MovingMechanism.TRANSFER).get().defaultBlockState()
        .setValue(BlockStateProperties.FACING,Direction.EAST));
    level.setBlockAndUpdate(controller.west(),AllBlocks.CREATIVE_MOTOR.getDefaultState()
        .setValue(BlockStateProperties.FACING,Direction.EAST));
    ((CreativeMotorBlockEntity)level.getBlockEntity(controller.west())).generatedSpeed.setValue(64);
    for(int x:new int[]{0,4})for(int z:new int[]{-8,8})level.setBlockAndUpdate(origin.offset(x,0,z),
        SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var request=new MovingConstruction.ConfigureRequest("wing",16,origin,Direction.SOUTH,
        new MovingProgramSpec(MovingMechanism.TRANSFER,4,0,0,false));
    var state=new MovingAssemblyState[1];var car=new FoundationData.Vehicle[1];var start=new Vector3d[1];
    boolean[] released={false};int[] releaseTick={0};
    h.runAtTickTime(5,()->{
      state[0]=MovingConstruction.build(player,controller,request);
      for(var dock:state[0].docks.values()){
        var from=dock.from().offset(0,0,-24);var to=dock.to().offset(0,0,24);
        level.setBlockAndUpdate(from,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
        level.setBlockAndUpdate(to,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
        MotorbikeExamples.connect(level,from,dock.from(),new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
        MotorbikeExamples.connect(level,dock.to(),to,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
      }
    });
    h.runAtTickTime(50,()->{
      var s=state[0];h.assertTrue(s.locksEngaged,"Production source failed to lock: "+s.phase+" "+s.fault);
      var edge=MovingNativeAdapter.liveDockEdge(level,s);h.assertTrue(edge!=null,"No authenticated locked source bridge");
      car[0]=WingVehicle.create(level,MotorbikeExamples.sample(level,edge,8));
    });
    h.runAtTickTime(110,()->{
      var s=state[0];var v=car[0];
      h.assertTrue(Boarding.ready(level,v),"Normal Wing readiness failed: "+v.fault);
      VehicleOperations.restraints(level,v,true);
      start[0]=new Vector3d(SubLevelContainer.getContainer(level).getSubLevel(v.chassis).logicalPose().position());
      MovingRuntime.operate(player,controller,MovingRuntime.Action.CAPTURE,s.id,s.epoch,s.sequence,"end");
      h.assertTrue(s.retained()&&s.bodies.containsKey(v.chassis)&&FoundationData.get(level).vehicles.get(v.chassis)==v,
          "Production capture did not retain original registered Wing");
      MovingRuntime.operate(player,controller,MovingRuntime.Action.MOVE,s.id,s.epoch,s.sequence,"end");
      System.out.println("FD2001_PRODUCTION_STARTED id="+s.id+" chassis="+v.chassis+" duration="+s.program.build().durationSeconds());
    });
    for(int tick=120;tick<=700;tick+=10){final int time=tick;h.runAtTickTime(tick,()->{
      var s=state[0];var v=car[0];
      h.assertTrue(s.phase!=MovingAssemblyState.Phase.FAULT_HOLD,"Production coordinator fault at "+time+": "+s.fault);
      if(time%40==0)System.out.println("FD2001_PRODUCTION_STATE tick="+time+" phase="+s.phase+" pose="+s.actualPose+" velocity="+s.linearVelocity);
      if(!released[0]&&s.phase==MovingAssemblyState.Phase.RECONNECTED){
        h.assertTrue(s.locksEngaged&&MovingNativeAdapter.liveDockEdge(level,s)!=null,"Destination is not physically authenticated");
        var chassis=SubLevelContainer.getContainer(level).getSubLevel(v.chassis);
        h.assertTrue(chassis.logicalPose().position().x>start[0].x+3.8,"Registered Wing did not physically follow translation");
        MovingRuntime.operate(player,controller,MovingRuntime.Action.RELEASE,s.id,s.epoch,s.sequence,"end");
        h.assertTrue(!s.retained()&&v.boundFrom.equals(s.docks.get("end").from()),"Release did not bind actual receiving support");
        released[0]=true;releaseTick[0]=time;
      }
      if(released[0]&&time>=releaseTick[0]+30){
        h.assertTrue(v.restraints&&v.fault.isEmpty()&&FoundationData.get(level).vehicles.get(v.chassis)==v,
            "Production release changed rider restraint/vehicle identity or faulted: "+v.fault);
        var parent=(dev.ryanhcode.sable.sublevel.ServerSubLevel)SubLevelContainer.getContainer(level).getSubLevel(v.chassis);
        h.assertTrue(!dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrackSnap.isBearingDisengaged(parent),
            "Production release did not restore the native track guide");
        h.assertTrue(s.locksEngaged&&MovingNativeAdapter.liveDockEdge(level,s)!=null,"Released train lost authenticated support");
        System.out.println("FD2001_PRODUCTION_PASS id="+s.id+" chassis="+v.chassis+" bodyRetained="+s.retained()+" phase="+s.phase);
        h.succeed();
      }
    });}
  }
}
