package dev.fourthdimension.client;

import com.google.gson.Gson;
import dev.fourthdimension.runtime.EligibilityHooks;
import dev.fourthdimension.vehicle.Boarding;
import dev.ryanhcode.sable.api.sublevel.ClientSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.network.client.ClientSableInterpolationState;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Opt-in diagnostic of Sable's per-body pose-buffer age during an occupied FD ride. */
@EventBusSubscriber(modid = "fourthdimension", value = Dist.CLIENT)
public final class ClientSnapshotTrace {
  private static final Gson GSON = new Gson();
  private static BufferedWriter writer;
  private static boolean completed;
  private static int samples;
  private static int movingSamples;
  private static double previousX = Double.NaN;
  private static double previousZ = Double.NaN;

  private ClientSnapshotTrace() {}

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public static void tick(ClientTickEvent.Post event) {
    if (!Boolean.getBoolean("fourthdimension.snapshotTrace") || completed) return;
    var minecraft = Minecraft.getInstance();
    if (minecraft.level == null || minecraft.player == null) return;
    if (!minecraft.player.isPassenger() || minecraft.player.getVehicle() == null) {
      if (writer != null) complete();
      return;
    }
    var binding = Boarding.at(minecraft.level, minecraft.player.getVehicle().blockPosition());
    if (binding == null || !binding.carrier().riderSeats.containsKey(minecraft.player.getUUID())) return;
    var data = EligibilityHooks.CLIENT.get(minecraft.level);
    var raw = SubLevelContainer.getContainer(minecraft.level);
    if (data == null || !(raw instanceof ClientSubLevelContainer container)) return;
    var interpolation = container.getInterpolation();
    double pointer = interpolation.getTickPointer();
    double infoTick = readInfoTick(interpolation);
    var chassis = container.getSubLevel(binding.vehicle().chassis);
    if (chassis == null) return;
    double x = chassis.logicalPose().position().x;
    double z = chassis.logicalPose().position().z;
    if (Double.isFinite(previousX) && Math.hypot(x - previousX, z - previousZ) > .001)
      movingSamples++;
    previousX = x;
    previousZ = z;
    if (writer == null) {
      writer = open();
      write(Map.of("type", "header", "delay", interpolation.getInterpolationDelay(),
          "player", minecraft.player.getUUID().toString(),
          "mountedChassis", binding.vehicle().chassis.toString()));
    }
    var bodies = new ArrayList<Map<String, Object>>();
    for (var vehicle : data.vehicles.values()) {
      sampleBody(container, bodies, vehicle.chassis, "chassis", vehicle.chassis, pointer);
      for (var carrier : vehicle.carriers)
        sampleBody(container, bodies, carrier.body, "carrier", vehicle.chassis, pointer);
      if (vehicle.recoveryCart != null)
        sampleBody(container, bodies, vehicle.recoveryCart, "recovery", vehicle.chassis, pointer);
    }
    var entry = new LinkedHashMap<String, Object>();
    entry.put("type", "sample");
    entry.put("clientGameTime", minecraft.level.getGameTime());
    entry.put("pointer", pointer);
    entry.put("informationTick", infoTick);
    entry.put("mountedChassis", binding.vehicle().chassis.toString());
    entry.put("mountedCarrier", binding.carrier().body.toString());
    entry.put("chassisX", x);
    entry.put("chassisZ", z);
    entry.put("bodies", bodies);
    write(entry);
    samples++;
  }

  private static void sampleBody(ClientSubLevelContainer container, List<Map<String, Object>> entries,
      UUID id, String role, UUID chassis, double pointer) {
    var raw = container.getSubLevel(id);
    if (!(raw instanceof ClientSubLevel body)) return;
    var buffer = body.getInterpolator().buffer;
    int size = buffer.size();
    var entry = new LinkedHashMap<String, Object>();
    entry.put("id", id.toString());
    entry.put("role", role);
    entry.put("chassis", chassis.toString());
    entry.put("previousTick", size > 1 ? buffer.get(size - 2).gameTick() : null);
    entry.put("latestTick", size > 0 ? buffer.get(size - 1).gameTick() : null);
    entry.put("bufferSize", size);
    if (size >= 2 && pointer > buffer.get(size - 1).gameTick() + 1e-8) {
      entry.put("previousSnapshotPose", pose(buffer.get(size - 2).pose()));
      entry.put("latestSnapshotPose", pose(buffer.get(size - 1).pose()));
      entry.put("lastTickPose", pose(body.lastPose()));
      entry.put("currentTickPose", pose(body.logicalPose()));
      var center = body.getPlot().getCenterBlock();
      entry.put("plotCenter", new double[] {center.getX() + .5, center.getY() + .5,
          center.getZ() + .5});
    }
    entries.add(entry);
  }

  private static Map<String, Object> pose(Pose3dc value) {
    var position = value.position();
    var orientation = value.orientation();
    var rotationPoint = value.rotationPoint();
    var scale = value.scale();
    return Map.of(
        "position", new double[] {position.x(), position.y(), position.z()},
        "orientation", new double[] {orientation.x(), orientation.y(), orientation.z(), orientation.w()},
        "rotationPoint", new double[] {rotationPoint.x(), rotationPoint.y(), rotationPoint.z()},
        "scale", new double[] {scale.x(), scale.y(), scale.z()});
  }

  private static Field infoTickField() {
    try {
      var result = ClientSableInterpolationState.class.getDeclaredField("mostRecentTick");
      result.setAccessible(true);
      return result;
    } catch (ReflectiveOperationException failure) {
      throw new IllegalStateException("Pinned Sable client info tick field changed", failure);
    }
  }

  private static double readInfoTick(ClientSableInterpolationState state) {
    try {
      return InfoTickField.VALUE.getDouble(state);
    } catch (IllegalAccessException failure) {
      throw new IllegalStateException("Cannot inspect Sable client info tick", failure);
    }
  }

  private static final class InfoTickField {
    private static final Field VALUE = infoTickField();
  }

  private static BufferedWriter open() {
    try {
      var path = net.neoforged.fml.loading.FMLPaths.GAMEDIR.get()
          .resolve("fd401-client-snapshot-trace.jsonl.gz");
      return new BufferedWriter(new OutputStreamWriter(
          new GZIPOutputStream(Files.newOutputStream(path)), StandardCharsets.UTF_8));
    } catch (IOException failure) {
      throw new UncheckedIOException(failure);
    }
  }

  private static void write(Map<String, Object> entry) {
    try {
      writer.write(GSON.toJson(entry));
      writer.newLine();
    } catch (IOException failure) {
      throw new UncheckedIOException(failure);
    }
  }

  private static void complete() {
    write(Map.of("type", "complete", "samples", samples, "movingSamples", movingSamples));
    try {
      writer.close();
    } catch (IOException failure) {
      throw new UncheckedIOException(failure);
    }
    writer = null;
    completed = true;
  }
}
