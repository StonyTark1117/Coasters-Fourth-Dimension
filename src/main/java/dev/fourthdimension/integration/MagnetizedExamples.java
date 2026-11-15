package dev.fourthdimension.integration;

import dev.fourthdimension.vehicle.WingExamples;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** A prepared native route; the player equips the launch anchor and selects the drive. */
@EventBusSubscriber(modid="fourthdimension")
public final class MagnetizedExamples {
  private MagnetizedExamples(){}
  @SubscribeEvent public static void register(RegisterCommandsEvent event){
    event.getDispatcher().register(Commands.literal("fd").requires(s->s.hasPermission(2)).then(Commands.literal("demo")
        .then(Commands.literal("magnetized").executes(ctx->{
          try{
            if(!OptionalIntegrations.installed(OptionalIntegrations.MAGNETIZED).mayInspect())throw new IllegalArgumentException("Install the supported Magnetized profile");
            var l=ctx.getSource().getLevel();var origin=BlockPos.containing(ctx.getSource().getPosition()).offset(0,3,12);
            var example=WingExamples.create(l,origin,true);
            var mode=OptionalCalls.staticCall("net.antopfr.coastersmagnetized.magnet.AnchorMode","valueOf","MAGNET");
            for(int i=0;i<example.edges().size();i++){
              var anchor=example.edges().get(i).from();
              l.setBlockAndUpdate(anchor.below(),Blocks.REDSTONE_BLOCK.defaultBlockState());
              if(i>=2)OptionalCalls.staticCall("net.antopfr.coastersmagnetized.magnet.MagnetizedAnchors","setMode",l,anchor,mode);
            }
            var start=example.edges().getFirst().from();
            for(var p:BlockPos.betweenClosed(start.offset(-4,-1,-4),start.offset(0,-1,-1)))l.setBlockAndUpdate(p,Blocks.SMOOTH_STONE.defaultBlockState());
            ctx.getSource().sendSuccess(()->Component.literal("Magnetized loop ready. Right-click the first anchor with Gold, set its force, then use the car editor near the station: Claim Magnetized, board, Close restraints, Dispatch. Drive forward/reverse requires a held stopped train. Stop on the level dock to unload. Loss of equipped-section power latches a hold; restore, Stop and Resume owner explicitly."),false);return 1;
          }catch(IllegalArgumentException failure){ctx.getSource().sendFailure(Component.literal(failure.getMessage()));return 0;}
        }))));
  }
}
