package com.ultra.megamod.feature.citizen.raid;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.Random;

/**
 * Represents a single raid event using the culture-based raider system.
 * Tracks raid state, spawns waves of culture-specific raiders, and
 * determines when the raid is complete.
 */
public class RaidEvent {

    public enum EventStatus {
        PREPARING,
        ACTIVE,
        FINISHED
    }

    private static final Random random = new Random();

    private final int raidId;
    private final RaiderCulture culture;
    private final BlockPos targetCenter;
    private final int totalRaiders;
    private int spawnedRaiders;
    private int killedRaiders;
    private EventStatus status;
    private final long startTick;
    private int currentWave;
    private final int maxWaves;
    private final int[] waveSizes;
    private int ticksInCurrentPhase;
    private final int difficulty;

    private static final int PREPARE_TICKS = 600;   // 30 seconds warning
    private static final int WAVE_TIMEOUT = 6000;    // 5 minutes per wave

    public RaidEvent(int raidId, RaiderCulture culture, BlockPos targetCenter,
                     int difficulty, int maxWaves, int[] waveSizes, long startTick) {
        this.raidId = raidId;
        this.culture = culture;
        this.targetCenter = targetCenter;
        this.difficulty = difficulty;
        this.maxWaves = maxWaves;
        this.waveSizes = waveSizes;
        this.startTick = startTick;
        this.status = EventStatus.PREPARING;
        this.currentWave = 0;
        this.spawnedRaiders = 0;
        this.killedRaiders = 0;
        this.ticksInCurrentPhase = 0;

        int total = 0;
        for (int ws : waveSizes) total += ws;
        this.totalRaiders = total;
    }

    /**
     * Tick this raid event. Returns true if the event has just finished.
     */
    public boolean tick(ServerLevel level) {
        ticksInCurrentPhase++;

        switch (status) {
            case PREPARING:
                if (ticksInCurrentPhase >= PREPARE_TICKS) {
                    status = EventStatus.ACTIVE;
                    currentWave = 1;
                    ticksInCurrentPhase = 0;
                    spawnWave(level);
                }
                break;

            case ACTIVE:
                // Check wave progress every 2 seconds
                if (ticksInCurrentPhase % 40 == 0) {
                    int alive = countAliveRaiders(level);
                    int spawnedThisWave = waveSizes[currentWave - 1];
                    int deadThisWave = spawnedThisWave - alive;
                    double killRatio = spawnedThisWave > 0 ? (double) deadThisWave / spawnedThisWave : 1.0;

                    // Wave cleared (80%+ killed)
                    if (killRatio >= 0.80) {
                        killedRaiders += deadThisWave;
                        cleanupStragglers(level);

                        if (currentWave < maxWaves) {
                            currentWave++;
                            ticksInCurrentPhase = 0;
                            spawnWave(level);
                        } else {
                            // All waves completed
                            status = EventStatus.FINISHED;
                            return true;
                        }
                    }

                    // All raiders from final wave dead
                    if (currentWave == maxWaves && alive == 0) {
                        killedRaiders += spawnedThisWave;
                        status = EventStatus.FINISHED;
                        return true;
                    }
                }

                // Wave timeout
                if (ticksInCurrentPhase >= WAVE_TIMEOUT) {
                    int alive = countAliveRaiders(level);
                    int spawnedThisWave = waveSizes[currentWave - 1];
                    killedRaiders += (spawnedThisWave - alive);
                    cleanupStragglers(level);
                    status = EventStatus.FINISHED;
                    return true;
                }
                break;

            case FINISHED:
                return true;
        }
        return false;
    }

    /**
     * Spawn the current wave of raiders around the target center.
     */
    public void spawnWave(ServerLevel level) {
        int count = waveSizes[currentWave - 1];
        for (int i = 0; i < count; i++) {
            int dx = random.nextInt(31) - 15;
            int dz = random.nextInt(31) - 15;
            BlockPos spawnPos = targetCenter.offset(dx, 0, dz);
            spawnPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnPos);

            EntityType<? extends Monster> entityType = pickRaiderType(i, count);
            Mob mob = (Mob) entityType.create(level, EntitySpawnReason.EVENT);
            if (mob == null) continue;

            mob.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

            if (mob instanceof AbstractRaiderEntity raider) {
                raider.setCulture(culture);
                raider.setRaidId(raidId);
                raider.setTargetPos(targetCenter);
                raider.applyCultureScaling();
            }

            mob.addTag("colony_raider");
            mob.addTag("raid_" + raidId);
            level.addFreshEntity(mob);
            spawnedRaiders++;
        }
    }

    /**
     * Pick a raider entity type based on position in the wave.
     * Last raider in the final wave is the chief/boss type.
     */
    private EntityType<? extends Monster> pickRaiderType(int index, int waveSize) {
        boolean isChief = (currentWave == maxWaves && index == waveSize - 1);
        boolean isArcher = !isChief && random.nextFloat() < 0.35f;

        if (isChief) {
            return getChiefType();
        } else if (isArcher) {
            return getArcherType();
        } else {
            return getMeleeType();
        }
    }

    private EntityType<? extends Monster> getMeleeType() {
        return switch (culture) {
            case BARBARIAN -> RaiderEntityRegistry.BARBARIAN.get();
            case PIRATE -> RaiderEntityRegistry.PIRATE.get();
            case EGYPTIAN -> RaiderEntityRegistry.MUMMY.get();
            case NORSEMEN -> RaiderEntityRegistry.SHIELDMAIDEN.get();
            case AMAZON -> RaiderEntityRegistry.AMAZON_SPEARMAN.get();
            case DROWNED_PIRATE -> RaiderEntityRegistry.DROWNED_PIRATE.get();
        };
    }

    private EntityType<? extends Monster> getArcherType() {
        return switch (culture) {
            case BARBARIAN -> RaiderEntityRegistry.ARCHER_BARBARIAN.get();
            case PIRATE -> RaiderEntityRegistry.ARCHER_PIRATE.get();
            case EGYPTIAN -> RaiderEntityRegistry.ARCHER_MUMMY.get();
            case NORSEMEN -> RaiderEntityRegistry.NORSEMEN_ARCHER.get();
            case AMAZON -> RaiderEntityRegistry.ARCHER_AMAZON.get();
            case DROWNED_PIRATE -> RaiderEntityRegistry.DROWNED_ARCHER_PIRATE.get();
        };
    }

    private EntityType<? extends Monster> getChiefType() {
        return switch (culture) {
            case BARBARIAN -> RaiderEntityRegistry.CHIEF_BARBARIAN.get();
            case PIRATE -> RaiderEntityRegistry.CAPTAIN_PIRATE.get();
            case EGYPTIAN -> RaiderEntityRegistry.PHARAO.get();
            case NORSEMEN -> RaiderEntityRegistry.NORSEMEN_CHIEF.get();
            case AMAZON -> RaiderEntityRegistry.AMAZON_CHIEF.get();
            case DROWNED_PIRATE -> RaiderEntityRegistry.DROWNED_CAPTAIN_PIRATE.get();
        };
    }

    private int countAliveRaiders(ServerLevel level) {
        int count = 0;
        AABB area = new AABB(
                targetCenter.getX() - 60, targetCenter.getY() - 50, targetCenter.getZ() - 60,
                targetCenter.getX() + 60, targetCenter.getY() + 50, targetCenter.getZ() + 60);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, area)) {
            if (mob.isAlive() && mob.getTags().contains("raid_" + raidId)) {
                count++;
            }
        }
        return count;
    }

    private void cleanupStragglers(ServerLevel level) {
        AABB area = new AABB(
                targetCenter.getX() - 60, targetCenter.getY() - 50, targetCenter.getZ() - 60,
                targetCenter.getX() + 60, targetCenter.getY() + 50, targetCenter.getZ() + 60);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, area)) {
            if (mob.isAlive() && mob.getTags().contains("raid_" + raidId)) {
                mob.discard();
            }
        }
    }

    // --- Getters ---

    public int getRaidId() { return raidId; }
    public RaiderCulture getCulture() { return culture; }
    public BlockPos getTargetCenter() { return targetCenter; }
    public int getTotalRaiders() { return totalRaiders; }
    public int getSpawnedRaiders() { return spawnedRaiders; }
    public int getKilledRaiders() { return killedRaiders; }
    public EventStatus getStatus() { return status; }
    public long getStartTick() { return startTick; }
    public int getCurrentWave() { return currentWave; }
    public int getMaxWaves() { return maxWaves; }
    public int getDifficulty() { return difficulty; }

    public boolean isComplete() {
        return status == EventStatus.FINISHED;
    }

    // --- NBT Save/Load ---

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("RaidId", raidId);
        tag.putInt("Culture", culture.ordinal());
        tag.putInt("TargetX", targetCenter.getX());
        tag.putInt("TargetY", targetCenter.getY());
        tag.putInt("TargetZ", targetCenter.getZ());
        tag.putInt("TotalRaiders", totalRaiders);
        tag.putInt("SpawnedRaiders", spawnedRaiders);
        tag.putInt("KilledRaiders", killedRaiders);
        tag.putInt("Status", status.ordinal());
        tag.putLong("StartTick", startTick);
        tag.putInt("CurrentWave", currentWave);
        tag.putInt("MaxWaves", maxWaves);
        tag.putInt("Difficulty", difficulty);
        tag.putInt("TicksInPhase", ticksInCurrentPhase);
        tag.putIntArray("WaveSizes", waveSizes);
        return tag;
    }

    public static RaidEvent load(CompoundTag tag) {
        int raidId = tag.getIntOr("RaidId", 0);
        RaiderCulture culture = RaiderCulture.fromOrdinal(tag.getIntOr("Culture", 0));
        BlockPos center = new BlockPos(
                tag.getIntOr("TargetX", 0),
                tag.getIntOr("TargetY", 0),
                tag.getIntOr("TargetZ", 0));
        int difficulty = tag.getIntOr("Difficulty", 7);
        int maxWaves = tag.getIntOr("MaxWaves", 2);

        int[] defaultWaves = new int[maxWaves];
        for (int i = 0; i < maxWaves; i++) defaultWaves[i] = 5;
        int[] waveSizesArr = tag.getIntArray("WaveSizes").orElse(defaultWaves);

        long startTick = tag.getLongOr("StartTick", 0L);

        RaidEvent event = new RaidEvent(raidId, culture, center, difficulty, maxWaves, waveSizesArr, startTick);
        event.spawnedRaiders = tag.getIntOr("SpawnedRaiders", 0);
        event.killedRaiders = tag.getIntOr("KilledRaiders", 0);
        event.status = EventStatus.values()[Math.min(tag.getIntOr("Status", 0), EventStatus.values().length - 1)];
        event.currentWave = tag.getIntOr("CurrentWave", 0);
        event.ticksInCurrentPhase = tag.getIntOr("TicksInPhase", 0);
        return event;
    }
}
