package dev.fourthdimension.runtime;

import com.mojang.logging.LogUtils;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

/** Opt-in packet evidence; the extra tick is never loaded into authoritative saved data. */
public final class IntegrationDiagnostics {
  private IntegrationDiagnostics() {}

  public static void snapshot(String side, Player player, CompoundTag tag) {
    if (!Boolean.getBoolean("fourthdimension.validation")) return;
    LogUtils.getLogger()
        .info(
            "FD_SNAPSHOT side={} player={} dimension={} tick={} sha256={} railsSha256={} rails={}"
                + " vehicles={}",
            side,
            player.getName().getString(),
            player.level().dimension().location(),
            tag.getLong("validation_tick"),
            digest(tag),
            digest(tag.getList("rails", Tag.TAG_COMPOUND)),
            tag.getList("rails", Tag.TAG_COMPOUND).size(),
            tag.getList("vehicles", Tag.TAG_COMPOUND).size());
  }

  private static String digest(Tag tag) {
    try {
      // StringTagVisitor sorts compound keys; list order is retained on the wire.
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256")
                  .digest(tag.toString().getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException impossible) {
      throw new IllegalStateException("Java runtime has no SHA-256", impossible);
    }
  }
}
