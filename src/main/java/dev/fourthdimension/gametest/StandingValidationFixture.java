package dev.fourthdimension.gametest;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
/** Read-only observations for the local validation process. No bypass of ordinary controls. */
@EventBusSubscriber(modid="fourthdimension")
public final class StandingValidationFixture {
 @SubscribeEvent public static void register(net.neoforged.neoforge.event.RegisterCommandsEvent e){if(!Boolean.getBoolean("fourthdimension.validation"))return;e.getDispatcher().register(Commands.literal("fd2201").requires(s->s.hasPermission(2)).then(Commands.literal("inspect").executes(ctx->{var l=ctx.getSource().getLevel();var container=SubLevelContainer.getContainer(l);int n=0;for(var v:FoundationData.get(l).vehicles.values())if(StandingVehicle.is(v)){var p=container.getSubLevel(v.chassis);var c=v.carriers.getFirst();var body=container.getSubLevel(c.body);ctx.getSource().sendSuccess(()->Component.literal("FD2201_STATE variant="+v.variant+" parent="+v.chassis+" body="+c.body+" center="+(p==null?"unloaded":CoasterCartResolve.cartCenterWorld(p))+" parentPlot="+(p==null?"unloaded":p.getPlot().getCenterBlock())+" childPlot="+(body==null?"unloaded":body.getPlot().getCenterBlock())+" childPose="+(body==null?"unloaded":body.logicalPose())+" q="+v.standing.travel+" qdot="+v.standing.velocity+" retained="+v.stopping+" interrupted="+v.standing.interrupted+" restraints="+v.restraints+" latched="+c.latched+" ready="+v.boardingReady+" riders="+c.occupants+" dock="+v.dock+" fault="+v.fault+" diagnostics="+StandingRuntime.dockDiagnostic(l,v)),true);n++;}return n;})));}
}
