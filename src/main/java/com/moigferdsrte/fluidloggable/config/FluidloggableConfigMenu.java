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
                .setTitle(Component.literal("Fluidloggable"));
        final ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        final ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
        final ConfigCategory blocks = builder.getOrCreateCategory(Component.literal("Block Mixins"));

        general.addEntry(entryBuilder
                .startBooleanToggle(Component.literal("Client-only Compatibility Mode"), FluidloggableConfig.isClientOnlyCompatibilityModeEnabled())
                .setDefaultValue(FluidloggableConfig.defaultClientOnlyCompatibilityMode())
                .setSaveConsumer(FluidloggableConfig::setClientOnlyCompatibilityModeEnabled)
                .setTooltip(Component.literal("Requires restart. Disables Fluidloggable mixins on physical clients so vanilla servers use the vanilla block state palette."))
                .requireRestart()
                .build());

        for (String mixin : FluidloggableConfig.BLOCK_MIXINS) {
            blocks.addEntry(entryBuilder
                    .startBooleanToggle(Component.literal(FluidloggableConfig.displayName(mixin)), FluidloggableConfig.isBlockMixinEnabled(mixin))
                    .setDefaultValue(true)
                    .setSaveConsumer(enabled -> FluidloggableConfig.setBlockMixinEnabled(mixin, enabled))
                    .setTooltip(Component.literal("Requires restart."))
                    .requireRestart()
                    .build());
        }

        builder.setSavingRunnable(FluidloggableConfig::save);
        return builder.build();
    }
}
