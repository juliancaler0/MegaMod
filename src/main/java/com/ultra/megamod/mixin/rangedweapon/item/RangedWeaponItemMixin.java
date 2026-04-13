package com.ultra.megamod.mixin.rangedweapon.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ultra.megamod.lib.rangedweapon.api.CustomRangedWeapon;
import com.ultra.megamod.lib.rangedweapon.api.EntityAttributes_RangedWeapon;
import com.ultra.megamod.lib.rangedweapon.api.RangedConfig;
import com.ultra.megamod.lib.rangedweapon.internal.ArrowExtension;
import com.ultra.megamod.lib.rangedweapon.internal.RangedItemSettings;
import com.ultra.megamod.lib.rangedweapon.internal.ScalingUtil;
import com.ultra.megamod.lib.rangedweapon.internal.AttributeUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ProjectileWeaponItem.class)
abstract class RangedWeaponItemMixin extends Item implements CustomRangedWeapon {
    RangedWeaponItemMixin(Properties settings) {
        super(settings);
    }

    @ModifyVariable(method = "<init>", at = @At("HEAD"), ordinal = 0)
    private static Item.Properties applyDefaultAttributes(Item.Properties settings) {
        var rangedSettings = ((RangedItemSettings) settings);
        var config = rangedSettings.getRangedAttributes();
        if (config != null) {
            ItemAttributeModifiers existingAttributes = null;
            var componentBuilder = rangedSettings.rwa_getComponentBuilder();
            if (componentBuilder != null) {
                var existingComponents = ((ComponentMapBuilderAccessor) componentBuilder).rwa_components();
                var existing = existingComponents.get(DataComponents.ATTRIBUTE_MODIFIERS);
                if (existing instanceof ItemAttributeModifiers attributeModifiers) {
                    existingAttributes = attributeModifiers;
                }
            }
            var rangedAttributes = AttributeUtils.fromRangedConfig(config);
            var applicableAttributes = AttributeUtils.mergeComponents(rangedAttributes, existingAttributes);
            return settings.attributes(applicableAttributes);
        } else {
            return settings;
        }
    }

    // CustomRangedWeapon

    private RangedConfig typeBaseLine = RangedConfig.BOW;

    public void setTypeBaseline(RangedConfig config) {
        this.typeBaseLine = config;
    }

    public RangedConfig getTypeBaseline() {
        return this.typeBaseLine;
    }

    // Thread-local storage for the shooter and velocity multiplier to pass between shoot and shootProjectile
    @Unique
    private static final ThreadLocal<LivingEntity> rwa_currentShooter = new ThreadLocal<>();
    @Unique
    private static final ThreadLocal<Double> rwa_velocityMultiplier = new ThreadLocal<>();
    @Unique
    private static final ThreadLocal<CustomRangedWeapon> rwa_currentWeapon = new ThreadLocal<>();

    /**
     * Intercept the shoot method to modify velocity and capture context for damage modification.
     * In 1.21.11, shootProjectile is called inside a lambda within shoot(),
     * so we modify the velocity parameter directly and use thread-local storage
     * to pass context to the shootProjectile override.
     */
    @ModifyVariable(
            method = "shoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Ljava/util/List;FFZLnet/minecraft/world/entity/LivingEntity;)V",
            at = @At("HEAD"),
            ordinal = 0, // velocity parameter (float)
            argsOnly = true
    )
    private float rwa_modifyVelocity(float velocity,
            ServerLevel level, LivingEntity shooter, InteractionHand hand, ItemStack weapon,
            List<ItemStack> projectileItems, float velocity2, float inaccuracy, boolean isCrit, @Nullable LivingEntity target) {
        var bonusVelocity = shooter.getAttributeValue(EntityAttributes_RangedWeapon.VELOCITY.entry);
        var velocityMult = ScalingUtil.arrowVelocityMultiplier((ProjectileWeaponItem)(Object)this, bonusVelocity);
        rwa_currentShooter.set(shooter);
        rwa_velocityMultiplier.set(velocityMult);
        rwa_currentWeapon.set(this);
        return velocity * (float) velocityMult;
    }

    @Inject(
            method = "shoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Ljava/util/List;FFZLnet/minecraft/world/entity/LivingEntity;)V",
            at = @At("TAIL")
    )
    private void rwa_cleanupContext(ServerLevel level, LivingEntity shooter, InteractionHand hand, ItemStack weapon,
            List<ItemStack> projectileItems, float velocity, float inaccuracy, boolean isCrit, @Nullable LivingEntity target,
            CallbackInfo ci) {
        rwa_currentShooter.remove();
        rwa_velocityMultiplier.remove();
        rwa_currentWeapon.remove();
    }

    /**
     * After spawnProjectile creates the projectile, apply damage modifications.
     * We wrap Projectile.spawnProjectile to capture the created projectile.
     */
    @WrapOperation(
            method = "shoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Ljava/util/List;FFZLnet/minecraft/world/entity/LivingEntity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectile(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Consumer;)Lnet/minecraft/world/entity/projectile/Projectile;")
    )
    private <T extends Projectile> T rwa_applyDamageAfterSpawn(T projectile, ServerLevel level, ItemStack ammo, java.util.function.Consumer<T> consumer, Operation<T> original) {
        T result = original.call(projectile, level, ammo, consumer);

        // After the projectile has been spawned (and shootProjectile was called inside consumer),
        // apply damage modifications
        var shooter = rwa_currentShooter.get();
        var velocityMult = rwa_velocityMultiplier.get();
        var weapon = rwa_currentWeapon.get();
        if (shooter != null && velocityMult != null && weapon != null
            && result instanceof AbstractArrow arrow
            && !((ArrowExtension)result).rwa_isModified()) {
            var rangedDamage = shooter.getAttributeValue(EntityAttributes_RangedWeapon.DAMAGE.entry);
            if (rangedDamage > 0) {
                var multiplier = ScalingUtil.arrowDamageMultiplier(weapon.getTypeBaseline().damage(), rangedDamage, velocityMult);
                var finalDamage = ((ArrowExtension)arrow).rwa_getBaseDamage() * multiplier;
                ((ArrowExtension)arrow).rwa_setBaseDamage(finalDamage);
                ((ArrowExtension)result).rwa_markModified(true);
            }
        }

        return result;
    }
}
