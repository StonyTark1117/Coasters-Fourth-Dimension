package dev.fourthdimension.vehicle;

import net.minecraft.world.entity.Entity;

/** Prone geometry is local to the rigid native body; SeatEntity stays in its seat block. */
public final class FlyingBikeRiderPose {
  public static final double EYE_HEIGHT=.70, HEAD_Z=-.35;
  private FlyingBikeRiderPose(){}
  public static Boarding.Binding binding(Entity rider){
    if(rider==null||!rider.isAlive()||rider.getVehicle()==null)return null;
    var b=Boarding.at(rider.level(),rider.getVehicle().blockPosition());
    return b!=null&&FlyingBikeVehicle.is(b.vehicle())?b:null;
  }
}
