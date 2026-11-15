package dev.fourthdimension.gametest;

import com.simibubi.create.AllBlocks;
import dev.fourthdimension.vehicle.*;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import java.util.List;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension_fd1801_clearance")
@PrefixGameTestTemplate(false)
public final class FlyingClearanceGameTests {
 @GameTest(template="empty",timeoutTicks=100)
 public static void flyingWholeEnvelope(GameTestHelper h){
  var level=h.getLevel();var from=new BlockPos(-800,120,400);var to=from.offset(0,0,80);
  for(int x=-51;x<=-49;x++)for(int z=24;z<=31;z++)level.setChunkForced(x,z,true);
  for(var p:List.of(from,to))level.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
  var edge=FlyingExamples.connect(level,from,to,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
  var middle=FlyingExamples.sample(level,edge,40);
  h.assertTrue(FlyingTrack.obstruction(level,middle)==null,"Empty full sweep");
  var upper=BlockPos.containing(middle.point().add(0,.7,0));level.setBlockAndUpdate(upper,Blocks.STONE.defaultBlockState());
  h.assertTrue(upper.equals(FlyingTrack.obstruction(level,middle)),"Upper running-gear obstacle rejected");
  level.removeBlock(upper,false);
  var fork=BlockPos.containing(middle.point().add(2,-2,0));level.setBlockAndUpdate(fork,Blocks.STONE.defaultBlockState());
  h.assertTrue(fork.equals(FlyingTrack.obstruction(level,middle)),"Outer fixed-fork obstacle rejected");level.removeBlock(fork,false);
  var anchor=(CoasterAnchorpointBlockEntity)level.getBlockEntity(from);anchor.setChainLiftShaft(Direction.Axis.X);
  var near=FlyingExamples.sample(level,edge,1);
  level.setBlockAndUpdate(from.east(),AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.WEST));
  h.assertTrue(from.east().equals(FlyingTrack.obstruction(level,near)),"Adjacent motor rejected above seats");
  for(int i=1;i<=2;i++)level.setBlockAndUpdate(from.east(i),AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS,Direction.Axis.X));
  level.setBlockAndUpdate(from.east(3),AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.WEST));
  h.assertTrue(FlyingTrack.obstruction(level,near)==null,"Native anchor and aligned shaft ports permitted with motor outside sweep");
  System.out.println("FD_FLYING_CLEARANCE PASS upperGear=true fixedFork=true adjacentMotorRejected=true remoteMotorAndNativeShafts=true");h.succeed();
 }
}
