package dev.fourthdimension.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.fourthdimension.vehicle.Boarding;
import dev.ryanhcode.sable.mixinhelpers.camera.camera_rotation.EntitySubLevelRotationHelper.Type;
import java.util.UUID;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.joml.Quaterniond;
import org.lwjgl.glfw.GLFW;

/** Changes only the local camera's inherited frame. Seat, body and eye position stay native. */
@EventBusSubscriber(modid = "fourthdimension", value = Dist.CLIENT)
public final class RiderComfort {
  private static Entity mounted;
  private static UUID body;
  private static Quaterniond stable;
  private RiderComfort() {}

  @EventBusSubscriber(modid = "fourthdimension", value = Dist.CLIENT,
      bus = EventBusSubscriber.Bus.MOD)
  public static final class Keys {
    public static final KeyMapping TOGGLE = new KeyMapping("key.fourthdimension.comfort",
        InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.fourthdimension");
    @SubscribeEvent public static void register(RegisterKeyMappingsEvent event) { event.register(TOGGLE); }
  }

  @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
    var mc = Minecraft.getInstance();
    if (mc.player == null || !mc.player.isAlive() || !mc.player.isPassenger()) clear();
    while (Keys.TOGGLE.consumeClick()) {
      boolean enabled = !dev.fourthdimension.runtime.RiderComfortSettings.ENABLED.get();
      dev.fourthdimension.runtime.RiderComfortSettings.ENABLED.set(enabled);
      dev.fourthdimension.runtime.RiderComfortSettings.SPEC.save();
      clear();
      if (mc.player != null) mc.player.displayClientMessage(Component.translatable(
          enabled ? "message.fourthdimension.comfort.on" : "message.fourthdimension.comfort.off"), true);
    }
  }

  private static void clear() { mounted = null; body = null; stable = null; }

  public static Quaterniond orientation(Entity entity, Type type, Quaterniond nativeOrientation) {
    if (type != Type.CAMERA || entity == null) return nativeOrientation;
    var mc = Minecraft.getInstance();
    if (entity != mc.player
        || !dev.fourthdimension.runtime.RiderComfortSettings.ENABLED.get()) return nativeOrientation;
    if (!entity.isAlive()) { clear(); return nativeOrientation; }
    var seat = entity.getVehicle();
    var binding = seat == null ? null : Boarding.at(entity.level(), seat.blockPosition());
    if (binding == null || nativeOrientation == null) { clear(); return nativeOrientation; }
    if (mounted != seat || !binding.carrier().body.equals(body) || stable == null) {
      // Anchor a world-upright heading once per mount/toggle. Do not follow a seat's
      // rotations, including through vertical/inverted poses where projected yaw is singular.
      stable = dev.fourthdimension.contract.ComfortFrame.uprightFrame(nativeOrientation);
      mounted = seat;
      body = binding.carrier().body;
    }
    return new Quaterniond(stable);
  }
}
