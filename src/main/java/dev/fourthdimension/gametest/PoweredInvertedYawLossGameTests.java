package dev.fourthdimension.gametest;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.integration.RideAuthority;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;
import java.util.UUID;
@GameTestHolder("fourthdimension_fd1901_loss")
@PrefixGameTestTemplate(false)
public final class PoweredInvertedYawLossGameTests {
  @GameTest(template="empty",timeoutTicks=650)
  public static void suppliedPowerAndControllerLossNeverAutoResume(GameTestHelper h){
    var l=h.getLevel();var e=new PoweredInvertedYawExamples.Example[1];var angle=new double[1];var sequence=new long[1];var origin=new BlockPos(100,100,100);
    h.runAtTickTime(1,()->{for(int x=5;x<=11;x++)for(int z=3;z<=18;z++)l.setChunkForced(x,z,true);e[0]=PoweredInvertedYawExamples.create(l,origin,1,UUID.randomUUID());});
    h.runAtTickTime(50,()->PoweredInvertedYawExamples.bind(l,e[0]));
    h.runAtTickTime(80,()->{VehicleOperations.restraints(l,e[0].cars().getFirst(),true);VehicleOperations.dispatch(l,e[0].cars().getFirst());});
    h.runAtTickTime(170,()->{var v=e[0].cars().getFirst();h.assertTrue(v.fault.isEmpty()&&Math.abs(v.carriers.getFirst().angle)>.1,"Yaw/travel active before actual power removal");angle[0]=v.carriers.getFirst().angle;l.setBlockAndUpdate(origin.east(5),Blocks.AIR.defaultBlockState());});
    h.runAtTickTime(220,()->{var v=e[0].cars().getFirst();var body=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.chassis);h.assertTrue(v.poweredYaw.resumeRequired&&v.restraints&&RideAuthority.holds(l,v)&&!v.fault.isEmpty(),"Power loss retains authority/restraints");h.assertTrue(SubLevelContainer.getContainer(l).physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d()).length()<=.05,"Power loss stops native travel");h.assertTrue(Math.abs(v.carriers.getFirst().angle)>.1,"Interrupted yaw is retained, not zeroed");
      var restored=PoweredInvertedYawState.read(v.poweredYaw.save());h.assertTrue(restored.resumeRequired&&restored.reference==v.poweredYaw.reference&&restored.sequence==v.poweredYaw.sequence,"Interrupted reference/event/hold state survives NBT");sequence[0]=e[0].governor().eventSequence;
      l.setBlockAndUpdate(origin.east(5),AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.WEST));((CreativeMotorBlockEntity)l.getBlockEntity(origin.east(5))).generatedSpeed.setValue(128);
    });
    h.runAtTickTime(260,()->{var v=e[0].cars().getFirst();h.assertTrue(v.stopping&&v.poweredYaw.resumeRequired&&RideAuthority.holds(l,v)&&e[0].governor().outputRpm()==0,"Restored power does not launch");h.assertTrue(e[0].governor().eventSequence==sequence[0],"Power restore does not replay scene outputs");PoweredInvertedYawRuntime.resume(l,v);});
    h.runAtTickTime(320,()->{var v=e[0].cars().getFirst();h.assertTrue(!v.stopping&&v.fault.isEmpty(),"Explicit recovery resumed only after revalidation");PoweredInvertedYawRuntime.capture(l,v);});
    h.runAtTickTime(380,()->{var v=e[0].cars().getFirst();
      // Reproduce the persisted history left by an earlier completed Show.
      v.poweredYaw.interruptedPhase=PoweredInvertedYawState.Phase.HELD_DWELL;
      v.poweredYaw.interruptedMode=YawState.Mode.SHOW_STOP;
      v.maintenance=true;
      PoweredInvertedYawRuntime.direction(l,v,-1);PoweredInvertedYawRuntime.resume(l,v);
    });
    h.runAtTickTime(440,()->{var v=e[0].cars().getFirst();h.assertTrue(!v.stopping&&!v.maintenance&&v.fault.isEmpty()&&v.poweredYaw.phase==PoweredInvertedYawState.Phase.RUN&&v.poweredYaw.direction==-1,"Later retained recovery ignores completed Show history and releases maintenance");l.setBlockAndUpdate(e[0].governor().getBlockPos(),Blocks.AIR.defaultBlockState());});
    h.runAtTickTime(510,()->{var v=e[0].cars().getFirst();var body=(ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.chassis);h.assertTrue(v.stopping&&v.restraints&&v.poweredYaw.resumeRequired&&RideAuthority.holds(l,v),"Removed controller retains complete hold");h.assertTrue(SubLevelContainer.getContainer(l).physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d()).length()<=.05,"Controller loss stops native travel");h.assertTrue(!Boarding.ready(l,v),"No unload outside qualified captured station");System.out.println("FD_POWERED_LOSS PASS actualInputRemoved=true restorationNoLaunch=true explicitResume=true actualControllerRemoved=true retainedYaw="+v.carriers.getFirst().angle);h.succeed();});
  }
}
