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

/** Distinct physical paths, one restrained Wing per mechanism; no family cross-product. */
@GameTestHolder("fourthdimension_fd2002_mechanisms")
@PrefixGameTestTemplate(false)
public final class MovingMechanismNativeGameTests {
  @GameTest(template="empty",timeoutTicks=2200) public static void turntable(GameTestHelper h){run(h,0,new MovingProgramSpec(MovingMechanism.TURNTABLE,0,0,90,false));}
  @GameTest(template="empty",timeoutTicks=2200) public static void drop(GameTestHelper h){run(h,1,new MovingProgramSpec(MovingMechanism.DROP,4,0,0,false));}
  @GameTest(template="empty",timeoutTicks=2200) public static void tiltThirty(GameTestHelper h){run(h,2,new MovingProgramSpec(MovingMechanism.TILT,30,0,0,false));}
  @GameTest(template="empty",timeoutTicks=2200) public static void elevator(GameTestHelper h){run(h,3,new MovingProgramSpec(MovingMechanism.ELEVATOR,4,0,0,false));}
  @GameTest(template="empty",timeoutTicks=2200) public static void teeter(GameTestHelper h){run(h,4,new MovingProgramSpec(MovingMechanism.TEETER,1,0,0,false));}
  static void run(GameTestHelper h,int index,MovingProgramSpec spec){run(h,index,spec,null);}
  static void run(GameTestHelper h,int index,MovingProgramSpec spec,String measurement){
    var level=h.getLevel();var origin=new BlockPos(-700-index*128,180,-700);var controller=origin.west(24);
    for(int x=(origin.getX()>>4)-3;x<=(origin.getX()>>4)+3;x++)for(int z=(origin.getZ()>>4)-3;z<=(origin.getZ()>>4)+3;z++)level.setChunkForced(x,z,true);
    var player=FakePlayerFactory.get(level,new GameProfile(new UUID(0xFD20020000000000L,index+1),"FDMechanism"+index));
    player.setGameMode(GameType.CREATIVE);player.setPos(controller.getX()+.5,controller.getY(),controller.getZ()+2.5);
    level.setBlockAndUpdate(controller,MovingContent.CONTROLLERS.get(spec.mechanism()).get().defaultBlockState().setValue(BlockStateProperties.FACING,Direction.EAST));
    level.setBlockAndUpdate(controller.west(),AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.EAST));
    var motor=(CreativeMotorBlockEntity)level.getBlockEntity(controller.west());motor.generatedSpeed.setValue(64);
    for(var local:List.of(spec.build().start(),spec.build().end()))for(int z:new int[]{-8,8}){
      var point=local.transform(new MovingProgram.Point(0,-.25,z));
      var socket=BlockPos.containing(origin.getX()+.5+point.x(),origin.getY()+.5+point.y(),origin.getZ()+.5+point.z());
      level.setBlockAndUpdate(socket,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    }
    var request=new MovingConstruction.ConfigureRequest("wing",16,origin,Direction.SOUTH,spec);
    var state=new MovingAssemblyState[1];var car=new FoundationData.Vehicle[1];var telemetry=new MovingNativeMeasurements[1];
    int[] interrupt={-1};boolean[] resumed={false},finished={false};
    double[] maxima=new double[4];Vector3d[] previousVelocity={null},previousAngular={null};
    h.runAtTickTime(5,()->{
      var s=state[0]=MovingConstruction.build(player,controller,request);
      var joined=new HashSet<BlockPos>();
      for(var dock:s.docks.values())for(boolean last:new boolean[]{false,true}){
        var socket=last?dock.to():dock.from();if(!joined.add(socket))continue;
        var frame=MovingNativeAdapter.socketFrame(s,dock,last);
        var outward=frame.forward().scale(last?1:-1);var far=BlockPos.containing(frame.rawPosition().add(outward.scale(24)));
        level.setBlockAndUpdate(far,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
        if(last)MotorbikeExamples.connect(level,socket,far,outward,outward.scale(-1),frame.normal(),frame.normal());
        else MotorbikeExamples.connect(level,far,socket,outward.scale(-1),outward,frame.normal(),frame.normal());
      }
    });
    h.runAtTickTime(50,()->{
      var s=state[0];h.assertTrue(s.locksEngaged,"Source not locked: "+s.phase+" "+s.fault);
      var edge=MovingNativeAdapter.liveDockEdge(level,s);h.assertTrue(edge!=null,"Source bridge unavailable");
      car[0]=WingVehicle.create(level,MotorbikeExamples.sample(level,edge,8));
    });
    h.runAtTickTime(110,()->{
      var s=state[0];var v=car[0];h.assertTrue(Boarding.ready(level,v),"Wing setup not stopped: "+v.fault);
      VehicleOperations.restraints(level,v,true);
      MovingRuntime.operate(player,controller,MovingRuntime.Action.CAPTURE,s.id,s.epoch,s.sequence,s.destination);
      if(measurement!=null)telemetry[0]=new MovingNativeMeasurements(level,s,measurement);
      MovingRuntime.operate(player,controller,MovingRuntime.Action.MOVE,s.id,s.epoch,s.sequence,s.destination);
      System.out.println("FD2002_BEGIN mechanism="+spec.mechanism()+" id="+s.id+" duration="+spec.build().durationSeconds()+" limits="+spec.build().limits());
    });
    for(int tick=111;tick<=2100;tick++){final int time=tick;h.runAtTickTime(tick,()->{
      if(finished[0])return;var s=state[0];var v=car[0];MovingNativeAdapter.readback(level,s);
      if(time%100==0)System.out.println("FD2002_SAMPLE mechanism="+spec.mechanism()+" tick="+time+" phase="+s.phase+" actual="+s.actualPose+" v="+s.linearVelocity+" w="+s.angularVelocity);
      maxima[0]=Math.max(maxima[0],s.linearVelocity.length());maxima[1]=Math.max(maxima[1],s.angularVelocity.length());
      if(previousVelocity[0]!=null){maxima[2]=Math.max(maxima[2],new Vector3d(s.linearVelocity).sub(previousVelocity[0]).length()/.05);
        maxima[3]=Math.max(maxima[3],new Vector3d(s.angularVelocity).sub(previousAngular[0]).length()/.05);}
      previousVelocity[0]=new Vector3d(s.linearVelocity);previousAngular[0]=new Vector3d(s.angularVelocity);
      if(interrupt[0]<0&&s.programSeconds>=spec.build().durationSeconds()*.35){
        interrupt[0]=time;if(telemetry[0]!=null)telemetry[0].interrupt();
        if(spec.mechanism()==MovingMechanism.DROP||spec.mechanism()==MovingMechanism.ELEVATOR)motor.generatedSpeed.setValue(0);
        else MovingRuntime.emergencyHold(level,s,"Native fixture operator stop during "+spec.mechanism());
        System.out.println("FD2002_INTERRUPT mechanism="+spec.mechanism()+" pose="+s.actualPose+" v="+s.linearVelocity+" w="+s.angularVelocity);
      }
      if(interrupt[0]<0||resumed[0])h.assertTrue(s.phase!=MovingAssemblyState.Phase.FAULT_HOLD,"Unexpected "+spec.mechanism()+" fault: "+s.fault);
      if(interrupt[0]>=0&&!resumed[0]&&time>=interrupt[0]+40&&MovingNativeAdapter.stopped(level,s)){
        h.assertTrue(s.phase==MovingAssemblyState.Phase.FAULT_HOLD&&!s.locksEngaged,"Interruption did not disconnect and hold");
        motor.generatedSpeed.setValue(64);
        // Allow real kinetic graph propagation before the explicit resume request.
        if(time>=interrupt[0]+45){if(telemetry[0]!=null)telemetry[0].resumed();MovingRuntime.operate(player,controller,MovingRuntime.Action.RESUME,s.id,s.epoch,s.sequence,s.destination);resumed[0]=true;
          System.out.println("FD2002_RESUME mechanism="+spec.mechanism()+" pose="+s.actualPose);}
      }
      if(resumed[0]&&s.phase==MovingAssemblyState.Phase.RECONNECTED){
        var target=s.docks.get(s.destination).pose();
        h.assertTrue(s.locksEngaged&&MovingNativeAdapter.liveDockEdge(level,s)!=null,"Endpoint graph lacks measured physical lock");
        h.assertTrue(s.actualPose.position().distance(target.position())<=MovingNativeAdapter.POSITION_TOLERANCE
            &&MovingNativeAdapter.angularError(s.actualPose.orientation(),target.orientation())<=MovingNativeAdapter.ANGLE_TOLERANCE,"Measured endpoint mismatch");
        h.assertTrue(MovingNativeAdapter.stopped(level,s)&&s.bodies.containsKey(v.chassis)&&v.restraints,"Physical Wing retention/stop lost");
        var parent=(dev.ryanhcode.sable.sublevel.ServerSubLevel)SubLevelContainer.getContainer(level).getSubLevel(v.chassis);
        var bed=(dev.ryanhcode.sable.sublevel.ServerSubLevel)SubLevelContainer.getContainer(level).getSubLevel(s.bed);
        var local=s.bodies.get(v.chassis);var c=bed.getPlot().getCenterBlock();
        var expected=bed.logicalPose().transformPosition(local.position().add(c.getX()+.5,c.getY()+.5,c.getZ()+.5));
        h.assertTrue(parent.logicalPose().position().distance(expected)<.05,"Native Wing failed to follow complete rigid transform");
        System.out.println("FD2002_PASS mechanism="+spec.mechanism()+" actual="+s.actualPose+" target="+target+" maxSpeed="+maxima[0]+" maxAngular="+maxima[1]+" tickAcceleration="+maxima[2]+" tickAngularAcceleration="+maxima[3]);
        finished[0]=true;if(telemetry[0]!=null)telemetry[0].finish();h.succeed();
      }
    });}
  }
}
