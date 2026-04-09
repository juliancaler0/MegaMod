/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Items
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.living.LivingDeathEvent
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.ultra.megamod.feature.recovery;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="megamod")
public class RecoveryCoordinates {
    private static final String TAG_DEATH_X = "megamod:death_x";
    private static final String TAG_DEATH_Y = "megamod:death_y";
    private static final String TAG_DEATH_Z = "megamod:death_z";
    private static final String TAG_DEATH_DIM = "megamod:death_dim";

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)livingEntity;
        CompoundTag persistentData = player.getPersistentData();
        persistentData.putInt(TAG_DEATH_X, player.getBlockX());
        persistentData.putInt(TAG_DEATH_Y, player.getBlockY());
        persistentData.putInt(TAG_DEATH_Z, player.getBlockZ());
        persistentData.putString(TAG_DEATH_DIM, player.level().dimension().identifier().toString());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        boolean holdingCompass;
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        if (player2.tickCount % 10 != 0) {
            return;
        }
        boolean bl = holdingCompass = player2.getMainHandItem().is(Items.RECOVERY_COMPASS) || player2.getOffhandItem().is(Items.RECOVERY_COMPASS);
        if (!holdingCompass) {
            return;
        }
        CompoundTag persistentData = player2.getPersistentData();
        if (!persistentData.contains(TAG_DEATH_X)) {
            player2.displayClientMessage((Component)Component.literal((String)"No death location recorded").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}), true);
            return;
        }
        int x = persistentData.getIntOr(TAG_DEATH_X, 0);
        int y = persistentData.getIntOr(TAG_DEATH_Y, 0);
        int z = persistentData.getIntOr(TAG_DEATH_Z, 0);
        String dim = persistentData.getStringOr(TAG_DEATH_DIM, "");
        String dimDisplay = RecoveryCoordinates.formatDimensionName(dim);
        MutableComponent message = Component.literal((String)"Last Death: ").withStyle(ChatFormatting.RED).append((Component)Component.literal((String)(x + ", " + y + ", " + z)).withStyle(ChatFormatting.WHITE)).append((Component)Component.literal((String)" in ").withStyle(ChatFormatting.GRAY)).append((Component)Component.literal((String)dimDisplay).withStyle(ChatFormatting.AQUA));
        player2.displayClientMessage((Component)message, true);
    }

    private static String formatDimensionName(String dim) {
        int colonIndex = dim.indexOf(58);
        String path = colonIndex >= 0 ? dim.substring(colonIndex + 1) : dim;
        String[] words = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; ++i) {
            if (i > 0) {
                sb.append(' ');
            }
            if (words[i].isEmpty()) continue;
            sb.append(Character.toUpperCase(words[i].charAt(0)));
            if (words[i].length() <= 1) continue;
            sb.append(words[i].substring(1));
        }
        return sb.toString();
    }
}

