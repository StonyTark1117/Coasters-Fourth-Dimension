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
public final class PoweredInvertedYawGovernorEntity extends SplitShaftBlockEntity implements RideAuthority.Controller {
  public UUID vehicle;
  public BlockPos anchor;
  private UUID identity=UUID.randomUUID();
  public BlockPos master;
  private final Map<BlockPos,Integer> drives=new LinkedHashMap<>();
  private final Map<UUID,Long> route=new LinkedHashMap<>();
  private final Map<UUID,Integer> routeDirections=new LinkedHashMap<>();
  private long pulseUntil;
  public String lastEvent="";
  public long eventSequence;
  public boolean sound;
  private List<UUID> cars=List.of();
  private double outputRpm,requestedBps,acceleration=2;
  private long leaseTick=Long.MIN_VALUE,leaseEpoch;
  private RideAuthority.Token token;
  private String fault="Unbound";
  public PoweredInvertedYawGovernorEntity(BlockPos pos,BlockState state){super(PoweredInvertedYawContent.GOVERNOR_ENTITY.get(),pos,state);}
  public Direction output(){return getBlockState().getValue(PoweredInvertedYawGovernorBlock.FACING);}
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
  @Override public boolean authorityAvailable(UUID id){return authorizesRide(id)&&systemAvailability()==null;}
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
  public BlockPos discoverAnchor(){
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
    if(a==null||a.resolveChainLiftSpanPath(0)==null||a.getChainLiftCommittedLength()<8||Math.abs(a.getChainLiftTravelSign())!=1)
      return "Restore a >=8-block committed native Chain span";
    if(Math.abs(inputRpm())<.001||isOverStressed())return "Supply non-overstressed kinetic power to the rear input";
    return null;
  }
  private PoweredInvertedYawGovernorEntity at(BlockPos p){return level!=null&&p!=null&&level.hasChunkAt(p)&&level.getBlockEntity(p) instanceof PoweredInvertedYawGovernorEntity g?g:null;}
  public String systemAvailability(){
    if(master!=null&&!master.equals(worldPosition))return "Use the bound master governor";
    String local=availability();if(local!=null)return local;
    if(drives.isEmpty()||route.isEmpty())return "Bind a completely governed powered circuit";
    var data=FoundationData.get((ServerLevel)level);
    for(var e:route.entrySet()){
      var r=data.rails.stream().filter(a->a.id.equals(e.getKey())).findFirst().orElse(null);
      if(r==null||!r.valid||r.geometryHash!=e.getValue())return "Powered route changed; retained hold";
    }
    for(var pos:drives.keySet()){var g=at(pos);if(g==null||!worldPosition.equals(g.master)||!Objects.equals(vehicle,g.vehicle))return "Route governor missing or rebound";String reason=g.availability();if(reason!=null)return reason;}
    return null;
  }
  public static List<FoundationData.Rail> route(ServerLevel l,FoundationData.Vehicle v){
    var first=PoweredInvertedYawRuntime.rail(l,v);if(first==null)throw new IllegalArgumentException("Matching powered route unavailable");
    var all=FoundationData.get(l).rails;var result=new ArrayList<FoundationData.Rail>();var todo=new ArrayDeque<FoundationData.Rail>();todo.add(first);
    while(!todo.isEmpty()){var r=todo.removeFirst();if(result.contains(r))continue;if(result.size()>=64)throw new IllegalArgumentException("Powered circuit exceeds64 sections");result.add(r);
      for(var endpoint:List.of(r.from,r.to)){var adjacent=all.stream().filter(a->a.from.equals(endpoint)||a.to.equals(endpoint)).toList();if(adjacent.size()!=2)throw new IllegalArgumentException("Use one unbranched complete powered circuit");
        for(var next:adjacent){if(next.family!=v.family||!next.variant.equals(v.variant))throw new IllegalArgumentException("Mixed powered circuit");if(!result.contains(next))todo.add(next);}}
    }return List.copyOf(result);
  }
  public void bind(FoundationData.Vehicle selected){
    if(!(level instanceof ServerLevel server))throw new IllegalArgumentException("Server binding required");
    var bound=VehicleOperations.consist(server,selected);
    if(bound.stream().anyMatch(v->!PoweredInvertedYawVehicle.is(v)||!v.stopping||!v.carriers.getFirst().occupants.isEmpty()))throw new IllegalArgumentException("Bind an empty stopped matching train");
    if(vehicle!=null&&!new HashSet<>(cars).equals(new HashSet<>(bound.stream().map(v->v.chassis).toList())))throw new IllegalArgumentException("Governor already owns another consist");
    if(selected.authority.owner!=null&&!identity.equals(selected.authority.owner))throw new IllegalArgumentException("Another controller owns this ride");
    var circuit=route(server,selected);var canonical=new LinkedHashMap<UUID,Integer>();var first=circuit.getFirst();var current=first;var entry=first.from;
    do{int sign=current.from.equals(entry)?1:-1;canonical.put(current.id,sign);var exit=sign==1?current.to:current.from;var previous=current;
      current=circuit.stream().filter(r->r!=previous&&(r.from.equals(exit)||r.to.equals(exit))).findFirst().orElseThrow();entry=exit;
    }while(current!=first&&canonical.size()<=64);
    if(canonical.size()!=circuit.size()||!entry.equals(first.from))throw new IllegalArgumentException("Powered route is not one complete circuit");
    var discovered=new LinkedHashMap<BlockPos,Integer>();var fingerprints=new LinkedHashMap<UUID,Long>();
    for(var r:circuit){
      var edge=dev.fourthdimension.track.TrackGeometry.resolve(server,r);if(edge==null||PoweredInvertedYawTrack.rejection(server,edge)!=null)throw new IllegalArgumentException("Invalid powered circuit geometry");
      var point=CoasterPathGraphManager.sampleEdgePosition(server,edge,.5);var hit=new CoasterPathTrackFrame.GraphHit(point,edge,.5);
      var chain=ChainLiftSpanQuery.findAtGraphHit(server,hit,0);if(chain==null)throw new IllegalArgumentException("Every moving section needs native Chain coverage");
      var a=chain.anchor();var path=a.resolveChainLiftSpanPath(0);var firstHit=new CoasterPathTrackFrame.GraphHit(CoasterPathGraphManager.sampleEdgePosition(server,edge,.01),edge,.01);var lastHit=new CoasterPathTrackFrame.GraphHit(CoasterPathGraphManager.sampleEdgePosition(server,edge,.99),edge,.99);
      if(path==null||!ChainLiftSpanQuery.graphHitOnSpan(path,firstHit)||!ChainLiftSpanQuery.graphHitOnSpan(path,lastHit))throw new IllegalArgumentException("Complete the section's native Chain span");
      PoweredInvertedYawGovernorEntity governor=null;
      for(var direction:Direction.values())if(direction.getAxis()==a.getChainLiftShaftAxis())for(int n=1;n<=16;n++){
        var candidate=at(a.getBlockPos().relative(direction,n));if(candidate!=null&&a.getBlockPos().equals(candidate.discoverAnchor())){if(governor!=null)throw new IllegalArgumentException("Competing governors at a Chain anchor");governor=candidate;}}
      if(governor==null)throw new IllegalArgumentException("Supply each Chain anchor through a powered yaw governor");
      if(governor.master!=null&&!governor.master.equals(worldPosition))throw new IllegalArgumentException("A route governor belongs to another controller");
      int sign=a.getBlockPos().equals(r.from)?1:a.getBlockPos().equals(r.to)?-1:0;
      if(sign==0||discovered.put(governor.worldPosition,sign*canonical.get(r.id))!=null)throw new IllegalArgumentException("Use one governor and endpoint-anchored Chain span per section");
      fingerprints.put(r.id,r.geometryHash);
    }
    if(!discovered.containsKey(worldPosition))throw new IllegalArgumentException("Master governor must drive this circuit");
    // Validate supplied energy before publishing any ownership binding.
    for(var pos:discovered.keySet()){var g=at(pos);g.anchor=g.discoverAnchor();var reason=g.availability();if(reason!=null||Math.abs(g.inputRpm())<128)throw new IllegalArgumentException(reason==null?"Supply128 RPM to every governor":reason);}
    vehicle=selected.chassis;cars=bound.stream().map(v->v.chassis).toList();drives.clear();drives.putAll(discovered);route.clear();route.putAll(fingerprints);routeDirections.clear();routeDirections.putAll(canonical);master=worldPosition;
    for(var pos:drives.keySet()){var g=at(pos);g.master=worldPosition;g.vehicle=vehicle;g.cars=cars;g.setChanged();g.sendData();}
    if(selected.authority.owner==null)token=RideAuthority.claim(server,worldPosition,vehicle,RideAuthority.NATIVE_CHAIN);
    else if(!identity.equals(selected.authority.owner))throw new IllegalArgumentException("Another controller owns this ride");else token=RideAuthority.token(selected);
    for(var v:bound){v.poweredYaw.controller=worldPosition.immutable();v.poweredYaw.consist=cars;}
    fault="Held";FoundationData.get(server).setDirty();setChanged();sendData();
  }
  @Override public void validateRelease(ServerLevel server,FoundationData.Vehicle selected){
    var reason=systemAvailability();if(reason!=null)throw new IllegalArgumentException(reason);
    for(var pos:drives.keySet())if(Math.abs(at(pos).inputRpm())<128)throw new IllegalArgumentException("Supply at least128 RPM to every powered governor");
    for(var v:VehicleOperations.consist(server,selected))if(!authorizesRide(v.chassis)||!v.restraints)throw new IllegalArgumentException("Retain the complete bound train");
  }
  public int railDirection(FoundationData.Rail rail){return routeDirections.getOrDefault(rail.id,0);}
  public Set<UUID> routeIds(){return Set.copyOf(route.keySet());}
  public void acknowledge(FoundationData.Vehicle selected){
    var server=(ServerLevel)level;
    RideAuthority.holds(server,selected);
    token=RideAuthority.token(selected);
    if(selected.authority.suspended)token=RideAuthority.resume(server,worldPosition,selected.chassis,token);
    else if(!selected.authority.held)token=RideAuthority.hold(server,worldPosition,selected.chassis,token);
    for(var pos:drives.keySet()){var g=at(pos);if(g!=null){g.setOutput(0);g.leaseTick=Long.MIN_VALUE;}}fault="Held";
  }
  public void release(FoundationData.Vehicle selected){
    var server=(ServerLevel)level;
    if(token==null)throw new IllegalArgumentException("Explicitly Capture/revalidate the governor before release");
    token=RideAuthority.release(server,worldPosition,selected.chassis,token);
    leaseEpoch=selected.authority.epoch;
    for(var v:VehicleOperations.consist(server,selected))if(PoweredInvertedYawVehicle.is(v))v.poweredYaw.epoch=leaseEpoch;
    fault="Running";setChanged();
  }
  public void command(UUID chassis,long epoch,double signedBps,double maxAcceleration){
    if(!(level instanceof ServerLevel server)||!server.getServer().isSameThread()
        ||!authorizesRide(chassis)||!RideAuthority.permits(server,chassis,identity,epoch,RideAuthority.NATIVE_CHAIN))
      throw new IllegalArgumentException("Stale or held governor authority");
    if(!Double.isFinite(signedBps)||Math.abs(signedBps)>8||!Double.isFinite(maxAcceleration)||maxAcceleration<=0||maxAcceleration>2)
      throw new IllegalArgumentException("Governor speed <=8 b/s and acceleration <=2 b/s²");
    String reason=systemAvailability();if(reason!=null)throw new IllegalArgumentException(reason);
    for(var e:drives.entrySet()){var g=at(e.getKey());if(g.outputRpm* -signedBps*e.getValue()*g.anchorEntity().getChainLiftTravelSign()<-.01)throw new IllegalArgumentException("Stop and authorize reversal first");g.requestedBps=signedBps*e.getValue();g.acceleration=maxAcceleration;g.leaseEpoch=epoch;g.leaseTick=server.getGameTime();}

  }
  @Override public void tick(){
    super.tick();if(!(level instanceof ServerLevel server))return;
    if(pulseUntil>0&&server.getGameTime()>=pulseUntil){pulseUntil=0;server.updateNeighborsAt(worldPosition,getBlockState().getBlock());}
    var reason=availability();
    boolean permitted=vehicle!=null&&reason==null&&leaseTick>=0&&server.getGameTime()-leaseTick<=1
        &&at(master)!=null&&RideAuthority.permits(server,vehicle,at(master).identity,leaseEpoch,RideAuthority.NATIVE_CHAIN);
    if(!permitted){if(outputRpm!=0)setOutput(0);if(reason!=null)fault=reason;return;}
    double target=-requestedBps*16*anchorEntity().getChainLiftTravelSign();
    double maximum=Math.min(128,Math.abs(inputRpm()));target=Math.clamp(target,-maximum,maximum);
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
  public void emit(String kind,UUID car,UUID event,long epoch,long sequence){
    if(!(level instanceof ServerLevel server)||!worldPosition.equals(master))return;
    lastEvent=kind+" ride="+vehicle+" car="+car+" event="+event+" tick="+server.getGameTime()+" epoch="+epoch+" sequence="+(eventSequence+1)+" car_sequence="+sequence;
    eventSequence++;pulseUntil=server.getGameTime()+4;server.updateNeighborsAt(worldPosition,getBlockState().getBlock());
    if(sound)server.playSound(null,worldPosition,net.minecraft.sounds.SoundEvents.NOTE_BLOCK_CHIME.value(),net.minecraft.sounds.SoundSource.BLOCKS,.5f,1);
    if(Boolean.getBoolean("fourthdimension.validation"))com.mojang.logging.LogUtils.getLogger().info("FD_POWERED_EVENT {}",lastEvent);setChanged();sendData();
  }
  public boolean pulsing(){return level!=null&&pulseUntil>level.getGameTime();}
  @Override protected void write(CompoundTag n,HolderLookup.Provider registries,boolean client){
    super.write(n,registries,client);n.putUUID("authority_id",identity);
    if(vehicle!=null)n.putUUID("vehicle",vehicle);if(anchor!=null)n.putLong("anchor",anchor.asLong());
    var ids=new ListTag();for(var id:cars)ids.add(NbtUtils.createUUID(id));n.put("cars",ids);
    n.putDouble("ramp_rpm",outputRpm);n.putString("fault",fault);
    if(master!=null)n.putLong("master",master.asLong());var list=new ListTag();drives.forEach((pos,sign)->{var d=new CompoundTag();d.putLong("pos",pos.asLong());d.putInt("sign",sign);list.add(d);});n.put("drives",list);
    var routes=new ListTag();route.forEach((id,hash)->{var d=new CompoundTag();d.putUUID("id",id);d.putLong("hash",hash);d.putInt("direction",routeDirections.getOrDefault(id,0));routes.add(d);});n.put("route",routes);n.putString("last_event",lastEvent);n.putLong("event_sequence",eventSequence);n.putBoolean("sound",sound);

  }
  @Override protected void read(CompoundTag n,HolderLookup.Provider registries,boolean client){
    super.read(n,registries,client);if(n.hasUUID("authority_id"))identity=n.getUUID("authority_id");
    vehicle=n.hasUUID("vehicle")?n.getUUID("vehicle"):null;anchor=n.contains("anchor")?BlockPos.of(n.getLong("anchor")):null;
    var ids=new ArrayList<UUID>();for(Tag id:n.getList("cars",Tag.TAG_INT_ARRAY))ids.add(NbtUtils.loadUUID(id));cars=List.copyOf(ids);
    master=n.contains("master")?BlockPos.of(n.getLong("master")):null;drives.clear();for(var raw:n.getList("drives",Tag.TAG_COMPOUND)){var d=(CompoundTag)raw;drives.put(BlockPos.of(d.getLong("pos")),d.getInt("sign"));}route.clear();routeDirections.clear();for(var raw:n.getList("route",Tag.TAG_COMPOUND)){var d=(CompoundTag)raw;route.put(d.getUUID("id"),d.getLong("hash"));routeDirections.put(d.getUUID("id"),d.getInt("direction"));}lastEvent=n.getString("last_event");eventSequence=n.getLong("event_sequence");sound=n.getBoolean("sound");pulseUntil=0;
    outputRpm=client?n.getDouble("ramp_rpm"):0;leaseTick=Long.MIN_VALUE;fault=client?n.getString("fault"):"Restored; explicitly Capture then Dispatch/Resume";
  }
}
