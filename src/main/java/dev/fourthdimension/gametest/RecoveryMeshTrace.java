package dev.fourthdimension.gametest;

import dev.fourthdimension.vehicle.BankedRecoveryExamples;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.SubLevel;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3d;

/** Read-only native block/state, pose and mounted-rider samples for an independent mesh audit. */
final class RecoveryMeshTrace {
  private final Path path;
  private final Set<UUID> registered = new HashSet<>();

  RecoveryMeshTrace(String fileName) {
    path = net.neoforged.fml.loading.FMLPaths.GAMEDIR.get().resolve(fileName);
    try {
      Files.writeString(path, "");
    } catch (IOException failure) {
      throw new UncheckedIOException(failure);
    }
  }

  void sample(ServerLevel level, BankedRecoveryExamples.Example example, long tick) {
    var container = SubLevelContainer.getContainer(level);
    var poses = new ArrayList<Map<String, Object>>();
    for (var car : example.cars()) {
      record(poses, container.getSubLevel(car.chassis), car.chassis, "chassis");
      for (var carrier : car.carriers)
        record(poses, container.getSubLevel(carrier.body), car.chassis, "carrier");
    }
    var ordinary = example.recovery().bearing();
    record(poses, ordinary, ordinary.getUniqueId(), "ordinary");
    var riders = new ArrayList<Map<String, Object>>();
    for (var car : example.cars())
      for (var carrier : car.carriers)
        recordMountedPlayers(riders, container.getSubLevel(carrier.body), carrier.seats);
    append(
        Map.of(
            "type", "sample", "family", example.recovery().car().family.name(),
            "variant", example.recovery().car().variant, "tick", tick, "poses", poses,
            "riders", riders));
  }

  void complete(BankedRecoveryExamples.Example example, long tick) {
    append(
        Map.of(
            "type", "complete", "variant", example.recovery().car().variant,
            "tick", tick, "cars", example.cars().size()));
  }

  private void record(List<Map<String, Object>> poses, SubLevel body, UUID car, String role) {
    if (body == null) throw new IllegalStateException("Missing body during recovery mesh trace");
    var center = body.getPlot().getCenterBlock();
    if (registered.add(body.getUniqueId())) {
      var blocks = new ArrayList<Map<String, Object>>();
      // The inspected factory topology fits this box. Reject occupied boundary
      // cells; arbitrary assemblies outside these bounds are not certified.
      for (int x = -5; x <= 5; x++)
        for (int y = -3; y <= 4; y++)
          for (int z = -5; z <= 5; z++) {
            var offset = new BlockPos(x, y, z);
            var state = body.getPlot().getEmbeddedLevelAccessor().getBlockState(offset);
            if (state.isAir()) continue;
            if (Math.abs(x) == 5 || y == -3 || y == 4 || Math.abs(z) == 5)
              throw new IllegalStateException("Recovery mesh content exceeds exporter bounds");
            var properties = new TreeMap<String, String>();
            state.getValues().forEach((key, value) -> properties.put(key.getName(), value.toString()));
            blocks.add(
                Map.of(
                    "offset", new int[] {x, y, z},
                    "block", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString(),
                    "properties", properties));
          }
      if (blocks.isEmpty()) throw new IllegalStateException("Empty native recovery mesh body");
      append(
          Map.of(
              "type", "body", "id", body.getUniqueId().toString(),
              "car", car.toString(), "role", role, "blocks", blocks));
    }
    var pose = body.logicalPose();
    var origin =
        pose.transformPosition(
            new Vector3d(center.getX() + .5, center.getY() + .5, center.getZ() + .5));
    var axes = new ArrayList<double[]>();
    for (var axis : List.of(new Vector3d(1, 0, 0), new Vector3d(0, 1, 0), new Vector3d(0, 0, 1)))
      axes.add(vector(axis.rotate(pose.orientation())));
    poses.add(
        Map.of("id", body.getUniqueId().toString(), "origin", vector(origin), "axes", axes));
  }

  /**
   * Sable renders a rider with its containing carrier pose. Recording the live eye point and the
   * native passenger dimensions lets the offline verifier reconstruct its oriented body volume.
   */
  private void recordMountedPlayers(
      List<Map<String, Object>> riders, SubLevel body, List<BlockPos> seats) {
    if (body == null) throw new IllegalStateException("Missing carrier during rider trace");
    var center = body.getPlot().getCenterBlock();
    for (var offset : seats) {
      var seat = center.offset(offset);
      for (var seatEntity : body.getLevel().getEntitiesOfClass(SeatEntity.class, new AABB(seat)))
        for (var passenger : seatEntity.getPassengers()) {
          riders.add(
              Map.of(
                  "id", passenger.getUUID().toString(), "body", body.getUniqueId().toString(),
                  "eye", vector(new Vector3d(passenger.getEyePosition().x, passenger.getEyePosition().y, passenger.getEyePosition().z)),
                  "width", (double) passenger.getBbWidth(), "height", (double) passenger.getBbHeight(),
                  "eyeHeight", (double) passenger.getEyeHeight()));
        }
    }
  }

  private static double[] vector(Vector3d value) {
    return new double[] {value.x, value.y, value.z};
  }

  private void append(Map<String, Object> entry) {
    try {
      Files.writeString(
          path, new com.google.gson.Gson().toJson(entry) + "\n", StandardOpenOption.APPEND);
    } catch (IOException failure) {
      throw new UncheckedIOException(failure);
    }
  }
}
