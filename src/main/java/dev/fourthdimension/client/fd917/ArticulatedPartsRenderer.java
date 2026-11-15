package dev.fourthdimension.client.fd917;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.fourthdimension.runtime.EligibilityHooks;
import dev.fourthdimension.vehicle.CarrierSeatBlock;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Quaternionf;
import org.joml.Vector3d;

/** World-lit hinge motion on the same interpolated body as the seat shell and rider. */
@EventBusSubscriber(modid = "fourthdimension", value = Dist.CLIENT)
public final class ArticulatedPartsRenderer {
  private static final double SECONDS = .35;
  private record Key(BlockPos seat, ProductionModels.Part part) {}
  private static final class Motion {
    boolean closed;
    double from, target, changed;
    Motion(boolean closed, double now) { this.closed = closed; from = target = closed ? 0 : 1; changed = now; }
    double at(double now) {
      double t = Math.clamp((now-changed)/SECONDS, 0, 1);
      return from + (target-from)*t*t*(3-2*t);
    }
    double update(boolean next, double now) {
      if (closed != next) { from = at(now); target = next ? 0 : 1; closed = next; changed = now; }
      return at(now);
    }
  }
  private static final Map<ClientSubLevel, Map<Key, Motion>> MOTION = new WeakHashMap<>();
  private static Object lastLevel;
  private static int generation = -1;
  private ArticulatedPartsRenderer() {}

  @SubscribeEvent public static void logout(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
    MOTION.clear();lastLevel=null;
  }
  @SubscribeEvent
  public static void render(RenderLevelStageEvent event) {
    if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;
    var mc = Minecraft.getInstance();
    if (lastLevel != mc.level || generation != ProductionModels.generation()) {
      MOTION.clear(); lastLevel = mc.level; generation = ProductionModels.generation();
    }
    if (mc.level == null) return;
    var data = EligibilityHooks.data(mc.level);
    var container = SubLevelContainer.getContainer(mc.level);
    if (data == null || container == null) return;
    long renderStarted=dev.fourthdimension.client.fd917.MechanismRenderMetrics.start();
    var camera = event.getCamera().getPosition();
    var buffers = mc.renderBuffers().bufferSource();
    var out = buffers.getBuffer(Sheets.cutoutBlockSheet());
    var stack = event.getPoseStack();
    double now = System.nanoTime()/1.0e9;
    for (var vehicle : data.vehicles.values()) for (var carrier : vehicle.carriers) {
      if (!(container.getSubLevel(carrier.body) instanceof ClientSubLevel body)) continue;
      var pose = body.renderPose();
      var motions = MOTION.computeIfAbsent(body, ignored -> new HashMap<>());
      var seen = new HashSet<Key>();
      for (var relative : carrier.seats) {
        var seat = body.getPlot().getCenterBlock().offset(relative);
        var state = mc.level.getBlockState(seat);
        // The optional provider owns its own restraints and synchronized attachments.
        if (!(state.getBlock() instanceof CarrierSeatBlock)) continue;
        var parts = ProductionModels.parts(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
        if (parts.isEmpty()) continue;
        var origin = pose.transformPosition(new Vector3d(seat.getX(), seat.getY(), seat.getZ()));
        var center = pose.transformPosition(new Vector3d(seat.getX()+.5, seat.getY()+.5, seat.getZ()+.5));
        boolean visible = center.distanceSquared(camera.x, camera.y, camera.z) <= 128*128 && ProductionMesh.visible(event,center,3);
        int light = visible ? LevelRenderer.getLightColor(mc.level,
            BlockPos.containing(center.x, center.y, center.z)) : 0;
        for (var part : parts) {
          if (!part.matches(state)) continue;
          var key = new Key(relative, part); seen.add(key);
          double progress = motions.computeIfAbsent(key, ignored -> new Motion(part.closed(state), now))
              .update(part.closed(state), now);
          if (!visible) continue;
          var model = mc.getModelManager().getModel(ModelResourceLocation.standalone(part.model()));
          stack.pushPose();
          stack.translate(origin.x-camera.x, origin.y-camera.y, origin.z-camera.z);
          stack.mulPose(new Quaternionf(pose.orientation()));
          facing(stack, state, part.facing());
          stack.translate(part.x(), part.y(), part.z());
          float radians = (float)Math.toRadians(part.openDegrees()*progress);
          stack.mulPose(switch (part.axis()) {
            case "y" -> new Quaternionf().rotationY(radians);
            case "z" -> new Quaternionf().rotationZ(radians);
            default -> new Quaternionf().rotationX(radians);
          });
          stack.translate(-part.x(), -part.y(), -part.z());
          mc.getBlockRenderer().getModelRenderer().renderModel(stack.last(), out, state, model,
              1, 1, 1, light, OverlayTexture.NO_OVERLAY);
          stack.popPose();
        }
      }
      motions.keySet().retainAll(seen);
    }
    buffers.endBatch(Sheets.cutoutBlockSheet());
    dev.fourthdimension.client.fd917.MechanismRenderMetrics.finish("restraints",renderStarted);
  }

  private static void facing(PoseStack stack, BlockState state, boolean rotate) {
    if (!rotate || !state.hasProperty(CarrierSeatBlock.FACING)) return;
    float degrees = switch (state.getValue(CarrierSeatBlock.FACING)) {
      case EAST -> -90; case SOUTH -> 180; case WEST -> 90; default -> 0;
    };
    stack.translate(.5, .5, .5);
    stack.mulPose(new Quaternionf().rotationY((float)Math.toRadians(degrees)));
    stack.translate(-.5, -.5, -.5);
  }
}
