package com.ultra.megamod.feature.citizen.job;

/**
 * All possible AI states for citizen worker state machines.
 * Ported from MineColonies' AIWorkerState enum, adapted for MegaMod.
 * <p>
 * Each state has an {@code okayToEat} flag indicating whether the citizen
 * can be interrupted (e.g., to eat) while in that state.
 */
public enum AIWorkerState {

    // ==================== General ====================

    /** Initial idle state. AI starts here after assignment. */
    IDLE(true),

    /** Initialization check: verify building, tools, etc. */
    INIT(true),

    /** Inventory is full, need to dump items at workplace. */
    INVENTORY_FULL(false),

    /** Preparing tools and materials before starting work. */
    PREPARING(true),

    /** Walk to the workplace building to begin work. */
    START_WORKING(true),

    /** Waiting for needed items to be delivered. */
    NEEDS_ITEM(true),

    /** Main decision node: figure out what to do next. */
    DECIDE(true),

    /** Paused / free time (not working). */
    PAUSED(true),

    /** Walking to workplace building. */
    WALKING_TO_WORKPLACE(true),

    /** Walking to a specific target block. */
    WALKING_TO_TARGET(true),

    /** Depositing items at the workplace chest. */
    DEPOSITING(false),

    /** Gathering required materials from storage. */
    GATHERING_REQUIRED_MATERIALS(true),

    // ==================== Miner ====================

    /** Check if a mineshaft exists. */
    MINER_CHECK_MINESHAFT(true),

    /** Walk to the ladder entrance. */
    MINER_WALKING_TO_LADDER(true),

    /** Repair or extend the ladder. */
    MINER_REPAIRING_LADDER(true),

    /** Mine the main shaft downward. */
    MINER_MINING_SHAFT(false),

    /** Build/reinforce the shaft structure. */
    MINER_BUILDING_SHAFT(false),

    /** Mine a branch tunnel node. */
    MINER_MINING_NODE(false),

    // ==================== Farmer ====================

    /** Hoe the farmland. */
    FARMER_HOE(true),

    /** Plant seeds on prepared farmland. */
    FARMER_PLANT(true),

    /** Harvest mature crops. */
    FARMER_HARVEST(true),

    /** Walk to the assigned farm field. */
    FARMER_WALKING_TO_FIELD(true),

    /** Check field status and decide what to do. */
    FARMER_CHECK_FIELD(true),

    // ==================== Builder ====================

    /** Load the structure schematic. */
    BUILDER_LOAD_STRUCTURE(false),

    /** Place blocks from the blueprint. */
    BUILDER_BUILDING_STEP(false),

    /** Complete the build and clean up. */
    BUILDER_COMPLETE_BUILD(false),

    /** Pick up materials from storage. */
    BUILDER_PICK_UP(false),

    /** Request missing materials. */
    BUILDER_REQUEST_MATERIALS(true),

    // ==================== Deliveryman ====================

    /** Prepare the delivery (pick items from warehouse). */
    DELIVERY_PREPARE(true),

    /** Deliver items to the requesting building. */
    DELIVERY_DELIVER(true),

    /** Pick up items from a building. */
    DELIVERY_PICKUP(true),

    /** Dump excess inventory at warehouse. */
    DELIVERY_DUMPING(false),

    // ==================== Fisherman ====================

    /** Search for a body of water. */
    FISHERMAN_SEARCHING_WATER(true),

    /** Walk to the found water spot. */
    FISHERMAN_WALKING_TO_WATER(true),

    /** Check the water location is valid. */
    FISHERMAN_CHECK_WATER(true),

    /** Cast line and fish. */
    FISHERMAN_START_FISHING(false),

    // ==================== Lumberjack ====================

    /** Search for trees to chop. */
    LUMBERJACK_SEARCHING_TREE(true),

    /** Chop down a tree. */
    LUMBERJACK_CHOP_TREE(false),

    /** Gather saplings and replant. */
    LUMBERJACK_GATHERING(true);

    private final boolean okayToEat;

    AIWorkerState(boolean okayToEat) {
        this.okayToEat = okayToEat;
    }

    /**
     * Whether the citizen can be interrupted to eat while in this state.
     *
     * @return true if interruptible for eating
     */
    public boolean isOkayToEat() {
        return okayToEat;
    }
}
