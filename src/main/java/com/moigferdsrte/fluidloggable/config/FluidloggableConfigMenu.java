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
        final ConfigCategory blocks = builder.getOrCreateCategory(Component.literal("Block Mixins"));

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
