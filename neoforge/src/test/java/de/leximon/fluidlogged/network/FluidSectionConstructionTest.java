package de.leximon.fluidlogged.network;

import com.mojang.serialization.Lifecycle;
import de.leximon.fluidlogged.mixin.extensions.LevelChunkSectionExtension;
import io.netty.buffer.Unpooled;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FluidSectionConstructionTest {
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void emptySectionsCanBeInspectedAndSynced(boolean usePalettes) {
        LevelChunkSection original = section(usePalettes);
        // Sable's first full sync calls this before writing the chunk.
        int size = original.getSerializedSize();
        var extension = (LevelChunkSectionExtension) original;
        assertTrue(extension.fluidlogged$getFluidStates().isEmpty());
        assertTrue(original.hasOnlyAir());
        assertSame(Fluids.EMPTY.defaultFluidState(), original.getFluidState(2, 3, 4));
        assertSame(Fluids.EMPTY.defaultFluidState(), extension.fluidlogged$getFluidStateExact(2, 3, 4));

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            original.write(buf);
            assertEquals(size, buf.readableBytes());
            LevelChunkSection decoded = section(!usePalettes);
            var decodedExtension = (LevelChunkSectionExtension) decoded;
            // Reading an empty section must also clear old fluid data.
            decodedExtension.fluidlogged$setFluidState(2, 3, 4, Fluids.WATER.defaultFluidState());
            decoded.read(buf);
            assertFalse(buf.isReadable());
            assertTrue(decoded.hasOnlyAir());
            assertTrue(decodedExtension.fluidlogged$getFluidStates().isEmpty());
        } finally {
            buf.release();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void fluidsSurviveSyncAndCanBeRemoved(boolean usePalettes) {
        LevelChunkSection original = section(usePalettes);
        LevelChunkSection decoded = section(!usePalettes);
        var extension = (LevelChunkSectionExtension) original;
        var decodedExtension = (LevelChunkSectionExtension) decoded;
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            for (Fluid fluid : List.of(Fluids.WATER, Fluids.FLOWING_WATER, Fluids.LAVA,
                    Fluids.FLOWING_LAVA, FluidSyncTest.TestFluids.SOURCE, FluidSyncTest.TestFluids.FLOWING)) {
                for (FluidState state : fluid.getStateDefinition().getPossibleStates()) {
                    assertSame(Fluids.EMPTY.defaultFluidState(),
                            extension.fluidlogged$setFluidState(2, 3, 4, state));
                    assertFalse(original.hasOnlyAir());
                    assertSame(state, original.getFluidState(2, 3, 4));
                    buf.clear();
                    original.write(buf);
                    assertEquals(original.getSerializedSize(), buf.readableBytes());
                    decoded.read(buf);
                    assertFalse(buf.isReadable());
                    assertSame(state, decoded.getFluidState(2, 3, 4));
                    assertSame(state, decodedExtension.fluidlogged$getFluidStateExact(2, 3, 4));
                    assertSame(state, extension.fluidlogged$setFluidState(2, 3, 4, Fluids.EMPTY.defaultFluidState()));
                    assertSame(state, decodedExtension.fluidlogged$setFluidState(2, 3, 4, Fluids.EMPTY.defaultFluidState()));
                    assertTrue(original.hasOnlyAir());
                    assertTrue(decoded.hasOnlyAir());
                    assertSame(Fluids.EMPTY.defaultFluidState(), original.getFluidState(2, 3, 4));
                }
            }
        } finally {
            buf.release();
        }
    }

    private static LevelChunkSection section(boolean usePalettes) {
        var biomes = new MappedRegistry<Biome>(Registries.BIOME, Lifecycle.stable());
        Biome biome = new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.8F).downfall(0.4F)
                .specialEffects(new BiomeSpecialEffects.Builder().fogColor(0).waterColor(0)
                        .waterFogColor(0).skyColor(0).build())
                .mobSpawnSettings(MobSpawnSettings.EMPTY).generationSettings(BiomeGenerationSettings.EMPTY).build();
        Registry.register(biomes, Biomes.PLAINS, biome);
        biomes.freeze();
        if (!usePalettes) {
            return new LevelChunkSection(biomes);
        }
        // Same constructor used by Sable 2.0.5's LevelPlot.newEmptyChunk.
        return new LevelChunkSection(
                new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(),
                        PalettedContainer.Strategy.SECTION_STATES),
                new PalettedContainer<>(biomes.asHolderIdMap(), biomes.getHolderOrThrow(Biomes.PLAINS),
                        PalettedContainer.Strategy.SECTION_BIOMES));
    }
}
