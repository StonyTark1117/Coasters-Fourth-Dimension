package dev.fourthdimension.shuttle;

import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.physics.constraint.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.joml.Quaterniond;
import org.joml.Vector3d;

/**
 * Native carriage guide and mechanical catch. Released trains receive only gravity and rail
 * guidance.
 */
public final class ShuttleRuntime {
  private ShuttleRuntime() {}

  private static final Map<ServerLevel, Map<BlockPos, ShuttleWinchEntity>> WINCHES =
      new WeakHashMap<>();
  private static final Map<ShuttleWinchEntity, Handles> HANDLES = new WeakHashMap<>();
  private static final Map<ShuttleWinchEntity, CachedRoute> ROUTES = new WeakHashMap<>();

  private static final class Handles {
    GenericConstraintHandle guide, catchJoint;
    final Map<UUID, GenericConstraintHandle> supportHolds = new HashMap<>();
    Vector3d worldAnchor;
    Quaterniond worldOrientation;
    UUID body;
  }

  private record Chunk(int x, int z) {}

  private record CachedRoute(
      ShuttleRoute route,
      Map<UUID, Long> revisions,
      Set<Chunk> requiredChunks,
      int railCount,
      Map<BlockPos, Integer> nodeDegrees) {
    boolean current(ServerLevel level, ShuttleWinchEntity winch) {
      if (!route.start.equals(winch.routeStart) || !route.end.equals(winch.routeEnd)) return false;
      var rails = FoundationData.get(level).rails;
      if (rails.size() != railCount) return false;
      for (var expected : nodeDegrees.entrySet()) {
        int actual = 0;
        for (var rail : rails)
          if (rail.from().equals(expected.getKey()) || rail.to().equals(expected.getKey())) actual++;
        if (actual != expected.getValue()) return false;
      }
      for (var segment : route.segments)
        if (!segment.rail().valid
            || revisions.getOrDefault(segment.rail().id, Long.MIN_VALUE) != segment.rail().revision)
          return false;
      for (var chunk : requiredChunks)
        if (!dev.ryanhcode.sable.sublevel.system.ticket.PhysicsChunkTicketManager
            .isChunkLoadedEnough(level, chunk.x, chunk.z)) return false;
      return true;
    }
  }

  public static void register(ServerLevel level, ShuttleWinchEntity winch) {
    var previous =
        WINCHES.computeIfAbsent(level, k -> new HashMap<>()).put(winch.getBlockPos(), winch);
    if (previous != null && previous != winch) {
      var handles = HANDLES.remove(previous);
      if (handles != null) {
        remove(handles.guide);
        remove(handles.catchJoint);
        removeSupportHolds(handles);
      }
    }
  }

  public static Collection<ShuttleWinchEntity> winches(ServerLevel level) {
    return WINCHES.getOrDefault(level, Map.of()).values();
  }

  public static ShuttleWinchEntity owner(ServerLevel level, UUID car) {
    if (car == null) return null;
    return winches(level).stream().filter(w -> w.cars.contains(car)).findFirst().orElse(null);
  }

  public static boolean running(ServerLevel level, UUID car) {
    var w = owner(level, car);
    return w != null && w.phase != ShuttleWinchEntity.Phase.BOARDING;
  }

  /** An explicit stationary handover; never pulls against the powered physical catch. */
  public static void detachForRecovery(
      net.minecraft.server.level.ServerPlayer player, FoundationData.Vehicle selected) {
    var level = player.serverLevel();
    var winch = owner(level, selected.chassis);
    if (winch == null)
      throw new IllegalArgumentException("This car has no shuttle winch assignment");
    var operator = RecoveryOperations.operatorWorldPosition(player);
    if (!level.hasChunkAt(winch.getBlockPos())
        || level.getBlockEntity(winch.getBlockPos()) != winch
        || operator.distanceSquared(
                new Vector3d(
                    winch.getBlockPos().getX() + .5,
                    winch.getBlockPos().getY() + .5,
                    winch.getBlockPos().getZ() + .5))
            > 12 * 12
        || !level.mayInteract(player, winch.getBlockPos())
        || !player.mayBuild()
        || winch.phase != ShuttleWinchEntity.Phase.BOARDING
            && winch.phase != ShuttleWinchEntity.Phase.FAULT)
      throw new IllegalArgumentException(
          "Stop the shuttle and inspect its nearby loaded winch before detaching");
    var data = FoundationData.get(level);
    var cars = winch.cars.stream().map(data.vehicles::get).toList();
    if (cars.isEmpty()
        || cars.stream()
            .anyMatch(
                v ->
                    v == null
                        || v.recoveryCart != null
                        || !RecoveryOperations.retained(v)
                        || !Boarding.ready(level, v)))
      throw new IllegalArgumentException(
          "Capture, retain and close restraints on the complete stopped PAX train");
    var container = SubLevelContainer.getContainer(level);
    var carriage = winch.carriage == null ? null : container.getSubLevel(winch.carriage);
    if (winch.carriage != null && carriage == null)
      throw new IllegalArgumentException(
          "Restore the assigned native carriage before handing over recovery");
    var handles = HANDLES.remove(winch);
    if (handles != null) {
      remove(handles.catchJoint);
      remove(handles.guide);
      removeSupportHolds(handles);
    }
    cars.forEach(
        v -> {
          v.stopping = true;
          v.shuttleDetached = true;
          v.revision++;
        });
    winch.vehicle = null;
    winch.cars = List.of();
    winch.carriage = null;
    winch.routeStart = winch.routeEnd = null;
    winch.phase = winch.interrupted = ShuttleWinchEntity.Phase.BOARDING;
    winch.fault = "";
    winch.changed();
    if (carriage != null)
      container.removeSubLevel(
          carriage, dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason.REMOVED);
    data.setDirty();
  }

  /** Permanent native destruction is a fault; normal unloading must never call this. */
  public static void onBodyRemoved(ServerLevel level, UUID body) {
    var data = FoundationData.get(level);
    for (var winch : List.copyOf(winches(level))) {
      boolean assigned =
          body.equals(winch.carriage)
              || winch.cars.contains(body)
              || winch.cars.stream()
                  .map(data.vehicles::get)
                  .filter(Objects::nonNull)
                  .anyMatch(v -> v.carriers.stream().anyMatch(c -> c.body.equals(body)));
      if (assigned) {
        winch.fail(
            "Native shuttle component removed: retain and inspect every car before recovery");
        data.setDirty();
      }
    }
  }

  public static void releaseBrakes(ServerLevel level, ShuttleWinchEntity winch, boolean capture) {
    for (var id : winch.cars) {
      var v = FoundationData.get(level).vehicles.get(id);
      if (v == null) continue;
      v.stopping = false;
      v.restraints = true;
      v.fault = "";
      v.carriers.forEach(c -> c.capture = capture);
      v.revision++;
    }
    FoundationData.get(level).setDirty();
  }

  public static void tick(ServerLevel level, double dt) {
    for (var winch : List.copyOf(winches(level))) {
      if (winch.vehicle == null) continue;
      // read() turns an in-motion winch into FAULT, but loading happens before this runtime can
      // access FoundationData. Retain all persisted cars now, before any FAULT inspection/catch.
      if (winch.consumeLoadedActiveTrainRetention()) winch.fail(winch.fault);
      ShuttleDiagnostics.observe(level, winch);
      try {
        if (level.hasChunkAt(winch.getBlockPos())
            && !level.getBlockState(winch.getBlockPos()).is(ShuttleContent.WINCH.get())) {
          winch.fail("Shuttle winch removed: retain and inspect every car before recovery");
          var handles = HANDLES.remove(winch);
          if (handles != null) {
            remove(handles.guide);
            remove(handles.catchJoint);
            removeSupportHolds(handles);
          }
          var container = SubLevelContainer.getContainer(level);
          var body = winch.carriage == null ? null : container.getSubLevel(winch.carriage);
          if (body != null)
            container.removeSubLevel(
                body, dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason.REMOVED);
          WINCHES.get(level).remove(winch.getBlockPos(), winch);
          continue;
        }
        // Boarding is stationary, including a train already caught by its carriage.
        // Native bodies can restore after the winch or wait in Sable holding.
        // Rebuild an existing catch only when both bodies are available; start()
        // still validates the complete train, carriage and route before moving.
        if (winch.phase == ShuttleWinchEntity.Phase.BOARDING) {
          // A captured train still needs a physical station hold. A trailing native cart can
          // otherwise lose its snap while the lead remains caught, then fall out of the loaded
          // world before the operator starts the winch. The holds are released on the next
          // non-BOARDING tick, before the powered lift advances.
          var container = SubLevelContainer.getContainer(level);
          if (winch.carriage != null
              && container.getSubLevel(winch.carriage) instanceof ServerSubLevel
              && container.getSubLevel(winch.vehicle) instanceof ServerSubLevel lead) {
            var held = HANDLES.computeIfAbsent(winch, k -> new Handles());
            ensureCarriage(level, winch, null, held);
            catchTrain(level, lead, winch, held);
          }
          var data = FoundationData.get(level);
          if (winch.cars.stream()
              .map(data.vehicles::get)
              .allMatch(car -> car != null && Boarding.ready(level, car)))
            holdUnsupportedTrain(level, winch);
          else releaseSupportHolds(winch);
          continue;
        }
        if (!level.hasChunkAt(winch.getBlockPos())
            || !dev.ryanhcode.sable.sublevel.system.ticket.PhysicsChunkTicketManager
                .isChunkLoadedEnough(
                    level, winch.getBlockPos().getX() >> 4, winch.getBlockPos().getZ() >> 4)
            || level.getBlockEntity(winch.getBlockPos()) != winch)
          throw new IllegalArgumentException("Shuttle winch chunk unavailable or winch removed");
        if (winch.phase == ShuttleWinchEntity.Phase.FAULT) {
          // Native constraints are transient. Rebuild the stationary catch after a reload,
          // without advancing the persisted carriage coordinate or resuming the lift.
          var container = SubLevelContainer.getContainer(level);
          if (winch.carriage != null
              && container.getSubLevel(winch.carriage) != null
              && container.getSubLevel(winch.vehicle) instanceof ServerSubLevel lead
              && (winch.interrupted == ShuttleWinchEntity.Phase.LIFTING
                  || winch.interrupted == ShuttleWinchEntity.Phase.CAPTURING
                  || winch.interrupted == ShuttleWinchEntity.Phase.BOARDING)) {
            var held = HANDLES.computeIfAbsent(winch, k -> new Handles());
            ensureCarriage(level, winch, null, held);
            catchTrain(level, lead, winch, held);
          }
          if (VehicleOperations.MATCHING_SUPPORT_LOST.equals(winch.fault))
            holdUnsupportedTrain(level, winch);
          else releaseSupportHolds(winch);
          continue;
        }
        releaseSupportHolds(winch);
        var route = route(level, winch);
        if (winch.phase != ShuttleWinchEntity.Phase.BOARDING) {
          winch.validateTrain(false);
          if (Math.abs(winch.getSpeed()) < 16)
            throw new IllegalArgumentException(
                "Winch power lost: retain the train; inspect and resume or recover");
        }
        var container = SubLevelContainer.getContainer(level);
        var lead = (ServerSubLevel) container.getSubLevel(winch.vehicle);
        if (lead == null) throw new IllegalArgumentException("Shuttle lead car unavailable");
        var location = route.locate(lead);
        var velocity =
            container.physicsSystem().getPipeline().getLinearVelocity(lead, new Vector3d());
        double speed =
            velocity.dot(location.tangent().x, location.tangent().y, location.tangent().z);
        var handles = HANDLES.computeIfAbsent(winch, k -> new Handles());
        if (winch.phase == ShuttleWinchEntity.Phase.LIFTING) {
          ensureCarriage(level, winch, route, handles);
          catchTrain(level, lead, winch, handles);
          double rate = Math.min(128, Math.abs(winch.getSpeed())) / 32.0;
          // Ease to a stationary release rather than imparting launch velocity.
          double remaining = winch.carriageDistance - 3;
          double step = Math.min(rate, Math.sqrt(Math.max(0, remaining) * 2)) * dt;
          winch.carriageDistance = Math.max(3, winch.carriageDistance - step);
          positionGuide(level, winch, route, handles);
          if (winch.carriageDistance <= 3.001
              && Math.abs(location.distance() - 3) < .2
              && Math.abs(speed) < .1) {
            remove(handles.catchJoint);
            handles.catchJoint = null;
            releaseBrakes(level, winch, false);
            winch.phase = ShuttleWinchEntity.Phase.OUTBOUND;
            winch.changed();
          }
        } else if (winch.phase == ShuttleWinchEntity.Phase.OUTBOUND
            || winch.phase == ShuttleWinchEntity.Phase.RETURNING) {
          remove(handles.catchJoint);
          handles.catchJoint = null;
          ensureCarriage(level, winch, route, handles);
          winch.carriageDistance = Math.min(winch.stationDistance, winch.carriageDistance + 2 * dt);
          positionGuide(level, winch, route, handles);
          winch.maximumDistance = Math.max(winch.maximumDistance, location.distance());
          for (var id : winch.cars) {
            var car = container.getSubLevel(id);
            double coordinate = route.locate(car).distance();
            if (coordinate < 1 || coordinate > route.length - 2)
              throw new IllegalArgumentException(
                  "Guarded spike limit reached: inspect energy and recover the train");
          }
          if (winch.phase == ShuttleWinchEntity.Phase.OUTBOUND
              && speed < -.2
              && winch.maximumDistance > winch.stationDistance + 25) {
            winch.phase = ShuttleWinchEntity.Phase.RETURNING;
            winch.changed();
          }
          if (winch.phase == ShuttleWinchEntity.Phase.RETURNING
              && location.distance() <= winch.stationDistance + speed * speed / 12 + 1) {
            winch.phase = ShuttleWinchEntity.Phase.CAPTURING;
            for (var id : winch.cars) {
              var v = FoundationData.get(level).vehicles.get(id);
              v.stopping = true;
              v.carriers.forEach(c -> c.capture = true);
            }
            winch.changed();
          }
        } else if (winch.phase == ShuttleWinchEntity.Phase.CAPTURING) {
          ensureCarriage(level, winch, route, handles);
          if (handles.catchJoint == null || !handles.catchJoint.isValid()) {
            if (Math.abs(speed) > .08) continue;
            winch.carriageDistance = location.distance();
            positionGuide(level, winch, route, handles);
            // Move the empty carriage before engaging a physical catch; no long-range snapping.
            var body = container.getSubLevel(winch.carriage);
            var target = handles.worldAnchor;
            if (body.logicalPose().position().distance(target) > .1) continue;
            catchTrain(level, lead, winch, handles);
            releaseBrakes(level, winch, true);
          }
          double delta = winch.stationDistance - winch.carriageDistance;
          double returnSpeed = Math.min(2, Math.sqrt(Math.abs(delta) * 2));
          winch.carriageDistance +=
              Math.copySign(Math.min(Math.abs(delta), returnSpeed * dt), delta);
          positionGuide(level, winch, route, handles);
          if (Math.abs(delta) < .02
              && Math.abs(location.distance() - winch.stationDistance) < .15) {
            for (var id : winch.cars) FoundationData.get(level).vehicles.get(id).stopping = true;
            if (winch.cars.stream()
                .allMatch(
                    id -> Boarding.ready(level, FoundationData.get(level).vehicles.get(id)))) {
              winch.phase = ShuttleWinchEntity.Phase.BOARDING;
              winch.cycles++;
              winch.changed();
            }
          }
        } else if (winch.carriage != null) {
          ensureCarriage(level, winch, route, handles);
          catchTrain(level, lead, winch, handles);
          positionGuide(level, winch, route, handles);
        }
        winch.setChanged();
      } catch (IllegalArgumentException failure) {
        if (winch.phase != ShuttleWinchEntity.Phase.FAULT
            || !winch.fault.equals(failure.getMessage())) winch.fail(failure.getMessage());
      }
    }
  }

  /**
   * Route resolution fingerprints every segment. Rail mutations already increment the persisted
   * revision, so retain that expensive verification while avoiding a complete rebuild for each
   * physics substep of an unchanged shuttle.
   */
  private static ShuttleRoute route(ServerLevel level, ShuttleWinchEntity winch) {
    var cached = ROUTES.get(winch);
    if (cached != null && cached.current(level, winch)) return cached.route;
    var route = ShuttleRoute.resolve(level, winch.routeStart, winch.routeEnd);
    var revisions = new HashMap<UUID, Long>();
    var requiredChunks = new HashSet<Chunk>();
    var nodeDegrees = new HashMap<BlockPos, Integer>();
    for (var segment : route.segments) {
      revisions.put(segment.rail().id, segment.rail().revision);
      nodeDegrees.compute(segment.rail().from(), (node, count) -> count == null ? 1 : count + 1);
      nodeDegrees.compute(segment.rail().to(), (node, count) -> count == null ? 1 : count + 1);
      requiredChunks.add(new Chunk(segment.rail().from().getX() >> 4, segment.rail().from().getZ() >> 4));
      requiredChunks.add(new Chunk(segment.rail().to().getX() >> 4, segment.rail().to().getZ() >> 4));
      for (double distance = 0; distance <= segment.length(); distance += 8) {
        double t =
            dev.fourthdimension.track.TrackGeometry
                .table(level, segment.edge())
                .parameterAt(distance);
        var point =
            dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager.sampleEdgePosition(
                level, segment.edge(), t);
        requiredChunks.add(
            new Chunk(
                net.minecraft.util.Mth.floor(point.x) >> 4,
                net.minecraft.util.Mth.floor(point.z) >> 4));
      }
    }
    ROUTES.put(
        winch,
        new CachedRoute(
            route,
            Map.copyOf(revisions),
            Set.copyOf(requiredChunks),
            FoundationData.get(level).rails.size(),
            Map.copyOf(nodeDegrees)));
    return route;
  }

  private static void ensureCarriage(
      ServerLevel level, ShuttleWinchEntity winch, ShuttleRoute route, Handles handles) {
    var container = SubLevelContainer.getContainer(level);
    var pipeline = container.physicsSystem().getPipeline();
    ServerSubLevel body;
    if (winch.carriage == null) {
      var sample = route.sample(winch.carriageDistance);
      var basis = CoasterPathTrackFrame.trackBasisAtHit(level, sample.hit());
      var point =
          CoasterPathTrackFrame.pathAnchorForSnappedBlock(level, sample.hit())
              .add(basis.up().scale(3));
      var pose = new Pose3d();
      pose.position().set(point.x, point.y, point.z);
      pose.orientation().set(CoasterPathTrackFrame.quaternionFromTrackBasis(basis));
      body = (ServerSubLevel) container.allocateNewSubLevel(pose);
      var plot = body.getPlot();
      plot.newEmptyChunk(plot.getCenterChunk());
      plot.getEmbeddedLevelAccessor()
          .setBlock(BlockPos.ZERO, ShuttleContent.CARRIAGE.get().defaultBlockState(), 3);
      var center = plot.getCenterBlock();
      body.logicalPose()
          .rotationPoint()
          .set(center.getX() + .5, center.getY() + .5, center.getZ() + .5);
      body.logicalPose().position().set(point.x, point.y, point.z);
      body.updateLastPose();
      pipeline.onStatsChanged(body);
      pipeline.teleport(body, body.logicalPose().position(), body.logicalPose().orientation());
      winch.carriage = body.getUniqueId();
      winch.changed();
    } else {
      body = (ServerSubLevel) container.getSubLevel(winch.carriage);
      if (body == null || body.isRemoved())
        throw new IllegalArgumentException("Native lift carriage unavailable; recover the shuttle");
    }
    if (handles.guide == null
        || !handles.guide.isValid()
        || !body.getUniqueId().equals(handles.body)) {
      remove(handles.guide);
      remove(handles.catchJoint);
      handles.catchJoint = null;
      handles.body = body.getUniqueId();
      var center = body.getPlot().getCenterBlock();
      handles.worldAnchor = new Vector3d(body.logicalPose().position());
      handles.worldOrientation = new Quaterniond(body.logicalPose().orientation());
      handles.guide =
          pipeline.addConstraint(
              null,
              body,
              new GenericConstraintConfiguration(
                  handles.worldAnchor,
                      new Vector3d(center.getX() + .5, center.getY() + .5, center.getZ() + .5),
                  handles.worldOrientation, new Quaterniond()));
      for (var axis : ConstraintJointAxis.ALL)
        handles.guide.setMotor(axis, 0, 30000, 4000, true, 80000);
    }
  }

  private static void positionGuide(
      ServerLevel level, ShuttleWinchEntity winch, ShuttleRoute route, Handles handles) {
    var sample = route.sample(winch.carriageDistance);
    var basis = CoasterPathTrackFrame.trackBasisAtHit(level, sample.hit());
    var point =
        CoasterPathTrackFrame.pathAnchorForSnappedBlock(level, sample.hit())
            .add(basis.up().scale(3));
    handles.worldAnchor.set(point.x, point.y, point.z);
    handles.worldOrientation.set(CoasterPathTrackFrame.quaternionFromTrackBasis(basis));
    handles.guide.setFrame1(handles.worldAnchor, handles.worldOrientation);
    // Re-anchoring a world joint alone does not wake a sleeping native body.
    // The catch moves through the solver; waking it adds no launch impulse.
    var container = SubLevelContainer.getContainer(level);
    container
        .physicsSystem()
        .getPipeline()
        .wakeUp((ServerSubLevel) container.getSubLevel(winch.carriage));
  }

  private static void catchTrain(
      ServerLevel level, ServerSubLevel lead, ShuttleWinchEntity winch, Handles handles) {
    if (handles.catchJoint != null && handles.catchJoint.isValid()) return;
    var container = SubLevelContainer.getContainer(level);
    var carriage = (ServerSubLevel) container.getSubLevel(winch.carriage);
    var a = lead.getPlot().getCenterBlock();
    var b = carriage.getPlot().getCenterBlock();
    handles.catchJoint =
        container
            .physicsSystem()
            .getPipeline()
            .addConstraint(
                lead,
                carriage,
                new GenericConstraintConfiguration(
                    new Vector3d(a.getX() + .5, a.getY() + 3.5, a.getZ() + .5),
                    new Vector3d(b.getX() + .5, b.getY() + .5, b.getZ() + .5),
                    new Quaterniond(),
                    new Quaterniond(),
                    EnumSet.of(
                        ConstraintJointAxis.LINEAR_X,
                        ConstraintJointAxis.LINEAR_Y,
                        ConstraintJointAxis.LINEAR_Z)));
    handles.catchJoint.setContactsEnabled(false);
  }

  /** Keep every coupled chassis at its fault position while its matching rail is absent. */
  private static void holdUnsupportedTrain(ServerLevel level, ShuttleWinchEntity winch) {
    var container = SubLevelContainer.getContainer(level);
    if (container == null) return;
    var handles = HANDLES.computeIfAbsent(winch, ignored -> new Handles());
    var pipeline = container.physicsSystem().getPipeline();
    for (var id : winch.cars) {
      if (!(container.getSubLevel(id) instanceof ServerSubLevel body) || body.isRemoved())
        throw new IllegalArgumentException("Shuttle car unavailable during support loss");
      var hold = handles.supportHolds.get(id);
      if (hold != null && hold.isValid()) continue;
      remove(hold);
      var center = body.getPlot().getCenterBlock();
      hold =
          pipeline.addConstraint(
              null,
              body,
              new GenericConstraintConfiguration(
                  new Vector3d(body.logicalPose().position()),
                  new Vector3d(center.getX() + .5, center.getY() + .5, center.getZ() + .5),
                  new Quaterniond(body.logicalPose().orientation()),
                  new Quaterniond()));
      for (var axis : ConstraintJointAxis.ALL)
        hold.setMotor(axis, 0, 30000, 4000, true, 80000);
      handles.supportHolds.put(id, hold);
    }
  }

  public static void releaseSupportHolds(ShuttleWinchEntity winch) {
    var handles = HANDLES.get(winch);
    if (handles != null) removeSupportHolds(handles);
  }

  private static void removeSupportHolds(Handles handles) {
    handles.supportHolds.values().forEach(ShuttleRuntime::remove);
    handles.supportHolds.clear();
  }

  private static void remove(PhysicsConstraintHandle handle) {
    if (handle != null && handle.isValid()) handle.remove();
  }
}
