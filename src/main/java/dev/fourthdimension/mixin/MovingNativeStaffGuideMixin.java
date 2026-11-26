package dev.fourthdimension.mixin;

import dev.fourthdimension.moving.MovingNativeAdapter;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Establish scoped wheel/rail contact exclusion for the authenticated fixed-world dock bridge. */
@Mixin(targets="dev.silvergold.simulatedcoasters.track.cart.CoasterTrackStaffRailStabilizer",remap=false)
public abstract class MovingNativeStaffGuideMixin {
  @Inject(method="updateDuringStaffRailSnap",at=@At("TAIL"),remap=false)
  private static void fourthdimension$rollingContact(ServerSubLevel chassis,ServerLevel level,
      SubLevelPhysicsSystem physics,CoasterPathEdge edge,Vector3d point,
      CoasterPathTrackFrame.TrackBasis basis,Vec3 bearing,Vec3 center,CallbackInfo ci) {
    MovingNativeAdapter.nativeGuideUpdated(chassis,level,edge);
  }
}
