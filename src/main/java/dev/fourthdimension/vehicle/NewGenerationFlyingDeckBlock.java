package dev.fourthdimension.vehicle;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.*;

/** Rotating outer fork. Its two arms sit outside the inner row's constant X extent. */
public final class NewGenerationFlyingDeckBlock extends Block {
  public static final double[][] BOXES={
    {-.125,2.15,-.125,.125,2.80,.125},
    {-1.5,2.15,-.125,1.5,2.30,.125},
    {-1.5,-.125,-.125,-1.25,2.30,.125},{1.25,-.125,-.125,1.5,2.30,.125}
  };
  public static final List<BlockPos> PARTS=parts();
  public static final IntegerProperty PART=IntegerProperty.create("part",0,PARTS.size()-1);
  private static List<BlockPos> parts(){var p=new ArrayList<BlockPos>();for(int x=-2;x<=2;x++)for(int y=-1;y<=3;y++)for(int z=-1;z<=1;z++){var a=new BlockPos(x,y,z);if(!shape(a).isEmpty())p.add(a);}return List.copyOf(p);}
  public NewGenerationFlyingDeckBlock(Properties p){super(p);registerDefaultState(defaultBlockState().setValue(PART,0));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(PART);}
  @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return shape(PARTS.get(s.getValue(PART)));}
  public static VoxelShape shape(BlockPos p){double x=p.getX()-.5,y=p.getY()-.5,z=p.getZ()-.5;var result=Shapes.empty();for(var box:BOXES){double a=Math.max(x,box[0]),b=Math.max(y,box[1]),c=Math.max(z,box[2]),d=Math.min(x+1,box[3]),e=Math.min(y+1,box[4]),f=Math.min(z+1,box[5]);if(a<d&&b<e&&c<f)result=Shapes.or(result,Block.box((a-x)*16,(b-y)*16,(c-z)*16,(d-x)*16,(e-y)*16,(f-z)*16));}return result;}
}
