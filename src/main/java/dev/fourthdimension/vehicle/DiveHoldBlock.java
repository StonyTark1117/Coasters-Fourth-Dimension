package dev.fourthdimension.vehicle;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
/** Redstone-enabled release controller. Loss of its enable signal retains the physical hold. */
public final class DiveHoldBlock extends BaseEntityBlock {
  public static final MapCodec<DiveHoldBlock> CODEC=simpleCodec(DiveHoldBlock::new);
  public DiveHoldBlock(Properties p){super(p);}
  @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
  @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new DiveHoldEntity(p,s);}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level raw,BlockPos p,Player player,BlockHitResult hit){
    if(!(raw instanceof ServerLevel level))return InteractionResult.SUCCESS;
    if(!player.mayBuild()||!level.mayInteract(player,p))return InteractionResult.FAIL;
    try{
      var c=(DiveHoldEntity)level.getBlockEntity(p);
      if(c.from==null)c.bind();else if(player.isShiftKeyDown())c.changeDwell();
      player.displayClientMessage(Component.literal(c.status()+". Redstone enables release; sneak-use edits empty/stopped dwell; car Resume revalidates interruption."),false);
      return InteractionResult.SUCCESS;
    }catch(IllegalArgumentException failure){player.displayClientMessage(Component.literal(failure.getMessage()),true);return InteractionResult.FAIL;}
  }
}
