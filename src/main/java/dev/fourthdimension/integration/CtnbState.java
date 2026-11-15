package dev.fourthdimension.integration;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/** Retained station/reverse dwell; departure permission is never restored automatically. */
public final class CtnbState {
  public BlockPos from,to;
  public String kind="";
  public int remaining;
  public boolean departing;
  long sampled=Long.MIN_VALUE;
  public boolean matches(BlockPos a,BlockPos b){return from!=null && (from.equals(a)&&to.equals(b)||from.equals(b)&&to.equals(a));}
  public CompoundTag save(){var t=new CompoundTag();if(from!=null){t.putLong("from",from.asLong());t.putLong("to",to.asLong());}t.putString("kind",kind);t.putInt("remaining",remaining);return t;}
  public static CtnbState read(CompoundTag t){var s=new CtnbState();if(t.contains("from")){s.from=BlockPos.of(t.getLong("from"));s.to=BlockPos.of(t.getLong("to"));}s.kind=t.getString("kind");s.remaining=t.getInt("remaining");if(s.remaining<0||s.remaining>1200)throw new IllegalArgumentException("Invalid CTNB dwell");return s;}
}
