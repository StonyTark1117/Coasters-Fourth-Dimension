package dev.fourthdimension.runtime;

import com.mojang.logging.LogUtils;
import dev.fourthdimension.shuttle.ShuttleExamples;
import dev.fourthdimension.track.ExampleLayouts;
import dev.fourthdimension.track.TrackDesign;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;

/** Creates an opt-in, empty-world five-family scene for packaged client transform validation. */
public final class CarrierValidationFixture {
  private static final List<BlockPos> CIRCUIT_ORIGINS =
      List.of(
          new BlockPos(-150, 180, 0),
          new BlockPos(-75, 180, 0),
          new BlockPos(0, 180, 0),
          new BlockPos(75, 180, 0));
  private static final List<TrackDesign> CIRCUIT_DESIGNS =
      List.of(
          TrackDesign.ARROW,
          TrackDesign.LEGACY,
          TrackDesign.FREE_SPIN,
          TrackDesign.REVISED);
  private static final BlockPos PAX_ORIGIN = new BlockPos(145, 180, 0);
  private static final BlockPos SPAWN = new BlockPos(0, 180, -30);

  private CarrierValidationFixture() {}

  public static void create(ServerLevel level) {
    if (!Boolean.getBoolean("fourthdimension.carrierFixture")) return;
    var data = FoundationData.get(level);
    if (!data.vehicles.isEmpty() || !data.rails.isEmpty())
      throw new IllegalStateException(
          "FD_CARRIER_FIXTURE requires a fresh empty world; remove the previous validation save");
    preload(level, new BlockPos(-160, 160, -64), new BlockPos(160, 220, 96));
    level.setBlockAndUpdate(SPAWN.below(), Blocks.SMOOTH_STONE.defaultBlockState());
    level.setDefaultSpawnPos(SPAWN, 0);
    for (int i = 0; i < CIRCUIT_ORIGINS.size(); i++)
      ExampleLayouts.create(level, CIRCUIT_ORIGINS.get(i), CIRCUIT_DESIGNS.get(i));
    ShuttleExamples.create(level, PAX_ORIGIN, false);
    int carriers = data.vehicles.values().stream().mapToInt(v -> v.carriers.size()).sum();
    LogUtils.getLogger()
        .info(
            "FD_CARRIER_FIXTURE created families={} vehicles={} carriers={} rails={}",
            TrackDesign.values().length,
            data.vehicles.size(),
            carriers,
            data.rails.size());
  }

  public static void placePlayer(
      net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
    if (!Boolean.getBoolean("fourthdimension.carrierFixture")
        || !(event.getEntity() instanceof ServerPlayer player)
        || player.serverLevel() != player.server.overworld()) return;
    player.teleportTo(SPAWN.getX() + .5, SPAWN.getY(), SPAWN.getZ() + .5);
  }

  private static void preload(ServerLevel level, BlockPos min, BlockPos max) {
    for (int chunkX = min.getX() >> 4; chunkX <= max.getX() >> 4; chunkX++)
      for (int chunkZ = min.getZ() >> 4; chunkZ <= max.getZ() >> 4; chunkZ++)
        level.getChunk(chunkX, chunkZ);
  }
}
