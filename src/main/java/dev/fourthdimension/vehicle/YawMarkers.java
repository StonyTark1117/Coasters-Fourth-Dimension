package dev.fourthdimension.vehicle;

import dev.fourthdimension.FourthDimension;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class YawMarkers {
  private YawMarkers(){}
  public static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(FourthDimension.ID);
  public static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(FourthDimension.ID);
  public static final DeferredRegister<BlockEntityType<?>> ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,FourthDimension.ID);
  public static final DeferredBlock<YawMarkerBlock> MARKER=BLOCKS.register("yaw_marker",()->new YawMarkerBlock(BlockBehaviour.Properties.of().strength(2).noOcclusion()));
  public static final DeferredItem<BlockItem> ITEM=ITEMS.register("yaw_marker",()->new BlockItem(MARKER.get(),new Item.Properties()));
  public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<YawMarkerEntity>> ENTITY=ENTITIES.register("yaw_marker",()->BlockEntityType.Builder.of(YawMarkerEntity::new,MARKER.get()).build(null));
  public static void register(IEventBus bus){BLOCKS.register(bus);ITEMS.register(bus);ENTITIES.register(bus);}
}
