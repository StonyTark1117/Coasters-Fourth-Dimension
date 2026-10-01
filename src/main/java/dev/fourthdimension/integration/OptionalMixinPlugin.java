package dev.fourthdimension.integration;

import java.util.*;
import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.*;

/** Loader metadata only: no optional class may be initialized during mixin discovery. */
public final class OptionalMixinPlugin implements IMixinConfigPlugin {
  @Override
  public boolean shouldApplyMixin(String target, String mixin) {
    if ((mixin.endsWith(".EngineeredDriveMixin") || mixin.endsWith(".EngineeredMassMixin"))) {
      var mods = LoadingModList.get();
      return mods != null
          && mods.getMods().stream()
              .anyMatch(
                  mod ->
                      mod.getModId().equals("coastersengineered")
                          && mod.getVersion().toString().equals("1.1.1"));
    }
    if (mixin.substring(mixin.lastIndexOf('.') + 1).startsWith("Additions")) {
      var mods = LoadingModList.get();
      return mods != null
          && mods.getMods().stream()
              .anyMatch(
                  mod ->
                      mod.getModId().equals("coasterfins")
                          && mod.getVersion().toString().equals("0.2.0"));
    }
    if (mixin.endsWith(".MagnetizedDriveMixin")) {
      var mods = LoadingModList.get();
      return mods != null
          && mods.getMods().stream()
              .anyMatch(
                  mod ->
                      mod.getModId().equals("coastersmagnetized")
                          && mod.getVersion().toString().equals("1.1.0"));
    }
    if (mixin.substring(mixin.lastIndexOf('.') + 1).startsWith("Immersion")) {
      var mods = LoadingModList.get();
      return mods != null
          && mods.getMods().stream()
              .anyMatch(
                  mod ->
                      mod.getModId().equals("coasters_immersion")
                          && mod.getVersion().toString().equals("0.2.3"));
    }
    return false;
  }

  @Override
  public void onLoad(String mixinPackage) {}

  @Override
  public String getRefMapperConfig() {
    return null;
  }

  @Override
  public void acceptTargets(Set<String> own, Set<String> others) {}

  @Override
  public List<String> getMixins() {
    return null;
  }

  @Override
  public void preApply(String name, ClassNode node, String mixin, IMixinInfo info) {}

  @Override
  public void postApply(String name, ClassNode node, String mixin, IMixinInfo info) {}
}
