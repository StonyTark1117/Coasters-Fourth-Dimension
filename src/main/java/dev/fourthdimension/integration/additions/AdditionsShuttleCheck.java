package dev.fourthdimension.integration.additions;

import dev.fourthdimension.shuttle.*;
import dev.plushivr.coasterfins.block.StationBlockEntity;
import dev.plushivr.coasterfins.logic.*;
import dev.plushivr.coasterfins.registry.CFBlocks;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

/** Real powered external equipment throughout the occupied shuttle catch/lift/return test. */
public final class AdditionsShuttleCheck {
  private AdditionsShuttleCheck() {}

  public static void attach(GameTestHelper helper, ShuttleExamples.Example example) {
    var level = helper.getLevel();
    var winch = example.winch();
    var route = ShuttleRoute.resolve(level, winch.routeStart, winch.routeEnd);
    var finPos = winch.getBlockPos().offset(-4, 4, 0);
    level.setBlockAndUpdate(
        finPos,
        BuiltInRegistries.BLOCK
            .get(ResourceLocation.parse("coasterfins:coaster_fin"))
            .defaultBlockState());
    var fin = level.getBlockEntity(finPos);
    var tag = fin.saveWithoutMetadata(level.registryAccess());
    var spans = new ListTag();
    for (var segment : route.segments)
      spans.add(new FinSpanSegment(segment.rail().from, segment.rail().to, 0, 1, true).write());
    tag.putString("SpanMode", "boost");
    tag.put("SpanSegments", spans);
    fin.loadWithComponents(tag, level.registryAccess());
    level.setBlockAndUpdate(finPos.below(), Blocks.REDSTONE_BLOCK.defaultBlockState());
    var stationPos = winch.getBlockPos().offset(-4, 0, 0);
    level.setBlockAndUpdate(stationPos, CFBlocks.STATION.get().defaultBlockState());
    var station = (StationBlockEntity) level.getBlockEntity(stationPos);
    var middle = route.segments.get(1).rail();
    station.setSpan(List.of(new FinSpanSegment(middle.from, middle.to, 0, 1, true)));
    station.setDispatch(true, 1, 0, 0);
    System.out.println(
        "FD_ADDITIONS_PAX_PROBE powered BOOST across all "
            + route.segments.size()
            + " route segments; auto station delay=0");
  }

  public static void verify(GameTestHelper helper, ShuttleWinchEntity winch) {
    var fin = helper.getLevel().getBlockEntity(winch.getBlockPos().offset(-4, 4, 0));
    helper.assertTrue(fin instanceof SpanController, "PAX external fin disappeared");
    var controller = (SpanController) fin;
    helper.assertTrue(
        controller.hasSpan() && controller.isForceActive(),
        "PAX external fin was not actively powered across its route");
  }
}
