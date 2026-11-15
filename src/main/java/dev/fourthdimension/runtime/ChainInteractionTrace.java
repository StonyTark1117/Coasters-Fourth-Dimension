package dev.fourthdimension.runtime;

import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.Sable;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Opt-in trace of native Chain selection delivery, never a synthetic interaction. */
@EventBusSubscriber(modid = "fourthdimension")
public final class ChainInteractionTrace {
  private ChainInteractionTrace() {}
  @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
  public static void block(PlayerInteractEvent.RightClickBlock event) {
    if (!Boolean.getBoolean("fourthdimension.validation") || !event.getItemStack().is(Items.CHAIN)) return;
    var level = event.getLevel();
    var body = Sable.HELPER.getContaining(level, event.getPos());
    var cart = CoasterCartResolve.findCartSubLevelAt(level, event.getPos());
    LogUtils.getLogger().info("FD_CHAIN_INPUT side={} player={} hand={} pos={} state={} canceled={} body={} cart={}",
        level.isClientSide ? "client" : "server", event.getEntity().getName().getString(), event.getHand(),
        event.getPos(), level.getBlockState(event.getPos()), event.isCanceled(),
        body == null ? null : body.getUniqueId(), cart == null ? null : cart.getUniqueId());
  }
  @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
  public static void after(PlayerInteractEvent.RightClickBlock event) {
    if (!Boolean.getBoolean("fourthdimension.validation") || !event.getItemStack().is(Items.CHAIN)) return;
    LogUtils.getLogger().info("FD_CHAIN_INPUT_END side={} canceled={} result={} item={}",
        event.getLevel().isClientSide ? "client" : "server", event.isCanceled(),
        event.getCancellationResult(), event.getItemStack().getComponents());
  }
}
