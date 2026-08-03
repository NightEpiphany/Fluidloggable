package com.moigferdsrte.fluidloggable.mixin.state;

import com.moigferdsrte.fluidloggable.block.FluidloggedBlockStateSupport;
import com.moigferdsrte.fluidloggable.block.ConfiguredFluidloggableBlockSupport;
import com.moigferdsrte.fluidloggable.block.LavaloggableBlockSupport;
import com.moigferdsrte.fluidloggable.block.WaterloggableBlockSupport;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Function;

@Mixin(StateDefinition.Builder.class)
public abstract class StateDefinitionBuilderMixin<O, S extends StateHolder<O, S>> {
	@Shadow
	@Final
	private O owner;

	@Shadow
	@Final
	public Map<String, Property<?>> properties;

	@Inject(method = "create", at = @At("HEAD"))
	private void fluidloggable$addConfiguredFluidProperties(
			final Function<O, S> stateFactory,
			final StateDefinition.Factory<O, S> factory,
			final CallbackInfoReturnable<StateDefinition<O, S>> cir
	) {
		if (!(owner instanceof Block block) || !ConfiguredFluidloggableBlockSupport.matchesConfiguration(block)) {
			return;
		}

		if (!properties.containsKey(WaterloggableBlockSupport.WATERLOGGED.getName())) {
			properties.put(WaterloggableBlockSupport.WATERLOGGED.getName(), WaterloggableBlockSupport.WATERLOGGED);
		}
		if (!properties.containsKey(LavaloggableBlockSupport.LAVALOGGED.getName())) {
			properties.put(LavaloggableBlockSupport.LAVALOGGED.getName(), LavaloggableBlockSupport.LAVALOGGED);
		}
	}

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
