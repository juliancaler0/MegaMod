/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.Holder
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
 *  net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
 *  net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.ultra.megamod.feature.subtitles;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="megamod")
public class BiomeDiscoverySubtitles {
    private static final String TAG_DISCOVERED_BIOMES = "megamod:discovered_biomes";
    private static final String TAG_LAST_BIOME = "megamod:last_biome";
    private static final int FADE_IN = 10;
    private static final int STAY = 40;
    private static final int FADE_OUT = 10;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        CompoundTag discoveredBiomes;
        CompoundTag persistentData;
        String lastBiome;
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        if (player2.tickCount % 20 != 0) {
            return;
        }
        Holder<?> biomeHolder = player2.level().getBiome(player2.blockPosition());
        ResourceKey<?> biomeKey = biomeHolder.unwrapKey().orElse(null);
        if (biomeKey == null) {
            return;
        }
        String biomeId = biomeKey.identifier().toString();
        if (biomeId.equals(lastBiome = (persistentData = player2.getPersistentData()).getStringOr(TAG_LAST_BIOME, ""))) {
            return;
        }
        persistentData.putString(TAG_LAST_BIOME, biomeId);
        if (persistentData.contains(TAG_DISCOVERED_BIOMES)) {
            discoveredBiomes = persistentData.getCompoundOrEmpty(TAG_DISCOVERED_BIOMES);
        } else {
            discoveredBiomes = new CompoundTag();
            persistentData.put(TAG_DISCOVERED_BIOMES, discoveredBiomes);
        }
        if (discoveredBiomes.getBooleanOr(biomeId, false)) {
            return;
        }
        discoveredBiomes.putBoolean(biomeId, true);
        // Challenge hook: discover_biome
        com.ultra.megamod.feature.skills.challenges.SkillChallenges.addProgress(player2, "discover_biome", 1);
        String biomeName = BiomeDiscoverySubtitles.formatBiomeName(biomeId);
        MutableComponent title = Component.literal(biomeName).withStyle(ChatFormatting.GREEN);
        MutableComponent subtitle = Component.literal("Biome Discovered").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC});
        player2.connection.send(new ClientboundSetTitlesAnimationPacket(10, 40, 10));
        player2.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        player2.connection.send(new ClientboundSetTitleTextPacket(title));
    }

    private static String formatBiomeName(String biomeId) {
        int colonIndex = biomeId.indexOf(58);
        String path = colonIndex >= 0 ? biomeId.substring(colonIndex + 1) : biomeId;
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

