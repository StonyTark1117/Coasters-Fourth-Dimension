package dev.fourthdimension.track;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;

/** Two anchor clicks use the native graph; a dedicated item supplies the mechanical identity. */
public final class FamilyTrackItem extends Item {
  public final TrackDesign design;

  public FamilyTrackItem(Properties properties, TrackDesign design) {
    super(properties);
    this.design = design;
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    var player = context.getPlayer();
    if (player == null) return InteractionResult.PASS;
    if (context.getLevel().isClientSide()) return InteractionResult.SUCCESS;
    var level = (ServerLevel) context.getLevel();
    var stack = context.getItemInHand();
    var first = stack.get(TrackItems.SELECTION.get());
    var pos = context.getClickedPos();
    try {
      if (player.isShiftKeyDown()) {
        stack.remove(TrackItems.SELECTION.get());
        player.displayClientMessage(Component.literal("Track selection cleared"), true);
      } else if (first == null) {
        TrackPlacement.checkEndpoint(level, pos, player);
        stack.set(TrackItems.SELECTION.get(), GlobalPos.of(level.dimension(), pos));
        player.displayClientMessage(
            Component.literal("Select a second coaster anchor; sneak-click to cancel"), true);
      } else {
        if (!first.dimension().equals(level.dimension()))
          throw new IllegalArgumentException("Select anchors in the same dimension");
        var rail = TrackPlacement.connect(level, first.pos(), pos, design, player, stack);
        stack.remove(TrackItems.SELECTION.get());
        player.displayClientMessage(
            Component.literal(
                rail.valid
                    ? "Placed "
                        + design.variant
                        + "; edit the curve with the Coasters handles, then rebind its profile"
                    : "Support restored; use Rotation Tool: Repair matching track, then Realign matching track"),
            true);
      }
      return InteractionResult.SUCCESS;
    } catch (IllegalArgumentException | IllegalStateException failure) {
      player.displayClientMessage(Component.literal(failure.getMessage()), true);
      return InteractionResult.FAIL;
    }
  }

  @Override
  public boolean isFoil(ItemStack stack) {
    return stack.has(TrackItems.SELECTION.get());
  }
}
