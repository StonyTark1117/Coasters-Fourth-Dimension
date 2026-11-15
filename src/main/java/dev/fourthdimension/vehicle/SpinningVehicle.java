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

public final class SpinningVehicle {
  public static final String VARIANT = "conventional_v1";
  public static final double PIVOT = 1.75 - .3125;
  public static final NativeTrackFit FIT = new NativeTrackFit(NativeTrackFit.N_CONTACTS,
      NativeTrackFit.ABOVE, new NativeTrackFit.Envelope(2.25,3.65,.95,2.25));
  public static final List<BlockPos> SEATS = List.of(new BlockPos(-1,-1,-1),
      new BlockPos(0,-1,-1), new BlockPos(-1,-1,0), new BlockPos(0,-1,0));
  private SpinningVehicle() {}
  public static boolean is(FoundationData.Vehicle v) {
    return v.family == Family.SPINNING && VARIANT.equals(v.variant);
  }
  public static void validate(FoundationData.Vehicle v) {
    if (v.carriers.size()!=1) throw new IllegalArgumentException("S1 requires one carrier");
    var c=v.carriers.getFirst();
    if (c.body.equals(v.chassis) || !c.seats.equals(SEATS) || c.side!=0 || c.longitudinal!=0
        || c.pivotHeight!=PIVOT || c.childPivotOffset!=-.5 || c.childPivotX!=-.5 || c.childPivotZ!=-.5
        || v.ballast!=0 || c.ballast!=0 || v.damping!=.35 || v.recoveryCart!=null)
      throw new IllegalArgumentException("Invalid S1 geometry/loading definition");
  }
  public static FoundationData.Vehicle create(ServerLevel level, CoasterPathTrackFrame.GraphHit hit) {
    var rejection=SpinningTrack.rejection(level,hit.edge());
    if (rejection!=null) throw new IllegalArgumentException(rejection);
    var obstruction=SpinningTrack.obstruction(level,hit);
    if (obstruction!=null) throw new IllegalArgumentException("Clear/load S1 envelope at "+obstruction.toShortString());
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
    var v=new FoundationData.Vehicle(chassis.getUniqueId(),Family.SPINNING);
    v.variant=VARIANT; v.damping=.35; v.stopping=true;
    v.boundFrom=hit.edge().from(); v.boundTo=hit.edge().to();
    data.vehicles.put(v.chassis,v);
    try {
      var blocks=chassis.getPlot().getEmbeddedLevelAccessor();
      blocks.setBlock(new BlockPos(0,1,0),FamilyControllers.CONTROLLERS.get(Family.SPINNING).get().defaultBlockState(),3);
      var p=chassis.getPlot().getCenterBlock();
      var world=chassis.logicalPose().transformPosition(new Vector3d(p.getX()+.5,p.getY()+.5+PIVOT,p.getZ()+.5));
      var pose=new Pose3d();pose.position().set(world);pose.orientation().set(orientation);
      var body=(ServerSubLevel)container.allocateNewSubLevel(pose);
      var c=new FoundationData.Carrier(body.getUniqueId(),0);
      c.capture=true;c.pivotHeight=PIVOT;c.childPivotX=-.5;c.childPivotOffset=-.5;c.childPivotZ=-.5;
      c.seats=SEATS;v.carriers.add(c);
      var plot=body.getPlot();plot.newEmptyChunk(plot.getCenterChunk());
      var child=plot.getEmbeddedLevelAccessor();
      for(var seat:SEATS) child.setBlock(seat,SpinningSeatBlock.state(seat),3);
      for(int x=-1;x<=0;x++) for(int z=-1;z<=0;z++)
        child.setBlock(new BlockPos(x,0,z),VehicleParts.SPIN_BEARING.get().defaultBlockState(),3);
      var center=plot.getCenterBlock();
      body.logicalPose().rotationPoint().set(center.getX(),center.getY(),center.getZ());
      body.logicalPose().position().set(world);body.logicalPose().orientation().set(orientation);
      body.updateLastPose();
      var pipeline=container.physicsSystem().getPipeline();pipeline.onStatsChanged(body);
      pipeline.teleport(body,body.logicalPose().position(),body.logicalPose().orientation());
      CoasterCartTrackSnap.applyInitialSnapAfterSpawn(chassis,level,hit);
      data.setDirty();FoundationSync.broadcast(level);return v;
    } catch(RuntimeException failure) {
      for(var c:v.carriers) {var body=container.getSubLevel(c.body);if(body!=null)container.removeSubLevel(body,SubLevelRemovalReason.REMOVED);}
      container.removeSubLevel(chassis,SubLevelRemovalReason.REMOVED);data.vehicles.remove(v.chassis);throw failure;
    }
  }
}
