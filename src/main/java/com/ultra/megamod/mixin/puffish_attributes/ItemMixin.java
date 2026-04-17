package com.ultra.megamod.mixin.puffish_attributes;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.ultra.megamod.lib.puffish_attributes.api.DynamicModification;
import com.ultra.megamod.lib.puffish_attributes.api.PuffishAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Applies {@code CONSUMING_SPEED} as a reciprocal scale on the use-time in ticks for any
 * consumable item (food, potions, milk buckets, etc.), so higher consuming_speed means
 * fewer ticks.
 *
 * <p>1.21.11 Parchment renames Yarn's {@code getMaxUseTime} to {@code getUseDuration}
 * and {@code ConsumableComponent#getConsumeTicks} to {@code Consumable#consumeTicks}.
 * The Local capture target is still {@code LivingEntity user} (argsOnly).</p>
 */
@Mixin(Item.class)
public abstract class ItemMixin {

	@ModifyExpressionValue(
			method = "getUseDuration",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/component/Consumable;consumeTicks()I"
			),
			require = 0
	)
	private int modifyExpressionValueAtGetConsumeTicks(
			int ticks,
			@Local(argsOnly = true) LivingEntity user
	) {
		return Math.max(1, Math.round(DynamicModification.create()
				.withPositive(PuffishAttributes.CONSUMING_SPEED, user)
				.applyToReciprocal(ticks)));
	}

}
