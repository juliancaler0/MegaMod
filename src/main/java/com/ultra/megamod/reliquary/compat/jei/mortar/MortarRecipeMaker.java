package com.ultra.megamod.reliquary.compat.jei.mortar;

import net.minecraft.world.item.ItemStack;
import com.ultra.megamod.reliquary.init.ModItems;
import com.ultra.megamod.reliquary.util.potions.PotionEssence;
import com.ultra.megamod.reliquary.util.potions.PotionHelper;
import com.ultra.megamod.reliquary.util.potions.PotionIngredient;
import com.ultra.megamod.reliquary.util.potions.PotionMap;

import java.util.ArrayList;
import java.util.List;

public class MortarRecipeMaker {
	private MortarRecipeMaker() {
	}

	public static List<MortarRecipeJEI> getRecipes() {
		ArrayList<MortarRecipeJEI> recipes = new ArrayList<>();

		for (PotionEssence essence : PotionMap.potionCombinations) {

			List<ItemStack> inputs = essence.getIngredients().stream().map(PotionIngredient::getItem).toList();

			ItemStack output = new ItemStack(ModItems.POTION_ESSENCE.get(), 1);
			PotionHelper.addPotionContentsToStack(output, essence.getPotionContents());

			recipes.add(new MortarRecipeJEI(inputs, output));
		}

		return recipes;
	}
}
