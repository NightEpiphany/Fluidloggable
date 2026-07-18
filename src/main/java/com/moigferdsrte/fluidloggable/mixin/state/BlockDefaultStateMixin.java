package com.moigferdsrte.fluidloggable.mixin.state;

import com.moigferdsrte.fluidloggable.block.FluidloggedBlockStateSupport;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Block.class)
public abstract class BlockDefaultStateMixin {
	@ModifyVariable(method = "registerDefaultState", at = @At("HEAD"), argsOnly = true, name = "state")
	private BlockState fluidloggable$defaultLavaloggedToFalse(final BlockState state) {
		return FluidloggedBlockStateSupport.withDefaultLavaloggedFalse(state);
	}
}
