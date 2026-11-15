package dev.fourthdimension.vehicle;
import dev.ryanhcode.sable.api.block.BlockSubLevelCustomCenterOfMass;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.*;
/** One native mass token exactly one block under the outboard rotary pivot. */
public final class AxisKeelBlock extends Block implements BlockSubLevelCustomCenterOfMass {
  public AxisKeelBlock(Properties p){super(p);}
  @Override public Vector3dc getCenterOfMass(BlockGetter l,BlockState s){return new Vector3d(0,.9,.5);}
}
