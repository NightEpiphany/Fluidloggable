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
		final ConfigCategory dynamicContainers = builder.getOrCreateCategory(
				Component.translatable("fluidloggable.config.category.dynamic_containers")
		);
        final ConfigCategory blocks = builder.getOrCreateCategory(Component.translatable("fluidloggable.config.category.block_mixins"));
		final ConfigCategory compatibility = builder.getOrCreateCategory(
				Component.translatable("fluidloggable.config.category.compat_mixins")
		);

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

		dynamicContainers.addEntry(entryBuilder
				.startStrList(
						Component.translatable("fluidloggable.config.fluidloggable_mod_ids"),
						FluidloggableConfig.getFluidloggableModIds()
				)
				.setDefaultValue(FluidloggableConfig.DEFAULT_FLUIDLOGGABLE_MOD_IDS)
				.setSaveConsumer(FluidloggableConfig::setFluidloggableModIds)
				.setTooltip(Component.translatable("fluidloggable.config.fluidloggable_mod_ids.tooltip"))
				.requireRestart()
				.build());

		dynamicContainers.addEntry(entryBuilder
				.startStrList(
						Component.translatable("fluidloggable.config.fluidloggable_block_ids"),
						FluidloggableConfig.getFluidloggableBlockIds()
				)
				.setDefaultValue(FluidloggableConfig.DEFAULT_FLUIDLOGGABLE_BLOCK_IDS)
				.setSaveConsumer(FluidloggableConfig::setFluidloggableBlockIds)
				.setTooltip(Component.translatable("fluidloggable.config.fluidloggable_block_ids.tooltip"))
				.requireRestart()
				.build());

        general.addEntry(entryBuilder
                .startBooleanToggle(
                        Component.translatable("fluidloggable.config.collision_shape_fluid_blocking"),
                        FluidloggableConfig.isCollisionShapeFluidBlockingEnabled()
                )
                .setDefaultValue(FluidloggableConfig.defaultCollisionShapeFluidBlocking())
                .setSaveConsumer(FluidloggableConfig::setCollisionShapeFluidBlockingEnabled)
                .setTooltip(Component.translatable("fluidloggable.config.collision_shape_fluid_blocking.tooltip"))
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

		for (FluidloggableConfig.CompatibilityMixinGroup group : FluidloggableConfig.COMPATIBILITY_MIXIN_GROUPS) {
			for (String mixin : group.mixins()) {
				compatibility.addEntry(entryBuilder
						.startBooleanToggle(
								Component.literal(group.displayName() + " - " + FluidloggableConfig.displayName(mixin)),
								FluidloggableConfig.isCompatibilityMixinEnabled(group.modId(), mixin)
						)
						.setDefaultValue(true)
						.setSaveConsumer(enabled -> FluidloggableConfig.setCompatibilityMixinEnabled(
								group.modId(),
								mixin,
								enabled
						))
						.setTooltip(Component.translatable("fluidloggable.config.block_mixin.tooltip"))
						.requireRestart()
						.build());
			}
		}

        builder.setSavingRunnable(FluidloggableConfig::save);
        return builder.build();
    }
}
