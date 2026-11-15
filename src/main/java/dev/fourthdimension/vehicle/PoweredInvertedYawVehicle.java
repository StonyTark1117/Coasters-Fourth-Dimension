package dev.fourthdimension.vehicle;

import dev.fourthdimension.contract.*;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.TrackDesign;
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

public final class PoweredInvertedYawVehicle {
  public static final String VARIANT = "upright_yaw_v1";
  public static final double PIVOT = -3.0 - .3125;
  public static final NativeTrackFit FIT = new NativeTrackFit(NativeTrackFit.N_CONTACTS,
      NativeTrackFit.OVERHEAD, new NativeTrackFit.Envelope(1.5,1.1,4.5,1.75));
  public static final List<BlockPos> SEATS = List.of(new BlockPos(-1,0,0),new BlockPos(0,0,0));
  private PoweredInvertedYawVehicle() {}
  public static boolean is(FoundationData.Vehicle v) {
    return v.family == Family.POWERED_INVERTED && VARIANT.equals(v.variant);
  }
  public static void validate(FoundationData.Vehicle v) {
    if (v.carriers.size()!=1) throw new IllegalArgumentException("Powered Inverted Yaw requires one carrier");
    var c=v.carriers.getFirst();
    if (c.body.equals(v.chassis) || !c.seats.equals(SEATS) || c.side!=0 || c.longitudinal!=0
        || c.pivotHeight!=PIVOT || c.childPivotOffset!=0 || c.childPivotX!=-.5 || c.childPivotZ!=0
        || v.ballast!=0 || c.ballast!=0 || v.damping!=0 || v.recoveryCart!=null)
      throw new IllegalArgumentException("Invalid Powered Inverted Yaw geometry/loading definition");
  }
  public static FoundationData.Vehicle create(ServerLevel level, CoasterPathTrackFrame.GraphHit hit) {
    var rejection=PoweredInvertedYawTrack.rejection(level,hit.edge());
    if (rejection!=null) throw new IllegalArgumentException(rejection);
    var obstruction=PoweredInvertedYawTrack.obstruction(level,hit);
    if (obstruction!=null) throw new IllegalArgumentException("Clear/load Powered Inverted Yaw envelope at "+obstruction.toShortString());
    var data=FoundationData.get(level);
    if (data.vehicles.size()>=64) throw new IllegalArgumentException("Vehicle capacity reached");
    var container=SubLevelContainer.getContainer(level);
    var orientation=CoasterPathTrackFrame.quaternionFromTrackBasis(CoasterPathTrackFrame.trackBasisAtHit(level,hit));
    var bearing=CoasterPathTrackFrame.pathAnchorForSnappedBlock(level,hit);
    for (var other:data.vehicles.values()) {
      var body=container.getSubLevel(other.chassis);
      var center=body==null?null:CoasterCartResolve.cartCenterWorld(body);
      if(center!=null && center.distance(new Vector3d(bearing.x,bearing.y,bearing.z))
          < Math.max(6,WingVehicle.spacing(other))-.01)
        throw new IllegalArgumentException("Leave six blocks or the other car's larger spacing between bearings");
    }
    var chassis=CoasterCartSpawner.spawnMinimalContraption(level,
        CoasterCartLinkPlacement.plotOriginForBearingCenter(bearing,orientation),orientation,null,false);
    if(chassis==null) throw new IllegalStateException("Native chassis allocation failed");
    var v=new FoundationData.Vehicle(chassis.getUniqueId(),Family.POWERED_INVERTED);
    v.variant=VARIANT; v.damping=0; v.stopping=true;
    v.boundFrom=hit.edge().from(); v.boundTo=hit.edge().to();
    var r=FoundationData.get(level).rails.stream().filter(a->a.matches(v.boundFrom,v.boundTo)).findFirst().orElseThrow();v.poweredYaw.owner=r.poweredYaw.yaw.owner;
    data.vehicles.put(v.chassis,v);
    try {
      var blocks=chassis.getPlot().getEmbeddedLevelAccessor();
      blocks.setBlock(new BlockPos(0,-1,0),FamilyControllers.CONTROLLERS.get(Family.POWERED_INVERTED).get().defaultBlockState(),3);
      var p=chassis.getPlot().getCenterBlock();
      var world=chassis.logicalPose().transformPosition(new Vector3d(p.getX()+.5,p.getY()+.5+PIVOT,p.getZ()+.5));
      var pose=new Pose3d();pose.position().set(world);pose.orientation().set(orientation);
      var body=(ServerSubLevel)container.allocateNewSubLevel(pose);
      var c=new FoundationData.Carrier(body.getUniqueId(),0);
      c.capture=true;c.pivotHeight=PIVOT;c.childPivotX=-.5;
      c.seats=SEATS;v.carriers.add(c);
      var plot=body.getPlot();plot.newEmptyChunk(plot.getCenterChunk());
      var child=plot.getEmbeddedLevelAccessor();
      for(var seat:SEATS) child.setBlock(seat,PoweredInvertedYawContent.SEAT.get().defaultBlockState(),3);
      child.setBlock(new BlockPos(0,-1,0),PoweredInvertedYawContent.BEARING.get().defaultBlockState(),3);
      for(int y=-2;y>=-3;y--)blocks.setBlock(new BlockPos(0,y,0),PoweredInvertedYawContent.SPINDLE.get().defaultBlockState(),3);
      var center=plot.getCenterBlock();body.updateMergedMassData(1);
      var com=body.getMassTracker().getCenterOfMass();
      if(com==null||body.getMassTracker().getMass()<=0)throw new IllegalStateException("Upright carrier requires native mass");
      var position=new Vector3d(com).sub(center.getX(),center.getY()+.5,center.getZ()+.5).rotate(orientation).add(world);
      body.logicalPose().rotationPoint().set(com);body.logicalPose().position().set(position);body.logicalPose().orientation().set(orientation);body.updateLastPose();
      var pipeline=container.physicsSystem().getPipeline();pipeline.onStatsChanged(body);pipeline.teleport(body,position,orientation);
      CoasterCartTrackSnap.applyInitialSnapAfterSpawn(chassis,level,hit);
      data.setDirty();FoundationSync.broadcast(level);return v;
    } catch(RuntimeException failure) {
      for(var c:v.carriers) {var body=container.getSubLevel(c.body);if(body!=null)container.removeSubLevel(body,SubLevelRemovalReason.REMOVED);}
      container.removeSubLevel(chassis,SubLevelRemovalReason.REMOVED);data.vehicles.remove(v.chassis);throw failure;
    }
  }
}
