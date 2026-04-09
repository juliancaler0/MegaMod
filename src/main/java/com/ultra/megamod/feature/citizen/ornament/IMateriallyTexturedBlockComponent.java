package com.ultra.megamod.feature.citizen.ornament;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

/**
 * Defines a single texture component slot on a retexturable block.
 * Each component has a unique ID, a default block texture, and an optional flag.
 */
public interface IMateriallyTexturedBlockComponent {

    /**
     * Unique identifier for this component (e.g. "megamod:frame", "megamod:panel").
     */
    Identifier getId();

    /**
     * The default block whose texture is used when no material is supplied.
     */
    Block getDefault();

    /**
     * Whether this component is optional. If false, the player must supply a material.
     */
    boolean isOptional();
}
