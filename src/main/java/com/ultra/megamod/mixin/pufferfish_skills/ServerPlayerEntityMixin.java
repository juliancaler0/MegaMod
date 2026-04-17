package com.ultra.megamod.mixin.pufferfish_skills;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.IncreaseStatExperienceSource;
import com.ultra.megamod.lib.pufferfish_skills.reward.builtin.AttributeReward;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void injectAtInit(CallbackInfo ci) {
		SkillsAPI.updateRewards((ServerPlayer) (Object) this, AttributeReward.class);
	}

	@Inject(method = "awardStat(Lnet/minecraft/stats/Stat;I)V", at = @At("HEAD"))
	private void injectAtIncreaseStat(Stat<?> stat, int amount, CallbackInfo ci) {
		var player = (ServerPlayer) (Object) this;
		SkillsAPI.updateExperienceSources(
				player,
				IncreaseStatExperienceSource.class,
				es -> (int) Math.round(es.calculation().evaluate(
						new IncreaseStatExperienceSource.Data(player, stat, amount)
				))
		);
	}
}
