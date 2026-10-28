package dev.fourthdimension.client;

import com.mojang.logging.LogUtils;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.EligibilityHooks;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.UUID;
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
 * carrier orientation before dispatch, then optionally observes family-level chassis displacement.
 * The dynamic phase intentionally does not compare a replicated carrier snapshot with a live body:
 * those sources can be one simulation tick apart while a car moves. An optional rider mode requires
 * the local player to remain assigned to and mounted in one carrier seat. An optional support-loss
 * mode further requires that rider's selected-family car to reach its replicated fail-closed state. When
 * configured, that state must arrive only after the client observed the chassis move. It does not verify rendered
 * mesh clearance, player appearance, camera behavior, or a complete moving-ride lifecycle.
 */
@EventBusSubscriber(modid = "fourthdimension", value = Dist.CLIENT)
public final class CarrierTransformValidation {
  private static final double MAX_ANGLE_ERROR = .02;
  private static int completeSamples;
  private static int waitedTicks;
  private static double maxAngleError;
  private static java.util.Map<UUID, Vector3d> motionStarts = new java.util.HashMap<>();
  private static java.util.Map<UUID, Double> maxMotionDistances = new java.util.HashMap<>();
  private static java.util.Set<UUID> compiledBodies = new java.util.HashSet<>();
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
      motionStarts = new java.util.HashMap<>();
      maxMotionDistances = new java.util.HashMap<>();
      compiledBodies = new java.util.HashSet<>();
      CarrierRenderMetrics.reset();
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
    int nativeBodies = 0;
    double sampleMaxError = 0;
    boolean staticPhase = !motionRequired() || completeSamples < requiredSamples();
    for (var vehicle : data.vehicles.values()) {
      var parent = container.getSubLevel(vehicle.chassis);
      if (parent == null) {
        incomplete("missing chassis " + vehicle.chassis);
        return;
      }
      nativeBodies++;
      recordCompiled(parent, vehicle.chassis);
      families.add(vehicle.family);
      if (motionRequired()) {
        var center = parent.getPlot().getCenterBlock();
        var position =
            parent
                .logicalPose()
                .transformPosition(new Vector3d(center.getX() + .5, center.getY() + .5, center.getZ() + .5));
        var start = motionStarts.putIfAbsent(vehicle.chassis, new Vector3d(position));
        if (start != null)
          maxMotionDistances.merge(vehicle.chassis, position.distance(start), Math::max);
      }
      for (var carrier : vehicle.carriers) {
        riderAssignments += carrier.riderSeats.size();
        if (!staticPhase) continue;
        var child = container.getSubLevel(carrier.body);
        if (child == null) {
          incomplete("missing carrier " + carrier.body);
          return;
        }
        nativeBodies++;
        recordCompiled(child, carrier.body);
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
      }
    }
    boolean riderMounted = localRiderReady(minecraft);
    boolean supportLossObserved = supportLossReady(minecraft);
    boolean supportLossAfterMotion = supportLossAfterMotion(supportLossObserved);
    // The active-route fixture deliberately delays support loss until after the pre-dispatch
    // angle samples. Do not keep comparing free carriers against a static snapshot while their
    // chassis is in normal motion; require the exact fail-closed state once that phase begins.
    boolean mayAwaitMovingSupportLoss =
        staticPhase && supportLossMotionRequired() && !supportLossObserved;
    boolean supportLossIncomplete =
        supportLossRequired() && (!supportLossObserved || !supportLossAfterMotion);
    if (families.size() < requiredFamilies()
        || (staticPhase && carriers == 0)
        || (riderRequired() && !riderMounted)
        || (supportLossIncomplete && !mayAwaitMovingSupportLoss)
        || (staticPhase && renderRequired() && compiledBodies.size() < nativeBodies)
        || (staticPhase
            && renderRequired()
            && CarrierRenderMetrics.snapshot().sectionLayerBodies().size() < nativeBodies)) {
      incomplete(
          "required family matrix is not loaded required="
              + requiredFamilies()
              + " families="
              + families
              + " carriers="
              + carriers
              + " riderRequired="
              + riderRequired()
              + " riderMounted="
              + riderMounted
              + " supportLossRequired="
              + supportLossRequired()
              + " supportLossObserved="
              + supportLossObserved
              + " supportLossAfterMotion="
              + supportLossAfterMotion
              + " compiledBodies="
              + compiledBodies.size()
              + "/"
              + nativeBodies
              + " sectionLayerBodies="
              + CarrierRenderMetrics.snapshot().sectionLayerBodies().size()
              + "/"
              + nativeBodies);
      return;
    }
    if (staticPhase) {
      completeSamples++;
      maxAngleError = Math.max(maxAngleError, sampleMaxError);
      if (completeSamples < requiredSamples()) return;
      if (!motionRequired()) {
        finish(
            minecraft,
            families,
            carriers,
            riderAssignments,
            EnumSet.noneOf(Family.class),
            new EnumMap<>(Family.class));
      }
      return;
    }
    var movedFamilies = EnumSet.noneOf(Family.class);
    var movedChassis = new EnumMap<Family, Integer>(Family.class);
    for (var vehicle : data.vehicles.values()) {
      if (maxMotionDistances.getOrDefault(vehicle.chassis, 0d) < 1) continue;
      movedFamilies.add(vehicle.family);
      movedChassis.merge(vehicle.family, 1, Integer::sum);
    }
    if (motionRequired()
        && (movedFamilies.size() < requiredMotionFamilies()
            || movedChassis.getOrDefault(Family.PAX, 0) < requiredMotionPaxChassis())) {
      incomplete(
          "controlled chassis motion below requirement families="
              + requiredMotionFamilies()
              + " paxChassis="
              + requiredMotionPaxChassis()
              + " movedFamilies="
              + movedFamilies
              + " movedChassis="
              + movedChassis
              + " observed="
              + maxMotionDistances);
      return;
    }
    finish(minecraft, families, carriers, riderAssignments, movedFamilies, movedChassis);
  }

  private static void finish(
      Minecraft minecraft,
      EnumSet<Family> families,
      int carriers,
      int riderAssignments,
      EnumSet<Family> movedFamilies,
      EnumMap<Family, Integer> movedChassis) {
    completed = true;
    LogUtils.getLogger()
        .info(
            "FD_CARRIER_VALIDATION passed samples={} target={} waitedTicks={} families={} "
                + "requiredFamilies={} carriers={} riderAssignments={} maxAngleError={} "
                + "compiledBodies={} sectionLayerCalls={} sectionLayerBodies={} renderRequired={} "
                + "riderRequired={} riderMounted={} supportLossRequired={} supportLossObserved={} "
                + "supportLossAfterMotion={} "
                + "motionFamilies={} motionChassis={} motionDistances={}",
            completeSamples,
            requiredSamples(),
            waitedTicks,
            families.size(),
            requiredFamilies(),
            carriers,
            riderAssignments,
            maxAngleError,
            compiledBodies.size(),
            CarrierRenderMetrics.snapshot().sectionLayerCalls(),
            CarrierRenderMetrics.snapshot().sectionLayerBodies().size(),
            renderRequired(),
            riderRequired(),
            localRiderReady(minecraft),
            supportLossRequired(),
            supportLossReady(minecraft),
            supportLossAfterMotion(supportLossReady(minecraft)),
            movedFamilies,
            movedChassis,
            maxMotionDistances);
    if (Boolean.getBoolean("fourthdimension.carrierValidationExit")) minecraft.stop();
  }

  private static void incomplete(String reason) {
    if (waitedTicks > requiredTimeout())
      throw new IllegalStateException("FD_CARRIER_VALIDATION did not initialize: " + reason);
  }

  private static int requiredTimeout() {
    try {
      return Math.clamp(
          Integer.parseInt(System.getProperty("fourthdimension.carrierValidationTimeout", "600")),
          600,
          12_000);
    } catch (NumberFormatException ignored) {
      return 600;
    }
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
    return Boolean.getBoolean("fourthdimension.carrierValidationMotion")
        || Boolean.getBoolean("fourthdimension.carrierValidationSupportLossMotion");
  }

  /** Verifies the local client has the replicated fourth-dimension seat binding it is riding. */
  private static boolean localRiderReady(Minecraft minecraft) {
    var player = minecraft.player;
    if (player == null || !player.isPassenger() || player.getVehicle() == null) return false;
    var binding =
        dev.fourthdimension.vehicle.Boarding.at(minecraft.level, player.getVehicle().blockPosition());
    return binding != null && binding.carrier().riderSeats.containsKey(player.getUUID());
  }

  private static boolean riderRequired() {
    return Boolean.getBoolean("fourthdimension.carrierValidationRider");
  }

  /** The client must receive the exact fail-closed state while its local rider remains assigned. */
  private static boolean supportLossReady(Minecraft minecraft) {
    if (!supportLossRequired() || minecraft.level == null) return !supportLossRequired();
    var player = minecraft.player;
    if (player == null || !player.isPassenger() || player.getVehicle() == null) return false;
    var binding =
        dev.fourthdimension.vehicle.Boarding.at(minecraft.level, player.getVehicle().blockPosition());
    return binding != null
        && binding.vehicle().family == supportLossFamily()
        && binding.vehicle().stopping
        && binding.vehicle().restraints
        && "Matching track support lost".equals(binding.vehicle().fault)
        && binding.carrier().riderSeats.containsKey(player.getUUID());
  }

  private static boolean supportLossRequired() {
    return Boolean.getBoolean("fourthdimension.carrierValidationSupportLoss");
  }

  private static boolean supportLossMotionRequired() {
    return Boolean.getBoolean("fourthdimension.carrierValidationSupportLossMotion");
  }

  /** Requires the observed fail-closed state to follow an actual local chassis displacement. */
  private static boolean supportLossAfterMotion(boolean supportLossObserved) {
    if (!supportLossMotionRequired()) return true;
    if (!supportLossObserved) return false;
    return maxMotionDistances.entrySet().stream()
        .anyMatch(
            entry -> {
              var data = EligibilityHooks.CLIENT.get(Minecraft.getInstance().level);
              var vehicle = data == null ? null : data.vehicles.get(entry.getKey());
              return vehicle != null
                  && vehicle.family == supportLossFamily()
                  && entry.getValue() >= 1;
            });
  }

  private static Family supportLossFamily() {
    var requested =
        System.getProperty("fourthdimension.carrierValidationSupportLossFamily", Family.ARROW.name());
    try {
      return Family.valueOf(requested.trim().toUpperCase(java.util.Locale.ROOT));
    } catch (IllegalArgumentException ignored) {
      return Family.byId(requested.trim());
    }
  }

  /**
   * Records that Sable compiled the native model section containing this vehicle body.
   *
   * <p>This does not assert a submitted frame, shader behavior, occlusion result, player mesh, or
   * clearance. It only proves that the replicated chassis/carrier entered Sable's client block
   * rendering pipeline.
   */
  private static void recordCompiled(
      dev.ryanhcode.sable.sublevel.SubLevel body, UUID id) {
    if (!renderRequired() || !(body instanceof ClientSubLevel clientBody)) return;
    var center = clientBody.getPlot().getCenterBlock();
    if (clientBody.getRenderData().isSectionCompiled(center.getX(), center.getY(), center.getZ()))
      compiledBodies.add(id);
  }

  private static boolean renderRequired() {
    return Boolean.getBoolean("fourthdimension.carrierValidationRender");
  }

  private static int requiredMotionPaxChassis() {
    try {
      return Math.clamp(
          Integer.parseInt(System.getProperty("fourthdimension.carrierValidationMotionPaxChassis", "0")),
          0,
          4);
    } catch (NumberFormatException ignored) {
      return 0;
    }
  }

  private static int requiredMotionFamilies() {
    try {
      return Math.clamp(
          Integer.parseInt(System.getProperty("fourthdimension.carrierValidationMotionFamilies", "1")),
          1,
          Family.values().length);
    } catch (NumberFormatException ignored) {
      return 1;
    }
  }
}
