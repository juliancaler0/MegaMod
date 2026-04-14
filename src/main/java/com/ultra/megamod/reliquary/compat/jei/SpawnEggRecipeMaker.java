package com.ultra.megamod.reliquary.compat.jei;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import com.ultra.megamod.reliquary.Reliquary;
import com.ultra.megamod.reliquary.crafting.FragmentRecipeHelper;
import com.ultra.megamod.reliquary.init.ModItems;
import com.ultra.megamod.reliquary.item.MobCharmRegistry;

import java.util.ArrayList;
import java.util.List;

public class SpawnEggRecipeMaker {
	private SpawnEggRecipeMaker() {
	}

	public static List<RecipeHolder<CraftingRecipe>> getRecipes() {
		List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();

		for (ResourceLocation regName : MobCharmRegistry.getRegisteredNames()) {
			Ingredient fragmentIngredient = Ingredient.of(ModItems.MOB_CHARM_FRAGMENT.get().getStackFor(regName));
			Ingredient eggIngredient = Ingredient.of(Items.EGG);

			NonNullList<Ingredient> inputs = NonNullList.create();
			inputs.add(fragmentIngredient);
			inputs.add(fragmentIngredient);
			inputs.add(eggIngredient);

			ItemStack output = FragmentRecipeHelper.getSpawnEggStack(regName);

			ResourceLocation id = Reliquary.getRL("reliquary.fragment_to_spawn_egg." + output.getDescriptionId());
			recipes.add(new RecipeHolder<>(id, new ShapelessRecipe("reliquary.fragment_to_spawn_egg", CraftingBookCategory.MISC, output, inputs)));
		}

		return recipes;
	}
}
