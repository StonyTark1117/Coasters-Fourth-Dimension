package dev.fourthdimension.gametest;

import com.simibubi.create.content.trains.track.TrackMaterial;
import dev.fourthdimension.integration.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.station.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartChainLiftDrive;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

@GameTestHolder("fourthdimension_fd811")
@PrefixGameTestTemplate(false)
public final class CtnbGameTests {
  @GameTest(template="empty",timeoutTicks=440)
  public static void selectedNativeControlsAndMaterials(GameTestHelper h){
    var l=h.getLevel();var from=new BlockPos(1600,100,600);var to=from.offset(0,0,80);
    for(int x=99;x<=101;x++)for(int z=36;z<=47;z++)l.setChunkForced(x,z,true);
    for(var p:List.of(from,to))l.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var edge=WingExamples.connect(l,from,to,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
    var station=from.offset(6,0,20);var cars=new FoundationData.Vehicle[1];
    h.runAtTickTime(10,()->{
      h.assertTrue(OptionalIntegrations.installed(OptionalIntegrations.CTNB).mayInspect(),"Exact CTNB code/geometry fingerprint");
      paint(l,edge,"LAUNCH");dial(l,from,4);cars[0]=WingVehicle.create(l,WingExamples.sample(l,edge,20));
      l.setBlockAndUpdate(station,StationContent.STATION.get().defaultBlockState());((BoardingStationEntity)l.getBlockEntity(station)).vehicle=cars[0].chassis;
      l.setBlockAndUpdate(from.below(),Blocks.REDSTONE_BLOCK.defaultBlockState());
      profiles(h,from.offset(0,0,100));
    });
    h.runAtTickTime(40,()->{
      var v=cars[0];RideAuthority.claim(l,station,v.chassis,RideAuthority.CTNB);
      denied(h,()->RideAuthority.dispatch(l,station,v.chassis,RideAuthority.token(v)),"open restraint dispatch");
      reset(l,v,0);drive(l,v,edge);h.assertTrue(Math.abs(speed(l,v))<1e-8,"Held actual Chain callback has no CTNB force");
      h.assertTrue(!(boolean)OptionalCalls.staticCall("dev.notzyvex.coasters_extras.track.TrackRepaint","repaint",l,from,to,material("BRAKE")),"Occupied geometry cannot repaint");
      VehicleOperations.restraints(l,v,true);
    });
    h.runAtTickTime(50,()->{
      var v=cars[0];RideAuthority.dispatch(l,station,v.chassis,RideAuthority.token(v));reset(l,v,0);drive(l,v,edge);
      h.assertTrue(Math.abs(speed(l,v)-.4)<1e-6,"One bounded native CTNB impulse, actual="+speed(l,v));
    });
    h.runAtTickTime(75,()->{
      var v=cars[0];h.assertTrue(speed(l,v)>.3&&v.fault.isEmpty(),"Actual CTNB travel through native physics: "+v.fault);
      VehicleOperations.capture(l,v);
    });
    h.runAtTickTime(130,()->{
      var v=cars[0];RideAuthority.direction(l,v,-1);RideAuthority.dispatch(l,station,v.chassis,RideAuthority.token(v));reset(l,v,0);drive(l,v,edge);
      h.assertTrue(speed(l,v)<0,"Explicit stopped reverse launch");
      paint(l,edge,"POWERED_BOOST");l.setBlockAndUpdate(from.below(),Blocks.AIR.defaultBlockState());drive(l,v,edge);
      h.assertTrue(v.authority.suspended&&v.authority.held,"Power loss latches hold");
      l.setBlockAndUpdate(from.below(),Blocks.REDSTONE_BLOCK.defaultBlockState());reset(l,v,0);drive(l,v,edge);h.assertTrue(Math.abs(speed(l,v))<1e-8,"Power restoration does not restart");VehicleOperations.capture(l,v);
    });
    h.runAtTickTime(155,()->{
      var v=cars[0];RideAuthority.resume(l,station,v.chassis,RideAuthority.token(v));RideAuthority.direction(l,v,1);
    });
    h.runAtTickTime(165,()->{
      var v=cars[0];RideAuthority.dispatch(l,station,v.chassis,RideAuthority.token(v));
      paint(l,edge,"STATION");dial(l,from,1);l.setBlockAndUpdate(from.below(),Blocks.AIR.defaultBlockState());v.ctnb.departing=false;drive(l,v,edge);
      h.assertTrue(v.stopping&&v.authority.held&&v.ctnb.remaining>0,"Native station captures and starts real dial dwell");
      denied(h,()->RideAuthority.dispatch(l,station,v.chassis,RideAuthority.token(v)),"early station release");
      var saved=CtnbState.read(v.ctnb.save());h.assertTrue(saved.remaining==v.ctnb.remaining&&!saved.departing,"Saved dwell has no automatic departure lease");
    });
    h.runAtTickTime(345,()->{
      var v=cars[0];h.assertTrue(v.ctnb.remaining==0&&v.stopping&&v.authority.held,"Dwell expiration remains held for explicit Dispatch: "+v.ctnb.remaining);
      l.setBlockAndUpdate(from.below(),Blocks.REDSTONE_BLOCK.defaultBlockState());denied(h,()->RideAuthority.dispatch(l,station,v.chassis,RideAuthority.token(v)),"station signal hold");
      l.setBlockAndUpdate(from.below(),Blocks.AIR.defaultBlockState());RideAuthority.dispatch(l,station,v.chassis,RideAuthority.token(v));
      paint(l,edge,"SENSOR");drive(l,v,edge);h.assertTrue((boolean)OptionalCalls.staticCall("dev.notzyvex.coasters_extras.net.SensorTracker","occupied",from,to,l.getGameTime()),"Actual CTNB sensor gets managed occupancy");
      paint(l,edge,"REVERSE");dial(l,from,4);l.setBlockAndUpdate(from.below(),Blocks.REDSTONE_BLOCK.defaultBlockState());v.ctnb.departing=false;drive(l,v,edge);h.assertTrue(v.stopping&&v.authority.held,"Reverse section captures before changing direction");
    });
    h.runAtTickTime(395,()->{
      var v=cars[0];RideAuthority.direction(l,v,-1);RideAuthority.dispatch(l,station,v.chassis,RideAuthority.token(v));reset(l,v,0);drive(l,v,edge);h.assertTrue(speed(l,v)<0,"Validated reverse section release");
      var b=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.chassis);
      paint(l,edge,"BOBSLED");h.assertTrue(!EligibilityHooks.allows(b,l,WingExamples.sample(l,edge,20)),"Unqualified angular provider rejected");paint(l,edge,"BRAKE");
      reset(l,v,12);drive(l,v,edge);h.assertTrue(speed(l,v)<12&&speed(l,v)>=11.79999,"Braking is bounded and never flips velocity");
      VehicleOperations.capture(l,v);
      h.assertTrue(EngineeredInterlock.holds(b,l)&&RideAuthority.deniesProvider(l,v,RideAuthority.NATIVE_CHAIN),"Conflicting forces denied");
      System.out.println("FD811_NATIVE PASS measuredMaterials=true preserveProfile=true occupiedRepaintDenied=true heldForceZero=true singleImpulse=true actualTravel=true reverse=true stationDwellAndHold=true sensor=true powerLossLatch=true unsafeRejected=true");h.succeed();
    });
  }
  private static void dial(ServerLevel l,BlockPos p,int value){
    try{var type=(com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType<?>)Class.forName("dev.notzyvex.coasters_extras.StationBoostBehaviour").getField("TYPE").get(null);
      var b=com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour.get(l,p,type);if(b==null)throw new IllegalArgumentException("CTNB dial missing");OptionalCalls.call(b,"setValue",value);
    }catch(ReflectiveOperationException e){throw new IllegalArgumentException(e);}
  }
  public static TrackMaterial material(String name){try{return (TrackMaterial)Class.forName("dev.notzyvex.coasters_extras.track.ModTrackMaterials").getField(name).get(null);}catch(ReflectiveOperationException e){throw new IllegalArgumentException(e);}}
  /** Fixture-only material changes isolate behavior without claiming player construction. */
  public static void paint(ServerLevel l,CoasterPathEdge edge,String name){for(var p:List.of(edge.from(),edge.to())){var a=(CoasterAnchorpointBlockEntity)l.getBlockEntity(p);a.getAnchorPeerCurvesView().get(p.equals(edge.from())?edge.to():edge.from()).setMaterial(material(name));a.setChanged();}edge.bezier().setMaterial(material(name));}
  private static void drive(ServerLevel l,FoundationData.Vehicle v,CoasterPathEdge e){var c=SubLevelContainer.getContainer(l);var b=(ServerSubLevel)c.getSubLevel(v.chassis);var hit=WingExamples.sample(l,e,20);CoasterCartChainLiftDrive.applySpeedMatch(b,l,c.physicsSystem(),hit,CoasterPathTrackFrame.trackBasisAtHit(l,hit),.05);}
  private static double speed(ServerLevel l,FoundationData.Vehicle v){var c=SubLevelContainer.getContainer(l);return c.physicsSystem().getPipeline().getLinearVelocity((ServerSubLevel)c.getSubLevel(v.chassis),new Vector3d()).z;}
  private static void reset(ServerLevel l,FoundationData.Vehicle v,double z){var c=SubLevelContainer.getContainer(l);var b=(ServerSubLevel)c.getSubLevel(v.chassis);var p=c.physicsSystem().getPipeline();p.addLinearAndAngularVelocity(b,p.getLinearVelocity(b,new Vector3d()).negate().add(0,0,z),new Vector3d());}
  private static void denied(GameTestHelper h,Runnable action,String name){try{action.run();}catch(IllegalArgumentException expected){return;}h.fail("Accepted "+name);}
  private static void profiles(GameTestHelper h,BlockPos from){
    var l=h.getLevel();var to=from.offset(0,0,40);for(var p:List.of(from,to))l.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var e=WingExamples.connect(l,from,to,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
    var profile=new dev.fourthdimension.physics.RotationProfile(List.of(new dev.fourthdimension.physics.RotationProfile.Key(0,0),new dev.fourthdimension.physics.RotationProfile.Key(40,Math.PI*2)));
    var r=new FoundationData.Rail(from,to,dev.fourthdimension.contract.Family.ARROW,profile,UUID.randomUUID(),TrackDesign.ARROW.variant,TrackGeometry.fingerprint(l,e),0,true);FoundationData.get(l).rails.add(r);TrackPlacement.captureOriginalCurve(r,e.bezier());
    h.assertTrue((boolean)OptionalCalls.staticCall("dev.notzyvex.coasters_extras.track.TrackRepaint","repaint",l,from,to,material("BOOST")),"Actual CTNB material tool hook accepts empty certified Arrow");
    h.assertTrue(r.profile==profile&&r.valid&&r.geometryHash==TrackGeometry.fingerprint(l,e)&&r.family==dev.fourthdimension.contract.Family.ARROW,"Repaint preserves exact profile, family and geometry");
    System.out.println("FD811_REPAINT directGuard="+CtnbTracks.mayRepaint(l,from,to,material("BOBSLED"))+" rails="+FoundationData.get(l).rails.size()+" revision="+r.revision+" material="+material("BOBSLED").id);
    try{System.out.println("FD811_REPAINT methods="+java.util.Arrays.toString(Class.forName("dev.notzyvex.coasters_extras.track.TrackRepaint").getDeclaredMethods()));}catch(ClassNotFoundException e1){throw new IllegalArgumentException(e1);}
    h.assertTrue(!(boolean)OptionalCalls.staticCall("dev.notzyvex.coasters_extras.track.TrackRepaint","repaint",l,from,to,material("BOBSLED")),"Unknown geometry/force provider rejected on original sections");
  }
}
