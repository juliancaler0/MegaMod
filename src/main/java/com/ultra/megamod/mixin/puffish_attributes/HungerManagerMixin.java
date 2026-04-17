package com.ultra.megamod.mixin.puffish_attributes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ultra.megamod.lib.puffish_attributes.api.DynamicModification;
import com.ultra.megamod.lib.puffish_attributes.api.PuffishAttributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Applies {@code NATURAL_REGENERATION} to the hunger-tick healing and {@code STAMINA} to
 * the saturation-cap constants used in the same method.
 *
 * <p>1.21.11 Parchment renames Yarn's {@code HungerManager} to {@link FoodData}, its
 * {@code update} method to {@code tick}, and the {@code ServerPlayerEntity#heal(F)V}
 * signature is now {@link ServerPlayer}#heal(F)V. The "4.0f" constants in Yarn became
 * "6.0f" upstream in 1.21 when Mojang rebalanced saturation; the Signed ModifyConstant
 * ordinals still target the two appearances — first the {@code Math.min(saturationLevel,
 * 6.0F)} cap and then the exhaustion accumulator inside the same branch. {@code require=0}
 * tolerates future tweaks that drop one of the literals.</p>
 */
@Mixin(value = FoodData.class, priority = 1100)
public abstract class HungerManagerMixin {

	@WrapOperation(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerPlayer;heal(F)V"
			)
	)
	private void wrapOperationAtHeal(ServerPlayer player, float amount, Operation<Void> operation) {
		operation.call(player, Math.max(0.0f, DynamicModification.create()
				.withPositive(PuffishAttributes.NATURAL_REGENERATION, player)
				.applyTo(amount)));
	}

	@ModifyConstant(
			method = "tick",
			constant = @Constant(floatValue = 6.0f, ordinal = 0),
			require = 0
	)
	private float modifyConstant0AtUpdate(float value, ServerPlayer player) {
		return getStamina(player);
	}

	@ModifyConstant(
			method = "tick",
			constant = @Constant(floatValue = 6.0f, ordinal = 1),
			require = 0
	)
	private float modifyConstant1AtUpdate(float value, ServerPlayer player) {
		return getStamina(player);
	}

	@Unique
	private float getStamina(Player player) {
		return (float) player.getAttributeValue(PuffishAttributes.STAMINA);
	}
}
