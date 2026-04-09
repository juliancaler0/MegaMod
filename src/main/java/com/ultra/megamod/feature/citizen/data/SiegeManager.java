package com.ultra.megamod.feature.citizen.data;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.citizen.CitizenConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * Manages sieges between factions.
 * Static singleton with manual NbtIo file persistence.
 */
public class SiegeManager {
    private static SiegeManager INSTANCE;
    private static final String FILE_NAME = "megamod_sieges.dat";

    private final List<SiegeData> activeSieges = new ArrayList<>();
    private boolean dirty = false;

    public static SiegeManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new SiegeManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    public List<SiegeData> getActiveSieges() {
        return Collections.unmodifiableList(activeSieges);
    }

    public SiegeData getSiege(String attacker, String defender) {
        for (SiegeData siege : activeSieges) {
            if (siege.getAttackerFaction().equals(attacker) && siege.getDefenderFaction().equals(defender)) {
                return siege;
            }
        }
        return null;
    }

    public void startSiege(String attacker, String defender, ServerLevel level) {
        if (getSiege(attacker, defender) != null) return;
        SiegeData siege = new SiegeData(attacker, defender,
            CitizenConfig.SIEGE_HEALTH_DEFAULT,
            CitizenConfig.SIEGE_HEALTH_DEFAULT,
            level.getServer().getTickCount());
        activeSieges.add(siege);
        dirty = true;
    }

    public void endSiege(String attacker, String defender) {
        activeSieges.removeIf(s ->
            s.getAttackerFaction().equals(attacker) && s.getDefenderFaction().equals(defender));
        dirty = true;
    }

    public void forceWinAttacker(String attacker, String defender, ServerLevel level) {
        endSiege(attacker, defender);
        // Transfer claims from defender to attacker
        ClaimManager.get(level).transferClaims(defender, attacker);
        dirty = true;
    }

    public void forceWinDefender(String attacker, String defender, ServerLevel level) {
        endSiege(attacker, defender);
        dirty = true;
    }

    public void setSiegeClaimHealth(String attacker, String defender, int health) {
        SiegeData siege = getSiege(attacker, defender);
        if (siege != null) {
            siege.setClaimHealth(health);
            dirty = true;
        }
    }

    // ---- Persistence ----

    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            Path worldDir = level.getServer().getWorldPath(LevelResource.ROOT);
            File dataDir = worldDir.resolve("data").toFile();
            if (!dataDir.exists()) dataDir.mkdirs();
            File file = new File(dataDir, FILE_NAME);

            CompoundTag root = new CompoundTag();
            ListTag siegeList = new ListTag();
            for (SiegeData siege : activeSieges) {
                CompoundTag st = new CompoundTag();
                st.putString("attacker", siege.getAttackerFaction());
                st.putString("defender", siege.getDefenderFaction());
                st.putInt("health", siege.getClaimHealth());
                st.putInt("maxHealth", siege.getMaxClaimHealth());
                st.putLong("startTick", siege.getStartTick());
                siegeList.add(st);
            }
            root.put("sieges", siegeList);
            NbtIo.writeCompressed(root, file.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save SiegeManager", e);
        }
    }

    private void loadFromDisk(ServerLevel level) {
        try {
            Path worldDir = level.getServer().getWorldPath(LevelResource.ROOT);
            File file = worldDir.resolve("data").resolve(FILE_NAME).toFile();
            if (!file.exists()) return;

            CompoundTag root = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());
            Tag tag = root.get("sieges");
            if (tag instanceof ListTag siegeList) {
                for (int i = 0; i < siegeList.size(); i++) {
                    if (siegeList.get(i) instanceof CompoundTag st) {
                        activeSieges.add(new SiegeData(
                            st.getStringOr("attacker", ""),
                            st.getStringOr("defender", ""),
                            st.getIntOr("health", 100),
                            st.getIntOr("maxHealth", 100),
                            st.getLongOr("startTick", 0L)
                        ));
                    }
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load SiegeManager", e);
        }
    }

    /**
     * Data class representing an active siege between two factions.
     */
    public static class SiegeData {
        private final String attackerFaction;
        private final String defenderFaction;
        private int claimHealth;
        private final int maxClaimHealth;
        private final long startTick;

        public SiegeData(String attackerFaction, String defenderFaction, int claimHealth, int maxClaimHealth, long startTick) {
            this.attackerFaction = attackerFaction;
            this.defenderFaction = defenderFaction;
            this.claimHealth = claimHealth;
            this.maxClaimHealth = maxClaimHealth;
            this.startTick = startTick;
        }

        public String getAttackerFaction() { return attackerFaction; }
        public String getDefenderFaction() { return defenderFaction; }
        public int getClaimHealth() { return claimHealth; }
        public void setClaimHealth(int health) { this.claimHealth = health; }
        public int getMaxClaimHealth() { return maxClaimHealth; }
        public long getStartTick() { return startTick; }
    }
}
