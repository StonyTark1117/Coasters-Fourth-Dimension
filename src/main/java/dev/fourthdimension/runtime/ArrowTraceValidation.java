package dev.fourthdimension.runtime;

import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3d;
import org.slf4j.Logger;

/** Short-lived, validation-only sampler for both members of an occupied native Arrow consist. */
public final class ArrowTraceValidation {
  private static final Logger LOGGER = LogUtils.getLogger();
  private static UUID first;
  private static UUID second;
  private static long startedAt;
  private static long lastSample = Long.MIN_VALUE;

  private ArrowTraceValidation() {}

  public static int start(CommandSourceStack source, ServerPlayer player) {
    var level = player.serverLevel();
    var assigned =
        FoundationData.get(level).vehicles.values().stream()
            .filter(v -> v.carriers.stream().anyMatch(c -> c.riderSeats.containsKey(player.getUUID())))
            .findFirst()
            .orElse(null);
    if (assigned == null) {
      source.sendFailure(Component.literal("Player must be seated in an assigned Arrow chassis"));
      return 0;
    }
    if (assigned.family != dev.fourthdimension.contract.Family.ARROW) {
      source.sendFailure(Component.literal("This trace accepts only the original Arrow family"));
      return 0;
    }
    var container = SubLevelContainer.getContainer(level);
    var lead = container == null ? null : container.getSubLevel(assigned.chassis);
    if (lead == null) {
      source.sendFailure(Component.literal("Assigned Arrow chassis is not loaded"));
      return 0;
    }
    var linked = CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(lead).stream().sorted().toList();
    if (linked.size() != 2
        || linked.stream().anyMatch(
            id -> {
              var member = FoundationData.get(level).vehicles.get(id);
              return member == null || member.family != dev.fourthdimension.contract.Family.ARROW;
            })) {
      source.sendFailure(Component.literal("Expected exactly two loaded native-linked original chassis"));
      return 0;
    }
    first = linked.get(0);
    second = linked.get(1);
    startedAt = level.getGameTime();
    lastSample = Long.MIN_VALUE;
    LOGGER.info("FD_ARROW_TRACE start tick={} rider={} a={} b={} linked={}", startedAt,
        player.getGameProfile().getName(), first, second,
        CoasterCartTrainLinkConstraint.areLinkedCarts(container.getSubLevel(first), container.getSubLevel(second)));
    source.sendSuccess(() -> Component.literal("Arrow native consist trace started for 180 seconds"), false);
    return 1;
  }

  public static void tick(ServerTickEvent.Post event) {
    if (first == null) return;
    var level = event.getServer().overworld();
    long now = level.getGameTime();
    if (now - startedAt > 3_600) {
      LOGGER.info("FD_ARROW_TRACE stop reason=timeout samplesEndedAtTick={}", now);
      first = null;
      second = null;
      return;
    }
    if (now % 5 != 0 || now == lastSample) return;
    lastSample = now;
    var container = SubLevelContainer.getContainer(level);
    var a = container == null ? null : container.getSubLevel(first);
    var b = container == null ? null : container.getSubLevel(second);
    if (a == null || b == null) {
      LOGGER.error("FD_ARROW_TRACE sample tick={} loadedA={} loadedB={} linked=false", now, a != null, b != null);
      return;
    }
    Vector3d pa = CoasterCartResolve.cartCenterWorld(a);
    Vector3d pb = CoasterCartResolve.cartCenterWorld(b);
    var physics = container.physicsSystem().getPipeline();
    Vector3d va = physics.getLinearVelocity((dev.ryanhcode.sable.sublevel.ServerSubLevel) a, new Vector3d());
    Vector3d vb = physics.getLinearVelocity((dev.ryanhcode.sable.sublevel.ServerSubLevel) b, new Vector3d());
    boolean linked = CoasterCartTrainLinkConstraint.areLinkedCarts(a, b);
    LOGGER.info("FD_ARROW_TRACE sample tick={} elapsedTicks={} a={} pA={} vA={} b={} pB={} vB={} nativeLinked={}",
        now, now - startedAt, first, pa, va, second, pb, vb, linked);
    if (!linked) LOGGER.error("FD_ARROW_TRACE LINK_LOST tick={} a={} b={}", now, first, second);
  }
}
