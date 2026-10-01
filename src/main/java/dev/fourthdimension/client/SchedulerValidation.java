package dev.fourthdimension.client;

import com.mojang.logging.LogUtils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Opt-in checks against the installed, transformed native scheduler; no font or reload changes. */
@EventBusSubscriber(modid = "fourthdimension", value = Dist.CLIENT)
public final class SchedulerValidation {
  private static boolean checked;

  @SubscribeEvent
  public static void tick(ClientTickEvent.Post event) {
    if (checked || !Boolean.getBoolean("fourthdimension.schedulerValidation")) return;
    var mc = Minecraft.getInstance();
    if (mc.level == null || mc.getOverlay() != null) return;
    checked = true;
    try {
      var type = Class.forName("foundry.veil.impl.ThreadTaskScheduler");
      var constructor = type.getConstructor(String.class, int.class, Supplier.class);
      var completion = type.getMethod("getCompletedFuture");
      int schedules = 0, tasks = 0;
      for (int size : new int[] {0, 1, 6, 17}) {
        for (int iteration = 0; iteration < 128; iteration++) {
          var next = new AtomicInteger();
          var executed = new AtomicIntegerArray(size);
          Supplier<Runnable> source =
              () -> {
                int task = next.getAndIncrement();
                return task >= size ? null : () -> executed.incrementAndGet(task);
              };
          Object scheduler = constructor.newInstance("FDStartupCheck", 6, source);
          ((CompletableFuture<?>) completion.invoke(scheduler)).get(2, TimeUnit.SECONDS);
          for (int task = 0; task < size; task++) {
            if (executed.get(task) != 1)
              throw new IllegalStateException("Completion skipped or duplicated task " + task);
          }
          schedules++;
          tasks += size;
        }
      }
      LogUtils.getLogger()
          .info(
              "FD_SCHEDULER passed schedules={} completedTasks={} emptySchedules=128 maxWorkers=6",
              schedules,
              tasks);
    } catch (Exception failure) {
      throw new IllegalStateException("FD_SCHEDULER validation failed", failure);
    }
  }
}
