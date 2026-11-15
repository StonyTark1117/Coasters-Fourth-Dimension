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
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

/** Original procedural tube/spine meshes, shared by placed tracks and placement ghosts. */
@EventBusSubscriber(modid = "fourthdimension", value = Dist.CLIENT)
public final class OriginalTrackRenderer {
  private record Quad(Vec3 a, Vec3 b, Vec3 c, Vec3 d, Vec3 normal,
                      BlockPos lightAt, float width, float length, float phase, int color, String material) {}

  private static final RenderType TRANSLUCENT = RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);
  private record Transparent(List<Quad> quads,float alpha) {}
  private static final Map<BlockPos, Integer> FRAME_LIGHT = new HashMap<>();
  private static final Map<String, TextureAtlasSprite> FRAME_SPRITES = new HashMap<>();

  private record Mesh(
      long revision,
      long geometry,
      long modelGeneration,
      List<Quad> quads,
      dev.fourthdimension.client.integration.TrackStylesRenderer.Mesh decoration,
      List<Quad> originalFallback, AABB bounds) {
    Mesh(long revision,long geometry,long generation,List<Quad> quads,
         dev.fourthdimension.client.integration.TrackStylesRenderer.Mesh decoration,
         List<Quad> fallback) {
      this(revision,geometry,generation,quads,decoration,fallback,boundsOf(quads));
    }
  }

  private static AABB boundsOf(List<Quad> quads) {
    AABB bounds=null;
    for(var q:quads) {
      var box=new AABB(q.a,q.c).minmax(new AABB(q.b,q.d));
      bounds=bounds==null?box:bounds.minmax(box);
    }
    return bounds==null?new AABB(0,0,0,0,0,0):bounds.inflate(2);
  }

  private record Decoration(UUID rail, Mesh mesh, float alpha) {}

  private static final Map<UUID, Mesh> MESHES = new HashMap<>();
  private static net.minecraft.world.level.Level lastLevel;
  private static String ghostKey = "";
  private static List<Quad> ghost = List.of();
  private static final Map<UUID, Long> SEEN = new HashMap<>();
  private static final dev.fourthdimension.client.integration.TrackStylesFallbackState
      STYLE_DRAW_FAILURES = new dev.fourthdimension.client.integration.TrackStylesFallbackState();

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
      FRAME_LIGHT.clear();
      FRAME_SPRITES.clear();
      SEEN.clear();
      STYLE_DRAW_FAILURES.clear();
      TrackRenderMetrics.reset();
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
    STYLE_DRAW_FAILURES.retain(live);
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
    // Event subscribers are loaded during mod construction, before registry-backed
    // Sheets materials are safe to initialize. Resolve this only in a live render.
    var surface = net.minecraft.client.renderer.Sheets.cutoutBlockSheet();
    var consumer = buffers.getBuffer(surface);
    FRAME_LIGHT.clear();
    FRAME_SPRITES.clear();
    long renderStart = System.nanoTime();
    int inRangeRails = 0;
    int inRangeQuads = 0;
    int inRangeDecorationPieces = 0;
    var decorations = new ArrayList<Decoration>();
    var transparent = new ArrayList<Transparent>();
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
            long buildStart = System.nanoTime();
            var decoration =
                STYLE_DRAW_FAILURES.quarantines(rail.id, generation, rail.revision)
                    ? null
                    : dev.fourthdimension.client.integration.TrackStylesRenderer.build(edge, rail);
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
                    decoration,
                    decoration == null
                        ? List.of()
                        : buildSpineFallback(
                            edge, TrackDesign.byVariant(rail.variant).color));
            MESHES.put(rail.id, mesh);
            TrackRenderMetrics.meshBuilt(System.nanoTime() - buildStart);
          }
          if (!event.getFrustum().isVisible(mesh.bounds)) continue;
          if(rail.valid)draw(consumer, pose.last(), mesh.quads, 1f);
          else transparent.add(new Transparent(mesh.quads,.4f));
          if (rail.profile.keys().stream().anyMatch(key -> Math.abs(key.angle()) >= .25))
            TrackRenderMetrics.renderedNonzeroProfile(rail.id);
          inRangeRails++;
          inRangeQuads += mesh.quads.size();
          if (mesh.decoration != null) {
            decorations.add(new Decoration(rail.id, mesh, rail.valid ? 1f : .4f));
            inRangeDecorationPieces += mesh.decoration.pieces().size();
          }
        }
    // Normal world steel is opaque. Only invalid previews and placement ghosts need
    // blending/sorting; putting every tube in the translucent batch adds avoidable cost.
    buffers.endBatch(surface);
    var translucentConsumer=buffers.getBuffer(TRANSLUCENT);
    for(var part:transparent)draw(translucentConsumer,pose.last(),part.quads,part.alpha);
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
          draw(translucentConsumer, pose.last(), ghost, .35f);
        } catch (IllegalArgumentException ignored) {
          /* Invalid hover has no placeable ghost. */
        }
      }
    }
    var failedDecorations = new ArrayList<Decoration>();
    for (var decoration : decorations)
      if (!dev.fourthdimension.client.integration.TrackStylesRenderer.draw(
          decoration.mesh.decoration, pose, buffers)) {
        STYLE_DRAW_FAILURES.quarantine(
            decoration.rail, decoration.mesh.modelGeneration, decoration.mesh.revision);
        MESHES.remove(decoration.rail);
        inRangeDecorationPieces -= decoration.mesh.decoration.pieces().size();
        failedDecorations.add(decoration);
      }
    if (!decorations.isEmpty()) buffers.endBatch(RenderType.cutoutMipped());
    if (!failedDecorations.isEmpty()) {
      // Reacquire after the provider batch: a provider can fail after queuing a partial cutout.
      // Its style is disabled for this generation, and the only suppressed Original component is
      // restored immediately; the next frame contains the fully Original cached mesh.
      for (var decoration : failedDecorations) {
        draw(
            buffers.getBuffer(decoration.alpha<1?TRANSLUCENT:surface),
            pose.last(),
            decoration.mesh.originalFallback,
            decoration.alpha);
        inRangeQuads += decoration.mesh.originalFallback.size();
      }
    }
    buffers.endBatch(surface);
    buffers.endBatch(TRANSLUCENT);
    pose.popPose();
    TrackRenderMetrics.rendered(
        System.nanoTime() - renderStart,
        inRangeRails,
        inRangeQuads,
        inRangeDecorationPieces);
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
    double[] railPhase = new double[5];
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
            .add(right.scale(-(design == TrackDesign.PIPELINE ? 1.5 : TrackDesign.HALF_GAUGE)))
            .add(up.scale(TrackDesign.MAIN_RAIL_HEIGHT)),
        center.add(right.scale(design == TrackDesign.PIPELINE ? 1.5 : TrackDesign.HALF_GAUGE)).add(up.scale(TrackDesign.MAIN_RAIL_HEIGHT)),
        center.add(right.scale(-TrackDesign.CONTROL_HALF_GAUGE)).add(up.scale(control)),
        center.add(right.scale(TrackDesign.CONTROL_HALF_GAUGE)).add(up.scale(control)),
        center.add(
            up.scale(
                (design == TrackDesign.PIPELINE ? -3.0 : design.overhead() ? .90 : TrackDesign.SPINE_HEIGHT)
                    + (ownership.originalOwns(
                            dev.fourthdimension.integration.TrackRenderOwnership.Component.SPINE)
                        ? 0
                        : dev.fourthdimension.physics.ArrowDecorationPlacement.VERTICAL_OFFSET)))
      };
      if (previous != null) {
        for (int rail = 0; rail < 5; rail++) {
          if ((rail == 2 || rail == 3) && design.family != dev.fourthdimension.contract.Family.ARROW) continue;
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
              rail < 2 ? TrackDesign.RAIL_HALF_WIDTH : rail == 4 ? (design.overhead() ? .18 : .22) : .09,
              rail < 2 ? TrackDesign.RAIL_HALF_HEIGHT : rail == 4 ? (design.overhead() ? .18 : .22) : .09,
              rail < 2 ? 0xc6d0d4 : rail == 4 ? decorationColor : 0xf0ca69,
              railPhase[rail]);
          railPhase[rail] += previous[rail].distanceTo(points[rail]);
        }
      }
      if (i % 8 == 0 || i == count) {
        // Native lower bearing slices render through the main-rail centreline. The support
        // truss therefore stays outboard of its wheel envelope and joins below the spine.
        if (ownership.originalOwns(
            dev.fourthdimension.integration.TrackRenderOwnership.Component.RUNNING_SUPPORTS)) {
          var underbrace = center.add(up.scale(design == TrackDesign.PIPELINE ? -3.0 : design.overhead() ? .95 : TrackDesign.RUNNING_UNDERBRACE_HEIGHT));
          for (int side : new int[] {-1, 1}) {
            var outerBottom =
                underbrace.add(right.scale(side * (design == TrackDesign.PIPELINE ? 2.40 : TrackDesign.RUNNING_OUTBOARD_HALF_GAUGE)));
            var outerTop =
                center
                    .add(right.scale(side * (design == TrackDesign.PIPELINE ? 2.40 : TrackDesign.RUNNING_OUTBOARD_HALF_GAUGE)))
                    .add(up.scale(TrackDesign.MAIN_RAIL_HEIGHT));
            tube(
                result,
                underbrace,
                outerBottom,
                basis.forward(),
                up,
                basis.forward(),
                up,
                TrackDesign.RUNNING_CROSSBAR_RADIUS,
                decorationColor);
            if (design == TrackDesign.PIPELINE) {
              // P rail saddle clears the running, side and upstop wheel envelopes.
              var saddle = center.add(right.scale(side * 1.62)).add(up.scale(.18));
              tube(result, saddle, center.add(right.scale(side * 2.40)).add(up.scale(.18)),
                  basis.forward(), up, basis.forward(), up, .04, .02, decorationColor);
              tube(result, saddle, center.add(right.scale(side * 1.62)).add(up.scale(.26)),
                  basis.forward(), right, basis.forward(), right, .04, .005, decorationColor);
            }
            tube(
                result,
                outerBottom,
                outerTop,
                basis.forward(),
                right,
                basis.forward(),
                right,
                TrackDesign.RUNNING_CROSSBAR_RADIUS,
                decorationColor);
          }
        }
        // This native crossbrace joins both external spine beams below the cart channel.
        if (!ownership.originalOwns(
            dev.fourthdimension.integration.TrackRenderOwnership.Component.SPINE)) {
          tube(
              result,
              center
                  .add(up.scale(TrackDesign.RUNNING_UNDERBRACE_HEIGHT))
                  .add(right.scale(-TrackDesign.RUNNING_OUTBOARD_HALF_GAUGE)),
              center
                  .add(up.scale(TrackDesign.RUNNING_UNDERBRACE_HEIGHT))
                  .add(right.scale(TrackDesign.RUNNING_OUTBOARD_HALF_GAUGE)),
              basis.forward(),
              up,
              basis.forward(),
              up,
              TrackDesign.RUNNING_CROSSBAR_RADIUS,
              decorationColor);
          // Bridge the spine-height step when this section meets Original appearance.
          if (i == 0 || i == count)
            tube(
                result,
                center.add(up.scale(TrackDesign.SPINE_HEIGHT)),
                points[4],
                basis.forward(),
                right,
                basis.forward(),
                right,
                TrackDesign.RUNNING_MOUNT_RADIUS,
                decorationColor);
        }
        for (int side = 0; side < 2; side++) {
          if (design.family == dev.fourthdimension.contract.Family.ARROW)
            tube(
                result,
                points[side + 2],
                points[4],
                basis.forward(),
                right,
                basis.forward(),
                right,
                TrackDesign.CONTROL_SUPPORT_RADIUS,
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

  /** The Original spine is the only procedural component omitted for a styled decoration. */
  private static List<Quad> buildSpineFallback(CoasterPathEdge edge, int color) {
    var level = Minecraft.getInstance().level;
    var table = TrackGeometry.table(level, edge);
    int count = Math.max(8, (int) Math.ceil(table.length() * 4));
    var result = new ArrayList<Quad>();
    Vec3 previous = null, oldUp = null, oldRight = null;
    for (int i = 0; i <= count; i++) {
      double distance = table.length() * i / count;
      Vec3 center = CoasterPathGraphManager.sampleEdgePosition(level, edge, table.parameterAt(distance));
      var basis =
          CoasterPathTrackFrame.trackBasisAtHit(
              level, new CoasterPathTrackFrame.GraphHit(center, edge, table.parameterAt(distance)));
      Vec3 right = basis.right(), up = basis.up();
      Vec3 current = center.add(up.scale(TrackDesign.SPINE_HEIGHT));
      if (previous != null)
        tube(result, previous, current, oldRight, oldUp, right, up, .22, .22, color, distance-table.length()/count);
      previous = current;
      oldUp = up;
      oldRight = right;
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
    tube(out,a,b,ar,au,br,bu,radiusX,radiusY,color,0);
  }

  private static void tube(List<Quad> out, Vec3 a,Vec3 b,Vec3 ar,Vec3 au,Vec3 br,Vec3 bu,
                           double radiusX,double radiusY,int color,double phase) {
    // Split long supports so material density stays 16 pixels/block. The mesh is cached;
    // only current world light is looked up at draw time, once per occupied block.
    double length=a.distanceTo(b);
    if(length<1e-9)return;
    double fraction=phase-Math.floor(phase);
    for (double offset=0;offset<length-1e-9;) {
      double endOffset=Math.min(length,offset+1-fraction);
      double t0=offset/length, t1=endOffset/length;
      var start=a.lerp(b,t0); var end=a.lerp(b,t1);
      var sr=ar.lerp(br,t0); var su=au.lerp(bu,t0);
      var er=ar.lerp(br,t1); var eu=au.lerp(bu,t1);
      for (int face = 0; face < 8; face++) {
        double p=2*Math.PI*face/8, q=2*Math.PI*(face+1)/8;
        var v0=start.add(sr.scale(Math.cos(p)*radiusX)).add(su.scale(Math.sin(p)*radiusY));
        var v1=start.add(sr.scale(Math.cos(q)*radiusX)).add(su.scale(Math.sin(q)*radiusY));
        var v2=end.add(er.scale(Math.cos(q)*radiusX)).add(eu.scale(Math.sin(q)*radiusY));
        var v3=end.add(er.scale(Math.cos(p)*radiusX)).add(eu.scale(Math.sin(p)*radiusY));
        var normal=v1.subtract(v0).cross(v2.subtract(v0)).normalize();
        var outward=sr.scale(Math.cos((p+q)/2)).add(su.scale(Math.sin((p+q)/2)));
        if(normal.dot(outward)<0) {
          var swap=v0; v0=v3; v3=swap;
          swap=v1; v1=v2; v2=swap; normal=normal.scale(-1);
        }
        String material=color==0xc6d0d4||color==0xf0ca69?"machined":"structural";
        out.add(new Quad(v0,v1,v2,v3,normal,BlockPos.containing(v0.add(v2).scale(.5)),
            (float)v0.distanceTo(v1),(float)(endOffset-offset),(float)fraction,color,material));
      }
      offset=endOffset; fraction=0;
    }
  }

  private static void draw(VertexConsumer buffer, PoseStack.Pose pose, List<Quad> quads, float alpha) {
    var mc=Minecraft.getInstance();
    for(var q:quads) {
      var sprite=FRAME_SPRITES.computeIfAbsent(q.material, name->mc.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
          .apply(ResourceLocation.fromNamespaceAndPath("fourthdimension","block/fd917/"+name)));
      int light=FRAME_LIGHT.computeIfAbsent(q.lightAt, pos->LevelRenderer.getLightColor(mc.level,pos));
      // Geometry is subdivided before caching, so no UV reaches a neighboring atlas sprite.
      float u=sprite.getU(Math.min(1,q.width)), v=sprite.getV(Math.min(1,q.phase+q.length));
      vertex(buffer,pose,q,q.a,sprite.getU0(),sprite.getV(q.phase),light,alpha);
      vertex(buffer,pose,q,q.b,u,sprite.getV(q.phase),light,alpha);
      vertex(buffer,pose,q,q.c,u,v,light,alpha);
      vertex(buffer,pose,q,q.d,sprite.getU0(),v,light,alpha);
    }
  }

  private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, Quad q, Vec3 p,
                             float u,float v,int light,float alpha) {
    buffer.addVertex(pose.pose(),(float)p.x,(float)p.y,(float)p.z)
        .setColor((q.color>>16)&255,(q.color>>8)&255,q.color&255,(int)(alpha*255))
        .setUv(u,v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
        .setNormal(pose,(float)q.normal.x,(float)q.normal.y,(float)q.normal.z);
  }
}
