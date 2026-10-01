package dev.fourthdimension.gametest;

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
                  rail.profile.angleAt(distance)));
        }
        sections.add(
            Map.of("rail", rail.id.toString(), "length", table.length(), "frames", frames));
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
              example.cars().size()));
    }
    try {
      Files.writeString(
          net.neoforged.fml.loading.FMLPaths.GAMEDIR.get().resolve(fileName),
          new com.google.gson.Gson().toJson(geometry));
    } catch (IOException failedExport) {
      throw new UncheckedIOException(failedExport);
    }
  }

  private static double[] vector(Vec3 value) {
    return new double[] {value.x, value.y, value.z};
  }
}
