package dev.fourthdimension.vehicle;
import dev.ryanhcode.sable.api.block.BlockSubLevelCustomCenterOfMass;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.*;
/** Mass token used by NativeLoading; never a second physical body or a placed ballast block. */
public final class SuspendedKeelBlock extends Block implements BlockSubLevelCustomCenterOfMass {
  public SuspendedKeelBlock(Properties properties){super(properties);}
  @Override public Vector3dc getCenterOfMass(BlockGetter level,BlockState state){return new Vector3d(0,.25,.5);}
}
