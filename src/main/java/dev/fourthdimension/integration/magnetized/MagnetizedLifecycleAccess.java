package dev.fourthdimension.integration.magnetized;

import com.google.gson.JsonObject;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.antopfr.coastersmagnetized.magnet.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** Only called by the opt-in fixture when the exact optional artifact is available. */
public final class MagnetizedLifecycleAccess {
  private MagnetizedLifecycleAccess() {}

  public static void configure(ServerLevel level, BlockPos pos, boolean brake) {
    MagnetizedAnchors.setMode(level, pos, brake ? AnchorMode.BRAKE : AnchorMode.MAGNET);
    var block = (SmartBlockEntity) level.getBlockEntity(pos);
    block.getBehaviour(ScrollValueBehaviour.TYPE).setValue(brake ? 63 : 37);
    block.setChanged();
    block.sendData();
  }

  public static JsonObject inspect(ServerLevel level, BlockPos pos) {
    var result = new JsonObject();
    result.addProperty("mode", MagnetizedAnchors.modeOf(level, pos).name());
    result.addProperty("percent", MagnetScrollValue.percentAt(level, pos, -1));
    return result;
  }
}
