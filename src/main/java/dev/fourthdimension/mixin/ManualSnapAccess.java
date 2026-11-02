package dev.fourthdimension.mixin;

import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrackSnap;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.GraphHit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Pinned bridge to the base coaster's exact physical snap procedure for manual support repair. */
@Mixin(value = CoasterCartTrackSnap.class, remap = false)
public interface ManualSnapAccess {
  @Invoker("computeTravelNegatesEdgeTangent")
  static boolean fourthdimension$negatesEdgeTangent(
      ServerSubLevel chassis, Level level, GraphHit hit) {
    throw new AssertionError("Mixin invoker was not applied");
  }

  @Invoker("applySnap")
  static void fourthdimension$physicalSnap(
      ServerSubLevel chassis,
      ServerLevel level,
      SubLevelPhysicsSystem physics,
      GraphHit hit,
      Vec3 worldBearing,
      Vec3 plotBearing,
      boolean negateEdgeTangent) {
    throw new AssertionError("Mixin invoker was not applied");
  }
}
