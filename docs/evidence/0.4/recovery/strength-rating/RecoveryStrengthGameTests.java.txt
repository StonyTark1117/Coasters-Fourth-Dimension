package dev.fourthdimension.gametest;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackDesign;
import dev.fourthdimension.track.TrackGeometry;
import dev.fourthdimension.vehicle.Boarding;
import dev.fourthdimension.vehicle.RecoveryExamples;
import dev.fourthdimension.vehicle.RecoveryOperations;
import dev.fourthdimension.vehicle.VehicleFactory;
import dev.fourthdimension.vehicle.VehicleOperations;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartLinkPlacement;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartSpawner;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.joml.Vector3d;

/** Physical boundary check for the supported occupied recovery-consist rating. */
@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class RecoveryStrengthGameTests {
  private record Fixture(
      BlockPos origin,
      RecoveryExamples.Example controls,
      FoundationData.Rail rail,
      List<FoundationData.Vehicle> cars,
      ServerSubLevel bearing,
      List<Pig> riders) {}

  @GameTest(
      template = "empty",
      templateNamespace = "fourthdimension_fd402",
      batch = "fourthdimension_fd402_strength",
      timeoutTicks = 620)
  public static void fourOccupiedArrowCarsTowAndFiveAreRejected(GameTestHelper helper) {
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    var operator =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000432"),
                "FDRecoveryStrength"));
    operator.setGameMode(GameType.CREATIVE);
    var rated = create(level, new BlockPos(700, 180, -800), 4);
    var oversized = create(level, new BlockPos(700, 180, -600), 5);
    var started = new boolean[1];
    var start = new Vector3d[1];
    var maxSpacing = new double[1];
    var maxAngle = new double[1];
    var maxSpeed = new double[1];

    helper.runAtTickTime(
        90,
        () -> {
          assemble(level, oversized);
          retain(level, oversized);
          RecoveryOperations.select(operatorAt(operator, oversized.bearing()), oversized.bearing());
          boolean rejected = false;
          try {
            RecoveryOperations.attach(
                operatorAt(
                    operator,
                    container.getSubLevel(oversized.cars().getLast().chassis)),
                oversized.cars().getLast());
          } catch (IllegalArgumentException expected) {
            rejected = expected.getMessage().contains("rated for at most 4 original cars");
          }
          helper.assertTrue(rejected, "Five-car Arrow recovery exceeded the rated consist boundary");
          helper.assertTrue(
              oversized.cars().stream().allMatch(car -> car.recoveryCart == null)
                  && !CoasterCartTrainLinkConstraint.areLinkedCarts(
                      container.getSubLevel(oversized.cars().getLast().chassis),
                      oversized.bearing()),
              "Rejected oversized recovery left partial coupling state");
          cleanup(level, oversized);

          assemble(level, rated);
          board(level, rated);
          retain(level, rated);
          RecoveryOperations.select(operatorAt(operator, rated.bearing()), rated.bearing());
          RecoveryOperations.attach(
              operatorAt(operator, container.getSubLevel(rated.cars().getLast().chassis)),
              rated.cars().getLast());
          helper.assertTrue(
              rated.cars().stream()
                  .allMatch(car -> rated.bearing().getUniqueId().equals(car.recoveryCart)),
              "Rated recovery identity did not cover all four cars");
        });
    helper.runAtTickTime(
        150,
        () -> {
          helper.assertTrue(
              rated.cars().stream().allMatch(car -> Boarding.ready(level, car)),
              "Rated occupied train failed to settle");
          RecoveryOperations.start(level, rated.cars().getLast());
          rated.controls().motor().generatedSpeed.setValue(
              8
                  * ((CoasterAnchorpointBlockEntity) level.getBlockEntity(rated.rail().from))
                      .getChainLiftTravelSign());
          toggle(operator, rated.controls().powerLever());
          start[0] = new Vector3d(CoasterCartResolve.cartCenterWorld(rated.bearing()));
          started[0] = true;
        });
    helper.onEachTick(
        () -> {
          if (!started[0]) return;
          var bodies = new ArrayList<ServerSubLevel>();
          for (var car : rated.cars()) {
            var body = (ServerSubLevel) container.getSubLevel(car.chassis);
            bodies.add(body);
            maxAngle[0] = Math.max(maxAngle[0],
                car.carriers.stream().mapToDouble(carrier -> Math.abs(carrier.angle)).max().orElse(0));
          }
          bodies.add(rated.bearing());
          for (int i = 1; i < bodies.size(); i++)
            maxSpacing[0] = Math.max(
                maxSpacing[0],
                Math.abs(
                    CoasterCartResolve.cartCenterWorld(bodies.get(i - 1))
                            .distance(CoasterCartResolve.cartCenterWorld(bodies.get(i)))
                        - TrackDesign.ARROW.axleSpacing()));
          for (var body : bodies)
            maxSpeed[0] = Math.max(
                maxSpeed[0],
                container.physicsSystem().getPipeline()
                    .getLinearVelocity(body, new Vector3d()).length());
        });
    helper.runAtTickTime(
        390,
        () -> {
          double travel = CoasterCartResolve.cartCenterWorld(rated.bearing()).distance(start[0]);
          helper.assertTrue(travel > 3, "Rated four-car Arrow train did not tow");
          helper.assertTrue(maxSpacing[0] < .15, "Rated train exceeded coupler spacing tolerance");
          helper.assertTrue(maxAngle[0] < .04, "Rated train lost retained carrier alignment");
          helper.assertTrue(maxSpeed[0] < RecoveryOperations.MAX_SPEED + .02,
              "Rated train exceeded the recovery speed guard");
          helper.assertTrue(
              rated.cars().stream().allMatch(car -> car.fault.isEmpty())
                  && rated.riders().stream().allMatch(Pig::isPassenger),
              "Rated tow faulted or lost an occupied seat");
          RecoveryOperations.brake(level, rated.cars().getLast());
          toggle(operator, rated.controls().powerLever());
          System.out.println(
              "FD_RECOVERY_STRENGTH cars=4 riders=" + rated.riders().size()
                  + " travel=" + travel + " maxSpacing=" + maxSpacing[0]
                  + " maxAngle=" + maxAngle[0] + " maxSpeed=" + maxSpeed[0]
                  + " oversizedCars=5 oversizedRejected=true");
        });
    helper.runAtTickTime(
        500,
        () -> {
          helper.assertTrue(
              rated.cars().stream().allMatch(car -> Boarding.ready(level, car)),
              "Rated four-car train failed normal braking");
          RecoveryOperations.detach(level, rated.cars().getLast());
          var originalIds = rated.cars().stream().map(car -> car.chassis)
              .collect(java.util.stream.Collectors.toSet());
          helper.assertTrue(
              CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(
                      container.getSubLevel(rated.cars().getLast().chassis))
                  .equals(originalIds)
                  && rated.cars().stream().allMatch(car -> car.recoveryCart == null)
                  && !CoasterCartTrainLinkConstraint.areLinkedCarts(
                      container.getSubLevel(rated.cars().getLast().chassis), rated.bearing()),
              "Rated recovery detach damaged original links");
          cleanup(level, rated);
          helper.succeed();
        });
  }

  private static Fixture create(
      net.minecraft.server.level.ServerLevel level, BlockPos origin, int count) {
    force(level, origin);
    var controls = RecoveryExamples.create(level, origin, TrackDesign.ARROW);
    var data = FoundationData.get(level);
    var container = SubLevelContainer.getContainer(level);
    var rail = data.rails.stream()
        .filter(candidate -> candidate.matches(controls.car().boundFrom, controls.car().boundTo))
        .findFirst().orElseThrow();
    var edge = TrackGeometry.resolve(level, rail);
    var cars = new ArrayList<FoundationData.Vehicle>();
    cars.add(controls.car());
    for (int i = 1; i < count; i++) {
      double t = TrackGeometry.table(level, edge)
          .parameterAt(12 + i * TrackDesign.ARROW.axleSpacing());
      cars.add(VehicleFactory.create(
          level,
          TrackDesign.ARROW,
          new CoasterPathTrackFrame.GraphHit(
              CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t)));
    }
    container.removeSubLevel(controls.bearing(), SubLevelRemovalReason.REMOVED);
    double t = TrackGeometry.table(level, edge)
        .parameterAt(12 + count * TrackDesign.ARROW.axleSpacing());
    var hit = new CoasterPathTrackFrame.GraphHit(
        CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t);
    var orientation = CoasterPathTrackFrame.quaternionFromTrackBasis(
        CoasterPathTrackFrame.trackBasisAtHit(level, hit));
    var bearing = CoasterCartSpawner.spawnMinimalContraption(
        level,
        CoasterCartLinkPlacement.plotOriginForBearingCenter(
            CoasterPathTrackFrame.pathAnchorForSnappedBlock(level, hit), orientation),
        orientation,
        hit,
        true);
    if (bearing == null) throw new IllegalStateException("Rated recovery bearing allocation failed");
    return new Fixture(origin, controls, rail, List.copyOf(cars), bearing, new ArrayList<>());
  }

  private static void assemble(net.minecraft.server.level.ServerLevel level, Fixture fixture) {
    var container = SubLevelContainer.getContainer(level);
    for (int i = 1; i < fixture.cars().size(); i++)
      if (!CoasterCartTrainLinkConstraint.linkCarts(
          level,
          (ServerSubLevel) container.getSubLevel(fixture.cars().get(i - 1).chassis),
          (ServerSubLevel) container.getSubLevel(fixture.cars().get(i).chassis),
          TrackDesign.ARROW.axleSpacing()))
        throw new IllegalStateException("Rated Arrow train assembly failed");
  }

  private static void board(net.minecraft.server.level.ServerLevel level, Fixture fixture) {
    var container = SubLevelContainer.getContainer(level);
    for (var car : fixture.cars())
      for (var carrier : car.carriers)
        for (var offset : carrier.seats) {
          var pig = EntityType.PIG.create(level);
          pig.setNoAi(true);
          pig.setPos(fixture.origin().getX(), fixture.origin().getY(), fixture.origin().getZ());
          level.addFreshEntity(pig);
          SeatBlock.sitDown(
              level,
              container.getSubLevel(carrier.body).getPlot().getCenterBlock().offset(offset),
              pig);
          if (!pig.isPassenger()) throw new IllegalStateException("Rated recovery rider failed to board");
          fixture.riders().add(pig);
        }
  }

  private static void retain(net.minecraft.server.level.ServerLevel level, Fixture fixture) {
    var end = fixture.cars().getLast();
    VehicleOperations.restraints(level, end, true);
    VehicleOperations.retention(level, end, true);
  }

  private static net.minecraft.server.level.ServerPlayer operatorAt(
      net.minecraft.server.level.ServerPlayer player, dev.ryanhcode.sable.sublevel.SubLevel body) {
    var center = CoasterCartResolve.cartCenterWorld(body);
    player.setPos(center.x + 1, center.y + 1, center.z);
    return player;
  }

  private static void toggle(net.minecraft.server.level.ServerPlayer player, BlockPos lever) {
    player.serverLevel().getBlockState(lever).useWithoutItem(
        player.serverLevel(),
        player,
        new BlockHitResult(Vec3.atCenterOf(lever), Direction.UP, lever, false));
  }

  private static void cleanup(net.minecraft.server.level.ServerLevel level, Fixture fixture) {
    var data = FoundationData.get(level);
    var container = SubLevelContainer.getContainer(level);
    fixture.riders().forEach(Pig::discard);
    for (var car : fixture.cars()) {
      for (var carrier : car.carriers) {
        var body = container.getSubLevel(carrier.body);
        if (body != null) container.removeSubLevel(body, SubLevelRemovalReason.REMOVED);
      }
      var body = container.getSubLevel(car.chassis);
      if (body != null) container.removeSubLevel(body, SubLevelRemovalReason.REMOVED);
      data.vehicles.remove(car.chassis);
    }
    if (!fixture.bearing().isRemoved())
      container.removeSubLevel(fixture.bearing(), SubLevelRemovalReason.REMOVED);
    var anchor = level.getBlockEntity(fixture.rail().from);
    if (anchor instanceof CoasterAnchorpointBlockEntity coaster)
      coaster.removeAnchorPeerCurve(level, fixture.rail().to);
    data.rails.remove(fixture.rail());
    data.setDirty();
  }

  private static void force(net.minecraft.server.level.ServerLevel level, BlockPos origin) {
    int centerX = origin.getX() >> 4;
    int centerZ = origin.getZ() >> 4;
    for (int x = centerX - 1; x <= centerX + 1; x++)
      for (int z = centerZ - 1; z <= centerZ + 4; z++) level.setChunkForced(x, z, true);
  }
}
