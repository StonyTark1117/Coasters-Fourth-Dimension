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
public final class FlyingBikeGovernorBlock extends DirectionalKineticBlock implements IBE<FlyingBikeGovernorEntity> {
  private static final java.util.Map<java.util.UUID,net.minecraft.core.GlobalPos> SELECTED=new java.util.HashMap<>();
  public FlyingBikeGovernorBlock(Properties properties){super(properties);registerDefaultState(defaultBlockState().setValue(FACING,Direction.EAST));}
  @Override public Direction.Axis getRotationAxis(BlockState state){return state.getValue(FACING).getAxis();}
  @Override public boolean hasShaftTowards(LevelReader level,BlockPos pos,BlockState state,Direction face){return face.getAxis()==getRotationAxis(state);}
  @Override public Class<FlyingBikeGovernorEntity> getBlockEntityClass(){return FlyingBikeGovernorEntity.class;}
  @Override public BlockEntityType<? extends FlyingBikeGovernorEntity> getBlockEntityType(){return FlyingBikeContent.GOVERNOR_ENTITY.get();}
  @Override protected InteractionResult useWithoutItem(BlockState state,Level raw,BlockPos pos,Player player,BlockHitResult hit){
    if(!(raw instanceof ServerLevel level))return InteractionResult.SUCCESS;
    if(!player.mayBuild()||!level.mayInteract(player,pos))return InteractionResult.FAIL;
    var governor=getBlockEntity(level,pos);
    try{
      if(player.isShiftKeyDown()){
        var selected=SELECTED.remove(player.getUUID());
        if(selected==null){
          if(governor.vehicle==null||governor.leader!=null)throw new IllegalArgumentException("Use the first governor normally to bind an empty bike before linking");
          SELECTED.put(player.getUUID(),net.minecraft.core.GlobalPos.of(level.dimension(),pos));
          player.displayClientMessage(Component.literal("First launch selected. Sneak-use the second governor to link it."),false);return InteractionResult.SUCCESS;
        }
        if(!selected.dimension().equals(level.dimension())||!level.hasChunkAt(selected.pos())||!(level.getBlockEntity(selected.pos()) instanceof FlyingBikeGovernorEntity first))throw new IllegalArgumentException("First launch unavailable; select it again");
        first.link(governor);player.displayClientMessage(Component.literal("Two flying-bike launches bound to one ride authority."),false);return InteractionResult.SUCCESS;
      }
      if(governor.vehicle==null){
        var container=SubLevelContainer.getContainer(level);FoundationData.Vehicle nearest=null;double best=144;
        for(var v:FoundationData.get(level).vehicles.values())if(FlyingBikeVehicle.is(v)){
          var body=container.getSubLevel(v.chassis);var center=body==null?null:CoasterCartResolve.cartCenterWorld(body);
          double d=center==null?Double.POSITIVE_INFINITY:center.distanceSquared(new Vector3d(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5));
          if(d<best){best=d;nearest=v;}
        }
        if(nearest==null)throw new IllegalArgumentException("Place a stopped Flying Bike train within twelve blocks");
        governor.bind(nearest);
      }
      player.displayClientMessage(Component.literal("Flying Bike launch: "+governor.status()+". Rear shaft supplies power; front shaft feeds a >=32-block Chain. Sneak-use first then second to link; use car Stop/Dispatch or Resume."),false);
      return InteractionResult.SUCCESS;
    }catch(IllegalArgumentException failure){player.displayClientMessage(Component.literal(failure.getMessage()),true);return InteractionResult.FAIL;}
  }
}
