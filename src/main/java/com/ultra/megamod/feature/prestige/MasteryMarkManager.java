package com.ultra.megamod.feature.prestige;

import com.ultra.megamod.MegaMod;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Cross-system prestige currency: "Marks of Mastery"
 * Awarded for major milestones across all systems.
 * Spent on cosmetic titles, exclusive items, and permanent minor boosts.
 */
public class MasteryMarkManager {
    private static MasteryMarkManager INSTANCE;
    private static final String FILE_NAME = "megamod_mastery_marks.dat";
    private final Map<UUID, Integer> marks = new HashMap<>();
    private final Map<UUID, java.util.Set<String>> claimedMilestones = new HashMap<>();
    private boolean dirty = false;

    public static MasteryMarkManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new MasteryMarkManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    public int getMarks(UUID playerId) {
        return marks.getOrDefault(playerId, 0);
    }

    public void addMarks(UUID playerId, int amount) {
        marks.put(playerId, getMarks(playerId) + amount);
        dirty = true;
    }

    public boolean spendMarks(UUID playerId, int amount) {
        int current = getMarks(playerId);
        if (current < amount) return false;
        marks.put(playerId, current - amount);
        dirty = true;
        return true;
    }

    /**
     * Award marks for a milestone if not already claimed.
     * Returns true if marks were awarded (first time).
     */
    public boolean awardMilestone(ServerPlayer player, String milestoneId, int markAmount, String description) {
        UUID uuid = player.getUUID();
        java.util.Set<String> claimed = claimedMilestones.computeIfAbsent(uuid, k -> new java.util.HashSet<>());
        if (claimed.contains(milestoneId)) return false;
        claimed.add(milestoneId);
        addMarks(uuid, markAmount);
        player.sendSystemMessage(Component.literal("+" + markAmount + " Marks of Mastery").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("  " + description).withStyle(ChatFormatting.GRAY));
        dirty = true;
        return true;
    }

    public boolean hasClaimed(UUID playerId, String milestoneId) {
        java.util.Set<String> claimed = claimedMilestones.get(playerId);
        return claimed != null && claimed.contains(milestoneId);
    }

    // ── Milestone Definitions ──

    /** Dungeon tier clear milestones. */
    public static final int DUNGEON_NORMAL_MARKS = 5;
    public static final int DUNGEON_HARD_MARKS = 10;
    public static final int DUNGEON_NIGHTMARE_MARKS = 20;
    public static final int DUNGEON_INFERNAL_MARKS = 40;

    /** Skill prestige milestones. */
    public static final int SKILL_PRESTIGE_MARKS = 25;

    /** Museum wing completion milestones. */
    public static final int MUSEUM_WING_MARKS = 15;
    public static final int MUSEUM_COMPLETE_MARKS = 50;

    /** Bounty milestones. */
    public static final int BOUNTY_50_MARKS = 10;
    public static final int BOUNTY_100_MARKS = 20;

    /** Colony milestones. */
    public static final int COLONY_50_CITIZENS_MARKS = 15;
    public static final int SIEGE_WIN_MARKS = 10;

    // ── Prestige Shop Items ──

    public static final int COST_TITLE = 50;
    public static final int COST_BONUS_COINS = 100; // permanent +5% coin drops
    public static final int COST_BONUS_XP = 100;    // permanent +5% XP gain
    public static final int COST_EXCLUSIVE_FURNITURE = 30;

    // Persistence
    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        dirty = false;
        try {
            Path dir = level.getServer().getWorldPath(LevelResource.ROOT).resolve("data");
            dir.toFile().mkdirs();
            CompoundTag root = new CompoundTag();
            for (var entry : marks.entrySet()) {
                CompoundTag playerTag = new CompoundTag();
                playerTag.putInt("marks", entry.getValue());
                java.util.Set<String> claimed = claimedMilestones.getOrDefault(entry.getKey(), java.util.Set.of());
                StringBuilder sb = new StringBuilder();
                for (String m : claimed) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(m);
                }
                playerTag.putString("claimed", sb.toString());
                root.put(entry.getKey().toString(), playerTag);
            }
            NbtIo.writeCompressed(root, dir.resolve(FILE_NAME));
        } catch (Exception e) {
            MegaMod.LOGGER.warn("Failed to save mastery marks: {}", e.getMessage());
        }
    }

    public void loadFromDisk(ServerLevel level) {
        try {
            Path file = level.getServer().getWorldPath(LevelResource.ROOT).resolve("data").resolve(FILE_NAME);
            if (!file.toFile().exists()) return;
            CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
            for (String key : root.keySet()) {
                try {
                    UUID id = UUID.fromString(key);
                    CompoundTag playerTag = root.getCompoundOrEmpty(key);
                    marks.put(id, playerTag.getIntOr("marks", 0));
                    String claimedStr = playerTag.getStringOr("claimed", "");
                    if (!claimedStr.isEmpty()) {
                        java.util.Set<String> set = new java.util.HashSet<>();
                        for (String m : claimedStr.split(",")) {
                            if (!m.isEmpty()) set.add(m);
                        }
                        claimedMilestones.put(id, set);
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            MegaMod.LOGGER.warn("Failed to load mastery marks: {}", e.getMessage());
        }
    }
}
