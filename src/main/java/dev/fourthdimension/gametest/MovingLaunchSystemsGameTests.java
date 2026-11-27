package dev.fourthdimension.gametest;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.fourthdimension.moving.*;
import dev.fourthdimension.integration.*;
import dev.fourthdimension.station.*;
import dev.fourthdimension.runtime.FoundationData;
import dev.fourthdimension.vehicle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.silvergold.simulatedcoasters.SimulatedCoastersBlocks;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;
import org.joml.Vector3d;

/** Combined native consumer fixture; Creative setup is disclosed, not player construction evidence. */
@GameTestHolder("fourthdimension_fd2008")
@PrefixGameTestTemplate(false)
public final class MovingLaunchSystemsGameTests {
  private static final String LS="dev.coasterslaunchsystems.";
  static Object call(Object target,String name,Object... args){
    try{
      Class<?> type=target instanceof String text?Class.forName(text):target.getClass();
      for(var method:type.getMethods())if(method.getName().equals(name)&&method.getParameterCount()==args.length){
        boolean match=true;var types=method.getParameterTypes();
        for(int i=0;i<args.length;i++)if(args[i]!=null&&!box(types[i]).isInstance(args[i]))match=false;
        if(match)return method.invoke(target instanceof String?null:target,args);
      }throw new NoSuchMethodException(type+"."+name);
    }catch(java.lang.reflect.InvocationTargetException e){if(e.getCause() instanceof RuntimeException r)throw r;throw new IllegalStateException(e.getCause());}
    catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
  }
  private static Class<?> box(Class<?> c){return c==boolean.class?Boolean.class:c==int.class?Integer.class:c==long.class?Long.class:c==double.class?Double.class:c==float.class?Float.class:c;}
  private static net.minecraft.world.level.block.Block block(String name){try{return (net.minecraft.world.level.block.Block)((java.util.function.Supplier<?>)Class.forName(LS+"content.EquipmentContent").getField(name).get(null)).get();}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}}
  static Object install(net.minecraft.server.level.ServerLevel l,BlockPos from,BlockPos to,BlockPos pos,boolean exit){
    l.setBlockAndUpdate(pos,block("CONTROLLER").defaultBlockState());var c=l.getBlockEntity(pos);call(c,"bind",l,from,to);
    int depth=FoundationData.get(l).moving.assemblies.values().stream().anyMatch(a->a.configuration.equals("arrow")&&a.docks.values().stream().anyMatch(d->d.from().equals(from)&&d.to().equals(to)))?4:3;
    for(int d=8;d<=16;d+=2){module(l,c,from,to,d,false,false,depth);module(l,c,from,to,d,true,false,depth);}
    for(int d=21;d<=Math.min(47,to.getZ()-from.getZ()-1);d+=2)module(l,c,from,to,d,true,false,depth);
    if(exit)for(int d=11;d<to.getZ()-from.getZ();d+=2)module(l,c,from,to,d,false,true,depth);
    return c;
  }
  private static void module(net.minecraft.server.level.ServerLevel l,Object c,BlockPos from,BlockPos to,int distance,boolean fin,boolean exit,int depth){
    var pos=from.offset(fin?1:-1,-depth,distance);var motor=fin?pos.east():pos.west();
    l.setBlockAndUpdate(pos,block(fin?"FIN":"TIRE").defaultBlockState().setValue(BlockStateProperties.AXIS,Direction.Axis.X));
    l.setBlockAndUpdate(motor,AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,fin?Direction.WEST:Direction.EAST));
    ((CreativeMotorBlockEntity)l.getBlockEntity(motor)).generatedSpeed.setValue(64);
    var cabinet=l.getBlockEntity(pos);
    try{call(cabinet,"bind",l,from,to);}catch(RuntimeException failure){
      var track=call(LS+"track.DirectedTrack","resolve",l,from,to);
      System.out.println("FD2008_BIND_FAILURE pos="+pos+" track="+track+" dock="+RideAuthority.movingFixedEdge(l,from,to));
      if(track instanceof Optional<?> opt&&opt.isPresent()) {var edge=(dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge)call(opt.get(),"edge");var hit=dev.silvergold.simulatedcoasters.track.graph.CoasterPathTrackFrame.closestHitOnEdge(l,edge,pos.getCenter());System.out.println("FD2008_BIND_DELTA="+hit.point().subtract(pos.getCenter()));}
      throw failure;
    }call(c,"link",cabinet,exit);call(cabinet,"setArmed",true);
  }
  private static void denied(GameTestHelper h,Runnable action,String label){boolean denied=false;try{action.run();}catch(IllegalArgumentException expected){denied=true;}h.assertTrue(denied,label+" was accepted");}
  @GameTest(template="empty",timeoutTicks=3200)
  public static void retainedWingToActualFixedLaunch(GameTestHelper h){run(h,false,false);}
  public static void run(GameTestHelper h,boolean arrow,boolean manual){run(h,arrow,manual,false);}
  public static void run(GameTestHelper h,boolean arrow,boolean manual,boolean idle){run(h,arrow,manual,idle,false);}
  public static void runFailures(GameTestHelper h){run(h,false,false,false,true);}
  private static void run(GameTestHelper h,boolean arrow,boolean manual,boolean idle,boolean failures){
    var l=h.getLevel();var origin=new BlockPos(-420,130,-420);var control=origin.west(14);
    for(int x=-30;x<=-24;x++)for(int z=-31;z<=-20;z++)l.setChunkForced(x,z,true);
    var p=FakePlayerFactory.get(l,new GameProfile(UUID.fromString("fdfdfdfd-0000-0000-0000-000000002008"),"FDLaunch"));
    p.setGameMode(GameType.CREATIVE);p.setPos(control.getX()+.5,control.getY(),control.getZ()+2.5);
    l.setBlockAndUpdate(control,MovingContent.CONTROLLERS.get(MovingMechanism.TRANSFER).get().defaultBlockState().setValue(BlockStateProperties.FACING,Direction.EAST));
    l.setBlockAndUpdate(control.west(),AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.EAST));
    ((CreativeMotorBlockEntity)l.getBlockEntity(control.west())).generatedSpeed.setValue(64);
    for(int x:new int[]{0,8})for(int z:new int[]{-24,24})l.setBlockAndUpdate(origin.offset(x,0,z),SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
    var state=new MovingAssemblyState[1];var car=new FoundationData.Vehicle[1];Object[] controllers=new Object[3];var old=new RideAuthority.Token[1];
    int[] stage={0},since={0};double[] peak={0},impulse={0},retainedImpulse={0},approachImpulse={0};
    java.util.function.Consumer<dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent> measurement=event->{
      if(event.getPhysicsSystem().getLevel()!=l||car[0]==null)return;
      var body=(dev.ryanhcode.sable.sublevel.ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(car[0].chassis);if(body==null||body.getQueuedForceGroups()==null)return;
      var group=body.getQueuedForceGroups().get(dev.ryanhcode.sable.api.physics.force.ForceGroups.PROPULSION.get());
      if(group==null)return;double amount=group.getForceTotal().getLocalForce().length();
      if(stage[0]==1)retainedImpulse[0]+=amount;else if(stage[0]>=4)impulse[0]+=amount;else if(stage[0]==-1)approachImpulse[0]+=amount;
    };
    net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,measurement);
    var start=new Vector3d[1];
    h.runAtTickTime(5,()->{
      state[0]=MovingConstruction.build(p,control,new MovingConstruction.ConfigureRequest(arrow?"arrow":"wing",48,origin,Direction.SOUTH,new MovingProgramSpec(MovingMechanism.TRANSFER,8,0,0,false)));
      for(var dock:state[0].docks.values())for(boolean last:new boolean[]{false,true}){
        var near=last?dock.to():dock.from();var far=near.offset(0,0,last?64:-24);
        l.setBlockAndUpdate(far,SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
        var fixed=MotorbikeExamples.connect(l,last?near:far,last?far:near,new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(0,1,0),new Vec3(0,1,0));
        if(arrow)author(l,fixed);
      }
    });
    h.runAtTickTime(50,()->{
      var s=state[0];var edge=MovingNativeAdapter.liveDockEdge(l,s);h.assertTrue(edge!=null,"Source bridge failed to lock");
      if(arrow){var setup=author(l,edge);try{car[0]=VehicleFactory.create(l,dev.fourthdimension.track.TrackDesign.ARROW,MotorbikeExamples.sample(l,edge,8));}finally{FoundationData.get(l).rails.remove(setup);}}
      else car[0]=WingVehicle.create(l,MotorbikeExamples.sample(l,edge,8));var body=(dev.ryanhcode.sable.sublevel.ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(car[0].chassis);
      System.out.println("FD2008_SPAWN pose="+body.logicalPose()+" safety="+WingRuntime.safety(l,body,car[0]));
      var plot=body.getPlot().getEmbeddedLevelAccessor();plot.setBlock(new BlockPos(0,-1,0),block("TIRE_SURFACE").defaultBlockState(),3);plot.setBlock(new BlockPos(1,-1,0),block("BRAKE_FIN").defaultBlockState(),3);
      var dock=s.docks.get(s.source);if(!arrow)controllers[0]=install(l,dock.from(),dock.to(),origin.west(10).below(2),false);
    });
    h.runAtTickTime(115,()->{var v=car[0];var b=(dev.ryanhcode.sable.sublevel.ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.chassis);
      System.out.println("FD2008_INITIAL fault="+v.fault+" stopping="+v.stopping+" safety="+WingRuntime.safety(l,b,v)+" pose="+b.logicalPose()+" velocity="+SubLevelContainer.getContainer(l).physicsSystem().getPipeline().getLinearVelocity(b,new Vector3d())+" support="+WingTrack.support(l,v));
      if(manual&&!idle){net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(measurement);System.out.println("FD2008_OCCUPIED_SETUP car="+v.chassis+" assembly="+state[0].id+" source="+((net.minecraft.world.level.block.entity.BlockEntity)controllers[0]).getBlockPos());h.succeed();return;}
      if(arrow){var dock=state[0].docks.get(state[0].source);controllers[0]=install(l,dock.from(),dock.to(),origin.west(10).below(2),false);}
      if(arrow&&VehicleOperations.MATCHING_SUPPORT_LOST.equals(v.fault)){VehicleOperations.repairMatchingSupport(l,v);VehicleOperations.realignMatchingSupport(l,v);System.out.println("FD2008_FIXTURE_REPAIR post-interface native support realigned through normal guarded API");}
      if(!idle){VehicleOperations.restraints(l,car[0],true);call(controllers[0],"setArmed",true);}});
    Runnable capture=()->{
      var s=state[0];var v=car[0];h.assertTrue(v.authority.owner!=null,"LS did not claim exact native authority: "+call(controllers[0],"status"));
      if(failures){
        var registry=providers();var real=registry.get(v.authority.provider);
        var uncertified=new RideAuthority.ExternalProvider(){
          public RideAuthority.Controller controller(net.minecraft.server.level.ServerLevel level,BlockPos pos){return real.controller(level,pos);}
          public boolean supports(net.minecraft.server.level.ServerLevel level,FoundationData.Vehicle car){return real.supports(level,car);}
        };
        try{registry.put(v.authority.provider,uncertified);denied(h,()->RideAuthority.validateMovingCapture(l,v),"API1 provider without moving opt-in");}
        finally{registry.put(v.authority.provider,real);}
        RideAuthority.validateMovingCapture(l,v);
      }
      old[0]=RideAuthority.token(v);start[0]=new Vector3d(SubLevelContainer.getContainer(l).getSubLevel(v.chassis).logicalPose().position());
      MovingRuntime.operate(p,control,MovingRuntime.Action.CAPTURE,s.id,s.epoch,s.sequence,"end");
      h.assertTrue(v.authority.suspended&&!old[0].equals(RideAuthority.token(v)),"Capture did not revoke old launch token");
      denied(h,()->RideAuthority.dispatch(l,v.authority.controller,v.chassis,old[0]),"retained stale dispatch");
      MovingRuntime.operate(p,control,MovingRuntime.Action.MOVE,s.id,s.epoch,s.sequence,"end");stage[0]=1;
    };
    h.runAtTickTime(145,()->{if(idle)return;if(arrow){call(controllers[0],"requestDispatch",call(controllers[0],"requestRevision"));stage[0]=-1;}else capture.run();});
    for(int tick=150;tick<3150;tick+=5){final int time=tick;h.runAtTickTime(tick,()->{
      var s=state[0];var v=car[0];var body=(dev.ryanhcode.sable.sublevel.ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.chassis);
      if(idle){
        MovingNativeAdapter.readback(l,s);
        if(time%20==0)System.out.println("FD2008_IDLE tick="+time+" pose="+s.actualPose+" velocity="+s.linearVelocity+" car="+v.fault+" live="+(MovingNativeAdapter.liveDockEdge(l,s)!=null));
        if(time>=1200){h.assertTrue(MovingNativeAdapter.liveDockEdge(l,s)!=null&&v.fault.isEmpty(),"Stationary source dock drifted: "+s.actualPose+" "+v.fault);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(measurement);h.succeed();}return;
      }
      if(stage[0]>=4&&stage[0]<=5)peak[0]=Math.max(peak[0],SubLevelContainer.getContainer(l).physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d()).length());
      if(stage[0]>=4&&stage[0]<=5&&call(controllers[1],"phase").toString().equals("FAULT")){System.out.println("FD2008_EXIT_FAILURE pose="+body.logicalPose()+" fault="+v.fault+" support="+WingTrack.support(l,v)+" bound="+v.boundFrom+"/"+v.boundTo);h.fail(call(controllers[1],"status").toString());}
      if(time%100==0)System.out.println("FD2008_STATE tick="+time+" stage="+stage[0]+" moving="+s.phase+" fault="+s.fault+" car="+v.fault+" authority="+v.authority.reason+" ls="+call(controllers[stage[0]<2?0:1],"status"));
      if(stage[0]==-1&&approachImpulse[0]>.01&&call(controllers[0],"phase").toString().equals("HOLD")){
        RideAuthority.capture(l,v.authority.controller,v.chassis,RideAuthority.token(v));stage[0]=-2;
      }else if(stage[0]==-2&&Boarding.ready(l,v)){
        h.assertTrue(approachImpulse[0]>.01,"No source LS force");call(controllers[0],"setArmed",true);since[0]=time;stage[0]=-3;
      }else if(stage[0]==-3&&time>=since[0]+10){capture.run();
      }else if(stage[0]==1){
        h.assertTrue(s.phase!=MovingAssemblyState.Phase.FAULT_HOLD,"Moving fault: "+s.fault);
        var motion=call(LS+"api.MotionProviders","resolve",l,v.chassis);var snapshot=((Optional<?>)motion).orElseThrow();
        h.assertTrue(!(boolean)call(snapshot,"propulsionAllowed")&&!(boolean)call(snapshot,"brakingAllowed"),"LS force permitted during moving retention");
        if(s.phase==MovingAssemblyState.Phase.RECONNECTED){
          h.assertTrue(body.logicalPose().position().x>start[0].x+7.8,"Train did not follow physical transfer");
          MovingRuntime.operate(p,control,MovingRuntime.Action.RELEASE,s.id,s.epoch,s.sequence,"end");
          var d=s.docks.get("end");controllers[1]=install(l,d.from(),d.to(),origin.east(18).below(2),true);
          controllers[2]=install(l,d.to(),d.to().offset(0,0,64),d.to().offset(4,-2,8),false);
          call(controllers[1],"connectDestination",controllers[2]);call(controllers[2],"setArmed",true);
          stage[0]=2;since[0]=time;
        }
      }else if(stage[0]==2&&time>=since[0]+30){
        call(controllers[1],"setArmed",true);stage[0]=3;since[0]=time;
      }else if(stage[0]==3&&Set.of("READY","READY_EXIT").contains(call(controllers[1],"phase").toString())){
        h.assertTrue(s.service.externalOwner!=null&&!v.authority.owner.equals(old[0].owner())&&v.authority.held,"Receiving handoff not fresh and held");
        denied(h,()->RideAuthority.returnFromMoving(l,v.chassis,RideAuthority.token(v),((net.minecraft.world.level.block.entity.BlockEntity)controllers[0]).getBlockPos()),"competing second receiver");
        denied(h,()->RideAuthority.dispatch(l,v.authority.controller,v.chassis,old[0]),"pre-move launch token");
        call(controllers[1],"requestDispatch",call(controllers[1],"requestRevision"));stage[0]=4;
        if(failures){
          var registry=providers();var real=registry.remove(v.authority.provider);var owner=v.authority.owner;
          try{
            h.assertTrue(RideAuthority.holds(l,v)&&v.authority.suspended,"Missing provider failed to suspend released service");
            h.assertTrue(owner.equals(v.authority.owner)&&RideAuthority.externalSelected(v)&&!RideAuthority.externalAvailable(l,v),"Missing provider invented native fallback");
            denied(h,()->RideAuthority.dispatch(l,v.authority.controller,v.chassis,RideAuthority.token(v)),"missing provider dispatch");
          }finally{registry.put(v.authority.provider,real);}
          h.assertTrue(v.authority.suspended&&v.authority.held,"Provider return automatically resumed");
          stage[0]=6;since[0]=time;
        }
      }else if(stage[0]==6&&time>=since[0]+40){
        call(controllers[1],"setArmed",true);stage[0]=7;
      }else if(stage[0]==7&&Set.of("READY","READY_EXIT").contains(call(controllers[1],"phase").toString())){
        h.assertTrue(v.authority.held&&!v.authority.suspended,"Fresh arm failed to resume held service");
        call(controllers[1],"requestDispatch",call(controllers[1],"requestRevision"));
        stage[0]=9;
      }else if(stage[0]==9&&!v.authority.held){
        l.removeBlock(((net.minecraft.world.level.block.entity.BlockEntity)controllers[1]).getBlockPos(),false);
        h.assertTrue(RideAuthority.holds(l,v)&&v.authority.suspended,"Controller loss did not immediately hold");
        stage[0]=8;since[0]=time;
      }else if(stage[0]==8&&time>=since[0]+10){
        h.assertTrue(!s.service.cars.get(v.chassis).dispatched&&v.stopping&&v.restraints,"Lost controller left moving-service dispatch active");
        h.assertTrue(RideAuthority.externalSelected(v)&&!RideAuthority.externalAvailable(l,v),"Lost controller lost provider identity");
        h.assertTrue(SubLevelContainer.getContainer(l).physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d()).length()<.02,"Lost controller failed physical stop");
        denied(h,()->RideAuthority.dispatch(l,v.authority.controller,v.chassis,RideAuthority.token(v)),"removed controller dispatch");
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(measurement);
        System.out.println("FD2008_LOSS_PASS uncertifiedDenied=true missingProviderHeld=true noFallback=true explicitRearm=true controllerLossHeld=true serviceDispatchRevoked=true");h.succeed();
      }else if(stage[0]==4){
        peak[0]=Math.max(peak[0],SubLevelContainer.getContainer(l).physicsSystem().getPipeline().getLinearVelocity(body,new Vector3d()).length());
        if(call(controllers[1],"phase").toString().equals("READY_EXIT")){call(controllers[1],"requestDispatch",call(controllers[1],"requestRevision"));stage[0]=5;}
        else if(call(controllers[1],"phase").toString().equals("EXITING"))stage[0]=5;
      }else if(stage[0]==5&&!MovingServiceRuntime.applies(l,v)){
        h.assertTrue(peak[0]>.5&&impulse[0]>.01,"No actual LS consumer impulse/departure speed: impulse="+impulse[0]+" peak="+peak[0]);
        h.assertTrue(retainedImpulse[0]<1e-8,"Propulsion impulse leaked through moving retention");
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(measurement);
        h.assertTrue(v.restraints&&v.fault.isEmpty(),"Departure failed native vehicle guards");
        System.out.println("FD2008_CONSUMER_PASS variant="+v.variant+" approachImpulse="+approachImpulse[0]+" impulse="+impulse[0]+" retainedImpulse="+retainedImpulse[0]+" peak="+peak[0]+" position="+body.logicalPose().position()+" owner="+v.authority.owner+" original="+old[0].owner()+" retainedForceDenied=true staleDenied=true serviceCleared=true");h.succeed();
      }
    });}
  }
  /** Test-only dependency withdrawal; production has no unregister/backdoor API. */
  @SuppressWarnings("unchecked")
  private static Map<String,RideAuthority.ExternalProvider> providers(){
    try{var field=RideAuthority.class.getDeclaredField("EXTERNAL");field.setAccessible(true);return (Map<String,RideAuthority.ExternalProvider>)field.get(null);}
    catch(ReflectiveOperationException failure){throw new IllegalStateException(failure);}
  }
  private static FoundationData.Rail author(net.minecraft.server.level.ServerLevel l,dev.silvergold.simulatedcoasters.track.graph.CoasterPathEdge edge){
    var design=dev.fourthdimension.track.TrackDesign.ARROW;double length=dev.fourthdimension.track.TrackGeometry.table(l,edge).length();
    var r=new FoundationData.Rail(edge.from(),edge.to(),design.family,new dev.fourthdimension.physics.RotationProfile(List.of(new dev.fourthdimension.physics.RotationProfile.Key(0,0),new dev.fourthdimension.physics.RotationProfile.Key(length,0))),UUID.randomUUID(),design.variant,dev.fourthdimension.track.TrackGeometry.fingerprint(l,edge),0,true);
    dev.fourthdimension.track.TrackPlacement.captureOriginalCurve(r,edge.bezier());FoundationData.get(l).rails.add(r);return r;
  }
}
