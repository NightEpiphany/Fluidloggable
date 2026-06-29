package com.moigferdsrte.fluidloggable.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.moigferdsrte.fluidloggable.Fluidloggable;
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
    private static final String BLOCK_MIXINS_KEY = "blockMixins";
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("fluidloggable.json");
    private static final Map<String, Boolean> blockMixins = new LinkedHashMap<>();
    private static boolean loaded;

    private FluidloggableConfig() {
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }

        resetToDefaults();
        if (Files.exists(CONFIG_PATH)) {
            readConfig();
        }

        loaded = true;
        save();
    }

    public static synchronized void reload() {
        loaded = false;
        load();
    }

    public static synchronized void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(toJson(), writer);
            }
        } catch (IOException exception) {
            Fluidloggable.LOGGER.warn("Failed to save Fluidloggable config", exception);
        }
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
        blockMixins.clear();
        for (String mixin : BLOCK_MIXINS) {
            blockMixins.put(mixin, true);
        }
    }

    private static void readConfig() {
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            final JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject()) {
                return;
            }

            final JsonObject object = root.getAsJsonObject();
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
        final JsonObject blockMixinObject = new JsonObject();
        for (Map.Entry<String, Boolean> entry : blockMixins.entrySet()) {
            blockMixinObject.addProperty(entry.getKey(), entry.getValue());
        }
        root.add(BLOCK_MIXINS_KEY, blockMixinObject);
        return root;
    }
}
