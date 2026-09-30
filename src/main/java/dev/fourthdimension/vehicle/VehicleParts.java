package dev.fourthdimension.vehicle;

import dev.fourthdimension.FourthDimension;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.*;

public final class VehicleParts {
  private VehicleParts() {}

  public static final DeferredRegister.Blocks BLOCKS =
      DeferredRegister.createBlocks(FourthDimension.ID);
  public static final DeferredBlock<CarrierSeatBlock> ARROW_SEAT =
      BLOCKS.register(
          "arrow_seat",
          () ->
              new CarrierSeatBlock(
                  BlockBehaviour.Properties.of().strength(3).noOcclusion(), DyeColor.RED));
  public static final DeferredBlock<CarrierSeatBlock> LEGACY_SEAT =
      BLOCKS.register(
          "legacy_seat",
          () ->
              new CarrierSeatBlock(
                  BlockBehaviour.Properties.of().strength(3).noOcclusion(), DyeColor.YELLOW));
  public static final DeferredBlock<Block> AXLE =
      BLOCKS.register(
          "carrier_axle",
          () ->
              new MechanicalPartBlock(
                  BlockBehaviour.Properties.of().strength(4).noOcclusion(), true));
  public static final DeferredBlock<Block> STRUT =
      BLOCKS.register(
          "wing_strut",
          () ->
              new MechanicalPartBlock(
                  BlockBehaviour.Properties.of().strength(4).noOcclusion(), false));
}
