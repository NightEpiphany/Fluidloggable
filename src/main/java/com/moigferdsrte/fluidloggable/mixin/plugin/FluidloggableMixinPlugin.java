package com.moigferdsrte.fluidloggable.mixin.plugin;

import com.moigferdsrte.fluidloggable.config.FluidloggableConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class FluidloggableMixinPlugin implements IMixinConfigPlugin {
    private static final String MOD_PACKAGE = "com.moigferdsrte.fluidloggable.";
    private static final String BASE_MIXIN_PACKAGE = "com.moigferdsrte.fluidloggable.mixin.base.";
    private static final String SODIUM_COMPAT_PACKAGE = "com.moigferdsrte.fluidloggable.compat.sodium.mixin.";
    private static final String COMFORTS_COMPAT_PACKAGE = "com.moigferdsrte.fluidloggable.compat.comforts.mixin.";
    private static final String CREATE_FLY_PACKAGE = "com.moigferdsrte.fluidloggable.compat.create.fly.mixin.";
    private static final String CREATE_COPYCATS_PACKAGE = "com.moigferdsrte.fluidloggable.compat.create.copycats.mixin.";
    private static final String FARMERS_DELIGHT_COMPAT_PACKAGE = "com.moigferdsrte.fluidloggable.compat.farmersdelight.mixin.";
    private static final String BEDROCKIFY_COMPAT_PACKAGE = "com.moigferdsrte.fluidloggable.compat.bedrockify.mixin.";
	private static final Map<String, String> COMPATIBILITY_PARENT_MIXINS = Map.ofEntries(
			Map.entry("bedrockify:PotionCauldronBlockMixin", "CauldronBlockMixin"),
			Map.entry("comforts:BaseComfortsBlockMixin", "BedBlockMixin"),
			Map.entry("farmersdelight:HangingTomatoBlockMixin", "CropBlockMixin"),
			Map.entry("farmersdelight:MushroomColonyBlockMixin", "VegetationBlockMixin"),
			Map.entry("farmersdelight:RiceBlockMixin", "VegetationBlockMixin"),
			Map.entry("farmersdelight:RicePaniclesBlockMixin", "CropBlockMixin"),
			Map.entry("farmersdelight:TomatoBlockMixin", "CropBlockMixin")
	);

    @Override
    public void onLoad(String mixinPackage) {
        FluidloggableConfig.load();
    }

    @Override
    public String getRefMapperConfig() {
        return "";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (isClientOnlyCompatibilityMode() && mixinClassName.startsWith(MOD_PACKAGE)) {
            return false;
        }

        if (mixinClassName.startsWith(BASE_MIXIN_PACKAGE)) {
            return FluidloggableConfig.isBlockMixinEnabled(simpleName(mixinClassName));
        }
        if (mixinClassName.startsWith(SODIUM_COMPAT_PACKAGE)) {
            return FabricLoader.getInstance().isModLoaded("sodium");
        }

		final String compatibilityModId = compatibilityModId(mixinClassName);
		if (compatibilityModId != null) {
			final String mixinSimpleName = simpleName(mixinClassName);
			return FabricLoader.getInstance().isModLoaded(compatibilityModId)
					&& FluidloggableConfig.isCompatibilityMixinEnabled(compatibilityModId, mixinSimpleName)
					&& isParentMixinEnabled(compatibilityModId, mixinSimpleName);
        }

        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    private static String simpleName(final String className) {
        return className.substring(className.lastIndexOf('.') + 1);
    }

	private static String compatibilityModId(final String mixinClassName) {
		if (mixinClassName.startsWith(BEDROCKIFY_COMPAT_PACKAGE)) {
			return "bedrockify";
		}
		if (mixinClassName.startsWith(COMFORTS_COMPAT_PACKAGE)) {
			return "comforts";
		}
		if (mixinClassName.startsWith(CREATE_FLY_PACKAGE)) {
			return "create";
		}
		if (mixinClassName.startsWith(CREATE_COPYCATS_PACKAGE)) {
			return "copycats";
		}
		if (mixinClassName.startsWith(FARMERS_DELIGHT_COMPAT_PACKAGE)) {
			return "farmersdelight";
		}
		return null;
	}

	private static boolean isParentMixinEnabled(final String modId, final String mixinSimpleName) {
		final String parentMixin = COMPATIBILITY_PARENT_MIXINS.get(modId + ':' + mixinSimpleName);
		return parentMixin == null || FluidloggableConfig.isBlockMixinEnabled(parentMixin);
	}

    private static boolean isClientOnlyCompatibilityMode() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
                && FluidloggableConfig.isClientOnlyCompatibilityModeEnabled();
    }
}
