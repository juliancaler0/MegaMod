package com.ultra.megamod.mixin.puffish_attributes;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.ultra.megamod.lib.puffish_attributes.api.DynamicModification;
import com.ultra.megamod.lib.puffish_attributes.api.PuffishAttributes;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Player-scoped mixin: registers player-only attributes, applies {@code KNOCKBACK} to the
 * melee-attack outgoing knockback, scales movement speed by {@code SPRINTING_SPEED} when
 * sprinting, and layers {@code PICKAXE/AXE/SHOVEL/MINING/BREAKING_SPEED} on top of the
 * vanilla block-breaking multiplier.
 *
 * <p>1.21.11 Parchment renames Yarn's {@code createPlayerAttributes} to
 * {@link Player#createAttributes()}, {@code createLivingAttributes} stays but now returns
 * {@link AttributeSupplier.Builder}. {@code getBlockBreakingSpeed} is {@code getDestroySpeed}
 * (with a nullable {@code BlockPos} overload). Yarn's {@code ItemStack.getMiningSpeedMultiplier}
 * is now {@code ItemStack.getDestroySpeed(BlockState)}. The Yarn hook into
 * {@code onTargetDamaged} in {@code attack} has moved — 1.21.11 routes extra knockback
 * through {@link LivingEntity#causeExtraKnockback(Entity, float, net.minecraft.world.phys.Vec3)};
 * we additively boost that argument here. {@code StatusEffectUtil.hasHaste} is now
 * {@code MobEffectUtil.hasDigSpeed}. {@code getMovementSpeed} is {@code getSpeed}.</p>
 */
@Mixin(value = Player.class, priority = 1100)
public abstract class PlayerEntityMixin {

	private static final double VANILLA_KNOCKBACK = 0.4;

	@ModifyExpressionValue(
			method = "createAttributes",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;createLivingAttributes()Lnet/minecraft/world/entity/ai/attributes/AttributeSupplier$Builder;")
	)
	private static AttributeSupplier.Builder modifyExpressionValueAtCreateLivingAttributes(AttributeSupplier.Builder builder) {
		return builder
				.add(PuffishAttributes.STAMINA)
				.add(PuffishAttributes.FORTUNE)
				.add(PuffishAttributes.MINING_SPEED)
				.add(PuffishAttributes.BREAKING_SPEED)
				.add(PuffishAttributes.PICKAXE_SPEED)
				.add(PuffishAttributes.AXE_SPEED)
				.add(PuffishAttributes.SHOVEL_SPEED)
				.add(PuffishAttributes.SPRINTING_SPEED)
				.add(PuffishAttributes.MOUNT_SPEED)
				.add(PuffishAttributes.CONSUMING_SPEED)
				.add(PuffishAttributes.KNOCKBACK)
				.add(PuffishAttributes.REPAIR_COST)
				.add(PuffishAttributes.NATURAL_REGENERATION)
				.add(PuffishAttributes.TAMED_DAMAGE)
				.add(PuffishAttributes.TAMED_RESISTANCE)
				.add(PuffishAttributes.EXPERIENCE);
	}

	/**
	 * Yarn's reference injected an {@code addVelocity / takeKnockback} pair at
	 * {@code onTargetDamaged}. In 1.21.11 the canonical knockback path is
	 * {@link LivingEntity#causeExtraKnockback(Entity, float, net.minecraft.world.phys.Vec3)},
	 * so we wrap the {@code knockback} float argument in {@code Player#attack} and add
	 * the dynamic bonus on top. The Yarn reference computed
	 * {@code dm.applyTo(0.4) - 0.4}; we do the same here for parity.
	 */
	@WrapOperation(
			method = "attack",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/LivingEntity;causeExtraKnockback(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/phys/Vec3;)V"
			),
			require = 0
	)
	private void wrapOperationAtCauseExtraKnockback(LivingEntity self, Entity target, float knockback, net.minecraft.world.phys.Vec3 deltaMovement, Operation<Void> operation) {
		var player = (Player) (Object) this;
		var bonus = DynamicModification.create()
				.withPositive(PuffishAttributes.KNOCKBACK, player)
				.applyTo(VANILLA_KNOCKBACK) - VANILLA_KNOCKBACK;
		operation.call(self, target, knockback + (float) bonus, deltaMovement);
	}

	@ModifyReturnValue(method = "getSpeed", at = @At("RETURN"))
	private float injectAtGetMovementSpeed(float speed) {
		var player = (Player) (Object) this;

		if (!player.isSprinting()) {
			return speed;
		}

		return DynamicModification.create()
				.withPositive(PuffishAttributes.SPRINTING_SPEED, player)
				.applyTo(speed);
	}

	/**
	 * Applies PICKAXE/AXE/SHOVEL/MINING_SPEED to the tool mining multiplier before the
	 * vanilla {@code f > 1.0F} fast-path check. Yarn targets
	 * {@code ItemStack.getMiningSpeedMultiplier(BlockState)}; Parchment renames this to
	 * {@code ItemStack.getDestroySpeed(BlockState)}. The wrapping preserves the "only
	 * modify when the stack actually breaks the block faster than bare hands" guard from
	 * the reference so vanilla enchantments keep their rules.
	 */
	@WrapOperation(
			method = "getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)F",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemStack;getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;)F"
			),
			require = 0
	)
	private float wrapOperationAtGetMiningSpeedMultiplier(ItemStack itemStack, BlockState blockState, Operation<Float> operation) {
		var speed = operation.call(itemStack, blockState);

		// This check is required to not break vanilla enchantments behavior
		if (speed <= 1.0f) {
			return speed;
		}

		var player = (Player) (Object) this;

		var dm = DynamicModification.create();

		if (itemStack.is(ItemTags.PICKAXES)) {
			dm.withPositive(PuffishAttributes.PICKAXE_SPEED, player);
		}
		if (itemStack.is(ItemTags.AXES)) {
			dm.withPositive(PuffishAttributes.AXE_SPEED, player);
		}
		if (itemStack.is(ItemTags.SHOVELS)) {
			dm.withPositive(PuffishAttributes.SHOVEL_SPEED, player);
		}
		dm.withPositive(PuffishAttributes.MINING_SPEED, player);

		return dm.applyTo(speed);
	}

	/**
	 * Applies BREAKING_SPEED after the vanilla Haste-status check has populated the
	 * local {@code f} speed variable. In 1.21.11 the call is
	 * {@code MobEffectUtil.hasDigSpeed(LivingEntity)} (Yarn: {@code StatusEffectUtil.hasHaste}).
	 * We capture the local float and rewrite it in place via {@link LocalFloatRef}, mirroring
	 * the reference.
	 */
	@Inject(
			method = "getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)F",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/effect/MobEffectUtil;hasDigSpeed(Lnet/minecraft/world/entity/LivingEntity;)Z"
			),
			require = 0
	)
	private void injectAtGetBlockBreakingSpeed(BlockState state, net.minecraft.core.BlockPos pos, CallbackInfoReturnable<Float> cir, @Local LocalFloatRef speed) {
		var player = (Player) (Object) this;

		speed.set(DynamicModification.create()
				.withPositive(PuffishAttributes.BREAKING_SPEED, player)
				.applyTo(speed.get()));
	}

}
