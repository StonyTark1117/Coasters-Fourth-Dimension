package dev.fourthdimension.integration;

import com.google.gson.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.shuttle.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3d;

/** Explicit validation scene and buffered tick observations. Never enabled in ordinary play. */
public final class PerformanceFixture {
  private PerformanceFixture() {}

  private static final BlockPos PAX = new BlockPos(-600, 180, 600);
  private static Sample sample;

  public static void install() {
    if (!Boolean.getBoolean("fourthdimension.validation")) return;
    NeoForge.EVENT_BUS.addListener(PerformanceFixture::register);
    NeoForge.EVENT_BUS.addListener(PerformanceFixture::pre);
    NeoForge.EVENT_BUS.addListener(PerformanceFixture::post);
  }

  private static void register(RegisterCommandsEvent event) {
    var root = Commands.literal("performance");
    for (var action : List.of("tracks", "cars", "assemble", "board", "operate", "report"))
      root.then(
          Commands.literal(action)
              .executes(
                  ctx -> {
                    try {
                      if (sample != null)
                        throw new IllegalArgumentException("Wait for the active sample");
                      var level = ctx.getSource().getLevel();
                      if (action.equals("report"))
                        LogUtils.getLogger().info("FD_PERFORMANCE_SCENE {}", snapshot(level));
                      else act(level, action);
                      ctx.getSource()
                          .sendSuccess(
                              () -> Component.literal("Performance fixture: " + action), false);
                      return 1;
                    } catch (IllegalArgumentException e) {
                      ctx.getSource().sendFailure(Component.literal(e.getMessage()));
                      return 0;
                    }
                  }));
    root.then(
        Commands.literal("sample")
            .then(
                Commands.argument("label", StringArgumentType.word())
                    .executes(
                        ctx -> {
                          if (sample != null
                              || ctx.getSource().getServer().tickRateManager().isFrozen()) {
                            ctx.getSource()
                                .sendFailure(
                                    Component.literal(
                                        "Unfreeze the server and finish any active sample"));
                            return 0;
                          }
                          sample =
                              new Sample(
                                  ctx.getSource().getLevel(),
                                  StringArgumentType.getString(ctx, "label"));
                          ctx.getSource()
                              .sendSuccess(
                                  () -> Component.literal("Sampling 600 ordinary server ticks"),
                                  false);
                          return 1;
                        })));
    event
        .getDispatcher()
        .register(
            Commands.literal("fd")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("validation").then(root)));
  }

  private static BlockPos from(int lane) {
    return new BlockPos(-900 + lane * 32, 160, 100);
  }

  private static void force(ServerLevel level, BlockPos a, BlockPos b) {
    for (int x = a.getX() >> 4; x <= b.getX() >> 4; x++)
      for (int z = a.getZ() >> 4; z <= b.getZ() >> 4; z++) {
        level.setChunkForced(x, z, true);
        level.getChunk(x, z);
      }
  }

  private static ShuttleWinchEntity winch(ServerLevel level) {
    return (ShuttleWinchEntity) level.getBlockEntity(PAX.offset(-6, 0, 8));
  }

  private static List<FoundationData.Vehicle> lane(ServerLevel level, int lane) {
    var container = SubLevelContainer.getContainer(level);
    return FoundationData.get(level).vehicles.values().stream()
        .filter(v -> v.boundFrom.equals(from(lane)))
        .sorted(
            Comparator.comparingDouble(
                v -> CoasterCartResolve.cartCenterWorld(container.getSubLevel(v.chassis)).z()))
        .toList();
  }

  private static void act(ServerLevel level, String action) {
    var data = FoundationData.get(level);
    var container = SubLevelContainer.getContainer(level);
    if (action.equals("tracks")) {
      if (!data.rails.isEmpty() || !data.vehicles.isEmpty())
        throw new IllegalArgumentException("Use a fresh benchmark world");
      var player =
          FakePlayerFactory.get(
              level,
              new com.mojang.authlib.GameProfile(
                  UUID.fromString("fdfdfdfd-0000-0000-0000-000000000904"), "FDBenchmark"));
      player.setGameMode(GameType.CREATIVE);
      player.setItemInHand(InteractionHand.MAIN_HAND, TrackItems.ARROW.get().getDefaultInstance());
      for (int i = 0; i < 5; i++) {
        var a = from(i);
        var b = a.offset(0, 0, 120);
        force(level, a.offset(-16, 0, -16), b.offset(16, 0, 16));
        var middle = a.offset(0, 0, 60);
        for (var p : List.of(a, middle, b))
          level.setBlockAndUpdate(
              p, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
        for (var span : List.of(List.of(a, middle), List.of(middle, b))) {
          player.setPos(span.getLast().getCenter().add(2, 1, 0));
          TrackPlacement.connect(
              level,
              span.getFirst(),
              span.getLast(),
              i < 2 ? TrackDesign.ARROW : TrackDesign.FREE_SPIN,
              player,
              player.getMainHandItem());
        }
      }
      // Force the future shuttle corridor in every scene, including the empty baseline.
      force(level, PAX.offset(-24, 0, -72), PAX.offset(24, 0, 104));
      return;
    }
    if (action.equals("cars")) {
      if (data.rails.size() != 10 || !data.vehicles.isEmpty())
        throw new IllegalArgumentException("Create empty benchmark tracks first");
      for (int i = 0; i < 5; i++) {
        int index = i;
        var rail =
            data.rails.stream().filter(r -> r.from.equals(from(index))).findFirst().orElseThrow();
        var edge = TrackGeometry.resolve(level, rail);
        for (int c = 0; c < (i < 2 ? 7 : 1); c++) {
          double t = TrackGeometry.table(level, edge).parameterAt(12 + c * 6);
          VehicleFactory.create(
              level,
              i < 2 ? TrackDesign.ARROW : TrackDesign.FREE_SPIN,
              new CoasterPathTrackFrame.GraphHit(
                  CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t));
        }
      }
      ShuttleExamples.create(level, PAX, false);
      return;
    }
    if (data.vehicles.size() != 21)
      throw new IllegalArgumentException("Expected 21 benchmark cars");
    if (action.equals("assemble")) {
      for (int i = 0; i < 2; i++) {
        var cars = lane(level, i);
        for (int c = 1; c < cars.size(); c++)
          if (!CoasterCartTrainLinkConstraint.linkCarts(
              level,
              (ServerSubLevel) container.getSubLevel(cars.get(c - 1).chassis),
              (ServerSubLevel) container.getSubLevel(cars.get(c).chassis),
              6)) throw new IllegalArgumentException("Arrow benchmark coupling failed");
      }
      var pax =
          data.vehicles.values().stream()
              .filter(v -> v.family == Family.PAX)
              .sorted(
                  Comparator.comparingDouble(
                      v ->
                          CoasterCartResolve.cartCenterWorld(container.getSubLevel(v.chassis)).z()))
              .toList();
      ShuttleExamples.assemble(level, new ShuttleExamples.Example(pax, winch(level)));
      return;
    }
    if (action.equals("board")) {
      for (var v : data.vehicles.values()) {
        if (!Boarding.ready(level, v)
            || v.restraints
            || v.carriers.stream().anyMatch(c -> !c.occupants.isEmpty()))
          throw new IllegalArgumentException("Benchmark cars must be empty and ready");
        for (var c : v.carriers)
          for (var seat : c.seats) {
            var pig = EntityType.PIG.create(level);
            pig.setNoAi(true);
            pig.addTag("fd_performance");
            pig.setPos(v.boundFrom.getCenter().add(0, 3, 30));
            level.addFreshEntity(pig);
            SeatBlock.sitDown(
                level, container.getSubLevel(c.body).getPlot().getCenterBlock().offset(seat), pig);
            if (!pig.isPassenger())
              throw new IllegalArgumentException("Benchmark rider did not board");
          }
      }
      // Board the entire consist before closing its restraints.
      for (var v : data.vehicles.values()) VehicleOperations.restraints(level, v, true);
      return;
    }
    if (action.equals("operate")) {
      for (int i = 0; i < 5; i++) VehicleOperations.dispatch(level, lane(level, i).getFirst());
      // A single initial velocity, not a per-tick drive or bypass of operational validation.
      var pipeline = container.physicsSystem().getPipeline();
      for (var v : data.vehicles.values())
        if (v.family != Family.PAX) {
          var ids = new ArrayList<UUID>();
          ids.add(v.chassis);
          v.carriers.forEach(c -> ids.add(c.body));
          for (var id : ids)
            pipeline.addLinearAndAngularVelocity(
                (ServerSubLevel) container.getSubLevel(id), new Vector3d(0, 0, .5), new Vector3d());
        }
      winch(level).start();
    }
  }

  private static JsonObject snapshot(ServerLevel level) {
    var result = new JsonObject();
    result.addProperty("tick", level.getGameTime());
    var data = FoundationData.get(level);
    var container = SubLevelContainer.getContainer(level);
    result.addProperty("rails", data.rails.size());
    result.addProperty("forcedChunks", level.getForcedChunks().size());
    var cars = new JsonArray();
    for (var v : data.vehicles.values()) {
      var car = new JsonObject();
      car.addProperty("id", v.chassis.toString());
      car.addProperty("family", v.family.name());
      car.addProperty("fault", v.fault);
      car.addProperty("held", v.stopping);
      car.addProperty("restraints", v.restraints);
      var parent = container.getSubLevel(v.chassis);
      car.addProperty("loaded", parent != null && !parent.isRemoved());
      if (parent != null) {
        var p = CoasterCartResolve.cartCenterWorld(parent);
        car.addProperty("z", p.z());
        car.addProperty("linked", VehicleOperations.consist(level, v).size());
      }
      var carriers = new JsonArray();
      for (var c : v.carriers) {
        var body = new JsonObject();
        body.addProperty("id", c.body.toString());
        body.addProperty("angle", c.angle);
        body.addProperty("loaded", container.getSubLevel(c.body) != null);
        body.addProperty("capture", c.capture);
        var riders = new JsonArray();
        for (var id : c.occupants) {
          var rider = level.getEntity(id);
          var r = new JsonObject();
          r.addProperty("id", id.toString());
          r.addProperty("alive", rider != null && rider.isAlive());
          r.addProperty("mounted", rider != null && rider.isPassenger());
          if (rider != null && rider.getVehicle() != null) {
            r.addProperty("seat", rider.getVehicle().getUUID().toString());
            var riderBody = dev.ryanhcode.sable.Sable.HELPER.getContaining(rider.getVehicle());
            r.addProperty("carrier", riderBody == null ? "" : riderBody.getUniqueId().toString());
          }
          riders.add(r);
        }
        body.add("riders", riders);
        carriers.add(body);
      }
      car.add("carriers", carriers);
      cars.add(car);
    }
    result.add("cars", cars);
    var w = winch(level);
    if (w != null) {
      result.addProperty("phase", w.phase.name());
      result.addProperty("cycles", w.cycles);
      result.addProperty("carriage", w.carriage == null ? "" : w.carriage.toString());
    }
    return result;
  }

  private static final class Sample {
    final ServerLevel level;
    final String label;
    final JsonObject before;
    final long[] ticks = new long[600], carrierNanos = new long[600], carrierSteps = new long[600];
    final Map<String, Integer> phases = new TreeMap<>();
    int index;
    long start, nanos, steps, wall;
    boolean armed;

    Sample(ServerLevel level, String label) {
      this.level = level;
      this.label = label;
      before = snapshot(level);
    }
  }

  private static void pre(ServerTickEvent.Pre event) {
    if (sample == null || event.getServer() != sample.level.getServer()) return;
    sample.start = System.nanoTime();
    sample.nanos = CarrierRuntime.nanos;
    sample.steps = CarrierRuntime.steps;
    if (sample.index == 0) sample.wall = sample.start;
    sample.armed = true;
  }

  private static void post(ServerTickEvent.Post event) {
    if (sample == null || !sample.armed || event.getServer() != sample.level.getServer()) return;
    var s = sample;
    long end = System.nanoTime();
    s.ticks[s.index] = end - s.start;
    s.carrierNanos[s.index] = CarrierRuntime.nanos - s.nanos;
    s.carrierSteps[s.index] = CarrierRuntime.steps - s.steps;
    var w = winch(s.level);
    if (w != null) s.phases.merge(w.phase.name(), 1, Integer::sum);
    s.index++;
    s.armed = false;
    if (s.index != s.ticks.length) return;
    sample = null;
    var result = new JsonObject();
    result.addProperty("label", s.label);
    result.addProperty("wallNanos", end - s.wall);
    result.add("before", s.before);
    result.add("after", snapshot(s.level));
    var gson = new Gson();
    result.add("tickNanos", gson.toJsonTree(s.ticks));
    result.add("carrierNanos", gson.toJsonTree(s.carrierNanos));
    result.add("carrierSteps", gson.toJsonTree(s.carrierSteps));
    result.add("phases", gson.toJsonTree(s.phases));
    LogUtils.getLogger().info("FD_PERFORMANCE_SAMPLE {}", result);
  }
}
