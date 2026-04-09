package com.ultra.megamod.feature.citizen.colony;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Server-side colony implementation. Stores all colony state: identity, members,
 * buildings, citizens, permissions, raid settings, happiness, day count, etc.
 * <p>
 * This replaces the old FactionData class. The API is designed to be a drop-in
 * replacement where FactionData methods map directly:
 * <ul>
 *   <li>FactionData.getFactionId() -> Colony.getFactionId()</li>
 *   <li>FactionData.getDisplayName() -> Colony.getDisplayName()</li>
 *   <li>FactionData.getLeaderUuid() -> Colony.getLeaderUuid()</li>
 *   <li>FactionData.getMemberCount() -> Colony.getMemberCount()</li>
 *   <li>FactionData.getTownChestPos() -> Colony.getTownChestPos()</li>
 *   <li>FactionData.hasTownChest() -> Colony.hasTownChest()</li>
 *   <li>FactionData.isMember(UUID) -> Colony.isMember(UUID)</li>
 *   <li>FactionData.PlayerRank -> ColonyPermissions rank IDs</li>
 *   <li>FactionData.setMemberRank() -> Colony.setMemberRank()</li>
 * </ul>
 * <p>
 * Modeled after MineColonies' Colony class with full NBT persistence.
 */
public class Colony implements IColony {

    // ==================== PlayerRank (compatibility with old FactionData.PlayerRank) ====================

    /**
     * Player rank enum for backwards compatibility with FactionData.PlayerRank.
     * Maps to ColonyPermissions rank IDs.
     */
    public enum PlayerRank {
        LEADER(ColonyPermissions.OWNER_RANK_ID),
        OFFICER(ColonyPermissions.OFFICER_RANK_ID),
        MEMBER(ColonyPermissions.FRIEND_RANK_ID);

        private final int permissionRankId;

        PlayerRank(int permissionRankId) {
            this.permissionRankId = permissionRankId;
        }

        public int getPermissionRankId() {
            return permissionRankId;
        }

        @Nullable
        public static PlayerRank fromPermissionRankId(int id) {
            for (PlayerRank pr : values()) {
                if (pr.permissionRankId == id) return pr;
            }
            return null;
        }
    }

    // ==================== Fields ====================

    /** Unique integer ID (auto-incremented by ColonyManager). */
    private final int id;

    /** String faction ID (e.g., "drek"), used as the lookup key throughout the codebase. */
    @NotNull
    private final String factionId;

    /** Human-readable name (e.g., "Drek's Colony"). */
    @NotNull
    private String displayName;

    /** UUID of the colony owner/leader. */
    @Nullable
    private UUID leaderUuid;

    /** Center position (Town Hall / Town Chest). */
    @Nullable
    private BlockPos center;

    /** Dimension the colony is in. */
    @Nullable
    private ResourceKey<Level> dimension;

    /** Player members: UUID -> PlayerRank. */
    private final Map<UUID, PlayerRank> members = new LinkedHashMap<>();

    /** Join requests: UUIDs of players who requested to join. */
    private final Set<UUID> joinRequests = new LinkedHashSet<>();

    /** Permissions system. */
    @NotNull
    private final ColonyPermissions permissions = new ColonyPermissions();

    /** Building manager. */
    @NotNull
    private final ColonyBuildingManager buildingManager = new ColonyBuildingManager();

    /** Citizen manager (colony-level tracking). */
    @NotNull
    private final ColonyCitizenManager citizenManager = new ColonyCitizenManager();

    /** Overall colony happiness (0.0 - 10.0). */
    private double happiness = 5.0;

    /** Day count since colony founding. */
    private int day = 0;

    /** Whether raiders can attack this colony. */
    private boolean raidersEnabled = true;

    /** Whether the colony is abandoned. */
    private boolean abandoned = false;

    /** Maximum NPC count (increases with Town Hall level). */
    private int maxNPCs = 4;

    /** Colony team color index. */
    private int teamColor = 15; // White

    /** Whether the colony needs saving. */
    private boolean dirty = true;

    /** Whether the colony is under attack. */
    private boolean underAttack = false;

    /** Last contact time (game time ticks). */
    private long lastContactTime = 0;

    /** Creation time (game time ticks). */
    private long creationTime = 0;

    // ==================== Constructors ====================

    /**
     * Creates a new colony.
     *
     * @param id          unique integer ID
     * @param factionId   string faction ID
     * @param displayName human-readable name
     * @param leaderUuid  owner UUID
     */
    public Colony(int id, @NotNull String factionId, @NotNull String displayName, @Nullable UUID leaderUuid) {
        this.id = id;
        this.factionId = factionId;
        this.displayName = displayName;
        this.leaderUuid = leaderUuid;
        if (leaderUuid != null) {
            members.put(leaderUuid, PlayerRank.LEADER);
        }
    }

    /**
     * Creates a colony from NBT (used during loading).
     */
    Colony(int id, @NotNull String factionId) {
        this.id = id;
        this.factionId = factionId;
        this.displayName = factionId;
    }

    // ==================== IColony Implementation ====================

    @Override
    public int getId() { return id; }

    @Override
    @NotNull
    public String getFactionId() { return factionId; }

    @Override
    @NotNull
    public String getDisplayName() { return displayName; }

    @Override
    public void setDisplayName(@NotNull String name) {
        this.displayName = name;
        dirty = true;
    }

    @Override
    @Nullable
    public BlockPos getCenter() { return center; }

    public void setCenter(@Nullable BlockPos pos) {
        this.center = pos;
        dirty = true;
    }

    @Override
    public void setTownChestPos(@NotNull BlockPos pos) {
        setCenter(pos);
    }

    @Override
    @Nullable
    public UUID getLeaderUuid() { return leaderUuid; }

    public void setLeaderUuid(@Nullable UUID uuid) {
        this.leaderUuid = uuid;
        dirty = true;
    }

    @Override
    public int getMemberCount() { return members.size(); }

    /** Returns an unmodifiable set of all member UUIDs. */
    public java.util.Set<UUID> getMemberUuids() { return java.util.Collections.unmodifiableSet(members.keySet()); }

    @Override
    @Nullable
    public ResourceKey<Level> getDimension() { return dimension; }

    public void setDimension(@Nullable ResourceKey<Level> dim) {
        this.dimension = dim;
        dirty = true;
    }

    @Override
    @NotNull
    public ColonyPermissions getPermissions() { return permissions; }

    @Override
    @NotNull
    public ColonyBuildingManager getBuildingManager() { return buildingManager; }

    @Override
    @NotNull
    public ColonyCitizenManager getCitizenManager() { return citizenManager; }

    @Override
    public boolean isRemote() { return false; }

    @Override
    public boolean isMember(@NotNull UUID playerUuid) {
        return members.containsKey(playerUuid);
    }

    @Override
    public double getOverallHappiness() { return happiness; }

    public void setOverallHappiness(double happiness) {
        this.happiness = Math.max(0.0, Math.min(10.0, happiness));
        dirty = true;
    }

    @Override
    public int getDay() { return day; }

    public void setDay(int day) {
        this.day = day;
        dirty = true;
    }

    public void incrementDay() {
        this.day++;
        dirty = true;
    }

    @Override
    public boolean isRaidersEnabled() { return raidersEnabled; }

    public void setRaidersEnabled(boolean enabled) {
        this.raidersEnabled = enabled;
        dirty = true;
    }

    @Override
    public boolean isAbandoned() { return abandoned; }

    public void setAbandoned(boolean abandoned) {
        this.abandoned = abandoned;
        dirty = true;
    }

    @Override
    public int getMaxNPCs() { return maxNPCs; }

    public void setMaxNPCs(int max) {
        this.maxNPCs = max;
        dirty = true;
    }

    @Override
    public void markDirty() { this.dirty = true; }

    // ==================== Member Management ====================

    /**
     * Adds a player as a member with the given rank.
     *
     * @return true if added or rank updated
     */
    public boolean addMember(@NotNull UUID uuid, @NotNull PlayerRank rank) {
        members.put(uuid, rank);
        ColonyPermissions.Rank permRank = permissions.getRank(rank.getPermissionRankId());
        if (permRank != null) {
            // Try to get the player name from existing permissions entry, or use UUID string
            String name = uuid.toString().substring(0, 8);
            ColonyPermissions.ColonyPlayer existing = permissions.getPlayers().get(uuid);
            if (existing != null) {
                name = existing.getName();
            }
            permissions.addPlayer(uuid, name, permRank);
        }
        dirty = true;
        return true;
    }

    /**
     * Removes a member.
     *
     * @return true if the member was removed
     */
    public boolean removeMember(@NotNull UUID uuid) {
        if (uuid.equals(leaderUuid)) return false; // Cannot remove leader
        boolean removed = members.remove(uuid) != null;
        if (removed) {
            permissions.removePlayer(uuid);
            dirty = true;
        }
        return removed;
    }

    /**
     * Sets a member's rank.
     */
    public void setMemberRank(@NotNull UUID uuid, @NotNull PlayerRank rank) {
        if (!members.containsKey(uuid)) return;
        members.put(uuid, rank);
        ColonyPermissions.Rank permRank = permissions.getRank(rank.getPermissionRankId());
        if (permRank != null) {
            permissions.setPlayerRank(uuid, permRank);
        }
        dirty = true;
    }

    /**
     * Gets a member's rank, or null if not a member.
     */
    @Nullable
    public PlayerRank getMemberRank(@NotNull UUID uuid) {
        return members.get(uuid);
    }

    /**
     * Returns an unmodifiable view of the member map.
     */
    @NotNull
    public Map<UUID, PlayerRank> getMembers() {
        return Collections.unmodifiableMap(members);
    }

    // ==================== Join Requests ====================

    public boolean addJoinRequest(@NotNull UUID uuid) {
        boolean added = joinRequests.add(uuid);
        if (added) dirty = true;
        return added;
    }

    public boolean removeJoinRequest(@NotNull UUID uuid) {
        boolean removed = joinRequests.remove(uuid);
        if (removed) dirty = true;
        return removed;
    }

    public boolean hasJoinRequest(@NotNull UUID uuid) {
        return joinRequests.contains(uuid);
    }

    @NotNull
    public Set<UUID> getJoinRequests() {
        return Collections.unmodifiableSet(joinRequests);
    }

    // ==================== Misc State ====================

    public int getTeamColor() { return teamColor; }
    public void setTeamColor(int color) { this.teamColor = color; dirty = true; }

    public boolean isUnderAttack() { return underAttack; }
    public void setUnderAttack(boolean underAttack) { this.underAttack = underAttack; }

    public long getLastContactTime() { return lastContactTime; }
    public void setLastContactTime(long time) { this.lastContactTime = time; }

    public long getCreationTime() { return creationTime; }
    public void setCreationTime(long time) { this.creationTime = time; }

    public boolean isDirty() { return dirty; }
    public void clearDirty() { dirty = false; }

    // ==================== NBT Serialization ====================

    private static final String TAG_ID = "id";
    private static final String TAG_FACTION_ID = "factionId";
    private static final String TAG_DISPLAY_NAME = "displayName";
    private static final String TAG_LEADER_UUID = "leaderUuid";
    private static final String TAG_CENTER_X = "centerX";
    private static final String TAG_CENTER_Y = "centerY";
    private static final String TAG_CENTER_Z = "centerZ";
    private static final String TAG_HAS_CENTER = "hasCenter";
    private static final String TAG_DIMENSION = "dimension";
    private static final String TAG_MEMBERS = "members";
    private static final String TAG_UUID = "uuid";
    private static final String TAG_RANK = "rank";
    private static final String TAG_JOIN_REQUESTS = "joinRequests";
    private static final String TAG_PERMISSIONS = "permissions";
    private static final String TAG_BUILDINGS = "buildings";
    private static final String TAG_CITIZENS = "citizens";
    private static final String TAG_HAPPINESS = "happiness";
    private static final String TAG_DAY = "day";
    private static final String TAG_RAIDERS = "raidersEnabled";
    private static final String TAG_ABANDONED = "abandoned";
    private static final String TAG_MAX_NPCS = "maxNPCs";
    private static final String TAG_TEAM_COLOR = "teamColor";
    private static final String TAG_LAST_CONTACT = "lastContact";
    private static final String TAG_CREATION_TIME = "creationTime";

    /**
     * Saves the colony to NBT.
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_ID, id);
        tag.putString(TAG_FACTION_ID, factionId);
        tag.putString(TAG_DISPLAY_NAME, displayName);

        if (leaderUuid != null) {
            tag.putString(TAG_LEADER_UUID, leaderUuid.toString());
        }

        if (center != null) {
            tag.putBoolean(TAG_HAS_CENTER, true);
            tag.putInt(TAG_CENTER_X, center.getX());
            tag.putInt(TAG_CENTER_Y, center.getY());
            tag.putInt(TAG_CENTER_Z, center.getZ());
        } else {
            tag.putBoolean(TAG_HAS_CENTER, false);
        }

        if (dimension != null) {
            tag.putString(TAG_DIMENSION, dimension.identifier().toString());
        }

        // Members
        net.minecraft.nbt.ListTag memberList = new net.minecraft.nbt.ListTag();
        for (Map.Entry<UUID, PlayerRank> entry : members.entrySet()) {
            CompoundTag memberTag = new CompoundTag();
            memberTag.putString(TAG_UUID, entry.getKey().toString());
            memberTag.putString(TAG_RANK, entry.getValue().name());
            memberList.add(memberTag);
        }
        tag.put(TAG_MEMBERS, memberList);

        // Join requests
        net.minecraft.nbt.ListTag requestList = new net.minecraft.nbt.ListTag();
        for (UUID uuid : joinRequests) {
            CompoundTag reqTag = new CompoundTag();
            reqTag.putString(TAG_UUID, uuid.toString());
            requestList.add(reqTag);
        }
        tag.put(TAG_JOIN_REQUESTS, requestList);

        // Sub-managers
        tag.put(TAG_PERMISSIONS, permissions.save());
        tag.put(TAG_BUILDINGS, buildingManager.save());
        tag.put(TAG_CITIZENS, citizenManager.save());

        // Colony state
        tag.putDouble(TAG_HAPPINESS, happiness);
        tag.putInt(TAG_DAY, day);
        tag.putBoolean(TAG_RAIDERS, raidersEnabled);
        tag.putBoolean(TAG_ABANDONED, abandoned);
        tag.putInt(TAG_MAX_NPCS, maxNPCs);
        tag.putInt(TAG_TEAM_COLOR, teamColor);
        tag.putLong(TAG_LAST_CONTACT, lastContactTime);
        tag.putLong(TAG_CREATION_TIME, creationTime);

        return tag;
    }

    /**
     * Loads the colony state from NBT.
     */
    public void load(CompoundTag tag) {
        displayName = tag.getStringOr(TAG_DISPLAY_NAME, factionId);

        if (tag.contains(TAG_LEADER_UUID)) {
            leaderUuid = UUID.fromString(tag.getStringOr(TAG_LEADER_UUID, ""));
        }

        if (tag.getBooleanOr(TAG_HAS_CENTER, false)) {
            center = new BlockPos(
                    tag.getIntOr(TAG_CENTER_X, 0),
                    tag.getIntOr(TAG_CENTER_Y, 0),
                    tag.getIntOr(TAG_CENTER_Z, 0)
            );
        } else {
            center = null;
        }

        if (tag.contains(TAG_DIMENSION)) {
            String dimStr = tag.getStringOr(TAG_DIMENSION, "");
            if (!dimStr.isEmpty()) {
                dimension = ResourceKey.create(
                        net.minecraft.core.registries.Registries.DIMENSION,
                        net.minecraft.resources.Identifier.parse(dimStr)
                );
            }
        }

        // Members
        members.clear();
        if (tag.contains(TAG_MEMBERS)) {
            net.minecraft.nbt.ListTag memberList = tag.getListOrEmpty(TAG_MEMBERS);
            for (int i = 0; i < memberList.size(); i++) {
                if (!(memberList.get(i) instanceof CompoundTag memberTag)) continue;
                String uuidStr = memberTag.getStringOr(TAG_UUID, "");
                UUID uuid = uuidStr.isEmpty() ? null : UUID.fromString(uuidStr);
                if (uuid == null) continue;
                String rankStr = memberTag.getStringOr(TAG_RANK, "MEMBER");
                PlayerRank rank;
                try {
                    rank = PlayerRank.valueOf(rankStr);
                } catch (IllegalArgumentException e) {
                    rank = PlayerRank.MEMBER;
                }
                members.put(uuid, rank);
            }
        }

        // Join requests
        joinRequests.clear();
        if (tag.contains(TAG_JOIN_REQUESTS)) {
            net.minecraft.nbt.ListTag requestList = tag.getListOrEmpty(TAG_JOIN_REQUESTS);
            for (int i = 0; i < requestList.size(); i++) {
                if (!(requestList.get(i) instanceof CompoundTag reqTag)) continue;
                String uuidStr2 = reqTag.getStringOr(TAG_UUID, "");
                UUID uuid = uuidStr2.isEmpty() ? null : UUID.fromString(uuidStr2);
                if (uuid != null) joinRequests.add(uuid);
            }
        }

        // Sub-managers
        if (tag.contains(TAG_PERMISSIONS)) {
            permissions.load(tag.getCompoundOrEmpty(TAG_PERMISSIONS));
        }
        if (tag.contains(TAG_BUILDINGS)) {
            buildingManager.load(tag.getCompoundOrEmpty(TAG_BUILDINGS));
        }
        if (tag.contains(TAG_CITIZENS)) {
            citizenManager.load(tag.getCompoundOrEmpty(TAG_CITIZENS));
        }

        // Colony state
        happiness = tag.getDoubleOr(TAG_HAPPINESS, 5.0);
        day = tag.getIntOr(TAG_DAY, 0);
        raidersEnabled = tag.getBooleanOr(TAG_RAIDERS, true);
        abandoned = tag.getBooleanOr(TAG_ABANDONED, false);
        maxNPCs = tag.getIntOr(TAG_MAX_NPCS, 4);
        teamColor = tag.getIntOr(TAG_TEAM_COLOR, 15);
        lastContactTime = tag.getLongOr(TAG_LAST_CONTACT, 0L);
        creationTime = tag.getLongOr(TAG_CREATION_TIME, 0L);

        dirty = false;
    }

    @Override
    public String toString() {
        return "Colony{id=" + id + ", factionId='" + factionId + "', name='" + displayName + "'}";
    }
}
