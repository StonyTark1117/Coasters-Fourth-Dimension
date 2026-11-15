package dev.fourthdimension.integration;

import net.minecraft.resources.ResourceLocation;

/** Finite cosmetic chassis choices. These never select the provider's seat or physics styles. */
public enum SkinAppearance {
  ORIGINAL("original", "Original", null, null),
  INDUSTRIAL("industrial", "Industrial chassis", "industrial/sc_underbody_industrial", null),
  CLASSIC("classic", "Classic chassis", "classic_coaster/default/underbody", "classic_coaster/default/wheel"),
  ANTARCTICA("antarctica", "Antarctica chassis", "classic_coaster/default/underbody", "classic_coaster/default/antarctica/wheel"),
  HOTDOG("hotdog", "Hotdog chassis", "classic_coaster/default/underbody", "classic_coaster/default/hotdog/wheel");

  public final String id, label;
  public final ResourceLocation underbody, wheel;
  SkinAppearance(String id,String label,String underbody,String wheel) {
    this.id=id;this.label=label;this.underbody=model(underbody);this.wheel=model(wheel);
  }
  private static ResourceLocation model(String path) {
    return path==null?null:ResourceLocation.fromNamespaceAndPath("create_coaster_skins","coaster/"+path);
  }
  public static SkinAppearance read(String id) {
    for(var choice:values())if(choice.id.equals(id))return choice;
    return ORIGINAL;
  }
  public static SkinAppearance require(String id) {
    for(var choice:values())if(choice.id.equals(id))return choice;
    throw new IllegalArgumentException("Unsupported appearance. Full Skin bodies and seat/physics styles cannot replace a 4D vehicle.");
  }
}
