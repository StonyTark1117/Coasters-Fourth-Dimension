package dev.fourthdimension.vehicle;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.*;

/** Original fixed forks, cut at native block boundaries so rendered and selectable geometry agree. */
public final class FlyingFrameBlock extends Block {
  public static final double[][] BOXES={
    {-.125,-.55,-2.375,.125,-.40,2.375},
    {-.125,-2.625,-2.375,.125,-.40,-2.125},{-.125,-2.625,2.125,.125,-.40,2.375},
    {-2.25,-2.625,-2.375,2.25,-2.375,-2.125},{-2.25,-2.625,2.125,2.25,-2.375,2.375},
    {-2.25,-2.625,-2.375,-2.1875,-2.375,2.375},{2.1875,-2.625,-2.375,2.25,-2.375,2.375}
  };
  public static final List<BlockPos> PARTS=parts();
  public static final IntegerProperty PART=IntegerProperty.create("part",0,PARTS.size()-1);
  private static List<BlockPos> parts(){
    var p=new ArrayList<BlockPos>();
    for(int x=-2;x<=2;x++)for(int y=-3;y<=-1;y++)for(int z=-2;z<=2;z++){
      var pos=new BlockPos(x,y,z);if(!pos.equals(new BlockPos(0,-1,0))&&!shape(pos).isEmpty())p.add(pos);
    }return List.copyOf(p);
  }
  public FlyingFrameBlock(Properties p){super(p);registerDefaultState(defaultBlockState().setValue(PART,0));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(PART);}
  @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return shape(PARTS.get(s.getValue(PART)));}
  public static VoxelShape controllerShape(){return shape(new BlockPos(0,-1,0));}
  public static VoxelShape shape(BlockPos p){
    double x=p.getX()-.5,y=p.getY()-.1875,z=p.getZ()-.5;
    var result=Shapes.empty();for(var box:BOXES){
      double a=Math.max(x,box[0]),b=Math.max(y,box[1]),c=Math.max(z,box[2]);
      double d=Math.min(x+1,box[3]),e=Math.min(y+1,box[4]),f=Math.min(z+1,box[5]);
      if(a<d&&b<e&&c<f)result=Shapes.or(result,Block.box((a-x)*16,(b-y)*16,(c-z)*16,(d-x)*16,(e-y)*16,(f-z)*16));
    }return result;
  }
}
