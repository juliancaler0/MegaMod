package com.ultra.megamod.feature.citizen.building.workorder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * Enhanced work order for colony buildings. Tracks a single build/upgrade/repair/decoration/removal
 * task that a Builder citizen will carry out.
 */
public class ColonyWorkOrder {

    private final UUID orderId;
    private final UUID colonyId;
    private final String buildingId;
    private final BlockPos position;
    private final WorkOrderType type;
    private final int targetLevel;
    private final String style;
    private final String blueprintPath;
    private int priority;
    private UUID assignedBuilderId;
    private boolean claimed;
    private final long createdTick;
    private boolean adminBypass;
    private int speedMultiplier = 1;

    /** Materials required for this work order (item registry name → count needed). */
    private final Map<String, Integer> requiredResources = new LinkedHashMap<>();
    /** Materials already delivered to the builder for this work order. */
    private final Map<String, Integer> deliveredResources = new LinkedHashMap<>();

    private ColonyWorkOrder(UUID orderId, UUID colonyId, String buildingId, BlockPos position,
                            WorkOrderType type, int targetLevel, String style, String blueprintPath,
                            int priority, UUID assignedBuilderId, boolean claimed, long createdTick) {
        this.orderId = orderId;
        this.colonyId = colonyId;
        this.buildingId = buildingId;
        this.position = position;
        this.type = type;
        this.targetLevel = targetLevel;
        this.style = style;
        this.blueprintPath = blueprintPath;
        this.priority = priority;
        this.assignedBuilderId = assignedBuilderId;
        this.claimed = claimed;
        this.createdTick = createdTick;
    }

    // ========== Factory Methods ==========

    /**
     * Creates a new BUILD work order for constructing a building from scratch.
     */
    public static ColonyWorkOrder createBuild(UUID colonyId, String buildingId, BlockPos position,
                                              int targetLevel, String style, String blueprintPath,
                                              int priority, long createdTick) {
        return new ColonyWorkOrder(
                UUID.randomUUID(), colonyId, buildingId, position,
                WorkOrderType.BUILD, targetLevel, style, blueprintPath,
                priority, null, false, createdTick
        );
    }

    /**
     * Creates a new UPGRADE work order for upgrading an existing building to a higher level.
     */
    public static ColonyWorkOrder createUpgrade(UUID colonyId, String buildingId, BlockPos position,
                                                int targetLevel, String style, String blueprintPath,
                                                int priority, long createdTick) {
        return new ColonyWorkOrder(
                UUID.randomUUID(), colonyId, buildingId, position,
                WorkOrderType.UPGRADE, targetLevel, style, blueprintPath,
                priority, null, false, createdTick
        );
    }

    /**
     * Creates a new REPAIR work order for repairing a damaged building.
     */
    public static ColonyWorkOrder createRepair(UUID colonyId, String buildingId, BlockPos position,
                                               int currentLevel, String style, String blueprintPath,
                                               int priority, long createdTick) {
        return new ColonyWorkOrder(
                UUID.randomUUID(), colonyId, buildingId, position,
                WorkOrderType.REPAIR, currentLevel, style, blueprintPath,
                priority, null, false, createdTick
        );
    }

    // ========== Getters ==========

    public UUID getOrderId() { return orderId; }
    public UUID getColonyId() { return colonyId; }
    public String getBuildingId() { return buildingId; }
    public BlockPos getPosition() { return position; }
    public WorkOrderType getType() { return type; }
    public int getTargetLevel() { return targetLevel; }
    public String getStyle() { return style; }
    public String getBlueprintPath() { return blueprintPath; }
    public int getPriority() { return priority; }
    public UUID getAssignedBuilderId() { return assignedBuilderId; }
    public boolean isClaimed() { return claimed; }
    public long getCreatedTick() { return createdTick; }
    public boolean isAdminBypass() { return adminBypass; }
    public int getSpeedMultiplier() { return speedMultiplier; }

    // ========== Setters ==========

    public void setPriority(int priority) { this.priority = priority; }
    public void setAdminBypass(boolean bypass) { this.adminBypass = bypass; }
    public void setSpeedMultiplier(int multiplier) { this.speedMultiplier = multiplier; }

    /**
     * Claims this work order for the given builder. Marks it as claimed.
     */
    public void claim(UUID builderId) {
        this.assignedBuilderId = builderId;
        this.claimed = true;
    }

    /**
     * Unclaims this work order, freeing it for another builder.
     */
    public void unclaim() {
        this.assignedBuilderId = null;
        this.claimed = false;
    }

    // ========== Resource Tracking ==========

    /**
     * Adds a required resource for this work order.
     */
    public void addRequiredResource(Item item, int count) {
        String key = BuiltInRegistries.ITEM.getKey(item).toString();
        requiredResources.merge(key, count, Integer::sum);
    }

    /**
     * Adds a required resource by registry name.
     */
    public void addRequiredResource(String itemId, int count) {
        requiredResources.merge(itemId, count, Integer::sum);
    }

    /**
     * Marks items as delivered for this work order.
     */
    public void addDeliveredResource(Item item, int count) {
        String key = BuiltInRegistries.ITEM.getKey(item).toString();
        deliveredResources.merge(key, count, Integer::sum);
    }

    /**
     * Marks items as delivered by registry name.
     */
    public void addDeliveredResource(String itemId, int count) {
        deliveredResources.merge(itemId, count, Integer::sum);
    }

    /**
     * Sets the full required resources map (replaces existing).
     */
    public void setRequiredResources(Map<String, Integer> resources) {
        requiredResources.clear();
        requiredResources.putAll(resources);
    }

    /**
     * Returns an unmodifiable view of required resources (item registry name → count).
     */
    public Map<String, Integer> getRequiredResources() {
        return Collections.unmodifiableMap(requiredResources);
    }

    /**
     * Returns an unmodifiable view of delivered resources (item registry name → count).
     */
    public Map<String, Integer> getDeliveredResources() {
        return Collections.unmodifiableMap(deliveredResources);
    }

    /**
     * Returns how many of the given item are still needed (required - delivered).
     */
    public int getStillNeeded(String itemId) {
        int required = requiredResources.getOrDefault(itemId, 0);
        int delivered = deliveredResources.getOrDefault(itemId, 0);
        return Math.max(0, required - delivered);
    }

    /**
     * Returns the display name for an item by its registry ID.
     */
    public static String getItemDisplayName(String itemId) {
        try {
            Identifier id = Identifier.parse(itemId);
            Item item = BuiltInRegistries.ITEM.getValue(id);
            return new ItemStack(item).getHoverName().getString();
        } catch (Exception e) {
            return itemId;
        }
    }

    // ========== NBT Persistence ==========

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("orderId", orderId.toString());
        tag.putString("colonyId", colonyId.toString());
        tag.putString("buildingId", buildingId);
        tag.putInt("posX", position.getX());
        tag.putInt("posY", position.getY());
        tag.putInt("posZ", position.getZ());
        tag.putString("type", type.name());
        tag.putInt("targetLevel", targetLevel);
        tag.putString("style", style != null ? style : "");
        tag.putString("blueprintPath", blueprintPath != null ? blueprintPath : "");
        tag.putInt("priority", priority);
        tag.putBoolean("claimed", claimed);
        tag.putLong("createdTick", createdTick);
        if (assignedBuilderId != null) {
            tag.putString("assignedBuilderId", assignedBuilderId.toString());
        }
        tag.putBoolean("adminBypass", adminBypass);
        tag.putInt("speedMultiplier", speedMultiplier);
        // Save resource tracking
        if (!requiredResources.isEmpty()) {
            CompoundTag reqRes = new CompoundTag();
            for (Map.Entry<String, Integer> entry : requiredResources.entrySet()) {
                reqRes.putInt(entry.getKey(), entry.getValue());
            }
            tag.put("requiredResources", (Tag) reqRes);
        }
        if (!deliveredResources.isEmpty()) {
            CompoundTag delRes = new CompoundTag();
            for (Map.Entry<String, Integer> entry : deliveredResources.entrySet()) {
                delRes.putInt(entry.getKey(), entry.getValue());
            }
            tag.put("deliveredResources", (Tag) delRes);
        }
        return tag;
    }

    public static ColonyWorkOrder load(CompoundTag tag) {
        UUID orderId = UUID.fromString(tag.getStringOr("orderId", UUID.randomUUID().toString()));
        UUID colonyId = UUID.fromString(tag.getStringOr("colonyId", UUID.randomUUID().toString()));
        String buildingId = tag.getStringOr("buildingId", "");
        BlockPos position = new BlockPos(
                tag.getIntOr("posX", 0),
                tag.getIntOr("posY", 0),
                tag.getIntOr("posZ", 0)
        );
        WorkOrderType type;
        try {
            type = WorkOrderType.valueOf(tag.getStringOr("type", "BUILD"));
        } catch (IllegalArgumentException e) {
            type = WorkOrderType.BUILD;
        }
        int targetLevel = tag.getIntOr("targetLevel", 1);
        String style = tag.getStringOr("style", "");
        String blueprintPath = tag.getStringOr("blueprintPath", "");
        int priority = tag.getIntOr("priority", 0);
        boolean claimed = tag.getBooleanOr("claimed", false);
        long createdTick = tag.getLongOr("createdTick", 0L);
        UUID assignedBuilderId = null;
        if (tag.contains("assignedBuilderId")) {
            try {
                assignedBuilderId = UUID.fromString(tag.getStringOr("assignedBuilderId", ""));
            } catch (IllegalArgumentException ignored) {}
        }

        ColonyWorkOrder order = new ColonyWorkOrder(
                orderId, colonyId, buildingId, position,
                type, targetLevel, style, blueprintPath,
                priority, assignedBuilderId, claimed, createdTick
        );
        order.adminBypass = tag.getBooleanOr("adminBypass", false);
        order.speedMultiplier = tag.getIntOr("speedMultiplier", 1);
        // Load resource tracking
        if (tag.contains("requiredResources")) {
            CompoundTag reqRes = tag.getCompoundOrEmpty("requiredResources");
            for (String key : reqRes.keySet()) {
                order.requiredResources.put(key, reqRes.getIntOr(key, 0));
            }
        }
        if (tag.contains("deliveredResources")) {
            CompoundTag delRes = tag.getCompoundOrEmpty("deliveredResources");
            for (String key : delRes.keySet()) {
                order.deliveredResources.put(key, delRes.getIntOr(key, 0));
            }
        }
        return order;
    }

    @Override
    public String toString() {
        return "ColonyWorkOrder[id=%s, type=%s, building=%s, level=%d, claimed=%s]"
                .formatted(orderId, type, buildingId, targetLevel, claimed);
    }
}
