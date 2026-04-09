package com.ultra.megamod.feature.backpacks.upgrade.smelting;

import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Blast furnace upgrade for backpacks.
 * Smelts ores/metals at double speed (100 ticks per item).
 */
public class BlastFurnaceUpgrade extends AbstractSmeltingUpgrade {

    public BlastFurnaceUpgrade() {
        this.totalCookTime = 100;
    }

    @Override
    public String getId() {
        return "blast_furnace";
    }

    @Override
    public String getDisplayName() {
        return "Blast Furnace";
    }

    @Override
    public RecipeType<BlastingRecipe> getRecipeType() {
        return RecipeType.BLASTING;
    }
}
