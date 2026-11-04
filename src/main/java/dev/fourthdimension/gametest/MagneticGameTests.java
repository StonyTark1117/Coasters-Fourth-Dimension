package dev.fourthdimension.gametest;

import com.mojang.logging.LogUtils;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.magnetic.*;
import dev.fourthdimension.physics.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class MagneticGameTests {
  @GameTest(template = "empty", batch = "performanceFreeSpin", timeoutTicks = 2600)
  public static void eightRidersPassiveMagnetCircuitAndCapture(GameTestHelper helper) {
    var level = helper.getLevel();
    for (int x = -45; x <= -40; x++)
      for (int z = 11; z <= 18; z++) level.setChunkForced(x, z, true);
    var origin = new BlockPos(-700, 140, 200);
    var vehicle = ExampleLayouts.create(level, origin, TrackDesign.FREE_SPIN);
    var container = SubLevelContainer.getContainer(level);
    var riders = new ArrayList<net.minecraft.world.entity.animal.Pig>();
    double[] low = {0, 0, 0, 0}, high = {0, 0, 0, 0}, maxXZ = {0, 0}, separation = {0};
    long[] baseline = {0};
    CarrierRuntime.PerformanceSnapshot[] performance = {null};
    var running = new java.util.concurrent.atomic.AtomicBoolean(false);
    helper.onEachTick(
        () -> {
          if (!running.get()) return;
          var point = CoasterCartResolve.cartCenterWorld(container.getSubLevel(vehicle.chassis));
          maxXZ[0] = Math.max(maxXZ[0], point.x - origin.getX());
          maxXZ[1] = Math.max(maxXZ[1], point.z - origin.getZ());
          for (int i = 0; i < 4; i++) {
            low[i] = Math.min(low[i], vehicle.carriers.get(i).angle);
            high[i] = Math.max(high[i], vehicle.carriers.get(i).angle);
          }
          separation[0] =
              Math.max(
                  separation[0],
                  Math.abs(vehicle.carriers.get(0).angle - vehicle.carriers.get(3).angle));
        });
    helper.runAtTickTime(
        80,
        () -> {
          helper.assertTrue(
              vehicle.carriers.size() == 4
                  && vehicle.carriers.stream().allMatch(c -> c.seats.size() == 2),
              "Free Spin must have four independent pairs");
          helper.assertTrue(
              Boarding.ready(level, vehicle), "Four carriers did not initially capture");
          VehicleOperations.loadingPreset(level, vehicle, 2);
          helper.assertTrue(
              vehicle.carriers.getFirst().ballast == 160 && vehicle.carriers.getLast().ballast == 0,
              "Asymmetric preset not applied per carrier");
          VehicleOperations.loadingPreset(level, vehicle, 0);
          var saved = FoundationData.get(level).save(new CompoundTag(), level.registryAccess());
          var copy = FoundationData.load(saved, level.registryAccess());
          var restored = copy.vehicles.get(vehicle.chassis);
          helper.assertTrue(
              restored.carriers.size() == 4
                  && restored.carriers.getFirst().longitudinal == -3
                  && restored.carriers.getLast().longitudinal == 3,
              "Carrier identity/geometry lost on save");
          helper.assertTrue(
              copy.rails.stream()
                  .anyMatch(r -> r.family == TrackDesign.FREE_SPIN.family && r.magnets.size() == 3),
              "Equipment lost on save");
          for (var c : vehicle.carriers) {
            var body = (ServerSubLevel) container.getSubLevel(c.body);
            System.out.println(
                "FD02_CARRIER mass="
                    + body.getMassTracker().getMass()
                    + " inertia="
                    + body.getMassTracker().getInertiaTensor());
            var com =
                new org.joml.Vector3d(body.getMassTracker().getCenterOfMass())
                    .sub(
                        new org.joml.Vector3d(
                            body.getPlot().getCenterBlock().getX() + .5,
                            body.getPlot().getCenterBlock().getY() + .5,
                            body.getPlot().getCenterBlock().getZ() + .5));
            System.out.println(
                "FD02_MASS_REFERENCE com="
                    + com
                    + " firstMoment="
                    + (-4 * com.y)
                    + " inertiaAboutPivot="
                    + (body.getMassTracker().getInertiaTensor().m00()
                        + 4 * (com.y * com.y + com.z * com.z)));
            for (var offset : c.seats) {
              var pig = net.minecraft.world.entity.EntityType.PIG.create(level);
              pig.setNoAi(true);
              pig.setPos(-700, 145, 200);
              level.addFreshEntity(pig);
              SeatBlock.sitDown(level, body.getPlot().getCenterBlock().offset(offset), pig);
              helper.assertTrue(pig.isPassenger(), "One of eight seats could not board");
              riders.add(pig);
            }
          }
          VehicleOperations.restraints(level, vehicle, true);
          CarrierRuntime.resetPerformanceSnapshot();
          VehicleOperations.dispatch(level, vehicle);
          baseline[0] = MagneticRuntime.applications;
          running.set(true);
        });
    helper.runAtTickTime(
        220,
        () ->
            level.setBlockAndUpdate(
                origin.offset(-6, 0, 21), Blocks.REDSTONE_BLOCK.defaultBlockState()));
    helper.runAtTickTime(
        2450,
        () -> {
          running.set(false);
          performance[0] = CarrierRuntime.performanceSnapshot();
          helper.assertTrue(
              performance[0].carrierUpdates() >= 8_000,
              "Free Spin performance baseline did not cover enough live carrier updates");
          if (Boolean.getBoolean("fourthdimension.performanceTargetValidation"))
            helper.assertTrue(
                CarrierRuntime.meetsPerformanceTarget(performance[0]),
                "Free Spin scene exceeded FD904 target: invocation="
                    + performance[0].averageNanos()
                    + "ns carrier="
                    + performance[0].averageCarrierNanos()
                    + "ns");
          System.out.println(
              "FD02_NATIVE applications="
                  + (MagneticRuntime.applications - baseline[0])
                  + " maxEnergyIncrease="
                  + MagneticRuntime.maxEnergyIncrease
                  + " dissipated="
                  + MagneticRuntime.dissipatedEnergy
                  + " maxSpinImpulse="
                  + MagneticRuntime.largestSpinGain
                  + " angles="
                  + Arrays.toString(low)
                  + ".."
                  + Arrays.toString(high)
                  + " separation="
                  + separation[0]
                  + " maxXZ="
                  + Arrays.toString(maxXZ)
                  + " fault="
                  + vehicle.fault);
          LogUtils.getLogger()
              .info(
                  "FD_PERFORMANCE_FREE_SPIN riders=8 runtimeInvocations={} runtimeNanos={} averageNanosPerInvocation={} carrierUpdates={} carrierNanos={} averageNanosPerCarrier={} constraintsRebuilt={}",
                  performance[0].steps(),
                  performance[0].nanos(),
                  performance[0].averageNanos(),
                  performance[0].carrierUpdates(),
                  performance[0].carrierNanos(),
                  performance[0].averageCarrierNanos(),
                  performance[0].constraintsRebuilt());
          helper.assertTrue(
              maxXZ[0] > 30 && maxXZ[1] > 50, "Magnetic car did not complete circuit");
          helper.assertTrue(
              MagneticRuntime.applications > baseline[0] + 10,
              "Magnetic zones never affected native bodies");
          long damperImpulses = 0;
          double damperEnergy = 0, damperSpeedReduction = 0;
          for (var carrier : vehicle.carriers) {
            var audit =
                MagneticRuntime.audit(
                    (ServerSubLevel) container.getSubLevel(carrier.body));
            damperImpulses += audit.damperImpulses;
            damperEnergy += audit.damperEnergyChange;
            damperSpeedReduction += audit.damperAngularSpeedReduction;
          }
          System.out.println(
              "FD02_DAMPER impulses="
                  + damperImpulses
                  + " energy="
                  + damperEnergy
                  + " relativeAngularSpeedReduction="
                  + damperSpeedReduction);
          helper.assertTrue(
              damperImpulses > 0 && damperEnergy < 0,
              "Released Free Spin carriers received no dissipative damper impulse");
          helper.assertTrue(
              MagneticRuntime.maxEnergyIncrease < 1e-5 && MagneticRuntime.dissipatedEnergy > 0,
              "Passive native interaction generated energy");
          helper.assertTrue(
              separation[0] > .15, "Carrier targets did not produce independent motion");
          helper.assertTrue(
              riders.size() == 8 && riders.stream().allMatch(p -> p.isPassenger()),
              "Car lost riders");
          helper.assertTrue(
              Boarding.ready(level, vehicle) && vehicle.stopping && vehicle.restraints,
              "All four carriers did not return to station capture");
          var c = vehicle.carriers.getLast();
          double before = c.omega;
          c.omega = .2;
          boolean denied = false;
          try {
            VehicleOperations.restraints(level, vehicle, false);
          } catch (IllegalArgumentException e) {
            denied = true;
          }
          helper.assertTrue(denied, "One unsettled group allowed restraint release");
          c.omega = before;
          VehicleOperations.restraints(level, vehicle, false);
          riders.forEach(
              p -> {
                p.stopRiding();
                p.discard();
              });
          container.removeSubLevel(
              container.getSubLevel(vehicle.chassis), SubLevelRemovalReason.REMOVED);
          helper.succeed();
        });
  }
}
