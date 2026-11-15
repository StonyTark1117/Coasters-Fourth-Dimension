package dev.fourthdimension.vehicle;

import dev.fourthdimension.contract.*;
import dev.fourthdimension.runtime.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;

/** Native chassis plus one physical posture body. Positive design pitch is negative native X. */
public final class FlyingVehicle {
  public static final String VARIANT="station_changeover_v1";
  public static final double PIVOT=-2.8125;
  public static final NativeTrackFit FIT=new NativeTrackFit(NativeTrackFit.N_CONTACTS,NativeTrackFit.OVERHEAD,
      new NativeTrackFit.Envelope(2.25,1.10,4.50,2.50));
  public static final List<BlockPos> SEATS=List.of(new BlockPos(-2,0,0),new BlockPos(-1,0,0),new BlockPos(0,0,0),new BlockPos(1,0,0));
  private FlyingVehicle(){}
  public static boolean is(FoundationData.Vehicle v){return v.family==Family.FLYING&&VARIANT.equals(v.variant);}
  public static double phi(FoundationData.Carrier c){return -c.angle;}
  public static void validate(FoundationData.Vehicle v){
    if(v.carriers.size()!=1)throw new IllegalArgumentException("Conventional Flying requires one posture body");
    var c=v.carriers.getFirst();
    if(c.body.equals(v.chassis)||!c.seats.equals(SEATS)||c.side!=0||c.longitudinal!=0||c.pivotHeight!=PIVOT
        ||c.childPivotX!=-.5||c.childPivotOffset!=0||c.childPivotZ!=0||c.angle> .05||c.angle< -Math.PI/2-.05
        ||v.ballast!=0||c.ballast!=0||v.recoveryCart!=null)
      throw new IllegalArgumentException("Invalid Flying geometry/load definition");
    v.flying.validate();
  }
  public static FoundationData.Vehicle create(ServerLevel level,CoasterPathTrackFrame.GraphHit hit){
    var reason=FlyingTrack.rejection(level,hit.edge());if(reason!=null)throw new IllegalArgumentException(reason);
    var obstruction=FlyingTrack.obstruction(level,hit);if(obstruction!=null)throw new IllegalArgumentException("Clear/load Flying posture sweep at "+obstruction.toShortString());
    var data=FoundationData.get(level);if(data.vehicles.size()>=64)throw new IllegalArgumentException("Vehicle capacity reached");
    var container=SubLevelContainer.getContainer(level);
    var orientation=CoasterPathTrackFrame.quaternionFromTrackBasis(CoasterPathTrackFrame.trackBasisAtHit(level,hit));
    var bearing=CoasterPathTrackFrame.pathAnchorForSnappedBlock(level,hit);
    for(var other:data.vehicles.values()){
      var b=container.getSubLevel(other.chassis);var center=b==null?null:CoasterCartResolve.cartCenterWorld(b);
      if(center!=null&&center.distance(new Vector3d(bearing.x,bearing.y,bearing.z))<Math.max(8,WingVehicle.spacing(other))-.01)
        throw new IllegalArgumentException("Leave eight blocks or the other car's larger spacing between bearings");
    }
    var chassis=CoasterCartSpawner.spawnMinimalContraption(level,CoasterCartLinkPlacement.plotOriginForBearingCenter(bearing,orientation),orientation,null,false);
    if(chassis==null)throw new IllegalStateException("Native chassis allocation failed");
    var v=new FoundationData.Vehicle(chassis.getUniqueId(),Family.FLYING);v.variant=VARIANT;v.damping=0;v.stopping=true;
    v.boundFrom=hit.edge().from();v.boundTo=hit.edge().to();data.vehicles.put(v.chassis,v);
    try{
      var blocks=chassis.getPlot().getEmbeddedLevelAccessor();
      blocks.setBlock(new BlockPos(0,-1,0),FamilyControllers.CONTROLLERS.get(Family.FLYING).get().defaultBlockState(),3);
      for(int i=0;i<FlyingFrameBlock.PARTS.size();i++)blocks.setBlock(FlyingFrameBlock.PARTS.get(i),VehicleParts.FLYING_FRAME.get().defaultBlockState().setValue(FlyingFrameBlock.PART,i),3);
      var p=chassis.getPlot().getCenterBlock();
      var world=chassis.logicalPose().transformPosition(new Vector3d(p.getX()+.5,p.getY()+.5+PIVOT,p.getZ()+.5));
      var pose=new Pose3d();pose.position().set(world);pose.orientation().set(orientation);
      var child=(ServerSubLevel)container.allocateNewSubLevel(pose);
      var c=new FoundationData.Carrier(child.getUniqueId(),0);c.capture=true;c.pivotHeight=PIVOT;c.childPivotX=-.5;c.seats=SEATS;v.carriers.add(c);
      var plot=child.getPlot();plot.newEmptyChunk(plot.getCenterChunk());var childBlocks=plot.getEmbeddedLevelAccessor();
      for(var seat:SEATS)childBlocks.setBlock(seat,VehicleParts.FLYING_SEAT.get().defaultBlockState(),3);
      childBlocks.setBlock(new BlockPos(0,-1,0),VehicleParts.FLYING_LOCK.get().defaultBlockState(),3);
      var center=plot.getCenterBlock();child.updateMergedMassData(1);
      var centerOfMass=child.getMassTracker().getCenterOfMass();
      var centerWorld=new Vector3d(centerOfMass).sub(center.getX(),center.getY()+.5,center.getZ()+.5).rotate(orientation).add(world);
      // Native pose.position is the COM, not the hinge. Keep Sable's rotation point at the COM.
      child.logicalPose().rotationPoint().set(centerOfMass);
      child.logicalPose().position().set(centerWorld);child.logicalPose().orientation().set(orientation);child.updateLastPose();
      var pipeline=container.physicsSystem().getPipeline();pipeline.onStatsChanged(child);pipeline.teleport(child,centerWorld,orientation);
      CoasterCartTrackSnap.applyInitialSnapAfterSpawn(chassis,level,hit);data.setDirty();FoundationSync.broadcast(level);return v;
    }catch(RuntimeException failure){
      for(var c:v.carriers){var body=container.getSubLevel(c.body);if(body!=null)container.removeSubLevel(body,SubLevelRemovalReason.REMOVED);}
      container.removeSubLevel(chassis,SubLevelRemovalReason.REMOVED);data.vehicles.remove(v.chassis);throw failure;
    }
  }
}
