package com.ultra.megamod.feature.combat.spell.client;

import com.ultra.megamod.feature.combat.PlayerClassManager.PlayerClass;
import com.ultra.megamod.feature.combat.client.ClientClassCache;
import com.ultra.megamod.feature.combat.spell.SpellDefinition;
import com.ultra.megamod.feature.combat.spell.SpellRegistry;
import com.ultra.megamod.feature.skills.SkillBranch;
import com.ultra.megamod.feature.skills.SkillTreeType;
import com.ultra.megamod.feature.skills.network.SkillSyncPayload;

import java.util.*;

/**
 * Client-side utility for checking spell unlock status using synced data.
 * Uses {@link SkillSyncPayload#clientLevels} for tree levels and
 * {@link ClientClassCache} for the player's class.
 * <p>
 * This is an approximation — the server remains authoritative via
 * {@link com.ultra.megamod.feature.combat.spell.SpellUnlockManager}.
 */
public final class SpellUnlockClientHelper {

    private SpellUnlockClientHelper() {}

    /**
     * Maps spell tier to required tree level — mirrors SpellUnlockManager logic.
     */
    public static int getRequiredLevel(int spellTier) {
        return switch (spellTier) {
            case 0, 1 -> 1;
            case 2 -> 5;
            case 3 -> 15;
            case 4 -> 25;
            default -> 1;
        };
    }

    /**
     * Returns the required tree level for a specific spell.
     */
    public static int getRequiredLevelForSpell(String spellId) {
        SpellDefinition spell = SpellRegistry.get(spellId);
        if (spell == null) return 0;
        return getRequiredLevel(spell.tier());
    }

    /**
     * Returns the SkillTreeType associated with a PlayerClass, or null for NONE.
     */
    public static SkillTreeType getTreeForClass(PlayerClass cls) {
        if (cls == null || cls == PlayerClass.NONE) return null;
        SkillBranch branch = cls.toBranch();
        return branch != null ? branch.getTreeType() : null;
    }

    /**
     * Returns the player's current level in their class's associated skill tree.
     */
    public static int getPlayerTreeLevel() {
        PlayerClass cls = ClientClassCache.getPlayerClass();
        SkillTreeType tree = getTreeForClass(cls);
        if (tree == null) return 0;
        return SkillSyncPayload.clientLevels.getOrDefault(tree, 0);
    }

    /**
     * Checks whether a spell is unlocked for the local player based on synced data.
     * Spells with no class requirement are always unlocked.
     */
    public static boolean isSpellUnlocked(SpellDefinition spell) {
        if (spell == null) return false;

        // No class requirement — always available
        if (spell.classRequirement() == null || spell.classRequirement().isEmpty()) return true;

        PlayerClass cls = ClientClassCache.getPlayerClass();
        if (cls == PlayerClass.NONE) return false;

        // Class must match
        if (!cls.name().equalsIgnoreCase(spell.classRequirement())) return false;

        // Check tree level
        SkillTreeType tree = getTreeForClass(cls);
        if (tree == null) return false;
        int playerLevel = SkillSyncPayload.clientLevels.getOrDefault(tree, 0);
        int requiredLevel = getRequiredLevel(spell.tier());
        return playerLevel >= requiredLevel;
    }

    /**
     * Checks whether a spell is unlocked by spell ID.
     */
    public static boolean isSpellUnlocked(String spellId) {
        return isSpellUnlocked(SpellRegistry.get(spellId));
    }

    /**
     * Returns the display string for a spell's tree requirement (e.g., "Combat Lv. 5").
     */
    public static String getRequirementString(SpellDefinition spell) {
        if (spell == null || spell.classRequirement() == null || spell.classRequirement().isEmpty()) {
            return "";
        }
        try {
            PlayerClass reqClass = PlayerClass.valueOf(spell.classRequirement().toUpperCase());
            SkillTreeType tree = getTreeForClass(reqClass);
            if (tree == null) return reqClass.getDisplayName() + " class";
            int reqLevel = getRequiredLevel(spell.tier());
            return reqClass.getDisplayName() + " class, " + tree.getDisplayName() + " Lv. " + reqLevel;
        } catch (IllegalArgumentException e) {
            return spell.classRequirement() + " class";
        }
    }

    /**
     * Returns the first locked spell that would be next to unlock based on current level.
     * Considers only spells that match the player's class.
     */
    public static SpellDefinition getNextUnlock() {
        PlayerClass cls = ClientClassCache.getPlayerClass();
        if (cls == PlayerClass.NONE) return null;

        SkillTreeType tree = getTreeForClass(cls);
        if (tree == null) return null;

        int playerLevel = SkillSyncPayload.clientLevels.getOrDefault(tree, 0);

        // Find all locked spells for this class, sorted by required level
        SpellDefinition bestCandidate = null;
        int bestReqLevel = Integer.MAX_VALUE;

        for (SpellDefinition spell : SpellRegistry.ALL_SPELLS.values()) {
            if (spell.classRequirement() == null || spell.classRequirement().isEmpty()) continue;
            if (!cls.name().equalsIgnoreCase(spell.classRequirement())) continue;

            int reqLevel = getRequiredLevel(spell.tier());
            if (reqLevel > playerLevel && reqLevel < bestReqLevel) {
                bestReqLevel = reqLevel;
                bestCandidate = spell;
            }
        }

        return bestCandidate;
    }

    /**
     * Returns all spells for a given class, grouped by required level.
     * Keys are sorted ascending. Values are sorted alphabetically.
     */
    public static TreeMap<Integer, List<SpellDefinition>> getSpellsByLevel(PlayerClass cls) {
        TreeMap<Integer, List<SpellDefinition>> map = new TreeMap<>();
        if (cls == null || cls == PlayerClass.NONE) return map;

        for (SpellDefinition spell : SpellRegistry.ALL_SPELLS.values()) {
            if (spell.classRequirement() == null || spell.classRequirement().isEmpty()) continue;
            if (!cls.name().equalsIgnoreCase(spell.classRequirement())) continue;

            int reqLevel = getRequiredLevel(spell.tier());
            map.computeIfAbsent(reqLevel, k -> new ArrayList<>()).add(spell);
        }

        // Sort each group alphabetically
        for (List<SpellDefinition> group : map.values()) {
            group.sort(Comparator.comparing(SpellDefinition::name));
        }

        return map;
    }

    /**
     * Returns all spells for the given class, sorted by tier then name.
     */
    public static List<SpellDefinition> getAllSpellsForClass(PlayerClass cls) {
        if (cls == null || cls == PlayerClass.NONE) return List.of();

        List<SpellDefinition> result = new ArrayList<>();
        for (SpellDefinition spell : SpellRegistry.ALL_SPELLS.values()) {
            if (spell.classRequirement() == null || spell.classRequirement().isEmpty()) continue;
            if (!cls.name().equalsIgnoreCase(spell.classRequirement())) continue;
            result.add(spell);
        }

        result.sort(Comparator.comparingInt(SpellDefinition::tier).thenComparing(SpellDefinition::name));
        return result;
    }
}
