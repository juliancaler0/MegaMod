package com.ultra.megamod.feature.adminmodules.modules.world;

import com.ultra.megamod.feature.adminmodules.AdminModule;
import com.ultra.megamod.feature.adminmodules.ModuleCategory;
import com.ultra.megamod.feature.adminmodules.ModuleSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import java.util.*;

public class VeinMinerModule extends AdminModule {
    private ModuleSetting.IntSetting maxBlocks;
    private final Set<BlockPos> pending = new HashSet<>();
    // Track the last block we queued a vein for, to avoid re-flooding on repeated BreakSpeed events
    private BlockPos lastQueuedPos = null;

    public VeinMinerModule() {
        super("vein_miner", "VeinMiner", "Breaks connected ore veins", ModuleCategory.WORLD);
    }

    @Override
    protected void initSettings() {
        maxBlocks = integer("Max Blocks", 32, 1, 128, "Max blocks to break per vein");
    }

    @Override
    public void onServerTick(ServerPlayer player, ServerLevel level) {
        if (pending.isEmpty()) return;
        int count = 0;
        Iterator<BlockPos> it = pending.iterator();
        while (it.hasNext() && count < 8) {
            BlockPos pos = it.next();
            it.remove();
            if (!level.getBlockState(pos).isAir()) {
                level.destroyBlock(pos, true);
                count++;
            }
        }
        // Reset the last queued position when the vein is fully processed
        if (pending.isEmpty()) {
            lastQueuedPos = null;
        }
    }

    @Override
    public void onBreakSpeed(ServerPlayer player, PlayerEvent.BreakSpeed event) {
        BlockPos pos = event.getPosition().orElse(null);
        if (pos == null) return;
        // BreakSpeed fires repeatedly while mining -- only flood fill once per block position
        if (pos.equals(lastQueuedPos)) return;
        ServerLevel level = (ServerLevel) player.level();
        BlockState state = level.getBlockState(pos);
        if (isOre(state)) {
            lastQueuedPos = pos.immutable();
            floodFill(level, pos, state.getBlock());
        }
    }

    private boolean isOre(BlockState state) {
        return state.is(BlockTags.COAL_ORES) || state.is(BlockTags.IRON_ORES) || state.is(BlockTags.GOLD_ORES)
            || state.is(BlockTags.DIAMOND_ORES) || state.is(BlockTags.EMERALD_ORES) || state.is(BlockTags.LAPIS_ORES)
            || state.is(BlockTags.REDSTONE_ORES) || state.is(BlockTags.COPPER_ORES);
    }

    private void floodFill(ServerLevel level, BlockPos start, Block block) {
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(start);
        while (!queue.isEmpty() && visited.size() < maxBlocks.getValue()) {
            BlockPos current = queue.poll();
            if (visited.contains(current)) continue;
            visited.add(current);
            if (level.getBlockState(current).getBlock() == block) {
                pending.add(current);
                for (BlockPos neighbor : BlockPos.betweenClosed(current.offset(-1,-1,-1), current.offset(1,1,1))) {
                    if (!visited.contains(neighbor)) queue.add(neighbor.immutable());
                }
            }
        }
    }
}
