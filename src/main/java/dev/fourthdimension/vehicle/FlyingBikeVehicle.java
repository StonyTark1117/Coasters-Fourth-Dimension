package dev.fourthdimension.vehicle;

import dev.fourthdimension.contract.*;
import dev.fourthdimension.runtime.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;

/** Two rigid prone seats on the native chassis. No second body or rotary constraint exists. */
public final class FlyingBikeVehicle {
  public static final String VARIANT="prone_multilaunch_v1";
  public static final NativeTrackFit FIT=new NativeTrackFit(NativeTrackFit.N_CONTACTS,
      NativeTrackFit.OVERHEAD,new NativeTrackFit.Envelope(2.25,1.10,4.50,2.50));
  public static final List<BlockPos> SEATS=List.of(new BlockPos(-1,-3,0),new BlockPos(1,-3,0));
  public static final List<BlockPos> FRAME=List.of(new BlockPos(-1,-1,0),new BlockPos(1,-1,0),
      new BlockPos(-1,-2,0),new BlockPos(1,-2,0));
  private FlyingBikeVehicle(){}
  public static boolean is(FoundationData.Vehicle v){return v.family==Family.FLYING_BIKE&&VARIANT.equals(v.variant);}
  public static void validate(FoundationData.Vehicle v){
    if(v.carriers.size()!=1)throw new IllegalArgumentException("Flying Bike needs one fixed seat group");
    var c=v.carriers.getFirst();
    if(!c.body.equals(v.chassis)||!c.seats.equals(SEATS)||c.side!=0||c.longitudinal!=0||c.pivotHeight!=0
        ||c.childPivotOffset!=0||c.childPivotX!=0||c.childPivotZ!=0||c.angle!=0||c.omega!=0
        ||c.liftHeld||c.liftPending||v.damping!=0||v.ballast!=0||c.ballast!=0||v.recoveryCart!=null)
      throw new IllegalArgumentException("Invalid fixed overhead geometry/loading");
  }
  public static FoundationData.Vehicle create(ServerLevel level,CoasterPathTrackFrame.GraphHit hit){
    String reason=FlyingBikeTrack.rejection(level,hit.edge());if(reason!=null)throw new IllegalArgumentException(reason);
    var obstruction=FlyingBikeTrack.obstruction(level,hit);
    if(obstruction!=null)throw new IllegalArgumentException("Clear/load Flying Bike envelope at "+obstruction.toShortString());
    var data=FoundationData.get(level);if(data.vehicles.size()>=64)throw new IllegalArgumentException("Vehicle capacity reached");
    var container=SubLevelContainer.getContainer(level);
    var orientation=CoasterPathTrackFrame.quaternionFromTrackBasis(CoasterPathTrackFrame.trackBasisAtHit(level,hit));
    var bearing=CoasterPathTrackFrame.pathAnchorForSnappedBlock(level,hit);
    for(var other:data.vehicles.values()){
      var body=container.getSubLevel(other.chassis);var center=body==null?null:CoasterCartResolve.cartCenterWorld(body);
      if(center!=null&&center.distance(new Vector3d(bearing.x,bearing.y,bearing.z))<Math.max(8,WingVehicle.spacing(other))-.01)
        throw new IllegalArgumentException("Leave eight blocks or the other car's larger spacing between bearings");
    }
    var chassis=CoasterCartSpawner.spawnMinimalContraption(level,
        CoasterCartLinkPlacement.plotOriginForBearingCenter(bearing,orientation),orientation,null,false);
    if(chassis==null)throw new IllegalStateException("Native chassis allocation failed");
    var v=new FoundationData.Vehicle(chassis.getUniqueId(),Family.FLYING_BIKE);
    v.variant=VARIANT;v.damping=0;v.stopping=true;v.boundFrom=hit.edge().from();v.boundTo=hit.edge().to();
    var c=new FoundationData.Carrier(v.chassis,0);c.pivotHeight=0;c.seats=SEATS;c.capture=true;v.carriers.add(c);
    data.vehicles.put(v.chassis,v);
    try{
      var blocks=chassis.getPlot().getEmbeddedLevelAccessor();
      blocks.setBlock(new BlockPos(0,-1,0),FamilyControllers.CONTROLLERS.get(Family.FLYING_BIKE).get().defaultBlockState(),3);
      for(int i=0;i<FRAME.size();i++)blocks.setBlock(FRAME.get(i),FlyingBikeContent.FRAME.get().defaultBlockState().setValue(FlyingBikeFrameBlock.PART,i),3);
      for(var seat:SEATS)blocks.setBlock(seat,FlyingBikeContent.SEAT.get().defaultBlockState(),3);
      CoasterCartTrackSnap.applyInitialSnapAfterSpawn(chassis,level,hit);
      data.setDirty();FoundationSync.broadcast(level);return v;
    }catch(RuntimeException failure){data.vehicles.remove(v.chassis);container.removeSubLevel(chassis,SubLevelRemovalReason.REMOVED);throw failure;}
  }
}
