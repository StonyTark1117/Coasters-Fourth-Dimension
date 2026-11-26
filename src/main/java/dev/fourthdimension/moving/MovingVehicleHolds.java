package dev.fourthdimension.moving;

import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.shuttle.ShuttleRuntime;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.server.level.ServerLevel;

/** Ownership transfer of fixed-world travel holds; rider/carrier articulation stays installed. */
public final class MovingVehicleHolds {
  private MovingVehicleHolds() {}

  public static void validate(ServerLevel level,MovingAssemblyState state) {
    for(var id:state.cars){
      var winch=ShuttleRuntime.owner(level,id);
      if(winch!=null&&!state.cars.containsAll(winch.cars))
        throw new IllegalArgumentException("Capture the complete winch train before transferring its support");
    }
  }

  /** Called only after all native bed-to-body constraints have been installed successfully. */
  public static void releaseAfterCapture(ServerLevel level,MovingAssemblyState state) {
    var container=SubLevelContainer.getContainer(level);var data=FoundationData.get(level);
    for(var id:state.cars){
      if(!(container.getSubLevel(id) instanceof ServerSubLevel parent))continue;
      DiveRuntime.releaseMovingWorldHold(parent);
      PipelineRuntime.releaseMovingWorldHold(parent);
      StandingRuntime.releaseMovingWorldHold(parent);
      MotorbikeRuntime.releaseMovingWorldHold(parent);
      NewGenerationFlyingRuntime.releaseMovingWorldHold(parent);
      var v=data.vehicles.get(id);
      if(v!=null){
        PoweredInvertedYawRuntime.releaseHold(level,v);
        if(RollerBallVehicle.is(v))RollerBallRuntime.releaseHold(level,v);
      }
      var winch=ShuttleRuntime.owner(level,id);
      if(winch!=null)ShuttleRuntime.releaseForMovingSupport(winch);
    }
    MovingPaxRecovery.captured(level,state);
  }
}
