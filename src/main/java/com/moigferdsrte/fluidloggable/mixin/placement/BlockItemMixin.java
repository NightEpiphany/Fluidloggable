package com.moigferdsrte.fluidloggable.mixin.placement;

import com.moigferdsrte.fluidloggable.Fluidloggable;
import com.moigferdsrte.fluidloggable.block.WaterloggableBlockSupport;
import com.moigferdsrte.fluidloggable.extension.LevelExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
	@Shadow
	protected abstract boolean placeBlock(BlockPlaceContext context, BlockState placementState);

	@Redirect(
		method = "place",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BlockItem;placeBlock(Lnet/minecraft/world/item/context/BlockPlaceContext;Lnet/minecraft/world/level/block/state/BlockState;)Z")
	)
	private boolean fluidloggable$preserveFluidWhenPlacingBlock(final BlockItem item, final BlockPlaceContext context, final BlockState placementState) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		FluidState previousFluid = level.getFluidState(pos);
		if (!this.placeBlock(context, placementState)) {
			return false;
		}

		BlockState placedState = level.getBlockState(pos);
		if (previousFluid.is(FluidTags.WATER) && WaterloggableBlockSupport.canStoreWater(placedState)) {
			((LevelExtension)level).fluidloggable$setFluid(pos, previousFluid, Block.UPDATE_ALL | Fluidloggable.UPDATE_SCHEDULE_FLUID_TICK);
		}
		return true;
	}
}
