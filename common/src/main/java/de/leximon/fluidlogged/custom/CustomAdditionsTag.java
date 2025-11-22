package de.leximon.fluidlogged.custom;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import static de.leximon.fluidlogged.Fluidlogged.MOD_ID;

public class CustomAdditionsTag {

    public static final TagKey<Block> FLUIDLOGGABLE = create();

    private static TagKey<Block> create() {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "custom_fluidloggable"));
    }
}
