package com.ultra.megamod.reliquary.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;
import com.ultra.megamod.reliquary.init.ModItems;
import com.ultra.megamod.reliquary.item.AlkahestryTomeItem;

import java.util.List;

public class AlkahestryDrainRecipe implements CraftingRecipe {
	private final int chargeToDrain;
	private final ItemStack result;

	public AlkahestryDrainRecipe(int chargeToDrain, ItemStack result) {
		this.chargeToDrain = chargeToDrain;
		this.result = result;
		AlkahestryRecipeRegistry.setDrainRecipe(this);
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public boolean matches(CraftingInput inv, Level level) {
		boolean hasTome = false;
		ItemStack tome = ItemStack.EMPTY;
		for (int slot = 0; slot < inv.size(); slot++) {
			ItemStack stack = inv.getItem(slot);
			if (stack.isEmpty()) {
				continue;
			}
			if (!hasTome && stack.getItem() == ModItems.ALKAHESTRY_TOME.get()) {
				hasTome = true;
				tome = stack;
			} else {
				return false;
			}
		}

		return hasTome && AlkahestryTomeItem.getCharge(tome) > 0;
	}

	@Override
	public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) {
		ItemStack tome = getTome(inv).copy();

		int charge = AlkahestryTomeItem.getCharge(tome);
		ItemStack ret = result.copy();
		ret.setCount(Math.min(ret.getMaxStackSize(), charge / chargeToDrain));

		return ret;
	}

	private ItemStack getTome(CraftingInput inv) {
		for (int slot = 0; slot < inv.size(); slot++) {
			ItemStack stack = inv.getItem(slot);
			if (stack.getItem() == ModItems.ALKAHESTRY_TOME.get()) {
				return stack;
			}
		}

		return ItemStack.EMPTY;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
		NonNullList<ItemStack> ret = CraftingRecipe.super.getRemainingItems(inv);
		for (int slot = 0; slot < inv.size(); slot++) {
			ItemStack stack = inv.getItem(slot);
			if (stack.getItem() == ModItems.ALKAHESTRY_TOME.get()) {
				ItemStack tome = stack.copy();
				int charge = AlkahestryTomeItem.getCharge(tome);
				int itemCount = Math.min(result.getMaxStackSize(), charge / chargeToDrain);
				ModItems.ALKAHESTRY_TOME.get().useCharge(tome, itemCount * chargeToDrain);
				ret.set(slot, tome);
			}
		}

		return ret;
	}

	@Override
	public RecipeSerializer<AlkahestryDrainRecipe> getSerializer() {
		return (RecipeSerializer<AlkahestryDrainRecipe>) ModItems.ALKAHESTRY_DRAIN_SERIALIZER.get();
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.MISC;
	}

	@Override
	public PlacementInfo placementInfo() {
		return PlacementInfo.NOT_PLACEABLE;
	}

	@Override
	public List<RecipeDisplay> display() {
		return List.of();
	}

	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return result;
	}

	public ItemStack getResult() {
		return result;
	}

	private Integer getChargeToDrain() {
		return chargeToDrain;
	}

	public static class Serializer implements RecipeSerializer<AlkahestryDrainRecipe> {
		private static final MapCodec<AlkahestryDrainRecipe> CODEC = RecordCodecBuilder.mapCodec(
				instance -> instance.group(
								Codec.INT.fieldOf("charge").forGetter((AlkahestryDrainRecipe recipe) -> recipe.chargeToDrain),
								ItemStack.CODEC.fieldOf("result").forGetter((AlkahestryDrainRecipe recipe) -> recipe.result)
						)
						.apply(instance, AlkahestryDrainRecipe::new));

		private static final StreamCodec<RegistryFriendlyByteBuf, AlkahestryDrainRecipe> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT,
				AlkahestryDrainRecipe::getChargeToDrain,
				ItemStack.STREAM_CODEC,
				AlkahestryDrainRecipe::getResult,
				AlkahestryDrainRecipe::new
		);

		@Override
		public MapCodec<AlkahestryDrainRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, AlkahestryDrainRecipe> streamCodec() {
			return STREAM_CODEC;
		}

	}
}
