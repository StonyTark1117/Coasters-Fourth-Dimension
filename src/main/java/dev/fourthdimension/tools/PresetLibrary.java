package dev.fourthdimension.tools;

import java.io.IOException;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** One bounded local library; names never become paths and invalid files are never overwritten. */
public final class PresetLibrary {
  private PresetLibrary() {}
  public static List<SettingPreset> read(Path file) throws IOException {
    if (!Files.exists(file)) return List.of();
    if (Files.size(file) > 800000) throw new IOException("Preset library exceeds 800 KB");
    var lines = Files.readAllLines(file, StandardCharsets.UTF_8);
    if (lines.size() > 64) throw new IOException("At most 64 saved presets");
    var result = new ArrayList<SettingPreset>();
    for (var line : lines) if (!line.isBlank()) result.add(SettingPreset.parse(line));
    return List.copyOf(result);
  }
  public static void save(Path file, SettingPreset preset) throws IOException {
    var entries = new ArrayList<>(read(file));
    entries.removeIf(p -> p.name().equals(preset.name()));
    entries.add(preset);
    if (entries.size() > 64) throw new IOException("Library full: rename an existing preset to replace it");
    var lines = entries.stream().map(SettingPreset::exportText).toList();
    Files.createDirectories(file.getParent());
    var scratch = Files.createTempFile(file.getParent(), "presets-", ".new");
    try {
      Files.write(scratch, lines, StandardCharsets.UTF_8);
      Files.move(scratch, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    } finally { Files.deleteIfExists(scratch); }
  }
}
