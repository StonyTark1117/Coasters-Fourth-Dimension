package dev.fourthdimension.vehicle;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

/** Actual unwrapped native state and resumable authority for the compound Flying hierarchy. */
public final class YawState {
  public enum Mode { STATION_CAPTURE, PROFILE, TARGET, SHOW_STOP, BLEND, HOLD_FAULT }
  public enum Winding { NEAREST, POSITIVE, NEGATIVE }
  public UUID deck, owner, section, previousSection, marker, command;
  public String markerDimension="", status="READY", stopName="";
  public Mode mode=Mode.STATION_CAPTURE, nextMode=Mode.PROFILE;
  public double angle, velocity, reference, referenceVelocity, lastTarget;
  public double entryAngle, entryVelocity, entryDistance, blendProgress, lastDistance, branch;
  public double postureReference, postureVelocity;
  public int direction=1;
  public long epoch, sequence, tick, sectionRevision, routeFingerprint, showTicks, lastShowTick=-1;
  public boolean initialized, resumeRequired, holdAcknowledged, travelAuthorized;

  public CompoundTag save(){
    var t=new CompoundTag();t.putInt("version",1);t.putInt("transform_order",1);
    t.putDouble("pivot_y",-3.3125);t.putString("axes","chassis_y_then_deck_x");
    put(t,"deck",deck);put(t,"owner",owner);put(t,"section",section);put(t,"previous_section",previousSection);put(t,"marker",marker);put(t,"command",command);
    t.putString("marker_dimension",markerDimension);t.putString("status",status);t.putString("stop_name",stopName);
    t.putString("mode",mode.name());t.putString("next_mode",nextMode.name());
    t.putDouble("angle",angle);t.putDouble("velocity",velocity);t.putDouble("reference",reference);t.putDouble("reference_velocity",referenceVelocity);t.putDouble("last_target",lastTarget);
    t.putDouble("entry_angle",entryAngle);t.putDouble("entry_velocity",entryVelocity);t.putDouble("entry_distance",entryDistance);t.putDouble("blend_progress",blendProgress);t.putDouble("last_distance",lastDistance);t.putDouble("branch",branch);
    t.putDouble("posture_reference",postureReference);t.putDouble("posture_velocity",postureVelocity);
    t.putInt("direction",direction);t.putLong("epoch",epoch);t.putLong("sequence",sequence);t.putLong("tick",tick);t.putLong("section_revision",sectionRevision);t.putLong("route_fingerprint",routeFingerprint);t.putLong("show_ticks",showTicks);t.putLong("last_show_tick",lastShowTick);
    t.putBoolean("initialized",initialized);t.putBoolean("resume_required",resumeRequired);t.putBoolean("hold_acknowledged",holdAcknowledged);t.putBoolean("travel_authorized",travelAuthorized);return t;
  }
  private static void put(CompoundTag t,String k,UUID id){if(id!=null)t.putUUID(k,id);}
  private static UUID id(CompoundTag t,String k){return t.hasUUID(k)?t.getUUID(k):null;}
  public static YawState read(CompoundTag t){
    if(t.getInt("version")!=1||t.getInt("transform_order")!=1||t.getDouble("pivot_y")!=-3.3125||!t.getString("axes").equals("chassis_y_then_deck_x"))throw new IllegalArgumentException("Unknown compound Flying transform");
    var s=new YawState();s.deck=id(t,"deck");s.owner=id(t,"owner");s.section=id(t,"section");s.previousSection=id(t,"previous_section");s.marker=id(t,"marker");s.command=id(t,"command");
    s.markerDimension=t.getString("marker_dimension");s.status=t.getString("status");s.stopName=t.getString("stop_name");s.mode=Mode.valueOf(t.getString("mode"));s.nextMode=Mode.valueOf(t.getString("next_mode"));
    s.angle=t.getDouble("angle");s.velocity=t.getDouble("velocity");s.reference=t.getDouble("reference");s.referenceVelocity=t.getDouble("reference_velocity");s.lastTarget=t.getDouble("last_target");
    s.entryAngle=t.getDouble("entry_angle");s.entryVelocity=t.getDouble("entry_velocity");s.entryDistance=t.getDouble("entry_distance");s.blendProgress=t.getDouble("blend_progress");s.lastDistance=t.getDouble("last_distance");s.branch=t.getDouble("branch");
    s.postureReference=t.getDouble("posture_reference");s.postureVelocity=t.getDouble("posture_velocity");s.direction=t.contains("direction")?t.getInt("direction"):1;s.epoch=t.getLong("epoch");s.sequence=t.getLong("sequence");s.tick=t.getLong("tick");s.sectionRevision=t.getLong("section_revision");s.routeFingerprint=t.getLong("route_fingerprint");s.showTicks=t.getLong("show_ticks");s.lastShowTick=t.getLong("last_show_tick");
    s.initialized=t.getBoolean("initialized");s.resumeRequired=t.getBoolean("resume_required");s.holdAcknowledged=t.getBoolean("hold_acknowledged");s.travelAuthorized=t.getBoolean("travel_authorized");s.validate();return s;
  }
  public void validate(){
    for(double v:new double[]{angle,velocity,reference,referenceVelocity,lastTarget,entryAngle,entryVelocity,entryDistance,blendProgress,lastDistance,branch,postureReference,postureVelocity})if(!Double.isFinite(v))throw new IllegalArgumentException("Nonfinite yaw state");
    if((direction!=1&&direction!=-1)||deck==null||Math.abs(angle)>8*Math.PI+.05||Math.abs(reference)>8*Math.PI+.001||Math.abs(lastTarget)>8*Math.PI+.001||Math.abs(referenceVelocity)>.600001||Math.abs(postureVelocity)>.250001||postureReference< -Math.PI/2-.001||postureReference>.001||blendProgress<0||blendProgress>1||showTicks<0||epoch<0||sequence<0)throw new IllegalArgumentException("Invalid compound Flying state");
  }
}
