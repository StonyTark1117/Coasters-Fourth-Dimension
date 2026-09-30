package dev.fourthdimension.client;

import dev.fourthdimension.editor.RotationTool;
import dev.fourthdimension.physics.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** Client-only ghost geometry. Free motion is explicitly a reproducible reference scenario. */
@EventBusSubscriber(modid = "fourthdimension", value = Dist.CLIENT)
public final class RidePreview {
  private record Line(Vec3 a, Vec3 b, int color) {}

  private static java.util.List<Line> lines = java.util.List.of();
  private static long cachedAt = Long.MIN_VALUE;
  private static UUID selected;
  private static RotationProfile controlled;
  private static String label;

  private RidePreview() {}

  public static void select(FoundationData.Rail rail, RotationProfile profile) {
    selected = rail.id;
    controlled = profile;
    cachedAt = Long.MIN_VALUE;
    label =
        rail.family.driven()
            ? "Controlled poses | authored angles | conservative swept clearance"
            : "Free example | empty | 45 deg, 0 rad/s | 2 blocks/s | conservative swept clearance";
  }

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    var player = Minecraft.getInstance().player;
    if (player == null || !(player.getMainHandItem().getItem() instanceof RotationTool))
      selected = null;
  }

  @SubscribeEvent
  public static void hud(RenderGuiEvent.Post event) {
    var mc = Minecraft.getInstance();
    if (selected != null) {
      int y = 8;
      for (var line :
          mc.font.split(
              net.minecraft.network.chat.Component.literal(label),
              Math.max(80, mc.getWindow().getGuiScaledWidth() - 16))) {
        event.getGuiGraphics().drawString(mc.font, line, 8, y, 0xffefb2);
        y += mc.font.lineHeight + 1;
      }
    }
  }

  @SubscribeEvent
  public static void render(RenderLevelStageEvent event) {
    if (selected == null || event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES)
      return;
    var mc = Minecraft.getInstance();
    if (mc.level == null) return;
    var data = EligibilityHooks.data(mc.level);
    if (data == null) return;
    var rail = data.rails.stream().filter(r -> r.id.equals(selected)).findFirst().orElse(null);
    if (rail == null) return;
    var edge = TrackGeometry.resolve(mc.level, rail);
    if (edge == null) return;
    long now = mc.level.getGameTime() / 20;
    if (cachedAt != now) {
      lines = build(rail, edge);
      cachedAt = now;
    }
    var pose = event.getPoseStack();
    var camera = event.getCamera().getPosition();
    pose.pushPose();
    pose.translate(-camera.x, -camera.y, -camera.z);
    var buffers = mc.renderBuffers().bufferSource();
    var buffer = buffers.getBuffer(RenderType.lines());
    for (var line : lines) {
      var normal = line.b.subtract(line.a).normalize();
      for (var point : new Vec3[] {line.a, line.b})
        buffer
            .addVertex(pose.last().pose(), (float) point.x, (float) point.y, (float) point.z)
            .setColor((line.color >> 16) & 255, (line.color >> 8) & 255, line.color & 255, 190)
            .setNormal(pose.last(), (float) normal.x, (float) normal.y, (float) normal.z);
    }
    buffers.endBatch(RenderType.lines());
    pose.popPose();
  }

  private static java.util.List<Line> build(FoundationData.Rail rail, CoasterPathEdge edge) {
    var mc = Minecraft.getInstance();
    var table = TrackGeometry.table(mc.level, edge);
    var output = new java.util.ArrayList<Line>();
    var state = new FreeRotor.State(Math.PI / 4, 0);
    var loading = new FreeRotor.Loading(6, 3, 7.25);
    double previousDistance = 0;
    for (double distance = 0; distance <= table.length(); distance += 2) {
      double t = table.parameterAt(distance);
      var point = CoasterPathGraphManager.sampleEdgePosition(mc.level, edge, t);
      var basis =
          CoasterPathTrackFrame.trackBasisAtHit(
              mc.level, new CoasterPathTrackFrame.GraphHit(point, edge, t));
      if (!rail.family.driven()) {
        // Empty legacy reference: fixed forward speed, native game's gravity scale; no random
        // phase.
        double elapsed = (distance - previousDistance) / 2;
        int steps = (int) Math.ceil(elapsed * 120);
        for (int n = 0; n < steps; n++) {
          double sample = previousDistance + (distance - previousDistance) * (n + .5) / steps;
          double sampleT = table.parameterAt(sample);
          var p = CoasterPathGraphManager.sampleEdgePosition(mc.level, edge, sampleT);
          var frame =
              CoasterPathTrackFrame.trackBasisAtHit(
                  mc.level, new CoasterPathTrackFrame.GraphHit(p, edge, sampleT));
          double longitudinalTorque =
              -9.81 * frame.forward().y * loading.firstMoment() * Math.cos(state.angle());
          state =
              FreeRotor.step(
                  state, loading, 9.81 * frame.up().y, .8, longitudinalTorque, elapsed / steps);
        }
      }
      double angle =
          rail.family.driven()
              ? controlled.angleAt(
                  edge.from().equals(rail.from) ? distance : table.length() - distance)
              : state.angle();
      var up = basis.up().scale(Math.cos(angle)).add(basis.forward().scale(Math.sin(angle)));
      var forward =
          basis.forward().scale(Math.cos(angle)).subtract(basis.up().scale(Math.sin(angle)));
      for (double side : new double[] {-3, 3}) {
        var pivot = point.add(basis.up().scale(2.3125)).add(basis.right().scale(side));
        box(output, pivot.add(up.scale(-.65)), basis.right(), up, forward, .8, .7, .45, 0x67e5ff);
      }
      if (((int) distance) % 6 == 0) {
        for (double side : new double[] {-3.5, 3.5}) {
          var center = point.add(basis.up().scale(2.3125)).add(basis.right().scale(side));
          boolean obstruction =
              dev.fourthdimension.vehicle.SweptClearance.obstructed(
                  mc.level, center, basis.right(), basis.up(), basis.forward(), 1.5, 2.7, 2.7);
          box(
              output,
              center,
              basis.right(),
              basis.up(),
              basis.forward(),
              1.5,
              2.7,
              2.7,
              obstruction ? 0xff5b5b : 0xf2d36b);
        }
      }
      previousDistance = distance;
    }
    return java.util.List.copyOf(output);
  }

  private static void box(
      java.util.List<Line> out,
      Vec3 center,
      Vec3 right,
      Vec3 up,
      Vec3 forward,
      double x,
      double y,
      double z,
      int color) {
    var points = new Vec3[8];
    for (int i = 0; i < 8; i++)
      points[i] =
          center
              .add(right.scale((i & 1) == 0 ? -x : x))
              .add(up.scale((i & 2) == 0 ? -y : y))
              .add(forward.scale((i & 4) == 0 ? -z : z));
    for (int i = 0; i < 8; i++)
      for (int bit : new int[] {1, 2, 4})
        if ((i & bit) == 0) out.add(new Line(points[i], points[i | bit], color));
  }
}
