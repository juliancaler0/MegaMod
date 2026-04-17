package com.ultra.megamod.feature.skills.prestige;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.skills.SkillTreeType;
import com.ultra.megamod.feature.skills.adminbridge.SkillAdminBridge;
import com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Per-player, per-category prestige on top of Pufferfish Skills.
 *
 * <p>Each prestige level:
 * <ul>
 *     <li>Resets all skills in the selected category via {@link SkillsAPI#getCategory}
 *     <li>Increments the prestige counter (capped at {@link #MAX_PRESTIGE})
 *     <li>Applies a permanent {@link AttributeModifier} sized to the new total prestige
 *     <li>Costs {@link #COST_BASE} * (currentPrestige + 1) MegaCoins
 * </ul>
 *
 * <p>Modifiers are reapplied on player login so they survive logout. File schema is
 * versioned ({@link #SCHEMA_VERSION}); older files written by the previous enum-keyed
 * implementation are migrated on load.
 */
public class PrestigeManager {
    private static PrestigeManager INSTANCE;
    private static final String FILE_NAME = "megamod_prestige.dat";
    private static final int SCHEMA_VERSION = 2;

    public static final int MAX_PRESTIGE = 5;
    public static final double BONUS_PER_PRESTIGE = 0.02; // 2% per prestige
    public static final int COST_BASE = 5000;             // MegaCoins
    public static final int MIN_SPENT_POINTS = 20;        // floor before prestige is unlocked

    // Category IDs — match the skill_tree_rpgs data pack categories.
    public static final Identifier CLASS_CATEGORY = Identifier.fromNamespaceAndPath("skill_tree_rpgs", "class_skills");
    public static final Identifier WEAPON_CATEGORY = Identifier.fromNamespaceAndPath("skill_tree_rpgs", "weapon_skills");
    public static final List<Identifier> ALL_CATEGORIES = List.of(CLASS_CATEGORY, WEAPON_CATEGORY);

    // Stable modifier IDs so we can remove + re-apply cleanly on each prestige change.
    private static final Identifier CLASS_MODIFIER_ID = Identifier.fromNamespaceAndPath("megamod", "prestige/class_skills");
    private static final Identifier WEAPON_MODIFIER_ID = Identifier.fromNamespaceAndPath("megamod", "prestige/weapon_skills");

    // Class prestige → boosts magic crit damage (scales all spell trees).
    // Weapon prestige → boosts melee/ranged attack damage (scales every weapon root).
    private static final Identifier CLASS_ATTRIBUTE_ID = Identifier.fromNamespaceAndPath("megamod", "sp_critical_damage");
    private static final Identifier WEAPON_ATTRIBUTE_ID = Identifier.fromNamespaceAndPath("minecraft", "attack_damage");

    private final Map<UUID, Map<Identifier, Integer>> prestigeLevels = new HashMap<>();
    private boolean dirty = false;

    public static PrestigeManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new PrestigeManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    private void markDirty() { this.dirty = true; }

    // --- Read API -------------------------------------------------------

    public int getPrestigeLevel(UUID playerId, Identifier categoryId) {
        var levels = prestigeLevels.get(playerId);
        return levels == null ? 0 : levels.getOrDefault(categoryId, 0);
    }

    /** Legacy enum overload — COMBAT maps to weapon_skills, everything else to class_skills. */
    public int getPrestigeLevel(UUID playerId, SkillTreeType tree) {
        return getPrestigeLevel(playerId, legacyToCategory(tree));
    }

    public int getTotalPrestige(UUID playerId) {
        var levels = prestigeLevels.get(playerId);
        if (levels == null) return 0;
        int total = 0;
        for (int v : levels.values()) total += v;
        return total;
    }

    public Map<Identifier, Integer> snapshot(UUID playerId) {
        var levels = prestigeLevels.get(playerId);
        return levels == null ? Collections.emptyMap() : new LinkedHashMap<>(levels);
    }

    public double getPrestigeBonus(UUID playerId, Identifier categoryId) {
        return getPrestigeLevel(playerId, categoryId) * BONUS_PER_PRESTIGE;
    }

    /** Legacy enum overload. */
    public double getPrestigeBonus(UUID playerId, SkillTreeType tree) {
        return getPrestigeBonus(playerId, legacyToCategory(tree));
    }

    public boolean hasThirdBranchUnlock(UUID playerId, SkillTreeType tree) {
        return getPrestigeLevel(playerId, tree) >= 3;
    }

    public int getPrestigeCost(UUID playerId, Identifier categoryId) {
        return COST_BASE * (getPrestigeLevel(playerId, categoryId) + 1);
    }

    // --- Prestige workflow ---------------------------------------------

    public enum Result {
        SUCCESS,
        MAX_REACHED,
        UNKNOWN_CATEGORY,
        INSUFFICIENT_POINTS,
        INSUFFICIENT_FUNDS
    }

    /** Full prestige workflow: checks + charge + reset skills + increment + reapply modifier. */
    public Result prestige(ServerPlayer player, Identifier categoryId) {
        var catOpt = SkillsAPI.getCategory(categoryId);
        if (catOpt.isEmpty()) return Result.UNKNOWN_CATEGORY;
        var cat = catOpt.get();

        UUID uuid = player.getUUID();
        int current = getPrestigeLevel(uuid, categoryId);
        if (current >= MAX_PRESTIGE) return Result.MAX_REACHED;

        int spent = cat.getSpentPoints(player);
        if (spent < MIN_SPENT_POINTS) return Result.INSUFFICIENT_POINTS;

        int cost = getPrestigeCost(uuid, categoryId);
        ServerLevel level = (ServerLevel) player.level();
        EconomyManager eco = EconomyManager.get(level);
        int wallet = eco.getWallet(uuid) + eco.getBank(uuid);
        if (wallet < cost) return Result.INSUFFICIENT_FUNDS;
        // Charge wallet first, then bank.
        int wal = eco.getWallet(uuid);
        if (wal >= cost) {
            eco.setWallet(uuid, wal - cost);
        } else {
            eco.setWallet(uuid, 0);
            eco.setBank(uuid, eco.getBank(uuid) - (cost - wal));
        }

        cat.resetSkills(player);
        prestigeLevels.computeIfAbsent(uuid, k -> new LinkedHashMap<>()).put(categoryId, current + 1);
        markDirty();
        reapplyModifiers(player);
        return Result.SUCCESS;
    }

    /** Admin-only hard set (no cost, no validation). Use from admin panel. */
    public void setPrestige(ServerPlayer player, Identifier categoryId, int level) {
        level = Math.max(0, Math.min(MAX_PRESTIGE, level));
        prestigeLevels.computeIfAbsent(player.getUUID(), k -> new LinkedHashMap<>()).put(categoryId, level);
        markDirty();
        reapplyModifiers(player);
    }

    /** Legacy adapter used by /cosm_prestige_up etc. — does NOT run the cost/reset flow. */
    public boolean prestige(UUID uuid, SkillTreeType tree) {
        Identifier categoryId = legacyToCategory(tree);
        int current = getPrestigeLevel(uuid, categoryId);
        if (current >= MAX_PRESTIGE) return false;
        prestigeLevels.computeIfAbsent(uuid, k -> new LinkedHashMap<>()).put(categoryId, current + 1);
        markDirty();
        return true;
    }

    public boolean decrementPrestige(UUID uuid, SkillTreeType tree) {
        Identifier categoryId = legacyToCategory(tree);
        int current = getPrestigeLevel(uuid, categoryId);
        if (current <= 0) return false;
        prestigeLevels.computeIfAbsent(uuid, k -> new LinkedHashMap<>()).put(categoryId, current - 1);
        markDirty();
        return true;
    }

    // --- Attribute modifier application --------------------------------

    /** Clears both prestige modifiers and reapplies them sized to the player's current levels. */
    public void reapplyModifiers(ServerPlayer player) {
        UUID uuid = player.getUUID();
        applyCategoryModifier(player, CLASS_ATTRIBUTE_ID, CLASS_MODIFIER_ID, getPrestigeLevel(uuid, CLASS_CATEGORY));
        applyCategoryModifier(player, WEAPON_ATTRIBUTE_ID, WEAPON_MODIFIER_ID, getPrestigeLevel(uuid, WEAPON_CATEGORY));
    }

    private void applyCategoryModifier(ServerPlayer player, Identifier attributeId, Identifier modifierId, int level) {
        var attrHolder = BuiltInRegistries.ATTRIBUTE.get(attributeId);
        if (attrHolder.isEmpty()) return;
        Holder<Attribute> holder = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attrHolder.get().value());
        AttributeInstance inst = player.getAttribute(holder);
        if (inst == null) return;
        // Always remove the old modifier before applying a new one — AttributeInstance throws
        // if the same id is registered twice.
        inst.removeModifier(modifierId);
        if (level > 0) {
            inst.addPermanentModifier(new AttributeModifier(
                modifierId,
                level * BONUS_PER_PRESTIGE,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
    }

    // --- Legacy ⇄ category mapping -------------------------------------

    public static Identifier legacyToCategory(SkillTreeType tree) {
        return tree == SkillTreeType.COMBAT ? WEAPON_CATEGORY : CLASS_CATEGORY;
    }

    // --- Persistence ---------------------------------------------------

    private void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (!dataFile.exists()) return;
            CompoundTag root = NbtIo.readCompressed(dataFile.toPath(), NbtAccounter.unlimitedHeap());
            int schema = root.getIntOr("schema", 1);
            CompoundTag players = root.getCompoundOrEmpty("players");
            for (String key : players.keySet()) {
                UUID uuid = UUID.fromString(key);
                CompoundTag pTag = players.getCompoundOrEmpty(key);
                Map<Identifier, Integer> levels = new LinkedHashMap<>();
                if (schema >= 2) {
                    // New schema: keys are full category IDs ("skill_tree_rpgs:class_skills").
                    for (String k : pTag.keySet()) {
                        try {
                            Identifier id = Identifier.parse(k);
                            levels.put(id, pTag.getIntOr(k, 0));
                        } catch (Exception ignored) { }
                    }
                } else {
                    // Legacy schema: enum names (combat/mining/farming/arcane/survival) — fold
                    // combat → weapon_skills, everything else → class_skills (max wins).
                    int cls = 0;
                    int wep = 0;
                    for (SkillTreeType t : SkillTreeType.values()) {
                        int v = pTag.getIntOr(t.name().toLowerCase(), 0);
                        if (t == SkillTreeType.COMBAT) wep = Math.max(wep, v);
                        else cls = Math.max(cls, v);
                    }
                    if (cls > 0) levels.put(CLASS_CATEGORY, cls);
                    if (wep > 0) levels.put(WEAPON_CATEGORY, wep);
                    if (cls > 0 || wep > 0) markDirty(); // trigger re-save in new schema
                }
                if (!levels.isEmpty()) prestigeLevels.put(uuid, levels);
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load prestige data", e);
        }
    }

    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) dataDir.mkdirs();
            File dataFile = new File(dataDir, FILE_NAME);
            CompoundTag root = new CompoundTag();
            root.putInt("schema", SCHEMA_VERSION);
            CompoundTag players = new CompoundTag();
            for (var entry : prestigeLevels.entrySet()) {
                CompoundTag pTag = new CompoundTag();
                for (var kv : entry.getValue().entrySet()) {
                    pTag.putInt(kv.getKey().toString(), kv.getValue());
                }
                players.put(entry.getKey().toString(), pTag);
            }
            root.put("players", players);
            NbtIo.writeCompressed(root, dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save prestige data", e);
        }
    }

    public Set<UUID> knownPlayers() {
        return prestigeLevels.keySet();
    }

    /** Suppress the categoryFor helper — forwarder kept for external callers already using it. */
    public static Identifier categoryFor(String legacyOrId) {
        return SkillAdminBridge.categoryFor(legacyOrId);
    }
}
