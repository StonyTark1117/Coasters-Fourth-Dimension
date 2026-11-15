package dev.fourthdimension.vehicle;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.transmission.SplitShaftBlockEntity;
import dev.fourthdimension.integration.RideAuthority;
import dev.fourthdimension.runtime.FoundationData;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.chainlift.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

/** Real Create transmission. A fresh ride-authority lease requests speed; it never supplies energy. */
public final class S2SpinningGovernorEntity extends SplitShaftBlockEntity implements RideAuthority.Controller {
  public UUID vehicle;
  public BlockPos anchor;
  private UUID identity=UUID.randomUUID();
  private List<UUID> cars=List.of();
  private double outputRpm,requestedBps,acceleration=3;
  private long leaseTick=Long.MIN_VALUE,leaseEpoch;
  private RideAuthority.Token token;
  private String fault="Unbound";
  public S2SpinningGovernorEntity(BlockPos pos,BlockState state){super(S2SpinningContent.GOVERNOR_ENTITY.get(),pos,state);}
  public Direction output(){return getBlockState().getValue(S2SpinningGovernorBlock.FACING);}
  public double inputRpm(){
    if(level==null||!level.hasChunkAt(worldPosition.relative(output().getOpposite())))return 0;
    var block=level.getBlockEntity(worldPosition.relative(output().getOpposite()));
    return block instanceof KineticBlockEntity k&&!k.isOverStressed()?k.getSpeed():0;
  }
  @Override public float calculateStressApplied(){return lastStressApplied=8;}
  @Override public float getRotationSpeedModifier(Direction face){
    if(face==output().getOpposite())return 1;
    double input=inputRpm();
    return face==output()&&Math.abs(input)>.001&&!isOverStressed()?(float)(outputRpm/input):0;
  }
  @Override public UUID authorityId(){return identity;}
  @Override public boolean authorizesRide(UUID id){return vehicle!=null&&cars.contains(id);}
  @Override public boolean authorityAvailable(UUID id){return authorizesRide(id)&&availability()==null;}
  public String status(){return String.format(java.util.Locale.ROOT,"%s input %.1f RPM, output %.1f RPM, Chain %.2f b/s",fault,inputRpm(),outputRpm,actualBps());}
  public double outputRpm(){return outputRpm;}
  public double actualBps(){
    var a=anchorEntity();var path=a==null?null:a.resolveChainLiftSpanPath(0);
    // Native applySpeedMatch negates Chain world velocity projected on track forward.
    // Expose vehicle travel in the committed span direction, not belt-top motion.
    return path==null?0:-ChainLiftDriveDirection.signedChainBlocksPerSecond(a,path,0,256,16);
  }
  private CoasterAnchorpointBlockEntity anchorEntity(){
    return level!=null&&anchor!=null&&level.hasChunkAt(anchor)&&level.getBlockEntity(anchor) instanceof CoasterAnchorpointBlockEntity a?a:null;
  }
  private BlockPos discoverAnchor(){
    for(int i=1;i<=16;i++){
      var pos=worldPosition.relative(output(),i);if(!level.hasChunkAt(pos))break;
      if(level.getBlockEntity(pos) instanceof CoasterAnchorpointBlockEntity a){
        if(a.hasChainLiftSpan()&&a.getChainLiftShaftAxis()==output().getAxis())return pos;
        break;
      }
      var state=level.getBlockState(pos);
      if(!state.is(AllBlocks.SHAFT.get())||state.getValue(BlockStateProperties.AXIS)!=output().getAxis())break;
    }return null;
  }
  public String availability(){
    if(level==null||anchor==null||!anchor.equals(discoverAnchor()))return "Restore the straight output shaft to the bound Chain anchor";
    var a=anchorEntity();
    if(a==null||a.resolveChainLiftSpanPath(0)==null||a.getChainLiftCommittedLength()<24||Math.abs(a.getChainLiftTravelSign())!=1)
      return "Restore a >=24-block committed native Chain span";
    if(Math.abs(inputRpm())<.001||isOverStressed())return "Supply non-overstressed kinetic power to the rear input";
    return null;
  }
  public void bind(FoundationData.Vehicle selected){
    if(!(level instanceof ServerLevel server))throw new IllegalArgumentException("Server binding required");
    var bound=VehicleOperations.consist(server,selected);
    if(bound.stream().anyMatch(v->!v.stopping||!v.carriers.getFirst().occupants.isEmpty()))
      throw new IllegalArgumentException("Bind only an empty stopped complete train");
    var found=discoverAnchor();if(found==null)throw new IllegalArgumentException("Connect the output shaft to a committed Chain anchor within sixteen blocks");
    if(vehicle!=null&&!new HashSet<>(cars).equals(new HashSet<>(bound.stream().map(v->v.chassis).toList())))
      throw new IllegalArgumentException("This governor already owns a different consist");
    anchor=found;vehicle=selected.chassis;cars=bound.stream().map(v->v.chassis).toList();
    String reason=availability();if(reason!=null)throw new IllegalArgumentException(reason);
    if(selected.authority.owner==null)token=RideAuthority.claim(server,worldPosition,vehicle,RideAuthority.NATIVE_CHAIN);
    else if(!identity.equals(selected.authority.owner))throw new IllegalArgumentException("Another controller owns this ride");
    else token=RideAuthority.token(selected);
    for(var v:bound)if(S2SpinningVehicle.is(v))v.s2.governor=worldPosition.immutable();
    fault="Held";setChanged();sendData();
  }
  @Override public void validateRelease(ServerLevel server,FoundationData.Vehicle selected){
    var reason=availability();if(reason!=null)throw new IllegalArgumentException(reason);
    if(Math.abs(inputRpm())<192)throw new IllegalArgumentException("Supply at least192 RPM for the12 b/s S2 launch");
    var path=anchorEntity().resolveChainLiftSpanPath(0);var container=SubLevelContainer.getContainer(server);
    for(var v:VehicleOperations.consist(server,selected)){
      if(!authorizesRide(v.chassis))throw new IllegalArgumentException("Governor consist binding changed");
      var r=S2SpinningRuntime.rail(server,v);var edge=r==null?null:dev.fourthdimension.track.TrackGeometry.resolve(server,r);
      var parent=container.getSubLevel(v.chassis);var center=parent==null?null:CoasterCartResolve.cartCenterWorld(parent);
      if(edge==null||center==null)throw new IllegalArgumentException("Native launch route unavailable");
      var hit=CoasterPathTrackFrame.closestHitOnEdge(server,edge,new Vec3(center.x,center.y,center.z));
      // Initial launch needs every car on the governed span. Explicit rollback recovery
      // on a coast section may revalidate without claiming that it has Chain propulsion.
      if(v.s2.phase.equals("DOCK_OPEN")||v.s2.phase.equals("CAPTURED"))
        if(hit==null||!ChainLiftSpanQuery.graphHitOnSpan(path,hit))throw new IllegalArgumentException("Place every car on the governed launch span");
    }
  }
  public void acknowledge(FoundationData.Vehicle selected){
    var server=(ServerLevel)level;
    RideAuthority.holds(server,selected);
    token=RideAuthority.token(selected);
    if(selected.authority.suspended)token=RideAuthority.resume(server,worldPosition,selected.chassis,token);
    else if(!selected.authority.held)token=RideAuthority.hold(server,worldPosition,selected.chassis,token);
    setOutput(0);leaseTick=Long.MIN_VALUE;fault="Held";
  }
  public void release(FoundationData.Vehicle selected){
    var server=(ServerLevel)level;
    if(token==null)throw new IllegalArgumentException("Explicitly Capture/revalidate the governor before release");
    token=RideAuthority.release(server,worldPosition,selected.chassis,token);
    leaseEpoch=selected.authority.epoch;
    for(var v:VehicleOperations.consist(server,selected))if(S2SpinningVehicle.is(v))v.s2.epoch=leaseEpoch;
    fault="Running";setChanged();
  }
  public void command(UUID chassis,long epoch,double signedBps,double maxAcceleration){
    if(!(level instanceof ServerLevel server)||!server.getServer().isSameThread()
        ||!authorizesRide(chassis)||!RideAuthority.permits(server,chassis,identity,epoch,RideAuthority.NATIVE_CHAIN))
      throw new IllegalArgumentException("Stale or held governor authority");
    if(!Double.isFinite(signedBps)||Math.abs(signedBps)>12||!Double.isFinite(maxAcceleration)||maxAcceleration<=0||maxAcceleration>3)
      throw new IllegalArgumentException("Governor speed <=12 b/s and acceleration <=3 b/s²");
    if(outputRpm* -signedBps*anchorEntity().getChainLiftTravelSign() < -.01)
      throw new IllegalArgumentException("Stop and explicitly authorize reversal before changing drive sign");
    requestedBps=signedBps;acceleration=maxAcceleration;leaseEpoch=epoch;leaseTick=server.getGameTime();
  }
  @Override public void tick(){
    super.tick();if(!(level instanceof ServerLevel server))return;
    var reason=availability();
    boolean permitted=vehicle!=null&&reason==null&&leaseTick>=0&&server.getGameTime()-leaseTick<=1
        &&RideAuthority.permits(server,vehicle,identity,leaseEpoch,RideAuthority.NATIVE_CHAIN);
    if(!permitted){if(outputRpm!=0)setOutput(0);if(reason!=null)fault=reason;return;}
    double target=-requestedBps*16*anchorEntity().getChainLiftTravelSign();
    double maximum=Math.min(192,Math.abs(inputRpm()));target=Math.clamp(target,-maximum,maximum);
    setOutput(outputRpm+Math.clamp(target-outputRpm,-acceleration*.05*16,acceleration*.05*16));
  }
  private void setOutput(double next){
    if(Math.abs(next-outputRpm)<1e-6)return;
    // A smooth ratio change keeps the supplied input network attached. Repeated
    // detach/re-attach inserts a zero-speed transition every tick and correctly
    // trips Create's flicker protection. Update only this straight, source-owned
    // shaft line, then let Create account for its real stress at the new speed.
    var line=new ArrayList<KineticBlockEntity>();
    BlockPos previous=worldPosition;
    for(int i=1;i<=16;i++){
      var pos=worldPosition.relative(output(),i);
      if(!(level.getBlockEntity(pos) instanceof KineticBlockEntity k))break;
      if(!pos.equals(anchor)&&(!k.getBlockState().is(AllBlocks.SHAFT.get())
          ||k.getBlockState().getValue(BlockStateProperties.AXIS)!=output().getAxis()))break;
      if(k.hasSource()&&!previous.equals(k.source)){
        // Never overwrite a competing source or synthesize an independent drive.
        next=0;break;
      }
      line.add(k);previous=pos;if(pos.equals(anchor))break;
    }
    if(next!=0&&(line.isEmpty()||!line.getLast().getBlockPos().equals(anchor)
        ||availability()!=null||!hasSource()||getSourceFacing()!=output().getOpposite()))next=0;
    outputRpm=next;previous=worldPosition;
    for(var k:line){
      float old=k.getSpeed();
      if(!k.hasSource())k.setSource(previous);
      k.setSpeed((float)next);k.onSpeedChanged(old);k.sendData();
      previous=k.getBlockPos();
    }
    if(hasNetwork())getOrCreateNetwork().updateNetwork();
    setChanged();sendData();
  }
  @Override protected void write(CompoundTag n,HolderLookup.Provider registries,boolean client){
    super.write(n,registries,client);n.putUUID("authority_id",identity);
    if(vehicle!=null)n.putUUID("vehicle",vehicle);if(anchor!=null)n.putLong("anchor",anchor.asLong());
    var ids=new ListTag();for(var id:cars)ids.add(NbtUtils.createUUID(id));n.put("cars",ids);
    n.putDouble("ramp_rpm",outputRpm);n.putString("fault",fault);
  }
  @Override protected void read(CompoundTag n,HolderLookup.Provider registries,boolean client){
    super.read(n,registries,client);if(n.hasUUID("authority_id"))identity=n.getUUID("authority_id");
    vehicle=n.hasUUID("vehicle")?n.getUUID("vehicle"):null;anchor=n.contains("anchor")?BlockPos.of(n.getLong("anchor")):null;
    var ids=new ArrayList<UUID>();for(Tag id:n.getList("cars",Tag.TAG_INT_ARRAY))ids.add(NbtUtils.loadUUID(id));cars=List.copyOf(ids);
    outputRpm=client?n.getDouble("ramp_rpm"):0;leaseTick=Long.MIN_VALUE;fault=client?n.getString("fault"):"Restored; explicitly Capture then Dispatch/Resume";
  }
}
