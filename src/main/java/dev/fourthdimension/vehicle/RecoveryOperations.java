package dev.fourthdimension.vehicle;

import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.server.level.*;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

/** Explicit operator recovery, using native couplers and ordinary longitudinal propulsion. */
public final class RecoveryOperations {
  private RecoveryOperations() {}

  private record Selection(
      net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension, UUID id) {}

  private static final Map<ServerPlayer, Selection> SELECTED = new WeakHashMap<>();
  public static final double MAX_SPEED = .75;

  public static void select(ServerPlayer player, SubLevel body) {
    requireOrdinary(player, body);
    SELECTED.put(player, new Selection(player.serverLevel().dimension(), body.getUniqueId()));
  }

  private static void requireOrdinary(ServerPlayer player, SubLevel body) {
    var center = body == null ? null : CoasterCartResolve.cartCenterWorld(body);
    if (center == null
        || center.distanceSquared(new Vector3d(player.getX(), player.getY(), player.getZ()))
            > 16 * 16
        || !player.mayBuild()
        || !player
            .serverLevel()
            .mayInteract(
                player, net.minecraft.core.BlockPos.containing(center.x, center.y, center.z))
        || !ordinary(player.serverLevel(), body))
      throw new IllegalArgumentException(
          "Select a nearby certified bare ordinary bearing with sneak-use of the Rotation Tool");
  }

  public static boolean ordinary(net.minecraft.world.level.Level level, SubLevel body) {
    var data = EligibilityHooks.data(level);
    return body != null
        && !body.isRemoved()
        && data != null
        && !data.vehicles.containsKey(body.getUniqueId())
        && FamilyControllers.find(body).isEmpty()
        && OrdinaryGearCertificate.fits(body);
  }

  public static List<FoundationData.Vehicle> group(ServerLevel level, UUID cart) {
    return FoundationData.get(level).vehicles.values().stream()
        .filter(v -> cart.equals(v.recoveryCart))
        .toList();
  }

  public static boolean retained(FoundationData.Vehicle v) {
    return v.maintenance
        && v.restraints
        && !FoundationData.VARIANT.equals(v.variant)
        && !v.carriers.isEmpty()
        && v.carriers.stream().allMatch(c -> c.capture);
  }

  private static boolean components(ServerLevel level, FoundationData.Vehicle v) {
    var container = SubLevelContainer.getContainer(level);
    return container.getSubLevel(v.chassis) != null
        && FamilyControllers.find(container.getSubLevel(v.chassis)).equals(Set.of(v.family))
        && v.carriers.stream().allMatch(c -> container.getSubLevel(c.body) != null);
  }

  private static boolean support(ServerLevel level, FoundationData.Vehicle v) {
    var rail =
        FoundationData.get(level).rails.stream()
            .filter(r -> r.matches(v.boundFrom, v.boundTo))
            .findFirst()
            .orElse(null);
    return rail != null
        && rail.valid
        && rail.variant.equals(v.variant)
        && TrackGeometry.resolve(level, rail) != null;
  }

  /** Independent current main-rail check; never derives a 4D family from ordinary gear. */
  public static boolean ordinarySupport(
      net.minecraft.world.level.Level level, SubLevel cart, FoundationData.Vehicle v) {
    var center = CoasterCartResolve.cartCenterWorld(cart);
    if (center == null) return false;
    if (!(level instanceof ServerLevel server)) {
      var snapshot = EligibilityHooks.data(level);
      return snapshot != null
          && snapshot.rails.stream()
              .anyMatch(
                  r -> r.valid && r.variant.equals(v.variant) && r.matches(v.boundFrom, v.boundTo));
    }
    var hit =
        CoasterPathTrackFrame.nearestGraphHit(
            server, CoasterPathGraphManager.get(server), new Vec3(center.x, center.y, center.z), 2);
    var data = EligibilityHooks.data(level);
    return hit != null
        && data != null
        && data.rails.stream()
            .anyMatch(
                r ->
                    r.valid
                        && r.variant.equals(v.variant)
                        && r.matches(hit.edge().from(), hit.edge().to()));
  }

  public static boolean canCouple(ServerLevel level, SubLevel a, SubLevel b, double distance) {
    var data = FoundationData.get(level);
    var first = data.vehicles.get(a.getUniqueId());
    var second = data.vehicles.get(b.getUniqueId());
    if ((first == null) == (second == null)) return false;
    var v = first == null ? second : first;
    var cart = first == null ? a : b;
    if (v.recoveryCartRemoved
        || !retained(v)
        || !cart.getUniqueId().equals(v.recoveryCart)
        || !ordinary(level, cart)
        || !Double.isFinite(distance)
        || Math.abs(distance - TrackDesign.byVariant(v.variant).axleSpacing()) > .01
        || !ordinarySupport(level, cart, v)
        || speed(level, cart) > .08) return false;
    var ids = CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(cart);
    if (ids.stream().anyMatch(id -> !id.equals(cart.getUniqueId()))) return false;
    var cars = group(level, v.recoveryCart);
    var linked = CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(first == null ? b : a);
    return !cars.isEmpty()
        && cars.stream()
            .allMatch(
                c ->
                    c.variant.equals(v.variant)
                        && retained(c)
                        && components(level, c)
                        && support(level, c)
                        && Boarding.ready(level, c)
                        && dev.fourthdimension.shuttle.ShuttleRuntime.owner(level, c.chassis)
                            == null)
        && cars.stream()
            .map(c -> c.chassis)
            .collect(java.util.stream.Collectors.toSet())
            .equals(linked);
  }

  public static void attach(ServerPlayer player, FoundationData.Vehicle selected) {
    var level = player.serverLevel();
    var container = SubLevelContainer.getContainer(level);
    var selection = SELECTED.get(player);
    var id =
        selection != null && selection.dimension().equals(level.dimension())
            ? selection.id()
            : null;
    var raw = id == null ? null : container.getSubLevel(id);
    requireOrdinary(player, raw);
    var cart = (ServerSubLevel) raw;
    var cars = VehicleOperations.consist(level, selected);
    if (cars.stream()
        .anyMatch(
            v ->
                v.recoveryCart != null
                    || !retained(v)
                    || !Boarding.ready(level, v)
                    || !components(level, v)
                    || !support(level, v)
                    || dev.fourthdimension.shuttle.ShuttleRuntime.owner(level, v.chassis) != null))
      throw new IllegalArgumentException(
          "Capture and retain every car, close restraints, and detach any shuttle winch before"
              + " coupling");
    var parent = (ServerSubLevel) container.getSubLevel(selected.chassis);
    double spacing = TrackDesign.byVariant(selected.variant).axleSpacing();
    var center = CoasterCartResolve.cartCenterWorld(parent);
    var ordinaryCenter = CoasterCartResolve.cartCenterWorld(cart);
    if (center == null
        || ordinaryCenter == null
        || Math.abs(center.distance(ordinaryCenter) - spacing) > .25)
      throw new IllegalArgumentException(
          "Place the recovery bearing " + spacing + " blocks from the end car axle");
    if (!ordinarySupport(level, cart, selected) || speed(level, cart) > .08)
      throw new IllegalArgumentException("Stop the ordinary bearing on matching functional track");
    cars.forEach(v -> v.recoveryCart = id);
    boolean linked = false;
    try {
      linked = CoasterCartTrainLinkConstraint.linkCarts(level, parent, cart, spacing);
      if (!linked)
        throw new IllegalArgumentException(
            "Recovery coupling rejected: select a free bearing and an end car");
    } finally {
      if (!linked) cars.forEach(v -> v.recoveryCart = null);
    }
    for (var v : cars) {
      v.stopping = true;
      v.revision++;
    }
    SELECTED.remove(player);
    FoundationData.get(level).setDirty();
  }

  public static void start(ServerLevel level, FoundationData.Vehicle selected) {
    var cars = requireGroup(level, selected);
    var cart = SubLevelContainer.getContainer(level).getSubLevel(selected.recoveryCart);
    if (selected.recoveryCartRemoved
        || !ordinary(level, cart)
        || speed(level, cart) > .08
        || cars.stream()
            .anyMatch(
                v ->
                    !retained(v)
                        || !components(level, v)
                        || !support(level, v)
                        || dev.fourthdimension.shuttle.ShuttleRuntime.owner(level, v.chassis)
                            != null
                        || !Boarding.ready(level, v)
                        || !ordinarySupport(level, cart, v)
                        || !CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(
                                SubLevelContainer.getContainer(level).getSubLevel(v.chassis))
                            .contains(v.recoveryCart)
                        || !ConsistRules.eligible(
                            level,
                            SubLevelContainer.getContainer(level).getSubLevel(v.chassis),
                            v)))
      throw new IllegalArgumentException(
          "Restore the complete matching recovery consist, stop and align every carrier before"
              + " towing");
    for (var v : cars) {
      v.stopping = false;
      v.fault = "";
      v.revision++;
    }
    FoundationData.get(level).setDirty();
  }

  public static void brake(ServerLevel level, FoundationData.Vehicle selected) {
    for (var v : requireGroup(level, selected)) {
      v.stopping = true;
      v.revision++;
    }
    FoundationData.get(level).setDirty();
  }

  public static void detach(ServerLevel level, FoundationData.Vehicle selected) {
    var cars = requireGroup(level, selected);
    var cart = SubLevelContainer.getContainer(level).getSubLevel(selected.recoveryCart);
    if (cart == null && !selected.recoveryCartRemoved)
      throw new IllegalArgumentException(
          "Restore the unavailable recovery bearing before disconnecting; permanent removal must be"
              + " observed");
    if ((cart != null && speed(level, cart) > .08)
        || cars.stream().anyMatch(v -> !components(level, v) || !Boarding.ready(level, v)))
      throw new IllegalArgumentException("Brake and align every car before disconnecting recovery");
    for (var v : cars)
      dev.fourthdimension.mixin.RecoveryLinkAccess.fourthdimension$unlink(
          level, v.chassis, selected.recoveryCart);
    for (var v : cars) {
      v.recoveryCart = null;
      v.recoveryCartRemoved = false;
      v.stopping = true;
      v.revision++;
    }
    FoundationData.get(level).setDirty();
  }

  private static List<FoundationData.Vehicle> requireGroup(
      ServerLevel level, FoundationData.Vehicle v) {
    if (v.recoveryCart == null)
      throw new IllegalArgumentException("Attach a selected recovery bearing first");
    return group(level, v.recoveryCart);
  }

  private static double speed(ServerLevel level, SubLevel body) {
    return body instanceof ServerSubLevel server
        ? SubLevelContainer.getContainer(level)
            .physicsSystem()
            .getPipeline()
            .getLinearVelocity(server, new Vector3d())
            .length()
        : Double.POSITIVE_INFINITY;
  }

  /** Missing/broken native equipment retains mode and requires explicit operator resume. */
  public static void tick(ServerLevel level, double dt) {
    var container = SubLevelContainer.getContainer(level);
    var data = FoundationData.get(level);
    for (var id :
        data.vehicles.values().stream()
            .map(v -> v.recoveryCart)
            .filter(Objects::nonNull)
            .distinct()
            .toList()) {
      var cars = group(level, id);
      var cart = container.getSubLevel(id);
      String fault = "";
      if (!ordinary(level, cart)
          || cars.stream()
              .anyMatch(
                  v ->
                      v.recoveryCartRemoved
                          || !retained(v)
                          || !components(level, v)
                          || !support(level, v)
                          || dev.fourthdimension.shuttle.ShuttleRuntime.owner(level, v.chassis)
                              != null
                          || !ordinarySupport(level, cart, v)
                          || !CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(
                                  container.getSubLevel(v.chassis))
                              .contains(id)
                          || !ConsistRules.eligible(level, container.getSubLevel(v.chassis), v)))
        fault =
            "Recovery equipment, coupling or matching track unavailable: brake and inspect before"
                + " resume";
      else if (speed(level, cart) > MAX_SPEED
          || cars.stream()
              .anyMatch(v -> speed(level, container.getSubLevel(v.chassis)) > MAX_SPEED))
        fault = "Recovery speed exceeded 0.75 blocks/s: brake and inspect propulsion";
      if (fault.isEmpty() && cart instanceof ServerSubLevel lead) {
        var center = CoasterCartResolve.cartCenterWorld(lead);
        var hit =
            CoasterPathTrackFrame.nearestGraphHit(
                level,
                CoasterPathGraphManager.get(level),
                new Vec3(center.x, center.y, center.z),
                2);
        var leadingRail =
            data.rails.stream()
                .filter(r -> r.matches(hit.edge().from(), hit.edge().to()))
                .findFirst()
                .orElse(null);
        // Check the ordinary leading axle before it reaches a wrong-family or open end.
        // This only requests braking; it gives the ordinary bearing no 4D mechanism.
        BoundaryGuard.check(level, lead, cars.getFirst(), leadingRail);
      }
      if (!fault.isEmpty())
        for (var v : cars) {
          if (!v.stopping || !v.fault.equals(fault)) v.revision++;
          v.stopping = true;
          v.restraints = true;
          v.carriers.forEach(c -> c.capture = true);
          v.fault = fault;
        }
      if (cars.stream().anyMatch(v -> v.stopping || !v.fault.isEmpty())) {
        for (var v : cars)
          if (!v.stopping) {
            v.stopping = true;
            v.revision++;
          }
        if (cart instanceof ServerSubLevel body) {
          var pipeline = container.physicsSystem().getPipeline();
          var velocity = pipeline.getLinearVelocity(body, new Vector3d());
          if (velocity.length() > 0)
            pipeline.addLinearAndAngularVelocity(
                body, velocity.mul(-Math.min(1, 6 * dt / velocity.length())), new Vector3d());
        }
      }
    }
  }
}
