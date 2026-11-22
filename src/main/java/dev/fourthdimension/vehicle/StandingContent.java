package dev.fourthdimension.vehicle;
import dev.fourthdimension.FourthDimension;
import dev.fourthdimension.track.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
public final class StandingContent {
 public static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(FourthDimension.ID);
 public static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(FourthDimension.ID);
 public static final DeferredBlock<StandingSeatBlock> SEAT=BLOCKS.register("standing_seat",()->new StandingSeatBlock(BlockBehaviour.Properties.of().strength(3).noOcclusion()));
 public static final DeferredBlock<StandingFrameBlock> FRAME=BLOCKS.register("standing_frame",()->new StandingFrameBlock(BlockBehaviour.Properties.of().strength(4).noOcclusion()));
 public static final DeferredBlock<StandingCarriageBlock> CARRIAGE=BLOCKS.register("surf_carriage",()->new StandingCarriageBlock(BlockBehaviour.Properties.of().strength(4).noOcclusion()));
 public static final DeferredItem<FamilyTrackItem> STANDARD_TRACK=ITEMS.register("standing_track",()->new FamilyTrackItem(new Item.Properties(),TrackDesign.STANDING_STANDARD));
 public static final DeferredItem<FamilyTrackItem> SURF_TRACK=ITEMS.register("surf_track",()->new FamilyTrackItem(new Item.Properties(),TrackDesign.STANDING_SURF));
 public static final DeferredItem<VehicleItem> STANDARD_CAR=ITEMS.register("standing_car",()->new VehicleItem(new Item.Properties().stacksTo(1),TrackDesign.STANDING_STANDARD));
 public static final DeferredItem<VehicleItem> SURF_CAR=ITEMS.register("surf_car",()->new VehicleItem(new Item.Properties().stacksTo(1),TrackDesign.STANDING_SURF));
 private StandingContent(){}
 public static void register(IEventBus bus){BLOCKS.register(bus);ITEMS.register(bus);}
}
