package dev.fourthdimension.gametest;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.*;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;
import org.joml.Quaterniond;
import org.joml.Vector3d;

/**
 * Every original car must complete the bend and bank; preserving one leading axle is insufficient.
 */
@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class BankedRecoveryTrainGameTests {
  private static final class Train {
    final TrackDesign design;
    final BankedRecoveryExamples.Example example;
    final Map<UUID, Set<Integer>> visited = new HashMap<>();
    final Map<UUID, Long> bankTicks = new HashMap<>();
    final Map<UUID, List<UUID>> carrierIds = new HashMap<>();
    final Map<UUID, UUID> mounts = new HashMap<>();
    final List<Pig> riders = new ArrayList<>();
    final List<dev.fourthdimension.physics.RotationProfile> profiles;
    double maxSpacing, maxAngle, maxSpeed, maxAnchorOffset, maxFrameError;
    boolean started, braked, finished;
    long brakeTick;

    Train(TrackDesign design, BankedRecoveryExamples.Example example) {
      this.design = design;
      this.example = example;
      profiles = example.rails().stream().map(r -> r.profile).toList();
      for (var car : example.cars()) {
        visited.put(car.chassis, new HashSet<>());
        bankTicks.put(car.chassis, 0L);
        carrierIds.put(car.chassis, car.carriers.stream().map(c -> c.body).toList());
      }
    }
  }

  @GameTest(template = "empty", batch = "recoveryBankTrain", timeoutTicks = 11000)
  public static void occupiedOriginalTrainsTraverseBendAndBankBeforeDisconnect(
      GameTestHelper helper) {
    var level = helper.getLevel();
    var data = FoundationData.get(level);
    var container = SubLevelContainer.getContainer(level);
    for (int x = -42; x <= -31; x++)
      for (int z = 30; z <= 47; z++) level.setChunkForced(x, z, true);
    var operator =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000420"), "FDBankedTrain"));
    operator.setGameMode(GameType.CREATIVE);
    var trains =
        List.of(
            new Train(
                TrackDesign.ARROW,
                BankedRecoveryExamples.createTrain(
                    level, new BlockPos(-650, 240, 500), TrackDesign.ARROW)),
            new Train(
                TrackDesign.PAX,
                BankedRecoveryExamples.createTrain(
                    level, new BlockPos(-650, 240, 660), TrackDesign.PAX)));
    for (var train : trains) BankedRecoveryExamples.queueAssembly(level, train.example);
    for (var train : trains)
      helper.assertTrue(
          !train.design.family.driven()
              || train.profiles.get(2).keys().stream()
                  .anyMatch(k -> Math.abs(k.angle() - 2 * Math.PI) < 1e-9),
          "Train fixture erased its nonzero Arrow profile");
    RecoveryRouteFrames.write(
        level,
        "recovery-bank-train-native-frames.json",
        trains.stream().map(t -> t.example).toList());
    helper.runAtTickTime(
        100,
        () -> {
          for (var train : trains) {
            var example = train.example;
            var e = example.recovery();
            for (int i = 1; i < example.cars().size(); i++)
              helper.assertTrue(
                  CoasterCartTrainLinkConstraint.areLinkedCarts(
                      container.getSubLevel(example.cars().get(i - 1).chassis),
                      container.getSubLevel(example.cars().get(i).chassis)),
                  "Queued native train assembly failed " + train.design);
            for (var car : example.cars())
              for (var carrier : car.carriers)
                for (var offset : carrier.seats) {
                  var pig = EntityType.PIG.create(level);
                  pig.setNoAi(true);
                  pig.setPos(
                      example.anchors().getFirst().getX(),
                      240,
                      example.anchors().getFirst().getZ());
                  level.addFreshEntity(pig);
                  SeatBlock.sitDown(
                      level,
                      container.getSubLevel(carrier.body).getPlot().getCenterBlock().offset(offset),
                      pig);
                  helper.assertTrue(
                      pig.isPassenger(), "Banked train native passenger failed to board");
                  train.riders.add(pig);
                  train.mounts.put(pig.getUUID(), pig.getVehicle().getUUID());
                }
            e.motor()
                .generatedSpeed
                .setValue(
                    8
                        * ((CoasterAnchorpointBlockEntity)
                                level.getBlockEntity(example.anchors().getFirst()))
                            .getChainLiftTravelSign());
            var center = CoasterCartResolve.cartCenterWorld(e.bearing());
            operator.setPos(center.x + 1, center.y + 1, center.z);
            operator.setItemInHand(
                InteractionHand.MAIN_HAND, TrackItems.ROTATION_TOOL.get().getDefaultInstance());
            operator.setShiftKeyDown(true);
            var result =
                operator
                    .getMainHandItem()
                    .useOn(
                        new UseOnContext(
                            operator,
                            InteractionHand.MAIN_HAND,
                            new BlockHitResult(
                                new Vec3(center.x, center.y, center.z),
                                Direction.UP,
                                e.bearing().getPlot().getCenterBlock(),
                                false)));
            operator.setShiftKeyDown(false);
            helper.assertTrue(result.consumesAction(), "Train ordinary bearing selection failed");
            position(operator, container.getSubLevel(e.car().chassis));
            edit(operator, e.car(), 11);
            edit(operator, e.car(), 14);
            edit(operator, e.car(), 20);
          }
        });
    helper.runAtTickTime(
        160,
        () -> {
          for (var train : trains) {
            var e = train.example.recovery();
            helper.assertTrue(
                train.example.cars().stream().allMatch(c -> Boarding.ready(level, c)),
                "Original banked train failed to settle");
            position(operator, container.getSubLevel(e.car().chassis));
            edit(operator, e.car(), 21);
            toggle(operator, e.powerLever());
            train.started = true;
          }
        });
    helper.onEachTick(
        () -> {
          for (var train : trains) {
            if (!train.started || train.finished) continue;
            var e = train.example.recovery();
            var cars = train.example.cars();
            helper.assertTrue(
                train.riders.stream()
                    .allMatch(
                        p ->
                            p.isPassenger()
                                && train.mounts.get(p.getUUID()).equals(p.getVehicle().getUUID())),
                "Banked train changed original native passenger seats " + train.design);
            helper.assertTrue(
                train.example.rails().stream().map(r -> r.profile).toList().equals(train.profiles),
                "Recovery changed original train profiles");
            boolean allOnLevelRunout = true;
            var bodies = new ArrayList<SubLevel>();
            for (var car : cars) {
              var body = container.getSubLevel(car.chassis);
              helper.assertTrue(
                  body != null && car.fault.isEmpty(),
                  "Banked train recovery fault " + train.design + ": " + car.fault);
              helper.assertTrue(
                  car.variant.equals(train.design.variant)
                      && RecoveryOperations.retained(car)
                      && e.bearing().getUniqueId().equals(car.recoveryCart)
                      && car.carriers.stream()
                          .map(c -> c.body)
                          .toList()
                          .equals(train.carrierIds.get(car.chassis)),
                  "Banked train replaced components or changed retention/family");
              var hit = hit(level, body);
              helper.assertTrue(
                  hit != null && RecoveryOperations.ordinarySupport(level, e.bearing(), car),
                  "Banked train lost matching support");
              int section = section(train.example, hit);
              helper.assertTrue(section >= 0, "Banked train left original route");
              train.visited.get(car.chassis).add(section);
              var basis = CoasterPathTrackFrame.trackBasisAtHit(level, hit);
              if (section == 2) {
                double bank = Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, basis.up().y))));
                helper.assertTrue(
                    bank > 29 && bank < 31, "Original train missed sustained native bank");
                train.bankTicks.merge(car.chassis, 1L, Long::sum);
              }
              allOnLevelRunout &=
                  section == 4
                      && TrackGeometry.distance(
                              level, hit.edge(), train.example.rails().get(4), hit.t())
                          > 5;
              train.maxAngle =
                  Math.max(
                      train.maxAngle,
                      car.carriers.stream().mapToDouble(c -> Math.abs(c.angle)).max().orElse(0));
              bodies.add(body);
            }
            helper.assertTrue(
                !data.vehicles.containsKey(e.bearing().getUniqueId()),
                "Train ordinary bearing acquired 4D state");
            bodies.add(e.bearing());
            for (int i = 1; i < bodies.size(); i++) {
              helper.assertTrue(
                  CoasterCartTrainLinkConstraint.areLinkedCarts(bodies.get(i - 1), bodies.get(i)),
                  "Banked train original/native recovery coupling lost");
              train.maxSpacing =
                  Math.max(
                      train.maxSpacing,
                      Math.abs(
                          CoasterCartResolve.cartCenterWorld(bodies.get(i - 1))
                                  .distance(CoasterCartResolve.cartCenterWorld(bodies.get(i)))
                              - train.design.axleSpacing()));
            }
            for (var body : bodies) {
              var memberHit = hit(level, body);
              helper.assertTrue(
                  memberHit != null && section(train.example, memberHit) >= 0,
                  "Train member lost native route support");
              var center = CoasterCartResolve.cartCenterWorld(body);
              var anchor = CoasterPathTrackFrame.pathAnchorForSnappedBlock(level, memberHit);
              train.maxAnchorOffset =
                  Math.max(
                      train.maxAnchorOffset,
                      center.distance(new Vector3d(anchor.x, anchor.y, anchor.z)));
              var expected =
                  CoasterPathTrackFrame.quaternionFromTrackBasis(
                      CoasterPathTrackFrame.trackBasisAtHit(level, memberHit));
              double dot =
                  Math.abs(
                      new Quaterniond(body.logicalPose().orientation()).normalize().dot(expected));
              train.maxFrameError =
                  Math.max(
                      train.maxFrameError,
                      Math.toDegrees(2 * Math.acos(Math.max(-1, Math.min(1, dot)))));
              train.maxSpeed =
                  Math.max(
                      train.maxSpeed,
                      container
                          .physicsSystem()
                          .getPipeline()
                          .getLinearVelocity((ServerSubLevel) body, new Vector3d())
                          .length());
            }
            if (helper.getTick() % 600 == 0) {
              System.out.println(
                  "FD_RECOVERY_BANK_TRAIN_PROGRESS family="
                      + train.design
                      + " tick="
                      + helper.getTick()
                      + " sections="
                      + cars.stream()
                          .map(
                              c ->
                                  section(
                                      train.example, hit(level, container.getSubLevel(c.chassis))))
                          .toList()
                      + " maxSpacing="
                      + train.maxSpacing
                      + " maxAngle="
                      + train.maxAngle
                      + " maxAnchorOffset="
                      + train.maxAnchorOffset
                      + " maxFrameError="
                      + train.maxFrameError);
            }
            if (!train.braked && allOnLevelRunout) {
              helper.assertTrue(
                  train.visited.values().stream().allMatch(v -> v.size() == 5)
                      && train.bankTicks.values().stream().allMatch(t -> t > 100)
                      && train.maxSpacing < .15
                      && train.maxAngle < .04
                      && train.maxSpeed < RecoveryOperations.MAX_SPEED + .02
                      && train.maxAnchorOffset < .1
                      && train.maxFrameError < 5,
                  "Banked train geometry/retention metrics failed " + train.design);
              position(operator, container.getSubLevel(e.car().chassis));
              edit(operator, e.car(), 22);
              toggle(operator, e.powerLever());
              train.braked = true;
              train.brakeTick = helper.getTick();
            }
            if (train.braked && helper.getTick() - train.brakeTick >= 100) {
              helper.assertTrue(
                  cars.stream().allMatch(c -> Boarding.ready(level, c)),
                  "Whole banked train failed normal braking");
              helper.assertTrue(
                  ((CoasterAnchorpointBlockEntity)
                                  level.getBlockEntity(train.example.anchors().getFirst()))
                              .getSpeed()
                          == 0
                      && Math.abs(e.motor().generatedSpeed.getValue()) == 8,
                  "Banked train failed normal external power cut");
              position(operator, container.getSubLevel(e.car().chassis));
              edit(operator, e.car(), 23);
              var linked =
                  CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(
                      container.getSubLevel(e.car().chassis));
              helper.assertTrue(
                  linked.equals(
                          cars.stream()
                              .map(c -> c.chassis)
                              .collect(java.util.stream.Collectors.toSet()))
                      && cars.stream()
                          .allMatch(
                              c ->
                                  c.recoveryCart == null
                                      && c.maintenance
                                      && c.stopping
                                      && c.restraints)
                      && !CoasterCartTrainLinkConstraint.areLinkedCarts(
                          container.getSubLevel(e.car().chassis), e.bearing()),
                  "Banked recovery disconnect damaged original train links or retention");
              edit(operator, e.car(), 12);
              train.riders.forEach(Pig::stopRiding);
              helper.assertTrue(
                  train.riders.stream().noneMatch(Pig::isPassenger),
                  "Banked original train failed native unloading");
              train.finished = true;
              System.out.println(
                  "FD_RECOVERY_BANK_TRAIN family="
                      + train.design
                      + " cars="
                      + cars.size()
                      + " riders="
                      + train.riders.size()
                      + " allCarsVisitedSections=5 minSustainedBankTicks="
                      + train.bankTicks.values().stream().mapToLong(Long::longValue).min().orElse(0)
                      + " maxSpacing="
                      + train.maxSpacing
                      + " maxAngle="
                      + train.maxAngle
                      + " maxSpeed="
                      + train.maxSpeed
                      + " maxAnchorOffset="
                      + train.maxAnchorOffset
                      + " maxFrameErrorDegrees="
                      + train.maxFrameError
                      + " originalSeats=true originalCarriers=true originalLinksAfterDetach=true"
                      + " profilesUnchanged=true normalBrakePowerCutDetachUnload=true");
            }
          }
          if (trains.stream().allMatch(t -> t.finished)) {
            for (var train : trains) {
              train.riders.forEach(Pig::discard);
              for (var car : train.example.cars()) {
                for (var carrier : car.carriers)
                  container.removeSubLevel(
                      container.getSubLevel(carrier.body), SubLevelRemovalReason.REMOVED);
                container.removeSubLevel(
                    container.getSubLevel(car.chassis), SubLevelRemovalReason.REMOVED);
                data.vehicles.remove(car.chassis);
              }
              container.removeSubLevel(
                  train.example.recovery().bearing(), SubLevelRemovalReason.REMOVED);
              for (var rail : train.example.rails()) {
                ((CoasterAnchorpointBlockEntity) level.getBlockEntity(rail.from))
                    .removeAnchorPeerCurve(level, rail.to);
                data.rails.remove(rail);
              }
            }
            data.setDirty();
            helper.succeed();
          }
        });
  }

  private static int section(
      BankedRecoveryExamples.Example example, CoasterPathTrackFrame.GraphHit hit) {
    if (hit == null) return -1;
    for (int i = 0; i < example.rails().size(); i++)
      if (example.rails().get(i).matches(hit.edge().from(), hit.edge().to())) return i;
    return -1;
  }

  private static CoasterPathTrackFrame.GraphHit hit(
      net.minecraft.server.level.ServerLevel level, SubLevel body) {
    var center = CoasterCartResolve.cartCenterWorld(body);
    return CoasterPathTrackFrame.nearestGraphHit(
        level, CoasterPathGraphManager.get(level), new Vec3(center.x, center.y, center.z), 2);
  }

  private static void position(ServerPlayer player, SubLevel body) {
    var center = CoasterCartResolve.cartCenterWorld(body);
    player.setPos(center.x + 1, center.y + 1, center.z);
  }

  private static void edit(ServerPlayer player, FoundationData.Vehicle car, int action) {
    EditorNetwork.apply(player, new EditorNetwork.Edit(car.chassis, car.revision, action, ""));
  }

  private static void toggle(ServerPlayer player, BlockPos lever) {
    player
        .serverLevel()
        .getBlockState(lever)
        .useWithoutItem(
            player.serverLevel(),
            player,
            new BlockHitResult(Vec3.atCenterOf(lever), Direction.UP, lever, false));
  }
}
