package com.ultra.megamod.feature.combat.spell;

import com.ultra.megamod.feature.combat.PlayerClassManager;
import com.ultra.megamod.feature.combat.PlayerClassManager.PlayerClass;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.skills.SkillBranch;
import com.ultra.megamod.feature.skills.SkillManager;
import com.ultra.megamod.feature.skills.SkillTreeType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * Determines whether a player can cast a given spell based on their class
 * selection and skill tree level.
 *
 * Spell unlock rules (post class-system migration):
 * - Player must have the matching class selected (e.g., WIZARD for wizard spells)
 * - Player must meet a minimum skill tree level in the class's associated tree:
 *     T0-T1 spells -> level 1
 *     T2 spells    -> level 5
 *     T3 spells    -> level 15
 *     T4 spells    -> level 25
 * - Spells with no classRequirement are always castable
 * - Admins bypass all restrictions
 * - SpellBook items in offhand bypass restrictions for their contained spells
 */
public final class SpellUnlockManager {

    private SpellUnlockManager() {}

    /**
     * Returns all spell IDs the player has unlocked via their class + tree level.
     *
     * @param playerId the player's UUID
     * @param level    any ServerLevel (used to obtain managers)
     * @return unmodifiable set of unlocked spell IDs
     */
    public static Set<String> getUnlockedSpells(UUID playerId, ServerLevel level) {
        PlayerClassManager pcm = PlayerClassManager.get(level);
        PlayerClass playerClass = pcm.getPlayerClass(playerId);
        SkillManager skills = SkillManager.get(level);

        Set<String> unlocked = new HashSet<>();
        for (SpellDefinition spell : SpellRegistry.ALL_SPELLS.values()) {
            if (isUnlockedByClass(playerId, spell, playerClass, skills)) {
                unlocked.add(spell.id());
            }
        }

        // Add permanently learned spells from scrolls
        unlocked.addAll(pcm.getLearnedSpells(playerId));

        return Collections.unmodifiableSet(unlocked);
    }

    /**
     * Checks whether a player can cast a specific spell.
     *
     * @param playerId the player's UUID
     * @param spellId  the spell ID to check (e.g. "fireball", "heal")
     * @param level    any ServerLevel (used to obtain managers)
     * @return true if the player can cast the spell, false otherwise
     */
    public static boolean canCastSpell(UUID playerId, String spellId, ServerLevel level) {
        // Admin bypass — admins can cast any spell
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerId);
        if (player != null && AdminSystem.isAdmin(player)) return true;

        // SpellBook bypass — check offhand
        if (player != null) {
            ItemStack offhand = player.getItemInHand(InteractionHand.OFF_HAND);
            if (offhand.getItem() instanceof SpellBookItem book) {
                if (book.getSpellIds().contains(spellId)) {
                    return true;
                }
            }
        }

        // Check permanently learned spells (from spell scrolls) — bypasses class/level
        PlayerClassManager pcm = PlayerClassManager.get(level);
        if (pcm.hasLearnedSpell(playerId, spellId)) return true;

        // Look up spell definition
        SpellDefinition spell = SpellRegistry.get(spellId);
        if (spell == null) return true; // unknown spell — allow by default

        // No class requirement — always castable
        if (spell.classRequirement() == null || spell.classRequirement().isEmpty()) return true;

        // Class-based unlock check
        PlayerClass playerClass = pcm.getPlayerClass(playerId);
        SkillManager skills = SkillManager.get(level);

        return isUnlockedByClass(playerId, spell, playerClass, skills);
    }

    /**
     * Core unlock logic: checks class match and class branch point investment.
     * Spells gate on points invested in the CLASS BRANCH (Paladin, Warrior, etc.),
     * not just the tree level. This ensures players invest in their class identity
     * to unlock higher-tier spells.
     */
    private static boolean isUnlockedByClass(UUID playerId, SpellDefinition spell,
                                              PlayerClass playerClass, SkillManager skills) {
        // No class requirement — always available
        if (spell.classRequirement() == null || spell.classRequirement().isEmpty()) return true;

        // Must have a class selected
        if (playerClass == PlayerClass.NONE) return false;

        // Class must match
        if (!playerClass.name().equalsIgnoreCase(spell.classRequirement())) return false;

        // Check class branch investment based on spell tier
        SkillBranch branch = playerClass.toBranch();
        if (branch == null) return false;
        int branchPoints = skills.getPointsInBranch(playerId, branch);
        int requiredPoints = getRequiredBranchPoints(spell.tier());
        return branchPoints >= requiredPoints;
    }

    /**
     * Maps spell tier to required points invested in the class branch.
     * T0-T1: Free with class (0 points needed)
     * T2: 3 points (just started investing in class branch)
     * T3: 8 points (solid investment, past T2 node)
     * T4: 15 points (near capstone, deep investment)
     */
    private static int getRequiredBranchPoints(int spellTier) {
        return switch (spellTier) {
            case 0, 1 -> 0;  // Free with class selection
            case 2 -> 3;     // After T1+T2 nodes (1+2=3)
            case 3 -> 8;     // After T1+T2+T3 nodes (1+2+3=6, need 2 more from other tree)
            case 4 -> 15;    // Near capstone (1+2+3+5=11, need some T5 or combo)
            default -> 0;
        };
    }

    /**
     * Returns the required class branch points for a spell's tier (useful for UI display).
     */
    public static int getRequiredLevelForSpell(String spellId) {
        SpellDefinition spell = SpellRegistry.get(spellId);
        if (spell == null) return 0;
        return getRequiredBranchPoints(spell.tier());
    }

    /**
     * Returns the class requirement string for a spell, or null if none.
     */
    public static String getClassRequirement(String spellId) {
        SpellDefinition spell = SpellRegistry.get(spellId);
        return spell != null ? spell.classRequirement() : null;
    }
}
