package com.moigferdsrte.fluidloggable.mixin.state;

import com.moigferdsrte.fluidloggable.block.FluidloggedBlockStateSupport;
import com.moigferdsrte.fluidloggable.block.LavaloggableBlockSupport;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StateDefinition.Builder.class)
public abstract class StateDefinitionBuilderMixin<O, S extends StateHolder<O, S>> {
	@Inject(method = "add", at = @At("RETURN"))
	private void fluidloggable$addLavaloggedWithWaterlogged(
			final Property<?>[] properties,
			final CallbackInfoReturnable<StateDefinition.Builder<O, S>> cir
	) {
		if (FluidloggedBlockStateSupport.shouldAddLavalogged(properties)) {
			cir.getReturnValue().add(LavaloggableBlockSupport.LAVALOGGED);
		}
	}
}
