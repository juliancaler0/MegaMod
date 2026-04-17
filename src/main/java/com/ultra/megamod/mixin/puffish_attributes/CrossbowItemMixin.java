package com.ultra.megamod.mixin.puffish_attributes;

import com.ultra.megamod.lib.puffish_attributes.api.DynamicModification;
import com.ultra.megamod.lib.puffish_attributes.api.PuffishAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Applies {@code CROSSBOW_PROJECTILE_SPEED} to the crossbow's projectile velocity.
 *
 * <p>In 1.21.11 {@code CrossbowItem#shootProjectile} takes velocity as the fourth parameter
 * (ordinal 0 float, argsOnly). ModifyVariable at HEAD scales it before the vanilla call chain.</p>
 */
@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {

	@ModifyVariable(
			method = "shootProjectile",
			at = @At("HEAD"),
			ordinal = 0,
			argsOnly = true
	)
	private float modifyVelocityAtShootProjectile(float velocity, LivingEntity user) {
		return DynamicModification.create()
				.withPositive(PuffishAttributes.CROSSBOW_PROJECTILE_SPEED, user)
				.applyTo(velocity);
	}

}
