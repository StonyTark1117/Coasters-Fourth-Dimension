package dev.fourthdimension.integration;

import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.runtime.FoundationSync;
import dev.fourthdimension.track.ProfileEditor;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

/** Uses the ordinary server-side track editing permissions/revision/occupancy transaction. */
public final class AppearanceEditor {
  private AppearanceEditor() {}

  public static void apply(
      ServerPlayer player, UUID id, long revision, TrackAppearance appearance) {
    var rail = ProfileEditor.requireEditable(player, id, revision);
    if (!appearance.supports(rail.family))
      throw new IllegalArgumentException("This decorative style fits controlled Arrow track only");
    if (!appearance.original()
        && (!TrackAppearance.CANDIDATES.contains(appearance.id())
            || !OptionalIntegrations.installed(OptionalIntegrations.TRACK_STYLES).mayInspect()))
      throw new IllegalArgumentException(
          "This appearance needs the inspected Track Styles 1.2.0 release");
    rail.appearance = appearance;
    rail.revision++;
    FoundationData.get(player.serverLevel()).setDirty();
    FoundationSync.broadcast(player.serverLevel());
  }
}
