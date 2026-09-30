package dev.fourthdimension.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.fourthdimension.integration.*;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Uses separately installed model resources; no Track Styles implementation or assets are bundled.
 */
@EventBusSubscriber(modid = "fourthdimension", value = Dist.CLIENT)
public final class TrackStylesVisual {
  private static final Map<String, PartialModel> SPINES = new HashMap<>();
  private static final Map<UUID, Mesh> MESHES = new HashMap<>();
  private static Level lastLevel;
  private static final Map<BakedModel, Optional<Source>> SOURCES = new WeakHashMap<>();

  private record Source(
      List<BakedQuad> quads, double centerX, double centerY, double minZ, double span) {}

  private record Vertex(Vec3 point, Vec3 normal, float u, float v, int color) {}

  private record Part(BlockPos lightPosition, List<Vertex> vertices) {}

  private record Mesh(long revision, long geometry, BakedModel model, List<Part> parts) {}

  private TrackStylesVisual() {}

  public static void retain(Set<UUID> rails) {
    MESHES.keySet().retainAll(rails);
  }

  @SubscribeEvent
  public static void models(ModelEvent.RegisterAdditional event) {
    if (!OptionalIntegrations.supported("coasterssimulatedextratypes", Set.of("1.2.0"))) return;
    for (var name : List.of("arrow_4d", "arrow_4d_dbl_spine")) {
      var location =
          ResourceLocation.fromNamespaceAndPath(
              "coasterssimulatedextratypes", "block/track/" + name + "/" + name + "_center_beam");
      SPINES.put("coasterssimulatedextratypes:" + name, PartialModel.of(location));
      event.register(ModelResourceLocation.standalone(location));
    }
  }

  public static boolean available(FoundationData.Rail rail) {
    var appearance = rail.appearance.effective(rail.family);
    if (!appearance.provider().equals(AppearanceProviders.TRACK_STYLES)) return false;
    var model = SPINES.get(appearance.style());
    return model != null
        && model.get() != Minecraft.getInstance().getModelManager().getMissingModel()
        && source(model.get()).isPresent();
  }

  public static void render(
      FoundationData.Rail rail,
      CoasterPathEdge edge,
      long geometry,
      PoseStack pose,
      MultiBufferSource buffers) {
    var level = Minecraft.getInstance().level;
    if (level != lastLevel) {
      MESHES.clear();
      lastLevel = level;
    }
    var model = SPINES.get(rail.appearance.style()).get();
    var mesh = MESHES.get(rail.id);
    if (mesh == null
        || mesh.revision != rail.revision
        || mesh.geometry != geometry
        || mesh.model != model) {
      mesh = new Mesh(rail.revision, geometry, model, build(level, rail, edge, model));
      MESHES.put(rail.id, mesh);
    }
    var buffer = buffers.getBuffer(RenderType.cutoutMipped());
    for (var part : mesh.parts) {
      int light = LevelRenderer.getLightColor(level, part.lightPosition);
      for (var vertex : part.vertices) {
        var p = vertex.point;
        var n = vertex.normal;
        buffer
            .addVertex(pose.last().pose(), (float) p.x, (float) p.y, (float) p.z)
            .setColor(vertex.color)
            .setUv(vertex.u, vertex.v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(pose.last(), (float) n.x, (float) n.y, (float) n.z);
      }
    }
  }

  private static List<Part> build(
      Level level, FoundationData.Rail rail, CoasterPathEdge edge, BakedModel model) {
    var source = source(model).orElseThrow();
    var quads = source.quads;
    var table = TrackGeometry.table(level, edge);
    int pieces = Math.max(1, (int) Math.ceil(table.length() * 2));
    double length = table.length() / pieces;
    var parts = new ArrayList<Part>();
    int tint = TrackDesign.byVariant(rail.variant).color;
    for (int i = 0; i < pieces; i++) {
      var vertices = new ArrayList<Vertex>();
      for (var quad : quads) {
        var raw = quad.getVertices();
        int stride = raw.length / 4;
        boolean startCap = true, endCap = true;
        for (int j = 0; j < 4; j++) {
          double z = Float.intBitsToFloat(raw[j * stride + 2]);
          startCap &= Math.abs(z - source.minZ) < 1e-6;
          endCap &= Math.abs(z - source.minZ - source.span) < 1e-6;
        }
        if ((startCap && i > 0) || (endCap && i < pieces - 1)) continue;
        for (int j = 0; j < 4; j++) {
          int offset = j * stride;
          double x = Float.intBitsToFloat(raw[offset]) - source.centerX;
          double y = Float.intBitsToFloat(raw[offset + 1]) - source.centerY - .55;
          double z = Float.intBitsToFloat(raw[offset + 2]);
          double distance = (i + (z - source.minZ) / source.span) * length;
          double t = table.parameterAt(distance);
          var center = CoasterPathGraphManager.sampleEdgePosition(level, edge, t);
          var frame =
              CoasterPathTrackFrame.trackBasisAtHit(
                  level, new CoasterPathTrackFrame.GraphHit(center, edge, t));
          var point = center.add(frame.right().scale(x)).add(frame.up().scale(y));
          int packedNormal = raw[offset + 7];
          var normal =
              frame
                  .right()
                  .scale((byte) packedNormal / 127.0)
                  .add(frame.up().scale((byte) (packedNormal >> 8) / 127.0))
                  .add(frame.forward().scale((byte) (packedNormal >> 16) / 127.0))
                  .normalize();
          float shade = quad.isShade() ? (float) (.6 + .4 * Math.max(0, normal.y)) : 1;
          int original = raw[offset + 3];
          int red = (int) (((tint >> 16) & 255) * (original & 255) / 255f * shade);
          int green = (int) (((tint >> 8) & 255) * ((original >> 8) & 255) / 255f * shade);
          int blue = (int) ((tint & 255) * ((original >> 16) & 255) / 255f * shade);
          vertices.add(
              new Vertex(
                  point,
                  normal,
                  Float.intBitsToFloat(raw[offset + 4]),
                  Float.intBitsToFloat(raw[offset + 5]),
                  0xff000000 | red << 16 | green << 8 | blue));
        }
      }
      var center =
          CoasterPathGraphManager.sampleEdgePosition(
              level, edge, table.parameterAt((i + .5) * length));
      parts.add(new Part(BlockPos.containing(center), List.copyOf(vertices)));
    }
    return List.copyOf(parts);
  }

  private static Optional<Source> source(BakedModel model) {
    return SOURCES.computeIfAbsent(model, TrackStylesVisual::readSource);
  }

  private static Optional<Source> readSource(BakedModel model) {
    var quads = new ArrayList<BakedQuad>();
    var faces = new ArrayList<Direction>(Arrays.asList(Direction.values()));
    faces.add(null);
    for (var face : faces)
      quads.addAll(
          model.getQuads(
              Blocks.AIR.defaultBlockState(), face, RandomSource.create(0), ModelData.EMPTY, null));
    double minZ = Double.POSITIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
    double minY = minZ, maxY = maxZ, minX = minZ, maxX = maxZ;
    for (var quad : quads) {
      var raw = quad.getVertices();
      int stride = raw.length / 4;
      for (int j = 0; j < 4; j++) {
        double x = Float.intBitsToFloat(raw[j * stride]),
            y = Float.intBitsToFloat(raw[j * stride + 1]),
            z = Float.intBitsToFloat(raw[j * stride + 2]);
        minX = Math.min(minX, x);
        maxX = Math.max(maxX, x);
        minY = Math.min(minY, y);
        maxY = Math.max(maxY, y);
        minZ = Math.min(minZ, z);
        maxZ = Math.max(maxZ, z);
      }
    }
    if (!(maxZ > minZ) || !Double.isFinite(maxX + maxY + maxZ + minX + minY + minZ))
      return Optional.empty();
    return Optional.of(
        new Source(List.copyOf(quads), (minX + maxX) / 2, (minY + maxY) / 2, minZ, maxZ - minZ));
  }
}
