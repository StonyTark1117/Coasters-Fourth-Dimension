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

/** Hip at the block center. Mesh, physical mount and child-local torso loading share this frame. */
public final class NewGenerationFlyingSeatBlock extends CarrierSeatBlock implements BlockSubLevelCustomCenterOfMass {
  public static final BooleanProperty LEGS=BooleanProperty.create("legs");
  public NewGenerationFlyingSeatBlock(Properties p){super(p,DyeColor.PURPLE);registerDefaultState(defaultBlockState().setValue(LEGS,false));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){super.createBlockStateDefinition(b);b.add(LEGS);}
  /** NativeLoading applies rider mass at seat.above(): this offset puts its torso .625 above the hip. */
  @Override public Vector3dc getCenterOfMass(BlockGetter l,BlockState s){return new Vector3d(.5,.125,.5);}
  @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
    var shape=Shapes.or(Block.box(1,5,1,15,8,15),Block.box(1,8,14,15,29,17),
        Block.box(2,-11,-1,14,-8,6),Block.box(6,-8,4,10,5,7));
    for(int x:new int[]{3,13})shape=Shapes.or(shape,s.getValue(RESTRAINED)
        ?Shapes.or(Block.box(x-1,11,1,x+1,27,3),Block.box(x-1,25,3,x+1,28,16))
        :Block.box(x-1,11,16,x+1,30,18));
    shape=Shapes.or(shape,s.getValue(RESTRAINED)?Block.box(2,10,1,14,13,3):Block.box(2,28,16,14,30,18));
    return Shapes.or(shape,s.getValue(LEGS)
        ?Shapes.or(Block.box(2,-6,-2,14,-3,0),Block.box(1,-6,-1,3,4,1),Block.box(13,-6,-1,15,4,1))
        :Shapes.or(Block.box(0,-6,5,2,4,7),Block.box(14,-6,5,16,4,7)));
  }
}
