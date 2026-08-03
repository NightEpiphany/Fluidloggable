package com.moigferdsrte.fluidloggable.block;

import com.moigferdsrte.fluidloggable.Fluidloggable;
import com.moigferdsrte.fluidloggable.config.FluidloggableConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

/** Matches dynamic fluid containers configured by mod id or exact block id. */
public final class ConfiguredFluidloggableBlockSupport {
	private static volatile Selection selection;

	private ConfiguredFluidloggableBlockSupport() {
	}

	public static boolean isConfigured(final BlockState state, final BlockGetter level, final BlockPos pos) {
		return isConfigured(state) && shapeValidation(state, level, pos);
	}

	private static boolean shapeValidation(final BlockState state, final BlockGetter level, final BlockPos pos) {
		return !state.isCollisionShapeFullBlock(level, pos)
				|| FluidloggableConfig.DEFAULT_FLUIDLOGGABLE_BLOCK_IDS.stream().anyMatch(id -> BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().equals(id))
				|| FluidloggableConfig.DEFAULT_FLUIDLOGGABLE_MOD_IDS.stream().anyMatch(id -> BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace().equals(id));
	}

	public static boolean isConfigured(final BlockState state) {
		return state.hasProperty(WaterloggableBlockSupport.WATERLOGGED)
				&& state.hasProperty(LavaloggableBlockSupport.LAVALOGGED)
				&& matchesConfiguration(state.getBlock());
	}

	public static boolean matchesConfiguration(final Block block) {
		Selection current = selection;
		if (current == null) {
			current = refresh();
		}

		final Identifier blockId = block.properties().id == null
				? BuiltInRegistries.BLOCK.getKey(block)
				: block.properties().id.identifier();
		return current.modIds().contains(blockId.getNamespace()) || current.blockIds().contains(blockId);
	}

	public static void invalidate() {
		selection = null;
	}

	private static synchronized Selection refresh() {
		if (selection != null) {
			return selection;
		}

		final Set<String> modIds = new HashSet<>();
		for (String value : FluidloggableConfig.getFluidloggableModIds()) {
			final String modId = value.trim();
			if (Identifier.isValidNamespace(modId)) {
				modIds.add(modId);
			} else {
				Fluidloggable.LOGGER.warn("Ignoring invalid fluidloggable mod id in config: {}", value);
			}
		}

		final Set<Identifier> blockIds = new HashSet<>();
		for (String value : FluidloggableConfig.getFluidloggableBlockIds()) {
			final Identifier blockId = Identifier.tryParse(value.trim());
			if (blockId != null) {
				blockIds.add(blockId);
			} else {
				Fluidloggable.LOGGER.warn("Ignoring invalid fluidloggable block id in config: {}", value);
			}
		}

		selection = new Selection(Set.copyOf(modIds), Set.copyOf(blockIds));
		return selection;
	}

	private record Selection(Set<String> modIds, Set<Identifier> blockIds) {
	}
}
