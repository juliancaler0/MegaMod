/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.CustomData
 */
package com.ultra.megamod.feature.relics.ability.usable;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.List;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class SpaceDissectorAbility {
    private static final String KEY_MARKED_X = "marked_x";
    private static final String KEY_MARKED_Y = "marked_y";
    private static final String KEY_MARKED_Z = "marked_z";
    private static final String KEY_MARKED_DIM = "marked_dim";
    public static final List<RelicAbility> ABILITIES = List.of(new RelicAbility("Mark", "Save your current position", 1, RelicAbility.CastType.INSTANTANEOUS, List.of()), new RelicAbility("Warp", "Teleport to your marked position", 3, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("range", 16.0, 32.0, RelicStat.ScaleType.ADD, 2.0))));

    public static void register() {
        AbilityCastHandler.registerAbility("Space Dissector", "Mark", SpaceDissectorAbility::executeMark);
        AbilityCastHandler.registerAbility("Space Dissector", "Warp", SpaceDissectorAbility::executeWarp);
    }

    private static void executeMark(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        tag.putDouble(KEY_MARKED_X, player.getX());
        tag.putDouble(KEY_MARKED_Y, player.getY());
        tag.putDouble(KEY_MARKED_Z, player.getZ());
        tag.putString(KEY_MARKED_DIM, player.level().dimension().identifier().toString());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        player.displayClientMessage(Component.literal(("Position marked at (" + (int)player.getX() + ", " + (int)player.getY() + ", " + (int)player.getZ() + ")")), true);
        ServerLevel level = (ServerLevel) player.level();
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0f, 1.2f);
    }

    private static void executeWarp(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double dz;
        double dy;
        double range = stats[0];
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        if (!tag.contains(KEY_MARKED_X)) {
            player.displayClientMessage(Component.literal("No position marked! Use Mark first."), true);
            return;
        }
        double markedX = tag.getDoubleOr(KEY_MARKED_X, 0.0);
        double markedY = tag.getDoubleOr(KEY_MARKED_Y, 0.0);
        double markedZ = tag.getDoubleOr(KEY_MARKED_Z, 0.0);
        String markedDim = tag.getStringOr(KEY_MARKED_DIM, "");
        String currentDim = player.level().dimension().identifier().toString();
        if (!currentDim.equals(markedDim)) {
            player.displayClientMessage(Component.literal("Marked position is in a different dimension!"), true);
            return;
        }
        double dx = markedX - player.getX();
        double distance = Math.sqrt(dx * dx + (dy = markedY - player.getY()) * dy + (dz = markedZ - player.getZ()) * dz);
        if (distance > range) {
            player.displayClientMessage(Component.literal(("Too far! Distance: " + (int)distance + " blocks (max: " + (int)range + ")")), true);
            return;
        }
        ServerLevel level = (ServerLevel) player.level();
        player.teleportTo(level, markedX, markedY, markedZ, Set.of(), player.getYRot(), player.getXRot(), false);
        player.displayClientMessage(Component.literal("Warped to marked position!"), true);
        level.playSound(null, markedX, markedY, markedZ, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}

