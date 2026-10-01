import java.nio.file.*;
import java.util.*;
import jdk.jfr.consumer.*;

/** Streaming JDK 21 reader: preserves recorded stack depth without expanding a huge JSON dump. */
public class BenchmarkJfrSummary {
  static List<List<Object>> top(Map<String, Long> counts, int limit) {
    return counts.entrySet().stream()
        .sorted(
            Map.Entry.<String, Long>comparingByValue()
                .reversed()
                .thenComparing(Map.Entry.comparingByKey()))
        .limit(limit)
        .map(e -> List.<Object>of(e.getKey(), e.getValue()))
        .toList();
  }

  static Map<String, Object> summarize(Path path) throws Exception {
    Map<String, Long> cpu = new TreeMap<>(),
        own = new TreeMap<>(),
        allocation = new TreeMap<>(),
        threads = new TreeMap<>(),
        classes = new TreeMap<>();
    List<Object> ticks = new ArrayList<>();
    long total = 0, ownSamples = 0, weight = 0, truncated = 0, maxHeapReserved = 0;
    try (var recording = new RecordingFile(path)) {
      while (recording.hasMoreEvents()) {
        var event = recording.readEvent();
        var kind = event.getEventType().getName();
        if (kind.equals("minecraft.ServerTickTime")) {
          ticks.add(
              Map.of(
                  "startTime",
                  event.getStartTime().toString(),
                  "averageTickNanos",
                  event.getDuration("averageTickDuration").toNanos()));
          continue;
        }
        if (kind.equals("jdk.GCHeapSummary")) {
          RecordedObject space = event.getValue("heapSpace");
          maxHeapReserved = Math.max(maxHeapReserved, space.getLong("reservedSize"));
          continue;
        }
        boolean execution = kind.equals("jdk.ExecutionSample");
        if (!execution && !kind.equals("jdk.ObjectAllocationSample")) continue;
        var thread = event.getThread(execution ? "sampledThread" : "eventThread");
        String name = thread == null ? "unknown" : thread.getJavaName();
        if (execution) threads.merge(name, 1L, Long::sum);
        if (!"Server thread".equals(name)) continue;
        var stack = event.getStackTrace();
        List<String> methods =
            stack == null
                ? List.of()
                : stack.getFrames().stream()
                    .map(
                        f ->
                            f.getMethod().getType().getName().replace('/', '.')
                                + "."
                                + f.getMethod().getName())
                    .toList();
        var local = methods.stream().filter(m -> m.startsWith("dev.fourthdimension.")).toList();
        if (execution) {
          total++;
          if (stack != null && stack.isTruncated()) truncated++;
          if (!methods.isEmpty()) cpu.merge(methods.getFirst(), 1L, Long::sum);
          if (!local.isEmpty()) ownSamples++;
          new LinkedHashSet<>(local).forEach(m -> own.merge(m, 1L, Long::sum));
        } else {
          long amount = event.getLong("weight");
          weight += amount;
          allocation.merge(
              local.isEmpty() ? "(no Fourth Dimension frame in sampled stack)" : local.getFirst(),
              amount,
              Long::sum);
          classes.merge(event.getClass("objectClass").getName(), amount, Long::sum);
        }
      }
    }
    if (total == 0 || ticks.isEmpty())
      throw new IllegalStateException("Missing server samples: " + path);
    Map<String, Object> result = new TreeMap<>();
    result.put("all_execution_samples_by_thread", threads);
    result.put("server_execution_samples", total);
    result.put("server_samples_with_visible_fourthdimension_frame", ownSamples);
    result.put("truncated_server_execution_stacks", truncated);
    result.put("top_server_leaf_methods", top(cpu, 20));
    result.put("inclusive_fourthdimension_method_samples", top(own, 25));
    result.put("server_sampled_allocation_weight_bytes", weight);
    result.put("allocation_weight_by_nearest_fourthdimension_frame", top(allocation, 20));
    result.put("allocation_weight_by_class", top(classes, 20));
    result.put("native_server_tick_time_events", ticks);
    result.put("max_observed_heap_reserved_bytes", maxHeapReserved);
    result.put(
        "limitations",
        "Statistical full recorded stacks, not exact method times. Allocation weights are"
            + " estimates. Truncated frames and native solver threads are not attributed.");
    return result;
  }

  static String json(Object value) {
    if (value instanceof String s)
      return "\""
          + s.replace("\\", "\\\\")
              .replace("\"", "\\\"")
              .replace("\n", "\\n")
              .replace("\r", "\\r")
              .replace("\t", "\\t")
          + "\"";
    if (value instanceof Map<?, ?> map)
      return "{"
          + String.join(
              ",",
              map.entrySet().stream()
                  .map(e -> json(e.getKey().toString()) + ":" + json(e.getValue()))
                  .toList())
          + "}";
    if (value instanceof List<?> list)
      return "[" + String.join(",", list.stream().map(BenchmarkJfrSummary::json).toList()) + "]";
    return value.toString();
  }

  public static void main(String[] args) throws Exception {
    Map<String, Object> result = new TreeMap<>();
    try (var paths = Files.list(Path.of(args[0]))) {
      for (var path : paths.filter(p -> p.toString().endsWith(".jfr")).sorted().toList())
        result.put(path.getFileName().toString(), summarize(path));
    }
    System.out.println(json(result));
  }
}
