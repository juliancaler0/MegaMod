package com.ultra.megamod.feature.adminmodules.modules.combat;

import com.ultra.megamod.feature.adminmodules.AdminModule;
import com.ultra.megamod.feature.adminmodules.ModuleCategory;
import com.ultra.megamod.feature.adminmodules.ModuleSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class SurroundModule extends AdminModule {
    private ModuleSetting.BoolSetting doubleHeight;
    private ModuleSetting.EnumSetting blockType;
    private ModuleSetting.BoolSetting autoCenter;
    private int tickCounter = 0;

    public SurroundModule() {
        super("surround", "Surround", "Places obsidian around you for protection", ModuleCategory.COMBAT);
    }

    @Override
    protected void initSettings() {
        doubleHeight = bool("Double Height", false, "Place 2-high walls");
        blockType = enumVal("Block", "Obsidian", List.of("Obsidian", "Ender Chest", "Crying Obsidian"), "Block type to place");
        autoCenter = bool("Center", true, "Auto-center player in block before placing");
    }

    private BlockState getPlacementBlock() {
        return switch (blockType.getValue()) {
            case "Ender Chest" -> Blocks.ENDER_CHEST.defaultBlockState();
            case "Crying Obsidian" -> Blocks.CRYING_OBSIDIAN.defaultBlockState();
            default -> Blocks.OBSIDIAN.defaultBlockState();
        };
    }

    @Override
    public void onServerTick(ServerPlayer player, ServerLevel level) {
        // Rate limit: only check every 5 ticks (4 times per second) to prevent spamming setBlock calls
        if (++tickCounter % 5 != 0) return;

        BlockPos base = player.blockPosition();
        BlockState block = getPlacementBlock();

        // Auto-center: snap player to center of their block for tight surround
        if (autoCenter.getValue()) {
            double centerX = base.getX() + 0.5;
            double centerZ = base.getZ() + 0.5;
            double distSq = (player.getX() - centerX) * (player.getX() - centerX) +
                            (player.getZ() - centerZ) * (player.getZ() - centerZ);
            // Only center if not already close (avoid jitter)
            if (distSq > 0.04) { // > 0.2 blocks off-center
                player.teleportTo(centerX, player.getY(), centerZ);
                // Recalculate base after centering
                base = player.blockPosition();
            }
        }

        int[][] offsets = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        boolean allPlaced = true;
        for (int[] off : offsets) {
            BlockPos pos = base.offset(off[0], 0, off[1]);
            if (level.getBlockState(pos).isAir()) {
                level.setBlock(pos, block, 3);
                allPlaced = false;
            }
            if (doubleHeight.getValue()) {
                BlockPos upper = pos.above();
                if (level.getBlockState(upper).isAir()) {
                    level.setBlock(upper, block, 3);
                    allPlaced = false;
                }
            }
        }

        // Also place block below feet if air (floor protection)
        BlockPos below = base.below();
        if (level.getBlockState(below).isAir()) {
            level.setBlock(below, block, 3);
            allPlaced = false;
        }
    }
}
