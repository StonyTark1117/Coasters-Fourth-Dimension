package dev.fourthdimension.mixin;

import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Remove only the inspected recovery pair, preserving links between the original cars. */
@Mixin(value = CoasterCartTrainLinkConstraint.class, remap = false)
public interface RecoveryLinkAccess {
  @Invoker("unlinkPair")
  static void fourthdimension$unlink(ServerLevel level, UUID a, UUID b) {
    throw new AssertionError("Mixin invoker was not applied");
  }
}
