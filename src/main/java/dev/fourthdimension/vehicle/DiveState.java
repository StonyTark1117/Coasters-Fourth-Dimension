package dev.fourthdimension.vehicle;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
/** Server tick dwell and committed release survive reload independently of the runtime constraint. */
public final class DiveState {
  public enum Phase { DOCK_OPEN, SECURED, RUNNING, APPROACH, HOLD_ACQUIRING, HELD, RELEASE_ARMED, DROP, BRAKING, DOCK_CLOSED, OPEN }
  public Phase phase=Phase.DOCK_OPEN;
  public BlockPos controller,from,to;
  public long geometry,releaseEpoch;
  public int remaining=60,stoppedTicks;
  public boolean needsResume,restored;
  public CompoundTag save(){var t=new CompoundTag();t.putInt("version",1);t.putString("phase",phase.name());
    if(controller!=null)t.putLong("controller",controller.asLong());
    if(from!=null){t.putLong("from",from.asLong());t.putLong("to",to.asLong());}
    t.putLong("geometry",geometry);t.putLong("release_epoch",releaseEpoch);t.putInt("remaining",remaining);
    t.putBoolean("needs_resume",needsResume);return t;}
  public static DiveState load(CompoundTag t){
    if(t.getInt("version")!=1)throw new IllegalArgumentException("Unsupported Dive state version");
    var s=new DiveState();s.phase=Phase.valueOf(t.getString("phase"));
    if(t.contains("controller"))s.controller=BlockPos.of(t.getLong("controller"));
    if(t.contains("from")){s.from=BlockPos.of(t.getLong("from"));s.to=BlockPos.of(t.getLong("to"));}
    s.geometry=t.getLong("geometry");s.releaseEpoch=t.getLong("release_epoch");s.remaining=t.getInt("remaining");
    if(s.remaining<0||s.remaining>200)throw new IllegalArgumentException("Invalid Dive dwell");
    s.needsResume=t.getBoolean("needs_resume");s.restored=true;return s;
  }
}
