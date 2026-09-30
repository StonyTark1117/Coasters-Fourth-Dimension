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
  public static final DeferredBlock<CarrierSeatBlock> FREE_SPIN_SEAT =
      BLOCKS.register(
          "free_spin_seat",
          () ->
              new CarrierSeatBlock(
                  BlockBehaviour.Properties.of().strength(3).noOcclusion(), DyeColor.BLUE));
  public static final DeferredBlock<CarrierSeatBlock> REVISED_SEAT =
      BLOCKS.register(
          "revised_seat",
          () ->
              new CarrierSeatBlock(
                  BlockBehaviour.Properties.of().strength(3).noOcclusion(), DyeColor.PURPLE));
  public static final DeferredBlock<CarrierSeatBlock> PAX_SEAT =
      BLOCKS.register(
          "pax_seat",
          () ->
              new CarrierSeatBlock(
                  BlockBehaviour.Properties.of().strength(3).noOcclusion(), DyeColor.GREEN));
  public static final DeferredBlock<Block> REVISED_AXLE =
      BLOCKS.register(
          "revised_axle",
          () ->
              new MechanicalPartBlock(
                  BlockBehaviour.Properties.of().strength(4).noOcclusion(),
                  Block.box(0, -2, 6, 16, 2, 10)));
  public static final DeferredBlock<Block> REVISED_STRUT =
      BLOCKS.register(
          "revised_strut",
          () ->
              new MechanicalPartBlock(
                  BlockBehaviour.Properties.of().strength(4).noOcclusion(),
                  Block.box(0, 12, 4, 16, 20, 12)));
  public static final DeferredBlock<Block> AXLE =
      BLOCKS.register(
          "carrier_axle",
          () ->
              new MechanicalPartBlock(
                  BlockBehaviour.Properties.of().strength(4).noOcclusion(), true));
  public static final DeferredBlock<Block> FREE_SPIN_AXLE =
      BLOCKS.register(
          "free_spin_axle",
          () ->
              new MechanicalPartBlock(
                  BlockBehaviour.Properties.of().strength(4).noOcclusion(),
                  Block.box(0, 6, 6, 16, 10, 10)));
  public static final DeferredBlock<Block> STRUT =
      BLOCKS.register(
          "wing_strut",
          () ->
              new MechanicalPartBlock(
                  BlockBehaviour.Properties.of().strength(4).noOcclusion(), false));
}
