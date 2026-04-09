package com.ultra.megamod.feature.combat.animation.api;

import com.ultra.megamod.feature.combat.animation.AttackHand;
import com.ultra.megamod.feature.combat.animation.WeaponAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;

/**
 * Mixin interface for MinecraftClient to expose combo and swing state.
 * Ported 1:1 from BetterCombat (net.bettercombat.api.MinecraftClient_BetterCombat).
 */
public interface MinecraftClient_BetterCombat {
    int getComboCount();
    boolean hasTargetsInReach();

    @Nullable
    default Entity getCursorTarget() {
        var client = (Minecraft) this;
        if (client.hitResult != null && client.hitResult.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult) client.hitResult).getEntity();
        }
        return null;
    }

    int getUpswingTicks();
    float getSwingProgress();

    default boolean isWeaponSwingInProgress() {
        return getSwingProgress() < 1F;
    }

    @Nullable
    AttackHand getCurrentAttackHand();

    @Nullable
    default WeaponAttributes.Attack getCurrentAttack() {
        var attackHand = getCurrentAttackHand();
        if (attackHand == null) return null;
        return attackHand.attack();
    }

    void cancelUpswing();
}
