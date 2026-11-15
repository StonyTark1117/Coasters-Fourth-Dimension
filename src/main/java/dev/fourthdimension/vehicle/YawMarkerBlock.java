package dev.fourthdimension.vehicle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class YawMarkerBlock extends BaseEntityBlock {
  public static final MapCodec<YawMarkerBlock> CODEC=simpleCodec(YawMarkerBlock::new);
  public YawMarkerBlock(Properties p){super(p);}
  @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
  @Override public RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
  @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState s,net.minecraft.world.level.BlockGetter l,BlockPos p,net.minecraft.world.phys.shapes.CollisionContext c){
    return net.minecraft.world.phys.shapes.Shapes.or(Block.box(1,0,1,15,3,15),Block.box(6,3,6,10,12,10),Block.box(3,12,3,13,15,13),Block.box(7,15,7,9,16,9));
  }
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new YawMarkerEntity(p,s);}
  @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,LivingEntity entity,ItemStack stack){super.setPlacedBy(l,p,s,entity,stack);if(!l.isClientSide&&entity!=null&&l.getBlockEntity(p) instanceof YawMarkerEntity m){m.owner=entity.getUUID();m.setChanged();l.sendBlockUpdated(p,s,s,2);}}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){if(!l.isClientSide&&l.getBlockEntity(p) instanceof YawMarkerEntity m)player.displayClientMessage(Component.literal("Yaw marker "+p.toShortString()+" | "+m.id+" | projection uses chassis yaw plane"),false);return InteractionResult.SUCCESS;}
}
