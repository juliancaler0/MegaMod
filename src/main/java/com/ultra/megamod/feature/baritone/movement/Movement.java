package com.ultra.megamod.feature.baritone.movement;

import com.ultra.megamod.feature.baritone.pathfinding.BetterBlockPos;
import com.ultra.megamod.feature.baritone.pathfinding.CalculationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

/**
 * Abstract base class for all movement types.
 * Uses velocity-based movement (setDeltaMovement + hurtMarked) for smooth
 * interpolation on the client. Falls back to teleport only for large corrections.
 */
public abstract class Movement {
    protected final BetterBlockPos src;
    protected final BetterBlockPos dest;
    protected final MovementState state = new MovementState();

    protected Movement(BetterBlockPos src, BetterBlockPos dest) {
        this.src = src;
        this.dest = dest;
    }

    public BetterBlockPos getSrc() { return src; }
    public BetterBlockPos getDest() { return dest; }

    /** Calculate the cost of this movement (for A* planning). COST_INF = impossible. */
    public abstract double calculateCost(CalculationContext ctx);

    /** Execute one tick of this movement on the server player. Returns movement status. */
    public abstract MovementState.Status tick(ServerPlayer player, ServerLevel level);

    // === Server-side movement helpers ===

    /**
     * Move the player towards a target position using smooth velocity.
     * Sets deltaMovement so the client interpolates naturally instead of
     * snapping via teleport each tick.
     */
    protected void moveTowards(ServerPlayer player, double tx, double ty, double tz, double speed) {
        Vec3 current = player.position();
        double dx = tx - current.x;
        double dy = ty - current.y;
        double dz = tz - current.z;
        double horizDist = Math.sqrt(dx * dx + dz * dz);

        if (horizDist < 0.05 && Math.abs(dy) < 0.1) {
            // Very close — snap via teleport to avoid oscillation
            teleportPlayer(player, tx, ty, tz);
            player.setDeltaMovement(Vec3.ZERO);
            player.hurtMarked = true;
            return;
        }

        // Calculate velocity toward target, capped at speed
        double factor = Math.min(speed / Math.max(horizDist, 0.01), 1.0);
        double vx = dx * factor;
        double vz = dz * factor;

        // Y velocity: smoothly approach target Y
        double vy;
        if (Math.abs(dy) < 0.1) {
            vy = 0;
        } else if (Math.abs(dy) < 1.5) {
            vy = dy * 0.25; // Gentle vertical correction
        } else {
            vy = Math.signum(dy) * 0.2;
        }

        // Preserve existing Y velocity for gravity/jumping unless we need to override
        Vec3 currentVel = player.getDeltaMovement();
        if (dy < -0.5 && currentVel.y < -0.1) {
            // Falling — let gravity handle Y but still guide X/Z
            vy = currentVel.y;
        }

        player.setDeltaMovement(new Vec3(vx, vy, vz));
        player.hurtMarked = true; // Force the server to sync velocity to client
    }

    /** Teleport player reliably using connection.teleport — used sparingly for corrections */
    protected void teleportPlayer(ServerPlayer player, double x, double y, double z) {
        player.teleportTo((ServerLevel) player.level(), x, y, z,
            Set.of(), player.getYRot(), player.getXRot(), false);
    }

    /** Jump the player using velocity instead of teleport */
    protected void jump(ServerPlayer player) {
        if (player.onGround()) {
            Vec3 vel = player.getDeltaMovement();
            player.setDeltaMovement(new Vec3(vel.x, 0.42, vel.z)); // 0.42 = vanilla jump velocity
            player.hurtMarked = true;
        }
    }

    /** Break a block on the server */
    protected void breakBlock(ServerLevel level, ServerPlayer player, BlockPos pos) {
        if (!level.getBlockState(pos).isAir()) {
            level.destroyBlock(pos, true, player);
        }
    }

    /** Place a block on the server */
    protected void placeBlock(ServerLevel level, BlockPos pos, net.minecraft.world.level.block.state.BlockState blockState) {
        level.setBlockAndUpdate(pos, blockState);
    }

    /** Check if the player has arrived at a position (feet within tolerance) */
    protected boolean isAtPosition(ServerPlayer player, BetterBlockPos pos) {
        double dx = player.getX() - (pos.x + 0.5);
        double dz = player.getZ() - (pos.z + 0.5);
        double dy = player.getY() - pos.y;
        return dx * dx + dz * dz < 0.25 && Math.abs(dy) < 1.0;
    }

    /** Center position for a block (feet pos) */
    protected double centerX() { return dest.x + 0.5; }
    protected double centerY() { return dest.y; }
    protected double centerZ() { return dest.z + 0.5; }
}
