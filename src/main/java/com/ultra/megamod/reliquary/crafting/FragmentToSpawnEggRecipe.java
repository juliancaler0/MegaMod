package com.ultra.megamod.reliquary.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import com.ultra.megamod.reliquary.init.ModItems;

import java.util.List;

public class FragmentToSpawnEggRecipe extends ShapelessRecipe {
	private final ShapelessRecipe recipeDelegate;

	public FragmentToSpawnEggRecipe(ShapelessRecipe recipeDelegate) {
		super(recipeDelegate.group(), CraftingBookCategory.MISC, recipeDelegate.result, List.of());
		this.recipeDelegate = recipeDelegate;
	}

	@Override
	public boolean matches(CraftingInput inv, Level level) {
		return recipeDelegate.matches(inv, level) && FragmentRecipeHelper.hasOnlyOneFragmentType(inv);
	}

	@Override
	public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) {
		return FragmentRecipeHelper.getRegistryName(inv).map(FragmentRecipeHelper::getSpawnEggStack)
				.orElse(new ItemStack(FragmentRecipeHelper.FALL_BACK_SPAWN_EGG));
	}

	@Override
	public RecipeSerializer<ShapelessRecipe> getSerializer() {
		@SuppressWarnings("unchecked")
		RecipeSerializer<ShapelessRecipe> ser = (RecipeSerializer<ShapelessRecipe>) (RecipeSerializer<?>) ModItems.FRAGMENT_TO_SPAWN_EGG_SERIALIZER.get();
		return ser;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	public static class Serializer implements RecipeSerializer<FragmentToSpawnEggRecipe> {
		private static final MapCodec<FragmentToSpawnEggRecipe> CODEC = RecipeSerializer.SHAPELESS_RECIPE.codec()
				.xmap(FragmentToSpawnEggRecipe::new, recipe -> recipe.recipeDelegate);
		private static final StreamCodec<RegistryFriendlyByteBuf, FragmentToSpawnEggRecipe> STREAM_CODEC = RecipeSerializer.SHAPELESS_RECIPE.streamCodec()
				.map(FragmentToSpawnEggRecipe::new, recipe -> recipe.recipeDelegate);

		@Override
		public MapCodec<FragmentToSpawnEggRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, FragmentToSpawnEggRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
