package dev.fourthdimension.gametest;

import com.google.gson.Gson;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.track.*;
import dev.fourthdimension.vehicle.*;
import dev.fourthdimension.shuttle.*;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.silvergold.simulatedcoasters.track.cart.*;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3d;

/** Disclosed benchmark setup and normal operator actions; never player-input evidence. */
@EventBusSubscriber(modid="fourthdimension")
public final class Fd904ParkFixture {
  private static final Gson JSON=new Gson();
  private static final List<String> NAMES=List.of("arrow","legacy","free_spin","revised","pax","wing","spinning","suspended","fixed_inverted","axis","roller_ball","flying","new_generation_flying","pipeline","dive","s2_spinning","powered_inverted_yaw");
  private static final Map<String,Group> GROUPS=new LinkedHashMap<>();
  private static final Map<String,Runnable> BIND=new HashMap<>();
  private static boolean running,draining;
  private static final class Group {
    String name; UUID lead; List<UUID> cars; int x,y,z; double[] station;
    boolean held; transient boolean departed,preparing,captureRequested,failed; transient int dwell,cycles; transient long dispatchTick;
  }
  private static Path manifest(){return Path.of(System.getProperty("fourthdimension.parkManifest"));}
  private static FoundationData.Vehicle car(ServerLevel l,Group g){return Objects.requireNonNull(FoundationData.get(l).vehicles.get(g.lead),"Missing park chassis "+g.name);}
  private static Vector3d center(ServerLevel l,Group g){var body=SubLevelContainer.getContainer(l).getSubLevel(g.lead);return body==null?null:CoasterCartResolve.cartCenterWorld(body);}
  private static void save(){try{Files.writeString(manifest(),JSON.toJson(GROUPS.values())+"\n");}catch(java.io.IOException e){throw new IllegalStateException(e);}}
  @SubscribeEvent public static void commands(RegisterCommandsEvent event){
    if(!Boolean.getBoolean("fourthdimension.validation")||System.getProperty("fourthdimension.parkManifest")==null)return;
    var root=Commands.literal("fd904").requires(s->s.hasPermission(2));
    for(String action:List.of("create","bind","fill","inspect","pause","play","resume","authority","transfer","station","seated","close","mount","view","dispatch","capture","open","unload"))root.then(Commands.literal(action).then(Commands.argument("name",StringArgumentType.word()).executes(ctx->{
      var l=ctx.getSource().getLevel();String name=StringArgumentType.getString(ctx,"name");
      try{
        if(action.equals("create"))create(l,name);
        else {var g=Objects.requireNonNull(GROUPS.get(name),"Unknown park group");var v=car(l,g);
          switch(action){
            case "bind" -> {var bind=BIND.remove(name);if(bind!=null)bind.run();var p=center(l,g);g.station=new double[]{p.x,p.y,p.z};save();}
            case "fill" -> fill(l,g);
            case "pause" -> g.held=true;
            case "play" -> g.held=false;
            case "close" -> restraints(l,v,true);
            case "seated" -> {if(NewGenerationFlyingVehicle.is(v))NewGenerationFlyingRuntime.unwind(l,v);else FlyingRuntime.requestPosture(l,v,false,false);}
            case "transfer" -> dev.fourthdimension.integration.RideAuthority.transfer(l,v.authority.controller,v.chassis,dev.fourthdimension.integration.RideAuthority.token(v),v.dock);
            case "station" -> {if(!DiveVehicle.is(v)||!Boarding.ready(l,v))throw new IllegalArgumentException("Ready Dive dock required");var pos=new BlockPos(g.x-5,g.y,g.z+6);if(!l.getBlockState(pos).isAir())throw new IllegalArgumentException("Benchmark station position occupied");var old=v.dock;l.setBlockAndUpdate(pos,dev.fourthdimension.station.StationContent.STATION.get().defaultBlockState());var station=(dev.fourthdimension.station.BoardingStationEntity)l.getBlockEntity(pos);station.vehicle=v.chassis;DiveRuntime.rebindDock(l,v,pos);station.setChanged();if(!old.equals(pos)&&l.getBlockEntity(old) instanceof dev.fourthdimension.station.BoardingStationEntity previous){previous.vehicle=null;previous.setChanged();}System.out.println("FD904_STATION dive old="+old+" new="+pos);}
            case "authority" -> dev.fourthdimension.integration.RideAuthority.resume(l,v.authority.controller,v.chassis,dev.fourthdimension.integration.RideAuthority.token(v));
            case "resume" -> {if(SuspendedVehicle.is(v))SuspendedRuntime.resumeToDock(l,v);else if(SpinningVehicle.is(v))SpinningRuntime.resumeToDock(l,v);else if(FixedInvertedVehicle.is(v))FixedInvertedRuntime.resumeToDock(l,v);else if(AxisVehicle.is(v))AxisRuntime.resumeToDock(l,v);else if(NewGenerationFlyingVehicle.is(v))NewGenerationFlyingRuntime.retry(l,v);else if(PoweredInvertedYawVehicle.is(v))PoweredInvertedYawRuntime.resume(l,v);else if(FlyingVehicle.is(v))FlyingRuntime.resumeToDock(l,v);else if(PipelineVehicle.is(v))PipelineRuntime.resumeToDock(l,v);else if(DiveVehicle.is(v))DiveRuntime.resumeToDock(l,v);else if(S2SpinningVehicle.is(v))S2SpinningRuntime.resume(l,v);else RollerBallRuntime.resume(l,v);g.departed=true;g.captureRequested=false;g.failed=false;}
            case "inspect" -> ctx.getSource().sendSuccess(()->Component.literal(diagnostic(l,g)),false);
            case "dispatch" -> dispatch(l,g);
            case "capture" -> {VehicleOperations.capture(l,v);g.dwell=0;g.captureRequested=true;}
            case "open" -> restraints(l,v,false);
            case "unload" -> {if(!Boarding.ready(l,v)||v.restraints)throw new IllegalArgumentException("Open ready station required");for(var seat:seats(l,g))for(var rider:List.copyOf(seat.getPassengers()))rider.stopRiding();}
            case "view" -> {var p=ctx.getSource().getPlayerOrException();if(p.isPassenger())throw new IllegalArgumentException("Unload player before park camera");p.connection.teleport(g.x-18,g.y+22,g.z+25, -70,20);}
            case "mount" -> {if(!Boarding.ready(l,v)||v.restraints)throw new IllegalArgumentException("Open ready station required");var p=ctx.getSource().getPlayerOrException();var c=v.carriers.getFirst();var body=SubLevelContainer.getContainer(l).getSubLevel(c.body);var pos=body.getPlot().getCenterBlock().offset(c.seats.getFirst());for(var seat:l.getEntitiesOfClass(SeatEntity.class,new AABB(pos)))if(seat.blockPosition().equals(pos))for(var rider:List.copyOf(seat.getPassengers())){if(rider instanceof net.minecraft.world.entity.player.Player)throw new IllegalArgumentException("Seat already has a player");rider.stopRiding();rider.discard();}SeatBlock.sitDown(l,pos,p);if(!p.isPassenger())throw new IllegalArgumentException("Player mount rejected");}
          }
        }
        ctx.getSource().sendSuccess(()->Component.literal(snapshot(l)),false);return 1;
      }catch(RuntimeException e){ctx.getSource().sendFailure(Component.literal("FD904_REJECT "+action+" "+name+": "+e));return 0;}
    })));
    for(String action:List.of("adopt","start","drain","snapshot","combined","checkpoint"))root.then(Commands.literal(action).executes(ctx->{
      try{switch(action){
        case "adopt" -> {if(!GROUPS.isEmpty())throw new IllegalArgumentException("Already adopted");for(var g:JSON.fromJson(Files.readString(manifest()),Group[].class))GROUPS.put(g.name,g);}
        case "start" -> {if(GROUPS.size()!=17||GROUPS.values().stream().anyMatch(g->g.station==null))throw new IllegalArgumentException("Bind all17 styles first");GROUPS.values().forEach(g->g.failed=false);running=true;draining=false;}
        case "drain" -> {draining=true;running=true;}
        case "combined" -> combined(ctx.getSource().getLevel());
        case "checkpoint" -> save();
      }ctx.getSource().sendSuccess(()->Component.literal(snapshot(ctx.getSource().getLevel())),false);return 1;
      }catch(Exception e){ctx.getSource().sendFailure(Component.literal("FD904_REJECT "+action+": "+e));return 0;}
    }));event.getDispatcher().register(root);
  }
  private static void create(ServerLevel l,String name){
    int index=NAMES.indexOf(name);if(index<0||GROUPS.containsKey(name))throw new IllegalArgumentException("New declared style required");
    var g=new Group();g.name=name;g.x=-850+(index%5)*350;g.y=110;g.z=-850+(index/5)*480;
    if(name.equals("roller_ball"))g.x=-750; // Example declares the narrower ±800 X origin bound.
    var origin=new BlockPos(g.x,g.y,g.z);var old=new HashSet<>(FoundationData.get(l).vehicles.keySet());FoundationData.Vehicle lead;
    var existingEdges=new HashSet<String>();
    for(var e:CoasterPathGraphManager.get(l).edges())existingEdges.add(e.from()+"/"+e.to());
    if(name.equals("pax"))for(int x=(g.x-8)>>4;x<=(g.x+8)>>4;x++)for(int z=(g.z-58)>>4;z<=(g.z+90)>>4;z++){l.setChunkForced(x,z,true);l.getChunk(x,z);}
    switch(name){
      case "arrow","legacy","free_spin","revised" -> {
        var design=switch(name){case "arrow"->TrackDesign.ARROW;case "legacy"->TrackDesign.LEGACY;case "free_spin"->TrackDesign.FREE_SPIN;default->TrackDesign.REVISED;};
        lead=ExampleLayouts.create(l,origin,design);
        if(name.equals("arrow")){
          var rail=FoundationData.get(l).rails.stream().filter(r->r.matches(lead.boundFrom,lead.boundTo)).findFirst().orElseThrow();
          var edge=TrackGeometry.resolve(l,rail);var trailing=VehicleFactory.create(l,design,FlyingExamples.sample(l,edge,6));
          BIND.put(name,()->{var c=SubLevelContainer.getContainer(l);if(!CoasterCartTrainLinkConstraint.linkCarts(l,(ServerSubLevel)c.getSubLevel(lead.chassis),(ServerSubLevel)c.getSubLevel(trailing.chassis),6))throw new IllegalArgumentException("Arrow coupling rejected");});
        }
      }
      case "pax" -> {var e=ShuttleExamples.create(l,origin,false);lead=e.cars().getFirst();BIND.put(name,()->ShuttleExamples.assemble(l,e));}
      case "wing" -> lead=WingExamples.create(l,origin,true).vehicle();
      case "spinning" -> lead=SpinningExamples.create(l,origin,true).vehicle();
      case "suspended" -> lead=SuspendedExamples.create(l,origin,true).vehicle();
      case "fixed_inverted" -> lead=FixedInvertedExamples.create(l,origin,true).vehicle();
      case "axis" -> lead=AxisExamples.create(l,origin,true).vehicle();
      case "roller_ball" -> lead=RollerBallExamples.create(l,origin,true).car();
      case "flying" -> lead=FlyingExamples.create(l,origin,true).vehicle();
      case "new_generation_flying" -> lead=NewGenerationFlyingExamples.create(l,origin,new UUID(0,904)).vehicle();
      case "pipeline" -> lead=PipelineExamples.create(l,origin,true).vehicle();
      case "dive" -> lead=DiveExamples.create(l,origin,true).vehicle();
      case "s2_spinning" -> {var e=S2SpinningExamples.create(l,origin,1);lead=e.cars().getFirst();BIND.put(name,()->S2SpinningExamples.bind(l,e));}
      case "powered_inverted_yaw" -> {var e=PoweredInvertedYawExamples.create(l,origin,1,new UUID(0,904));lead=e.cars().getFirst();BIND.put(name,()->PoweredInvertedYawExamples.bind(l,e));}
      default -> throw new IllegalArgumentException(name);
    }
    g.lead=lead.chassis;g.cars=FoundationData.get(l).vehicles.keySet().stream().filter(id->!old.contains(id)).toList();GROUPS.put(name,g);
    // Keep only route neighborhoods loaded, including native chain/controller endpoints.
    for(var edge:CoasterPathGraphManager.get(l).edges())if(!existingEdges.contains(edge.from()+"/"+edge.to()))
      for(int i=0;i<=64;i++){var p=CoasterPathGraphManager.sampleEdgePosition(l,edge,i/64d);int x=((int)Math.floor(p.x))>>4,z=((int)Math.floor(p.z))>>4;for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)l.setChunkForced(x+dx,z+dz,true);}
    save();
  }
  private static List<SeatEntity> seats(ServerLevel l,Group g){var result=new ArrayList<SeatEntity>();var container=SubLevelContainer.getContainer(l);for(var id:g.cars)for(var c:FoundationData.get(l).vehicles.get(id).carriers){var body=container.getSubLevel(c.body);if(body!=null)for(var offset:c.seats)result.addAll(l.getEntitiesOfClass(SeatEntity.class,new AABB(body.getPlot().getCenterBlock().offset(offset))));}return result.stream().distinct().toList();}
  private static void fill(ServerLevel l,Group g){
    var container=SubLevelContainer.getContainer(l);for(var id:g.cars){var v=FoundationData.get(l).vehicles.get(id);if(!Boarding.ready(l,v)||v.restraints)throw new IllegalArgumentException("Open ready car required "+id);for(var c:v.carriers)for(var offset:c.seats){var body=container.getSubLevel(c.body);var pos=body.getPlot().getCenterBlock().offset(offset);if(Boarding.seatOccupied(l,pos))continue;var rider=EntityType.PIG.create(l);rider.setNoAi(true);rider.setPersistenceRequired();rider.setPos(g.x,g.y+2,g.z);l.addFreshEntity(rider);SeatBlock.sitDown(l,pos,rider);if(!rider.isPassenger())throw new IllegalArgumentException("Pig mount rejected "+pos);}}
  }
  private static void restraints(ServerLevel l,FoundationData.Vehicle v,boolean closed){
    if(NewGenerationFlyingVehicle.is(v)){NewGenerationFlyingRuntime.restraints(l,v,true,closed);NewGenerationFlyingRuntime.restraints(l,v,false,closed);}
    else if(FlyingVehicle.is(v)){FlyingRuntime.restraints(l,v,true,closed);FlyingRuntime.restraints(l,v,false,closed);}
    else VehicleOperations.restraints(l,v,closed);
  }
  private static void dispatch(ServerLevel l,Group g){var v=car(l,g);
    if(DiveVehicle.is(v)&&v.authority.owner!=null&&l.getBlockEntity(v.dock) instanceof dev.fourthdimension.station.BoardingStationEntity station&&!station.authorityId().equals(v.authority.owner)){
      dev.fourthdimension.integration.RideAuthority.transfer(l,v.authority.controller,v.chassis,dev.fourthdimension.integration.RideAuthority.token(v),v.dock);g.dwell=0;System.out.println("FD904_TRANSFER dive to station");return;
    }
if(!v.restraints)restraints(l,v,true);if(FlyingVehicle.is(v)&&v.flying.lock!=FlyingState.Lock.PRONE){if(!v.flying.transitioning())FlyingRuntime.requestPosture(l,v,true,false);g.preparing=true;return;}if(v.authority.owner==null||PoweredInvertedYawVehicle.is(v)||(S2SpinningVehicle.is(v)&&!S2SpinningRuntime.optional(v)))VehicleOperations.dispatch(l,v);else dev.fourthdimension.integration.RideAuthority.dispatch(l,v.authority.controller,v.chassis,dev.fourthdimension.integration.RideAuthority.token(v));g.preparing=false;g.departed=false;g.captureRequested=false;g.dwell=0;g.dispatchTick=l.getGameTime();g.cycles++;System.out.println("FD904_DISPATCH "+g.name+" cycle="+g.cycles);}
  @SubscribeEvent public static void tick(ServerTickEvent.Post event){
    if(!running)return;var l=event.getServer().overworld();
    for(var g:GROUPS.values())try{
      if(g.failed)continue;
      var v=car(l,g);if(!v.fault.isEmpty())throw new IllegalArgumentException(v.fault);
      var p=center(l,g);if(p==null)throw new IllegalArgumentException("Missing native chassis");
      double distance=p.distance(g.station[0],g.station[1],g.station[2]);if(distance>30)g.departed=true;
      boolean ready=g.cars.stream().allMatch(id->Boarding.ready(l,FoundationData.get(l).vehicles.get(id)));
      if(g.preparing){if(draining){g.preparing=false;continue;}if(v.flying.lock==FlyingState.Lock.PRONE&&!v.flying.transitioning())dispatch(l,g);continue;}
      if(g.name.equals("pax")){var w=ShuttleRuntime.owner(l,g.lead);if(w.phase==ShuttleWinchEntity.Phase.BOARDING&&ready&&!draining&&!g.held&&++g.dwell>=40)dispatch(l,g);continue;}
      if(!v.stopping&&g.departed&&!g.captureRequested&&distance<2){g.captureRequested=true;VehicleOperations.capture(l,v);System.out.println("FD904_CAPTURE "+g.name+" distance="+distance);}
      if(FlyingVehicle.is(v)&&v.stopping&&g.departed&&FlyingRuntime.dock(l,v)&&v.flying.lock==FlyingState.Lock.PRONE&&!v.flying.transitioning())FlyingRuntime.requestPosture(l,v,false,false);
      if(NewGenerationFlyingVehicle.is(v)&&v.stopping&&g.departed&&NewGenerationFlyingRuntime.dock(l,v)&&!v.yaw.mode.name().equals("STATION_CAPTURE"))NewGenerationFlyingRuntime.unwind(l,v);
      if(v.stopping&&ready){if(!draining&&!g.held&&++g.dwell>=40)dispatch(l,g);}
      if(NewGenerationFlyingVehicle.is(v)&&v.stopping&&v.yaw.status.equals("AWAIT_SHOW_ACK")&&SubLevelContainer.getContainer(l).physicsSystem().getPipeline().getLinearVelocity((ServerSubLevel)SubLevelContainer.getContainer(l).getSubLevel(v.chassis),new Vector3d()).length()<.05){
        var rail=NewGenerationFlyingRuntime.rail(l,v);var interval=rail.yaw.intervals.stream().filter(i->!i.stopName().isEmpty()).findFirst().orElseThrow();NewGenerationFlyingRuntime.show(l,v,UUID.randomUUID(),v.yaw.epoch,interval.stopName());
      }
      if(NewGenerationFlyingVehicle.is(v)&&v.stopping&&v.yaw.holdAcknowledged){var rail=NewGenerationFlyingRuntime.rail(l,v);var interval=rail.yaw.intervals.stream().filter(i->i.stopName().equals(v.yaw.stopName)).findFirst().orElse(null);if(interval!=null&&v.yaw.showTicks>=interval.showDuration()&&Math.abs(v.yaw.angle-interval.profile().keys().getLast().angle())<Math.toRadians(2)&&Math.abs(v.yaw.velocity)<.05)VehicleOperations.dispatch(l,v);}
    }catch(RuntimeException e){draining=true;g.failed=true;g.held=true;System.err.println("FD904_STOP "+g.name+": "+e+"; draining remaining rides");}
    if(l.getGameTime()%100==0)System.out.println(snapshot(l));
  }
  private static void combined(ServerLevel l) throws ReflectiveOperationException {
    var g=GROUPS.get("arrow");var v=car(l,g);if(!Boarding.ready(l,v))throw new IllegalArgumentException("Capture Arrow at station before profile setup");
    var data=FoundationData.get(l);int index=0;
    for(var rail:data.rails)if(rail.family==dev.fourthdimension.contract.Family.ARROW){
      var appearance=new dev.fourthdimension.integration.TrackAppearance(dev.fourthdimension.integration.TrackAppearance.CANDIDATES.get(index++%2),-1);
      if(!appearance.supportsProfile(rail.profile))throw new IllegalArgumentException("Arrow appearance outside supported stroke");rail.appearance=appearance;
    }
    var rail=data.rails.stream().filter(r->r.matches(v.boundFrom,v.boundTo)).findFirst().orElseThrow();
    var pos=rail.from.offset(-6,0,0);if(!l.getBlockState(pos).isAir())throw new IllegalArgumentException("Engineered setup controller position occupied");
    var block=net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.parse("coastersengineered:linear_motor_controller"));
    if(block==net.minecraft.world.level.block.Blocks.AIR)throw new IllegalArgumentException("Frozen Engineered provider required");
    ((dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity)l.getBlockEntity(rail.from)).clearLift();
    l.setBlockAndUpdate(pos,block.defaultBlockState());
    var segment=EngineeredGameTests.type("span.SpanSegment").getConstructor(BlockPos.class,BlockPos.class,double.class,double.class).newInstance(rail.from,rail.to,0d,1d);
    var span=EngineeredGameTests.type("span.TrackSpan").getConstructor(List.class).newInstance(List.of(segment));
    var behaviour=EngineeredGameTests.invoke(EngineeredGameTests.type("span.TrackSpanBehaviour"),"at",l,rail.from);
    if(!(boolean)EngineeredGameTests.invoke(behaviour,"setSpan",span))throw new IllegalArgumentException("Engineered span rejected");
    var controller=l.getBlockEntity(pos);EngineeredGameTests.invoke(controller,"bindTo",rail.from);
    var kind=EngineeredGameTests.type("controller.MotorStep$Kind");var heading=EngineeredGameTests.type("controller.MotorStep$Heading");
    var step=EngineeredGameTests.type("controller.MotorStep").getConstructor(kind,heading,int.class).newInstance(kind.getField("LAUNCH").get(null),heading.getField("FORWARD").get(null),4);
    EngineeredGameTests.invoke(controller,"setSteps",List.of(step));data.setDirty();FoundationSync.broadcast(l);
    System.out.println("FD904_COMBINED arrowStyles="+index+" motor="+pos.toShortString()+" from="+rail.from+" to="+rail.to+" speed=4 heading=FORWARD");
  }
  private static int traceSteps;
  @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGHEST)
  public static void tracePreFirst(dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent e){trace(e.getPhysicsSystem(),e.getTimeStep(),"pre_first");}
  @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST)
  public static void tracePreLast(dev.ryanhcode.sable.neoforge.event.ForgeSablePrePhysicsTickEvent e){trace(e.getPhysicsSystem(),e.getTimeStep(),"pre_last");}
  @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST)
  public static void tracePost(dev.ryanhcode.sable.neoforge.event.ForgeSablePostPhysicsTickEvent e){trace(e.getPhysicsSystem(),e.getTimeStep(),"post");}
  private static Object field(Class<?> type,Object object,String name)throws ReflectiveOperationException{var f=type.getDeclaredField(name);f.setAccessible(true);return f.get(object);}
  private static void trace(dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem physics,double dt,String stage){
    if(!Boolean.getBoolean("fourthdimension.fd904Trace")||traceSteps>=160)return;
    var l=physics.getLevel();if(!l.dimension().equals(net.minecraft.world.level.Level.OVERWORLD))return;
    var container=SubLevelContainer.getContainer(l);boolean found=false;
    for(var v:FoundationData.get(l).vehicles.values()){
      if(!RollerBallVehicle.is(v)&&!DiveVehicle.is(v))continue;
      if(!(container.getSubLevel(v.chassis) instanceof ServerSubLevel body))continue;
      found=true;var out=new LinkedHashMap<String,Object>();out.put("stage",stage);out.put("step",traceSteps);out.put("tick",l.getGameTime());out.put("dt",dt);out.put("variant",v.variant);out.put("id",v.chassis);out.put("velocity",physics.getPipeline().getLinearVelocity(body,new Vector3d()).toString());out.put("center",CoasterCartResolve.cartCenterWorld(body).toString());out.put("com",body.getMassTracker().getCenterOfMass().toString());out.put("worldCom",body.logicalPose().transformPosition(new Vector3d(body.getMassTracker().getCenterOfMass())).toString());out.put("rotationPoint",body.logicalPose().rotationPoint().toString());out.put("mass",body.getMassTracker().getMass());out.put("selfMass",body.getSelfMassTracker().getMass());out.put("stopping",v.stopping);out.put("restraints",v.restraints);
      try{
        Object hold;
        if(DiveVehicle.is(v))hold=((Map<?,?>)field(DiveRuntime.class,null,"HOLDS")).get(body);
        else{var child=container.getSubLevel(v.carriers.getFirst().body);var live=((Map<?,?>)field(RollerBallRuntime.class,null,"LIVE")).get(child);hold=live==null?null:field(live.getClass(),live,"hold");}
        out.put("hold",hold==null?"none":hold.getClass().getName());
        if(hold instanceof dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintHandle h){out.put("holdValid",h.isValid());if(h.isValid()){var linear=new Vector3d();var angular=new Vector3d();h.getJointImpulses(linear,angular);out.put("holdImpulse",linear.toString());out.put("holdAngularImpulse",angular.toString());}}
      }catch(Exception failure){out.put("diagnosticError",failure.toString());}
      System.out.println("FD904_PHYSICS "+JSON.toJson(out));
    }
    if(found&&stage.equals("post"))traceSteps++;
  }
  private static String diagnostic(ServerLevel l,Group g){
    var v=car(l,g);var c=SubLevelContainer.getContainer(l);var p=(ServerSubLevel)c.getSubLevel(v.chassis);var out=new LinkedHashMap<String,Object>();
    out.put("name",g.name);out.put("velocity",c.physicsSystem().getPipeline().getLinearVelocity(p,new Vector3d()).toString());out.put("mass",p.getMassTracker().getMass());
    out.put("carrier",v.carriers.stream().map(a->Map.of("body",a.body,"angle",a.angle,"omega",a.omega,"occupants",a.occupants,"seats",a.riderSeats.toString(),"held",a.liftHeld,"latched",a.latched)).toList());
    out.put("dock",v.dock);out.put("authority",v.authority);out.put("dive",v.dive);out.put("fixture",Map.of("departed",g.departed,"preparing",g.preparing,"captureRequested",g.captureRequested,"failed",g.failed));out.put("poweredYaw",v.poweredYaw);out.put("roller",v.roller);out.put("flying",v.flying);out.put("yaw",v.yaw);
    var r=FoundationData.get(l).rails.stream().filter(a->a.matches(v.boundFrom,v.boundTo)).findFirst().orElse(null);
    if(r!=null){var e=TrackGeometry.resolve(l,r);var pos=center(l,g);var hit=CoasterPathTrackFrame.closestHitOnEdge(l,e,new net.minecraft.world.phys.Vec3(pos.x,pos.y,pos.z));var table=TrackGeometry.table(l,e);
      out.put("from",e.from().toShortString());out.put("to",e.to().toShortString());out.put("hit",hit.t());out.put("distance",table.distanceAt(hit.t()));out.put("length",table.length());
      var span=dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftSpanQuery.findAtGraphHit(l,hit,0);if(span!=null)out.put("chainSpeed",dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftDriveDirection.signedChainBlocksPerSecond(span.anchor(),span.path(),0,256,16));
    }return "FD904_DIAGNOSTIC "+JSON.toJson(out);
  }
  private static String snapshot(ServerLevel l){var data=FoundationData.get(l);var groups=new ArrayList<Object>();for(var g:GROUPS.values()){var v=car(l,g);var p=center(l,g);var state=new LinkedHashMap<String,Object>();state.put("name",g.name);state.put("cars",g.cars);state.put("center",p==null?null:new double[]{p.x,p.y,p.z});state.put("ready",Boarding.ready(l,v));state.put("stopping",v.stopping);state.put("fault",v.fault);state.put("cycles",g.cycles);state.put("held",g.held);state.put("restraints",v.restraints);state.put("riders",seats(l,g).stream().mapToInt(s->s.getPassengers().size()).sum());groups.add(state);}return "FD904_STATE "+JSON.toJson(Map.of("tick",l.getGameTime(),"running",running,"draining",draining,"vehicles",data.vehicles.size(),"carriers",data.vehicles.values().stream().mapToInt(v->v.carriers.size()).sum(),"rails",data.rails.size(),"forcedChunks",l.getForcedChunks().size(),"groups",groups));}
}
