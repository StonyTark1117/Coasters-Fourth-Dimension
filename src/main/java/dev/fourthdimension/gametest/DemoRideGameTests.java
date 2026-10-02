package dev.fourthdimension.gametest;

import com.mojang.logging.LogUtils;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class DemoRideGameTests {
  @GameTest(template = "empty", batch = "performanceControlled", timeoutTicks = 2100)
  public static void occupiedExamplesCompleteNativePoweredCircuits(GameTestHelper helper) {
    var level = helper.getLevel();
    for (int x = -14; x <= 10; x++)
      for (int z = -26; z <= -20; z++) level.setChunkForced(x, z, true);
    var origins = List.of(new BlockPos(-200, 112, -400), new BlockPos(100, 112, -400));
    var vehicles = new ArrayList<FoundationData.Vehicle>();
    var riders = new ArrayList<net.minecraft.world.entity.animal.Pig>();
    var container = SubLevelContainer.getContainer(level);
    for (int i = 0; i < 2; i++)
      vehicles.add(ExampleLayouts.create(level, origins.get(i), TrackDesign.values()[i]));
    double[] maxAngle = {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
    double[] minAngle = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
    double[] maxX = {0, 0}, maxZ = {0, 0};
    CarrierRuntime.PerformanceSnapshot[] performance = {null};
    var running = new java.util.concurrent.atomic.AtomicBoolean(false);
    helper.onEachTick(
        () -> {
          if (!running.get()) return;
          for (int i = 0; i < 2; i++) {
            var v = vehicles.get(i);
            var point = CoasterCartResolve.cartCenterWorld(container.getSubLevel(v.chassis));
            maxX[i] = Math.max(maxX[i], point.x - origins.get(i).getX());
            maxZ[i] = Math.max(maxZ[i], point.z - origins.get(i).getZ());
            maxAngle[i] = Math.max(maxAngle[i], v.carriers.getFirst().angle);
            minAngle[i] = Math.min(minAngle[i], v.carriers.getFirst().angle);
          }
        });
    helper.runAtTickTime(
        80,
        () -> {
          CarrierRuntime.resetPerformanceSnapshot();
          for (var v : vehicles) {
            helper.assertTrue(Boarding.ready(level, v), "Example did not start captured");
            var rider = net.minecraft.world.entity.EntityType.PIG.create(level);
            rider.setNoAi(true);
            rider.setPos(0, 112, -400);
            level.addFreshEntity(rider);
            riders.add(rider);
            var c = v.carriers.getFirst();
            var body = container.getSubLevel(c.body);
            com.simibubi.create.content.contraptions.actors.seat.SeatBlock.sitDown(
                level, body.getPlot().getCenterBlock().offset(c.seats.getFirst()), rider);
            helper.assertTrue(rider.isPassenger(), "Example passenger could not board");
            VehicleOperations.restraints(level, v, true);
            VehicleOperations.dispatch(level, v);
          }
          running.set(true);
        });
    helper.runAtTickTime(
        220,
        () -> {
          for (var origin : origins)
            level.setBlockAndUpdate(
                origin.offset(-6, 0, 21), Blocks.REDSTONE_BLOCK.defaultBlockState());
        });
    helper.runAtTickTime(
        2000,
        () -> {
          running.set(false);
          performance[0] = CarrierRuntime.performanceSnapshot();
          helper.assertTrue(
              performance[0].carrierUpdates() >= 10_000,
              "Controlled performance baseline did not cover enough live carrier updates");
          for (int i = 0; i < 2; i++) {
            var v = vehicles.get(i);
            System.out.println(
                "FD_DEMO family="
                    + v.family
                    + " angleRange="
                    + minAngle[i]
                    + ".."
                    + maxAngle[i]
                    + " maxXZ="
                    + maxX[i]
                    + ","
                    + maxZ[i]
                    + " ready="
                    + v.boardingReady
                    + " fault="
                    + v.fault);
            helper.assertTrue(
                maxX[i] > 30 && maxZ[i] > 50, "Example did not travel around its circuit");
            helper.assertTrue(
                i == 0 ? maxAngle[i] > 6 : maxAngle[i] - minAngle[i] > .2,
                "Example did not demonstrate its rotation mechanism");
            helper.assertTrue(riders.get(i).isPassenger(), "Example lost its passenger");
            helper.assertTrue(
                Boarding.ready(level, v) && v.stopping && v.restraints,
                "Returning example failed station capture");
            VehicleOperations.restraints(level, v, false);
            riders.get(i).stopRiding();
            riders.get(i).discard();
            container.removeSubLevel(
                container.getSubLevel(v.chassis), SubLevelRemovalReason.REMOVED);
          }
          LogUtils.getLogger()
              .info(
                  "FD_PERFORMANCE_CONTROLLED families=[ARROW, ZACSPIN] runtimeInvocations={} runtimeNanos={} averageNanosPerInvocation={} carrierUpdates={} carrierNanos={} averageNanosPerCarrier={} constraintsRebuilt={}",
                  performance[0].steps(),
                  performance[0].nanos(),
                  performance[0].averageNanos(),
                  performance[0].carrierUpdates(),
                  performance[0].carrierNanos(),
                  performance[0].averageCarrierNanos(),
                  performance[0].constraintsRebuilt());
          running.set(false);
          helper.succeed();
        });
  }
}
