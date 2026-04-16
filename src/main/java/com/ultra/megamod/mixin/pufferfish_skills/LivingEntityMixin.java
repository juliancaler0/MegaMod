package com.ultra.megamod.mixin.pufferfish_skills;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerLevel;
import com.ultra.megamod.lib.pufferfish_skills.access.DamageSourceAccess;
import com.ultra.megamod.lib.pufferfish_skills.access.WorldChunkAccess;
import com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.DealDamageExperienceSource;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.HealExperienceSource;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.KillEntityExperienceSource;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.SharedKillEntityExperienceSource;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.util.AntiFarmingPerEntity;
import com.ultra.megamod.lib.pufferfish_skills.util.AttackerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.WeakHashMap;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

	@Unique
	private int entityDroppedXp = 0;

	@Unique
	private final Map<ServerPlayer, Float> damageShare = new WeakHashMap<>();

	@Unique
	private final AntiFarmingPerEntity.State antiFarmingPerEntityState = new AntiFarmingPerEntity.State();

	@Inject(method = "heal", at = @At("TAIL"))
	private void injectAtHeal(float amount, CallbackInfo ci) {
		if (amount > 0) {
			if (((LivingEntity) (Object) this) instanceof ServerPlayer player) {
				SkillsAPI.updateExperienceSources(
						player,
						HealExperienceSource.class,
						es -> (int) Math.round(es.calculation().evaluate(
								new HealExperienceSource.Data(player, amount)
						))
				);
			}
		}
	}

	@Inject(method = "applyDamage", at = @At("TAIL"))
	private void injectAtApplyDamage(ServerLevel world, DamageSource source, float damage, CallbackInfo ci) {
		AttackerInfo.detect(source.getAttacker(), attackerInfo -> {
			var entity = ((LivingEntity) (Object) this);
			var weapon = ((DamageSourceAccess) source).getWeapon().orElse(ItemStack.EMPTY);
			var player = attackerInfo.player();

			var antiFarmingPerChunkState = ((WorldChunkAccess) entity.getWorld()
					.getLevelChunk(entity.getBlockPos()))
					.getAntiFarmingPerChunkState();
			antiFarmingPerChunkState.removeOutdated();

			damageShare.compute(player, (key, value) -> {
				if (value == null) {
					return damage;
				} else {
					return value + damage;
				}
			});

			antiFarmingPerEntityState.removeOutdated();
			SkillsAPI.updateExperienceSources(
					player,
					DealDamageExperienceSource.class,
					es -> {
						if (attackerInfo.matchesTamedActivity(es.tamedActivity())
								&& es.antiFarmingPerChunk()
								.map(antiFarmingPerChunkState::tryIncrement)
								.orElse(true)
						) {
							float limitedDamage = es.antiFarmingPerEntity()
									.map(antiFarming -> antiFarmingPerEntityState.addAndLimit(
											antiFarming,
											damage
									))
									.orElse(damage);
							if (limitedDamage > Mth.EPSILON) {
								return (int) Math.round(es.calculation().evaluate(
										new DealDamageExperienceSource.Data(
												player,
												entity,
												weapon,
												limitedDamage,
												source
										)
								));
							}
						}
						return 0;
					}
			);
		});
	}

	@Inject(method = "drop", at = @At("TAIL"))
	private void injectAtDrop(ServerLevel world, DamageSource source, CallbackInfo ci) {
		AttackerInfo.detect(source.getAttacker(), attackerInfo -> {
			var entity = ((LivingEntity) (Object) this);
			var weapon = ((DamageSourceAccess) source).getWeapon().orElse(ItemStack.EMPTY);
			var player = attackerInfo.player();

			var antiFarmingPerChunkState = ((WorldChunkAccess) entity.getWorld()
					.getLevelChunk(entity.getBlockPos()))
					.getAntiFarmingPerChunkState();
			antiFarmingPerChunkState.removeOutdated();

			SkillsAPI.updateExperienceSources(
					player,
					KillEntityExperienceSource.class,
					es -> {
						if (attackerInfo.matchesTamedActivity(es.tamedActivity())
								&& es
								.antiFarmingPerChunk()
								.map(antiFarmingPerChunkState::tryIncrement)
								.orElse(true)
						) {
							return (int) Math.round(es.calculation().evaluate(
									new KillEntityExperienceSource.Data(
											player,
											entity,
											weapon,
											source,
											entityDroppedXp
									)
							));
						}
						return 0;
					}
			);

			var entries = damageShare.entrySet();
			var totalDamage = entries.stream().mapToDouble(Map.Entry::getValue).sum();
			for (var entry : entries) {
				SkillsAPI.updateExperienceSources(
						entry.getKey(),
						SharedKillEntityExperienceSource.class,
						es -> {
							if (attackerInfo.matchesTamedActivity(es.tamedActivity())
									&& es.antiFarmingPerChunk()
									.map(antiFarmingPerChunkState::tryIncrement)
									.orElse(true)
							) {
								return (int) Math.round(es.calculation().evaluate(
										new SharedKillEntityExperienceSource.Data(
												entry.getKey(),
												entity,
												weapon,
												source,
												entityDroppedXp,
												totalDamage,
												entries.size(),
												entry.getValue() / totalDamage
										)
								));
							}
							return 0;
						}
				);
			}
		});
	}

	@ModifyArg(
			method = "dropExperience",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/ExperienceOrbEntity;spawn(Lnet/minecraft/server/world/ServerLevel;Lnet/minecraft/util/math/Vec3;I)V"
			),
			index = 2
	)
	private int injectAtDropExperience(int droppedXp) {
		entityDroppedXp = droppedXp;
		return droppedXp;
	}
}
