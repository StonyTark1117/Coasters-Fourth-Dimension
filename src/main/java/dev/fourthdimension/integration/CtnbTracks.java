package dev.fourthdimension.integration;

import com.simibubi.create.content.trains.track.TrackMaterial;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.silvergold.simulatedcoasters.CoasterTrackMaterials;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** Explicit measured N-contact material set; a texture name never grants eligibility. */
public final class CtnbTracks {
  public static final Set<String> MATERIALS = Set.of("boost_track", "powered_boost_track",
      "station_track", "launch_track", "reverse_track", "brake_track", "sensor_track");
  private CtnbTracks() {}
  public static boolean selected(TrackMaterial material) {
    return material != null && material.id.getNamespace().equals(OptionalIntegrations.CTNB)
        && MATERIALS.contains(material.id.getPath())
        && OptionalIntegrations.installed(OptionalIntegrations.CTNB).mayInspect();
  }
  public static boolean nativeContacts(TrackMaterial material) {
    return material != null && (CoasterTrackMaterials.COASTER.id.toString().equals(material.id.toString()) || selected(material));
  }
  public static boolean supports(FoundationData.Vehicle v) {
    return WingVehicle.is(v) || v.family == Family.ARROW && TrackDesign.ARROW.variant.equals(v.variant);
  }
  public static TrackMaterial material(net.minecraft.world.level.Level level, CoasterPathEdge edge) {
    if (level.hasChunkAt(edge.from()) && level.getBlockEntity(edge.from()) instanceof dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity anchor) {
      var curve=anchor.getAnchorPeerCurvesView().get(edge.to());
      if(curve!=null)return curve.getMaterial();
    }
    return edge.bezier()==null?null:edge.bezier().getMaterial();
  }
  public static boolean eligible(net.minecraft.world.level.Level level, FoundationData.Vehicle v, CoasterPathEdge edge) {
    if (RideAuthority.CTNB.equals(v.authority.provider) && v.boundFrom != null
        && !v.boundFrom.equals(edge.from()) && !v.boundFrom.equals(edge.to())
        && !v.boundTo.equals(edge.from()) && !v.boundTo.equals(edge.to())) return false;
    if (edge.bezier() == null) return true;
    var material = material(level,edge);
    return !material.id.getNamespace().equals(OptionalIntegrations.CTNB)
        || selected(material) && supports(v);
  }
  /** Repainting keeps section IDs, family and profiles; parked/occupied approaches cannot mutate. */
  public static boolean mayRepaint(ServerLevel level, BlockPos from, BlockPos to, TrackMaterial material) {
    var data = FoundationData.get(level);
    var rails = data.rails.stream().filter(r -> r.matches(from,to)).toList();
    for (var v : data.vehicles.values()) {
      if (v.boundFrom == null) continue;
      if (Set.of(from,to).contains(v.boundFrom) || Set.of(from,to).contains(v.boundTo)) return false;
    }
    if (rails.isEmpty()) return true;
    return nativeContacts(material) && rails.stream().allMatch(r -> r.family == Family.ARROW
        && r.variant.equals(TrackDesign.ARROW.variant));
  }
  public static void repainted(ServerLevel level, BlockPos from, BlockPos to) {
    var data=FoundationData.get(level);
    TrackGeometry.invalidate(level);
    for (var rail:data.rails) if(rail.matches(from,to)) {
      var edge=TrackGeometry.resolve(level,rail);
      if(edge!=null && nativeContacts(edge.bezier().getMaterial()))
        TrackPlacement.captureOriginalCurve(rail,edge.bezier());
      rail.revision++;
      data.setDirty();
    }
    FoundationSync.broadcast(level);
  }
}
