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

/** S2-only identities; S1 registration and assets remain unchanged. */
public final class S2SpinningContent {
  public static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(FourthDimension.ID);
  public static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(FourthDimension.ID);
  public static final DeferredRegister<BlockEntityType<?>> ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,FourthDimension.ID);
  public static final DeferredBlock<S2SpinningGovernorBlock> GOVERNOR=BLOCKS.register("s2_spinning_governor",
      ()->new S2SpinningGovernorBlock(BlockBehaviour.Properties.of().strength(4).noOcclusion()));
  public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<S2SpinningGovernorEntity>> GOVERNOR_ENTITY=ENTITIES.register("s2_spinning_governor",
      ()->BlockEntityType.Builder.of(S2SpinningGovernorEntity::new,GOVERNOR.get()).build(null));
  public static final DeferredItem<BlockItem> GOVERNOR_ITEM=ITEMS.register("s2_spinning_governor",
      ()->new BlockItem(GOVERNOR.get(),new Item.Properties()));
  public static final DeferredBlock<S2SpinningSeatBlock> SEAT=BLOCKS.register("s2_spinning_seat",
      ()->new S2SpinningSeatBlock(BlockBehaviour.Properties.of().strength(3).noOcclusion()));
  public static final DeferredBlock<SpinningBearingBlock> BEARING=BLOCKS.register("s2_spinning_bearing",
      ()->new SpinningBearingBlock(BlockBehaviour.Properties.of().strength(4).noOcclusion()));
  public static final DeferredItem<FamilyTrackItem> TRACK=ITEMS.register("s2_spinning_track",
      ()->new FamilyTrackItem(new Item.Properties(),TrackDesign.S2_SPINNING));
  public static final DeferredItem<VehicleItem> CAR=ITEMS.register("s2_spinning_car",
      ()->new VehicleItem(new Item.Properties().stacksTo(1),TrackDesign.S2_SPINNING));
  private S2SpinningContent(){}
  public static void register(IEventBus bus){BLOCKS.register(bus);ITEMS.register(bus);ENTITIES.register(bus);}
}
