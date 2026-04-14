package com.ultra.megamod.reliquary.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import com.ultra.megamod.reliquary.Reliquary;
import com.ultra.megamod.reliquary.compat.jei.alkahestry.AlkahestryChargingRecipeCategory;
import com.ultra.megamod.reliquary.compat.jei.alkahestry.AlkahestryCraftingRecipeCategory;
import com.ultra.megamod.reliquary.compat.jei.cauldron.CauldronRecipeCategory;
import com.ultra.megamod.reliquary.compat.jei.cauldron.CauldronRecipeMaker;
import com.ultra.megamod.reliquary.compat.jei.infernaltear.InfernalTearRecipeCategory;
import com.ultra.megamod.reliquary.compat.jei.infernaltear.InfernalTearRecipeMaker;
import com.ultra.megamod.reliquary.compat.jei.lingering.ArrowShotRecipeMaker;
import com.ultra.megamod.reliquary.compat.jei.magazines.MagazineRecipeMaker;
import com.ultra.megamod.reliquary.compat.jei.mortar.MortarRecipeCategory;
import com.ultra.megamod.reliquary.compat.jei.mortar.MortarRecipeMaker;
import com.ultra.megamod.reliquary.crafting.AlkahestryRecipeRegistry;
import com.ultra.megamod.reliquary.init.ModBlocks;
import com.ultra.megamod.reliquary.init.ModDataComponents;
import com.ultra.megamod.reliquary.init.ModItems;
import com.ultra.megamod.reliquary.reference.Config;

import java.util.Collections;
import java.util.Optional;

@SuppressWarnings("unused") //plugin class is used by JEI's reflection
@JeiPlugin
public class ReliquaryPlugin implements IModPlugin {
	private static final String EFFECTS_TAG = "effects";

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		registerNbtSubtypeInterpreter(registration, ModItems.MOB_CHARM_FRAGMENT.get(), ModDataComponents.ENTITY_NAME.get());
		registerNbtSubtypeInterpreter(registration, ModItems.MOB_CHARM.get(), ModDataComponents.ENTITY_NAME.get());
		if (Boolean.FALSE.equals(Config.COMMON.disable.disablePotions.get())) {
			registerNbtSubtypeInterpreter(registration, ModItems.POTION_ESSENCE.get(), DataComponents.POTION_CONTENTS);
			registerNbtSubtypeInterpreter(registration, ModItems.POTION.get(), DataComponents.POTION_CONTENTS);
			registerNbtSubtypeInterpreter(registration, ModItems.SPLASH_POTION.get(), DataComponents.POTION_CONTENTS);
			registerNbtSubtypeInterpreter(registration, ModItems.LINGERING_POTION.get(), DataComponents.POTION_CONTENTS);
			registerNbtSubtypeInterpreter(registration, ModItems.TIPPED_ARROW.get(), DataComponents.POTION_CONTENTS);
			registerNbtSubtypeInterpreter(registration, ModItems.NEUTRAL_BULLET.get(), DataComponents.POTION_CONTENTS);
			registerNbtSubtypeInterpreter(registration, ModItems.NEUTRAL_MAGAZINE.get(), DataComponents.POTION_CONTENTS);
		}
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

		if (Boolean.FALSE.equals(Config.COMMON.disable.disableAlkahestry.get())) {
			registration.addRecipeCategories(new AlkahestryCraftingRecipeCategory(guiHelper));
			registration.addRecipeCategories(new AlkahestryChargingRecipeCategory(guiHelper));
		}

		if (Boolean.FALSE.equals(Config.COMMON.disable.disablePotions.get())) {
			registration.addRecipeCategories(new MortarRecipeCategory(guiHelper));
			registration.addRecipeCategories(new CauldronRecipeCategory(guiHelper));
		}
		registration.addRecipeCategories(new InfernalTearRecipeCategory(guiHelper));
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(Blocks.CRAFTING_TABLE), AlkahestryChargingRecipeCategory.TYPE, AlkahestryCraftingRecipeCategory.TYPE);

		registration.addRecipeCatalyst(new ItemStack(ModBlocks.APOTHECARY_MORTAR.get()), MortarRecipeCategory.TYPE);
		registration.addRecipeCatalyst(new ItemStack(ModBlocks.APOTHECARY_CAULDRON.get()), CauldronRecipeCategory.TYPE);
		registration.addRecipeCatalyst(new ItemStack(ModItems.INFERNAL_TEAR.get()), InfernalTearRecipeCategory.TYPE);
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		if (Boolean.FALSE.equals(Config.COMMON.disable.disableAlkahestry.get())) {
			registration.addRecipes(AlkahestryCraftingRecipeCategory.TYPE, AlkahestryRecipeRegistry.getCraftingRecipes());
			registration.addRecipes(AlkahestryChargingRecipeCategory.TYPE, AlkahestryRecipeRegistry.getChargingRecipes());
		}
		if (Boolean.FALSE.equals(Config.COMMON.disable.disablePotions.get())) {
			registration.addRecipes(MortarRecipeCategory.TYPE, MortarRecipeMaker.getRecipes());
			registration.addRecipes(CauldronRecipeCategory.TYPE, CauldronRecipeMaker.getRecipes());
			registration.addRecipes(RecipeTypes.CRAFTING, ArrowShotRecipeMaker.getRecipes(new ItemStack(ModItems.TIPPED_ARROW.get()), new ItemStack(Items.ARROW), 0.125F, "arrow"));
			registration.addRecipes(RecipeTypes.CRAFTING, ArrowShotRecipeMaker.getRecipes(new ItemStack(ModItems.NEUTRAL_BULLET.get()), "bullet"));
		}
		if (Boolean.FALSE.equals(Config.COMMON.disable.disableHandgun.get())) {
			registration.addRecipes(RecipeTypes.CRAFTING, MagazineRecipeMaker.getRecipes());
		}
		if (Boolean.FALSE.equals(Config.COMMON.disable.disableCharms.get())) {
			registration.addRecipes(RecipeTypes.CRAFTING, MobCharmRecipeMaker.getRecipes());
			registerMobCharmBeltRecipe(registration);
		}
		if (Boolean.FALSE.equals(Config.COMMON.disable.disableSpawnEggRecipes.get())) {
			registration.addRecipes(RecipeTypes.CRAFTING, SpawnEggRecipeMaker.getRecipes());
		}
		registration.addRecipes(InfernalTearRecipeCategory.TYPE, InfernalTearRecipeMaker.getRecipes());

		ItemDescriptionBuilder.addIngredientInfo(registration);
	}

	private void registerMobCharmBeltRecipe(IRecipeRegistration registration) {
		NonNullList<ItemStack> fragments = NonNullList.create();
		ModItems.MOB_CHARM_FRAGMENT.get().addCreativeTabItems(fragments::add);
		ItemStack[] fragmentStacks = fragments.toArray(new ItemStack[0]);

		ShapedRecipePattern pattern = new ShapedRecipePattern(3, 3, NonNullList.of(Ingredient.EMPTY,
				Ingredient.of(() -> Items.LEATHER),
				Ingredient.of(() -> Items.LEATHER),
				Ingredient.of(() -> Items.LEATHER),
				Ingredient.of(fragmentStacks),
				Ingredient.EMPTY,
				Ingredient.of(fragmentStacks),
				Ingredient.of(fragmentStacks),
				Ingredient.of(fragmentStacks),
				Ingredient.of(fragmentStacks)
		), Optional.empty());

		registration.addRecipes(RecipeTypes.CRAFTING, Collections.singletonList(new RecipeHolder<>(Reliquary.getRL("items/mob_charm_belt"),
				new ShapedRecipe("", CraftingBookCategory.MISC, pattern, new ItemStack(ModItems.MOB_CHARM_BELT.get())))));
	}

	private void registerNbtSubtypeInterpreter(ISubtypeRegistration registration, Item item, DataComponentType<?> component) {
		registration.registerSubtypeInterpreter(item, new ComponentSubtypeInterpreter(component));
	}

	@Override
	public ResourceLocation getPluginUid() {
		return Reliquary.getRL("default");
	}
}
