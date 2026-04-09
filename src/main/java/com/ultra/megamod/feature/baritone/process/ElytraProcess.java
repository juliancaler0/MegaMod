package com.ultra.megamod.feature.baritone.process;

import com.ultra.megamod.feature.baritone.goals.GoalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Process: fly using elytra to reach distant goals quickly.
 *
 * - Activated via "elytra <x> <y> <z>" command or auto when goal > 500 blocks away
 * - Equips elytra from inventory if not wearing
 * - Launches: jump + activate elytra flight
 * - Steers toward goal: adjusts pitch/yaw, maintains cruise altitude (~200)
 * - Firework boost every 60 ticks if available
 * - Lands when within 50 blocks of goal
 * - Aborts if health < 6
 */
public class ElytraProcess implements BotProcess {
    private boolean active = false;
    private String status = "Idle";
    private ServerPlayer player;
    private ServerLevel level;

    // Target destination
    private int goalX, goalY, goalZ;

    // Flight state
    private FlightPhase phase = FlightPhase.EQUIP;
    private int ticksInPhase = 0;
    private int totalFlightTicks = 0;
    private int fireworkCooldown = 0;

    // Configuration
    private int cruiseAltitude = 200;
    private static final int FIREWORK_INTERVAL = 60;
    private static final double LAND_DISTANCE_SQ = 50.0 * 50.0;
    private static final double ARRIVE_DISTANCE_SQ = 5.0 * 5.0;
    private static final float MIN_HEALTH_ABORT = 6.0f;
    private static final double BOOST_SPEED = 1.5;
    private static final double STEER_STRENGTH = 0.08;
    private static final int LAUNCH_JUMP_TICKS = 10;
    private static final int LAUNCH_GLIDE_TICKS = 5;

    private enum FlightPhase {
        EQUIP,      // equip elytra if needed
        LAUNCH,     // jump to get airborne
        ACTIVATE,   // activate elytra gliding
        CRUISE,     // fly toward destination
        DESCEND,    // approaching target, descend
        LAND,       // near ground, stop elytra
        DONE,       // arrived
        ABORTED     // safety abort
    }

    public void start(int x, int y, int z, ServerPlayer player, ServerLevel level) {
        this.goalX = x;
        this.goalY = y;
        this.goalZ = z;
        this.player = player;
        this.level = level;
        this.phase = FlightPhase.EQUIP;
        this.ticksInPhase = 0;
        this.totalFlightTicks = 0;
        this.fireworkCooldown = 0;
        this.active = true;
        this.status = "Elytra flight to " + x + ", " + y + ", " + z;
    }

    public void start(int x, int y, int z, ServerPlayer player, ServerLevel level, int altitude) {
        start(x, y, z, player, level);
        this.cruiseAltitude = altitude;
    }

    public void updateState(ServerPlayer player, ServerLevel level) {
        this.player = player;
        this.level = level;
    }

    @Override
    public String name() { return "Elytra"; }

    @Override
    public boolean isActive() { return active; }

    @Override
    public double priority() { return 70; }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean safeToCancel) {
        if (!active || player == null || level == null) return null;

        totalFlightTicks++;
        ticksInPhase++;
        if (fireworkCooldown > 0) fireworkCooldown--;

        // Safety check: abort if health is too low
        if (player.getHealth() < MIN_HEALTH_ABORT && phase != FlightPhase.LAND && phase != FlightPhase.DONE && phase != FlightPhase.ABORTED) {
            phase = FlightPhase.LAND;
            ticksInPhase = 0;
            status = "Elytra ABORTING: low health!";
        }

        double horizontalDistSq = getHorizontalDistanceSq();

        switch (phase) {
            case EQUIP -> {
                if (tryEquipElytra()) {
                    phase = FlightPhase.LAUNCH;
                    ticksInPhase = 0;
                    status = "Elytra: launching...";
                } else {
                    status = "Elytra: no elytra available!";
                    active = false;
                    return null;
                }
            }
            case LAUNCH -> {
                // Jump to get airborne
                if (ticksInPhase == 1) {
                    player.jumpFromGround();
                }
                // Wait a few ticks for the jump apex
                if (ticksInPhase >= LAUNCH_JUMP_TICKS) {
                    phase = FlightPhase.ACTIVATE;
                    ticksInPhase = 0;
                    status = "Elytra: activating glide...";
                }
                // Keep jumping if on ground
                if (ticksInPhase > 3 && player.onGround()) {
                    player.jumpFromGround();
                }
            }
            case ACTIVATE -> {
                // Activate elytra flight
                if (!player.isFallFlying()) {
                    player.startFallFlying();
                }
                if (ticksInPhase >= LAUNCH_GLIDE_TICKS) {
                    if (player.isFallFlying()) {
                        phase = FlightPhase.CRUISE;
                        ticksInPhase = 0;
                        status = "Elytra: cruising to target...";
                    } else {
                        // Failed to activate — retry launch
                        phase = FlightPhase.LAUNCH;
                        ticksInPhase = 0;
                        status = "Elytra: retrying launch...";
                    }
                }
            }
            case CRUISE -> {
                if (!player.isFallFlying()) {
                    // Lost flight — try to relaunch
                    if (player.onGround()) {
                        phase = FlightPhase.LAUNCH;
                        ticksInPhase = 0;
                        status = "Elytra: relaunching...";
                        break;
                    }
                }

                // Steer toward target
                steerToward(goalX, cruiseAltitude, goalZ);

                // Use fireworks for boost
                if (fireworkCooldown <= 0) {
                    tryUseFirework();
                }

                // Check if close enough to start descending
                if (horizontalDistSq < LAND_DISTANCE_SQ) {
                    phase = FlightPhase.DESCEND;
                    ticksInPhase = 0;
                    status = "Elytra: descending to target...";
                }

                status = "Elytra: cruising (dist=" + (int) Math.sqrt(horizontalDistSq) + ", alt=" + (int) player.getY() + ")";
            }
            case DESCEND -> {
                if (!player.isFallFlying()) {
                    // If we stopped flying, check if we're close enough
                    if (horizontalDistSq < ARRIVE_DISTANCE_SQ) {
                        phase = FlightPhase.DONE;
                        ticksInPhase = 0;
                    } else if (player.onGround()) {
                        // Walk the rest
                        phase = FlightPhase.DONE;
                        ticksInPhase = 0;
                    }
                    break;
                }

                // Steer down toward actual target Y
                steerToward(goalX, goalY, goalZ);

                // Check if near ground — stop elytra
                int groundDist = getDistanceToGround();
                if (groundDist <= 3) {
                    // Cancel elytra flight by stopping
                    player.stopFallFlying();
                    phase = FlightPhase.LAND;
                    ticksInPhase = 0;
                    status = "Elytra: landing...";
                }

                // Check arrival
                if (horizontalDistSq < ARRIVE_DISTANCE_SQ) {
                    player.stopFallFlying();
                    phase = FlightPhase.LAND;
                    ticksInPhase = 0;
                }

                status = "Elytra: descending (dist=" + (int) Math.sqrt(horizontalDistSq) + ", ground=" + groundDist + ")";
            }
            case LAND -> {
                // Just wait for landing
                if (player.onGround() || ticksInPhase > 100) {
                    phase = FlightPhase.DONE;
                    ticksInPhase = 0;
                }
                // Dampen movement for softer landing
                Vec3 motion = player.getDeltaMovement();
                player.setDeltaMovement(motion.x * 0.8, Math.max(motion.y, -0.5), motion.z * 0.8);
                player.hurtMarked = true;
                status = "Elytra: landing...";
            }
            case DONE -> {
                status = "Elytra: arrived! (" + totalFlightTicks / 20 + "s flight)";
                active = false;
                return null;
            }
            case ABORTED -> {
                status = "Elytra: aborted (safety)";
                active = false;
                return null;
            }
        }

        // During flight phases we don't use pathfinding — we steer directly
        // But return a goal so the process manager knows what we're targeting
        if (phase == FlightPhase.DONE || phase == FlightPhase.ABORTED) {
            return null;
        }

        // For LAUNCH/EQUIP phases, don't set path goal (we handle movement directly)
        if (phase == FlightPhase.LAUNCH || phase == FlightPhase.ACTIVATE || phase == FlightPhase.EQUIP) {
            return new PathingCommand(new GoalBlock(goalX, goalY, goalZ), PathingCommand.CommandType.REQUEST_PAUSE);
        }

        // For flight phases, signal that we're handling our own movement
        return new PathingCommand(new GoalBlock(goalX, goalY, goalZ), PathingCommand.CommandType.REQUEST_PAUSE);
    }

    /**
     * Steer the player toward a target position by adjusting velocity.
     */
    private void steerToward(int targetX, int targetY, int targetZ) {
        if (player == null) return;

        Vec3 playerPos = player.position();
        double dx = targetX + 0.5 - playerPos.x;
        double dy = targetY + 0.5 - playerPos.y;
        double dz = targetZ + 0.5 - playerPos.z;

        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        if (horizontalDist < 0.1) horizontalDist = 0.1;

        // Calculate desired direction vector (normalized)
        double dirX = dx / horizontalDist;
        double dirZ = dz / horizontalDist;

        // Pitch control: adjust vertical velocity
        double desiredPitch;
        if (dy > 10) {
            // Need to go up — pitch up
            desiredPitch = 0.15;
        } else if (dy < -10) {
            // Need to go down — pitch down
            desiredPitch = -0.2;
        } else {
            // Level flight with slight correction
            desiredPitch = dy * 0.01;
        }

        // Get current velocity and blend toward desired direction
        Vec3 motion = player.getDeltaMovement();
        double speed = motion.horizontalDistance();
        if (speed < 0.5) speed = 0.5;

        double newVx = motion.x + dirX * STEER_STRENGTH;
        double newVy = motion.y + desiredPitch * STEER_STRENGTH;
        double newVz = motion.z + dirZ * STEER_STRENGTH;

        // Clamp vertical speed
        newVy = Math.max(-1.0, Math.min(0.5, newVy));

        // Apply gravity compensation while gliding
        if (player.isFallFlying()) {
            newVy += 0.02; // Slight upward to counteract some gravity
        }

        player.setDeltaMovement(newVx, newVy, newVz);
        player.hurtMarked = true;

        // Set look direction toward target for visual feedback
        float yaw = (float) (Math.atan2(-dx, dz) * (180.0 / Math.PI));
        float pitch = (float) (-Math.atan2(dy, horizontalDist) * (180.0 / Math.PI));
        player.setYRot(yaw);
        player.setXRot(Math.max(-60, Math.min(60, pitch)));
    }

    /**
     * Try to equip elytra from inventory.
     * Returns true if elytra is already equipped or was successfully equipped.
     */
    private boolean tryEquipElytra() {
        if (player == null) return false;

        // Check if already wearing elytra
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() == Items.ELYTRA) return true;

        // Search inventory for elytra
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == Items.ELYTRA) {
                // Swap with current chestplate
                player.getInventory().setItem(i, chest);
                player.setItemSlot(EquipmentSlot.CHEST, stack);
                return true;
            }
        }
        return false;
    }

    /**
     * Try to use a firework rocket for speed boost.
     */
    private void tryUseFirework() {
        if (player == null || !player.isFallFlying()) return;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == Items.FIREWORK_ROCKET) {
                // Consume one firework and apply boost
                stack.shrink(1);
                if (stack.isEmpty()) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }

                // Apply boost in the direction of travel
                Vec3 look = player.getLookAngle();
                Vec3 motion = player.getDeltaMovement();
                player.setDeltaMovement(
                    motion.x + look.x * BOOST_SPEED,
                    motion.y + look.y * 0.5,
                    motion.z + look.z * BOOST_SPEED
                );
                player.hurtMarked = true;

                fireworkCooldown = FIREWORK_INTERVAL;
                return;
            }
        }
    }

    /**
     * Get the horizontal distance squared to the goal.
     */
    private double getHorizontalDistanceSq() {
        double dx = player.getX() - goalX;
        double dz = player.getZ() - goalZ;
        return dx * dx + dz * dz;
    }

    /**
     * Get the vertical distance to the ground below the player.
     */
    private int getDistanceToGround() {
        BlockPos pos = player.blockPosition();
        for (int dy = 0; dy < 64; dy++) {
            BlockPos check = pos.below(dy);
            BlockState state = level.getBlockState(check);
            if (!state.isAir() && !state.liquid()) {
                return dy;
            }
        }
        return 64;
    }

    public int getGoalX() { return goalX; }
    public int getGoalY() { return goalY; }
    public int getGoalZ() { return goalZ; }
    public int getTotalFlightTicks() { return totalFlightTicks; }

    @Override
    public void onLostControl() {
        // If we lose control mid-flight, try to land safely
        if (player != null && player.isFallFlying()) {
            player.stopFallFlying();
        }
    }

    @Override
    public void cancel() {
        if (player != null && player.isFallFlying()) {
            player.stopFallFlying();
        }
        active = false;
        status = "Elytra flight cancelled";
    }

    @Override
    public String getStatus() { return status; }
}
