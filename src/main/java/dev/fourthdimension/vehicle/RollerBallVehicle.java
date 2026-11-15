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

public final class RollerBallVehicle {
  public static final String VARIANT = "vertical_zigzag_v1";
  public static final double PIVOT = 2.0 - .3125;
  public static final NativeTrackFit FIT = new NativeTrackFit(NativeTrackFit.N_CONTACTS,
      NativeTrackFit.ABOVE, new NativeTrackFit.Envelope(4.50,4.75,.75,2.75));
  public static final List<BlockPos> SEATS=List.of(new BlockPos(-4,0,-1),new BlockPos(3,0,-1),new BlockPos(-4,0,0),new BlockPos(3,0,0));
  private RollerBallVehicle() {}
  public static boolean is(FoundationData.Vehicle v) {
    return v.family == Family.ROLLER_BALL && VARIANT.equals(v.variant);
  }
  public static void validate(FoundationData.Vehicle v) {
    if (v.carriers.size()!=1) throw new IllegalArgumentException("Roller Ball requires one carrier");
    var c=v.carriers.getFirst();
    if (c.body.equals(v.chassis) || !c.seats.equals(SEATS) || c.side!=0 || c.longitudinal!=0
        || c.pivotHeight!=PIVOT || c.childPivotOffset!=1 || c.childPivotX!=0 || c.childPivotZ!=0
        || v.ballast!=0 || c.ballast!=0 || v.damping!=.6 || v.recoveryCart!=null)
      throw new IllegalArgumentException("Invalid Roller Ball geometry/loading definition");
  }
  public static FoundationData.Vehicle create(ServerLevel level, CoasterPathTrackFrame.GraphHit hit) {
    var rejection=RollerBallTrack.rejection(level,hit.edge());
    if (rejection!=null) throw new IllegalArgumentException(rejection);
    var obstruction=RollerBallTrack.obstruction(level,hit);
    if (obstruction!=null) throw new IllegalArgumentException("Clear/load Roller Ball envelope at "+obstruction.toShortString());
    var data=FoundationData.get(level);
    if (data.vehicles.size()>=64) throw new IllegalArgumentException("Vehicle capacity reached");
    var container=SubLevelContainer.getContainer(level);
    var orientation=CoasterPathTrackFrame.quaternionFromTrackBasis(CoasterPathTrackFrame.trackBasisAtHit(level,hit));
    var bearing=CoasterPathTrackFrame.pathAnchorForSnappedBlock(level,hit);
    for (var other:data.vehicles.values()) {
      var body=container.getSubLevel(other.chassis);
      var center=body==null?null:CoasterCartResolve.cartCenterWorld(body);
      if(center!=null && center.distance(new Vector3d(bearing.x,bearing.y,bearing.z))
          < Math.max(8,WingVehicle.spacing(other))-.01)
        throw new IllegalArgumentException("Leave eight blocks or the other car's larger spacing between bearings");
    }
    var chassis=CoasterCartSpawner.spawnMinimalContraption(level,
        CoasterCartLinkPlacement.plotOriginForBearingCenter(bearing,orientation),orientation,null,false);
    if(chassis==null) throw new IllegalStateException("Native chassis allocation failed");
    var v=new FoundationData.Vehicle(chassis.getUniqueId(),Family.ROLLER_BALL);
    v.variant=VARIANT; v.damping=.6; v.stopping=true;
    v.boundFrom=hit.edge().from(); v.boundTo=hit.edge().to();
    data.vehicles.put(v.chassis,v);
    try {
      var blocks=chassis.getPlot().getEmbeddedLevelAccessor();
      blocks.setBlock(new BlockPos(0,1,0),FamilyControllers.CONTROLLERS.get(Family.ROLLER_BALL).get().defaultBlockState(),3);
      var p=chassis.getPlot().getCenterBlock();
      var world=chassis.logicalPose().transformPosition(new Vector3d(p.getX()+.5,p.getY()+.5+PIVOT,p.getZ()+.5));
      var pose=new Pose3d();pose.position().set(world);pose.orientation().set(orientation);
      var body=(ServerSubLevel)container.allocateNewSubLevel(pose);
      var c=new FoundationData.Carrier(body.getUniqueId(),0);
      c.capture=true;c.pivotHeight=PIVOT;c.childPivotX=0;c.childPivotOffset=1;c.childPivotZ=0;
      c.seats=SEATS;v.carriers.add(c);
      var plot=body.getPlot();plot.newEmptyChunk(plot.getCenterChunk());
      var child=plot.getEmbeddedLevelAccessor();
      for(var seat:SEATS) child.setBlock(seat,VehicleParts.ROLLER_BALL_SEAT.get().defaultBlockState().setValue(RollerBallSeatBlock.LEFT,seat.getX()<0).setValue(CarrierSeatBlock.FACING,seat.getZ()<0?net.minecraft.core.Direction.NORTH:net.minecraft.core.Direction.SOUTH),3);
      for(int x=-3;x<=2;x++)child.setBlock(new BlockPos(x,1,0),VehicleParts.ROLLER_BALL_AXLE.get().defaultBlockState(),3);
      blocks.setBlock(new BlockPos(0,2,0),VehicleParts.ROLLER_BALL_FRAME.get().defaultBlockState(),3);
      var center=plot.getCenterBlock();
      body.updateMergedMassData(1);
      var centerOfMass=body.getMassTracker().getCenterOfMass();
      var centerWorld=new Vector3d(centerOfMass).sub(center.getX()+.5,center.getY()+1.5,center.getZ()+.5)
          .rotate(orientation).add(world);
      // Native body position and rotationPoint describe COM; the joint owns the offset hinge.
      body.logicalPose().rotationPoint().set(centerOfMass);
      body.logicalPose().position().set(centerWorld);body.logicalPose().orientation().set(orientation);
      body.updateLastPose();
      var pipeline=container.physicsSystem().getPipeline();pipeline.onStatsChanged(body);
      pipeline.teleport(body,centerWorld,orientation);
      CoasterCartTrackSnap.applyInitialSnapAfterSpawn(chassis,level,hit);
      data.setDirty();FoundationSync.broadcast(level);return v;
    } catch(RuntimeException failure) {
      for(var c:v.carriers) {var body=container.getSubLevel(c.body);if(body!=null)container.removeSubLevel(body,SubLevelRemovalReason.REMOVED);}
      container.removeSubLevel(chassis,SubLevelRemovalReason.REMOVED);data.vehicles.remove(v.chassis);throw failure;
    }
  }
}
