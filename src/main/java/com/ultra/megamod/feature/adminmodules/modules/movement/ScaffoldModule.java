package com.ultra.megamod.feature.adminmodules.modules.movement;

import com.ultra.megamod.feature.adminmodules.AdminModule;
import com.ultra.megamod.feature.adminmodules.ModuleCategory;
import com.ultra.megamod.feature.adminmodules.ModuleSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public class ScaffoldModule extends AdminModule {
    private ModuleSetting.IntSetting extend;
    private ModuleSetting.EnumSetting block;
    private ModuleSetting.BoolSetting tower;

    public ScaffoldModule() {
        super("scaffold", "Scaffold", "Auto-places blocks below you", ModuleCategory.MOVEMENT);
    }

    @Override
    protected void initSettings() {
        extend = integer("Extend", 0, 0, 3, "Extra blocks in movement direction");
        block = enumVal("Block", "Cobblestone", List.of("Cobblestone", "Netherrack", "Obsidian", "Dirt"), "Block type");
        tower = bool("Tower", false, "Place blocks straight up when looking up");
    }

    @Override
    public void onServerTick(ServerPlayer player, ServerLevel level) {
        BlockState placeState = switch (block.getValue()) {
            case "Netherrack" -> Blocks.NETHERRACK.defaultBlockState();
            case "Obsidian" -> Blocks.OBSIDIAN.defaultBlockState();
            case "Dirt" -> Blocks.DIRT.defaultBlockState();
            default -> Blocks.COBBLESTONE.defaultBlockState();
        };

        // Tower mode: when looking up (pitch < -45), place blocks at feet and push up
        if (tower.getValue() && player.getXRot() < -45.0f) {
            BlockPos feetPos = player.blockPosition();
            if (level.getBlockState(feetPos.below()).isAir()) {
                level.setBlock(feetPos.below(), placeState, 3);
            }
            // Push player up if standing on placed block
            if (!level.getBlockState(feetPos.below()).isAir() && player.onGround()) {
                player.setDeltaMovement(player.getDeltaMovement().add(0, 0.42, 0));
                player.hurtMarked = true;
            }
            return;
        }

        BlockPos below = player.blockPosition().below();
        if (level.getBlockState(below).isAir()) {
            level.setBlock(below, placeState, 3);
        }

        int ext = extend.getValue();
        if (ext > 0) {
            // Use actual movement direction from velocity instead of yaw.
            // Yaw-based calculation had inverted directions for MC's coordinate system
            // (yaw 0 = south = +Z, yaw 90 = west = -X).
            // Fall back to facing direction if not moving.
            Vec3 vel = player.getDeltaMovement();
            double horizSpeedSq = vel.x * vel.x + vel.z * vel.z;
            int dx, dz;
            if (horizSpeedSq > 0.0001) {
                // Use velocity direction
                double angle = Math.atan2(-vel.x, vel.z);
                dx = Math.round((float) -Math.sin(angle));
                dz = Math.round((float) Math.cos(angle));
            } else {
                // Use facing direction (yaw): yaw 0 = south (+Z), 90 = west (-X)
                float yaw = player.getYRot();
                double rad = Math.toRadians(yaw);
                dx = Math.round((float) -Math.sin(rad));
                dz = Math.round((float) Math.cos(rad));
            }

            for (int i = 1; i <= ext; i++) {
                BlockPos extPos = below.offset(dx * i, 0, dz * i);
                if (level.getBlockState(extPos).isAir()) {
                    level.setBlock(extPos, placeState, 3);
                }
            }
        }
    }
}
