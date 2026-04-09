package com.ultra.megamod.feature.citizen.data;

import com.ultra.megamod.feature.citizen.colony.Colony;
import com.ultra.megamod.feature.citizen.colony.ColonyManager;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

/**
 * Compatibility wrapper that delegates to {@link ColonyManager}.
 * <p>
 * All 55+ files that import FactionManager continue to compile unchanged.
 * Every method here is a thin delegate to ColonyManager, which is the real
 * implementation. Over time, callers should migrate to using ColonyManager directly.
 */
public final class FactionManager {

    private final ColonyManager delegate;

    private FactionManager(ColonyManager delegate) {
        this.delegate = delegate;
    }

    // ==================== Singleton ====================

    /**
     * Gets (or lazily creates) the FactionManager singleton.
     * Delegates to ColonyManager.get().
     */
    public static FactionManager get(@NotNull ServerLevel level) {
        return new FactionManager(ColonyManager.get(level));
    }

    /**
     * Resets the singleton. Delegates to ColonyManager.reset().
     */
    public static void reset() {
        ColonyManager.reset();
    }

    // ==================== Colony Creation ====================

    @Nullable
    public FactionData createFaction(@NotNull String factionId, @NotNull String displayName, @NotNull UUID ownerUuid) {
        Colony colony = delegate.createFaction(factionId, displayName, ownerUuid);
        return colony != null ? new FactionData(colony) : null;
    }

    @Nullable
    public FactionData createFaction(@NotNull String factionId, @NotNull String displayName,
                                     @NotNull UUID ownerUuid, int cost) {
        Colony colony = delegate.createFaction(factionId, displayName, ownerUuid, cost);
        return colony != null ? new FactionData(colony) : null;
    }

    // ==================== Colony Deletion ====================

    public boolean deleteFaction(@NotNull String factionId) {
        return delegate.deleteFaction(factionId);
    }

    // ==================== Lookup ====================

    @Nullable
    public FactionData getFaction(@NotNull String factionId) {
        Colony colony = delegate.getFaction(factionId);
        return colony != null ? new FactionData(colony) : null;
    }

    @Nullable
    public String getPlayerFaction(@NotNull UUID playerUuid) {
        return delegate.getPlayerFaction(playerUuid);
    }

    @Nullable
    public FactionData getPlayerFactionData(@NotNull UUID playerUuid) {
        Colony colony = delegate.getPlayerFactionData(playerUuid);
        return colony != null ? new FactionData(colony) : null;
    }

    @NotNull
    public Collection<FactionData> getAllFactions() {
        return delegate.getAllFactions().stream()
                .map(FactionData::new)
                .toList();
    }

    public int getFactionCount() {
        return delegate.getFactionCount();
    }

    // ==================== Membership ====================

    public boolean joinFaction(@NotNull UUID playerUuid, @NotNull String factionId) {
        return delegate.joinFaction(playerUuid, factionId);
    }

    public boolean leaveFaction(@NotNull UUID playerUuid) {
        return delegate.leaveFaction(playerUuid);
    }

    public boolean requestJoin(@NotNull UUID playerUuid, @NotNull String factionId) {
        return delegate.requestJoin(playerUuid, factionId);
    }

    public boolean acceptJoinRequest(@NotNull String factionId, @NotNull UUID playerUuid) {
        return delegate.acceptJoinRequest(factionId, playerUuid);
    }

    // ==================== Dirty Flag ====================

    public void markDirty() {
        delegate.markDirty();
    }

    // ==================== Bans ====================

    private static final java.util.Set<UUID> bannedPlayers = new java.util.HashSet<>();

    public void banPlayer(@NotNull UUID playerUuid) {
        bannedPlayers.add(playerUuid);
    }

    public void unbanPlayer(@NotNull UUID playerUuid) {
        bannedPlayers.remove(playerUuid);
    }

    @NotNull
    public java.util.Set<UUID> getBannedPlayers() {
        return java.util.Collections.unmodifiableSet(bannedPlayers);
    }

    // ==================== Persistence ====================

    public void saveToDisk(@NotNull ServerLevel level) {
        delegate.saveToDisk(level);
    }

    public void loadFromDisk(@NotNull ServerLevel level) {
        delegate.loadFromDisk(level);
    }
}
