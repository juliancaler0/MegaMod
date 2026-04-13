package com.ultra.megamod.mixin.rangedweapon;

import com.ultra.megamod.lib.rangedweapon.internal.ArrowExtension;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Random;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin implements ArrowExtension {
    private static final Random CRIT_RANDOM = new Random();
    @Shadow private double baseDamage;
    @Shadow public abstract boolean isCritArrow();

    /**
     * This math magic fixes incorrectly scaling damage when critical strikes occur
     * by eliminating flat additions.
     */

    @ModifyVariable(method = "onHitEntity", at = @At("STORE"), ordinal = 0)
    private int modifyCritDamage(int value) {
        if (!isCritArrow()) { return value; }
        var projectile = (AbstractArrow) ((Object) this);
        var velocity = projectile.getDeltaMovement().length();
        var critMultiplier = 1F + (0.1F + CRIT_RANDOM.nextFloat() * 0.5F);
        // System.out.println("Critical strike! Damage: " + (velocity * this.baseDamage) + " critMultiplier: " + critMultiplier);
        return (int) Math.round(Mth.clamp(velocity * this.baseDamage * critMultiplier, 0.0, 2.147483647E9));
    }

    private boolean rwa_modified = false;
    public void rwa_markModified(boolean modified) {
        this.rwa_modified = modified;
    }
    public boolean rwa_isModified() {
        return this.rwa_modified;
    }

    @Override
    public double rwa_getBaseDamage() {
        return this.baseDamage;
    }

    @Override
    public void rwa_setBaseDamage(double damage) {
        this.baseDamage = damage;
    }
}
