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

/** Actual native support across a disk-state/cache reload; cold body loading remains a distinct client check. */
@GameTestHolder("fourthdimension_fd2001_restore_support")
@PrefixGameTestTemplate(false)
public final class MovingRestoreSupportGameTests {
  @GameTest(template="empty",timeoutTicks=240)
  public static void uncapturedWingSavedDockRestoresSupport(GameTestHelper h){
    var level=h.getLevel();var origin=new BlockPos(-420,130,-600);var controller=origin.west(14);
    for(int x=-29;x<=-24;x++)for(int z=-40;z<=-35;z++)level.setChunkForced(x,z,true);
    var player=FakePlayerFactory.get(level,new GameProfile(UUID.fromString("fdfdfdfd-0000-0000-0000-000000002008"),"FDMoving"));
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
    var restored=new MovingAssemblyState[1];
    int[] physicsChecks={0};boolean[] unsupported={false};
    java.util.function.Consumer<dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent> observer=e->{
      if(e.getPhysicsSystem().getLevel()!=level||restored[0]==null)return;
      physicsChecks[0]++;
      if(!MovingNativeRestoreSupport.held(level,car[0].chassis)&&MovingNativeAdapter.liveDockEdge(level,restored[0])==null)unsupported[0]=true;
    };
    net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(observer);
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
      var old=state[0];var v=car[0];
      h.assertTrue(Boarding.ready(level,v),"Uncaptured Wing was not ready on the original dock: "+v.fault);
      h.assertTrue(!old.retained()&&old.cars.isEmpty(),"Fixture must stay uncaptured");
      start[0]=new Vector3d(SubLevelContainer.getContainer(level).getSubLevel(v.chassis).logicalPose().position());
      var loaded=restored[0]=MovingAssemblyState.read(old.save(true));
      h.assertTrue(loaded.restored&&!loaded.restoreDock.isEmpty()&&loaded.phase==MovingAssemblyState.Phase.FAULT_HOLD,
          "Disk state did not require measured stationary revalidation");
      FoundationData.get(level).moving.assemblies.put(loaded.id,loaded);
      MovingNativeRestoreSupport.prepareAll(level);
      var expected=new HashSet<UUID>();expected.add(v.chassis);expected.add(old.bed);v.carriers.forEach(c->expected.add(c.body));
      h.assertTrue(MovingNativeRestoreSupport.heldBodies(level,loaded).containsAll(expected),"Bootstrap omitted an actual chassis/carrier/bed");
      expected.forEach(id->h.assertTrue(MovingNativeRestoreSupport.held(level,id),"Bootstrap has no valid physical lock for "+id));
      boolean refused=false;try{MovingNativeRestoreSupport.releaseAfterDock(level,loaded);}catch(IllegalArgumentException unavailable){refused=true;}
      h.assertTrue(refused,"Bootstrap released before a measured authenticated bridge existed");
      discardTransientBinding(level,old.id);
      h.assertTrue(MovingNativeAdapter.liveDockEdge(level,loaded)==null,"Stale native cache survived restart simulation");
      expected.forEach(id->h.assertTrue(MovingNativeRestoreSupport.held(level,id),"Disconnected first interval lacks physical support for "+id));
      System.out.println("FD2001_RESTORE_BOOTSTRAP id="+loaded.id+" chassis="+v.chassis+" held="+expected+" original="+start[0]);
    });
    for(int tick=111;tick<=170;tick++){final int time=tick;h.runAtTickTime(tick,()->{
      var s=restored[0];var v=car[0];var parent=SubLevelContainer.getContainer(level).getSubLevel(v.chassis);
      h.assertTrue(parent!=null,"Uncaptured chassis disappeared");
      h.assertTrue(parent.logicalPose().position().distance(start[0])<.025,"Uncaptured Wing moved during support restoration: "+parent.logicalPose().position());
      h.assertTrue(MovingNativeRestoreSupport.held(level,v.chassis)||MovingNativeAdapter.liveDockEdge(level,s)!=null,
          "An unsupported physics interval occurred after restored state installation");
      if(time==170){
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(observer);
        h.assertTrue(physicsChecks[0]>0&&!unsupported[0],"Pre-physics support observer detected an unsupported interval");
        h.assertTrue(!s.restored&&s.phase==MovingAssemblyState.Phase.DOCKED&&s.fault.isEmpty(),"Stationary restore failed: "+s.phase+" "+s.fault);
        h.assertTrue(MovingNativeAdapter.liveDockEdge(level,s)!=null&&s.locksEngaged,"Restored dock lacks measured native bridge");
        h.assertTrue(MovingNativeRestoreSupport.heldBodies(level,s).isEmpty(),"Bootstrap locks leaked after live bridge authentication");
        h.assertTrue(Boarding.ready(level,v)&&FoundationData.get(level).vehicles.get(v.chassis)==v,"Normal Wing support/identity was not restored: "+v.fault);
        System.out.println("FD2001_RESTORE_PASS id="+s.id+" chassis="+v.chassis+" pose="+s.actualPose+" body="+parent.logicalPose().position()+" bridge=true bootstrapReleased=true physicsChecks="+physicsChecks[0]);h.succeed();
      }
    });}
  }
  /** Disclosed in-process cache-loss simulation; does not claim a cold JVM/body deserialization. */
  @SuppressWarnings("unchecked")
  private static void discardTransientBinding(net.minecraft.server.level.ServerLevel level,UUID id){
    if(!Boolean.getBoolean("fourthdimension.validation"))throw new IllegalStateException("Validation disabled");
    try{
      var field=MovingNativeAdapter.class.getDeclaredField("BINDINGS");field.setAccessible(true);
      var levels=(Map<Object,Map<UUID,Object>>)field.get(null);var binding=levels.get(level).remove(id);
      var remove=MovingNativeAdapter.class.getDeclaredMethod("removeHandles",binding.getClass());remove.setAccessible(true);remove.invoke(null,binding);
      var publish=MovingNativeAdapter.class.getDeclaredMethod("publish",net.minecraft.server.level.ServerLevel.class);publish.setAccessible(true);publish.invoke(null,level);
    }catch(ReflectiveOperationException failure){throw new IllegalStateException(failure);}
  }
}
