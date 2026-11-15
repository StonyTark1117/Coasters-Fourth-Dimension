package dev.fourthdimension.client;

import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import dev.silvergold.simulatedcoasters.client.track.ChainLiftAnchorOutlineClient;
import dev.silvergold.simulatedcoasters.client.track.graph.CoasterPathGraphManagerClient;
import dev.silvergold.simulatedcoasters.track.anchor.ChainLiftClientIndex;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftSpanQuery;
import dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftSpanOverlap;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphSpans;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphSpans.SpanPath;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

/** On-demand client inspection; never places track, sends input or changes selection. */
@EventBusSubscriber(modid = "fourthdimension", value = Dist.CLIENT)
public final class ChainLiftDiagnostics {
  private static int ticks;

  private ChainLiftDiagnostics() {}

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    if (!Boolean.getBoolean("fourthdimension.validation") || ++ticks % 20 != 0) return;
    var mc = Minecraft.getInstance();
    if (mc.level == null || mc.player == null) return;
    var request = mc.gameDirectory.toPath().resolve("fd-chain-diagnostic.request");
    if (!Files.isRegularFile(request)) return;
    var result = new LinkedHashMap<String, Object>();
    result.put("at", java.time.Instant.now().toString());
    result.put("gameTime", mc.level.getGameTime());
    try {
      String probe = Files.readString(request).trim();
      Files.delete(request);
      result.put("input", inputState(mc));
      result.put("clientWorldGameTime", mc.level.getGameTime());
      result.put("assignedVehicleSnapshot", assignedVehicleSnapshot(mc));
      var selected = (BlockPos) read("selectedAnchor");
      var cached = (SpanPath) read("spanPreviewCachedPath");
      result.put("selectedAnchor", position(selected));
      result.put("cachedKey", String.valueOf(read("spanPreviewCacheKey")));
      result.put("cachedStatus", String.valueOf(read("spanPreviewCachedStatus")));
      result.put("cachedPath", path(cached));
      result.put("heldItem", mc.player.getMainHandItem().toString());
      result.put("hit", String.valueOf(mc.hitResult));
      result.put("hitType", mc.hitResult == null ? null : mc.hitResult.getType().name());
      if (mc.hitResult instanceof BlockHitResult hit) {
        result.put("hitBlock", position(hit.getBlockPos()));
        result.put("hitState", mc.level.getBlockState(hit.getBlockPos()).toString());
        var entity = mc.level.getBlockEntity(hit.getBlockPos());
        result.put("hitBlockEntity", entity == null ? null : entity.getClass().getName());
      }
      var outlineSelection = staticField(
          "com.simibubi.create.content.trains.track.TrackBlockOutline", "result");
      result.put("trackOutlineSelection", String.valueOf(outlineSelection));
      var hoverGraphHit = resolveWrenchGraphHit(mc);
      result.put("wrenchGraphHit", String.valueOf(hoverGraphHit));
      if (hoverGraphHit instanceof CoasterPathTrackFrame.GraphHit graphHit) {
        result.put("wrenchSpanHit", String.valueOf(ChainLiftSpanQuery.findAtGraphHit(
            mc.level, graphHit,
            net.createmod.catnip.animation.AnimationTickHolder.getPartialTicks(mc.level))));
      } else {
        result.put("wrenchSpanHit", null);
      }
      SpanPath requested = null;
      BlockPos probeStart = null;
      if (!probe.isEmpty()) {
        String[] parts = probe.split("\\s+");
        if (parts.length != 6)
          throw new IllegalArgumentException("Expected blank request or six endpoint coordinates");
        probeStart = new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
            Integer.parseInt(parts[2]));
        var end = new BlockPos(Integer.parseInt(parts[3]), Integer.parseInt(parts[4]),
            Integer.parseInt(parts[5]));
        requested = CoasterPathGraphSpans.spanPathBetweenAnchors(mc.level,
            CoasterPathGraphManagerClient.get(mc.level), probeStart, end, 0);
        result.put("probeStart", position(probeStart));
        result.put("probeEnd", position(end));
        result.put("probePath", path(requested));
      }
      var anchors = new ArrayList<Map<String, Object>>();
      for (var anchor : ChainLiftClientIndex.snapshotAnchors()) {
        if (anchor.isRemoved() || anchor.getLevel() != mc.level) continue;
        var row = new LinkedHashMap<String, Object>();
        row.put("anchor", position(anchor.getBlockPos()));
        row.put("currentBlockEntity", mc.level.getBlockEntity(anchor.getBlockPos()) == anchor);
        row.put("hasShaft", anchor.hasChainLiftShaft());
        row.put("hasSpan", anchor.hasChainLiftSpan());
        row.put("speed", anchor.getSpeed());
        row.put("committedSegments", anchor.getChainLiftCommittedSegments().toString());
        // Resolve from this anchor's synchronized data, without trusting a prior path-cache entry.
        var existing = anchor.resolveChainLiftSpanPath(0);
        row.put("resolvedPath", path(existing));
        row.put("overlapsCached", existing != null && cached != null
            && !anchor.getBlockPos().equals(selected)
            && ChainLiftSpanOverlap.spansOverlap(cached, existing));
        row.put("overlapsProbe", existing != null && requested != null
            && !anchor.getBlockPos().equals(probeStart)
            && ChainLiftSpanOverlap.spansOverlap(requested, existing));
        anchors.add(row);
      }
      result.put("indexedAnchors", anchors);
    } catch (Exception failure) {
      result.put("error", failure.toString());
    }
    try {
      var output = mc.gameDirectory.toPath().resolve("fd-chain-diagnostic.json");
      Files.writeString(output,
          new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(result) + "\n");
      LogUtils.getLogger().info("FD_CHAIN_DIAGNOSTIC output={} error={}", output, result.get("error"));
    } catch (Exception failure) {
      LogUtils.getLogger().warn("FD_CHAIN_DIAGNOSTIC could not write snapshot", failure);
    }
  }

  private static Object read(String name) throws ReflectiveOperationException {
    var field = ChainLiftAnchorOutlineClient.class.getDeclaredField(name);
    field.setAccessible(true);
    return field.get(null);
  }

  private static Object staticField(String className, String name)
      throws ReflectiveOperationException {
    var field = Class.forName(className).getDeclaredField(name);
    field.setAccessible(true);
    return field.get(null);
  }

  private static Object resolveWrenchGraphHit(Minecraft mc) throws ReflectiveOperationException {
    var handler = Class.forName(
        "dev.silvergold.simulatedcoasters.client.track.CoasterWrenchCurveAnchorOutlineClient");
    var method = handler.getDeclaredMethod("resolveHoveredCurveGraphHit", Minecraft.class);
    method.setAccessible(true);
    return method.invoke(null, mc);
  }

  private static Object inputState(Minecraft mc) {
    var state = new LinkedHashMap<String, Object>();
    long window = mc.getWindow().getWindow();
    state.put("screen", mc.screen == null ? null : mc.screen.getClass().getName());
    state.put("windowActive", mc.isWindowActive());
    state.put("glfwFocused", GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_FOCUSED));
    state.put("glfwCursorMode", GLFW.glfwGetInputMode(window, GLFW.GLFW_CURSOR));
    state.put("glfwRawMouseMotion", GLFW.glfwGetInputMode(window, GLFW.GLFW_RAW_MOUSE_MOTION));
    state.put("rawMouseOption", mc.options.rawMouseInput().get());
    state.put("mouseGrabbed", mc.mouseHandler.isMouseGrabbed());
    state.put("mouseX", mc.mouseHandler.xpos());
    state.put("mouseY", mc.mouseHandler.ypos());
    state.put("deltaX", mc.mouseHandler.getXVelocity());
    state.put("deltaY", mc.mouseHandler.getYVelocity());
    state.put("playerYaw", mc.player.getYRot());
    state.put("playerPitch", mc.player.getXRot());
    state.put("passenger", mc.player.isPassenger());
    state.put("worldStableView", dev.fourthdimension.runtime.RiderComfortSettings.ENABLED.get());
    var camera = mc.gameRenderer.getMainCamera();
    var q = camera.rotation();
    state.put("cameraQuaternion", new float[] {q.x, q.y, q.z, q.w});
    state.put("cameraUp", new float[] {camera.getUpVector().x, camera.getUpVector().y, camera.getUpVector().z});
    var nativeQ = dev.ryanhcode.sable.mixinhelpers.camera.camera_rotation.EntitySubLevelRotationHelper
        .getSubLevelInheritedOrientation(mc.player,
            sub -> ((dev.ryanhcode.sable.sublevel.ClientSubLevel) sub).renderPose(),
            dev.ryanhcode.sable.mixinhelpers.camera.camera_rotation.EntitySubLevelRotationHelper.Type.CAMERA);
    state.put("nativeSeatOrientation", nativeQ == null ? null
        : new double[] {nativeQ.x, nativeQ.y, nativeQ.z, nativeQ.w});
    try {
      var field = net.minecraft.client.MouseHandler.class.getDeclaredField("ignoreFirstMove");
      field.setAccessible(true);
      state.put("ignoreFirstMove", field.get(mc.mouseHandler));
    } catch (ReflectiveOperationException failure) {
      state.put("ignoreFirstMoveError", failure.toString());
    }
    return state;
  }

  private static Object assignedVehicleSnapshot(Minecraft mc) {
    var data = dev.fourthdimension.runtime.EligibilityHooks.CLIENT.get(mc.level);
    if (data == null || mc.player == null) return Map.of("available", false);
    var vehicle = data.vehicles.values().stream()
        .filter(candidate -> candidate.carriers.stream()
            .anyMatch(carrier -> carrier.riderSeats.containsKey(mc.player.getUUID())
                || carrier.occupants.contains(mc.player.getUUID())))
        .findFirst().orElse(null);
    if (vehicle == null) return Map.of("available", true, "assignedVehicle", false);
    var state = new LinkedHashMap<String, Object>();
    state.put("available", true);
    state.put("assignedVehicle", true);
    state.put("chassis", vehicle.chassis.toString());
    state.put("family", vehicle.family.id());
    state.put("revision", vehicle.revision);
    state.put("stopping", vehicle.stopping);
    state.put("maintenance", vehicle.maintenance);
    state.put("alignmentRequired", vehicle.alignmentRequired);
    state.put("fault", vehicle.fault);
    state.put("restraints", vehicle.restraints);
    state.put("boardingReady", vehicle.boardingReady);
    state.put("boundFrom", position(vehicle.boundFrom));
    state.put("boundTo", position(vehicle.boundTo));
    var carriers = new ArrayList<Map<String, Object>>();
    for (var carrier : vehicle.carriers) {
      var row = new LinkedHashMap<String, Object>();
      row.put("body", carrier.body.toString());
      row.put("capture", carrier.capture);
      row.put("occupants", carrier.occupants.stream().map(Object::toString).toList());
      row.put("riderSeats", carrier.riderSeats.keySet().stream().map(Object::toString).toList());
      carriers.add(row);
    }
    state.put("carriers", carriers);

    if (vehicle.boundFrom == null || vehicle.boundTo == null) return state;
    var rail = data.rails.stream()
        .filter(candidate -> candidate.matches(vehicle.boundFrom, vehicle.boundTo))
        .findFirst().orElse(null);
    state.put("matchingRailFound", rail != null);
    if (rail != null) {
    var railState = new LinkedHashMap<String, Object>();
    railState.put("from", position(rail.from));
    railState.put("to", position(rail.to));
    railState.put("valid", rail.valid);
    railState.put("variant", rail.variant);
    railState.put("revision", rail.revision);
    state.put("matchingRail", railState);
    }

    var body = CoasterCartResolve.findCartById(mc.level, vehicle.chassis);
    var center = body == null ? null : CoasterCartResolve.cartCenterWorld(body);
    state.put("nativeBodyLoaded", body != null);
    if (center == null) return state;
    var bearingPos = dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrackSnap
        .representativeBearingPlotPos(body);
    state.put("bearingPlotPosition", position(bearingPos));
    if (bearingPos != null && body.getPlot() != null) {
      var bearingState = body.getPlot().getEmbeddedLevelAccessor().getBlockState(bearingPos);
      state.put("bearingBlockState", bearingState.toString());
      state.put("bearingSnapEngaged", bearingState.hasProperty(
          dev.silvergold.simulatedcoasters.track.cart.CoasterCartBlock.SNAP_ENGAGED)
              && bearingState.getValue(
                  dev.silvergold.simulatedcoasters.track.cart.CoasterCartBlock.SNAP_ENGAGED));
    }
    state.put("nativeCartCenter", new double[] {center.x, center.y, center.z});
    var graph = CoasterPathGraphManagerClient.get(mc.level);
    var centerWorld = new net.minecraft.world.phys.Vec3(center.x, center.y, center.z);
    var nearbyHits = new ArrayList<Map<String, Object>>();
    for (var edge : graph.edges()) {
      var candidate = CoasterPathTrackFrame.closestHitOnEdge(mc.level, edge, centerWorld);
      if (candidate == null) continue;
      var distance = candidate.point().distanceTo(centerWorld);
      if (distance > 3.0) continue;
      var candidateRow = new LinkedHashMap<String, Object>();
      candidateRow.put("from", position(edge.from()));
      candidateRow.put("to", position(edge.to()));
      candidateRow.put("t", candidate.t());
      candidateRow.put("point", new double[] {
          candidate.point().x, candidate.point().y, candidate.point().z});
      candidateRow.put("distance", distance);
      var candidateChain = ChainLiftSpanQuery.findAtGraphHit(mc.level, candidate,
          net.createmod.catnip.animation.AnimationTickHolder.getPartialTicks(mc.level));
      candidateRow.put("chainAnchor", candidateChain == null
          ? null : position(candidateChain.anchor().getBlockPos()));
      candidateRow.put("chainSpeed", candidateChain == null
          ? null : candidateChain.anchor().getSpeed());
      candidateRow.put("chainTravelSign", candidateChain == null
          ? null : candidateChain.anchor().getChainLiftTravelSign());
      nearbyHits.add(candidateRow);
    }
    nearbyHits.sort(java.util.Comparator.comparingDouble(row -> (double) row.get("distance")));
    state.put("nearestGraphCandidates", nearbyHits.stream().limit(8).toList());
    CoasterPathEdge selectedEdge = graph.edges().stream()
        .filter(edge -> (vehicle.boundFrom.equals(edge.from()) && vehicle.boundTo.equals(edge.to()))
            || (vehicle.boundFrom.equals(edge.to()) && vehicle.boundTo.equals(edge.from())))
        .findFirst().orElse(null);
    state.put("matchingGraphEdgeFound", selectedEdge != null);
    if (selectedEdge == null) return state;
    var hit = CoasterPathTrackFrame.closestHitOnEdge(
        mc.level, selectedEdge, new net.minecraft.world.phys.Vec3(center.x, center.y, center.z));
    state.put("selectedGraphHit", hit == null ? null : Map.of(
        "from", position(hit.edge().from()),
        "to", position(hit.edge().to()),
        "t", hit.t(),
        "point", new double[] {hit.point().x, hit.point().y, hit.point().z},
        "distance", hit.point().distanceTo(
            new net.minecraft.world.phys.Vec3(center.x, center.y, center.z))));
    if (hit == null) return state;
    var chain = ChainLiftSpanQuery.findAtGraphHit(
        mc.level, hit, net.createmod.catnip.animation.AnimationTickHolder.getPartialTicks(mc.level));
    if (chain != null) {
      var anchor = chain.anchor();
      state.put("chainDrive", Map.of(
          "anchor", position(anchor.getBlockPos()),
          "speed", anchor.getSpeed(),
          "travelSign", anchor.getChainLiftTravelSign(),
          "path", path(chain.path())));
    } else {
      state.put("chainDrive", null);
    }
    var chassis = CoasterCartResolve.findCartById(mc.level, vehicle.chassis);
    var linkedIds = chassis == null ? null
        : dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint
            .getTrainLinkedCartIds(chassis);
    state.put("clientTrainLinkIds", linkedIds == null
        ? null : linkedIds.stream().map(Object::toString).toList());
    if (chassis != null) {
      var container = dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(mc.level);
      var members = new ArrayList<Map<String, Object>>();
      for (var id : linkedIds == null ? java.util.List.<java.util.UUID>of() : linkedIds) {
        var member = new LinkedHashMap<String, Object>();
        member.put("id", id.toString());
        member.put("clientBodyLoaded", CoasterCartResolve.findCartById(mc.level, id) != null);
        member.put("clientVehicleRecord", data.vehicles.containsKey(id));
        member.put("containerBodyLoaded", container != null && container.getSubLevel(id) != null);
        members.add(member);
      }
      state.put("clientTrainMembers", members);
      var componentBodies = new ArrayList<Map<String, Object>>();
      var componentIds = new ArrayList<java.util.UUID>();
      componentIds.add(vehicle.chassis);
      vehicle.carriers.forEach(carrier -> componentIds.add(carrier.body));
      for (var id : componentIds) {
        var component = new LinkedHashMap<String, Object>();
        component.put("id", id.toString());
        var componentBody = CoasterCartResolve.findCartById(mc.level, id);
        component.put("clientCartBodyLoaded", componentBody != null);
        component.put("clientBodyRemoved", componentBody != null && componentBody.isRemoved());
        componentBodies.add(component);
      }
      state.put("clientVehicleComponentBodies", componentBodies);
      try {
        state.put("clientConsistEligible", dev.fourthdimension.vehicle.ConsistRules
            .eligible(mc.level, chassis, vehicle));
      } catch (RuntimeException failure) {
        state.put("clientConsistEligibleError", failure.toString());
      }
    }
    return state;
  }

  private static Object position(BlockPos pos) {
    return pos == null ? null : new int[] {pos.getX(), pos.getY(), pos.getZ()};
  }

  private static Object path(SpanPath path) {
    if (path == null) return null;
    return Map.of("length", path.totalLength(), "segments", path.segments().stream()
        .map(s -> Map.of("from", position(s.edge().from()), "to", position(s.edge().to()),
            "tStart", s.tStart(), "tEnd", s.tEnd())).toList());
  }
}
