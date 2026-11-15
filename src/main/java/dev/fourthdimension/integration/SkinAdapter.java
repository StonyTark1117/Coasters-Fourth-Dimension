package dev.fourthdimension.integration;

import dev.fourthdimension.runtime.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.server.level.ServerPlayer;

/** Saved cosmetic intent uses the existing authoritative vehicle snapshot, never native skin tags. */
public final class SkinAdapter {
  private SkinAdapter() {}
  public static FoundationData.Vehicle owner(SubLevel body) {
    if(body==null)return null;
    var data=EligibilityHooks.data(body.getLevel());
    if(data==null)return null;
    var direct=data.vehicles.get(body.getUniqueId());
    if(direct!=null)return direct;
    for(var vehicle:data.vehicles.values())
      if(vehicle.carriers.stream().anyMatch(c->c.body.equals(body.getUniqueId())))return vehicle;
    return null;
  }
  public static boolean supported(FoundationData.Vehicle vehicle) {
    return !FoundationData.VARIANT.equals(vehicle.variant) && !PipelineVehicle.is(vehicle);
  }
  public static void select(ServerPlayer player,FoundationData.Vehicle vehicle,String id) {
    var choice=SkinAppearance.require(id);
    if(choice!=SkinAppearance.ORIGINAL) {
      if(!supported(vehicle))throw new IllegalArgumentException("This vehicle keeps its purpose-built Original chassis.");
      if(!OptionalIntegrations.installed("create_coaster_skins").mayInspect())
        throw new IllegalArgumentException("Install the supported Coaster Skin 1.1.0 release on the server to select this appearance.");
    }
    for(var car:VehicleOperations.consist(player.serverLevel(),vehicle))
      if(!Boarding.ready(player.serverLevel(),car) || car.carriers.stream().anyMatch(c->!c.occupants.isEmpty()))
        throw new IllegalArgumentException("Change appearance at an empty, stopped, captured boarding position for the whole train.");
    vehicle.appearance=choice;
    vehicle.revision++;
    FoundationData.get(player.serverLevel()).setDirty();
  }
  public static Object nativeSkin() {
    try{return Class.forName("medev.coaster.skins.skin.SkinCatalog").getField("NATIVE").get(null);}
    catch(ReflectiveOperationException failure){throw new IllegalStateException("Coaster Skin native fallback unavailable",failure);}
  }
}
