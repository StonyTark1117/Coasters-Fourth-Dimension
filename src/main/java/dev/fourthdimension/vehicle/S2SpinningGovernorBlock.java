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
public final class S2SpinningGovernorBlock extends DirectionalKineticBlock implements IBE<S2SpinningGovernorEntity> {
  public S2SpinningGovernorBlock(Properties properties){super(properties);registerDefaultState(defaultBlockState().setValue(FACING,Direction.EAST));}
  @Override public Direction.Axis getRotationAxis(BlockState state){return state.getValue(FACING).getAxis();}
  @Override public boolean hasShaftTowards(LevelReader level,BlockPos pos,BlockState state,Direction face){return face.getAxis()==getRotationAxis(state);}
  @Override public Class<S2SpinningGovernorEntity> getBlockEntityClass(){return S2SpinningGovernorEntity.class;}
  @Override public BlockEntityType<? extends S2SpinningGovernorEntity> getBlockEntityType(){return S2SpinningContent.GOVERNOR_ENTITY.get();}
  @Override protected InteractionResult useWithoutItem(BlockState state,Level raw,BlockPos pos,Player player,BlockHitResult hit){
    if(!(raw instanceof ServerLevel level))return InteractionResult.SUCCESS;
    if(!player.mayBuild()||!level.mayInteract(player,pos))return InteractionResult.FAIL;
    var governor=getBlockEntity(level,pos);
    try{
      if(governor.vehicle==null||player.isShiftKeyDown()){
        var container=SubLevelContainer.getContainer(level);FoundationData.Vehicle nearest=null;double best=144;
        for(var v:FoundationData.get(level).vehicles.values())if(S2SpinningVehicle.is(v)){
          var body=container.getSubLevel(v.chassis);var center=body==null?null:CoasterCartResolve.cartCenterWorld(body);
          double d=center==null?Double.POSITIVE_INFINITY:center.distanceSquared(new Vector3d(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5));
          if(d<best){best=d;nearest=v;}
        }
        if(nearest==null)throw new IllegalArgumentException("Place a stopped Launched Inverting Spinning train within twelve blocks");
        governor.bind(nearest);
      }
      player.displayClientMessage(Component.literal("Kinetic launch governor: "+governor.status()+". Rear shaft supplies power; front shaft feeds a >=24-block Chain. Use car Capture/Dispatch or Resume; sneak-use binds."),false);
      return InteractionResult.SUCCESS;
    }catch(IllegalArgumentException failure){player.displayClientMessage(Component.literal(failure.getMessage()),true);return InteractionResult.FAIL;}
  }
}
