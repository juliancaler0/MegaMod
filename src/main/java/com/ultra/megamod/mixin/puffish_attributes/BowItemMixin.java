package com.ultra.megamod.mixin.puffish_attributes;

import com.ultra.megamod.lib.puffish_attributes.api.DynamicModification;
import com.ultra.megamod.lib.puffish_attributes.api.PuffishAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Applies {@code BOW_PROJECTILE_SPEED} to the bow's projectile velocity.
 *
 * <p>In 1.21.11 {@code BowItem#shootProjectile(LivingEntity, Projectile, int, float, float, float, LivingEntity)}
 * is the override invoked by {@code ProjectileWeaponItem#shoot} for each arrow. The fourth
 * parameter (ordinal 0 float, argsOnly) is the per-projectile velocity.</p>
 */
@Mixin(BowItem.class)
public abstract class BowItemMixin {

	@ModifyVariable(
			method = "shootProjectile",
			at = @At("HEAD"),
			ordinal = 0,
			argsOnly = true
	)
	private float modifyVelocityAtShootProjectile(float velocity, LivingEntity user) {
		return DynamicModification.create()
				.withPositive(PuffishAttributes.BOW_PROJECTILE_SPEED, user)
				.applyTo(velocity);
	}

}
