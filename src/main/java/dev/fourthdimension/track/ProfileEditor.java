package dev.fourthdimension.track;

import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.*;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;

/** All UI and command edits go through this server-side transaction. */
public final class ProfileEditor {
  private ProfileEditor() {}

  public static FoundationData.Rail requireEditable(ServerPlayer player, UUID id, long revision) {
    var data = FoundationData.get(player.serverLevel());
    var rail =
        data.rails.stream()
            .filter(r -> r.id.equals(id))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Track no longer exists"));
    if (!player.mayBuild()
        || !player.serverLevel().mayInteract(player, rail.from)
        || !player.serverLevel().mayInteract(player, rail.to))
      throw new IllegalArgumentException("No permission to edit this track");
    if (Math.min(
            player.distanceToSqr(rail.from.getCenter()), player.distanceToSqr(rail.to.getCenter()))
        > 64)
      throw new IllegalArgumentException("Stand within eight blocks of the selected section");
    if (rail.revision != revision)
      throw new IllegalArgumentException("Track changed; select it again");
    if (!rail.original())
      throw new IllegalArgumentException("Foundation fixtures are not editable content");
    // A connected occupied axle can straddle this section even while its binding is on its
    // neighbor.
    for (var candidate : data.rails)
      if (candidate == rail || candidate.adjacent(rail))
        if (TrackPlacement.occupied(data, candidate))
          throw new IllegalArgumentException("Track or its approach is occupied");
    return rail;
  }

  public static void apply(
      ServerPlayer player, UUID id, long revision, RotationProfile proposed, boolean rebind) {
    var level = player.serverLevel();
    var rail = requireEditable(player, id, revision);
    FoundationData.get(level)
        .checkProfileCapacity(proposed.keys().size() - rail.profile.keys().size());
    if (!rail.family.driven())
      throw new IllegalArgumentException("Free-spin tracks do not prescribe seat angles");
    var edge = TrackGeometry.resolve(level, rail);
    if (edge == null || CoasterPathGraphManager.edgeHostedOnSubLevel(level, edge))
      throw new IllegalArgumentException("Track must be present on fixed world anchors");
    long hash = TrackGeometry.fingerprint(level, edge);
    if (!rebind && (!rail.valid || hash != rail.geometryHash))
      throw new IllegalArgumentException(
          "Geometry changed; explicitly rebind before editing the profile");
    TrackGeometry.invalidate(level);
    double length = TrackGeometry.table(level, edge).length();
    if (proposed.keys().getFirst().distance() != 0
        || Math.abs(proposed.keys().getLast().distance() - length) > 1e-4)
      throw new IllegalArgumentException(
          "Profile keys must cover the entire section length: " + length);
    for (var neighbor : FoundationData.get(level).rails)
      if (neighbor != rail && neighbor.valid && neighbor.adjacent(rail)) {
        if (!neighbor.variant.equals(rail.variant))
          throw new IllegalArgumentException("Incompatible family at junction");
        for (var endpoint : List.of(rail.from, rail.to))
          if (endpoint.equals(neighbor.from) || endpoint.equals(neighbor.to)) {
            var a =
                endpoint.equals(rail.from) ? proposed.keys().getFirst() : proposed.keys().getLast();
            var b =
                endpoint.equals(neighbor.from)
                    ? neighbor.profile.keys().getFirst()
                    : neighbor.profile.keys().getLast();
            double aOut = endpoint.equals(rail.from) ? a.slope() : -a.slope();
            double bOut = endpoint.equals(neighbor.from) ? b.slope() : -b.slope();
            if (Math.abs(a.angle() - b.angle()) > 1e-7 || Math.abs(aOut + bOut) > 1e-7)
              throw new IllegalArgumentException(
                  "Junction must match unwrapped angle, rail displacement and slope");
          }
      }
    rail.profile = proposed;
    rail.geometryHash = hash;
    rail.valid = true;
    rail.revision++;
    FoundationData.get(level).setDirty();
    FoundationSync.broadcast(level);
  }

  public static void rebind(ServerPlayer player, UUID id, long revision) {
    var rail = requireEditable(player, id, revision);
    var edge = TrackGeometry.resolve(player.level(), rail);
    if (edge == null)
      throw new IllegalArgumentException(
          "Restore this section's original anchors before rebinding");
    TrackGeometry.invalidate(player.level());
    if (rail.family.driven())
      apply(
          player,
          id,
          revision,
          rail.profile.scaledTo(TrackGeometry.table(player.level(), edge).length()),
          true);
    else {
      rail.geometryHash = TrackGeometry.fingerprint(player.level(), edge);
      rail.valid = true;
      rail.revision++;
      FoundationData.get(player.serverLevel()).setDirty();
      FoundationSync.broadcast(player.serverLevel());
    }
  }
}
