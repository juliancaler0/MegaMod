package com.ultra.megamod.reliquary.crafting.alkahestry;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import com.ultra.megamod.reliquary.Reliquary;
import com.ultra.megamod.reliquary.crafting.AlkahestryCraftingRecipe;
import com.ultra.megamod.reliquary.crafting.conditions.AlkahestryEnabledCondition;

public class CraftingRecipeBuilder {
	private final Ingredient ingredient;
	private final int charge;
	private final int resultCount;

	private CraftingRecipeBuilder(Ingredient ingredient, int charge, int resultCount) {
		this.ingredient = ingredient;
		this.charge = charge;
		this.resultCount = resultCount;
	}

	public static CraftingRecipeBuilder craftingRecipe(ItemLike item, int charge, int resultCount) {
		return new CraftingRecipeBuilder(Ingredient.of(item), charge, resultCount);
	}

	public static CraftingRecipeBuilder craftingRecipe(TagKey<Item> tag, int charge, int resultCount) {
		HolderSet.Named<Item> holderSet = BuiltInRegistries.ITEM.getOrThrow(tag);
		return new CraftingRecipeBuilder(Ingredient.of(holderSet), charge, resultCount);
	}

	public void save(RecipeOutput recipeOutput, Identifier id) {
		Identifier fullId = Reliquary.getRL("alkahestry/crafting/" + id.getPath());
		ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, fullId);
		recipeOutput.withConditions(new AlkahestryEnabledCondition())
				.accept(key, new AlkahestryCraftingRecipe(ingredient, charge, resultCount), null);
	}
}
