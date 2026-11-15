package dev.fourthdimension.vehicle;

import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.mixin.ManualSnapAccess;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import java.util.*;
import net.minecraft.server.level.*;
import org.joml.Vector3d;

public final class VehicleOperations {
  public static final String MATCHING_SUPPORT_LOST = "Matching track support lost";

  private VehicleOperations() {}

  /**
   * Enter the retained, fail-closed state used when an original functional section disappears.
   * Capture is intentional: an active-route loss must become serviceable after braking, rather
   * than leaving a rider in a stopped but permanently unalignable free carrier.
   */
  public static void failForLostSupport(ServerLevel level, FoundationData.Vehicle vehicle) {
    // A coupled PAX train is one safety unit: losing the matching support under any car must
    // capture, restrain, and brake its whole compatible consist before recovery can begin.
    var cars =
        vehicle.family == dev.fourthdimension.contract.Family.PAX
            ? consist(level, vehicle)
            : List.of(vehicle);
    for (var car : cars) {
      if (car.stopping
          && car.restraints
          && MATCHING_SUPPORT_LOST.equals(car.fault)
          && car.alignmentRequired
          && car.carriers.stream().allMatch(carrier -> carrier.capture)) continue;
      car.stopping = true;
      car.restraints = true;
      car.carriers.forEach(carrier -> carrier.capture = true);
      car.fault = MATCHING_SUPPORT_LOST;
      car.alignmentRequired = true;
      car.revision++;
    }
    // An active PAX lift owns the same coupled cars. Fault it immediately so its next physics
    // callback cannot continue a guide/catch motion after support recovery has become required.
    var winch = dev.fourthdimension.shuttle.ShuttleRuntime.owner(level, vehicle.chassis);
    if (winch != null && winch.phase != dev.fourthdimension.shuttle.ShuttleWinchEntity.Phase.BOARDING)
      winch.fail(MATCHING_SUPPORT_LOST);
  }

  public static FoundationData.Vehicle accessible(ServerPlayer player, UUID id) {
    var v = FoundationData.get(player.serverLevel()).vehicles.get(id);
    if (v == null || FoundationData.VARIANT.equals(v.variant))
      throw new IllegalArgumentException("Select an original 4D vehicle");
    var parent = SubLevelContainer.getContainer(player.level()).getSubLevel(id);
    var center = parent == null ? null : CoasterCartResolve.cartCenterWorld(parent);
    var operator = RecoveryOperations.operatorWorldPosition(player);
    if (center == null
        || center.distanceSquared(operator) > 12 * 12
        || !player
            .serverLevel()
            .mayInteract(
                player, net.minecraft.core.BlockPos.containing(center.x, center.y, center.z))
        || !player.mayBuild())
      throw new IllegalArgumentException("Vehicle is out of reach or cannot be edited");
    return v;
  }

  public static List<FoundationData.Vehicle> consist(
      ServerLevel level, FoundationData.Vehicle vehicle) {
    var container = SubLevelContainer.getContainer(level);
    var parent = container.getSubLevel(vehicle.chassis);
    if (parent == null) throw new IllegalArgumentException("Vehicle chassis is unavailable");
    var ids = CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(parent);
    var result = new ArrayList<FoundationData.Vehicle>();
    for (var id : ids) {
      var v = FoundationData.get(level).vehicles.get(id);
      if (v == null
          && id.equals(vehicle.recoveryCart)
          && RecoveryOperations.ordinary(level, container.getSubLevel(id))) continue;
      if (v == null || !v.variant.equals(vehicle.variant))
        throw new IllegalArgumentException("Incompatible or missing vehicle in consist");
      result.add(v);
    }
    if (result.isEmpty()) result.add(vehicle);
    return result;
  }

  public static void capture(ServerLevel level, FoundationData.Vehicle vehicle) {
    var winch = dev.fourthdimension.shuttle.ShuttleRuntime.owner(level, vehicle.chassis);
    if (winch != null
        && winch.phase != dev.fourthdimension.shuttle.ShuttleWinchEntity.Phase.BOARDING) {
      winch.fail("Operator emergency capture: inspect the shuttle before recovery");
      return;
    }
    if (SpinningVehicle.is(vehicle)) {
      var container = SubLevelContainer.getContainer(level);
      var parent = container.getSubLevel(vehicle.chassis);
      var rail = FoundationData.get(level).rails.stream().filter(r -> r.matches(vehicle.boundFrom, vehicle.boundTo)).findFirst().orElse(null);
      if (parent instanceof ServerSubLevel body && SpinningRuntime.safety(level, body, vehicle, rail) == null
          && !vehicle.alignmentRequired) vehicle.fault = "";
    }
    if (WingVehicle.is(vehicle)) {
      var container = SubLevelContainer.getContainer(level);
      for (var v : consist(level, vehicle)) {
        var body = container.getSubLevel(v.chassis);
        if (body instanceof ServerSubLevel parent && WingTrack.support(level, v) != null) {
          // Explicit Stop acknowledges a repaired ordinary route; it never dispatches.
          var reason = WingRuntime.safety(level, parent, v);
          if (reason == null) { v.fault = ""; v.alignmentRequired = false; }
        }
      }
    }
    for (var v : consist(level, vehicle)) {
      v.stopping = true;
      v.carriers.forEach(c -> c.capture = true);
      v.revision++;
    }
    FoundationData.get(level).setDirty();
  }

  public static void restraints(ServerLevel level, FoundationData.Vehicle vehicle, boolean closed) {
    if (!closed && vehicle.recoveryCart != null)
      throw new IllegalArgumentException("Disconnect recovery before opening restraints");
    if (!closed && dev.fourthdimension.shuttle.ShuttleRuntime.running(level, vehicle.chassis))
      throw new IllegalArgumentException(
          "Shuttle restraints remain closed until final station capture");
    var consist = consist(level, vehicle);
    for (var v : consist)
      if (!Boarding.ready(level, v))
        throw new IllegalArgumentException(
            "Stop and align every carrier before operating restraints");
    for (var v : consist) {
      v.restraints = closed;
      v.revision++;
    }
    FoundationData.get(level).setDirty();
  }

  /** Read-only preflight shared by diagnostics and every dispatch request. */
  public static void validateDispatch(ServerLevel level, FoundationData.Vehicle vehicle) {
    if (vehicle.recoveryCart != null)
      throw new IllegalArgumentException("Disconnect recovery before ride dispatch");
    var data = FoundationData.get(level);
    var consist = consist(level, vehicle);
    for (var v : consist) {
      if (v.alignmentRequired)
        throw new IllegalArgumentException(
            "Use Realign matching track after repair before dispatch");
      if (!v.restraints || !Boarding.ready(level, v))
        throw new IllegalArgumentException(
            "All carriers must be captured and restraints closed before dispatch");
      if (v.maintenance)
        throw new IllegalArgumentException("Release maintenance retention before dispatch");
      if (WingVehicle.is(v)) {
        if (WingTrack.support(level, v) == null || !v.fault.isEmpty())
          throw new IllegalArgumentException("Restore and inspect Wing's ordinary track before dispatch");
        continue;
      }
      var rail =
          data.rails.stream()
              .filter(r -> r.matches(v.boundFrom, v.boundTo))
              .findFirst()
              .orElse(null);
      if (rail == null
          || !rail.valid
          || !rail.variant.equals(v.variant)
          || TrackGeometry.resolve(level, rail) == null)
        throw new IllegalArgumentException("Vehicle has lost its matching track support");
      if (MATCHING_SUPPORT_LOST.equals(v.fault))
        throw new IllegalArgumentException(
            "Restore the original matching section, then use Repair matching track before dispatch");
    }
    if (vehicle.family == dev.fourthdimension.contract.Family.PAX) {
      var winch = dev.fourthdimension.shuttle.ShuttleRuntime.owner(level, vehicle.chassis);
      if (winch == null)
        throw new IllegalArgumentException("Bind a powered shuttle winch before PAX dispatch");
      winch.validateStart();
      return;
    }
  }

  public static void dispatch(ServerLevel level, FoundationData.Vehicle vehicle) {
    validateDispatch(level, vehicle);
    if (vehicle.family == dev.fourthdimension.contract.Family.PAX) {
      dev.fourthdimension.shuttle.ShuttleRuntime.owner(level, vehicle.chassis).start();
      return;
    }
    var data = FoundationData.get(level);
    var consist = consist(level, vehicle);
    for (var v : consist) {
      v.stopping = false;
      v.fault = "";
      v.carriers.forEach(c -> { c.capture = false; c.latched = false; });
      v.revision++;
    }
    data.setDirty();
  }

  /**
   * A deliberate repair step for a returned original section. It accepts only the same stored
   * geometry; changed geometry still requires the normal unoccupied profile-rebind workflow.
   * Repair never opens restraints or resumes the vehicle.
   */
  public static void repairMatchingSupport(ServerLevel level, FoundationData.Vehicle vehicle) {
    var data = FoundationData.get(level);
    var consist = consist(level, vehicle);
    for (var v : consist) {
      if (!MATCHING_SUPPORT_LOST.equals(v.fault))
        throw new IllegalArgumentException("This vehicle is not stopped for matching track support loss");
      // The lost section can leave the native chassis without a track constraint, so the normal
      // station-ready velocity check is unavailable until after this exact section is restored.
      // Captured carriers, stopped motion, and closed restraints are the repair interlock.
      if (!v.stopping || !v.restraints || v.carriers.stream().anyMatch(carrier -> !carrier.capture))
        throw new IllegalArgumentException(
            "Keep restraints closed and capture every carrier before repair");
      var rail =
          data.rails.stream()
              .filter(candidate -> candidate.matches(v.boundFrom, v.boundTo))
              .findFirst()
              .orElseThrow(
                  () -> new IllegalArgumentException("Restore the original matching track section first"));
      if (!rail.variant.equals(v.variant) || rail.family != v.family)
        throw new IllegalArgumentException("Restored section does not match this 4D vehicle family");
      var edge = TrackGeometry.resolve(level, rail);
      if (edge == null)
        throw new IllegalArgumentException("Restore the original matching track section first");
      if (TrackGeometry.fingerprint(level, edge) != rail.geometryHash)
        throw new IllegalArgumentException(
            "Restored geometry changed; unload riders and explicitly rebind the profile before operating");
    }
    var shuttleRoute = repairedShuttleRoute(level, vehicle);
    var repaired = new HashSet<FoundationData.Rail>();
    for (var v : consist) {
      var rail =
          data.rails.stream().filter(candidate -> candidate.matches(v.boundFrom, v.boundTo)).findFirst().orElseThrow();
      if (repaired.add(rail) && !rail.valid) {
        rail.valid = true;
        rail.revision++;
      }
      v.fault = "";
      v.revision++;
    }
    for (var rail : shuttleRoute)
      if (!rail.valid) {
        rail.valid = true;
        rail.revision++;
      }
    TrackGeometry.invalidate(level);
    data.setDirty();
  }

  /**
   * An active shuttle's route is one operating unit. A single removed curve can make the
   * upstream graph temporarily report every route leg as stale; restore only legs whose stored
   * geometry still matches, then let the winch perform its normal captured recovery.
   */
  private static List<FoundationData.Rail> repairedShuttleRoute(
      ServerLevel level, FoundationData.Vehicle vehicle) {
    if (vehicle.family != dev.fourthdimension.contract.Family.PAX) return List.of();
    var winch = dev.fourthdimension.shuttle.ShuttleRuntime.owner(level, vehicle.chassis);
    if (winch == null || winch.routeStart == null || winch.routeEnd == null) return List.of();
    var data = FoundationData.get(level);
    var nodes = new ArrayDeque<net.minecraft.core.BlockPos>();
    var seenNodes = new HashSet<net.minecraft.core.BlockPos>();
    var seenRails = new LinkedHashSet<FoundationData.Rail>();
    nodes.add(winch.routeStart);
    while (!nodes.isEmpty()) {
      var node = nodes.remove();
      if (!seenNodes.add(node)) continue;
      var legs =
          data.rails.stream().filter(rail -> rail.from.equals(node) || rail.to.equals(node)).toList();
      int expected = node.equals(winch.routeStart) || node.equals(winch.routeEnd) ? 1 : 2;
      if (legs.size() != expected)
        throw new IllegalArgumentException("Shuttle route must have exactly two open terminals and no branches");
      for (var rail : legs) {
        if (rail.family != dev.fourthdimension.contract.Family.PAX
            || !rail.variant.equals(dev.fourthdimension.track.TrackDesign.PAX.variant))
          throw new IllegalArgumentException("Every shuttle segment requires valid matching PAX track");
        var edge = TrackGeometry.resolve(level, rail);
        if (edge == null || TrackGeometry.fingerprint(level, edge) != rail.geometryHash)
          throw new IllegalArgumentException(
              "Restored geometry changed; unload riders and explicitly rebind the profile before operating");
        seenRails.add(rail);
        nodes.add(rail.from.equals(node) ? rail.to : rail.from);
      }
    }
    if (!seenNodes.contains(winch.routeEnd) || seenRails.size() < 3)
      throw new IllegalArgumentException("Shuttle needs a station and two spikes");
    return List.copyOf(seenRails);
  }

  /**
   * Re-engages the original native chassis with its repaired support without releasing riders or
   * restraints. The base coaster snap operation rebuilds the native running contact only against
   * the exact repaired section already verified by the preceding repair action.
   */
  public static void realignMatchingSupport(ServerLevel level, FoundationData.Vehicle vehicle) {
    var data = FoundationData.get(level);
    var container = SubLevelContainer.getContainer(level);
    if (container == null) throw new IllegalArgumentException("Native vehicle physics are unavailable");
    var consist = consist(level, vehicle);
    var winch = dev.fourthdimension.shuttle.ShuttleRuntime.owner(level, vehicle.chassis);
    if (winch != null
        && !new HashSet<>(winch.cars).equals(
            consist.stream().map(car -> car.chassis).collect(java.util.stream.Collectors.toSet())))
      throw new IllegalArgumentException("Shuttle consist changed; recover and rebind before realignment");
    // A station-adjacent shuttle is captured at known station slots, including when a lift
    // faulted for missing support. Snapping each car to its independent closest point after an
    // unsupported hold can compress the six-block native couplers until they break. Re-engage
    // the whole captured train at its bound station spacing instead.
    var stationRoute =
        winch != null
                && (winch.phase == dev.fourthdimension.shuttle.ShuttleWinchEntity.Phase.BOARDING
                    || (winch.phase == dev.fourthdimension.shuttle.ShuttleWinchEntity.Phase.FAULT
                        && MATCHING_SUPPORT_LOST.equals(winch.fault)))
                && winch.routeStart != null
                && winch.routeEnd != null
            ? dev.fourthdimension.shuttle.ShuttleRoute.resolve(
                level, winch.routeStart, winch.routeEnd)
            : null;
    record Candidate(
        FoundationData.Vehicle vehicle,
        ServerSubLevel chassis,
        dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit hit) {}
    var candidates = new ArrayList<Candidate>();
    for (var v : consist) {
      if (!v.alignmentRequired)
        throw new IllegalArgumentException("This vehicle does not require matching-track realignment");
      if (!v.stopping || !v.restraints || v.carriers.stream().anyMatch(carrier -> !carrier.capture))
        throw new IllegalArgumentException(
            "Keep restraints closed and capture every carrier before realignment");
      // Older saves can contain the boundary fault that used to overwrite a successful support
      // repair while alignmentRequired remained true. The exact restored rail checks below still
      // apply, so permit that one stranded state to complete its required native re-engagement.
      if (!v.fault.isEmpty() && !BoundaryGuard.END_OF_COMPATIBLE_TRACK.equals(v.fault))
        throw new IllegalArgumentException("Repair matching track before realignment");
      var rail =
          data.rails.stream()
              .filter(candidate -> candidate.matches(v.boundFrom, v.boundTo))
              .findFirst()
              .orElseThrow(
                  () -> new IllegalArgumentException("Restore the original matching track section first"));
      if (!rail.valid || !rail.variant.equals(v.variant) || rail.family != v.family)
        throw new IllegalArgumentException("Restored section does not match this 4D vehicle family");
      var edge = TrackGeometry.resolve(level, rail);
      if (edge == null)
        throw new IllegalArgumentException("Restore the original matching track section first");
      if (TrackGeometry.fingerprint(level, edge) != rail.geometryHash)
        throw new IllegalArgumentException(
            "Restored geometry changed; unload riders and explicitly rebind the profile before operating");
      if (!(container.getSubLevel(v.chassis) instanceof ServerSubLevel chassis) || chassis.isRemoved())
        throw new IllegalArgumentException("Vehicle chassis is unavailable");
      var bearingPlot = CoasterCartTrackSnap.representativeBearingPlotPos(chassis);
      if (bearingPlot == null) throw new IllegalArgumentException("Vehicle bearing is unavailable");
      var bearing =
          chassis
              .logicalPose()
              .transformPosition(net.minecraft.world.phys.Vec3.atCenterOf(bearingPlot));
      var hit = CoasterPathTrackFrame.closestHitOnEdge(level, edge, bearing);
      if (hit == null) throw new IllegalArgumentException("Matching track has no usable snap point");
      if (stationRoute != null) {
        int stationIndex = winch.cars.indexOf(v.chassis);
        if (stationIndex < 0)
          throw new IllegalArgumentException("Shuttle car is missing from its bound station");
        var stationHit = stationRoute.sample(winch.stationDistance + 6 * stationIndex).hit();
        if (!rail.matches(stationHit.edge().from(), stationHit.edge().to()))
          throw new IllegalArgumentException("Shuttle station slot is outside the restored section");
        if (bearing.distanceTo(stationHit.point()) > 8)
          throw new IllegalArgumentException(
              "Shuttle car is too far from its station slot for physical realignment");
        hit = stationHit;
      }
      if (!EligibilityHooks.allows(chassis, level, hit))
        throw new IllegalArgumentException("Vehicle and matching track cannot re-engage");
      candidates.add(new Candidate(v, chassis, hit));
    }
    // A captured unsupported body can pick up gravity/solver residue between periodic brake
    // callbacks. The normal explicit action closes that gap: after every candidate has passed the
    // exact-rail, geometry, restraint and native-bearing checks, atomically stop the whole
    // validated consist and verify the chassis before applying the native snap in this same tick.
    var pendingBodies = new LinkedHashSet<UUID>();
    for (var candidate : candidates) {
      pendingBodies.add(candidate.vehicle.chassis);
      candidate.vehicle.carriers.forEach(carrier -> pendingBodies.add(carrier.body));
    }
    var pipeline = container.physicsSystem().getPipeline();
    var bodies = new ArrayList<ServerSubLevel>();
    for (var id : pendingBodies) {
      if (!(container.getSubLevel(id) instanceof ServerSubLevel body) || body.isRemoved())
        throw new IllegalArgumentException("Vehicle carrier is unavailable for realignment");
      bodies.add(body);
    }
    if (winch != null) dev.fourthdimension.shuttle.ShuttleRuntime.releaseSupportHolds(winch);
    for (var body : bodies) pipeline.resetVelocity(body);
    for (var candidate : candidates) {
      if (pipeline.getLinearVelocity(candidate.chassis, new Vector3d()).lengthSquared() > 1.0e-8)
        throw new IllegalArgumentException(
            "Unable to stop every linked chassis before realignment");
    }
    for (var candidate : candidates) {
      var plotBearing = CoasterCartTrackSnap.representativeBearingPlotPos(candidate.chassis);
      if (plotBearing == null) throw new IllegalArgumentException("Vehicle bearing is unavailable");
      var localBearing = net.minecraft.world.phys.Vec3.atCenterOf(plotBearing);
      var worldBearing = candidate.chassis.logicalPose().transformPosition(localBearing);
      var negate =
          ManualSnapAccess.fourthdimension$negatesEdgeTangent(
              candidate.chassis, level, candidate.hit);
      CoasterCartTrackSnap.applyInitialSnapAfterSpawn(candidate.chassis, level, candidate.hit);
      ManualSnapAccess.fourthdimension$physicalSnap(
          candidate.chassis,
          level,
          container.physicsSystem(),
          candidate.hit,
          worldBearing,
          localBearing,
          negate);
    }
    for (var candidate : candidates) {
      candidate.vehicle.alignmentRequired = false;
      if (BoundaryGuard.END_OF_COMPATIBLE_TRACK.equals(candidate.vehicle.fault))
        candidate.vehicle.fault = "";
      candidate.vehicle.revision++;
    }
    data.setDirty();
  }

  public static void tune(
      ServerLevel level, FoundationData.Vehicle vehicle, double damping, double ballast) {
    if (!vehicle.family.foundationFixture() || vehicle.family.driven()
        || !Boarding.ready(level, vehicle)
        || vehicle.carriers.stream().anyMatch(c -> !c.occupants.isEmpty()))
      throw new IllegalArgumentException("Tune an empty, stopped, captured free-spin vehicle");
    if (!Double.isFinite(damping)
        || damping < 0
        || damping > 250
        || !Double.isFinite(ballast)
        || ballast < 0
        || ballast > 240)
      throw new IllegalArgumentException("Damping must be 0..250 and ballast 0..240 per carrier");
    vehicle.damping = damping;
    vehicle.ballast = ballast;
    vehicle.carriers.forEach(c -> c.ballast = 0);
    vehicle.revision++;
    FoundationData.get(level).setDirty();
  }

  public static void retention(
      ServerLevel level, FoundationData.Vehicle vehicle, boolean retained) {
    var cars = consist(level, vehicle);
    if (!retained && cars.stream().anyMatch(v -> v.recoveryCart != null))
      throw new IllegalArgumentException("Disconnect recovery before releasing retention");
    if (!retained && cars.stream().anyMatch(v -> !Boarding.ready(level, v)))
      throw new IllegalArgumentException("Capture every vehicle before releasing retention");
    if (retained) capture(level, vehicle);
    for (var v : cars) {
      v.maintenance = retained;
      v.revision++;
    }
    FoundationData.get(level).setDirty();
  }

  /** Retire an evacuated, isolated PAX car after handing it off from the shuttle winch. */
  public static void retireDetachedPax(ServerLevel level, FoundationData.Vehicle vehicle) {
    if (vehicle.family != dev.fourthdimension.contract.Family.PAX
        || FoundationData.VARIANT.equals(vehicle.variant))
      throw new IllegalArgumentException("Select an original PAX car");
    if (dev.fourthdimension.shuttle.ShuttleRuntime.owner(level, vehicle.chassis) != null)
      throw new IllegalArgumentException("Detach the shuttle winch before retiring this car");
    if (!vehicle.shuttleDetached)
      throw new IllegalArgumentException("Hand this car over from its shuttle winch before retirement");
    if (vehicle.recoveryCart != null)
      throw new IllegalArgumentException("Disconnect recovery before retiring this car");
    if (!vehicle.maintenance || !vehicle.stopping || !Boarding.ready(level, vehicle))
      throw new IllegalArgumentException("Retain and capture the stopped car before retiring it");
    if (vehicle.carriers.stream()
        .anyMatch(c -> !c.occupants.isEmpty() || !c.riderSeats.isEmpty()))
      throw new IllegalArgumentException("Unload every rider before retiring this car");
    var container = SubLevelContainer.getContainer(level);
    var chassis = container == null ? null : container.getSubLevel(vehicle.chassis);
    if (chassis == null || chassis.isRemoved())
      throw new IllegalArgumentException("Vehicle chassis is unavailable");
    if (CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(chassis).size() != 1)
      throw new IllegalArgumentException("Unlink this car before retiring it");
    container.removeSubLevel(chassis, SubLevelRemovalReason.REMOVED);
  }

  public static void loadingPreset(ServerLevel level, FoundationData.Vehicle vehicle, int preset) {
    if (vehicle.family != dev.fourthdimension.contract.Family.FREE_SPIN || preset < 0 || preset > 2)
      throw new IllegalArgumentException("Select a Free Spin loading preset");
    tune(level, vehicle, vehicle.damping, 0);
    for (int i = 0; i < vehicle.carriers.size(); i++)
      vehicle.carriers.get(i).ballast = preset == 1 || (preset == 2 && i == 0) ? 160 : 0;
    FoundationData.get(level).setDirty();
  }

  public static void brake(ServerLevel level, FoundationData.Vehicle vehicle, double dt) {
    var container = SubLevelContainer.getContainer(level);
    var pipeline = container.physicsSystem().getPipeline();
    // Support loss and a repaired section awaiting explicit realignment are both hard emergency
    // stops. For a coupled PAX train, braking only the detected lead leaves linked chassis feeding
    // momentum back through couplers, so freeze the compatible consist until native reattachment.
    boolean hardStop = MATCHING_SUPPORT_LOST.equals(vehicle.fault) || vehicle.alignmentRequired;
    var cars =
        vehicle.family == dev.fourthdimension.contract.Family.PAX && hardStop
            ? consist(level, vehicle)
            : List.of(vehicle);
    var bodies = new LinkedHashSet<UUID>();
    for (var car : cars) {
      bodies.add(car.chassis);
      car.carriers.forEach(carrier -> bodies.add(carrier.body));
    }
    for (var id : bodies) {
      if (!(container.getSubLevel(id) instanceof ServerSubLevel body)) continue;
      if (hardStop) {
        // Sable exposes an atomic native reset; counter-impulses leave angular/coupler solver
        // residue that can pull a stopped PAX consist apart during a later track snap.
        pipeline.resetVelocity(body);
        continue;
      }
      var velocity = pipeline.getLinearVelocity(body, new Vector3d());
      double speed = velocity.length();
      if (speed == 0) continue;
      pipeline.addLinearAndAngularVelocity(
          body, velocity.mul(-Math.min(1, 6 * dt / speed)), new Vector3d());
    }
  }
}
