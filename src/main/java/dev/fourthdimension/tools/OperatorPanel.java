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
    var external = new CompoundTag();
    boolean externalOwner = RideAuthority.externalSelected(vehicle);
    String power="External propulsion; power telemetry unavailable",controller="Vehicle controls";
    var winch=ShuttleRuntime.owner(level,id);
    if(winch!=null){telemetry=RideTelemetry.atController(level,winch.getBlockPos(),id);controller="PAX shuttle winch";power=winch.getSpeed()+" RPM";}
    else if(externalOwner){
      controller=vehicle.authority.provider+" / "+vehicle.authority.owner;
      try {
        external=RideAuthority.externalOperatorStatus(level,vehicle);
        controller+=" / "+external.getString("phase");
        power=external.getString("charge");
        if(!external.getBoolean("dispatch_ready"))telemetry=telemetry.denyDispatch(external.getString("blocked_reason").isBlank()
            ? external.getString("request") : external.getString("blocked_reason"));
      }catch(IllegalArgumentException | IllegalStateException unavailable){telemetry=telemetry.denyDispatch(unavailable.getMessage());}
    }
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
    var result=telemetry.save();result.putBoolean("external",externalOwner);result.put("operator",external);result.putLong("revision",vehicle.revision);result.putString("controller",controller);result.putString("power",power);
    var route = FoundationData.get(level).rideLocal;
    if (dev.fourthdimension.integration.RideLocalOperations.ROUTE_ID.equals(route.routeId)
        && vehicle.family == dev.fourthdimension.contract.Family.ARROW) {
      var train = route.trains.get(vehicle.chassis);
      result.putString("route", route.routeId); result.putInt("route_trains", route.trains.size());
      result.putInt("route_block", train == null ? -1 : train.block);
      result.putBoolean("route_managed", dev.fourthdimension.integration.RideLocalOperations.isManaged(level, vehicle));
      if (externalOwner && external.getBoolean("route_managed")) {
        result.putString("route_denial", external.getString("route_denial"));
        result.putInt("route_block", external.getInt("route_block"));
      } else if (train == null) result.putString("route_denial", "Admit this complete Arrow train before dispatch");
      else if (train.unresolved || train.restored || train.braking)
        result.putString("route_denial", "Route reservation is braking or unresolved; recover before dispatch");
      if (!result.getString("route_denial").isBlank()) {
        result.putBoolean("dispatch", false);
        result.putString("denial", result.getString("route_denial"));
      }
    }
    String recovery=vehicle.recoveryCart!=null?(vehicle.stopping?"Recovery attached / braked":"Recovery towing"):
        vehicle.alignmentRequired?"Matching support realignment required":vehicle.shuttleDetached?"PAX detached for recovery":vehicle.maintenance?"Retained for maintenance":"No recovery attachment";
    result.putString("recovery",recovery);
    result.putBoolean("pax",winch!=null);result.putBoolean("authority",vehicle.authority.owner!=null);
    result.putBoolean("powered",PoweredInvertedYawVehicle.is(vehicle));result.putBoolean("flying",FlyingVehicle.is(vehicle)||NewGenerationFlyingVehicle.is(vehicle));
    result.putString("optional",externalOwner ? "Request: "+external.getString("request") : "Optional drive controls require an installed, bound provider; use vehicle controls to bind.");
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
