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
    private static final String FARMERS_DELIGHT_COMPAT_PACKAGE = "com.moigferdsrte.fluidloggable.compat.farmersdelight.mixin.";
    private static final Map<String, String> FARMERS_DELIGHT_PARENT_MIXINS = Map.of(
            "HangingTomatoBlockMixin", "CropBlockMixin",
            "MushroomColonyBlockMixin", "VegetationBlockMixin",
            "RiceBlockMixin", "VegetationBlockMixin",
            "RicePaniclesBlockMixin", "CropBlockMixin",
            "TomatoBlockMixin", "CropBlockMixin"
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
        if (mixinClassName.startsWith(FARMERS_DELIGHT_COMPAT_PACKAGE)) {
            return FabricLoader.getInstance().isModLoaded("farmersdelight")
                    && FluidloggableConfig.isBlockMixinEnabled(FARMERS_DELIGHT_PARENT_MIXINS.getOrDefault(simpleName(mixinClassName), ""));
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

    private static boolean isClientOnlyCompatibilityMode() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
                && FluidloggableConfig.isClientOnlyCompatibilityModeEnabled();
    }
}
