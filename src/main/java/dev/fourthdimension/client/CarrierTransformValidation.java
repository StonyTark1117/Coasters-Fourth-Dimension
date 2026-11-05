package dev.fourthdimension.client;

import com.mojang.logging.LogUtils;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.EligibilityHooks;
import dev.fourthdimension.track.TrackDesign;
import dev.fourthdimension.track.TrackGeometry;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrackSnap;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
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
 * Lifecycle-only fixtures can explicitly skip that static gate when a separate static matrix has
 * already established it and the fixture must dispatch before its support-loss observation.
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
  private static double maxRecoveryChassisBankAngle;
  private static double maxRecoveryBearingBankAngle;
  private static java.util.Map<UUID, Vector3d> motionStarts = new java.util.HashMap<>();
  private static java.util.Map<UUID, Double> maxMotionDistances = new java.util.HashMap<>();
  private static java.util.Set<UUID> compiledBodies = new java.util.HashSet<>();
  private static boolean supportLossSeen;
  private static boolean supportRepairSeen;
  private static boolean supportRealignmentSeen;
  private static boolean recoveryLinkSeen;
  private static boolean recoveryMotionSeen;
  private static boolean recoveryCycleSeen;
  private static boolean recoveryProfileSeen;
  private static boolean recoveryEditorSeen;
  private static boolean recoveryEditorActionSeen;
  private static boolean recoveryEditorActionApplied;
  private static boolean recoveryEditorAttachRendered;
  private static boolean recoveryEditorAttachSeen;
  private static boolean recoveryEditorAttachApplied;
  private static boolean recoveryTrainSeen;
  private static boolean recoveryTrainMotionSeen;
  private static boolean localRiderRenderObservationArmed;
  private static CameraType playerRenderCameraBefore;
  private static boolean completed;
  private static net.minecraft.world.level.Level lastLevel;

  private CarrierTransformValidation() {}

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    if (!Boolean.getBoolean("fourthdimension.carrierValidation")) return;
    var minecraft = Minecraft.getInstance();
    if (minecraft.level != lastLevel) {
      restorePlayerRenderCamera(minecraft);
      completeSamples = 0;
      waitedTicks = 0;
      maxAngleError = 0;
      maxRecoveryChassisBankAngle = 0;
      maxRecoveryBearingBankAngle = 0;
      motionStarts = new java.util.HashMap<>();
      maxMotionDistances = new java.util.HashMap<>();
      compiledBodies = new java.util.HashSet<>();
      supportLossSeen = false;
      supportRepairSeen = false;
      supportRealignmentSeen = false;
      recoveryLinkSeen = false;
      recoveryMotionSeen = false;
      recoveryCycleSeen = false;
      recoveryProfileSeen = false;
      recoveryEditorSeen = false;
      recoveryEditorActionSeen = false;
      recoveryEditorActionApplied = false;
      recoveryEditorAttachRendered = false;
      recoveryEditorAttachSeen = false;
      recoveryEditorAttachApplied = false;
      recoveryTrainSeen = false;
      recoveryTrainMotionSeen = false;
      localRiderRenderObservationArmed = false;
      playerRenderCameraBefore = null;
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
    String angleMismatch = null;
    boolean staticPhase = !skipStaticSamples() && (!motionRequired() || completeSamples < requiredSamples());
    boolean recoveryLinkObserved = false;
    var linkedRecoveryTrainBodies = new java.util.ArrayList<SubLevel>();
    var requiredRenderBodies = new java.util.HashSet<UUID>();
    var requiredSectionLayerBodies = new java.util.HashSet<UUID>();
    var requiredNativeCartBodies = new java.util.HashSet<UUID>();
    vehicles:
    for (var vehicle : data.vehicles.values()) {
      var parent = container.getSubLevel(vehicle.chassis);
      if (parent == null) {
        incomplete("missing chassis " + vehicle.chassis);
        return;
      }
      nativeBodies++;
      requiredRenderBodies.add(vehicle.chassis);
      requiredSectionLayerBodies.add(vehicle.chassis);
      recordCompiled(parent, vehicle.chassis);
      families.add(vehicle.family);
      if (motionRequired()) recordMotion(parent, vehicle.chassis);
      if (recoveryBankRequired()
          && maxMotionDistances.getOrDefault(vehicle.chassis, 0d) >= 1)
        maxRecoveryChassisBankAngle =
            Math.max(maxRecoveryChassisBankAngle, bankAngle(parent));
      if (recoveryRequired() && vehicle.recoveryCart != null) {
        var recovery = container.getSubLevel(vehicle.recoveryCart);
        if (recovery == null) {
          incomplete("missing linked recovery bearing " + vehicle.recoveryCart);
          return;
        }
        // Foundation snapshots and native cart-link payloads can legitimately cross while the
        // server completes the requested detach. Before the first observation this is a real
        // replication failure; after it, retain the successful linked-group observation.
        if (!recoveryLinkSeen && !recoveryNativeLinkPresent(parent, recovery)) {
          incomplete("recovery bearing exists but its native link is not replicated");
          return;
        }
        nativeBodies++;
        requiredRenderBodies.add(vehicle.recoveryCart);
        requiredNativeCartBodies.add(vehicle.recoveryCart);
        recordCompiled(recovery, vehicle.recoveryCart);
        if (motionRequired()) recordMotion(recovery, vehicle.recoveryCart);
        if (recoveryBankRequired()
            && maxMotionDistances.getOrDefault(vehicle.recoveryCart, 0d) >= 1)
          maxRecoveryBearingBankAngle =
              Math.max(maxRecoveryBearingBankAngle, bankAngle(recovery));
        recoveryLinkObserved = true;
        recoveryLinkSeen = true;
        linkedRecoveryTrainBodies.add(parent);
        if (recoveryEditorActionRequired() && recoveryEditorActionSeen && !vehicle.stopping)
          recoveryEditorActionApplied = true;
        if (recoveryEditorAttachRequired() && recoveryEditorAttachSeen)
          recoveryEditorAttachApplied = true;
      }
      if (recoveryProfileRequired() && renderedNonzeroProfileRail(data, vehicle))
        recoveryProfileSeen = true;
      for (var carrier : vehicle.carriers) {
        riderAssignments += carrier.riderSeats.size();
        if (!staticPhase) continue;
        var child = container.getSubLevel(carrier.body);
        if (child == null) {
          incomplete("missing carrier " + carrier.body);
          return;
        }
        nativeBodies++;
        requiredRenderBodies.add(carrier.body);
        requiredSectionLayerBodies.add(carrier.body);
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
          angleMismatch =
              "angle mismatch body="
                  + carrier.body
                  + " snapshot="
                  + carrier.angle
                  + " native="
                  + nativeAngle
                  + " error="
                  + error;
          break vehicles;
        }
        sampleMaxError = Math.max(sampleMaxError, error);
        carriers++;
      }
    }
    if (recoveryTrainRequired() && linkedRecoveryTrainBodies.size() >= 2) {
      boolean originalTrainLinked =
          java.util.stream.IntStream.range(1, linkedRecoveryTrainBodies.size())
              .allMatch(
                  index ->
                      CoasterCartTrainLinkConstraint.areLinkedCarts(
                          linkedRecoveryTrainBodies.get(index - 1),
                          linkedRecoveryTrainBodies.get(index)));
      if (originalTrainLinked) {
        recoveryTrainSeen = true;
        if (linkedRecoveryTrainBodies.stream()
            .allMatch(body -> maxMotionDistances.getOrDefault(body.getUniqueId(), 0d) >= 1))
          recoveryTrainMotionSeen = true;
      }
    }
    // Client Sable bodies can receive an initial movement packet after their snapshot. Treat the
    // gate as the documented consecutive-sample check: reset startup jitter, but fail by timeout
    // if the client never reaches a stable replicated orientation.
    if (staticPhase && angleMismatch != null) {
      completeSamples = 0;
      incomplete(angleMismatch);
      return;
    }
    boolean riderMounted = localRiderReady(minecraft);
    if (playerRenderRequired() && riderMounted && !localRiderRenderObservationArmed) {
      // Ignore any third-person player draw that happened before the rider reached this exact
      // carrier seat. The renderer observation below is therefore tied to a live binding.
      CarrierRenderMetrics.beginLocalRiderObservation();
      localRiderRenderObservationArmed = true;
      enablePlayerRenderCamera(minecraft);
    }
    recoveryCycleSeen |= recoveryCycleReady(minecraft, data);
    boolean hadSeenSupportLoss = supportLossSeen;
    boolean supportLossObserved = supportLossReady(minecraft);
    supportLossSeen |= supportLossObserved;
    // A healthy, captured boarding car can already satisfy the repaired-state shape. Require
    // the client to have observed the fault on an earlier tick before accepting repair.
    boolean hadSeenSupportRepair = supportRepairSeen;
    boolean supportRepairObserved = hadSeenSupportLoss && supportRepairReady(minecraft);
    supportRepairSeen |= supportRepairObserved;
    // That same healthy car can also pass the native bearing check. Count realignment only
    // after a repaired state was observed on an earlier tick.
    boolean supportRealignmentObserved =
        hadSeenSupportRepair && supportRealignmentReady(minecraft);
    supportRealignmentSeen |= supportRealignmentObserved;
    boolean supportLossAfterMotion = supportLossAfterMotion(supportLossSeen);
    // The active-route fixture deliberately delays support loss until after the pre-dispatch
    // angle samples. Do not keep comparing free carriers against a static snapshot while their
    // chassis is in normal motion; require the exact fail-closed state once that phase begins.
    boolean mayAwaitMovingSupportLoss =
        staticPhase && supportLossMotionRequired() && !supportLossObserved;
    boolean supportLossIncomplete =
        supportLossRequired() && (!supportLossSeen || !supportLossAfterMotion);
    boolean supportRepairIncomplete =
        supportRepairRequired() && (!supportLossSeen || !supportRepairSeen);
    boolean supportRealignmentIncomplete =
        supportRealignmentRequired() && (!supportLossSeen || !supportRepairSeen || !supportRealignmentSeen);
    // The recovery-cycle fixture intentionally unloads both the rider and its scene after a
    // successful release. Retain the observations made while it was live rather than treating
    // that expected teardown as an empty, uninitialized family matrix.
    boolean completedRecoveryLifecycle = recoveryCycleRequired() && recoveryCycleSeen;
    if ((!completedRecoveryLifecycle && families.size() < requiredFamilies())
        || (staticPhase && carriers == 0)
        || (recoveryRequired() && !recoveryLinkObserved && !recoveryLinkSeen)
        || (recoveryEditorRequired() && !recoveryEditorSeen)
        || (recoveryEditorActionRequired()
            && (!recoveryEditorActionSeen || !recoveryEditorActionApplied))
        || (recoveryEditorAttachRequired()
            && (!recoveryEditorAttachRendered
                || !recoveryEditorAttachSeen
                || !recoveryEditorAttachApplied))
        || (recoveryTrainRequired() && !recoveryTrainSeen)
        || (riderRequired() && !riderMounted && !(recoveryCycleRequired() && recoveryCycleSeen))
        || (staticPhase
            && playerRenderRequired()
            && (!localRiderRenderObservationArmed
                || CarrierRenderMetrics.snapshot().localRiderRenderCalls() == 0))
        || (supportLossIncomplete && !mayAwaitMovingSupportLoss)
        || supportRepairIncomplete
        || supportRealignmentIncomplete
        || (staticPhase && renderRequired() && !compiledBodies.containsAll(requiredRenderBodies))
        || (staticPhase
            && renderRequired()
            && !CarrierRenderMetrics.snapshot()
                .sectionLayerBodies()
                .containsAll(requiredSectionLayerBodies))
        || (staticPhase
            && renderRequired()
            && !CarrierRenderMetrics.snapshot().coasterCartBodies().containsAll(requiredNativeCartBodies))) {
      incomplete(
          "required family matrix is not loaded required="
              + requiredFamilies()
              + " families="
              + families
              + " carriers="
              + carriers
              + " recoveryRequired="
              + recoveryRequired()
              + " recoveryLinkObserved="
              + recoveryLinkObserved
              + " recoveryEditorRequired="
              + recoveryEditorRequired()
              + " recoveryEditorSeen="
              + recoveryEditorSeen
              + " recoveryEditorActionRequired="
              + recoveryEditorActionRequired()
              + " recoveryEditorActionSeen="
              + recoveryEditorActionSeen
              + " recoveryEditorActionApplied="
              + recoveryEditorActionApplied
              + " recoveryEditorAttachRequired="
              + recoveryEditorAttachRequired()
              + " recoveryEditorAttachRendered="
              + recoveryEditorAttachRendered
              + " recoveryEditorAttachSeen="
              + recoveryEditorAttachSeen
              + " recoveryEditorAttachApplied="
              + recoveryEditorAttachApplied
              + " recoveryTrainRequired="
              + recoveryTrainRequired()
              + " recoveryTrainSeen="
              + recoveryTrainSeen
              + " recoveryTrainMotionSeen="
              + recoveryTrainMotionSeen
              + " riderRequired="
              + riderRequired()
              + " riderMounted="
              + riderMounted
              + " playerRenderRequired="
              + playerRenderRequired()
              + " localRiderRenderCalls="
              + CarrierRenderMetrics.snapshot().localRiderRenderCalls()
              + " supportLossRequired="
              + supportLossRequired()
              + " supportLossObserved="
              + supportLossSeen
              + " supportLossAfterMotion="
              + supportLossAfterMotion
              + " supportRepairRequired="
              + supportRepairRequired()
              + " supportRepairObserved="
              + supportRepairSeen
              + " supportRealignmentRequired="
              + supportRealignmentRequired()
              + " supportRealignmentObserved="
              + supportRealignmentSeen
              + " compiledBodies="
              + compiledBodies.size()
              + "/"
              + nativeBodies
              + " requiredRenderBodies="
              + requiredRenderBodies.size()
              + " sectionLayerBodies="
              + CarrierRenderMetrics.snapshot().sectionLayerBodies().size()
              + "/"
              + nativeBodies
              + " requiredRenderBodyIds="
              + requiredRenderBodies
              + " sectionLayerBodyIds="
              + CarrierRenderMetrics.snapshot().sectionLayerBodies()
              + " requiredNativeCartBodyIds="
              + requiredNativeCartBodies
              + " nativeCartBodyIds="
              + CarrierRenderMetrics.snapshot().coasterCartBodies());
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
    boolean recoveryMoved =
        data.vehicles.values().stream()
            .filter(vehicle -> vehicle.recoveryCart != null)
            .anyMatch(
                vehicle -> maxMotionDistances.getOrDefault(vehicle.recoveryCart, 0d) >= 1);
    recoveryMotionSeen |= recoveryMoved;
    if (motionRequired()
        && (!completedRecoveryLifecycle
            && (movedFamilies.size() < requiredMotionFamilies()
            || movedChassis.getOrDefault(Family.PAX, 0) < requiredMotionPaxChassis()
            || (recoveryMotionRequired() && !recoveryMotionSeen)
            || (recoveryBankRequired()
                && (maxRecoveryChassisBankAngle < .5 || maxRecoveryBearingBankAngle < .5))
            || (recoveryCycleRequired()
                && (!recoveryLinkSeen || !recoveryMotionSeen || !recoveryCycleSeen))
            || (recoveryProfileRequired() && !recoveryProfileSeen)
            || (recoveryTrainRequired() && !recoveryTrainMotionSeen)))) {
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
              + maxMotionDistances
              + " recoveryMotionRequired="
              + recoveryMotionRequired()
              + " recoveryMoved="
              + recoveryMoved
              + " recoveryBankRequired="
              + recoveryBankRequired()
              + " maxRecoveryChassisBankAngle="
              + maxRecoveryChassisBankAngle
              + " maxRecoveryBearingBankAngle="
              + maxRecoveryBearingBankAngle
              + " recoveryCycleRequired="
              + recoveryCycleRequired()
              + " recoveryLinkSeen="
              + recoveryLinkSeen
              + " recoveryMotionSeen="
              + recoveryMotionSeen
              + " recoveryCycleSeen="
              + recoveryCycleSeen
              + " recoveryProfileRequired="
              + recoveryProfileRequired()
              + " recoveryProfileSeen="
              + recoveryProfileSeen
              + " recoveryEditorRequired="
              + recoveryEditorRequired()
              + " recoveryEditorSeen="
              + recoveryEditorSeen
              + " recoveryTrainRequired="
              + recoveryTrainRequired()
              + " recoveryTrainSeen="
              + recoveryTrainSeen
              + " recoveryTrainMotionSeen="
              + recoveryTrainMotionSeen);
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
    restorePlayerRenderCamera(minecraft);
    LogUtils.getLogger()
        .info(
            "FD_CARRIER_VALIDATION passed samples={} target={} waitedTicks={} families={} "
                + "requiredFamilies={} carriers={} riderAssignments={} maxAngleError={} "
                + "compiledBodies={} sectionLayerCalls={} sectionLayerBodies={} nativeCartBodies={} renderRequired={} "
                + "riderRequired={} riderMounted={} supportLossRequired={} supportLossObserved={} "
                + "playerRenderRequired={} localRiderRenderCalls={} "
                + "supportLossAfterMotion={} supportRepairRequired={} supportRepairObserved={} "
                + "supportRealignmentRequired={} supportRealignmentObserved={} "
                + "recoveryRequired={} recoveryMotionRequired={} recoveryBankRequired={} recoveryCycleRequired={} recoveryProfileRequired={} recoveryEditorRequired={} recoveryEditorActionRequired={} recoveryEditorAttachRequired={} recoveryTrainRequired={} recoveryLinkSeen={} recoveryMotionSeen={} recoveryCycleSeen={} recoveryProfileSeen={} recoveryEditorSeen={} recoveryEditorActionSeen={} recoveryEditorActionApplied={} recoveryEditorAttachRendered={} recoveryEditorAttachSeen={} recoveryEditorAttachApplied={} recoveryTrainSeen={} recoveryTrainMotionSeen={} maxRecoveryChassisBankAngle={} maxRecoveryBearingBankAngle={} motionFamilies={} motionChassis={} motionDistances={}",
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
            CarrierRenderMetrics.snapshot().coasterCartBodies().size(),
            renderRequired(),
            riderRequired(),
            localRiderReady(minecraft),
            playerRenderRequired(),
            CarrierRenderMetrics.snapshot().localRiderRenderCalls(),
            supportLossRequired(),
            supportLossSeen,
            supportLossAfterMotion(supportLossSeen),
            supportRepairRequired(),
            supportRepairSeen,
            supportRealignmentRequired(),
            supportRealignmentSeen,
            recoveryRequired(),
            recoveryMotionRequired(),
            recoveryBankRequired(),
            recoveryCycleRequired(),
            recoveryProfileRequired(),
            recoveryEditorRequired(),
            recoveryEditorActionRequired(),
            recoveryEditorAttachRequired(),
            recoveryTrainRequired(),
            recoveryLinkSeen,
            recoveryMotionSeen,
            recoveryCycleSeen,
            recoveryProfileSeen,
            recoveryEditorSeen,
            recoveryEditorActionSeen,
            recoveryEditorActionApplied,
            recoveryEditorAttachRendered,
            recoveryEditorAttachSeen,
            recoveryEditorAttachApplied,
            recoveryTrainSeen,
            recoveryTrainMotionSeen,
            maxRecoveryChassisBankAngle,
            maxRecoveryBearingBankAngle,
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

  /** Lifecycle-only fixtures can rely on the dedicated static transform matrix instead. */
  private static boolean skipStaticSamples() {
    return Boolean.getBoolean("fourthdimension.carrierValidationSkipStatic");
  }

  private static boolean motionRequired() {
    return Boolean.getBoolean("fourthdimension.carrierValidationMotion")
        || Boolean.getBoolean("fourthdimension.carrierValidationSupportLossMotion")
        || recoveryMotionRequired()
        || recoveryBankRequired()
        || recoveryCycleRequired()
        || recoveryProfileRequired();
  }

  private static boolean recoveryRequired() {
    return Boolean.getBoolean("fourthdimension.carrierValidationRecovery");
  }

  private static boolean recoveryMotionRequired() {
    return Boolean.getBoolean("fourthdimension.carrierValidationRecoveryMotion");
  }

  private static boolean recoveryBankRequired() {
    return Boolean.getBoolean("fourthdimension.carrierValidationRecoveryBank");
  }

  private static boolean recoveryCycleRequired() {
    return Boolean.getBoolean("fourthdimension.carrierValidationRecoveryCycle");
  }

  private static boolean recoveryProfileRequired() {
    return Boolean.getBoolean("fourthdimension.carrierValidationRecoveryProfile");
  }

  private static boolean recoveryEditorRequired() {
    return Boolean.getBoolean("fourthdimension.carrierValidationRecoveryEditor");
  }

  private static boolean recoveryEditorActionRequired() {
    return Boolean.getBoolean("fourthdimension.carrierValidationRecoveryEditorAction");
  }

  private static boolean recoveryEditorAttachRequired() {
    return Boolean.getBoolean("fourthdimension.carrierValidationRecoveryEditorAttach");
  }

  private static boolean recoveryTrainRequired() {
    return Boolean.getBoolean("fourthdimension.carrierValidationRecoveryTrain");
  }

  /**
   * A single recovery car must directly link to its ordinary bearing. A native original train has
   * one such direct edge at its end; its remaining cars prove recovery through membership in that
   * same native train graph.
   */
  private static boolean recoveryNativeLinkPresent(SubLevel car, SubLevel recovery) {
    return CoasterCartTrainLinkConstraint.areLinkedCarts(car, recovery)
        || (recoveryTrainRequired()
            && CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(car)
                .contains(recovery.getUniqueId()));
  }

  /** Called by the actual recovery editor after it has rendered its linked-vehicle controls. */
  public static void recoveryEditorRendered() {
    recoveryEditorSeen = true;
  }

  /** Called only by the visible recovery editor's Start tow button callback. */
  public static void recoveryEditorStartTowClicked() {
    recoveryEditorActionSeen = true;
  }

  /** Called by the visible pre-attachment editor after the Attach recovery control is drawn. */
  public static void recoveryEditorAttachRendered() {
    recoveryEditorAttachRendered = true;
  }

  /** Called only by the visible pre-attachment editor's Attach recovery button callback. */
  public static void recoveryEditorAttachClicked() {
    recoveryEditorAttachSeen = true;
  }

  /** Lets the opt-in client fixture wait for native recovery replication before its screen click. */
  public static boolean recoveryEditorActionReady() {
    return recoveryLinkSeen;
  }

  private static boolean renderedNonzeroProfileRail(
      dev.fourthdimension.runtime.FoundationData data,
      dev.fourthdimension.runtime.FoundationData.Vehicle vehicle) {
    if (vehicle.family != Family.ARROW) return false;
    var rendered = TrackRenderMetrics.snapshot().nonzeroProfileRails();
    return data.rails.stream()
        .anyMatch(
            rail ->
                rendered.contains(rail.id)
                    && rail.matches(vehicle.boundFrom, vehicle.boundTo)
                    && rail.family == Family.ARROW
                    && rail.profile.keys().stream()
                        .anyMatch(key -> Math.abs(key.angle()) >= .25));
  }

  /** Requires normal recovery release only after the client observed the linked moving group. */
  private static boolean recoveryCycleReady(
      Minecraft minecraft, dev.fourthdimension.runtime.FoundationData data) {
    if (!recoveryCycleRequired() || minecraft.player == null || minecraft.player.isPassenger()) return false;
    if (!recoveryLinkSeen || !recoveryMotionSeen) return false;
    if (data.vehicles.values().stream()
        .anyMatch(
            vehicle ->
                vehicle.family == recoveryFixtureFamily()
                    && vehicle.recoveryCart == null
                    && vehicle.stopping
                    && !vehicle.restraints)) return true;
    // Native unlink and the rider's dismount can arrive one client tick before the larger
    // Free Spin carrier snapshot clears recoveryCart. That crossed state is an observed release,
    // not a missing link: the production detach has already removed the native constraint.
    var container = SubLevelContainer.getContainer(minecraft.level);
    return data.vehicles.values().stream()
        .filter(vehicle -> vehicle.family == recoveryFixtureFamily() && vehicle.recoveryCart != null)
        .anyMatch(
            vehicle -> {
              var parent = container.getSubLevel(vehicle.chassis);
              var recovery = container.getSubLevel(vehicle.recoveryCart);
              return parent != null
                  && recovery != null
                  && !CoasterCartTrainLinkConstraint.areLinkedCarts(parent, recovery);
            });
  }

  /** Matches the flat recovery fixture selected for this client invocation. */
  private static Family recoveryFixtureFamily() {
    var requested =
        System.getProperty(
            "fourthdimension.carrierFixtureRecoveryDesign", TrackDesign.ARROW.name());
    try {
      return TrackDesign.valueOf(requested.trim().toUpperCase(java.util.Locale.ROOT)).family;
    } catch (IllegalArgumentException failure) {
      throw new IllegalStateException(
          "FD_CARRIER_VALIDATION recovery design must name a supported TrackDesign: " + requested,
          failure);
    }
  }

  private static void recordMotion(dev.ryanhcode.sable.sublevel.SubLevel body, UUID id) {
    var center = body.getPlot().getCenterBlock();
    var position =
        body
            .logicalPose()
            .transformPosition(new Vector3d(center.getX() + .5, center.getY() + .5, center.getZ() + .5));
    var start = motionStarts.putIfAbsent(id, new Vector3d(position));
    if (start != null) maxMotionDistances.merge(id, position.distance(start), Math::max);
  }

  /** Measures physical roll from the native body's world-up vector, not controlled carrier spin. */
  private static double bankAngle(dev.ryanhcode.sable.sublevel.SubLevel body) {
    var up = new Vector3d(0, 1, 0).rotate(body.logicalPose().orientation());
    return Math.acos(Math.clamp(up.y, -1, 1));
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

  /** Requires a real third-person local-player entity draw after the rider joins its carrier. */
  private static boolean playerRenderRequired() {
    return Boolean.getBoolean("fourthdimension.carrierValidationPlayerRender");
  }

  /**
   * In the opt-in client probe, make vanilla submit the mounted local-player entity without
   * requiring a synthetic key press. This is deliberately separate from the assertion itself so
   * ordinary validation and production clients retain their selected camera.
   */
  private static void enablePlayerRenderCamera(Minecraft minecraft) {
    if (!Boolean.getBoolean("fourthdimension.carrierValidationPlayerRenderAutoCamera")
        || playerRenderCameraBefore != null)
      return;
    playerRenderCameraBefore = minecraft.options.getCameraType();
    if (playerRenderCameraBefore == CameraType.FIRST_PERSON)
      minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
  }

  private static void restorePlayerRenderCamera(Minecraft minecraft) {
    if (playerRenderCameraBefore == null) return;
    minecraft.options.setCameraType(playerRenderCameraBefore);
    playerRenderCameraBefore = null;
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

  /** The client must receive the explicit repaired state without an implicit restart or unseat. */
  private static boolean supportRepairReady(Minecraft minecraft) {
    if (!supportRepairRequired() || minecraft.level == null) return !supportRepairRequired();
    var player = minecraft.player;
    if (player == null || !player.isPassenger() || player.getVehicle() == null) return false;
    var binding =
        dev.fourthdimension.vehicle.Boarding.at(minecraft.level, player.getVehicle().blockPosition());
    if (binding == null
        || binding.vehicle().family != supportLossFamily()
        || !binding.vehicle().stopping
        || !binding.vehicle().restraints
        || !binding.vehicle().fault.isEmpty()
        || !binding.carrier().riderSeats.containsKey(player.getUUID())) return false;
    return EligibilityHooks.CLIENT.get(minecraft.level).rails.stream()
        .anyMatch(
            rail ->
                rail.matches(binding.vehicle().boundFrom, binding.vehicle().boundTo) && rail.valid);
  }

  private static boolean supportRepairRequired() {
    return supportLossRequired()
        && Boolean.getBoolean("fourthdimension.carrierValidationSupportRepair");
  }

  /** Requires the replicated stopped chassis bearing to lie on the exact repaired support. */
  private static boolean supportRealignmentReady(Minecraft minecraft) {
    if (!supportRealignmentRequired() || minecraft.level == null) return !supportRealignmentRequired();
    var player = minecraft.player;
    if (player == null || !player.isPassenger() || player.getVehicle() == null) return false;
    var binding =
        dev.fourthdimension.vehicle.Boarding.at(minecraft.level, player.getVehicle().blockPosition());
    if (binding == null
        || binding.vehicle().family != supportLossFamily()
        || !binding.vehicle().stopping
        || !binding.vehicle().restraints
        || !binding.vehicle().fault.isEmpty()
        || binding.vehicle().alignmentRequired
        || !binding.vehicle().boardingReady
        || !binding.carrier().riderSeats.containsKey(player.getUUID())) return false;
    var rail =
        EligibilityHooks.CLIENT.get(minecraft.level).rails.stream()
            .filter(
                candidate ->
                    candidate.matches(binding.vehicle().boundFrom, binding.vehicle().boundTo)
                        && candidate.valid)
            .findFirst()
            .orElse(null);
    if (rail == null) return false;
    var container = SubLevelContainer.getContainer(minecraft.level);
    var chassis = container == null ? null : container.getSubLevel(binding.vehicle().chassis);
    if (chassis == null) return false;
    var bearingPlot = CoasterCartTrackSnap.representativeBearingPlotPos(chassis);
    var edge = TrackGeometry.resolve(minecraft.level, rail);
    if (bearingPlot == null || edge == null) return false;
    var bearing = chassis.logicalPose().transformPosition(Vec3.atCenterOf(bearingPlot));
    var hit = CoasterPathTrackFrame.closestHitOnEdge(minecraft.level, edge, bearing);
    if (hit == null) return false;
    var expectedBearing =
        hit.point().add(CoasterPathTrackFrame.trackBasisAtHit(minecraft.level, hit).up().normalize().scale(.3125));
    return expectedBearing.distanceToSqr(bearing) < .04;
  }

  private static boolean supportRealignmentRequired() {
    return supportRepairRequired()
        && Boolean.getBoolean("fourthdimension.carrierValidationSupportRealignment");
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
