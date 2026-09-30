package dev.fourthdimension.track;

import dev.fourthdimension.FourthDimension;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.*;

public final class TrackItems {
  private TrackItems() {}

  public static final DeferredRegister.Items ITEMS =
      DeferredRegister.createItems(FourthDimension.ID);
  public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
      DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, FourthDimension.ID);
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<GlobalPos>> SELECTION =
      COMPONENTS.register(
          "track_selection",
          () ->
              DataComponentType.<GlobalPos>builder()
                  .persistent(GlobalPos.CODEC)
                  .networkSynchronized(GlobalPos.STREAM_CODEC)
                  .build());
  public static final DeferredItem<dev.fourthdimension.vehicle.VehicleItem> ARROW_CAR =
      ITEMS.register(
          "arrow_car",
          () ->
              new dev.fourthdimension.vehicle.VehicleItem(
                  new Item.Properties().stacksTo(1), TrackDesign.ARROW));
  public static final DeferredItem<dev.fourthdimension.vehicle.VehicleItem> LEGACY_CAR =
      ITEMS.register(
          "legacy_zacspin_car",
          () ->
              new dev.fourthdimension.vehicle.VehicleItem(
                  new Item.Properties().stacksTo(1), TrackDesign.LEGACY));
  public static final DeferredItem<dev.fourthdimension.vehicle.VehicleItem> FREE_SPIN_CAR =
      ITEMS.register(
          "free_spin_car",
          () ->
              new dev.fourthdimension.vehicle.VehicleItem(
                  new Item.Properties().stacksTo(1), TrackDesign.FREE_SPIN));
  public static final DeferredItem<FamilyTrackItem> FREE_SPIN =
      ITEMS.register(
          "free_spin_track",
          () -> new FamilyTrackItem(new Item.Properties(), TrackDesign.FREE_SPIN));
  public static final DeferredItem<dev.fourthdimension.vehicle.VehicleItem> REVISED_CAR =
      ITEMS.register(
          "revised_zacspin_car",
          () ->
              new dev.fourthdimension.vehicle.VehicleItem(
                  new Item.Properties().stacksTo(1), TrackDesign.REVISED));
  public static final DeferredItem<FamilyTrackItem> REVISED =
      ITEMS.register(
          "revised_zacspin_track",
          () -> new FamilyTrackItem(new Item.Properties(), TrackDesign.REVISED));
  public static final DeferredItem<dev.fourthdimension.vehicle.VehicleItem> PAX_CAR =
      ITEMS.register(
          "pax_car",
          () ->
              new dev.fourthdimension.vehicle.VehicleItem(
                  new Item.Properties().stacksTo(1), TrackDesign.PAX));
  public static final DeferredItem<FamilyTrackItem> PAX =
      ITEMS.register(
          "pax_track", () -> new FamilyTrackItem(new Item.Properties(), TrackDesign.PAX));
  public static final DeferredItem<dev.fourthdimension.editor.RotationTool> ROTATION_TOOL =
      ITEMS.register(
          "rotation_tool",
          () -> new dev.fourthdimension.editor.RotationTool(new Item.Properties().stacksTo(1)));
  public static final DeferredItem<dev.fourthdimension.magnetic.MagneticEquipmentItem> INDUCER =
      ITEMS.register(
          "magnetic_inducer",
          () ->
              new dev.fourthdimension.magnetic.MagneticEquipmentItem(
                  new Item.Properties(), dev.fourthdimension.physics.MagneticSpan.Mode.INDUCE));
  public static final DeferredItem<dev.fourthdimension.magnetic.MagneticEquipmentItem> DAMPER =
      ITEMS.register(
          "magnetic_damper",
          () ->
              new dev.fourthdimension.magnetic.MagneticEquipmentItem(
                  new Item.Properties(), dev.fourthdimension.physics.MagneticSpan.Mode.DAMP));
  public static final DeferredItem<FamilyTrackItem> ARROW =
      ITEMS.register(
          "arrow_track", () -> new FamilyTrackItem(new Item.Properties(), TrackDesign.ARROW));
  public static final DeferredItem<FamilyTrackItem> LEGACY =
      ITEMS.register(
          "legacy_zacspin_track",
          () -> new FamilyTrackItem(new Item.Properties(), TrackDesign.LEGACY));
}
