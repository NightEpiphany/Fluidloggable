package com.moigferdsrte.fluidloggable.mixin.storage;

import com.moigferdsrte.fluidloggable.block.WaterloggableBlockSupport;
import com.moigferdsrte.fluidloggable.extension.LevelChunkSectionExtension;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunkSection.class)
public class LevelChunkSectionMixin implements LevelChunkSectionExtension {
	@Unique
	private Short2ObjectMap<FluidState> fluidloggable$fluidStates;

	@Override
	public Short2ObjectMap<FluidState> fluidloggable$createAndSetFluidStatesMap() {
		Short2ObjectOpenHashMap<FluidState> map = new Short2ObjectOpenHashMap<>();
		map.defaultReturnValue(Fluids.EMPTY.defaultFluidState());
		this.fluidloggable$fluidStates = map;
		return map;
	}

	@Override
	public Short2ObjectMap<FluidState> fluidloggable$getFluidStates() {
		return this.fluidloggable$fluidStates;
	}

	@Override
	public void fluidloggable$copyFluidStatesFrom(final LevelChunkSectionExtension source) {
		this.fluidloggable$createAndSetFluidStatesMap().putAll(source.fluidloggable$getFluidStates());
	}

	@Override
	public FluidState fluidloggable$setFluidState(final int x, final int y, final int z, final FluidState fluidState) {
		short key = fluidloggable$packLocalPos(x, y, z);
		return fluidState.isEmpty() ? this.fluidloggable$fluidStates.remove(key) : this.fluidloggable$fluidStates.put(key, fluidState);
	}

	@Override
	public FluidState fluidloggable$getFluidStateExact(final int x, final int y, final int z) {
		return this.fluidloggable$fluidStates.get(fluidloggable$packLocalPos(x, y, z));
	}

	@Inject(method = "<init>(Lnet/minecraft/world/level/chunk/PalettedContainer;Lnet/minecraft/world/level/chunk/PalettedContainerRO;)V", at = @At("RETURN"))
	private void fluidloggable$initFromContainers(final CallbackInfo ci) {
		this.fluidloggable$createAndSetFluidStatesMap();
	}

	@Inject(method = "<init>(Lnet/minecraft/world/level/chunk/PalettedContainerFactory;)V", at = @At("RETURN"))
	private void fluidloggable$initFromFactory(final CallbackInfo ci) {
		this.fluidloggable$createAndSetFluidStatesMap();
	}

	@Inject(method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunkSection;)V", at = @At("RETURN"))
	private void fluidloggable$copyFluidStates(final LevelChunkSection source, final CallbackInfo ci) {
		this.fluidloggable$copyFluidStatesFrom((LevelChunkSectionExtension)source);
	}

	@Inject(method = "getFluidState", at = @At("HEAD"), cancellable = true)
	private void fluidloggable$getStoredFluidState(final int x, final int y, final int z, final CallbackInfoReturnable<FluidState> cir) {
		FluidState storedFluid = this.fluidloggable$fluidStates.get(fluidloggable$packLocalPos(x, y, z));
		if (!storedFluid.isEmpty()) {
			cir.setReturnValue(storedFluid);
		}
	}

	@Inject(method = "getFluidState", at = @At("RETURN"), cancellable = true)
	private void fluidloggable$preferVanillaWaterloggedState(final int x, final int y, final int z, final CallbackInfoReturnable<FluidState> cir) {
		if (!cir.getReturnValue().isEmpty()) {
			return;
		}

		BlockState blockState = ((LevelChunkSection)(Object)this).getBlockState(x, y, z);
		if (WaterloggableBlockSupport.isWaterlogged(blockState)) {
			cir.setReturnValue(Fluids.WATER.getSource(false));
		}
	}

	@Inject(method = "hasOnlyAir", at = @At("RETURN"), cancellable = true)
	private void fluidloggable$storedFluidsMakeSectionNonEmpty(final CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue() && !this.fluidloggable$fluidStates.isEmpty()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "hasFluid", at = @At("RETURN"), cancellable = true)
	private void fluidloggable$hasStoredFluid(final CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue() && !this.fluidloggable$fluidStates.isEmpty()) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "isRandomlyTickingFluids", at = @At("RETURN"), cancellable = true)
	private void fluidloggable$storedFluidsCanRandomTick(final CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue()) {
			cir.setReturnValue(this.fluidloggable$fluidStates.values().stream().anyMatch(FluidState::isRandomlyTicking));
		}
	}

	@Inject(method = "getSerializedSize", at = @At("RETURN"), cancellable = true)
	private void fluidloggable$addStoredFluidPacketSize(final CallbackInfoReturnable<Integer> cir) {
		cir.setReturnValue(cir.getReturnValue() + Short.BYTES + this.fluidloggable$fluidStates.size() * (Short.BYTES + Integer.BYTES));
	}

	@Inject(method = "write", at = @At("TAIL"))
	private void fluidloggable$writeStoredFluids(final FriendlyByteBuf buffer, final CallbackInfo ci) {
		buffer.writeShort(this.fluidloggable$fluidStates.size());
		for (Short2ObjectMap.Entry<FluidState> entry : this.fluidloggable$fluidStates.short2ObjectEntrySet()) {
			buffer.writeShort(entry.getShortKey());
			buffer.writeInt(Fluid.FLUID_STATE_REGISTRY.getId(entry.getValue()));
		}
	}

	@Inject(method = "read", at = @At("TAIL"))
	private void fluidloggable$readStoredFluids(final FriendlyByteBuf buffer, final CallbackInfo ci) {
		this.fluidloggable$fluidStates.clear();
		int size = buffer.readShort();
		for (int i = 0; i < size; i++) {
			short key = buffer.readShort();
			FluidState fluidState = Fluid.FLUID_STATE_REGISTRY.byId(buffer.readInt());
			if (fluidState != null && !fluidState.isEmpty()) {
				this.fluidloggable$fluidStates.put(key, fluidState);
			}
		}
	}

	@Unique
	private static short fluidloggable$packLocalPos(final int x, final int y, final int z) {
		return (short)(x << 8 | y << 4 | z);
	}
}
