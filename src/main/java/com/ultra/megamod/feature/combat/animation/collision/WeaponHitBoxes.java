package com.ultra.megamod.feature.combat.animation.collision;

import com.ultra.megamod.feature.combat.animation.WeaponAttributes;
import net.minecraft.world.phys.Vec3;

/**
 * Creates hitbox dimensions based on weapon attack shape.
 * Ported 1:1 from BetterCombat (net.bettercombat.client.collision.WeaponHitBoxes).
 */
public class WeaponHitBoxes {

    public static Vec3 createHitbox(WeaponAttributes.HitboxShape shape, double attackRange, boolean isSpinAttack) {
        float zMult = isSpinAttack ? 2 : 1;
        return switch (shape) {
            case FORWARD_BOX -> new Vec3(attackRange * 0.5, attackRange * 0.5, attackRange);
            case VERTICAL_PLANE -> new Vec3(attackRange / 3.0, attackRange * 2.0, attackRange * zMult);
            case HORIZONTAL_PLANE -> new Vec3(attackRange * 2.0, attackRange / 3.0, attackRange * zMult);
        };
    }
}
