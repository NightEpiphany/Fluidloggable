package de.leximon.fluidlogged;

import de.leximon.fluidlogged.api.FluidloggedRegistries;
import de.leximon.fluidlogged.config.Addon;
import de.leximon.fluidlogged.config.Config;
import de.leximon.fluidlogged.custom.CustomAdditionsTag;
import de.leximon.fluidlogged.mixin.extensions.LevelExtension;
import de.leximon.fluidlogged.platform.services.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

public class Fluidlogged {

    public static final String MOD_ID = "fluidlogged";

    public static final int UPDATE_SCHEDULE_FLUID_TICK = 0x80;

    public static final Config CONFIG = new Config();

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static int getFluidId(@Nullable FluidState fluidState) {
        if (fluidState == null) {
            return 0;
        } else {
            int i = Services.PLATFORM.getFluidStateIdMapper().getId(fluidState);
            return i == -1 ? 0 : i;
        }
    }

    public static boolean canPlaceFluid(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
        if (blockState.getBlock() instanceof LiquidBlockContainer container
                && container.canPlaceLiquid(null, blockGetter, blockPos, blockState, fluid))
            return true;
        return CONFIG.isFluidloggable(blockState);
    }

    public static boolean isFluidloggable(BlockState blockState) {
        if (blockState.getBlock() instanceof LiquidBlockContainer)
            return true;
        return CONFIG.isFluidloggable(blockState);
    }

    public static boolean isFluidPermeable(BlockState blockState) {
        if (!CONFIG.isFluidPermeabilityEnabled())
            return false;
        return CONFIG.isFluidPermeable(blockState) || CONFIG.isShapeIndependentFluidPermeable(blockState);
    }

    public static boolean isShapeIndependentFluidPermeable(BlockState blockState) {
        if (!CONFIG.isFluidPermeabilityEnabled())
            return false;
        return CONFIG.isShapeIndependentFluidPermeable(blockState);
    }

    @ApiStatus.Internal
    public static class Internal {

        public static void initialize() {
            FluidloggedRegistries.register(
                    FluidloggedRegistries.ADDONS, id("mod_defaults"),
                    Addon.builder()
                            .enabledByDefault(true)
                            .fluidloggableBlocks(() -> {
                                Collection<Block> res = new ArrayList<>();
                                for (var i : BuiltInRegistries.BLOCK.entrySet()) {
                                    Class<? extends BlockBehaviour> aClass = i.getValue().getClass();
                                    if (
                                            aClass == Block.class ||
                                                    aClass == LiquidBlock.class ||
                                                    aClass == RotatedPillarBlock.class ||
                                                    aClass == StairBlock.class ||
                                                    aClass == SlabBlock.class ||
                                                    aClass == WallBlock.class ||
                                                    aClass == FenceBlock.class ||
                                                    i.getValue() instanceof GrassBlock ||
                                                    i.getValue() instanceof SnowyDirtBlock ||
                                                    i.getValue() instanceof SimpleWaterloggedBlock
                                    ) continue;
                                    res.add(i.getValue());
                                }
                                return res;
                            }).fluidloggableBlockTags(CustomAdditionsTag.FLUIDLOGGABLE, BlockTags.FENCE_GATES)
//                            .fluidloggableBlocks(
//                                    Blocks.STONECUTTER,
//                                    Blocks.GRINDSTONE,
//                                    Blocks.LECTERN,
//                                    Blocks.BREWING_STAND,
//                                    Blocks.ENCHANTING_TABLE,
//                                    Blocks.BELL,
//                                    Blocks.COMPOSTER,
//                                    Blocks.SNIFFER_EGG,
//                                    Blocks.TURTLE_EGG,
//                                    Blocks.CAKE,
//                                    Blocks.LEVER,
//                                    Blocks.VINE,
//                                    Blocks.DRAGON_EGG,
//                                    Blocks.END_PORTAL_FRAME,
//                                    Blocks.DAYLIGHT_DETECTOR,
//                                    Blocks.HOPPER,
//                                    Blocks.PISTON_HEAD,
//                                    Blocks.BARRIER,
//                                    Blocks.END_ROD,
//                                    Blocks.END_PORTAL,
//                                    Blocks.BAMBOO,
//                                    Blocks.AZALEA,
//                                    Blocks.MOSS_CARPET,
//                                    Blocks.PLAYER_HEAD,
//                                    Blocks.PLAYER_WALL_HEAD,
//                                    Blocks.CREEPER_HEAD,
//                                    Blocks.CREEPER_WALL_HEAD,
//                                    Blocks.DRAGON_HEAD,
//                                    Blocks.DRAGON_WALL_HEAD,
//                                    Blocks.PIGLIN_HEAD,
//                                    Blocks.PIGLIN_WALL_HEAD,
//                                    Blocks.SKELETON_SKULL,
//                                    Blocks.SKELETON_WALL_SKULL,
//                                    Blocks.WITHER_SKELETON_SKULL,
//                                    Blocks.WITHER_SKELETON_WALL_SKULL,
//                                    Blocks.ZOMBIE_HEAD,
//                                    Blocks.ZOMBIE_WALL_HEAD,
//
//                                    Blocks.PINK_PETALS,
//                                    Blocks.CHORUS_FLOWER,
//                                    Blocks.CHORUS_PLANT,
//                                    Blocks.COCOA,
//                                    Blocks.SPAWNER,
//                                    Blocks.TRIAL_SPAWNER,
//                                    Blocks.VAULT,
//                                    Blocks.BEACON,
//                                    Blocks.TRIPWIRE,
//                                    Blocks.TRIPWIRE_HOOK,
//                                    Blocks.PISTON_HEAD,
//                                    Blocks.STICKY_PISTON,
//                                    Blocks.PISTON,
//                                    Blocks.MOVING_PISTON,
//                                    Blocks.REPEATER,
//                                    Blocks.COMPARATOR,
//                                    Blocks.REDSTONE_TORCH,
//                                    Blocks.REDSTONE_WALL_TORCH,
//                                    Blocks.REDSTONE_WIRE,
//                                    Blocks.SUGAR_CANE,
//                                    Blocks.SNOW,
//                                    Blocks.POWDER_SNOW,
//                                    Blocks.CACTUS,
//                                    Blocks.CAULDRON,
//                                    Blocks.LAVA_CAULDRON,
//                                    Blocks.WATER_CAULDRON,
//                                    Blocks.POWDER_SNOW_CAULDRON,
//                                    Blocks.LILY_PAD,
//                                    Blocks.CAVE_VINES,
//                                    Blocks.CAVE_VINES_PLANT,
//                                    Blocks.SPORE_BLOSSOM,
//                                    Blocks.FROGSPAWN,
//                                    Blocks.WARPED_FUNGUS,
//                                    Blocks.CRIMSON_FUNGUS,
//                                    Blocks.WARPED_ROOTS,
//                                    Blocks.CRIMSON_ROOTS,
//                                    Blocks.WEEPING_VINES,
//                                    Blocks.TWISTING_VINES,
//                                    Blocks.DIRT_PATH,
//                                    Blocks.FARMLAND,
//                                    Blocks.TORCHFLOWER
//                            )
//                            .fluidloggableBlockTags(
//                                    BlockTags.FENCE_GATES,
//                                    BlockTags.DOORS,
//                                    BlockTags.BEDS,
//                                    BlockTags.PRESSURE_PLATES,
//                                    BlockTags.BUTTONS,
//                                    BlockTags.WOOL_CARPETS,
//                                    BlockTags.BANNERS,
//                                    BlockTags.FLOWER_POTS,
//                                    BlockTags.CANDLE_CAKES,
//                                    BlockTags.ANVIL,
//                                    BlockTags.SHULKER_BOXES,
//                                    BlockTags.CROPS
//                            )
                            .build()
            );

            CONFIG.load();
        }

        public static boolean hasDifferentLightEmission(FluidState prevFluidState, FluidState newFluidState) {
            return prevFluidState.createLegacyBlock().getLightEmission() != newFluidState.createLegacyBlock().getLightEmission();
        }

        public static BlockState handleBlockRemoval(Level instance, BlockPos blockPos, int flags, int maxUpdateDepth) {
            FluidState fluidState = instance.getFluidState(blockPos);

            ((LevelExtension) instance).fluidlogged$setFluid(
                    blockPos,
                    Fluids.EMPTY.defaultFluidState(),
                    flags,
                    maxUpdateDepth
            );

            int replacementFluidLevel = Mth.clamp(8 - fluidState.getAmount(), 0, 8);
            return fluidState.createLegacyBlock()
                    .trySetValue(LiquidBlock.LEVEL, replacementFluidLevel);
        }

    }

}
