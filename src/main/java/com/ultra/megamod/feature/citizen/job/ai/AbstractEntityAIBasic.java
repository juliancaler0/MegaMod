package com.ultra.megamod.feature.citizen.job.ai;

import com.ultra.megamod.feature.citizen.colony.Colony;
import com.ultra.megamod.feature.citizen.colony.ColonyBuildingManager;
import com.ultra.megamod.feature.citizen.colony.ColonyManager;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import com.ultra.megamod.feature.citizen.job.AIWorkerState;
import com.ultra.megamod.feature.citizen.job.IJob;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

/**
 * Base state machine AI for all citizen workers.
 * <p>
 * Ported from MineColonies' AbstractAISkeleton + AbstractEntityAIBasic,
 * simplified into a single class for MegaMod. Uses a {@code Map<AIWorkerState, Supplier<AIWorkerState>>}
 * for state transitions. Each tick, the current state's handler is called, and the
 * returned state becomes the new current state.
 * <p>
 * Subclasses register their state handlers via {@link #registerTarget(AIWorkerState, Supplier)}
 * and implement job-specific work logic in those handlers.
 * <p>
 * The base class provides common behaviors:
 * <ul>
 *   <li>Walking to the workplace building</li>
 *   <li>Inventory management (checking fullness, dumping items)</li>
 *   <li>Delay/cooldown system between actions</li>
 *   <li>Block breaking and placing helpers</li>
 * </ul>
 */
public abstract class AbstractEntityAIBasic {

    // ==================== Constants ====================

    /** Standard delay between work actions (in ticks). */
    protected static final int STANDARD_DELAY = 5;

    /** One second in ticks. */
    protected static final int TICKS_SECOND = 20;

    /** How close the citizen needs to be to a target position to count as "arrived". */
    protected static final double WALK_CLOSE_ENOUGH = 2.5;

    /** Distance at which we consider the citizen "at" the building. */
    protected static final double AT_BUILDING_DISTANCE = 3.0;

    /** Maximum items in inventory before we force a dump. */
    protected static final int MAX_INVENTORY_ITEMS_BEFORE_DUMP = 20;

    /** The tick rate at which this AI is updated (every N game ticks). */
    protected static final int AI_TICK_RATE = 5;

    // ==================== Fields ====================

    /** The job this AI belongs to. */
    @NotNull
    protected final IJob job;

    /** The citizen entity. */
    @NotNull
    protected final MCEntityCitizen worker;

    /** Convenience reference to the world. */
    @NotNull
    protected final Level world;

    /** Current AI state. */
    @NotNull
    private AIWorkerState state;

    /** Map of state -> handler function. Each handler returns the next state. */
    private final Map<AIWorkerState, Supplier<AIWorkerState>> stateHandlers = new LinkedHashMap<>();

    /** Delay counter (in ticks). When > 0, the AI waits instead of executing. */
    private int delay = 0;

    /** The block the citizen is currently working at. */
    @Nullable
    protected BlockPos currentWorkingLocation = null;

    /** Counter for consecutive ticks in the same state (prevents infinite loops). */
    private int stateTickCounter = 0;

    /** Maximum ticks in one state before forcing a reset. */
    private static final int MAX_TICKS_IN_STATE = 600; // 30 seconds at AI_TICK_RATE=5

    /** Tick counter to throttle AI execution. */
    private int tickCounter = 0;

    // ==================== Constructor ====================

    /**
     * Create a new AI for the given job.
     * Registers default state handlers for IDLE, START_WORKING, INVENTORY_FULL, and DECIDE.
     *
     * @param job the job this AI services
     */
    protected AbstractEntityAIBasic(@NotNull IJob job) {
        this.job = job;
        this.worker = job.getCitizen();
        this.world = worker.level();
        this.state = AIWorkerState.IDLE;

        // Register default state handlers
        registerTarget(AIWorkerState.IDLE, this::handleIdle);
        registerTarget(AIWorkerState.INIT, this::handleInit);
        registerTarget(AIWorkerState.START_WORKING, this::handleStartWorking);
        registerTarget(AIWorkerState.INVENTORY_FULL, this::handleInventoryFull);
        registerTarget(AIWorkerState.DECIDE, this::handleDecide);
        registerTarget(AIWorkerState.DEPOSITING, this::handleDepositing);
        registerTarget(AIWorkerState.WALKING_TO_WORKPLACE, this::handleWalkToWorkplace);
    }

    // ==================== State Registration ====================

    /**
     * Register a state handler. When the AI is in the given state, the handler
     * will be called each AI tick and should return the next state.
     *
     * @param targetState the state to handle
     * @param handler     function that returns the next state
     */
    protected void registerTarget(@NotNull AIWorkerState targetState, @NotNull Supplier<AIWorkerState> handler) {
        stateHandlers.put(targetState, handler);
    }

    // ==================== Tick ====================

    /**
     * Called every game tick from the job. Throttles execution to every
     * {@link #AI_TICK_RATE} ticks, then evaluates the current state handler.
     */
    public void tick() {
        tickCounter++;
        if (tickCounter < AI_TICK_RATE) {
            return;
        }
        tickCounter = 0;

        // Handle delay
        if (delay > 0) {
            delay -= AI_TICK_RATE;
            return;
        }

        // Safety check: don't run AI on client side
        if (world.isClientSide()) {
            return;
        }

        // Check if worker is alive
        if (!worker.isAlive()) {
            return;
        }

        // Execute the current state handler
        try {
            Supplier<AIWorkerState> handler = stateHandlers.get(state);
            if (handler != null) {
                AIWorkerState newState = handler.get();
                if (newState != null && newState != state) {
                    state = newState;
                    stateTickCounter = 0;
                } else {
                    stateTickCounter++;
                    // Safety: if stuck in one state too long, reset
                    if (stateTickCounter > MAX_TICKS_IN_STATE) {
                        state = AIWorkerState.IDLE;
                        stateTickCounter = 0;
                    }
                }
            } else {
                // No handler for this state - fall back to IDLE
                state = AIWorkerState.IDLE;
                stateTickCounter = 0;
            }
        } catch (Exception e) {
            // On exception, reset to IDLE to prevent permanent breakage
            state = AIWorkerState.IDLE;
            stateTickCounter = 0;
        }
    }

    // ==================== Default State Handlers ====================

    /**
     * IDLE: Transition to START_WORKING.
     */
    protected AIWorkerState handleIdle() {
        return AIWorkerState.START_WORKING;
    }

    /**
     * INIT: Check basics and transition to START_WORKING.
     */
    protected AIWorkerState handleInit() {
        if (job.getBuildingPos() == null) {
            setDelay(TICKS_SECOND * 5);
            return AIWorkerState.IDLE;
        }
        return AIWorkerState.START_WORKING;
    }

    /**
     * START_WORKING: Walk to the workplace building, then transition to DECIDE.
     * Subclasses can override to add preparation logic.
     */
    protected AIWorkerState handleStartWorking() {
        BlockPos buildingPos = job.getBuildingPos();
        if (buildingPos == null) {
            setDelay(TICKS_SECOND * 5);
            return AIWorkerState.IDLE;
        }

        if (!walkToBlock(buildingPos)) {
            return AIWorkerState.START_WORKING;
        }

        return AIWorkerState.DECIDE;
    }

    /**
     * DECIDE: Main decision point. Subclasses should override this to route
     * to their specific work states. Default checks inventory and goes to IDLE.
     */
    protected AIWorkerState handleDecide() {
        if (isInventoryFull()) {
            return AIWorkerState.INVENTORY_FULL;
        }
        return AIWorkerState.IDLE;
    }

    /**
     * INVENTORY_FULL: Walk to building and dump inventory.
     */
    protected AIWorkerState handleInventoryFull() {
        BlockPos buildingPos = job.getBuildingPos();
        if (buildingPos == null) {
            return AIWorkerState.IDLE;
        }

        if (!walkToBlock(buildingPos)) {
            return AIWorkerState.INVENTORY_FULL;
        }

        return AIWorkerState.DEPOSITING;
    }

    /**
     * DEPOSITING: Dump items from citizen inventory into the workplace chest
     * (or drop on the ground if no chest is available).
     */
    protected AIWorkerState handleDepositing() {
        dumpInventory();
        job.clearActionsDone();
        return AIWorkerState.START_WORKING;
    }

    /**
     * WALKING_TO_WORKPLACE: Walk to the building.
     */
    protected AIWorkerState handleWalkToWorkplace() {
        BlockPos buildingPos = job.getBuildingPos();
        if (buildingPos == null) {
            return AIWorkerState.IDLE;
        }
        if (!walkToBlock(buildingPos)) {
            return AIWorkerState.WALKING_TO_WORKPLACE;
        }
        return AIWorkerState.DECIDE;
    }

    // ==================== Navigation Helpers ====================

    /**
     * Try to walk the citizen to the given position.
     *
     * @param pos the target position
     * @return true if the citizen is close enough to the target
     */
    protected boolean walkToBlock(@NotNull BlockPos pos) {
        if (worker.blockPosition().closerThan(pos, WALK_CLOSE_ENOUGH)) {
            return true;
        }

        worker.getNavigation().moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 1.0);
        return false;
    }

    /**
     * Walk to the workplace building.
     *
     * @return true if the citizen has arrived
     */
    protected boolean walkToBuilding() {
        BlockPos buildingPos = job.getBuildingPos();
        if (buildingPos == null) {
            return false;
        }
        return walkToBlock(buildingPos);
    }

    /**
     * Check if the citizen is near the given position.
     *
     * @param pos      the position to check
     * @param distance the maximum distance
     * @return true if within distance
     */
    protected boolean isNear(@NotNull BlockPos pos, double distance) {
        return worker.blockPosition().closerThan(pos, distance);
    }

    // ==================== Inventory Helpers ====================

    /**
     * Check if the citizen's inventory is full (no empty slots).
     *
     * @return true if inventory is full
     */
    protected boolean isInventoryFull() {
        SimpleContainer inv = worker.getCitizenInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the citizen has a specific item in their inventory.
     *
     * @param stack the item to look for (matches item type, ignores count)
     * @return true if found
     */
    protected boolean hasItemInInventory(@NotNull ItemStack stack) {
        SimpleContainer inv = worker.getCitizenInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (ItemStack.isSameItem(inv.getItem(i), stack)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find a slot containing the given item type.
     *
     * @param stack the item to look for
     * @return the slot index, or -1 if not found
     */
    protected int findItemInInventory(@NotNull ItemStack stack) {
        SimpleContainer inv = worker.getCitizenInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (ItemStack.isSameItem(inv.getItem(i), stack)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Count how many of a given item the citizen has.
     *
     * @param stack the item to count (matches item type)
     * @return total count
     */
    protected int countItemInInventory(@NotNull ItemStack stack) {
        SimpleContainer inv = worker.getCitizenInventory();
        int count = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack slotStack = inv.getItem(i);
            if (ItemStack.isSameItem(slotStack, stack)) {
                count += slotStack.getCount();
            }
        }
        return count;
    }

    /**
     * Add an item to the citizen's inventory.
     *
     * @param stack the item to add
     * @return the remainder that didn't fit (empty if all fit)
     */
    protected ItemStack addToInventory(@NotNull ItemStack stack) {
        SimpleContainer inv = worker.getCitizenInventory();
        return inv.addItem(stack);
    }

    /**
     * Dump all items from the citizen inventory. In a real colony system this
     * would go into the building's chests; here we drop on the ground at the
     * building pos for simplicity (players/deliverymen can pick up).
     */
    protected void dumpInventory() {
        SimpleContainer inv = worker.getCitizenInventory();
        if (world instanceof ServerLevel serverLevel) {
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (!stack.isEmpty()) {
                    worker.spawnAtLocation(serverLevel, stack.copy());
                    inv.setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }

    /**
     * Get the number of non-empty slots in the citizen's inventory.
     *
     * @return count of occupied slots
     */
    protected int getOccupiedSlotCount() {
        SimpleContainer inv = worker.getCitizenInventory();
        int count = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (!inv.getItem(i).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    // ==================== Block Interaction Helpers ====================

    /**
     * Mine/break a block at the given position. Drops items into the citizen's
     * inventory or on the ground if inventory is full.
     *
     * @param pos the block position to break
     * @return true if the block was broken
     */
    protected boolean mineBlock(@NotNull BlockPos pos) {
        if (!(world instanceof ServerLevel serverLevel)) {
            return false;
        }

        BlockState blockState = world.getBlockState(pos);
        if (blockState.isAir()) {
            return true;
        }

        // Get drops
        List<ItemStack> drops = Block.getDrops(blockState, serverLevel,
                pos, world.getBlockEntity(pos));

        // Break the block
        world.destroyBlock(pos, false);

        // Add drops to inventory
        for (ItemStack drop : drops) {
            ItemStack remainder = addToInventory(drop);
            if (!remainder.isEmpty()) {
                worker.spawnAtLocation(serverLevel, remainder);
            }
        }

        // Swing arm for visual feedback
        worker.swing(InteractionHand.MAIN_HAND);

        // Increment actions
        job.incrementActionsDone();

        return true;
    }

    /**
     * Place a block at the given position.
     *
     * @param pos   the position to place at
     * @param state the block state to place
     * @return true if placed successfully
     */
    protected boolean placeBlock(@NotNull BlockPos pos, @NotNull BlockState state) {
        if (world.isClientSide()) {
            return false;
        }

        if (!world.getBlockState(pos).isAir() && !world.getBlockState(pos).canBeReplaced()) {
            return false;
        }

        world.setBlock(pos, state, Block.UPDATE_ALL);
        worker.swing(InteractionHand.MAIN_HAND);
        job.incrementActionsDone();
        return true;
    }

    // ==================== Delay / Timing ====================

    /**
     * Set a delay before the next AI tick executes.
     *
     * @param ticks number of ticks to wait
     */
    protected void setDelay(int ticks) {
        this.delay = ticks;
    }

    // ==================== State Access ====================

    /**
     * Get the current AI state.
     *
     * @return the current state
     */
    @NotNull
    public AIWorkerState getState() {
        return state;
    }

    /**
     * Force-set the AI state. Use sparingly.
     *
     * @param newState the new state
     */
    protected void setState(@NotNull AIWorkerState newState) {
        this.state = newState;
        this.stateTickCounter = 0;
    }

    /**
     * Reset the AI back to IDLE.
     */
    public void resetAI() {
        this.state = AIWorkerState.IDLE;
        this.stateTickCounter = 0;
        this.delay = 0;
        this.currentWorkingLocation = null;
    }

    /**
     * Called when the AI is being removed (job change, death, etc.).
     * Subclasses can override to clean up resources.
     */
    public void onRemoval() {
        resetAI();
    }

    // ==================== Colony / Building Access ====================

    /**
     * Try to get the colony this citizen belongs to.
     *
     * @return the colony, or null if not found
     */
    @Nullable
    protected Colony getColony() {
        int colonyId = worker.getCitizenColonyHandler().getColonyId();
        if (colonyId <= 0 || !(world instanceof ServerLevel serverLevel)) {
            return null;
        }
        return ColonyManager.get(serverLevel).getColonyById(colonyId);
    }

    /**
     * Try to get the building entry for this job.
     *
     * @return the building entry, or null if not found
     */
    @Nullable
    protected ColonyBuildingManager.BuildingEntry getBuildingEntry() {
        Colony colony = getColony();
        if (colony == null || job.getBuildingPos() == null) {
            return null;
        }
        return colony.getBuildingManager().getBuilding(job.getBuildingPos());
    }

    /**
     * Get the building level for the workplace.
     *
     * @return the building level, or 0 if not found
     */
    protected int getBuildingLevel() {
        ColonyBuildingManager.BuildingEntry entry = getBuildingEntry();
        return entry != null ? entry.getLevel() : 0;
    }
}
