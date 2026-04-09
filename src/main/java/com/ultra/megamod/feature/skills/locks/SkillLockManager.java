package com.ultra.megamod.feature.skills.locks;

import com.ultra.megamod.feature.combat.PlayerClassManager;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.skills.SkillBranch;
import com.ultra.megamod.feature.skills.SkillManager;
import com.ultra.megamod.feature.skills.SkillNode;
import com.ultra.megamod.feature.skills.SkillTreeDefinitions;
import com.ultra.megamod.feature.toggles.FeatureToggleManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Set;

/**
 * Core logic for checking skill-based item and enchantment locks.
 */
public final class SkillLockManager {

    private SkillLockManager() {}

    // Toggle IDs in FeatureToggleManager
    public static final String TOGGLE_SKILL_LOCKS = "skill_item_locks";
    public static final String TOGGLE_ADMIN_BYPASS = "admin_skill_lock_bypass";

    // ==================== Main Check Methods ====================

    /**
     * Check if a player can USE an item (weapon, tool, consumable, equipment).
     * Returns true if allowed, false if locked.
     */
    public static boolean canUseItem(ServerPlayer player, ItemStack stack) {
        if (!isLockSystemEnabled(player)) return true;
        if (isAdminBypassing(player)) return true;
        if (stack.isEmpty()) return true;

        String itemPath = getItemPath(stack);

        // Check direct use locks
        for (SkillLockDefinitions.UseLock lock : SkillLockDefinitions.USE_LOCKS) {
            if (lock.itemPatterns().contains(itemPath)) {
                return hasRequiredBranch(player, lock.branchA(), lock.branchB());
            }
        }

        // Check splash/lingering potions
        if (SkillLockDefinitions.SPLASH_POTIONS.itemPatterns().contains(itemPath)) {
            return hasRequiredBranch(player,
                    SkillLockDefinitions.SPLASH_POTIONS.branchA(),
                    SkillLockDefinitions.SPLASH_POTIONS.branchB());
        }
        if (SkillLockDefinitions.LINGERING_POTIONS.itemPatterns().contains(itemPath)) {
            return hasRequiredBranch(player,
                    SkillLockDefinitions.LINGERING_POTIONS.branchA(),
                    SkillLockDefinitions.LINGERING_POTIONS.branchB());
        }

        // Check enchanted vanilla items (only locked when enchanted)
        SkillLockDefinitions.UseLock vanillaLock = SkillLockDefinitions.ENCHANTED_VANILLA_LOCKS.get(itemPath);
        if (vanillaLock != null && isEnchanted(stack)) {
            return hasRequiredBranch(player, vanillaLock.branchA(), vanillaLock.branchB());
        }

        return true;
    }

    /**
     * Check if an enchantment can GENERATE for this player (enchanting table, loot, books).
     * Returns true if allowed, false if the enchantment should be suppressed.
     */
    public static boolean canGenerateEnchant(ServerPlayer player, String enchantPath, int level) {
        if (!isLockSystemEnabled(player)) return true;
        if (isAdminBypassing(player)) return true;

        for (SkillLockDefinitions.EnchantLock lock : SkillLockDefinitions.ENCHANT_LOCKS) {
            if (lock.enchantId().equals(enchantPath) && level >= lock.minLockedLevel()) {
                return hasRequiredBranch(player, lock.branchA(), lock.branchB());
            }
        }
        return true;
    }

    /**
     * Check if a player can CRAFT an item (recipe output).
     * Returns true if allowed, false if locked.
     */
    public static boolean canCraftItem(ServerPlayer player, ItemStack result) {
        if (!isLockSystemEnabled(player)) return true;
        if (isAdminBypassing(player)) return true;
        if (result.isEmpty()) return true;

        String itemPath = getItemPath(result);

        for (SkillLockDefinitions.CraftLock lock : SkillLockDefinitions.CRAFT_LOCKS) {
            if (lock.itemPatterns().contains(itemPath)) {
                return hasRequiredBranch(player, lock.branchA(), lock.branchB());
            }
        }
        return true;
    }

    /**
     * Get the craft lock for an item (for recipe hiding/tooltips).
     * Returns null if not craft-locked.
     */
    public static SkillLockDefinitions.CraftLock getCraftLock(ItemStack result) {
        if (result.isEmpty()) return null;
        String itemPath = getItemPath(result);
        for (SkillLockDefinitions.CraftLock lock : SkillLockDefinitions.CRAFT_LOCKS) {
            if (lock.itemPatterns().contains(itemPath)) return lock;
        }
        return null;
    }

    /**
     * Get the lock info for a use-locked item (for tooltips).
     * Returns null if the item is not locked.
     */
    public static SkillLockDefinitions.UseLock getUseLock(ItemStack stack) {
        if (stack.isEmpty()) return null;
        String itemPath = getItemPath(stack);

        for (SkillLockDefinitions.UseLock lock : SkillLockDefinitions.USE_LOCKS) {
            if (lock.itemPatterns().contains(itemPath)) {
                return lock;
            }
        }

        // Splash/lingering
        if (SkillLockDefinitions.SPLASH_POTIONS.itemPatterns().contains(itemPath)) {
            return SkillLockDefinitions.SPLASH_POTIONS;
        }
        if (SkillLockDefinitions.LINGERING_POTIONS.itemPatterns().contains(itemPath)) {
            return SkillLockDefinitions.LINGERING_POTIONS;
        }

        // Enchanted vanilla
        SkillLockDefinitions.UseLock vanillaLock = SkillLockDefinitions.ENCHANTED_VANILLA_LOCKS.get(itemPath);
        if (vanillaLock != null && isEnchanted(stack)) {
            return vanillaLock;
        }

        return null;
    }

    /**
     * Get the lock category name for a locked item (for chat messages).
     */
    public static String getLockMessage(ItemStack stack) {
        SkillLockDefinitions.UseLock lock = getUseLock(stack);
        if (lock == null) return null;
        String branchA = lock.branchA().getDisplayName();
        String branchB = lock.branchB() != null ? lock.branchB().getDisplayName() : null;
        if (branchB != null) {
            return "Requires " + branchA + " or " + branchB;
        }
        return "Requires " + branchA;
    }

    // ==================== Internal Helpers ====================

    private static boolean isLockSystemEnabled(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        FeatureToggleManager toggles = FeatureToggleManager.get(level);
        return toggles.isEnabled(TOGGLE_SKILL_LOCKS);
    }

    public static boolean isAdminBypassing(ServerPlayer player) {
        if (!AdminSystem.isAdmin(player)) return false;
        ServerLevel level = (ServerLevel) player.level();
        FeatureToggleManager toggles = FeatureToggleManager.get(level);
        return toggles.isEnabled(TOGGLE_ADMIN_BYPASS);
    }

    /**
     * Check if player has the required branch. For class archetype branches
     * (Paladin, Warrior, Wizard, Rogue, Ranger), checks the player's chosen class
     * via PlayerClassManager. For regular skill branches, checks tier 3+ nodes.
     */
    private static boolean hasRequiredBranch(ServerPlayer player, SkillBranch branchA, SkillBranch branchB) {
        // Admin bypass — admins can use any class weapon/armor
        if (AdminSystem.isAdmin(player)) return true;

        ServerLevel level = (ServerLevel) player.level();

        // If either branch is a class archetype, use class-based check
        boolean aIsClass = PlayerClassManager.isClassBranch(branchA);
        boolean bIsClass = PlayerClassManager.isClassBranch(branchB);

        if (aIsClass || bIsClass) {
            PlayerClassManager classManager = PlayerClassManager.get(level);
            return classManager.classAllowsBranch(player.getUUID(), branchA, branchB);
        }

        // Regular skill branch check: tier 3+ node required
        SkillManager manager = SkillManager.get(level);
        Set<String> unlocked = manager.getUnlockedNodes(player.getUUID());

        if (branchA != null && isBranchSpecialized(unlocked, branchA)) return true;
        if (branchB != null && isBranchSpecialized(unlocked, branchB)) return true;
        return false;
    }

    /**
     * A branch is "specialized" if the player has at least one tier 3+ node in it.
     */
    private static boolean isBranchSpecialized(Set<String> unlockedNodes, SkillBranch branch) {
        for (String nodeId : unlockedNodes) {
            SkillNode node = SkillTreeDefinitions.getNodeById(nodeId);
            if (node != null && node.branch() == branch && node.tier() >= 3) {
                return true;
            }
        }
        return false;
    }

    private static String getItemPath(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
    }

    private static boolean isEnchanted(ItemStack stack) {
        // In 1.21.x, check for enchantments via the component system
        ItemEnchantments enchants = stack.getOrDefault(
                net.minecraft.core.component.DataComponents.ENCHANTMENTS,
                ItemEnchantments.EMPTY);
        return !enchants.isEmpty();
    }
}
