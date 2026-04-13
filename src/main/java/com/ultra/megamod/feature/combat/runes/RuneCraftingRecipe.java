package com.ultra.megamod.feature.combat.runes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Custom recipe type for the Rune Crafting Altar.
 * Two ingredients (base + addition) produce a result.
 */
public class RuneCraftingRecipe implements Recipe<RuneCraftingRecipeInput> {
    final Ingredient base;
    final Ingredient addition;
    final ItemStack result;

    public RuneCraftingRecipe(Ingredient base, Ingredient addition, ItemStack result) {
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    @Override
    public boolean matches(RuneCraftingRecipeInput input, Level level) {
        return this.base.test(input.getItem(0)) && this.addition.test(input.getItem(1));
    }

    @Override
    public ItemStack assemble(RuneCraftingRecipeInput input, HolderLookup.Provider registries) {
        ItemStack itemStack = input.base().transmuteCopy(this.result.getItem(), this.result.getCount());
        itemStack.applyComponents(this.result.getComponentsPatch());
        return itemStack;
    }

    @Override
    public RecipeSerializer<RuneCraftingRecipe> getSerializer() {
        return RuneCrafting.RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<RuneCraftingRecipe> getType() {
        return RuneCrafting.RECIPE_TYPE.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of();
    }

    public boolean isEmpty() {
        return this.base.isEmpty() || this.addition.isEmpty();
    }

    public static final String NAME = "crafting";

    // -- Serializer --

    public static class Serializer implements RecipeSerializer<RuneCraftingRecipe> {
        private static final MapCodec<RuneCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                                Ingredient.CODEC.fieldOf("base").forGetter(recipe -> recipe.base),
                                Ingredient.CODEC.fieldOf("addition").forGetter(recipe -> recipe.addition),
                                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
                        )
                        .apply(instance, RuneCraftingRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, RuneCraftingRecipe> STREAM_CODEC =
                StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        @Override
        public MapCodec<RuneCraftingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, RuneCraftingRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static RuneCraftingRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            Ingredient base = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            Ingredient addition = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
            return new RuneCraftingRecipe(base, addition, result);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, RuneCraftingRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.base);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.addition);
            ItemStack.STREAM_CODEC.encode(buf, recipe.result);
        }
    }
}
