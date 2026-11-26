package dev.fourthdimension.gametest;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.moving.*;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.runtime.CarrierRuntime;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import dev.fourthdimension.track.OrdinaryGearCertificate;
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

/** Bounded production maximum workload and full retained show-turn measurement. */
@GameTestHolder("fourthdimension_fd2007_maxshow")
@PrefixGameTestTemplate(false)
public final class MovingMaximumShowGameTests {
  @GameTest(template="empty",timeoutTicks=4200)
  public static void maximumNativeShowTurn(GameTestHelper h){
    var spec=new MovingProgramSpec(MovingMechanism.TURNTABLE,0,0,90,true);
    int index=20;
    String measurement="max48-four-native-show450-20261126";
    var level=h.getLevel();var origin=new BlockPos(-700-index*128,180,-700);var controller=origin.west(44);
    for(int x=(origin.getX()>>4)-4;x<=(origin.getX()>>4)+4;x++)for(int z=(origin.getZ()>>4)-4;z<=(origin.getZ()>>4)+4;z++)level.setChunkForced(x,z,true);
    var player=FakePlayerFactory.get(level,new GameProfile(new UUID(0xFD20020000000000L,index+1),"FDMechanism"+index));
    player.setGameMode(GameType.CREATIVE);player.setPos(controller.getX()+.5,controller.getY(),controller.getZ()+2.5);
    level.setBlockAndUpdate(controller,MovingContent.CONTROLLERS.get(spec.mechanism()).get().defaultBlockState().setValue(BlockStateProperties.FACING,Direction.EAST));
    level.setBlockAndUpdate(controller.west(),AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.EAST));
    var motor=(CreativeMotorBlockEntity)level.getBlockEntity(controller.west());motor.generatedSpeed.setValue(64);
    for(var local:List.of(spec.build().start(),spec.build().end()))for(int z:new int[]{-24,24}){
      var point=local.transform(new MovingProgram.Point(0,-.25,z));
      var socket=BlockPos.containing(origin.getX()+.5+point.x(),origin.getY()+.5+point.y(),origin.getZ()+.5+point.z());
      level.setBlockAndUpdate(socket,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    }
    var request=new MovingConstruction.ConfigureRequest("native_service",48,origin,Direction.SOUTH,spec);
    var state=new MovingAssemblyState[1];var cars=new ArrayList<ServerSubLevel>();var performance=new CarrierRuntime.PerformanceSnapshot[1];
    double[] angle={0,0},serverAverage={0,0};long[] worldTicks={0,0,0},serverTicks={0,0,0};var telemetry=new MovingNativeMeasurements[1];
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
      for(int i=0;i<4;i++)cars.add(spawn(h,edge,9+6*i));
    });
    h.runAtTickTime(110,()->{
      var s=state[0];
      for(int i=0;i<3;i++)h.assertTrue(CoasterCartTrainLinkConstraint.linkCarts(level,cars.get(i),cars.get(i+1),6),"Native coupling setup failed");
      var physics=SubLevelContainer.getContainer(level).physicsSystem();
      for(var car:cars){physics.updatePose(car);physics.getPipeline().resetVelocity(car);}
      physics.getPipeline().resetVelocity((ServerSubLevel)SubLevelContainer.getContainer(level).getSubLevel(s.bed));
      MovingRuntime.operate(player,controller,MovingRuntime.Action.CAPTURE,s.id,s.epoch,s.sequence,s.destination);
      h.assertTrue(s.cars.size()==4&&s.bodies.size()==4,"Expected maximum four-car production capture");
      performance[0]=CarrierRuntime.performanceSnapshot();
      telemetry[0]=new MovingNativeMeasurements(level,s,measurement);
      MovingRuntime.operate(player,controller,MovingRuntime.Action.MOVE,s.id,s.epoch,s.sequence,s.destination);
      System.out.println("FD2002_BEGIN mechanism="+spec.mechanism()+" id="+s.id+" duration="+spec.build().durationSeconds()+" limits="+spec.build().limits());
    });
    for(int tick=111;tick<=4100;tick++){final int time=tick;h.runAtTickTime(tick,()->{
      if(finished[0])return;var s=state[0];MovingNativeAdapter.readback(level,s);
      var forward=s.actualPose.orientation().transform(new Vector3d(0,0,1));
      double yaw=Math.atan2(forward.x,forward.z),delta=yaw-angle[0];
      angle[1]+=Math.atan2(Math.sin(delta),Math.cos(delta));angle[0]=yaw;
      double average=level.getServer().getAverageTickTimeNanos()/1_000_000.0;serverAverage[0]=Math.max(serverAverage[0],average);serverAverage[1]=average;
      var allTicks=level.getServer().getTickTimesNanos();long full=allTicks[(level.getServer().getTickCount()-1)%100];serverTicks[0]+=full;serverTicks[1]++;serverTicks[2]=Math.max(serverTicks[2],full);
      var tickTimes=level.getServer().getTickTime(level.dimension());
      if(tickTimes!=null){long n=tickTimes[(level.getServer().getTickCount()-1)%100];worldTicks[0]+=n;worldTicks[1]++;worldTicks[2]=Math.max(worldTicks[2],n);}
      if(s.programSeconds>0&&s.phase!=MovingAssemblyState.Phase.RECONNECTED)
        h.assertTrue(!s.locksEngaged&&MovingNativeAdapter.liveDockEdge(level,s)==null,"Show connected at intermediate yaw="+angle[1]);
      if(time%100==0)System.out.println("FD2002_SAMPLE mechanism="+spec.mechanism()+" tick="+time+" phase="+s.phase+" actual="+s.actualPose+" v="+s.linearVelocity+" w="+s.angularVelocity);
      maxima[0]=Math.max(maxima[0],s.linearVelocity.length());maxima[1]=Math.max(maxima[1],s.angularVelocity.length());
      if(previousVelocity[0]!=null){maxima[2]=Math.max(maxima[2],new Vector3d(s.linearVelocity).sub(previousVelocity[0]).length()/.05);
        maxima[3]=Math.max(maxima[3],new Vector3d(s.angularVelocity).sub(previousAngular[0]).length()/.05);}
      previousVelocity[0]=new Vector3d(s.linearVelocity);previousAngular[0]=new Vector3d(s.angularVelocity);
      if(interrupt[0]<0&&s.programSeconds>=spec.build().durationSeconds()*.35){
        interrupt[0]=time;if(telemetry[0]!=null)telemetry[0].interrupt();
        if(spec.mechanism()==MovingMechanism.DROP||spec.mechanism()==MovingMechanism.ELEVATOR)motor.generatedSpeed.setValue(0);
        else MovingRuntime.emergencyHold(level,s,"Measured maximum native show-turn stop");
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
        h.assertTrue(MovingNativeAdapter.stopped(level,s)&&s.bodies.size()==4,"Maximum native retention/stop lost");
        h.assertTrue(angle[1]>=Math.toRadians(450)-.02,"Actual full show-turn not observed: "+angle[1]);
        var bed=(ServerSubLevel)SubLevelContainer.getContainer(level).getSubLevel(s.bed);
        var center=bed.getPlot().getCenterBlock();double maxError=0;
        for(var car:cars){
          h.assertTrue(s.bodies.containsKey(car.getUniqueId()),"Native identity lost");
          var local=s.bodies.get(car.getUniqueId());
          var expected=bed.logicalPose().transformPosition(local.position().add(center.getX()+.5,center.getY()+.5,center.getZ()+.5));
          maxError=Math.max(maxError,car.logicalPose().position().distance(expected));
        }
        h.assertTrue(maxError<.05,"Four bodies failed complete rigid transform: "+maxError);
        var end=CarrierRuntime.performanceSnapshot();long callbacks=end.steps()-performance[0].steps(),nanos=end.nanos()-performance[0].nanos();
        h.assertTrue(callbacks>0&&nanos>0,"Missing addon workload timing");
        System.out.println("FD2007_MAXSHOW_WORKLOAD bedLength=48 cars=4 physicalBodies=5 callbacks="+callbacks+" addonNanos="+nanos+" averageAddonCallbackNanos="+(nanos/callbacks)
          +" observedWorldTicks="+worldTicks[1]+" averageWorldTickNanos="+(worldTicks[0]/Math.max(1,worldTicks[1]))+" maxWorldTickNanos="+worldTicks[2]
          +" averageServerTickNanos="+(serverTicks[0]/Math.max(1,serverTicks[1]))+" maxServerTickNanos="+serverTicks[2]
          +" serverSmoothedTickMsMax="+serverAverage[0]+" serverSmoothedTickMsFinal="+serverAverage[1]+" actualUnwrappedYaw="+angle[1]+" maxBodyPoseError="+maxError);
        System.out.println("FD2002_PASS mechanism="+spec.mechanism()+" actual="+s.actualPose+" target="+target+" maxSpeed="+maxima[0]+" maxAngular="+maxima[1]+" tickAcceleration="+maxima[2]+" tickAngularAcceleration="+maxima[3]);
        finished[0]=true;if(telemetry[0]!=null)telemetry[0].finish();h.succeed();
      }
    });}
  }
  private static ServerSubLevel spawn(GameTestHelper h,CoasterPathEdge edge,double distance){
    var level=h.getLevel();var hit=MotorbikeExamples.sample(level,edge,distance);
    var q=CoasterPathTrackFrame.quaternionFromTrackBasis(CoasterPathTrackFrame.trackBasisAtHit(level,hit));
    var origin=CoasterCartLinkPlacement.plotOriginForBearingCenter(CoasterPathTrackFrame.pathAnchorForSnappedBlock(level,hit),q);
    var body=CoasterCartSpawner.spawnMinimalContraption(level,origin,q,hit,true);
    h.assertTrue(body!=null&&OrdinaryGearCertificate.fits(body),"Ordinary native certificate failed");return body;
  }

}
