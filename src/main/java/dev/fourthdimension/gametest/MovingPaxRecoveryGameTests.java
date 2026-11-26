package dev.fourthdimension.gametest;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.moving.*;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.shuttle.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

/** Original four-car PAX station, actual ride to table, full show turn and real carriage recovery. */
@GameTestHolder("fourthdimension_fd2001_pax_recovery")
@PrefixGameTestTemplate(false)
public final class MovingPaxRecoveryGameTests {
  @GameTest(template="empty",timeoutTicks=5200)
  public static void originalWinchRecoversAfterFullShowTurn(GameTestHelper h){
    var level=h.getLevel();var origin=new BlockPos(-340,180,-740);var control=origin.west(28);
    for(int x=-25;x<=-18;x++)for(int z=-56;z<=-38;z++)level.setChunkForced(x,z,true);
    var player=FakePlayerFactory.get(level,new GameProfile(new UUID(0xFD20010000000000L,10),"FDMovingPax"));
    player.setGameMode(GameType.CREATIVE);player.setPos(control.getX()+.5,control.getY(),control.getZ()+2.5);
    level.setBlockAndUpdate(control,MovingContent.CONTROLLERS.get(MovingMechanism.TURNTABLE).get().defaultBlockState().setValue(BlockStateProperties.FACING,Direction.EAST));
    motor(level,control.west(),64);
    var spec=new MovingProgramSpec(MovingMechanism.TURNTABLE,0,0,0,true);
    var state=new MovingAssemblyState[1];var cars=new ArrayList<FoundationData.Vehicle>();var winch=new ShuttleWinchEntity[1];
    var telemetry=new MovingNativeMeasurements[1];var originalStation=new double[1];var originalCarriage=new UUID[1];
    var originalRoute=new BlockPos[2];int[] stoppedAt={-1},moveAt={-1},interruptedAt={-1},releasedAt={-1};
    boolean[] resumed={false},dispatch={false},catchSeen={false},done={false};
    var stationRail=new FoundationData.Rail[1];
    h.runAtTickTime(5,()->{
      for(int z:new int[]{-16,16})level.setBlockAndUpdate(origin.offset(0,0,z),SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      var s=state[0]=MovingConstruction.build(player,control,new MovingConstruction.ConfigureRequest("pax",32,origin,Direction.SOUTH,spec));
      var a=origin.offset(0,24,-120);var b=origin.offset(0,0,-72);var c=origin.offset(0,0,-40);
      var d=s.docks.get(s.source).from();var e=s.docks.get(s.source).to();
      var f=origin.offset(0,0,48);var g=origin.offset(0,32,96);
      connect(level,a,b,new Vec3(0,-.8,1).normalize(),new Vec3(0,0,1));
      stationRail[0]=connect(level,b,c,new Vec3(0,0,1),new Vec3(0,0,1));
      connect(level,c,d,new Vec3(0,0,1),new Vec3(0,0,1));
      connect(level,e,f,new Vec3(0,0,1),new Vec3(0,0,1));
      connect(level,f,g,new Vec3(0,0,1),new Vec3(0,.8,1).normalize());
      // Native primary-edge orientation is canonicalized after both endpoints join.
      // Match the normal ShuttleExamples authored-route finalization before any ride begins.
      TrackGeometry.invalidate(level);
      for(var rail:FoundationData.get(level).rails){
        if(rail.matches(s.railFrom,s.railTo))continue;
        var resolved=TrackGeometry.resolve(level,rail);long before=rail.geometryHash;
        rail.profile=rail.profile.scaledTo(TrackGeometry.table(level,resolved).length());
        rail.geometryHash=TrackGeometry.fingerprint(level,resolved);
        if(before!=rail.geometryHash)System.out.println("FD2001_PAX_CANONICAL_BIND from="+rail.from+" to="+rail.to+" before="+before+" actual="+rail.geometryHash);
      }
      var w=origin.offset(-6,0,-64);
      level.setBlockAndUpdate(w,ShuttleContent.WINCH.get().defaultBlockState().setValue(ShuttleWinchBlock.AXIS,Direction.Axis.X));
      motor(level,w.west(),128);winch[0]=(ShuttleWinchEntity)level.getBlockEntity(w);
    });
    h.runAtTickTime(50,()->{
      var edge=TrackGeometry.resolve(level,stationRail[0]);
      for(int i=0;i<4;i++)cars.add(VehicleFactory.create(level,TrackDesign.PAX,MotorbikeExamples.sample(level,edge,8+6*i)));
    });
    h.runAtTickTime(130,()->{
      ShuttleExamples.assemble(level,new ShuttleExamples.Example(List.copyOf(cars),winch[0]));
      originalStation[0]=winch[0].stationDistance;originalRoute[0]=winch[0].routeStart;originalRoute[1]=winch[0].routeEnd;
    });
    h.runAtTickTime(180,()->{
      VehicleOperations.restraints(level,cars.getFirst(),true);winch[0].start();originalCarriage[0]=winch[0].carriage;
      System.out.println("FD2001_PAX_STARTED station="+originalStation[0]+" carriage="+originalCarriage[0]+" cars="+winch[0].cars);
    });
    for(int tick=181;tick<=5100;tick++){final int time=tick;h.runAtTickTime(tick,()->{
      if(done[0])return;var s=state[0];var w=winch[0];var first=cars.getFirst();
      if(originalCarriage[0]==null&&w.carriage!=null)originalCarriage[0]=w.carriage;
      var parent=SubLevelContainer.getContainer(level).getSubLevel(w.vehicle);
      double z=parent.logicalPose().position().z-(origin.getZ()+.5);
      if(time%100==0)System.out.println("FD2001_PAX_SAMPLE tick="+time+" phase="+w.phase+" table="+s.phase+" z="+z+" carriage="+w.carriageDistance+" fault="+w.fault);
      if(stoppedAt[0]<0){
        if(w.phase==ShuttleWinchEntity.Phase.FAULT){
          MovingNativeAdapter.readback(level,s);
          System.out.println("FD2001_PAX_ENTRY_FAULT pose="+s.actualPose+" v="+s.linearVelocity+" w="+s.angularVelocity+" live="+(MovingNativeAdapter.liveDockEdge(level,s)!=null));
          for(var c:cars){var body=SubLevelContainer.getContainer(level).getSubLevel(c.chassis);var pos=body.getPlot().getCenterBlock();
            System.out.println("FD2001_PAX_ENTRY_BODY id="+c.chassis+" pose="+body.logicalPose()+" cart="+level.getBlockState(pos)+" bound="+c.boundFrom+"/"+c.boundTo);
          }
        }
        h.assertTrue(w.phase!=ShuttleWinchEntity.Phase.FAULT,"Original ride fault before table: "+w.fault);
        if(w.phase==ShuttleWinchEntity.Phase.OUTBOUND&&z>=-9){
          h.assertTrue(z<0,"Original PAX train skipped the bounded capture position");
          VehicleOperations.capture(level,first);stoppedAt[0]=time;
          System.out.println("FD2001_PAX_OPERATOR_CAPTURE z="+z+" carriage="+w.carriageDistance);
        }
        return;
      }
      if(moveAt[0]<0){
        if(time<stoppedAt[0]+40||cars.stream().anyMatch(c->!ShuttleRuntime.readyForRecovery(level,c)))return;
        if(!physicallyStopped(level,cars,time%100==0))return;
        MovingRuntime.operate(player,control,MovingRuntime.Action.CAPTURE,s.id,s.epoch,s.sequence,s.destination);
        h.assertTrue(s.cars.size()==4&&s.bodies.keySet().containsAll(w.cars),"Moving support did not capture all four original PAX chassis");
        telemetry[0]=new MovingNativeMeasurements(level,s,"pax-show360");
        MovingRuntime.operate(player,control,MovingRuntime.Action.MOVE,s.id,s.epoch,s.sequence,s.destination);moveAt[0]=time;
        System.out.println("FD2001_PAX_SHOW_STARTED duration="+spec.build().durationSeconds()+" bodies="+s.bodies.size());return;
      }
      if(releasedAt[0]<0){
        if(interruptedAt[0]<0&&s.programSeconds>spec.build().durationSeconds()*.35){
          telemetry[0].interrupt();MovingRuntime.emergencyHold(level,s,"PAX full-show validation operator stop");interruptedAt[0]=time;
        }
        if(interruptedAt[0]>=0&&!resumed[0]&&time>interruptedAt[0]+45&&MovingNativeAdapter.stopped(level,s)){
          telemetry[0].resumed();MovingRuntime.operate(player,control,MovingRuntime.Action.RESUME,s.id,s.epoch,s.sequence,s.destination);resumed[0]=true;
        }
        if(interruptedAt[0]<0||resumed[0])h.assertTrue(s.phase!=MovingAssemblyState.Phase.FAULT_HOLD,"PAX moving support fault: "+s.fault);
        if(resumed[0]&&s.phase==MovingAssemblyState.Phase.RECONNECTED){
          h.assertTrue(s.locksEngaged&&MovingNativeAdapter.liveDockEdge(level,s)!=null,"Full360 endpoint lacks physical socket lock");
          telemetry[0].finish();MovingRuntime.operate(player,control,MovingRuntime.Action.RELEASE,s.id,s.epoch,s.sequence,s.destination);releasedAt[0]=time;
          System.out.println("FD2001_PAX_RELEASED originalStation="+w.stationDistance+" carriage="+w.carriageDistance+" catch="+ShuttleRuntime.movingCatchEngaged(level,w));
        }return;
      }
      h.assertTrue(originalCarriage[0].equals(w.carriage)&&originalRoute[0].equals(w.routeStart)&&originalRoute[1].equals(w.routeEnd)
          &&Math.abs(originalStation[0]-w.stationDistance)<.001,"Moving recovery reassigned original PAX apparatus/station");
      if(!dispatch[0]&&time>=releasedAt[0]+20){
        h.assertTrue(!ShuttleRuntime.movingCatchEngaged(level,w),"Fixture must begin with carriage away from transferred lead");
        VehicleOperations.dispatch(level,first);dispatch[0]=true;
        h.assertTrue(w.phase==ShuttleWinchEntity.Phase.CAPTURING,"Explicit Dispatch did not request original carriage recovery");
        System.out.println("FD2001_PAX_RECOVERY_REQUEST z="+z+" carriage="+w.carriageDistance);return;
      }
      if(dispatch[0]){
        if(time%200==0)for(var car:cars){
          var body=(dev.ryanhcode.sable.sublevel.ServerSubLevel)SubLevelContainer.getContainer(level).getSubLevel(car.chassis);
          System.out.println("FD2001_PAX_RETURN_READY id="+car.chassis+" ready="+Boarding.ready(level,car)
              +" v="+SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d()).length()
              +" service="+s.service.cars.containsKey(car.chassis)+" carriers="+car.carriers.stream().map(c->c.capture+":"+c.angle+":"+c.omega).toList());
        }
        h.assertTrue(w.phase!=ShuttleWinchEntity.Phase.FAULT,"Original carriage recovery failed: "+w.fault);
        if(ShuttleRuntime.movingCatchEngaged(level,w))catchSeen[0]=true;
        if(w.phase==ShuttleWinchEntity.Phase.BOARDING&&cars.stream().allMatch(c->ShuttleRuntime.stationHeld(level,c))){
          h.assertTrue(catchSeen[0],"No actual native catch was observed before station return");
          h.assertTrue(cars.stream().allMatch(c->ShuttleRuntime.stationHeld(level,c)&&c.restraints&&c.fault.isEmpty()),"Whole original train not safely held at fixed station");
          h.assertTrue(s.service.cars.isEmpty(),"Moving service posture ownership did not clear after departure");
          System.out.println("FD2001_PAX_RECOVERY_PASS station="+w.stationDistance+" carriage="+w.carriage+" originalCars="+w.cars+" table="+s.phase);done[0]=true;h.succeed();
        }
      }
    });}
  }
  private static boolean physicallyStopped(net.minecraft.server.level.ServerLevel level,List<FoundationData.Vehicle> cars,boolean log){
    var container=SubLevelContainer.getContainer(level);var physics=container.physicsSystem();boolean stopped=true;
    for(var car:cars){var ids=new ArrayList<UUID>();ids.add(car.chassis);car.carriers.forEach(c->ids.add(c.body));
      for(var id:ids){var b=(dev.ryanhcode.sable.sublevel.ServerSubLevel)container.getSubLevel(id);physics.updatePose(b);
        double v=physics.getPipeline().getLinearVelocity(b,new Vector3d()).length();double w=physics.getPipeline().getAngularVelocity(b,new Vector3d()).length();
        if(v>MovingNativeAdapter.LOCK_SPEED||w>Math.toRadians(.1)){stopped=false;if(log)System.out.println("FD2001_PAX_SETTLING id="+id+" v="+v+" w="+w);}
      }
    }return stopped;
  }
  private static void motor(net.minecraft.server.level.ServerLevel level,BlockPos pos,int speed){
    level.setBlockAndUpdate(pos,AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.EAST));
    ((CreativeMotorBlockEntity)level.getBlockEntity(pos)).generatedSpeed.setValue(speed);
  }
  private static FoundationData.Rail connect(net.minecraft.server.level.ServerLevel level,BlockPos from,BlockPos to,Vec3 first,Vec3 last){
    for(var p:List.of(from,to))if(!(level.getBlockEntity(p) instanceof CoasterAnchorpointBlockEntity))level.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    MotorbikeExamples.connect(level,from,to,first,last.scale(-1),new Vec3(0,1,0),new Vec3(0,1,0));
    var curve=((CoasterAnchorpointBlockEntity)level.getBlockEntity(from)).getAnchorPeerCurvesView().get(to);
    var edge=CoasterPathEdge.curve(from,to,curve);double length=TrackGeometry.table(level,edge).length();
    var rail=new FoundationData.Rail(from,to,TrackDesign.PAX.family,new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(length,0))),
        UUID.randomUUID(),TrackDesign.PAX.variant,TrackGeometry.fingerprint(level,edge),0,true);
    TrackPlacement.captureOriginalCurve(rail,curve);FoundationData.get(level).rails.add(rail);TrackGeometry.invalidate(level);return rail;
  }
}
