package dev.fourthdimension.integration;

import com.simibubi.create.content.trains.track.TrackMaterial;
import dev.fourthdimension.station.BoardingStationEntity;
import dev.fourthdimension.vehicle.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Disclosed native geometry fixture; the empty variant leaves the station for player repaint/build. */
@EventBusSubscriber(modid="fourthdimension")
public final class CtnbExamples {
  private CtnbExamples(){}
  public static TrackMaterial material(String name){
    try{return (TrackMaterial)Class.forName("dev.notzyvex.coasters_extras.track.ModTrackMaterials").getField(name).get(null);}
    catch(ReflectiveOperationException e){throw new IllegalArgumentException("CTNB material is unavailable",e);}
  }
  @SubscribeEvent public static void register(RegisterCommandsEvent event){
    var demo=Commands.literal("demo");
    for(boolean car:new boolean[]{false,true})demo.then(Commands.literal(car?"ctnb":"ctnb_track").executes(ctx->{
      try{
        if(!OptionalIntegrations.installed(OptionalIntegrations.CTNB).mayInspect())throw new IllegalArgumentException("Install the supported CTNB profile");
        var l=ctx.getSource().getLevel();var origin=BlockPos.containing(ctx.getSource().getPosition()).offset(0,3,12);
        var example=WingExamples.create(l,origin,false);
        String[] kinds={"STATION","LAUNCH","SENSOR","POWERED_BOOST","REVERSE","BOOST","BRAKE","BOOST"};
        for(int i=0;i<example.edges().size();i++){
          var e=example.edges().get(i);
          if(i!=0||car)OptionalCalls.staticCall("dev.notzyvex.coasters_extras.track.TrackRepaint","repaint",l,e.from(),e.to(),material(kinds[i]));
          if(i>=2)l.setBlockAndUpdate(e.from().below(),Blocks.REDSTONE_BLOCK.defaultBlockState());
        }
        if(car){var v=WingVehicle.create(l,WingExamples.sample(l,example.edges().getFirst(),18));var station=(BoardingStationEntity)l.getBlockEntity(example.station());station.vehicle=v.chassis;station.setChanged();}
        ctx.getSource().sendSuccess(()->Component.literal("CTNB loop ready. Empty version: sneak-use Station Track on the first curve, place a Wing car and bind the Boarding Station. Claim CTNB, board, close restraints and Dispatch. The reverse section captures; choose reverse then Dispatch. Station return captures for the native dial dwell and requires explicit Dispatch; powered station anchors hold. Stop, open and unload at the level platform."),false);return 1;
      }catch(IllegalArgumentException e){ctx.getSource().sendFailure(Component.literal(e.getMessage()));return 0;}
    }));
    event.getDispatcher().register(Commands.literal("fd").requires(s->s.hasPermission(2)).then(demo));
  }
}
