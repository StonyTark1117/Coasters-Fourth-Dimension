package dev.fourthdimension.vehicle;

import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.station.*;
import dev.fourthdimension.track.TrackGeometry;
import java.util.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Original authored finite route built on the accepted fixed full-roll example geometry. */
public final class NewGenerationFlyingExamples {
 private NewGenerationFlyingExamples(){}
 public static void register(RegisterCommandsEvent event){
  if(Boolean.getBoolean("fourthdimension.validation"))event.getDispatcher().register(Commands.literal("fd").requires(a->a.hasPermission(2)).then(Commands.literal("ng_inspect").executes(ctx->{
   var l=ctx.getSource().getLevel();var data=FoundationData.get(l);var container=dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(l);
   for(var v:data.vehicles.values())if(NewGenerationFlyingVehicle.is(v)){
    var b=(dev.ryanhcode.sable.sublevel.ServerSubLevel)container.getSubLevel(v.chassis);var r=NewGenerationFlyingRuntime.rail(l,v);
    ctx.getSource().sendSuccess(()->Component.literal("NG "+v.chassis+" center="+dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve.cartCenterWorld(b)+" velocity="+container.physicsSystem().getPipeline().getLinearVelocity(b,new org.joml.Vector3d())+" safety="+NewGenerationFlyingRuntime.safety(l,b,v)+" stopped="+v.stopping+" drive="+v.yaw.travelAuthorized+" psi="+v.yaw.angle+" phi="+(-v.carriers.getFirst().angle)),false);
    if(r!=null){var edge=TrackGeometry.resolve(l,r);var center=dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve.cartCenterWorld(b);var hit=dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.closestHitOnEdge(l,edge,new net.minecraft.world.phys.Vec3(center.x,center.y,center.z));var basis=dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.trackBasisAtHit(l,hit);var span=dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftSpanQuery.findAtGraphHit(l,hit,0);
     if(span!=null){double signed=dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftDriveDirection.signedChainBlocksPerSecond(span.anchor(),span.path(),0,256,16);var velocity=dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftSpanQuery.chainWorldVelocityAtHit(l,span.path(),hit,signed,0);ctx.getSource().sendSuccess(()->Component.literal("drive anchor="+span.anchor().getBlockPos()+" t="+hit.t()+" signed="+signed+" velocity="+velocity+" tangent="+basis.forward()+" nativeTarget="+(-velocity.dot(basis.forward()))+" interlock="+dev.fourthdimension.integration.PropulsionInterlock.holds(b,l)),false);}
    }
    for(var bodyId:List.of(v.chassis,v.carriers.getFirst().body)){
     var subject=(dev.ryanhcode.sable.sublevel.ServerSubLevel)container.getSubLevel(bodyId);var origin=subject.getPlot().getCenterBlock();
     for(var offset:BlockPos.betweenClosed(new BlockPos(-3,-3,-3),new BlockPos(3,4,3))){var pos=origin.offset(offset);var state=l.getBlockState(pos);for(var local:state.getCollisionShape(l,pos).toAabbs()){
      double[] min={Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY},max={-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE};
      for(int corner=0;corner<8;corner++){var point=subject.logicalPose().transformPosition(new org.joml.Vector3d(pos.getX()+((corner&1)==0?local.minX:local.maxX),pos.getY()+((corner&2)==0?local.minY:local.maxY),pos.getZ()+((corner&4)==0?local.minZ:local.maxZ)));double[] xyz={point.x,point.y,point.z};for(int axis=0;axis<3;axis++){min[axis]=Math.min(min[axis],xyz[axis]);max[axis]=Math.max(max[axis],xyz[axis]);}}
      var bounds=new net.minecraft.world.phys.AABB(min[0],min[1],min[2],max[0],max[1],max[2]);
      for(var world:BlockPos.betweenClosed(BlockPos.containing(min[0],min[1],min[2]),BlockPos.containing(max[0],max[1],max[2])))for(var collision:l.getBlockState(world).getCollisionShape(l,world).toAabbs())if(bounds.intersects(collision.move(world))){String message="body="+bodyId+" local="+offset.toShortString()+" state="+state+" world bounds="+bounds+" overlaps "+world.toShortString()+" "+l.getBlockState(world)+" shape="+collision.move(world);ctx.getSource().sendSuccess(()->Component.literal(message),false);}
     }}
    }
    var deck=(dev.ryanhcode.sable.sublevel.ServerSubLevel)container.getSubLevel(v.yaw.deck);var dp=deck.getPlot().getCenterBlock();
    for(int boxIndex=0;boxIndex<NewGenerationFlyingDeckBlock.BOXES.length;boxIndex++){
     var box=NewGenerationFlyingDeckBlock.BOXES[boxIndex];double[] min={Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY},max={-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE};
     for(int corner=0;corner<8;corner++){var world=deck.logicalPose().transformPosition(new org.joml.Vector3d(dp.getX()+.5+box[(corner&1)==0?0:3],dp.getY()+.5+box[(corner&2)==0?1:4],dp.getZ()+.5+box[(corner&4)==0?2:5]));double[] xyz={world.x,world.y,world.z};for(int axis=0;axis<3;axis++){min[axis]=Math.min(min[axis],xyz[axis]);max[axis]=Math.max(max[axis],xyz[axis]);}}
     var bounds=new net.minecraft.world.phys.AABB(min[0],min[1],min[2],max[0],max[1],max[2]);
     for(var p:BlockPos.betweenClosed(BlockPos.containing(min[0],min[1],min[2]),BlockPos.containing(max[0],max[1],max[2])))for(var collision:l.getBlockState(p).getCollisionShape(l,p).toAabbs())if(bounds.intersects(collision.move(p))){String message="deck box="+boxIndex+" world bounds="+bounds+" overlaps "+p.toShortString()+" "+l.getBlockState(p)+" shape="+collision.move(p);ctx.getSource().sendSuccess(()->Component.literal(message),false);}
    }
    if(r!=null)for(var next:data.rails)if(next.matches(r.from,r.to)||next.from.equals(r.to)||next.to.equals(r.to)||next.from.equals(r.from)||next.to.equals(r.from)){
     var edge=TrackGeometry.resolve(l,next);ctx.getSource().sendSuccess(()->Component.literal("edge "+next.from.toShortString()+" -> "+next.to.toShortString()+" rejection="+(edge==null?"missing":NewGenerationFlyingTrack.rejection(l,edge))+" valid="+next.valid),false);
    }
   }return 1;
  })));

  event.getDispatcher().register(Commands.literal("fd").requires(s->s.hasPermission(2)).then(Commands.literal("demo").then(Commands.literal("new_generation_flying").executes(ctx->{
   try{var owner=ctx.getSource().getPlayerOrException().getUUID();create(ctx.getSource().getLevel(),BlockPos.containing(ctx.getSource().getPosition()).offset(0,4,12),owner);ctx.getSource().sendSuccess(()->Component.literal("Compound Flying route placed: seated sideways, simultaneous changeover, prone projected targeting/full roll, named Show stop, seated return. Secure both restraints before Dispatch; explicitly unwind at the dock to unload."),false);return 1;}catch(IllegalArgumentException e){ctx.getSource().sendFailure(Component.literal(e.getMessage()));return 0;}
  }))));
 }
 public static FlyingExamples.Example create(ServerLevel l,BlockPos origin,UUID owner){
  var base=FlyingExamples.create(l,origin,false,true);var data=FoundationData.get(l);
  for(int n=0;n<base.edges().size();n++){
   var edge=base.edges().get(n);var old=data.rails.stream().filter(r->r.matches(edge.from(),edge.to())).findFirst().orElseThrow();double length=TrackGeometry.table(l,edge).length();
   // Each scenery marker stays near its own section, including the seated approach.
   // The controller deliberately faults unloaded markers; a demo must not rely on
   // remote chunks left loaded by its construction command.
   var markerPos=edge.from().offset(-24,6,24);YawMarkerEntity marker=null;
   if(n==0||n>=2&&n<=7){l.setBlockAndUpdate(markerPos,YawMarkers.MARKER.get().defaultBlockState());marker=(YawMarkerEntity)l.getBlockEntity(markerPos);marker.owner=owner;marker.setChanged();}
   var r=new FoundationData.Rail(old.from,old.to,Family.FLYING,old.profile,old.id,NewGenerationFlyingVehicle.VARIANT,old.geometryHash,old.revision,true);r.originalCurve=old.originalCurve;r.yaw.owner=owner;r.yaw.geometry=r.geometryHash;
   double a=n==0?0:n==1?0:n>=15?0:Math.PI/2,b=n==1?Math.PI/2:n==14?0:a;
   var mode=n>=2&&n<=7?YawState.Mode.TARGET:n==9?YawState.Mode.SHOW_STOP:YawState.Mode.PROFILE;
   double yawFrom=n==0?Math.PI/2:n==1?Math.PI/2:Math.PI,yawTo=n==1?Math.PI:n==9?3*Math.PI:yawFrom;
   r.yaw.intervals=List.of(new YawSection.Interval(UUID.randomUUID(),"Section "+n,0,length,mode,Math.min(8,length),a,b,new RotationProfile(List.of(new RotationProfile.Key(0,yawFrom),new RotationProfile.Key(length,yawTo))),mode==YawState.Mode.TARGET?marker.id:null,mode==YawState.Mode.TARGET?markerPos:null,mode==YawState.Mode.TARGET?l.dimension().location().toString():"",YawState.Winding.NEAREST,.6,.8,n==9?"Scenery Show":"",n==9?400:0));
   if(n==0)r.yaw.intervals=List.of(
       new YawSection.Interval(UUID.randomUUID(),"Seated sideways",0,16,YawState.Mode.PROFILE,4,0,0,new RotationProfile(List.of(new RotationProfile.Key(0,Math.PI/2),new RotationProfile.Key(16,Math.PI/2))),null,null,"",YawState.Winding.NEAREST,.6,.8,"",0),
       new YawSection.Interval(UUID.randomUUID(),"Seated scenery",16,length,YawState.Mode.TARGET,8,0,0,new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(length-16,0))),marker.id,markerPos,l.dimension().location().toString(),YawState.Winding.NEAREST,.6,.8,"",0));
   if(n==9)r.yaw.intervals=List.of(
       new YawSection.Interval(UUID.randomUUID(),"Show approach",0,8,YawState.Mode.PROFILE,0,Math.PI/2,Math.PI/2,new RotationProfile(List.of(new RotationProfile.Key(0,Math.PI),new RotationProfile.Key(8,Math.PI))),null,null,"",YawState.Winding.NEAREST,.6,.8,"",0),
       new YawSection.Interval(UUID.randomUUID(),"Scenery Show",8,length,YawState.Mode.SHOW_STOP,0,Math.PI/2,Math.PI/2,new RotationProfile(List.of(new RotationProfile.Key(0,Math.PI),new RotationProfile.Key(length-8,3*Math.PI))),null,null,"",YawState.Winding.NEAREST,.6,.8,"Scenery Show",400));
   data.rails.set(data.rails.indexOf(old),r);
  }
  // The compound yaw sweep is wider than Conventional Flying: keep the full motor
  // cube four blocks from the upright port and extend only its native drive shaft.
  for(var edge:base.edges())for(var scan:BlockPos.betweenClosed(edge.from().offset(-3,0,-3),edge.from().offset(3,0,3))){
   var p=scan.immutable();if(!(l.getBlockEntity(p) instanceof CreativeMotorBlockEntity m))continue;
   var state=l.getBlockState(p);var facing=state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING);
   int speed=m.generatedSpeed.getValue()<0?-32:32;var outside=p.relative(facing.getOpposite());
   l.setBlockAndUpdate(outside,state);((CreativeMotorBlockEntity)l.getBlockEntity(outside)).generatedSpeed.setValue(speed);
   l.setBlockAndUpdate(p,com.simibubi.create.AllBlocks.SHAFT.getDefaultState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS,facing.getAxis()));
  }
  var v=NewGenerationFlyingVehicle.create(l,FlyingExamples.sample(l,base.edges().getFirst(),6));v.yaw.owner=owner;var station=(BoardingStationEntity)l.getBlockEntity(base.station());station.vehicle=v.chassis;station.setChanged();v.dock=base.station();v.flying.dockFrom=base.edges().getFirst().from();v.flying.dockTo=base.edges().getFirst().to();v.flying.dockGeometry=NewGenerationFlyingRuntime.rail(l,v).geometryHash;
  data.setDirty();FoundationSync.broadcast(l);return new FlyingExamples.Example(base.edges(),v,base.station());
 }
}
