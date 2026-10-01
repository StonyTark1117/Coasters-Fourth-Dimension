package dev.fourthdimension.integration.additions;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.content.redstone.link.RedstoneLinkBlock;
import com.simibubi.create.content.redstone.link.RedstoneLinkBlockEntity;
import dev.fourthdimension.track.TrackDesign;
import dev.plushivr.coasterfins.block.StationBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Native receiving links for validation; internal mechanical marker blocks have no item form. */
public final class AdditionsWirelessFixture {
  private AdditionsWirelessFixture() {}

  private static BlockPos receiver(BlockPos station, int channel) {
    return station.offset(-4, 0, 4 + channel * 2);
  }

  public static void place(ServerLevel level, BlockPos station) {
    for (int channel = 0; channel < 4; channel++) {
      var pos = receiver(station, channel);
      level.setBlockAndUpdate(pos.below(), Blocks.SMOOTH_STONE.defaultBlockState());
      level.setBlockAndUpdate(
          pos,
          AllBlocks.REDSTONE_LINK
              .getDefaultState()
              .setValue(BlockStateProperties.FACING, Direction.UP)
              .setValue(RedstoneLinkBlock.RECEIVER, true));
      // Direct setBlock placement omits the native NBT read that creates deferred link behaviours.
      // Load through the public block-entity path before assigning ordinary item frequencies.
      level
          .getBlockEntity(pos)
          .loadWithComponents(new net.minecraft.nbt.CompoundTag(), level.registryAccess());
    }
  }

  /** Call after native deferred link behaviours have initialized. */
  public static void bind(ServerLevel level, StationBlockEntity station, TrackDesign design) {
    var first =
        List.of(
                Items.RED_WOOL,
                Items.ORANGE_WOOL,
                Items.BLUE_WOOL,
                Items.PURPLE_WOOL,
                Items.CYAN_WOOL)
            .get(design.ordinal())
            .getDefaultInstance();
    var seconds = List.of(Items.DIAMOND, Items.EMERALD, Items.REDSTONE, Items.GOLD_INGOT);
    for (int channel = 0; channel < 4; channel++) {
      var second = seconds.get(channel).getDefaultInstance();
      station.setFrequency(channel, 0, first);
      station.setFrequency(channel, 1, second);
      var link =
          ((RedstoneLinkBlockEntity) level.getBlockEntity(receiver(station.getBlockPos(), channel)))
              .getBehaviour(LinkBehaviour.TYPE);
      if (link == null)
        throw new IllegalArgumentException("Native receiving link has not initialized");
      link.setFrequency(true, first);
      link.setFrequency(false, second);
    }
    station.setChanged();
  }

  public static List<Integer> received(ServerLevel level, BlockPos station) {
    return java.util.stream.IntStream.range(0, 4)
        .mapToObj(
            channel ->
                ((RedstoneLinkBlockEntity) level.getBlockEntity(receiver(station, channel)))
                    .getReceivedSignal())
        .toList();
  }

  public static void assertDelivery(
      net.minecraft.gametest.framework.GameTestHelper helper,
      StationBlockEntity station,
      String phase) {
    var signals = received(helper.getLevel(), station.getBlockPos());
    for (int channel = 0; channel < 4; channel++)
      helper.assertTrue(
          signals.get(channel) == station.channel(channel).power(),
          "Wireless station signal mismatch at "
              + station.getBlockPos()
              + " "
              + phase
              + " channel="
              + channel
              + " received="
              + signals);
    System.out.println(
        "FD_ADDITIONS_WIRELESS phase="
            + phase
            + " station="
            + station.getBlockPos().toShortString()
            + " received="
            + signals);
  }
}
