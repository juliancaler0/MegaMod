package com.ultra.megamod.feature.combat.animation.api.fx;

import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Registry for item stack predicates used for conditional weapon trail appearances.
 * Ported 1:1 from BetterCombat (net.bettercombat.api.fx.ItemConditions).
 */
public class ItemConditions {
    private static final Map<String, Predicate<ItemStack>> CONDITIONS = new HashMap<>();

    static {
        register("is_enchanted", ItemStack::isEnchanted);
    }

    public static void register(String id, Predicate<ItemStack> predicate) {
        CONDITIONS.put(id, predicate);
    }

    public static Predicate<ItemStack> get(String id) {
        return CONDITIONS.get(id);
    }

    public static boolean test(String id, ItemStack itemStack) {
        Predicate<ItemStack> predicate = CONDITIONS.get(id);
        if (predicate == null) {
            return false;
        }
        return predicate.test(itemStack);
    }

    public static boolean has(String id) {
        return CONDITIONS.containsKey(id);
    }
}
