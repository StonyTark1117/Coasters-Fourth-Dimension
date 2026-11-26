package dev.fourthdimension.gametest;

import dev.fourthdimension.moving.*;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;

/** Real authored fixed routes plus an actual hosted insert; prospective checks do not move a train. */
@GameTestHolder("fourthdimension_fd2001_routes")
@PrefixGameTestTemplate(false)
public final class MovingRouteGameTests {
  @GameTest(template="empty",timeoutTicks=200)
  public static void ngFlyingCandidateKeepsOwnedTargets(GameTestHelper h){
    var l=h.getLevel();var o=new BlockPos(-500,150,-500);force(l,o,12,26);
    var example=NewGenerationFlyingExamples.create(l,o,UUID.randomUUID());
    var car=example.vehicle();var gap=example.edges().getFirst();
    var s=prepare(h,car,gap,48,new BlockPos(-1100,150,-500));
    h.runAtTickTime(50,()->{
      removeGap(l,gap);var data=FoundationData.get(l);var before=List.copyOf(data.rails);
      MovingRouteAdapters.validateExit(l,s,car,s.docks.get("candidate"));
      var target=before.stream().flatMap(r->r.yaw.intervals.stream()).filter(i->i.mode()==YawState.Mode.TARGET).findFirst().orElseThrow();
      var marker=(YawMarkerEntity)l.getBlockEntity(target.markerPos());var owner=marker.owner;
      marker.owner=UUID.randomUUID();expect(h,"owned yaw target",()->MovingRouteAdapters.validateExit(l,s,car,s.docks.get("candidate")));marker.owner=owner;
      MovingRouteAdapters.validateExit(l,s,car,s.docks.get("candidate"));
      h.assertTrue(data.rails.equals(before),"Candidate validation mutated original route records");
      h.assertTrue(MovingRouteContext.start(l,car)==null,"Candidate route scope leaked after refusal");
      System.out.println("FD2001_ROUTE_PASS NG_FLYING fixedRails="+before.size()+" markerOwnershipRefusal=true scopedOnly=true");h.succeed();
    });
  }
  public static void rollerCandidateKeepsSectionOrder(GameTestHelper h){
    var l=h.getLevel();var o=new BlockPos(500,100,-500);force(l,o,3,15);
    var example=RollerBallExamples.create(l,o,true);var car=example.car();var gap=example.edges().getFirst();
    var s=prepare(h,car,gap,32,new BlockPos(1100,150,-500));
    h.runAtTickTime(50,()->{
      removeGap(l,gap);var data=FoundationData.get(l);var before=List.copyOf(data.rails);
      MovingRouteAdapters.validateExit(l,s,car,s.docks.get("candidate"));
      var summit=before.stream().filter(r->r.family==car.family&&r.rollerSection==RollerBallState.Section.SUMMIT).findFirst().orElseThrow();
      var mode=summit.rollerSection;summit.rollerSection=RollerBallState.Section.RETURN;
      expect(h,"route order",()->MovingRouteAdapters.validateExit(l,s,car,s.docks.get("candidate")));summit.rollerSection=mode;
      MovingRouteAdapters.validateExit(l,s,car,s.docks.get("candidate"));
      h.assertTrue(data.rails.equals(before),"Candidate validation mutated original route records");
      h.assertTrue(MovingRouteContext.start(l,car)==null,"Candidate route scope leaked after refusal");
      System.out.println("FD2001_ROUTE_PASS ROLLER_BALL orderedFixedRoute=true sectionRefusal=true scopedOnly=true");h.succeed();
    });
  }
  static MovingAssemblyState prepare(GameTestHelper h,FoundationData.Vehicle car,CoasterPathEdge gap,int length,BlockPos remote){
    var l=h.getLevel();force(l,remote,3,3);
    diagnose(l,gap,"beforeSocket");
    var entry=MovingCapabilityCatalog.find(car.family,car.variant).orElseThrow();
    var s=new MovingAssemblyState(UUID.randomUUID(),remote.west(12),"transfer",car.variant,length);s.configuration=entry.configuration().id();
    var a=gap.bezier().starts.getFirst();var b=gap.bezier().starts.getSecond();
    double yaw=Math.atan2(b.x-a.x,b.z-a.z);var q=new org.joml.Quaterniond().rotationY(yaw);
    var pose=new MovingAssemblyState.Pose((a.x+b.x)/2,(a.y+b.y)/2+.25,(a.z+b.z)/2,q.x,q.y,q.z,q.w);
    var dock=new MovingAssemblyState.Dock("candidate",UUID.randomUUID(),gap.from(),gap.to(),pose);
    s.docks.put("candidate",dock);s.source="candidate";s.destination="candidate";
    var parts=new ArrayList<MovingNativeAdapter.BlockPlacement>();
    for(int z=-length/2;z<=length/2;z++)for(int x=-1;x<=1;x++)parts.add(new MovingNativeAdapter.BlockPlacement(new BlockPos(x,-1,z),Blocks.IRON_BLOCK.defaultBlockState()));
    // Remote bed intentionally stays away from fixed geometry. Only the prospective view is tested.
    MovingNativeAdapter.createBed(l,s,new MovingAssemblyState.Pose(remote.getX()+.5,remote.getY()+.5,remote.getZ()+.5,0,0,0,1),parts);
    FoundationData.get(l).moving.assemblies.put(s.id,s);diagnose(l,gap,"afterSocket");return s;
  }
  static void removeGap(ServerLevel l,CoasterPathEdge gap){
    diagnose(l,gap,"beforeRemove");
    ((CoasterAnchorpointBlockEntity)l.getBlockEntity(gap.from())).removeAnchorPeerCurve(l,gap.to());
    FoundationData.get(l).rails.removeIf(r->r.matches(gap.from(),gap.to()));TrackGeometry.invalidate(l);
    // This is newly authored fixture destination geometry, not a repair of an accepted ride.
    // Native gap cutting reoptimizes adjacent handles. Bind the finished geometry and scale its
    // original named programs to the freshly measured lengths before testing production refusal.
    for(var rail:FoundationData.get(l).rails)if(rail.from.equals(gap.from())||rail.to.equals(gap.from())||rail.from.equals(gap.to())||rail.to.equals(gap.to())){
      var edge=TrackGeometry.resolve(l,rail);if(edge==null)throw new IllegalArgumentException("New destination fixed edge missing");
      long actual=TrackGeometry.fingerprint(l,edge);double length=TrackGeometry.table(l,edge).length();
      double scale=length/rail.profile.keys().getLast().distance();
      rail.profile=scaled(rail.profile,scale);rail.geometryHash=actual;rail.valid=true;
      rail.yaw.geometry=actual;
      rail.yaw.intervals=rail.yaw.intervals.stream().map(i->new YawSection.Interval(i.id(),i.name(),i.start()*scale,i.end()*scale,i.mode(),i.blend()*scale,i.phiFrom(),i.phiTo(),scaled(i.profile(),scale),i.marker(),i.markerPos(),i.dimension(),i.winding(),i.speed(),i.acceleration(),i.stopName(),i.showDuration())).toList();
      TrackPlacement.captureOriginalCurve(rail,edge.bezier());
      System.out.println("FD2001_ROUTE_AUTHORED endpoints="+rail.from+"/"+rail.to+" measured="+actual+" length="+length);
    }
  }
  static dev.fourthdimension.physics.RotationProfile scaled(dev.fourthdimension.physics.RotationProfile p,double scale){return new dev.fourthdimension.physics.RotationProfile(p.keys().stream().map(k->new dev.fourthdimension.physics.RotationProfile.Key(k.distance()*scale,k.angle(),k.slope()/scale)).toList());}

  static void diagnose(ServerLevel l,CoasterPathEdge gap,String phase){
    for(var rail:FoundationData.get(l).rails)if(rail.from.equals(gap.from())||rail.to.equals(gap.from())||rail.from.equals(gap.to())||rail.to.equals(gap.to())){
      var edge=TrackGeometry.resolve(l,rail);if(edge==null)continue;
      var old=new com.simibubi.create.content.trains.track.BezierConnection(rail.originalCurve.copy(),rail.from);
      var original=CoasterPathEdge.curve(old.bePositions.getFirst(),old.bePositions.getSecond(),old);
      System.out.println("FD2001_ROUTE_GEOMETRY phase="+phase+" endpoints="+rail.from+"/"+rail.to+" saved="+rail.geometryHash+" resolved="+TrackGeometry.fingerprint(l,edge)+" originalNow="+TrackGeometry.fingerprint(l,original)+" curve="+edge.bezier().write(rail.from));
    }
  }
  static void force(ServerLevel l,BlockPos o,int xRange,int zRange){for(int x=(o.getX()>>4)-xRange;x<=(o.getX()>>4)+xRange;x++)for(int z=(o.getZ()>>4)-zRange;z<=(o.getZ()>>4)+zRange;z++)l.setChunkForced(x,z,true);}
  static void expect(GameTestHelper h,String text,Runnable r){try{r.run();throw new IllegalStateException("Expected route refusal containing "+text);}catch(IllegalArgumentException e){h.assertTrue(e.getMessage().toLowerCase(java.util.Locale.ROOT).contains(text),"Wrong route refusal: "+e.getMessage());}}
}
