package dev.fourthdimension.gametest;

import static dev.fourthdimension.gametest.EngineeredGameTests.*;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.integration.OptionalIntegrations;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

/**
 * Native presence, a real redstone pulse, automatic sequence advancement and observer crossings.
 */
@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class EngineeredSequenceGameTests {
  private static final class Lane {
    FoundationData.Rail rail;
    FoundationData.Vehicle car;
    Object controller, observer;
    BlockPos controllerPos, observerPos;
    Vector3d held;
    Pig rider;
    boolean observed, departed;
    int captureTick, maxObserverPower;
  }

  @GameTest(template = "empty", batch = "fourthdimension_engineered_sequence", timeoutTicks = 650)
  public static void heldPresenceRedstoneSequenceAndObserverCrossing(GameTestHelper helper) {
    if (!OptionalIntegrations.installed(OptionalIntegrations.ENGINEERED).mayInspect()) {
      System.out.println("FD_ENGINEERED_SEQUENCE absent/uninspected: scenario not exercised");
      helper.succeed();
      return;
    }
    var level = helper.getLevel();
    var container = SubLevelContainer.getContainer(level);
    var data = FoundationData.get(level);
    for (int x = -61; x <= -52; x++)
      for (int z = -58; z <= -51; z++) level.setChunkForced(x, z, true);
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000416"), "FDSequence"));
    player.setGameMode(GameType.CREATIVE);
    player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
    var lanes = new ArrayList<Lane>();
    for (var design : TrackDesign.values()) {
      var lane = new Lane();
      var from = new BlockPos(-950 + design.ordinal() * 24, 150, -900);
      var to = from.offset(0, 0, 64);
      for (var pos : List.of(from, to))
        level.setBlockAndUpdate(
            pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
      lane.rail = TrackPlacement.connect(level, from, to, design, player, player.getMainHandItem());
      lane.controllerPos = from.offset(-6, 0, 0);
      lane.observerPos = from.offset(-6, 0, 4);
      level.setBlockAndUpdate(
          lane.controllerPos,
          BuiltInRegistries.BLOCK
              .get(ResourceLocation.parse("coastersengineered:linear_motor_controller"))
              .defaultBlockState());
      level.setBlockAndUpdate(
          lane.observerPos,
          BuiltInRegistries.BLOCK
              .get(ResourceLocation.parse("coastersengineered:coaster_observer"))
              .defaultBlockState());
      lanes.add(lane);
    }
    helper.runAtTickTime(
        15,
        () -> {
          for (int i = 0; i < lanes.size(); i++) {
            var f = lanes.get(i);
            f.controller = configureMotor(helper, f.rail, f.controllerPos, 10, "FORWARD");
            var span =
                construct(
                    "span.TrackSpan",
                    List.of(construct("span.SpanSegment", f.rail.from, f.rail.to, 0d, .375d)));
            helper.assertTrue(
                (boolean)
                    invoke(
                        invoke(type("span.TrackSpanBehaviour"), "at", level, f.rail.from),
                        "setSpan",
                        span),
                "Short launch span rejected");
            invoke(
                f.controller,
                "setSteps",
                List.of(
                    construct(
                        "controller.MotorStep",
                        enumeration("controller.MotorStep$Kind", "STOP"),
                        enumeration("controller.MotorStep$Heading", "FORWARD"),
                        0),
                    construct(
                        "controller.MotorStep",
                        enumeration("controller.MotorStep$Kind", "LAUNCH"),
                        enumeration("controller.MotorStep$Heading", "FORWARD"),
                        10),
                    construct(
                        "controller.MotorStep",
                        enumeration("controller.MotorStep$Kind", "COAST"))));
            var edge = TrackGeometry.resolve(level, f.rail);
            double t = edge.from().equals(f.rail.from) ? .1875 : .8125;
            f.car =
                VehicleFactory.create(
                    level,
                    TrackDesign.values()[i],
                    new CoasterPathTrackFrame.GraphHit(
                        CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t));
            f.observer = level.getBlockEntity(f.observerPos);
            helper.assertTrue(f.observer != null, "Native observer missing");
            invoke(
                f.observer,
                "watch",
                invoke(
                    type("span.SpanSegment"),
                    "point",
                    edge,
                    edge.from().equals(f.rail.from) ? .28125d : .71875d));
          }
        });
    helper.runAtTickTime(
        100,
        () -> {
          for (var f : lanes) {
            helper.assertTrue(Boarding.ready(level, f.car), "Captured car not ready");
            f.held =
                new Vector3d(
                    CoasterCartResolve.cartCenterWorld(container.getSubLevel(f.car.chassis)));
            boolean presence = (boolean) invoke(f.controller, "isDriving");
            System.out.println(
                "FD_ENGINEERED_SEQUENCE held family="
                    + f.car.family
                    + " mode="
                    + invoke(f.controller, "commandMode")
                    + " nativePresent="
                    + presence);
            helper.assertTrue(
                presence, "Captured car disappeared from native controller: " + f.car.family);
            helper.assertTrue(
                invoke(f.controller, "commandMode").toString().equals("STOP"),
                "Unpulsed STOP advanced");
            level.setBlockAndUpdate(
                f.controllerPos.south(), Blocks.REDSTONE_BLOCK.defaultBlockState());
          }
        });
    helper.runAtTickTime(
        110, () -> lanes.forEach(f -> level.removeBlock(f.controllerPos.south(), false)));
    helper.runAtTickTime(
        180,
        () -> {
          for (var f : lanes) {
            double drift =
                CoasterCartResolve.cartCenterWorld(container.getSubLevel(f.car.chassis))
                    .distance(f.held);
            helper.assertTrue(
                drift < .1 && f.car.stopping && !f.car.restraints,
                "Pulse bypassed capture/restraints");
            helper.assertTrue(
                invoke(f.controller, "commandMode").toString().equals("LAUNCH")
                    && (boolean) invoke(f.controller, "isDriving"),
                "Held car caused premature sequence departure");
            System.out.println(
                "FD_ENGINEERED_SEQUENCE pulseHeld family="
                    + f.car.family
                    + " drift="
                    + drift
                    + " mode=LAUNCH nativePresent=true");
          }
        });
    helper.runAtTickTime(
        200,
        () -> {
          for (var f : lanes) {
            f.rider = EntityType.PIG.create(level);
            f.rider.setNoAi(true);
            f.rider.setPos(f.held.x, f.held.y, f.held.z);
            level.addFreshEntity(f.rider);
            var carrier = f.car.carriers.getFirst();
            SeatBlock.sitDown(
                level,
                container
                    .getSubLevel(carrier.body)
                    .getPlot()
                    .getCenterBlock()
                    .offset(carrier.seats.getFirst()),
                f.rider);
            helper.assertTrue(f.rider.isPassenger(), "Native boarding failed");
            VehicleOperations.restraints(level, f.car, true);
            if (f.car.family != Family.PAX) VehicleOperations.dispatch(level, f.car);
          }
        });
    int[] tick = {0};
    helper.onEachTick(
        () -> {
          tick[0]++;
          if (tick[0] < 201) return;
          for (var f : lanes) {
            int power = (int) invoke(f.observer, "getPower");
            if (power > 0) {
              helper.assertTrue(
                  level.getSignal(f.observerPos, net.minecraft.core.Direction.UP) == power,
                  "Native observer did not expose its redstone signal");
              f.observed = true;
              f.maxObserverPower = Math.max(f.maxObserverPower, power);
            }
            if (f.car.family == Family.PAX || f.departed) continue;
            if (invoke(f.controller, "commandMode").toString().equals("COAST")) {
              double travel =
                  CoasterCartResolve.cartCenterWorld(container.getSubLevel(f.car.chassis)).z
                      - f.held.z;
              helper.assertTrue(
                  travel > 12 && f.observed,
                  "Sequence advanced without real departure/observer crossing");
              f.departed = true;
              f.captureTick = tick[0];
              VehicleOperations.capture(level, f.car);
              System.out.println(
                  "FD_ENGINEERED_SEQUENCE departed family="
                      + f.car.family
                      + " travel="
                      + travel
                      + " observer=true maxSignal="
                      + f.maxObserverPower
                      + " mode=COAST tick="
                      + tick[0]);
            }
          }
        });
    helper.runAtTickTime(
        580,
        () -> {
          for (var f : lanes) {
            if (f.car.family == Family.PAX) {
              helper.assertTrue(
                  !f.departed
                      && f.car.stopping
                      && !f.observed
                      && CoasterCartResolve.cartCenterWorld(container.getSubLevel(f.car.chassis))
                              .distance(f.held)
                          < .1,
                  "External sequence moved held PAX car");
              helper.assertTrue(
                  invoke(f.controller, "commandMode").toString().equals("LAUNCH")
                      && (boolean) invoke(f.controller, "isDriving"),
                  "PAX presence disappeared");
            } else
              helper.assertTrue(
                  f.departed && tick[0] - f.captureTick > 80,
                  "Car failed native sequence departure");
            helper.assertTrue(
                Boarding.ready(level, f.car)
                    && f.car.restraints
                    && f.rider.isPassenger()
                    && f.rider.getHealth() == 10
                    && f.car.fault.isEmpty(),
                "Sequence/capture lost rider or readiness");
            VehicleOperations.restraints(level, f.car, false);
            f.rider.stopRiding();
            f.rider.discard();
            System.out.println(
                "FD_ENGINEERED_SEQUENCE complete family="
                    + f.car.family
                    + " departed="
                    + f.departed
                    + " observer="
                    + f.observed
                    + " ready=true unloaded=true");
          }
          for (var f : lanes) {
            for (var c : f.car.carriers)
              container.removeSubLevel(
                  container.getSubLevel(c.body), SubLevelRemovalReason.REMOVED);
            container.removeSubLevel(
                container.getSubLevel(f.car.chassis), SubLevelRemovalReason.REMOVED);
            data.vehicles.remove(f.car.chassis);
            data.rails.remove(f.rail);
            for (var pos : List.of(f.rail.from, f.rail.to, f.controllerPos, f.observerPos))
              level.removeBlock(pos, false);
          }
          for (int x = -61; x <= -52; x++)
            for (int z = -58; z <= -51; z++) level.setChunkForced(x, z, false);
          helper.succeed();
        });
  }
}
