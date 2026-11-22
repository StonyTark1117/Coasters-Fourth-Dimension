package dev.fourthdimension.runtime;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.fourthdimension.contract.Family;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import java.util.UUID;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/** Validation-only repair for the bounded FD1701 saved-world fixture. */
@EventBusSubscriber(modid = "fourthdimension")
public final class Fd1701FixtureRepair {
  private Fd1701FixtureRepair() {}

  @SubscribeEvent
  public static void register(RegisterCommandsEvent event) {
    if (!Boolean.getBoolean("fourthdimension.validation")) return;
    event.getDispatcher().register(
        Commands.literal("fd")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("fd1701_repair")
                .then(Commands.argument("chassis", StringArgumentType.word())
                    .executes(context -> repair(
                        context.getSource(), StringArgumentType.getString(context, "chassis")))))
            .then(Commands.literal("fd1701_list")
                .executes(context -> list(context.getSource())))
            .then(Commands.literal("fd1701_stage")
                .executes(context -> stage(context.getSource()))));
  }

  private static int stage(net.minecraft.commands.CommandSourceStack source) {
    var level = source.getLevel();
    var data = FoundationData.get(level);
    var route = data.rideLocal;
    if (!dev.fourthdimension.integration.RideLocalOperations.ROUTE_ID.equals(route.routeId)
        || route.rails.size() != 8) {
      source.sendFailure(Component.literal("Expected the configured eight-segment Arrow route"));
      return 0;
    }
    var railId = route.rails.get(4);
    var rail = data.rails.stream().filter(r -> r.id.equals(railId)).findFirst().orElse(null);
    if (rail == null || rail.family != Family.ARROW || !rail.original()
        || !level.hasChunkAt(rail.from) || !level.hasChunkAt(rail.to)) {
      source.sendFailure(Component.literal("Rail 4 is missing, unsupported, or unloaded"));
      return 0;
    }
    var edge = dev.fourthdimension.track.TrackGeometry.resolve(level, rail);
    if (edge == null) {
      source.sendFailure(Component.literal("Rail 4 native edge is unresolved"));
      return 0;
    }
    try {
      var point = dev.silvergold.simulatedcoasters.track.graph.CoasterPathGraphManager
          .sampleEdgePosition(level, edge, .2);
      var vehicle = dev.fourthdimension.vehicle.VehicleFactory.create(level,
          dev.fourthdimension.track.TrackDesign.ARROW,
          new dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit(point, edge, .2));
      var body = SubLevelContainer.getContainer(level).getSubLevel(vehicle.chassis);
      var center = dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve.cartCenterWorld(body);
      source.sendSuccess(() -> Component.literal("FD1701 validation fixture staged rail=4 t=.2 chassis="
          + vehicle.chassis + " center=" + center + "; no reservation or motion claimed"), false);
      return 1;
    } catch (IllegalArgumentException | IllegalStateException failure) {
      source.sendFailure(Component.literal("FD1701 stage rejected: " + failure.getMessage()));
      return 0;
    }
  }

  private static int list(net.minecraft.commands.CommandSourceStack source) {
    if (!(source.getLevel() instanceof ServerLevel level)) return 0;
    var container = SubLevelContainer.getContainer(level);
    for (var vehicle : FoundationData.get(level).vehicles.values()) {
      var body = container.getSubLevel(vehicle.chassis);
      var center = body instanceof ServerSubLevel nativeBody
          ? dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve.cartCenterWorld(nativeBody)
          : null;
      source.sendSuccess(() -> Component.literal(
          "vehicle chassis=" + vehicle.chassis + " family=" + vehicle.family
              + " center=" + (center == null ? "missing" : center.toString())), false);
    }
    return 1;
  }

  private static int repair(net.minecraft.commands.CommandSourceStack source, String raw) {
    UUID id;
    try {
      id = UUID.fromString(raw);
    } catch (IllegalArgumentException e) {
      source.sendFailure(Component.literal("Invalid chassis UUID"));
      return 0;
    }
    if (!(source.getLevel() instanceof ServerLevel level)) return 0;
    var vehicle = FoundationData.get(level).vehicles.get(id);
    if (vehicle == null || vehicle.family != Family.ARROW || !vehicle.original()) {
      source.sendFailure(Component.literal("Expected an original Arrow chassis UUID"));
      return 0;
    }
    var body = SubLevelContainer.getContainer(level).getSubLevel(id);
    if (!(body instanceof ServerSubLevel chassis) || chassis.isRemoved()) {
      source.sendFailure(Component.literal("Arrow chassis native body is unavailable"));
      return 0;
    }
    var blocks = chassis.getPlot().getEmbeddedLevelAccessor();
    var cartPos = BlockPos.ZERO;
    var controllerPos = new BlockPos(0, 1, 0);
    var cart = blocks.getBlockState(cartPos);
    var controller = blocks.getBlockState(controllerPos);
    boolean changed = false;
    if (!cart.is(SimulatedCoastersBlocks.COASTER_CART_BLOCK.get())) {
      if (!cart.isAir()) {
        source.sendFailure(Component.literal("Refusing to overwrite non-air chassis cart cell: " + cart));
        return 0;
      }
      BlockState repaired = SimulatedCoastersBlocks.COASTER_CART_BLOCK.get().defaultBlockState();
      if (repaired.hasProperty(dev.silvergold.simulatedcoasters.track.cart.CoasterCartBlock.SNAP_ENGAGED)) {
        repaired = repaired.setValue(
            dev.silvergold.simulatedcoasters.track.cart.CoasterCartBlock.SNAP_ENGAGED, true);
      }
      blocks.setBlock(cartPos, repaired, 3);
      changed = true;
    }
    if (!controller.is(FamilyControllers.CONTROLLERS.get(Family.ARROW).get())) {
      if (!controller.isAir()) {
        source.sendFailure(Component.literal("Refusing to overwrite non-air Arrow controller cell: " + controller));
        return 0;
      }
      blocks.setBlock(controllerPos,
          FamilyControllers.CONTROLLERS.get(Family.ARROW).get().defaultBlockState(), 3);
      changed = true;
    }
    if (!changed) {
      source.sendFailure(Component.literal("FD1701 repair found both native blocks already present"));
      return 0;
    }
    chassis.updateLastPose();
    SubLevelContainer.getContainer(level).physicsSystem().getPipeline().onStatsChanged(chassis);
    FoundationData.get(level).setDirty();
    FoundationSync.broadcast(level);
    source.sendSuccess(() -> Component.literal(
        "FD1701 repaired chassis " + id + " (cart=(0,0,0), controller=(0,1,0)); inspect then Resume"), false);
    return 1;
  }
}
