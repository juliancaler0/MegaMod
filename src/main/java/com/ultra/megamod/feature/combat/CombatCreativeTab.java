package com.ultra.megamod.feature.combat;

import com.ultra.megamod.feature.combat.items.ArcherItemRegistry;
import com.ultra.megamod.feature.combat.items.ClassArmorRegistry;
import com.ultra.megamod.feature.combat.items.ClassWeaponRegistry;
import com.ultra.megamod.feature.combat.items.JewelryRegistry;
import com.ultra.megamod.feature.combat.runes.RuneRegistry;
import com.ultra.megamod.feature.combat.spell.SpellItemRegistry;
import com.ultra.megamod.lib.spellengine.rpg_series.item.RPGItemRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/**
 * Routes combat items to the correct MegaMod creative tabs:
 * - Weapons tab: class weapons, archer items (bows, quivers)
 * - Armor tab: class armor sets
 * - Relics tab: jewelry (rings, necklaces, gems)
 * - MegaMod tab: spell books, scrolls, misc utility
 */
public class CombatCreativeTab {

    public static void onBuildContents(BuildCreativeModeTabContentsEvent event) {
        ResourceKey<CreativeModeTab> tab = event.getTabKey();

        ResourceKey<CreativeModeTab> WEAPONS = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath("megamod", "megamod_weapons_tab"));
        ResourceKey<CreativeModeTab> ARMOR = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath("megamod", "megamod_armor_tab"));
        ResourceKey<CreativeModeTab> RELICS = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath("megamod", "megamod_relics_tab"));
        ResourceKey<CreativeModeTab> MEGAMOD = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath("megamod", "megamod_tab"));

        if (tab.equals(WEAPONS)) {
            // Class weapons (claymores, swords, daggers, staves, bows, crossbows, shields, etc.)
            ClassWeaponRegistry.ITEMS.getEntries().forEach(e -> event.accept(e.get()));
            // Archer items (quivers, auto-fire hook)
            ArcherItemRegistry.ITEMS.getEntries().forEach(e -> event.accept(e.get()));
            // SpellEngine-registered weapons & shields (Paladin, Wizard, Rogue, Arsenal)
            RPGItemRegistry.ITEMS.getEntries().forEach(e -> {
                Item item = e.get();
                if (!isArmorPiece(item)) event.accept(item);
            });
        } else if (tab.equals(ARMOR)) {
            // Class armor (archer, ranger)
            ClassArmorRegistry.ITEMS.getEntries().forEach(e -> event.accept(e.get()));
            // SpellEngine-registered armor (Paladin/Priest/Crusader, Wizard robes, Rogue/Warrior)
            RPGItemRegistry.ITEMS.getEntries().forEach(e -> {
                Item item = e.get();
                if (isArmorPiece(item)) event.accept(item);
            });
        } else if (tab.equals(RELICS)) {
            // Jewelry (gems, rings, necklaces)
            JewelryRegistry.ITEMS.getEntries().forEach(e -> event.accept(e.get()));
        } else if (tab.equals(MEGAMOD)) {
            // Spell books, scrolls, runes
            SpellItemRegistry.ITEMS.getEntries().forEach(e -> event.accept(e.get()));
            RuneRegistry.ITEMS.getEntries().forEach(e -> event.accept(e.get()));
        }
    }

    /** True when the item's EQUIPPABLE component targets an armor slot. */
    private static boolean isArmorPiece(Item item) {
        Equippable eq = new ItemStack(item).get(DataComponents.EQUIPPABLE);
        if (eq == null) return false;
        EquipmentSlot slot = eq.slot();
        return slot == EquipmentSlot.HEAD
                || slot == EquipmentSlot.CHEST
                || slot == EquipmentSlot.LEGS
                || slot == EquipmentSlot.FEET;
    }
}
