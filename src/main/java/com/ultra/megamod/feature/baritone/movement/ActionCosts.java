package com.ultra.megamod.feature.baritone.movement;

/**
 * Cost constants for pathfinding movement types.
 * Adapted from Baritone — lower is better/faster.
 */
public final class ActionCosts {
    private ActionCosts() {}

    /** Ticks to walk one block at normal speed */
    public static final double WALK_ONE_BLOCK_COST = 20.0 / 4.317;
    /** Ticks to sprint one block */
    public static final double SPRINT_ONE_BLOCK_COST = 20.0 / 5.612;
    /** Multiplier for walking on a block diagonally (sqrt(2)) */
    public static final double SQRT_2 = Math.sqrt(2);
    /** Cost to walk one block diagonally */
    public static final double WALK_ONE_DIAGONAL_COST = WALK_ONE_BLOCK_COST * SQRT_2;
    /** Cost to sprint one block diagonally */
    public static final double SPRINT_ONE_DIAGONAL_COST = SPRINT_ONE_BLOCK_COST * SQRT_2;
    /** Cost of jumping (in addition to horizontal cost) */
    public static final double JUMP_PENALTY = 2.0;
    /** Cost of falling per block */
    public static final double FALL_ONE_BLOCK_COST = WALK_ONE_BLOCK_COST * 0.8;
    /** Cost per block of fall distance after safe threshold */
    public static final double FALL_DAMAGE_PENALTY = 50.0;
    /** Max safe fall distance (3 blocks = no damage) */
    public static final int MAX_SAFE_FALL = 3;
    /** Max fall distance we ever attempt (with water bucket) */
    public static final int MAX_FALL = 256;
    /** Penalty for breaking a block (base, scaled by hardness) */
    public static final double BREAK_BLOCK_BASE_COST = 4.0;
    /** Penalty for placing a block */
    public static final double PLACE_BLOCK_COST = 3.0;
    /** Penalty for moving through water */
    public static final double WATER_WALK_PENALTY = 3.0;
    /** Cost multiplier for sneaking */
    public static final double SNEAK_MULTIPLIER = 3.0;
    /** Really high cost to mark impossible moves */
    public static final double COST_INF = 1_000_000.0;

    // === New costs for expanded movement system ===

    /** Cost to swim one block horizontally in water */
    public static final double SWIM_ONE_BLOCK_COST = WALK_ONE_BLOCK_COST * 2.2;
    /** Cost to climb one block up/down a ladder or vine */
    public static final double CLIMB_ONE_BLOCK_COST = WALK_ONE_BLOCK_COST * 1.8;
    /** Cost to swim one block diagonally */
    public static final double SWIM_ONE_DIAGONAL_COST = SWIM_ONE_BLOCK_COST * SQRT_2;
    /** Extra penalty for entering water (getting wet) */
    public static final double WATER_ENTRY_PENALTY = 2.0;
}
