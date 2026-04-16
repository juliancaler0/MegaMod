package com.ultra.megamod.mixin.pufferfish_skills;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.server.level.ServerPlayer;
import com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.CraftItemExperienceSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
public abstract class CraftingResultSlotMixin {

	@Shadow
	@Final
	private Player player;

	@Shadow
	private int amount;

	@Inject(
			method = "onCrafted(Lnet/minecraft/item/ItemStack;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/ItemStack;onCraftByPlayer(Lnet/minecraft/entity/player/Player;I)V"
			)
	)
	private void injectAtOnCraftByPlayer(ItemStack stack, CallbackInfo ci) {
		if (player instanceof ServerPlayer serverPlayer) {
			SkillsAPI.updateExperienceSources(
					serverPlayer,
					CraftItemExperienceSource.class,
					es -> (int) Math.round(es.calculation().evaluate(
							new CraftItemExperienceSource.Data(serverPlayer, stack)
					) * amount)
			);
		}
	}

}
