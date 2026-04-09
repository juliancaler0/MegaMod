/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.JukeboxBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 */
package com.ultra.megamod.feature.blockanimations;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid="megamod", value={Dist.CLIENT})
public class ImprovedBlockAnimations {
    private static final int SCAN_RADIUS = 16;
    private static final int TICK_INTERVAL = 5;
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.isPaused()) {
            return;
        }
        if (++tickCounter < 5) {
            return;
        }
        tickCounter = 0;
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        BlockPos playerPos = player.blockPosition();
        RandomSource random = level.random;
        for (BlockPos pos : BlockPos.betweenClosed((BlockPos)playerPos.offset(-16, -16, -16), (BlockPos)playerPos.offset(16, 16, 16))) {
            if (!level.isLoaded(pos)) continue;
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.ENCHANTING_TABLE)) {
                ImprovedBlockAnimations.spawnEnchantingTableParticles(level, pos, random);
                continue;
            }
            if (state.is(Blocks.BREWING_STAND)) {
                ImprovedBlockAnimations.spawnBrewingStandParticles(level, pos, random);
                continue;
            }
            if (state.is(Blocks.BEACON)) {
                ImprovedBlockAnimations.spawnBeaconParticles(level, pos, random);
                continue;
            }
            if (state.is(Blocks.DRAGON_EGG)) {
                ImprovedBlockAnimations.spawnDragonEggParticles(level, pos, random);
                continue;
            }
            if (!state.is(Blocks.JUKEBOX) || !((Boolean)state.getValue((Property)JukeboxBlock.HAS_RECORD)).booleanValue()) continue;
            boolean noteBlockBelow = level.getBlockState(pos.below()).is(Blocks.NOTE_BLOCK);
            ImprovedBlockAnimations.spawnJukeboxParticles(level, pos, random, noteBlockBelow);
        }
    }

    private static void spawnEnchantingTableParticles(ClientLevel level, BlockPos pos, RandomSource random) {
        double cx = (double)pos.getX() + 0.5;
        double cy = (double)pos.getY() + 1.0;
        double cz = (double)pos.getZ() + 0.5;
        for (int i = 0; i < 4; ++i) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double radius = 0.4 + random.nextDouble() * 0.3;
            double px = cx + Math.cos(angle) * radius;
            double pz = cz + Math.sin(angle) * radius;
            double vy = 0.05 + random.nextDouble() * 0.1;
            level.addParticle((ParticleOptions)ParticleTypes.ENCHANT, px, cy + random.nextDouble() * 0.5, pz, 0.0, vy, 0.0);
        }
        if (random.nextInt(3) == 0) {
            level.addParticle((ParticleOptions)ParticleTypes.END_ROD, cx + (random.nextDouble() - 0.5) * 0.6, cy + 0.3 + random.nextDouble() * 0.4, cz + (random.nextDouble() - 0.5) * 0.6, 0.0, 0.02, 0.0);
        }
    }

    private static void spawnBrewingStandParticles(ClientLevel level, BlockPos pos, RandomSource random) {
        double cx = (double)pos.getX() + 0.5;
        double cy = (double)pos.getY() + 0.8;
        double cz = (double)pos.getZ() + 0.5;
        for (int i = 0; i < 3; ++i) {
            double offsetX = (random.nextDouble() - 0.5) * 0.4;
            double offsetZ = (random.nextDouble() - 0.5) * 0.4;
            level.addParticle((ParticleOptions)ParticleTypes.SPLASH, cx + offsetX, cy + random.nextDouble() * 0.3, cz + offsetZ, 0.0, 0.03, 0.0);
        }
        if (random.nextInt(2) == 0) {
            level.addParticle((ParticleOptions)ParticleTypes.WITCH, cx + (random.nextDouble() - 0.5) * 0.5, cy + 0.2, cz + (random.nextDouble() - 0.5) * 0.5, (random.nextDouble() - 0.5) * 0.02, 0.01 + random.nextDouble() * 0.02, (random.nextDouble() - 0.5) * 0.02);
        }
        if (random.nextInt(4) == 0) {
            level.addParticle((ParticleOptions)ParticleTypes.DRIPPING_WATER, cx + (random.nextDouble() - 0.5) * 0.6, cy - 0.1, cz + (random.nextDouble() - 0.5) * 0.6, 0.0, 0.0, 0.0);
        }
    }

    private static void spawnBeaconParticles(ClientLevel level, BlockPos pos, RandomSource random) {
        double cx = (double)pos.getX() + 0.5;
        double cy = (double)pos.getY() + 1.0;
        double cz = (double)pos.getZ() + 0.5;
        for (int i = 0; i < 3; ++i) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double radius = 0.5 + random.nextDouble() * 0.8;
            double px = cx + Math.cos(angle) * radius;
            double pz = cz + Math.sin(angle) * radius;
            level.addParticle((ParticleOptions)ParticleTypes.END_ROD, px, cy + random.nextDouble() * 0.5, pz, 0.0, 0.015 + random.nextDouble() * 0.02, 0.0);
        }
        if (random.nextInt(3) == 0) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double speed = 0.02 + random.nextDouble() * 0.03;
            level.addParticle((ParticleOptions)ParticleTypes.END_ROD, cx, cy + 0.3, cz, Math.cos(angle) * speed, 0.01, Math.sin(angle) * speed);
        }
        if (random.nextInt(2) == 0) {
            level.addParticle((ParticleOptions)ParticleTypes.END_ROD, cx + (random.nextDouble() - 0.5) * 1.2, cy + random.nextDouble() * 1.5, cz + (random.nextDouble() - 0.5) * 1.2, 0.0, 0.03 + random.nextDouble() * 0.02, 0.0);
        }
    }

    private static void spawnDragonEggParticles(ClientLevel level, BlockPos pos, RandomSource random) {
        int i;
        double cx = (double)pos.getX() + 0.5;
        double cy = (double)pos.getY() + 0.5;
        double cz = (double)pos.getZ() + 0.5;
        for (i = 0; i < 5; ++i) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double radius = 0.3 + random.nextDouble() * 0.5;
            double px = cx + Math.cos(angle) * radius;
            double pz = cz + Math.sin(angle) * radius;
            double vx = (cx - px) * 0.05;
            double vz = (cz - pz) * 0.05;
            level.addParticle((ParticleOptions)ParticleTypes.PORTAL, px, cy + (random.nextDouble() - 0.5) * 0.8, pz, vx, random.nextDouble() * 0.05, vz);
        }
        if (random.nextInt(3) == 0) {
            for (i = 0; i < 2; ++i) {
                level.addParticle((ParticleOptions)ParticleTypes.WITCH, cx + (random.nextDouble() - 0.5) * 0.6, cy + random.nextDouble() * 0.8, cz + (random.nextDouble() - 0.5) * 0.6, (random.nextDouble() - 0.5) * 0.02, 0.01 + random.nextDouble() * 0.02, (random.nextDouble() - 0.5) * 0.02);
            }
        }
    }

    private static void spawnJukeboxParticles(ClientLevel level, BlockPos pos, RandomSource random, boolean enhanced) {
        int i;
        double cx = (double)pos.getX() + 0.5;
        double cy = (double)pos.getY() + 1.2;
        double cz = (double)pos.getZ() + 0.5;
        double spread = enhanced ? 1.5 : 0.8;
        int noteCount = enhanced ? 3 : 1;
        int auraCount = enhanced ? 4 : 2;
        for (i = 0; i < noteCount; ++i) {
            level.addParticle((ParticleOptions)ParticleTypes.NOTE, cx + (random.nextDouble() - 0.5) * spread, cy + random.nextDouble() * 0.5, cz + (random.nextDouble() - 0.5) * spread, random.nextDouble(), 0.0, 0.0);
        }
        for (i = 0; i < auraCount; ++i) {
            level.addParticle((ParticleOptions)ParticleTypes.HAPPY_VILLAGER, cx + (random.nextDouble() - 0.5) * spread, cy + random.nextDouble() * 0.6, cz + (random.nextDouble() - 0.5) * spread, 0.0, 0.02, 0.0);
        }
        if (random.nextInt(2) == 0) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double radius = 0.4 + random.nextDouble() * (enhanced ? 1.0 : 0.4);
            level.addParticle((ParticleOptions)ParticleTypes.END_ROD, cx + Math.cos(angle) * radius, cy + random.nextDouble() * 0.5, cz + Math.sin(angle) * radius, 0.0, 0.01 + random.nextDouble() * 0.02, 0.0);
        }
        if (enhanced && random.nextInt(2) == 0) {
            for (int i2 = 0; i2 < 3; ++i2) {
                double angle = random.nextDouble() * Math.PI * 2.0;
                double speed = 0.03 + random.nextDouble() * 0.04;
                level.addParticle((ParticleOptions)ParticleTypes.NOTE, cx, cy + 0.3, cz, Math.cos(angle) * speed, 0.02, Math.sin(angle) * speed);
            }
        }
    }
}

