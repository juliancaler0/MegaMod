package com.ultra.megamod.reliquary.crafting.alkahestry;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import com.ultra.megamod.reliquary.Reliquary;
import com.ultra.megamod.reliquary.crafting.AlkahestryChargingRecipe;
import com.ultra.megamod.reliquary.crafting.conditions.AlkahestryEnabledCondition;

public class ChargingRecipeBuilder {
	private final Ingredient ingredient;
	private final int charge;

	private ChargingRecipeBuilder(ItemLike ingredient, int charge) {
		this.ingredient = Ingredient.of(ingredient);
		this.charge = charge;
	}

	public static ChargingRecipeBuilder chargingRecipe(ItemLike result, int charge) {
		return new ChargingRecipeBuilder(result, charge);
	}

	public void build(RecipeOutput recipeOutput, Identifier id) {
		Identifier fullId = Reliquary.getRL("alkahestry/charging/" + id.getPath());
		ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, fullId);
		recipeOutput.withConditions(new AlkahestryEnabledCondition())
				.accept(key, new AlkahestryChargingRecipe(ingredient, charge), null);
	}
}
