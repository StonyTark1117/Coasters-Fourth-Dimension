package dev.fourthdimension.vehicle;
import dev.fourthdimension.integration.RideAuthority;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackGeometry;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
public final class DiveHoldEntity extends BlockEntity implements RideAuthority.Controller {
  private UUID identity=UUID.randomUUID();
  public BlockPos from,to;public long geometry;public int dwell=60;
  public DiveHoldEntity(BlockPos p,BlockState s){super(DiveContent.HOLD_ENTITY.get(),p,s);}
  public UUID authorityId(){return identity;}
  public boolean authorizesRide(UUID id){
    if(!(level instanceof ServerLevel server))return false;var v=FoundationData.get(server).vehicles.get(id);
    return v!=null&&DiveVehicle.is(v)&&worldPosition.equals(v.dive.controller);
  }
  public boolean authorityAvailable(UUID id){return !isRemoved()&&authorizesRide(id)&&powered();}
  public boolean powered(){return level!=null&&level.hasNeighborSignal(worldPosition);}
  public CoasterPathEdge edge(){
    if(!(level instanceof ServerLevel server)||from==null)return null;
    var r=FoundationData.get(server).rails.stream().filter(x->x.matches(from,to)).findFirst().orElse(null);
    return r!=null&&r.valid&&r.geometryHash==geometry?TrackGeometry.resolve(server,r):null;
  }
  public void bind(){
    var server=(ServerLevel)level;CoasterPathEdge selected=null;double best=144;
    for(var r:FoundationData.get(server).rails){
      var e=TrackGeometry.resolve(server,r);if(e==null||DiveTrack.rejection(server,e)!=null)continue;
      var h=CoasterPathTrackFrame.closestHitOnEdge(server,e,Vec3.atCenterOf(worldPosition));
      double d=h.point().distanceToSqr(Vec3.atCenterOf(worldPosition));
      if(d<best&&holdShape(server,e)){selected=e;best=d;}
    }
    if(selected==null)throw new IllegalArgumentException("Place within twelve blocks of a >=28-block Dive hold section aimed 60 degrees down");
    from=selected.from();to=selected.to();geometry=TrackGeometry.fingerprint(server,selected);setChanged();
  }
  public static boolean holdShape(ServerLevel l,CoasterPathEdge e){
    var table=TrackGeometry.table(l,e);if(table.length()<28)return false;
    for(double d:new double[]{4,12,20,24}){
      double t=table.parameterAt(d);var b=CoasterPathTrackFrame.trackBasisAtHit(l,new CoasterPathTrackFrame.GraphHit(CoasterPathGraphManager.sampleEdgePosition(l,e,t),e,t));
      if(Math.abs(b.forward().y+Math.sin(Math.toRadians(60)))>.035)return false;
    }return true;
  }
  public void changeDwell(){
    var server=(ServerLevel)level;
    var connected=new HashSet<BlockPos>();var pending=new ArrayDeque<BlockPos>();if(from!=null)pending.add(from);
    while(!pending.isEmpty()){var p=pending.remove();if(!connected.add(p))continue;
      for(var r:FoundationData.get(server).rails)if(r.family==dev.fourthdimension.contract.Family.DIVE&&r.valid){
        if(r.from.equals(p))pending.add(r.to);else if(r.to.equals(p))pending.add(r.from);
      }
    }
    for(var v:FoundationData.get(server).vehicles.values())if(DiveVehicle.is(v)&&(authorizesRide(v.chassis)||connected.contains(v.boundFrom))){
      if(!v.stopping||v.carriers.stream().anyMatch(c->!c.occupants.isEmpty()))throw new IllegalArgumentException("Dwell edits require the bound train empty and stopped");
      if(v.dive.phase==DiveState.Phase.HELD||v.dive.phase==DiveState.Phase.RELEASE_ARMED)throw new IllegalArgumentException("Finish or reset the held cycle before changing dwell");
    }
    dwell=dwell>=200?20:dwell+20;setChanged();
  }
  public void validateRelease(ServerLevel l,FoundationData.Vehicle v){DiveRuntime.validateRelease(l,v,this);}
  public String status(){
    if(from==null)return "Dive hold unbound";
    if(level instanceof ServerLevel server)for(var v:FoundationData.get(server).vehicles.values())if(authorizesRide(v.chassis))
      return "Dive "+v.dive.phase+" / "+v.dive.remaining+" ticks / "+(v.dive.needsResume?"Resume required":powered()?"enabled":"power lost")+(v.fault.isEmpty()?"":" / "+v.fault);
    return "Dive hold "+dwell+" ticks / "+(powered()?"enabled":"supply redstone");
  }
  @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider p){super.saveAdditional(t,p);t.putUUID("identity",identity);t.putInt("dwell",dwell);
    if(from!=null){t.putLong("from",from.asLong());t.putLong("to",to.asLong());t.putLong("geometry",geometry);}}
  @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider p){super.loadAdditional(t,p);if(t.hasUUID("identity"))identity=t.getUUID("identity");dwell=Math.clamp(t.getInt("dwell"),20,200);
    if(t.contains("from")){from=BlockPos.of(t.getLong("from"));to=BlockPos.of(t.getLong("to"));geometry=t.getLong("geometry");}}
}
