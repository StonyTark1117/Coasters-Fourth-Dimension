package dev.fourthdimension.gametest;

import dev.fourthdimension.integration.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.station.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartChainLiftDrive;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

@GameTestHolder("fourthdimension_fd812")
@PrefixGameTestTemplate(false)
public final class MagnetizedDriveGameTests {
  @GameTest(template="empty",timeoutTicks=210)
  public static void nativeOptionalForceOwnership(GameTestHelper h){
    var l=h.getLevel();var from=new BlockPos(1600,100,600);var to=from.offset(0,0,80);
    for(int x=99;x<=101;x++)for(int z=36;z<=44;z++)l.setChunkForced(x,z,true);
    for(var p:List.of(from,to))l.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var edge=WingExamples.connect(l,from,to,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
    var station=from.offset(6,0,20);var cars=new FoundationData.Vehicle[1];
    h.runAtTickTime(10,()->{
      h.assertTrue(OptionalIntegrations.installed(OptionalIntegrations.MAGNETIZED).mayInspect(),"Exact Magnetized callback fingerprint");
      cars[0]=WingVehicle.create(l,WingExamples.sample(l,edge,20));
      l.setBlockAndUpdate(station,StationContent.STATION.get().defaultBlockState());((BoardingStationEntity)l.getBlockEntity(station)).vehicle=cars[0].chassis;
      mode(l,from,"MAGNET");l.setBlockAndUpdate(from.below(),Blocks.REDSTONE_BLOCK.defaultBlockState());
    });
    h.runAtTickTime(50,()->{
      var v=cars[0];RideAuthority.claim(l,station,v.chassis,RideAuthority.MAGNETIZED);
      denied(h,()->RideAuthority.dispatch(l,station,v.chassis,RideAuthority.token(v)),"open restraint dispatch");
      var c=SubLevelContainer.getContainer(l);var b=(ServerSubLevel)c.getSubLevel(v.chassis);var hit=WingExamples.sample(l,edge,20);
      var basis=dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.trackBasisAtHit(l,hit);
      double before=c.physicsSystem().getPipeline().getLinearVelocity(b,new Vector3d()).z;
      CoasterCartChainLiftDrive.applySpeedMatch(b,l,c.physicsSystem(),hit,basis,.05);
      h.assertTrue(Math.abs(c.physicsSystem().getPipeline().getLinearVelocity(b,new Vector3d()).z-before)<1e-8,"Real addon callback cannot bypass held/open restraints");
      VehicleOperations.restraints(l,v,true);
    });
    h.runAtTickTime(58,()->{
      var v=cars[0];RideAuthority.dispatch(l,station,v.chassis,RideAuthority.token(v));
      var c=SubLevelContainer.getContainer(l);var b=(ServerSubLevel)c.getSubLevel(v.chassis);var pipeline=c.physicsSystem().getPipeline();
      var hit=WingExamples.sample(l,edge,20);var basis=dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.trackBasisAtHit(l,hit);
      reset(l,b,0);
      double acceleration=(double)OptionalCalls.staticCall("net.antopfr.coastersmagnetized.magnet.MagnetBoost","accelerationFor",l,edge);
      CoasterCartChainLiftDrive.applySpeedMatch(b,l,c.physicsSystem(),hit,basis,.05);
      double actual=pipeline.getLinearVelocity(b,new Vector3d()).z;
      h.assertTrue(Math.abs(actual-Math.min(12,acceleration)*.05)<1e-6,"Exactly one real forward force from stationary start: "+actual);
      reset(l,b,-2);CoasterCartChainLiftDrive.applySpeedMatch(b,l,c.physicsSystem(),hit,basis,.05);
      h.assertTrue(pipeline.getLinearVelocity(b,new Vector3d()).z>-2,"Forward command brakes reverse rollback instead of amplifying it");
      mode(l,from,"BRAKE");reset(l,b,2);CoasterCartChainLiftDrive.applySpeedMatch(b,l,c.physicsSystem(),hit,basis,.05);
      h.assertTrue(pipeline.getLinearVelocity(b,new Vector3d()).z<2,"Actual brake equipment removes longitudinal energy");
      h.assertTrue(EngineeredInterlock.holds(b,l)&&RideAuthority.deniesProvider(l,v,RideAuthority.NATIVE_CHAIN),"Competing providers denied");
      mode(l,from,"MAGNET");reset(l,b,0);VehicleOperations.capture(l,v);RideAuthority.direction(l,v,-1);
    });
    h.runAtTickTime(70,()->{
      var v=cars[0];RideAuthority.dispatch(l,station,v.chassis,RideAuthority.token(v));
      var c=SubLevelContainer.getContainer(l);var b=(ServerSubLevel)c.getSubLevel(v.chassis);var hit=WingExamples.sample(l,edge,20);
      var basis=dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.trackBasisAtHit(l,hit);reset(l,b,0);
      CoasterCartChainLiftDrive.applySpeedMatch(b,l,c.physicsSystem(),hit,basis,.05);
      h.assertTrue(c.physicsSystem().getPipeline().getLinearVelocity(b,new Vector3d()).z<0,"Explicit reverse stationary launch");
      l.setBlockAndUpdate(from.below(),Blocks.AIR.defaultBlockState());
      CoasterCartChainLiftDrive.applySpeedMatch(b,l,c.physicsSystem(),hit,basis,.05);
      h.assertTrue(v.authority.suspended&&v.authority.held,"Power loss latches authority hold");
      l.setBlockAndUpdate(from.below(),Blocks.REDSTONE_BLOCK.defaultBlockState());reset(l,b,0);
      CoasterCartChainLiftDrive.applySpeedMatch(b,l,c.physicsSystem(),hit,basis,.05);
      h.assertTrue(c.physicsSystem().getPipeline().getLinearVelocity(b,new Vector3d()).length()<1e-8,"Restored power cannot restart ride");
      VehicleOperations.capture(l,v);
      var state=RideAuthorityState.read(v.authority.save());h.assertTrue(state.direction==-1,"Direction persists");
      System.out.println("FD812_NATIVE PASS callbackFence=true oneImpulse=true stationaryForward=true stationaryReverse=true rollback=true brake=true powerLossLatch=true powerRestoreDenied=true competitorsDenied=true directionSaved=true");
      h.succeed();
    });
  }
  public static void mode(net.minecraft.server.level.ServerLevel l,BlockPos p,String mode){
    var value=OptionalCalls.staticCall("net.antopfr.coastersmagnetized.magnet.AnchorMode","valueOf",mode);
    OptionalCalls.staticCall("net.antopfr.coastersmagnetized.magnet.MagnetizedAnchors","setMode",l,p,value);
  }
  private static void reset(net.minecraft.server.level.ServerLevel l,ServerSubLevel b,double z){
    var p=SubLevelContainer.getContainer(l).physicsSystem().getPipeline();var velocity=p.getLinearVelocity(b,new Vector3d());
    p.addLinearAndAngularVelocity(b,new Vector3d(velocity).negate().add(0,0,z),new Vector3d());
  }
  private static void denied(GameTestHelper h,Runnable action,String name){try{action.run();}catch(IllegalArgumentException expected){return;}h.fail("Accepted "+name);}
}
