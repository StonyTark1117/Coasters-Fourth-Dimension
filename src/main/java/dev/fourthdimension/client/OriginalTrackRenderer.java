package dev.fourthdimension.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.trains.track.BezierConnection;
import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.joml.Matrix4f;

/** Original procedural tube/spine meshes, shared by placed tracks and placement ghosts. */
@EventBusSubscriber(modid = "fourthdimension", value = Dist.CLIENT)
public final class OriginalTrackRenderer {
  private record Quad(Vec3 a, Vec3 b, Vec3 c, Vec3 d, int color) {}

  private record Mesh(
      long revision,
      long geometry,
      long modelGeneration,
      List<Quad> quads,
      dev.fourthdimension.client.integration.TrackStylesRenderer.Mesh decoration) {}

  private static final Map<UUID, Mesh> MESHES = new HashMap<>();
  private static net.minecraft.world.level.Level lastLevel;
  private static String ghostKey = "";
  private static List<Quad> ghost = List.of();
  private static final Map<UUID, Long> SEEN = new HashMap<>();

  private OriginalTrackRenderer() {}

  public static boolean owns(BezierConnection curve) {
    var level = Minecraft.getInstance().level;
    var data = level == null ? null : EligibilityHooks.data(level);
    return data != null
        && data.rails.stream()
            .anyMatch(
                r ->
                    r.original()
                        && r.matches(curve.bePositions.getFirst(), curve.bePositions.getSecond()));
  }

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    var level = Minecraft.getInstance().level;
    if (level != lastLevel) {
      MESHES.clear();
      SEEN.clear();
      ghostKey = "";
      ghost = List.of();
      lastLevel = level;
    }
    if (level == null) return;
    var data = EligibilityHooks.data(level);
    if (data == null) return;
    Set<UUID> live = new HashSet<>();
    for (var rail : data.rails)
      if (rail.original()) {
        live.add(rail.id);
        if (!Objects.equals(SEEN.get(rail.id), rail.revision)) {
          for (var pos : List.of(rail.from, rail.to)) {
            var anchor = level.getBlockEntity(pos);
            if (anchor != null) VisualizationHelper.queueUpdate(anchor);
          }
          SEEN.put(rail.id, rail.revision);
          MESHES.remove(rail.id);
        }
      }
    MESHES.keySet().retainAll(live);
    SEEN.keySet().retainAll(live);
  }

  @SubscribeEvent
  public static void render(RenderLevelStageEvent event) {
    if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;
    var mc = Minecraft.getInstance();
    if (mc.level == null || mc.player == null) return;
    var camera = event.getCamera().getPosition();
    var pose = event.getPoseStack();
    pose.pushPose();
    pose.translate(-camera.x, -camera.y, -camera.z);
    var buffers = mc.renderBuffers().bufferSource();
    var consumer = buffers.getBuffer(RenderType.debugQuads());
    var decorations =
        new ArrayList<dev.fourthdimension.client.integration.TrackStylesRenderer.Mesh>();
    var data = EligibilityHooks.data(mc.level);
    if (data != null)
      for (var rail : data.rails)
        if (rail.original()
            && Math.min(rail.from.distToCenterSqr(camera), rail.to.distToCenterSqr(camera))
                < 160 * 160) {
          var edge = TrackGeometry.resolve(mc.level, rail);
          if (edge == null) continue;
          long hash = TrackGeometry.fingerprint(mc.level, edge);
          var mesh = MESHES.get(rail.id);
          long generation = dev.fourthdimension.client.integration.TrackStylesCatalog.generation();
          if (mesh == null
              || mesh.revision != rail.revision
              || mesh.geometry != hash
              || mesh.modelGeneration != generation) {
            var decoration =
                dev.fourthdimension.client.integration.TrackStylesRenderer.build(edge, rail);
            var ownership =
                decoration == null
                    ? dev.fourthdimension.integration.TrackRenderOwnership.original()
                    : decoration.ownership();
            int tint =
                decoration == null && !rail.appearance.original()
                    ? TrackDesign.byVariant(rail.variant).color
                    : rail.appearance.tint() < 0
                        ? TrackDesign.byVariant(rail.variant).color
                        : rail.appearance.tint();
            mesh =
                new Mesh(
                    rail.revision,
                    hash,
                    generation,
                    build(
                        edge,
                        TrackDesign.byVariant(rail.variant),
                        edge.from().equals(rail.from) ? rail.profile : rail.profile.reversed(),
                        rail.magnets,
                        edge.from().equals(rail.from),
                        ownership,
                        tint),
                    decoration);
            MESHES.put(rail.id, mesh);
          }
          draw(consumer, pose.last().pose(), mesh.quads, rail.valid ? 1f : .4f);
          if (mesh.decoration != null) decorations.add(mesh.decoration);
        }
    var held = mc.player.getMainHandItem();
    if (held.getItem() instanceof FamilyTrackItem item
        && mc.hitResult instanceof BlockHitResult hit) {
      var first = held.get(TrackItems.SELECTION.get());
      if (first != null && first.dimension().equals(mc.level.dimension())) {
        try {
          String key =
              first.pos()
                  + ":"
                  + hit.getBlockPos()
                  + ":"
                  + item.design
                  + ":"
                  + (mc.level.getGameTime() / 10);
          if (!key.equals(ghostKey)) {
            var curve = TrackPlacement.preview(mc.level, first.pos(), hit.getBlockPos());
            var edge =
                CoasterPathEdge.curve(
                    curve.bePositions.getFirst(), curve.bePositions.getSecond(), curve);
            double length = TrackGeometry.table(mc.level, edge).length();
            ghost =
                build(
                    edge,
                    item.design,
                    new RotationProfile(
                        List.of(new RotationProfile.Key(0, 0), new RotationProfile.Key(length, 0))),
                    List.of(),
                    true,
                    dev.fourthdimension.integration.TrackRenderOwnership.original(),
                    item.design.color);
            ghostKey = key;
          }
          draw(consumer, pose.last().pose(), ghost, .35f);
        } catch (IllegalArgumentException ignored) {
          /* Invalid hover has no placeable ghost. */
        }
      }
    }
    buffers.endBatch(RenderType.debugQuads());
    for (var decoration : decorations)
      dev.fourthdimension.client.integration.TrackStylesRenderer.draw(decoration, pose, buffers);
    if (!decorations.isEmpty()) buffers.endBatch(RenderType.cutoutMipped());
    pose.popPose();
  }

  private static List<Quad> build(
      CoasterPathEdge edge,
      TrackDesign design,
      RotationProfile profile,
      List<dev.fourthdimension.physics.MagneticSpan> magnets,
      boolean canonical,
      dev.fourthdimension.integration.TrackRenderOwnership ownership,
      int decorationColor) {
    var level = Minecraft.getInstance().level;
    var table = TrackGeometry.table(level, edge);
    int count = Math.max(8, (int) Math.ceil(table.length() * 4));
    var result = new ArrayList<Quad>();
    Vec3[] previous = null;
    Vec3 oldUp = null, oldRight = null;
    for (int i = 0; i <= count; i++) {
      double distance = table.length() * i / count, t = table.parameterAt(distance);
      Vec3 center = CoasterPathGraphManager.sampleEdgePosition(level, edge, t);
      var basis =
          CoasterPathTrackFrame.trackBasisAtHit(
              level, new CoasterPathTrackFrame.GraphHit(center, edge, t));
      Vec3 right = basis.right(), up = basis.up();
      double control = design.controlHeight(profile.angleAt(distance));
      Vec3[] points = {
        center
            .add(right.scale(-TrackDesign.HALF_GAUGE))
            .add(up.scale(TrackDesign.MAIN_RAIL_HEIGHT)),
        center.add(right.scale(TrackDesign.HALF_GAUGE)).add(up.scale(TrackDesign.MAIN_RAIL_HEIGHT)),
        center.add(right.scale(-TrackDesign.CONTROL_HALF_GAUGE)).add(up.scale(control)),
        center.add(right.scale(TrackDesign.CONTROL_HALF_GAUGE)).add(up.scale(control)),
        center.add(up.scale(-.55))
      };
      if (previous != null) {
        for (int rail = 0; rail < 5; rail++) {
          if ((rail == 2 || rail == 3) && !design.family.driven()) continue;
          if (rail == 4
              && !ownership.originalOwns(
                  dev.fourthdimension.integration.TrackRenderOwnership.Component.SPINE)) continue;
          tube(
              result,
              previous[rail],
              points[rail],
              oldRight,
              oldUp,
              right,
              up,
              rail < 2 ? TrackDesign.RAIL_HALF_WIDTH : rail == 4 ? .22 : .09,
              rail < 2 ? TrackDesign.RAIL_HALF_HEIGHT : rail == 4 ? .22 : .09,
              rail < 2 ? 0xc6d0d4 : rail == 4 ? decorationColor : 0xf0ca69);
        }
      }
      if (i % 8 == 0) {
        if (ownership.originalOwns(
            dev.fourthdimension.integration.TrackRenderOwnership.Component.TIES))
          tube(
              result,
              points[0],
              points[1],
              basis.forward(),
              up,
              basis.forward(),
              up,
              .06,
              decorationColor);
        for (int side = 0; side < 2; side++) {
          if (ownership.originalOwns(
              dev.fourthdimension.integration.TrackRenderOwnership.Component.TIES))
            tube(
                result,
                center
                    .add(right.scale(side == 0 ? -.5 : .5))
                    .add(up.scale(TrackDesign.MAIN_RAIL_HEIGHT)),
                points[4],
                basis.forward(),
                right,
                basis.forward(),
                right,
                .08,
                decorationColor);
          if (design.family.driven())
            tube(
                result,
                points[side + 2],
                points[4],
                basis.forward(),
                right,
                basis.forward(),
                right,
                .055,
                decorationColor);
        }
      }
      previous = points;
      oldRight = right;
      oldUp = up;
    }
    for (var span : magnets) {
      Vec3[] last = null;
      int panels = Math.max(1, (int) Math.ceil((span.end() - span.start()) * 4));
      for (int n = 0; n <= panels; n++) {
        double d = span.start() + (span.end() - span.start()) * n / panels;
        double t = table.parameterAt(canonical ? d : table.length() - d);
        var p = CoasterPathGraphManager.sampleEdgePosition(level, edge, t);
        var basis =
            CoasterPathTrackFrame.trackBasisAtHit(
                level, new CoasterPathTrackFrame.GraphHit(p, edge, t));
        Vec3[] points = {
          p.add(basis.right().scale(-TrackDesign.MAGNET_HALF_GAUGE))
              .add(basis.up().scale(TrackDesign.MAGNET_HEIGHT)),
          p.add(basis.right().scale(TrackDesign.MAGNET_HALF_GAUGE))
              .add(basis.up().scale(TrackDesign.MAGNET_HEIGHT))
        };
        if (last != null)
          for (int side = 0; side < 2; side++)
            tube(
                result,
                last[side],
                points[side],
                basis.right(),
                basis.up(),
                basis.right(),
                basis.up(),
                .12,
                span.mode() == dev.fourthdimension.physics.MagneticSpan.Mode.INDUCE
                    ? 0xe89835
                    : 0x35c4d8);
        if (n == 0 || n == panels || n % 8 == 0) {
          var foot = p.add(basis.up().scale(TrackDesign.MAGNET_SUPPORT_HEIGHT));
          for (int side = 0; side < 2; side++) {
            var knee =
                foot.add(basis.right().scale((side == 0 ? -1 : 1) * TrackDesign.MAGNET_HALF_GAUGE));
            tube(
                result,
                foot,
                knee,
                basis.forward(),
                basis.up(),
                basis.forward(),
                basis.up(),
                TrackDesign.MAGNET_SUPPORT_RADIUS,
                decorationColor);
            tube(
                result,
                knee,
                points[side],
                basis.forward(),
                basis.right(),
                basis.forward(),
                basis.right(),
                TrackDesign.MAGNET_SUPPORT_RADIUS,
                decorationColor);
          }
        }
        last = points;
      }
    }
    return List.copyOf(result);
  }

  private static void tube(
      List<Quad> out,
      Vec3 a,
      Vec3 b,
      Vec3 ar,
      Vec3 au,
      Vec3 br,
      Vec3 bu,
      double radius,
      int color) {
    tube(out, a, b, ar, au, br, bu, radius, radius, color);
  }

  private static void tube(
      List<Quad> out,
      Vec3 a,
      Vec3 b,
      Vec3 ar,
      Vec3 au,
      Vec3 br,
      Vec3 bu,
      double radiusX,
      double radiusY,
      int color) {
    for (int face = 0; face < 8; face++) {
      double p = 2 * Math.PI * face / 8, q = 2 * Math.PI * (face + 1) / 8;
      double light = .72 + .28 * Math.max(0, Math.sin((p + q) / 2));
      int shaded =
          ((int) (((color >> 16) & 255) * light) << 16)
              | ((int) (((color >> 8) & 255) * light) << 8)
              | (int) ((color & 255) * light);
      out.add(
          new Quad(
              a.add(ar.scale(Math.cos(p) * radiusX)).add(au.scale(Math.sin(p) * radiusY)),
              a.add(ar.scale(Math.cos(q) * radiusX)).add(au.scale(Math.sin(q) * radiusY)),
              b.add(br.scale(Math.cos(q) * radiusX)).add(bu.scale(Math.sin(q) * radiusY)),
              b.add(br.scale(Math.cos(p) * radiusX)).add(bu.scale(Math.sin(p) * radiusY)),
              shaded));
    }
  }

  private static void draw(VertexConsumer buffer, Matrix4f matrix, List<Quad> quads, float alpha) {
    for (var quad : quads)
      for (var point : List.of(quad.a, quad.b, quad.c, quad.d))
        buffer
            .addVertex(matrix, (float) point.x, (float) point.y, (float) point.z)
            .setColor(
                (quad.color >> 16) & 255,
                (quad.color >> 8) & 255,
                quad.color & 255,
                (int) (alpha * 255));
  }
}
