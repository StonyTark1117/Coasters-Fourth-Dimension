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

/** Four rigid seats on the native chassis. No second body or rotary constraint exists. */
public final class PipelineVehicle {
  public static final String VARIANT="seated_side_rail_v1";
  public static final NativeTrackFit FIT=new NativeTrackFit("pipeline_p_contacts_v1",
      "between_side_rails_v1",new NativeTrackFit.Envelope(2.05,1.0,2.50,2.75));
  public static final List<BlockPos> SEATS=List.of(new BlockPos(-1,-2,-1),new BlockPos(1,-2,-1),
      new BlockPos(-1,-2,1),new BlockPos(1,-2,1));
  public static final List<BlockPos> FRAME=frame();
  private static List<BlockPos> frame(){
    var result=new java.util.ArrayList<BlockPos>();
    for(int z=-2;z<=2;z++)for(int x=-1;x<=1;x++)result.add(new BlockPos(x,-3,z));
    for(int z:new int[]{-2,2})for(int x=-1;x<=1;x++)result.add(new BlockPos(x,0,z));
    for(int z:new int[]{-2,2})for(int x:new int[]{-1,1})for(int y:new int[]{-2,-1})result.add(new BlockPos(x,y,z));
    result.add(new BlockPos(0,-1,0));
    return List.copyOf(result);
  }
  private PipelineVehicle(){}
  public static boolean is(FoundationData.Vehicle v){return v.family==Family.PIPELINE&&VARIANT.equals(v.variant);}
  public static void validate(FoundationData.Vehicle v){
    if(v.carriers.size()!=1)throw new IllegalArgumentException("Pipeline needs one fixed seat group");
    var c=v.carriers.getFirst();
    if(!c.body.equals(v.chassis)||!c.seats.equals(SEATS)||c.side!=0||c.longitudinal!=0||c.pivotHeight!=0
        ||c.childPivotOffset!=0||c.childPivotX!=0||c.childPivotZ!=0||c.angle!=0||c.omega!=0
        ||c.liftHeld||c.liftPending||v.damping!=0||v.ballast!=0||c.ballast!=0||v.recoveryCart!=null)
      throw new IllegalArgumentException("Invalid fixed Pipeline geometry/loading");
  }
  public static FoundationData.Vehicle create(ServerLevel level,CoasterPathTrackFrame.GraphHit hit){
    String reason=PipelineTrack.rejection(level,hit.edge());if(reason!=null)throw new IllegalArgumentException(reason);
    var obstruction=PipelineTrack.obstruction(level,hit);
    if(obstruction!=null)throw new IllegalArgumentException("Clear/load Pipeline envelope at "+obstruction.toShortString());
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
    var v=new FoundationData.Vehicle(chassis.getUniqueId(),Family.PIPELINE);
    v.variant=VARIANT;v.damping=0;v.stopping=true;v.boundFrom=hit.edge().from();v.boundTo=hit.edge().to();
    var c=new FoundationData.Carrier(v.chassis,0);c.pivotHeight=0;c.seats=SEATS;c.capture=true;v.carriers.add(c);
    data.vehicles.put(v.chassis,v);
    try{
      var blocks=chassis.getPlot().getEmbeddedLevelAccessor();
      blocks.setBlock(new BlockPos(0,-2,0),FamilyControllers.CONTROLLERS.get(Family.PIPELINE).get().defaultBlockState(),3);
      for(int i=0;i<FRAME.size();i++)blocks.setBlock(FRAME.get(i),VehicleParts.PIPELINE_FRAME.get().defaultBlockState().setValue(PipelineFrameBlock.PART,i),3);
      for(var seat:SEATS)blocks.setBlock(seat,VehicleParts.PIPELINE_SEAT.get().defaultBlockState().setValue(PipelineSeatBlock.RIGHT,seat.getX()>0).setValue(PipelineSeatBlock.REAR,seat.getZ()>0),3);
      CoasterCartTrackSnap.applyInitialSnapAfterSpawn(chassis,level,hit);
      data.setDirty();FoundationSync.broadcast(level);return v;
    }catch(RuntimeException failure){data.vehicles.remove(v.chassis);container.removeSubLevel(chassis,SubLevelRemovalReason.REMOVED);throw failure;}
  }
}
