package dev.fourthdimension.gametest;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.shuttle.ShuttleExamples;
import dev.fourthdimension.shuttle.ShuttleRuntime;
import dev.fourthdimension.shuttle.ShuttleWinchEntity;
import dev.fourthdimension.vehicle.Boarding;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.joml.Vector3d;

@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class ShuttleBoardingHoldGameTests {
  @GameTest(
      templateNamespace = "fourthdimension",
      template = "empty",
      batch = "fourthdimension_engineered_availability",
      timeoutTicks = 250)
  public static void reloadedBoardingHoldSettlesMovingTrain(GameTestHelper helper) {
    var level = helper.getLevel();
    for (int x = 18; x <= 19; x++)
      for (int z = -48; z <= -38; z++) level.setChunkForced(x, z, true);
    var example = ShuttleExamples.create(level, new BlockPos(300, 180, -700), false);
    var container = SubLevelContainer.getContainer(level);
    var pipeline = container.physicsSystem().getPipeline();
    ShuttleWinchEntity[] winch = {example.winch()};
    helper.runAtTickTime(80, () -> ShuttleExamples.assemble(level, example));
    helper.runAtTickTime(
        100,
        () -> {
          var pos = winch[0].getBlockPos();
          var state = level.getBlockState(pos);
          var saved = winch[0].saveWithFullMetadata(level.registryAccess());
          level.removeBlockEntity(pos);
          var restored =
              (ShuttleWinchEntity)
                  BlockEntity.loadStatic(pos, state, saved, level.registryAccess());
          helper.assertTrue(restored != null, "Winch failed to deserialize");
          level.setBlockEntity(restored);
          ShuttleRuntime.register(level, restored);
          winch[0] = restored;
          // Simulate stale persisted carrier data and pose from a cold station reload.
          // ShuttleRuntime must refresh the parent pose and restore neutral capture before
          // installing station holds; otherwise the saved angle blocks normal boarding.
          for (var car : example.cars()) {
            var parent = (ServerSubLevel) container.getSubLevel(car.chassis);
            for (var carrier : car.carriers) {
              var child = (ServerSubLevel) container.getSubLevel(carrier.body);
              carrier.angle = .6;
              carrier.omega = .3;
              var staleOrientation =
                  new org.joml.Quaterniond(parent.logicalPose().orientation())
                      .mul(new org.joml.Quaterniond().rotateX(.6));
              child.logicalPose().orientation().set(staleOrientation);
              child.updateLastPose();
              pipeline.teleport(child, child.logicalPose().position(), staleOrientation);
              pipeline.addLinearAndAngularVelocity(
                  child, new Vector3d(), new Vector3d(.2, 0, 0));
            }
          }
          // Registration removes the previous winch's transient holds, as on load.
          // Introduce real chassis motion before the replacement installs its hold.
          for (var car : example.cars()) {
            pipeline.addLinearAndAngularVelocity(
                (ServerSubLevel) container.getSubLevel(car.chassis),
                new Vector3d(.2, 0, 0),
                new Vector3d());
            helper.assertTrue(!Boarding.ready(level, car), "Disturbance missed boarding guard");
          }
          ShuttleRuntime.tick(level, .05);
          helper.assertTrue(
              example.cars().stream().allMatch(car -> Boarding.ready(level, car)),
              "Cold-reloaded station hold failed to restore neutral carrier capture and stop the train");
          for (var car : example.cars())
            helper.assertTrue(
                pipeline.getLinearVelocity(
                            (ServerSubLevel) container.getSubLevel(car.chassis), new Vector3d())
                        .length()
                    <= .08,
                "Rebuilt hold allowed unsafe chassis speed");
          helper.assertTrue(
              example.cars().stream()
                  .allMatch(
                      car ->
                          ShuttleRuntime.stationHeld(level, car)
                              && car.carriers.stream()
                                  .allMatch(
                                      carrier ->
                                          Math.abs(carrier.angle) <= .04
                                              && Math.abs(carrier.omega) <= .08)),
              "Cold station restore did not persist neutral, physically held carrier capture");
          helper.assertTrue(
              restored.phase == ShuttleWinchEntity.Phase.BOARDING
                  && restored.carriage == null
                  && restored.cars.equals(example.cars().stream().map(car -> car.chassis).toList())
                  && example.cars().stream().allMatch(car -> !car.restraints && car.fault.isEmpty()),
                  "Station hold changed phase, assigned cars, restraints or faults");
        });
    helper.runAtTickTime(
        210,
        () -> {
          helper.assertTrue(
              example.cars().stream().allMatch(car -> Boarding.ready(level, car)),
              "Reloaded station train did not remain boardable through native physics: "
                  + snapshot(level, example));
          helper.assertTrue(
              CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(
                          container.getSubLevel(winch[0].vehicle))
                      .size()
                  == 4,
              "Reloaded station hold lost a native coupling");
          var carrier = example.cars().getLast().carriers.getFirst();
          var body = container.getSubLevel(carrier.body);
          var seat = body.getPlot().getCenterBlock().offset(carrier.seats.getFirst());
          helper.assertTrue(Boarding.canBoard(level, seat), "Trailing-car seat rejected boarding");
          var rider = EntityType.PIG.create(level);
          rider.setNoAi(true);
          rider.setPos(300, 183, -700);
          level.addFreshEntity(rider);
          SeatBlock.sitDown(level, seat, rider);
          helper.assertTrue(rider.isPassenger(), "Normal seat mount failed after hold rebuild");
          rider.stopRiding();
          rider.discard();
          System.out.println(
              "FD_PAX_BOARDING_HOLD reloaded=true movingCars=4 readyCars=4 nativeLinkedCars=4"
                  + " trailingSeatMounted=true");
          helper.succeed();
        });
  }

  private static String snapshot(ServerLevel level, ShuttleExamples.Example example) {
    var container = SubLevelContainer.getContainer(level);
    var details = example.cars().stream().map(car -> {
      var body = (ServerSubLevel) container.getSubLevel(car.chassis);
      return "car=" + car.chassis
          + " ready=" + Boarding.ready(level, car)
          + " velocity=" + container.physicsSystem().getPipeline().getLinearVelocity(body, new Vector3d())
          + " position=" + body.logicalPose().position()
          + " rotationPoint=" + body.logicalPose().rotationPoint()
          + " center=" + body.getPlot().getCenterBlock()
          + " carriers=" + car.carriers.stream()
              .map(c -> "capture=" + c.capture + ",angle=" + c.angle + ",omega=" + c.omega).toList();
    }).toList();
    System.out.println("FD_PAX_BOARDING_DIAGNOSTIC " + details);
    return details.getFirst();
  }
}
