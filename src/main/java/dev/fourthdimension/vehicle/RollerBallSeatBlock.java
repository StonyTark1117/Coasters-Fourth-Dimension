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

/** Fractional hip offsets produce the declared ±3.25 spacing on one native body. */
public final class RollerBallSeatBlock extends CarrierSeatBlock implements BlockSubLevelCustomCenterOfMass {
  public static final BooleanProperty LEFT=BooleanProperty.create("left");
  public RollerBallSeatBlock(Properties p){super(p,DyeColor.LIME);registerDefaultState(defaultBlockState().setValue(LEFT,false));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){super.createBlockStateDefinition(b);b.add(LEFT);}
  public static double hipX(BlockState s){return s.getValue(LEFT)?1.25:.75;}
  @Override public Vector3dc getCenterOfMass(BlockGetter l,BlockState s){return new Vector3d(hipX(s),.125,1);}
  @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
    double x=(hipX(s)-.5)*16;
    boolean south=s.getValue(FACING)==net.minecraft.core.Direction.SOUTH;
    double[][] parts={{1,5,1,15,8,15},{1,8,13,15,27,16},{1,-10,0,15,-7,9},
      {6,-7,5,10,5,8},{6,8,10,10,24,13},{6,22,10,10,26,24}};
    var boxes=new java.util.ArrayList<double[]>(java.util.Arrays.asList(parts));
    if(s.getValue(RESTRAINED))boxes.addAll(java.util.List.of(
      new double[]{2,11,1,4,25,3},new double[]{12,11,1,14,25,3},new double[]{2,24,3,4,27,15},
      new double[]{12,24,3,14,27,15},new double[]{2,10,1,14,13,3}));
    else boxes.addAll(java.util.List.of(new double[]{2,11,16,4,28,18},new double[]{12,11,16,14,28,18},new double[]{2,26,16,14,28,18}));
    VoxelShape shape=Shapes.empty();
    for(var b:boxes)shape=Shapes.or(shape,box(x+b[0],b[1],8+(south?16-b[5]:b[2]),x+b[3],b[4],8+(south?16-b[2]:b[5])));
    return shape;

  }
}
