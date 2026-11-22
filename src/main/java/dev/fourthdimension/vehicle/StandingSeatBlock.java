package dev.fourthdimension.vehicle;
import dev.ryanhcode.sable.api.block.BlockSubLevelCustomCenterOfMass;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.*;
import org.joml.*;
/** A standing footplate and rear support, not a seated cushion. */
public final class StandingSeatBlock extends CarrierSeatBlock implements BlockSubLevelCustomCenterOfMass {
  public static final BooleanProperty RIGHT=BooleanProperty.create("right"),SURF=BooleanProperty.create("surf");
  public static final double FEET_Y=.4375;
  public StandingSeatBlock(Properties p){super(p,DyeColor.CYAN);registerDefaultState(defaultBlockState().setValue(RIGHT,false).setValue(SURF,false));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){super.createBlockStateDefinition(b);b.add(RIGHT,SURF);}
  public static double footX(BlockState s){return s.getValue(RIGHT)?.15:.85;}
  @Override public Vector3dc getCenterOfMass(BlockGetter l,BlockState s){return new Vector3d(footX(s),.3375,.5);}
  @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){double x=footX(s)*16;return Shapes.or(Block.box(x-6.8,5.4,.8,x+6.8,7,15.2),Block.box(x-5,7,13.6,x+5,34,16),s.getValue(RESTRAINED)?Block.box(x-5,20,2,x+5,32,4):Block.box(x-5,33,15,x+5,39,18));}
}
