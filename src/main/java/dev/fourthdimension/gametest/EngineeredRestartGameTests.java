package dev.fourthdimension.gametest;

import static dev.fourthdimension.gametest.EngineeredGameTests.*;

import com.google.gson.*;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.integration.OptionalIntegrations;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
 * Opt-in two-process test. The dedicated runner preserves the world between prepare and restore.
 */
@net.neoforged.fml.common.EventBusSubscriber(modid = "fourthdimension")
@GameTestHolder("fourthdimension")
@PrefixGameTestTemplate(false)
public final class EngineeredRestartGameTests {
  private static final Path RECORD = Path.of("engineered-restart-fixture.json");

  @net.neoforged.bus.api.SubscribeEvent(
      priority = net.neoforged.bus.api.EventPriority.LOWEST,
      receiveCanceled = true)
  public static void traceRestoreMount(net.neoforged.neoforge.event.entity.EntityMountEvent event) {
    if (!System.getProperty("fourthdimension.engineeredRestart", "").startsWith("restore")
        || !event.isMounting()) return;
    var seat = event.getEntityBeingMounted();
    var binding = Boarding.at(seat.level(), seat.blockPosition());
    if (binding != null)
      System.out.println(
          "FD_ENGINEERED_RESTART mount family="
              + binding.vehicle().family
              + " rider="
              + event.getEntityMounting().getUUID()
              + " canceled="
              + event.isCanceled()
              + " restraints="
              + binding.vehicle().restraints
              + " savedOccupant="
              + binding.carrier().occupants.contains(event.getEntityMounting().getUUID()));
  }

  private static final class Lane {
    FoundationData.Rail rail;
    FoundationData.Vehicle car;
    Object controller, observer;
    BlockPos controllerPos, observerPos;
    Pig rider;
    Vector3d held;
    boolean observed, departed;
  }

  @GameTest(template = "empty", batch = "fourthdimension_engineered_restart", timeoutTicks = 650)
  public static void occupiedProgramColdRestart(GameTestHelper helper) {
    String phase = System.getProperty("fourthdimension.engineeredRestart", "");
    if (phase.isEmpty()) {
      System.out.println("FD_ENGINEERED_RESTART no dedicated phase: cold restart not exercised");
      helper.succeed();
      return;
    }
    boolean absent = phase.equals("restoreAbsent");
    helper.assertTrue(
        phase.equals("prepare") || phase.equals("restore") || absent, "Unknown restart phase");
    helper.assertTrue(
        OptionalIntegrations.installed(OptionalIntegrations.ENGINEERED).mayInspect() != absent,
        "Restart phase does not match Engineered availability");
    var level = helper.getLevel();
    var data = FoundationData.get(level);
    var container = SubLevelContainer.getContainer(level);
    for (int x = -61; x <= -52; x++)
      for (int z = -58; z <= -51; z++) level.setChunkForced(x, z, true);
    boolean prepare = phase.equals("prepare");
    var lanes = new ArrayList<Lane>();
    if (prepare) {
      helper.assertTrue(
          data.rails.isEmpty() && data.vehicles.isEmpty() && !Files.exists(RECORD),
          "Prepare requires a fresh disposable world");
      var player =
          FakePlayerFactory.get(
              level,
              new com.mojang.authlib.GameProfile(
                  UUID.fromString("fdfdfdfd-0000-0000-0000-000000000417"), "FDRestart"));
      player.setGameMode(GameType.CREATIVE);
      player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
      for (var design : TrackDesign.values()) {
        var lane = positions(design.ordinal());
        var from = lane.controllerPos.offset(6, 0, 0);
        var to = from.offset(0, 0, 64);
        for (var pos : List.of(from, to))
          level.setBlockAndUpdate(
              pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
        player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
        lane.rail =
            TrackPlacement.connect(level, from, to, design, player, player.getMainHandItem());
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
              helper.assertTrue(
                  (boolean)
                      invoke(
                          invoke(type("span.TrackSpanBehaviour"), "at", level, f.rail.from),
                          "setSpan",
                          construct(
                              "span.TrackSpan",
                              List.of(
                                  construct(
                                      "span.SpanSegment", f.rail.from, f.rail.to, 0d, .375d)))),
                  "Short span rejected");
              invoke(
                  f.controller,
                  "setSteps",
                  List.of(step("STOP", 0), step("LAUNCH", 10), step("COAST", 0)));
              var edge = TrackGeometry.resolve(level, f.rail);
              double t = edge.from().equals(f.rail.from) ? .1875 : .8125;
              f.car =
                  VehicleFactory.create(
                      level,
                      TrackDesign.values()[i],
                      new CoasterPathTrackFrame.GraphHit(
                          CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t));
              f.observer = level.getBlockEntity(f.observerPos);
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
              helper.assertTrue(Boarding.ready(level, f.car), "Initial readiness missing");
              f.held =
                  new Vector3d(
                      CoasterCartResolve.cartCenterWorld(container.getSubLevel(f.car.chassis)));
              f.rider = EntityType.PIG.create(level);
              f.rider.setNoAi(true);
              f.rider.setPersistenceRequired();
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
              helper.assertTrue(f.rider.isPassenger(), "Boarding failed");
              VehicleOperations.restraints(level, f.car, true);
              assertNewBoardingRejected(helper, f);
              level.setBlockAndUpdate(
                  f.controllerPos.south(), Blocks.REDSTONE_BLOCK.defaultBlockState());
            }
          });
      helper.runAtTickTime(
          110, () -> lanes.forEach(f -> level.removeBlock(f.controllerPos.south(), false)));
      helper.runAtTickTime(
          200,
          () -> {
            var rows = new JsonArray();
            for (var f : lanes) {
              assertHeld(helper, f);
              var row = new JsonObject();
              row.addProperty("family", f.car.family.name());
              row.addProperty("chassis", f.car.chassis.toString());
              row.addProperty("rider", f.rider.getUUID().toString());
              row.addProperty("x", f.held.x);
              row.addProperty("y", f.held.y);
              row.addProperty("z", f.held.z);
              row.addProperty("observerTarget", invoke(f.observer, "target").toString());
              rows.add(row);
              System.out.println(
                  "FD_ENGINEERED_RESTART prepared family="
                      + f.car.family
                      + " chassis="
                      + f.car.chassis
                      + " rider="
                      + f.rider.getUUID()
                      + " mode=LAUNCH retained=true");
            }
            try {
              Files.writeString(
                  RECORD, new GsonBuilder().setPrettyPrinting().create().toJson(rows));
            } catch (java.io.IOException failure) {
              throw new IllegalStateException(failure);
            }
            helper.succeed();
            System.out.println("FD_ENGINEERED_RESTART PREPARED families=5");
          });
      return;
    }
    JsonArray rows;
    try {
      rows = JsonParser.parseString(Files.readString(RECORD)).getAsJsonArray();
    } catch (java.io.IOException failure) {
      throw new IllegalStateException(failure);
    }
    helper.assertTrue(
        rows.size() == 5 && data.vehicles.size() == 5 && data.rails.size() == 5,
        "Saved fixture counts changed");
    helper.runAtTickTime(
        100,
        () -> {
          for (int i = 0; i < rows.size(); i++) {
            var row = rows.get(i).getAsJsonObject();
            var f = positions(i);
            f.car = data.vehicles.get(UUID.fromString(row.get("chassis").getAsString()));
            helper.assertTrue(
                f.car != null && f.car.family.name().equals(row.get("family").getAsString()),
                "Persisted car identity changed");
            var from = f.controllerPos.offset(6, 0, 0);
            f.rail = data.rails.stream().filter(r -> r.from.equals(from)).findFirst().orElseThrow();
            f.controller = level.getBlockEntity(f.controllerPos);
            f.observer = level.getBlockEntity(f.observerPos);
            f.rider = (Pig) level.getEntity(UUID.fromString(row.get("rider").getAsString()));
            f.held =
                new Vector3d(
                    row.get("x").getAsDouble(),
                    row.get("y").getAsDouble(),
                    row.get("z").getAsDouble());
            if (absent) {
              helper.assertTrue(
                  f.controller == null && f.observer == null,
                  "Removed addon block entities survived");
              assertRetained(helper, f);
              assertNewBoardingRejected(helper, f);
              lanes.add(f);
              System.out.println(
                  "FD_ENGINEERED_RESTART removedAddon family="
                      + f.car.family
                      + " chassis="
                      + f.car.chassis
                      + " rider="
                      + f.rider.getUUID()
                      + " retained=true addonBlocksAbsent=true");
              continue;
            }
            helper.assertTrue(
                f.controller != null && f.observer != null,
                "Native controller/observer missing after restart");
            helper.assertTrue(
                (boolean) invoke(f.controller, "isLinked")
                    && invoke(f.controller, "boundAnchor").equals(from),
                "Controller lost anchor link");
            var steps = (List<?>) invoke(f.controller, "steps");
            helper.assertTrue(
                (boolean)
                    invoke(
                        type("controller.MotorStep"),
                        "same",
                        steps,
                        List.of(step("STOP", 0), step("LAUNCH", 10), step("COAST", 0))),
                "Saved motor program changed");
            helper.assertTrue(
                invoke(f.observer, "target")
                        .toString()
                        .equals(row.get("observerTarget").getAsString())
                    && (boolean) invoke(f.observer, "isWatching"),
                "Observer lost target");
            var span = invoke(type("span.TrackSpanBehaviour"), "at", level, from);
            helper.assertTrue(
                Math.abs((double) invoke(span, "length") - 24) < .01, "Saved span length changed");
            assertHeld(helper, f);
            assertNewBoardingRejected(helper, f);
            lanes.add(f);
            System.out.println(
                "FD_ENGINEERED_RESTART restored family="
                    + f.car.family
                    + " chassis="
                    + f.car.chassis
                    + " rider="
                    + f.rider.getUUID()
                    + " mode=LAUNCH retained=true span=24 observer=true");
          }
        });
    if (absent) {
      helper.runAtTickTime(
          200,
          () -> {
            for (var f : lanes) {
              assertRetained(helper, f);
              VehicleOperations.restraints(level, f.car, false);
              f.rider.stopRiding();
              helper.assertTrue(!f.rider.isPassenger(), "Removed-addon rider could not unload");
              System.out.println(
                  "FD_ENGINEERED_RESTART removedComplete family="
                      + f.car.family
                      + " ready=true unloaded=true");
            }
            helper.succeed();
            System.out.println("FD_ENGINEERED_RESTART REMOVED families=5");
          });
      return;
    }
    helper.runAtTickTime(
        200,
        () -> {
          for (var f : lanes) {
            assertHeld(helper, f);
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
                  level.getSignal(f.observerPos, Direction.UP) == power,
                  "Restored observer signal mismatch");
              f.observed = true;
            }
            if (f.car.family == Family.PAX || f.departed) continue;
            if (invoke(f.controller, "commandMode").toString().equals("COAST")) {
              double travel =
                  CoasterCartResolve.cartCenterWorld(container.getSubLevel(f.car.chassis)).z
                      - f.held.z;
              helper.assertTrue(
                  travel > 12 && f.observed, "Restored sequence advanced without departure");
              f.departed = true;
              VehicleOperations.capture(level, f.car);
              System.out.println(
                  "FD_ENGINEERED_RESTART departed family="
                      + f.car.family
                      + " travel="
                      + travel
                      + " observer=true mode=COAST");
            }
          }
        });
    helper.runAtTickTime(
        580,
        () -> {
          for (var f : lanes) {
            if (f.car.family == Family.PAX) {
              assertHeld(helper, f);
              helper.assertTrue(!f.observed, "Held PAX crossed observer");
            } else helper.assertTrue(f.departed && f.observed, "Restored car did not depart");
            helper.assertTrue(
                Boarding.ready(level, f.car)
                    && f.car.restraints
                    && f.car.fault.isEmpty()
                    && f.rider.isPassenger()
                    && f.rider.getHealth() == 10,
                "Restored capture lost readiness/rider");
            VehicleOperations.restraints(level, f.car, false);
            f.rider.stopRiding();
            helper.assertTrue(!f.rider.isPassenger(), "Restored rider could not unload");
            System.out.println(
                "FD_ENGINEERED_RESTART complete family="
                    + f.car.family
                    + " departed="
                    + f.departed
                    + " observer="
                    + f.observed
                    + " ready=true unloaded=true");
          }
          helper.succeed();
          System.out.println("FD_ENGINEERED_RESTART RESTORED families=5");
        });
  }

  private static void assertNewBoardingRejected(GameTestHelper helper, Lane f) {
    var newcomer = EntityType.PIG.create(helper.getLevel());
    boolean mounted = newcomer.startRiding(f.rider.getVehicle(), true);
    helper.assertTrue(
        !mounted && !newcomer.isPassenger(),
        "Closed restraint accepted new forced boarding: " + f.car.family);
    newcomer.discard();
    System.out.println("FD_ENGINEERED_RESTART newBoardingRejected family=" + f.car.family);
  }

  private static Object step(String kind, int value) {
    return construct(
        "controller.MotorStep",
        enumeration("controller.MotorStep$Kind", kind),
        enumeration("controller.MotorStep$Heading", "FORWARD"),
        value);
  }

  private static Lane positions(int index) {
    var lane = new Lane();
    lane.controllerPos = new BlockPos(-956 + index * 24, 150, -900);
    lane.observerPos = lane.controllerPos.offset(0, 0, 4);
    return lane;
  }

  private static void assertHeld(GameTestHelper helper, Lane f) {
    assertRetained(helper, f);
    helper.assertTrue(
        invoke(f.controller, "commandMode").toString().equals("LAUNCH")
            && (boolean) invoke(f.controller, "isDriving"),
        "Saved LAUNCH/presence lost: " + f.car.family);
  }

  private static void assertRetained(GameTestHelper helper, Lane f) {
    var level = helper.getLevel();
    double drift =
        CoasterCartResolve.cartCenterWorld(
                SubLevelContainer.getContainer(level).getSubLevel(f.car.chassis))
            .distance(f.held);
    helper.assertTrue(
        drift < .1
            && f.car.stopping
            && f.car.restraints
            && Boarding.ready(level, f.car)
            && f.car.fault.isEmpty(),
        "Saved captured state lost: " + f.car.family + " drift=" + drift + " fault=" + f.car.fault);
    helper.assertTrue(
        f.rider != null && f.rider.isPassenger() && f.rider.getHealth() == 10,
        "Occupied rider state lost: "
            + f.car.family
            + " exists="
            + (f.rider != null)
            + " passenger="
            + (f.rider != null && f.rider.isPassenger())
            + " health="
            + (f.rider == null ? -1 : f.rider.getHealth()));
  }
}
