package dev.fourthdimension.station;

import dev.fourthdimension.FourthDimension;
import dev.fourthdimension.track.TrackItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.*;

public final class StationContent {
  private StationContent() {}

  public static final DeferredRegister.Blocks BLOCKS =
      DeferredRegister.createBlocks(FourthDimension.ID);
  public static final DeferredRegister<BlockEntityType<?>> ENTITIES =
      DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, FourthDimension.ID);
  public static final DeferredBlock<BoardingStation> STATION =
      BLOCKS.register(
          "boarding_station",
          () -> new BoardingStation(BlockBehaviour.Properties.of().noOcclusion().strength(3)));
  public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BoardingStationEntity>>
      ENTITY =
          ENTITIES.register(
              "boarding_station",
              () ->
                  BlockEntityType.Builder.of(BoardingStationEntity::new, STATION.get())
                      .build(null));

  public static void registerItem() {
    TrackItems.ITEMS.register(
        "boarding_station", () -> new BlockItem(STATION.get(), new Item.Properties()));
  }
}
