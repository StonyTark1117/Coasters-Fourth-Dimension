package dev.fourthdimension.moving;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import java.util.*;
import net.minecraft.commands.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.joml.Vector3d;

/** Disclosed example ingredients and validation observation. Never performs operator ride actions. */
public final class MovingExamples {
  private MovingExamples(){}
  public static void register(RegisterCommandsEvent event){
    if(!Boolean.getBoolean("fourthdimension.validation"))return;
    var root=Commands.literal("fd").requires(s->s.hasPermission(2));
    var setup=Commands.literal("moving_setup").executes(c->setup(c.getSource(),MovingMechanism.TRANSFER));
    for(var mechanism:MovingMechanism.values())setup.then(Commands.literal(mechanism.id()).executes(c->setup(c.getSource(),mechanism)));
    root.then(setup).then(Commands.literal("moving_connect").executes(c->connect(c.getSource())))
      .then(Commands.literal("moving_spawn_wing").executes(c->spawn(c.getSource())))
      .then(Commands.literal("moving_inspect").executes(c->inspect(c.getSource())));
    event.getDispatcher().register(root);
  }
  private static void message(CommandSourceStack source,String message){source.sendSuccess(()->Component.literal(message),false);System.out.println("FD2001_EXAMPLE "+message);}
  private static ServerPlayer player(CommandSourceStack source){var p=source.getPlayer();if(p==null)throw new IllegalArgumentException("Run the example setup as the intended operator player");return p;}
  private static MovingAssemblyState nearest(CommandSourceStack source){
    return FoundationData.get(source.getLevel()).moving.assemblies.values().stream()
      .filter(s->s.controller.distToCenterSqr(source.getPosition())<=96*96)
      .min(Comparator.comparingDouble(s->s.controller.distToCenterSqr(source.getPosition())))
      .orElseThrow(()->new IllegalArgumentException("Build an assembly normally within 96 blocks first"));
  }
  private static int setup(CommandSourceStack source,MovingMechanism mechanism){try{
    var p=player(source);var level=source.getLevel();var controller=p.blockPosition().offset(0,0,3);var defaults=MovingConstruction.defaults(controller,mechanism);var origin=defaults.origin();
    if(Math.abs((long)origin.getX())>800||Math.abs((long)origin.getZ())>800)throw new IllegalArgumentException("Choose a clear example site within 800 blocks of world origin");
    var request=new MovingConstruction.ConfigureRequest("wing",16,origin,Direction.SOUTH,defaults.program());
    var plan=new MovingAssemblyState(UUID.randomUUID(),controller,mechanism.id(),WingVehicle.VARIANT,16);plan.program=request.program();plan.configuration="wing";
    plan.origin=new MovingAssemblyState.Pose(origin.getX()+.5,origin.getY()+.5,origin.getZ()+.5,0,0,0,1);
    var placements=new LinkedHashMap<BlockPos,net.minecraft.world.level.block.state.BlockState>();
    placements.put(controller,MovingContent.CONTROLLERS.get(mechanism).get().defaultBlockState().setValue(BlockStateProperties.FACING,Direction.EAST));
    placements.put(controller.west(),AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.EAST));
    for(var pose:List.of(request.program().build().start(),request.program().build().end()))for(int end:new int[]{-8,8}){
      var at=MovingMachineryGeometry.compose(plan.origin,pose);var v=new Vector3d(0,-.25,end).rotate(at.orientation()).add(at.position());
      placements.put(BlockPos.containing(v.x,v.y,v.z),SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    }
    for(var pos:placements.keySet())if(!level.hasChunkAt(pos)||!level.mayInteract(p,pos)||!level.getBlockState(pos).isAir()||!level.getFluidState(pos).isEmpty())throw new IllegalArgumentException("Example setup needs clear editable loaded blocks at "+pos.toShortString());
    for(var entry:placements.entrySet())level.setBlockAndUpdate(entry.getKey(),entry.getValue());
    ((CreativeMotorBlockEntity)level.getBlockEntity(controller.west())).generatedSpeed.setValue(64);
    var preview=MovingConstruction.preview(p,controller,request);
    for(var raw:preview.getList("costs",Tag.TAG_COMPOUND)){var cost=(CompoundTag)raw;var item=net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse(cost.getString("item")));int remaining=cost.getInt("count");while(remaining>0){var stack=new ItemStack(item,Math.min(64,remaining));remaining-=stack.getCount();if(!p.getInventory().add(stack))p.drop(stack,false);}}
    message(source,"Disclosed setup only. Controller="+controller.toShortString()+"; use normal panel: Wing, length16, SOUTH, origin="+origin.toShortString()+", program="+request.program()+". Preview, inspect Materials/Socket positions, Validate and Build normally. Real Create input64RPM and exact ingredient bill supplied; no assembly or ride action created. Construction preview valid="+preview.getBoolean("valid")+" "+preview.getString("error"));
    return 1;
  }catch(IllegalArgumentException e){source.sendFailure(Component.literal(e.getMessage()));return 0;}}
  private static int connect(CommandSourceStack source){try{
    var s=nearest(source);var level=source.getLevel();
    if(!s.configuration.equals("wing")||s.retained()||!s.cars.isEmpty())throw new IllegalArgumentException("This disclosed approach fixture requires an empty Wing example");
    record Join(BlockPos socket,BlockPos far,Vec3 outward,Vec3 normal,boolean last){}
    var joins=new ArrayList<Join>();var sockets=new HashSet<BlockPos>();
    for(var dock:s.docks.values())for(boolean last:new boolean[]{false,true}){
      var socket=last?dock.to():dock.from();if(!sockets.add(socket))continue;
      var frame=MovingNativeAdapter.socketFrame(s,dock,last);
      var outward=frame.forward().scale(last?1:-1);
      var far=BlockPos.containing(frame.rawPosition().add(outward.scale(24)));
      if(!level.hasChunkAt(far)||!level.getBlockState(far).isAir())throw new IllegalArgumentException("Clear/load the fixture approach anchor "+far.toShortString());
      joins.add(new Join(socket,far,outward,frame.normal(),last));
    }
    for(var join:joins){
      level.setBlockAndUpdate(join.far(),SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      if(join.last())MotorbikeExamples.connect(level,join.socket(),join.far(),join.outward(),join.outward().scale(-1),join.normal(),join.normal());
      else MotorbikeExamples.connect(level,join.far(),join.socket(),join.outward().scale(-1),join.outward(),join.normal(),join.normal());
    }
    message(source,"Disclosed fixed approach fixture connected for "+s.id+". This is not normal track-tool construction evidence. Wait for measured source locks, then moving_spawn_wing if a setup car is needed.");return 1;
  }catch(IllegalArgumentException e){source.sendFailure(Component.literal(e.getMessage()));return 0;}}
  private static int spawn(CommandSourceStack source){try{
    var s=nearest(source);var level=source.getLevel();if(!s.configuration.equals("wing")||s.retained()||!s.cars.isEmpty())throw new IllegalArgumentException("Use an empty Wing bed at its measured locked source");
    var edge=MovingNativeAdapter.liveDockEdge(level,s);if(edge==null)throw new IllegalArgumentException("Wait for the actual bed and source locks; no authenticated bridge yet");
    var v=WingVehicle.create(level,MotorbikeExamples.sample(level,edge,s.deckLength/2.0));
    message(source,"Disclosed native Wing spawn chassis="+v.chassis+". Board normally, close restraints normally, then use Capture/Move/Release in the moving controller. No riders or operator actions were installed.");return 1;
  }catch(IllegalArgumentException e){source.sendFailure(Component.literal(e.getMessage()));return 0;}}
  private static int inspect(CommandSourceStack source){try{
    var s=nearest(source);var level=source.getLevel();message(source,"STATE "+s.save(false));
    var operator=source.getPlayer();if(operator!=null)message(source,"SNAPSHOT "+MovingRuntime.snapshot(operator,s.controller));
    var ids=new LinkedHashSet<UUID>(s.bodies.keySet());if(s.bed!=null)ids.add(s.bed);
    for(var v:FoundationData.get(level).vehicles.values())if(v.chassis!=null){var body=SubLevelContainer.getContainer(level).getSubLevel(v.chassis);if(body!=null&&body.logicalPose().position().distance(s.actualPose.position())<64){ids.add(v.chassis);v.carriers.forEach(c->ids.add(c.body));message(source,"CAR "+v.chassis+" restraints="+v.restraints+" stopped="+v.stopping+" fault="+v.fault+" riderSeats="+v.carriers.stream().map(c->c.riderSeats).toList());}}
    for(var id:ids){var nativeBody=SubLevelContainer.getContainer(level).getSubLevel(id);if(!(nativeBody instanceof ServerSubLevel body))continue;var plot=body.getPlot().getBoundingBox();if(plot==null)continue;
      message(source,"BODY "+id+" plot="+body.getPlot().getCenterBlock()+" pose="+body.logicalPose()+" linear="+SubLevelContainer.getContainer(level).physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d()));
      var box=new AABB(plot.minX(),plot.minY(),plot.minZ(),plot.maxX()+1,plot.maxY()+1,plot.maxZ()+1);
      for(var seat:level.getEntitiesOfClass(SeatEntity.class,box)){var at=seat.position();var world=body.logicalPose().transformPosition(new Vector3d(at.x,at.y,at.z));message(source,"SEAT "+seat.getUUID()+" local="+at+" world="+world+" passengers="+seat.getPassengers().stream().map(e->e.getUUID()+":"+e.getName().getString()).toList());}
      if(FoundationData.get(level).vehicles.containsKey(id)&&WingVehicle.is(FoundationData.get(level).vehicles.get(id))){var center=body.getPlot().getCenterBlock();for(var local:WingVehicle.SEATS){var p=center.offset(local);var world=body.logicalPose().transformPosition(new Vector3d(p.getX()+.5,p.getY()+.5,p.getZ()+.5));message(source,"WING_TARGET block="+p.toShortString()+" world="+world);}}
    }
    return 1;
  }catch(IllegalArgumentException e){source.sendFailure(Component.literal(e.getMessage()));return 0;}}
}
