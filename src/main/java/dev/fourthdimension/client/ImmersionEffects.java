package dev.fourthdimension.client;

import dev.fourthdimension.integration.OptionalCalls;
import dev.fourthdimension.integration.OptionalIntegrations;
import dev.fourthdimension.runtime.RiderComfortSettings;
import dev.fourthdimension.vehicle.Boarding;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Quaterniond;

/** Optional presentation only: never writes the carrier, passenger or mouse-look state. */
@EventBusSubscriber(modid="fourthdimension", value=Dist.CLIENT)
public final class ImmersionEffects {
  private static final String ROOT="dev.mito.coastersimmersion.";
  private static Entity previousSeat;
  private static long filterCalls;
  private ImmersionEffects() {}

  public static boolean enabled() {
    return OptionalIntegrations.installed("coasters_immersion").mayInspect();
  }

  public static SubLevel carrier(Entity rider) {
    if(rider==null||!rider.isAlive()||rider.getVehicle()==null)return null;
    var binding=Boarding.at(rider.level(),rider.getVehicle().blockPosition());
    if(binding==null)return null;
    var container=SubLevelContainer.getContainer(rider.level());
    var body=container==null?null:container.getSubLevel(binding.carrier().body);
    if(body==null||body.isRemoved())return null;
    var offset=rider.getVehicle().blockPosition().subtract(body.getPlot().getCenterBlock());
    return binding.carrier().seats.contains(offset)?body:null;
  }

  public static boolean comfort(Entity rider) {
    return rider==Minecraft.getInstance().player&&carrier(rider)!=null&&RiderComfortSettings.ENABLED.get();
  }

  public static boolean localComfort() { return comfort(Minecraft.getInstance().player); }

  /** Called by the first FD return callback; the native addon hook is bypassed for this return. */
  public static Quaterniond compose(Entity rider,Quaterniond neutral) {
    if(neutral==null||rider!=Minecraft.getInstance().getCameraEntity()||!enabled())return neutral;
    if(comfort(rider)) {
      OptionalCalls.staticCall(ROOT+"smooth.CameraFilter","reset");
      return neutral;
    }
    if(!Boolean.TRUE.equals(OptionalCalls.staticCall(ROOT+"config.ImmersionClientConfig","cameraProcessingActive")))return neutral;
    var result=new Quaterniond(neutral);
    OptionalCalls.staticCall(ROOT+"smooth.CameraFilter","apply",result);
    filterCalls++;
    return result;
  }

  @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
    if(!enabled())return;
    var rider=Minecraft.getInstance().player;
    var seat=carrier(rider)==null?null:rider.getVehicle();
    if(Boolean.getBoolean("fourthdimension.validation")&&rider!=null&&rider.tickCount%20==0) {
      var body=carrier(rider);
      var up=body==null?Double.NaN:new org.joml.Vector3d(0,1,0).rotate(body.logicalPose().orientation()).y;
      com.mojang.logging.LogUtils.getLogger().info(
          "FD_IMMERSION player={} body={} mounted={} comfort={} up={} speed={} filterCalls={} camera={} look={},{}",
          rider.getGameProfile().getName(),body==null?null:body.getUniqueId(),seat!=null,localComfort(),up,
          OptionalCalls.staticCall(ROOT+"client.ride.RideTelemetry","speed"),filterCalls,
          Minecraft.getInstance().options.getCameraType(),rider.getYRot(),rider.getXRot());
    }
    if(seat==previousSeat)return;
    if(previousSeat!=null||seat!=null) {
      for(var type:java.util.List.of("smooth.CameraFilter","client.camera.BodyTravel","client.camera.NeckModel","client.ride.RideTelemetry"))
        OptionalCalls.staticCall(ROOT+type,"reset");
    }
    previousSeat=seat;
  }
}
