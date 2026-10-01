package dev.fourthdimension.gametest;

import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class OptionalEquipmentGameTests {
  @GameTest(template = "empty", timeoutTicks = 80)
  public static void inactiveAnchorStrengthRespectsOwnershipAndDeletion(GameTestHelper helper) {
    var level = helper.getLevel();
    var from = new BlockPos(950, 140, 900);
    var to = from.offset(0, 0, 30);
    var unrelated = from.offset(5, 0, 0);
    for (int x = (from.getX() - 16) >> 4; x <= (from.getX() + 16) >> 4; x++)
      for (int z = (from.getZ() - 16) >> 4; z <= (to.getZ() + 16) >> 4; z++)
        level.setChunkForced(x, z, true);
    for (var pos : List.of(from, to, unrelated))
      level.setBlockAndUpdate(
          pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000411"), "FDOptionalOwnership"));
    player.setGameMode(GameType.CREATIVE);
    player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
    player.setPos(to.getCenter().add(2, 1, 0));
    var rail =
        TrackPlacement.connect(
            level, from, to, TrackDesign.ARROW, player, player.getMainHandItem());
    helper.runAtTickTime(
        15,
        () -> {
          var anchor = (CoasterAnchorpointBlockEntity) level.getBlockEntity(from);
          var other = (CoasterAnchorpointBlockEntity) level.getBlockEntity(unrelated);
          var registries = level.registryAccess();
          var original = anchor.saveWithFullMetadata(registries);
          var behaviour = anchor.getBehaviour(ScrollValueBehaviour.TYPE);
          if (behaviour != null) {
            // The remembered 83 must never override a later setting owned by a live behaviour.
            var tag = original.copy();
            tag.putInt("ScrollValue", 83);
            anchor.loadWithComponents(tag, registries);
            behaviour.setValue(7);
            helper.assertTrue(behaviour.getValue() == 7, "Live owner did not accept control value");
            helper.assertTrue(
                anchor.saveWithFullMetadata(registries).getInt("ScrollValue") == 7,
                "Inactive remembered value overwrote the installed behaviour");
            System.out.println(
                "FD_OPTIONAL_STRENGTH owner="
                    + behaviour.getClass().getName()
                    + " remembered=83 saved=7 currentOwner=true");
          } else {
            for (int value : new int[] {0, 37, 100}) {
              var tag = original.copy();
              tag.putInt("ScrollValue", value);
              anchor.loadWithComponents(tag, registries);
              var saved = anchor.saveWithFullMetadata(registries);
              helper.assertTrue(
                  saved.contains("ScrollValue", Tag.TAG_INT)
                      && saved.getInt("ScrollValue") == value,
                  "Valid inactive strength lost: " + value);
              helper.assertTrue(
                  !anchor.getUpdateTag(registries).contains("ScrollValue"),
                  "Inactive setting leaked into client update data");
              System.out.println("FD_OPTIONAL_STRENGTH retained=" + value + " clientPacket=false");
            }
            var deleted = original.copy();
            deleted.remove("ScrollValue");
            anchor.loadWithComponents(deleted, registries);
            helper.assertTrue(
                !anchor.saveWithFullMetadata(registries).contains("ScrollValue"),
                "Explicit field removal resurrected the remembered value");
            System.out.println("FD_OPTIONAL_STRENGTH explicitDeletion=true");
            for (var invalid :
                List.<Tag>of(
                    IntTag.valueOf(-1),
                    IntTag.valueOf(101),
                    LongTag.valueOf(37),
                    StringTag.valueOf("37"),
                    ByteTag.valueOf((byte) 37))) {
              var tag = original.copy();
              tag.put("ScrollValue", invalid);
              anchor.loadWithComponents(tag, registries);
              helper.assertTrue(
                  !anchor.saveWithFullMetadata(registries).contains("ScrollValue"),
                  "Invalid inactive value retained: " + invalid);
              System.out.println(
                  "FD_OPTIONAL_STRENGTH rejected=" + invalid + " type=" + invalid.getId());
            }
            var otherTag = other.saveWithFullMetadata(registries);
            otherTag.putInt("ScrollValue", 37);
            other.loadWithComponents(otherTag, registries);
            helper.assertTrue(
                !other.saveWithFullMetadata(registries).contains("ScrollValue"),
                "Unrelated native anchor adopted optional field retention");
            System.out.println("FD_OPTIONAL_STRENGTH unrelatedAnchorUntouched=true");
            var retained = original.copy();
            retained.putInt("ScrollValue", 37);
            anchor.loadWithComponents(retained, registries);
            level.setBlockAndUpdate(from, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(
                from, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
            var replacement = (CoasterAnchorpointBlockEntity) level.getBlockEntity(from);
            helper.assertTrue(
                replacement != anchor
                    && !replacement.saveWithFullMetadata(registries).contains("ScrollValue"),
                "Replacement anchor inherited the removed block's inactive setting");
            System.out.println("FD_OPTIONAL_STRENGTH replacementEmpty=true");
          }
          FoundationData.get(level).rails.remove(rail);
          FoundationData.get(level).setDirty();
          helper.succeed();
        });
  }
}
