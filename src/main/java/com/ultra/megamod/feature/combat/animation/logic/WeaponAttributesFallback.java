package com.ultra.megamod.feature.combat.animation.logic;

import com.ultra.megamod.feature.combat.animation.WeaponAttributeRegistry;
import com.ultra.megamod.feature.combat.animation.config.FallbackConfig;
import com.ultra.megamod.feature.combat.animation.utils.PatternMatching;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;

/**
 * Assigns weapon attributes to items that match regex patterns from the fallback config.
 * Ported 1:1 from BetterCombat (net.bettercombat.logic.WeaponAttributesFallback).
 */
public class WeaponAttributesFallback {

    private static FallbackConfig fallbackConfig;

    public static void setFallbackConfig(FallbackConfig config) {
        fallbackConfig = config;
    }

    public static void initialize() {
        if (fallbackConfig == null) {
            fallbackConfig = FallbackConfig.createDefault();
        }

        for (var entry : BuiltInRegistries.ITEM.entrySet()) {
            var itemId = entry.getKey().identifier();
            var item = entry.getValue();

            if (PatternMatching.matches(itemId.toString(), fallbackConfig.blacklist_item_id_regex)) {
                continue;
            }

            FallbackConfig.CompatibilitySpecifier[] specifiers = null;
            if (hasAttributeModifier(item, Attributes.ATTACK_DAMAGE)) {
                specifiers = fallbackConfig.fallback_compatibility;
            } else if (item instanceof ProjectileWeaponItem) {
                specifiers = fallbackConfig.ranged_weapons;
            }
            if (specifiers == null) {
                continue;
            }

            for (var fallbackOption : specifiers) {
                if (WeaponAttributeRegistry.getAttributes(item) == null
                        && PatternMatching.matches(itemId.toString(), fallbackOption.item_id_regex)) {
                    var archetype = WeaponAttributeRegistry.getArchetype(fallbackOption.weapon_attributes);
                    if (archetype != null) {
                        WeaponAttributeRegistry.register(item, archetype);
                        break;
                    }
                }
            }
        }
    }

    private static boolean hasAttributeModifier(Item item, net.minecraft.core.Holder<Attribute> searchedAttribute) {
        var defaultInstance = item.getDefaultInstance();
        var attributes = defaultInstance.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (attributes != null) {
            for (var entry : attributes.modifiers()) {
                var attribute = entry.attribute();
                if (attribute.equals(searchedAttribute)) {
                    return true;
                }
            }
        }
        return false;
    }
}
