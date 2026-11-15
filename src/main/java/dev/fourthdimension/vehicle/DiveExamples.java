package dev.fourthdimension.vehicle;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.trains.track.BezierConnection;
import dev.fourthdimension.runtime.*;
import dev.fourthdimension.station.*;
import dev.fourthdimension.track.TrackGeometry;
import dev.silvergold.simulatedcoasters.*;
import dev.silvergold.simulatedcoasters.track.CoasterBezierHandleEdit;
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

/** Original Dive functional loop. Example creation is disclosed setup, not player construction. */
public final class DiveExamples {
  public record Example(List<CoasterPathEdge> edges, FoundationData.Vehicle vehicle, BlockPos station) {}
  private DiveExamples() {}
  private record Pending(List<FoundationData.Vehicle> cars,BlockPos dock,long deadline) {}
  private static final Map<ServerLevel,List<Pending>> PENDING=new WeakHashMap<>();
  /** Example-only coupling waits for the same stopped level-dock guards required by normal Chain use. */
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
        if(!linked)throw new IllegalStateException("Dive example coupling rejected");
      }
      DiveRuntime.rebindDock(level,job.cars.getFirst(),job.dock);FoundationData.get(level).setDirty();FoundationSync.broadcast(level);
    }
  }
  public static Example train(ServerLevel level,BlockPos origin) {
    var example=create(level,origin,true);var cars=new ArrayList<FoundationData.Vehicle>();cars.add(example.vehicle);
    for(int i=1;i<3;i++) {
      var v=DiveVehicle.create(level,sample(level,example.edges.getFirst(),6+8*i));cars.add(v);
      var dock=origin.offset(-5,0,6+8*i);level.setBlockAndUpdate(dock,StationContent.STATION.get().defaultBlockState());
      ((BoardingStationEntity)level.getBlockEntity(dock)).vehicle=v.chassis;v.dock=dock;v.fixedDockFrom=example.edges.getFirst().from();v.fixedDockTo=example.edges.getFirst().to();v.fixedDockGeometry=DiveRuntime.rail(level,v).geometryHash;
    }
    PENDING.computeIfAbsent(level,k->new ArrayList<>()).add(new Pending(List.copyOf(cars),example.station,level.getGameTime()+200));
    return example;
  }

  public static void register(RegisterCommandsEvent event) {
    var demo = Commands.literal("demo");
    for (boolean car : new boolean[] {true, false})
      demo.then(Commands.literal(car ? "dive" : "dive_track").executes(ctx -> {
        try {
          create(ctx.getSource().getLevel(), BlockPos.containing(ctx.getSource().getPosition()).offset(0, 3, 12), car);
          ctx.getSource().sendSuccess(() -> Component.literal(
              "Dive loop built. Board at the level station; close restraints, then Dispatch. "
                  + "Use Stop or power the station to stop the returning car. Open restraints and Shift only after every car stops at the level dock."), false);
          return 1;
        } catch (IllegalArgumentException failure) {
          ctx.getSource().sendFailure(Component.literal(failure.getMessage()));
          return 0;
        }
      }));
    demo.then(Commands.literal("dive_train").executes(ctx->{
      try {train(ctx.getSource().getLevel(),BlockPos.containing(ctx.getSource().getPosition()).offset(0,3,12));
        ctx.getSource().sendSuccess(()->Component.literal("Three-car Dive example built; wait for the eight-block couplers, then board."),false);return 1;
      } catch(IllegalArgumentException failure){ctx.getSource().sendFailure(Component.literal(failure.getMessage()));return 0;}
    }));
    event.getDispatcher().register(Commands.literal("fd").requires(s -> s.hasPermission(2)).then(demo));
  }

  public static Example create(ServerLevel level, BlockPos origin, boolean includeVehicle) {
    return create(level, origin, includeVehicle, true);
  }

  /** Opt in to native lifted endpoints without changing established example geometry. */
  public static Example create(ServerLevel level, BlockPos origin, boolean includeVehicle,
      boolean nativePlacementEndpoints) {
    if (Math.abs((long) origin.getX()) > 850 || Math.abs((long) origin.getZ()) > 850)
      throw new IllegalArgumentException("Choose an example area near world origin");
    for (var pos : BlockPos.betweenClosed(origin.offset(-8,-2,-40),origin.offset(104,100,400)))
      if (!level.getBlockState(pos).isAir())
        throw new IllegalArgumentException("Dive example needs an empty 113 x 103 x 441 area: " + pos);
    int[][] offsets={{0,0,0},{0,0,48},{0,64,144},{0,96,208},{0,96,240},
        {0,72,280},{0,24,308},{0,0,352},{32,0,384},{64,0,384},{96,0,352},
        {96,0,288},{96,0,224},{96,0,160},{96,0,96},{96,0,48},{96,0,0},{64,0,-32},{32,0,-32}};
    Vec3[] tangent=new Vec3[offsets.length];
    for(int i=0;i<tangent.length;i++)tangent[i]=i<=7?new Vec3(0,0,1):i<=9?new Vec3(1,0,0):i<=16?new Vec3(0,0,-1):new Vec3(-1,0,0);
    tangent[2]=new Vec3(0,.55,.8351646544);tangent[5]=tangent[6]=new Vec3(0,-48,28).normalize();
    var anchors = new ArrayList<BlockPos>();
    for (int i = 0; i < offsets.length; i++) {
      var pos = origin.offset(offsets[i][0], offsets[i][1], offsets[i][2]);
      anchors.add(pos);
      level.setBlockAndUpdate(pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());

    }
    var normals = new ArrayList<Vec3>();
    for (int i = 0; i < anchors.size(); i++)
      normals.add(CoasterTrackPlacement.anchorFaceNormalForAxis(level, anchors.get(i), tangent[i]));
    var starts = new ArrayList<Vec3>();
    for (int i = 0; i < anchors.size(); i++)
      // Resolve each hub once, using its authored forward tangent for both incident
      // curves. Native lift follows the banked normal, not a fixed world-Y offset.
      starts.add(nativePlacementEndpoints
          ? CoasterBezierHandleEdit.liftedWorldCenterFromCanonicalTangent(level, anchors.get(i), tangent[i])
          : CoasterAnchorpointBlockEntity.worldCenterBase(level, anchors.get(i)));
    var edges = new ArrayList<CoasterPathEdge>();
    for (int i = 0; i < anchors.size(); i++) {
      int next = (i + 1) % anchors.size();
      edges.add(connect(level, anchors.get(i), anchors.get(next), tangent[i], tangent[next].scale(-1),
          normals.get(i), normals.get(next), starts.get(i), starts.get(next)));
    }
    var data = FoundationData.get(level);
    for (int i=0;i<edges.size();i++) {
      var edge=edges.get(i);
      var length=TrackGeometry.table(level,edge).length();
      var rail=new FoundationData.Rail(edge.from(),edge.to(),dev.fourthdimension.contract.Family.DIVE,
          new dev.fourthdimension.physics.RotationProfile(List.of(
              new dev.fourthdimension.physics.RotationProfile.Key(0,0),
              new dev.fourthdimension.physics.RotationProfile.Key(length,0))),
          UUID.randomUUID(),DiveVehicle.VARIANT,TrackGeometry.fingerprint(level,edge),0,true);
      dev.fourthdimension.track.TrackPlacement.captureOriginalCurve(rail,edge.bezier());

      data.rails.add(rail);
    }
    data.setDirty();
    for (var edge : edges) {
      var reason = DiveTrack.rejection(level, edge);
      if (reason != null) throw new IllegalArgumentException("Dive example geometry: " + reason);
    }
    // One upright 2 b/s Chain source spans crest approach and hold. The descent
    // below the released hold is unpowered; gravity supplies its first motion.
    for (int i = 0; i < anchors.size(); i++) {
      if (i==2||i==5||i==6) continue;
      var anchor = (CoasterAnchorpointBlockEntity) level.getBlockEntity(anchors.get(i));
      var edge = edges.get(i);
      var spanEdges = i == 1 ? edges.subList(1,3) : i == 4 ? edges.subList(4,6) : List.of(edge);
      var path = new CoasterPathGraphSpans.SpanPath(
          spanEdges.stream().map(e -> new CoasterPathGraphSpans.PathSegment(e,0,1)).toList(),
          spanEdges.stream().mapToDouble(e -> TrackGeometry.table(level,e).length()).sum());
      var axis = Math.abs(tangent[i].z) > .5 ? Direction.Axis.X : Direction.Axis.Z;
      anchor.commitChainLift(axis, ChainLiftEndRef.anchor(spanEdges.getLast().to()), path);
      var direction = axis == Direction.Axis.X ? Direction.EAST : (i == 8 || i == 9) ? Direction.NORTH : Direction.SOUTH;
      // Keep full motor cubes outside the vehicle reservation; only native shafts meet the port.
      for(int step=1;step<=3;step++)level.setBlockAndUpdate(anchors.get(i).relative(direction,step),
          AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS,axis));
      var pos = anchors.get(i).relative(direction,4);
      level.setBlockAndUpdate(pos, AllBlocks.CREATIVE_MOTOR.getDefaultState()
          .setValue(BlockStateProperties.FACING, direction.getOpposite()));
      ((CreativeMotorBlockEntity) level.getBlockEntity(pos)).generatedSpeed.setValue(
          (i==3||i==4?16:128) * anchor.getChainLiftTravelSign() * (direction == Direction.NORTH ? -1 : 1));
    }
    FoundationData.Vehicle vehicle = includeVehicle ? DiveVehicle.create(level, sample(level, edges.getFirst(), 6)) : null;
    // Capture must be reachable before lift entry, including a safely stopped
    // returning carrier that has not yet settled its spin.
    var stationPos = origin.offset(-5,0,18);
    level.setBlockAndUpdate(stationPos, StationContent.STATION.get().defaultBlockState());
    var station = (BoardingStationEntity) level.getBlockEntity(stationPos);
    if (vehicle != null) {station.vehicle = vehicle.chassis; vehicle.dock=stationPos;vehicle.fixedDockFrom=edges.getFirst().from();vehicle.fixedDockTo=edges.getFirst().to();vehicle.fixedDockGeometry=DiveRuntime.rail(level,vehicle).geometryHash;}
    station.setChanged();
    for (int side : new int[] {-1, 1})
      for (var p : BlockPos.betweenClosed(origin.offset(side*4,-1,-4),origin.offset(side*7,-1,48)))
        level.setBlockAndUpdate(p, Blocks.SMOOTH_STONE.defaultBlockState());
    var holdPos=anchors.get(5).offset(-5,0,0);
    level.setBlockAndUpdate(holdPos,DiveContent.HOLD.get().defaultBlockState());
    level.setBlockAndUpdate(holdPos.below(),Blocks.REDSTONE_BLOCK.defaultBlockState());
    ((DiveHoldEntity)level.getBlockEntity(holdPos)).bind();
    FoundationSync.broadcast(level);
    return new Example(List.copyOf(edges), vehicle, stationPos);
  }

  public static CoasterPathEdge connect(ServerLevel level, BlockPos from, BlockPos to,
      Vec3 firstTangent, Vec3 secondTangent, Vec3 firstNormal, Vec3 secondNormal) {
    return connect(level, from, to, firstTangent, secondTangent, firstNormal, secondNormal,
        CoasterAnchorpointBlockEntity.worldCenterBase(level,from),
        CoasterAnchorpointBlockEntity.worldCenterBase(level,to));
  }

  public static CoasterPathEdge connect(ServerLevel level, BlockPos from, BlockPos to,
      Vec3 firstTangent, Vec3 secondTangent, Vec3 firstNormal, Vec3 secondNormal,
      Vec3 firstStart, Vec3 secondStart) {
    var curve = new BezierConnection(Couple.create(from,to), Couple.create(firstStart,secondStart),
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
