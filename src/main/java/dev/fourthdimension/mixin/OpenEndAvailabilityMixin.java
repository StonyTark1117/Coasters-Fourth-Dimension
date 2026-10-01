package dev.fourthdimension.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** The global physics endpoint scan must not keep inactive 4D anchor chunks loaded. */
@Mixin(
    targets = "dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrackHitCache",
    remap = false)
public abstract class OpenEndAvailabilityMixin {
  @WrapOperation(
      method = "openEndRefs",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Ldev/silvergold/simulatedcoasters/track/graph/CoasterPathTraceEndpoints;pathOpenEndRefs(Lnet/minecraft/world/level/Level;Ldev/silvergold/simulatedcoasters/track/graph/CoasterPathGraph;Ljava/util/Set;)Ljava/util/List;"))
  private static List<CoasterPathTraceEndpoints.OpenEndRef> fourthdimension$loadedEndpoints(
      Level level,
      CoasterPathGraph graph,
      Set<Integer> indices,
      Operation<List<CoasterPathTraceEndpoints.OpenEndRef>> original) {
    return dev.fourthdimension.track.TrackEndpointAvailability.sample(
        level, graph, indices, original::call);
  }
}
