package com.ultra.megamod.feature.backpacks.upgrade.smelting;

import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;

/**
 * Standard furnace upgrade for backpacks.
 * Smelts items at the default rate (200 ticks per item).
 */
public class FurnaceUpgrade extends AbstractSmeltingUpgrade {

    @Override
    public String getId() {
        return "furnace";
    }

    @Override
    public String getDisplayName() {
        return "Furnace";
    }

    @Override
    public RecipeType<SmeltingRecipe> getRecipeType() {
        return RecipeType.SMELTING;
    }
}
