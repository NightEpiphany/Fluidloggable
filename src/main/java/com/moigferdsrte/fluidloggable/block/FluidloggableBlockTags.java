package com.moigferdsrte.fluidloggable.block;

import com.moigferdsrte.fluidloggable.Fluidloggable;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class FluidloggableBlockTags {
	public static final TagKey<Block> FLUID_PASS_BLOCKS = TagKey.create(
			Registries.BLOCK,
			Fluidloggable.id("fluid_pass_blocks")
	);

	private FluidloggableBlockTags() {
	}
}
