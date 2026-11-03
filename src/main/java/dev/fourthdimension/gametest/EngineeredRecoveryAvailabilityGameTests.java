package dev.fourthdimension.gametest;

import static dev.fourthdimension.gametest.EngineeredGameTests.*;

import dev.fourthdimension.integration.OptionalIntegrations;
import dev.fourthdimension.integration.RideTelemetry;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackDesign;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.ryanhcode.sable.sublevel.storage.serialization.SubLevelSerializer;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

/** A recovery-bearing callback must be safe before the next recovery tick sees an unload. */
@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class EngineeredRecoveryAvailabilityGameTests {
  @GameTest(template = "empty", batch = "fourthdimension_engineered_recovery_availability",
      timeoutTicks = 600)
  public static void recoveryBearingRejectsUnavailableTowComponents(GameTestHelper helper) {
    if (!OptionalIntegrations.installed(OptionalIntegrations.ENGINEERED).mayInspect()) {
      System.out.println("FD_ENGINEERED_RECOVERY_AVAILABILITY absent/uninspected: not exercised");
      helper.succeed();
      return;
    }
    runCase(helper, 0, new Counts(), new ArrayList<>());
  }

  private static void runCase(GameTestHelper helper, int index, Counts counts,
      List<String> failures) {
    if (index == 3) {
      helper.assertTrue(counts.unloads == 3 && counts.braked == 3 && counts.healthy == 3
              && counts.unavailable == 3 && counts.afterTick == 3,
          "Recovery availability skipped a required unload or callback");
      System.out.println("FD_ENGINEERED_RECOVERY_AVAILABILITY complete nativeUnloads="
          + counts.unloads + " brakedCalls=" + counts.braked + " healthyCalls=" + counts.healthy
          + " unavailableCalls=" + counts.unavailable + " afterTickCalls=" + counts.afterTick
          + " failures=" + failures);
      helper.assertTrue(failures.isEmpty(), "Unsafe recovery-bearing motor callbacks: " + failures);
      helper.succeed();
      return;
    }
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    // Distinct clear corridors and fresh native UUIDs avoid partial-restore link reuse.
    var origin = new BlockPos(-800 + index * 50, 120, -900);
    for (int x = (origin.getX() - 8) >> 4; x <= (origin.getX() + 8) >> 4; x++)
      for (int z = (origin.getZ() - 4) >> 4; z <= (origin.getZ() + 52) >> 4; z++)
        level.setChunkForced(x, z, true);
    var recovery = RecoveryExamples.create(level, origin, TrackDesign.ARROW);
    var car = recovery.car();
    var bearing = recovery.bearing();
    var ids = new ArrayList<UUID>();
    ids.add(bearing.getUniqueId());
    ids.add(car.chassis);
    car.carriers.forEach(c -> ids.add(c.body));
    var rail = FoundationData.get(level).rails.stream()
        .filter(r -> r.matches(car.boundFrom, car.boundTo)).findFirst().orElseThrow();
    var controllerPos = origin.west(6);
    level.setBlockAndUpdate(controllerPos, BuiltInRegistries.BLOCK
        .get(ResourceLocation.parse("coastersengineered:linear_motor_controller"))
        .defaultBlockState());
    helper.runAfterDelay(100, () -> {
      try {
        helper.assertTrue(Boarding.ready(level, car), "Fresh recovery car failed alignment");
        helper.assertTrue(car.carriers.size() == 2, "Expected both Arrow carriers");
        VehicleOperations.restraints(level, car, true);
        VehicleOperations.retention(level, car, true);
        var player = FakePlayerFactory.get(level, new com.mojang.authlib.GameProfile(
            UUID.fromString("fdfdfdfd-0000-0000-0000-000000000427"), "FDRecoveryAvailability"));
        player.setGameMode(GameType.CREATIVE);
        var center = CoasterCartResolve.cartCenterWorld(bearing);
        player.setPos(center.x, center.y, center.z);
        RecoveryOperations.select(player, bearing);
        RecoveryOperations.attach(player, car);
        helper.assertTrue(car.stopping && bearing.getUniqueId().equals(car.recoveryCart),
            "Normal recovery attach did not retain the bearing identity and brake");
        // Install while braked, then keep COAST through native-link/controller settling.
        var controller = configureMotor(helper, rail, controllerPos, 10, "FORWARD");
        setMode(controller, "COAST", 0);
        helper.runAfterDelay(30, () -> {
          try {
            helper.assertTrue(CoasterCartTrainLinkConstraint.areLinkedCarts(
                    (ServerSubLevel) container.getSubLevel(car.chassis), bearing),
                "Fresh recovery fixture lost its native coupling");
            setMode(controller, "LAUNCH", 10);
            double braked = probe(helper, bearing, ids, index, "braked");
            counts.braked++;
            if (!(braked < 1e-9)) failures.add(index + "/braked=" + braked);
            RecoveryOperations.start(level, car);
            helper.assertTrue(!car.stopping && car.fault.isEmpty()
                    && RecoveryOperations.retained(car),
                "Explicit Start tow did not establish a healthy retained recovery group");
            helper.assertTrue(probe(helper, bearing, ids, index, "healthy") > .001,
                "Healthy started bearing did not receive motor force");
            counts.healthy++;

            UUID id = index == 2 ? car.chassis : car.carriers.get(index).body;
            var body = (ServerSubLevel) container.getSubLevel(id);
            helper.assertTrue(body != null && !body.isRemoved(), "Unload target unavailable");
            var saved = SubLevelSerializer.toData(body, ids);
            System.out.println("FD_ENGINEERED_RECOVERY_AVAILABILITY unload case=" + index
                + " body=" + id + " role=" + (index == 2 ? "chassis" : "carrier")
                + " bearing=" + bearing.getUniqueId() + " reason=UNLOADED");
            container.removeSubLevel(body, SubLevelRemovalReason.UNLOADED);
            try {
              helper.assertTrue(container.getSubLevel(id) == null && body.isRemoved(),
                  "Native unload did not remove the target body");
              counts.unloads++;
              helper.assertTrue(FoundationData.get(level).vehicles.get(car.chassis) == car
                      && !car.stopping && car.fault.isEmpty() && RecoveryOperations.retained(car)
                      && bearing.getUniqueId().equals(car.recoveryCart)
                      && container.getSubLevel(bearing.getUniqueId()) == bearing,
                  "Unload changed logical towing policy instead of physical availability alone");
              helper.assertTrue(!RideTelemetry.sample(level, car.chassis).available(),
                  "Telemetry still reported an available recovery car");
              for (var survivingId : ids)
                if (!survivingId.equals(id)) {
                  var surviving = container.getSubLevel(survivingId);
                  helper.assertTrue(surviving != null && !surviving.isRemoved(),
                      "Another missing body would invalidate the single-component probe");
                }
              // No game/recovery tick occurs between native unload and this callback.
              double unavailable = probe(helper, bearing, ids, index, "unloaded-before-tick");
              counts.unavailable++;
              if (!(unavailable < 1e-9)) failures.add(index + "/before-tick=" + unavailable);
              RecoveryOperations.tick(level, .05);
              helper.assertTrue(car.stopping && !car.fault.isEmpty(),
                  "Recovery tick did not retain a missing-component brake/fault");
              double afterTick = probe(helper, bearing, ids, index, "unloaded-after-tick");
              counts.afterTick++;
              if (!(afterTick < 1e-9)) failures.add(index + "/after-tick=" + afterTick);
              System.out.println("FD_ENGINEERED_RECOVERY_AVAILABILITY fault case=" + index
                  + " stopping=" + car.stopping + " fault=" + car.fault);
            } finally {
              var restored = SubLevelSerializer.fullyLoad(level, saved);
              helper.assertTrue(restored != null && !restored.isRemoved()
                      && restored.getUniqueId().equals(id) && container.getSubLevel(id) == restored,
                  "Native same-ID restore failed");
              System.out.println("FD_ENGINEERED_RECOVERY_AVAILABILITY restored case=" + index
                  + " body=" + id + " linkRecoveryClaim=false");
            }
          } finally {
            setMode(controller, "COAST", 0);
            cleanup(helper, recovery, rail, origin, ids);
          }
          helper.runAfterDelay(3, () -> {
            for (var id : ids)
              helper.assertTrue(container.getSubLevel(id) == null,
                  "Old recovery body survived cleanup");
            helper.assertTrue(!FoundationData.get(level).vehicles.containsKey(car.chassis),
                "Old logical recovery car survived cleanup");
            runCase(helper, index + 1, counts, failures);
          });
        });
      } catch (RuntimeException | Error failure) {
        cleanup(helper, recovery, rail, origin, ids);
        throw failure;
      }
    });
  }

  private static void cleanup(GameTestHelper helper, RecoveryExamples.Example recovery,
      FoundationData.Rail rail, BlockPos origin, List<UUID> ids) {
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    recovery.car().stopping = true;
    for (var id : ids) {
      var body = container.getSubLevel(id);
      if (body != null && !body.isRemoved())
        container.removeSubLevel(body, SubLevelRemovalReason.REMOVED);
    }
    FoundationData.get(level).vehicles.remove(recovery.car().chassis);
    FoundationData.get(level).rails.remove(rail);
    for (var pos : BlockPos.betweenClosed(origin.offset(-8, -1, -4), origin.offset(8, 12, 52)))
      if (!level.getBlockState(pos).isAir()) level.removeBlock(pos, false);
    for (int x = (origin.getX() - 8) >> 4; x <= (origin.getX() + 8) >> 4; x++)
      for (int z = (origin.getZ() - 4) >> 4; z <= (origin.getZ() + 52) >> 4; z++)
        level.setChunkForced(x, z, false);
  }

  private static double probe(GameTestHelper helper, ServerSubLevel bearing,
      List<UUID> ids, int index, String state) {
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    helper.assertTrue(!bearing.isRemoved()
            && container.getSubLevel(bearing.getUniqueId()) == bearing,
        "Recovery motor source must remain the same live ordinary bearing");
    var totals = ids.stream().map(container::getSubLevel).filter(Objects::nonNull)
        .map(b -> ((ServerSubLevel) b).getOrCreateQueuedForceGroup(
            dev.ryanhcode.sable.api.physics.force.ForceGroups.PROPULSION.get()).getForceTotal())
        .toList();
    var forces = totals.stream().map(t -> new Vector3d(t.getLocalForce())).toList();
    var torques = totals.stream().map(t -> new Vector3d(t.getLocalTorque())).toList();
    var center = CoasterCartResolve.cartCenterWorld(bearing);
    var hit = CoasterPathTrackFrame.nearestGraphHit(level, CoasterPathGraphManager.get(level),
        new net.minecraft.world.phys.Vec3(center.x, center.y, center.z), 2);
    helper.assertTrue(hit != null, "Ordinary bearing lost its track hit");
    double maximum = 0;
    try {
      invoke(type("MotorDrive"), "apply", bearing, level, container.physicsSystem(), hit,
          CoasterPathTrackFrame.trackBasisAtHit(level, hit), .05d);
      for (int i = 0; i < totals.size(); i++) {
        maximum = Math.max(maximum, totals.get(i).getLocalForce().distance(forces.get(i)));
        maximum = Math.max(maximum, totals.get(i).getLocalTorque().distance(torques.get(i)));
      }
    } finally {
      for (int i = 0; i < totals.size(); i++) {
        totals.get(i).getLocalForce().set(forces.get(i));
        totals.get(i).getLocalTorque().set(torques.get(i));
      }
    }
    System.out.println("FD_ENGINEERED_RECOVERY_AVAILABILITY case=" + index + " state=" + state
        + " source=ordinary-bearing maxQueuedDelta=" + maximum);
    return maximum;
  }

  private static final class Counts {
    int unloads;
    int braked;
    int healthy;
    int unavailable;
    int afterTick;
  }
}
