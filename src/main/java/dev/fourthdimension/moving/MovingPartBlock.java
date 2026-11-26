package dev.fourthdimension.moving;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.*;

/** Authored structural modules. Their placement and locks are validated by the assembly runtime. */
public final class MovingPartBlock extends Block {
  public enum Kind { BED, OVERHEAD_GANTRY, PIPELINE_BED, PIPELINE_BRACE, GUIDE, PIVOT, LOCK, SOCKET }
  public static final DirectionProperty FACING=BlockStateProperties.FACING;
  public static final BooleanProperty LOCKED=BooleanProperty.create("locked");
  private final Kind kind;
  public MovingPartBlock(Properties properties,Kind kind){
    super(properties);this.kind=kind;
    registerDefaultState(defaultBlockState().setValue(FACING,Direction.NORTH).setValue(LOCKED,false));
  }
  public Kind kind(){return kind;}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING,LOCKED);}
  @Override public BlockState getStateForPlacement(BlockPlaceContext context){
    Direction facing=context.getPlayer()!=null&&context.getPlayer().isShiftKeyDown()?context.getClickedFace():context.getHorizontalDirection().getOpposite();
    return defaultBlockState().setValue(FACING,facing);
  }
  @Override protected BlockState rotate(BlockState state,Rotation rotation){return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));}
  @Override protected BlockState mirror(BlockState state,Mirror mirror){return rotate(state,mirror.getRotation(state.getValue(FACING)));}
  @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){
    VoxelShape shape=switch(kind){
      case BED->Shapes.or(box(0,3,0,4,8,16),box(12,3,0,16,8,16),box(0,3,0,16,6,3),box(0,3,13,16,6,16),box(5,5,5,11,20.25,11),box(3,4,6,13,6,10));
      case OVERHEAD_GANTRY->Shapes.or(box(0,0,5,3,16,11),box(13,0,5,16,16,11),box(0,12,4,16,16,12),box(5,5,5,11,14,11),box(0,0,0,5,3,16),box(11,0,0,16,3,16));
      case PIPELINE_BED->Shapes.or(box(0,3,0,3,10,16),box(13,3,0,16,10,16),box(0,0,6,16,4,10),box(0,8,5,5,12,11),box(11,8,5,16,12,11),box(5,3,5,11,22,11));
      case PIPELINE_BRACE->Shapes.or(box(6,4,0,10,8,16),state.getValue(LOCKED)?box(6,0,6,10,16,10):Shapes.empty());
      case GUIDE->Shapes.or(box(5,0,6,11,16,10),box(2,0,10,14,16,13),box(3,0,3,13,16,6));
      case PIVOT->Shapes.or(box(0,0,2,16,4,14),box(3,3,3,13,12,13),box(1,6,1,15,14,15),box(5,8,0,11,12,16),box(3,13,4,13,15,12));
      case LOCK->Shapes.or(box(1,0,2,15,4,15),box(3,3,6,13,11,15),box(5,5,1,11,9,8),box(6,10,7,10,12,13),state.getValue(LOCKED)?box(6,6,0,10,8,8):Shapes.empty());
      case SOCKET->Shapes.or(box(0,0,3,16,4,16),box(0,3,1,4,12,16),box(12,3,1,16,12,16),box(4,3,7,12,6,16),box(4,4,13,12,11,16));
    };
    return rotateShape(shape,state.getValue(FACING));
  }
  static VoxelShape rotateShape(VoxelShape shape,Direction facing){
    if(facing==Direction.NORTH)return shape;
    VoxelShape[] result={Shapes.empty()};
    shape.forAllBoxes((x1,y1,z1,x2,y2,z2)->{
      double[] min={Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY},max={Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY};
      for(double x:new double[]{x1,x2})for(double y:new double[]{y1,y2})for(double z:new double[]{z1,z2}){
        double[] p=switch(facing){case EAST->new double[]{1-z,y,x};case SOUTH->new double[]{1-x,y,1-z};case WEST->new double[]{z,y,1-x};case UP->new double[]{x,1-z,y};case DOWN->new double[]{x,z,1-y};default->new double[]{x,y,z};};
        for(int i=0;i<3;i++){min[i]=Math.min(min[i],p[i]);max[i]=Math.max(max[i],p[i]);}
      }
      result[0]=Shapes.or(result[0],Shapes.box(min[0],min[1],min[2],max[0],max[1],max[2]));
    });return result[0];
  }
}
