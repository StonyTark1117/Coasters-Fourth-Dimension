package dev.fourthdimension.client.fd917;

import java.util.HashMap;
import java.util.Map;

/** Bounded opt-in CPU-stage observations; not a GPU timer or gameplay authority. */
public final class MechanismRenderMetrics {
  public static final boolean ENABLED=Boolean.getBoolean("fourthdimension.validation");
  private static final Map<String,long[]> TIMES=new HashMap<>();
  private MechanismRenderMetrics() {}
  public static long start(){return ENABLED?System.nanoTime():0;}
  public static void finish(String stage,long start) {
    if(!ENABLED)return;
    var sample=TIMES.computeIfAbsent(stage,k->new long[3]);
    long elapsed=System.nanoTime()-start;sample[0]++;sample[1]+=elapsed;sample[2]=Math.max(sample[2],elapsed);
    if(sample[0]%600==0) {
      com.mojang.logging.LogUtils.getLogger().info("FD917_MECHANISM_COST stage={} frames=600 meanNanos={} peakNanos={}",stage,sample[1]/600,sample[2]);
      sample[1]=sample[2]=0;
    }
  }
}
