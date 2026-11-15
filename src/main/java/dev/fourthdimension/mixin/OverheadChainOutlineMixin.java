package dev.fourthdimension.mixin;

import dev.fourthdimension.vehicle.ConsistRules;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.silvergold.simulatedcoasters.client.cart.CoasterCartChainLinkOutlineClient;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartChainLinkInteraction;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** The normal outline, spacing hint and click sound use the same eligibility as the server. */
@Mixin(value=CoasterCartChainLinkOutlineClient.class, remap=false)
public abstract class OverheadChainOutlineMixin {
  @Redirect(method="renderFrame", at=@At(value="INVOKE", target="Ldev/silvergold/simulatedcoasters/track/cart/CoasterCartLinkPlacement;rejectsLinkDistance(D)Z"))
  private static boolean fourthdimension$preview(double distance, Minecraft client, float partialTick) {
    var target=CoasterCartChainLinkInteraction.resolveHoveredCart(client.level,client.hitResult);
    return ConsistRules.chainDistanceRejected(distance,client.level,client.player.getMainHandItem(),target);
  }

  @Redirect(method="playDenyIfCoupleBlocked", at=@At(value="INVOKE", target="Ldev/silvergold/simulatedcoasters/track/cart/CoasterCartLinkPlacement;rejectsLinkDistance(D)Z"))
  private static boolean fourthdimension$feedback(double distance, ItemStack chain, SubLevel target) {
    return ConsistRules.chainDistanceRejected(distance,target.getLevel(),chain,target);
  }

  @Redirect(method="renderFrame", at=@At(value="INVOKE", target="Ldev/silvergold/simulatedcoasters/track/cart/CoasterCartLinkPlacement;resolveChainLinkDistance(Lnet/minecraft/world/level/Level;Ldev/ryanhcode/sable/sublevel/SubLevel;Ldev/ryanhcode/sable/sublevel/SubLevel;Ljava/lang/Double;Z)D"))
  private static double fourthdimension$spacing(Level level, SubLevel first, SubLevel second,
      Double partialTick, boolean suppressSnap) {
    return ConsistRules.chainLinkDistance(level,first,second,partialTick,suppressSnap);
  }
}
