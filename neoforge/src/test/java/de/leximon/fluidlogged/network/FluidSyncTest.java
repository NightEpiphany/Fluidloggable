package de.leximon.fluidlogged.network;

import com.mojang.serialization.Lifecycle;
import de.leximon.fluidlogged.Fluidlogged;
import de.leximon.fluidlogged.mixin.extensions.LevelChunkSectionExtension;
import de.leximon.fluidlogged.platform.ForgePlatformHelper;
import de.leximon.fluidlogged.platform.services.Services;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.shorts.ShortArraySet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FluidSyncTest {
    // Loaded only by the test runtime. Like fertiliser, these are registered
    // after the vanilla fluid-state table has already been populated.
    @EventBusSubscriber(modid = Fluidlogged.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class TestFluids {
        static final FluidType TYPE = new FluidType(FluidType.Properties.create());
        static final BaseFlowingFluid.Properties PROPERTIES = new BaseFlowingFluid.Properties(
                () -> TYPE, () -> TestFluids.SOURCE, () -> TestFluids.FLOWING);
        static Fluid SOURCE;
        static Fluid FLOWING;

        @SubscribeEvent
        public static void register(RegisterEvent event) {
            event.register(NeoForgeRegistries.Keys.FLUID_TYPES, helper ->
                    helper.register(id("sync_test"), TYPE));
            event.register(Registries.FLUID, helper -> {
                SOURCE = new BaseFlowingFluid.Source(PROPERTIES);
                FLOWING = new BaseFlowingFluid.Flowing(PROPERTIES);
                helper.register(id("sync_test"), SOURCE);
                helper.register(id("flowing_sync_test"), FLOWING);
            });
        }

        private static ResourceLocation id(String path) {
            return ResourceLocation.fromNamespaceAndPath(Fluidlogged.MOD_ID, path);
        }
    }

    private static List<FluidState> states() {
        List<FluidState> states = new ArrayList<>();
        for (Fluid fluid : List.of(Fluids.EMPTY, Fluids.WATER, Fluids.FLOWING_WATER,
                Fluids.LAVA, Fluids.FLOWING_LAVA, TestFluids.SOURCE, TestFluids.FLOWING)) {
            states.addAll(fluid.getStateDefinition().getPossibleStates());
        }
        return states;
    }

    @Test
    void registeredModdedStatesAreMappedAndVanillaIdsArePreserved() {
        assertSame(TestFluids.SOURCE, BuiltInRegistries.FLUID.get(TestFluids.id("sync_test")));
        assertEquals(-1, Fluid.FLUID_STATE_REGISTRY.getId(TestFluids.SOURCE.defaultFluidState()),
                "The fixture must reproduce the missing vanilla ID");
        var mapper = Services.PLATFORM.getFluidStateIdMapper();
        for (FluidState state : states()) {
            int id = mapper.getIdOrThrow(state);
            assertSame(state, mapper.byIdOrThrow(id));
            if (Fluid.FLUID_STATE_REGISTRY.getId(state) != -1) {
                assertEquals(Fluid.FLUID_STATE_REGISTRY.getId(state), id);
            }
        }
        assertEquals(0, mapper.getIdOrThrow(Fluids.EMPTY.defaultFluidState()));
    }

    @Test
    void singleUpdatesRoundTripEverySourceFlowingAndFallingState() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            BlockPos pos = new BlockPos(-17, 70, 33);
            for (FluidState state : states()) {
                buf.clear();
                var packet = new ClientboundFluidUpdatePacket(pos, state);
                ClientboundFluidUpdatePacket.STREAM_CODEC.encode(buf, packet);
                var decoded = ClientboundFluidUpdatePacket.STREAM_CODEC.decode(buf);
                assertEquals(pos, decoded.pos());
                assertSame(state, decoded.state());
                assertFalse(buf.isReadable());
            }
        } finally {
            buf.release();
        }
    }

    @Test
    void groupedUpdatesUseTheSameIdsForEncodingAndDecoding() {
        LevelChunkSection section = section();
        var extension = (LevelChunkSectionExtension) section;
        var positions = new ShortArraySet();
        var states = states();
        for (int i = 0; i < states.size(); i++) {
            BlockPos pos = new BlockPos(i & 15, i >> 4, 3);
            extension.fluidlogged$setFluidState(pos.getX(), pos.getY(), pos.getZ(), states.get(i));
            positions.add(SectionPos.sectionRelativePos(pos));
        }
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        FriendlyByteBuf encodedAgain = new FriendlyByteBuf(Unpooled.buffer());
        try {
            var sectionPos = SectionPos.of(-2, 4, 7);
            var packet = new ClientboundSectionFluidsUpdatePacket(sectionPos, positions, section);
            ClientboundSectionFluidsUpdatePacket.STREAM_CODEC.encode(buf, packet);
            assertEquals(sectionPos.asLong(), buf.readLong());
            assertEquals(states.size(), buf.readVarInt());
            for (short pos : positions) {
                long entry = buf.readVarLong();
                assertEquals(pos, entry & 0xFFF);
                FluidState expected = section.getFluidState(SectionPos.sectionRelativeX(pos),
                        SectionPos.sectionRelativeY(pos), SectionPos.sectionRelativeZ(pos));
                assertSame(expected, Services.PLATFORM.getFluidStateIdMapper().byIdOrThrow((int) (entry >>> 12)));
            }
            buf.readerIndex(0);
            var decoded = ClientboundSectionFluidsUpdatePacket.STREAM_CODEC.decode(buf);
            assertFalse(buf.isReadable());
            ClientboundSectionFluidsUpdatePacket.STREAM_CODEC.encode(encodedAgain, decoded);
            buf.readerIndex(0);
            assertEquals(buf, encodedAgain);
        } finally {
            buf.release();
            encodedAgain.release();
        }
    }

    @Test
    void initialChunkSyncRetainsModdedStatesAndReportsTheCorrectSize() {
        LevelChunkSection original = section();
        var extension = (LevelChunkSectionExtension) original;
        var states = states();
        for (int i = 0; i < states.size(); i++) {
            extension.fluidlogged$setFluidState(i & 15, i >> 4, 3, states.get(i));
        }
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            original.write(buf);
            assertEquals(original.getSerializedSize(), buf.readableBytes());
            LevelChunkSection decoded = section();
            decoded.read(buf);
            assertFalse(buf.isReadable());
            for (int i = 0; i < states.size(); i++) {
                assertSame(states.get(i), decoded.getFluidState(i & 15, i >> 4, 3));
            }
        } finally {
            buf.release();
        }
    }

    @Test
    void rebuildingTheMapDoesNotChangeIds() {
        var mapper = Services.PLATFORM.getFluidStateIdMapper();
        var before = states().stream().map(mapper::getIdOrThrow).toList();
        ForgePlatformHelper.initializeFluidStateIdMapper();
        assertEquals(before, states().stream().map(Services.PLATFORM.getFluidStateIdMapper()::getIdOrThrow).toList());
    }

    @Test
    void unknownIdsAreRejectedInsteadOfBecomingNullFluidStates() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buf.writeBlockPos(BlockPos.ZERO);
            buf.writeVarInt(-1);
            assertThrows(IllegalArgumentException.class, () -> ClientboundFluidUpdatePacket.STREAM_CODEC.decode(buf));
        } finally {
            buf.release();
        }
    }

    private static LevelChunkSection section() {
        var biomes = new MappedRegistry<Biome>(Registries.BIOME, Lifecycle.stable());
        Biome biome = new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.8F).downfall(0.4F)
                .specialEffects(new BiomeSpecialEffects.Builder().fogColor(0).waterColor(0)
                        .waterFogColor(0).skyColor(0).build())
                .mobSpawnSettings(MobSpawnSettings.EMPTY).generationSettings(BiomeGenerationSettings.EMPTY).build();
        Registry.register(biomes, Biomes.PLAINS, biome);
        biomes.freeze();
        return new LevelChunkSection(biomes);
    }
}
