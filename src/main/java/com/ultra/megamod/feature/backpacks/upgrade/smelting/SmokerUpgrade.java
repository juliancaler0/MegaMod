package com.ultra.megamod.feature.backpacks.upgrade.smelting;

import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmokingRecipe;

/**
 * Smoker upgrade for backpacks.
 * Smelts food items at double speed (100 ticks per item).
 */
public class SmokerUpgrade extends AbstractSmeltingUpgrade {

    public SmokerUpgrade() {
        this.totalCookTime = 100;
    }

    @Override
    public String getId() {
        return "smoker";
    }

    @Override
    public String getDisplayName() {
        return "Smoker";
    }

    @Override
    public RecipeType<SmokingRecipe> getRecipeType() {
        return RecipeType.SMOKING;
    }
}
