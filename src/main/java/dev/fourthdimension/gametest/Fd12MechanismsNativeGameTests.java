package dev.fourthdimension.gametest;

import dev.fourthdimension.integration.RideLocalOperations;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.ExampleLayouts;
import dev.fourthdimension.track.TrackDesign;
import dev.fourthdimension.track.TrackGeometry;
import dev.fourthdimension.runtime.FoundationData.Rail;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import dev.fourthdimension.vehicle.VehicleFactory;
import dev.fourthdimension.vehicle.VehicleOperations;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelObserver;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.ryanhcode.sable.sublevel.storage.serialization.SubLevelSerializer;
import dev.ryanhcode.sable.sublevel.storage.serialization.SubLevelData;
import dev.ryanhcode.sable.api.sublevel.ticket.SubLevelLoadingTicketType;
import dev.fourthdimension.vehicle.VehicleLifecycle;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.util.Unit;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import dev.ryanhcode.sable.neoforge.event.ForgeSableSubLevelContainerReadyEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * FD1701C native assertions for the bounded ride-local reservation contract.
 * These tests use a native Arrow example where the lifecycle API needs a real
 * complete body. The occupied client/synchronization proof remains separate.
 */
@GameTestHolder("fourthdimension_fd1701c")
@PrefixGameTestTemplate(false)
public final class Fd12MechanismsNativeGameTests {
  private Fd12MechanismsNativeGameTests() {}

  @GameTest(template = "empty", batch = "fd1701c-limit")
  public static void admissionAtLimitDeniesFourthNativeVehicle(GameTestHelper h) {
    var level = h.getLevel();
    ExampleLayouts.createBankedArrow(level, new BlockPos(0, 100, 100));
    var state = FoundationData.get(level).rideLocal;
    state.trains.clear(); state.reservations.clear();
    var nativeTrains = new ArrayList<FoundationData.Vehicle>();
    for (int segment : new int[] {0, 2, 4}) nativeTrains.add(nativeAtRouteRail(level, state.rails.get(segment)));
    for (var train : nativeTrains) RideLocalOperations.admit(level, train);
    var admittedIds = new HashSet<>(state.trains.keySet());
    var admittedReservations = new HashMap<>(state.reservations);
    h.assertTrue(state.trains.size() == RideLocalOperations.MAX_TRAINS,
        "three native trains did not admit at the declared limit");
    var vehicle = nativeAtRouteRail(level, state.rails.get(6));
    boolean denied = false;
    try { RideLocalOperations.admit(level, vehicle); }
    catch (IllegalArgumentException expected) { denied = expected.getMessage().contains("limit"); }
    h.assertTrue(denied, "fourth native train was not denied at the declared limit");
    h.assertTrue(state.trains.size() == RideLocalOperations.MAX_TRAINS,
        "limit denial changed existing train count");
    h.assertTrue(state.trains.keySet().equals(admittedIds)
        && state.reservations.equals(admittedReservations),
        "limit denial changed admitted identities or reservation owners");
    h.succeed();
  }

  private static FoundationData.Vehicle nativeAtRouteRail(net.minecraft.server.level.ServerLevel level, UUID railId) {
    Rail rail = FoundationData.get(level).rails.stream().filter(r -> r.id.equals(railId)).findFirst().orElseThrow();
    var edge = TrackGeometry.resolve(level, rail);
    var point = CoasterPathGraphManager.sampleEdgePosition(level, edge, .2);
    return VehicleFactory.create(level, TrackDesign.ARROW,
        new CoasterPathTrackFrame.GraphHit(point, edge, .2));
  }

  @GameTest(template = "empty", batch = "fd1701c-removal")
  public static void nativeRemovalReleasesOnlyRemovedTrain(GameTestHelper h) {
    var level = h.getLevel();
    var vehicle = ExampleLayouts.createBankedArrow(level, new BlockPos(0, 100, 200));
    var state = FoundationData.get(level).rideLocal;
    state.trains.clear(); state.reservations.clear();
    RideLocalOperations.admit(level, vehicle);
    var removed = vehicle.chassis;
    var retained = UUID.randomUUID();
    state.trains.put(retained, new FoundationData.RideLocalTrain(retained, List.of(retained), 2));
    state.reserve(2, retained); state.reserve(3, retained);
    var container = SubLevelContainer.getContainer(level);
    for (var car : VehicleOperations.consist(level, vehicle)) {
      for (var carrier : car.carriers) {
        var body = container.getSubLevel(carrier.body);
        if (body != null) container.removeSubLevel(body, SubLevelRemovalReason.REMOVED);
      }
      var body = container.getSubLevel(car.chassis);
      if (body != null) container.removeSubLevel(body, SubLevelRemovalReason.REMOVED);
      FoundationData.get(level).vehicles.remove(car.chassis);
    }
    RideLocalOperations.onPermanentRemoval(level, removed);
    h.assertTrue(state.blockOwner(0) == null && state.blockOwner(1) == null,
        "removed train retained a reservation");
    h.assertTrue(state.blockOwner(2).equals(retained) && state.blockOwner(3).equals(retained),
        "removing one train disturbed another train's reservation");
    h.assertTrue(!state.trains.containsKey(removed) && state.trains.containsKey(retained),
        "train identity removal was not isolated");
    h.succeed();
  }

  @GameTest(template = "empty", batch = "fd1701c-observer")
  public static void nativeRemovalUsesLifecycleObserverAndTick(GameTestHelper h) {
    var level = h.getLevel();
    var vehicle = ExampleLayouts.createBankedArrow(level, new BlockPos(0, 100, 400));
    var state = FoundationData.get(level).rideLocal;
    state.trains.clear(); state.reservations.clear();
    RideLocalOperations.admit(level, vehicle);
    var container = SubLevelContainer.getContainer(level);
    VehicleLifecycle.ready(new ForgeSableSubLevelContainerReadyEvent(level, container));
    for (var car : VehicleOperations.consist(level, vehicle)) {
      for (var carrier : car.carriers) {
        var body = container.getSubLevel(carrier.body);
        if (body != null) container.removeSubLevel(body, SubLevelRemovalReason.REMOVED);
      }
      var body = container.getSubLevel(car.chassis);
      if (body != null) container.removeSubLevel(body, SubLevelRemovalReason.REMOVED);
    }
    VehicleLifecycle.tick(new LevelTickEvent.Post(() -> true, level));
    h.assertTrue(!FoundationData.get(level).vehicles.containsKey(vehicle.chassis),
        "lifecycle tick did not remove the permanently deleted native vehicle");
    h.assertTrue(!state.trains.containsKey(vehicle.chassis)
        && state.reservations.values().stream().noneMatch(vehicle.chassis::equals),
        "lifecycle observer/tick left stale route train or reservations");
    h.succeed();
  }

  @GameTest(template = "empty", batch = "fd1701c-unload")
  public static void unloadReloadMarksOccupiedTrainUnresolvedAndBraked(GameTestHelper h) {
    var level = h.getLevel();
    var vehicle = ExampleLayouts.createBankedArrow(level, new BlockPos(0, 100, 500));
    var state = FoundationData.get(level).rideLocal;
    state.trains.clear(); state.reservations.clear();
    RideLocalOperations.admit(level, vehicle);
    var container = SubLevelContainer.getContainer(level);
    var body = (dev.ryanhcode.sable.sublevel.ServerSubLevel) container.getSubLevel(vehicle.chassis);
    h.assertTrue(body != null && !body.isRemoved(), "native chassis body missing before transient unload");
    var ids = new ArrayList<UUID>();
    ids.add(vehicle.chassis);
    ids.add(vehicle.carriers.getFirst().body);
    var heldChunks = new LinkedHashSet<ChunkPos>();
    var savedBodies = new LinkedHashMap<UUID, SubLevelData>();
    for (var id : ids) {
      var nativeBody = (dev.ryanhcode.sable.sublevel.ServerSubLevel) container.getSubLevel(id);
      h.assertTrue(nativeBody != null && !nativeBody.isRemoved(), "native body missing before snapshot: " + id);
      var center = nativeBody.getPlot().getCenterBlock();
      var centerChunk = new ChunkPos(center);
      for (int dx = -4; dx <= 4; dx++)
        for (int dz = -4; dz <= 4; dz++)
          heldChunks.add(new ChunkPos(centerChunk.x + dx, centerChunk.z + dz));
      savedBodies.put(id, SubLevelSerializer.toData(nativeBody, ids));
    }
    var reservationsBefore = new HashMap<>(state.reservations);
    container.addObserver(new SubLevelObserver() {
      @Override public void onSubLevelRemoved(dev.ryanhcode.sable.sublevel.SubLevel removed,
          SubLevelRemovalReason reason) {
        if (ids.contains(removed.getUniqueId()))
          System.out.println("FD1701C removal id=" + removed.getUniqueId() + " reason=" + reason);
      }
    });
    for (var chunk : heldChunks) level.setChunkForced(chunk.x, chunk.z, true);
    for (var id : ids) {
      var nativeBody = container.getSubLevel(id);
      if (nativeBody != null) container.removeSubLevel(nativeBody, SubLevelRemovalReason.UNLOADED);
    }
    h.assertTrue(container.getSubLevel(vehicle.chassis) == null,
        "native chassis unload did not remove the body");
    h.runAfterDelay(12, () -> {
      RideLocalOperations.reconcile(level);
      var train = state.trains.get(vehicle.chassis);
      h.assertTrue(train != null && train.unresolved && train.braking,
          "transient native unload did not mark occupied train unresolved/braked");
      boolean denied = false;
      try { RideLocalOperations.beforeDispatch(level, vehicle); }
      catch (IllegalArgumentException expected) { denied = true; }
      h.assertTrue(denied, "dispatch was not denied while transiently unloaded train was unresolved");
      h.assertTrue(state.reservations.equals(reservationsBefore),
          "reconciliation or rejected dispatch changed reservations while body was absent");
      for (var entry : savedBodies.entrySet()) {
        var restored = SubLevelSerializer.fullyLoad(level, entry.getValue());
        System.out.println("FD1701C restore id=" + entry.getKey() + " loaded=" + (restored != null));
        h.assertTrue(restored != null && container.getSubLevel(entry.getKey()) != null
                && container.getSubLevel(entry.getKey()).getUniqueId().equals(entry.getKey()),
            "native body restore did not return the same UUID body: " + entry.getKey());
        container.addForceLoadTicket((dev.ryanhcode.sable.sublevel.ServerSubLevel) restored,
            SubLevelLoadingTicketType.COMMAND_FORCED, Unit.INSTANCE);
      }
      h.runAfterDelay(12, () -> {
        for (var id : ids) {
          var restoredBody = container.getSubLevel(id);
          h.assertTrue(restoredBody != null && !restoredBody.isRemoved()
              && restoredBody.getUniqueId().equals(id),
              "native body UUID did not survive post-restore ticks: " + id);
        }
        h.assertTrue(state.reservations.equals(reservationsBefore),
            "post-restore ticks changed route reservations before explicit resume");
        RideLocalOperations.reconcile(level);
        RideLocalOperations.resume(level, vehicle.chassis);
        var resumed = state.trains.get(vehicle.chassis);
        h.assertTrue(resumed != null && !resumed.unresolved && !resumed.braking,
            "explicit resume did not clear restored unresolved/braking state");
        h.assertTrue(state.blockOwner(resumed.block).equals(vehicle.chassis)
                && state.blockOwner(resumed.nextBlock).equals(vehicle.chassis),
            "explicit resume did not restore both route reservation owners");
        boolean dispatched = true;
        try { RideLocalOperations.beforeDispatch(level, vehicle); }
        catch (IllegalArgumentException e) { dispatched = false; }
        h.assertTrue(dispatched, "dispatch remained denied after physical body restore and resume");
        for (var id : ids) {
          var restoredBody = (dev.ryanhcode.sable.sublevel.ServerSubLevel) container.getSubLevel(id);
          if (restoredBody != null)
            container.removeForceLoadTicket(restoredBody, SubLevelLoadingTicketType.COMMAND_FORCED, Unit.INSTANCE);
        }
        for (var chunk : heldChunks) level.setChunkForced(chunk.x, chunk.z, false);
        h.succeed();
      });
    });
  }

  @GameTest(template = "empty", batch = "fd1701c-restart")
  public static void nativeRestartStateRoundTripPreservesFailClosedState(GameTestHelper h) {
    var level = h.getLevel();
    var vehicle = ExampleLayouts.createBankedArrow(level, new BlockPos(0, 100, 300));
    var state = FoundationData.get(level).rideLocal;
    state.trains.clear(); state.reservations.clear();
    RideLocalOperations.admit(level, vehicle);
    var saved = new CompoundTag();
    FoundationData.get(level).save(saved, level.registryAccess());
    var loaded = FoundationData.load(saved, level.registryAccess());
    var restored = loaded.rideLocal.trains.get(vehicle.chassis);
    h.assertTrue(restored != null && restored.chassis.equals(vehicle.chassis)
        && restored.nextBlock == state.trains.get(vehicle.chassis).nextBlock
        && restored.braking && restored.restored
        && loaded.rideLocal.blockOwner(restored.block).equals(vehicle.chassis)
        && loaded.rideLocal.blockOwner(restored.nextBlock).equals(vehicle.chassis),
        "save/load lost train identity, next block, braking, restored, or reservation state");
    var storage = level.getDataStorage();
    storage.set("fourthdimension_foundation", loaded);
    try {
      h.assertTrue(FoundationData.get(level) == loaded, "loaded FoundationData was not installed into live storage");
      boolean denied = false;
      try { RideLocalOperations.beforeDispatch(level, vehicle); }
      catch (IllegalArgumentException expected) { denied = true; }
      h.assertTrue(denied, "loaded restored native train dispatched without explicit resume");
      RideLocalOperations.resume(level, vehicle.chassis);
      var resumed = FoundationData.get(level).rideLocal.trains.get(vehicle.chassis);
      h.assertTrue(!resumed.restored && !resumed.braking && !resumed.unresolved,
          "explicit resume did not clear loaded fail-closed state");
    } finally {
      storage.set("fourthdimension_foundation", FoundationData.load(saved, level.registryAccess()));
    }
    h.succeed();
  }
}
