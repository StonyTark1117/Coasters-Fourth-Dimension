package dev.fourthdimension.client.ponder;

import dev.fourthdimension.vehicle.CarrierSeatBlock;
import java.util.ArrayList;
import java.util.List;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Authored diagrams. Native runtime and physics acceptance use separate GameTests. */
final class FourthDimensionScenes {
  private FourthDimensionScenes() {}

  private static void start(SceneBuilder scene, String lesson) {
    scene.title(lesson, PonderLessons.ALL.get(lesson).title());
    scene.configureBasePlate(0, 0, 13);
    scene.scaleSceneView(.55f);
    scene.showBasePlate();
    scene.idle(10);
  }

  private static void text(SceneBuilder scene, String lesson, int index, Vec3 target) {
    scene
        .overlay()
        .showText(100)
        .text(PonderLessons.ALL.get(lesson).text().get(index))
        .independent(50)
        .attachKeyFrame();
    scene
        .overlay()
        .showOutline(
            PonderPalette.GREEN,
            "lesson_target",
            scene.getScene().getSceneBuildingUtil().select().position(BlockPos.containing(target)),
            100);
    scene.idle(110);
  }

  private static void controls(SceneBuilder scene, Vec3 target, String item, boolean sneak) {
    var control =
        scene
            .overlay()
            .showControls(target, Pointing.DOWN, 70)
            .rightClick()
            .withItem(new ItemStack(BuiltInRegistries.ITEM.get(FourthDimensionPonder.id(item))));
    if (sneak) control.whileSneaking();
  }

  private static Selection body(SceneBuildingUtil util) {
    return util.select().fromTo(2, 3, 2, 10, 4, 10);
  }

  private static List<ElementLink<WorldSectionElement>> carriers(
      SceneBuilder scene, SceneBuildingUtil util, boolean paired, boolean legacy, boolean lower) {
    scene.world().showSection(util.select().layer(1), Direction.DOWN);
    scene.world().showSection(util.select().layer(2), Direction.DOWN);
    scene.world().showSection(util.select().fromTo(4, 3, 2, 8, 4, 10), Direction.DOWN);
    var result = new ArrayList<ElementLink<WorldSectionElement>>();
    for (int z : paired ? new int[] {3, 9} : new int[] {6}) {
      for (int side : new int[] {-1, 1}) {
        int x = side < 0 ? 2 : 9;
        int startZ = legacy ? z - 1 : z;
        int endZ = legacy ? z + 1 : z;
        var link =
            scene
                .world()
                .showIndependentSection(
                    util.select().fromTo(x, 3, startZ, x + 1, 4, endZ), Direction.DOWN);
        // Pivots match the schematic axle, including the revised lower hinge.
        scene
            .world()
            .configureCenterOfRotation(
                link, new Vec3(side < 0 ? 3.5 : 9.5, lower ? 4 : 4.5, z + .5));
        result.add(link);
      }
    }
    scene.idle(20);
    return result;
  }

  private static void rotate(
      SceneBuilder scene, List<ElementLink<WorldSectionElement>> groups, double... angles) {
    for (int i = 0; i < groups.size(); i++)
      scene.world().rotateSection(groups.get(i), angles[i % angles.length], 0, 0, 80);
  }

  private static void freeFamily(
      SceneBuilder scene,
      SceneBuildingUtil util,
      String name,
      boolean paired,
      boolean legacy,
      boolean lower) {
    start(scene, name);
    var groups = carriers(scene, util, paired, legacy, lower);
    text(scene, name, 0, new Vec3(3, 3, 6));
    rotate(scene, groups, 115, -70, 55, -135);
    text(scene, name, 1, new Vec3(6, 2, 6));
    controls(scene, new Vec3(3, 4, 6), "rotation_tool", false);
    text(scene, name, 2, new Vec3(3, 4, 6));
    rotate(scene, groups, -115, 70, -55, 135);
    text(scene, name, 3, new Vec3(9, 3, 6));
    text(scene, name, 4, new Vec3(3, 3, 6));
    scene.markAsFinished();
  }

  static void matching(SceneBuilder scene, SceneBuildingUtil util) {
    start(scene, "matching");
    scene.world().showSection(util.select().layersFrom(1), Direction.DOWN);
    text(scene, "matching", 0, new Vec3(2, 1, 5));
    for (int x : new int[] {2, 4, 6, 8, 10})
      scene
          .overlay()
          .showOutline(
              PonderPalette.GREEN, "family_" + x, util.select().fromTo(x, 1, 2, x, 2, 10), 90);
    text(scene, "matching", 1, new Vec3(6, 2, 6));
    text(scene, "matching", 2, new Vec3(2, 1, 8));
    text(scene, "matching", 3, new Vec3(6, 1, 8));
    text(scene, "matching", 4, new Vec3(10, 2, 6));
    scene.markAsFinished();
  }

  static void construction(SceneBuilder scene, SceneBuildingUtil util) {
    start(scene, "construction");
    scene.world().showSection(util.select().position(6, 2, 1), Direction.DOWN);
    scene.world().showSection(util.select().position(6, 2, 11), Direction.DOWN);
    controls(scene, new Vec3(6.5, 2.5, 1.5), "arrow_track", false);
    text(scene, "construction", 0, new Vec3(6.5, 2.5, 1.5));
    controls(scene, new Vec3(6.5, 2.5, 11.5), "arrow_track", true);
    text(scene, "construction", 1, new Vec3(6.5, 2.5, 11.5));
    scene.world().showSection(util.select().fromTo(6, 2, 2, 6, 2, 10), Direction.DOWN);
    text(scene, "construction", 2, new Vec3(6, 2, 6));
    controls(scene, new Vec3(6, 2, 1), "arrow_car", false);
    scene
        .overlay()
        .showOutline(
            PonderPalette.GREEN, "corridor", util.select().fromTo(1, 1, 2, 11, 7, 10), 100);
    text(scene, "construction", 3, new Vec3(6, 3, 6));
    controls(scene, new Vec3(6, 2, 1), "rotation_tool", false);
    text(scene, "construction", 4, new Vec3(6, 2, 1));
    text(scene, "construction", 5, new Vec3(6, 2, 6));
    scene.markAsFinished();
  }

  static void arrow(SceneBuilder scene, SceneBuildingUtil util) {
    start(scene, "arrow");
    var groups = carriers(scene, util, false, false, false);
    text(scene, "arrow", 0, new Vec3(3, 3, 6));
    rotate(scene, groups, 90);
    text(scene, "arrow", 1, new Vec3(3, 4, 6));
    controls(scene, new Vec3(6, 2, 1), "rotation_tool", false);
    text(scene, "arrow", 2, new Vec3(6, 2, 1));
    rotate(scene, groups, 360);
    text(scene, "arrow", 3, new Vec3(9, 4, 6));
    rotate(scene, groups, -90);
    text(scene, "arrow", 4, new Vec3(3, 3, 6));
    text(scene, "arrow", 5, new Vec3(3, 3, 6));
    scene.markAsFinished();
  }

  static void legacy(SceneBuilder scene, SceneBuildingUtil util) {
    freeFamily(scene, util, "legacy", false, true, false);
  }

  static void freeSpin(SceneBuilder scene, SceneBuildingUtil util) {
    freeFamily(scene, util, "free_spin", true, false, false);
  }

  static void revised(SceneBuilder scene, SceneBuildingUtil util) {
    freeFamily(scene, util, "revised", true, false, true);
  }

  static void magnets(SceneBuilder scene, SceneBuildingUtil util) {
    start(scene, "magnets");
    var groups = carriers(scene, util, true, false, false);
    controls(scene, new Vec3(6, 2, 1), "magnetic_inducer", false);
    text(scene, "magnets", 0, new Vec3(6, 2, 1));
    controls(scene, new Vec3(6, 2, 1), "rotation_tool", false);
    text(scene, "magnets", 1, new Vec3(6, 2, 1));
    scene
        .overlay()
        .showOutline(PonderPalette.RED, "inducer", util.select().fromTo(4, 2, 3, 8, 2, 3), 100);
    rotate(scene, groups, 130, 0, 0, -95);
    text(scene, "magnets", 2, new Vec3(4, 2, 3));
    controls(scene, new Vec3(8, 2, 9), "magnetic_damper", false);
    scene
        .overlay()
        .showOutline(PonderPalette.BLUE, "damper", util.select().fromTo(4, 2, 9, 8, 2, 9), 100);
    rotate(scene, groups, 15, 0, 0, -10);
    text(scene, "magnets", 3, new Vec3(8, 2, 9));
    text(scene, "magnets", 4, new Vec3(3, 4, 3));
    text(scene, "magnets", 5, new Vec3(6, 1, 9));
    rotate(scene, groups, -145, 0, 0, 105);
    text(scene, "magnets", 6, new Vec3(9, 3, 9));
    scene.markAsFinished();
  }

  static void boarding(SceneBuilder scene, SceneBuildingUtil util) {
    start(scene, "boarding");
    var groups = carriers(scene, util, false, false, false);
    text(scene, "boarding", 0, new Vec3(6.5, 1.5, .5));
    scene.world().toggleRedstonePower(util.select().position(6, 1, 1));
    scene
        .world()
        .modifyBlock(
            new BlockPos(6, 1, 0),
            state -> state.setValue(dev.fourthdimension.station.BoardingStation.READY, true),
            false);
    text(scene, "boarding", 1, new Vec3(6.5, 1.5, .5));
    scene.overlay().showControls(new Vec3(3, 3, 6), Pointing.DOWN, 70).rightClick();
    text(scene, "boarding", 2, new Vec3(3, 3, 6));
    controls(scene, new Vec3(3, 3, 6), "rotation_tool", false);
    scene
        .world()
        .modifyBlocks(
            body(util),
            state ->
                state.getBlock() instanceof CarrierSeatBlock
                    ? state.setValue(CarrierSeatBlock.RESTRAINED, true)
                    : state,
            false);
    scene
        .world()
        .modifyBlock(
            new BlockPos(6, 1, 0),
            state -> state.setValue(dev.fourthdimension.station.BoardingStation.LOCKED, true),
            false);
    text(scene, "boarding", 3, new Vec3(6.5, 1.5, .5));
    scene.world().toggleRedstonePower(util.select().position(6, 1, 1));
    scene
        .world()
        .modifyBlock(
            new BlockPos(6, 1, 0),
            state -> state.setValue(dev.fourthdimension.station.BoardingStation.READY, false),
            false);
    rotate(scene, groups, 45);
    text(scene, "boarding", 4, new Vec3(6, 1, 6));
    rotate(scene, groups, -45);
    scene.world().toggleRedstonePower(util.select().position(6, 1, 1));
    scene
        .world()
        .modifyBlock(
            new BlockPos(6, 1, 0),
            state ->
                state
                    .setValue(dev.fourthdimension.station.BoardingStation.READY, true)
                    .setValue(dev.fourthdimension.station.BoardingStation.LOCKED, false),
            false);
    scene
        .world()
        .modifyBlocks(
            body(util),
            state ->
                state.getBlock() instanceof CarrierSeatBlock
                    ? state.setValue(CarrierSeatBlock.RESTRAINED, false)
                    : state,
            false);
    text(scene, "boarding", 5, new Vec3(3, 3, 6));
    text(scene, "boarding", 6, new Vec3(6.5, 1.5, .5));
    scene.markAsFinished();
  }

  static void pax(SceneBuilder scene, SceneBuildingUtil util) {
    start(scene, "pax");
    scene.world().showSection(util.select().fromTo(6, 1, 1, 6, 3, 11), Direction.DOWN);
    scene.world().showSection(util.select().position(4, 1, 6), Direction.DOWN);
    var catchLink =
        scene.world().showIndependentSection(util.select().position(5, 2, 6), Direction.DOWN);
    var chassis =
        scene
            .world()
            .showIndependentSection(util.select().fromTo(4, 4, 6, 8, 4, 6), Direction.DOWN);
    var left =
        scene
            .world()
            .showIndependentSection(util.select().fromTo(2, 3, 6, 3, 4, 6), Direction.DOWN);
    var right =
        scene
            .world()
            .showIndependentSection(util.select().fromTo(9, 3, 6, 10, 4, 6), Direction.DOWN);
    scene.world().configureCenterOfRotation(left, new Vec3(3.5, 4.5, 6.5));
    scene.world().configureCenterOfRotation(right, new Vec3(9.5, 4.5, 6.5));
    var train = List.of(chassis, left, right);
    text(scene, "pax", 0, new Vec3(6, 2, 3));
    controls(scene, new Vec3(6, 3, 6), "pax_car", false);
    text(scene, "pax", 1, new Vec3(6, 3, 6));
    scene.overlay().showControls(new Vec3(4, 1, 6), Pointing.DOWN, 70).rightClick().whileSneaking();
    text(scene, "pax", 2, new Vec3(4, 1, 6));
    scene.overlay().showControls(new Vec3(4, 1, 6), Pointing.DOWN, 70).rightClick();
    text(scene, "pax", 3, new Vec3(4, 1, 6));
    var lift = new Vec3(0, 2, -4);
    train.forEach(link -> scene.world().moveSection(link, lift, 80));
    scene.world().moveSection(catchLink, lift, 80);
    text(scene, "pax", 4, new Vec3(6, 5, 2));
    scene.world().moveSection(catchLink, new Vec3(0, -2, 4), 80);
    train.forEach(link -> scene.world().moveSection(link, new Vec3(0, 0, 8), 80));
    rotate(scene, List.of(left, right), 115, -70);
    text(scene, "pax", 5, new Vec3(6, 5, 10));
    train.forEach(link -> scene.world().moveSection(link, new Vec3(0, -2, -4), 80));
    rotate(scene, List.of(left, right), -115, 70);
    text(scene, "pax", 6, new Vec3(5, 2, 6));
    text(scene, "pax", 7, new Vec3(4, 1, 6));
    scene.markAsFinished();
  }
}
