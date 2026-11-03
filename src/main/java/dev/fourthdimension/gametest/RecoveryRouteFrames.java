package dev.fourthdimension.gametest;

import dev.fourthdimension.physics.ArrowDecorationPlacement;
import dev.fourthdimension.physics.ModelFrame;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.BankedRecoveryExamples;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/** Exact native frames for independent geometry and later mesh-clearance audits. */
final class RecoveryRouteFrames {
  private RecoveryRouteFrames() {}

  static void write(
      ServerLevel level, String fileName, List<BankedRecoveryExamples.Example> examples) {
    var geometry = new ArrayList<Map<String, Object>>();
    for (var example : examples) {
      var design = TrackDesign.byVariant(example.recovery().car().variant);
      var sections = new ArrayList<Map<String, Object>>();
      for (var rail : example.rails()) {
        var edge = TrackGeometry.resolve(level, rail);
        var table = TrackGeometry.table(level, edge);
        boolean canonical = edge.from().equals(rail.from);
        var renderProfile = canonical ? rail.profile : rail.profile.reversed();
        var frames = new ArrayList<Map<String, Object>>();
        int samples = Math.max(8, (int) Math.ceil(table.length() * 4));
        for (int i = 0; i <= samples; i++) {
          double distance = table.length() * i / samples;
          double t = table.parameterAt(distance);
          var point = CoasterPathGraphManager.sampleEdgePosition(level, edge, t);
          var frame =
              CoasterPathTrackFrame.trackBasisAtHit(
                  level, new CoasterPathTrackFrame.GraphHit(point, edge, t));
          frames.add(
              Map.of(
                  "distance",
                  distance,
                  "center",
                  vector(point),
                  "right",
                  vector(frame.right()),
                  "up",
                  vector(frame.up()),
                  "forward",
                  vector(frame.forward()),
                  "profileAngle",
                  rail.profile.angleAt(distance),
                  "renderProfileAngle",
                  renderProfile.angleAt(distance)));
        }
        // Potential decoration placement inputs, not a claim that a provider/model rendered.
        // Keep these distances identical to TrackStylesRenderer.build; no grid interpolation.
        var spines = new ArrayList<Map<String, Object>>();
        var ties = new ArrayList<Map<String, Object>>();
        int segments = Math.max(1, (int) Math.ceil(table.length() * 2));
        double step = table.length() / segments;
        for (int i = 0; i < segments; i++) {
          double distance = (i + .5) * step;
          spines.add(
              decoration(level, edge, rail, renderProfile, distance, table.parameterAt(distance), true, step));
        }
        for (double distance = 0; distance <= table.length() + 1e-6; distance += 2) {
          double d = Math.min(distance, table.length());
          ties.add(decoration(level, edge, rail, renderProfile, d, table.parameterAt(d), false, step));
        }
        var section = new LinkedHashMap<String, Object>();
        section.put("rail", rail.id.toString());
        section.put("length", table.length());
        section.put("frames", frames);
        section.put("family", rail.family.id());
        section.put("variant", rail.variant);
        section.put("geometryHash", Long.toString(rail.geometryHash));
        section.put("revision", rail.revision);
        section.put("valid", rail.valid);
        section.put("profile", rail.profile.keys());
        section.put("renderProfile", renderProfile.keys());
        section.put("canonical", canonical);
        section.put("railFrom", position(rail.from));
        section.put("railTo", position(rail.to));
        section.put("edgeFrom", position(edge.from()));
        section.put("edgeTo", position(edge.to()));
        section.put("appearance", Map.of("id", rail.appearance.id(), "tint", rail.appearance.tint()));
        section.put(
            "decorationInputs",
            Map.of(
                "schema", 1,
                "scope", "potential-renderer-inputs-only; no model availability or clearance claim",
                "matrixLayout", "column-major-model-to-world",
                "spines", spines,
                "ties", ties));
        sections.add(section);
      }
      geometry.add(
          Map.of(
              "family",
              design.name(),
              "variant",
              design.variant,
              "sections",
              sections,
              "carCount",
              example.cars().size(),
              "chassis",
              example.cars().stream().map(car -> car.chassis.toString()).toList(),
              "dimension",
              level.dimension().location().toString(),
              "gameTime",
              level.getGameTime()));
    }
    try {
      Files.writeString(
          net.neoforged.fml.loading.FMLPaths.GAMEDIR.get().resolve(fileName),
          new com.google.gson.Gson().toJson(geometry));
    } catch (IOException failedExport) {
      throw new UncheckedIOException(failedExport);
    }
  }

  private static Map<String, Object> decoration(
      ServerLevel level,
      CoasterPathEdge edge,
      FoundationData.Rail rail,
      RotationProfile renderProfile,
      double distance,
      double parameter,
      boolean spine,
      double step) {
    var center = CoasterPathGraphManager.sampleEdgePosition(level, edge, parameter);
    var basis =
        CoasterPathTrackFrame.trackBasisAtHit(
            level, new CoasterPathTrackFrame.GraphHit(center, edge, parameter));
    var up = basis.up();
    var forward = basis.forward();
    var transform =
        ModelFrame.at(
            center.x,
            center.y,
            center.z,
            new org.joml.Vector3d(forward.x, forward.y, forward.z),
            new org.joml.Vector3d(up.x, up.y, up.z));
    // Reuse the production model-origin/scale transforms, including float step rounding.
    if (spine) ArrowDecorationPlacement.spine(transform, (float) step);
    else ArrowDecorationPlacement.tie(transform);
    var input = new LinkedHashMap<String, Object>();
    input.put("distance", distance);
    input.put("parameter", parameter);
    input.put("center", vector(center));
    input.put("right", vector(basis.right()));
    input.put("up", vector(up));
    input.put("forward", vector(forward));
    input.put("profileAngle", rail.profile.angleAt(distance));
    input.put("renderProfileAngle", renderProfile.angleAt(distance));
    input.put(
        "scale",
        spine
            ? new float[] {1, 1, (float) step}
            : new float[] {ArrowDecorationPlacement.TIE_SCALE_X, 1, 1});
    input.put("modelTransform", transform.get(new float[16]));
    return input;
  }

  private static int[] position(net.minecraft.core.BlockPos value) {
    return new int[] {value.getX(), value.getY(), value.getZ()};
  }

  private static double[] vector(Vec3 value) {
    return new double[] {value.x, value.y, value.z};
  }
}
