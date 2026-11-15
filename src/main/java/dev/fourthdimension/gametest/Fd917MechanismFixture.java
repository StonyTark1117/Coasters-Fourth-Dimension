package dev.fourthdimension.gametest;

import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import com.mojang.brigadier.arguments.StringArgumentType;

/** Disclosed setup/observations only, gated by the local validation JVM property. */
@EventBusSubscriber(modid="fourthdimension")
public final class Fd917MechanismFixture {
  @SubscribeEvent public static void register(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
    if(!Boolean.getBoolean("fourthdimension.validation"))return;
    var root=Commands.literal("fd917_mechanism").requires(s->s.hasPermission(2));
    for(String action:java.util.List.of("inspect","close","open","capture","dispatch"))
      root.then(Commands.literal(action).then(Commands.argument("family",StringArgumentType.word()).executes(ctx->{
        var level=ctx.getSource().getLevel();String family=StringArgumentType.getString(ctx,"family");
        var cars=FoundationData.get(level).vehicles.values().stream().filter(v->switch(family) {
          case "s1" -> SpinningVehicle.is(v);
          case "s2" -> S2SpinningVehicle.is(v);
          case "conventional_flying" -> FlyingVehicle.is(v)&&!NewGenerationFlyingVehicle.is(v);
          case "new_generation_flying" -> NewGenerationFlyingVehicle.is(v);
          default -> v.family.id().equals(family);
        }).toList();
        if(cars.size()!=1){ctx.getSource().sendFailure(Component.literal("Require exactly one "+family+" vehicle"));return 0;}
        var car=cars.getFirst();
        try {
          if(action.equals("close"))VehicleOperations.restraints(level,car,true);
          if(action.equals("open"))VehicleOperations.restraints(level,car,false);
          if(action.equals("capture"))VehicleOperations.capture(level,car);
          if(action.equals("dispatch"))VehicleOperations.dispatch(level,car);
        }catch(IllegalArgumentException failure){ctx.getSource().sendFailure(Component.literal(failure.getMessage()));return 0;}
        var container=SubLevelContainer.getContainer(level);var body=container.getSubLevel(car.chassis);
        ctx.getSource().sendSuccess(()->Component.literal("FD917_MECHANISM action="+action+" family="+family+" id="+car.chassis
            +" center="+(body==null?"unloaded":CoasterCartResolve.cartCenterWorld(body))+" stopping="+car.stopping
            +" restraints="+car.restraints+" fault="+car.fault+" bound="+car.boundFrom+"/"+car.boundTo
            +" carriers="+car.carriers.stream().map(c->"body="+c.body+",angle="+c.angle+",latched="+c.latched+",lift="+c.liftHeld+",riders="+c.occupants.size()).toList()),true);
        return 1;
      })));
    root.then(Commands.literal("inspect_shuttles").executes(ctx->{
      for(var winch:dev.fourthdimension.shuttle.ShuttleRuntime.winches(ctx.getSource().getLevel())) {
        var state=dev.fourthdimension.shuttle.Fd917ShuttleSync.state(winch);
        ctx.getSource().sendSuccess(()->Component.literal("FD917_SHUTTLE_STATE "+state),false);
      }
      return 1;
    }));
    root.then(Commands.literal("packet_bound").executes(ctx->{
      var states=new java.util.ArrayList<dev.fourthdimension.shuttle.Fd917ShuttleSync.State>();
      for(int i=0;i<dev.fourthdimension.shuttle.Fd917ShuttleSync.MAX_STATES;i++)states.add(
          new dev.fourthdimension.shuttle.Fd917ShuttleSync.State(new net.minecraft.core.BlockPos(i,120,i),
              java.util.UUID.randomUUID(),java.util.UUID.randomUUID(),new net.minecraft.core.BlockPos(-100,120,-100),
              new net.minecraft.core.BlockPos(100,120,100),true,"CAPTURING",true,true,Float.MAX_VALUE));
      var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),ctx.getSource().registryAccess());
      try {
        var payload=new dev.fourthdimension.shuttle.Fd917ShuttleSync.Snapshot(ctx.getSource().getLevel().dimension().location(),
            dev.fourthdimension.shuttle.Fd917ShuttleSync.encode(states));
        dev.fourthdimension.shuttle.Fd917ShuttleSync.Snapshot.CODEC.encode(buffer,payload);int bytes=buffer.readableBytes();
        var decoded=dev.fourthdimension.shuttle.Fd917ShuttleSync.Snapshot.CODEC.decode(buffer);
        if(!decoded.equals(payload)||buffer.isReadable())throw new IllegalStateException("Snapshot roundtrip mismatch");
        ctx.getSource().sendSuccess(()->Component.literal("FD917_SHUTTLE_BOUND PASS states="+states.size()+" bytes="+bytes+" accountedLimit=131072"),false);
      }finally{buffer.release();}
      return 1;
    }));
    root.then(Commands.literal("reverse_pipeline").executes(ctx->{
      var level=ctx.getSource().getLevel();var data=FoundationData.get(level);
      var cars=data.vehicles.values().stream().filter(PipelineVehicle::is).toList();
      if(cars.size()!=1||!cars.getFirst().stopping)throw new IllegalArgumentException("One stopped Pipeline fixture required");
      var motors=new java.util.HashSet<net.minecraft.core.BlockPos>();
      for(var rail:data.rails)if(rail.family==dev.fourthdimension.contract.Family.PIPELINE)
        for(var pos:java.util.List.of(rail.from,rail.to))for(var direction:net.minecraft.core.Direction.values())for(int distance=1;distance<=3;distance++) {
          var target=pos.relative(direction,distance);
          if(!level.hasChunkAt(target))break;
          if(level.getBlockEntity(target) instanceof com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity motor) {
            if(motors.add(target))motor.generatedSpeed.setValue(-motor.generatedSpeed.getValue());
            break;
          }
          var state=level.getBlockState(target);
          // Native examples place the motor behind two coaxial shafts.
          if(!com.simibubi.create.AllBlocks.SHAFT.has(state)||state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS)!=direction.getAxis())break;
        }
      ctx.getSource().sendSuccess(()->Component.literal("FD917_PIPELINE_REVERSE changedMotors="+motors.size()),false);return motors.size();
    }));
    event.getDispatcher().register(root);
  }
}
