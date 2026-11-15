package dev.fourthdimension.mixin;

import dev.fourthdimension.vehicle.ConsistRules;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartChainLinkInteraction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Preserve native Chain selection/linking while admitting only qualified eight-block overhead pairs. */
@Mixin(value=CoasterCartChainLinkInteraction.class, remap=false)
public abstract class OverheadChainCouplingMixin {
  @Redirect(method="handleLinkClick", at=@At(value="INVOKE", target="Ldev/silvergold/simulatedcoasters/track/cart/CoasterCartLinkPlacement;rejectsLinkDistance(D)Z"))
  private static boolean fourthdimension$distance(double distance, ServerLevel level,
      ServerPlayer player, ItemStack chain, ServerSubLevel target) {
    return ConsistRules.chainDistanceRejected(distance,level,chain,target);
  }

  @Redirect(method="handleLinkClick", at=@At(value="INVOKE", target="Ldev/silvergold/simulatedcoasters/track/cart/CoasterCartLinkPlacement;resolveChainLinkDistance(Lnet/minecraft/world/level/Level;Ldev/ryanhcode/sable/sublevel/SubLevel;Ldev/ryanhcode/sable/sublevel/SubLevel;Ljava/lang/Double;Z)D"))
  private static double fourthdimension$spacing(Level level, SubLevel first, SubLevel second,
      Double partialTick, boolean suppressSnap) {
    return ConsistRules.chainLinkDistance(level,first,second,partialTick,suppressSnap);
  }
}
