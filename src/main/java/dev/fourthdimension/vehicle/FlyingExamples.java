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

/** Original Flying functional loop. Example creation is disclosed setup, not player construction. */
public final class FlyingExamples {
  public record Example(List<CoasterPathEdge> edges, FoundationData.Vehicle vehicle, BlockPos station) {}
  private FlyingExamples() {}
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
        if(!linked)throw new IllegalStateException("Flying example coupling rejected");
      }
      FlyingRuntime.rebindDock(level,job.cars.getFirst(),job.dock);FoundationData.get(level).setDirty();FoundationSync.broadcast(level);
    }
  }
  public static Example train(ServerLevel level,BlockPos origin) {
    var example=create(level,origin,true);var cars=new ArrayList<FoundationData.Vehicle>();cars.add(example.vehicle);
    for(int i=1;i<4;i++) {
      var v=FlyingVehicle.create(level,sample(level,example.edges.getFirst(),6+8*i));cars.add(v);
      var dock=origin.offset(-4,-3,6+8*i);level.setBlockAndUpdate(dock,StationContent.STATION.get().defaultBlockState());
      ((BoardingStationEntity)level.getBlockEntity(dock)).vehicle=v.chassis;v.dock=dock;v.flying.dockFrom=example.edges.getFirst().from();v.flying.dockTo=example.edges.getFirst().to();v.flying.dockGeometry=FlyingRuntime.rail(level,v).geometryHash;
    }
    PENDING.computeIfAbsent(level,k->new ArrayList<>()).add(new Pending(List.copyOf(cars),example.station,level.getGameTime()+200));
    return example;
  }

  public static void register(RegisterCommandsEvent event) {
    var demo = Commands.literal("demo");
    for (boolean car : new boolean[] {true, false})
      demo.then(Commands.literal(car ? "flying" : "flying_track").executes(ctx -> {
        try {
          create(ctx.getSource().getLevel(), BlockPos.containing(ctx.getSource().getPosition()).offset(0, 3, 12), car);
          ctx.getSource().sendSuccess(() -> Component.literal(
              "Flying loop built. Board at the level station; close body and leg restraints, Change to prone, wait for the endpoint lock, then Dispatch. "
                  + "Use Stop or power the station to stop the returning car. Return seated before opening either restraint or using Shift."), false);
          return 1;
        } catch (IllegalArgumentException failure) {
          ctx.getSource().sendFailure(Component.literal(failure.getMessage()));
          return 0;
        }
      }));
    demo.then(Commands.literal("flying_train").executes(ctx->{
      try {train(ctx.getSource().getLevel(),BlockPos.containing(ctx.getSource().getPosition()).offset(0,3,12));
        ctx.getSource().sendSuccess(()->Component.literal("Four-car Flying example built; wait for the eight-block couplers, then board."),false);return 1;
      } catch(IllegalArgumentException failure){ctx.getSource().sendFailure(Component.literal(failure.getMessage()));return 0;}
    }));
    event.getDispatcher().register(Commands.literal("fd").requires(s -> s.hasPermission(2)).then(demo));
  }

  public static Example create(ServerLevel level, BlockPos origin, boolean includeVehicle) {
    return create(level, origin, includeVehicle, false);
  }

  /** Opt in to native lifted endpoints without changing established example geometry. */
  public static Example create(ServerLevel level, BlockPos origin, boolean includeVehicle,
      boolean nativePlacementEndpoints) {
    if (Math.abs((long) origin.getX()) > 850 || Math.abs((long) origin.getZ()) > 850)
      throw new IllegalArgumentException("Choose an example area near world origin");
    for (var pos : BlockPos.betweenClosed(origin.offset(-8,-6,-56),origin.offset(152,32,360)))
      if (!level.getBlockState(pos).isAir())
        throw new IllegalArgumentException("Flying example needs an empty 161 x 39 x 417 area: " + pos);
    int[][] offsets={{0,0,0},{0,0,48},{0,12,96},{0,12,128},{0,12,160},{0,12,192},{0,12,224},
        {0,0,272},{32,0,304},{64,0,304},{96,0,272},{96,0,216},{96,0,160},{96,0,104},
        {96,0,48},{96,0,0},{64,0,-32},{32,0,-32}};
    Vec3[] tangent=new Vec3[offsets.length];
    for(int i=0;i<tangent.length;i++)tangent[i]=i<=7?new Vec3(0,0,1):i<=9?new Vec3(1,0,0):i<=15?new Vec3(0,0,-1):new Vec3(-1,0,0);
    var anchors = new ArrayList<BlockPos>();
    for (int i = 0; i < offsets.length; i++) {
      var pos = origin.offset(offsets[i][0], offsets[i][1], offsets[i][2]);
      anchors.add(pos);
      level.setBlockAndUpdate(pos, SimulatedCoastersBlocks.COASTER_ANCHORPOINT.get().defaultBlockState());
      if (i>=3 && i<=6)
        ((CoasterAnchorpointBlockEntity) level.getBlockEntity(pos)).setJointBankRadians((float) Math.toRadians(90*(i-2)));
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
      var rail=new FoundationData.Rail(edge.from(),edge.to(),dev.fourthdimension.contract.Family.FLYING,
          new dev.fourthdimension.physics.RotationProfile(List.of(
              new dev.fourthdimension.physics.RotationProfile.Key(0,0),
              new dev.fourthdimension.physics.RotationProfile.Key(length,0))),
          UUID.randomUUID(),FlyingVehicle.VARIANT,TrackGeometry.fingerprint(level,edge),0,true);
      dev.fourthdimension.track.TrackPlacement.captureOriginalCurve(rail,edge.bezier());

      data.rails.add(rail);
    }
    data.setDirty();
    for (var edge : edges) {
      var reason = FlyingTrack.rejection(level, edge);
      if (reason != null) throw new IllegalArgumentException("Flying example geometry: " + reason);
    }
    // Power the complete roll from its upright approach: banked-anchor motors would
    // enter either the below-track seat sweep or the overhead running gear.
    for (int i = 0; i < anchors.size(); i++) {
      if (i >= 3 && i <= 5) continue;
      var anchor = (CoasterAnchorpointBlockEntity) level.getBlockEntity(anchors.get(i));
      var edge = edges.get(i);
      var spanEdges = i == 2 ? edges.subList(2,6) : List.of(edge);
      var path = new CoasterPathGraphSpans.SpanPath(
          spanEdges.stream().map(e -> new CoasterPathGraphSpans.PathSegment(e,0,1)).toList(),
          spanEdges.stream().mapToDouble(e -> TrackGeometry.table(level,e).length()).sum());
      var axis = Math.abs(tangent[i].z) > .5 ? Direction.Axis.X : Direction.Axis.Z;
      anchor.commitChainLift(axis, ChainLiftEndRef.anchor(spanEdges.getLast().to()), path);
      // The final quarter-roll sweeps the bearing's upper half east of the port.
      // Feed its upright endpoint from the west so the approaching bearing clears the shaft.
      var direction = i == 6 ? Direction.WEST : axis == Direction.Axis.X ? Direction.EAST
          : (i == 8 || i == 9) ? Direction.NORTH : Direction.SOUTH;
      // Keep full motor cubes outside the vehicle reservation; only native shafts meet the port.
      for(int step=1;step<=2;step++)level.setBlockAndUpdate(anchors.get(i).relative(direction,step),
          AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS,axis));
      var pos = anchors.get(i).relative(direction,3);
      level.setBlockAndUpdate(pos, AllBlocks.CREATIVE_MOTOR.getDefaultState()
          .setValue(BlockStateProperties.FACING, direction.getOpposite()));
      ((CreativeMotorBlockEntity) level.getBlockEntity(pos)).generatedSpeed.setValue(
          (i<=1?64:128) * anchor.getChainLiftTravelSign()
              * (direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE ? -1 : 1));
    }
    FoundationData.Vehicle vehicle = includeVehicle ? FlyingVehicle.create(level, sample(level, edges.getFirst(), 6)) : null;
    // Capture must be reachable before lift entry, including a safely stopped
    // returning carrier that has not yet settled its spin.
    var stationPos = origin.offset(-4,-3,18);
    level.setBlockAndUpdate(stationPos, StationContent.STATION.get().defaultBlockState());
    var station = (BoardingStationEntity) level.getBlockEntity(stationPos);
    if (vehicle != null) {station.vehicle = vehicle.chassis; vehicle.dock=stationPos;vehicle.flying.dockFrom=edges.getFirst().from();vehicle.flying.dockTo=edges.getFirst().to();vehicle.flying.dockGeometry=FlyingRuntime.rail(level,vehicle).geometryHash;}
    station.setChanged();
    for (int side : new int[] {-1, 1})
      for (var p : BlockPos.betweenClosed(origin.offset(side*4,-5,-4),origin.offset(side*7,-5,48)))
        level.setBlockAndUpdate(p, Blocks.SMOOTH_STONE.defaultBlockState());
    FoundationSync.broadcast(level);
    return new Example(List.copyOf(edges), vehicle, stationPos);
  }

  public static CoasterPathEdge connect(ServerLevel level, BlockPos from, BlockPos to,
      Vec3 firstTangent, Vec3 secondTangent, Vec3 firstNormal, Vec3 secondNormal) {
    return connect(level, from, to, firstTangent, secondTangent, firstNormal, secondNormal,
        CoasterAnchorpointBlockEntity.worldCenterBase(level,from),
        CoasterAnchorpointBlockEntity.worldCenterBase(level,to));
  }

  private static CoasterPathEdge connect(ServerLevel level, BlockPos from, BlockPos to,
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
