package com.ultra.megamod.mixin.rangedweapon.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ultra.megamod.lib.rangedweapon.api.CustomRangedWeapon;
import com.ultra.megamod.lib.rangedweapon.api.EntityAttributes_RangedWeapon;
import com.ultra.megamod.lib.rangedweapon.internal.ArrowExtension;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ProjectileUtil.class)
public class ProjectileUtilMixin {

    /**
     * This mixin applies to non-standard shoot cases, such as
     * - some mobs (skeletons, illusioners) shooting with bows
     */

    @WrapOperation(method = "getMobArrow",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/arrow/AbstractArrow;setBaseDamageFromMob(F)V"))
    private static void rwa$applyDamage(
            // Mixin parameters
            AbstractArrow instance, float velocity, Operation<Void> original,
            // Context parameters
            LivingEntity entity, ItemStack projectile, float velocity2, @Nullable ItemStack bow
    ) {
        original.call(instance, velocity);
        if (bow == null) {
            return;
        }
        if ( !((ArrowExtension)instance).rwa_isModified()
                && bow.getItem() instanceof CustomRangedWeapon rangedWeapon) {
            var currentDamage = entity.getAttributeValue(EntityAttributes_RangedWeapon.DAMAGE.entry);
            var multiplier = currentDamage / rangedWeapon.getTypeBaseline().damage();
            ((ArrowExtension)instance).rwa_setBaseDamage(((ArrowExtension)instance).rwa_getBaseDamage() * multiplier);
            ((ArrowExtension)instance).rwa_markModified(true);
        }
    }
}
