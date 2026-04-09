package com.ultra.megamod.feature.citizen.data;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Tool, sword, and armor tier restrictions based on building (hut) level.
 * Matches the MegaColonies tier system:
 * <ul>
 *   <li>Hut L0: Wood/Gold only (tier 0)</li>
 *   <li>Hut L1: Stone (tier 1)</li>
 *   <li>Hut L2: Iron (tier 2)</li>
 *   <li>Hut L3: Diamond (tier 3)</li>
 *   <li>Hut L4-5: Netherite (tier 4)</li>
 * </ul>
 *
 * Enchantment limits:
 * <ul>
 *   <li>Hut L0: No enchantments</li>
 *   <li>Hut L1: Max enchant level 1</li>
 *   <li>Hut L2: Max enchant level 2</li>
 *   <li>Hut L3: Max enchant level 3</li>
 *   <li>Hut L4: Max enchant level 4</li>
 *   <li>Hut L5: Unlimited</li>
 * </ul>
 */
public class ToolLevelRestrictions {

    // Tool tiers: WOOD=0, STONE=1, IRON=2, DIAMOND=3, NETHERITE=4
    public static final int TIER_WOOD = 0;
    public static final int TIER_STONE = 1;
    public static final int TIER_IRON = 2;
    public static final int TIER_DIAMOND = 3;
    public static final int TIER_NETHERITE = 4;

    /**
     * Returns the maximum tool/weapon tier allowed for the given hut level.
     * Hut L0: 0 (wood/gold), L1: 1 (stone), L2: 2 (iron), L3: 3 (diamond), L4+: 4 (netherite).
     */
    public static int getMaxToolTier(int hutLevel) {
        if (hutLevel <= 0) return TIER_WOOD;
        if (hutLevel == 1) return TIER_STONE;
        if (hutLevel == 2) return TIER_IRON;
        if (hutLevel == 3) return TIER_DIAMOND;
        return TIER_NETHERITE; // L4-5
    }

    /**
     * Returns the tier of a given tool/weapon ItemStack.
     * Uses mining speed from the Tool component as a proxy for tier:
     * <ul>
     *   <li>Speed <= 2.0 (wood/gold): tier 0</li>
     *   <li>Speed <= 4.0 (stone): tier 1</li>
     *   <li>Speed <= 6.0 (iron): tier 2</li>
     *   <li>Speed <= 8.0 (diamond): tier 3</li>
     *   <li>Speed > 8.0 (netherite): tier 4</li>
     * </ul>
     * For swords (no mining speed), checks item type.
     */
    public static int getItemTier(ItemStack stack) {
        if (stack.isEmpty()) return -1;

        Item item = stack.getItem();

        // Sword tier detection by direct item comparison
        int swordTier = getSwordTier(item);
        if (swordTier >= 0) {
            return swordTier;
        }

        // Tool tier detection via Tool component mining speed
        Tool tool = stack.get(DataComponents.TOOL);
        if (tool != null) {
            float speed = tool.defaultMiningSpeed();
            if (speed <= 2.0f) return TIER_WOOD;
            if (speed <= 4.0f) return TIER_STONE;
            if (speed <= 6.5f) return TIER_IRON;
            if (speed <= 8.5f) return TIER_DIAMOND;
            return TIER_NETHERITE;
        }

        // Fallback: check by item class for known types
        if (item == Items.WOODEN_PICKAXE || item == Items.WOODEN_AXE || item == Items.WOODEN_SHOVEL || item == Items.WOODEN_HOE
            || item == Items.GOLDEN_PICKAXE || item == Items.GOLDEN_AXE || item == Items.GOLDEN_SHOVEL || item == Items.GOLDEN_HOE) {
            return TIER_WOOD;
        }
        if (item == Items.STONE_PICKAXE || item == Items.STONE_AXE || item == Items.STONE_SHOVEL || item == Items.STONE_HOE) {
            return TIER_STONE;
        }
        if (item == Items.IRON_PICKAXE || item == Items.IRON_AXE || item == Items.IRON_SHOVEL || item == Items.IRON_HOE) {
            return TIER_IRON;
        }
        if (item == Items.DIAMOND_PICKAXE || item == Items.DIAMOND_AXE || item == Items.DIAMOND_SHOVEL || item == Items.DIAMOND_HOE) {
            return TIER_DIAMOND;
        }
        if (item == Items.NETHERITE_PICKAXE || item == Items.NETHERITE_AXE || item == Items.NETHERITE_SHOVEL || item == Items.NETHERITE_HOE) {
            return TIER_NETHERITE;
        }

        return TIER_WOOD; // unknown tools default to lowest tier
    }

    /**
     * Returns the tier of a sword item, or -1 if not a vanilla sword.
     */
    private static int getSwordTier(Item item) {
        if (item == Items.WOODEN_SWORD || item == Items.GOLDEN_SWORD) return TIER_WOOD;
        if (item == Items.STONE_SWORD) return TIER_STONE;
        if (item == Items.IRON_SWORD) return TIER_IRON;
        if (item == Items.DIAMOND_SWORD) return TIER_DIAMOND;
        if (item == Items.NETHERITE_SWORD) return TIER_NETHERITE;
        return -1; // not a vanilla sword
    }

    /**
     * Returns the armor tier based on the armor item.
     * Leather = 0, Chain/Gold = 1, Iron = 2, Diamond = 3, Netherite = 4.
     */
    public static int getArmorTier(ItemStack stack) {
        if (stack.isEmpty()) return -1;
        Item item = stack.getItem();

        // Leather
        if (item == Items.LEATHER_HELMET || item == Items.LEATHER_CHESTPLATE
            || item == Items.LEATHER_LEGGINGS || item == Items.LEATHER_BOOTS) {
            return TIER_WOOD;
        }
        // Chain / Gold
        if (item == Items.CHAINMAIL_HELMET || item == Items.CHAINMAIL_CHESTPLATE
            || item == Items.CHAINMAIL_LEGGINGS || item == Items.CHAINMAIL_BOOTS
            || item == Items.GOLDEN_HELMET || item == Items.GOLDEN_CHESTPLATE
            || item == Items.GOLDEN_LEGGINGS || item == Items.GOLDEN_BOOTS) {
            return TIER_STONE;
        }
        // Iron
        if (item == Items.IRON_HELMET || item == Items.IRON_CHESTPLATE
            || item == Items.IRON_LEGGINGS || item == Items.IRON_BOOTS) {
            return TIER_IRON;
        }
        // Diamond
        if (item == Items.DIAMOND_HELMET || item == Items.DIAMOND_CHESTPLATE
            || item == Items.DIAMOND_LEGGINGS || item == Items.DIAMOND_BOOTS) {
            return TIER_DIAMOND;
        }
        // Netherite
        if (item == Items.NETHERITE_HELMET || item == Items.NETHERITE_CHESTPLATE
            || item == Items.NETHERITE_LEGGINGS || item == Items.NETHERITE_BOOTS) {
            return TIER_NETHERITE;
        }

        // Check if it has Equippable component (custom armor)
        if (stack.has(DataComponents.EQUIPPABLE)) {
            // Custom armor defaults to wood tier (most permissive)
            return TIER_WOOD;
        }

        return -1; // not armor
    }

    /**
     * Returns true if the given tool/weapon can be used at the given hut level.
     */
    public static boolean canUseTool(ItemStack tool, int hutLevel) {
        int itemTier = getItemTier(tool);
        if (itemTier < 0) return true; // not a tool
        int maxTier = getMaxToolTier(hutLevel);

        // Also check enchantment levels
        if (!checkEnchantments(tool, hutLevel)) return false;

        return itemTier <= maxTier;
    }

    /**
     * Returns true if the given armor can be worn at the given hut level.
     */
    public static boolean canUseArmor(ItemStack armor, int hutLevel) {
        int armorTier = getArmorTier(armor);
        if (armorTier < 0) return true; // not armor
        int maxTier = getMaxToolTier(hutLevel);

        if (!checkEnchantments(armor, hutLevel)) return false;

        return armorTier <= maxTier;
    }

    /**
     * Returns the maximum enchantment level allowed at the given hut level.
     * L0: 0 (no enchants), L1: 1, L2: 2, L3: 3, L4: 4, L5+: unlimited (Integer.MAX_VALUE).
     */
    public static int getMaxEnchantLevel(int hutLevel) {
        if (hutLevel <= 0) return 0;
        if (hutLevel >= 5) return Integer.MAX_VALUE;
        return hutLevel; // L1=1, L2=2, L3=3, L4=4
    }

    /**
     * Checks that all enchantments on the item are within the allowed level for the hut.
     */
    private static boolean checkEnchantments(ItemStack stack, int hutLevel) {
        int maxEnchant = getMaxEnchantLevel(hutLevel);
        if (maxEnchant == Integer.MAX_VALUE) return true; // unlimited

        ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
        if (enchantments == null || enchantments.isEmpty()) return true;

        // Check each enchantment level
        for (var entry : enchantments.entrySet()) {
            if (entry.getIntValue() > maxEnchant) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a human-readable tier name for display purposes.
     */
    public static String getTierName(int tier) {
        return switch (tier) {
            case TIER_WOOD -> "Wood/Gold";
            case TIER_STONE -> "Stone";
            case TIER_IRON -> "Iron";
            case TIER_DIAMOND -> "Diamond";
            case TIER_NETHERITE -> "Netherite";
            default -> "Unknown";
        };
    }
}
