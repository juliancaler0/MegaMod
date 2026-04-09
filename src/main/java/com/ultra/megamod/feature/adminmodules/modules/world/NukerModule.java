package com.ultra.megamod.feature.adminmodules.modules.world;

import com.ultra.megamod.feature.adminmodules.AdminModule;
import com.ultra.megamod.feature.adminmodules.ModuleCategory;
import com.ultra.megamod.feature.adminmodules.ModuleSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NukerModule extends AdminModule {
    private ModuleSetting.IntSetting radius;
    private ModuleSetting.IntSetting rate;
    private ModuleSetting.EnumSetting shape;
    private ModuleSetting.BoolSetting dropItems;
    private ModuleSetting.BoolSetting whitelistMode;
    private ModuleSetting.EnumSetting whitelistBlock;
    private ModuleSetting.EnumSetting sortMode;
    private ModuleSetting.BoolSetting flattenMode;
    private ModuleSetting.BoolSetting smashMode;
    private ModuleSetting.BoolSetting rotate;
    private int tick = 0;

    public NukerModule() {
        super("nuker", "Nuker", "Breaks blocks in area around you", ModuleCategory.WORLD);
    }

    @Override
    protected void initSettings() {
        radius = integer("Radius", 4, 1, 8, "Break radius");
        rate = integer("Rate", 5, 1, 20, "Blocks per tick");
        shape = enumVal("Shape", "Sphere", List.of("Sphere", "Cube", "Flat"), "Break shape");
        dropItems = bool("DropItems", true, "Whether broken blocks drop items");
        whitelistMode = bool("WhitelistMode", false, "Only break the selected block type");
        whitelistBlock = enumVal("WhitelistBlock", "Stone", List.of(
                "Stone", "Dirt", "Gravel", "Sand", "Netherrack", "Cobblestone",
                "Deepslate", "Obsidian", "Sandstone", "Terracotta", "Andesite",
                "Diorite", "Granite", "Tuff", "Calcite", "Basalt", "Blackstone",
                "EndStone", "Prismarine", "SoulSand", "Grass"
        ), "Block to target in whitelist mode");
        sortMode = enumVal("SortMode", "None", List.of("None", "Closest", "TopDown"), "Block break order");
        flattenMode = bool("FlattenMode", false, "Only break blocks at or above player feet Y");
        smashMode = bool("SmashMode", false, "Only break instant-break blocks (hardness 0)");
        rotate = bool("Rotate", false, "Rotate player head toward block being broken");
    }

    @Override
    public void onServerTick(ServerPlayer player, ServerLevel level) {
        if (++tick % 2 != 0) return;
        int r = radius.getValue();
        int maxBreak = rate.getValue();
        boolean drops = dropItems.getValue();
        boolean whitelist = whitelistMode.getValue();
        boolean flatten = flattenMode.getValue();
        boolean smash = smashMode.getValue();
        BlockPos center = player.blockPosition();
        Block targetBlock = whitelist ? getWhitelistBlock() : null;
        int feetY = center.getY();

        // Step 1: Collect all breakable positions
        List<BlockPos> candidates = new ArrayList<>();
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    if ("Flat".equals(shape.getValue()) && y != 0) continue;
                    if ("Sphere".equals(shape.getValue()) && (x * x + y * y + z * z) > r * r) continue;
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir() || state.getBlock() == Blocks.BEDROCK) continue;
                    if (whitelist && state.getBlock() != targetBlock) continue;

                    // Step 2: Apply flatten filter
                    if (flatten && pos.getY() < feetY) continue;

                    // Step 3: Apply smash filter (hardness 0 = instant break)
                    if (smash && state.getDestroySpeed(level, pos) > 0.0f) continue;

                    candidates.add(pos);
                }
            }
        }

        // Step 4: Sort based on sortMode
        String sort = sortMode.getValue();
        if ("Closest".equals(sort)) {
            candidates.sort(Comparator.comparingDouble(pos -> pos.distSqr(center)));
        } else if ("TopDown".equals(sort)) {
            candidates.sort(Comparator.comparingInt((BlockPos pos) -> pos.getY()).reversed()
                    .thenComparingDouble(pos -> pos.distSqr(center)));
        }

        // Step 5: Break up to maxBreak blocks
        boolean doRotate = rotate.getValue();
        int broken = 0;
        for (BlockPos pos : candidates) {
            if (broken >= maxBreak) break;

            // Rotate player toward block before breaking
            if (doRotate) {
                double dx = pos.getX() + 0.5 - player.getX();
                double dy = pos.getY() + 0.5 - player.getEyeY();
                double dz = pos.getZ() + 0.5 - player.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                float yaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
                float pitch = (float) -(Mth.atan2(dy, dist) * (180.0 / Math.PI));
                player.setYRot(yaw);
                player.setXRot(pitch);
            }

            level.destroyBlock(pos, drops);
            broken++;
        }
    }

    private Block getWhitelistBlock() {
        return switch (whitelistBlock.getValue()) {
            case "Dirt" -> Blocks.DIRT;
            case "Gravel" -> Blocks.GRAVEL;
            case "Sand" -> Blocks.SAND;
            case "Netherrack" -> Blocks.NETHERRACK;
            case "Cobblestone" -> Blocks.COBBLESTONE;
            case "Deepslate" -> Blocks.DEEPSLATE;
            case "Obsidian" -> Blocks.OBSIDIAN;
            case "Sandstone" -> Blocks.SANDSTONE;
            case "Terracotta" -> Blocks.TERRACOTTA;
            case "Andesite" -> Blocks.ANDESITE;
            case "Diorite" -> Blocks.DIORITE;
            case "Granite" -> Blocks.GRANITE;
            case "Tuff" -> Blocks.TUFF;
            case "Calcite" -> Blocks.CALCITE;
            case "Basalt" -> Blocks.BASALT;
            case "Blackstone" -> Blocks.BLACKSTONE;
            case "EndStone" -> Blocks.END_STONE;
            case "Prismarine" -> Blocks.PRISMARINE;
            case "SoulSand" -> Blocks.SOUL_SAND;
            case "Grass" -> Blocks.GRASS_BLOCK;
            default -> Blocks.STONE;
        };
    }
}
