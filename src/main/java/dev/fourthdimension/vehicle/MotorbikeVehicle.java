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

/** Two original straddle seats on the native chassis. No second body or rotary constraint exists. */
public final class MotorbikeVehicle {
  public static final String VARIANT="straddle_v1";
  public static final NativeTrackFit FIT=new NativeTrackFit(NativeTrackFit.N_CONTACTS,
      NativeTrackFit.ABOVE,new NativeTrackFit.Envelope(1.85,2.65,.95,1.5));
  public static final List<BlockPos> SEATS=List.of(new BlockPos(-1,1,0),new BlockPos(1,1,0));
  public static final List<BlockPos> FRAME=List.of(new BlockPos(-1,0,0),new BlockPos(1,0,0));
  private MotorbikeVehicle(){}
  public static boolean is(FoundationData.Vehicle v){return v.family==Family.MOTORBIKE&&VARIANT.equals(v.variant);}
  public static void validate(FoundationData.Vehicle v){
    if(v.carriers.size()!=1)throw new IllegalArgumentException("Motorbike needs one fixed seat group");
    var c=v.carriers.getFirst();
    if(!c.body.equals(v.chassis)||!c.seats.equals(SEATS)||c.side!=0||c.longitudinal!=0||c.pivotHeight!=0
        ||c.childPivotOffset!=0||c.childPivotX!=0||c.childPivotZ!=0||c.angle!=0||c.omega!=0
        ||c.liftHeld||c.liftPending||v.damping!=0||v.ballast!=0||c.ballast!=0||v.recoveryCart!=null)
      throw new IllegalArgumentException("Invalid fixed Motorbike geometry/loading");
  }
  public static FoundationData.Vehicle create(ServerLevel level,CoasterPathTrackFrame.GraphHit hit){
    String reason=MotorbikeTrack.rejection(level,hit.edge());if(reason!=null)throw new IllegalArgumentException(reason);
    var obstruction=MotorbikeTrack.obstruction(level,hit);
    if(obstruction!=null)throw new IllegalArgumentException("Clear/load Motorbike envelope at "+obstruction.toShortString());
    var data=FoundationData.get(level);if(data.vehicles.size()>=64)throw new IllegalArgumentException("Vehicle capacity reached");
    var container=SubLevelContainer.getContainer(level);
    var orientation=CoasterPathTrackFrame.quaternionFromTrackBasis(CoasterPathTrackFrame.trackBasisAtHit(level,hit));
    var bearing=CoasterPathTrackFrame.pathAnchorForSnappedBlock(level,hit);
    for(var other:data.vehicles.values()){
      var body=container.getSubLevel(other.chassis);var center=body==null?null:CoasterCartResolve.cartCenterWorld(body);
      if(center!=null&&center.distance(new Vector3d(bearing.x,bearing.y,bearing.z))<Math.max(6,WingVehicle.spacing(other))-.01)
        throw new IllegalArgumentException("Leave six blocks or the other car's larger spacing between bearings");
    }
    var chassis=CoasterCartSpawner.spawnMinimalContraption(level,
        CoasterCartLinkPlacement.plotOriginForBearingCenter(bearing,orientation),orientation,null,false);
    if(chassis==null)throw new IllegalStateException("Native chassis allocation failed");
    var v=new FoundationData.Vehicle(chassis.getUniqueId(),Family.MOTORBIKE);
    v.variant=VARIANT;v.damping=0;v.stopping=true;v.boundFrom=hit.edge().from();v.boundTo=hit.edge().to();
    var c=new FoundationData.Carrier(v.chassis,0);c.pivotHeight=0;c.seats=SEATS;c.capture=true;v.carriers.add(c);
    data.vehicles.put(v.chassis,v);
    try{
      var blocks=chassis.getPlot().getEmbeddedLevelAccessor();
      blocks.setBlock(new BlockPos(0,1,0),FamilyControllers.CONTROLLERS.get(Family.MOTORBIKE).get().defaultBlockState(),3);
      for(int i=0;i<FRAME.size();i++)blocks.setBlock(FRAME.get(i),MotorbikeContent.FRAME.get().defaultBlockState(),3);
      for(var seat:SEATS)blocks.setBlock(seat,MotorbikeContent.SEAT.get().defaultBlockState(),3);
      CoasterCartTrackSnap.applyInitialSnapAfterSpawn(chassis,level,hit);
      data.setDirty();FoundationSync.broadcast(level);return v;
    }catch(RuntimeException failure){data.vehicles.remove(v.chassis);container.removeSubLevel(chassis,SubLevelRemovalReason.REMOVED);throw failure;}
  }
}
