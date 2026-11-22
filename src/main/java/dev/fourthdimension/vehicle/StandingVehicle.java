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
/** Two original standing places; Surf alone owns a translating native child. */
public final class StandingVehicle {
 public static final String STANDARD="standing_standard_v1",SURF="standing_surf_v1";
 public static final NativeTrackFit FIT=new NativeTrackFit(NativeTrackFit.N_CONTACTS,NativeTrackFit.ABOVE,new NativeTrackFit.Envelope(1.75,4,.95,1.75));
 public static final List<BlockPos> SEATS=List.of(new BlockPos(-1,1,0),new BlockPos(1,1,0));
 public static final BlockPos CARRIAGE=new BlockPos(0,1,0);
 public static final List<BlockPos> FRAME=List.of(new BlockPos(0,0,1));
 private StandingVehicle(){}
 public static boolean is(FoundationData.Vehicle v){return v.family==Family.STANDING&&(STANDARD.equals(v.variant)||SURF.equals(v.variant));}
 public static boolean surf(FoundationData.Vehicle v){return is(v)&&SURF.equals(v.variant);}
 public static TrackDesign design(FoundationData.Vehicle v){if(!is(v))throw new IllegalArgumentException("Unknown standing configuration");return surf(v)?TrackDesign.STANDING_SURF:TrackDesign.STANDING_STANDARD;}
 public static NativeTrackFit fit(TrackDesign d){if(d!=TrackDesign.STANDING_STANDARD&&d!=TrackDesign.STANDING_SURF)throw new IllegalArgumentException("Unknown standing track");return FIT;}
 public static void validate(FoundationData.Vehicle v){if(!is(v)||v.carriers.size()!=1)throw new IllegalArgumentException("Standing needs one original two-place group");var c=v.carriers.getFirst();if(c.body.equals(v.chassis)==surf(v)||!c.seats.equals(SEATS)||c.side!=0||c.longitudinal!=0||c.pivotHeight!=0||c.childPivotX!=0||c.childPivotOffset!=0||c.childPivotZ!=0||c.angle!=0||c.omega!=0||c.liftHeld||c.liftPending||v.damping!=0||v.ballast!=0||c.ballast!=0||v.recoveryCart!=null)throw new IllegalArgumentException("Invalid standing geometry/loading");v.standing.validate();}
 public static FoundationData.Vehicle create(ServerLevel l,CoasterPathTrackFrame.GraphHit hit,TrackDesign d){fit(d);var reason=StandingTrack.rejection(l,hit.edge(),d);if(reason!=null)throw new IllegalArgumentException(reason);var obstacle=StandingTrack.obstruction(l,hit);if(obstacle!=null)throw new IllegalArgumentException("Clear/load standing envelope at "+obstacle.toShortString());var data=FoundationData.get(l);if(data.vehicles.size()>=64)throw new IllegalArgumentException("Vehicle capacity reached");var container=SubLevelContainer.getContainer(l);var orientation=CoasterPathTrackFrame.quaternionFromTrackBasis(CoasterPathTrackFrame.trackBasisAtHit(l,hit));var bearing=CoasterPathTrackFrame.pathAnchorForSnappedBlock(l,hit);
  for(var other:data.vehicles.values()){var body=container.getSubLevel(other.chassis);var center=body==null?null:CoasterCartResolve.cartCenterWorld(body);if(center!=null&&center.distance(new Vector3d(bearing.x,bearing.y,bearing.z))<Math.max(8,WingVehicle.spacing(other))-.01)throw new IllegalArgumentException("Leave eight blocks or the other car's larger spacing between bearings");}
  var parent=CoasterCartSpawner.spawnMinimalContraption(l,CoasterCartLinkPlacement.plotOriginForBearingCenter(bearing,orientation),orientation,null,false);if(parent==null)throw new IllegalStateException("Native chassis allocation failed");var v=new FoundationData.Vehicle(parent.getUniqueId(),Family.STANDING);v.variant=d==TrackDesign.STANDING_SURF?SURF:STANDARD;v.damping=0;v.stopping=true;v.boundFrom=hit.edge().from();v.boundTo=hit.edge().to();data.vehicles.put(v.chassis,v);
  try{var blocks=parent.getPlot().getEmbeddedLevelAccessor();blocks.setBlock(new BlockPos(0,0,-1),FamilyControllers.CONTROLLERS.get(Family.STANDING).get().defaultBlockState(),3);for(var p:FRAME)blocks.setBlock(p,StandingContent.FRAME.get().defaultBlockState().setValue(StandingFrameBlock.SURF,surf(v)),3);
   ServerSubLevel owner=parent;if(surf(v)){var p=parent.getPlot().getCenterBlock();var world=parent.logicalPose().transformPosition(new Vector3d(p.getX()+.5,p.getY()+.5,p.getZ()+.5));var pose=new Pose3d();pose.position().set(world);pose.orientation().set(orientation);owner=(ServerSubLevel)container.allocateNewSubLevel(pose);owner.getPlot().newEmptyChunk(owner.getPlot().getCenterChunk());}
   var c=new FoundationData.Carrier(owner.getUniqueId(),0);c.pivotHeight=0;c.seats=SEATS;c.capture=true;c.latched=true;v.carriers.add(c);var childBlocks=owner.getPlot().getEmbeddedLevelAccessor();if(surf(v))childBlocks.setBlock(CARRIAGE,StandingContent.CARRIAGE.get().defaultBlockState(),3);for(var p:SEATS)childBlocks.setBlock(p,StandingContent.SEAT.get().defaultBlockState().setValue(StandingSeatBlock.RIGHT,p.getX()>0).setValue(StandingSeatBlock.SURF,surf(v)),3);
   if(surf(v)){owner.updateMergedMassData(1);var pc=parent.getPlot().getCenterBlock();var cc=owner.getPlot().getCenterBlock();var anchor=parent.logicalPose().transformPosition(new Vector3d(pc.getX()+.5,pc.getY()+.5,pc.getZ()+.5));var com=owner.getMassTracker().getCenterOfMass();var pos=new Vector3d(com).sub(cc.getX()+.5,cc.getY()+.5,cc.getZ()+.5).rotate(orientation).add(anchor);owner.logicalPose().rotationPoint().set(com);owner.logicalPose().position().set(pos);owner.logicalPose().orientation().set(orientation);owner.updateLastPose();var pipeline=container.physicsSystem().getPipeline();pipeline.onStatsChanged(owner);pipeline.teleport(owner,pos,orientation);}
   CoasterCartTrackSnap.applyInitialSnapAfterSpawn(parent,l,hit);data.setDirty();FoundationSync.broadcast(l);return v;
  }catch(RuntimeException failure){for(var c:v.carriers)if(!c.body.equals(v.chassis)){var b=container.getSubLevel(c.body);if(b!=null)container.removeSubLevel(b,SubLevelRemovalReason.REMOVED);}container.removeSubLevel(parent,SubLevelRemovalReason.REMOVED);data.vehicles.remove(v.chassis);throw failure;}
 }
}
