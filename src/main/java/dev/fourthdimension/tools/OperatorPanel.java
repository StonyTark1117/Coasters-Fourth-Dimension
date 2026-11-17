package dev.fourthdimension.tools;

import dev.fourthdimension.editor.EditorNetwork;
import dev.fourthdimension.integration.*;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.*;
import dev.fourthdimension.shuttle.ShuttleRuntime;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

/** Read-only whole-consist sampling; commands continue through EditorNetwork's guarded actions. */
public final class OperatorPanel {
  private OperatorPanel() {}
  public static CompoundTag sample(ServerPlayer player,UUID id){
    var vehicle=VehicleOperations.accessible(player,id);var level=player.serverLevel();
    var telemetry=RideTelemetry.sample(level,id);
    String power="External propulsion; power telemetry unavailable",controller="Vehicle controls";
    var winch=ShuttleRuntime.owner(level,id);
    if(winch!=null){telemetry=RideTelemetry.atController(level,winch.getBlockPos(),id);controller="PAX shuttle winch";power=winch.getSpeed()+" RPM";}
    else if(vehicle.authority.owner!=null){
      controller=vehicle.authority.provider+" / "+(vehicle.authority.suspended?"suspended":vehicle.authority.held?"held":"active");
      var pos=vehicle.authority.controller;
      if(pos==null||!level.hasChunkAt(pos))telemetry=telemetry.denyDispatch("Bound controller is unavailable");
      else {
        var block=level.getBlockEntity(pos);
        if(block instanceof com.simibubi.create.content.kinetics.base.KineticBlockEntity k)power=k.getSpeed()+" RPM"+(k.isOverStressed()?" / overstressed":"");
        if(block instanceof RideAuthority.Controller control){try{control.validateRelease(level,vehicle);}catch(IllegalArgumentException e){telemetry=telemetry.denyDispatch(e.getMessage());}}
        else telemetry=telemetry.denyDispatch("Restore the bound authority controller");
        if(vehicle.authority.suspended)telemetry=telemetry.denyDispatch(vehicle.authority.reason);
      }
    }
    if(PoweredInvertedYawVehicle.is(vehicle)){
      var governor=PoweredInvertedYawRuntime.governor(level,vehicle);
      power=governor==null?"Powered governor unavailable":governor.inputRpm()+" input RPM";
    }
    var result=telemetry.save();result.putLong("revision",vehicle.revision);result.putString("controller",controller);result.putString("power",power);
    String recovery=vehicle.recoveryCart!=null?(vehicle.stopping?"Recovery attached / braked":"Recovery towing"):
        vehicle.alignmentRequired?"Matching support realignment required":vehicle.shuttleDetached?"PAX detached for recovery":vehicle.maintenance?"Retained for maintenance":"No recovery attachment";
    result.putString("recovery",recovery);
    result.putBoolean("pax",winch!=null);result.putBoolean("authority",vehicle.authority.owner!=null);
    result.putBoolean("powered",PoweredInvertedYawVehicle.is(vehicle));result.putBoolean("flying",FlyingVehicle.is(vehicle)||NewGenerationFlyingVehicle.is(vehicle));
    result.putString("optional","Optional drive controls require an installed, bound provider; use vehicle controls to bind.");
    return result;
  }
  public static void shuttle(ServerPlayer player,UUID id,long revision,boolean recover){
    var v=VehicleOperations.accessible(player,id);
    if(v.revision!=revision)throw new IllegalArgumentException("Vehicle changed; review current panel state");
    var winch=ShuttleRuntime.owner(player.serverLevel(),id);
    if(winch==null||!player.serverLevel().mayInteract(player,winch.getBlockPos())||player.distanceToSqr(winch.getBlockPos().getCenter())>64)
      throw new IllegalArgumentException("Stand within eight blocks of the bound shuttle winch");
    if(recover)winch.recover();else winch.resume();
    // Consume the displayed revision, including for direct winch actions.
    v.revision++;FoundationData.get(player.serverLevel()).setDirty();
  }
}
