package com.ultra.megamod.feature.citizen.quest;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * Tracks per-quest-instance objective progress for event-driven objectives
 * like block placement, block breaking, entity killing, and building upgrades.
 * <p>
 * This is the MegaMod equivalent of MineColonies' {@code IObjectiveInstance}
 * implementations (BlockPlacementProgressInstance, BlockMiningProgressInstance, etc.).
 */
public class QuestObjectiveInstance {

    /**
     * The type of objective being tracked.
     */
    public enum ObjectiveType {
        DIALOGUE,
        DELIVERY,
        PLACE_BLOCK,
        BREAK_BLOCK,
        KILL_ENTITY,
        BUILD_BUILDING
    }

    private final ObjectiveType type;
    private int currentProgress;
    private int requiredProgress;

    /** For delivery objectives: the item ID being delivered. */
    private String itemId;
    /** For block objectives: the block ID being tracked. */
    private String blockId;
    /** For entity objectives: the entity type ID being tracked. */
    private String entityTypeId;
    /** For building objectives: the building type being tracked. */
    private String buildingType;
    /** For building objectives: the level required. */
    private int requiredLevel;
    /** For building objectives: whether existing buildings count. */
    private boolean countExisting;
    /** The next objective index to advance to when fulfilled (-1 = complete). */
    private int nextObjective;

    public QuestObjectiveInstance(ObjectiveType type, int requiredProgress) {
        this.type = type;
        this.currentProgress = 0;
        this.requiredProgress = requiredProgress;
        this.nextObjective = -1;
    }

    // ==================== Getters / Setters ====================

    public ObjectiveType getType() { return type; }
    public int getCurrentProgress() { return currentProgress; }
    public int getRequiredProgress() { return requiredProgress; }
    public boolean isFulfilled() { return currentProgress >= requiredProgress; }
    public int getMissingQuantity() { return Math.max(0, requiredProgress - currentProgress); }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getBlockId() { return blockId; }
    public void setBlockId(String blockId) { this.blockId = blockId; }

    public String getEntityTypeId() { return entityTypeId; }
    public void setEntityTypeId(String entityTypeId) { this.entityTypeId = entityTypeId; }

    public String getBuildingType() { return buildingType; }
    public void setBuildingType(String buildingType) { this.buildingType = buildingType; }

    public int getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(int requiredLevel) { this.requiredLevel = requiredLevel; }

    public boolean isCountExisting() { return countExisting; }
    public void setCountExisting(boolean countExisting) { this.countExisting = countExisting; }

    public int getNextObjective() { return nextObjective; }
    public void setNextObjective(int nextObjective) { this.nextObjective = nextObjective; }

    /**
     * Increments progress by 1.
     *
     * @return true if the objective is now fulfilled
     */
    public boolean incrementProgress() {
        currentProgress++;
        return isFulfilled();
    }

    /**
     * Sets progress to a specific value.
     */
    public void setCurrentProgress(int progress) {
        this.currentProgress = progress;
    }

    // ==================== NBT Persistence ====================

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", type.name());
        tag.putInt("progress", currentProgress);
        tag.putInt("required", requiredProgress);
        tag.putInt("nextObj", nextObjective);
        if (itemId != null) tag.putString("itemId", itemId);
        if (blockId != null) tag.putString("blockId", blockId);
        if (entityTypeId != null) tag.putString("entityTypeId", entityTypeId);
        if (buildingType != null) tag.putString("buildingType", buildingType);
        tag.putInt("reqLevel", requiredLevel);
        tag.putBoolean("countExisting", countExisting);
        return tag;
    }

    public static QuestObjectiveInstance load(CompoundTag tag) {
        ObjectiveType type;
        try {
            type = ObjectiveType.valueOf(tag.getStringOr("type", "DIALOGUE"));
        } catch (IllegalArgumentException e) {
            type = ObjectiveType.DIALOGUE;
        }
        int required = tag.getIntOr("required", 1);
        QuestObjectiveInstance instance = new QuestObjectiveInstance(type, required);
        instance.currentProgress = tag.getIntOr("progress", 0);
        instance.nextObjective = tag.getIntOr("nextObj", -1);

        String item = tag.getStringOr("itemId", "");
        if (!item.isEmpty()) instance.itemId = item;

        String block = tag.getStringOr("blockId", "");
        if (!block.isEmpty()) instance.blockId = block;

        String entity = tag.getStringOr("entityTypeId", "");
        if (!entity.isEmpty()) instance.entityTypeId = entity;

        String building = tag.getStringOr("buildingType", "");
        if (!building.isEmpty()) instance.buildingType = building;

        instance.requiredLevel = tag.getIntOr("reqLevel", 0);
        instance.countExisting = tag.getBooleanOr("countExisting", false);
        return instance;
    }
}
