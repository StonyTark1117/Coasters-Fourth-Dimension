package dev.fourthdimension.runtime;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import java.io.BufferedWriter;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordingFile;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Bounded, opt-in raw measurements; never enabled by normal gameplay or an acceptance gate. */
public final class RuntimePerformanceRecorder {
  private static Recorder recorder;
  private static boolean registered;

  private RuntimePerformanceRecorder() {}

  /** Call once from the mod constructor. No listeners, threads or files without performanceFile. */
  public static synchronized void register() {
    if (registered) return;
    registered = true;
    String output = System.getProperty("fourthdimension.performanceFile", "");
    if (output.isBlank()) return;
    try {
      int seconds = Integer.getInteger("fourthdimension.performanceSeconds", 300);
      if (seconds < 1 || seconds > 3600)
        throw new IllegalArgumentException("performanceSeconds must be 1..3600");
      Path path = Path.of(output);
      if (!path.isAbsolute() || path.normalize().startsWith(Path.of("/tmp")))
        throw new IllegalArgumentException("performanceFile requires an absolute persistent path");
      recorder = new Recorder(path, seconds);
      recorder.start();
      NeoForge.EVENT_BUS.addListener(RuntimePerformanceRecorder::serverTick);
      // Only this nested class references client event/game classes. Dedicated servers never load it.
      if (FMLEnvironment.dist == Dist.CLIENT) ClientEvents.register();
      Runtime.getRuntime().addShutdownHook(new Thread(() -> recorder.stop("jvm_shutdown"), "fd-performance-close"));
      LogUtils.getLogger().info("FD_PERFORMANCE recording={} seconds={}", path, seconds);
    } catch (Exception failure) {
      if (recorder != null) recorder.stop("initialization_failure");
      LogUtils.getLogger().error("FD_PERFORMANCE unavailable; no measurement claim", failure);
    }
  }

  private static void serverTick(ServerTickEvent.Post event) {
    if (!recorder.accepting.get()) return;
    var server = event.getServer();
    int tick = server.getTickCount();
    var row = recorder.row("tick");
    row.put("duration_ns", server.getTickTimesNanos()[tick % 100]);
    row.put("tick", tick);
    row.put("server", server.isDedicatedServer() ? "dedicated" : "integrated");
    row.put("players", server.getPlayerCount());
    recorder.offer(row);
  }

  private static final class ClientEvents {
    private static long previous;
    private static Object previousLevel;

    static void register() {
      NeoForge.EVENT_BUS.addListener(ClientEvents::frame);
    }

    private static void frame(net.neoforged.neoforge.client.event.RenderFrameEvent.Pre event) {
      if (!recorder.accepting.get()) return;
      var minecraft = net.minecraft.client.Minecraft.getInstance();
      long now = System.nanoTime();
      // Do not bridge loading/menu transitions into a purported in-world interval.
      if (minecraft.level == null || minecraft.level != previousLevel) {
        previous = 0;
        previousLevel = minecraft.level;
      }
      if (minecraft.level != null && previous != 0) {
        var row = recorder.row("frame");
        row.put("duration_ns", now - previous);
        row.put("dimension", minecraft.level.dimension().location().toString());
        row.put("game_tick", minecraft.level.getGameTime());
        row.put("paused", minecraft.isPaused());
        row.put("screen", minecraft.screen == null ? "none" : minecraft.screen.getClass().getName());
        recorder.offer(row);
      }
      previous = minecraft.level == null ? 0 : now;
    }
  }

  private static final class Recorder {
    private static final Gson GSON = new Gson();
    private final ArrayBlockingQueue<Map<String, Object>> queue = new ArrayBlockingQueue<>(8192);
    private final AtomicBoolean accepting = new AtomicBoolean(true);
    private final AtomicLong dropped = new AtomicLong();
    private final long anchorNanos = System.nanoTime();
    private final Instant anchorWall = Instant.now();
    private final String process = FMLEnvironment.dist == Dist.CLIENT ? "client" : "server";
    private final Path path;
    private final int seconds;
    private final BufferedWriter output;
    private final Thread writer;
    private Recording jfr;
    private volatile String stopReason = "duration";

    Recorder(Path path, int seconds) throws Exception {
      this.path = path;
      this.seconds = seconds;
      Files.createDirectories(path.getParent());
      output = Files.newBufferedWriter(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
      writer = new Thread(this::writeLoop, "fd-performance-writer");
      writer.setDaemon(true);
    }

    Map<String, Object> row(String kind) {
      long nanos = System.nanoTime();
      var row = new LinkedHashMap<String, Object>();
      row.put("kind", kind);
      row.put("unix_ns", epochNanos(anchorWall) + nanos - anchorNanos);
      row.put("monotonic_ns", nanos);
      row.put("process", process);
      return row;
    }

    static long epochNanos(Instant instant) {
      return instant.getEpochSecond() * 1_000_000_000L + instant.getNano();
    }

    void offer(Map<String, Object> row) {
      if (accepting.get() && !queue.offer(row)) dropped.incrementAndGet();
    }

    void start() {
      var manifest = row("manifest");
      manifest.put("collector", "fd-runtime-performance-v1");
      manifest.put("pid", ProcessHandle.current().pid());
      manifest.put("java", System.getProperty("java.version"));
      manifest.put("seconds", seconds);
      manifest.put("frame_scope", "Consecutive in-world RenderFrameEvent.Pre CPU intervals, includes pacing; not GPU presentation");
      manifest.put("tick_scope", "Minecraft native tickServer ring sample, including worlds/status/autosave; excludes idle pacing and Post listeners");
      manifest.put("gc_scope", "JFR jdk.GCPhasePause duration, not concurrent collection elapsed time");
      manifest.put("post_gc_scope", "JFR jdk.GCHeapSummary After GC heapUsed, not RSS or allocated heap");
      offer(manifest);
      try {
        if (Files.exists(Path.of(path + ".jfr")))
          throw new IllegalArgumentException("Refusing to overwrite existing JFR data");
        jfr = new Recording();
        jfr.enable("jdk.GCPhasePause").withThreshold(Duration.ZERO);
        jfr.enable("jdk.GCHeapSummary");
        jfr.start();
      } catch (Exception failure) {
        if (jfr != null) jfr.close();
        jfr = null;
        var row = row("collector_error");
        row.put("source", "jfr");
        row.put("error", failure.toString());
        offer(row);
      }
      writer.start();
    }

    long finishGc() throws Exception {
      if (jfr == null) return 0;
      Path recording = Path.of(path + ".jfr");
      try (var recordingHandle = jfr) {
        recordingHandle.stop();
        recordingHandle.dump(recording);
      }
      long count = 0;
      // Read the finished recording, including the final JFR chunk. Streaming
      // notifications can lag shutdown and must not silently omit its last GC.
      try (var events = new RecordingFile(recording)) {
        while (events.hasMoreEvents()) {
          var event = events.readEvent();
          Map<String, Object> row;
          if (event.getEventType().getName().equals("jdk.GCPhasePause")) {
            row = row("gc");
            row.put("duration_ns", event.getDuration().toNanos());
          } else if (event.getEventType().getName().equals("jdk.GCHeapSummary")
              && "After GC".equals(event.getString("when"))) {
            row = row("post_gc");
            row.put("after_bytes", event.getLong("heapUsed"));
          } else continue;
          row.put("unix_ns", epochNanos(event.getStartTime()));
          row.remove("monotonic_ns"); // The JFR event time is authoritative, not extraction time.
          row.put("gc_id", event.getInt("gcId"));
          write(row);
          count++;
        }
      }
      return count;
    }

    void write(Map<String, Object> row) throws Exception {
      output.write(GSON.toJson(row));
      output.newLine();
    }

    void writeLoop() {
      long nextHeap = anchorNanos;
      long count = 0;
      try (output) {
        while (accepting.get()) {
          long now = System.nanoTime();
          if (now - anchorNanos >= seconds * 1_000_000_000L || Files.exists(Path.of(path + ".stop"))) {
            if (Files.exists(Path.of(path + ".stop"))) stopReason = "stop_file";
            accepting.set(false);
            break;
          }
          var row = queue.poll(100, TimeUnit.MILLISECONDS);
          if (row != null) {
            write(row);
            count++;
          }
          if (now >= nextHeap) {
            var heap = row("heap");
            var usage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
            heap.put("used_bytes", usage.getUsed());
            heap.put("committed_bytes", usage.getCommitted());
            heap.put("max_bytes", usage.getMax());
            write(heap);
            count++;
            output.flush();
            nextHeap = now + 1_000_000_000L;
          }
        }
        count += finishGc();
        Map<String, Object> row;
        while ((row = queue.poll()) != null) {
          write(row);
          count++;
        }
        var complete = row("complete");
        complete.put("reason", stopReason);
        complete.put("rows", count);
        complete.put("dropped_events", dropped.get());
        complete.put("lossless", dropped.get() == 0);
        write(complete);
        LogUtils.getLogger().info("FD_PERFORMANCE complete={} rows={} dropped={}", path, count, dropped.get());
      } catch (Exception failure) {
        accepting.set(false);
        if (jfr != null) jfr.close();
        LogUtils.getLogger().error("FD_PERFORMANCE incomplete={} retain partial data", path, failure);
      }
    }

    void stop(String reason) {
      stopReason = reason;
      accepting.set(false);
      if (Thread.currentThread() == writer) return;
      try {
        writer.join(10_000);
      } catch (InterruptedException interrupted) {
        Thread.currentThread().interrupt();
      }
    }
  }
}
