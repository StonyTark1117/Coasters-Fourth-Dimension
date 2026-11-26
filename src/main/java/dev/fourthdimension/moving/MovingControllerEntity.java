package dev.fourthdimension.moving;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Stationary Create input only. MovingRuntime owns the assembly, locks, leases and motion. */
public final class MovingControllerEntity extends KineticBlockEntity {
  public MovingControllerEntity(BlockPos pos,BlockState state){super(MovingContent.CONTROLLER_ENTITY.get(),pos,state);}
  public String mechanismId(){return ((MovingControllerBlock)getBlockState().getBlock()).mechanism().id();}
  public double inputRpm(){return isOverStressed()?0:Math.abs(getSpeed());}
  public boolean powered(){return inputRpm()>=16;}
  @Override public float calculateStressApplied(){return lastStressApplied=16;}
}
