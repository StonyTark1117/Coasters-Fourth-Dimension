package dev.fourthdimension.tools;

/** Original upholstery choices; structural metal, safety lamps and restraint materials stay authored. */
public enum VehiclePalette {
  ORIGINAL("Original", "", 0xb9bec6), OCEAN("Ocean", "cyan", 0x169cad),
  CRIMSON("Crimson", "red", 0xb83232), FOREST("Forest", "green", 0x397c39),
  SUNSET("Sunset", "orange", 0xe58a28);
  public final String label, material;
  public final int swatch;
  VehiclePalette(String label,String material,int swatch){this.label=label;this.material=material;this.swatch=swatch;}
  public static VehiclePalette require(String id){try{return valueOf(id);}catch(RuntimeException e){throw new IllegalArgumentException("Unknown original palette");}}
  public static VehiclePalette read(String id){try{return require(id);}catch(IllegalArgumentException e){return ORIGINAL;}}
}
