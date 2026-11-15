package dev.fourthdimension.mixin;

import org.spongepowered.asm.mixin.Mixin;

/** Runs after the pinned optional mixins so their merged force callbacks can be fenced. */
@Mixin(targets={"dev.silvergold.simulatedcoasters.track.cart.CoasterCartChainLiftDrive",
    "dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrackSnap"},priority=1,remap=false)
public abstract class OptionalDriveFenceMixin {}
