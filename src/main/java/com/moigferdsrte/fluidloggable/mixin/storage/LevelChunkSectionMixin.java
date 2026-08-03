package com.moigferdsrte.fluidloggable.mixin.storage;

import com.moigferdsrte.fluidloggable.block.FluidloggedBlockStateSupport;
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
	public synchronized Short2ObjectMap<FluidState> fluidloggable$createAndSetFluidStatesMap() {
		Short2ObjectOpenHashMap<FluidState> map = new Short2ObjectOpenHashMap<>();
		map.defaultReturnValue(Fluids.EMPTY.defaultFluidState());
		this.fluidloggable$fluidStates = map;
		return map;
	}

	@Override
	public synchronized Short2ObjectMap<FluidState> fluidloggable$copyFluidStates() {
		Short2ObjectOpenHashMap<FluidState> copy = new Short2ObjectOpenHashMap<>();
		for (Short2ObjectMap.Entry<FluidState> entry : this.fluidloggable$fluidStates.short2ObjectEntrySet()) {
			copy.put(entry.getShortKey(), entry.getValue());
		}
		copy.defaultReturnValue(Fluids.EMPTY.defaultFluidState());
		return copy;
	}

	@Override
	public synchronized void fluidloggable$copyFluidStatesFrom(final LevelChunkSectionExtension source) {
		this.fluidloggable$fluidStates = source.fluidloggable$copyFluidStates();
	}

	@Override
	public synchronized FluidState fluidloggable$setFluidState(final int x, final int y, final int z, final FluidState fluidState) {
		short key = fluidloggable$packLocalPos(x, y, z);
		return fluidState.isEmpty() ? this.fluidloggable$fluidStates.remove(key) : this.fluidloggable$fluidStates.put(key, fluidState);
	}

	@Override
	public synchronized FluidState fluidloggable$getFluidStateExact(final int x, final int y, final int z) {
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
	private synchronized void fluidloggable$getStoredFluidState(final int sectionX, final int sectionY, final int sectionZ, final CallbackInfoReturnable<FluidState> cir) {
		final short key = fluidloggable$packLocalPos(sectionX, sectionY, sectionZ);
		FluidState storedFluid = this.fluidloggable$fluidStates.get(key);
		if (!storedFluid.isEmpty()) {
			BlockState blockState = ((LevelChunkSection)(Object)this).getBlockState(sectionX, sectionY, sectionZ);
			if (FluidloggedBlockStateSupport.canStoreFluid(blockState, storedFluid.getType())) {
				cir.setReturnValue(storedFluid);
			}
		}
	}

	@Inject(method = "getFluidState", at = @At("RETURN"), cancellable = true)
	private void fluidloggable$preferVanillaWaterloggedState(final int sectionX, final int sectionY, final int sectionZ, final CallbackInfoReturnable<FluidState> cir) {
		if (!cir.getReturnValue().isEmpty()) {
			return;
		}

		BlockState blockState = ((LevelChunkSection)(Object)this).getBlockState(sectionX, sectionY, sectionZ);
		if (WaterloggableBlockSupport.isWaterlogged(blockState)) {
			cir.setReturnValue(Fluids.WATER.getSource(false));
		}
	}

	@Inject(method = "hasOnlyAir", at = @At("RETURN"), cancellable = true)
	private synchronized void fluidloggable$storedFluidsMakeSectionNonEmpty(final CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue() && !this.fluidloggable$fluidStates.isEmpty()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "hasFluid", at = @At("RETURN"), cancellable = true)
	private synchronized void fluidloggable$hasStoredFluid(final CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue() && !this.fluidloggable$fluidStates.isEmpty()) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "isRandomlyTickingFluids", at = @At("RETURN"), cancellable = true)
	private synchronized void fluidloggable$storedFluidsCanRandomTick(final CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue()) {
			cir.setReturnValue(this.fluidloggable$fluidStates.values().stream().anyMatch(FluidState::isRandomlyTicking));
		}
	}

	@Inject(method = "getSerializedSize", at = @At("RETURN"), cancellable = true)
	private synchronized void fluidloggable$addStoredFluidPacketSize(final CallbackInfoReturnable<Integer> cir) {
		cir.setReturnValue(cir.getReturnValue() + Short.BYTES + this.fluidloggable$fluidStates.size() * (Short.BYTES + Integer.BYTES));
	}

	@Inject(method = "write", at = @At("TAIL"))
	private void fluidloggable$writeStoredFluids(final FriendlyByteBuf buffer, final CallbackInfo ci) {
		Short2ObjectMap<FluidState> fluidStates = this.fluidloggable$copyFluidStates();
		buffer.writeShort(fluidStates.size());
		for (Short2ObjectMap.Entry<FluidState> entry : fluidStates.short2ObjectEntrySet()) {
			buffer.writeShort(entry.getShortKey());
			buffer.writeInt(Fluid.FLUID_STATE_REGISTRY.getId(entry.getValue()));
		}
	}

	@Inject(method = "read", at = @At("TAIL"))
	private synchronized void fluidloggable$readStoredFluids(final FriendlyByteBuf buffer, final CallbackInfo ci) {
		this.fluidloggable$fluidStates.clear();
		int size = buffer.readShort();
		for (int i = 0; i < size; i++) {
			short key = buffer.readShort();
			FluidState fluidState = Fluid.FLUID_STATE_REGISTRY.byId(buffer.readInt());
			if (fluidState != null && !fluidState.isEmpty()) {
				this.fluidloggable$fluidStates.put(key, fluidState);
				this.fluidloggable$syncStoredFluidProperty(key, fluidState);
			}
		}
	}

	@Unique
	private void fluidloggable$syncStoredFluidProperty(final short packedPos, final FluidState fluidState) {
		final int x = packedPos >> 8 & 15;
		final int y = packedPos >> 4 & 15;
		final int z = packedPos & 15;
		final LevelChunkSection section = (LevelChunkSection) (Object) this;
		final BlockState state = section.getBlockState(x, y, z);
		final BlockState synced = FluidloggedBlockStateSupport.withFluid(state, fluidState);
		if (synced != state) {
			section.setBlockState(x, y, z, synced);
		}
	}

	@Unique
	private static short fluidloggable$packLocalPos(final int x, final int y, final int z) {
		return (short)(x << 8 | y << 4 | z);
	}
}
