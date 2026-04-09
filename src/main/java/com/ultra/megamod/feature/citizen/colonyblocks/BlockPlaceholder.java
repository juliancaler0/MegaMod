package com.ultra.megamod.feature.citizen.colonyblocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Placeholder block used in blueprints for substitution during building.
 * Three variants exist, each with a distinct visual and placement behavior:
 * <ul>
 *   <li><b>SUBSTITUTION</b> (tan) -- "keep what's here"; the builder preserves whatever block already exists.</li>
 *   <li><b>SOLID_SUBSTITUTION</b> (brown) -- if the existing block is non-solid, replace it with a fill block (cobblestone).</li>
 *   <li><b>FLUID_SUBSTITUTION</b> (blue) -- place the dimension-appropriate fluid (water in overworld, lava in nether).</li>
 * </ul>
 * Each type is registered as a separate block in {@link ColonyBlockRegistry}.
 */
public class BlockPlaceholder extends Block {

    public enum PlaceholderType {
        /** Keep whatever block is already present at this position. */
        SUBSTITUTION,
        /** Replace non-solid blocks with a solid fill block (cobblestone). */
        SOLID_SUBSTITUTION,
        /** Place dimension-appropriate fluid (water/lava). */
        FLUID_SUBSTITUTION
    }

    private static final MapCodec<BlockPlaceholder> CODEC = BlockPlaceholder.simpleCodec(BlockPlaceholder::new);

    private final PlaceholderType type;

    /**
     * Creates a placeholder block with the given type.
     */
    public BlockPlaceholder(BlockBehaviour.Properties props, PlaceholderType type) {
        super(props);
        this.type = type;
    }

    /**
     * Default constructor for codec/deferred registration (defaults to SUBSTITUTION).
     */
    public BlockPlaceholder(BlockBehaviour.Properties props) {
        this(props, PlaceholderType.SUBSTITUTION);
    }

    @Override
    protected MapCodec<? extends BlockPlaceholder> codec() {
        return CODEC;
    }

    public PlaceholderType getPlaceholderType() {
        return type;
    }
}
