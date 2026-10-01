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

  private static final class Handles {
    GenericConstraintHandle guide, catchJoint;
    Vector3d worldAnchor;
    Quaterniond worldOrientation;
    UUID body;
  }

  public static void register(ServerLevel level, ShuttleWinchEntity winch) {
    var previous =
        WINCHES.computeIfAbsent(level, k -> new HashMap<>()).put(winch.getBlockPos(), winch);
    if (previous != null && previous != winch) {
      var handles = HANDLES.remove(previous);
      if (handles != null) {
        remove(handles.guide);
        remove(handles.catchJoint);
      }
    }
  }

  public static Collection<ShuttleWinchEntity> winches(ServerLevel level) {
    return WINCHES.getOrDefault(level, Map.of()).values();
  }

  public static ShuttleWinchEntity owner(ServerLevel level, UUID car) {
    return winches(level).stream().filter(w -> w.cars.contains(car)).findFirst().orElse(null);
  }

  public static boolean running(ServerLevel level, UUID car) {
    var w = owner(level, car);
    return w != null && w.phase != ShuttleWinchEntity.Phase.BOARDING;
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
      try {
        // Deserialization can enter FAULT without passing through fail(). Reapply its
        // persisted train retention before physics, including when chunks return separately.
        if (winch.phase == ShuttleWinchEntity.Phase.FAULT) {
          var data = FoundationData.get(level);
          for (var id : winch.cars) {
            var car = data.vehicles.get(id);
            if (car != null
                && (!car.stopping
                    || !car.restraints
                    || car.carriers.stream().anyMatch(c -> !c.capture))) {
              winch.fail(winch.fault);
              break;
            }
          }
        }
        // Check simulation availability before any world block lookup. Querying the old winch
        // while its chunk leaves ticking range can renew a FULL ticket and prevent eviction.
        if (!dev.ryanhcode.sable.sublevel.system.ticket.PhysicsChunkTicketManager
                .isChunkLoadedEnough(
                    level, winch.getBlockPos().getX() >> 4, winch.getBlockPos().getZ() >> 4)
            || !level.hasChunkAt(winch.getBlockPos())) {
          if (winch.phase != ShuttleWinchEntity.Phase.FAULT)
            winch.fail("Shuttle winch chunk unavailable or winch removed");
          // Native bodies can return before the winch chunk. Retain their guide without a
          // world-block lookup, rather than allowing that load-order gap to drop the carriage.
          holdCarriage(level, winch);
          continue;
        }
        if (!level.getBlockState(winch.getBlockPos()).is(ShuttleContent.WINCH.get())) {
          winch.fail("Shuttle winch removed: retain and inspect every car before recovery");
          var handles = HANDLES.remove(winch);
          if (handles != null) {
            remove(handles.guide);
            remove(handles.catchJoint);
          }
          var container = SubLevelContainer.getContainer(level);
          var body = winch.carriage == null ? null : container.getSubLevel(winch.carriage);
          if (body != null)
            container.removeSubLevel(
                body, dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason.REMOVED);
          WINCHES.get(level).remove(winch.getBlockPos(), winch);
          continue;
        }
        if (level.getBlockEntity(winch.getBlockPos()) != winch)
          throw new IllegalArgumentException("Shuttle winch chunk unavailable or winch removed");
        if (winch.phase == ShuttleWinchEntity.Phase.FAULT) {
          // Native constraints are transient. Rebuild the stationary catch after a reload,
          // without advancing the persisted carriage coordinate or resuming the lift.
          holdCarriage(level, winch);
          continue;
        }
        var route = ShuttleRoute.resolve(level, winch.routeStart, winch.routeEnd);
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
          if (!catchTrain(level, lead, winch, handles))
            throw new IllegalArgumentException(
                "Native carriage is not aligned with the train; capture and recover before"
                    + " lifting");
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
            if (!catchTrain(level, lead, winch, handles)) continue;
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

  private static void holdCarriage(ServerLevel level, ShuttleWinchEntity winch) {
    var container = SubLevelContainer.getContainer(level);
    if (winch.carriage == null || container.getSubLevel(winch.carriage) == null) return;
    var held = HANDLES.computeIfAbsent(winch, k -> new Handles());
    ensureCarriage(level, winch, null, held);
    if (container.getSubLevel(winch.vehicle) instanceof ServerSubLevel lead
        && (winch.interrupted == ShuttleWinchEntity.Phase.LIFTING
            || winch.interrupted == ShuttleWinchEntity.Phase.CAPTURING
            || winch.interrupted == ShuttleWinchEntity.Phase.BOARDING))
      catchTrain(level, lead, winch, held);
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

  private static boolean catchTrain(
      ServerLevel level, ServerSubLevel lead, ShuttleWinchEntity winch, Handles handles) {
    if (handles.catchJoint != null && handles.catchJoint.isValid()) return true;
    var container = SubLevelContainer.getContainer(level);
    var carriage = (ServerSubLevel) container.getSubLevel(winch.carriage);
    var a = lead.getPlot().getCenterBlock();
    var b = carriage.getPlot().getCenterBlock();
    // CAPTURING includes braking and moving the empty carriage before engagement. Restoring
    // that phase must not turn a separated carriage into a long-range constraint on the train.
    var trainPoint =
        lead.logicalPose()
            .transformPosition(new Vector3d(a.getX() + .5, a.getY() + 3.5, a.getZ() + .5));
    var carriagePoint =
        carriage
            .logicalPose()
            .transformPosition(new Vector3d(b.getX() + .5, b.getY() + .5, b.getZ() + .5));
    if (trainPoint.distanceSquared(carriagePoint) > .25 * .25) return false;
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
    return true;
  }

  private static void remove(PhysicsConstraintHandle handle) {
    if (handle != null && handle.isValid()) handle.remove();
  }
}
