package com.moigferdsrte.fluidloggable.mixin.client;

import com.moigferdsrte.fluidloggable.extension.SectionCopyExtension;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCopy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderSectionRegion.class)
public abstract class RenderSectionRegionMixin {
	@Shadow
	@Final
	private int minSectionX;

	@Shadow
	@Final
	private int minSectionY;

	@Shadow
	@Final
	private int minSectionZ;

	@Shadow
	@Final
	private ClientLevel level;

	@Shadow
	private SectionCopy getSection(final int sectionX, final int sectionY, final int sectionZ) {
		throw new AssertionError();
	}

	@Inject(method = "getFluidState", at = @At("HEAD"), cancellable = true)
	private void fluidloggable$getStoredFluidState(final BlockPos pos, final CallbackInfoReturnable<FluidState> cir) {
		int sectionX = SectionPos.blockToSectionCoord(pos.getX());
		int sectionY = SectionPos.blockToSectionCoord(pos.getY());
		int sectionZ = SectionPos.blockToSectionCoord(pos.getZ());
		if (!this.fluidloggable$isInCopiedRegion(sectionX, sectionY, sectionZ)) {
			cir.setReturnValue(this.level.getFluidState(pos));
			return;
		}

		SectionCopy section = this.getSection(sectionX, sectionY, sectionZ);
		cir.setReturnValue(((SectionCopyExtension)section).fluidloggable$getFluidState(pos));
	}

	@Unique
    private boolean fluidloggable$isInCopiedRegion(final int sectionX, final int sectionY, final int sectionZ) {
		return sectionX >= this.minSectionX
			&& sectionX < this.minSectionX + RenderSectionRegion.SIZE
			&& sectionY >= this.minSectionY
			&& sectionY < this.minSectionY + RenderSectionRegion.SIZE
			&& sectionZ >= this.minSectionZ
			&& sectionZ < this.minSectionZ + RenderSectionRegion.SIZE;
	}
}
