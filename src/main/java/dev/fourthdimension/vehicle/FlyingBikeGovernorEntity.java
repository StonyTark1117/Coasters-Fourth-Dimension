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
public final class FlyingBikeGovernorEntity extends SplitShaftBlockEntity implements RideAuthority.Controller {
  public UUID vehicle;
  public BlockPos anchor, peer, leader;
  private UUID peerIdentity;
  private long boundGeometry;
  private UUID identity=UUID.randomUUID();
  private List<UUID> cars=List.of();
  private double outputRpm,requestedBps,acceleration=3;
  private long leaseTick=Long.MIN_VALUE,leaseEpoch;
  private RideAuthority.Token token;
  private String fault="Unbound";
  public FlyingBikeGovernorEntity(BlockPos pos,BlockState state){super(FlyingBikeContent.GOVERNOR_ENTITY.get(),pos,state);}
  public Direction output(){return getBlockState().getValue(FlyingBikeGovernorBlock.FACING);}
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
  @Override public boolean authorityAvailable(UUID id){return leader==null&&authorizesRide(id)&&availability()==null;}
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
  private long geometry(){
    var a=anchorEntity();var path=a==null?null:a.resolveChainLiftSpanPath(0);
    if(path==null||!(level instanceof ServerLevel server))return 0;
    long hash=Double.doubleToLongBits(a.getChainLiftCommittedLength());
    for(var segment:path.segments()){
      var edge=segment.edge();var r=dev.fourthdimension.moving.MovingRouteContext.rails(server).stream().filter(rail->rail.matches(edge.from(),edge.to())).findFirst().orElse(null);
      if(r==null||!r.valid)return 0;hash=31*hash+edge.from().asLong();hash=31*hash+edge.to().asLong();hash=31*hash+r.geometryHash;
    }return hash;
  }
  public String localAvailability(){
    if(level==null||anchor==null||!anchor.equals(discoverAnchor()))return "Restore the straight output shaft to the bound Chain anchor";
    var a=anchorEntity();
    if(a==null||a.resolveChainLiftSpanPath(0)==null||a.getChainLiftCommittedLength()<32||Math.abs(a.getChainLiftTravelSign())!=1)
      return "Restore a >=32-block committed native Chain span";
    if(boundGeometry!=0&&boundGeometry!=geometry())return "Bound launch geometry changed; restore its exact span";
    if(Math.abs(inputRpm())<.001||isOverStressed())return "Supply non-overstressed kinetic power to the rear input";
    return null;
  }
  public FlyingBikeGovernorEntity second(){
    return level!=null&&peer!=null&&level.hasChunkAt(peer)&&level.getBlockEntity(peer) instanceof FlyingBikeGovernorEntity g
        &&identity.equals(g.peerIdentity)&&worldPosition.equals(g.leader)&&g.identity.equals(peerIdentity)?g:null;
  }
  private FlyingBikeGovernorEntity coordinator(){
    if(leader==null)return this;
    return level!=null&&level.hasChunkAt(leader)&&level.getBlockEntity(leader) instanceof FlyingBikeGovernorEntity g&&g.second()==this?g:null;
  }
  public String availability(){
    var reason=localAvailability();if(reason!=null)return reason;
    if(leader!=null)return "Use the first launch coordinator";
    var next=second();if(next==null)return "Link exactly two distinct flying-bike launch governors";
    reason=next.localAvailability();if(reason!=null)return "Second launch: "+reason;
    if(anchor.equals(next.anchor))return "Launch anchors must be distinct";
    if(Math.abs(inputRpm())<192||Math.abs(next.inputRpm())<192)return "Supply 192 RPM and adequate stress to both launches";
    return null;
  }
  public void bind(FoundationData.Vehicle selected){
    if(!(level instanceof ServerLevel server)||!FlyingBikeVehicle.is(selected)||leader!=null)
      throw new IllegalArgumentException("Bind the first governor to a Flying Bike");
    if(!selected.stopping||selected.carriers.stream().anyMatch(c->!c.occupants.isEmpty()))
      throw new IllegalArgumentException("Bind only an empty stopped car");
    if(vehicle!=null&&!vehicle.equals(selected.chassis))throw new IllegalArgumentException("Governor already belongs to another car");
    var found=discoverAnchor();if(found==null)throw new IllegalArgumentException("Connect output shaft to a committed Chain within sixteen blocks");
    anchor=found;var reason=localAvailability();if(reason!=null)throw new IllegalArgumentException(reason);
    boundGeometry=geometry();
    vehicle=selected.chassis;cars=List.of(vehicle);selected.flyingBike.governor=worldPosition.immutable();
    fault="Link second launch";setChanged();sendData();
  }
  public void link(FlyingBikeGovernorEntity next){
    if(!(level instanceof ServerLevel server)||vehicle==null||leader!=null||next==this||next.level!=level
        ||worldPosition.distSqr(next.worldPosition)>256*256||next.vehicle!=null)
      throw new IllegalArgumentException("Choose an unbound second governor within 256 blocks");
    var v=FoundationData.get(server).vehicles.get(vehicle);
    if(v==null||!v.stopping||v.carriers.stream().anyMatch(c->!c.occupants.isEmpty()))throw new IllegalArgumentException("Stop and empty the bound car before linking");
    if(peer!=null)throw new IllegalArgumentException("First launch is already linked");
    if(v.authority.owner!=null&&!identity.equals(v.authority.owner))throw new IllegalArgumentException("Another controller owns this car");
    var ownReason=localAvailability();if(ownReason!=null)throw new IllegalArgumentException(ownReason);
    if(Math.abs(inputRpm())<192||Math.abs(next.inputRpm())<192)throw new IllegalArgumentException("Supply 192 RPM to both governors before linking");
    next.anchor=next.discoverAnchor();String reason=next.localAvailability();if(reason!=null)throw new IllegalArgumentException(reason);
    if(next.anchor.equals(anchor))throw new IllegalArgumentException("Launch spans must be distinct");
    var p=anchorEntity().resolveChainLiftSpanPath(0);var q=next.anchorEntity().resolveChainLiftSpanPath(0);
    for(var a:p.segments())for(var b:q.segments())if(a.edge().from().equals(b.edge().from())&&a.edge().to().equals(b.edge().to())||a.edge().from().equals(b.edge().to())&&a.edge().to().equals(b.edge().from()))
      throw new IllegalArgumentException("Launch spans must not overlap");
    next.boundGeometry=next.geometry();
    peer=next.worldPosition.immutable();peerIdentity=next.identity;next.leader=worldPosition.immutable();next.peerIdentity=identity;
    next.vehicle=vehicle;next.cars=List.of(vehicle);
    try {
      if(v.authority.owner==null)token=RideAuthority.claim(server,worldPosition,vehicle,RideAuthority.NATIVE_CHAIN);
      else token=RideAuthority.token(v);
    } catch(RuntimeException failure) {
      peer=null;peerIdentity=null;next.boundGeometry=0;next.leader=null;next.peerIdentity=null;next.vehicle=null;next.cars=List.of();
      throw failure;
    }
    FoundationData.get(server).setDirty();
    fault="Held";next.fault="Subordinate launch";setChanged();next.setChanged();sendData();next.sendData();
  }
  public int spanAt(CoasterPathTrackFrame.GraphHit hit){
    var next=second();if(hit==null||next==null)return 0;
    if(anchorEntity()==null||next.anchorEntity()==null)return 0;
    if(ChainLiftSpanQuery.graphHitOnSpan(anchorEntity().resolveChainLiftSpanPath(0),hit))return 1;
    if(ChainLiftSpanQuery.graphHitOnSpan(next.anchorEntity().resolveChainLiftSpanPath(0),hit))return 2;
    return 0;
  }
  public void drive(FoundationData.Vehicle v,int active,double approachSpeed){
    // Native Chain speed matching must meet an approaching car, not catch it on a stopped belt.
    double matching=Math.clamp(approachSpeed,0,12);
    command(v.chassis,v.authority.epoch,active==1?8:matching,3);
    var next=second();if(next==null)throw new IllegalArgumentException("Second launch unavailable");
    next.command(v.chassis,v.authority.epoch,active==2?12:matching,3);
  }
  public void cut(){setOutput(0);leaseTick=Long.MIN_VALUE;var next=second();if(next!=null){next.setOutput(0);next.leaseTick=Long.MIN_VALUE;}}
  @Override public void validateRelease(ServerLevel server,FoundationData.Vehicle selected){
    var reason=availability();if(reason!=null)throw new IllegalArgumentException(reason);
    if(!FlyingBikeVehicle.is(selected)||!vehicle.equals(selected.chassis))throw new IllegalArgumentException("Wrong Flying Bike owner");
    FlyingBikeRuntime.validateRoute(server,selected,this);
  }
  public void acknowledge(FoundationData.Vehicle selected){
    var server=(ServerLevel)level;
    RideAuthority.holds(server,selected);
    token=RideAuthority.token(selected);
    if(selected.authority.suspended)token=RideAuthority.resume(server,worldPosition,selected.chassis,token);
    else if(!selected.authority.held)token=RideAuthority.hold(server,worldPosition,selected.chassis,token);
    cut();fault="Held";
  }
  public void release(FoundationData.Vehicle selected){
    var server=(ServerLevel)level;
    if(token==null)throw new IllegalArgumentException("Explicitly Stop/revalidate the governor before release");
    token=RideAuthority.release(server,worldPosition,selected.chassis,token);
    leaseEpoch=selected.authority.epoch;

    fault="Running";setChanged();
  }
  public void command(UUID chassis,long epoch,double signedBps,double maxAcceleration){
    if(!(level instanceof ServerLevel server)||!server.getServer().isSameThread()
        ||!authorizesRide(chassis)||coordinator()==null||!RideAuthority.permits(server,chassis,coordinator().identity,epoch,RideAuthority.NATIVE_CHAIN))
      throw new IllegalArgumentException("Stale or held governor authority");
    if(!Double.isFinite(signedBps)||Math.abs(signedBps)>12||!Double.isFinite(maxAcceleration)||maxAcceleration<=0||maxAcceleration>3)
      throw new IllegalArgumentException("Governor speed <=12 b/s and acceleration <=3 b/s²");
    if(outputRpm* -signedBps*anchorEntity().getChainLiftTravelSign() < -.01)
      throw new IllegalArgumentException("Stop and explicitly authorize reversal before changing drive sign");
    requestedBps=signedBps;acceleration=maxAcceleration;leaseEpoch=epoch;leaseTick=server.getGameTime();
  }
  @Override public void tick(){
    super.tick();if(!(level instanceof ServerLevel server))return;
    var master=coordinator();var reason=master==null?"Coordinator unavailable":master.availability();
    boolean permitted=vehicle!=null&&reason==null&&leaseTick>=0&&server.getGameTime()-leaseTick<=1
        &&master!=null&&RideAuthority.permits(server,vehicle,master.identity,leaseEpoch,RideAuthority.NATIVE_CHAIN);
    if(!permitted){if(outputRpm!=0||Math.abs(actualBps())>.001)setOutput(0);if(reason!=null)fault=reason;return;}
    double target=-requestedBps*16*anchorEntity().getChainLiftTravelSign();
    double maximum=Math.min(192,Math.abs(inputRpm()));target=Math.clamp(target,-maximum,maximum);
    setOutput(outputRpm+Math.clamp(target-outputRpm,-acceleration*.05*16,acceleration*.05*16));
  }
  private void setOutput(double next){
    // Create persists the driven shaft speed separately from our transient lease.
    // A zero request must reconcile that line even after read reset outputRpm.
    if(next!=0&&Math.abs(next-outputRpm)<1e-6)return;
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
        ||localAvailability()!=null||!hasSource()||getSourceFacing()!=output().getOpposite()))next=0;
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
    n.putLong("bound_geometry",boundGeometry);
    if(peer!=null)n.putLong("peer",peer.asLong());if(leader!=null)n.putLong("leader",leader.asLong());if(peerIdentity!=null)n.putUUID("peer_identity",peerIdentity);
    n.putDouble("ramp_rpm",outputRpm);n.putString("fault",fault);
  }
  @Override protected void read(CompoundTag n,HolderLookup.Provider registries,boolean client){
    super.read(n,registries,client);if(n.hasUUID("authority_id"))identity=n.getUUID("authority_id");
    vehicle=n.hasUUID("vehicle")?n.getUUID("vehicle"):null;anchor=n.contains("anchor")?BlockPos.of(n.getLong("anchor")):null;
    var ids=new ArrayList<UUID>();for(Tag id:n.getList("cars",Tag.TAG_INT_ARRAY))ids.add(NbtUtils.loadUUID(id));cars=List.copyOf(ids);
    boundGeometry=n.getLong("bound_geometry");
    peer=n.contains("peer")?BlockPos.of(n.getLong("peer")):null;leader=n.contains("leader")?BlockPos.of(n.getLong("leader")):null;peerIdentity=n.hasUUID("peer_identity")?n.getUUID("peer_identity"):null;
    outputRpm=client?n.getDouble("ramp_rpm"):0;leaseTick=Long.MIN_VALUE;fault=client?n.getString("fault"):"Restored; explicitly Stop then Dispatch/Resume";
  }
}
