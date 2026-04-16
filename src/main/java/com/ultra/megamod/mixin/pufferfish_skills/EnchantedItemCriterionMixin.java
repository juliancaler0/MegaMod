package com.ultra.megamod.mixin.pufferfish_skills;

import net.minecraft.advancements.criterion.EnchantedItemTrigger;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.EnchantItemExperienceSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantedItemTrigger.class)
public class EnchantedItemCriterionMixin {

	@Inject(method = "trigger", at = @At("HEAD"))
	private void injectAtTrigger(ServerPlayer serverPlayer, ItemStack stack, int levels, CallbackInfo ci) {
		SkillsAPI.updateExperienceSources(
				serverPlayer,
				EnchantItemExperienceSource.class,
				es -> (int) Math.round(es.calculation().evaluate(
						new EnchantItemExperienceSource.Data(serverPlayer, stack, levels)
				))
		);
	}
}
