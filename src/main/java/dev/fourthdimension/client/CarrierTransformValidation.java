package dev.fourthdimension.client;

import com.mojang.logging.LogUtils;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.EligibilityHooks;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import java.util.EnumMap;
import java.util.EnumSet;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Quaterniond;
import org.joml.Vector3d;

/**
 * Opt-in packaged-client check for replicated fourth-dimension carrier transforms.
 *
 * <p>It observes the client snapshot and its live Sable bodies; it never writes poses, riders, or
 * camera state. The required number of functional families is configurable because client worlds
 * can intentionally contain a single-family fixture. A passing sample establishes synchronized
 * carrier orientation before dispatch, then optionally observes controlled-car displacement. The
 * dynamic phase intentionally does not compare a replicated carrier snapshot with a live body: those
 * sources can be one simulation tick apart while a car moves. It does not verify rendered mesh
 * clearance, player appearance, camera behavior, or a moving-ride lifecycle.
 */
@EventBusSubscriber(modid = "fourthdimension", value = Dist.CLIENT)
public final class CarrierTransformValidation {
  private static final double MAX_ANGLE_ERROR = .02;
  private static int completeSamples;
  private static int waitedTicks;
  private static double maxAngleError;
  private static EnumMap<Family, Vector3d> motionStarts = new EnumMap<>(Family.class);
  private static EnumMap<Family, Double> maxMotionDistances = new EnumMap<>(Family.class);
  private static boolean completed;
  private static net.minecraft.world.level.Level lastLevel;

  private CarrierTransformValidation() {}

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    if (!Boolean.getBoolean("fourthdimension.carrierValidation")) return;
    var minecraft = Minecraft.getInstance();
    if (minecraft.level != lastLevel) {
      completeSamples = 0;
      waitedTicks = 0;
      maxAngleError = 0;
      motionStarts = new EnumMap<>(Family.class);
      maxMotionDistances = new EnumMap<>(Family.class);
      completed = false;
      lastLevel = minecraft.level;
    }
    if (completed || minecraft.level == null || minecraft.getOverlay() != null) return;
    waitedTicks++;
    var data = EligibilityHooks.CLIENT.get(minecraft.level);
    var container = SubLevelContainer.getContainer(minecraft.level);
    if (data == null || container == null || data.vehicles.isEmpty()) {
      incomplete("missing client vehicle snapshot or native container");
      return;
    }
    var families = EnumSet.noneOf(Family.class);
    int carriers = 0;
    int riderAssignments = 0;
    double sampleMaxError = 0;
    boolean staticPhase = !motionRequired() || completeSamples < requiredSamples();
    for (var vehicle : data.vehicles.values()) {
      var parent = container.getSubLevel(vehicle.chassis);
      if (parent == null) {
        incomplete("missing chassis " + vehicle.chassis);
        return;
      }
      families.add(vehicle.family);
      if (motionRequired() && vehicle.family != Family.PAX) {
        var center = parent.getPlot().getCenterBlock();
        var position =
            parent
                .logicalPose()
                .transformPosition(new Vector3d(center.getX() + .5, center.getY() + .5, center.getZ() + .5));
        var start = motionStarts.putIfAbsent(vehicle.family, new Vector3d(position));
        if (start != null)
          maxMotionDistances.merge(vehicle.family, position.distance(start), Math::max);
      }
      if (!staticPhase) continue;
      for (var carrier : vehicle.carriers) {
        var child = container.getSubLevel(carrier.body);
        if (child == null) {
          incomplete("missing carrier " + carrier.body);
          return;
        }
        var relative =
            new Quaterniond(parent.logicalPose().orientation())
                .conjugate()
                .mul(child.logicalPose().orientation());
        double nativeAngle = 2 * Math.atan2(relative.x, relative.w);
        double error =
            Math.abs(
                Math.atan2(
                    Math.sin(nativeAngle - carrier.angle), Math.cos(nativeAngle - carrier.angle)));
        if (!Double.isFinite(error) || error > MAX_ANGLE_ERROR) {
          throw new IllegalStateException(
              "FD_CARRIER_VALIDATION angle mismatch body="
                  + carrier.body
                  + " snapshot="
                  + carrier.angle
                  + " native="
                  + nativeAngle
                  + " error="
                  + error);
        }
        sampleMaxError = Math.max(sampleMaxError, error);
        carriers++;
        riderAssignments += carrier.riderSeats.size();
      }
    }
    if (families.size() < requiredFamilies() || (staticPhase && carriers == 0)) {
      incomplete(
          "required family matrix is not loaded required="
              + requiredFamilies()
              + " families="
              + families
              + " carriers="
              + carriers);
      return;
    }
    if (staticPhase) {
      completeSamples++;
      maxAngleError = Math.max(maxAngleError, sampleMaxError);
      if (completeSamples < requiredSamples()) return;
      if (!motionRequired()) {
        finish(minecraft, families, carriers, riderAssignments, EnumSet.noneOf(Family.class));
      }
      return;
    }
    var movedFamilies =
        maxMotionDistances.entrySet().stream()
            .filter(entry -> entry.getValue() >= 1)
            .map(java.util.Map.Entry::getKey)
            .collect(java.util.stream.Collectors.toCollection(() -> EnumSet.noneOf(Family.class)));
    if (motionRequired() && movedFamilies.size() < requiredMotionFamilies()) {
      incomplete(
          "controlled chassis motion below requirement required="
              + requiredMotionFamilies()
              + " observed="
              + maxMotionDistances);
      return;
    }
    finish(minecraft, families, carriers, riderAssignments, movedFamilies);
  }

  private static void finish(
      Minecraft minecraft,
      EnumSet<Family> families,
      int carriers,
      int riderAssignments,
      EnumSet<Family> movedFamilies) {
    completed = true;
    LogUtils.getLogger()
        .info(
            "FD_CARRIER_VALIDATION passed samples={} target={} waitedTicks={} families={} "
                + "requiredFamilies={} carriers={} riderAssignments={} maxAngleError={} "
                + "motionFamilies={} motionDistances={}",
            completeSamples,
            requiredSamples(),
            waitedTicks,
            families.size(),
            requiredFamilies(),
            carriers,
            riderAssignments,
            maxAngleError,
            movedFamilies,
            maxMotionDistances);
    if (Boolean.getBoolean("fourthdimension.carrierValidationExit")) minecraft.stop();
  }

  private static void incomplete(String reason) {
    if (waitedTicks > 600)
      throw new IllegalStateException("FD_CARRIER_VALIDATION did not initialize: " + reason);
  }

  private static int requiredFamilies() {
    try {
      return Math.clamp(
          Integer.parseInt(System.getProperty("fourthdimension.carrierValidationFamilies", "1")),
          1,
          Family.values().length);
    } catch (NumberFormatException ignored) {
      return 1;
    }
  }

  private static int requiredSamples() {
    try {
      return Math.clamp(
          Integer.parseInt(System.getProperty("fourthdimension.carrierValidationSamples", "40")),
          20,
          2_000);
    } catch (NumberFormatException ignored) {
      return 40;
    }
  }

  private static boolean motionRequired() {
    return Boolean.getBoolean("fourthdimension.carrierValidationMotion");
  }

  private static int requiredMotionFamilies() {
    try {
      return Math.clamp(
          Integer.parseInt(System.getProperty("fourthdimension.carrierValidationMotionFamilies", "1")),
          1,
          Family.values().length - 1);
    } catch (NumberFormatException ignored) {
      return 1;
    }
  }
}
