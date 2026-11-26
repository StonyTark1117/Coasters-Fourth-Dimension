package dev.fourthdimension.moving;

import dev.fourthdimension.contract.Family;
import dev.fourthdimension.contract.NativeTrackFit;
import dev.fourthdimension.contract.TrackContract.Mechanism;
import dev.fourthdimension.track.TrackDesign;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Single server catalog for admission, diagnostics, previews and saved-program validation.
 * This pure value API is NOT a permission boundary: every request field must be obtained by
 * the authenticated native adapter, never trusted from client packets or saved release flags.
 * Catalog permission is a required design pairing, not proof that its native adapter passed.
 */
public final class MovingCapabilityCatalog {
  public static final int VERSION = 1;
  public static final String ORDINARY_PROVIDER = "coasters:ordinary";
  public static final String ORDINARY_VARIANT = "ordinary_n_v1";
  public static final String FD_PROVIDER = "fourthdimension:original";
  public static final double END_MARGIN = 2;
  private static final Set<MovingMechanism> LEVEL = Set.of(MovingMechanism.TRANSFER,MovingMechanism.TURNTABLE,MovingMechanism.ELEVATOR);
  private static final Set<MovingMechanism> ALL = Set.copyOf(EnumSet.allOf(MovingMechanism.class));

  public enum Configuration {
    WING("Wing"), ARROW("Arrow controlled 4D"), LEGACY("Legacy ZacSpin"), FREE_SPIN("S&S Free Spin"),
    REVISED("Revised ZacSpin"), PAX("PAX shuttle"), SPINNING("Classic Spinning (S1)"),
    S2_SPINNING("Launched Inverting Spinning (S2)"), PIPELINE("Seated Pipeline"), DIVE("Dive"),
    SUSPENDED("Suspended swing"), FIXED_INVERTED("Fixed Inverted"), AXIS("Axis"),
    FLYING("Conventional Flying"), NEW_GENERATION_FLYING("New-generation Flying"),
    POWERED_INVERTED_YAW("Powered Inverted Yaw"), ROLLER_BALL("Roller Ball"),
    STANDING_STANDARD("Standard standing"), STANDING_SURF("Surf standing"), MOTORBIKE("Motorbike"),
    FLYING_BIKE("Suspended Flying Bike"), NATIVE_SERVICE("Native ordinary service cart");
    private final String label;
    Configuration(String label){this.label=label;}
    public String label(){return label;}
    public String id(){return name().toLowerCase(java.util.Locale.ROOT);}
    public static Configuration byId(String id){for(var c:values())if(c.id().equals(id))return c;throw new IllegalArgumentException("Unknown moving car configuration: "+id);}
  }

  /** Distinct obligations; chassis lock alone cannot satisfy a child-carrier or route handoff. */
  public enum Hold {
    CHASSIS, ARROW_LOCAL_CONTROL, CARRIER, POSTURE, YAW, SURF_HOME,
    SHUTTLE_ROUTE, ROLLER_BALL_ROUTE, POWERED_YAW_ROUTE, FLYING_BIKE_ROUTE
  }
  public enum Code {
    ALLOWED, UNKNOWN_CONFIGURATION, UNKNOWN_PROVIDER_VERSION, UNSUPPORTED_MOTION, SHALLOW_TILT_ONLY,
    WRONG_INSERT, CONTACT_MISMATCH, MOUNT_MISMATCH, MISSING_INTERFACE, NON_NATIVE_MATERIAL,
    INVALID_DECK, PARTIAL_TRAIN, MIXED_TRAIN, RECOVERY_CART, TOO_MANY_CARS, OVERSIZE_TRAIN,
    EMPTY_SERVICE_ONLY, CUSTOM_NATIVE_CART, CARRIER_NOT_HELD, RESTRAINTS_OPEN, STILL_ROLLING,
    INVALID_ROUTE, DESTINATION_UNAVAILABLE, OBSTRUCTED_SWEEP, PROPULSION_OWNER,
    MANAGED_CIRCUIT, LAUNCH_SYSTEMS
  }
  public record Decision(Code code,String message) {
    public boolean allowed(){return code==Code.ALLOWED;}
  }
  private static final Decision ALLOWED = new Decision(Code.ALLOWED,"Compatible; physical retention and docking guards remain authoritative");

  public record InsertRequirement(String provider,Family family,String variant,String contacts,String mount,
                                  Set<Mechanism> mechanisms) {
    public InsertRequirement { mechanisms=Set.copyOf(mechanisms); }
    public Insert certifiedNativeInsert(){return new Insert(provider,VERSION,family,variant,contacts,mount,mechanisms,true);}
  }
  /** capabilityVersion=1 is issued only after the pinned native 0.1.5/material/body inspection. */
  public record Insert(String provider,int capabilityVersion,Family family,String variant,String contacts,
                       String mount,Set<Mechanism> mechanisms,boolean nativeMaterial) {
    public Insert {
      Objects.requireNonNull(provider);Objects.requireNonNull(variant);Objects.requireNonNull(contacts);Objects.requireNonNull(mount);
      mechanisms=Set.copyOf(mechanisms);
    }
  }
  public record Entry(Configuration configuration,Family family,String variant,InsertRequirement insert,
                      int maxCars,Set<MovingMechanism> motions,double maxTiltRadians,Set<Hold> requiredHolds) {
    public Entry { motions=Set.copyOf(motions);requiredHolds=Set.copyOf(requiredHolds); }
    public Decision permission(MovingMechanism mechanism,double tiltRadians){
      if(mechanism==null || !motions.contains(mechanism))
        return deny(Code.UNSUPPORTED_MOTION,configuration.label()+": level transfer, rotation and lift only in 1.5");
      if(!Double.isFinite(tiltRadians) || tiltRadians<0)
        return deny(Code.UNSUPPORTED_MOTION,"Choose a finite supported pitch program");
      if(tiltRadians>maxTiltRadians+1e-9)
        return configuration==Configuration.SPINNING
            ?deny(Code.SHALLOW_TILT_ONLY,"Classic Spinning: choose 30° tilt")
            :deny(Code.UNSUPPORTED_MOTION,configuration.label()+": this pitch exceeds its moving-track permission");
      return ALLOWED;
    }
  }
  /** Measured occupied bounds include every car, child, rider and coupler, expressed about bed pivot. */
  public record Train(int cars,int passengers,MovingProgram.Bounds occupiedBounds,boolean complete,
                      boolean homogeneous,boolean recoveryAttached,boolean nativeUnmodified) {
    public Train { Objects.requireNonNull(occupiedBounds); }
  }
  public record Readiness(Set<Hold> held,boolean restraintsClosed,boolean stopped,boolean routeValid,
                          boolean destinationLoadedClearReserved,boolean sweepClear,boolean propulsionReleased,
                          boolean managedCircuit,boolean launchSystemsOwner) {
    public Readiness { held=Set.copyOf(held); }
  }
  public record Request(Configuration configuration,MovingProgram program,int deckLength,
                        Insert bed,Insert source,Insert destination,Train train,Readiness readiness) {
    public Request {
      Objects.requireNonNull(program);Objects.requireNonNull(bed);Objects.requireNonNull(source);
      Objects.requireNonNull(destination);Objects.requireNonNull(train);Objects.requireNonNull(readiness);
    }
  }

  private static final Map<Configuration,Entry> ENTRIES=build();
  private MovingCapabilityCatalog(){}
  public static List<Entry> entries(){return List.copyOf(ENTRIES.values());}
  public static Entry entry(Configuration configuration){return Objects.requireNonNull(ENTRIES.get(configuration),"Unknown configuration");}
  /** Null family deliberately does not identify native service: native-body inspection is mandatory. */
  public static Optional<Entry> find(Family family,String variant){
    if(family==null)return Optional.empty();
    return entries().stream().filter(e->e.family==family&&e.variant.equals(variant)).findFirst();
  }
  public static Decision assess(Request request){
    var e=ENTRIES.get(request.configuration);
    if(e==null)return deny(Code.UNKNOWN_CONFIGURATION,"Use a listed original car configuration or inspected empty native service cart");
    var permission=e.permission(request.program.mechanism(),request.program.maxAbsPitch());
    if(!permission.allowed())return permission;
    var ready=request.readiness;
    if(ready.launchSystemsOwner)return deny(Code.LAUNCH_SYSTEMS,"Launch Systems moving-track adapter is not available; release that owner on fixed track");
    if(ready.managedCircuit)return deny(Code.MANAGED_CIRCUIT,"Moving assemblies are unavailable inside the 1.2 managed Arrow circuit; use a standalone route");
    for(var where:List.of(Map.entry("Bed",request.bed),Map.entry("Source dock",request.source),Map.entry("Destination dock",request.destination))){
      var check=checkInsert(e,where.getValue(),where.getKey());if(!check.allowed())return check;
    }
    if(request.deckLength!=16&&request.deckLength!=32&&request.deckLength!=48)
      return deny(Code.INVALID_DECK,"Choose a 16, 32 or 48-block bed");
    var train=request.train;
    if(!train.complete)return deny(Code.PARTIAL_TRAIN,"Capture every linked car, child and coupler on this bed before moving");
    if(!train.homogeneous)return deny(Code.MIXED_TRAIN,"Use one homogeneous train with exactly matching family and variant");
    if(train.recoveryAttached)return deny(Code.RECOVERY_CART,"Detach recovery equipment on fixed track before admission");
    if(train.cars<1||train.cars>e.maxCars)return deny(Code.TOO_MANY_CARS,e.configuration.label()+": maximum "+e.maxCars+" car(s) on a moving bed");
    if(train.passengers<0)return deny(Code.EMPTY_SERVICE_ONLY,"Read actual live passenger count before admission");
    if(e.configuration==Configuration.NATIVE_SERVICE){
      if(train.passengers!=0)return deny(Code.EMPTY_SERVICE_ONLY,"Native ordinary cart: empty service transfer, rotation and lift only");
      if(!train.nativeUnmodified)return deny(Code.CUSTOM_NATIVE_CART,"Use an inspected unmodified Coasters 0.1.5 chassis; custom native contraptions are unsupported");
    }
    if(train.occupiedBounds.minZ() < -request.deckLength/2.0+END_MARGIN
        ||train.occupiedBounds.maxZ()>request.deckLength/2.0-END_MARGIN)
      return deny(Code.OVERSIZE_TRAIN,"Train exceeds "+request.deckLength+"-block bed including two-block end margins; use a larger bed or shorter train");
    if(!ready.held.containsAll(e.requiredHolds)){
      var missing=EnumSet.copyOf(e.requiredHolds);missing.removeAll(ready.held);
      return deny(Code.CARRIER_NOT_HELD,"Secure "+missing+" before moving "+e.configuration.label());
    }
    if(!ready.restraintsClosed)return deny(Code.RESTRAINTS_OPEN,"Close and verify every occupied car restraint on fixed track");
    if(!ready.stopped)return deny(Code.STILL_ROLLING,"Stop the complete train before securing the bed");
    if(!ready.routeValid)return deny(Code.INVALID_ROUTE,"Repair and revalidate this family's destination route/section contract");
    if(!ready.destinationLoadedClearReserved)return deny(Code.DESTINATION_UNAVAILABLE,"Load, clear and exclusively reserve the selected fixed exit");
    if(!ready.sweepClear)return deny(Code.OBSTRUCTED_SWEEP,"Clear the complete bed, train, carrier and rider swept volume");
    if(!ready.propulsionReleased)return deny(Code.PROPULSION_OWNER,"Stop fixed-span propulsion and complete the exclusive mechanism handoff");
    return ALLOWED;
  }

  public static Decision checkInsert(Entry e,Insert actual,String location){
    if(actual.capabilityVersion!=VERSION || (!actual.provider.equals(ORDINARY_PROVIDER)&&!actual.provider.equals(FD_PROVIDER)))
      return deny(Code.UNKNOWN_PROVIDER_VERSION,location+": unknown moving-support provider/version; use the registered pinned native insert");
    if(!actual.nativeMaterial)return deny(Code.NON_NATIVE_MATERIAL,location+": moving beds/docks require native material; CTNB is fixed-approach only");
    var required=e.insert;
    if(!actual.provider.equals(required.provider)||actual.family!=required.family||!actual.variant.equals(required.variant))
      return deny(Code.WRONG_INSERT,location+": "+e.configuration.label()+" requires "+required.variant+" at both docks and the bed");
    if(!actual.contacts.equals(required.contacts))return deny(Code.CONTACT_MISMATCH,location+": "+(e.configuration==Configuration.PIPELINE?"Pipeline needs P rails":"Use matching "+required.contacts));
    if(!actual.mount.equals(required.mount))return deny(Code.MOUNT_MISMATCH,location+": use "+required.mount+" with its full occupied clearance");
    if(!actual.mechanisms.containsAll(required.mechanisms))return deny(Code.MISSING_INTERFACE,location+": preserve required functional rail interfaces "+required.mechanisms);
    return ALLOWED;
  }
  private static Decision deny(Code code,String message){return new Decision(code,message);}

  private static Map<Configuration,Entry> build(){
    var map=new EnumMap<Configuration,Entry>(Configuration.class);
    for(var c:Configuration.values()){
      if(c==Configuration.WING||c==Configuration.NATIVE_SERVICE){
        var insert=new InsertRequirement(ORDINARY_PROVIDER,null,ORDINARY_VARIANT,NativeTrackFit.N_CONTACTS,NativeTrackFit.ABOVE,Set.of());
        map.put(c,new Entry(c,c==Configuration.WING?Family.WING:null,c==Configuration.WING?"wing_fixed_v1":"native_service_v1",insert,
            c==Configuration.WING?2:4,c==Configuration.WING?ALL:LEVEL,c==Configuration.WING?Math.PI/2:0,Set.of(Hold.CHASSIS)));
        continue;
      }
      // Identical enum names intentionally reuse the existing functional IDs; no livery/body-model input.
      var design=TrackDesign.valueOf(c.name());
      boolean advanced=switch(c){case ARROW,SPINNING,S2_SPINNING,PIPELINE,DIVE,FIXED_INVERTED->true;default->false;};
      int cars=switch(c){
        case PIPELINE,AXIS->2;case DIVE->3;
        case ARROW,PAX,S2_SPINNING,SUSPENDED,FIXED_INVERTED,FLYING,NEW_GENERATION_FLYING,POWERED_INVERTED_YAW->4;
        default->1;
      };
      var holds=EnumSet.of(Hold.CHASSIS);
      switch(c){
        case ARROW->holds.add(Hold.ARROW_LOCAL_CONTROL);
        case LEGACY,FREE_SPIN,REVISED,PAX,SPINNING,S2_SPINNING,SUSPENDED,AXIS,ROLLER_BALL->holds.add(Hold.CARRIER);
        case FLYING->holds.add(Hold.POSTURE);
        case NEW_GENERATION_FLYING->{holds.add(Hold.POSTURE);holds.add(Hold.YAW);}
        case POWERED_INVERTED_YAW->holds.add(Hold.YAW);
        case STANDING_SURF->holds.add(Hold.SURF_HOME);
        default->{}
      }
      switch(c){
        case PAX->holds.add(Hold.SHUTTLE_ROUTE);case ROLLER_BALL->holds.add(Hold.ROLLER_BALL_ROUTE);
        case POWERED_INVERTED_YAW->holds.add(Hold.POWERED_YAW_ROUTE);case FLYING_BIKE->holds.add(Hold.FLYING_BIKE_ROUTE);
        default->{}
      }
      // contract() loads specialized native bodies for specialties, so keep this pure catalog's exact
      // semantic set separate and verify equivalence in the native adapter when certifying an insert.
      var semantics=semantics(c);
      var insert=new InsertRequirement(FD_PROVIDER,design.family,design.variant,
          c==Configuration.PIPELINE?"pipeline_p_contacts_v1":NativeTrackFit.N_CONTACTS,
          c==Configuration.PIPELINE?"between_side_rails_v1":design.overhead()?NativeTrackFit.OVERHEAD:NativeTrackFit.ABOVE,semantics);
      map.put(c,new Entry(c,design.family,design.variant,insert,cars,advanced?ALL:LEVEL,
          advanced?(c==Configuration.SPINNING?Math.PI/6:Math.PI/2):0,holds));
    }
    return java.util.Collections.unmodifiableMap(map);
  }
  private static Set<Mechanism> semantics(Configuration c){
    return switch(c){
      case ARROW->Set.of(Mechanism.ROTARY_CLEARANCE,Mechanism.CONTROL_RAILS);
      case FREE_SPIN->Set.of(Mechanism.ROTARY_CLEARANCE,Mechanism.MAGNET_INTERFACE);
      case LEGACY,REVISED,PAX->Set.of(Mechanism.ROTARY_CLEARANCE);
      case SPINNING->Set.of(Mechanism.ROTARY_CLEARANCE,Mechanism.STATION_CAPTURE);
      case S2_SPINNING->Set.of(Mechanism.ROTARY_CLEARANCE,Mechanism.STATION_CAPTURE,Mechanism.MAGNET_INTERFACE,Mechanism.LONGITUDINAL_DRIVE);
      case PIPELINE,DIVE,STANDING_STANDARD,STANDING_SURF,MOTORBIKE->Set.of(Mechanism.STATION_CAPTURE);
      case FIXED_INVERTED->Set.of(Mechanism.STATION_CAPTURE,Mechanism.OVERHEAD_MOUNT);
      case SUSPENDED->Set.of(Mechanism.ROTARY_CLEARANCE,Mechanism.STATION_CAPTURE,Mechanism.OVERHEAD_MOUNT,Mechanism.SWING_LIMITS,Mechanism.LIFT_LOCK);
      case AXIS->Set.of(Mechanism.ROTARY_CLEARANCE,Mechanism.STATION_CAPTURE,Mechanism.ROTATION_DRIVE,Mechanism.LIFT_LOCK);
      case FLYING->Set.of(Mechanism.ROTARY_CLEARANCE,Mechanism.STATION_CAPTURE,Mechanism.OVERHEAD_MOUNT,Mechanism.POSTURE_LOCK);
      case NEW_GENERATION_FLYING->Set.of(Mechanism.ROTARY_CLEARANCE,Mechanism.STATION_CAPTURE,Mechanism.OVERHEAD_MOUNT,Mechanism.POSTURE_LOCK,Mechanism.YAW);
      case POWERED_INVERTED_YAW->Set.of(Mechanism.ROTARY_CLEARANCE,Mechanism.STATION_CAPTURE,Mechanism.OVERHEAD_MOUNT,Mechanism.YAW,Mechanism.LONGITUDINAL_DRIVE);
      case ROLLER_BALL->Set.of(Mechanism.ROTARY_CLEARANCE,Mechanism.STATION_CAPTURE,Mechanism.LIFT_LOCK);
      case FLYING_BIKE->Set.of(Mechanism.STATION_CAPTURE,Mechanism.OVERHEAD_MOUNT,Mechanism.LONGITUDINAL_DRIVE);
      default->Set.of();
    };
  }
}
