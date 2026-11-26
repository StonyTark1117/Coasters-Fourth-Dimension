package dev.fourthdimension.gametest;

import dev.fourthdimension.moving.*;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

/** Legacy experimental retained records are preserved but cannot infer a missing constraint anchor. */
@GameTestHolder("fourthdimension_fd2001_legacy_anchor")
@PrefixGameTestTemplate(false)
public final class MovingLegacyAnchorGameTests {
  @GameTest(template="empty",timeoutTicks=180)
  public static void legacyMissingAnchorRetainsActualSurvivor(GameTestHelper h){
    var level=h.getLevel();var data=FoundationData.get(level);var origin=new BlockPos(-500,130,-320);
    for(int x=-34;x<=-29;x++)for(int z=-23;z<=-18;z++)level.setChunkForced(x,z,true);
    var s=new MovingAssemblyState(UUID.randomUUID(),origin.west(10),"transfer",WingVehicle.VARIANT,16);
    var pose=new MovingAssemblyState.Pose(origin.getX()+.5,origin.getY()+.5,origin.getZ()+.5,0,0,0,1);
    var dock=new MovingAssemblyState.Dock("a",UUID.randomUUID(),origin.offset(0,0,-8),origin.offset(0,0,8),pose);
    s.docks.put("a",dock);s.source="a";s.destination="a";
    for(var p:List.of(dock.from(),dock.to(),dock.from().offset(0,0,-24),dock.to().offset(0,0,24)))
      level.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    MotorbikeExamples.connect(level,dock.from().offset(0,0,-24),dock.from(),new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
    MotorbikeExamples.connect(level,dock.to(),dock.to().offset(0,0,24),new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
    MovingNativeAdapter.createBed(level,s,pose,Blocks.IRON_BLOCK.defaultBlockState());
    var car=new FoundationData.Vehicle[1];var loaded=new MovingAssemblyState[1];var start=new Vector3d[1];
    h.runAtTickTime(45,()->{
      MovingNativeAdapter.dock(level,s,dock);
      var edge=CoasterPathGraphManager.get(level).edges().stream().filter(e->e.from().equals(dock.from())&&e.to().equals(dock.to())).findFirst().orElseThrow();
      car[0]=WingVehicle.create(level,MotorbikeExamples.sample(level,edge,8));
      car[0].restraints=true;data.vehicles.remove(car[0].chassis); // Disclosed adapter fixture, not normal controls.
    });
    h.runAtTickTime(100,()->{
      s.cars=List.of(car[0].chassis);MovingNativeAdapter.capture(level,s,s.cars);MovingNativeAdapter.disconnect(level,s);
      var saved=s.save(true);for(var tag:saved.getList("bodies",10))((net.minecraft.nbt.CompoundTag)tag).remove("anchor");
      var old=loaded[0]=MovingAssemblyState.read(saved);
      h.assertTrue(old.bodies.equals(s.bodies)&&old.bodyAnchors.isEmpty(),"Legacy identities were not preserved");
      var child=SubLevelContainer.getContainer(level).getSubLevel(car[0].chassis);start[0]=new Vector3d(child.logicalPose().position());
      boolean refused=false;try{MovingNativeAdapter.restore(level,old);}catch(IllegalArgumentException e){System.out.println("FD2001_LEGACY_RESTORE_DIAG "+e);refused=e.getMessage().contains("no saved body anchors");}
      h.assertTrue(refused,"Missing legacy anchors were silently inferred");
      refused=false;try{MovingNativeAdapter.drive(level,old,pose,.05);}catch(IllegalArgumentException e){refused=true;}
      h.assertTrue(refused,"Legacy fault hold permitted motion");
      refused=false;try{MovingNativeAdapter.release(level,old);}catch(IllegalArgumentException e){refused=true;}
      h.assertTrue(refused,"Legacy fault hold permitted release");
    });
    for(int tick=101;tick<=150;tick++)h.runAtTickTime(tick,()->{
      var old=loaded[0];MovingNativeAdapter.hold(level,old,.05);
      h.assertTrue(MovingNativeAdapter.faultHoldReady(level,old,car[0].chassis),"Survivor lost actual independent hold");
      var child=SubLevelContainer.getContainer(level).getSubLevel(car[0].chassis);
      h.assertTrue(child.logicalPose().position().distance(start[0])<.025,"Legacy survivor shifted during fault hold");
    });
    h.runAtTickTime(151,()->{
      h.assertTrue(MovingNativeAdapter.stopped(level,loaded[0]),"Legacy bed did not remain stationary");
      h.assertTrue(loaded[0].bodyAnchors.isEmpty()&&loaded[0].bodies.equals(s.bodies),"Fault hold changed saved identities/anchors");
      System.out.println("FD2001_LEGACY_ANCHOR_PASS body="+car[0].chassis+" actual="+start[0]+" independentHold=true motionDenied=true releaseDenied=true");h.succeed();
    });
  }
}
