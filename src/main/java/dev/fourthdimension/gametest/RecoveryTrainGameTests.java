package dev.fourthdimension.gametest;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

/** Full original consists, with only the ordinary bearing receiving native chain propulsion. */
@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class RecoveryTrainGameTests {
  private static final class Train {
    RecoveryExamples.Example example;
    FoundationData.Rail rail;
    final List<FoundationData.Vehicle> cars = new ArrayList<>();
    ServerSubLevel bearing;
    Vector3d start;
    double maxSpacing;
    final List<Pig> riders = new ArrayList<>();
  }

  @GameTest(template = "empty", batch = "recoveryTrain", timeoutTicks = 520)
  public static void occupiedOriginalTrainsPreserveLinksThroughRecovery(GameTestHelper helper) {
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    var data = FoundationData.get(level);
    for (int x = -58; x <= -49; x++)
      for (int z = 49; z <= 54; z++) level.setChunkForced(x, z, true);
    var operator =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000405"), "FDRecoveryTrains"));
    operator.setGameMode(GameType.CREATIVE);
    var trains = new ArrayList<Train>();
    for (var design : List.of(TrackDesign.ARROW, TrackDesign.PAX)) {
      var train = new Train();
      trains.add(train);
      train.example =
          RecoveryExamples.create(
              level, new BlockPos(design == TrackDesign.ARROW ? -900 : -840, 160, 800), design);
      train.rail =
          data.rails.stream()
              .filter(r -> r.matches(train.example.car().boundFrom, train.example.car().boundTo))
              .findFirst()
              .orElseThrow();
      train.cars.add(train.example.car());
      var edge = TrackGeometry.resolve(level, train.rail);
      for (double distance :
          design == TrackDesign.ARROW ? new double[] {6} : new double[] {18, 24, 30}) {
        double t = TrackGeometry.table(level, edge).parameterAt(distance);
        train.cars.add(
            VehicleFactory.create(
                level,
                design,
                new CoasterPathTrackFrame.GraphHit(
                    CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t)));
      }
      if (design == TrackDesign.ARROW) train.bearing = train.example.bearing();
      else {
        // Build the free bearing beyond the end of the actual four-car train.
        // This is initial fixture construction, before any recovery link exists.
        container.removeSubLevel(train.example.bearing(), SubLevelRemovalReason.REMOVED);
        double t = TrackGeometry.table(level, edge).parameterAt(36);
        var hit =
            new CoasterPathTrackFrame.GraphHit(
                CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t);
        var q =
            CoasterPathTrackFrame.quaternionFromTrackBasis(
                CoasterPathTrackFrame.trackBasisAtHit(level, hit));
        train.bearing =
            CoasterCartSpawner.spawnMinimalContraption(
                level,
                CoasterCartLinkPlacement.plotOriginForBearingCenter(
                    CoasterPathTrackFrame.pathAnchorForSnappedBlock(level, hit), q),
                q,
                hit,
                true);
        helper.assertTrue(train.bearing != null, "Native PAX recovery bearing failed to spawn");
      }
    }
    helper.runAtTickTime(
        80,
        () -> {
          for (var train : trains) {
            for (int i = 1; i < train.cars.size(); i++)
              helper.assertTrue(
                  CoasterCartTrainLinkConstraint.linkCarts(
                      level,
                      (ServerSubLevel) container.getSubLevel(train.cars.get(i - 1).chassis),
                      (ServerSubLevel) container.getSubLevel(train.cars.get(i).chassis),
                      6),
                  "Original native train coupling failed");
            for (var car : train.cars)
              for (var carrier : car.carriers)
                for (var offset : carrier.seats) {
                  var pig = EntityType.PIG.create(level);
                  pig.setNoAi(true);
                  pig.setPos(
                      train.rail.from.getX(), train.rail.from.getY(), train.rail.from.getZ());
                  level.addFreshEntity(pig);
                  SeatBlock.sitDown(
                      level,
                      container.getSubLevel(carrier.body).getPlot().getCenterBlock().offset(offset),
                      pig);
                  helper.assertTrue(pig.isPassenger(), "Original train passenger failed to board");
                  train.riders.add(pig);
                }
            var end =
                train.cars.get(
                    train.cars.getFirst().family == dev.fourthdimension.contract.Family.ARROW
                        ? 0
                        : 3);
            VehicleOperations.restraints(level, end, true);
            VehicleOperations.retention(level, end, true);
            RecoveryOperations.select(operatorAt(operator, train.bearing), train.bearing);
            operatorAt(operator, container.getSubLevel(end.chassis));
            edit(operator, end, 20);
            helper.assertTrue(
                train.cars.stream()
                    .allMatch(
                        c ->
                            train.bearing.getUniqueId().equals(c.recoveryCart)
                                && RecoveryOperations.retained(c)),
                "Recovery identity did not cover the complete original train");
          }
        });
    helper.runAtTickTime(
        140,
        () -> {
          for (var train : trains) {
            var end = end(train);
            operatorAt(operator, container.getSubLevel(end.chassis));
            edit(operator, end, 21);
            train.start =
                new Vector3d(
                    CoasterCartResolve.cartCenterWorld(container.getSubLevel(end.chassis)));
            var lever = train.example.powerLever();
            level
                .getBlockState(lever)
                .useWithoutItem(
                    level,
                    operator,
                    new net.minecraft.world.phys.BlockHitResult(
                        net.minecraft.world.phys.Vec3.atCenterOf(lever),
                        Direction.UP,
                        lever,
                        false));
            train
                .example
                .motor()
                .generatedSpeed
                .setValue(
                    8
                        * ((CoasterAnchorpointBlockEntity) level.getBlockEntity(train.rail.from))
                            .getChainLiftTravelSign());
          }
        });
    helper.onEachTick(
        () -> {
          for (var train : trains)
            if (train.start != null)
              for (int i = 1; i < train.cars.size(); i++)
                train.maxSpacing =
                    Math.max(
                        train.maxSpacing,
                        Math.abs(
                            CoasterCartResolve.cartCenterWorld(
                                        container.getSubLevel(train.cars.get(i - 1).chassis))
                                    .distance(
                                        CoasterCartResolve.cartCenterWorld(
                                            container.getSubLevel(train.cars.get(i).chassis)))
                                - 6));
        });
    helper.runAtTickTime(
        340,
        () -> {
          for (var train : trains) {
            var end = end(train);
            double travel =
                CoasterCartResolve.cartCenterWorld(container.getSubLevel(end.chassis))
                    .distance(train.start);
            helper.assertTrue(
                travel > 3
                    && train.maxSpacing < .15
                    && train.cars.stream()
                        .allMatch(c -> c.fault.isEmpty() && RecoveryOperations.retained(c))
                    && train.riders.stream().allMatch(Pig::isPassenger),
                "Loaded original train failed native recovery: "
                    + train.cars.stream().map(c -> c.fault).toList());
            System.out.println(
                "FD_RECOVERY_TRAIN family="
                    + end.family
                    + " cars="
                    + train.cars.size()
                    + " riders="
                    + train.riders.size()
                    + " travel="
                    + travel
                    + " maxSpacing="
                    + train.maxSpacing);
            operatorAt(operator, container.getSubLevel(end.chassis));
            edit(operator, end, 22);
            train.example.motor().generatedSpeed.setValue(0);
          }
        });
    helper.runAtTickTime(
        410,
        () -> {
          for (var train : trains) {
            var end = end(train);
            helper.assertTrue(
                train.cars.stream().allMatch(c -> Boarding.ready(level, c)),
                "Original train did not stop");
            operatorAt(operator, container.getSubLevel(end.chassis));
            edit(operator, end, 23);
            var ids =
                CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(
                    container.getSubLevel(end.chassis));
            helper.assertTrue(
                ids.equals(
                        train.cars.stream()
                            .map(c -> c.chassis)
                            .collect(java.util.stream.Collectors.toSet()))
                    && !CoasterCartTrainLinkConstraint.areLinkedCarts(
                        container.getSubLevel(end.chassis), train.bearing)
                    && train.cars.stream()
                        .allMatch(
                            c ->
                                c.recoveryCart == null
                                    && c.maintenance
                                    && c.stopping
                                    && c.restraints),
                "Recovery disconnect damaged original train links or retention");
            edit(operator, end, 12);
            train.riders.forEach(Pig::stopRiding);
            helper.assertTrue(
                train.riders.stream().noneMatch(Pig::isPassenger),
                "Original recovery train did not unload");
          }
          System.out.println(
              "FD_RECOVERY_TRAIN originalArrowCars=2 originalPaxCars=4 occupiedRiders=24"
                  + " nativeChainDrive=true originalLinksAfterDetach=true retentionPreserved=true"
                  + " normalUnloading=true");
          helper.succeed();
        });
  }

  private static FoundationData.Vehicle end(Train train) {
    return train.cars.get(
        train.cars.getFirst().family == dev.fourthdimension.contract.Family.ARROW ? 0 : 3);
  }

  private static net.minecraft.server.level.ServerPlayer operatorAt(
      net.minecraft.server.level.ServerPlayer player, dev.ryanhcode.sable.sublevel.SubLevel body) {
    var p = CoasterCartResolve.cartCenterWorld(body);
    player.setPos(p.x + 1, p.y + 1, p.z);
    return player;
  }

  private static void edit(
      net.minecraft.server.level.ServerPlayer player, FoundationData.Vehicle car, int action) {
    EditorNetwork.apply(player, new EditorNetwork.Edit(car.chassis, car.revision, action, ""));
  }
}
