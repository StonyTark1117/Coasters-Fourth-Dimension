package dev.fourthdimension.vehicle;

import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackGeometry;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.chainlift.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import org.joml.Vector3d;

/** Read-only, validation-property-only diagnostics for original example and rider evidence. */
public final class MotorbikeDiagnostics {
  private MotorbikeDiagnostics() {}
  public static int inspect(CommandSourceStack source) {
    var level=source.getLevel();var container=SubLevelContainer.getContainer(level);
    for(var v:FoundationData.get(level).vehicles.values())if(MotorbikeVehicle.is(v)
        &&container.getSubLevel(v.chassis) instanceof ServerSubLevel body){
      var center=CoasterCartResolve.cartCenterWorld(body);
      say(source,"Motorbike "+v.chassis+" center="+center+" plot="+body.getPlot().getCenterBlock()
          +" velocity="+container.physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d())
          +" stopped="+v.stopping+" restraints="+v.restraints+" capture="+v.carriers.getFirst().capture
          +" riders="+v.carriers.getFirst().occupants.size()+" fault="+v.fault+" safety="+MotorbikeRuntime.safety(level,body,v));
      for(var rail:FoundationData.get(level).rails)if(rail.matches(v.boundFrom,v.boundTo)){
        var edge=TrackGeometry.resolve(level,rail);if(edge==null)continue;
        var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
        var basis=CoasterPathTrackFrame.trackBasisAtHit(level,hit);var span=ChainLiftSpanQuery.findAtGraphHit(level,hit,0);
        if(span!=null){double signed=ChainLiftDriveDirection.signedChainBlocksPerSecond(span.anchor(),span.path(),0,256,16);
          var velocity=ChainLiftSpanQuery.chainWorldVelocityAtHit(level,span.path(),hit,signed,0);
          say(source,"drive anchor="+span.anchor().getBlockPos()+" hit="+hit.edge().from()+" -> "+hit.edge().to()+" t="+hit.t()
              +" signed="+signed+" velocity="+velocity+" tangent="+basis.forward()+" nativeTarget="+(-velocity.dot(basis.forward())));
          for(var seg:span.path().segments())say(source,"segment "+seg.edge().from()+" -> "+seg.edge().to()+" t="+seg.tStart()+" -> "+seg.tEnd());
        }
      }
      var origin=body.getPlot().getCenterBlock();
      for(var offset:BlockPos.betweenClosed(new BlockPos(-2,0,-1),new BlockPos(2,2,1))){
        var local=origin.offset(offset);var state=level.getBlockState(local);
        for(var box:state.getCollisionShape(level,local).toAabbs()){
          double[] min={Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY},max={-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE};
          for(int corner=0;corner<8;corner++){
            var point=body.logicalPose().transformPosition(new Vector3d(local.getX()+((corner&1)==0?box.minX:box.maxX),local.getY()+((corner&2)==0?box.minY:box.maxY),local.getZ()+((corner&4)==0?box.minZ:box.maxZ)));
            double[] xyz={point.x,point.y,point.z};for(int axis=0;axis<3;axis++){min[axis]=Math.min(min[axis],xyz[axis]);max[axis]=Math.max(max[axis],xyz[axis]);}
          }
          var bounds=new net.minecraft.world.phys.AABB(min[0],min[1],min[2],max[0],max[1],max[2]);
          for(var world:BlockPos.betweenClosed(BlockPos.containing(min[0],min[1],min[2]),BlockPos.containing(max[0],max[1],max[2])))
            for(var collision:level.getBlockState(world).getCollisionShape(level,world).toAabbs())if(bounds.intersects(collision.move(world)))
              say(source,"AABB overlap local="+offset+" state="+state+" bounds="+bounds+" world="+world+" block="+level.getBlockState(world)+" shape="+collision.move(world));
        }
      }
    }return 1;
  }
  private static void say(CommandSourceStack s,String text){s.sendSuccess(()->Component.literal(text),false);}
}
