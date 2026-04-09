package com.ultra.megamod.feature.dungeons.insurance;

import com.ultra.megamod.feature.dungeons.DungeonTier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class InsuranceCosts {

    /**
     * Calculate insurance cost for an item in a given dungeon tier.
     * Cost = base cost (from item value) * tier multiplier.
     */
    public static int getCost(ItemStack stack, DungeonTier tier) {
        if (stack.isEmpty()) return 0;
        int baseCost = getBaseCost(stack);
        double multiplier = getTierMultiplier(tier);
        return Math.max(10, (int) (baseCost * multiplier));
    }

    private static int getBaseCost(ItemStack stack) {
        int cost = getMaterialCost(stack);

        // Rarity bonus
        Rarity rarity = stack.getRarity();
        if (rarity == Rarity.EPIC) cost += 600;
        else if (rarity == Rarity.RARE) cost += 300;
        else if (rarity == Rarity.UNCOMMON) cost += 100;

        // Enchantment bonus: each enchant level adds value
        ItemEnchantments enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (!enchants.isEmpty()) {
            // Count total enchantment levels
            int totalLevels = enchants.size(); // rough count
            cost += totalLevels * 75;
        }

        // Stack size discount — bulk items cost less per unit
        if (stack.getCount() > 1) {
            cost = cost / 2; // half price per item for stacks
        }

        return Math.max(25, cost);
    }

    /**
     * Value based on material tier / item type, not just rarity.
     */
    private static int getMaterialCost(ItemStack stack) {
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

        // Mythic Netherite (dungeon exclusive, highest tier)
        if (id.startsWith("mythic_netherite_")) return 1500;

        // Netherite tier
        if (id.contains("netherite")) return 800;

        // Diamond tier
        if (id.contains("diamond")) return 400;

        // Iron tier
        if (id.contains("iron") && (id.contains("sword") || id.contains("axe") || id.contains("pickaxe")
                || id.contains("shovel") || id.contains("hoe") || id.contains("helmet")
                || id.contains("chestplate") || id.contains("leggings") || id.contains("boots"))) return 150;

        // Special vanilla items
        if (stack.getItem() == Items.ELYTRA) return 1200;
        if (stack.getItem() == Items.TRIDENT) return 600;
        if (stack.getItem() == Items.MACE) return 600;
        if (stack.getItem() == Items.TOTEM_OF_UNDYING) return 500;
        if (stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) return 400;
        if (stack.getItem() == Items.NETHER_STAR) return 1000;
        if (stack.getItem() == Items.SHIELD) return 100;
        if (stack.getItem() == Items.BOW || stack.getItem() == Items.CROSSBOW) return 100;

        // Modded relics/weapons — check if from megamod namespace
        String ns = BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
        if ("megamod".equals(ns)) {
            // Dungeon exclusive items
            if (id.contains("unique_") || id.contains("staff_")) return 500;
            if (id.contains("geomancer_")) return 600;
            // Relics
            if (stack.getItem() instanceof com.ultra.megamod.feature.relics.RelicItem) return 400;
            // RPG weapons
            if (stack.getItem() instanceof com.ultra.megamod.feature.relics.weapons.RpgWeaponItem) return 500;
            // Other megamod items
            return 200;
        }

        // Default: basic items
        return 100;
    }

    private static double getTierMultiplier(DungeonTier tier) {
        return switch (tier) {
            case NORMAL -> 1.0;
            case HARD -> 1.5;
            case NIGHTMARE -> 2.5;
            case INFERNAL -> 4.0;
            case MYTHIC -> 6.0;
            case ETERNAL -> 10.0;
        };
    }
}
