package com.ultra.megamod.feature.combat.paladins.item;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.paladins.PaladinsMod;
import com.ultra.megamod.feature.combat.paladins.content.PaladinSounds;
import com.ultra.megamod.lib.spellengine.api.config.ShieldConfig;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Equipment;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Shield;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Shields;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Supplier;

public class PaladinShields {
    public static final ArrayList<Shield.Entry> entries = new ArrayList<>();

    private static Shield.Entry add(Shield.Entry entry) {
        entries.add(entry);
        return entry;
    }

    private static Supplier<Ingredient> ingredient(String idString, boolean requirement, Item fallback) {
        var id = Identifier.parse(idString);
        if (requirement) {
            return () -> Ingredient.of(fallback);
        } else {
            return () -> {
                return Ingredient.of(fallback);
            };
        }
    }

    // MARK: Shields

    public static Shield.Entry iron_kite_shield = add(Shields.createStandard(MegaMod.MODID, "iron_kite_shield", Equipment.Tier.TIER_1, () -> Ingredient.of(Items.IRON_INGOT), PaladinSounds.shield_equip.entry()));
    public static Shield.Entry golden_kite_shield = add(Shields.createStandard(MegaMod.MODID, "golden_kite_shield", Equipment.Tier.GOLDEN, () -> Ingredient.of(Items.GOLD_INGOT), PaladinSounds.shield_equip.entry()));
    public static Shield.Entry diamond_kite_shield = add(Shields.createStandard(MegaMod.MODID, "diamond_kite_shield", Equipment.Tier.TIER_2, () -> Ingredient.of(Items.DIAMOND), PaladinSounds.shield_equip.entry()));
    public static Shield.Entry netherite_kite_shield = add(Shields.createStandard(MegaMod.MODID, "netherite_kite_shield", Equipment.Tier.TIER_3, () -> Ingredient.of(Items.NETHERITE_INGOT), PaladinSounds.shield_equip.entry()));

    public static void register(Map<String, ShieldConfig> configs) {
        // Compat mod shields (always included)
        var repair = ingredient("betternether:nether_ruby", false, Items.NETHERITE_INGOT);
        add(Shields.createStandard(MegaMod.MODID, "ruby_kite_shield", Equipment.Tier.TIER_4, repair, PaladinSounds.shield_equip.entry()));

        var endRepair = ingredient("betterend:aeternium_ingot", false, Items.NETHERITE_INGOT);
        add(Shields.createStandard(MegaMod.MODID, "aeternium_kite_shield", Equipment.Tier.TIER_4, endRepair, PaladinSounds.shield_equip.entry()));

        var aetherRepair = ingredient("aether:ambrosium_shard", false, Items.NETHERITE_INGOT);
        add(Shields.createStandard(MegaMod.MODID, "aether_kite_shield", Equipment.Tier.TIER_4, aetherRepair, PaladinSounds.shield_equip.entry())
                .loot(-1, "aether"));

        Shield.register(configs, entries, Group.KEY, null);
    }

    public static void init(IEventBus modEventBus) {
        register(PaladinsMod.shieldConfig.shields);
    }
}
