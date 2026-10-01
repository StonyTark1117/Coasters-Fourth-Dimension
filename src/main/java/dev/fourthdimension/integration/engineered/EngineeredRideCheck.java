package dev.fourthdimension.integration.engineered;

import dev.fourthdimension.integration.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.jwaterfall.coastersengineered.controller.MotorStep;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3d;

/** Optional native check; isolated so absent Engineered classes are never resolved. */
public final class EngineeredRideCheck {
  private EngineeredRideCheck() {}

  public static void run(GameTestHelper helper) {
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    var origins =
        List.of(
            new BlockPos(-800, 150, -700),
            new BlockPos(-650, 150, -700),
            new BlockPos(-500, 150, -700));
    var examples = new ArrayList<EngineeredExamples.Example>();
    var riders = new ArrayList<net.minecraft.world.entity.animal.Pig>();
    var designs = List.of(TrackDesign.ARROW, TrackDesign.LEGACY, TrackDesign.FREE_SPIN);
    for (int i = 0; i < origins.size(); i++) {
      var origin = origins.get(i);
      for (int x = (origin.getX() - 16) >> 4; x <= (origin.getX() + 48) >> 4; x++)
        for (int z = (origin.getZ() - 16) >> 4; z <= (origin.getZ() + 64) >> 4; z++)
          level.setChunkForced(x, z, true);
      examples.add(EngineeredExamples.create(level, origin, designs.get(i)));
    }
    double[] maxX = new double[3], maxZ = new double[3];
    boolean[] observerSeen = new boolean[3], launched = new boolean[1];
    Vector3d[] held = new Vector3d[3];
    helper.onEachTick(
        () -> {
          if (!launched[0]) return;
          for (int i = 0; i < examples.size(); i++) {
            var ex = examples.get(i);
            var center =
                CoasterCartResolve.cartCenterWorld(container.getSubLevel(ex.vehicle().chassis));
            helper.assertTrue(center != null, "Engineered ride lost chassis");
            maxX[i] = Math.max(maxX[i], center.x - origins.get(i).getX());
            maxZ[i] = Math.max(maxZ[i], center.z - origins.get(i).getZ());
            observerSeen[i] |= ex.observer().getPower() > 0;
          }
        });
    helper.runAtTickTime(
        80,
        () -> {
          for (int i = 0; i < examples.size(); i++) {
            var ex = examples.get(i);
            helper.assertTrue(
                Boarding.ready(level, ex.vehicle()), "Engineered fixture not captured");
            var chassis =
                (dev.ryanhcode.sable.sublevel.ServerSubLevel)
                    container.getSubLevel(ex.vehicle().chassis);
            double expectedMass = chassis.getMassTracker().getMass();
            for (var carrier : ex.vehicle().carriers)
              expectedMass +=
                  ((dev.ryanhcode.sable.sublevel.ServerSubLevel)
                          container.getSubLevel(carrier.body))
                      .getMassTracker()
                      .getMass();
            helper.assertTrue(
                Math.abs(
                        dev.jwaterfall.coastersengineered.MotorForce.mass(level, chassis)
                            - expectedMass)
                    < 1e-6,
                "Engineered omitted or double-counted carrier mass");
            // A direct upstream entry must be cancelled before it dereferences drive data.
            dev.jwaterfall.coastersengineered.MotorDrive.apply(
                chassis, level, null, null, null, .05);
            held[i] =
                new Vector3d(
                    CoasterCartResolve.cartCenterWorld(
                        container.getSubLevel(ex.vehicle().chassis)));
            // Advance STOP to LAUNCH while restraints are open. Motor must not release the held
            // car.
            level.setBlockAndUpdate(
                ex.station().south(), Blocks.REDSTONE_BLOCK.defaultBlockState());
          }
        });
    helper.runAtTickTime(
        120,
        () -> {
          for (int i = 0; i < examples.size(); i++) {
            var ex = examples.get(i);
            helper.assertTrue(
                ex.launch().commandMode() == MotorStep.Kind.LAUNCH,
                "Engineered controller failed to advance STOP on redstone");
            helper.assertTrue(
                CoasterCartResolve.cartCenterWorld(container.getSubLevel(ex.vehicle().chassis))
                            .distance(held[i])
                        < .15
                    && !ExternalDrivePolicy.allows(level, ex.vehicle().chassis),
                "Engineered motor bypassed captured carriers or open restraints");
            level.removeBlock(ex.station().south(), false);
          }
        });
    helper.runAtTickTime(
        145,
        () -> {
          for (var ex : examples) {
            helper.assertTrue(
                ex.vehicle().stopping
                    && !RideTelemetry.sample(level, ex.vehicle().chassis).dispatchReady(),
                "Station dispatched with open restraints");
            var rider = net.minecraft.world.entity.EntityType.PIG.create(level);
            rider.setNoAi(true);
            rider.setPos(ex.station().getX(), ex.station().getY() + 1, ex.station().getZ());
            level.addFreshEntity(rider);
            // The outer seat on the last Free Spin carrier reproduced a stall
            // when passive induction and trim braking shared the same section.
            var carrier = ex.vehicle().carriers.getLast();
            com.simibubi.create.content.contraptions.actors.seat.SeatBlock.sitDown(
                level,
                container
                    .getSubLevel(carrier.body)
                    .getPlot()
                    .getCenterBlock()
                    .offset(carrier.seats.getLast()),
                rider);
            helper.assertTrue(rider.isPassenger(), "Engineered rider failed to board");
            riders.add(rider);
            VehicleOperations.restraints(level, ex.vehicle(), true);
            RideOperations.request(
                level, ex.station(), ex.vehicle().chassis, RideOperations.Request.DISPATCH);
            helper.assertTrue(
                ExternalDrivePolicy.allows(level, ex.vehicle().chassis),
                "Validated dispatch did not release motor interlock");
          }
          launched[0] = true;
        });
    helper.runAtTickTime(
        350,
        () -> {
          for (var ex : examples)
            level.setBlockAndUpdate(
                ex.station().south(), Blocks.REDSTONE_BLOCK.defaultBlockState());
        });
    helper.runAtTickTime(
        2250,
        () -> {
          launched[0] = false;
          for (int i = 0; i < examples.size(); i++) {
            var ex = examples.get(i);
            var v = ex.vehicle();
            System.out.println(
                "FD_ENGINEERED_END family="
                    + v.family
                    + " center="
                    + CoasterCartResolve.cartCenterWorld(container.getSubLevel(v.chassis))
                    + " stopped="
                    + v.stopping
                    + " ready="
                    + Boarding.ready(level, v)
                    + " controller="
                    + ex.launch().commandMode());
          }
          for (int i = 0; i < examples.size(); i++) {
            var ex = examples.get(i);
            var v = ex.vehicle();
            System.out.println(
                "FD_ENGINEERED family="
                    + v.family
                    + " maxXZ="
                    + maxX[i]
                    + ","
                    + maxZ[i]
                    + " observer="
                    + observerSeen[i]
                    + " ready="
                    + Boarding.ready(level, v)
                    + " fault="
                    + v.fault);
            helper.assertTrue(
                maxX[i] > 30 && maxZ[i] > 50, "Engineered motors did not complete circuit");
            helper.assertTrue(observerSeen[i], "Engineered observer missed 4D chassis");
            helper.assertTrue(
                v.stopping
                    && Boarding.ready(level, v)
                    && v.restraints
                    && riders.get(i).isPassenger(),
                "Engineered return did not retain and capture the occupied car");
            helper.assertTrue(
                !ExternalDrivePolicy.allows(level, v.chassis),
                "Returning capture did not inhibit motors");
            helper.assertTrue(
                RideOperations.nearby(level, ex.station(), v.chassis),
                "Returning car coasted outside its station's dispatch range");
            helper.assertTrue(
                ex.launch().commandMode() == MotorStep.Kind.COAST,
                "Held returning car disappeared from the Engineered sequence's span report");
            VehicleOperations.restraints(level, v, false);
            riders.get(i).stopRiding();
            riders.get(i).discard();
            container.removeSubLevel(
                container.getSubLevel(v.chassis), SubLevelRemovalReason.REMOVED);
          }
          helper.succeed();
        });
  }
}
