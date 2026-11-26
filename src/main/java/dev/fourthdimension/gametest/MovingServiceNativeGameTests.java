package dev.fourthdimension.gametest;

import dev.fourthdimension.moving.*;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

/** Native released-service fault forest. Fixture setup is not normal route admission. */
@GameTestHolder("fourthdimension_fd2001_service")
@PrefixGameTestTemplate(false)
public final class MovingServiceNativeGameTests {
  @GameTest(template="empty",timeoutTicks=230)
  public static void missingIntermediateDeckRetainsSurvivingNativeRoots(GameTestHelper h){
    var l=h.getLevel();var data=FoundationData.get(l);var container=SubLevelContainer.getContainer(l);
    var origin=new BlockPos(-1850,150,-1800);
    for(int x=(origin.getX()>>4)-1;x<=(origin.getX()>>4)+1;x++)for(int z=(origin.getZ()>>4)-2;z<=(origin.getZ()>>4)+2;z++)l.setChunkForced(x,z,true);
    var s=new MovingAssemblyState(UUID.randomUUID(),origin.west(12),"transfer",NewGenerationFlyingVehicle.VARIANT,16);s.configuration="new_generation_flying";
    var pose=new MovingAssemblyState.Pose(origin.getX()+.5,origin.getY()+.5,origin.getZ()+.5,0,0,0,1);
    var dock=new MovingAssemblyState.Dock("a",UUID.randomUUID(),origin.offset(0,0,-8),origin.offset(0,0,8),pose);
    s.docks.put("a",dock);s.source="a";s.destination="a";
    for(var p:List.of(dock.from(),dock.to()))l.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var parts=new ArrayList<MovingNativeAdapter.BlockPlacement>();
    for(int z=-8;z<=8;z++)for(int x=-1;x<=1;x++)parts.add(new MovingNativeAdapter.BlockPlacement(new BlockPos(x,1,z),Blocks.IRON_BLOCK.defaultBlockState()));
    MovingNativeAdapter.createBed(l,s,pose,parts);
    var car=new FoundationData.Vehicle[1];var initial=new LinkedHashMap<UUID,Vector3d>();
    h.runAtTickTime(45,()->{
      MovingNativeAdapter.dock(l,s,dock);
      var edge=CoasterPathGraphManager.get(l).edges().stream().filter(e->e.from().equals(dock.from())&&e.to().equals(dock.to())).findFirst().orElseThrow();
      double length=TrackGeometry.table(l,edge).length();
      var r=new FoundationData.Rail(edge.from(),edge.to(),TrackDesign.NEW_GENERATION_FLYING.family,new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(length,0))),UUID.randomUUID(),NewGenerationFlyingVehicle.VARIANT,TrackGeometry.fingerprint(l,edge),0,true);
      data.rails.add(r);TrackPlacement.captureOriginalCurve(r,edge.bezier());
      car[0]=NewGenerationFlyingVehicle.create(l,MotorbikeExamples.sample(l,edge,8));car[0].restraints=true;
    });
    // Disclosed gravity/velocity braking isolates service loss from the absent full NG route.
    for(int tick=46;tick<=99;tick++)h.runAtTickTime(tick,()->{
      var v=car[0];for(var id:List.of(v.chassis,v.yaw.deck,v.carriers.getFirst().body))container.physicsSystem().getPipeline().resetVelocity(body(h,id));
    });
    h.runAtTickTime(100,()->{
      var v=car[0];boolean emptyDenied=false;try{MovingAdmission.requirePhysicallyEmpty(l,s);}catch(IllegalArgumentException expected){emptyDenied=true;}
      h.assertTrue(emptyDenied&&s.cars.isEmpty()&&!s.retained(),"Uncaptured native car allowed empty return");
      s.cars=List.of(v.chassis);var ids=List.of(v.chassis,v.yaw.deck,v.carriers.getFirst().body);
      for(var id:ids)container.physicsSystem().getPipeline().resetVelocity(body(h,id));
      container.physicsSystem().getPipeline().resetVelocity(body(h,s.bed));
      MovingNativeAdapter.capture(l,s,ids);
      MovingServiceRuntime.prepare(l,s,dock);
      h.assertTrue(s.service.cars.get(v.chassis).holds.size()==2,"NG service omitted chassis→deck→seat graph");
      var disk=MovingAssemblyState.read(s.save(true));
      h.assertTrue(!disk.service.cars.get(v.chassis).dispatched&&disk.service.cars.get(v.chassis).holds.equals(s.service.cars.get(v.chassis).holds),"Disk save lost exact local frames or permitted dispatch");
      MovingNativeAdapter.release(l,s);s.released=true;s.phase=MovingAssemblyState.Phase.FAULT_HOLD;
      data.moving.assemblies.put(s.id,s);
      container.removeSubLevel(body(h,v.yaw.deck),SubLevelRemovalReason.REMOVED);
      h.assertTrue(data.vehicles.get(v.chassis)==v,"Service missing-deck lifecycle discarded surviving car");
      System.out.println("FD2001_SERVICE_DECK_REMOVED chassis="+v.chassis+" missing="+v.yaw.deck+" seat="+v.carriers.getFirst().body);
    });
    for(int tick=101;tick<=210;tick++)h.runAtTickTime(tick,()->MovingServiceRuntime.tickFaults(l,s,.05));
    h.runAtTickTime(180,()->{
      var v=car[0];h.assertTrue(MovingServiceRuntime.faultHoldReady(l,v),"Surviving chassis/seat roots lack native world holds");
      h.assertTrue(MovingServiceRuntime.stopped(l,v),"Surviving roots did not stop");
      for(var id:List.of(v.chassis,v.carriers.getFirst().body))initial.put(id,new Vector3d(body(h,id).logicalPose().position()));
    });
    h.runAtTickTime(210,()->{
      var v=car[0];h.assertTrue(container.getSubLevel(v.yaw.deck)==null,"Missing deck was recreated");
      h.assertTrue(data.vehicles.get(v.chassis)==v&&s.service.cars.containsKey(v.chassis),"Fault lost service identity");
      h.assertTrue(MovingServiceRuntime.faultHoldReady(l,v)&&MovingServiceRuntime.stopped(l,v),"Native survivor fault retention was not sustained");
      for(var e:initial.entrySet())h.assertTrue(body(h,e.getKey()).logicalPose().position().distance(e.getValue())<.025,"Surviving disconnected root drifted");
      boolean refused=false;try{MovingServiceRuntime.validateDispatch(l,v);}catch(IllegalArgumentException expected){refused=true;}
      h.assertTrue(refused&&!s.service.cars.get(v.chassis).dispatched,"Incomplete NG graph allowed dispatch");
      h.assertTrue(s.service.cars.get(v.chassis).holds.stream().anyMatch(x->x.child().equals(v.yaw.deck)),"Missing identity dropped from saved frame graph");
      System.out.println("FD2001_SERVICE_SURVIVORS_PASS chassis="+v.chassis+" missing="+v.yaw.deck+" surviving="+initial.keySet()+" nativeHeld=true drift<.025 dispatchDenied=true");h.succeed();
    });
  }
  private static ServerSubLevel body(GameTestHelper h,UUID id){var b=SubLevelContainer.getContainer(h.getLevel()).getSubLevel(id);h.assertTrue(b instanceof ServerSubLevel&&!b.isRemoved(),"Expected native service body missing: "+id);return (ServerSubLevel)b;}
}
