package dev.fourthdimension.vehicle;

import net.minecraft.world.entity.Entity;

/** Shared identity test for native seat attachment and client-only limb presentation. */
public final class MotorbikeRiderPose {
  private MotorbikeRiderPose(){}
  public static Boarding.Binding binding(Entity rider){
    if(rider==null||!rider.isAlive()||rider.getVehicle()==null)return null;
    var b=Boarding.at(rider.level(),rider.getVehicle().blockPosition());
    return b!=null&&MotorbikeVehicle.is(b.vehicle())?b:null;
  }
}
