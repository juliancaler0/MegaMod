package com.ultra.megamod.feature.corruption;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.corruption.CorruptionManager.CorruptionZone;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class PurgeManager {
    private static PurgeManager INSTANCE;
    private static final String FILE_NAME = "megamod_purge.dat";
    private boolean dirty = false;

    private PurgeEvent activePurge;

    // Tick tracking for wave spawning during purge
    private long lastWaveSpawnTick = 0;
    private int waveCount = 0;
    private static final int TOTAL_WAVES = 3;
    private static final long WAVE_SPAWN_INTERVAL = 2000; // spawn a wave every ~100 seconds

    private static final Random RANDOM = new Random();

    public static class PurgeEvent {
        public int purgeId;
        public int targetZoneId;
        public UUID initiatorUuid;
        public String initiatorFaction; // optional, may be empty
        public long startTick;
        public int duration;         // ticks (default 6000 = 5 minutes)
        public int killsRequired;    // mobs to kill to clear the zone (10 * tier * tier)
        public int currentKills;
        public boolean completed;
        public List<UUID> participants;

        public PurgeEvent() {
            participants = new ArrayList<>();
        }

        public PurgeEvent(int purgeId, int targetZoneId, UUID initiatorUuid, String initiatorFaction,
                          long startTick, int duration, int killsRequired) {
            this.purgeId = purgeId;
            this.targetZoneId = targetZoneId;
            this.initiatorUuid = initiatorUuid;
            this.initiatorFaction = initiatorFaction != null ? initiatorFaction : "";
            this.startTick = startTick;
            this.duration = duration;
            this.killsRequired = killsRequired;
            this.currentKills = 0;
            this.completed = false;
            this.participants = new ArrayList<>();
            this.participants.add(initiatorUuid);
        }

        public long getTimeRemainingTicks(long currentTick) {
            long elapsed = currentTick - startTick;
            return Math.max(0, duration - elapsed);
        }

        public boolean isExpired(long currentTick) {
            return currentTick - startTick >= duration;
        }

        public boolean isKillsComplete() {
            return currentKills >= killsRequired;
        }

        public void addParticipant(UUID uuid) {
            if (!participants.contains(uuid)) {
                participants.add(uuid);
            }
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("purgeId", purgeId);
            tag.putInt("targetZoneId", targetZoneId);
            tag.putString("initiatorUuid", initiatorUuid.toString());
            tag.putString("initiatorFaction", initiatorFaction);
            tag.putLong("startTick", startTick);
            tag.putInt("duration", duration);
            tag.putInt("killsRequired", killsRequired);
            tag.putInt("currentKills", currentKills);
            tag.putBoolean("completed", completed);
            ListTag participantList = new ListTag();
            for (UUID uuid : participants) {
                participantList.add(StringTag.valueOf(uuid.toString()));
            }
            tag.put("participants", (Tag) participantList);
            return tag;
        }

        public static PurgeEvent load(CompoundTag tag) {
            PurgeEvent event = new PurgeEvent();
            event.purgeId = tag.getIntOr("purgeId", 0);
            event.targetZoneId = tag.getIntOr("targetZoneId", 0);
            try {
                event.initiatorUuid = UUID.fromString(tag.getStringOr("initiatorUuid", new UUID(0, 0).toString()));
            } catch (Exception e) {
                event.initiatorUuid = new UUID(0, 0);
            }
            event.initiatorFaction = tag.getStringOr("initiatorFaction", "");
            event.startTick = tag.getLongOr("startTick", 0L);
            event.duration = tag.getIntOr("duration", 6000);
            event.killsRequired = tag.getIntOr("killsRequired", 20);
            event.currentKills = tag.getIntOr("currentKills", 0);
            event.completed = tag.getBooleanOr("completed", false);
            event.participants = new ArrayList<>();
            ListTag participantList = tag.getListOrEmpty("participants");
            for (int i = 0; i < participantList.size(); i++) {
                try {
                    event.participants.add(UUID.fromString(participantList.getStringOr(i, "")));
                } catch (Exception ignored) {}
            }
            return event;
        }
    }

    // ---- Singleton Access ----

    public static PurgeManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new PurgeManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    private int nextPurgeId = 1;

    // ---- Purge Lifecycle ----

    /**
     * Start a purge event for the given corruption zone.
     * Player must be within 64 blocks of the zone.
     */
    public PurgeEvent startPurge(int zoneId, ServerPlayer initiator) {
        if (activePurge != null && !activePurge.completed) return null;

        ServerLevel level = initiator.level();
        CorruptionManager cm = CorruptionManager.get(level);
        CorruptionZone zone = cm.getZone(zoneId);
        if (zone == null || !zone.active) return null;

        int killsRequired = 10 * zone.tier * zone.tier;
        int duration = 6000; // 5 minutes
        String factionId = ""; // Could be enriched from FactionManager if needed

        activePurge = new PurgeEvent(nextPurgeId++, zoneId, initiator.getUUID(), factionId,
                level.getServer().getTickCount(), duration, killsRequired);
        lastWaveSpawnTick = level.getServer().getTickCount();
        waveCount = 0;
        markDirty();

        // Broadcast purge start
        String playerName = initiator.getGameProfile().name();
        Component broadcast = Component.literal("[Purge] ")
                .withStyle(ChatFormatting.DARK_RED)
                .append(Component.literal(playerName + " has initiated a purge at Zone #" + zoneId + "! ")
                        .withStyle(ChatFormatting.RED))
                .append(Component.literal("Kill " + killsRequired + " corrupted mobs in 5 minutes!")
                        .withStyle(ChatFormatting.GOLD));

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            player.sendSystemMessage(broadcast);
        }

        // Spawn the first wave immediately
        spawnPurgeWave(level, zone);
        waveCount++;

        MegaMod.LOGGER.info("Purge #{} started for zone #{} by {} - requires {} kills",
                activePurge.purgeId, zoneId, playerName, killsRequired);
        return activePurge;
    }

    /**
     * Record a kill during an active purge. The killer's position must be in the purge zone.
     */
    public void recordKill(UUID killer, BlockPos pos, ServerLevel level) {
        if (activePurge == null || activePurge.completed) return;

        CorruptionManager cm = CorruptionManager.get(level);
        CorruptionZone zone = cm.getZone(activePurge.targetZoneId);
        if (zone == null) return;

        // Verify kill is in the zone
        if (!zone.containsBlock(pos.getX(), pos.getZ())) return;

        activePurge.currentKills++;
        activePurge.addParticipant(killer);
        markDirty();

        // Check for completion
        if (activePurge.isKillsComplete()) {
            completePurge(level, true);
        }
    }

    /**
     * Stop an active purge (admin action).
     */
    public void stopPurge(ServerLevel level) {
        if (activePurge == null || activePurge.completed) return;
        activePurge.completed = true;

        Component broadcast = Component.literal("[Purge] ")
                .withStyle(ChatFormatting.DARK_RED)
                .append(Component.literal("The purge has been cancelled by an admin.")
                        .withStyle(ChatFormatting.YELLOW));

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            player.sendSystemMessage(broadcast);
        }

        activePurge = null;
        markDirty();
    }

    /**
     * Tick the purge manager. Handles wave spawning and timeout.
     */
    public void tick(ServerLevel level) {
        if (activePurge == null || activePurge.completed) return;

        long currentTick = level.getServer().getTickCount();

        // Check timeout
        if (activePurge.isExpired(currentTick)) {
            completePurge(level, false);
            return;
        }

        // Spawn waves at intervals (3 waves total, spaced WAVE_SPAWN_INTERVAL apart)
        if (waveCount < TOTAL_WAVES && currentTick - lastWaveSpawnTick >= WAVE_SPAWN_INTERVAL) {
            lastWaveSpawnTick = currentTick;
            CorruptionManager cm = CorruptionManager.get(level);
            CorruptionZone zone = cm.getZone(activePurge.targetZoneId);
            if (zone != null) {
                spawnPurgeWave(level, zone);
                waveCount++;

                // Notify about wave
                Component waveMsg = Component.literal("[Purge] ")
                        .withStyle(ChatFormatting.DARK_RED)
                        .append(Component.literal("Wave " + waveCount + "/" + TOTAL_WAVES + "! More corrupted mobs incoming!")
                                .withStyle(ChatFormatting.RED));
                for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                    player.sendSystemMessage(waveMsg);
                }
            }
        }
    }

    /**
     * Complete the purge -- success or failure.
     */
    private void completePurge(ServerLevel level, boolean success) {
        if (activePurge == null) return;

        CorruptionManager cm = CorruptionManager.get(level);
        CorruptionZone zone = cm.getZone(activePurge.targetZoneId);

        if (success) {
            // SUCCESS: deactivate zone, shrink radius to 0 over time (immediate for simplicity), then remove
            if (zone != null && zone.active) {
                zone.active = false;
                cm.removeZone(zone.zoneId);
            }
            cm.incrementPurgesCompleted();

            // Rewards: tier * 100 MegaCoins per participant + bonus XP
            int tier = zone != null ? zone.tier : 1;
            int reward = tier * 100;
            EconomyManager eco = EconomyManager.get(level);

            Component successMsg = Component.literal("[Purge] ")
                    .withStyle(ChatFormatting.DARK_RED)
                    .append(Component.literal("PURGE SUCCESSFUL! ")
                            .withStyle(ChatFormatting.GREEN))
                    .append(Component.literal("+" + reward + " MegaCoins for all participants!")
                            .withStyle(ChatFormatting.GOLD));

            for (UUID participantUuid : activePurge.participants) {
                eco.addWallet(participantUuid, reward);
                eco.addAuditEntry("Purge System", "PURGE_REWARD", reward,
                        "Purge zone #" + activePurge.targetZoneId + " reward");
                ServerPlayer participant = level.getServer().getPlayerList().getPlayer(participantUuid);
                if (participant != null) {
                    participant.sendSystemMessage(successMsg);
                    // Grant XP for combat skill tree
                    participant.giveExperiencePoints(200 * tier);
                }
            }

            // Drop corruption shards for all online participants
            for (UUID participantUuid : activePurge.participants) {
                ServerPlayer participant = level.getServer().getPlayerList().getPlayer(participantUuid);
                if (participant != null) {
                    int shardCount = 2 + RANDOM.nextInt(4); // 2-5 shards
                    net.minecraft.world.item.ItemStack shards = new net.minecraft.world.item.ItemStack(
                            CorruptionRegistry.CORRUPTION_SHARD.get(), shardCount);
                    participant.spawnAtLocation((ServerLevel) participant.level(), shards);
                }
            }

            MegaMod.LOGGER.info("Purge #{} succeeded for zone #{}: {}/{} kills by {} participants",
                    activePurge.purgeId, activePurge.targetZoneId,
                    activePurge.currentKills, activePurge.killsRequired, activePurge.participants.size());

        } else {
            // FAILURE: zone gets +1 tier (max 4), spawns extra mobs
            if (zone != null && zone.active) {
                int newTier = Math.min(4, zone.tier + 1);
                cm.setZoneTier(zone.zoneId, newTier);
                // Also expand radius a bit
                zone.radius = Math.min(zone.maxRadius, zone.radius + 8);
            }

            cm.incrementPurgesFailed();

            Component failMsg = Component.literal("[Purge] ")
                    .withStyle(ChatFormatting.DARK_RED)
                    .append(Component.literal("PURGE FAILED! ")
                            .withStyle(ChatFormatting.RED))
                    .append(Component.literal("The corruption grows stronger... (" + activePurge.currentKills + "/" + activePurge.killsRequired + ")")
                            .withStyle(ChatFormatting.DARK_PURPLE));

            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                player.sendSystemMessage(failMsg);
            }

            MegaMod.LOGGER.info("Purge #{} failed for zone #{}: {}/{} kills",
                    activePurge.purgeId, activePurge.targetZoneId,
                    activePurge.currentKills, activePurge.killsRequired);
        }

        activePurge.completed = true;
        activePurge = null;
        markDirty();
    }

    /**
     * Spawn a wave of corrupted mobs in the purge zone.
     */
    private void spawnPurgeWave(ServerLevel level, CorruptionZone zone) {
        int tier = zone.tier;
        int totalMobs = activePurge.killsRequired / TOTAL_WAVES;
        totalMobs = Math.max(3, totalMobs);

        int centerX = (int) zone.centerX;
        int centerZ = (int) zone.centerZ;
        int spawnRadius = Math.max(16, zone.radius / 2);

        for (int i = 0; i < totalMobs; i++) {
            int offsetX = RANDOM.nextInt(spawnRadius * 2 + 1) - spawnRadius;
            int offsetZ = RANDOM.nextInt(spawnRadius * 2 + 1) - spawnRadius;
            int spawnX = centerX + offsetX;
            int spawnZ = centerZ + offsetZ;
            int spawnY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnX, spawnZ);

            EntityType<?> mobType = getRandomMobType(tier);

            try {
                Mob mob = (Mob) mobType.create(level, EntitySpawnReason.MOB_SUMMONED);
                if (mob != null) {
                    mob.setPos(spawnX + 0.5, spawnY, spawnZ + 0.5);

                    // Scale stats by tier (+25% HP, +15% damage per tier)
                    if (mob.getAttribute(Attributes.MAX_HEALTH) != null) {
                        double baseHP = mob.getAttribute(Attributes.MAX_HEALTH).getBaseValue();
                        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseHP * (1.0 + 0.25 * tier));
                        mob.setHealth(mob.getMaxHealth());
                    }
                    if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
                        double baseDmg = mob.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue();
                        mob.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(baseDmg * (1.0 + 0.15 * tier));
                    }

                    // Purple glow effect
                    mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, 0, true, false));

                    // Tags
                    mob.addTag("megamod_corrupted");
                    mob.addTag("megamod_purge_mob");
                    mob.setPersistenceRequired();

                    level.addFreshEntity(mob);
                }
            } catch (Exception ignored) {}
        }
    }

    /**
     * Get a random mob type appropriate for the tier.
     */
    private static EntityType<?> getRandomMobType(int tier) {
        List<EntityType<?>> types = new ArrayList<>();
        // Tier 1: Zombies, Skeletons (2-4 mobs)
        types.add(EntityType.ZOMBIE);
        types.add(EntityType.SKELETON);

        if (tier >= 2) {
            // Tier 2: + Spiders, Creepers
            types.add(EntityType.SPIDER);
            types.add(EntityType.CREEPER);
        }
        if (tier >= 3) {
            // Tier 3: + Witches, Phantoms, Strays
            types.add(EntityType.WITCH);
            types.add(EntityType.PHANTOM);
            types.add(EntityType.STRAY);
        }
        if (tier >= 4) {
            // Tier 4: + Wither Skeletons, Evokers, Ravagers
            types.add(EntityType.WITHER_SKELETON);
            types.add(EntityType.EVOKER);
            types.add(EntityType.RAVAGER);
        }

        return types.get(RANDOM.nextInt(types.size()));
    }

    // ---- Queries ----

    public PurgeEvent getActivePurge() {
        return activePurge;
    }

    public boolean hasPurgeActive() {
        return activePurge != null && !activePurge.completed;
    }

    public List<PurgeEvent> getActivePurges() {
        List<PurgeEvent> list = new ArrayList<>();
        if (activePurge != null && !activePurge.completed) {
            list.add(activePurge);
        }
        return list;
    }

    public PurgeEvent getPurgeForZone(int zoneId) {
        if (activePurge != null && !activePurge.completed && activePurge.targetZoneId == zoneId) {
            return activePurge;
        }
        return null;
    }

    /**
     * Remove a participant from the active purge (disconnect cleanup).
     */
    public void removeParticipant(UUID uuid) {
        if (activePurge != null && !activePurge.completed) {
            activePurge.participants.remove(uuid);
        }
    }

    // ---- Persistence ----

    private void markDirty() { dirty = true; }

    public boolean isDirty() { return dirty; }

    private void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());
                nextPurgeId = root.getIntOr("nextPurgeId", 1);
                if (root.getBooleanOr("hasPurge", false)) {
                    CompoundTag purgeTag = root.getCompoundOrEmpty("purge");
                    PurgeEvent event = PurgeEvent.load(purgeTag);
                    if (!event.completed) {
                        activePurge = event;
                    }
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load purge data", e);
        }
    }

    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) dataDir.mkdirs();
            File dataFile = new File(dataDir, FILE_NAME);
            CompoundTag root = new CompoundTag();
            root.putInt("nextPurgeId", nextPurgeId);
            if (activePurge != null && !activePurge.completed) {
                root.putBoolean("hasPurge", true);
                root.put("purge", (Tag) activePurge.save());
            } else {
                root.putBoolean("hasPurge", false);
            }
            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save purge data", e);
        }
    }
}
