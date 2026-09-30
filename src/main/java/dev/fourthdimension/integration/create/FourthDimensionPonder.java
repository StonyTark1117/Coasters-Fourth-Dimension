package dev.fourthdimension.integration.create;

import dev.fourthdimension.FourthDimension;
import dev.fourthdimension.track.TrackDesign;
import dev.fourthdimension.vehicle.CarrierSeatBlock;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.registration.*;
import net.createmod.ponder.api.scene.*;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** Original schematic scenes: animation explains operation; native ride demos verify dynamics. */
@EventBusSubscriber(modid = FourthDimension.ID, value = Dist.CLIENT)
public final class FourthDimensionPonder implements PonderPlugin {
  @SubscribeEvent
  public static void setup(FMLClientSetupEvent event) {
    event.enqueueWork(() -> PonderIndex.addPlugin(new FourthDimensionPonder()));
  }

  @Override
  public String getModId() {
    return FourthDimension.ID;
  }

  @Override
  public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
    for (var design : TrackDesign.values()) {
      String item =
          switch (design) {
            case ARROW -> "arrow";
            case LEGACY -> "legacy_zacspin";
            case FREE_SPIN -> "free_spin";
            case REVISED -> "revised_zacspin";
            case PAX -> "pax";
          };
      helper
          .forComponents(id(item + "_track"), id(item + "_car"))
          .addStoryBoard(
              "family_" + design.name().toLowerCase(java.util.Locale.ROOT),
              (scene, util) -> family(scene, util, design));
    }
    helper
        .forComponents(id("boarding_station"), id("rotation_tool"))
        .addStoryBoard("family_arrow", FourthDimensionPonder::station);
    helper
        .forComponents(id("shuttle_winch"))
        .addStoryBoard("family_pax", (scene, util) -> family(scene, util, TrackDesign.PAX));
    helper
        .forComponents(id("magnetic_inducer"), id("magnetic_damper"))
        .addStoryBoard(
            "family_free_spin", (scene, util) -> family(scene, util, TrackDesign.FREE_SPIN));
  }

  private static ResourceLocation id(String path) {
    return ResourceLocation.fromNamespaceAndPath(FourthDimension.ID, path);
  }

  private static String name(TrackDesign design) {
    return switch (design) {
      case ARROW -> "Arrow controlled rotation";
      case LEGACY -> "Legacy ZacSpin";
      case FREE_SPIN -> "Magnetic Free Spin";
      case REVISED -> "Revised ZacSpin";
      case PAX -> "PAX catch, lift and release";
    };
  }

  private static void explain(SceneBuilder scene, String text, Vec3 point, int duration) {
    var overlay = scene.overlay().showText(duration).text(text).attachKeyFrame();
    if (point.y < 3) overlay.independent(60);
    else overlay.pointAt(point).placeNearTarget();
    scene.idle(duration + 10);
  }

  private static void base(SceneBuilder scene, SceneBuildingUtil util) {
    scene.configureBasePlate(0, 0, 13);
    scene.scaleSceneView(.65f);
    scene.showBasePlate();
    scene.world().showSection(util.select().fromTo(0, 1, 0, 12, 2, 12), Direction.DOWN);
  }

  private static void family(SceneBuilder scene, SceneBuildingUtil util, TrackDesign design) {
    scene.title("family_" + design.name().toLowerCase(java.util.Locale.ROOT), name(design));
    base(scene, util);
    explain(
        scene,
        "Build with the matching family track and car. The colored strip is a schematic track"
            + " centerline.",
        new Vec3(6.5, 2, 6.5),
        90);
    int[] rows = design.pairedRows() ? new int[] {3, 9} : new int[] {6};
    var wings =
        new java.util.ArrayList<
            net.createmod.ponder.api.element.ElementLink<
                net.createmod.ponder.api.element.WorldSectionElement>>();
    for (int x : new int[] {3, 9})
      for (int z : rows) {
        var wing =
            scene
                .world()
                .showIndependentSection(
                    util.select().fromTo(x - 1, 3, z - 1, x + 1, 5, z + 1), Direction.DOWN);
        scene.world().configureCenterOfRotation(wing, new Vec3(x + .5, 4.5, z + .5));
        wings.add(wing);
      }
    explain(
        scene,
        design.pairedRows()
            ? "Four independent carrier pairs share one chassis. Leave the full rotating envelope"
                + " clear."
            : design == TrackDesign.LEGACY
                ? "Two independent four-seat carriers rotate freely. Leave platforms outside their"
                    + " swept envelope."
                : "Two independent two-seat carriers share each chassis. Keep the rotating envelope"
                    + " clear.",
        new Vec3(3.5, 4.5, 6.5),
        90);
    if (design == TrackDesign.PAX) {
      explain(
          scene,
          "Bind up to four coupled PAX cars to a powered Shuttle Winch. Close every restraint"
              + " before starting.",
          new Vec3(1.5, 1.5, 1.5),
          100);
      var catchBody =
          scene.world().showIndependentSection(util.select().position(6, 3, 2), Direction.DOWN);
      scene.world().moveSection(catchBody, new Vec3(0, 2, 3), 60);
      explain(
          scene,
          "The mechanical catch lifts the train with carriers captured. At release, gravity drives"
              + " the train.",
          new Vec3(6.5, 4, 5),
          90);
    }
    int i = 0;
    for (var wing : wings)
      scene
          .world()
          .rotateSection(
              wing, design == TrackDesign.ARROW ? 360 : (++i % 2 == 0 ? -145 : 210), 0, 0, 80);
    explain(
        scene,
        switch (design) {
          case ARROW ->
              "Author rotation keys before placing cars. Adjacent sections must match unwrapped"
                  + " angle and slope.";
          case LEGACY ->
              "Gravity, motion and loading turn the carriers. Tune damping and ballast only while"
                  + " empty and captured.";
          case FREE_SPIN ->
              "Orange inducer spans exchange forward energy for spin. Cyan dampers reduce rotation."
                  + " Targets select carrier pairs.";
          case REVISED ->
              "The revised carrier uses an offset pivot and more clearance. Its four-pair layout is"
                  + " an original game approximation.";
          case PAX ->
              "Carriers rotate after release. Restraints stay closed through every outward and"
                  + " return station pass.";
        },
        new Vec3(9.5, 4.5, 6.5),
        110);
    explain(
        scene,
        design == TrackDesign.PAX
            ? "Wait for final capture before unloading. An interrupted cycle requires inspection"
                + " and explicit resume or recovery."
            : "Stop, capture and align all carriers before boarding. Close restraints and release"
                + " maintenance retention before dispatch.",
        new Vec3(1.5, 1.5, 1.5),
        110);
    scene
        .overlay()
        .showText(100)
        .text(
            "Appearance never changes track eligibility. A locked car still requires its matching"
                + " functional track.")
        .colored(PonderPalette.RED)
        .pointAt(new Vec3(6.5, 2, 6.5));
    scene.idle(110);
    scene.markAsFinished();
  }

  private static void station(SceneBuilder scene, SceneBuildingUtil util) {
    scene.title("station_operations", "Boarding and dispatch interlocks");
    base(scene, util);
    scene.world().showSection(util.select().fromTo(0, 3, 0, 12, 5, 12), Direction.DOWN);
    explain(
        scene,
        "Use the Boarding Station to bind a nearby car. Sneak-use rebinds it; normal operation"
            + " requires the car within eight blocks.",
        new Vec3(1.5, 1.5, 1.5),
        100);
    explain(
        scene,
        "Rear power requests capture. The front signal reports boarding alignment; the right"
            + " reports closed restraints.",
        new Vec3(1.5, 1.5, 1.5),
        100);
    scene
        .world()
        .modifyBlocks(
            util.select().fromTo(0, 3, 0, 12, 5, 12),
            state ->
                state.hasProperty(CarrierSeatBlock.RESTRAINED)
                    ? state.setValue(CarrierSeatBlock.RESTRAINED, true)
                    : state,
            false);
    explain(
        scene,
        "Close restraints using the Rotation Tool. Dispatch requires every linked car aligned,"
            + " restrained, supported and released from maintenance retention.",
        new Vec3(3.5, 4.5, 6.5),
        110);
    explain(
        scene,
        "Remove rear power to request dispatch. Use goggles or a Display Link for dispatch"
            + " readiness and the reason a request is blocked.",
        new Vec3(1.5, 1.5, 1.5),
        110);
    explain(
        scene,
        "Operate PAX through its Shuttle Winch. Ordinary station redstone cannot interrupt its"
            + " catch, lift and return sequence.",
        new Vec3(1.5, 1.5, 1.5),
        100);
    scene.markAsFinished();
  }
}
