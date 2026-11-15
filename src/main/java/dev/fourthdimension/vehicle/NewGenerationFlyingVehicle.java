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

/** Physical chassis → yaw deck → two-seat posture carrier; no renderer-owned rotation. */
public final class NewGenerationFlyingVehicle {
  public static final String VARIANT="route_changeover_yaw_v1";
  public static final double PIVOT=-3.3125;
  public static final NativeTrackFit FIT=new NativeTrackFit(NativeTrackFit.N_CONTACTS,NativeTrackFit.OVERHEAD,new NativeTrackFit.Envelope(2.75,1.1,5.25,2.75));
  public static final List<BlockPos> SEATS=List.of(new BlockPos(-1,0,0),new BlockPos(0,0,0));
  private NewGenerationFlyingVehicle(){}
  public static boolean is(FoundationData.Vehicle v){return v.family==Family.FLYING&&VARIANT.equals(v.variant);}
  public static void validate(FoundationData.Vehicle v){
    if(v.carriers.size()!=1)throw new IllegalArgumentException("Compound Flying needs one yaw deck and one two-seat posture carrier");
    var c=v.carriers.getFirst();v.yaw.validate();v.flying.validate();
    if(c.body.equals(v.chassis)||c.body.equals(v.yaw.deck)||v.chassis.equals(v.yaw.deck)||!c.seats.equals(SEATS)||c.side!=0||c.longitudinal!=0||c.pivotHeight!=0||c.childPivotX!=-.5||c.childPivotOffset!=0||c.childPivotZ!=0||v.ballast!=0||c.ballast!=0||v.recoveryCart!=null)throw new IllegalArgumentException("Invalid compound Flying hierarchy/geometry");
  }
  public static FoundationData.Vehicle create(ServerLevel l,CoasterPathTrackFrame.GraphHit hit){
    var reason=NewGenerationFlyingTrack.rejection(l,hit.edge());if(reason!=null)throw new IllegalArgumentException(reason);
    var obstruction=NewGenerationFlyingTrack.obstruction(l,hit);if(obstruction!=null)throw new IllegalArgumentException("Clear compound yaw/posture envelope at "+obstruction.toShortString());
    var data=FoundationData.get(l);if(data.vehicles.size()>=64)throw new IllegalArgumentException("Vehicle capacity reached");
    var container=SubLevelContainer.getContainer(l);var orientation=CoasterPathTrackFrame.quaternionFromTrackBasis(CoasterPathTrackFrame.trackBasisAtHit(l,hit));var bearing=CoasterPathTrackFrame.pathAnchorForSnappedBlock(l,hit);
    for(var other:data.vehicles.values()){var b=container.getSubLevel(other.chassis);var center=b==null?null:CoasterCartResolve.cartCenterWorld(b);if(center!=null&&center.distance(new Vector3d(bearing.x,bearing.y,bearing.z))<Math.max(8,WingVehicle.spacing(other))-.01)throw new IllegalArgumentException("Leave eight blocks between bearings");}
    var chassis=CoasterCartSpawner.spawnMinimalContraption(l,CoasterCartLinkPlacement.plotOriginForBearingCenter(bearing,orientation),orientation,null,false);if(chassis==null)throw new IllegalStateException("Native chassis allocation failed");
    var v=new FoundationData.Vehicle(chassis.getUniqueId(),Family.FLYING);v.variant=VARIANT;v.stopping=true;v.damping=0;v.boundFrom=hit.edge().from();v.boundTo=hit.edge().to();data.vehicles.put(v.chassis,v);
    try{
      chassis.getPlot().getEmbeddedLevelAccessor().setBlock(new BlockPos(0,-1,0),FamilyControllers.CONTROLLERS.get(Family.FLYING).get().defaultBlockState(),3);
      var p=chassis.getPlot().getCenterBlock();var pivot=chassis.logicalPose().transformPosition(new Vector3d(p.getX()+.5,p.getY()+.5+PIVOT,p.getZ()+.5));
      var pose=new Pose3d();pose.position().set(pivot);pose.orientation().set(orientation);
      var deck=(ServerSubLevel)container.allocateNewSubLevel(pose);v.yaw.deck=deck.getUniqueId();var plot=deck.getPlot();plot.newEmptyChunk(plot.getCenterChunk());
      for(int i=0;i<NewGenerationFlyingDeckBlock.PARTS.size();i++)plot.getEmbeddedLevelAccessor().setBlock(NewGenerationFlyingDeckBlock.PARTS.get(i),VehicleParts.NEW_GENERATION_FLYING_DECK.get().defaultBlockState().setValue(NewGenerationFlyingDeckBlock.PART,i),3);
      initialize(deck,pivot,new Vector3d(.5,.5,.5),orientation);
      var child=(ServerSubLevel)container.allocateNewSubLevel(pose);var c=new FoundationData.Carrier(child.getUniqueId(),0);c.pivotHeight=0;c.childPivotX=-.5;c.capture=true;c.seats=SEATS;v.carriers.add(c);plot=child.getPlot();plot.newEmptyChunk(plot.getCenterChunk());
      for(var seat:SEATS)plot.getEmbeddedLevelAccessor().setBlock(seat,VehicleParts.NEW_GENERATION_FLYING_SEAT.get().defaultBlockState(),3);
      plot.getEmbeddedLevelAccessor().setBlock(new BlockPos(0,-1,0),VehicleParts.FLYING_LOCK.get().defaultBlockState(),3);
      initialize(child,pivot,new Vector3d(0,.5,.5),orientation);
      CoasterCartTrackSnap.applyInitialSnapAfterSpawn(chassis,l,hit);data.setDirty();FoundationSync.broadcast(l);return v;
    }catch(RuntimeException e){if(Boolean.getBoolean("fourthdimension.validation"))e.printStackTrace();for(var c:v.carriers){var b=container.getSubLevel(c.body);if(b!=null)container.removeSubLevel(b,SubLevelRemovalReason.REMOVED);}if(v.yaw.deck!=null){var b=container.getSubLevel(v.yaw.deck);if(b!=null)container.removeSubLevel(b,SubLevelRemovalReason.REMOVED);}container.removeSubLevel(chassis,SubLevelRemovalReason.REMOVED);data.vehicles.remove(v.chassis);throw e;}
  }
  private static void initialize(ServerSubLevel b,Vector3d world,Vector3d local,org.joml.Quaterniondc orientation){
    var p=b.getPlot().getCenterBlock();b.updateMergedMassData(1);var com=b.getMassTracker().getCenterOfMass();if(com==null||b.getMassTracker().getMass()<=0)throw new IllegalStateException("Compound body has no native mass: "+b.getUniqueId()+" mass="+b.getMassTracker().getMass());var position=new Vector3d(com).sub(p.getX()+local.x,p.getY()+local.y,p.getZ()+local.z).rotate(orientation).add(world);
    b.logicalPose().rotationPoint().set(com);b.logicalPose().position().set(position);b.logicalPose().orientation().set(orientation);b.updateLastPose();var pipeline=SubLevelContainer.getContainer(b.getLevel()).physicsSystem().getPipeline();pipeline.onStatsChanged(b);pipeline.teleport(b,position,orientation);
  }
}
