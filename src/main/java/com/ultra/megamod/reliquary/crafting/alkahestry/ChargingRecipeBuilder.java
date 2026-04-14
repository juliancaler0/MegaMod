package com.ultra.megamod.reliquary.crafting.alkahestry;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
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

	public void build(RecipeOutput recipeOutput, ResourceLocation id) {
		ResourceLocation fullId = Reliquary.getRL("alkahestry/charging/" + id.getPath());
		recipeOutput.withConditions(new AlkahestryEnabledCondition())
				.accept(fullId, new AlkahestryChargingRecipe(ingredient, charge), null);
	}
}
