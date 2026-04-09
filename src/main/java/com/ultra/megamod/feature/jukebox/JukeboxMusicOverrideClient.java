/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.JukeboxBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 */
package com.ultra.megamod.feature.jukebox;

import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid="megamod", value={Dist.CLIENT})
public class JukeboxMusicOverrideClient {
    private static final int JUKEBOX_RANGE = 64;
    private static int tickCounter = 0;
    private static boolean wasSuppressing = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            wasSuppressing = false;
            return;
        }
        if (++tickCounter < 20) {
            if (wasSuppressing) {
                mc.getMusicManager().stopPlaying();
            }
            return;
        }
        tickCounter = 0;
        boolean jukeboxPlaying = JukeboxMusicOverrideClient.isJukeboxPlayingNearby((Level)mc.level, mc.player.blockPosition());
        if (jukeboxPlaying) {
            mc.getMusicManager().stopPlaying();
            wasSuppressing = true;
        } else {
            wasSuppressing = false;
        }
    }

    private static boolean isJukeboxPlayingNearby(Level level, BlockPos playerPos) {
        int range = 64;
        int step = 4;
        for (int x = -range; x <= range; x += step) {
            for (int y = -range; y <= range; y += step) {
                for (int z = -range; z <= range; z += step) {
                    if (!JukeboxMusicOverrideClient.checkForPlayingJukebox(level, playerPos.offset(x, y, z))) continue;
                    return true;
                }
            }
        }
        return JukeboxMusicOverrideClient.checkJukeboxBlockEntities(level, playerPos, range);
    }

    private static boolean checkForPlayingJukebox(Level level, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        return state.is(Blocks.JUKEBOX) && (Boolean)state.getValue((Property)JukeboxBlock.HAS_RECORD) != false;
    }

    private static boolean checkJukeboxBlockEntities(Level level, BlockPos center, int range) {
        int chunkRange = range >> 4;
        int centerChunkX = center.getX() >> 4;
        int centerChunkZ = center.getZ() >> 4;
        for (int cx = centerChunkX - chunkRange; cx <= centerChunkX + chunkRange; ++cx) {
            for (int cz = centerChunkZ - chunkRange; cz <= centerChunkZ + chunkRange; ++cz) {
                if (!level.hasChunk(cx, cz)) continue;
                LevelChunk chunk = level.getChunk(cx, cz);
                for (Map.Entry entry : chunk.getBlockEntities().entrySet()) {
                    BlockState state;
                    BlockPos bePos = (BlockPos)entry.getKey();
                    if (!bePos.closerThan((Vec3i)center, (double)range) || !(state = level.getBlockState(bePos)).is(Blocks.JUKEBOX) || !((Boolean)state.getValue((Property)JukeboxBlock.HAS_RECORD)).booleanValue()) continue;
                    return true;
                }
            }
        }
        return false;
    }
}

