package dev.fourthdimension.gametest;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.moving.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.physics.RotationProfile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import dev.fourthdimension.integration.*;
import dev.fourthdimension.station.*;
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
@GameTestHolder("fourthdimension_fd2001_engineered_handoff")
@PrefixGameTestTemplate(false)
public final class MovingEngineeredHandoffGameTests {
  @GameTest(template="empty",timeoutTicks=240)
  public static void fixedOrdinaryMotorRegression(GameTestHelper h){
    EngineeredGameTests.ordinaryBearingUsesMotorWithoutRotaryMembership(h);
  }
  @GameTest(template="empty",timeoutTicks=1050)
  public static void engineeredFixedToRetainedToFixed(GameTestHelper h){
    var level=h.getLevel();var origin=new BlockPos(-420,130,-620);var controller=origin.west(14);
    for(int x=-29;x<=-24;x++)for(int z=-42;z<=-36;z++)level.setChunkForced(x,z,true);
    var player=FakePlayerFactory.get(level,new GameProfile(UUID.fromString("fdfdfdfd-0000-0000-0000-000000002002"),"FDEngineeredMoving"));
    player.setGameMode(GameType.CREATIVE);player.setPos(controller.getX()+.5,controller.getY(),controller.getZ()+2.5);
    level.setBlockAndUpdate(controller,MovingContent.CONTROLLERS.get(MovingMechanism.TRANSFER).get().defaultBlockState()
        .setValue(BlockStateProperties.FACING,Direction.EAST));
    level.setBlockAndUpdate(controller.west(),AllBlocks.CREATIVE_MOTOR.getDefaultState()
        .setValue(BlockStateProperties.FACING,Direction.EAST));
    ((CreativeMotorBlockEntity)level.getBlockEntity(controller.west())).generatedSpeed.setValue(64);
    for(int x:new int[]{0,4})for(int z:new int[]{-8,8})level.setBlockAndUpdate(origin.offset(x,0,z),
        SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var request=new MovingConstruction.ConfigureRequest("arrow",16,origin,Direction.SOUTH,
        new MovingProgramSpec(MovingMechanism.TRANSFER,4,0,0,false));
    var state=new MovingAssemblyState[1];var car=new FoundationData.Vehicle[1];var start=new Vector3d[1];
    boolean[] released={false};int[] releaseTick={0};
    var sourceStation=origin.west(6);var destinationStation=origin.east(10);
    var originalAuthority=new UUID[1];boolean[] dispatched={false},transferred={false};int[] transferTick={0};
    h.runAtTickTime(5,()->{
      state[0]=MovingConstruction.build(player,controller,request);
      for(var dock:state[0].docks.values()){
        var from=dock.from().offset(0,0,-24);var to=dock.to().offset(0,0,24);
        level.setBlockAndUpdate(from,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
        level.setBlockAndUpdate(to,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
        author(level,MotorbikeExamples.connect(level,from,dock.from(),new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0)));
        author(level,MotorbikeExamples.connect(level,dock.to(),to,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0)));
      }
    });
    h.runAtTickTime(50,()->{
      var s=state[0];h.assertTrue(s.locksEngaged,"Production source failed to lock: "+s.phase+" "+s.fault);
      var edge=MovingNativeAdapter.liveDockEdge(level,s);h.assertTrue(edge!=null,"No authenticated locked source bridge");
      h.assertTrue(OptionalIntegrations.installed(OptionalIntegrations.ENGINEERED).mayInspect(),"Pinned Engineered addon unavailable");
      var setup=author(level,edge);
      try{car[0]=VehicleFactory.create(level,TrackDesign.ARROW,MotorbikeExamples.sample(level,edge,8));}
      finally{FoundationData.get(level).rails.remove(setup);}
      motor(h,setup,s.docks.get(s.source).from().west(6));
      for(var pos:List.of(sourceStation,destinationStation)){
        level.setBlockAndUpdate(pos,StationContent.STATION.get().defaultBlockState());
        ((BoardingStationEntity)level.getBlockEntity(pos)).vehicle=car[0].chassis;
      }


    });
    h.runAtTickTime(110,()->{
      var s=state[0];var v=car[0];
      h.assertTrue(Boarding.ready(level,v),"Normal Arrow readiness failed: "+v.fault);
      VehicleOperations.restraints(level,v,true);
      start[0]=new Vector3d(SubLevelContainer.getContainer(level).getSubLevel(v.chassis).logicalPose().position());
      // Disclosed stationary fixture setup: no movement/stop-control claim is made by this brake.
      var physics=SubLevelContainer.getContainer(level).physicsSystem();
      var ids=new ArrayList<UUID>();ids.add(v.chassis);v.carriers.forEach(c->ids.add(c.body));
      for(var id:ids){var body=(dev.ryanhcode.sable.sublevel.ServerSubLevel)SubLevelContainer.getContainer(level).getSubLevel(id);physics.updatePose(body);physics.getPipeline().resetVelocity(body);}
      RideAuthority.claim(level,sourceStation,v.chassis,RideAuthority.ENGINEERED);
      originalAuthority[0]=v.authority.owner;
      MovingRuntime.operate(player,controller,MovingRuntime.Action.CAPTURE,s.id,s.epoch,s.sequence,"end");
      h.assertTrue(s.retained()&&s.bodies.containsKey(v.chassis)&&FoundationData.get(level).vehicles.get(v.chassis)==v,
          "Production capture did not retain original registered Arrow");
      MovingRuntime.operate(player,controller,MovingRuntime.Action.MOVE,s.id,s.epoch,s.sequence,"end");
      System.out.println("FD2001_PRODUCTION_STARTED id="+s.id+" chassis="+v.chassis+" duration="+s.program.build().durationSeconds());
    });
    for(int tick=120;tick<=1000;tick+=10){final int time=tick;h.runAtTickTime(tick,()->{
      var s=state[0];var v=car[0];
      h.assertTrue(s.phase!=MovingAssemblyState.Phase.FAULT_HOLD,"Production coordinator fault at "+time+": "+s.fault);
      if(time%40==0)System.out.println("FD2001_PRODUCTION_STATE tick="+time+" phase="+s.phase+" pose="+s.actualPose+" velocity="+s.linearVelocity);
      if(!released[0]&&s.phase==MovingAssemblyState.Phase.RECONNECTED){
        h.assertTrue(s.locksEngaged&&MovingNativeAdapter.liveDockEdge(level,s)!=null,"Destination is not physically authenticated");
        var chassis=SubLevelContainer.getContainer(level).getSubLevel(v.chassis);
        h.assertTrue(chassis.logicalPose().position().x>start[0].x+3.8,"Registered Arrow did not physically follow translation");
        MovingRuntime.operate(player,controller,MovingRuntime.Action.RELEASE,s.id,s.epoch,s.sequence,"end");
        h.assertTrue(!s.retained()&&v.boundFrom.equals(s.docks.get("end").from()),"Release did not bind actual receiving support");
        released[0]=true;releaseTick[0]=time;
      }
      if(!released[0])h.assertTrue(RideAuthority.holds(level,v)&&EngineeredInterlock.holds((dev.ryanhcode.sable.sublevel.ServerSubLevel)SubLevelContainer.getContainer(level).getSubLevel(v.chassis),level),"Conflicting propulsion escaped retention");
      if(released[0]&&!transferred[0]&&time>=releaseTick[0]+30){
        h.assertTrue(v.restraints&&v.fault.isEmpty(),"Release fault: "+v.fault);
        var dock=s.docks.get("end");var drive=rail(level,MovingNativeAdapter.liveDockEdge(level,s));motor(h,drive,dock.from().east(6));
        RideAuthority.transfer(level,sourceStation,v.chassis,RideAuthority.token(v),destinationStation);
        h.assertTrue(!originalAuthority[0].equals(v.authority.owner)&&v.authority.held,"Explicit stopped station transfer failed");
        transferred[0]=true;transferTick[0]=time;
      }
      if(transferred[0]&&!dispatched[0]&&time>=transferTick[0]+10){
        RideAuthority.dispatch(level,destinationStation,v.chassis,RideAuthority.token(v));dispatched[0]=true;
        System.out.println("FD2001_ENGINEERED_DISPATCH original="+originalAuthority[0]+" receiving="+v.authority.owner);
      }
      if(dispatched[0]&&time==280){
        var b=(dev.ryanhcode.sable.sublevel.ServerSubLevel)SubLevelContainer.getContainer(level).getSubLevel(v.chassis);
        var edge=MovingNativeAdapter.liveDockEdge(level,s);
        var center=dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve.cartCenterWorld(b);
        var hit=dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
        var span=OptionalCalls.staticCall("dev.jwaterfall.coastersengineered.span.TrackSpanIndex","runAt",level,edge,hit.t());
        var ctrl=level.getBlockEntity(s.docks.get("end").from().east(6));
        System.out.println("FD2001_ENGINEERED_DIAG center="+center+" guard="+EngineeredInterlock.holds(b,level)+" hit="+hit.t()+" span="+span+" mode="+OptionalCalls.call(ctrl,"commandMode")+" value="+OptionalCalls.call(ctrl,"commandValue")+" bound="+OptionalCalls.staticCall("dev.jwaterfall.coastersengineered.controller.LinearMotorControllerIndex","boundTo",level,s.docks.get("end").from()));
        if(span!=null)System.out.println("FD2001_ENGINEERED_SPAN has="+OptionalCalls.call(span,"hasSpan")+" direction="+OptionalCalls.call(span,"directionAt",edge,hit.t()));
      }
      if(dispatched[0]&&time%40==0){var b=(dev.ryanhcode.sable.sublevel.ServerSubLevel)SubLevelContainer.getContainer(level).getSubLevel(v.chassis);System.out.println("FD2001_ENGINEERED_DEPART tick="+time+" pose="+b.logicalPose().position()+" fault="+v.fault+" stop="+v.stopping+" held="+v.authority.held+" reason="+v.authority.reason+" bound="+v.boundFrom+"/"+v.boundTo);}
      if(dispatched[0]&&!MovingServiceRuntime.applies(level,v)){
        var pose=SubLevelContainer.getContainer(level).getSubLevel(v.chassis).logicalPose().position();
        h.assertTrue(Math.abs(pose.z-start[0].z)>9,"Whole car did not depart to actual fixed support");
        h.assertTrue(v.fault.isEmpty()&&v.restraints,"Fixed departure fault: "+v.fault);
        h.assertTrue(RideAuthority.deniesProvider(level,v,RideAuthority.MAGNETIZED),"Competing Magnetized force was permitted");
        System.out.println("FD2001_ENGINEERED_HANDOFF_PASS retainedForceDenied=true originalIdentity=true fixedEquipmentOnly=true actualDeparture="+pose);h.succeed();
      }
    });}
  }
  private static FoundationData.Rail rail(net.minecraft.server.level.ServerLevel l,dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge edge){
    double length=TrackGeometry.table(l,edge).length();
    var r=new FoundationData.Rail(edge.from(),edge.to(),TrackDesign.ARROW.family,new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(length,0))),UUID.randomUUID(),TrackDesign.ARROW.variant,TrackGeometry.fingerprint(l,edge),0,true);
    TrackPlacement.captureOriginalCurve(r,edge.bezier());return r;
  }
  private static FoundationData.Rail author(net.minecraft.server.level.ServerLevel l,dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge edge){var r=rail(l,edge);FoundationData.get(l).rails.add(r);return r;}
  private static void motor(GameTestHelper h,FoundationData.Rail rail,BlockPos pos){
    var block=BuiltInRegistries.BLOCK.get(ResourceLocation.parse("coastersengineered:linear_motor_controller"));h.getLevel().setBlockAndUpdate(pos,block.defaultBlockState());
    EngineeredGameTests.configureMotor(h,rail,pos,4,"FORWARD");
  }
}
