package dev.fourthdimension.shuttle;

import dev.fourthdimension.station.StationContent;
import dev.fourthdimension.track.TrackItems;
import dev.fourthdimension.vehicle.MechanicalPartBlock;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.*;

public final class ShuttleContent {
  private ShuttleContent() {}

  public static final DeferredBlock<ShuttleWinchBlock> WINCH =
      StationContent.BLOCKS.register(
          "shuttle_winch", () -> new ShuttleWinchBlock(BlockBehaviour.Properties.of().strength(4)));
  public static final DeferredBlock<Block> CARRIAGE =
      StationContent.BLOCKS.register(
          "shuttle_carriage",
          () ->
              new MechanicalPartBlock(
                  BlockBehaviour.Properties.of().strength(4).noOcclusion(),
                  Block.box(2, 3, 2, 14, 13, 14)));
  public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShuttleWinchEntity>>
      ENTITY =
          StationContent.ENTITIES.register(
              "shuttle_winch",
              () -> BlockEntityType.Builder.of(ShuttleWinchEntity::new, WINCH.get()).build(null));

  public static void registerItem() {
    TrackItems.ITEMS.register(
        "shuttle_winch", () -> new BlockItem(WINCH.get(), new Item.Properties()));
  }
}
