package dev.fourthdimension.moving;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Front panel faces the operator; only the stationary rear face accepts kinetic power. */
public final class MovingControllerBlock extends DirectionalKineticBlock implements IBE<MovingControllerEntity> {
  private final MovingMechanism mechanism;
  public MovingControllerBlock(Properties properties,MovingMechanism mechanism){super(properties);this.mechanism=mechanism;}
  public MovingMechanism mechanism(){return mechanism;}
  @Override public Direction.Axis getRotationAxis(BlockState state){return state.getValue(FACING).getAxis();}
  @Override public boolean hasShaftTowards(LevelReader level,BlockPos pos,BlockState state,Direction face){return face==state.getValue(FACING).getOpposite();}
  @Override public Class<MovingControllerEntity> getBlockEntityClass(){return MovingControllerEntity.class;}
  @Override public BlockEntityType<? extends MovingControllerEntity> getBlockEntityType(){return MovingContent.CONTROLLER_ENTITY.get();}
  @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
    if(player instanceof ServerPlayer server)MovingNetwork.open(server,pos);
    return InteractionResult.sidedSuccess(level.isClientSide);
  }
}
