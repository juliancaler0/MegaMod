package com.ultra.megamod.lib.spellengine.compat;

import net.minecraft.world.damagesource.DamageSource;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class CriticalStrikeCompat {
    private static Predicate<DamageSource> isCriticalStrike = ds -> false;
    private static BiConsumer<DamageSource, Float> setCriticalStrike = (ds, crit) -> {
        // No-op
    };
    public static void init() {
        // CriticalStrike mod not present in MegaMod - stub only
    }

    public static boolean isCriticalStrike(DamageSource damageSource) {
        if (damageSource == null) {
            return false;
        }
        return isCriticalStrike.test(damageSource);
    }

    public static void setCriticalStrike(DamageSource damageSource, float critMultiplier) {
        if (damageSource == null) {
            return;
        }
        setCriticalStrike.accept(damageSource, critMultiplier);
    }
}
