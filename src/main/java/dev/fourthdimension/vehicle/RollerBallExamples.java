package dev.fourthdimension.vehicle;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.physics.RotationProfile;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.station.*;
import dev.fourthdimension.track.*;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftEndRef;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import static dev.fourthdimension.vehicle.RollerBallState.Section.*;

/** Fixed smooth vertical-plane route. Creation is disclosed setup, never player construction proof. */
public final class RollerBallExamples {
  public record Example(List<CoasterPathEdge> edges,FoundationData.Vehicle car,BlockPos dock){}
  private RollerBallExamples(){}
  public static void register(RegisterCommandsEvent e){
    var demo=Commands.literal("demo");
    for(boolean vehicle:new boolean[]{true,false})demo.then(Commands.literal(vehicle?"roller_ball":"roller_ball_track").executes(ctx->{
      try{create(ctx.getSource().getLevel(),BlockPos.containing(ctx.getSource().getPosition()).offset(0,28,0),vehicle);
        ctx.getSource().sendSuccess(()->Component.literal("Roller Ball vertical route built. One car, two side platforms. Board, close restraints and Dispatch; lift guides upright, descending zigzags tumble, return captures."),false);return 1;
      }catch(IllegalArgumentException ex){ctx.getSource().sendFailure(Component.literal(ex.getMessage()));return 0;}
    }));e.getDispatcher().register(Commands.literal("fd").requires(s->s.hasPermission(2)).then(demo));
  }
  public static Example create(ServerLevel l,BlockPos o,boolean includeCar){
    // All spans <=64; free runs descend between rounded switchbacks.
    int[][] yz={{0,0},{0,32},{16,48},{64,48},{112,48},{128,64},{112,80},{96,96},{88,144},{72,160},{56,144},{48,96},{32,80},{16,96},{8,144},{-8,160},{-32,144},{-32,88},{-32,32},{-32,-16},{-16,-32},{0,-16}};
    Vec3 z=new Vec3(0,0,1),up=new Vec3(0,1,0),down=new Vec3(0,-1,0),back=new Vec3(0,0,-1),slope=new Vec3(0,-1,6).normalize(),reverse=new Vec3(0,-1,-6).normalize();
    Vec3[] t={z,z,up,up,up,z,down,slope,slope,down,reverse,reverse,down,slope,slope,down,back,back,back,back,up,z};
    RollerBallState.Section[] modes={DOCK,LIFT,LIFT,LIFT,SUMMIT,SUMMIT,SUMMIT,FREE,FREE,FREE,FREE,FREE,FREE,FREE,FREE,FREE,RETURN,RETURN,RETURN,RETURN,RETURN,RETURN};
    if(o.getY()<40||o.getY()>170||Math.abs((long)o.getX())>800||Math.abs((long)o.getZ())>600)throw new IllegalArgumentException("Choose clear origin near world center at height40..170");
    for(var p:BlockPos.betweenClosed(o.offset(-8,-34,-36),o.offset(8,134,168)))if(!l.getBlockState(p).isAir())throw new IllegalArgumentException("Roller Ball needs clear 17 x 169 x 205 vertical volume at "+p);
    var points=new ArrayList<BlockPos>();for(var v:yz){var p=o.offset(0,v[0],v[1]);points.add(p);l.setBlockAndUpdate(p,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());}
    var edges=new ArrayList<CoasterPathEdge>();var data=FoundationData.get(l);
    for(int i=0;i<points.size();i++){int n=(i+1)%points.size();var a=t[i];var b=t[n];
      var edge=AxisExamples.connect(l,points.get(i),points.get(n),a,b.scale(-1),new Vec3(0,a.z,-a.y),new Vec3(0,b.z,-b.y));edges.add(edge);
    }
    // Complete native junction propagation before binding sampled geometry.
    TrackGeometry.invalidate(l);
    for(int i=0;i<edges.size();i++){
      var edge=edges.get(i);
      double length=TrackGeometry.table(l,edge).length();
      var r=new FoundationData.Rail(edge.from(),edge.to(),Family.ROLLER_BALL,new RotationProfile(List.of(new RotationProfile.Key(0,0),new RotationProfile.Key(length,0))),UUID.randomUUID(),RollerBallVehicle.VARIANT,TrackGeometry.fingerprint(l,edge),0,true);
      r.rollerSection=modes[i];TrackPlacement.captureOriginalCurve(r,edge.bezier());data.rails.add(r);
    }
    data.setDirty();for(var edge:edges){var reason=RollerBallTrack.rejection(l,edge);if(reason!=null)throw new IllegalArgumentException("Roller Ball route edge "+edges.indexOf(edge)+": "+reason);}
    for(int i=0;i<edges.size();i++)if(modes[i]!=FREE){
      var edge=edges.get(i);var a=(CoasterAnchorpointBlockEntity)l.getBlockEntity(edge.from());
      a.commitChainLift(Direction.Axis.X,ChainLiftEndRef.anchor(edge.to()),new CoasterPathGraphSpans.SpanPath(List.of(new CoasterPathGraphSpans.PathSegment(edge,0,1)),TrackGeometry.table(l,edge).length()));
      var motor=edge.from().east();l.setBlockAndUpdate(motor,AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.WEST));
      ((CreativeMotorBlockEntity)l.getBlockEntity(motor)).generatedSpeed.setValue(32*a.getChainLiftTravelSign());
    }
    var dock=o.offset(-6,1,12);l.setBlockAndUpdate(dock,StationContent.STATION.get().defaultBlockState());
    for(int side:new int[]{-1,1})for(var p:BlockPos.betweenClosed(o.offset(side<0?-8:6,0,4),o.offset(side<0?-6:8,0,24)))l.setBlockAndUpdate(p,Blocks.SMOOTH_STONE.defaultBlockState());
    FoundationData.Vehicle v=null;if(includeCar){v=RollerBallVehicle.create(l,AxisExamples.sample(l,edges.getFirst(),12));v.dock=dock;v.roller.dockFrom=edges.getFirst().from();v.roller.dockTo=edges.getFirst().to();v.roller.dockGeometry=data.rails.get(data.rails.size()-edges.size()).geometryHash;((BoardingStationEntity)l.getBlockEntity(dock)).vehicle=v.chassis;}
    FoundationSync.broadcast(l);return new Example(List.copyOf(edges),v,dock);
  }
}
