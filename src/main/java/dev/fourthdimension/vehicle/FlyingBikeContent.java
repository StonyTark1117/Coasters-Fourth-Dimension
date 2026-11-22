package dev.fourthdimension.vehicle;
import dev.fourthdimension.FourthDimension;
import dev.fourthdimension.track.*;
import net.minecraft.world.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
public final class FlyingBikeContent {
  public static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(FourthDimension.ID);
  public static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(FourthDimension.ID);
  public static final DeferredRegister<BlockEntityType<?>> ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,FourthDimension.ID);
  public static final DeferredBlock<FlyingBikeSeatBlock> SEAT=BLOCKS.register("flying_bike_seat",()->new FlyingBikeSeatBlock(BlockBehaviour.Properties.of().strength(3).noOcclusion()));
  public static final DeferredBlock<FlyingBikeFrameBlock> FRAME=BLOCKS.register("flying_bike_frame",()->new FlyingBikeFrameBlock(BlockBehaviour.Properties.of().strength(4).noOcclusion()));
  public static final DeferredBlock<FlyingBikeGovernorBlock> GOVERNOR=BLOCKS.register("flying_bike_governor",()->new FlyingBikeGovernorBlock(BlockBehaviour.Properties.of().strength(4).noOcclusion()));
  public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<FlyingBikeGovernorEntity>> GOVERNOR_ENTITY=ENTITIES.register("flying_bike_governor",()->BlockEntityType.Builder.of(FlyingBikeGovernorEntity::new,GOVERNOR.get()).build(null));
  public static final DeferredItem<BlockItem> GOVERNOR_ITEM=ITEMS.register("flying_bike_governor",()->new BlockItem(GOVERNOR.get(),new Item.Properties()));
  public static final DeferredItem<FamilyTrackItem> TRACK=ITEMS.register("flying_bike_track",()->new FamilyTrackItem(new Item.Properties(),TrackDesign.FLYING_BIKE));
  public static final DeferredItem<VehicleItem> CAR=ITEMS.register("flying_bike_car",()->new VehicleItem(new Item.Properties().stacksTo(1),TrackDesign.FLYING_BIKE));
  private FlyingBikeContent(){}
  public static void register(IEventBus b){BLOCKS.register(b);ITEMS.register(b);ENTITIES.register(b);}
}
