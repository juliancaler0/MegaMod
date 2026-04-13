package com.ultra.megamod.feature.combat.archers.item;

import net.minecraft.world.item.Item;

import java.util.HashMap;

public class ArcherItems {
    public static final HashMap<String, Item> entries;
    static {
        entries = new HashMap<>();
        for (var weaponEntry : ArcherWeapons.rangedEntries) {
            entries.put(weaponEntry.id().toString(), weaponEntry.item());
        }
        for (var weaponEntry : ArcherWeapons.meleeEntries) {
            entries.put(weaponEntry.id().toString(), weaponEntry.item());
        }
        for (var entry : ArcherArmors.entries) {
            var set = entry.armorSet();
            for (var piece : set.pieces()) {
                entries.put(set.idOf((Item) piece).toString(), (Item) piece);
            }
        }
    }
}
