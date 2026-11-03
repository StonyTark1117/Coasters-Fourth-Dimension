package dev.fourthdimension.client.integration;

import com.mojang.logging.LogUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.fourthdimension.integration.TrackAppearance;
import dev.fourthdimension.integration.TrackRenderOwnership;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackDesign;
import dev.fourthdimension.track.TrackGeometry;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.ArrayList;
import java.util.List;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Independently placed references to installed spine/tie models; never external rail components.
 */
public final class TrackStylesRenderer {
  public record Piece(PartialModel model, Matrix4f transform, BlockPos lightAt) {}

  public record Mesh(TrackRenderOwnership ownership, int tint, List<Piece> pieces) {}

  private TrackStylesRenderer() {}

  public static Mesh build(CoasterPathEdge edge, FoundationData.Rail rail) {
    if (!TrackAppearance.CANDIDATES.contains(rail.appearance.id())
        || !rail.appearance.supports(rail.family)
        || !rail.appearance.supportsProfile(rail.profile)) return null;
    var decoration = TrackStylesCatalog.snapshot().get(rail.appearance.style());
    if (decoration == null) return null;
    var mc = Minecraft.getInstance();
    try {
      var missing = mc.getModelManager().getMissingModel();
      if (decoration.spine().get() == missing
          || decoration.ties().get() == missing
          || CachedBuffers.partial(decoration.spine(), Blocks.IRON_BLOCK.defaultBlockState())
              .isEmpty()
          || CachedBuffers.partial(decoration.ties(), Blocks.IRON_BLOCK.defaultBlockState())
              .isEmpty()) return null;
      var table = TrackGeometry.table(mc.level, edge);
      var pieces = new ArrayList<Piece>();
      int segments = Math.max(1, (int) Math.ceil(table.length() * 2));
      double step = table.length() / segments;
      for (int i = 0; i < segments; i++) {
        double d = (i + .5) * step;
        var frame = frame(edge, table.parameterAt(d));
        pieces.add(
            new Piece(
                decoration.spine(),
                dev.fourthdimension.physics.ArrowDecorationPlacement.spine(
                    frame.transform, (float) step),
                BlockPos.containing(frame.center)));
      }
      for (double distance = 0; distance <= table.length() + 1e-6; distance += 2) {
        var frame = frame(edge, table.parameterAt(Math.min(distance, table.length())));
        // The provider's OBJ spine is centered; its tie has separate X=.5, Z=.25 origins.
        pieces.add(
            new Piece(
                decoration.ties(),
                dev.fourthdimension.physics.ArrowDecorationPlacement.tie(frame.transform),
                BlockPos.containing(frame.center)));
      }
      int tint =
          rail.appearance.tint() < 0
              ? TrackDesign.byVariant(rail.variant).color
              : rail.appearance.tint();
      return new Mesh(decoration.ownership(), tint, List.copyOf(pieces));
    } catch (RuntimeException | LinkageError unavailable) {
      // A failed or resource-overridden model cannot suppress the original decoration.
      return null;
    }
  }

  private record Frame(Vec3 center, Matrix4f transform) {}

  private static Frame frame(CoasterPathEdge edge, double parameter) {
    var level = Minecraft.getInstance().level;
    var center = CoasterPathGraphManager.sampleEdgePosition(level, edge, parameter);
    var basis =
        CoasterPathTrackFrame.trackBasisAtHit(
            level, new CoasterPathTrackFrame.GraphHit(center, edge, parameter));
    var u = basis.up();
    var f = basis.forward();
    var transform =
        dev.fourthdimension.physics.ModelFrame.at(
            center.x,
            center.y,
            center.z,
            new org.joml.Vector3d(f.x, f.y, f.z),
            new org.joml.Vector3d(u.x, u.y, u.z));
    return new Frame(center, transform);
  }

  public static boolean draw(Mesh mesh, PoseStack pose, MultiBufferSource buffers) {
    try {
      var level = Minecraft.getInstance().level;
      var consumer = buffers.getBuffer(RenderType.cutoutMipped());
      for (var piece : mesh.pieces)
        if (!drawPiece(
            pose,
            () -> {
              pose.mulPose(piece.transform);
              CachedBuffers.partial(piece.model, Blocks.IRON_BLOCK.defaultBlockState())
                  .reset()
                  .light(LevelRenderer.getLightColor(level, piece.lightAt))
                  .color(mesh.tint)
                  .renderInto(pose, consumer);
            })) {
          LogUtils.getLogger().warn("FD_TRACK_STYLES_DRAW_FALLBACK pieces={}", mesh.pieces.size());
          return false;
        }
      return true;
    } catch (RuntimeException | LinkageError unavailable) {
      LogUtils.getLogger().warn("FD_TRACK_STYLES_DRAW_FALLBACK pieces={}", mesh.pieces.size(), unavailable);
      return false;
    }
  }

  /** Runs one provider draw with a balanced pose even when a provider resource fails at draw time. */
  static boolean drawPiece(PoseStack pose, Runnable draw) {
    return TrackStylesDrawGuard.draw(pose::pushPose, draw, pose::popPose);
  }
}
