package de.leximon.fluidlogged.mixin.classes.network;

import com.llamalad7.mixinextras.sugar.Local;
import de.leximon.fluidlogged.mixin.extensions.ChunkHolderExtension;
import de.leximon.fluidlogged.mixin.extensions.LevelChunkSectionExtension;
import de.leximon.fluidlogged.platform.services.Services;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChunkHolder.class)
public abstract class ChunkHolderMixin implements ChunkHolderExtension {

    @Shadow public abstract @Nullable LevelChunk getTickingChunk();
    @Shadow @Final private LevelHeightAccessor levelHeightAccessor;
    @Shadow private boolean hasChangedSections;

    @Unique private ShortSet[] fluidlogged$changedFluidsPerSection;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void injectInit(ChunkPos chunkPos, int i, LevelHeightAccessor levelHeightAccessor, LevelLightEngine levelLightEngine, ChunkHolder.LevelChangeListener levelChangeListener, ChunkHolder.PlayerProvider playerProvider, CallbackInfo ci) {
        this.fluidlogged$changedFluidsPerSection = new ShortSet[levelHeightAccessor.getSectionsCount()];
    }

    @Override
    public void fluidlogged$fluidChanged(BlockPos blockPos) {
        LevelChunk levelChunk = this.getTickingChunk();
        if (levelChunk != null) {
            int i = this.levelHeightAccessor.getSectionIndex(blockPos.getY());
            if (this.fluidlogged$changedFluidsPerSection[i] == null) {
                this.hasChangedSections = true;
                this.fluidlogged$changedFluidsPerSection[i] = new ShortOpenHashSet();
            }

            this.fluidlogged$changedFluidsPerSection[i].add(SectionPos.sectionRelativePos(blockPos));
        }
    }

    @Inject(
            method = "broadcastChanges",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/level/ChunkHolder;changedBlocksPerSection:[Lit/unimi/dsi/fastutil/shorts/ShortSet;",
                    ordinal = 1,
                    opcode = Opcodes.GETFIELD
            )
    )
    private void injectBroadcastChanges(LevelChunk levelChunk, CallbackInfo ci, @Local List<ServerPlayer> players, @Local int i) {
        ShortSet changedFluids = this.fluidlogged$changedFluidsPerSection[i];
        if (changedFluids == null)
            return;

        this.fluidlogged$changedFluidsPerSection[i] = null;

        if (players.isEmpty())
            return;

        int sectionY = this.levelHeightAccessor.getSectionYFromSectionIndex(i);
        SectionPos sectionPos = SectionPos.of(levelChunk.getPos(), sectionY);
        LevelChunkSection levelChunkSection = levelChunk.getSection(i);

        if (changedFluids.size() == 1) {
            BlockPos blockPos = sectionPos.relativeToBlockPos(changedFluids.iterator().nextShort());
            FluidState fluidState = ((LevelChunkSectionExtension) levelChunkSection).fluidlogged$getFluidStateExact(
                    blockPos.getX() & 15,
                    blockPos.getY() & 15,
                    blockPos.getZ() & 15
            );

            Services.PLATFORM.broadcastFluidUpdatePacket(players, blockPos, fluidState);
            return;
        }

        Services.PLATFORM.broadcastSectionFluidsUpdatePacket(players, sectionPos, changedFluids, levelChunkSection);

    }
}
