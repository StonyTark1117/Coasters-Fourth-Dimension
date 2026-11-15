package dev.fourthdimension.vehicle;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import dev.fourthdimension.runtime.FoundationData;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.joml.Vector3d;

/** Directional stress-supplied transmission: rear input, arrow-facing governed output. */
public final class PoweredInvertedYawGovernorBlock extends DirectionalKineticBlock implements IBE<PoweredInvertedYawGovernorEntity> {
  public PoweredInvertedYawGovernorBlock(Properties properties){super(properties);registerDefaultState(defaultBlockState().setValue(FACING,Direction.EAST));}
  @Override public Direction.Axis getRotationAxis(BlockState state){return state.getValue(FACING).getAxis();}
  @Override public boolean hasShaftTowards(LevelReader level,BlockPos pos,BlockState state,Direction face){return face.getAxis()==getRotationAxis(state);}
  @Override public Class<PoweredInvertedYawGovernorEntity> getBlockEntityClass(){return PoweredInvertedYawGovernorEntity.class;}
  @Override public BlockEntityType<? extends PoweredInvertedYawGovernorEntity> getBlockEntityType(){return PoweredInvertedYawContent.GOVERNOR_ENTITY.get();}
  @Override public boolean isSignalSource(BlockState state){return true;}
  @Override public int getSignal(BlockState state,BlockGetter level,BlockPos pos,Direction side){return level.getBlockEntity(pos) instanceof PoweredInvertedYawGovernorEntity g&&g.pulsing()?15:0;}
  @Override protected InteractionResult useWithoutItem(BlockState state,Level raw,BlockPos pos,Player player,BlockHitResult hit){
    if(!(raw instanceof ServerLevel level))return InteractionResult.SUCCESS;
    if(!player.mayBuild()||!level.mayInteract(player,pos))return InteractionResult.FAIL;
    var governor=getBlockEntity(level,pos);
    try{
      if(governor.vehicle==null||player.isShiftKeyDown()){
        var container=SubLevelContainer.getContainer(level);FoundationData.Vehicle nearest=null;double best=144;
        for(var v:FoundationData.get(level).vehicles.values())if(PoweredInvertedYawVehicle.is(v)){
          var body=container.getSubLevel(v.chassis);var center=body==null?null:CoasterCartResolve.cartCenterWorld(body);
          double d=center==null?Double.POSITIVE_INFINITY:center.distanceSquared(new Vector3d(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5));
          if(d<best){best=d;nearest=v;}
        }
        if(nearest==null)throw new IllegalArgumentException("Place a stopped Powered Inverted Yaw train within twelve blocks");
        governor.bind(nearest);
      }
      player.displayClientMessage(Component.literal("Powered circuit governor: "+governor.status()+". Rear shaft supplies power; front shaft feeds one complete Chain section. Use car Capture/Dispatch or Resume; sneak-use binds."),false);
      return InteractionResult.SUCCESS;
    }catch(IllegalArgumentException failure){player.displayClientMessage(Component.literal(failure.getMessage()),true);return InteractionResult.FAIL;}
  }
}
