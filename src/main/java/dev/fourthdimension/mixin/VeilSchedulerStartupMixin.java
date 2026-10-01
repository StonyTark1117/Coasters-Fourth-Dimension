package dev.fourthdimension.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Veil 4.1.4 must publish its final worker count before workers can drain the source. */
@Mixin(targets = "foundry.veil.impl.ThreadTaskScheduler", remap = false)
public abstract class VeilSchedulerStartupMixin {
  @Shadow @Final private int threadCount;
  @Shadow @Final private CompletableFuture<?> completedFuture;
  @Unique private List<Thread> fourthdimension$deferredWorkers;

  @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;start()V"))
  private void fourthdimension$deferStart(Thread worker) {
    if (fourthdimension$deferredWorkers == null)
      fourthdimension$deferredWorkers = new ArrayList<>();
    fourthdimension$deferredWorkers.add(worker);
  }

  @Inject(method = "<init>", at = @At("RETURN"))
  private void fourthdimension$publishWorkers(
      String name, int requestedWorkers, Supplier<Runnable> source, CallbackInfo ci) {
    if (threadCount == 0) {
      completedFuture.complete(null);
    } else {
      for (var worker : fourthdimension$deferredWorkers) worker.start();
      fourthdimension$deferredWorkers.clear();
    }
  }
}
