package com.ultra.megamod.feature.citizen.data;

import com.ultra.megamod.feature.citizen.colony.Colony;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Compatibility wrapper around {@link Colony} that provides the old FactionData API.
 * <p>
 * All existing code that uses FactionData methods (getFactionId, getDisplayName,
 * getLeaderUuid, getMemberCount, getTownChestPos, hasTownChest, isMember,
 * setMemberRank, etc.) continues to work unchanged.
 * <p>
 * Mutations go directly through to the underlying Colony object, so changes
 * are immediately visible to ColonyManager and persist correctly.
 */
public class FactionData {

    /**
     * Player ranks, matching the old FactionData.PlayerRank enum.
     * Delegates to Colony.PlayerRank.
     */
    public enum PlayerRank {
        LEADER,
        OFFICER,
        MEMBER;

        /**
         * Converts to the Colony.PlayerRank equivalent.
         */
        public Colony.PlayerRank toColonyRank() {
            return switch (this) {
                case LEADER -> Colony.PlayerRank.LEADER;
                case OFFICER -> Colony.PlayerRank.OFFICER;
                case MEMBER -> Colony.PlayerRank.MEMBER;
            };
        }

        /**
         * Converts from Colony.PlayerRank.
         */
        @NotNull
        public static PlayerRank fromColonyRank(@Nullable Colony.PlayerRank rank) {
            if (rank == null) return MEMBER;
            return switch (rank) {
                case LEADER -> LEADER;
                case OFFICER -> OFFICER;
                case MEMBER -> MEMBER;
            };
        }
    }

    // ==================== Delegate ====================

    private final Colony colony;

    public FactionData(@NotNull Colony colony) {
        this.colony = colony;
    }

    /**
     * Access the underlying Colony object for cases where direct access is needed.
     */
    @NotNull
    public Colony getColony() {
        return colony;
    }

    // ==================== Getters (matching old FactionData API) ====================

    @NotNull
    public String getFactionId() {
        return colony.getFactionId();
    }

    @NotNull
    public String getDisplayName() {
        return colony.getDisplayName();
    }

    public void setDisplayName(@NotNull String name) {
        colony.setDisplayName(name);
    }

    @Nullable
    public UUID getLeaderUuid() {
        return colony.getLeaderUuid();
    }

    public void setLeaderUuid(@Nullable UUID uuid) {
        colony.setLeaderUuid(uuid);
    }

    public int getMemberCount() {
        return colony.getMemberCount();
    }

    /** Returns all member UUIDs for this faction. */
    public java.util.Set<java.util.UUID> getMembers() {
        return colony.getMemberUuids();
    }

    @Nullable
    public BlockPos getTownChestPos() {
        return colony.getTownChestPos();
    }

    public void setTownChestPos(@NotNull BlockPos pos) {
        colony.setTownChestPos(pos);
    }

    public boolean hasTownChest() {
        return colony.hasTownChest();
    }

    public boolean isMember(@NotNull UUID uuid) {
        return colony.isMember(uuid);
    }

    public int getMaxNPCs() {
        return colony.getMaxNPCs();
    }

    public void setMaxNPCs(int max) {
        colony.setMaxNPCs(max);
    }

    public boolean isAbandoned() {
        return colony.isAbandoned();
    }

    public void setAbandoned(boolean abandoned) {
        colony.setAbandoned(abandoned);
    }

    public boolean isRaidersEnabled() {
        return colony.isRaidersEnabled();
    }

    public void setRaidersEnabled(boolean enabled) {
        colony.setRaidersEnabled(enabled);
    }

    // ==================== Member Rank Management ====================

    public void setMemberRank(@NotNull UUID uuid, @NotNull PlayerRank rank) {
        colony.setMemberRank(uuid, rank.toColonyRank());
    }

    @NotNull
    public PlayerRank getMemberRank(@NotNull UUID uuid) {
        Colony.PlayerRank rank = colony.getMemberRank(uuid);
        return PlayerRank.fromColonyRank(rank);
    }

    // ==================== Aliases for ColonySyncHandler compat ====================

    @Nullable
    public UUID getOwnerId() {
        return colony.getLeaderUuid();
    }

    @NotNull
    public String getName() {
        return colony.getDisplayName();
    }

    public void setName(@NotNull String name) {
        colony.setDisplayName(name);
    }

    @Nullable
    public String getStyle() {
        // Style is stored as a colony property; return a default if not set
        return "colonial";
    }

    public void setStyle(@Nullable String style) {
        // TODO: Add style field to Colony when needed
    }

    public void setSetting(@NotNull String key, boolean value) {
        // TODO: Add settings map to Colony when needed
    }

    // ==================== Colony State ====================

    public double getOverallHappiness() {
        return colony.getOverallHappiness();
    }

    public int getDay() {
        return colony.getDay();
    }

    public boolean isUnderAttack() {
        return colony.isUnderAttack();
    }

    @Override
    public String toString() {
        return "FactionData{colony=" + colony + "}";
    }
}
