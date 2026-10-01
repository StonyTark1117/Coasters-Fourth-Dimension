package dev.fourthdimension.gametest;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Decode shipped schematics with Minecraft; no client Ponder classes on the server. */
@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class PonderDiagramGameTests {
  @GameTest(template = "empty", timeoutTicks = 40)
  public static void shippedDiagramsUseRegisteredBlocks(GameTestHelper helper) throws IOException {
    var seats =
        Map.of(
            "matching",
            5,
            "construction",
            0,
            "arrow",
            4,
            "legacy",
            8,
            "free_spin",
            8,
            "revised",
            8,
            "magnets",
            8,
            "boarding",
            4,
            "pax",
            4);
    int totalBlocks = 0;
    for (var entry : seats.entrySet()) {
      String path = "/assets/fourthdimension/ponder/" + entry.getKey() + ".nbt";
      try (var stream = PonderDiagramGameTests.class.getResourceAsStream(path)) {
        helper.assertTrue(stream != null, "Missing Ponder schematic " + path);
        var nbt = NbtIo.readCompressed(stream, NbtAccounter.create(1_000_000));
        var palette = nbt.getList("palette", Tag.TAG_COMPOUND);
        for (int i = 0; i < palette.size(); i++) {
          var state = palette.getCompound(i);
          var id = ResourceLocation.parse(state.getString("Name"));
          helper.assertTrue(BuiltInRegistries.BLOCK.containsKey(id), "Unknown diagram block " + id);
          var block = BuiltInRegistries.BLOCK.get(id);
          var properties = state.getCompound("Properties");
          for (String name : properties.getAllKeys()) {
            var property = block.getStateDefinition().getProperty(name);
            helper.assertTrue(
                property != null && property.getValue(properties.getString(name)).isPresent(),
                "Invalid diagram state " + id + " " + name);
          }
        }
        int foundSeats = 0;
        var blocks = nbt.getList("blocks", Tag.TAG_COMPOUND);
        var occupied = new java.util.HashSet<String>();
        for (int i = 0; i < blocks.size(); i++) {
          var block = blocks.getCompound(i);
          var position = block.getList("pos", Tag.TAG_INT);
          helper.assertTrue(position.size() == 3, "Invalid position in " + path);
          helper.assertTrue(
              occupied.add(position.toString()), "Overlapping diagram blocks " + path);
          var size = nbt.getList("size", Tag.TAG_INT);
          for (int axis = 0; axis < 3; axis++)
            helper.assertTrue(
                position.getInt(axis) >= 0 && position.getInt(axis) < size.getInt(axis),
                "Diagram block outside bounds " + path);
          int index = block.getInt("state");
          helper.assertTrue(index >= 0 && index < palette.size(), "Invalid palette index " + path);
          if (palette.getCompound(index).getString("Name").endsWith("_seat")) foundSeats++;
        }
        helper.assertTrue(foundSeats == entry.getValue(), "Wrong seat count for " + entry.getKey());
        totalBlocks += blocks.size();
      }
    }
    LogUtils.getLogger()
        .info("FD_PONDER_RESOURCES diagrams={} decodedBlocks={}", seats.size(), totalBlocks);
    helper.succeed();
  }
}
