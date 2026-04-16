package com.ultra.megamod.feature.combat.arsenal.item;

import com.ultra.megamod.feature.combat.arsenal.ArsenalMod;
import com.ultra.megamod.feature.combat.arsenal.spell.ArsenalSounds;
import com.ultra.megamod.feature.combat.arsenal.spell.ArsenalSpells;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Rarity;
import com.ultra.megamod.lib.spellengine.api.config.AttributeModifier;
import com.ultra.megamod.lib.spellengine.api.config.ShieldConfig;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellContainers;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Equipment;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Shield;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Shields;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArsenalShields {
    public static final ArrayList<Shield.Entry> entries = new ArrayList<>();

    private static Shield.Entry add(Shield.Entry entry) {
        entries.add(entry);
        return entry;
    }

    private static final List<AttributeModifier> UNIQUE_ATTRIBUTES =
            List.of(Shields.toughness(2), Shields.health(6));

    // MARK: Shields

    public static Shield.Entry unique_shield_1 = add(Shields.create(ArsenalMod.NAMESPACE, "unique_shield_1", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.NETHERITE_SCRAP), UNIQUE_ATTRIBUTES, ArsenalSounds.shield_equip.entry())
            .translatedName("Bulwark of Azzinoth")
            .spellContainer(SpellContainers.forRelic(ArsenalSpells.spiked_shield.id()))
            .lootTheme(Loot.Theme.EVIL.toString()));
    public static Shield.Entry unique_shield_2 = add(Shields.create(ArsenalMod.NAMESPACE, "unique_shield_2", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.IRON_BLOCK), UNIQUE_ATTRIBUTES, ArsenalSounds.shield_equip.entry())
            .translatedName("Bastion of Light")
            .spellContainer(SpellContainers.forRelic(ArsenalSpells.guarding_shield.id()))
            .lootTheme(Loot.Theme.GENERIC.toString()));
    public static Shield.Entry unique_shield_sw = add(Shields.create(ArsenalMod.NAMESPACE, "unique_shield_sw", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.GOLD_BLOCK), UNIQUE_ATTRIBUTES, ArsenalSounds.shield_equip.entry())
            .translatedName("Sword Breaker's Bulwark")
            .spellContainer(SpellContainers.forRelic(ArsenalSpells.unyielding_shield.id()))
            .lootTheme(Loot.Theme.ELVEN.toString()));

    // MARK: Nightmare _3 variant

    public static Shield.Entry unique_shield_3 = add(Shields.create(ArsenalMod.NAMESPACE, "unique_shield_3", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.NETHERITE_SCRAP), UNIQUE_ATTRIBUTES, ArsenalSounds.shield_equip.entry())
            .translatedName("Dreadnought Ward")
            .spellContainer(SpellContainers.forRelic(ArsenalSpells.spiked_shield.id()))
            .lootTheme(Loot.Theme.GENERIC.toString()));

    static {
        for (var entry: entries) {
            entry.rarity = Rarity.RARE;
        }
    }

    public static void register(Map<String, ShieldConfig> configs) {
        Shield.register(configs, entries, Group.KEY, null);
    }
}
