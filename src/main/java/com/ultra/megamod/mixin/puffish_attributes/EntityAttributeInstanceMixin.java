package com.ultra.megamod.mixin.puffish_attributes;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.ultra.megamod.lib.puffish_attributes.api.DynamicEntityAttribute;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes {@link DynamicEntityAttribute} instances report {@code NaN} as their base value
 * and ignore {@code setBaseValue} calls so vanilla computations can't clobber the value
 * that {@code DynamicModificationImpl} produces from the stacked modifiers.
 *
 * <p>1.21.11 Parchment renamed Yarn's {@code computeValue} to {@code calculateValue} and
 * replaced the NBT round-trip ({@code toNbt}/{@code fromNbt}) with the
 * {@code pack()}/{@code apply(Packed)} pair. The base-value persistence guard is therefore
 * moved into {@code pack()}, which is the only place {@code baseValue} is serialized today.
 */
@Mixin(AttributeInstance.class)
public class EntityAttributeInstanceMixin {
	@Shadow
	@Final
	private Holder<Attribute> attribute;

	@Inject(
			method = "getBaseValue",
			at = @At("HEAD"),
			cancellable = true
	)
	private void injectAtGetBaseValue(CallbackInfoReturnable<Double> cir) {
		if (attribute.value() instanceof DynamicEntityAttribute) {
			cir.setReturnValue(Double.NaN);
		}
	}

	@Inject(
			method = "calculateValue",
			at = @At("HEAD"),
			cancellable = true
	)
	private void injectAtComputeValue(CallbackInfoReturnable<Double> cir) {
		if (attribute.value() instanceof DynamicEntityAttribute) {
			cir.setReturnValue(Double.NaN);
		}
	}

	@Inject(
			method = "setBaseValue",
			at = @At("HEAD"),
			cancellable = true
	)
	private void injectAtSetBaseValue(double baseValue, CallbackInfo ci) {
		if (attribute.value() instanceof DynamicEntityAttribute) {
			ci.cancel();
		}
	}

	@ModifyExpressionValue(
			method = "pack",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;baseValue:D",
					opcode = Opcodes.GETFIELD
			),
			require = 0
	)
	private double modifyExpressionValueAtBaseValue(double original) {
		return attribute.value() instanceof DynamicEntityAttribute ? 0 : original;
	}
}
