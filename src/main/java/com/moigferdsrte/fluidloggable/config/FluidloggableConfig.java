package com.moigferdsrte.fluidloggable.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.moigferdsrte.fluidloggable.Fluidloggable;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FluidloggableConfig {
    public static final List<String> BLOCK_MIXINS = List.of(
            "AnvilBlockMixin",
            "AttachedStemBlockMixin",
            "BambooBlockMixin",
            "BambooSaplingBlockMixin",
            "BannerBlockMixin",
            "BeaconBlockMixin",
            "BedBlockMixin",
            "BeetrootBlockMixin",
            "BellBlockMixin",
            "BrewingStandBlockMixin",
            "ButtonBlockMixin",
            "CactusBlockMixin",
            "CarpetBlockMixin",
            "CauldronBlockMixin",
            "CaveVinesBlockMixin",
            "CaveVinesPlantBlockMixin",
            "CobwebBlockMixin",
            "ComparatorBlockMixin",
            "ComposterBlockMixin",
            "CropBlockMixin",
            "DaylightDetectorBlockMixin",
            "DiodeBlockMixin",
            "DirtPathBlockMixin",
            "DoorBlockMixin",
            "DoublePlantBlockMixin",
            "EmptyCauldronBlockMixin",
            "EnchantingTableBlockMixin",
            "EnderDragonBlockMixin",
            "EndPortalBlockMixin",
            "EndPortalFrameBlockMixin",
            "EndRodBlockMixin",
            "FarmlandBlockMixin",
            "FenceGateBlockMixin",
            "FlowerBedBlockMixin",
            "FlowerPotBlockMixin",
            "HopperBlockMixin",
            "LavaCauldronBlockMixin",
            "LayeredCauldronBlockMixin",
            "LeafLitterBlockMixin",
            "LecternBlockMixin",
            "LeverBlockMixin",
            "NetherPortalBlockMixin",
            "NetherWartBlockMixin",
            "PistonArmBlockMixin",
            "PistonBaseBlockMixin",
            "PressurePlateBlockMixin",
            "RedstoneTorchBlockMixin",
            "RedstoneWallTorchBlockMixin",
            "RedStoneWireBlockMixin",
            "RepeaterBlockMixin",
            "SaplingBlockMixin",
            "ShulkerBoxBlockMixin",
            "SkullBlockMixin",
            "SpawnerBlockMixin",
            "StemBlockMixin",
            "StonecutterBlockMixin",
            "StructureVoidBlockMixin",
            "SugarCaneBlockMixin",
            "SweetBerryBushBlockMixin",
            "TorchBlockMixin",
            "TorchflowerCropBlockMixin",
            "TrapDoorBlockMixin",
            "TripwireBlockMixin",
            "TripwireHookBlockMixin",
            "TurtleEggBlockMixin",
            "TwistingVinesBlockMixin",
            "TwistingVinesPlantBlockMixin",
            "VaultBlockMixin",
            "VegetationBlockMixin",
            "VineBlockMixin",
            "WallBannerBlockMixin",
            "WallSkullBlockMixin",
            "WallTorchBlockMixin",
            "WeepingVinesBlockMixin",
            "WeepingVinesPlantBlockMixin",
            "WeightedPressurePlateBlockMixin"
    );

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CLIENT_ONLY_COMPATIBILITY_MODE_KEY = "clientOnlyCompatibilityMode";
    private static final String COLLISION_SHAPE_FLUID_BLOCKING_KEY = "collisionShapeFluidBlocking";
    private static final String LEGACY_TRAPDOOR_FLUID_BLOCKING_KEY = "trapdoorFluidBlocking";
    private static final String BLOCK_MIXINS_KEY = "blockMixins";
    private static final Map<String, Boolean> blockMixins = new LinkedHashMap<>();
    // Support for vanilla server.
    private static boolean clientOnlyCompatibilityMode;
    private static volatile boolean collisionShapeFluidBlocking;
    private static volatile boolean loaded;

    private FluidloggableConfig() {
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }

        resetToDefaults();
        final Path configPath = configPath();
        if (Files.exists(configPath)) {
            readConfig(configPath);
        }

        loaded = true;
        save();
    }

    public static synchronized void reload() {
        loaded = false;
        load();
    }

    public static synchronized void save() {
        final Path configPath = configPath();
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(toJson(), writer);
            }
        } catch (IOException exception) {
            Fluidloggable.LOGGER.warn("Failed to save Fluidloggable config", exception);
        }
    }

    public static synchronized boolean isClientOnlyCompatibilityModeEnabled() {
        load();
        return clientOnlyCompatibilityMode;
    }

    public static synchronized void setClientOnlyCompatibilityModeEnabled(final boolean enabled) {
        load();
        clientOnlyCompatibilityMode = enabled;
    }

    public static boolean defaultClientOnlyCompatibilityMode() {
        return FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT;
    }

    public static boolean isCollisionShapeFluidBlockingEnabled() {
        if (!loaded) {
            load();
        }
        return collisionShapeFluidBlocking;
    }

    public static synchronized void setCollisionShapeFluidBlockingEnabled(final boolean enabled) {
        load();
        collisionShapeFluidBlocking = enabled;
    }

    public static boolean defaultCollisionShapeFluidBlocking() {
        return true;
    }

    public static synchronized boolean isBlockMixinEnabled(final String mixinSimpleName) {
        load();
        return blockMixins.getOrDefault(mixinSimpleName, true);
    }

    public static synchronized void setBlockMixinEnabled(final String mixinSimpleName, final boolean enabled) {
        load();
        if (blockMixins.containsKey(mixinSimpleName)) {
            blockMixins.put(mixinSimpleName, enabled);
        }
    }

    public static synchronized Map<String, Boolean> getBlockMixins() {
        load();
        return Collections.unmodifiableMap(new LinkedHashMap<>(blockMixins));
    }

    public static String displayName(final String mixinSimpleName) {
        final String withoutSuffix = mixinSimpleName.endsWith("Mixin")
                ? mixinSimpleName.substring(0, mixinSimpleName.length() - "Mixin".length())
                : mixinSimpleName;
        final StringBuilder builder = new StringBuilder();
        for (int index = 0; index < withoutSuffix.length(); index++) {
            final char current = withoutSuffix.charAt(index);
            if (index > 0 && Character.isUpperCase(current) && !Character.isUpperCase(withoutSuffix.charAt(index - 1))) {
                builder.append(' ');
            }
            builder.append(current);
        }
        return builder.toString().replace("Red Stone", "Redstone");
    }

    private static void resetToDefaults() {
        clientOnlyCompatibilityMode = defaultClientOnlyCompatibilityMode();
        collisionShapeFluidBlocking = defaultCollisionShapeFluidBlocking();
        blockMixins.clear();
        for (String mixin : BLOCK_MIXINS) {
            blockMixins.put(mixin, true);
        }
    }

    private static void readConfig(final Path configPath) {
        try (Reader reader = Files.newBufferedReader(configPath)) {
            final JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject()) {
                return;
            }

            final JsonObject object = root.getAsJsonObject();
            final JsonElement clientOnlyCompatibilityModeElement = object.get(CLIENT_ONLY_COMPATIBILITY_MODE_KEY);
            if (clientOnlyCompatibilityModeElement != null && clientOnlyCompatibilityModeElement.isJsonPrimitive()) {
                clientOnlyCompatibilityMode = clientOnlyCompatibilityModeElement.getAsBoolean();
            }

            JsonElement collisionShapeFluidBlockingElement = object.get(COLLISION_SHAPE_FLUID_BLOCKING_KEY);
            if (collisionShapeFluidBlockingElement == null) {
                collisionShapeFluidBlockingElement = object.get(LEGACY_TRAPDOOR_FLUID_BLOCKING_KEY);
            }
            if (collisionShapeFluidBlockingElement != null && collisionShapeFluidBlockingElement.isJsonPrimitive()) {
                collisionShapeFluidBlocking = collisionShapeFluidBlockingElement.getAsBoolean();
            }

            final JsonElement blockMixinsElement = object.get(BLOCK_MIXINS_KEY);
            if (blockMixinsElement == null || !blockMixinsElement.isJsonObject()) {
                return;
            }

            for (Map.Entry<String, JsonElement> entry : blockMixinsElement.getAsJsonObject().entrySet()) {
                if (blockMixins.containsKey(entry.getKey()) && entry.getValue().isJsonPrimitive()) {
                    blockMixins.put(entry.getKey(), entry.getValue().getAsBoolean());
                }
            }
        } catch (RuntimeException | IOException exception) {
            Fluidloggable.LOGGER.warn("Failed to read Fluidloggable config, using defaults", exception);
        }
    }

    private static JsonObject toJson() {
        final JsonObject root = new JsonObject();
        root.addProperty(CLIENT_ONLY_COMPATIBILITY_MODE_KEY, clientOnlyCompatibilityMode);
        root.addProperty(COLLISION_SHAPE_FLUID_BLOCKING_KEY, collisionShapeFluidBlocking);
        final JsonObject blockMixinObject = new JsonObject();
        for (Map.Entry<String, Boolean> entry : blockMixins.entrySet()) {
            blockMixinObject.addProperty(entry.getKey(), entry.getValue());
        }
        root.add(BLOCK_MIXINS_KEY, blockMixinObject);
        return root;
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("fluidloggable.json");
    }
}
