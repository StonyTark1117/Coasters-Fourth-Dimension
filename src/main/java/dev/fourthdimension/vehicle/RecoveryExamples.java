package dev.fourthdimension.vehicle;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftEndRef;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Flat standalone teaching fixtures; the operator performs selection, locking and coupling. */
public final class RecoveryExamples {
  private RecoveryExamples() {}

  public static void register(RegisterCommandsEvent event) {
    var root = Commands.literal("fd").requires(s -> s.hasPermission(2));
    var demo = Commands.literal("demo");
    for (var design : TrackDesign.values()) {
      demo.then(
          Commands.literal("recovery_" + design.name().toLowerCase(Locale.ROOT))
              .executes(
                  ctx -> {
                    try {
                      create(
                          ctx.getSource().getLevel(),
                          BlockPos.containing(ctx.getSource().getPosition()).offset(0, 3, 12),
                          design);
                      ctx.getSource()
                          .sendSuccess(
                              () ->
                                  Component.literal(
                                      "Recovery example built with an unpowered native chain drive."
                                          + " Board, close restraints and retain the car. Sneak-use"
                                          + " the Rotation Tool on the bare bearing; select Attach"
                                          + " recovery on the end car, then Start tow. Set the"
                                          + " creative motor to 8 RPM in the bearing's direction."
                                          + " The lever above the clutch starts ON (power OFF)."
                                          + " Turn it OFF to supply towing power."
                                          + " Brake tow, turn the lever ON to stop power, then"
                                          + " Detach recovery; carriers remain"
                                          + " retained. Maximum recovery speed: 0.75 blocks/s."),
                              false);
                      return 1;
                    } catch (IllegalArgumentException | IllegalStateException failure) {
                      ctx.getSource().sendFailure(Component.literal(failure.getMessage()));
                      return 0;
                    }
                  }));
      demo.then(
          Commands.literal("recovery_bank_" + design.name().toLowerCase(Locale.ROOT))
              .executes(
                  ctx -> {
                    try {
                      BankedRecoveryExamples.create(
                          ctx.getSource().getLevel(),
                          BlockPos.containing(ctx.getSource().getPosition()).offset(0, 3, 12),
                          design);
                      ctx.getSource()
                          .sendSuccess(
                              () ->
                                  Component.literal(
                                      "Banked recovery test route built: quarter bend, 30-degree"
                                          + " bank and level runout. Use the flat recovery"
                                          + " workflow: close restraints, retain, select the bare"
                                          + " bearing, attach and start towing. Set 8 RPM; lever"
                                          + " OFF supplies power, ON cuts it. Brake on the level"
                                          + " runout, cut power, wait, then detach and unload."
                                          + " Native occupied traversal is tested; complete"
                                          + " rendered clearance is pending."),
                              false);
                      return 1;
                    } catch (IllegalArgumentException | IllegalStateException failure) {
                      ctx.getSource().sendFailure(Component.literal(failure.getMessage()));
                      return 0;
                    }
                  }));
    }
    event.getDispatcher().register(root.then(demo));
  }

  public record Example(
      FoundationData.Vehicle car,
      dev.ryanhcode.sable.sublevel.ServerSubLevel bearing,
      CreativeMotorBlockEntity motor,
      BlockPos powerLever) {}

  public static Example create(ServerLevel level, BlockPos origin, TrackDesign design) {
    var data = FoundationData.get(level);
    if (Math.abs(origin.getX()) + 8 > 1024
        || Math.abs(origin.getZ() - 4) > 1024
        || Math.abs(origin.getZ() + 52) > 1024
        || origin.getY() - 1 < level.getMinBuildHeight()
        || origin.getY() + 12 >= level.getMaxBuildHeight())
      throw new IllegalArgumentException(
          "Choose a recovery corridor inside the supported track and build bounds");
    if (data.vehicles.size() >= 64 || data.rails.size() >= 256)
      throw new IllegalArgumentException("Example capacity reached");
    data.checkProfileCapacity(2);
    for (var p : BlockPos.betweenClosed(origin.offset(-8, -1, -4), origin.offset(8, 12, 52)))
      if (!level.hasChunkAt(p) || !level.getBlockState(p).isAir())
        throw new IllegalArgumentException(
            "Load and clear a 17 by 14 by 57 corridor above the recovery example");
    var to = origin.offset(0, 0, 48);
    for (var p : List.of(origin, to))
      level.setBlockAndUpdate(
          p, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var player =
        FakePlayerFactory.get(
            level,
            new com.mojang.authlib.GameProfile(
                UUID.fromString("fdfdfdfd-0000-0000-0000-000000000404"), "FDRecoveryExample"));
    player.setGameMode(GameType.CREATIVE);
    player.setPos(to.getX() + 2, to.getY() + 1, to.getZ());
    player.setItemInHand(
        InteractionHand.MAIN_HAND,
        (switch (design) {
              case ARROW -> TrackItems.ARROW;
              case LEGACY -> TrackItems.LEGACY;
              case FREE_SPIN -> TrackItems.FREE_SPIN;
              case REVISED -> TrackItems.REVISED;
              case PAX -> TrackItems.PAX;
            })
            .get()
            .getDefaultInstance());
    var rail = TrackPlacement.connect(level, origin, to, design, player, player.getMainHandItem());
    var edge = TrackGeometry.resolve(level, rail);
    double t = TrackGeometry.table(level, edge).parameterAt(12);
    var car =
        VehicleFactory.create(
            level,
            design,
            new CoasterPathTrackFrame.GraphHit(
                CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t));
    t = TrackGeometry.table(level, edge).parameterAt(12 + design.axleSpacing());
    var hit =
        new CoasterPathTrackFrame.GraphHit(
            CoasterPathGraphManager.sampleEdgePosition(level, edge, t), edge, t);
    var q =
        CoasterPathTrackFrame.quaternionFromTrackBasis(
            CoasterPathTrackFrame.trackBasisAtHit(level, hit));
    var ordinary =
        CoasterCartSpawner.spawnMinimalContraption(
            level,
            CoasterCartLinkPlacement.plotOriginForBearingCenter(
                CoasterPathTrackFrame.pathAnchorForSnappedBlock(level, hit), q),
            q,
            hit,
            true);
    if (ordinary == null)
      throw new IllegalStateException("Native recovery bearing allocation failed");
    var anchor = (CoasterAnchorpointBlockEntity) level.getBlockEntity(origin);
    anchor.commitChainLift(
        Direction.Axis.X,
        ChainLiftEndRef.anchor(edge.to()),
        new CoasterPathGraphSpans.SpanPath(
            List.of(new CoasterPathGraphSpans.PathSegment(edge, 0, 1)),
            TrackGeometry.table(level, edge).length()));
    var clutchPos = origin.east();
    level.setBlockAndUpdate(
        clutchPos,
        AllBlocks.CLUTCH
            .getDefaultState()
            .setValue(BlockStateProperties.AXIS, Direction.Axis.X)
            .setValue(BlockStateProperties.POWERED, true));
    var leverPos = clutchPos.above();
    level.setBlockAndUpdate(
        leverPos,
        Blocks.LEVER
            .defaultBlockState()
            .setValue(BlockStateProperties.ATTACH_FACE, AttachFace.FLOOR)
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
            .setValue(BlockStateProperties.POWERED, true));
    var motorPos = origin.east(2);
    level.setBlockAndUpdate(
        motorPos,
        AllBlocks.CREATIVE_MOTOR
            .getDefaultState()
            .setValue(BlockStateProperties.FACING, Direction.WEST));
    ((CreativeMotorBlockEntity) level.getBlockEntity(motorPos)).generatedSpeed.setValue(0);
    for (int side : new int[] {-6, 6})
      for (var p :
          BlockPos.betweenClosed(
              origin.offset(side, -1, 8), origin.offset(side + (side < 0 ? -1 : 1), -1, 43)))
        level.setBlockAndUpdate(p, Blocks.SMOOTH_STONE.defaultBlockState());
    data.setDirty();
    FoundationSync.broadcast(level);
    return new Example(
        car, ordinary, (CreativeMotorBlockEntity) level.getBlockEntity(motorPos), leverPos);
  }
}
