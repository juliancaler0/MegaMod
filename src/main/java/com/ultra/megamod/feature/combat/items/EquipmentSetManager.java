package com.ultra.megamod.feature.combat.items;

import com.ultra.megamod.feature.attributes.AttributeHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages equipment set bonuses for the RPG combat system.
 * Checks player armor every second (20 ticks) and applies/removes
 * attribute modifiers based on how many pieces of each set are worn.
 *
 * Each set bonus tier is cumulative — the highest qualifying tier
 * replaces all lower tiers (not stacked).
 */
public class EquipmentSetManager {

    // ─── Data types ───

    /**
     * A single tier of bonus within an equipment set.
     * @param requiredPieces number of set pieces needed to activate this tier
     * @param attributeModifiers map of attribute -> flat bonus amount
     */
    public record SetBonus(int requiredPieces, Map<Holder<Attribute>, Double> attributeModifiers) {}

    /**
     * An equipment set definition.
     * @param id unique identifier for this set (e.g. "arcane_robe")
     * @param displayName human-readable name
     * @param itemIds registry IDs of all items in this set (e.g. "megamod:arcane_robe_head")
     * @param bonuses list of tiered bonuses, ordered by requiredPieces ascending
     */
    public record EquipmentSet(String id, String displayName, Set<String> itemIds, List<SetBonus> bonuses) {}

    // ─── Singleton state ───

    private static final List<EquipmentSet> ALL_SETS = new ArrayList<>();
    private static boolean initialized = false;

    /** Per-player tracking of which set bonus tier is currently active. */
    private static final Map<UUID, Map<String, Integer>> playerActiveTiers = new ConcurrentHashMap<>();

    /** Modifier ID prefix for set bonuses. */
    private static final String MODIFIER_PREFIX = "megamod_set_";

    // ─── Initialization ───

    /** Load all set definitions. Called once during mod setup. */
    public static void initialize() {
        if (initialized) return;
        ALL_SETS.addAll(EquipmentSetDefinitions.buildAllSets());
        initialized = true;
    }

    // ─── Core tick logic ───

    /**
     * Called on server player tick. Only processes every 20 ticks (1 second).
     * Checks which armor pieces the player is wearing, determines which set
     * bonuses should be active, and applies/removes modifiers accordingly.
     */
    public static void tickPlayer(ServerPlayer player) {
        if (!initialized) initialize();
        if (player.tickCount % 20 != 0) return;

        UUID playerId = player.getUUID();
        Map<String, Integer> activeTiers = playerActiveTiers.computeIfAbsent(playerId, k -> new HashMap<>());

        // Collect all equipped item IDs from armor slots
        Set<String> equippedIds = new HashSet<>();
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                equippedIds.add(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            }
        }

        // Evaluate each set
        for (EquipmentSet set : ALL_SETS) {
            int matchCount = 0;
            for (String itemId : set.itemIds()) {
                if (equippedIds.contains(itemId)) {
                    matchCount++;
                }
            }

            // Find the highest qualifying bonus tier
            int bestTier = -1;
            for (int i = 0; i < set.bonuses().size(); i++) {
                if (matchCount >= set.bonuses().get(i).requiredPieces()) {
                    bestTier = i;
                }
            }

            int previousTier = activeTiers.getOrDefault(set.id(), -1);

            if (bestTier != previousTier) {
                // Remove old tier modifiers
                if (previousTier >= 0) {
                    removeSetBonusModifiers(player, set, previousTier);
                }
                // Apply new tier modifiers
                if (bestTier >= 0) {
                    applySetBonusModifiers(player, set, bestTier);
                }
                // Update tracking
                if (bestTier >= 0) {
                    activeTiers.put(set.id(), bestTier);
                } else {
                    activeTiers.remove(set.id());
                }
            }
        }
    }

    // ─── Modifier management ───

    private static void applySetBonusModifiers(ServerPlayer player, EquipmentSet set, int tierIndex) {
        SetBonus bonus = set.bonuses().get(tierIndex);
        // Class mastery scaling: bonus increases by 10% per 5 points in class branch
        double masteryMultiplier = getClassMasteryMultiplier(player, set.id());
        for (Map.Entry<Holder<Attribute>, Double> entry : bonus.attributeModifiers().entrySet()) {
            Identifier modId = Identifier.fromNamespaceAndPath("megamod", MODIFIER_PREFIX + set.id() + "_" + getAttributeKey(entry.getKey()));
            double scaledValue = entry.getValue() * masteryMultiplier;
            AttributeHelper.addModifier(player, entry.getKey(), modId, scaledValue, AttributeModifier.Operation.ADD_VALUE);
        }
    }

    /**
     * Returns the class mastery multiplier for set bonuses.
     * Every 5 points invested in the player's class branch → +10% set bonus.
     * Only applies to sets matching the player's class (wizard robes for wizards, etc.).
     */
    private static double getClassMasteryMultiplier(ServerPlayer player, String setId) {
        try {
            var level = (net.minecraft.server.level.ServerLevel) player.level();
            var pcm = com.ultra.megamod.feature.combat.PlayerClassManager.get(level);
            var playerClass = pcm.getPlayerClass(player.getUUID());
            if (playerClass == com.ultra.megamod.feature.combat.PlayerClassManager.PlayerClass.NONE) return 1.0;

            // Check if this set matches the player's class (by keyword in set ID)
            String classKeyword = playerClass.name().toLowerCase();
            String setLower = setId.toLowerCase();
            boolean isClassSet = setLower.contains(classKeyword)
                    || (playerClass.name().equals("WIZARD") && (setLower.contains("wizard") || setLower.contains("arcane") || setLower.contains("fire_robe") || setLower.contains("frost_robe")))
                    || (playerClass.name().equals("PALADIN") && (setLower.contains("paladin") || setLower.contains("crusader") || setLower.contains("priest") || setLower.contains("prior")))
                    || (playerClass.name().equals("WARRIOR") && (setLower.contains("warrior") || setLower.contains("berserker")))
                    || (playerClass.name().equals("ROGUE") && (setLower.contains("rogue") || setLower.contains("assassin")))
                    || (playerClass.name().equals("RANGER") && (setLower.contains("ranger") || setLower.contains("archer")));

            if (!isClassSet) return 1.0;

            var skills = com.ultra.megamod.feature.skills.SkillManager.get(level);
            var branch = playerClass.toBranch();
            if (branch == null) return 1.0;
            int branchPoints = skills.getPointsInBranch(player.getUUID(), branch);
            // +10% per 5 points invested, max ~36% at 18 points
            return 1.0 + (branchPoints / 5) * 0.10;
        } catch (Exception e) {
            return 1.0;
        }
    }

    private static void removeSetBonusModifiers(ServerPlayer player, EquipmentSet set, int tierIndex) {
        SetBonus bonus = set.bonuses().get(tierIndex);
        for (Map.Entry<Holder<Attribute>, Double> entry : bonus.attributeModifiers().entrySet()) {
            Identifier modId = Identifier.fromNamespaceAndPath("megamod", MODIFIER_PREFIX + set.id() + "_" + getAttributeKey(entry.getKey()));
            AttributeHelper.removeModifier(player, entry.getKey(), modId);
        }
    }

    /**
     * Extracts a short key from an attribute holder for use in modifier IDs.
     * Falls back to hashCode if the registry key cannot be resolved.
     */
    private static String getAttributeKey(Holder<Attribute> holder) {
        try {
            return holder.unwrapKey()
                .map(key -> key.identifier().getPath())
                .orElse("attr_" + holder.hashCode());
        } catch (Exception e) {
            return "attr_" + holder.hashCode();
        }
    }

    // ─── Player lifecycle ───

    /** Clear tracking when a player disconnects. */
    public static void onPlayerLogout(ServerPlayer player) {
        playerActiveTiers.remove(player.getUUID());
    }

    /** Remove all set bonus modifiers from a player (e.g., on death or dimension change). */
    public static void clearAllBonuses(ServerPlayer player) {
        UUID playerId = player.getUUID();
        Map<String, Integer> activeTiers = playerActiveTiers.get(playerId);
        if (activeTiers == null) return;

        for (EquipmentSet set : ALL_SETS) {
            Integer tierIndex = activeTiers.get(set.id());
            if (tierIndex != null && tierIndex >= 0) {
                removeSetBonusModifiers(player, set, tierIndex);
            }
        }
        activeTiers.clear();
    }

    // ─── Query API ───

    /** Returns all registered equipment sets. */
    public static List<EquipmentSet> getAllSets() {
        if (!initialized) initialize();
        return Collections.unmodifiableList(ALL_SETS);
    }

    /**
     * Returns the currently active set bonuses for a player.
     * @return map of set ID -> active tier index, or empty if none
     */
    public static Map<String, Integer> getActiveBonuses(ServerPlayer player) {
        Map<String, Integer> tiers = playerActiveTiers.get(player.getUUID());
        return tiers != null ? Collections.unmodifiableMap(tiers) : Collections.emptyMap();
    }

    /**
     * Counts how many pieces of a given set the player currently has equipped.
     */
    public static int countEquippedPieces(ServerPlayer player, EquipmentSet set) {
        int count = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                if (set.itemIds().contains(id)) {
                    count++;
                }
            }
        }
        return count;
    }
}
