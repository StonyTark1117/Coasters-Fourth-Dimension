package dev.fourthdimension.mixin;

import dev.fourthdimension.runtime.FoundationData;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import java.util.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A rotary joint alone is not a Sable loading dependency; retain the entire vehicle together. */
@Mixin(value = SubLevelHelper.class, remap = false)
public abstract class LoadingDependencyMixin {
  @Inject(method = "getLoadingDependencyChain", at = @At("RETURN"), cancellable = true)
  private static void fourthdimension$vehicleGroup(
      ServerSubLevel sub, CallbackInfoReturnable<Collection<ServerSubLevel>> cir) {
    var level = sub.getLevel();
    var container = SubLevelContainer.getContainer(level);
    var result = new HashSet<>(cir.getReturnValue());
    Set<UUID> ids = new HashSet<>();
    result.forEach(body -> ids.add(body.getUniqueId()));
    boolean changed;
    do {
      changed = false;
      for (var v : FoundationData.get(level).vehicles.values()) {
        if (!ids.contains(v.chassis)
            && v.carriers.stream().noneMatch(c -> ids.contains(c.body))
            && (v.recoveryCart == null || !ids.contains(v.recoveryCart))) continue;
        var members = new ArrayList<UUID>();
        members.add(v.chassis);
        if (v.recoveryCart != null) members.add(v.recoveryCart);
        v.carriers.forEach(c -> members.add(c.body));
        for (var id : members)
          if (container.getSubLevel(id) instanceof ServerSubLevel member && ids.add(id)) {
            result.add(member);
            changed = true;
          }
      }
    } while (changed);
    cir.setReturnValue(result);
  }
}
