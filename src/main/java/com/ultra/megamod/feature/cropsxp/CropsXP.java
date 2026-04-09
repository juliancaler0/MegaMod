/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.ExperienceOrb
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.CropBlock
 *  net.minecraft.world.level.block.NetherWartBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.level.BlockEvent$BreakEvent
 */
package com.ultra.megamod.feature.cropsxp;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid="megamod")
public class CropsXP {
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Block block;
        Level level = (Level)event.getLevel();
        if (level.isClientSide()) {
            return;
        }
        BlockState state = event.getState();
        if (!CropsXP.isMatureCrop(state, block = state.getBlock())) {
            return;
        }
        if (level.getRandom().nextFloat() >= 0.4f) {
            return;
        }
        BlockPos pos = event.getPos();
        ServerLevel serverLevel = (ServerLevel)level;
        int xpAmount = 1 + level.getRandom().nextInt(3);
        ExperienceOrb.award((ServerLevel)serverLevel, (Vec3)pos.getCenter(), (int)xpAmount);
    }

    private static boolean isMatureCrop(BlockState state, Block block) {
        if (block == Blocks.PUMPKIN || block == Blocks.MELON) {
            return true;
        }
        if (block instanceof CropBlock) {
            CropBlock cropBlock = (CropBlock)block;
            return cropBlock.isMaxAge(state);
        }
        if (block instanceof NetherWartBlock) {
            int age = (Integer)state.getValue((Property)NetherWartBlock.AGE);
            return age >= 3;
        }
        return false;
    }
}

