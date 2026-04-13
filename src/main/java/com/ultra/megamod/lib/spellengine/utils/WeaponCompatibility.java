package com.ultra.megamod.lib.spellengine.utils;

import net.minecraft.world.item.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import com.ultra.megamod.lib.spellengine.SpellEngineMod;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellContainer;
import com.ultra.megamod.lib.spellengine.config.FallbackConfig;
import com.ultra.megamod.lib.spellengine.internals.container.SpellAssignments;

public class WeaponCompatibility {
    public static void initialize() {
        var config = SpellEngineMod.fallbackConfig;
        if (!config.enabled) {
            return;
        }

        // Process all items in the registry
        for (var entry : BuiltInRegistries.ITEM.entrySet()) {
            var itemId = entry.getKey().identifier();
            var item = entry.getValue();
            var itemEntry = item.builtInRegistryHolder();

            // Try melee weapons group
            if (config.melee_weapons.enabled &&
                    (item instanceof TridentItem || item instanceof MaceItem || item instanceof AxeItem
                        || item.components().has(net.minecraft.core.component.DataComponents.TOOL)) ) {
                SpellContainer container = processCompatGroup(
                        itemEntry,
                        config.melee_weapons
                );
                if (container != null) {
                    SpellAssignments.containers.putIfAbsent(itemId, container);
                    continue; // Don't process other groups
                }
            }

            // Try ranged weapons group
            if (config.ranged_weapons.enabled &&
                (item instanceof ProjectileWeaponItem) ) {
                SpellContainer container = processCompatGroup(
                        itemEntry,
                        config.ranged_weapons
                );
                if (container != null) {
                    SpellAssignments.containers.putIfAbsent(itemId, container);
                    continue; // Don't process other groups
                }
            }
        }
    }

    private static SpellContainer processCompatGroup(
            Holder<Item> itemEntry,
            FallbackConfig.CompatGroup group) {

        // Check blacklist
        if (group.blacklist != null && !group.blacklist.isEmpty()) {
            if (PatternMatching.matches(itemEntry, Registries.ITEM, group.blacklist)) {
                return null; // Item is blacklisted
            }
        }

        // Try to match against specifiers in order
        if (group.enable_specifiers) {
            for (var specifier : group.specifiers) {
                if (specifier.item != null && !specifier.item.isEmpty()) {
                    if (PatternMatching.matches(itemEntry, Registries.ITEM, specifier.item)) {
                        return specifier.container; // First match wins
                    }
                }
            }
        }

        // No specifier matched, use default
        return group.defaults;
    }
}
