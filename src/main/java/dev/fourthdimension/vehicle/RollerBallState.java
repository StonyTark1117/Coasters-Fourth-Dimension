package dev.fourthdimension.vehicle;

import dev.fourthdimension.physics.AxisDrive;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/** Native relative tumble and an explicit retained route/lift phase. */
public final class RollerBallState extends AxisDrive {
  public enum Section { DOCK, LIFT, SUMMIT, FREE, RETURN }
  public enum Phase { DOCK_OPEN, SECURED, LIFT_GUIDED, LIFT_LOCKED, SUMMIT_RELEASE, FREE_DESCENT, RETURN_DAMP, DOCK_CAPTURE, HOLD }
  public Phase phase=Phase.DOCK_CAPTURE;
  public BlockPos dockFrom,dockTo;
  public long dockGeometry, routeHash;
  public double pitch, direction;
  public boolean released, pendingRelease, restartHold;
  public CompoundTag save(){
    var t=new CompoundTag();t.putLong("route_hash",routeHash);t.putString("phase",phase.name());t.putDouble("pitch",pitch);t.putDouble("direction",direction);
    t.putDouble("command",command);t.putDouble("rate",commandRate);t.putBoolean("initialized",initialized);
    t.putBoolean("released",released);t.putBoolean("pending_release",pendingRelease);t.putBoolean("restart_hold",restartHold);
    if(dockFrom!=null){t.putLong("dock_from",dockFrom.asLong());t.putLong("dock_to",dockTo.asLong());t.putLong("dock_geometry",dockGeometry);}return t;
  }
  public static RollerBallState read(CompoundTag t){
    var s=new RollerBallState();s.routeHash=t.getLong("route_hash");if(t.contains("phase"))s.phase=Phase.valueOf(t.getString("phase"));
    s.pitch=t.getDouble("pitch");s.direction=t.getDouble("direction");s.command=t.getDouble("command");s.commandRate=t.getDouble("rate");s.initialized=t.getBoolean("initialized");
    s.released=t.getBoolean("released");s.pendingRelease=t.getBoolean("pending_release");s.restartHold=t.getBoolean("restart_hold");
    if(t.contains("dock_from")){s.dockFrom=BlockPos.of(t.getLong("dock_from"));s.dockTo=BlockPos.of(t.getLong("dock_to"));s.dockGeometry=t.getLong("dock_geometry");}
    if(!Double.isFinite(s.pitch)||!Double.isFinite(s.command)||!Double.isFinite(s.commandRate)||Math.abs(s.commandRate)>.35001||!Double.isFinite(s.direction)||Math.abs(s.direction)>1)throw new IllegalArgumentException("Invalid Roller Ball guide state");return s;
  }
}
