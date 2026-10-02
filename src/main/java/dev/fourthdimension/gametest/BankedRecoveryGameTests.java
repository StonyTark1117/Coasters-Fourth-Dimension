package dev.fourthdimension.gametest;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
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

/**
 * Full-route occupied native recovery; physics evidence does not establish rendered mesh clearance.
 */
@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class BankedRecoveryGameTests {
  private static final class Case {
    final TrackDesign design;
    final BankedRecoveryExamples.Example example;
    final List<Pig> riders = new ArrayList<>();
    final Map<UUID, UUID> mounts = new HashMap<>();
    final Set<Integer> visited = new HashSet<>();
    double maxSpacing, maxAngle, maxSpeed, maxBank, maxAnchorOffset, maxFrameError;
    long sustainedBankTicks;
    final List<dev.fourthdimension.physics.RotationProfile> profiles;
    boolean started, braked, finished;
    long brakeTick;

    Case(TrackDesign design, BankedRecoveryExamples.Example example) {
      this.design = design;
      this.example = example;
      profiles = example.rails().stream().map(r -> r.profile).toList();
    }
  }

  @GameTest(template = "empty", batch = "recoveryBank", timeoutTicks = 9000)
  public static void occupiedRecoveryTraversesQuarterBendBankAndLevelRunout(GameTestHelper helper) {
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    var data = FoundationData.get(level);
    for (int x = -57; x <= -48; x++)
      for (int z = 30; z <= 56; z++) level.setChunkForced(x, z, true);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000419"), "FDBankedRecovery"));
    player.setGameMode(GameType.CREATIVE);
    var meshTrace = new RecoveryMeshTrace("recovery-bank-mesh-trace.jsonl");
    var cases = new ArrayList<Case>();
    for (var design : TrackDesign.values())
      cases.add(
          new Case(
              design,
              BankedRecoveryExamples.create(
                  level, new BlockPos(-900, 240, 500 + 80 * design.ordinal()), design)));
    for (var c : cases) {
      helper.assertTrue(
          !c.design.family.driven()
              || c.profiles.get(2).keys().stream()
                  .anyMatch(k -> Math.abs(k.angle() - 2 * Math.PI) < 1e-9),
          "Controlled recovery fixture erased its nonzero rotation profile");
    }
    RecoveryRouteFrames.write(
        level, "recovery-bank-native-frames.json", cases.stream().map(c -> c.example).toList());
    helper.runAtTickTime(
        80,
        () -> {
          for (var c : cases) {
            var e = c.example.recovery();
            var car = e.car();
            helper.assertTrue(
                Boarding.ready(level, car), "Banked recovery initial alignment " + c.design);
            e.motor()
                .generatedSpeed
                .setValue(
                    8
                        * ((CoasterAnchorpointBlockEntity)
                                level.getBlockEntity(c.example.anchors().getFirst()))
                            .getChainLiftTravelSign());
            for (var carrier : car.carriers)
              for (var offset : carrier.seats) {
                var body = container.getSubLevel(carrier.body);
                var pig = EntityType.PIG.create(level);
                pig.setNoAi(true);
                pig.setPos(
                    c.example.anchors().getFirst().getX(),
                    240,
                    c.example.anchors().getFirst().getZ());
                level.addFreshEntity(pig);
                SeatBlock.sitDown(level, body.getPlot().getCenterBlock().offset(offset), pig);
                helper.assertTrue(pig.isPassenger(), "Native recovery seat mount failed");
                c.riders.add(pig);
                c.mounts.put(pig.getUUID(), pig.getVehicle().getUUID());
              }
            var center = CoasterCartResolve.cartCenterWorld(e.bearing());
            player.setPos(center.x + 1, center.y + 1, center.z);
            player.setItemInHand(
                InteractionHand.MAIN_HAND, TrackItems.ROTATION_TOOL.get().getDefaultInstance());
            player.setShiftKeyDown(true);
            var result =
                player
                    .getMainHandItem()
                    .useOn(
                        new UseOnContext(
                            player,
                            InteractionHand.MAIN_HAND,
                            new BlockHitResult(
                                new Vec3(center.x, center.y, center.z),
                                Direction.UP,
                                e.bearing().getPlot().getCenterBlock(),
                                false)));
            player.setShiftKeyDown(false);
            helper.assertTrue(result.consumesAction(), "Recovery selection failed " + c.design);
            position(player, container.getSubLevel(car.chassis));
            edit(player, car, 11);
            edit(player, car, 14);
            edit(player, car, 20);
          }
        });
    helper.runAtTickTime(
        140,
        () -> {
          for (var c : cases) {
            var e = c.example.recovery();
            position(player, container.getSubLevel(e.car().chassis));
            edit(player, e.car(), 21);
            toggle(player, e.powerLever());
            c.started = true;
          }
        });
    helper.onEachTick(
        () -> {
          for (var c : cases) {
            if (!c.started || c.finished) continue;
            if (helper.getTick() % RecoveryMeshTrace.SAMPLE_INTERVAL_TICKS == 0)
              meshTrace.sample(level, c.example, helper.getTick());
            var e = c.example.recovery();
            var car = e.car();
            var body = container.getSubLevel(car.chassis);
            helper.assertTrue(
                body != null && car.fault.isEmpty(),
                "Recovery fault " + c.design + ": " + car.fault);
            helper.assertTrue(
                car.variant.equals(c.design.variant)
                    && RecoveryOperations.retained(car)
                    && !data.vehicles.containsKey(e.bearing().getUniqueId()),
                "Recovery family/ordinary role changed " + c.design);
            helper.assertTrue(
                c.riders.stream()
                    .allMatch(
                        p ->
                            p.isPassenger()
                                && c.mounts.get(p.getUUID()).equals(p.getVehicle().getUUID())),
                "Recovery changed original native passenger seats " + c.design);
            helper.assertTrue(
                CoasterCartTrainLinkConstraint.areLinkedCarts(body, e.bearing()),
                "Recovery coupling lost " + c.design);
            var center = CoasterCartResolve.cartCenterWorld(body);
            var hit =
                CoasterPathTrackFrame.nearestGraphHit(
                    level,
                    CoasterPathGraphManager.get(level),
                    new Vec3(center.x, center.y, center.z),
                    2);
            helper.assertTrue(hit != null, "Recovery lost native track support " + c.design);
            int section = -1;
            for (int i = 0; i < c.example.rails().size(); i++)
              if (c.example.rails().get(i).matches(hit.edge().from(), hit.edge().to())) section = i;
            helper.assertTrue(
                section >= 0 && RecoveryOperations.ordinarySupport(level, e.bearing(), car),
                "Recovery left matching route " + c.design);
            c.visited.add(section);
            var basis = CoasterPathTrackFrame.trackBasisAtHit(level, hit);
            if (section == 2) {
              helper.assertTrue(
                  Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, basis.up().y)))) > 29,
                  "Sustained native bank was lost " + c.design);
              c.sustainedBankTicks++;
            }
            for (var member : List.of(body, e.bearing())) {
              var memberCenter = CoasterCartResolve.cartCenterWorld(member);
              var memberHit =
                  CoasterPathTrackFrame.nearestGraphHit(
                      level,
                      CoasterPathGraphManager.get(level),
                      new Vec3(memberCenter.x, memberCenter.y, memberCenter.z),
                      2);
              helper.assertTrue(memberHit != null, "Recovery member lost native support");
              var anchor = CoasterPathTrackFrame.pathAnchorForSnappedBlock(level, memberHit);
              c.maxAnchorOffset =
                  Math.max(
                      c.maxAnchorOffset,
                      memberCenter.distance(new org.joml.Vector3d(anchor.x, anchor.y, anchor.z)));
              var expected =
                  CoasterPathTrackFrame.quaternionFromTrackBasis(
                      CoasterPathTrackFrame.trackBasisAtHit(level, memberHit));
              double dot =
                  Math.abs(
                      new org.joml.Quaterniond(member.logicalPose().orientation())
                          .normalize()
                          .dot(expected));
              c.maxFrameError =
                  Math.max(
                      c.maxFrameError,
                      Math.toDegrees(2 * Math.acos(Math.max(-1, Math.min(1, dot)))));
            }
            c.maxBank =
                Math.max(
                    c.maxBank, Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, basis.up().y)))));
            c.maxSpacing =
                Math.max(
                    c.maxSpacing,
                    Math.abs(
                        center.distance(CoasterCartResolve.cartCenterWorld(e.bearing()))
                            - c.design.axleSpacing()));
            c.maxAngle =
                Math.max(
                    c.maxAngle,
                    car.carriers.stream()
                        .mapToDouble(group -> Math.abs(group.angle))
                        .max()
                        .orElse(0));
            c.maxSpeed =
                Math.max(
                    c.maxSpeed,
                    container
                        .physicsSystem()
                        .getPipeline()
                        .getLinearVelocity(
                            (dev.ryanhcode.sable.sublevel.ServerSubLevel) body,
                            new org.joml.Vector3d())
                        .length());
            if (helper.getTick() % 400 == 0)
              System.out.println(
                  "FD_RECOVERY_BANK_PROGRESS family="
                      + c.design
                      + " tick="
                      + helper.getTick()
                      + " section="
                      + section
                      + " t="
                      + hit.t()
                      + " speed="
                      + container
                          .physicsSystem()
                          .getPipeline()
                          .getLinearVelocity(
                              (dev.ryanhcode.sable.sublevel.ServerSubLevel) body,
                              new org.joml.Vector3d())
                          .length()
                      + " fault="
                      + car.fault);
            if (!c.braked
                && section == 4
                && TrackGeometry.distance(level, hit.edge(), c.example.rails().get(4), hit.t())
                    > 5) {
              helper.assertTrue(
                  c.visited.size() == 5
                      && c.sustainedBankTicks > 100
                      && c.example.rails().stream().map(r -> r.profile).toList().equals(c.profiles)
                      && c.maxAnchorOffset < .1
                      && c.maxFrameError < 5
                      && c.maxBank > 29
                      && c.maxBank < 31
                      && c.maxSpacing < .15
                      && c.maxAngle < .04
                      && c.maxSpeed <= RecoveryOperations.MAX_SPEED + .02,
                  "Banked recovery route/retention metrics failed "
                      + c.design
                      + " spacing="
                      + c.maxSpacing
                      + " angle="
                      + c.maxAngle
                      + " offset="
                      + c.maxAnchorOffset
                      + " frame="
                      + c.maxFrameError);
              position(player, body);
              edit(player, car, 22);
              toggle(player, e.powerLever());
              c.braked = true;
              c.brakeTick = helper.getTick();
            }
            if (c.braked && helper.getTick() - c.brakeTick >= 80) {
              helper.assertTrue(
                  Boarding.ready(level, car), "Banked recovery failed normal brake " + c.design);
              helper.assertTrue(
                  ((CoasterAnchorpointBlockEntity)
                                  level.getBlockEntity(c.example.anchors().getFirst()))
                              .getSpeed()
                          == 0
                      && Math.abs(e.motor().generatedSpeed.getValue()) == 8,
                  "Banked recovery power cut failed");
              position(player, body);
              edit(player, car, 23);
              helper.assertTrue(
                  car.recoveryCart == null && car.maintenance && car.stopping,
                  "Banked recovery detach changed retention");
              edit(player, car, 12);
              c.riders.forEach(Pig::stopRiding);
              helper.assertTrue(
                  c.riders.stream().noneMatch(Pig::isPassenger),
                  "Banked recovery unloading failed");
              meshTrace.complete(c.example, helper.getTick());
              c.finished = true;
              System.out.println(
                  "FD_RECOVERY_BANK family="
                      + c.design
                      + " sections="
                      + c.visited.size()
                      + " maxBank="
                      + c.maxBank
                      + " riders="
                      + c.riders.size()
                      + " maxSpacing="
                      + c.maxSpacing
                      + " maxAngle="
                      + c.maxAngle
                      + " maxSpeed="
                      + c.maxSpeed
                      + " maxAnchorOffset="
                      + c.maxAnchorOffset
                      + " maxFrameErrorDegrees="
                      + c.maxFrameError
                      + " sustainedBankTicks="
                      + c.sustainedBankTicks
                      + " profilesUnchanged=true normalBrakePowerCutDetachUnload=true");
            }
          }
          if (cases.stream().allMatch(c -> c.finished)) {
            // Remove only this fixture's entities and sublevels after preserving the evidence.
            for (var c : cases) {
              c.riders.forEach(Pig::discard);
              var car = c.example.recovery().car();
              for (var carrier : car.carriers)
                container.removeSubLevel(
                    container.getSubLevel(carrier.body),
                    dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason.REMOVED);
              container.removeSubLevel(
                  container.getSubLevel(car.chassis),
                  dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason.REMOVED);
              container.removeSubLevel(
                  c.example.recovery().bearing(),
                  dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason.REMOVED);
              data.vehicles.remove(car.chassis);
              for (var rail : c.example.rails()) {
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

  private static void position(ServerPlayer player, dev.ryanhcode.sable.sublevel.SubLevel body) {
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
