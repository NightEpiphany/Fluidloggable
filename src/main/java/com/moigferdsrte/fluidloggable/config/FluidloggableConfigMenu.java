package com.moigferdsrte.fluidloggable.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class FluidloggableConfigMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return FluidloggableConfigMenu::createScreen;
    }

    private static Screen createScreen(final Screen parent) {
        FluidloggableConfig.reload();

        final ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("fluidloggable.config.title"));
        final ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        final ConfigCategory general = builder.getOrCreateCategory(Component.translatable("fluidloggable.config.category.general"));
        final ConfigCategory blocks = builder.getOrCreateCategory(Component.translatable("fluidloggable.config.category.block_mixins"));

        general.addEntry(entryBuilder
                .startBooleanToggle(
                        Component.translatable("fluidloggable.config.client_only_compatibility_mode"),
                        FluidloggableConfig.isClientOnlyCompatibilityModeEnabled()
                )
                .setDefaultValue(FluidloggableConfig.defaultClientOnlyCompatibilityMode())
                .setSaveConsumer(FluidloggableConfig::setClientOnlyCompatibilityModeEnabled)
                .setTooltip(Component.translatable("fluidloggable.config.client_only_compatibility_mode.tooltip"))
                .requireRestart()
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(
                        Component.translatable("fluidloggable.config.trapdoor_fluid_blocking"),
                        FluidloggableConfig.isTrapdoorFluidBlockingEnabled()
                )
                .setDefaultValue(FluidloggableConfig.defaultTrapdoorFluidBlocking())
                .setSaveConsumer(FluidloggableConfig::setTrapdoorFluidBlockingEnabled)
                .setTooltip(Component.translatable("fluidloggable.config.trapdoor_fluid_blocking.tooltip"))
                .build());

        for (String mixin : FluidloggableConfig.BLOCK_MIXINS) {
            blocks.addEntry(entryBuilder
                    .startBooleanToggle(Component.literal(FluidloggableConfig.displayName(mixin)), FluidloggableConfig.isBlockMixinEnabled(mixin))
                    .setDefaultValue(true)
                    .setSaveConsumer(enabled -> FluidloggableConfig.setBlockMixinEnabled(mixin, enabled))
                    .setTooltip(Component.translatable("fluidloggable.config.block_mixin.tooltip"))
                    .requireRestart()
                    .build());
        }

        builder.setSavingRunnable(FluidloggableConfig::save);
        return builder.build();
    }
}
