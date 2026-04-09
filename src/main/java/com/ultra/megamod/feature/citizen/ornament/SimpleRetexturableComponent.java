package com.ultra.megamod.feature.citizen.ornament;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

/**
 * Standard implementation of a retexturable block component.
 *
 * @param id           unique component identifier
 * @param defaultBlock the block whose texture is used when no material is supplied
 * @param optional     whether the player must provide a material for this component
 */
public record SimpleRetexturableComponent(
        Identifier id,
        Block defaultBlock,
        boolean optional
) implements IMateriallyTexturedBlockComponent {

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public Block getDefault() {
        return defaultBlock;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }
}
