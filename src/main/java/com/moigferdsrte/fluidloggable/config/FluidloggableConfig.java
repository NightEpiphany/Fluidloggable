package com.moigferdsrte.fluidloggable.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.moigferdsrte.fluidloggable.Fluidloggable;
import com.moigferdsrte.fluidloggable.block.ConfiguredFluidloggableBlockSupport;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FluidloggableConfig {
	public static final List<String> DEFAULT_FLUIDLOGGABLE_MOD_IDS = List.of(
            "displaydelight",
            "rusticdelight",
            "betternether",
            "betterend",
            "nightlights",
            "refurbished_furniture",
            "windchimes",
            "sootychimneys",
            "enchantinginfuser",
			"xercamusic",
			"biomesoplenty"
    );
	public static final List<String> DEFAULT_FLUIDLOGGABLE_BLOCK_IDS = List.of(
			"deco_sniffer_egg:hollow_sniffer_egg",
			"friendsandfoes:crab_egg",
			"sereneseasons:season_sensor",
            "alexsmobs:transmutation_table",
			"alexsmobs:void_worm_effigy",
            "xercafood:vat",
            "xercablocks:carved_acacia_1",
            "xercablocks:carved_acacia_2",
            "xercablocks:carved_acacia_3",
            "xercablocks:carved_acacia_4",
            "xercablocks:carved_acacia_5",
            "xercablocks:carved_acacia_6",
            "xercablocks:carved_acacia_7",
            "xercablocks:carved_acacia_8",
            "thecopperierage:chime",
            "thecopperierage:exposed_chime",
            "thecopperierage:oxidized_chime",
            "thecopperierage:weathered_chime",
            "thecopperierage:waxed_chime",
            "thecopperierage:waxed_exposed_chime",
            "thecopperierage:waxed_oxidized_chime",
            "thecopperierage:waxed_weathered_chime",
			"thecopperierage:weighted_pressure_plate",
			"thecopperierage:exposed_weighted_pressure_plate",
			"thecopperierage:oxidized_weighted_pressure_plate",
			"thecopperierage:weathered_weighted_pressure_plate",
			"thecopperierage:waxed_weighted_pressure_plate",
			"thecopperierage:waxed_exposed_weighted_pressure_plate",
			"thecopperierage:waxed_oxidized_weighted_pressure_plate",
			"thecopperierage:waxed_weathered_weighted_pressure_plate"
    );

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

	public static final List<CompatibilityMixinGroup> COMPATIBILITY_MIXIN_GROUPS = List.of(
			new CompatibilityMixinGroup("bedrockify", "BedrockIfy", List.of(
					"PotionCauldronBlockMixin"
			)),
			new CompatibilityMixinGroup("comforts", "Comforts", List.of(
					"BaseComfortsBlockMixin"
			)),
			new CompatibilityMixinGroup("create", "Create", List.of(
					"AnalogLeverBlockMixin", "BasinBlockMixin", "BlazeBurnerBlockMixin",
					"DiodeBlockMixin", "DirectedDirectionalBlockMixin", "FluidTankBlockMixin",
					"KineticBlockMixin", "LitBlazeBurnerBlockMixin", "RedstoneRequesterBlockMixin",
					"RepackagerBlockMixin", "RotatedPillarKineticBlockMixin", "SchematicannonBlockMixin",
					"SchematicTableBlockMixin", "StockTickerBlockMixin", "TableClothBlockMixin",
					"ToggleLatchBlockMixin", "WrenchableDirectionalBlockMixin"
			)),
			new CompatibilityMixinGroup("copycats", "Create: Copycats", List.of(
					"CopycatBlockFluidloggableMixin"
			)),
			new CompatibilityMixinGroup("farmersdelight", "Farmer's Delight", List.of(
					"HangingTomatoBlockMixin", "MushroomColonyBlockMixin", "RiceBlockMixin",
					"RicePaniclesBlockMixin", "TomatoBlockMixin", "CanvasRugBlockMixin",
					"FeastBlockMixin", "PieBlockMixin", "RiceRollMedleyBlockMixin",
					"RotatedFeastBlockMixin", "TatamiHalfMatBlockMixin", "TatamiMatBlockMixin"
			))
	);

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CLIENT_ONLY_COMPATIBILITY_MODE_KEY = "clientOnlyCompatibilityMode";
    private static final String COLLISION_SHAPE_FLUID_BLOCKING_KEY = "collisionShapeFluidBlocking";
    private static final String LEGACY_TRAPDOOR_FLUID_BLOCKING_KEY = "trapdoorFluidBlocking";
	private static final String FLUIDLOGGABLE_MOD_IDS_KEY = "fluidloggableModIds";
	private static final String FLUIDLOGGABLE_BLOCK_IDS_KEY = "fluidloggableBlockIds";
	private static final String VERSION_KEY = "version";
    private static final String BLOCK_MIXINS_KEY = "blockMixins";
	private static final String COMPATIBILITY_MIXINS_KEY = "compatMixins";
    private static final Map<String, Boolean> blockMixins = new LinkedHashMap<>();
	private static final Map<String, Boolean> compatibilityMixins = new LinkedHashMap<>();
	private static List<String> fluidloggableModIds = DEFAULT_FLUIDLOGGABLE_MOD_IDS;
	private static List<String> fluidloggableBlockIds = DEFAULT_FLUIDLOGGABLE_BLOCK_IDS;
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
		final ConfigVersionManager.LoadResult loadResult = ConfigVersionManager.prepare(configPath);
		loadResult.config().ifPresent(FluidloggableConfig::applyConfig);

	        loaded = true;
		ConfiguredFluidloggableBlockSupport.invalidate();
		if (loadResult.shouldSave()) {
			save();
		}
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

	public static synchronized List<String> getFluidloggableModIds() {
		load();
		return List.copyOf(fluidloggableModIds);
	}

	public static synchronized void setFluidloggableModIds(final List<String> modIds) {
		load();
		fluidloggableModIds = List.copyOf(modIds);
		ConfiguredFluidloggableBlockSupport.invalidate();
	}

	public static synchronized List<String> getFluidloggableBlockIds() {
		load();
		return List.copyOf(fluidloggableBlockIds);
	}

	public static synchronized void setFluidloggableBlockIds(final List<String> blockIds) {
		load();
		fluidloggableBlockIds = List.copyOf(blockIds);
		ConfiguredFluidloggableBlockSupport.invalidate();
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

	public static synchronized boolean isCompatibilityMixinEnabled(final String modId, final String mixinSimpleName) {
		load();
		return compatibilityMixins.getOrDefault(compatibilityMixinKey(modId, mixinSimpleName), true);
	}

	public static synchronized void setCompatibilityMixinEnabled(
			final String modId,
			final String mixinSimpleName,
			final boolean enabled
	) {
		load();
		final String key = compatibilityMixinKey(modId, mixinSimpleName);
		if (compatibilityMixins.containsKey(key)) {
			compatibilityMixins.put(key, enabled);
		}
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
		fluidloggableModIds = DEFAULT_FLUIDLOGGABLE_MOD_IDS;
		fluidloggableBlockIds = DEFAULT_FLUIDLOGGABLE_BLOCK_IDS;
		blockMixins.clear();
		for (String mixin : BLOCK_MIXINS) {
			blockMixins.put(mixin, true);
		}
		compatibilityMixins.clear();
		for (CompatibilityMixinGroup group : COMPATIBILITY_MIXIN_GROUPS) {
			for (String mixin : group.mixins()) {
				compatibilityMixins.put(compatibilityMixinKey(group.modId(), mixin), true);
			}
		}
	}

	private static void applyConfig(final JsonObject object) {
		try {
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

			fluidloggableModIds = readStringList(object, FLUIDLOGGABLE_MOD_IDS_KEY, DEFAULT_FLUIDLOGGABLE_MOD_IDS);
			fluidloggableBlockIds = readStringList(object, FLUIDLOGGABLE_BLOCK_IDS_KEY, DEFAULT_FLUIDLOGGABLE_BLOCK_IDS);

			final JsonElement blockMixinsElement = object.get(BLOCK_MIXINS_KEY);
			if (blockMixinsElement != null && blockMixinsElement.isJsonObject()) {
				for (Map.Entry<String, JsonElement> entry : blockMixinsElement.getAsJsonObject().entrySet()) {
					if (blockMixins.containsKey(entry.getKey()) && entry.getValue().isJsonPrimitive()) {
						blockMixins.put(entry.getKey(), entry.getValue().getAsBoolean());
					}
				}
			}

			final JsonElement compatibilityMixinsElement = object.get(COMPATIBILITY_MIXINS_KEY);
			if (compatibilityMixinsElement != null && compatibilityMixinsElement.isJsonObject()) {
				readCompatibilityMixins(compatibilityMixinsElement.getAsJsonObject());
			}
		} catch (RuntimeException exception) {
			Fluidloggable.LOGGER.warn("Failed to read Fluidloggable config, using defaults", exception);
		}
	}

	private static JsonObject toJson() {
		final JsonObject root = new JsonObject();
		root.addProperty(VERSION_KEY, ConfigVersionManager.currentVersion());
		root.addProperty(CLIENT_ONLY_COMPATIBILITY_MODE_KEY, clientOnlyCompatibilityMode);
		root.addProperty(COLLISION_SHAPE_FLUID_BLOCKING_KEY, collisionShapeFluidBlocking);
		root.add(FLUIDLOGGABLE_MOD_IDS_KEY, GSON.toJsonTree(fluidloggableModIds));
		root.add(FLUIDLOGGABLE_BLOCK_IDS_KEY, GSON.toJsonTree(fluidloggableBlockIds));
		final JsonObject blockMixinObject = new JsonObject();
		for (Map.Entry<String, Boolean> entry : blockMixins.entrySet()) {
			blockMixinObject.addProperty(entry.getKey(), entry.getValue());
		}
		root.add(BLOCK_MIXINS_KEY, blockMixinObject);
		final JsonObject compatibilityMixinObject = new JsonObject();
		for (CompatibilityMixinGroup group : COMPATIBILITY_MIXIN_GROUPS) {
			final JsonObject groupObject = new JsonObject();
			for (String mixin : group.mixins()) {
				groupObject.addProperty(
						mixin,
						compatibilityMixins.getOrDefault(compatibilityMixinKey(group.modId(), mixin), true)
				);
			}
			compatibilityMixinObject.add(group.modId(), groupObject);
		}
		root.add(COMPATIBILITY_MIXINS_KEY, compatibilityMixinObject);
		return root;
	}

	private static void readCompatibilityMixins(final JsonObject compatibilityMixinObject) {
		for (CompatibilityMixinGroup group : COMPATIBILITY_MIXIN_GROUPS) {
			final JsonElement groupElement = compatibilityMixinObject.get(group.modId());
			if (groupElement == null || !groupElement.isJsonObject()) {
				continue;
			}

			for (Map.Entry<String, JsonElement> entry : groupElement.getAsJsonObject().entrySet()) {
				final String key = compatibilityMixinKey(group.modId(), entry.getKey());
				if (compatibilityMixins.containsKey(key) && entry.getValue().isJsonPrimitive()) {
					compatibilityMixins.put(key, entry.getValue().getAsBoolean());
				}
			}
		}
	}

	private static String compatibilityMixinKey(final String modId, final String mixinSimpleName) {
		return modId + ':' + mixinSimpleName;
	}

	private static List<String> readStringList(
			final JsonObject object,
			final String key,
			final List<String> fallback
	) {
		final JsonElement element = object.get(key);
		if (element == null || !element.isJsonArray()) {
			return fallback;
		}

		final java.util.ArrayList<String> values = new java.util.ArrayList<>();
		for (JsonElement entry : element.getAsJsonArray()) {
			if (entry.isJsonPrimitive() && entry.getAsJsonPrimitive().isString()) {
				values.add(entry.getAsString());
			}
		}
		return List.copyOf(values);
	}

	private static Path configPath() {
		return FabricLoader.getInstance().getConfigDir().resolve("fluidloggable.json");
	}

	public record CompatibilityMixinGroup(String modId, String displayName, List<String> mixins) {
		public CompatibilityMixinGroup {
			mixins = List.copyOf(mixins);
		}
	}
}
