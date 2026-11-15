package dev.fourthdimension.client;

import dev.fourthdimension.client.fd917.ProductionMesh;
import dev.fourthdimension.contract.Family;
import dev.fourthdimension.physics.ArrowTransmission;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.silvergold.simulatedcoasters.track.graph.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Quaterniond;
import org.joml.Vector3d;

/** Rail-contact bogie, rack, finite torsion coupling, 4x2 gearbox and actual-axle CV shaft. */
@EventBusSubscriber(modid="fourthdimension",value=Dist.CLIENT)
public final class ArrowMechanismRenderer {
  private ArrowMechanismRenderer() {}
  private record Travel(Vector3d origin,double distance) {}
  private static final java.util.Map<ClientSubLevel,Travel> TRAVEL=new java.util.WeakHashMap<>();
  private static Object lastLevel;
  private static long frames,nanos,samples,engagedSamples,releasedSamples;
  private static double maxContact,maxSpring,minInput=Double.POSITIVE_INFINITY,maxInput=Double.NEGATIVE_INFINITY,
      minActual=Double.POSITIVE_INFINITY,maxActual=Double.NEGATIVE_INFINITY;
  private record Frame(Vector3d origin,Vector3d right,Vector3d up,Vector3d forward) {
    Vector3d at(double x,double y,double z) { return new Vector3d(origin).fma(x,right).fma(y,up).fma(z,forward); }
  }

  @SubscribeEvent
  public static void logout(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
    TRAVEL.clear();lastLevel=null;
  }

  @SubscribeEvent
  public static void render(RenderLevelStageEvent event) {
    if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES)return;
    long started=System.nanoTime();
    var mc=Minecraft.getInstance();
    if(lastLevel!=mc.level){
      TRAVEL.clear();lastLevel=mc.level;frames=nanos=samples=engagedSamples=releasedSamples=0;
      maxContact=maxSpring=0;minInput=minActual=Double.POSITIVE_INFINITY;maxInput=maxActual=Double.NEGATIVE_INFINITY;
    }
    if(mc.level==null)return;
    var data=EligibilityHooks.data(mc.level); var container=SubLevelContainer.getContainer(mc.level);
    if(data==null||container==null)return;
    var camera=event.getCamera().getPosition(); var stack=event.getPoseStack();
    stack.pushPose(); stack.translate(-camera.x,-camera.y,-camera.z);
    var mesh=new ProductionMesh(stack.last());
    for(var vehicle:data.vehicles.values()) {
      if(vehicle.family!=Family.ARROW||FoundationData.VARIANT.equals(vehicle.variant)
          ||!(container.getSubLevel(vehicle.chassis) instanceof ClientSubLevel parent))continue;
      var parentPose=parent.renderPose(); var p=parent.getPlot().getCenterBlock();
      var expected=parentPose.transformPosition(new Vector3d(p.getX()+.5,p.getY()+.1875,p.getZ()+.5));
      var bodyForward=new Vector3d(0,0,1).rotate(parentPose.orientation());
      var old=TRAVEL.get(parent);
      double travel=old!=null&&old.origin.distanceSquared(expected)<4
          ?old.distance+new Vector3d(expected).sub(old.origin).dot(bodyForward):0;
      TRAVEL.put(parent,new Travel(new Vector3d(expected),travel));
      if(expected.distanceSquared(camera.x,camera.y,camera.z)>128*128
          || !event.getFrustum().isVisible(new net.minecraft.world.phys.AABB(
              expected.x-6,expected.y-6,expected.z-6,expected.x+6,expected.y+6,expected.z+6))) {
        continue;
      }
      // Same center station and canonical profile as the server; a single narrow roller contact
      // avoids pretending a rigid fore/aft truck can sample two different control heights.
      var rail=data.rails.stream().filter(r->r.matches(vehicle.boundFrom,vehicle.boundTo)).findFirst().orElse(null);
      if(rail==null)continue;
      var edge=TrackGeometry.resolve(mc.level,rail); if(edge==null)continue;
      var hit=CoasterPathTrackFrame.closestHitOnEdge(mc.level,edge,new Vec3(expected.x,expected.y,expected.z));
      if(hit==null)continue;
      var basis=CoasterPathTrackFrame.trackBasisAtHit(mc.level,hit);
      var right=vec(basis.right()); var up=vec(basis.up());
      var bodyRight=new Vector3d(1,0,0).rotate(parentPose.orientation());
      if(right.dot(bodyRight)<0)right.negate();
      var forward=new Vector3d(right).cross(up).normalize();
      var frame=new Frame(vec(hit.point()),right,up,forward);
      double input=rail.profile.angleAt(TrackGeometry.distance(mc.level,edge,rail,hit.t()));
      double h=TrackDesign.ARROW.controlHeight(input);
      int light=LevelRenderer.getLightColor(mc.level,BlockPos.containing(hit.point()));
      for(var carrier:vehicle.carriers) {
        if(!(container.getSubLevel(carrier.body) instanceof ClientSubLevel child))continue;
        var c=child.getPlot().getCenterBlock(); var childPose=child.renderPose();
        var pivot=childPose.transformPosition(new Vector3d(c.getX()+.5+carrier.childPivotX,
            c.getY()+.5+carrier.childPivotOffset,c.getZ()+.5+carrier.childPivotZ));
        double actual=Family.ARROW.axis().angle(carrier.angle,
            new Quaterniond(parentPose.orientation()).conjugate().mul(childPose.orientation()));
        int side=carrier.side<0?-1:1;
        // A late state packet cannot draw an attached spring beyond its physical travel.
        boolean coupled=!carrier.capture&&carrier.controlCoupled&&ArrowTransmission.withinSpring(input,actual);
        assembly(mesh,frame,side,h,input,actual,travel,coupled,pivot,light);
        // The rail-following guide must visibly bear on the actual chassis too. These
        // articulated stays terminate inside the retained wing-strut web, not a rail-frame
        // approximation of it, so interpolation/curvature cannot detach the support.
        for(double x:new double[]{1.65,2.02}) {
          var mount=parentPose.transformPosition(new Vector3d(p.getX()+.5+side*x,p.getY()+2.5,p.getZ()+.5));
          var guide=frame.at(side*x,x<2?-.10:1.15,.30+ArrowTransmission.RACK_PINION_RADIUS+.16);
          mesh.beam(guide,mount,frame.right,.09,.09,"structural",light);
          mesh.ring(mount,bodyRight,frame.up,.035,.07,.12,"machined",light);
        }
        // The retained baked axle is ahead of the actual hinge, not coaxial with it.
        // A crank on the inboard side of the bucket joins both in the child's real frame.
        // Keep the radial member outside the rider/head cushion; never translate the pivot
        // or use the commanded angle to make this eccentric axle appear connected.
        var crankRoot=childPose.transformPosition(new Vector3d(c.getX()+.5-side*.36,c.getY()+.5,c.getZ()+.5));
        var axleContact=childPose.transformPosition(new Vector3d(c.getX()+.5-side*.36,c.getY()+.5,c.getZ()-5.5/16));
        var childAxis=new Vector3d(1,0,0).rotate(childPose.orientation());
        var childUp=new Vector3d(0,1,0).rotate(childPose.orientation());
        mesh.beam(pivot,crankRoot,childUp,.07,.07,"machined",light);
        mesh.beam(crankRoot,axleContact,childAxis,.07,.07,"structural",light);
        if(Boolean.getBoolean("fourthdimension.validation")) {
          samples++;if(coupled){engagedSamples++;maxSpring=Math.max(maxSpring,Math.abs(input-actual)/8);}else releasedSamples++;
          minInput=Math.min(minInput,input);maxInput=Math.max(maxInput,input);
          minActual=Math.min(minActual,actual);maxActual=Math.max(maxActual,actual);
          maxContact=Math.max(maxContact,contactError(mc.level,edge,rail,hit,frame,side,h));
        }
      }
    }
    stack.popPose(); mc.renderBuffers().bufferSource().endBatch(Sheets.cutoutBlockSheet());
    nanos+=System.nanoTime()-started;frames++;
    if(Boolean.getBoolean("fourthdimension.validation")&&samples>0&&frames%600==0)
      com.mojang.logging.LogUtils.getLogger().info("FD917_ARROW_RENDER frames={} samples={} engaged={} released={} maxContact={} maxSlowSpring={} inputRange={},{} actualRange={},{} avgNanos={}",
          frames,samples,engagedSamples,releasedSamples,maxContact,maxSpring,minInput,maxInput,minActual,maxActual,nanos/frames);
  }

  /** Compare the two tread contact points to the actual eight-sided track mesh vertices,
   * including its quarter-block longitudinal interpolation, rather than only ideal curve math. */
  private static double contactError(net.minecraft.world.level.Level level,CoasterPathEdge edge,
      FoundationData.Rail rail,CoasterPathTrackFrame.GraphHit hit,Frame f,int side,double height) {
    var table=TrackGeometry.table(level,edge);int count=Math.max(8,(int)Math.ceil(table.length()*4));
    double distance=table.distanceAt(hit.t()),step=table.length()/count;
    int segment=Math.min(count-1,(int)Math.floor(distance/step));
    double mix=(distance-segment*step)/step,error=0;
    var basis=CoasterPathTrackFrame.trackBasisAtHit(level,hit);
    int lateral=side*(f.right.dot(vec(basis.right()))<0?-1:1);
    for(int vertical:new int[]{-1,1}) {
      Vector3d[] points=new Vector3d[2];
      for(int end=0;end<2;end++) {
        double d=(segment+end)*step,t=table.parameterAt(d);
        var center=CoasterPathGraphManager.sampleEdgePosition(level,edge,t);
        var b=CoasterPathTrackFrame.trackBasisAtHit(level,new CoasterPathTrackFrame.GraphHit(center,edge,t));
        double input=rail.profile.angleAt(edge.from().equals(rail.from)?d:table.length()-d);
        points[end]=vec(center.add(b.right().scale(lateral*TrackDesign.CONTROL_HALF_GAUGE))
            .add(b.up().scale(TrackDesign.ARROW.controlHeight(input)+vertical*.09)));
      }
      error=Math.max(error,points[0].lerp(points[1],mix).distance(f.at(side*TrackDesign.CONTROL_HALF_GAUGE,height+vertical*.09,0)));
    }
    return error;
  }

  private static void assembly(ProductionMesh m,Frame f,int side,double h,double input,
                               double actual,double travel,boolean coupled,Vector3d pivot,int light) {
    double railX=side*TrackDesign.CONTROL_HALF_GAUGE, rackX=side*1.08;
    var upper=f.at(railX,h+.21,0); var lower=f.at(railX,h-.21,0);
    m.wheel(upper,f.right,f.up,.12,.14,travel/.12,light);
    m.wheel(lower,f.right,f.up,.12,.14,-travel/.12,light);
    var topFork=f.at(rackX,h+.21,0); var lowerFork=f.at(rackX,h-.21,0);
    m.beam(upper,topFork,f.up,.055,.055,"machined",light);
    m.beam(lower,lowerFork,f.up,.055,.055,"machined",light);
    m.beam(topFork,lowerFork,f.forward,.08,.07,"structural",light);
    var toe=f.at(rackX,h+.21,.27); var tip=f.at(rackX,h+1.81,.27);
    m.beam(topFork,toe,f.up,.08,.08,"structural",light);
    m.beam(toe,tip,f.forward,.09,.04,"machined",light);
    // Teeth use a .05-block pitch: 20 teeth on the one-block-circumference input pinion.
    for(int i=0;i<=32;i++) {
      var a=f.at(rackX-.055,h+.21+i*.05,.296);
      var b=f.at(rackX+.055,h+.21+i*.05,.296);
      m.beam(a,b,f.up,.018,.016,"machined",light);
    }
    double pinionZ=.30+ArrowTransmission.RACK_PINION_RADIUS;
    var inputCenter=f.at(rackX,.15,pinionZ);
    gear(m,inputCenter,f.right,f.up,ArrowTransmission.RACK_PINION_RADIUS,20,input/8,.08,light);
    // Fixed rack guide bears on its un-toothed back; gimballed chassis links carry this bogie.
    for(double y:new double[]{-.10,.40}) {
      var guide=f.at(rackX,y,.225);
      m.beam(f.at(rackX-.09,y,.225),f.at(rackX+.09,y,.225),f.forward,.06,.05,"structural",light);
      m.beam(guide,f.at(side*1.65,y,pinionZ),f.up,.08,.08,"structural",light);
    }
    var springStart=f.at(side*1.24,.15,pinionZ);
    var springEnd=f.at(side*1.60,.15,pinionZ);
    m.beam(inputCenter,springStart,f.up,.055,.055,"machined",light);
    double slowInput=input/8, slowOutput=actual/8;
    spring(m,springStart,springEnd,f.up,f.forward,slowInput,coupled?slowOutput:slowInput,light);
    // The clutch opens axially; the spring unloads on the input side while the real output
    // remains connected to the neutral-return servo and both gearbox stages.
    var clutchInput=f.at(side*(coupled?1.66:1.60),.15,pinionZ);
    var gearOne=f.at(side*1.75,.15,pinionZ);
    gear(m,clutchInput,f.right,f.up,.13,16,coupled?slowOutput:slowInput,.04,light);
    m.beam(springEnd,clutchInput,f.up,.05,.05,"machined",light);
    gear(m,f.at(side*1.70,.15,pinionZ),f.right,f.up,.13,16,slowOutput,.04,light);
    m.beam(f.at(side*1.70,.15,pinionZ),gearOne,f.up,.06,.06,"machined",light);
    gear(m,gearOne,f.right,f.up,.50,64,slowOutput,.07,light);
    var intermediate=f.at(side*1.75,.775,pinionZ);
    gear(m,intermediate,f.right,f.up,.125,16,-actual/2+Math.PI/16,.07,light);
    var stageTwo=f.at(side*2.02,.775,pinionZ);
    m.beam(intermediate,stageTwo,f.up,.055,.055,"machined",light);
    gear(m,stageTwo,f.right,f.up,.25,32,-actual/2+Math.PI/16,.07,light);
    var output=f.at(side*2.02,1.15,pinionZ);
    gear(m,output,f.right,f.up,.125,16,actual+Math.PI/16,.07,light);
    // Explicit constant-velocity telescopic shaft. Both endpoint housings are attached to
    // actual bodies; a target quaternion never substitutes for the carrier's render pose.
    var shaftStart=f.at(side*2.12,1.15,pinionZ);
    m.beam(output,shaftStart,f.up,.075,.075,"machined",light);
    var mid=new Vector3d(shaftStart).lerp(pivot,.55);
    m.beam(shaftStart,mid,f.up,.11,.11,"machined",light);
    m.beam(mid,pivot,f.up,.075,.075,"machined",light);
    gear(m,shaftStart,f.right,f.up,.12,16,actual,.16,light);
    gear(m,pivot,f.right,f.up,.16,20,actual,.16,light);
    // Exposed bearing frame and native servo housing explain support and capture power.
    m.beam(f.at(side*1.65,-.10,pinionZ+.16),f.at(side*2.02,1.15,pinionZ+.16),f.right,.12,.12,"structural",light);
    m.beam(f.at(side*2.02,1.15,pinionZ+.16),pivot,f.up,.09,.09,"structural",light);
    var actuator=f.at(side*2.02,1.15,pinionZ+.26);
    m.beam(output,actuator,f.up,.22,.20,"structural",light);
    m.beam(actuator,f.at(side*2.02,1.15,pinionZ+.30),f.up,.08,.08,
        coupled?"indicator_ready":"indicator_hold",light);
  }

  private static void spring(ProductionMesh m,Vector3d a,Vector3d b,Vector3d up,Vector3d forward,
                             double from,double to,int light) {
    Vector3d previous=null;
    for(int i=0;i<=48;i++) {
      double t=i/48.0,angle=from+(4*Math.PI+to-from)*t;
      var p=new Vector3d(a).lerp(b,t).fma(.095*Math.cos(angle),up).fma(.095*Math.sin(angle),forward);
      if(previous!=null)m.beam(previous,p,up,.025,.025,"machined",light);
      previous=p;
    }
    // Radial ends physically attach the coil to its two shaft hubs.
    m.beam(a,new Vector3d(a).fma(.095*Math.cos(from),up).fma(.095*Math.sin(from),forward),up,.025,.025,"machined",light);
    m.beam(b,new Vector3d(b).fma(.095*Math.cos(to),up).fma(.095*Math.sin(to),forward),up,.025,.025,"machined",light);
  }

  private static void gear(ProductionMesh m,Vector3d center,Vector3d axle,Vector3d up,double radius,
                           int teeth,double angle,double width,int light) {
    var forward=new Vector3d(axle).cross(up).normalize(); var half=new Vector3d(axle).mul(width/2);
    for(int side:new int[]{-1,1}) for(int spoke=0;spoke<3;spoke++) {
      double phase=angle+spoke*2*Math.PI/3;
      var hub=new Vector3d(center).fma(side*(width/2+.002),axle);
      var end=new Vector3d(hub).fma(radius*.75*Math.cos(phase),up).fma(radius*.75*Math.sin(phase),forward);
      m.beam(hub,end,axle,.018,.008,"paint_yellow",light);
    }
    int count=teeth*4;
    for(int i=0;i<count;i++) {
      double a=angle+i*2*Math.PI/count,b=angle+(i+1)*2*Math.PI/count;
      double ra=radius+(i%4<2?.007:-.007),rb=radius+((i+1)%4<2?.007:-.007);
      var va=new Vector3d(up).mul(Math.cos(a)*ra).fma(Math.sin(a)*ra,forward);
      var vb=new Vector3d(up).mul(Math.cos(b)*rb).fma(Math.sin(b)*rb,forward);
      m.quad(new Vector3d(center).sub(half).add(va),new Vector3d(center).sub(half).add(vb),
          new Vector3d(center).add(half).add(vb),new Vector3d(center).add(half).add(va),"machined",light);
      for(int side:new int[]{-1,1}) {
        var hub=new Vector3d(center).fma(side,half);
        var first=new Vector3d(hub).add(side>0?va:vb);
        var second=new Vector3d(hub).add(side>0?vb:va);
        m.quad(hub,first,second,hub,"structural",light);
      }
    }
  }
  private static Vector3d vec(Vec3 v){return new Vector3d(v.x,v.y,v.z);}
}
