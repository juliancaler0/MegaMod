package com.ultra.megamod.feature.citizen.colony;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.util.AsyncSaveHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton that manages ALL colonies on the server.
 * <p>
 * This is the direct replacement for FactionManager. Every method from the old
 * FactionManager has an equivalent here so that consumer files need minimal changes:
 * <ul>
 *   <li>{@code FactionManager.get(level)} -> {@code ColonyManager.get(level)}</li>
 *   <li>{@code fm.getPlayerFaction(uuid)} -> {@code cm.getPlayerFaction(uuid)}</li>
 *   <li>{@code fm.getPlayerFactionData(uuid)} -> {@code cm.getPlayerFactionData(uuid)}</li>
 *   <li>{@code fm.getFaction(id)} -> {@code cm.getFaction(id)}</li>
 *   <li>{@code fm.getAllFactions()} -> {@code cm.getAllFactions()}</li>
 *   <li>{@code fm.createFaction(...)} -> {@code cm.createFaction(...)}</li>
 *   <li>{@code fm.deleteFaction(id)} -> {@code cm.deleteFaction(id)}</li>
 *   <li>{@code fm.leaveFaction(uuid)} -> {@code cm.leaveFaction(uuid)}</li>
 *   <li>{@code fm.joinFaction(uuid, id)} -> {@code cm.joinFaction(uuid, id)}</li>
 *   <li>{@code fm.requestJoin(uuid, id)} -> {@code cm.requestJoin(uuid, id)}</li>
 *   <li>{@code fm.acceptJoinRequest(id, uuid)} -> {@code cm.acceptJoinRequest(id, uuid)}</li>
 *   <li>{@code fm.getFactionCount()} -> {@code cm.getFactionCount()}</li>
 *   <li>{@code fm.saveToDisk(level)} -> {@code cm.saveToDisk(level)}</li>
 * </ul>
 * <p>
 * Persists to {@code world/data/megamod_colonies.dat}.
 * Uses the MegaMod persistence pattern: static singleton + NbtIo + dirty flag.
 */
public final class ColonyManager {

    private static final String SAVE_FILE = "megamod_colonies.dat";

    // ==================== Singleton ====================

    private static ColonyManager INSTANCE;

    /**
     * Gets or lazily creates the ColonyManager singleton, loading from disk if needed.
     * This mirrors the old {@code FactionManager.get(level)} pattern.
     */
    public static ColonyManager get(@NotNull ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new ColonyManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    /**
     * Resets the singleton (called on server stop / world unload).
     */
    public static void reset() {
        INSTANCE = null;
    }

    // ==================== Fields ====================

    /** All colonies, keyed by their string faction ID. */
    private final Map<String, Colony> coloniesByFactionId = new ConcurrentHashMap<>();

    /** All colonies, keyed by integer colony ID. */
    private final Map<Integer, Colony> coloniesById = new ConcurrentHashMap<>();

    /** Reverse lookup: player UUID -> faction ID of their colony. */
    private final Map<UUID, String> playerToFaction = new ConcurrentHashMap<>();

    /** Next auto-increment colony ID. */
    private int nextColonyId = 1;

    /** Whether data needs saving. */
    private boolean dirty = false;

    private ColonyManager() {}

    public void markDirty() {
        dirty = true;
    }

    // ==================== Colony Creation ====================

    /**
     * Creates a new colony. Matches old FactionManager.createFaction(id, name, ownerUUID).
     *
     * @param factionId   the string faction ID
     * @param displayName the display name
     * @param ownerUuid   the owner's UUID
     * @return the created Colony, or null if the factionId already exists
     */
    @Nullable
    public Colony createFaction(@NotNull String factionId, @NotNull String displayName, @NotNull UUID ownerUuid) {
        return createFaction(factionId, displayName, ownerUuid, 0);
    }

    /**
     * Creates a new colony (overload with cost parameter for compatibility).
     *
     * @param factionId   the string faction ID
     * @param displayName the display name
     * @param ownerUuid   the owner's UUID
     * @param cost        creation cost (handled by caller's economy deduction, not by us)
     * @return the created Colony, or null if the factionId already exists or owner already has a colony
     */
    @Nullable
    public Colony createFaction(@NotNull String factionId, @NotNull String displayName,
                                @NotNull UUID ownerUuid, int cost) {
        if (coloniesByFactionId.containsKey(factionId)) {
            return null;
        }

        // If player already owns a faction, prevent double-creation
        if (playerToFaction.containsKey(ownerUuid)) {
            return null;
        }

        int id = nextColonyId++;
        Colony colony = new Colony(id, factionId, displayName, ownerUuid);

        // Set owner in permissions
        colony.getPermissions().setOwner(ownerUuid, displayName);

        coloniesByFactionId.put(factionId, colony);
        coloniesById.put(id, colony);
        playerToFaction.put(ownerUuid, factionId);

        dirty = true;
        MegaMod.LOGGER.info("Created colony '{}' (id={}) for player {}", displayName, id, ownerUuid);
        return colony;
    }

    /**
     * Creates a colony for a player, with auto-ID and display name generation.
     * Convenience method matching the pattern used in AbstractBlockHut / TownChestBlock.
     *
     * @param player the player
     * @param level  the server level
     * @return the created Colony, or null on failure
     */
    @Nullable
    public Colony createColonyForPlayer(@NotNull Player player, @NotNull ServerLevel level) {
        String playerName = player.getGameProfile().name();
        String autoFactionId = playerName.toLowerCase().replaceAll("[^a-z0-9_]", "_");
        String autoDisplayName = playerName + "'s Colony";

        Colony created = createFaction(autoFactionId, autoDisplayName, player.getUUID(), 0);
        if (created == null) {
            // ID collision -- append part of UUID
            autoFactionId = autoFactionId + "_" + player.getUUID().toString().substring(0, 4);
            created = createFaction(autoFactionId, autoDisplayName, player.getUUID(), 0);
        }
        if (created != null) {
            created.setDimension(level.dimension());
            saveToDisk(level);
        }
        return created;
    }

    // ==================== Colony Deletion ====================

    /**
     * Deletes a colony by faction ID. Matches old FactionManager.deleteFaction().
     *
     * @return true if a colony was deleted
     */
    public boolean deleteFaction(@NotNull String factionId) {
        Colony colony = coloniesByFactionId.remove(factionId);
        if (colony == null) return false;

        coloniesById.remove(colony.getId());

        // Remove all player -> faction mappings for this colony
        Iterator<Map.Entry<UUID, String>> it = playerToFaction.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().equals(factionId)) {
                it.remove();
            }
        }

        dirty = true;
        MegaMod.LOGGER.info("Deleted colony '{}' (id={})", colony.getDisplayName(), colony.getId());
        return true;
    }

    // ==================== Colony Lookup ====================

    /**
     * Gets a colony by its string faction ID.
     * Matches old FactionManager.getFaction().
     */
    @Nullable
    public Colony getFaction(@NotNull String factionId) {
        return coloniesByFactionId.get(factionId);
    }

    /**
     * Gets a colony by its integer ID.
     */
    @Nullable
    public Colony getColonyById(int id) {
        return coloniesById.get(id);
    }

    /**
     * Gets the faction ID of the colony a player belongs to.
     * Matches old FactionManager.getPlayerFaction().
     *
     * @return the faction ID, or null if the player is not in any colony
     */
    @Nullable
    public String getPlayerFaction(@NotNull UUID playerUuid) {
        return playerToFaction.get(playerUuid);
    }

    /**
     * Gets the Colony object for a player.
     * Matches old FactionManager.getPlayerFactionData().
     *
     * @return the Colony, or null if the player is not in any colony
     */
    @Nullable
    public Colony getPlayerFactionData(@NotNull UUID playerUuid) {
        String factionId = playerToFaction.get(playerUuid);
        return factionId != null ? coloniesByFactionId.get(factionId) : null;
    }

    /**
     * Gets the Colony that is closest to the given position.
     */
    @Nullable
    public Colony getColonyByPos(@NotNull BlockPos pos) {
        Colony closest = null;
        double closestDist = Double.MAX_VALUE;
        for (Colony colony : coloniesByFactionId.values()) {
            BlockPos center = colony.getCenter();
            if (center != null && !center.equals(BlockPos.ZERO)) {
                double dist = center.distSqr(pos);
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = colony;
                }
            }
        }
        return closest;
    }

    /**
     * Gets the Colony that owns the given position (by checking if pos is within
     * the colony's claim radius). Returns null if no colony claims that position.
     */
    @Nullable
    public Colony getColonyByPlayer(@NotNull UUID playerUuid) {
        return getPlayerFactionData(playerUuid);
    }

    /**
     * Returns all colonies.
     * Matches old FactionManager.getAllFactions().
     */
    @NotNull
    public Collection<Colony> getAllFactions() {
        return Collections.unmodifiableCollection(coloniesByFactionId.values());
    }

    /**
     * Alias for getAllFactions().
     */
    @NotNull
    public Collection<Colony> getAllColonies() {
        return getAllFactions();
    }

    /**
     * Returns the number of colonies.
     * Matches old FactionManager.getFactionCount().
     */
    public int getFactionCount() {
        return coloniesByFactionId.size();
    }

    // ==================== Membership Management ====================

    /**
     * Adds a player to a colony as a MEMBER.
     * Matches old FactionManager.joinFaction().
     *
     * @return true if joined successfully
     */
    public boolean joinFaction(@NotNull UUID playerUuid, @NotNull String factionId) {
        if (playerToFaction.containsKey(playerUuid)) {
            // Already in a faction; only join if it is this one (idempotent)
            return factionId.equals(playerToFaction.get(playerUuid));
        }
        Colony colony = coloniesByFactionId.get(factionId);
        if (colony == null) return false;

        colony.addMember(playerUuid, Colony.PlayerRank.MEMBER);
        playerToFaction.put(playerUuid, factionId);
        dirty = true;
        return true;
    }

    /**
     * Removes a player from their colony.
     * Matches old FactionManager.leaveFaction().
     *
     * @return true if the player left
     */
    public boolean leaveFaction(@NotNull UUID playerUuid) {
        String factionId = playerToFaction.get(playerUuid);
        if (factionId == null) return false;

        Colony colony = coloniesByFactionId.get(factionId);
        if (colony == null) {
            playerToFaction.remove(playerUuid);
            return true;
        }

        // Leaders cannot leave; they must transfer ownership or delete
        if (playerUuid.equals(colony.getLeaderUuid())) {
            return false;
        }

        colony.removeMember(playerUuid);
        playerToFaction.remove(playerUuid);
        dirty = true;
        return true;
    }

    /**
     * Adds a join request to a colony.
     * Matches old FactionManager.requestJoin().
     *
     * @return true if request was added
     */
    public boolean requestJoin(@NotNull UUID playerUuid, @NotNull String factionId) {
        if (playerToFaction.containsKey(playerUuid)) return false;
        Colony colony = coloniesByFactionId.get(factionId);
        if (colony == null) return false;
        return colony.addJoinRequest(playerUuid);
    }

    /**
     * Accepts a join request, adding the player as MEMBER.
     * Matches old FactionManager.acceptJoinRequest().
     *
     * @return true if accepted
     */
    public boolean acceptJoinRequest(@NotNull String factionId, @NotNull UUID playerUuid) {
        Colony colony = coloniesByFactionId.get(factionId);
        if (colony == null) return false;
        if (!colony.hasJoinRequest(playerUuid)) return false;

        colony.removeJoinRequest(playerUuid);
        return joinFaction(playerUuid, factionId);
    }

    // ==================== Ticking ====================

    /**
     * Called once per server tick to update colony state (day counters, happiness, etc.).
     */
    public void tick(@NotNull ServerLevel level) {
        long gameTime = level.getGameTime();

        // Once per in-game day (24000 ticks)
        if (gameTime % 24000 == 0) {
            for (Colony colony : coloniesByFactionId.values()) {
                colony.incrementDay();
            }
        }
    }

    // ==================== Persistence ====================

    /**
     * Saves all colony data to disk.
     * Matches old FactionManager.saveToDisk().
     */
    public void saveToDisk(@NotNull ServerLevel level) {
        try {
            CompoundTag root = new CompoundTag();
            root.putInt("nextColonyId", nextColonyId);

            ListTag colonyList = new ListTag();
            for (Colony colony : coloniesByFactionId.values()) {
                colonyList.add(colony.save());
            }
            root.put("colonies", colonyList);

            File dataDir = new File(level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toFile(), "data");
            if (!dataDir.exists()) dataDir.mkdirs();
            File saveFile = new File(dataDir, SAVE_FILE);

            // Use async save if available, fall back to synchronous
            final File finalSaveFile = saveFile;
            final CompoundTag finalRoot = root;
            try {
                AsyncSaveHelper.saveAsync(() -> {
                    try {
                        NbtIo.writeCompressed(finalRoot, finalSaveFile.toPath());
                    } catch (Exception ex) {
                        MegaMod.LOGGER.error("Async colony save failed", ex);
                    }
                });
            } catch (NoClassDefFoundError | Exception e) {
                NbtIo.writeCompressed(root, saveFile.toPath());
            }

            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save colony data", e);
        }
    }

    /**
     * Loads all colony data from disk.
     * Matches old FactionManager.loadFromDisk().
     */
    public void loadFromDisk(@NotNull ServerLevel level) {
        coloniesByFactionId.clear();
        coloniesById.clear();
        playerToFaction.clear();
        nextColonyId = 1;

        try {
            File dataDir = new File(level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toFile(), "data");
            File saveFile = new File(dataDir, SAVE_FILE);

            if (!saveFile.exists()) {
                MegaMod.LOGGER.info("No colony data file found, starting fresh");
                // Attempt migration from old FactionManager format
                migrateFromFactionManager(dataDir);
                return;
            }

            CompoundTag root = NbtIo.readCompressed(saveFile.toPath(), net.minecraft.nbt.NbtAccounter.unlimitedHeap());
            if (root == null) return;

            nextColonyId = root.getIntOr("nextColonyId", 1);

            if (root.contains("colonies")) {
                ListTag colonyList = root.getListOrEmpty("colonies");
                for (int i = 0; i < colonyList.size(); i++) {
                    if (!(colonyList.get(i) instanceof CompoundTag colonyTag)) continue;
                    int id = colonyTag.getIntOr("id", nextColonyId++);
                    String factionId = colonyTag.getStringOr("factionId", "colony_" + id);

                    Colony colony = new Colony(id, factionId);
                    colony.load(colonyTag);

                    coloniesByFactionId.put(factionId, colony);
                    coloniesById.put(id, colony);

                    // Rebuild player -> faction index
                    for (UUID memberUuid : colony.getMembers().keySet()) {
                        playerToFaction.put(memberUuid, factionId);
                    }

                    if (id >= nextColonyId) {
                        nextColonyId = id + 1;
                    }
                }
            }

            MegaMod.LOGGER.info("Loaded {} colonies from disk", coloniesByFactionId.size());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load colony data", e);
        }
    }

    /**
     * Attempts to migrate data from the old FactionManager format (megamod_factions.dat).
     * This allows seamless upgrade from the faction system to the colony system.
     */
    private void migrateFromFactionManager(@NotNull File dataDir) {
        File oldFile = new File(dataDir, "megamod_factions.dat");
        if (!oldFile.exists()) return;

        try {
            MegaMod.LOGGER.info("Found old faction data, migrating to colony system...");
            CompoundTag root = NbtIo.readCompressed(oldFile.toPath(), net.minecraft.nbt.NbtAccounter.unlimitedHeap());
            if (root == null) return;

            if (root.contains("factions")) {
                ListTag factionList = root.getListOrEmpty("factions");
                for (int i = 0; i < factionList.size(); i++) {
                    if (!(factionList.get(i) instanceof CompoundTag fTag)) continue;
                    String factionId = fTag.getStringOr("factionId", "");
                    String displayName = fTag.getStringOr("displayName", factionId);

                    if (factionId.isEmpty()) continue;

                    int id = nextColonyId++;
                    Colony colony = new Colony(id, factionId);

                    // Migrate basic fields
                    colony.setDisplayName(displayName);

                    if (fTag.contains("leaderUuid")) {
                        UUID leader = UUID.fromString(fTag.getStringOr("leaderUuid", ""));
                        colony.setLeaderUuid(leader);
                        colony.addMember(leader, Colony.PlayerRank.LEADER);
                        playerToFaction.put(leader, factionId);
                    }

                    // Migrate town chest pos
                    if (fTag.contains("townChestX")) {
                        BlockPos townChest = new BlockPos(
                                fTag.getIntOr("townChestX", 0),
                                fTag.getIntOr("townChestY", 0),
                                fTag.getIntOr("townChestZ", 0)
                        );
                        if (!townChest.equals(BlockPos.ZERO)) {
                            colony.setCenter(townChest);
                        }
                    }

                    // Migrate members
                    if (fTag.contains("members")) {
                        ListTag memberList = fTag.getListOrEmpty("members");
                        for (int j = 0; j < memberList.size(); j++) {
                            if (!(memberList.get(j) instanceof CompoundTag mTag)) continue;
                            String mUuidStr = mTag.getStringOr("uuid", "");
                            if (mUuidStr.isEmpty()) continue;
                            UUID memberUuid = UUID.fromString(mUuidStr);
                            String rankStr = mTag.getStringOr("rank", "MEMBER");
                            Colony.PlayerRank rank;
                            try {
                                rank = Colony.PlayerRank.valueOf(rankStr);
                            } catch (IllegalArgumentException e) {
                                rank = Colony.PlayerRank.MEMBER;
                            }
                            colony.addMember(memberUuid, rank);
                            playerToFaction.put(memberUuid, factionId);
                        }
                    }

                    // Migrate settings
                    colony.setRaidersEnabled(fTag.getBooleanOr("raidersEnabled", true));
                    colony.setAbandoned(fTag.getBooleanOr("abandoned", false));
                    colony.setMaxNPCs(fTag.getIntOr("maxNPCs", 4));

                    coloniesByFactionId.put(factionId, colony);
                    coloniesById.put(id, colony);
                }
            }

            MegaMod.LOGGER.info("Migrated {} factions to colonies", coloniesByFactionId.size());
            dirty = true;
        } catch (Exception e) {
            MegaMod.LOGGER.warn("Failed to migrate old faction data", e);
        }
    }

    // ==================== Utility ====================

    public boolean isDirty() {
        if (dirty) return true;
        for (Colony colony : coloniesByFactionId.values()) {
            if (colony.isDirty()) return true;
        }
        return false;
    }

    /**
     * Returns the next available colony ID without incrementing.
     */
    public int peekNextColonyId() {
        return nextColonyId;
    }
}
