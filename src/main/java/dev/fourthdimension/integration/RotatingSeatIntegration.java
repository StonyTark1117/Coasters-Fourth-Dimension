package dev.fourthdimension.integration;

import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.Sable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.joml.Vector3d;

/** Actual Additions seats on existing FD carriers; no second mount or motion controller. */
@EventBusSubscriber(modid="fourthdimension")
public final class RotatingSeatIntegration {
  private static final String OWNED="fourthdimension_rotating_seat";
  public interface Bridge {
    BlockState fourthdimension$originalSeat();
    void fourthdimension$originalSeat(BlockState state);
    Boolean fourthdimension$linkState();
    void fourthdimension$linkState(boolean signal);
  }
  private RotatingSeatIntegration() {}
  public static boolean enabled() { return OptionalIntegrations.installed(OptionalIntegrations.ADDITIONS).mayInspect(); }
  public static boolean optional(BlockState state) {
    return state.getBlock().getClass().getName().equals("dev.plushivr.coasterfins.block.CoasterSeatBlock");
  }
  public static boolean managed(Level level,BlockPos pos) {
    return enabled()&&level.getBlockEntity(pos) instanceof Bridge bridge
        &&bridge.fourthdimension$originalSeat()!=null;
  }
  public static boolean ownsSeat(Entity seat) {
    return seat instanceof SeatEntity&&(seat.getPersistentData().getBoolean(OWNED)||managed(seat.level(),seat.blockPosition()));
  }

  @SuppressWarnings("unchecked")
  private static void attachment(Entity seat,String id,Object value) {
    var type=(AttachmentType<Object>)NeoForgeRegistries.ATTACHMENT_TYPES.get(ResourceLocation.fromNamespaceAndPath("coasterfins",id));
    if(type==null)throw new IllegalStateException("Missing inspected Additions seat attachment: "+id);
    seat.setData(type,value);
  }
  public static void mounted(Entity seat) {
    if(!managed(seat.level(),seat.blockPosition()))return;
    var binding=Boarding.at(seat.level(),seat.blockPosition());
    if(binding==null)return;
    seat.getPersistentData().putBoolean(OWNED,true);
    attachment(seat,"seat_pos",java.util.Optional.of(seat.blockPosition()));
    attachment(seat,"seat_locked",binding.vehicle().restraints);
  }
  public static void project(Level level,BlockPos pos,boolean closed) {
    if(!managed(level,pos))return;
    OptionalCalls.call(level.getBlockEntity(pos),"setPowered",!closed);
  }

  /** Native link edges request validated whole-consist restraints; steady signals cannot undo editor input. */
  public static void link(BlockEntity be,boolean powered) {
    if(!(be.getLevel() instanceof ServerLevel level)||!managed(level,be.getBlockPos())) {
      OptionalCalls.call(be,"setPowered",powered);return;
    }
    var bridge=(Bridge)be;
    var binding=Boarding.at(level,be.getBlockPos());
    if(binding==null) { OptionalCalls.call(be,"setPowered",false);return; }
    Boolean before=bridge.fourthdimension$linkState();
    bridge.fourthdimension$linkState(powered);
    if(before!=null&&before!=powered) {
      try { VehicleOperations.restraints(level,binding.vehicle(),!powered); }
      catch(IllegalArgumentException denied) {
        if(Boolean.getBoolean("fourthdimension.validation"))
          com.mojang.logging.LogUtils.getLogger().info("FD_ROTATING_SEAT_LINK denied={} pos={}",denied.getMessage(),be.getBlockPos());
      }
    }
    project(level,be.getBlockPos(),binding.vehicle().restraints);
  }

  @SubscribeEvent public static void select(PlayerInteractEvent.RightClickBlock event) {
    var player=event.getEntity();
    if(!enabled()||!player.isShiftKeyDown()||!(event.getItemStack().getItem() instanceof BlockItem held))return;
    var level=event.getLevel();var pos=event.getPos();var state=level.getBlockState(pos);
    var be=level.getBlockEntity(pos);
    var original=be instanceof Bridge bridge?bridge.fourthdimension$originalSeat():null;
    boolean install=state.getBlock().getClass()==CarrierSeatBlock.class&&optional(held.getBlock().defaultBlockState());
    boolean restore=original!=null&&held.getBlock()==original.getBlock();
    if(!install&&!restore)return;
    event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);
    if(!(level instanceof ServerLevel server))return;
    try {
      var binding=Boarding.at(level,pos);var body=Sable.HELPER.getContaining(level,pos);
      if(binding==null||body==null||!binding.carrier().seats.contains(pos.subtract(body.getPlot().getCenterBlock())))
        throw new IllegalArgumentException("Select a registered Fourth Dimension carrier seat");
      var point=body.logicalPose().transformPosition(new Vector3d(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5));
      var worldPos=BlockPos.containing(point.x,point.y,point.z);
      if(!player.mayBuild()||!level.mayInteract(player,worldPos)||!level.mayInteract(player,pos)
          ||player.getEyePosition().distanceToSqr(point.x,point.y,point.z)>
              Math.pow(player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.BLOCK_INTERACTION_RANGE)+Math.sqrt(3)/2,2))
        throw new IllegalArgumentException("Approach the seat with building permission");
      if(binding.vehicle().restraints||!Boarding.ready(server,binding.vehicle())
          ||binding.vehicle().carriers.stream().anyMatch(c->!c.occupants.isEmpty()||!c.riderSeats.isEmpty()))
        throw new IllegalArgumentException("Stop, align, open and empty the vehicle before changing seats");
      for(var offset:binding.carrier().seats)
        if(Boarding.seatOccupied(level,body.getPlot().getCenterBlock().offset(offset)))
          throw new IllegalArgumentException("Empty the carrier before changing seats");
      BlockState next=install?held.getBlock().defaultBlockState():original;
      if(install&&next.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
        next=next.setValue(BlockStateProperties.HORIZONTAL_FACING,state.getValue(CarrierSeatBlock.FACING));
      if(!level.setBlock(pos,next,3))throw new IllegalArgumentException("Seat replacement failed");
      if(install) {
        var installed=level.getBlockEntity(pos);
        if(!(installed instanceof Bridge bridge)) {
          level.setBlock(pos,state,3);throw new IllegalArgumentException("Inspected Additions seat bridge is unavailable");
        }
        bridge.fourthdimension$originalSeat(state);installed.setChanged();
        project(level,pos,binding.vehicle().restraints);
        if(installed instanceof SmartBlockEntity smart)smart.sendData();
      }
      var saved=RotatingSeatData.get(server);
      var key=new RotatingSeatData.Key(body.getUniqueId(),pos.subtract(body.getPlot().getCenterBlock()));
      if(install)saved.originals.put(key,state);else saved.originals.remove(key);
      saved.setDirty();
      if(!player.isCreative()) {
        event.getItemStack().shrink(1);
        var returned=new ItemStack(state.getBlock().asItem());
        if(!player.addItem(returned))player.drop(returned,false);
      }
      player.displayClientMessage(Component.literal(install?"Additions seat fitted; carrier restraints remain authoritative":"Original carrier seat restored"),true);
    } catch(IllegalArgumentException denied) {
      event.setCancellationResult(InteractionResult.FAIL);
      player.displayClientMessage(Component.literal(denied.getMessage()),true);
    }
  }
}
