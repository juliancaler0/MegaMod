package com.ultra.megamod.mixin.puffish_attributes;

import com.ultra.megamod.lib.puffish_attributes.api.DynamicModification;
import com.ultra.megamod.lib.puffish_attributes.api.PuffishAttributes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies {@code LIFE_STEAL} and {@code DAMAGE_REFLECTION} when damage is recorded into
 * the victim's combat log. The injection runs on every successful hit, so a life-steal
 * attacker heals for a fraction of each damage instance and a reflection victim returns
 * some damage using the vanilla thorns source.
 *
 * <p>1.21.11 Parchment renames Yarn's {@code DamageTracker} to {@link CombatTracker},
 * its private {@code entity} field (the victim) is renamed {@code mob}, and the tick
 * hook method is renamed from {@code onDamage} to {@code recordDamage}. Yarn's
 * {@code DamageSource#getAttacker()} is now {@code getEntity()} and {@code isOf(...)}
 * is {@code is(...)} for both {@link net.minecraft.resources.ResourceKey} and
 * {@link net.minecraft.tags.TagKey}. The ServerWorld-aware {@code damage(...)} call is
 * the 1.21.11 entity-damage method {@code hurtServer(ServerLevel, DamageSource, float)}.</p>
 */
@Mixin(CombatTracker.class)
public class DamageTrackerMixin {

	@Shadow
	@Final
	private LivingEntity mob;

	@Inject(
			method = "recordDamage",
			at = @At("HEAD")
	)
	private void injectAtAttack(DamageSource damageSource, float damage, CallbackInfo ci) {
		if (damageSource.getEntity() instanceof LivingEntity attacker) {
			var lifeSteal = DynamicModification.create()
					.withPositive(PuffishAttributes.LIFE_STEAL, attacker)
					.relativeTo(damage);

			if (lifeSteal > 0) {
				attacker.heal((float) lifeSteal);
			}

			if (!damageSource.is(DamageTypes.THORNS)) {
				var reflection = DynamicModification.create()
						.withPositive(PuffishAttributes.DAMAGE_REFLECTION, mob)
						.relativeTo(damage);

				if (reflection > 0 && attacker.level() instanceof ServerLevel serverLevel) {
					attacker.hurtServer(serverLevel, serverLevel.damageSources().thorns(mob), (float) reflection);
				}
			}
		}
	}

}
