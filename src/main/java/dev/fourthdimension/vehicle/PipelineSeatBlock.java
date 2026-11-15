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

/** Four fixed hips at graph Y=-1.70; body, original mesh and mass share these offsets. */
public final class PipelineSeatBlock extends CarrierSeatBlock implements BlockSubLevelCustomCenterOfMass {
  public static final BooleanProperty RIGHT=BooleanProperty.create("right"),REAR=BooleanProperty.create("rear");
  public PipelineSeatBlock(Properties p){super(p,DyeColor.BLUE);registerDefaultState(defaultBlockState().setValue(RIGHT,false).setValue(REAR,false));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){super.createBlockStateDefinition(b);b.add(RIGHT,REAR);}
  public static double hipX(BlockState s){return s.getValue(RIGHT)?.1:.9;}
  public static double hipZ(BlockState s){return s.getValue(REAR)?.75:.25;}
  @Override public Vector3dc getCenterOfMass(BlockGetter l,BlockState s){return new Vector3d(hipX(s),.1125,hipZ(s));}
  @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
    double x=hipX(s)*16,z=hipZ(s)*16;
    var shape=Shapes.or(Block.box(x-6,5,z-6,x+6,7.8,z+6),Block.box(x-6,7.8,z+5,x+6,25,z+7));
    for(int side:new int[]{-1,1}){
      double h=x+side*4;
      shape=Shapes.or(shape,s.getValue(RESTRAINED)?Block.box(h-1,10,z-4,h+1,24,z-2):Block.box(h-1,17,z+7,h+1,29,z+9));
    }
    return shape;
  }
}
