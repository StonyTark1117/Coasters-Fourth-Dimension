package dev.fourthdimension.vehicle;

import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.mixinhelpers.camera.camera_rotation.EntitySubLevelRotationHelper.Type;
import dev.ryanhcode.sable.sublevel.*;
import java.util.function.Function;
import net.minecraft.world.entity.Entity;
import org.joml.Quaterniond;

/** Neutral head frame only. Body, eye origin and passenger attachment stay on the physical child. */
public final class FlyingRiderPose {
  private FlyingRiderPose(){}
  public static Boarding.Binding binding(Entity rider){
    if(rider==null||!rider.isAlive()||rider.getVehicle()==null)return null;
    var b=Boarding.at(rider.level(),rider.getVehicle().blockPosition());
    return b!=null&&(FlyingVehicle.is(b.vehicle())||NewGenerationFlyingVehicle.is(b.vehicle()))?b:null;
  }
  public static double phi(Entity entity,Function<SubLevel,Pose3dc> poses){
    var b=binding(entity);if(b==null)return Double.NaN;
    var container=SubLevelContainer.getContainer(entity.level());if(container==null)return Double.NaN;
    var parentId=NewGenerationFlyingVehicle.is(b.vehicle())?b.vehicle().yaw.deck:b.vehicle().chassis;
    if(parentId==null)return Double.NaN;
    var parent=container.getSubLevel(parentId);var child=container.getSubLevel(b.carrier().body);
    if(parent==null||child==null||parent.isRemoved()||child.isRemoved())return Double.NaN;
    var relative=new Quaterniond(poses.apply(parent).orientation()).conjugate().mul(poses.apply(child).orientation());
    return Math.clamp(-2*Math.atan2(relative.x,relative.w),0,Math.PI/2);
  }
  public static Quaterniond orientation(Entity entity,Function<SubLevel,Pose3dc> poses,Type type,Quaterniond inherited){
    if(type!=Type.CAMERA||inherited==null)return inherited;
    double phi=phi(entity,poses);return Double.isFinite(phi)?new Quaterniond(inherited).rotateX(phi):inherited;
  }
}
