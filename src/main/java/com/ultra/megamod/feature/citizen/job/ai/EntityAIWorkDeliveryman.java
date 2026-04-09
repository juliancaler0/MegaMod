package com.ultra.megamod.feature.citizen.job.ai;

import com.ultra.megamod.feature.citizen.colony.Colony;
import com.ultra.megamod.feature.citizen.colony.ColonyBuildingManager;
import com.ultra.megamod.feature.citizen.job.AIWorkerState;
import com.ultra.megamod.feature.citizen.job.JobDeliveryman;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Deliveryman AI state machine. Implements the delivery workflow:
 * <ol>
 *   <li>Go to warehouse/base building</li>
 *   <li>Check for delivery tasks in the queue</li>
 *   <li>Pick up items from the source</li>
 *   <li>Walk to the destination</li>
 *   <li>Drop off items</li>
 *   <li>Return to warehouse</li>
 * </ol>
 * <p>
 * Ported from MineColonies' EntityAIWorkDeliveryman, simplified for MegaMod.
 * Since MegaMod doesn't have a full request system yet, the deliveryman
 * currently patrols between colony buildings, checking for items to move.
 */
public class EntityAIWorkDeliveryman extends AbstractEntityAIBasic {

    // ==================== Constants ====================

    /** Wait time when no deliveries are pending. */
    private static final int IDLE_WAIT = 100; // 5 seconds

    /** Standard delay between actions. */
    private static final int ACTION_DELAY = 10;

    // ==================== Fields ====================

    private final JobDeliveryman deliveryJob;

    /** Current delivery phase. */
    private DeliveryPhase phase = DeliveryPhase.IDLE;

    /** Target position for the current delivery step. */
    @Nullable
    private BlockPos deliveryTarget = null;

    /** Index for patrol route through buildings. */
    private int patrolIndex = 0;

    private enum DeliveryPhase {
        IDLE,
        PICKING_UP,
        DELIVERING,
        RETURNING
    }

    // ==================== Constructor ====================

    public EntityAIWorkDeliveryman(@NotNull JobDeliveryman job) {
        super(job);
        this.deliveryJob = job;

        // Register deliveryman-specific state handlers
        registerTarget(AIWorkerState.DELIVERY_PREPARE, this::prepareDelivery);
        registerTarget(AIWorkerState.DELIVERY_DELIVER, this::deliver);
        registerTarget(AIWorkerState.DELIVERY_PICKUP, this::pickup);
        registerTarget(AIWorkerState.DELIVERY_DUMPING, this::dump);
    }

    // ==================== Override Base Handlers ====================

    @Override
    protected AIWorkerState handleStartWorking() {
        if (!walkToBuilding()) {
            return AIWorkerState.START_WORKING;
        }
        return AIWorkerState.DECIDE;
    }

    @Override
    protected AIWorkerState handleDecide() {
        // Check for pending delivery tasks
        if (deliveryJob.hasDeliveries()) {
            return AIWorkerState.DELIVERY_PREPARE;
        }

        // If inventory has items, dump them first
        if (getOccupiedSlotCount() > 0) {
            return AIWorkerState.DELIVERY_DUMPING;
        }

        // No tasks - patrol colony buildings
        return patrolBuildings();
    }

    // ==================== Deliveryman State Handlers ====================

    /**
     * DELIVERY_PREPARE: Pick up items from the source.
     */
    private AIWorkerState prepareDelivery() {
        JobDeliveryman.DeliveryTask task = deliveryJob.getCurrentTask();
        if (task == null) {
            task = deliveryJob.pollNextTask();
        }

        if (task == null) {
            return AIWorkerState.DECIDE;
        }

        // Walk to the source
        if (!walkToBlock(task.getSource())) {
            return AIWorkerState.DELIVERY_PREPARE;
        }

        // At source - "pick up" items (simplified - in a real implementation
        // this would interact with a chest/warehouse)
        worker.swing(InteractionHand.MAIN_HAND);
        setDelay(ACTION_DELAY);

        // Proceed to delivery
        deliveryTarget = task.getDestination();
        return AIWorkerState.DELIVERY_DELIVER;
    }

    /**
     * DELIVERY_DELIVER: Walk to the destination and drop off items.
     */
    private AIWorkerState deliver() {
        if (deliveryTarget == null) {
            deliveryJob.completeCurrentTask();
            return AIWorkerState.DECIDE;
        }

        if (!walkToBlock(deliveryTarget)) {
            return AIWorkerState.DELIVERY_DELIVER;
        }

        // At destination - "deliver" items
        worker.swing(InteractionHand.MAIN_HAND);

        // Mark delivery as complete
        deliveryJob.completeCurrentTask();
        deliveryTarget = null;

        // Grant XP
        worker.addRawXp(1.0);

        setDelay(ACTION_DELAY);

        // Check for more deliveries
        if (deliveryJob.hasDeliveries()) {
            return AIWorkerState.DELIVERY_PREPARE;
        }

        return AIWorkerState.DELIVERY_DUMPING;
    }

    /**
     * DELIVERY_PICKUP: Pick up items from a building (part of patrol).
     */
    private AIWorkerState pickup() {
        // Simplified: just return to decide
        setDelay(ACTION_DELAY);
        return AIWorkerState.DECIDE;
    }

    /**
     * DELIVERY_DUMPING: Dump excess inventory at the warehouse/building.
     */
    private AIWorkerState dump() {
        if (!walkToBuilding()) {
            return AIWorkerState.DELIVERY_DUMPING;
        }

        dumpInventory();
        job.clearActionsDone();
        setDelay(ACTION_DELAY);
        return AIWorkerState.DECIDE;
    }

    // ==================== Patrol Logic ====================

    /**
     * When there are no deliveries, the deliveryman patrols between
     * colony buildings, looking for buildings that might need pickups.
     */
    private AIWorkerState patrolBuildings() {
        Colony colony = getColony();
        if (colony == null) {
            setDelay(IDLE_WAIT);
            return AIWorkerState.IDLE;
        }

        Map<BlockPos, ColonyBuildingManager.BuildingEntry> buildings =
                colony.getBuildingManager().getBuildings();

        if (buildings.isEmpty()) {
            setDelay(IDLE_WAIT);
            return AIWorkerState.IDLE;
        }

        // Get building positions as a list
        BlockPos[] positions = buildings.keySet().toArray(new BlockPos[0]);
        if (positions.length == 0) {
            setDelay(IDLE_WAIT);
            return AIWorkerState.IDLE;
        }

        // Cycle to next building
        patrolIndex = (patrolIndex + 1) % positions.length;
        BlockPos target = positions[patrolIndex];

        // Walk to the building
        if (!walkToBlock(target)) {
            setDelay(ACTION_DELAY);
            return AIWorkerState.DECIDE;
        }

        // "Check" the building (simplified - in a full implementation
        // this would check for pickup requests)
        worker.swing(InteractionHand.MAIN_HAND);
        setDelay(TICKS_SECOND * 5);
        return AIWorkerState.DECIDE;
    }
}
