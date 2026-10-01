package dev.fourthdimension.integration;

import dev.fourthdimension.runtime.FoundationData;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import java.util.*;
import net.minecraft.server.level.ServerLevel;

/**
 * Add only this cart's rotary bodies, leaving native rivets and per-cart motor ownership intact.
 */
public final class EngineeredAssembly {
  private EngineeredAssembly() {}

  public static List<ServerSubLevel> includeCarriers(
      ServerLevel level, ServerSubLevel chassis, List<ServerSubLevel> nativeBodies) {
    if (!OptionalIntegrations.installed(OptionalIntegrations.ENGINEERED).mayInspect())
      return nativeBodies;
    var vehicle = FoundationData.get(level).vehicles.get(chassis.getUniqueId());
    if (vehicle == null || FoundationData.VARIANT.equals(vehicle.variant)) return nativeBodies;
    var container = SubLevelContainer.getContainer(level);
    var result = new LinkedHashMap<UUID, ServerSubLevel>();
    for (var body : nativeBodies) result.put(body.getUniqueId(), body);
    for (var carrier : vehicle.carriers)
      if (container.getSubLevel(carrier.body) instanceof ServerSubLevel body && !body.isRemoved())
        result.putIfAbsent(body.getUniqueId(), body);
    return List.copyOf(result.values());
  }
}
