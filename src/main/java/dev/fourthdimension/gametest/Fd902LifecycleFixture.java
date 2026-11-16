package dev.fourthdimension.gametest;

import com.google.gson.Gson;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import com.simibubi.create.content.trains.track.BezierConnection;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.shuttle.ShuttleRuntime;
import dev.fourthdimension.shuttle.ShuttleWinchEntity;
import dev.fourthdimension.vehicle.Boarding;
import dev.fourthdimension.vehicle.VehicleOperations;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint;
import java.util.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3d;

/** Opt-in release observations only: actual phases, native bodies and ordinary operations. */
@EventBusSubscriber(modid="fourthdimension")
public final class Fd902LifecycleFixture {
  private static final Gson JSON=new Gson();
  private static ShuttleWinchEntity.Phase stopAt, lossAt;
  private static BlockPos removedFrom, removedTo;
  private static BezierConnection removedCurve;
  private static long lossTick=-1;
  private Fd902LifecycleFixture() {}
  private static boolean enabled(){return Boolean.getBoolean("fourthdimension.validation");}

  @SubscribeEvent public static void register(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
    if(!enabled())return;
    var root=Commands.literal("fd902").requires(s->s.hasPermission(2));
    for(String action:List.of("snapshot","fill","mount_player","assert_intact","assert_bodies","close","open","dispatch","capture","repair","realign","resume","recover","restore_curve","unload"))
      root.then(Commands.literal(action).executes(ctx->{
        try {
          var level=ctx.getSource().getLevel();var winch=winch(level);
          var car=FoundationData.get(level).vehicles.get(winch.vehicle);
          switch(action) {
            case "fill" -> fill(level,winch);
            case "assert_intact" -> intact(level,winch);
            case "assert_bodies" -> bodiesIntact(level,winch);
            case "mount_player" -> {
              var players=level.getServer().getPlayerList().getPlayers();
              if(players.size()!=1||!riders(level,winch).isEmpty())
                throw new IllegalArgumentException("One connected real player and empty train required");
              var last=FoundationData.get(level).vehicles.get(winch.cars.getLast());
              if(!Boarding.ready(level,last)||last.restraints)
                throw new IllegalArgumentException("Open ready station required");
              var carrier=last.carriers.getLast();
              var body=SubLevelContainer.getContainer(level).getSubLevel(carrier.body);
              SeatBlock.sitDown(level,body.getPlot().getCenterBlock().offset(carrier.seats.getFirst()),players.getFirst());
              if(!players.getFirst().isPassenger())throw new IllegalArgumentException("Native player mount rejected");
            }
            case "close" -> VehicleOperations.restraints(level,car,true);
            case "open" -> VehicleOperations.restraints(level,car,false);
            case "dispatch" -> VehicleOperations.dispatch(level,car);
            case "capture" -> VehicleOperations.capture(level,car);
            case "repair" -> VehicleOperations.repairMatchingSupport(level,car);
            case "realign" -> VehicleOperations.realignMatchingSupport(level,car);
            case "resume" -> winch.resume();
            case "recover" -> winch.recover();
            case "restore_curve" -> {
              if(removedCurve==null)throw new IllegalArgumentException("No curve retained in this process");
              ((CoasterAnchorpointBlockEntity)level.getBlockEntity(removedFrom)).putAnchorPeerCurve(level,removedTo,removedCurve);
              removedCurve=null;
            }
            case "unload" -> {
              for(var id:winch.cars)if(!Boarding.ready(level,FoundationData.get(level).vehicles.get(id))||FoundationData.get(level).vehicles.get(id).restraints)
                throw new IllegalArgumentException("Open at the settled station before native rider unload");
              for(var rider:riders(level,winch))rider.stopRiding();
            }
          }
          String state=snapshot(level,winch,action);
          ctx.getSource().sendSuccess(()->Component.literal(state),false);return 1;
        } catch(IllegalArgumentException failure) {
          ctx.getSource().sendFailure(Component.literal("FD902_REJECT "+action+": "+failure.getMessage()));return 0;
        }
      }));
    for(String action:List.of("stop_at","loss_at"))root.then(Commands.literal(action)
        .then(Commands.argument("phase",StringArgumentType.word()).executes(ctx->{
          var phase=ShuttleWinchEntity.Phase.valueOf(StringArgumentType.getString(ctx,"phase").toUpperCase(Locale.ROOT));
          if(phase==ShuttleWinchEntity.Phase.BOARDING||phase==ShuttleWinchEntity.Phase.FAULT)
            throw new IllegalArgumentException("Choose an active phase");
          winch(ctx.getSource().getLevel());
          if(action.equals("stop_at"))stopAt=phase;else lossAt=phase;
          ctx.getSource().sendSuccess(()->Component.literal("FD902_ARM "+action+"="+phase),false);return 1;
        })));
    event.getDispatcher().register(root);
  }

  private static ShuttleWinchEntity winch(ServerLevel level) {
    var all=ShuttleRuntime.winches(level).stream().filter(w->w.vehicle!=null).toList();
    if(all.size()!=1||all.getFirst().cars.size()!=4)throw new IllegalArgumentException("One bound four-car PAX required");
    return all.getFirst();
  }
  private static void fill(ServerLevel level,ShuttleWinchEntity winch) {
    if(!riders(level,winch).isEmpty())throw new IllegalArgumentException("Fixture already occupied");
    var data=FoundationData.get(level);var container=SubLevelContainer.getContainer(level);
    for(var id:winch.cars) {
      var car=data.vehicles.get(id);
      if(!Boarding.ready(level,car)||car.restraints)throw new IllegalArgumentException("Open ready station required");
      for(var carrier:car.carriers)for(var offset:carrier.seats) {
        var body=container.getSubLevel(carrier.body);var rider=EntityType.PIG.create(level);
        rider.setNoAi(true);rider.setPos(winch.getBlockPos().getX(),winch.getBlockPos().getY()+2,winch.getBlockPos().getZ());
        level.addFreshEntity(rider);SeatBlock.sitDown(level,body.getPlot().getCenterBlock().offset(offset),rider);
        if(!rider.isPassenger())throw new IllegalArgumentException("Native fixture mount rejected");
      }
    }
    intact(level,winch);
  }
  private static List<net.minecraft.world.entity.Entity> riders(ServerLevel level,ShuttleWinchEntity winch) {
    var result=new ArrayList<net.minecraft.world.entity.Entity>();var data=FoundationData.get(level);
    var container=SubLevelContainer.getContainer(level);
    for(var id:winch.cars)for(var carrier:data.vehicles.get(id).carriers) {
      var body=container.getSubLevel(carrier.body);if(body==null)continue;
      for(var offset:carrier.seats)for(var seat:body.getLevel().getEntitiesOfClass(SeatEntity.class,new AABB(body.getPlot().getCenterBlock().offset(offset))))
        result.addAll(seat.getPassengers());
    }
    return result;
  }
  private static void intact(ServerLevel level,ShuttleWinchEntity winch) {
    bodiesIntact(level,winch);
    if(riders(level,winch).size()!=16)throw new IllegalArgumentException("Require16 native mounted riders");
  }
  private static void bodiesIntact(ServerLevel level,ShuttleWinchEntity winch) {
    var expected=new HashSet<>(winch.cars);var container=SubLevelContainer.getContainer(level);
    int carriers=0;
    for(var id:winch.cars) {
      var car=FoundationData.get(level).vehicles.get(id);var body=container.getSubLevel(id);
      if(body==null||!new HashSet<>(CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(body)).equals(expected))
        throw new IllegalArgumentException("Native consist identity/link mismatch at "+id);
      for(var carrier:car.carriers) {if(container.getSubLevel(carrier.body)==null)throw new IllegalArgumentException("Carrier unavailable "+carrier.body);carriers++;}
    }
    if(carriers!=8)throw new IllegalArgumentException("Require eight loaded carriers");
  }

  @SubscribeEvent public static void tick(ServerTickEvent.Post event) {
    if(!enabled()||(stopAt==null&&lossAt==null))return;
    var level=event.getServer().overworld();var winch=winch(level);
    if(lossAt==winch.phase) {
      var phase=lossAt;lossAt=null;
      var station=FoundationData.get(level).rails.stream().filter(r->r.family==dev.fourthdimension.contract.Family.PAX&&r.from.getY()==r.to.getY()).toList();
      if(station.size()!=1)throw new IllegalArgumentException("One flat PAX station curve required");
      removedFrom=station.getFirst().from;removedTo=station.getFirst().to;
      var anchor=(CoasterAnchorpointBlockEntity)level.getBlockEntity(removedFrom);
      removedCurve=anchor.getAnchorPeerCurvesView().get(removedTo);
      if(removedCurve==null)throw new IllegalArgumentException("Original station curve unavailable");
      snapshot(level,winch,"before-loss-"+phase);
      anchor.removeAnchorPeerCurve(level,removedTo);lossTick=level.getGameTime();
      snapshot(level,winch,"curve-removed");
    }
    if(stopAt==winch.phase) {
      var phase=stopAt;stopAt=null;
      snapshot(level,winch,"normal-stop-"+phase);
      // The actual dedicated server exits through normal world save; no phase or body mutation.
      event.getServer().halt(false);
    }
  }
  @SubscribeEvent public static void stopped(ServerStoppedEvent event) {
    stopAt=lossAt=null;removedCurve=null;removedFrom=removedTo=null;lossTick=-1;
  }
  private static Map<String,Object> body(ServerLevel level,UUID id) {
    var result=new LinkedHashMap<String,Object>();result.put("id",String.valueOf(id));
    var container=SubLevelContainer.getContainer(level);var body=id==null?null:container.getSubLevel(id);
    result.put("loaded",body!=null);
    if(body instanceof ServerSubLevel nativeBody) {
      var p=CoasterCartResolve.cartCenterWorld(nativeBody);var v=container.physicsSystem().getPipeline().getLinearVelocity(nativeBody,new Vector3d());
      result.put("cartCenter",p!=null);
      if(p==null){var center=nativeBody.getPlot().getCenterBlock();p=nativeBody.logicalPose().transformPosition(new Vector3d(center.getX()+.5,center.getY()+.5,center.getZ()+.5));}
      result.put("position",new double[]{p.x,p.y,p.z});result.put("velocity",new double[]{v.x,v.y,v.z});
      result.put("links",CoasterCartTrainLinkConstraint.getTrainLinkedCartIds(body).stream().map(UUID::toString).sorted().toList());
    }
    return result;
  }
  private static String snapshot(ServerLevel level,ShuttleWinchEntity winch,String label) {
    var result=new LinkedHashMap<String,Object>();result.put("label",label);result.put("tick",level.getGameTime());
    result.put("phase",winch.phase.name());result.put("interrupted",winch.interrupted.name());result.put("fault",winch.fault);
    result.put("winch",winch.getBlockPos().asLong());result.put("lead",String.valueOf(winch.vehicle));result.put("carriage",body(level,winch.carriage));
    result.put("distance",winch.carriageDistance);result.put("cycles",winch.cycles);result.put("lossTick",lossTick);
    result.put("stopAt",stopAt==null?null:stopAt.name());result.put("lossAt",lossAt==null?null:lossAt.name());
    var cars=new ArrayList<Map<String,Object>>();var data=FoundationData.get(level);
    for(var id:winch.cars) {
      var car=data.vehicles.get(id);var state=body(level,id);state.put("stopping",car.stopping);state.put("restraints",car.restraints);
      state.put("alignmentRequired",car.alignmentRequired);state.put("fault",car.fault);state.put("ready",Boarding.ready(level,car));
      state.put("boundFrom",String.valueOf(car.boundFrom));state.put("boundTo",String.valueOf(car.boundTo));
      var carriers=new ArrayList<Map<String,Object>>();
      for(var c:car.carriers) {
        var item=body(level,c.body);item.put("angle",c.angle);item.put("omega",c.omega);item.put("capture",c.capture);
        item.put("occupants",c.occupants.stream().map(UUID::toString).toList());item.put("riderSeats",c.riderSeats.toString());carriers.add(item);
      }
      state.put("carriers",carriers);cars.add(state);
    }
    result.put("cars",cars);
    result.put("riders",riders(level,winch).stream().map(e->Map.of("id",e.getUUID().toString(),"type",e.getType().toString(),"seat",e.getVehicle().blockPosition().asLong(),"seatEntity",e.getVehicle().getUUID().toString())).toList());
    String line="FD902_STATE "+JSON.toJson(result);System.out.println(line);return line;
  }
}
