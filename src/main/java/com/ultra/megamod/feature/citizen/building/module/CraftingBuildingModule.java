package com.ultra.megamod.feature.citizen.building.module;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete implementation of {@link ICraftingBuildingModule}.
 * Manages a list of learned recipe identifiers up to a configurable capacity,
 * with full NBT persistence.
 *
 * Used by crafting buildings (Baker, Blacksmith, Cook, Sawmill, Smeltery,
 * Stonemason, Crusher, Dyer, Fletcher, Glassblower, ConcreteMixer,
 * StoneSmeltery, Kitchen, Mechanic, Alchemist).
 */
public class CraftingBuildingModule implements ICraftingBuildingModule, IPersistentModule {

    private static final String NBT_CRAFTING_TYPE = "CraftingType";
    private static final String NBT_MAX_RECIPES = "MaxRecipes";
    private static final String NBT_RECIPES = "Recipes";

    private final String craftingType;
    private final int maxRecipes;
    private final List<Identifier> recipes;

    /**
     * Creates a new crafting building module.
     *
     * @param craftingType the crafting type identifier (e.g., "blacksmith", "baker")
     * @param maxRecipes   the maximum number of recipes this building can learn
     */
    public CraftingBuildingModule(String craftingType, int maxRecipes) {
        this.craftingType = craftingType;
        this.maxRecipes = maxRecipes;
        this.recipes = new ArrayList<>();
    }

    @Override
    public String getModuleId() {
        return "crafting";
    }

    @Override
    public String getCraftingType() {
        return craftingType;
    }

    @Override
    public List<Identifier> getRecipes() {
        return Collections.unmodifiableList(recipes);
    }

    @Override
    public boolean addRecipe(Identifier recipeId) {
        if (recipeId == null) return false;
        if (recipes.size() >= maxRecipes) return false;
        if (recipes.contains(recipeId)) return false;
        recipes.add(recipeId);
        return true;
    }

    @Override
    public void removeRecipe(Identifier recipeId) {
        recipes.remove(recipeId);
    }

    @Override
    public int getMaxRecipes() {
        return maxRecipes;
    }

    @Nullable
    @Override
    public Identifier getFirstRecipeFor(ItemStack output) {
        // This would need recipe registry lookups to match output.
        // For now, return null — recipe matching is done at the worker AI level.
        return null;
    }

    // ==================== Persistence ====================

    @Override
    public void onBuildingLoad(CompoundTag tag) {
        CompoundTag moduleTag = tag.getCompoundOrEmpty(getModuleId());
        recipes.clear();
        if (moduleTag.contains(NBT_RECIPES)) {
            ListTag recipeList = moduleTag.getListOrEmpty(NBT_RECIPES);
            for (int i = 0; i < recipeList.size(); i++) {
                Tag entry = recipeList.get(i);
                if (entry instanceof StringTag stringTag) {
                    Identifier id = Identifier.tryParse(stringTag.value());
                    if (id != null) {
                        recipes.add(id);
                    }
                }
            }
        }
    }

    @Override
    public void onBuildingSave(CompoundTag tag) {
        CompoundTag moduleTag = new CompoundTag();
        moduleTag.putString(NBT_CRAFTING_TYPE, craftingType);
        moduleTag.putInt(NBT_MAX_RECIPES, maxRecipes);

        ListTag recipeList = new ListTag();
        for (Identifier id : recipes) {
            recipeList.add(StringTag.valueOf(id.toString()));
        }
        moduleTag.put(NBT_RECIPES, recipeList);

        tag.put(getModuleId(), moduleTag);
    }

    @Override
    public void onBuildingTick(Level level) {
        // Crafting module doesn't need per-tick logic.
    }
}
