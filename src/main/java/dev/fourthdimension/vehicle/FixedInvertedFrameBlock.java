package dev.fourthdimension.vehicle;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
/** Connected native frame blocks, each rendering its own original mesh section. */
public final class FixedInvertedFrameBlock extends Block {
  public static final IntegerProperty PART=IntegerProperty.create("part",0,9);
  public FixedInvertedFrameBlock(Properties p){super(p);registerDefaultState(defaultBlockState().setValue(PART,0));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(PART);}
  @Override public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,
      net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos ignored,
      net.minecraft.world.phys.shapes.CollisionContext context) {
    var p=FixedInvertedVehicle.FRAME.get(Math.min(state.getValue(PART),8));
    int x=p.getX(),y=p.getY();var shape=net.minecraft.world.phys.shapes.Shapes.empty();
    if(y==-1)shape=net.minecraft.world.phys.shapes.Shapes.or(shape,part(p,Math.max(-2.25,x-.5),-.9,1.25,Math.min(2.25,x+.5),-.75,1.5));
    if(Math.abs(x)==2)shape=net.minecraft.world.phys.shapes.Shapes.or(shape,part(p,x<0?-2.25:2,Math.max(-3.75,y-.1875),1.25,x<0?-2:2.25,Math.min(-.75,y+.8125),1.5));
    return shape;
  }
  private static net.minecraft.world.phys.shapes.VoxelShape part(net.minecraft.core.BlockPos p,
      double x0,double y0,double z0,double x1,double y1,double z1){
    return Block.box((x0-p.getX()+.5)*16,(y0-p.getY()+.1875)*16,(z0-p.getZ()+.5)*16,
        (x1-p.getX()+.5)*16,(y1-p.getY()+.1875)*16,(z1-p.getZ()+.5)*16);
  }
}
