package dev.fourthdimension.gametest;

import dev.fourthdimension.vehicle.BankedRecoveryExamples;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.neoforge.event.ForgeSablePostPhysicsTickEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3d;

/** Fixture-only native pose capture after each Sable Rapier physics substep. */
final class RecoverySubstepTrace {
  private static boolean registered;
  private static RecoverySubstepTrace active;

  private final ServerLevel level;
  private final List<UUID> bodyIds;
  private final BufferedWriter writer;
  private long steps;

  RecoverySubstepTrace(ServerLevel level, BankedRecoveryExamples.Example example) {
    if (active != null) throw new IllegalStateException("A substep trace is already active");
    if (!registered) {
      NeoForge.EVENT_BUS.addListener(RecoverySubstepTrace::postPhysicsTick);
      registered = true;
    }
    this.level = level;
    var ids = new ArrayList<UUID>();
    for (var car : example.cars()) {
      ids.add(car.chassis);
      car.carriers.forEach(carrier -> ids.add(carrier.body));
    }
    ids.add(example.recovery().bearing().getUniqueId());
    bodyIds = List.copyOf(ids);
    try {
      writer =
          new BufferedWriter(
              new java.io.OutputStreamWriter(
                  new GZIPOutputStream(
                      Files.newOutputStream(
                          net.neoforged.fml.loading.FMLPaths.GAMEDIR
                              .get()
                              .resolve("recovery-bank-train-arrow-substeps.jsonl.gz"))),
                  java.nio.charset.StandardCharsets.UTF_8));
    } catch (IOException failure) {
      throw new UncheckedIOException(failure);
    }
    write(Map.of("type", "trace", "family", "ARROW", "bodyIds", bodyIds));
    active = this;
  }

  private static void postPhysicsTick(ForgeSablePostPhysicsTickEvent event) {
    var trace = active;
    if (trace == null || event.getPhysicsSystem().getLevel() != trace.level) return;
    var container = SubLevelContainer.getContainer(trace.level);
    var poses = new ArrayList<Map<String, Object>>(trace.bodyIds.size());
    for (var id : trace.bodyIds) {
      var body = container.getSubLevel(id);
      if (body == null) throw new IllegalStateException("Substep trace lost a native body: " + id);
      var center = body.getPlot().getCenterBlock();
      var pose = body.logicalPose();
      var origin =
          pose.transformPosition(
              new Vector3d(center.getX() + .5, center.getY() + .5, center.getZ() + .5));
      var axes = new ArrayList<double[]>(3);
      for (var axis : List.of(new Vector3d(1, 0, 0), new Vector3d(0, 1, 0), new Vector3d(0, 0, 1)))
        axes.add(vector(axis.rotate(pose.orientation())));
      poses.add(Map.of("id", id.toString(), "origin", vector(origin), "axes", axes));
    }
    trace.write(
        Map.of(
            "type", "substep",
            "step", ++trace.steps,
            "gameTime", trace.level.getGameTime(),
            "partialPhysicsTick", event.getPhysicsSystem().getPartialPhysicsTick(),
            "timeStep", event.getTimeStep(),
            "poses", poses));
  }

  void complete() {
    if (active != this) throw new IllegalStateException("Substep trace was replaced");
    write(Map.of("type", "complete", "steps", steps));
    active = null;
    try {
      writer.close();
    } catch (IOException failure) {
      throw new UncheckedIOException(failure);
    }
  }

  private void write(Map<String, Object> entry) {
    try {
      writer.write(new com.google.gson.Gson().toJson(entry));
      writer.newLine();
    } catch (IOException failure) {
      throw new UncheckedIOException(failure);
    }
  }

  private static double[] vector(Vector3d value) {
    return new double[] {value.x, value.y, value.z};
  }
}
