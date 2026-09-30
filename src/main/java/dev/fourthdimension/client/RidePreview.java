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
  private static int loadingPreset;
  private static double previewDamping = .8;
  private static java.util.List<MagneticSpan> magnets = java.util.List.of();

  private RidePreview() {}

  public static void select(
      FoundationData.Rail rail,
      RotationProfile profile,
      int preset,
      double damping,
      java.util.List<MagneticSpan> equipment) {
    if (preset < 0 || preset > 2 || !Double.isFinite(damping) || damping < 0 || damping > 250)
      throw new IllegalArgumentException("Invalid preview loading/damping");
    dev.fourthdimension.magnetic.MagneticData.validate(rail, equipment);
    selected = rail.id;
    controlled = profile;
    cachedAt = Long.MIN_VALUE;
    loadingPreset = preset;
    previewDamping = damping;
    magnets = java.util.List.copyOf(equipment);
    label =
        rail.family.driven()
            ? "Controlled poses | authored angles | conservative swept clearance"
            : "Free reference | "
                + new String[] {
                      "empty",
                      "160 kg ballast/group",
                      rail.family == dev.fourthdimension.contract.Family.FREE_SPIN
                          ? "160 kg left-front only"
                          : "160 kg left group only"
                    }
                    [preset]
                + " | 45 deg, 0 rad/s | powered 2 blocks/s | damping "
                + damping
                + " | original mass model; live motion varies | swept clearance";
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
    boolean magnetic = rail.family == dev.fourthdimension.contract.Family.FREE_SPIN;
    var design = TrackDesign.byVariant(rail.variant);
    int groups = design.groups();
    var states = new FreeRotor.State[groups];
    java.util.Arrays.fill(states, new FreeRotor.State(Math.PI / 4, 0));
    double previousDistance = 0;
    boolean canonical = edge.from().equals(rail.from);
    for (double distance = 0; distance <= table.length(); distance += 2) {
      double t = table.parameterAt(canonical ? distance : table.length() - distance);
      var point = CoasterPathGraphManager.sampleEdgePosition(mc.level, edge, t);
      var basis =
          CoasterPathTrackFrame.trackBasisAtHit(
              mc.level, new CoasterPathTrackFrame.GraphHit(point, edge, t));
      var right = basis.right().scale(canonical ? 1 : -1);
      // Native cart local +Z points opposite track forward (quaternionFromTrackBasis).
      var facing = basis.forward().scale(canonical ? -1 : 1);
      for (int group = 0; group < groups; group++) {
        double side = group < groups / 2 ? -3 : 3;
        double longitudinal = design.pairedRows() ? (group % 2 == 0 ? -3 : 3) : 0;
        var state = states[group];
        double ballast = loadingPreset == 1 || (loadingPreset == 2 && group == 0) ? 1.6 : 0;
        var loading =
            design != TrackDesign.LEGACY
                ? new FreeRotor.Loading(
                    4 + ballast,
                    design == TrackDesign.REVISED ? 1.5 - ballast * .5 : 2.625,
                    (design == TrackDesign.REVISED ? 1.994791666666667 : 4.111979166666667)
                        + ballast * (design == TrackDesign.REVISED ? .37 : .12))
                : new FreeRotor.Loading(6 + ballast, 3, 7.25 + ballast * 1.12);
        if (!rail.family.driven()) {
          double elapsed = (distance - previousDistance) / 2;
          int steps = (int) Math.ceil(elapsed * 120);
          for (int n = 0; n < steps; n++) {
            double from = previousDistance + (distance - previousDistance) * n / steps;
            double to = previousDistance + (distance - previousDistance) * (n + 1) / steps;
            double sampleT =
                table.parameterAt(canonical ? (from + to) / 2 : table.length() - (from + to) / 2);
            var p = CoasterPathGraphManager.sampleEdgePosition(mc.level, edge, sampleT);
            var frame =
                CoasterPathTrackFrame.trackBasisAtHit(
                    mc.level, new CoasterPathTrackFrame.GraphHit(p, edge, sampleT));
            double torque =
                -9.81
                    * frame.forward().y
                    * (canonical ? 1 : -1)
                    * loading.firstMoment()
                    * Math.cos(state.angle());
            state =
                FreeRotor.step(
                    state, loading, 9.81 * frame.up().y, previewDamping, torque, elapsed / steps);
            if (magnetic)
              for (var span : magnets)
                if (span.targets(group)) {
                  double exposure = span.weightedLength(from - longitudinal, to - longitudinal) / 2;
                  double mass = 37 + (loadingPreset == 1 ? 6.4 : loadingPreset == 2 ? 1.6 : 0);
                  double cross = loading.firstMoment() * Math.cos(state.angle());
                  var impulse =
                      PassiveCoupler.impulse(
                          mass,
                          loading.inertia(),
                          cross,
                          mass * 2 + cross * state.omega(),
                          cross * 2 + loading.inertia() * state.omega(),
                          span,
                          exposure);
                  // Declared powered-speed reference: propulsion restores longitudinal speed.
                  state = new FreeRotor.State(state.angle(), state.omega() + impulse.rotation());
                }
          }
        }
        states[group] = state;
        double angle = rail.family.driven() ? controlled.angleAt(distance) : state.angle();
        var up = basis.up().scale(Math.cos(angle)).add(facing.scale(Math.sin(angle)));
        var forward = facing.scale(Math.cos(angle)).subtract(basis.up().scale(Math.sin(angle)));
        var pivot =
            point
                .add(basis.up().scale(2.3125 + design.childPivotOffset()))
                .add(right.scale(side))
                .add(facing.scale(longitudinal));
        box(output, pivot.add(up.scale(-.65)), right, up, forward, .8, .7, .45, 0x67e5ff);
        if (((int) distance) % 6 == 0) {
          var center = pivot.add(right.scale(side < 0 ? -.5 : .5));
          boolean blocked =
              dev.fourthdimension.vehicle.SweptClearance.obstructed(
                  mc.level,
                  center,
                  right,
                  basis.up(),
                  facing,
                  1.5,
                  design.rotorRadius(),
                  design.rotorRadius());
          box(
              output,
              center,
              right,
              basis.up(),
              facing,
              1.5,
              design.rotorRadius(),
              design.rotorRadius(),
              blocked ? 0xff5b5b : 0xf2d36b);
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
