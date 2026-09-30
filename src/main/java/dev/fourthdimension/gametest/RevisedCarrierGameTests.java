package dev.fourthdimension.gametest;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
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
import org.joml.Vector3d;

@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class RevisedCarrierGameTests {
  @GameTest(template = "empty", timeoutTicks = 2650)
  public static void revisedPivotEightRidersIndependentCircuitAndSave(GameTestHelper helper) {
    var level = helper.getLevel();
    for (int x = 27; x <= 31; x++) for (int z = 29; z <= 34; z++) level.setChunkForced(x, z, true);
    var origin = new BlockPos(450, 140, 480);
    var v = ExampleLayouts.create(level, origin, TrackDesign.REVISED);
    var container = SubLevelContainer.getContainer(level);
    var riders = new ArrayList<net.minecraft.world.entity.animal.Pig>();
    double[] low = {0, 0, 0, 0}, high = {0, 0, 0, 0}, extent = {0, 0};
    boolean[] running = {false};
    helper.onEachTick(
        () -> {
          if (!running[0]) return;
          var p = CoasterCartResolve.cartCenterWorld(container.getSubLevel(v.chassis));
          extent[0] = Math.max(extent[0], p.x - origin.getX());
          extent[1] = Math.max(extent[1], p.z - origin.getZ());
          for (int i = 0; i < 4; i++) {
            low[i] = Math.min(low[i], v.carriers.get(i).angle);
            high[i] = Math.max(high[i], v.carriers.get(i).angle);
          }
        });
    helper.runAtTickTime(
        80,
        () -> {
          helper.assertTrue(Boarding.ready(level, v), "Revised car did not capture");
          helper.assertTrue(v.carriers.size() == 4, "Revised pairs not independent");
          for (var c : v.carriers) {
            helper.assertTrue(
                c.childPivotOffset == -.5 && c.pivotHeight == 1.5 && c.seats.size() == 2,
                "Revised pivot geometry changed");
            var body = (ServerSubLevel) container.getSubLevel(c.body);
            var com =
                new Vector3d(body.getMassTracker().getCenterOfMass())
                    .sub(
                        new Vector3d(
                            body.getPlot().getCenterBlock().getX() + .5,
                            body.getPlot().getCenterBlock().getY() + .5 + c.childPivotOffset,
                            body.getPlot().getCenterBlock().getZ() + .5));
            double m = body.getMassTracker().getMass();
            System.out.println(
                "FD_REVISED_MASS mass="
                    + m
                    + " firstMoment="
                    + (-m * com.y)
                    + " inertia="
                    + (body.getMassTracker().getInertiaTensor().m00()
                        + m * (com.y * com.y + com.z * com.z)));
            helper.assertTrue(Math.abs(com.y) < .5, "Revised hinge not nearer center of mass");
            for (var offset : c.seats) {
              var pig = net.minecraft.world.entity.EntityType.PIG.create(level);
              pig.setNoAi(true);
              pig.setPos(450, 145, 480);
              level.addFreshEntity(pig);
              SeatBlock.sitDown(level, body.getPlot().getCenterBlock().offset(offset), pig);
              helper.assertTrue(pig.isPassenger(), "Revised seat failed boarding");
              riders.add(pig);
            }
          }
          var saved = FoundationData.get(level).save(new CompoundTag(), level.registryAccess());
          var restored = FoundationData.load(saved, level.registryAccess()).vehicles.get(v.chassis);
          helper.assertTrue(
              restored.carriers.getFirst().childPivotOffset == -.5,
              "Revised joint offset lost on save");
          VehicleOperations.restraints(level, v, true);
          VehicleOperations.dispatch(level, v);
          running[0] = true;
        });
    helper.runAtTickTime(
        220,
        () ->
            level.setBlockAndUpdate(
                origin.offset(-6, 0, 21), Blocks.REDSTONE_BLOCK.defaultBlockState()));
    helper.runAtTickTime(
        2500,
        () -> {
          running[0] = false;
          System.out.println(
              "FD_REVISED_NATIVE angles="
                  + Arrays.toString(low)
                  + ".."
                  + Arrays.toString(high)
                  + " extent="
                  + Arrays.toString(extent)
                  + " ready="
                  + v.boardingReady
                  + " fault="
                  + v.fault);
          helper.assertTrue(
              extent[0] > 30 && extent[1] > 50, "Revised car did not complete the circuit");
          helper.assertTrue(
              riders.size() == 8 && riders.stream().allMatch(p -> p.isPassenger()),
              "Revised car lost a rider");
          helper.assertTrue(
              high[0] - low[0] > .1, "Revised rotor did not respond to chassis movement");
          helper.assertTrue(
              Boarding.ready(level, v) && v.restraints && v.stopping,
              "Revised all-group capture failed");
          VehicleOperations.restraints(level, v, false);
          riders.forEach(
              p -> {
                p.stopRiding();
                p.discard();
              });
          container.removeSubLevel(container.getSubLevel(v.chassis), SubLevelRemovalReason.REMOVED);
          helper.succeed();
        });
  }
}
