package dev.fourthdimension.integration;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.runtime.FoundationSync;
import dev.fourthdimension.track.ProfileEditor;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;

/**
 * Cosmetic capabilities are separate from functional providers and never grant track eligibility.
 */
public final class AppearanceProviders {
  public static final String TRACK_STYLES = "coasterssimulatedextratypes:spines";

  public record Definition(
      TrackAppearance appearance,
      String label,
      Set<Family> families,
      Set<TrackAppearance.Component> components) {
    public Definition {
      families = Set.copyOf(families);
      components = Set.copyOf(components);
    }
  }

  private static final Map<TrackAppearance, Definition> STYLES = new LinkedHashMap<>();
  private static boolean frozen;

  private AppearanceProviders() {}

  public static synchronized void register(Definition style) {
    if (frozen) throw new IllegalStateException("Appearance registration is closed");
    if (style.appearance().equals(TrackAppearance.ORIGINAL)
        || STYLES.putIfAbsent(style.appearance(), style) != null)
      throw new IllegalArgumentException("Duplicate or reserved appearance");
  }

  public static synchronized void setup() {
    if (OptionalIntegrations.supported("coasterssimulatedextratypes", Set.of("1.2.0"))) {
      for (String style : List.of("arrow_4d", "arrow_4d_dbl_spine"))
        register(
            new Definition(
                new TrackAppearance(TRACK_STYLES, "coasterssimulatedextratypes:" + style),
                style.equals("arrow_4d") ? "Arrow single spine" : "Arrow double spine",
                Set.of(Family.ARROW),
                Set.of(TrackAppearance.Component.SPINE)));
    }
    frozen = true;
  }

  public static Definition definition(TrackAppearance appearance) {
    return STYLES.get(appearance);
  }

  public static List<Definition> choices(Family family) {
    var choices = new ArrayList<Definition>();
    choices.add(
        new Definition(
            TrackAppearance.ORIGINAL, "Original family style", Set.of(family), Set.of()));
    STYLES.values().stream().filter(s -> s.families().contains(family)).forEach(choices::add);
    return List.copyOf(choices);
  }

  public static void apply(ServerPlayer player, UUID id, long revision, String styleId) {
    var rail = ProfileEditor.requireEditable(player, id, revision);
    var style =
        choices(rail.family).stream()
            .filter(s -> s.appearance().style().equals(styleId))
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException("Appearance unavailable for this family"));
    rail.appearance = style.appearance();
    rail.revision++;
    FoundationData.get(player.serverLevel()).setDirty();
    FoundationSync.broadcast(player.serverLevel());
  }
}
