package dev.fourthdimension.mixin;

import com.simibubi.create.content.trains.track.BezierConnection;
import dev.fourthdimension.runtime.CurveEdgeKey;
import dev.silvergold.simulatedcoasters.track.CoasterPeerCurveHandleLengths;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = BezierConnection.class, remap = false)
public abstract class BezierCurveKeyMixin implements CurveEdgeKey {
  @Shadow @Final public Couple<BlockPos> bePositions;
  @Unique private volatile CurveEdgeKey.Key fourthdimension$key;

  @Override
  public String fourthdimension$edgeKey() {
    var first = bePositions.getFirst();
    var second = bePositions.getSecond();
    long a = first.asLong(), b = second.asLong();
    long low = Math.min(a, b), high = Math.max(a, b);
    var key = fourthdimension$key;
    if (key == null || key.first() != low || key.second() != high) {
      key = new CurveEdgeKey.Key(low, high,
          CoasterPeerCurveHandleLengths.edgeKeyFromEndpoints(first, second));
      fourthdimension$key = key;
    }
    return key.text();
  }
}
