package com.ultra.megamod.feature.combat.archers.item;

import com.ultra.megamod.feature.combat.archers.ArchersMod;
import com.ultra.megamod.lib.rangedweapon.api.RangedConfig;
import com.ultra.megamod.lib.spellengine.api.config.WeaponConfig;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Equipment;
import com.ultra.megamod.lib.spellengine.rpg_series.item.RangedWeapon;
import com.ultra.megamod.lib.spellengine.rpg_series.item.RangedWeapons;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Weapon;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Weapons;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Supplier;

public class ArcherWeapons {
    private static final String NAMESPACE = ArchersMod.ID;
    public static final ArrayList<RangedWeapon.Entry> rangedEntries = new ArrayList<>();
    public static final ArrayList<Weapon.Entry> meleeEntries = new ArrayList<>();

    private static RangedWeapon.Entry addRanged(RangedWeapon.Entry entry) {
        rangedEntries.add(entry);
        return entry;
    }

    private static Weapon.Entry addMelee(Weapon.Entry entry) {
        meleeEntries.add(entry);
        return entry;
    }

    private static Supplier<Ingredient> ingredient(String idString, boolean isLoaded, Item fallback) {
        var id = Identifier.parse(idString);
        if (isLoaded) {
            return () -> Ingredient.of(fallback);
        } else {
            return () -> {
                var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(id);
                var ingredient = item != null ? item : fallback;
                return Ingredient.of(ingredient);
            };
        }
    }

    private static final String BETTER_END = "betterend";
    private static final String BETTER_NETHER = "betternether";
    private static final String AETHER = "aether";

    // MARK: Spears

    public static final Weapon.Entry flint_spear = addMelee(Weapons.spearWithSkill(
            NAMESPACE, "flint_spear", Equipment.Tier.TIER_0, () -> Ingredient.of(Items.FLINT)));
    public static final Weapon.Entry iron_spear = addMelee(Weapons.spearWithSkill(
            NAMESPACE, "iron_spear", Equipment.Tier.TIER_1, () -> Ingredient.of(Items.IRON_INGOT)));
    public static final Weapon.Entry golden_spear = addMelee(Weapons.spearWithSkill(
            NAMESPACE, "golden_spear", Equipment.Tier.GOLDEN, () -> Ingredient.of(Items.GOLD_INGOT))
            .loot(Equipment.LootProperties.of("golden_weapon")));
    public static final Weapon.Entry diamond_spear = addMelee(Weapons.spearWithSkill(
            NAMESPACE, "diamond_spear", Equipment.Tier.TIER_2, () -> Ingredient.of(Items.DIAMOND)));
    public static final Weapon.Entry netherite_spear = addMelee(Weapons.spearWithSkill(
            NAMESPACE, "netherite_spear", Equipment.Tier.TIER_3, () -> Ingredient.of(Items.NETHERITE_INGOT)));

    // MARK: Bows

    public static final RangedWeapon.Entry composite_longbow = addRanged(RangedWeapons.longBow(
            NAMESPACE, "composite_longbow", Equipment.Tier.TIER_1, () -> Ingredient.of(Items.BONE)));
    public static final RangedWeapon.Entry mechanic_shortbow = addRanged(RangedWeapons.shortBow(
            NAMESPACE, "mechanic_shortbow", Equipment.Tier.TIER_2, () -> Ingredient.of(Items.REDSTONE)));
    public static final RangedWeapon.Entry royal_longbow = addRanged(RangedWeapons.longBow(
            NAMESPACE, "royal_longbow", Equipment.Tier.TIER_2, () -> Ingredient.of(Items.GOLD_INGOT)));
    public static final RangedWeapon.Entry netherite_shortbow = addRanged(RangedWeapons.shortBow(
            NAMESPACE, "netherite_shortbow", Equipment.Tier.TIER_3, () -> Ingredient.of(Items.NETHERITE_INGOT)));
    public static final RangedWeapon.Entry netherite_longbow = addRanged(RangedWeapons.longBow(
            NAMESPACE, "netherite_longbow", Equipment.Tier.TIER_3, () -> Ingredient.of(Items.NETHERITE_INGOT)));

    // MARK: Crossbows

    public static final RangedWeapon.Entry rapid_crossbow = addRanged(RangedWeapons.rapidCrossbow(
            NAMESPACE, "rapid_crossbow", Equipment.Tier.TIER_2, () -> Ingredient.of(Items.REDSTONE)));
    public static final RangedWeapon.Entry heavy_crossbow = addRanged(RangedWeapons.heavyCrossbow(
            NAMESPACE, "heavy_crossbow", Equipment.Tier.TIER_2, () -> Ingredient.of(Items.DIAMOND)));
    public static final RangedWeapon.Entry netherite_rapid_crossbow = addRanged(RangedWeapons.rapidCrossbow(
            NAMESPACE, "netherite_rapid_crossbow", Equipment.Tier.TIER_3, () -> Ingredient.of(Items.NETHERITE_INGOT)));
    public static final RangedWeapon.Entry netherite_heavy_crossbow = addRanged(RangedWeapons.heavyCrossbow(
            NAMESPACE, "netherite_heavy_crossbow", Equipment.Tier.TIER_3, () -> Ingredient.of(Items.NETHERITE_INGOT)));

    // MARK: Register

    public static void register(IEventBus modEventBus, Map<String, RangedConfig> rangedConfig, Map<String, WeaponConfig> meleeConfig) {
        // MegaMod always has items available — no conditional mod loading needed
        boolean alwaysEnable = ArchersMod.tweaksConfig.ignore_items_required_mods;

        if (alwaysEnable || net.neoforged.fml.ModList.get().isLoaded(BETTER_END)) {
            var aeterniumRepair = ingredient("betterend:aeternium_ingot", net.neoforged.fml.ModList.get().isLoaded(BETTER_END), Items.NETHERITE_INGOT);
            var crystalRepair = ingredient("betterend:crystal_shards", net.neoforged.fml.ModList.get().isLoaded(BETTER_END), Items.NETHERITE_INGOT);
            addMelee(Weapons.spearWithSkill(NAMESPACE, "aeternium_spear", Equipment.Tier.TIER_4, aeterniumRepair));
            addRanged(RangedWeapons.shortBow(NAMESPACE, "crystal_shortbow", Equipment.Tier.TIER_4, crystalRepair));
            addRanged(RangedWeapons.longBow(NAMESPACE, "crystal_longbow", Equipment.Tier.TIER_4, crystalRepair));
        }
        if (alwaysEnable || net.neoforged.fml.ModList.get().isLoaded(BETTER_NETHER)) {
            var rubyRepair = ingredient("betternether:nether_ruby", net.neoforged.fml.ModList.get().isLoaded(BETTER_NETHER), Items.NETHERITE_INGOT);
            addMelee(Weapons.spearWithSkill(NAMESPACE, "ruby_spear", Equipment.Tier.TIER_4, rubyRepair));
            addRanged(RangedWeapons.rapidCrossbow(NAMESPACE, "ruby_rapid_crossbow", Equipment.Tier.TIER_4, rubyRepair));
            addRanged(RangedWeapons.heavyCrossbow(NAMESPACE, "ruby_heavy_crossbow", Equipment.Tier.TIER_4, rubyRepair));
        }
        if (alwaysEnable || net.neoforged.fml.ModList.get().isLoaded(AETHER)) {
            var aetherRepair = ingredient("aether:ambrosium_shard", net.neoforged.fml.ModList.get().isLoaded(AETHER), Items.NETHERITE_INGOT);
            addMelee(Weapons.spearWithSkill(NAMESPACE, "aether_spear", Equipment.Tier.TIER_4, aetherRepair)
                    .loot(Equipment.LootProperties.of("aether")));
            addRanged(RangedWeapons.longBow(NAMESPACE, "aether_longbow", Equipment.Tier.TIER_4, aetherRepair).loot(-1, "aether"));
            addRanged(RangedWeapons.rapidCrossbow(NAMESPACE, "aether_rapid_crossbow", Equipment.Tier.TIER_4, aetherRepair).loot(-1, "aether"));
            addRanged(RangedWeapons.heavyCrossbow(NAMESPACE, "aether_heavy_crossbow", Equipment.Tier.TIER_4, aetherRepair).loot(-1, "aether"));
        }

        Weapon.register(meleeConfig, meleeEntries, Group.KEY);
        RangedWeapon.register(rangedConfig, rangedEntries, Group.KEY);
    }
}
