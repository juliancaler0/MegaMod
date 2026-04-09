package com.ultra.megamod.feature.baritone.process;

import com.ultra.megamod.feature.baritone.goals.Goal;
import com.ultra.megamod.feature.baritone.goals.GoalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

/**
 * Process: dig tunnels in a specified direction with configurable width/height.
 * Optionally places torches for lighting.
 */
public class TunnelProcess implements BotProcess {
    private boolean active = false;
    private String status = "Idle";
    private Direction direction;
    private int length;
    private int width;
    private int height;
    private BlockPos origin;
    private ServerLevel level;
    private int currentStep = 0;
    private int blocksBroken = 0;
    private boolean placeTorches = true;
    private static final int TORCH_INTERVAL = 8;
    private Consumer<BlockPos> blockBreakListener;

    public void start(Direction direction, int length, int width, int height, BlockPos origin, ServerLevel level) {
        this.direction = direction;
        this.length = length;
        this.width = Math.max(1, Math.min(width, 5));
        this.height = Math.max(2, Math.min(height, 5));
        this.origin = origin;
        this.level = level;
        this.currentStep = 0;
        this.blocksBroken = 0;
        this.active = true;
        this.status = "Tunneling " + direction.getName() + " (" + length + " blocks, " + this.width + "x" + this.height + ")";
    }

    @Override
    public String name() { return "Tunnel"; }

    @Override
    public boolean isActive() { return active; }

    @Override
    public double priority() { return 65; }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean safeToCancel) {
        if (!active || level == null) return null;

        if (currentStep >= length) {
            status = "Tunnel complete! (" + blocksBroken + " blocks broken)";
            active = false;
            return null;
        }

        // Calculate the front-center of the current tunnel step
        BlockPos frontCenter = getFrontPosition(currentStep);

        // Break all blocks in the current cross-section
        boolean allClear = true;
        for (int w = -(width / 2); w <= (width - 1) / 2; w++) {
            for (int h = 0; h < height; h++) {
                BlockPos breakPos = getOffsetPos(frontCenter, w, h);
                BlockState state = level.getBlockState(breakPos);
                if (!state.isAir() && !state.liquid()) {
                    level.destroyBlock(breakPos, true);
                    blocksBroken++;
                    if (blockBreakListener != null) blockBreakListener.accept(breakPos.immutable());
                    allClear = false;
                }
            }
        }

        // Place torch periodically
        if (placeTorches && currentStep > 0 && currentStep % TORCH_INTERVAL == 0) {
            BlockPos torchPos = frontCenter;
            BlockPos torchFloor = torchPos.below();
            if (level.getBlockState(torchPos).isAir() && !level.getBlockState(torchFloor).isAir()) {
                level.setBlockAndUpdate(torchPos, Blocks.TORCH.defaultBlockState());
            }
        }

        // Place floor under our feet if it's air (safety)
        BlockPos floorPos = frontCenter.below();
        if (level.getBlockState(floorPos).isAir() || level.getBlockState(floorPos).liquid()) {
            level.setBlockAndUpdate(floorPos, Blocks.COBBLESTONE.defaultBlockState());
        }

        if (allClear) {
            currentStep++;
        }

        status = "Tunneling " + direction.getName() + " (" + currentStep + "/" + length + ", " + blocksBroken + " broken)";

        // Navigate to the front of the tunnel
        BlockPos navTarget = getFrontPosition(currentStep);
        Goal goal = new GoalBlock(navTarget.getX(), navTarget.getY(), navTarget.getZ());
        return new PathingCommand(goal, PathingCommand.CommandType.SET_GOAL_AND_PATH);
    }

    private BlockPos getFrontPosition(int step) {
        return origin.relative(direction, step);
    }

    private BlockPos getOffsetPos(BlockPos center, int widthOffset, int heightOffset) {
        // Width is perpendicular to direction
        Direction perpendicular = direction.getClockWise();
        return center.relative(perpendicular, widthOffset).above(heightOffset);
    }

    public void onBlockBroken() {
        blocksBroken++;
    }

    public void setBlockBreakListener(Consumer<BlockPos> listener) { this.blockBreakListener = listener; }
    public int getBlocksBroken() { return blocksBroken; }
    public int getCurrentStep() { return currentStep; }
    public int getLength() { return length; }

    @Override
    public void onLostControl() {}

    @Override
    public void cancel() {
        active = false;
        status = "Tunnel stopped (" + currentStep + "/" + length + ", " + blocksBroken + " broken)";
    }

    @Override
    public String getStatus() { return status; }
}
