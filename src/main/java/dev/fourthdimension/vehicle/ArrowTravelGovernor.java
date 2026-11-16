package dev.fourthdimension.vehicle;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.physics.ArrowTransmission;
import dev.fourthdimension.physics.RotationServo;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.track.TrackGeometry;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

/** Passive transmission load: slow translation before a continuous program exhausts spring travel. */
public final class ArrowTravelGovernor {
  private ArrowTravelGovernor() {}
  private record Frame(FoundationData.Vehicle car, Vector3d tangent, double speed) {}
  public static long reductions;

  public static void tick(ServerLevel level, FoundationData data, double dt) {
    var container=SubLevelContainer.getContainer(level);
    var pipeline=container.physicsSystem().getPipeline();
    var visited=new HashSet<UUID>();
    for(var vehicle:data.vehicles.values()) {
      if(vehicle.family!=Family.ARROW||FoundationData.VARIANT.equals(vehicle.variant)
          ||vehicle.stopping||vehicle.maintenance||!visited.add(vehicle.chassis))continue;
      List<FoundationData.Vehicle> cars;
      try { cars=VehicleOperations.consist(level,vehicle); }
      catch(IllegalArgumentException unavailable) { continue; }
      var frames=new ArrayList<Frame>();double limit=Double.POSITIVE_INFINITY;
      for(var car:cars) {
        visited.add(car.chassis);
        if(car.stopping||!car.fault.isEmpty())continue;
        var body=container.getSubLevel(car.chassis);
        var rail=data.rails.stream().filter(r->r.matches(car.boundFrom,car.boundTo)).findFirst().orElse(null);
        if(!(body instanceof ServerSubLevel parent)||rail==null||!rail.valid)continue;
        var edge=TrackGeometry.resolve(level,rail);var center=CoasterCartResolve.cartCenterWorld(parent);
        if(edge==null||center==null)continue;
        var hit=CoasterPathTrackFrame.closestHitOnEdge(level,edge,new Vec3(center.x,center.y,center.z));
        var basis=CoasterPathTrackFrame.trackBasisAtHit(level,hit);
        var tangent=new Vector3d(basis.forward().x,basis.forward().y,basis.forward().z);
        double signed=pipeline.getLinearVelocity(parent,new Vector3d()).dot(tangent);
        frames.add(new Frame(car,tangent,signed));
        double at=TrackGeometry.distance(level,edge,rail,hit.t());
        double direction=Math.signum(signed)*(edge.from().equals(rail.from)?1:-1);
        // Include the braking approach to a Hermite ramp, whose slope starts at zero.
        double look=Math.max(4,Math.min(24,signed*signed/12+Math.abs(signed)*.5));
        double slope=0;
        for(double ahead=0;ahead<=look;ahead+=.5)
          slope=Math.max(slope,Math.abs(rail.profile.slopeAt(at+direction*ahead)));
        if(slope<1e-6)continue;
        double allowance=RotationServo.MAX_SPEED*.75;
        double currentSlope=rail.profile.slopeAt(at)*direction;
        for(var carrier:car.carriers) {
          double lag=rail.profile.angleAt(at)-carrier.angle;
          // Travel that unwinds the spring receives no lag penalty.
          if(lag*currentSlope>0)
            allowance*=Math.clamp((ArrowTransmission.MAX_AXLE_ERROR-.4-Math.abs(lag))/1.5,0,1);
        }
        limit=Math.min(limit,allowance/slope);
      }
      if(!Double.isFinite(limit))continue;
      // One equal tangent reduction per body in the complete consist. Never add motion,
      // rotate a carrier, rewrite its program, or change the provider's authority.
      for(var frame:frames) {
        double reduction=Math.min(Math.max(0,Math.abs(frame.speed)-limit),24*dt);
        if(reduction<=0)continue;
        var delta=new Vector3d(frame.tangent).mul(-Math.copySign(reduction,frame.speed));
        var ids=new ArrayList<UUID>();ids.add(frame.car.chassis);
        frame.car.carriers.forEach(c->ids.add(c.body));
        for(var id:ids)if(container.getSubLevel(id) instanceof ServerSubLevel body)
          pipeline.addLinearAndAngularVelocity(body,new Vector3d(delta),new Vector3d());
        reductions++;
      }
    }
  }
}
