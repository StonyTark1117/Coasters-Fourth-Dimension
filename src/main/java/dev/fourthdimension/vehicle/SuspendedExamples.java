package dev.fourthdimension.vehicle;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.trains.track.BezierConnection;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.station.*;
import dev.fourthdimension.track.TrackGeometry;
import dev.silvergold.simulatedcoasters.*;
import dev.silvergold.simulatedcoasters.track.CoasterTrackPlacement;
import dev.silvergold.simulatedcoasters.track.anchor.CoasterAnchorpointBlockEntity;
import dev.silvergold.simulatedcoasters.track.chainlift.ChainLiftEndRef;
import dev.silvergold.simulatedcoasters.track.graph.*;
import java.util.*;
import net.createmod.catnip.data.Couple;
import net.minecraft.commands.Commands;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Original Suspended functional loop. Example creation is disclosed setup, not player construction. */
public final class SuspendedExamples {
  public record Example(List<CoasterPathEdge> edges, FoundationData.Vehicle vehicle, BlockPos station) {}
  private SuspendedExamples() {}
  private record Pending(List<FoundationData.Vehicle> cars,BlockPos dock,long deadline) {}
  private static final Map<ServerLevel,List<Pending>> PENDING=new WeakHashMap<>();
  /** Example-only coupling waits for the same physical latches required by normal Wrench use. */
  public static void tick(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event) {
    if(!(event.getLevel() instanceof ServerLevel level))return;
    var pending=PENDING.get(level);if(pending==null)return;
    for(var iterator=pending.iterator();iterator.hasNext();) {
      var job=iterator.next();
      if(level.getGameTime()>job.deadline){iterator.remove();continue;}
      if(job.cars.stream().anyMatch(v->!v.boardingReady || !Boarding.ready(level,v)))continue;
      iterator.remove();var container=dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(level);
      for(int i=0;i<job.cars.size()-1;i++) {
        boolean linked=dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkConstraint.linkCarts(level,
            (dev.ryanhcode.sable.sublevel.ServerSubLevel)container.getSubLevel(job.cars.get(i).chassis),
            (dev.ryanhcode.sable.sublevel.ServerSubLevel)container.getSubLevel(job.cars.get(i+1).chassis),8);
        if(!linked)throw new IllegalStateException("Suspended example coupling rejected");
      }
      job.cars.forEach(v->v.dock=job.dock);FoundationData.get(level).setDirty();FoundationSync.broadcast(level);
    }
  }
  public static Example train(ServerLevel level,BlockPos origin) {
    var example=create(level,origin,true);var cars=new ArrayList<FoundationData.Vehicle>();cars.add(example.vehicle);
    for(int i=1;i<4;i++) {
      var v=SuspendedVehicle.create(level,sample(level,example.edges.getFirst(),6+8*i));cars.add(v);
      var dock=origin.offset(-4,-3,30+8*i);level.setBlockAndUpdate(dock,StationContent.STATION.get().defaultBlockState());
      ((BoardingStationEntity)level.getBlockEntity(dock)).vehicle=v.chassis;v.dock=dock;
    }
    PENDING.computeIfAbsent(level,k->new ArrayList<>()).add(new Pending(List.copyOf(cars),example.station,level.getGameTime()+200));
    return example;
  }

  public static void register(RegisterCommandsEvent event) {
    var demo = Commands.literal("demo");
    for (boolean car : new boolean[] {true, false})
      demo.then(Commands.literal(car ? "suspended" : "suspended_track").executes(ctx -> {
        try {
          create(ctx.getSource().getLevel(), BlockPos.containing(ctx.getSource().getPosition()).offset(0, 3, 12), car);
          ctx.getSource().sendSuccess(() -> Component.literal(
              "Suspended loop built. Board at the level station; close restraints and Dispatch. "
                  + "Use Capture or power the station to stop the returning car. Open restraints only after the dock latch settles."), false);
          return 1;
        } catch (IllegalArgumentException failure) {
          ctx.getSource().sendFailure(Component.literal(failure.getMessage()));
          return 0;
        }
      }));
    demo.then(Commands.literal("suspended_train").executes(ctx->{
      try {train(ctx.getSource().getLevel(),BlockPos.containing(ctx.getSource().getPosition()).offset(0,3,12));
        ctx.getSource().sendSuccess(()->Component.literal("Four-car Suspended example built; wait for the guarded eight-block couplers, then board."),false);return 1;
      } catch(IllegalArgumentException failure){ctx.getSource().sendFailure(Component.literal(failure.getMessage()));return 0;}
    }));
    event.getDispatcher().register(Commands.literal("fd").requires(s -> s.hasPermission(2)).then(demo));
  }

  public static Example create(ServerLevel level, BlockPos origin, boolean includeVehicle) {
    if (Math.abs((long) origin.getX()) > 850 || Math.abs((long) origin.getZ()) > 850)
      throw new IllegalArgumentException("Choose an example area near world origin");
    for (var pos : BlockPos.betweenClosed(origin.offset(-8, -6, -16), origin.offset(104, 16, 144)))
      if (!level.getBlockState(pos).isAir())
        throw new IllegalArgumentException("Suspended example needs an empty 113 x 23 x 161 area: " + pos);
    int[][] offsets = {{0,0,24},{0,0,64},{0,8,104},{32,8,136},{64,8,136},{96,8,104},{96,0,64},{96,0,24},{64,0,-8},{32,0,-8}};
    Vec3[] tangent = {new Vec3(0,0,1),new Vec3(0,0,1),new Vec3(0,0,1),new Vec3(1,0,0),new Vec3(1,0,0),
        new Vec3(0,0,-1),new Vec3(0,0,-1),new Vec3(0,0,-1),new Vec3(-1,0,0),new Vec3(-1,0,0)};
    var anchors = new ArrayList<BlockPos>();
    for (int i = 0; i < offsets.length; i++) {
      var pos = origin.offset(offsets[i][0], offsets[i][1], offsets[i][2]);
      anchors.add(pos);
      level.setBlockAndUpdate(pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      if (i == 3 || i == 4)
        ((CoasterAnchorpointBlockEntity) level.getBlockEntity(pos)).setJointBankRadians((float) Math.toRadians(29));
    }
    var normals = new ArrayList<Vec3>();
    for (int i = 0; i < anchors.size(); i++)
      normals.add(CoasterTrackPlacement.anchorFaceNormalForAxis(level, anchors.get(i), tangent[i]));
    var edges = new ArrayList<CoasterPathEdge>();
    for (int i = 0; i < anchors.size(); i++) {
      int next = (i + 1) % anchors.size();
      edges.add(connect(level, anchors.get(i), anchors.get(next), tangent[i], tangent[next].scale(-1),
          normals.get(i), normals.get(next)));
    }
    var data = FoundationData.get(level);
    for (int i=0;i<edges.size();i++) {
      var edge=edges.get(i);
      var length=TrackGeometry.table(level,edge).length();
      var rail=new FoundationData.Rail(edge.from(),edge.to(),dev.fourthdimension.contract.Family.SUSPENDED,
          new dev.fourthdimension.physics.RotationProfile(List.of(
              new dev.fourthdimension.physics.RotationProfile.Key(0,0),
              new dev.fourthdimension.physics.RotationProfile.Key(length,0))),
          UUID.randomUUID(),SuspendedVehicle.VARIANT,TrackGeometry.fingerprint(level,edge),0,true);
      dev.fourthdimension.track.TrackPlacement.captureOriginalCurve(rail,edge.bezier());
      rail.liftLock=i==1;
      data.rails.add(rail);
    }
    data.setDirty();
    for (var edge : edges) {
      var reason = SuspendedTrack.rejection(level, edge);
      if (reason != null) throw new IllegalArgumentException("Suspended example geometry: " + reason);
    }
    for (int i = 0; i < anchors.size(); i++) {
      var anchor = (CoasterAnchorpointBlockEntity) level.getBlockEntity(anchors.get(i));
      var edge = edges.get(i);
      var path = new CoasterPathGraphSpans.SpanPath(
          List.of(new CoasterPathGraphSpans.PathSegment(edge,0,1)), TrackGeometry.table(level,edge).length());
      var axis = Math.abs(tangent[i].z) > .5 ? Direction.Axis.X : Direction.Axis.Z;
      anchor.commitChainLift(axis, ChainLiftEndRef.anchor(edge.to()), path);
      var direction = axis == Direction.Axis.X ? Direction.EAST : (i == 3 || i == 4) ? Direction.NORTH : Direction.SOUTH;
      var pos = anchors.get(i).relative(direction);
      level.setBlockAndUpdate(pos, AllBlocks.CREATIVE_MOTOR.getDefaultState()
          .setValue(BlockStateProperties.FACING, direction.getOpposite()));
      ((CreativeMotorBlockEntity) level.getBlockEntity(pos)).generatedSpeed.setValue(
          (i<=1?32:64) * anchor.getChainLiftTravelSign() * (direction == Direction.NORTH ? -1 : 1));
    }
    FoundationData.Vehicle vehicle = includeVehicle ? SuspendedVehicle.create(level, sample(level, edges.getFirst(), 6)) : null;
    // Capture must be reachable before lift entry, including a safely stopped
    // returning carrier that has not yet settled its spin.
    var stationPos = origin.offset(-4, -3, 30);
    level.setBlockAndUpdate(stationPos, StationContent.STATION.get().defaultBlockState());
    var station = (BoardingStationEntity) level.getBlockEntity(stationPos);
    if (vehicle != null) {station.vehicle = vehicle.chassis; vehicle.dock=stationPos;}
    station.setChanged();
    for (int side : new int[] {-1, 1})
      for (var p : BlockPos.betweenClosed(origin.offset(side*4,-5,20),origin.offset(side*7,-5,66)))
        level.setBlockAndUpdate(p, Blocks.SMOOTH_STONE.defaultBlockState());
    FoundationSync.broadcast(level);
    return new Example(List.copyOf(edges), vehicle, stationPos);
  }

  public static CoasterPathEdge connect(ServerLevel level, BlockPos from, BlockPos to,
      Vec3 firstTangent, Vec3 secondTangent, Vec3 firstNormal, Vec3 secondNormal) {
    var curve = new BezierConnection(Couple.create(from,to), Couple.create(
        CoasterAnchorpointBlockEntity.worldCenterBase(level,from),
        CoasterAnchorpointBlockEntity.worldCenterBase(level,to)),
        Couple.create(firstTangent,secondTangent), Couple.create(firstNormal,secondNormal),
        true,false,CoasterTrackMaterials.COASTER);
    ((CoasterAnchorpointBlockEntity) level.getBlockEntity(from)).putAnchorPeerCurve(level,to,curve);
    return CoasterPathEdge.curve(from,to,curve);
  }

  public static CoasterPathTrackFrame.GraphHit sample(ServerLevel level, CoasterPathEdge edge, double distance) {
    double t = TrackGeometry.table(level,edge).parameterAt(distance);
    return new CoasterPathTrackFrame.GraphHit(CoasterPathGraphManager.sampleEdgePosition(level,edge,t),edge,t);
  }
}
