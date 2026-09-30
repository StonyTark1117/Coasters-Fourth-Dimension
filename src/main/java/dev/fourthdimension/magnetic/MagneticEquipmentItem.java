package dev.fourthdimension.magnetic;

import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.physics.MagneticSpan;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackGeometry;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.ArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public final class MagneticEquipmentItem extends Item {
  private final MagneticSpan.Mode mode;

  public MagneticEquipmentItem(Properties properties, MagneticSpan.Mode mode) {
    super(properties);
    this.mode = mode;
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
    if (!(context.getPlayer() instanceof ServerPlayer player)) return InteractionResult.PASS;
    try {
      if (player.distanceToSqr(context.getClickLocation()) > 64)
        throw new IllegalArgumentException("Equipment is out of reach");
      var level = player.serverLevel();
      var hit =
          CoasterPathTrackFrame.nearestGraphHit(
              level, CoasterPathGraphManager.get(level), context.getClickLocation(), 4);
      if (hit == null) throw new IllegalArgumentException("Use on a Free Spin track section");
      var rail =
          FoundationData.get(level).rails.stream()
              .filter(r -> r.matches(hit.edge().from(), hit.edge().to()))
              .findFirst()
              .orElseThrow(
                  () -> new IllegalArgumentException("Requires functional Free Spin track"));
      double length = rail.profile.keys().getLast().distance();
      double d = TrackGeometry.distance(level, hit.edge(), rail, hit.t());
      double start = Math.max(0, Math.min(length - 4, d - 2)), end = Math.min(length, start + 4);
      var proposed = new ArrayList<>(rail.magnets);
      proposed.add(new MagneticSpan(start, end, mode, 1, 4, .2, 15));
      MagneticData.apply(player, rail.id, rail.revision, proposed);
      EditorNetwork.open(player, new EditorNetwork.Target(rail.id, false));
      return InteractionResult.SUCCESS;
    } catch (IllegalArgumentException e) {
      player.displayClientMessage(Component.literal(e.getMessage()), false);
      return InteractionResult.FAIL;
    }
  }
}
