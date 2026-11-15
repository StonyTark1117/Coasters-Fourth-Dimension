package dev.fourthdimension.vehicle;
import dev.fourthdimension.FourthDimension;
import dev.fourthdimension.track.*;
import net.minecraft.world.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
public final class DiveContent {
  public static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(FourthDimension.ID);
  public static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(FourthDimension.ID);
  public static final DeferredRegister<BlockEntityType<?>> ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,FourthDimension.ID);
  public static final DeferredBlock<DiveSeatBlock> SEAT=BLOCKS.register("dive_seat",()->new DiveSeatBlock(BlockBehaviour.Properties.of().strength(3).noOcclusion()));
  public static final DeferredBlock<DiveFrameBlock> FRAME=BLOCKS.register("dive_frame",()->new DiveFrameBlock(BlockBehaviour.Properties.of().strength(4).noOcclusion()));
  public static final DeferredBlock<DiveHoldBlock> HOLD=BLOCKS.register("dive_hold",()->new DiveHoldBlock(BlockBehaviour.Properties.of().strength(4)));
  public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<DiveHoldEntity>> HOLD_ENTITY=ENTITIES.register("dive_hold",()->BlockEntityType.Builder.of(DiveHoldEntity::new,HOLD.get()).build(null));
  public static final DeferredItem<BlockItem> HOLD_ITEM=ITEMS.register("dive_hold",()->new BlockItem(HOLD.get(),new Item.Properties()));
  public static final DeferredItem<FamilyTrackItem> TRACK=ITEMS.register("dive_track",()->new FamilyTrackItem(new Item.Properties(),TrackDesign.DIVE));
  public static final DeferredItem<VehicleItem> CAR=ITEMS.register("dive_car",()->new VehicleItem(new Item.Properties().stacksTo(1),TrackDesign.DIVE));
  private DiveContent(){}
  public static void register(IEventBus bus){BLOCKS.register(bus);ITEMS.register(bus);ENTITIES.register(bus);}
}
