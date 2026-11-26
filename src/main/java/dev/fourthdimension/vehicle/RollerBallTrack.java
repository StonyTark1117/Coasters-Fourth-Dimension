package dev.fourthdimension.vehicle;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** N contacts and asymmetric outboard reservation, including native full roll. */
public final class RollerBallTrack {
  private static final Map<CoasterPathEdge,Boolean> SHAPES=new WeakHashMap<>();
  private RollerBallTrack(){}
  public static String rejection(Level level,CoasterPathEdge edge){
    if(CoasterPathGraphManager.edgeHostedOnSubLevel(level,edge))return "Roller Ball requires fixed world track";
    var data=EligibilityHooks.data(level);
    var rail=data==null?null:dev.fourthdimension.moving.MovingRouteContext.rails(level).stream().filter(r->r.matches(edge.from(),edge.to())).findFirst().orElse(null);
    if(rail==null||!rail.valid||rail.family!=Family.ROLLER_BALL||!RollerBallVehicle.VARIANT.equals(rail.variant))
      return "Roller Ball requires matching outboard track with capture and drive capability";
    if(!TrackDesign.ROLLER_BALL.nativeFit().accepts(RollerBallVehicle.FIT))return "Roller Ball contact/mount/envelope mismatch";
    return SHAPES.computeIfAbsent(edge,e->shapeFits(level,e))?null:"Roller Ball needs radius >=8 in one untwisted vertical plane";
  }
  private static boolean shapeFits(Level level,CoasterPathEdge edge){
    Vec3 last=null,tangent=null,up=null;
    for(int i=0;i<=256;i++){
      double t=i/256.0;var point=CoasterPathGraphManager.sampleEdgePosition(level,edge,t);
      var b=CoasterPathTrackFrame.trackBasisAtHit(level,new CoasterPathTrackFrame.GraphHit(point,edge,t));
      if(Math.abs(b.forward().x)>1e-4 || Math.abs(Math.abs(b.right().x)-1)>1e-4)return false;
      if(last!=null){
        double ds=last.distanceTo(point);if(ds<1e-7)return false;
        double bend=Math.acos(Math.clamp(tangent.dot(b.forward()),-1,1));
        if(bend/ds>1.0/8+1e-4)return false;
        // Parallel-transport previous up by the tangent change, then measure only local roll.
        var a=new org.joml.Vector3d(tangent.x,tangent.y,tangent.z);
        var z=new org.joml.Vector3d(b.forward().x,b.forward().y,b.forward().z);
        var q=new org.joml.Quaterniond().rotationTo(a,z);
        var transported=new org.joml.Vector3d(up.x,up.y,up.z).rotate(q);
        if(Math.acos(Math.clamp(transported.dot(b.up().x,b.up().y,b.up().z),-1,1))/ds>1e-4)return false;
      }
      last=point;tangent=b.forward();up=b.up();
    }return true;
  }

  private record Leg(FoundationData.Rail rail, CoasterPathEdge edge, boolean forward) {}

  /** Validate the connected closed ride, rather than treating isolated eligible curves as a ride. */
  public static long validateRoute(Level level, FoundationData.Vehicle vehicle) {
    var route = route(level, vehicle);
    int phase = 0;
    var samples = new ArrayList<List<Vec3>>();
    for (int i = 0; i < route.size(); i++) {
      var leg = route.get(i);
      int section = leg.rail.rollerSection.ordinal();
      if (section != phase && section != phase + 1)
        throw new IllegalArgumentException("Roller Ball route order is dock, lift, summit, free descent, return");
      phase = section;
      var reason = rejection(level, leg.edge);
      if (reason != null) throw new IllegalArgumentException(reason);
      if (TrackGeometry.fingerprint(level, leg.edge) != leg.rail.geometryHash)
        throw new IllegalArgumentException("Roller Ball route geometry changed; repair its binding");
      var next = route.get((i + 1) % route.size());
      var end = basis(level, leg, false).forward().scale(leg.forward ? 1 : -1);
      var start = basis(level, next, true).forward().scale(next.forward ? 1 : -1);
      if (end.dot(start) < .9999)
        throw new IllegalArgumentException("Roller Ball route junction must be tangent continuous");
      var table = TrackGeometry.table(level, leg.edge);
      var points = new ArrayList<Vec3>();
      int count = Math.max(1, (int) Math.ceil(table.length() / .5));
      for (int n = 0; n <= count; n++) {
        double t = table.parameterAt(table.length() * n / count);
        points.add(CoasterPathGraphManager.sampleEdgePosition(level, leg.edge, t));
      }
      samples.add(points);
    }
    if (phase != RollerBallState.Section.RETURN.ordinal())
      throw new IllegalArgumentException("Roller Ball needs lift, summit, free descent and return before its dock");
    for (int i = 0; i < route.size(); i++) for (int j = i + 2; j < route.size(); j++) {
      if (i == 0 && j == route.size() - 1) continue;
      for (var a : samples.get(i)) for (var b : samples.get(j))
        if (a.distanceToSqr(b) < 49.06)
          throw new IllegalArgumentException("Keep non-adjacent Roller Ball centerlines seven blocks apart");
    }
    return stamp(route);
  }

  public static long routeStamp(Level level, FoundationData.Vehicle vehicle) {
    return stamp(route(level, vehicle));
  }

  private static long stamp(List<Leg> route) {
    long hash = 0xcbf29ce484222325L;
    for (var leg : route) for (long value : new long[] {leg.rail.from.asLong(), leg.rail.to.asLong(),
        leg.rail.geometryHash, leg.rail.revision, leg.rail.rollerSection.ordinal()})
      hash = (hash ^ value) * 0x100000001b3L;
    return hash;
  }

  /** Explicit repair can revalidate adjacent legs invalidated by native graph propagation.
   * Every leg must still match its original stored fingerprint; never rebind changed geometry. */
  public static List<FoundationData.Rail> repairedRoute(Level level, FoundationData.Vehicle vehicle) {
    var legs=route(level,vehicle,false);
    for(var leg:legs)if(TrackGeometry.fingerprint(level,leg.edge)!=leg.rail.geometryHash)
      throw new IllegalArgumentException("Restored Roller Ball route geometry changed; restore every original section");
    return legs.stream().map(Leg::rail).toList();
  }

  private static List<Leg> route(Level level, FoundationData.Vehicle vehicle) {
    return route(level,vehicle,true);
  }

  private static List<Leg> route(Level level, FoundationData.Vehicle vehicle, boolean requireValid) {
    var data = EligibilityHooks.data(level);
    var state = vehicle.roller;
    if (data == null || state.dockFrom == null && dev.fourthdimension.moving.MovingRouteContext.start(level, vehicle) == null)
      throw new IllegalArgumentException("Bind a Roller Ball dock before validating its route");
    var dock = dev.fourthdimension.moving.MovingRouteContext.start(level, vehicle);
    if (dock == null) dock = dev.fourthdimension.moving.MovingRouteContext.rail(level,state.dockFrom,state.dockTo);
    if (dock == null || dock.rollerSection != RollerBallState.Section.DOCK)
      throw new IllegalArgumentException("Roller Ball dock section is unavailable");
    var result = walk(level, data, dock, dock.from, requireValid);
    if (result.size() > 1 && result.get(1).rail.rollerSection == RollerBallState.Section.RETURN)
      result = walk(level, data, dock, dock.to, requireValid);
    return result;
  }

  private static List<Leg> walk(Level level, FoundationData data, FoundationData.Rail dock, BlockPos start, boolean requireValid) {
    var result = new ArrayList<Leg>();
    var seen = new HashSet<FoundationData.Rail>();
    var rail = dock;
    var at = start;
    while (seen.add(rail)) {
      if (requireValid && !rail.valid || rail.family != Family.ROLLER_BALL || !RollerBallVehicle.VARIANT.equals(rail.variant))
        throw new IllegalArgumentException("Roller Ball closed route has incompatible or invalid support");
      var edge = TrackGeometry.resolve(level, rail);
      if (edge == null) throw new IllegalArgumentException("Load and restore the complete Roller Ball route");
      result.add(new Leg(rail, edge, edge.from().equals(at)));
      var end = rail.from.equals(at) ? rail.to : rail.from;
      var current = rail;
      var following = dev.fourthdimension.moving.MovingRouteContext.rails(level).stream().filter(r -> r != current && (r.from.equals(end) || r.to.equals(end))).toList();
      if (following.size() != 1) throw new IllegalArgumentException("Roller Ball requires one closed unbranched route");
      rail = following.getFirst();
      at = end;
    }
    if (rail != dock || !at.equals(start)) throw new IllegalArgumentException("Roller Ball route must return to its own dock");
    return result;
  }

  private static CoasterPathTrackFrame.TrackBasis basis(Level level, Leg leg, boolean start) {
    double t = start == leg.forward ? 0 : 1;
    return CoasterPathTrackFrame.trackBasisAtHit(level, new CoasterPathTrackFrame.GraphHit(
        CoasterPathGraphManager.sampleEdgePosition(level, leg.edge, t), leg.edge, t));
  }
  public static BlockPos obstruction(Level level,CoasterPathTrackFrame.GraphHit hit){
    var b=CoasterPathTrackFrame.trackBasisAtHit(level,hit);
    for(int sign:new int[]{-1,1}){
      var obstacle=SweptClearance.firstObstruction(level,hit.point().add(b.right().scale(sign*3.25)).add(b.up().scale(2)),
          b.right(),b.up(),b.forward(),1.25,2.5,2.5);
      if(obstacle!=null)return obstacle;
    }
    return SweptClearance.firstObstruction(level,hit.point().add(b.up().scale(2)),b.right(),b.up(),b.forward(),2.25,.125,.125);
  }
}
