package dev.fourthdimension.track;

import com.simibubi.create.content.trains.track.BezierConnection;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.*;
import dev.silvergold.simulatedcoasters.CoasterTrackMaterials;
import dev.silvergold.simulatedcoasters.track.CoasterTrackPlacement;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public final class TrackPlacement {
  private TrackPlacement() {}

  public static void checkEndpoint(ServerLevel level, BlockPos pos, Player player) {
    if (!player.mayBuild() || !level.mayInteract(player, pos))
      throw new IllegalArgumentException("Cannot build at this anchor");
    if (!level.hasChunkAt(pos)
        || !(level.getBlockEntity(pos) instanceof CoasterAnchorpointBlockEntity))
      throw new IllegalArgumentException("Use a Coasters anchor as the endpoint");
    if (Math.abs((long) pos.getX()) > 1024 || Math.abs((long) pos.getZ()) > 1024)
      throw new IllegalArgumentException(
          "Native physics currently requires building within 1024 blocks of origin");
  }

  public static BezierConnection preview(Level level, BlockPos from, BlockPos to) {
    if (from.equals(to) || from.distSqr(to) > 64 * 64)
      throw new IllegalArgumentException("Track spans must be distinct and at most 64 blocks");
    if (!level.hasChunkAt(from) || !level.hasChunkAt(to))
      throw new IllegalArgumentException("Both anchors must be loaded");
    if (!(level.getBlockEntity(from) instanceof CoasterAnchorpointBlockEntity a)
        || !(level.getBlockEntity(to) instanceof CoasterAnchorpointBlockEntity b))
      throw new IllegalArgumentException("Both endpoints must be coaster anchors");
    if (a.hasAnchorPeer(to))
      throw new IllegalArgumentException(
          "Remove the existing connection before placing another track");
    if (!a.canAcceptAnchorPeer(to) || !b.canAcceptAnchorPeer(from))
      throw new IllegalArgumentException("An anchor has no free connection");
    var curve =
        CoasterTrackPlacement.buildAnchorAnchorBezier(
            level, from, to, CoasterTrackMaterials.COASTER, false);
    if (curve == null) throw new IllegalArgumentException("Cannot construct this curve");
    return curve;
  }

  public static FoundationData.Rail connect(
      ServerLevel level,
      BlockPos from,
      BlockPos to,
      TrackDesign design,
      Player player,
      ItemStack held) {
    checkEndpoint(level, from, player);
    checkEndpoint(level, to, player);
    if (player.distanceToSqr(to.getCenter()) > 64
        || player.distanceToSqr(from.getCenter()) > 72 * 72)
      throw new IllegalArgumentException("Track placement is out of reach");
    var data = FoundationData.get(level);
    data.checkProfileCapacity(2);
    if (data.rails.size() >= 256) throw new IllegalStateException("Track capacity reached");
    for (var rail : data.rails)
      if (rail.from.equals(from)
          || rail.to.equals(from)
          || rail.from.equals(to)
          || rail.to.equals(to)) {
        if (occupied(data, rail))
          throw new IllegalArgumentException(
              "Connected track is occupied; remove vehicles before editing geometry");
        if (rail.matches(from, to)) continue;
        if (!rail.variant.equals(design.variant))
          throw new IllegalArgumentException("Track families cannot share a junction");
        for (var endpoint : List.of(from, to))
          if (endpoint.equals(rail.from) || endpoint.equals(rail.to)) {
            var key =
                endpoint.equals(rail.from)
                    ? rail.profile.keys().getFirst()
                    : rail.profile.keys().getLast();
            if (Math.abs(key.angle()) > 1e-7 || Math.abs(key.slope()) > 1e-7)
              throw new IllegalArgumentException(
                  "Return the adjoining profile to zero angle and slope before extending the"
                      + " track");
          }
      }
    var curve = preview(level, from, to);
    if (!CoasterTrackPlacement.validateCoasterCurveForPlacement(level, curve, null))
      throw new IllegalArgumentException(
          "Track intersects an invalid area or has unsuitable geometry");
    var edge =
        CoasterPathEdge.curve(curve.bePositions.getFirst(), curve.bePositions.getSecond(), curve);
    double length = TrackGeometry.table(level, edge).length();
    int cost = (int) Math.ceil(length / 4);
    int available = 0;
    for (var stack : player.getInventory().items)
      if (stack.is(held.getItem())) available += stack.getCount();
    if (!player.isCreative() && available < cost)
      throw new IllegalArgumentException("Requires " + cost + " matching track items");
    var rail =
        new FoundationData.Rail(
            edge.from(),
            edge.to(),
            design.family,
            new RotationProfile(
                List.of(new RotationProfile.Key(0, 0), new RotationProfile.Key(length, 0))),
            UUID.randomUUID(),
            design.variant,
            TrackGeometry.fingerprint(level, edge),
            0,
            true);
    ((CoasterAnchorpointBlockEntity) level.getBlockEntity(edge.from()))
        .putAnchorPeerCurve(level, edge.to(), curve);
    data.rails.removeIf(old -> old.matches(from, to));
    data.rails.add(rail);
    // The base mod records native handle lengths and rebuilds its graph during insertion.
    var storedEdge = TrackGeometry.resolve(level, rail);
    TrackGeometry.invalidate(level);
    rail.profile = rail.profile.scaledTo(TrackGeometry.table(level, storedEdge).length());
    rail.geometryHash = TrackGeometry.fingerprint(level, storedEdge);
    data.setDirty();
    if (!player.isCreative())
      for (var stack : player.getInventory().items)
        if (stack.is(held.getItem())) {
          int take = Math.min(cost, stack.getCount());
          stack.shrink(take);
          cost -= take;
          if (cost == 0) break;
        }
    FoundationSync.broadcast(level);
    return rail;
  }

  public static boolean occupied(FoundationData data, FoundationData.Rail rail) {
    return data.vehicles.values().stream().anyMatch(v -> rail.matches(v.boundFrom, v.boundTo));
  }

  /**
   * Curves altered by upstream handles keep their identity but require an explicit profile rebind.
   */
  public static void tick(LevelTickEvent.Post event) {
    if (!(event.getLevel() instanceof ServerLevel level) || level.getGameTime() % 10 != 0) return;
    var data = FoundationData.get(level);
    boolean changed = false;
    for (var rail : data.rails)
      if (rail.original()
          && rail.valid
          && level.hasChunkAt(rail.from)
          && level.hasChunkAt(rail.to)) {
        var edge = TrackGeometry.resolve(level, rail);
        if (edge == null || TrackGeometry.fingerprint(level, edge) != rail.geometryHash) {
          rail.valid = false;
          rail.revision++;
          changed = true;
        }
      }
    if (changed) {
      TrackGeometry.invalidate(level);
      data.setDirty();
      FoundationSync.broadcast(level);
    }
  }
}
