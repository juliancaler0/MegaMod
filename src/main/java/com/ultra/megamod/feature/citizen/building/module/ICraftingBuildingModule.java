package com.ultra.megamod.feature.citizen.building.module;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Module interface for buildings that can craft items.
 * Each crafting building has a type (e.g., "blacksmith", "baker") and
 * maintains a list of learned recipes up to its max capacity.
 */
public interface ICraftingBuildingModule extends IBuildingModule {

    /**
     * Returns the list of recipe identifiers this building has learned.
     *
     * @return unmodifiable list of recipe identifiers
     */
    List<Identifier> getRecipes();

    /**
     * Teaches a new recipe to this building.
     *
     * @param recipeId the identifier of the recipe to add
     * @return true if the recipe was added, false if already known or at max capacity
     */
    boolean addRecipe(Identifier recipeId);

    /**
     * Removes a recipe from this building's known recipes.
     *
     * @param recipeId the identifier of the recipe to remove
     */
    void removeRecipe(Identifier recipeId);

    /**
     * Returns the maximum number of recipes this building can learn.
     *
     * @return the max recipe count
     */
    int getMaxRecipes();

    /**
     * Finds the first recipe that produces the given output item.
     *
     * @param output the desired output item stack
     * @return the recipe identifier, or null if no matching recipe is known
     */
    @Nullable
    Identifier getFirstRecipeFor(ItemStack output);

    /**
     * Returns the crafting type identifier for this building.
     * Used to categorize what kind of crafting this building performs.
     *
     * @return the crafting type string (e.g., "blacksmith", "baker")
     */
    String getCraftingType();
}
