package dev.fourthdimension.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.integration.*;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.station.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftEndRef;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartChainLiftDrive;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

/** One actual installed conflict scenario; isolated velocity resets measure callback impulses only. */
@GameTestHolder("fourthdimension_fd813")
@PrefixGameTestTemplate(false)
public final class OperationsConflictGameTests {
  @GameTest(template="empty",timeoutTicks=240)
  public static void installedProvidersRespectOneOwner(GameTestHelper h){
    var l=h.getLevel();var from=new BlockPos(1600,100,600);var to=from.offset(0,0,80);
    for(int x=99;x<=101;x++)for(int z=36;z<=44;z++)l.setChunkForced(x,z,true);
    for(var id:List.of(OptionalIntegrations.CTNB,OptionalIntegrations.MAGNETIZED,OptionalIntegrations.ENGINEERED,OptionalIntegrations.ADDITIONS))
      h.assertTrue(OptionalIntegrations.installed(id).mayInspect(),"Exact combined installation: "+id);
    for(var p:List.of(from,to))l.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var edge=WingExamples.connect(l,from,to,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
    var station=from.offset(6,0,20);var cars=new FoundationData.Vehicle[1];var addition=new BlockEntity[1];var old=new RideAuthority.Token[1];
    h.runAtTickTime(10,()->{
      CtnbGameTests.paint(l,edge,"BOOST");
      MagnetizedDriveGameTests.mode(l,from,"MAGNET");l.setBlockAndUpdate(from.below(),Blocks.REDSTONE_BLOCK.defaultBlockState());
      var anchor=(CoasterAnchorpointBlockEntity)l.getBlockEntity(from);
      anchor.commitChainLift(Direction.Axis.X,ChainLiftEndRef.anchor(to),new CoasterPathGraphSpans.SpanPath(List.of(new CoasterPathGraphSpans.PathSegment(edge,0,1)),80));
      l.setBlockAndUpdate(from.east(),AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.WEST));
      ((CreativeMotorBlockEntity)l.getBlockEntity(from.east())).generatedSpeed.setValue(64*anchor.getChainLiftTravelSign());
      var motor=from.offset(-6,0,0);l.setBlockAndUpdate(motor,BuiltInRegistries.BLOCK.get(ResourceLocation.parse("coastersengineered:linear_motor_controller")).defaultBlockState());
      var rail=new FoundationData.Rail(from,to,dev.fourthdimension.contract.Family.ARROW,new dev.fourthdimension.physics.RotationProfile(List.of(new dev.fourthdimension.physics.RotationProfile.Key(0,0),new dev.fourthdimension.physics.RotationProfile.Key(80,0))),UUID.randomUUID(),TrackDesign.ARROW.variant,TrackGeometry.fingerprint(l,edge),0,true);
      EngineeredGameTests.configureMotor(h,rail,motor,10,"FORWARD");
      cars[0]=WingVehicle.create(l,WingExamples.sample(l,edge,20));
      l.setBlockAndUpdate(station,StationContent.STATION.get().defaultBlockState());((BoardingStationEntity)l.getBlockEntity(station)).vehicle=cars[0].chassis;
      var ap=from.offset(8,0,20);l.setBlockAndUpdate(ap,BuiltInRegistries.BLOCK.get(ResourceLocation.parse("coasterfins:station")).defaultBlockState());addition[0]=l.getBlockEntity(ap);AdditionsStationGameTests.setSpan(addition[0],from,to,.1,.5);
    });
    h.runAtTickTime(40,()->{var v=cars[0];RideAuthority.claim(l,station,v.chassis,RideAuthority.CTNB);VehicleOperations.restraints(l,v,true);OptionalCalls.call(addition[0],"setDispatch",true,1,0,0);});
    h.runAtTickTime(58,()->{
      var v=cars[0];var state=((AdditionsStationAdapter.Bridge)addition[0]).fourthdimension$stationState();
      h.assertTrue(!state.armed&&state.fault.contains("Another controller")&&state.telemetry.available(),"Actual Additions auto request denied with truthful telemetry under CTNB owner");
      RideAuthority.dispatch(l,station,v.chassis,RideAuthority.token(v));reset(l,v);chain(l,v,edge);
      h.assertTrue(Math.abs(speed(l,v)-.4)<1e-6,"CTNB exactly one impulse with MAG, Engineered and native Chain installed: "+speed(l,v));
      h.assertTrue(motor(l,v,edge)<1e-8,"Actual Engineered callback adds no competing force");
      // Positive control: same configured native motor and body must really produce force.
      v.authority.provider=RideAuthority.ENGINEERED;reset(l,v);double force=motor(l,v,edge);
      h.assertTrue(force>.001,"Configured Engineered positive control actually queues propulsion: "+force);v.authority.provider=RideAuthority.CTNB;
      VehicleOperations.capture(l,v);reset(l,v);old[0]=RideAuthority.token(v);
    });
    h.runAtTickTime(90,()->{var v=cars[0];RideAuthority.selectMagnetized(l,station,v);denied(h,()->RideAuthority.dispatch(l,station,v.chassis,old[0]),"old CTNB lease");});
    h.runAtTickTime(105,()->{
      var v=cars[0];RideAuthority.dispatch(l,station,v.chassis,RideAuthority.token(v));reset(l,v);chain(l,v,edge);
      double acceleration=(double)OptionalCalls.staticCall("net.antopfr.coastersmagnetized.magnet.MagnetBoost","accelerationFor",l,edge);
      h.assertTrue(Math.abs(speed(l,v)-Math.min(12,acceleration)*.05)<1e-6,"MAG exactly one impulse with CTNB material and native sources: "+speed(l,v));
      h.assertTrue(motor(l,v,edge)<1e-8,"Engineered remains fenced after transfer");
      VehicleOperations.capture(l,v);reset(l,v);
    });
    h.runAtTickTime(135,()->{
      var v=cars[0];chain(l,v,edge);h.assertTrue(motor(l,v,edge)<1e-8&&Math.abs(speed(l,v))<1e-8,"Held callbacks cannot launch");
      var state=((AdditionsStationAdapter.Bridge)addition[0]).fourthdimension$stationState();h.assertTrue(!state.armed&&state.telemetry.available(),"Additions cannot steal transferred MAG owner");
      OptionalCalls.call(addition[0],"setDispatch",false,1,0,0);
      System.out.println("FD813_CONFLICT PASS CTNB_MAG_NATIVE_CHAIN=true actualEngineeredCallback=true engineeredPositiveControl=true additionsForeignOwner=true heldTransfer=true staleLeaseRejected=true heldForceZero=true");h.succeed();
    });
  }
  private static void chain(ServerLevel l,FoundationData.Vehicle v,CoasterPathEdge e){var c=SubLevelContainer.getContainer(l);var hit=WingExamples.sample(l,e,20);CoasterCartChainLiftDrive.applySpeedMatch((ServerSubLevel)c.getSubLevel(v.chassis),l,c.physicsSystem(),hit,CoasterPathTrackFrame.trackBasisAtHit(l,hit),.05);}
  private static double motor(ServerLevel l,FoundationData.Vehicle v,CoasterPathEdge e){
    var c=SubLevelContainer.getContainer(l);var hit=WingExamples.sample(l,e,20);var b=(ServerSubLevel)c.getSubLevel(v.chassis);
    var total=b.getOrCreateQueuedForceGroup(dev.ryanhcode.sable.api.physics.force.ForceGroups.PROPULSION.get()).getForceTotal();
    var force=new Vector3d(total.getLocalForce());var torque=new Vector3d(total.getLocalTorque());
    OptionalCalls.staticCall("dev.jwaterfall.coastersengineered.MotorDrive","apply",b,l,c.physicsSystem(),hit,CoasterPathTrackFrame.trackBasisAtHit(l,hit),.05d);
    double delta=total.getLocalForce().distance(force);total.getLocalForce().set(force);total.getLocalTorque().set(torque);return delta;
  }
  private static double speed(ServerLevel l,FoundationData.Vehicle v){var c=SubLevelContainer.getContainer(l);return c.physicsSystem().getPipeline().getLinearVelocity((ServerSubLevel)c.getSubLevel(v.chassis),new Vector3d()).z;}
  private static void reset(ServerLevel l,FoundationData.Vehicle v){var c=SubLevelContainer.getContainer(l);var b=(ServerSubLevel)c.getSubLevel(v.chassis);var p=c.physicsSystem().getPipeline();p.addLinearAndAngularVelocity(b,p.getLinearVelocity(b,new Vector3d()).negate(),new Vector3d());}
  private static void denied(GameTestHelper h,Runnable r,String name){try{r.run();}catch(IllegalArgumentException expected){return;}h.fail("Accepted "+name);}
}
