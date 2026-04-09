package com.ultra.megamod.feature.arena;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.dimensions.DimensionHelper;
import com.ultra.megamod.feature.dimensions.MegaModDimensions;
import com.ultra.megamod.feature.dimensions.PocketManager;
import com.ultra.megamod.feature.economy.EconomyManager;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

public class ArenaManager {
    private static ArenaManager INSTANCE;
    private static final String FILE_NAME = "megamod_arena.dat";

    public enum ArenaMode { PVE, PVP, BOSS_RUSH }
    public enum ArenaState { WAITING, ACTIVE, COMPLETED, FAILED }

    public enum ChallengeMode {
        STANDARD_5(5, "5 Rounds", 75),
        STANDARD_10(10, "10 Rounds", 200),
        STANDARD_15(15, "15 Rounds", 400),
        STANDARD_20(20, "20 Rounds", 750),
        ENDLESS(0, "Endless", 0),
        NO_ARMOR(10, "No Armor", 500),
        NO_DAMAGE(10, "No Damage", 5000);

        public final int targetWaves;
        public final String displayName;
        public final int baseReward;

        ChallengeMode(int targetWaves, String displayName, int baseReward) {
            this.targetWaves = targetWaves;
            this.displayName = displayName;
            this.baseReward = baseReward;
        }

        public static ChallengeMode fromString(String name) {
            try { return valueOf(name); } catch (Exception e) { return STANDARD_5; }
        }
    }

    public static class ArenaInstance {
        public String instanceId;
        public ArenaMode mode;
        public List<UUID> players = new ArrayList<>();
        public int wave;
        public ArenaState state;
        public BlockPos origin;
        public long startTime;
        public int mobsAlive;
        // PvP specific
        public int[] roundScores = new int[2]; // scores for player 0 and player 1
        public int currentRound = 1;
        public long roundStartTime;
        // Boss Rush specific
        public int currentBossIndex = 0;
        public long intermissionEnd = 0;
        // PvE checkpoint — paused between waves waiting for player decision (Endless only)
        public boolean atCheckpoint = false;
        // Challenge system
        public ChallengeMode challengeMode = ChallengeMode.STANDARD_5;
        public boolean tookDamage = false; // tracked for NO_DAMAGE challenge

        public ArenaInstance(String instanceId, ArenaMode mode, BlockPos origin) {
            this.instanceId = instanceId;
            this.mode = mode;
            this.origin = origin;
            this.wave = 0;
            this.state = ArenaState.WAITING;
            this.startTime = System.currentTimeMillis();
            this.mobsAlive = 0;
            this.roundStartTime = System.currentTimeMillis();
        }
    }

    public static class ArenaStats {
        public int bestPveWave = 0;
        public int totalPveRuns = 0;
        public int pvpWins = 0;
        public int pvpLosses = 0;
        public int eloRating = 1000;
        public long bestBossRushTime = 0; // 0 = never completed
        public List<Integer> recentPveWaves = new ArrayList<>(); // last 5 runs
        // Challenge completion tracking
        public boolean completed5Rounds = false;
        public boolean completed10Rounds = false;
        public boolean completed15Rounds = false;
        public boolean completed20Rounds = false;
        public boolean completedNoArmor = false;
        public boolean completedNoDamage = false;
    }

    private final Map<String, ArenaInstance> activeInstances = new HashMap<>();
    private final Map<UUID, ArenaStats> playerStats = new HashMap<>();
    private final Map<UUID, String> playerToInstance = new HashMap<>();
    private int nextInstanceId = 1;
    private boolean dirty = false;
    private boolean loaded = false;

    public static ArenaManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new ArenaManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    private void markDirty() {
        this.dirty = true;
    }

    public ArenaStats getOrCreateStats(UUID playerId) {
        return playerStats.computeIfAbsent(playerId, k -> new ArenaStats());
    }

    // Convenience accessors for admin panel
    public int getEloRating(UUID id) { return getOrCreateStats(id).eloRating; }
    public int getPvpWins(UUID id) { return getOrCreateStats(id).pvpWins; }
    public int getPvpLosses(UUID id) { return getOrCreateStats(id).pvpLosses; }
    public int getBestPveWave(UUID id) { return getOrCreateStats(id).bestPveWave; }
    public long getBestBossRushTime(UUID id) { return getOrCreateStats(id).bestBossRushTime; }
    public int getActiveArenaCount() { return activeInstances.size(); }

    public ArenaInstance getInstanceForPlayer(UUID playerId) {
        String instanceId = playerToInstance.get(playerId);
        if (instanceId == null) return null;
        return activeInstances.get(instanceId);
    }

    public ArenaInstance getInstance(String instanceId) {
        return activeInstances.get(instanceId);
    }

    public Map<String, ArenaInstance> getActiveInstances() {
        return activeInstances;
    }

    public boolean isInArena(UUID playerId) {
        return playerToInstance.containsKey(playerId);
    }

    /**
     * Check if a player has unlocked a given challenge mode.
     */
    public boolean isChallengeUnlocked(UUID playerId, ChallengeMode mode) {
        ArenaStats stats = getOrCreateStats(playerId);
        return switch (mode) {
            case STANDARD_5 -> true; // always available
            case STANDARD_10 -> stats.completed5Rounds;
            case STANDARD_15 -> stats.completed10Rounds;
            case STANDARD_20 -> stats.completed15Rounds;
            case ENDLESS -> stats.completed20Rounds;
            case NO_ARMOR -> stats.completed10Rounds;
            case NO_DAMAGE -> stats.completedNoArmor;
        };
    }

    /**
     * Mark a challenge as completed for a player.
     */
    private void markChallengeCompleted(UUID playerId, ChallengeMode mode) {
        ArenaStats stats = getOrCreateStats(playerId);
        switch (mode) {
            case STANDARD_5 -> stats.completed5Rounds = true;
            case STANDARD_10 -> stats.completed10Rounds = true;
            case STANDARD_15 -> stats.completed15Rounds = true;
            case STANDARD_20 -> stats.completed20Rounds = true;
            case NO_ARMOR -> stats.completedNoArmor = true;
            case NO_DAMAGE -> stats.completedNoDamage = true;
            case ENDLESS -> {} // no completion flag for endless
        }
    }

    /**
     * Create a PvE arena for a solo player with a specific challenge mode.
     */
    public String createPveArena(ServerPlayer player, ServerLevel overworld) {
        return createPveArena(player, overworld, ChallengeMode.STANDARD_5);
    }

    public String createPveArena(ServerPlayer player, ServerLevel overworld, ChallengeMode challengeMode) {
        if (isInArena(player.getUUID())) return null;
        if (!isChallengeUnlocked(player.getUUID(), challengeMode)) return null;

        String id = "arena_pve_" + nextInstanceId++;
        BlockPos origin = PocketManager.get(overworld).allocateDungeonPocket("arena_" + id);

        ServerLevel pocketLevel = player.level().getServer().getLevel(MegaModDimensions.DUNGEON);
        if (pocketLevel == null) {
            MegaMod.LOGGER.warn("Arena: pocket dimension not found");
            return null;
        }

        ArenaBuilder.buildPveArena(pocketLevel, origin);

        ArenaInstance instance = new ArenaInstance(id, ArenaMode.PVE, origin);
        instance.challengeMode = challengeMode;
        instance.players.add(player.getUUID());
        instance.state = ArenaState.ACTIVE;
        instance.wave = 1;
        activeInstances.put(id, instance);
        playerToInstance.put(player.getUUID(), id);

        // Teleport player to center of arena (colosseum center = SIZE/2)
        int arenaCenter = 26; // ArenaBuilder.SIZE / 2
        BlockPos spawnPos = origin.offset(arenaCenter, 1, arenaCenter);
        DimensionHelper.teleportToDimension(player, MegaModDimensions.DUNGEON, spawnPos, "arena");

        // No Armor challenge: strip armor on entry
        if (challengeMode == ChallengeMode.NO_ARMOR) {
            net.minecraft.world.entity.EquipmentSlot[] armorSlots = {
                net.minecraft.world.entity.EquipmentSlot.HEAD,
                net.minecraft.world.entity.EquipmentSlot.CHEST,
                net.minecraft.world.entity.EquipmentSlot.LEGS,
                net.minecraft.world.entity.EquipmentSlot.FEET
            };
            for (net.minecraft.world.entity.EquipmentSlot slot : armorSlots) {
                net.minecraft.world.item.ItemStack armorStack = player.getItemBySlot(slot);
                if (!armorStack.isEmpty()) {
                    if (!player.getInventory().add(armorStack.copy())) {
                        player.spawnAtLocation((ServerLevel) player.level(), armorStack.copy());
                    }
                    player.setItemSlot(slot, net.minecraft.world.item.ItemStack.EMPTY);
                }
            }
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "NO ARMOR CHALLENGE! Your armor has been removed.").withStyle(net.minecraft.ChatFormatting.RED, net.minecraft.ChatFormatting.BOLD));
        }

        if (challengeMode == ChallengeMode.NO_DAMAGE) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "NO DAMAGE CHALLENGE! Taking ANY damage ends your run!").withStyle(net.minecraft.ChatFormatting.RED, net.minecraft.ChatFormatting.BOLD));
        }

        // Spawn first wave
        ArenaWaveSpawner.spawnWave(pocketLevel, instance);

        // Update stats
        ArenaStats stats = getOrCreateStats(player.getUUID());
        stats.totalPveRuns++;
        markDirty();

        MegaMod.LOGGER.info("Arena PvE {} created: {} for {}", challengeMode.displayName, id, player.getGameProfile().name());
        return id;
    }

    /**
     * Create a PvP arena for two players.
     */
    public String createPvpArena(ServerPlayer p1, ServerPlayer p2, ServerLevel overworld) {
        if (isInArena(p1.getUUID()) || isInArena(p2.getUUID())) return null;

        String id = "arena_pvp_" + nextInstanceId++;
        BlockPos origin = PocketManager.get(overworld).allocateDungeonPocket("arena_" + id);

        ServerLevel pocketLevel = p1.level().getServer().getLevel(MegaModDimensions.DUNGEON);
        if (pocketLevel == null) return null;

        ArenaBuilder.buildPvpArena(pocketLevel, origin);

        ArenaInstance instance = new ArenaInstance(id, ArenaMode.PVP, origin);
        instance.players.add(p1.getUUID());
        instance.players.add(p2.getUUID());
        instance.state = ArenaState.ACTIVE;
        instance.currentRound = 1;
        instance.roundStartTime = System.currentTimeMillis();
        activeInstances.put(id, instance);
        playerToInstance.put(p1.getUUID(), id);
        playerToInstance.put(p2.getUUID(), id);

        // Teleport players to opposite spawn pads
        BlockPos spawn1 = origin.offset(10, 1, 2);
        BlockPos spawn2 = origin.offset(10, 1, 18);
        DimensionHelper.teleportToDimension(p1, MegaModDimensions.DUNGEON, spawn1, "arena");
        DimensionHelper.teleportToDimension(p2, MegaModDimensions.DUNGEON, spawn2, "arena");

        markDirty();
        MegaMod.LOGGER.info("Arena PvP created: {} for {} vs {}", id,
                p1.getGameProfile().name(), p2.getGameProfile().name());
        return id;
    }

    /**
     * Create a Boss Rush arena for a solo player.
     */
    public String createBossRushArena(ServerPlayer player, ServerLevel overworld) {
        if (isInArena(player.getUUID())) return null;

        String id = "arena_boss_" + nextInstanceId++;
        BlockPos origin = PocketManager.get(overworld).allocateDungeonPocket("arena_" + id);

        ServerLevel pocketLevel = player.level().getServer().getLevel(MegaModDimensions.DUNGEON);
        if (pocketLevel == null) return null;

        ArenaBuilder.buildPveArena(pocketLevel, origin);

        ArenaInstance instance = new ArenaInstance(id, ArenaMode.BOSS_RUSH, origin);
        instance.players.add(player.getUUID());
        instance.state = ArenaState.ACTIVE;
        instance.currentBossIndex = 0;
        activeInstances.put(id, instance);
        playerToInstance.put(player.getUUID(), id);

        // Teleport player
        BlockPos spawnPos = origin.offset(15, 1, 15);
        DimensionHelper.teleportToDimension(player, MegaModDimensions.DUNGEON, spawnPos, "boss_rush");

        // Spawn first boss
        BossRushManager.spawnNextBoss(pocketLevel, instance);

        markDirty();
        MegaMod.LOGGER.info("Arena Boss Rush created: {} for {}", id, player.getGameProfile().name());
        return id;
    }

    /**
     * Called when all mobs in a PvE wave are dead. Advance to next wave.
     */
    public void completeWave(String instanceId, ServerLevel pocketLevel) {
        ArenaInstance instance = activeInstances.get(instanceId);
        if (instance == null || instance.mode != ArenaMode.PVE) return;

        instance.wave++;
        int completedWave = instance.wave - 1;

        // Update best wave for all players
        for (UUID playerId : instance.players) {
            ArenaStats stats = getOrCreateStats(playerId);
            if (completedWave > stats.bestPveWave) {
                stats.bestPveWave = completedWave;
            }
        }

        // Check if target waves reached for non-endless modes
        ChallengeMode cm = instance.challengeMode;
        if (cm.targetWaves > 0 && completedWave >= cm.targetWaves) {
            // Challenge completed! Mark completion and award bonus
            for (UUID playerId : instance.players) {
                markChallengeCompleted(playerId, cm);
                ServerPlayer player = pocketLevel.getServer().getPlayerList().getPlayer(playerId);
                if (player != null) {
                    String unlockMsg = getUnlockMessage(cm);
                    if (unlockMsg != null) {
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(unlockMsg)
                                .withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE, net.minecraft.ChatFormatting.BOLD));
                    }
                }
            }
            endArena(instanceId, true, pocketLevel);
            return;
        }

        // Endless mode: checkpoint every 5 waves
        if (cm == ChallengeMode.ENDLESS && completedWave > 0 && completedWave % 5 == 0) {
            instance.atCheckpoint = true;
            int reward = completedWave * 5;

            // Heal players and open checkpoint GUI
            for (UUID playerId : instance.players) {
                ServerPlayer player = pocketLevel.getServer().getPlayerList().getPlayer(playerId);
                if (player != null) {
                    player.setHealth(player.getMaxHealth());
                    player.getFoodData().setFoodLevel(20);

                    // Send checkpoint screen payload
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                            new com.ultra.megamod.feature.arena.network.ArenaCheckpointPayload(completedWave, reward));
                }
            }
            markDirty();
            return; // Don't spawn next wave until player decides
        }

        // Safety cap for endless
        if (cm == ChallengeMode.ENDLESS && instance.wave > 100) {
            endArena(instanceId, true, pocketLevel);
            return;
        }

        // Spawn next wave
        ArenaWaveSpawner.spawnWave(pocketLevel, instance);
        markDirty();
    }

    private static String getUnlockMessage(ChallengeMode completed) {
        return switch (completed) {
            case STANDARD_5 -> "10 Rounds unlocked!";
            case STANDARD_10 -> "15 Rounds, No Armor Challenge unlocked!";
            case STANDARD_15 -> "20 Rounds unlocked!";
            case STANDARD_20 -> "Endless Mode unlocked!";
            case NO_ARMOR -> "No Damage Challenge unlocked!";
            case NO_DAMAGE -> "Arena MASTERED! All challenges complete!";
            default -> null;
        };
    }

    /**
     * Continue from a checkpoint — spawn the next wave.
     */
    public void continueFromCheckpoint(String instanceId, ServerLevel pocketLevel) {
        ArenaInstance instance = activeInstances.get(instanceId);
        if (instance == null || !instance.atCheckpoint) return;

        instance.atCheckpoint = false;
        ArenaWaveSpawner.spawnWave(pocketLevel, instance);

        for (UUID playerId : instance.players) {
            ServerPlayer player = pocketLevel.getServer().getPlayerList().getPlayer(playerId);
            if (player != null) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "Wave " + instance.wave + " incoming!")
                        .withStyle(net.minecraft.ChatFormatting.GOLD));
            }
        }
        markDirty();
    }

    /**
     * Leave arena at checkpoint — exit with rewards earned so far.
     */
    public void leaveAtCheckpoint(String instanceId, ServerLevel pocketLevel) {
        ArenaInstance instance = activeInstances.get(instanceId);
        if (instance == null || !instance.atCheckpoint) return;

        instance.atCheckpoint = false;
        endArena(instanceId, true, pocketLevel);
    }

    /**
     * End an arena instance. Teleport players back and award coins.
     */
    public void endArena(String instanceId, boolean success, ServerLevel pocketLevel) {
        ArenaInstance instance = activeInstances.get(instanceId);
        if (instance == null) return;

        instance.state = success ? ArenaState.COMPLETED : ArenaState.FAILED;

        ServerLevel overworld = pocketLevel.getServer().overworld();
        EconomyManager eco = EconomyManager.get(overworld);

        for (UUID playerId : instance.players) {
            ServerPlayer player = pocketLevel.getServer().getPlayerList().getPlayer(playerId);
            if (player != null) {
                DimensionHelper.teleportBack(player);

                if (success && instance.mode == ArenaMode.PVE) {
                    ChallengeMode cm = instance.challengeMode;
                    int reward;
                    if (cm == ChallengeMode.ENDLESS) {
                        reward = (instance.wave - 1) * 10; // dynamic for endless
                    } else {
                        reward = cm.baseReward;
                    }
                    eco.addWallet(playerId, reward);
                    String label = cm == ChallengeMode.ENDLESS
                            ? "Endless Arena: Wave " + (instance.wave - 1) + " reached."
                            : cm.displayName + " Challenge complete!";
                    player.sendSystemMessage(
                            net.minecraft.network.chat.Component.literal(label + " +" + reward + " MC")
                                    .withStyle(net.minecraft.ChatFormatting.GREEN));
                } else if (instance.mode == ArenaMode.PVE) {
                    int reward = Math.max(5, (instance.wave - 1) * 8);
                    eco.addWallet(playerId, reward);
                    String failMsg = instance.challengeMode == ChallengeMode.NO_DAMAGE && instance.tookDamage
                            ? "No Damage failed! You took damage on wave " + instance.wave + "."
                            : "Arena over at wave " + instance.wave + ".";
                    player.sendSystemMessage(
                            net.minecraft.network.chat.Component.literal(failMsg + " +" + reward + " MC")
                                    .withStyle(net.minecraft.ChatFormatting.YELLOW));
                } else if (instance.mode == ArenaMode.BOSS_RUSH && success) {
                    int reward = 1000;
                    eco.addWallet(playerId, reward);
                    player.sendSystemMessage(
                            net.minecraft.network.chat.Component.literal("Boss Rush complete! +" + reward + " MC")
                                    .withStyle(net.minecraft.ChatFormatting.GREEN));
                }
            }

            // Record PvE wave history
            if (instance.mode == ArenaMode.PVE) {
                ArenaStats stats = getOrCreateStats(playerId);
                stats.recentPveWaves.add(instance.wave - 1);
                while (stats.recentPveWaves.size() > 5) {
                    stats.recentPveWaves.remove(0);
                }
            }

            // Notify quest system on successful arena completion
            if (success && instance.mode == ArenaMode.PVE) {
                try {
                    com.ultra.megamod.feature.quests.QuestEventListener.onArenaComplete(
                            playerId, overworld, false);
                } catch (Exception ignored) {}
            } else if (success && instance.mode == ArenaMode.BOSS_RUSH) {
                try {
                    com.ultra.megamod.feature.quests.QuestEventListener.onArenaComplete(
                            playerId, overworld, true);
                } catch (Exception ignored) {}
            }

            playerToInstance.remove(playerId);
            clearArenaHud(playerId);
        }

        // Free pocket
        PocketManager.get(overworld).freeDungeonPocket("arena_" + instanceId);
        activeInstances.remove(instanceId);
        markDirty();
        saveToDisk(overworld);

        MegaMod.LOGGER.info("Arena ended: {} success={}", instanceId, success);
    }

    /**
     * Remove a player from their arena on disconnect.
     */
    public void removePlayerOnDisconnect(UUID playerId, ServerLevel overworld) {
        String instanceId = playerToInstance.remove(playerId);
        if (instanceId == null) return;

        ArenaInstance instance = activeInstances.get(instanceId);
        if (instance == null) return;

        instance.players.remove(playerId);

        if (instance.players.isEmpty()) {
            // No players left, end the arena
            ServerLevel pocketLevel = overworld.getServer().getLevel(MegaModDimensions.DUNGEON);
            if (pocketLevel != null) {
                PocketManager.get(overworld).freeDungeonPocket("arena_" + instanceId);
            }
            activeInstances.remove(instanceId);
        }

        markDirty();
    }

    /**
     * Decrement mob count for an arena instance.
     */
    public void decrementMobCount(String instanceId) {
        ArenaInstance instance = activeInstances.get(instanceId);
        if (instance != null && instance.mobsAlive > 0) {
            instance.mobsAlive--;
            // Sync HUD to players
            syncArenaHud(instance);
        }
    }

    /** Send arena HUD sync to all players in an instance. */
    public void syncArenaHud(ArenaInstance instance) {
        net.minecraft.server.MinecraftServer server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        for (java.util.UUID pid : instance.players) {
            net.minecraft.server.level.ServerPlayer sp = server.getPlayerList().getPlayer(pid);
            if (sp != null) {
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp,
                        new com.ultra.megamod.feature.arena.network.ArenaHudSyncPayload(
                                instance.state == ArenaState.ACTIVE, instance.wave, instance.mobsAlive));
            }
        }
    }

    /** Clear arena HUD for players when they leave. */
    public void clearArenaHud(java.util.UUID playerId) {
        net.minecraft.server.MinecraftServer server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        net.minecraft.server.level.ServerPlayer sp = server.getPlayerList().getPlayer(playerId);
        if (sp != null) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp,
                    new com.ultra.megamod.feature.arena.network.ArenaHudSyncPayload(false, 0, 0));
        }
    }

    /**
     * Find which arena instance a position belongs to (by checking proximity to origin).
     */
    public ArenaInstance findInstanceAtPos(BlockPos pos) {
        for (ArenaInstance instance : activeInstances.values()) {
            if (instance.state != ArenaState.ACTIVE) continue;
            BlockPos o = instance.origin;
            // Colosseum is ~53x53, check within range
            if (Math.abs(pos.getX() - o.getX()) <= 60 &&
                Math.abs(pos.getZ() - o.getZ()) <= 60 &&
                Math.abs(pos.getY() - o.getY()) <= 20) {
                return instance;
            }
        }
        return null;
    }

    // --- Persistence ---

    public void saveToDisk(ServerLevel level) {
        if (!this.dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            File dataFile = new File(dataDir, FILE_NAME);

            CompoundTag root = new CompoundTag();
            root.putInt("nextInstanceId", nextInstanceId);

            // Save player stats
            CompoundTag statsTag = new CompoundTag();
            for (Map.Entry<UUID, ArenaStats> entry : playerStats.entrySet()) {
                CompoundTag st = new CompoundTag();
                ArenaStats s = entry.getValue();
                st.putInt("bestPveWave", s.bestPveWave);
                st.putInt("totalPveRuns", s.totalPveRuns);
                st.putInt("pvpWins", s.pvpWins);
                st.putInt("pvpLosses", s.pvpLosses);
                st.putInt("eloRating", s.eloRating);
                st.putLong("bestBossRushTime", s.bestBossRushTime);
                st.putBoolean("c5", s.completed5Rounds);
                st.putBoolean("c10", s.completed10Rounds);
                st.putBoolean("c15", s.completed15Rounds);
                st.putBoolean("c20", s.completed20Rounds);
                st.putBoolean("cNoArmor", s.completedNoArmor);
                st.putBoolean("cNoDamage", s.completedNoDamage);

                ListTag wavesTag = new ListTag();
                for (int w : s.recentPveWaves) {
                    CompoundTag wt = new CompoundTag();
                    wt.putInt("w", w);
                    wavesTag.add((Tag) wt);
                }
                st.put("recentPveWaves", (Tag) wavesTag);

                statsTag.put(entry.getKey().toString(), (Tag) st);
            }
            root.put("playerStats", (Tag) statsTag);

            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            this.dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save arena data", (Throwable) e);
        }
    }

    private void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());

                nextInstanceId = root.getIntOr("nextInstanceId", 1);

                CompoundTag statsTag = root.getCompoundOrEmpty("playerStats");
                for (String uuidStr : statsTag.keySet()) {
                    UUID uuid;
                    try {
                        uuid = UUID.fromString(uuidStr);
                    } catch (IllegalArgumentException e) {
                        continue;
                    }
                    CompoundTag st = statsTag.getCompoundOrEmpty(uuidStr);
                    ArenaStats s = new ArenaStats();
                    s.bestPveWave = st.getIntOr("bestPveWave", 0);
                    s.totalPveRuns = st.getIntOr("totalPveRuns", 0);
                    s.pvpWins = st.getIntOr("pvpWins", 0);
                    s.pvpLosses = st.getIntOr("pvpLosses", 0);
                    s.eloRating = st.getIntOr("eloRating", 1000);
                    s.bestBossRushTime = st.getLongOr("bestBossRushTime", 0L);
                    s.completed5Rounds = st.getBooleanOr("c5", false);
                    s.completed10Rounds = st.getBooleanOr("c10", false);
                    s.completed15Rounds = st.getBooleanOr("c15", false);
                    s.completed20Rounds = st.getBooleanOr("c20", false);
                    s.completedNoArmor = st.getBooleanOr("cNoArmor", false);
                    s.completedNoDamage = st.getBooleanOr("cNoDamage", false);

                    ListTag wavesTag = st.getListOrEmpty("recentPveWaves");
                    for (int i = 0; i < wavesTag.size(); i++) {
                        CompoundTag wt = wavesTag.getCompoundOrEmpty(i);
                        s.recentPveWaves.add(wt.getIntOr("w", 0));
                    }

                    playerStats.put(uuid, s);
                }
            }
            loaded = true;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load arena data", (Throwable) e);
        }
    }
}
