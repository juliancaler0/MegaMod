/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.server.level.ServerPlayer$RespawnConfig
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.storage.LevelData$RespawnData
 */
package com.ultra.megamod.feature.relics.ability.usable;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;

public class MagicMirrorAbility {
    public static final List<RelicAbility> ABILITIES = List.of(new RelicAbility("Recall", "Teleport to world spawn", 1, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("cooldown_reduction", 0.0, 30.0, RelicStat.ScaleType.ADD, 4.0))), new RelicAbility("Bed Recall", "Teleport to your bed or respawn point", 5, RelicAbility.CastType.INSTANTANEOUS, List.of()));

    public static void register() {
        AbilityCastHandler.registerAbility("Magic Mirror", "Recall", MagicMirrorAbility::executeRecall);
        AbilityCastHandler.registerAbility("Magic Mirror", "Bed Recall", MagicMirrorAbility::executeBedRecall);
    }

    private static void executeRecall(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        ServerLevel overworld = player.level().getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }
        LevelData.RespawnData respawnData = overworld.getRespawnData();
        BlockPos spawn = respawnData.pos();
        double x = (double)spawn.getX() + 0.5;
        double y = spawn.getY();
        double z = (double)spawn.getZ() + 0.5;
        player.teleportTo(overworld, x, y, z, Set.of(), player.getYRot(), player.getXRot(), false);
        player.displayClientMessage((Component)Component.literal((String)"Recalled to world spawn!"), true);
        overworld.playSound(null, x, y, z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static void executeBedRecall(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        ServerPlayer.RespawnConfig respawnConfig = player.getRespawnConfig();
        if (respawnConfig != null) {
            LevelData.RespawnData respawnData = respawnConfig.respawnData();
            ServerLevel targetLevel = player.level().getServer().getLevel(respawnData.dimension());
            if (targetLevel == null) {
                MagicMirrorAbility.executeRecallFallback(player);
                return;
            }
            BlockPos respawnPos = respawnData.pos();
            double x = (double)respawnPos.getX() + 0.5;
            double y = respawnPos.getY();
            double z = (double)respawnPos.getZ() + 0.5;
            player.teleportTo(targetLevel, x, y, z, Set.of(), player.getYRot(), player.getXRot(), false);
            player.displayClientMessage((Component)Component.literal((String)"Recalled to bed!"), true);
            targetLevel.playSound(null, x, y, z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.2f);
        } else {
            MagicMirrorAbility.executeRecallFallback(player);
        }
    }

    private static void executeRecallFallback(ServerPlayer player) {
        ServerLevel overworld = player.level().getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }
        LevelData.RespawnData respawnData = overworld.getRespawnData();
        BlockPos spawn = respawnData.pos();
        double x = (double)spawn.getX() + 0.5;
        double y = spawn.getY();
        double z = (double)spawn.getZ() + 0.5;
        player.teleportTo(overworld, x, y, z, Set.of(), player.getYRot(), player.getXRot(), false);
        player.displayClientMessage((Component)Component.literal((String)"No bed found, recalled to world spawn!"), true);
        overworld.playSound(null, x, y, z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0f, 0.8f);
    }
}

