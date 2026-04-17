package com.ultra.megamod.mixin.puffish_attributes;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.ultra.megamod.lib.puffish_attributes.api.DynamicModification;
import com.ultra.megamod.lib.puffish_attributes.api.PuffishAttributes;
import com.ultra.megamod.lib.puffish_attributes.util.DamageKind;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Central living-entity mixin: registers default attributes, applies offensive damage
 * scaling ({@code MELEE/RANGED/MAGIC/SWORD/AXE/TRIDENT/MACE/TAMED_DAMAGE}), defensive
 * shreds ({@code ARMOR_SHRED}, {@code TOUGHNESS_SHRED}, {@code PROTECTION_SHRED}),
 * healing, jump, fall reduction, and the composite resistance + shred applied after
 * the vanilla damage pipeline.
 *
 * <p>1.21.11 Parchment method renames applied:
 * <ul>
 *   <li>{@code damage(ServerWorld, DamageSource, float)} &rarr; {@code hurtServer(ServerLevel, DamageSource, float)}
 *   <li>{@code applyArmorToDamage(DamageSource, float)} &rarr; {@code getDamageAfterArmorAbsorb(DamageSource, float)}
 *   <li>{@code modifyAppliedDamage(DamageSource, float)} &rarr; {@code getDamageAfterMagicAbsorb(DamageSource, float)}
 *   <li>{@code computeFallDamage(float, float)} &rarr; {@code calculateFallDamage(double, float)} (int return)
 *   <li>{@code getJumpVelocity()} &rarr; {@code getJumpPower()}
 *   <li>{@code travelControlled} with {@code getSaddledSpeed} has been replaced by
 *       {@code travelRidden(Player, Vec3)}. The reference's {@code MOUNT_SPEED} patch
 *       hooked the per-frame {@code getSaddledSpeed} return; in 1.21.11 we instead scale
 *       the horse's {@code MOVEMENT_SPEED} attribute at rider time, approximating the
 *       same multiplier.
 *   <li>{@code createLivingAttributes} now returns {@link AttributeSupplier.Builder}
 *       (was {@code DefaultAttributeContainer.Builder}).
 *   <li>{@code Tameable} is {@link OwnableEntity} in 1.21.11.
 *   <li>{@code DamageUtil#getDamageLeft(...)} and {@code DamageUtil#getInflictedDamage(...)}
 *       no longer exist separately. The armor / protection reductions now happen inside
 *       {@code getDamageAfterArmorAbsorb} / {@code getDamageAfterMagicAbsorb} via
 *       {@code CombatRules#getDamageAfterAbsorb(...)} / {@code getDamageAfterMagicAbsorb(...)}.
 *       We redirect those two {@code CombatRules} calls to inject the shred terms at
 *       the exact point where they applied in the Yarn reference.
 *   <li>{@code ItemStack#isIn(TagKey)} &rarr; {@code is(TagKey)}; {@code getMainHandStack} &rarr;
 *       {@code getMainHandItem}.
 *   <li>{@code DamageSource#getAttacker} &rarr; {@code getEntity}.
 *   <li>{@code LivingEntity#getWorld()} &rarr; {@code level()}.
 *   <li>{@code damage(...)} with {@code ServerWorld} - {@code hurtServer(ServerLevel, ...)}.
 * </ul>
 */
@Mixin(value = LivingEntity.class, priority = 1100)
public abstract class LivingEntityMixin {

	@ModifyExpressionValue(
			method = "createLivingAttributes",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeSupplier;builder()Lnet/minecraft/world/entity/ai/attributes/AttributeSupplier$Builder;")
	)
	private static AttributeSupplier.Builder modifyExpressionValueAtBuilder(AttributeSupplier.Builder builder) {
		return builder
				.add(PuffishAttributes.MAGIC_DAMAGE)
				.add(PuffishAttributes.MELEE_DAMAGE)
				.add(PuffishAttributes.RANGED_DAMAGE)
				.add(PuffishAttributes.SWORD_DAMAGE)
				.add(PuffishAttributes.AXE_DAMAGE)
				.add(PuffishAttributes.TRIDENT_DAMAGE)
				.add(PuffishAttributes.MACE_DAMAGE)
				.add(PuffishAttributes.HEALING)
				.add(PuffishAttributes.JUMP)
				.add(PuffishAttributes.RESISTANCE)
				.add(PuffishAttributes.MAGIC_RESISTANCE)
				.add(PuffishAttributes.MELEE_RESISTANCE)
				.add(PuffishAttributes.RANGED_RESISTANCE)
				.add(PuffishAttributes.ARMOR_SHRED)
				.add(PuffishAttributes.TOUGHNESS_SHRED)
				.add(PuffishAttributes.PROTECTION_SHRED)
				.add(PuffishAttributes.RESISTANCE_SHRED)
				.add(PuffishAttributes.MAGIC_RESISTANCE_SHRED)
				.add(PuffishAttributes.MELEE_RESISTANCE_SHRED)
				.add(PuffishAttributes.RANGED_RESISTANCE_SHRED)
				.add(PuffishAttributes.DAMAGE_REFLECTION)
				.add(PuffishAttributes.STEALTH)
				.add(PuffishAttributes.LIFE_STEAL)
				.add(PuffishAttributes.FALL_REDUCTION)
				.add(PuffishAttributes.BOW_PROJECTILE_SPEED)
				.add(PuffishAttributes.CROSSBOW_PROJECTILE_SPEED);
	}

	@ModifyVariable(
			method = "hurtServer",
			at = @At("HEAD"),
			argsOnly = true,
			ordinal = 0
	)
	private float modifyVariableAtDamage(float damage, ServerLevel world, DamageSource source) {
		if (damage < 0) {
			return damage;
		}

		if (source.getEntity() instanceof LivingEntity attacker) {
			var dm = DynamicModification.create();

			var itemStack = attacker.getMainHandItem();
			var item = itemStack.getItem();
			if (itemStack.is(ItemTags.SWORDS)) {
				dm.withPositive(PuffishAttributes.SWORD_DAMAGE, attacker);
			}
			if (itemStack.is(ItemTags.AXES)) {
				dm.withPositive(PuffishAttributes.AXE_DAMAGE, attacker);
			}
			if (item instanceof TridentItem) {
				dm.withPositive(PuffishAttributes.TRIDENT_DAMAGE, attacker);
			}
			if (item instanceof MaceItem) {
				dm.withPositive(PuffishAttributes.MACE_DAMAGE, attacker);
			}

			var kind = DamageKind.of(source);
			if (kind.isMagic()) {
				dm.withPositive(PuffishAttributes.MAGIC_DAMAGE, attacker);
			} else {
				if (kind.isProjectile()) {
					dm.withPositive(PuffishAttributes.RANGED_DAMAGE, attacker);
				}
				if (kind.isMelee()) {
					dm.withPositive(PuffishAttributes.MELEE_DAMAGE, attacker);
				}
			}

			if (attacker instanceof OwnableEntity tameable) {
				var owner = tameable.getOwner();
				if (owner != null) {
					dm.withPositive(PuffishAttributes.TAMED_DAMAGE, owner);
				}
			}

			damage = dm.applyTo(damage);
		}

		return damage;
	}

	@WrapOperation(
			method = "getDamageAfterArmorAbsorb",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterAbsorb(Lnet/minecraft/world/entity/LivingEntity;FLnet/minecraft/world/damagesource/DamageSource;FF)F"),
			require = 0
	)
	private float wrapOperationAtApplyArmorToDamage(LivingEntity entity, float damage, DamageSource source, float armor, float toughness, Operation<Float> operation) {
		if (source.getEntity() instanceof LivingEntity attacker) {
			armor = Math.max(0.0f, DynamicModification.create()
					.withNegative(PuffishAttributes.ARMOR_SHRED, attacker)
					.applyTo(armor));
			toughness = Math.max(0.0f, DynamicModification.create()
					.withNegative(PuffishAttributes.TOUGHNESS_SHRED, attacker)
					.applyTo(toughness));
		}

		return operation.call(entity, damage, source, armor, toughness);
	}

	@WrapOperation(
			method = "getDamageAfterMagicAbsorb",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterMagicAbsorb(FF)F"),
			require = 0
	)
	private float wrapOperationAtModifyAppliedDamage(float damageDealt, float protection, Operation<Float> original, @Local(argsOnly = true) DamageSource source) {
		if (source.getEntity() instanceof LivingEntity attacker) {
			protection = Math.max(0.0f, DynamicModification.create()
					.withNegative(PuffishAttributes.PROTECTION_SHRED, attacker)
					.applyTo(protection));
		}

		return original.call(damageDealt, protection);
	}

	@ModifyExpressionValue(
			method = "travelRidden",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/LivingEntity;getRiddenSpeed(Lnet/minecraft/world/entity/player/Player;)F"
			),
			require = 0
	)
	private float modifyExpressionValueAtGetSaddledSpeed(
			float speed,
			@Local(argsOnly = true) Player controllingPlayer
	) {
		return DynamicModification.create()
				.withPositive(PuffishAttributes.MOUNT_SPEED, controllingPlayer)
				.applyTo(speed);
	}

	@ModifyVariable(
			method = "heal",
			at = @At("HEAD"),
			argsOnly = true,
			ordinal = 0
	)
	private float modifyVariableAtHeal(float amount) {
		if (amount < 0) {
			return amount;
		}

		return DynamicModification.create()
				.withPositive(PuffishAttributes.HEALING, ((LivingEntity) (Object) this))
				.applyTo(amount);
	}

	@ModifyReturnValue(
			method = "getJumpPower()F",
			at = @At("RETURN")
	)
	private float injectAtGetJumpVelocity(float jump) {
		return DynamicModification.create()
				.withPositive(PuffishAttributes.JUMP, ((LivingEntity) (Object) this))
				.applyTo(jump);
	}

	@ModifyVariable(
			method = "calculateFallDamage",
			at = @At("HEAD"),
			argsOnly = true,
			ordinal = 0
	)
	private double modifyVariableAtComputeFallDamage(double fallDistance) {
		return DynamicModification.create()
				.withNegative(PuffishAttributes.FALL_REDUCTION, ((LivingEntity) (Object) this))
				.applyTo(fallDistance)
				- DynamicModification.create()
				.withPositive(PuffishAttributes.JUMP, ((LivingEntity) (Object) this))
				.relativeTo(1.0f) * 10.0f;
	}

	@WrapMethod(
			method = "getDamageAfterArmorAbsorb"
	)
	private float wrapMethodApplyArmorToDamage(DamageSource source, float amount, Operation<Float> original) {
		var damage = original.call(source, amount);

		if (source.is(DamageTypeTags.BYPASSES_EFFECTS)) {
			return damage;
		}
		if (damage > Float.MAX_VALUE / 3.0f) {
			return damage;
		}

		var entity = ((LivingEntity) (Object) this);
		var kind = DamageKind.of(source);

		var dmResistance = DynamicModification.create();
		dmResistance.withPositive(PuffishAttributes.RESISTANCE, entity);
		if (kind.isMagic()) {
			dmResistance.withPositive(PuffishAttributes.MAGIC_RESISTANCE, entity);
		} else {
			if (kind.isProjectile()) {
				dmResistance.withPositive(PuffishAttributes.RANGED_RESISTANCE, entity);
			}
			if (kind.isMelee()) {
				dmResistance.withPositive(PuffishAttributes.MELEE_RESISTANCE, entity);
			}
		}

		if (entity instanceof OwnableEntity tameable) {
			var owner = tameable.getOwner();
			if (owner != null) {
				dmResistance.withPositive(PuffishAttributes.TAMED_RESISTANCE, owner);
			}
		}

		var resistance = dmResistance.relativeTo(damage);

		if (source.getEntity() instanceof LivingEntity attacker) {
			var dmShred = DynamicModification.create();
			dmShred.withNegative(PuffishAttributes.RESISTANCE_SHRED, attacker);
			if (kind.isMagic()) {
				dmShred.withNegative(PuffishAttributes.MAGIC_RESISTANCE_SHRED, attacker);
			} else {
				if (kind.isProjectile()) {
					dmShred.withNegative(PuffishAttributes.RANGED_RESISTANCE_SHRED, attacker);
				}
				if (kind.isMelee()) {
					dmShred.withNegative(PuffishAttributes.MELEE_RESISTANCE_SHRED, attacker);
				}
			}

			var shred = dmShred.relativeTo(resistance);

			// shred cannot be greater than resistance
			shred = Math.min(shred, resistance);

			resistance -= shred;
		}

		return Math.max(0.0f, damage - resistance);
	}
}
