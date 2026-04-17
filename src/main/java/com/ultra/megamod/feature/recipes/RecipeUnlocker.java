package com.ultra.megamod.feature.recipes;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.List;

/// Auto-unlocks all MegaMod crafting recipes in the recipe book on player login.
/// Backpack variants no longer have gating — everything is available once crafted.
@EventBusSubscriber(modid = "megamod")
public class RecipeUnlocker {

	@SubscribeEvent
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		Player player = event.getEntity();
		if (!(player instanceof ServerPlayer sp)) return;
		awardRecipes(sp);
	}

	public static void awardRecipes(ServerPlayer sp) {
		var recipeManager = sp.level().getServer().getRecipeManager();
		List<RecipeHolder<?>> toAward = new ArrayList<>();
		for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
			if (!"megamod".equals(holder.id().identifier().getNamespace())) continue;
			toAward.add(holder);
		}
		if (!toAward.isEmpty()) {
			sp.awardRecipes(toAward);
		}
	}
}
