package dev.fourthdimension.client.integration;

import com.mojang.logging.LogUtils;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.fourthdimension.integration.OptionalIntegrations;
import dev.fourthdimension.integration.TrackRenderOwnership;
import java.util.HashMap;
import java.util.Map;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

/**
 * Client-only, version-fenced access to a separately installed release's public model catalog.
 * Catalog discovery alone does not certify any style's geometry or enable its rendering.
 */
@EventBusSubscriber(modid = "fourthdimension", value = Dist.CLIENT)
public final class TrackStylesCatalog {
  public record Decoration(PartialModel spine, PartialModel ties) {
    public TrackRenderOwnership ownership() {
      return TrackRenderOwnership.decoration("coasterssimulatedextratypes:decoration");
    }
  }

  private static Map<String, Decoration> catalog = Map.of();
  private static long generation;

  private TrackStylesCatalog() {}

  public static Map<String, Decoration> snapshot() {
    return catalog;
  }

  public static long generation() {
    return generation;
  }

  @SubscribeEvent
  public static void modelsBaked(ModelEvent.BakingCompleted event) {
    catalog = Map.of();
    generation++;
    var installed = OptionalIntegrations.installed(OptionalIntegrations.TRACK_STYLES);
    if (!installed.mayInspect()) return;
    try {
      var models = Class.forName("top.claebcode.coasterssimulatedextratypes.MyTrackModels");
      var components =
          Class.forName("top.claebcode.coasterssimulatedextratypes.MyTrackModels$TrackComponents");
      var spine = components.getMethod("centerBeam");
      var ties = components.getMethod("tie");
      var field = models.getField("STYLES").get(null);
      if (!(field instanceof Map<?, ?> styles))
        throw new ReflectiveOperationException("Unexpected Track Styles catalog type");
      var found = new HashMap<String, Decoration>();
      for (var entry : styles.entrySet()) {
        if (!(entry.getKey() instanceof String name) || !components.isInstance(entry.getValue()))
          throw new ReflectiveOperationException("Unexpected Track Styles component entry");
        if (!(spine.invoke(entry.getValue()) instanceof PartialModel beam)
            || !(ties.invoke(entry.getValue()) instanceof PartialModel tie))
          throw new ReflectiveOperationException("Missing decorative model components");
        found.put(name, new Decoration(beam, tie));
      }
      catalog = Map.copyOf(found);
      LogUtils.getLogger()
          .info(
              "FD_OPTIONAL Track Styles {}: inspected {} decorative model entries; geometry"
                  + " certification pending",
              installed.version(),
              catalog.size());
    } catch (ReflectiveOperationException | LinkageError | RuntimeException failure) {
      // Never leave a partially usable catalog or remove the original appearance on failure.
      catalog = Map.of();
      LogUtils.getLogger()
          .warn(
              "FD_OPTIONAL Track Styles catalog unavailable; original appearance retained",
              failure);
    }
  }
}
