package com.ultra.megamod.feature.citizen.colony;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Client-side read-only view of a colony, synced from server via packets.
 * <p>
 * Same fields as Colony but populated from network data rather than NBT on disk.
 * All mutation methods are no-ops or absent -- clients only read.
 * <p>
 * Modeled after MineColonies' ColonyView.
 */
public class ColonyView implements IColony {

    private final int id;
    @NotNull
    private String factionId;
    @NotNull
    private String displayName;
    @Nullable
    private UUID leaderUuid;
    @Nullable
    private BlockPos center;
    @Nullable
    private ResourceKey<Level> dimension;
    private int memberCount;
    private double happiness = 5.0;
    private int day = 0;
    private boolean raidersEnabled = true;
    private boolean abandoned = false;
    private int maxNPCs = 4;
    private boolean underAttack = false;

    @NotNull
    private final ColonyPermissions permissions = new ColonyPermissions();
    @NotNull
    private final ColonyBuildingManager buildingManager = new ColonyBuildingManager();
    @NotNull
    private final ColonyCitizenManager citizenManager = new ColonyCitizenManager();

    // ==================== Constructor ====================

    public ColonyView(int id) {
        this.id = id;
        this.factionId = "";
        this.displayName = "";
    }

    /**
     * Creates a ColonyView from a CompoundTag received over the network.
     */
    public static ColonyView fromNetwork(CompoundTag tag) {
        int id = tag.getIntOr("id", 0);
        ColonyView view = new ColonyView(id);
        view.handleUpdate(tag);
        return view;
    }

    /**
     * Updates this view with data from the server.
     */
    public void handleUpdate(CompoundTag tag) {
        factionId = tag.getStringOr("factionId", "");
        displayName = tag.getStringOr("displayName", "");
        memberCount = tag.getIntOr("memberCount", 0);
        happiness = tag.getDoubleOr("happiness", 5.0);
        day = tag.getIntOr("day", 0);
        raidersEnabled = tag.getBooleanOr("raidersEnabled", true);
        abandoned = tag.getBooleanOr("abandoned", false);
        maxNPCs = tag.getIntOr("maxNPCs", 4);
        underAttack = tag.getBooleanOr("underAttack", false);

        if (tag.contains("leaderUuid")) {
            leaderUuid = UUID.fromString(tag.getStringOr("leaderUuid", ""));
        }

        if (tag.getBooleanOr("hasCenter", false)) {
            center = new BlockPos(
                    tag.getIntOr("centerX", 0),
                    tag.getIntOr("centerY", 0),
                    tag.getIntOr("centerZ", 0)
            );
        }

        if (tag.contains("dimension")) {
            String dimStr = tag.getStringOr("dimension", "");
            if (!dimStr.isEmpty()) {
                dimension = ResourceKey.create(
                        net.minecraft.core.registries.Registries.DIMENSION,
                        net.minecraft.resources.Identifier.parse(dimStr)
                );
            }
        }

        if (tag.contains("permissions")) {
            permissions.load(tag.getCompoundOrEmpty("permissions"));
        }
        if (tag.contains("buildings")) {
            buildingManager.load(tag.getCompoundOrEmpty("buildings"));
        }
        if (tag.contains("citizens")) {
            citizenManager.load(tag.getCompoundOrEmpty("citizens"));
        }
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
        // Client-side: no-op, synced from server
    }

    @Override
    @Nullable
    public BlockPos getCenter() { return center; }

    @Override
    @Nullable
    public UUID getLeaderUuid() { return leaderUuid; }

    @Override
    public int getMemberCount() { return memberCount; }

    @Override
    @Nullable
    public ResourceKey<Level> getDimension() { return dimension; }

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
    public boolean isRemote() { return true; }

    @Override
    public boolean isMember(@NotNull UUID playerUuid) {
        return permissions.getPlayers().containsKey(playerUuid);
    }

    @Override
    public double getOverallHappiness() { return happiness; }

    @Override
    public int getDay() { return day; }

    @Override
    public boolean isRaidersEnabled() { return raidersEnabled; }

    @Override
    public boolean isAbandoned() { return abandoned; }

    @Override
    public int getMaxNPCs() { return maxNPCs; }

    @Override
    public void markDirty() {
        // Client-side: no-op
    }

    public boolean isUnderAttack() { return underAttack; }

    /**
     * Creates a CompoundTag suitable for network sync (server -> client).
     * Called on the server Colony to produce the data for ColonyView.
     */
    public static CompoundTag createSyncTag(Colony colony) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("id", colony.getId());
        tag.putString("factionId", colony.getFactionId());
        tag.putString("displayName", colony.getDisplayName());
        tag.putInt("memberCount", colony.getMemberCount());
        tag.putDouble("happiness", colony.getOverallHappiness());
        tag.putInt("day", colony.getDay());
        tag.putBoolean("raidersEnabled", colony.isRaidersEnabled());
        tag.putBoolean("abandoned", colony.isAbandoned());
        tag.putInt("maxNPCs", colony.getMaxNPCs());
        tag.putBoolean("underAttack", colony.isUnderAttack());

        if (colony.getLeaderUuid() != null) {
            tag.putString("leaderUuid", colony.getLeaderUuid().toString());
        }

        BlockPos center = colony.getCenter();
        if (center != null) {
            tag.putBoolean("hasCenter", true);
            tag.putInt("centerX", center.getX());
            tag.putInt("centerY", center.getY());
            tag.putInt("centerZ", center.getZ());
        } else {
            tag.putBoolean("hasCenter", false);
        }

        if (colony.getDimension() != null) {
            tag.putString("dimension", colony.getDimension().identifier().toString());
        }

        tag.put("permissions", colony.getPermissions().save());
        tag.put("buildings", colony.getBuildingManager().save());
        tag.put("citizens", colony.getCitizenManager().save());

        return tag;
    }
}
