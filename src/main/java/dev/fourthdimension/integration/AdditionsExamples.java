package dev.fourthdimension.integration;

import dev.fourthdimension.vehicle.WingExamples;
import java.util.List;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Disclosed operator example. Normal station configuration, boarding and restraints follow setup. */
@EventBusSubscriber(modid="fourthdimension")
public final class AdditionsExamples {
  private AdditionsExamples() {}
  @SubscribeEvent public static void register(RegisterCommandsEvent event) {
    event.getDispatcher().register(Commands.literal("fd").requires(s->s.hasPermission(2))
        .then(Commands.literal("demo").then(Commands.literal("additions_station").executes(ctx->{
          try {
            if(!OptionalIntegrations.installed(OptionalIntegrations.ADDITIONS).mayInspect())throw new IllegalArgumentException("Install the supported Additions profile");
            var level=ctx.getSource().getLevel();
            var example=WingExamples.create(level,BlockPos.containing(ctx.getSource().getPosition()).offset(0,3,12),true);
            var edge=example.edges().getFirst();
            level.setBlockAndUpdate(example.station(),BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("coasterfins","station")).defaultBlockState());
            var station=level.getBlockEntity(example.station());
            var type=Class.forName("dev.plushivr.coasterfins.logic.FinSpanSegment");
            var constructor=type.getConstructor(BlockPos.class,BlockPos.class,double.class,double.class,boolean.class);
            OptionalCalls.call(station,"setSpan",List.of(constructor.newInstance(edge.from(),edge.to(),.05,.65,true)));
            OptionalCalls.call(station,"setDispatch",false,1,3,2);
            ctx.getSource().sendSuccess(()->Component.literal("Additions station loop ready. Empty-hand use the station: configure Link frequencies and Auto Dispatch. Board and close restraints with the Rotation Tool; the station requests dispatch only after its guards pass. Stop manually on return; no automatic restraint changes."),false);
            return 1;
          } catch(ReflectiveOperationException|IllegalArgumentException failure) {
            ctx.getSource().sendFailure(Component.literal(failure.getMessage()));return 0;
          }
        }))));
  }
}
