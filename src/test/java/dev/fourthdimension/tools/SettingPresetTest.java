package dev.fourthdimension.tools;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SettingPresetTest {
  @TempDir Path root;
  private SettingPreset preset(String name){return new SettingPreset(name,"arrow_controlled","arrow_controlled_v1",SettingPreset.Kind.PROFILE,"0:0:0;48:360:0");}
  @Test void roundTripNamesAndSettings(){var p=preset("Ocean | 360° ride");assertEquals(p,SettingPreset.parse(p.exportText()));}
  @Test void rejectsIncompatibleTargetsAndVersions(){var p=preset("Test");assertThrows(IllegalArgumentException.class,()->p.requireTarget("sns_free_spin",p.variant(),false));assertThrows(IllegalArgumentException.class,()->p.requireTarget(p.family(),p.variant(),true));assertThrows(IllegalArgumentException.class,()->SettingPreset.parse(p.exportText().replace("FD_PRESET|1|","FD_PRESET|2|")));assertThrows(IllegalArgumentException.class,()->SettingPreset.parse("x".repeat(12001)));assertThrows(IllegalArgumentException.class,()->preset("bad\nname"));assertThrows(IllegalArgumentException.class,()->SettingPreset.parse("FD_PRESET|1|_w|arrow|arrow|PROFILE|YQ"));}
  @Test void namesCannotEscapeLibraryAndReplacementIsExact() throws Exception{var path=root.resolve("library/presets.txt");PresetLibrary.save(path,preset("../escape"));assertFalse(Files.exists(root.resolve("escape")));PresetLibrary.save(path,preset("Keep"));PresetLibrary.save(path,preset("../escape"));assertEquals(2,PresetLibrary.read(path).size());assertEquals("Keep",PresetLibrary.read(path).getFirst().name());}
  @Test void corruptLibraryIsPreserved() throws Exception{var path=root.resolve("presets.txt");Files.writeString(path,"FD_PRESET|99|bad\n");assertThrows(IllegalArgumentException.class,()->PresetLibrary.save(path,preset("New")));assertEquals("FD_PRESET|99|bad\n",Files.readString(path));}
  @Test void finiteLibraryAndPaletteFallback() throws Exception{var path=root.resolve("presets.txt");for(int i=0;i<64;i++)PresetLibrary.save(path,preset("Preset "+i));assertThrows(java.io.IOException.class,()->PresetLibrary.save(path,preset("Overflow")));assertEquals(64,PresetLibrary.read(path).size());assertEquals(VehiclePalette.ORIGINAL,VehiclePalette.read("future_provider"));assertThrows(IllegalArgumentException.class,()->VehiclePalette.require("future_provider"));}
}
