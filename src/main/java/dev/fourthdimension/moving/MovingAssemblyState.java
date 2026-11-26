package dev.fourthdimension.moving;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import org.joml.Quaterniond;
import org.joml.Vector3d;

/** Saved physical identities and interrupted pose. A saved phase never grants release permission. */
public final class MovingAssemblyState {
  public static final int VERSION = 1;
  public enum Phase {
    DOCKED, ADMITTING, CAPTURED, RESERVED, DISCONNECTED, MOVING,
    ALIGNING, LOCKED, RECONNECTED, RELEASING, FAULT_HOLD
  }
  public record Token(UUID assembly, long epoch, long sequence) {}
  /** Immutable rigid transform; no reference to a mutable native pose escapes into saved state. */
  public record Pose(double x, double y, double z, double qx, double qy, double qz, double qw) {
    public static final Pose IDENTITY = new Pose(0, 0, 0, 0, 0, 0, 1);
    public Pose {
      double norm = qx*qx + qy*qy + qz*qz + qw*qw;
      if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
          || !Double.isFinite(norm) || Math.abs(norm - 1) > .001)
        throw new IllegalArgumentException("Invalid moving-support rigid pose");
    }
    public Vector3d position() { return new Vector3d(x,y,z); }
    public Quaterniond orientation() { return new Quaterniond(qx,qy,qz,qw); }
    CompoundTag save() {
      var t = new CompoundTag(); t.putDouble("x",x); t.putDouble("y",y); t.putDouble("z",z);
      t.putDouble("qx",qx); t.putDouble("qy",qy); t.putDouble("qz",qz); t.putDouble("qw",qw); return t;
    }
    static Pose read(CompoundTag t) {
      return new Pose(t.getDouble("x"),t.getDouble("y"),t.getDouble("z"),
          t.getDouble("qx"),t.getDouble("qy"),t.getDouble("qz"),t.getDouble("qw"));
    }
  }
  /** Socket identity plus receiving rail span; reservations compare spans, not player-facing names. */
  public record Dock(String name, UUID socket, BlockPos from, BlockPos to, Pose pose) {
    public Dock {
      if (name == null || name.isBlank() || name.length() > 40 || socket == null || pose == null
          || from == null || to == null || from.equals(to))
        throw new IllegalArgumentException("Invalid moving-track dock");
      from = from.immutable(); to = to.immutable();
    }
  }

  public final UUID id;
  public final BlockPos controller;
  public UUID bed;
  public BlockPos railFrom, railTo;
  public String mechanism, variant;
  public String configuration = "wing";
  public MovingProgramSpec program;
  public Pose origin = Pose.IDENTITY;
  public boolean returning;
  public final int deckLength;
  public final Map<String, Dock> docks = new LinkedHashMap<>();
  public final Map<BlockPos,String> fixedParts = new LinkedHashMap<>();
  public final Map<String,Integer> materials = new LinkedHashMap<>();
  public boolean released;
  public MovingServiceState service=new MovingServiceState();
  public double receivingLength;
  public MovingProgram.Bounds receivingEnvelope=new MovingProgram.Bounds(-3,-5,0,3,5,0);
  /** Complete chassis identities, separately from their child/carrier bodies. */
  public List<UUID> cars = List.of();
  public final Map<UUID, Pose> bodies = new LinkedHashMap<>();
  /** Capture-time plot-local constraint anchors. Missing legacy anchors never authorize recovery. */
  public final Map<UUID, Vector3d> bodyAnchors = new LinkedHashMap<>();
  public Pose actualPose = Pose.IDENTITY;
  public Vector3d linearVelocity = new Vector3d(), angularVelocity = new Vector3d();
  public String source = "", destination = "", fault = "";
  public Phase phase = Phase.DOCKED;
  public long epoch = 1, sequence;
  public double programSeconds;
  public boolean restored, locksEngaged;
  /** A reload hint only: native pose, sockets and all structural parts must authenticate it. */
  public String restoreDock = "";
  public Phase restoreStationaryPhase;

  public MovingAssemblyState(UUID id, BlockPos controller, String mechanism, String variant, int length) {
    this.id = Objects.requireNonNull(id); this.controller = Objects.requireNonNull(controller).immutable();
    this.mechanism = identifier(mechanism); this.variant = identifier(variant);
    if (length != 16 && length != 32 && length != 48) throw new IllegalArgumentException("Choose a 16, 32 or 48 block bed");
    deckLength = length;
  }
  private static String identifier(String value) {
    if (value == null || value.isBlank() || value.length() > 80 || !value.matches("[a-z0-9_:./-]+"))
      throw new IllegalArgumentException("Invalid moving-support identifier");
    return value;
  }
  public Token token() { return new Token(id,epoch,sequence); }
  public void check(Token expected) {
    if (!token().equals(expected)) throw new IllegalArgumentException("Moving-track control changed; refresh the panel");
  }
  public boolean retained() { return !bodies.isEmpty(); }
  public void fault(String reason) {
    if (phase != Phase.FAULT_HOLD || !Objects.equals(fault,reason)) { epoch++; sequence++; }
    phase = Phase.FAULT_HOLD; fault = Objects.requireNonNull(reason); locksEngaged = false;
  }
  public CompoundTag save(boolean disk) {
    validate(); var t = new CompoundTag(); t.putInt("version",VERSION); t.putUUID("id",id);
    t.putLong("controller",controller.asLong()); if(bed!=null)t.putUUID("bed",bed);
    if(railFrom!=null){t.putLong("rail_from",railFrom.asLong());t.putLong("rail_to",railTo.asLong());}
    t.putString("mechanism",mechanism); t.putString("variant",variant); t.putInt("length",deckLength);
    t.putString("phase",phase.name()); t.putString("source",source); t.putString("destination",destination);
    t.putString("fault",fault); t.putLong("epoch",epoch); t.putLong("sequence",sequence);
    t.putDouble("program_seconds",programSeconds); t.putBoolean("locks",locksEngaged);
    t.putString("configuration",configuration);t.put("origin",origin.save());t.putBoolean("returning",returning);
    if(program!=null){var pt=new CompoundTag();pt.putInt("version",MovingProgramSpec.VERSION);pt.putString("mechanism",program.mechanism().id());
      pt.putInt("amount",program.amount());pt.putInt("from",program.fromPort());pt.putInt("to",program.toPort());pt.putBoolean("show",program.fullShowTurn());t.put("program",pt);}
    t.put("service",service.save(disk));t.putBoolean("released",released);t.putDouble("receiving_length",receivingLength);
    var envelope=new CompoundTag();envelope.putDouble("min_x",receivingEnvelope.minX());envelope.putDouble("min_y",receivingEnvelope.minY());envelope.putDouble("min_z",receivingEnvelope.minZ());
    envelope.putDouble("max_x",receivingEnvelope.maxX());envelope.putDouble("max_y",receivingEnvelope.maxY());envelope.putDouble("max_z",receivingEnvelope.maxZ());t.put("receiving_envelope",envelope);
    var bill=new CompoundTag();materials.forEach(bill::putInt);t.put("materials",bill);
    t.putBoolean("restore",disk); t.put("pose",actualPose.save());
    t.put("linear",vector(linearVelocity)); t.put("angular",vector(angularVelocity));
    var ds=new ListTag(); for(var d:docks.values()) {
      var dt=new CompoundTag();dt.putString("name",d.name());dt.putUUID("socket",d.socket());
      dt.putLong("from",d.from().asLong());dt.putLong("to",d.to().asLong());dt.put("pose",d.pose().save());ds.add(dt);
    } t.put("docks",ds);
    var parts=new ListTag();for(var entry:fixedParts.entrySet()){var part=new CompoundTag();part.putLong("pos",entry.getKey().asLong());part.putString("block",entry.getValue());parts.add(part);}t.put("fixed_parts",parts);
    var cs=new ListTag();for(var c:cars)cs.add(NbtUtils.createUUID(c));t.put("cars",cs);
    var bs=new ListTag();for(var b:bodies.entrySet()){
      var bt=b.getValue().save();bt.putUUID("id",b.getKey());
      var anchor=bodyAnchors.get(b.getKey());if(anchor!=null)bt.put("anchor",vector(anchor));bs.add(bt);
    }t.put("bodies",bs);return t;
  }
  public static MovingAssemblyState read(CompoundTag t) {
    if(t.getInt("version")!=VERSION)throw new IllegalArgumentException("Unsupported moving-support save version");
    var s=new MovingAssemblyState(t.getUUID("id"),BlockPos.of(t.getLong("controller")),
        t.getString("mechanism"),t.getString("variant"),t.getInt("length"));
    if(t.hasUUID("bed"))s.bed=t.getUUID("bed");
    if(t.contains("rail_from")){s.railFrom=BlockPos.of(t.getLong("rail_from"));s.railTo=BlockPos.of(t.getLong("rail_to"));}
    s.service=MovingServiceState.read(t.getCompound("service"));s.released=t.getBoolean("released");s.receivingLength=t.getDouble("receiving_length");
    if(t.contains("receiving_envelope")){var e=t.getCompound("receiving_envelope");s.receivingEnvelope=new MovingProgram.Bounds(e.getDouble("min_x"),e.getDouble("min_y"),e.getDouble("min_z"),e.getDouble("max_x"),e.getDouble("max_y"),e.getDouble("max_z"));}
    var bill=t.getCompound("materials");if(bill.getAllKeys().size()>64)throw new IllegalArgumentException("Material bill capacity exceeded");
    for(var key:bill.getAllKeys()){identifier(key);int count=bill.getInt(key);if(count<1||count>8192)throw new IllegalArgumentException("Invalid paid material count");s.materials.put(key,count);}
    s.phase=Phase.valueOf(t.getString("phase"));s.source=t.getString("source");s.destination=t.getString("destination");
    s.fault=t.getString("fault");s.epoch=t.getLong("epoch");s.sequence=t.getLong("sequence");
    s.programSeconds=t.getDouble("program_seconds");s.locksEngaged=t.getBoolean("locks");
    s.configuration=t.getString("configuration");s.origin=Pose.read(t.getCompound("origin"));s.returning=t.getBoolean("returning");
    if(t.contains("program",Tag.TAG_COMPOUND)){var pt=t.getCompound("program");
      if(pt.getInt("version")!=MovingProgramSpec.VERSION)throw new IllegalArgumentException("Unknown moving program version");
      s.program=new MovingProgramSpec(MovingMechanism.byId(pt.getString("mechanism")),pt.getInt("amount"),pt.getInt("from"),pt.getInt("to"),pt.getBoolean("show"));}
    s.actualPose=Pose.read(t.getCompound("pose"));s.linearVelocity=readVector(t.getCompound("linear"));
    s.angularVelocity=readVector(t.getCompound("angular"));
    var ds=t.getList("docks",Tag.TAG_COMPOUND);var cs=t.getList("cars",Tag.TAG_INT_ARRAY);
    var bs=t.getList("bodies",Tag.TAG_COMPOUND);
    if(ds.size()>4||cs.size()>4||bs.size()>128)throw new IllegalArgumentException("Moving-support capacity exceeded");
    for(var raw:ds){var d=(CompoundTag)raw;var dock=new Dock(d.getString("name"),d.getUUID("socket"),
        BlockPos.of(d.getLong("from")),BlockPos.of(d.getLong("to")),Pose.read(d.getCompound("pose")));
      if(s.docks.putIfAbsent(dock.name(),dock)!=null)throw new IllegalArgumentException("Duplicate dock name");}
    var parts=t.getList("fixed_parts",Tag.TAG_COMPOUND);if(parts.size()>4096)throw new IllegalArgumentException("Moving structural part capacity exceeded");
    for(var raw:parts){var part=(CompoundTag)raw;var pos=BlockPos.of(part.getLong("pos"));var block=identifier(part.getString("block"));
      if(s.fixedParts.putIfAbsent(pos,block)!=null)throw new IllegalArgumentException("Duplicate moving structural part");}
    var cars=new ArrayList<UUID>();for(var raw:cs)cars.add(NbtUtils.loadUUID(raw));s.cars=List.copyOf(cars);
    for(var raw:bs){var b=(CompoundTag)raw;var id=b.getUUID("id");
      if(s.bodies.putIfAbsent(id,Pose.read(b))!=null)throw new IllegalArgumentException("Duplicate retained body");
      if(b.contains("anchor")){
        if(!b.contains("anchor",Tag.TAG_COMPOUND))throw new IllegalArgumentException("Invalid retained body anchor");
        var anchor=b.getCompound("anchor");
        if(!anchor.contains("x",Tag.TAG_ANY_NUMERIC)||!anchor.contains("y",Tag.TAG_ANY_NUMERIC)
            ||!anchor.contains("z",Tag.TAG_ANY_NUMERIC))throw new IllegalArgumentException("Incomplete retained body anchor");
        s.bodyAnchors.put(id,readVector(anchor));
      }
    }
    s.validate();
    if(t.getBoolean("restore")){
      if(!s.retained()&&!s.released&&s.service.cars.isEmpty()&&s.locksEngaged
          &&(s.phase==Phase.DOCKED||s.phase==Phase.RECONNECTED)){
        s.restoreStationaryPhase=s.phase;
        s.restoreDock=s.phase==Phase.DOCKED||s.returning?s.source:s.destination;
      }
      s.restored=true;s.fault("Restored moving assembly: revalidate and explicitly Resume");
    }
    return s;
  }
  private void validate() {
    identifier(mechanism);identifier(variant);identifier(configuration);
    if(program!=null&&!program.mechanism().id().equals(mechanism))throw new IllegalArgumentException("Moving controller and program disagree");
    if(!cars.containsAll(service.cars.keySet())||!service.cars.isEmpty()&&!docks.containsKey(service.dock)
        ||!Double.isFinite(receivingLength)||receivingLength<0||materials.size()>64
        ||materials.entrySet().stream().anyMatch(e->e.getValue()<1||e.getValue()>8192)||epoch<1||sequence<0||!Double.isFinite(programSeconds)||programSeconds<0||docks.size()>4
        ||cars.size()>4||new HashSet<>(cars).size()!=cars.size()||bodies.size()>128
        ||(!released&&!bodies.keySet().containsAll(cars))||released&&!bodies.isEmpty()||!bodies.isEmpty()&&(bed==null||cars.isEmpty())
        ||(railFrom==null)!=(railTo==null)||bed!=null&&bodies.containsKey(bed)
        ||!source.isEmpty()&&!docks.containsKey(source)||!destination.isEmpty()&&!docks.containsKey(destination)
        ||source.length()>40||destination.length()>40||fault.length()>512)
      throw new IllegalArgumentException("Invalid moving-support saved state");
    service.validate(this);
    if(!bodies.keySet().containsAll(bodyAnchors.keySet()))throw new IllegalArgumentException("Orphan retained body anchor");
    for(var anchor:bodyAnchors.values()){
      if(anchor==null||!anchor.isFinite())throw new IllegalArgumentException("Invalid retained body anchor");
    }
    vector(linearVelocity);vector(angularVelocity);
  }
  private static CompoundTag vector(Vector3d v) {
    if(!v.isFinite())throw new IllegalArgumentException("Invalid moving-support velocity");
    var t=new CompoundTag();t.putDouble("x",v.x);t.putDouble("y",v.y);t.putDouble("z",v.z);return t;
  }
  private static Vector3d readVector(CompoundTag t) {
    var v=new Vector3d(t.getDouble("x"),t.getDouble("y"),t.getDouble("z"));vector(v);return v;
  }
}
