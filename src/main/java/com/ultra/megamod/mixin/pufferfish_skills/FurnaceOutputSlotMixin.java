package com.ultra.megamod.mixin.pufferfish_skills;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.server.level.ServerPlayer;
import com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.SmeltItemExperienceSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FurnaceResultSlot.class)
public abstract class FurnaceOutputSlotMixin {

	@Shadow
	@Final
	private Player player;

	@Shadow
	private int removeCount;

	@Inject(
			method = "checkTakeAchievements(Lnet/minecraft/world/item/ItemStack;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemStack;onCraftedBy(Lnet/minecraft/world/entity/player/Player;I)V"
			)
	)
	private void injectAtOnCraftByPlayer(ItemStack stack, CallbackInfo ci) {
		if (player instanceof ServerPlayer serverPlayer) {
			final int amount = removeCount;
			SkillsAPI.updateExperienceSources(
					serverPlayer,
					SmeltItemExperienceSource.class,
					es -> (int) Math.round(es.calculation().evaluate(
							new SmeltItemExperienceSource.Data(serverPlayer, stack)
					) * amount)
			);
		}
	}

}
