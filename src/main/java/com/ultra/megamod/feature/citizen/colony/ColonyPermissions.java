package com.ultra.megamod.feature.citizen.colony;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Per-colony permission system modeled after MineColonies' Permissions.
 * <p>
 * Each colony tracks players mapped to ranks, and each rank has a set of allowed actions.
 * The owner rank is always ID 0, officer ID 1, friend ID 2, neutral ID 3, hostile ID 4.
 */
public class ColonyPermissions {

    // ==================== Rank Constants ====================

    public static final int OWNER_RANK_ID = 0;
    public static final int OFFICER_RANK_ID = 1;
    public static final int FRIEND_RANK_ID = 2;
    public static final int NEUTRAL_RANK_ID = 3;
    public static final int HOSTILE_RANK_ID = 4;

    // ==================== Action Enum ====================

    /**
     * Actions that can be permitted or denied per rank.
     */
    public enum Action {
        BUILD(0x1),
        INTERACT(0x2),
        CONTAINER(0x4),
        MANAGE(0x8),
        GUARDS_ATTACK(0x10),
        PLACE_BLOCKS(0x20),
        BREAK_BLOCKS(0x40),
        TELEPORT(0x80),
        EDIT_PERMISSIONS(0x100),
        RECRUIT(0x200),
        RECEIVE_MESSAGES(0x400),
        ACCESS_BUILDINGS(0x800),
        RALLY_GUARDS(0x1000);

        private final int flag;

        Action(int flag) {
            this.flag = flag;
        }

        public int getFlag() {
            return flag;
        }
    }

    // ==================== Rank Class ====================

    /**
     * A named rank with a permission bitmask.
     */
    public static class Rank {
        private final int id;
        private final String name;
        private final boolean isInitial;
        private int permissions;

        public Rank(int id, String name, boolean isInitial) {
            this.id = id;
            this.name = name;
            this.isInitial = isInitial;
            this.permissions = 0;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public boolean isInitial() { return isInitial; }

        public boolean hasPermission(Action action) {
            return (permissions & action.getFlag()) != 0;
        }

        public void addPermission(Action action) {
            permissions |= action.getFlag();
        }

        public void removePermission(Action action) {
            permissions &= ~action.getFlag();
        }

        public int getPermissionFlags() { return permissions; }
        public void setPermissionFlags(int flags) { this.permissions = flags; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Rank r)) return false;
            return id == r.id;
        }

        @Override
        public int hashCode() {
            return id;
        }
    }

    // ==================== ColonyPlayer Record ====================

    /**
     * A player entry in the colony permissions, mapping a UUID + name to a rank.
     */
    public static class ColonyPlayer {
        private final UUID uuid;
        private String name;
        private Rank rank;

        public ColonyPlayer(UUID uuid, String name, Rank rank) {
            this.uuid = uuid;
            this.name = name;
            this.rank = rank;
        }

        public UUID getUuid() { return uuid; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Rank getRank() { return rank; }
        public void setRank(Rank rank) { this.rank = rank; }
    }

    // ==================== Fields ====================

    private final Map<Integer, Rank> ranks = new LinkedHashMap<>();
    private final Map<UUID, ColonyPlayer> players = new HashMap<>();
    private UUID ownerUUID = null;
    private String ownerName = "";
    private boolean dirty = false;

    // ==================== Constructor ====================

    public ColonyPermissions() {
        loadDefaultRanks();
    }

    /**
     * Initialize the five default ranks with sensible permissions.
     * Uses fall-through pattern like MineColonies.
     */
    private void loadDefaultRanks() {
        ranks.clear();
        String[] rankNames = {"Owner", "Officer", "Friend", "Neutral", "Hostile"};
        for (int i = 0; i < rankNames.length; i++) {
            ranks.put(i, new Rank(i, rankNames[i], true));
        }

        // Owner gets everything
        Rank owner = ranks.get(OWNER_RANK_ID);
        for (Action a : Action.values()) {
            owner.addPermission(a);
        }

        // Officer gets most things except EDIT_PERMISSIONS
        Rank officer = ranks.get(OFFICER_RANK_ID);
        for (Action a : Action.values()) {
            if (a != Action.EDIT_PERMISSIONS) {
                officer.addPermission(a);
            }
        }

        // Friend gets interact/container/access/teleport/receive messages
        Rank friend = ranks.get(FRIEND_RANK_ID);
        friend.addPermission(Action.INTERACT);
        friend.addPermission(Action.CONTAINER);
        friend.addPermission(Action.ACCESS_BUILDINGS);
        friend.addPermission(Action.TELEPORT);
        friend.addPermission(Action.RECEIVE_MESSAGES);

        // Neutral gets interact only
        Rank neutral = ranks.get(NEUTRAL_RANK_ID);
        neutral.addPermission(Action.INTERACT);

        // Hostile gets guards attack
        Rank hostile = ranks.get(HOSTILE_RANK_ID);
        hostile.addPermission(Action.GUARDS_ATTACK);
    }

    // ==================== Owner Management ====================

    public void setOwner(@NotNull Player player) {
        this.ownerUUID = player.getUUID();
        this.ownerName = player.getGameProfile().name();
        addPlayer(player.getUUID(), ownerName, getRankOwner());
        dirty = true;
    }

    public void setOwner(@NotNull UUID uuid, @NotNull String name) {
        this.ownerUUID = uuid;
        this.ownerName = name;
        addPlayer(uuid, name, getRankOwner());
        dirty = true;
    }

    @Nullable
    public UUID getOwner() {
        return ownerUUID;
    }

    @NotNull
    public String getOwnerName() {
        return ownerName;
    }

    // ==================== Rank Access ====================

    public Rank getRankOwner() { return ranks.get(OWNER_RANK_ID); }
    public Rank getRankOfficer() { return ranks.get(OFFICER_RANK_ID); }
    public Rank getRankFriend() { return ranks.get(FRIEND_RANK_ID); }
    public Rank getRankNeutral() { return ranks.get(NEUTRAL_RANK_ID); }
    public Rank getRankHostile() { return ranks.get(HOSTILE_RANK_ID); }

    @Nullable
    public Rank getRank(int id) {
        return ranks.get(id);
    }

    public Map<Integer, Rank> getRanks() {
        return Collections.unmodifiableMap(ranks);
    }

    // ==================== Player Management ====================

    public boolean addPlayer(@NotNull UUID uuid, @NotNull String name, @NotNull Rank rank) {
        ColonyPlayer existing = players.get(uuid);
        if (existing != null) {
            existing.setRank(rank);
            existing.setName(name);
        } else {
            players.put(uuid, new ColonyPlayer(uuid, name, rank));
        }
        dirty = true;
        return true;
    }

    public boolean removePlayer(@NotNull UUID uuid) {
        if (uuid.equals(ownerUUID)) return false; // Cannot remove owner
        boolean removed = players.remove(uuid) != null;
        if (removed) dirty = true;
        return removed;
    }

    public boolean setPlayerRank(@NotNull UUID uuid, @NotNull Rank rank) {
        ColonyPlayer cp = players.get(uuid);
        if (cp == null) return false;
        cp.setRank(rank);
        dirty = true;
        return true;
    }

    @NotNull
    public Rank getRank(@NotNull UUID playerUuid) {
        ColonyPlayer cp = players.get(playerUuid);
        return cp != null ? cp.getRank() : getRankNeutral();
    }

    @NotNull
    public Rank getRank(@NotNull Player player) {
        return getRank(player.getUUID());
    }

    public boolean hasPermission(@NotNull Player player, @NotNull Action action) {
        return getRank(player).hasPermission(action);
    }

    public boolean hasPermission(@NotNull Rank rank, @NotNull Action action) {
        return rank.hasPermission(action);
    }

    public boolean isColonyMember(@NotNull Player player) {
        ColonyPlayer cp = players.get(player.getUUID());
        if (cp == null) return false;
        Rank r = cp.getRank();
        return r.getId() == OWNER_RANK_ID || r.getId() == OFFICER_RANK_ID || r.getId() == FRIEND_RANK_ID;
    }

    @NotNull
    public Map<UUID, ColonyPlayer> getPlayers() {
        return Collections.unmodifiableMap(players);
    }

    public Set<ColonyPlayer> getPlayersByRank(@NotNull Rank rank) {
        Set<ColonyPlayer> result = new HashSet<>();
        for (ColonyPlayer cp : players.values()) {
            if (cp.getRank().equals(rank)) {
                result.add(cp);
            }
        }
        return result;
    }

    // ==================== Permission Modification ====================

    public boolean setPermission(@NotNull Rank rank, @NotNull Action action, boolean enable) {
        if (enable) {
            rank.addPermission(action);
        } else {
            rank.removePermission(action);
        }
        dirty = true;
        return true;
    }

    public boolean alterPermission(@NotNull Rank actor, @NotNull Rank rank, @NotNull Action action, boolean enable) {
        // Only owner can change permissions; officers can change friend/neutral/hostile
        if (actor.getId() > OWNER_RANK_ID && rank.getId() <= actor.getId()) {
            return false;
        }
        if (action == Action.EDIT_PERMISSIONS && actor.getId() != OWNER_RANK_ID) {
            return false;
        }
        return setPermission(rank, action, enable);
    }

    // ==================== Dirty Flag ====================

    public boolean isDirty() { return dirty; }
    public void clearDirty() { dirty = false; }
    public void markDirty() { dirty = true; }

    // ==================== NBT Serialization ====================

    private static final String TAG_PLAYERS = "players";
    private static final String TAG_RANKS = "ranks";
    private static final String TAG_OWNER_UUID = "ownerUuid";
    private static final String TAG_OWNER_NAME = "ownerName";
    private static final String TAG_UUID = "uuid";
    private static final String TAG_NAME = "name";
    private static final String TAG_RANK_ID = "rankId";
    private static final String TAG_PERMISSIONS = "permissions";
    private static final String TAG_INITIAL = "initial";

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        // Save ranks
        ListTag rankList = new ListTag();
        for (Rank rank : ranks.values()) {
            CompoundTag rankTag = new CompoundTag();
            rankTag.putInt(TAG_RANK_ID, rank.getId());
            rankTag.putString(TAG_NAME, rank.getName());
            rankTag.putInt(TAG_PERMISSIONS, rank.getPermissionFlags());
            rankTag.putBoolean(TAG_INITIAL, rank.isInitial());
            rankList.add(rankTag);
        }
        tag.put(TAG_RANKS, rankList);

        // Save players
        ListTag playerList = new ListTag();
        for (ColonyPlayer cp : players.values()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putString(TAG_UUID, cp.getUuid().toString());
            playerTag.putString(TAG_NAME, cp.getName());
            playerTag.putInt(TAG_RANK_ID, cp.getRank().getId());
            playerList.add(playerTag);
        }
        tag.put(TAG_PLAYERS, playerList);

        if (ownerUUID != null) {
            tag.putString(TAG_OWNER_UUID, ownerUUID.toString());
            tag.putString(TAG_OWNER_NAME, ownerName);
        }

        return tag;
    }

    public void load(CompoundTag tag) {
        // Load ranks
        if (tag.contains(TAG_RANKS)) {
            ranks.clear();
            ListTag rankList = tag.getListOrEmpty(TAG_RANKS);
            for (int i = 0; i < rankList.size(); i++) {
                if (!(rankList.get(i) instanceof CompoundTag rankTag)) continue;
                int id = rankTag.getIntOr(TAG_RANK_ID, i);
                String name = rankTag.getStringOr(TAG_NAME, "Rank" + id);
                boolean initial = rankTag.getBooleanOr(TAG_INITIAL, true);
                Rank rank = new Rank(id, name, initial);
                rank.setPermissionFlags(rankTag.getIntOr(TAG_PERMISSIONS, 0));
                ranks.put(id, rank);
            }
        }

        // Ensure default ranks exist
        if (!ranks.containsKey(OWNER_RANK_ID)) loadDefaultRanks();

        // Load players
        players.clear();
        if (tag.contains(TAG_PLAYERS)) {
            ListTag playerList = tag.getListOrEmpty(TAG_PLAYERS);
            for (int i = 0; i < playerList.size(); i++) {
                if (!(playerList.get(i) instanceof CompoundTag playerTag)) continue;
                String uuidStr = playerTag.getStringOr(TAG_UUID, "");
                UUID uuid = uuidStr.isEmpty() ? null : UUID.fromString(uuidStr);
                if (uuid == null) continue;
                String name = playerTag.getStringOr(TAG_NAME, "Unknown");
                int rankId = playerTag.getIntOr(TAG_RANK_ID, NEUTRAL_RANK_ID);
                Rank rank = ranks.getOrDefault(rankId, getRankNeutral());
                players.put(uuid, new ColonyPlayer(uuid, name, rank));
            }
        }

        if (tag.contains(TAG_OWNER_UUID)) {
            ownerUUID = UUID.fromString(tag.getStringOr(TAG_OWNER_UUID, ""));
            ownerName = tag.getStringOr(TAG_OWNER_NAME, "");
        }

        dirty = false;
    }
}
