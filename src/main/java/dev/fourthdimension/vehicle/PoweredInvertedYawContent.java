package dev.fourthdimension.vehicle;

import dev.fourthdimension.FourthDimension;
import dev.fourthdimension.track.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/** Dedicated upright overhead yaw content. */
public final class PoweredInvertedYawContent {
  public static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(FourthDimension.ID);
  public static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(FourthDimension.ID);
  public static final DeferredRegister<BlockEntityType<?>> ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,FourthDimension.ID);
  public static final DeferredBlock<PoweredInvertedYawGovernorBlock> GOVERNOR=BLOCKS.register("powered_inverted_yaw_governor",
      ()->new PoweredInvertedYawGovernorBlock(BlockBehaviour.Properties.of().strength(4).noOcclusion()));
  public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<PoweredInvertedYawGovernorEntity>> GOVERNOR_ENTITY=ENTITIES.register("powered_inverted_yaw_governor",
      ()->BlockEntityType.Builder.of(PoweredInvertedYawGovernorEntity::new,GOVERNOR.get()).build(null));
  public static final DeferredItem<BlockItem> GOVERNOR_ITEM=ITEMS.register("powered_inverted_yaw_governor",
      ()->new BlockItem(GOVERNOR.get(),new Item.Properties()));
  public static final DeferredBlock<PoweredInvertedYawSeatBlock> SEAT=BLOCKS.register("powered_inverted_yaw_seat",
      ()->new PoweredInvertedYawSeatBlock(BlockBehaviour.Properties.of().strength(3).noOcclusion()));
  public static final DeferredBlock<PoweredInvertedYawBearingBlock> BEARING=BLOCKS.register("powered_inverted_yaw_bearing",
      ()->new PoweredInvertedYawBearingBlock(BlockBehaviour.Properties.of().strength(4).noOcclusion()));
  public static final DeferredBlock<PoweredInvertedYawSpindleBlock> SPINDLE=BLOCKS.register("powered_inverted_yaw_spindle",
      ()->new PoweredInvertedYawSpindleBlock(BlockBehaviour.Properties.of().strength(4).noOcclusion()));
  public static final DeferredItem<FamilyTrackItem> TRACK=ITEMS.register("powered_inverted_yaw_track",
      ()->new FamilyTrackItem(new Item.Properties(),TrackDesign.POWERED_INVERTED_YAW));
  public static final DeferredItem<VehicleItem> CAR=ITEMS.register("powered_inverted_yaw_car",
      ()->new VehicleItem(new Item.Properties().stacksTo(1),TrackDesign.POWERED_INVERTED_YAW));
  private PoweredInvertedYawContent(){}
  public static void register(IEventBus bus){BLOCKS.register(bus);ITEMS.register(bus);ENTITIES.register(bus);}
}
