package com.ultra.megamod.mixin.puffish_attributes;

import com.llamalad7.mixinextras.sugar.Local;
import com.ultra.megamod.lib.puffish_attributes.api.DynamicModification;
import com.ultra.megamod.lib.puffish_attributes.api.PuffishAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Applies {@code STEALTH} to the target-detection range used by mob AI: subtracts stealth
 * from the search distance before the vanilla {@code Math.max(range, 2.0)} floor clamp.
 *
 * <p>1.21.11 Parchment renames Yarn's {@code TargetPredicate} to {@link TargetingConditions}
 * and its {@code test} method is unchanged in shape. The target still calls
 * {@code Math.max(double, double)} with the range multiplied by visibility percent as
 * the first argument (ordinal 1 LivingEntity capture is the target being tested, which
 * matches the Yarn reference's {@code ordinal = 1}).</p>
 */
@Mixin(TargetingConditions.class)
public class TargetPredicateMixin {

	@ModifyArg(
			method = "test",
			at = @At(
					value = "INVOKE",
					target = "Ljava/lang/Math;max(DD)D"
			),
			index = 0
	)
	private double modifyArgAtMax(double distance, @Local(argsOnly = true, ordinal = 1) LivingEntity targetEntity) {
		return DynamicModification.create()
				.withNegative(PuffishAttributes.STEALTH, targetEntity)
				.applyTo(distance);
	}

}
