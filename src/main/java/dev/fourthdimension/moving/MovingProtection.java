package dev.fourthdimension.moving;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Normal dismantling returns only the paid owned parts after the empty bed is stopped. */
public final class MovingProtection {
  private MovingProtection() {}
  public static void breaking(BlockEvent.BreakEvent event) {
    if(event.getLevel() instanceof ServerLevel level&&MovingRuntime.protects(level,event.getPos())) {
      event.setCanceled(true);
      event.getPlayer().displayClientMessage(Component.literal("Use Dismantle at the moving-track controller after every train has cleared."),true);
    }
  }
}
