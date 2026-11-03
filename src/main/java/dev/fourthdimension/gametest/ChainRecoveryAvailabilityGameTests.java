package dev.fourthdimension.gametest;

import dev.fourthdimension.integration.RideTelemetry;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackDesign;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.ryanhcode.sable.sublevel.storage.serialization.SubLevelSerializer;
import dev.silvergold.simulatedcoasters.track.cart.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;

/** A recovery-bearing callback must be safe before the next recovery tick sees an unload. */
@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class ChainRecoveryAvailabilityGameTests {
  @GameTest(template = "empty", batch = "fourthdimension_chain_recovery_availability",
      timeoutTicks = 600)
  public static void nativeChainRejectsUnavailableTowComponents(GameTestHelper helper) {
    var steps = new ChainAvailabilityProbe.Steps(helper);
    runCase(helper, steps, 0, new Counts(), new ArrayList<>());
  }

  private static void runCase(GameTestHelper helper, ChainAvailabilityProbe.Steps steps,
      int index, Counts counts,
      List<String> failures) {
    if (index == 3) {
      helper.assertTrue(counts.unloads == 3 && counts.braked == 3 && counts.healthy == 3
              && counts.unavailable == 3 && counts.afterTick == 3,
          "Recovery availability skipped a required unload or callback");
      System.out.println("FD_CHAIN_RECOVERY_AVAILABILITY complete nativeUnloads="
          + counts.unloads + " brakedCalls=" + counts.braked + " healthyCalls=" + counts.healthy
          + " unavailableCalls=" + counts.unavailable + " afterTickCalls=" + counts.afterTick
          + " failures=" + failures);
      helper.assertTrue(failures.isEmpty(), "Unsafe recovery-bearing chain callbacks: " + failures);
      helper.succeed();
      return;
    }
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    // Distinct clear corridors and fresh native UUIDs avoid partial-restore link reuse.
    var origin = new BlockPos(-800 + index * 50, 120, -720);
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
    steps.after(100, () -> {
      try {
        helper.assertTrue(Boarding.ready(level, car), "Fresh recovery car failed alignment");
        helper.assertTrue(car.carriers.size() == 2, "Expected both Arrow carriers");
        VehicleOperations.restraints(level, car, true);
        VehicleOperations.retention(level, car, true);
        var player = FakePlayerFactory.get(level, new com.mojang.authlib.GameProfile(
            UUID.fromString("fdfdfdfd-0000-0000-0000-000000000428"), "FDChainRecovery"));
        player.setGameMode(GameType.CREATIVE);
        var center = CoasterCartResolve.cartCenterWorld(bearing);
        player.setPos(center.x, center.y, center.z);
        RecoveryOperations.select(player, bearing);
        RecoveryOperations.attach(player, car);
        helper.assertTrue(car.stopping && bearing.getUniqueId().equals(car.recoveryCart),
            "Normal recovery attach did not retain the bearing identity and brake");
        // Supply real chain power while braked; allow native coupling and kinetics to settle.
        ChainAvailabilityProbe.power(helper, recovery, origin);
        steps.after(30, () -> {
          try {
            helper.assertTrue(CoasterCartTrainLinkConstraint.areLinkedCarts(
                    (ServerSubLevel) container.getSubLevel(car.chassis), bearing),
                "Fresh recovery fixture lost its native coupling");
            double braked = probe(helper, bearing, ids, index, "braked");
            counts.braked++;
            if (!(braked < 1e-9)) failures.add(index + "/braked=" + braked);
            RecoveryOperations.start(level, car);
            helper.assertTrue(!car.stopping && car.fault.isEmpty()
                    && RecoveryOperations.retained(car),
                "Explicit Start tow did not establish a healthy retained recovery group");
            helper.assertTrue(probe(helper, bearing, ids, index, "healthy") > .001,
                "Healthy started bearing did not receive native chain velocity");
            counts.healthy++;

            UUID id = index == 2 ? car.chassis : car.carriers.get(index).body;
            var body = (ServerSubLevel) container.getSubLevel(id);
            helper.assertTrue(body != null && !body.isRemoved(), "Unload target unavailable");
            var saved = SubLevelSerializer.toData(body, ids);
            System.out.println("FD_CHAIN_RECOVERY_AVAILABILITY unload case=" + index
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
              System.out.println("FD_CHAIN_RECOVERY_AVAILABILITY fault case=" + index
                  + " stopping=" + car.stopping + " fault=" + car.fault);
            } finally {
              var restored = SubLevelSerializer.fullyLoad(level, saved);
              helper.assertTrue(restored != null && !restored.isRemoved()
                      && restored.getUniqueId().equals(id) && container.getSubLevel(id) == restored,
                  "Native same-ID restore failed");
              System.out.println("FD_CHAIN_RECOVERY_AVAILABILITY restored case=" + index
                  + " body=" + id + " linkRecoveryClaim=false");
            }
          } finally {
            ChainAvailabilityProbe.stop(helper, recovery);
            cleanup(helper, recovery, rail, origin, ids);
          }
          steps.after(3, () -> {
            for (var id : ids)
              helper.assertTrue(container.getSubLevel(id) == null,
                  "Old recovery body survived cleanup");
            helper.assertTrue(!FoundationData.get(level).vehicles.containsKey(car.chassis),
                "Old logical recovery car survived cleanup");
            runCase(helper, steps, index + 1, counts, failures);
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
    return ChainAvailabilityProbe.measure(helper, bearing, ids, "recovery", index,
        "ordinary-bearing", state);
  }

  private static final class Counts {
    int unloads;
    int braked;
    int healthy;
    int unavailable;
    int afterTick;
  }
}
