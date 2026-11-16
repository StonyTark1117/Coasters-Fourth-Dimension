package dev.fourthdimension.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.shuttle.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftEndRef;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import java.util.stream.Collectors;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

/** One occupied train from its actual powered catch through ordinary native recovery. */
@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class RecoveryHandoverGameTests {
  private static final class Tow {
    ShuttleRoute route;
    UUID carriage;
    ServerSubLevel bearing;
    CreativeMotorBlockEntity motor;
    BlockPos lever;
    CoasterAnchorpointBlockEntity anchor;
    Vector3d start;
    boolean powered, sawStall, finished;
    double maxSpacing, maxAngle, maxSpeed;
    final List<Pig> riders = new ArrayList<>();
    final Map<UUID, UUID> seats = new HashMap<>();
  }

  @GameTest(template = "empty", batch = "paxRecoveryHandover", timeoutTicks = 900)
  public static void occupiedPaxHandsOverItsWinchThenTowsAndUnloads(GameTestHelper helper) {
    runRecovery(helper, false);
  }

  @GameTest(template = "empty", batch = "paxRecoveryStall", timeoutTicks = 2400)
  public static void occupiedPaxBrakesOnPoweredNativeChainStall(GameTestHelper helper) {
    runRecovery(helper, true);
  }

  @GameTest(template = "empty", timeoutTicks = 500)
  public static void brokenPaxConsistCanBeRetainedDetachedAndRebound(GameTestHelper helper) {
    var level = helper.getLevel();
    var origin = new BlockPos(-780, 180, -400);
    for (int x = (origin.getX() - 12) >> 4; x <= (origin.getX() + 12) >> 4; x++)
      for (int z = -30; z <= -17; z++) level.setChunkForced(x, z, true);
    var example = ShuttleExamples.create(level, origin, false);
    var container = SubLevelContainer.getContainer(level);
    var cars = example.cars();
    var lead = cars.getFirst();
    var winch = example.winch();
    var operatorProfile =
        new com.mojang.authlib.GameProfile(
            UUID.fromString("fdfdfdfd-0000-0000-0000-000000000408"),
            "FDBrokenConsist");
    var operator =
        new ServerPlayer(
            level.getServer(),
            level,
            operatorProfile,
            net.minecraft.server.level.ClientInformation.createDefault());
    operator.connection = FakePlayerFactory.get(level, operatorProfile).connection;
    operator.setGameMode(GameType.CREATIVE);
    var maintainer =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000409"),
                "FDConsistMaintainer"));
    maintainer.setGameMode(GameType.CREATIVE);
    Pig[] rider = {null};
    helper.runAtTickTime(
        80,
        () -> {
          ShuttleExamples.assemble(level, example);
          var lastCarrier = cars.getLast().carriers.getFirst();
          rider[0] = EntityType.PIG.create(level);
          rider[0].setNoAi(true);
          rider[0].setPos(origin.getX(), origin.getY(), origin.getZ());
          level.addFreshEntity(rider[0]);
          SeatBlock.sitDown(
              level,
              container.getSubLevel(lastCarrier.body).getPlot().getCenterBlock().offset(lastCarrier.seats.getFirst()),
              rider[0]);
          helper.assertTrue(rider[0].isPassenger(), "Broken-consist rider did not board");
          var leadCarrier = lead.carriers.getFirst();
          var leadSeat =
              container.getSubLevel(leadCarrier.body).getPlot().getCenterBlock().offset(leadCarrier.seats.getFirst());
          operator.setPos(leadSeat.getX(), leadSeat.getY(), leadSeat.getZ());
          SeatBlock.sitDown(level, leadSeat, operator);
          helper.assertTrue(operator.isPassenger(), "Broken-consist operator did not board");
          at(maintainer, container.getSubLevel(lead.chassis));
          edit(maintainer, lead, 11);
          winch.fail("Native consist separated; inspect before recovery");
          for (var car : cars)
            CoasterCartTrainLinkConstraint.clearAllLinks(
                level, (ServerSubLevel) container.getSubLevel(car.chassis));
          helper.assertTrue(
              cars.stream()
                  .allMatch(
                      car ->
                          CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(
                                      container.getSubLevel(car.chassis))
                                  .size()
                              == 1),
              "Broken-consist fixture retained a native link");
        });
    helper.runAtTickTime(
        110,
        () -> {
          for (var car : cars) {
            at(maintainer, container.getSubLevel(car.chassis));
            edit(maintainer, car, 14);
          }
        });
    helper.runAtTickTime(
        150,
        () -> {
          helper.assertTrue(
              cars.stream().allMatch(car -> RecoveryOperations.retained(car) && Boarding.ready(level, car)),
              "Separated PAX cars did not settle for retained handover");
          helper.assertTrue(operator.isPassenger(), "Broken-consist operator dismounted before handover");
          edit(operator, lead, 24);
          helper.assertTrue(
              winch.vehicle == null
                  && winch.cars.isEmpty()
                  && operator.isPassenger()
                  && rider[0].isPassenger(),
              "Broken-consist handover did not safely detach the winch");
          for (int i = 1; i < cars.size(); i++)
            helper.assertTrue(
                CoasterCartTrainLinkConstraint.linkCarts(
                    level,
                    (ServerSubLevel) container.getSubLevel(cars.get(i - 1).chassis),
                    (ServerSubLevel) container.getSubLevel(cars.get(i).chassis),
                    6),
                "Stationary separated PAX cars could not be natively recoupled");
          helper.assertTrue(
              CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(
                          container.getSubLevel(lead.chassis))
                      .size()
                  == 4,
              "Recovered PAX consist did not contain all four cars");
          edit(operator, lead, 15);
          winch.bind(lead);
          edit(operator, lead, 13);
          helper.assertTrue(
              winch.cars.size() == 4
                  && winch.phase == ShuttleWinchEntity.Phase.LIFTING
                  && operator.isPassenger()
                  && rider[0].isPassenger(),
              "Rebound PAX winch could not dispatch the occupied train");
          System.out.println("FD_PAX_BROKEN_CONSIST retained=true detached=true recoupled=true rebound=true dispatched=true occupied=true");
          helper.succeed();
        });
  }

  @GameTest(template = "empty", timeoutTicks = 300)
  public static void emptyDetachedPaxCarCanBeRetiredAfterHandover(GameTestHelper helper) {
    var level = helper.getLevel();
    var origin = new BlockPos(-840, 180, -400);
    for (int x = (origin.getX() - 12) >> 4; x <= (origin.getX() + 12) >> 4; x++)
      for (int z = -30; z <= -17; z++) level.setChunkForced(x, z, true);
    var example = ShuttleExamples.create(level, origin, false);
    var container = SubLevelContainer.getContainer(level);
    var cars = example.cars();
    var lead = cars.getFirst();
    var retired = cars.getLast();
    var bodies = new ArrayList<UUID>();
    bodies.add(retired.chassis);
    retired.carriers.forEach(carrier -> bodies.add(carrier.body));
    var operator =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000410"), "FDPaxRetire"));
    operator.setGameMode(GameType.CREATIVE);
    helper.runAtTickTime(
        80,
        () -> {
          ShuttleExamples.assemble(level, example);
          at(operator, container.getSubLevel(lead.chassis));
          edit(operator, lead, 11);
          reject(
              helper,
              () -> VehicleOperations.retireDetachedPax(level, retired),
              "Bound shuttle car retired without handover");
          for (var car : cars)
            CoasterCartTrainLinkConstraint.clearAllLinks(
                level, (ServerSubLevel) container.getSubLevel(car.chassis));
          for (var car : cars) {
            at(operator, container.getSubLevel(car.chassis));
            edit(operator, car, 14);
          }
        });
    helper.runAtTickTime(
        130,
        () -> {
          helper.assertTrue(
              cars.stream().allMatch(car -> Boarding.ready(level, car)),
              "Separated PAX cars did not settle before retirement");
          reject(
              helper,
              () -> VehicleOperations.retireDetachedPax(level, retired),
              "Retained car retired while the winch still owned it");
          at(operator, container.getSubLevel(lead.chassis));
          edit(operator, lead, 24);
          helper.assertTrue(
              example.winch().cars.isEmpty() && cars.stream().allMatch(car -> car.shuttleDetached),
              "Retirement handover did not record per-car detach authority");

        });
    long[] retiredAt = {-1};
    helper.onEachTick(
        () -> {
          if (helper.getTick() <= 130) return;
          if (retiredAt[0] < 0) {
            boolean ready = retired.maintenance && retired.stopping && Boarding.ready(level, retired);
            if (!ready && helper.getTick() < 290) return;
            helper.assertTrue(ready, "Detached car did not settle: maintenance=" + retired.maintenance
                + " stopping=" + retired.stopping + " speed=" + container.physicsSystem().getPipeline()
                .getLinearVelocity((ServerSubLevel) container.getSubLevel(retired.chassis), new Vector3d()).length()
                + " carriers=" + retired.carriers.stream().map(c -> "capture=" + c.capture + ",angle=" + c.angle + ",omega=" + c.omega).toList());
            at(operator, container.getSubLevel(retired.chassis));
            edit(operator, retired, 27);
            retiredAt[0] = helper.getTick();
            return;
          }
          if (helper.getTick() - retiredAt[0] < 5) return;
          helper.assertTrue(
              !FoundationData.get(level).vehicles.containsKey(retired.chassis)
                  && bodies.stream().allMatch(id -> container.getSubLevel(id) == null)
                  && cars.stream()
                      .limit(3)
                      .allMatch(car -> FoundationData.get(level).vehicles.containsKey(car.chassis)),
              "Retirement did not remove exactly the selected empty car and its carriers");
          System.out.println("FD_PAX_RETIRE detached=true empty=true removed=true otherCarsIntact=true");
          helper.succeed();
        });
  }

  private static void runRecovery(GameTestHelper helper, boolean stalled) {
    var level = helper.getLevel();
    var data = FoundationData.get(level);
    var container = SubLevelContainer.getContainer(level);
    var origin = new BlockPos(stalled ? -660 : -720, 180, -400);
    for (int x = (origin.getX() - 12) >> 4; x <= (origin.getX() + 12) >> 4; x++)
      for (int z = -30; z <= -17; z++) level.setChunkForced(x, z, true);
    var example = ShuttleExamples.create(level, origin, false);
    var winch = example.winch();
    var lead = example.cars().getFirst();
    var end = example.cars().getLast();
    var carIds = example.cars().stream().map(c -> c.chassis).collect(Collectors.toSet());
    var carrierIds =
        example.cars().stream()
            .flatMap(c -> c.carriers.stream())
            .map(c -> c.body)
            .collect(Collectors.toSet());
    var operator =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString(
                    stalled
                        ? "fdfdfdfd-0000-0000-0000-000000000407"
                        : "fdfdfdfd-0000-0000-0000-000000000406"),
                "FDOccupiedHandover"));
    operator.setGameMode(GameType.CREATIVE);
    var tow = new Tow();

    helper.runAtTickTime(
        80,
        () -> {
          ShuttleExamples.assemble(level, example);
          tow.route = ShuttleRoute.resolve(level, winch.routeStart, winch.routeEnd);
          for (var car : example.cars())
            for (var carrier : car.carriers)
              for (var seat : carrier.seats) {
                var rider = EntityType.PIG.create(level);
                rider.setNoAi(true);
                rider.setPos(origin.getX(), origin.getY(), origin.getZ());
                level.addFreshEntity(rider);
                SeatBlock.sitDown(
                    level,
                    container.getSubLevel(carrier.body).getPlot().getCenterBlock().offset(seat),
                    rider);
                helper.assertTrue(rider.isPassenger(), "PAX handover rider failed to board");
                tow.riders.add(rider);
                tow.seats.put(rider.getUUID(), rider.getVehicle().getUUID());
              }
          helper.assertTrue(tow.riders.size() == 16, "PAX handover did not fill every seat");
          at(operator, container.getSubLevel(lead.chassis));
          edit(operator, lead, 11);
          reject(helper, () -> edit(operator, lead, 24), "Unretained occupied handover accepted");
        });
    helper.runAtTickTime(
        100,
        () -> {
          winch.start();
          at(operator, container.getSubLevel(lead.chassis));
          reject(helper, () -> edit(operator, lead, 24), "Active occupied handover accepted");
        });
    helper.runAtTickTime(
        120,
        () -> {
          helper.assertTrue(
              winch.carriage != null && container.getSubLevel(winch.carriage) != null,
              "Occupied shuttle never allocated its physical lift carriage");
          tow.carriage = winch.carriage;
          at(operator, container.getSubLevel(lead.chassis));
          edit(operator, lead, 10);
          edit(operator, lead, 14);
        });
    helper.runAtTickTime(
        210,
        () -> {
          helper.assertTrue(
              example.cars().stream()
                      .allMatch(c -> Boarding.ready(level, c) && RecoveryOperations.retained(c))
                  && tow.riders.stream().allMatch(Pig::isPassenger),
              "Occupied interrupted shuttle did not settle for handover");
          at(operator, container.getSubLevel(lead.chassis));
          edit(operator, lead, 24);
          helper.assertTrue(
              winch.vehicle == null
                  && winch.carriage == null
                  && winch.cars.isEmpty()
                  && container.getSubLevel(tow.carriage) == null
                  && CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(
                          container.getSubLevel(end.chassis))
                      .equals(carIds),
              "Occupied handover damaged original train or kept the carriage");
          var hit =
              tow.route
                  .sample(tow.route.locate(container.getSubLevel(end.chassis)).distance() + 6)
                  .hit();
          var orientation =
              CoasterPathTrackFrame.quaternionFromTrackBasis(
                  CoasterPathTrackFrame.trackBasisAtHit(level, hit));
          // Initial recovery-bearing placement after handover. Never relocate the train or drive it
          // artificially.
          tow.bearing =
              CoasterCartSpawner.spawnMinimalContraption(
                  level,
                  CoasterCartLinkPlacement.plotOriginForBearingCenter(
                      CoasterPathTrackFrame.pathAnchorForSnappedBlock(level, hit), orientation),
                  orientation,
                  hit,
                  true);
          helper.assertTrue(tow.bearing != null, "Ordinary PAX recovery bearing failed to place");

          var station = tow.route.segments.get(1);
          var spike = tow.route.segments.get(2);
          var anchor = (CoasterAnchorpointBlockEntity) level.getBlockEntity(station.edge().from());
          tow.anchor = anchor;
          anchor.commitChainLift(
              Direction.Axis.X,
              ChainLiftEndRef.anchor(spike.edge().to()),
              new CoasterPathGraphSpans.SpanPath(
                  List.of(
                      new CoasterPathGraphSpans.PathSegment(station.edge(), 0, 1),
                      new CoasterPathGraphSpans.PathSegment(spike.edge(), 0, 1)),
                  station.length() + spike.length()));
          var clutch = origin.east();
          level.setBlockAndUpdate(
              clutch,
              AllBlocks.CLUTCH
                  .getDefaultState()
                  .setValue(BlockStateProperties.AXIS, Direction.Axis.X)
                  .setValue(BlockStateProperties.POWERED, true));
          tow.lever = clutch.above();
          level.setBlockAndUpdate(
              tow.lever,
              Blocks.LEVER
                  .defaultBlockState()
                  .setValue(BlockStateProperties.ATTACH_FACE, AttachFace.FLOOR)
                  .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                  .setValue(BlockStateProperties.POWERED, true));
          level.setBlockAndUpdate(
              origin.east(2),
              AllBlocks.CREATIVE_MOTOR
                  .getDefaultState()
                  .setValue(BlockStateProperties.FACING, Direction.WEST));
          tow.motor = (CreativeMotorBlockEntity) level.getBlockEntity(origin.east(2));
          tow.motor.generatedSpeed.setValue(8 * anchor.getChainLiftTravelSign());
        });
    helper.runAtTickTime(
        290,
        () -> {
          RecoveryOperations.select(at(operator, tow.bearing), tow.bearing);
          at(operator, container.getSubLevel(end.chassis));
          edit(operator, end, 20);
          helper.assertTrue(
              example.cars().stream()
                      .allMatch(c -> tow.bearing.getUniqueId().equals(c.recoveryCart))
                  && !data.vehicles.containsKey(tow.bearing.getUniqueId()),
              "Occupied handover did not become one ordinary recovery group");
          reject(helper, () -> winch.bind(lead), "Winch retook an attached recovery train");
          helper.assertTrue(
              Math.abs(tow.anchor.getSpeed()) < .001,
              "Initial stop lever did not cut recovery chain power");
        });
    helper.runAtTickTime(
        350,
        () -> {
          tow.start =
              new Vector3d(CoasterCartResolve.cartCenterWorld(container.getSubLevel(end.chassis)));
          at(operator, container.getSubLevel(end.chassis));
          edit(operator, end, 21);
          if (!stalled) {
            power(operator, tow.lever);
            tow.powered = true;
          }
        });
    if (stalled) {
      helper.runAtTickTime(
          550,
          () -> {
            helper.assertTrue(
                example.cars().stream().allMatch(c -> !c.stopping && c.fault.isEmpty())
                    && Math.abs(tow.anchor.getSpeed()) < .001,
                "A recovery waiting for power was incorrectly treated as a driven stall");
            power(operator, tow.lever);
            tow.powered = true;
          });
      helper.runAtTickTime(
          2200,
          () ->
              helper.assertTrue(
                  tow.sawStall, "Powered native chain stall was never diagnosed or braked"));
    }
    helper.onEachTick(
        () -> {
          if (tow.start == null || tow.finished) return;
          if (tow.sawStall) return;
          helper.assertTrue(
              tow.riders.stream()
                  .allMatch(
                      r ->
                          r.isPassenger()
                              && tow.seats.get(r.getUUID()).equals(r.getVehicle().getUUID())),
              "Native recovery changed an original passenger seat");
          if (stalled
              && tow.powered
              && example.cars().stream()
                  .anyMatch(c -> c.fault.startsWith("Native chain drive stalled"))) {
            tow.sawStall = true;
            var sample = tow.route.locate(tow.bearing);
            var chain =
                dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftSpanQuery.findAtGraphHit(
                    level, sample.hit(), 0);
            double travel =
                CoasterCartResolve.cartCenterWorld(container.getSubLevel(end.chassis))
                    .distance(tow.start);
            helper.assertTrue(
                chain != null
                    && Math.abs(chain.anchor().getSpeed()) == 8
                    && travel > 8
                    && example.cars().stream()
                        .allMatch(
                            c ->
                                c.stopping
                                    && RecoveryOperations.retained(c)
                                    && c.fault.startsWith("Native chain drive stalled")),
                "Driven stall did not brake every retained occupied car");
            System.out.println(
                "FD_RECOVERY_STALL travel="
                    + travel
                    + " bearingDistance="
                    + sample.distance()
                    + " nativeTarget="
                    + dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftDriveDirection
                        .signedChainBlocksPerSecond(chain.anchor(), chain.path(), 0, 256, 16)
                    + " maxSpacing="
                    + tow.maxSpacing
                    + " maxAngle="
                    + tow.maxAngle
                    + " maxSpeed="
                    + tow.maxSpeed
                    + " riders=16 originalSeats=true poweredStall=true allCarsBraked=true"
                    + " idlePowerCutNotFaulted=true");
            power(operator, tow.lever);
            helper.runAfterDelay(
                80,
                () -> {
                  helper.assertTrue(
                      example.cars().stream()
                              .allMatch(
                                  c ->
                                      c.stopping
                                          && Boarding.ready(level, c)
                                          && c.fault.startsWith("Native chain drive stalled"))
                          && Math.abs(tow.anchor.getSpeed()) < .001
                          && Math.abs(tow.motor.generatedSpeed.getValue()) == 8
                          && tow.riders.stream()
                              .allMatch(
                                  r ->
                                      r.isPassenger()
                                          && tow.seats
                                              .get(r.getUUID())
                                              .equals(r.getVehicle().getUUID())),
                      "Stalled train resumed, lost passengers or failed to settle after power cut");
                  at(operator, container.getSubLevel(end.chassis));
                  edit(operator, end, 23);
                  helper.assertTrue(
                      CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(
                                  container.getSubLevel(end.chassis))
                              .equals(carIds)
                          && carrierIds.stream().allMatch(id -> container.getSubLevel(id) != null)
                          && example.cars().stream()
                              .allMatch(c -> c.recoveryCart == null && c.stopping && c.maintenance)
                          && !data.vehicles.containsKey(tow.bearing.getUniqueId()),
                      "Stall recovery disconnection replaced components or original train links");
                  edit(operator, end, 12);
                  tow.finished = true;
                  tow.riders.forEach(Pig::stopRiding);
                  helper.assertTrue(
                      tow.riders.stream().noneMatch(Pig::isPassenger),
                      "Stall passengers failed to unload");
                  System.out.println(
                      "FD_RECOVERY_STALL originalCars=4 originalCarriers=8 originalSeats=16"
                          + " originalLinksAfterDetach=true noAutomaticResume=true powerCut=true"
                          + " motorRetainedAt8RPM=true normalUnloading=true");
                  helper.succeed();
                });
          }
          for (int i = 1; i < example.cars().size(); i++)
            tow.maxSpacing =
                Math.max(
                    tow.maxSpacing,
                    Math.abs(
                        CoasterCartResolve.cartCenterWorld(
                                    container.getSubLevel(example.cars().get(i - 1).chassis))
                                .distance(
                                    CoasterCartResolve.cartCenterWorld(
                                        container.getSubLevel(example.cars().get(i).chassis)))
                            - 6));
          tow.maxSpacing =
              Math.max(
                  tow.maxSpacing,
                  Math.abs(
                      CoasterCartResolve.cartCenterWorld(container.getSubLevel(end.chassis))
                              .distance(CoasterCartResolve.cartCenterWorld(tow.bearing))
                          - 6));
          for (var car : example.cars()) {
            for (var carrier : car.carriers)
              tow.maxAngle = Math.max(tow.maxAngle, Math.abs(carrier.angle));
            tow.maxSpeed =
                Math.max(
                    tow.maxSpeed,
                    container
                        .physicsSystem()
                        .getPipeline()
                        .getLinearVelocity(
                            (ServerSubLevel) container.getSubLevel(car.chassis), new Vector3d())
                        .length());
          }
        });
    helper.runAtTickTime(
        590,
        () -> {
          if (stalled) return;
          double travel =
              CoasterCartResolve.cartCenterWorld(container.getSubLevel(end.chassis))
                  .distance(tow.start);
          System.out.println(
              "FD_RECOVERY_HANDOVER travel="
                  + travel
                  + " maxSpacing="
                  + tow.maxSpacing
                  + " maxAngle="
                  + tow.maxAngle
                  + " maxSpeed="
                  + tow.maxSpeed
                  + " faults="
                  + example.cars().stream().map(c -> c.fault).toList());
          helper.assertTrue(
              travel > 3
                  && tow.maxSpacing < .15
                  && tow.maxAngle < .04
                  && tow.maxSpeed <= RecoveryOperations.MAX_SPEED + .01
                  && example.cars().stream()
                      .allMatch(c -> c.fault.isEmpty() && RecoveryOperations.retained(c))
                  && tow.riders.stream()
                      .allMatch(
                          r ->
                              r.isPassenger()
                                  && tow.seats.get(r.getUUID()).equals(r.getVehicle().getUUID()))
                  && winch.vehicle == null
                  && winch.carriage == null
                  && container.getSubLevel(tow.carriage) == null,
              "Original occupied PAX train failed recovery towing after physical handover");
          at(operator, container.getSubLevel(end.chassis));
          reject(
              helper, () -> edit(operator, end, 23), "Occupied moving handover train disconnected");
          edit(operator, end, 22);
          level
              .getBlockState(tow.lever)
              .useWithoutItem(
                  level,
                  operator,
                  new BlockHitResult(Vec3.atCenterOf(tow.lever), Direction.UP, tow.lever, false));
        });
    helper.runAtTickTime(
        670,
        () -> {
          if (stalled) return;
          helper.assertTrue(
              example.cars().stream().allMatch(c -> Boarding.ready(level, c))
                  && Math.abs(tow.anchor.getSpeed()) < .001
                  && Math.abs(tow.motor.generatedSpeed.getValue()) == 8
                  && tow.riders.stream()
                      .allMatch(
                          r ->
                              r.isPassenger()
                                  && tow.seats.get(r.getUUID()).equals(r.getVehicle().getUUID())),
              "Occupied handed-over PAX train failed to brake");
          at(operator, container.getSubLevel(end.chassis));
          edit(operator, end, 23);
          helper.assertTrue(
              CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(
                          container.getSubLevel(end.chassis))
                      .equals(carIds)
                  && carrierIds.stream().allMatch(id -> container.getSubLevel(id) != null)
                  && example.cars().stream()
                      .allMatch(c -> c.recoveryCart == null && c.stopping && c.maintenance)
                  && winch.vehicle == null
                  && winch.carriage == null
                  && !data.vehicles.containsKey(tow.bearing.getUniqueId()),
              "Recovery disconnection replaced the handed-over PAX train or released retention");
          edit(operator, end, 12);
          tow.finished = true;
          tow.riders.forEach(Pig::stopRiding);
          helper.assertTrue(
              tow.riders.stream().noneMatch(Pig::isPassenger),
              "Handed-over riders failed to unload");
          System.out.println(
              "FD_RECOVERY_HANDOVER occupiedRiders=16 originalCars=4 originalCarriers=8"
                  + " physicalCarriageRemoved=true replacementCarriage=false nativeChainDrive=true"
                  + " movingDisconnectRejected=true originalLinksAfterDetach=true"
                  + " originalPassengerSeats=true powerCutWithMotorRetained=true"
                  + " normalUnloading=true");
          helper.succeed();
        });
  }

  private static void power(ServerPlayer player, BlockPos lever) {
    var level = player.serverLevel();
    level
        .getBlockState(lever)
        .useWithoutItem(
            level, player, new BlockHitResult(Vec3.atCenterOf(lever), Direction.UP, lever, false));
  }

  private static ServerPlayer at(ServerPlayer player, SubLevel body) {
    var center = CoasterCartResolve.cartCenterWorld(body);
    player.setPos(center.x + 1, center.y + 1, center.z);
    return player;
  }

  private static void edit(ServerPlayer player, FoundationData.Vehicle car, int action) {
    EditorNetwork.apply(player, new EditorNetwork.Edit(car.chassis, car.revision, action, ""));
  }

  private static void reject(GameTestHelper helper, Runnable action, String message) {
    boolean rejected = false;
    try {
      action.run();
    } catch (IllegalArgumentException expected) {
      rejected = true;
    }
    helper.assertTrue(rejected, message);
  }
}
