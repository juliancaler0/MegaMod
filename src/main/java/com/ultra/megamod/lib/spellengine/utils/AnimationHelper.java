package com.ultra.megamod.lib.spellengine.utils;


import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import com.ultra.megamod.lib.spellengine.api.spell.fx.PlayerAnimation;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCast;
import com.ultra.megamod.lib.spellengine.network.Packets;

import java.util.Collection;

public class AnimationHelper {
    public static void sendAnimationExcluding(Player animatedPlayer, Collection<ServerPlayer> trackingPlayers, SpellCast.Animation type, PlayerAnimation animation, float speed) {
        sendAnimation(animatedPlayer, false, trackingPlayers, type, animation, speed);
    }
    public static void sendAnimation(Player animatedPlayer, Collection<ServerPlayer> trackingPlayers, SpellCast.Animation type, PlayerAnimation animation, float speed) {
        sendAnimation(animatedPlayer, true, trackingPlayers, type, animation, speed);
    }
    public static void sendAnimation(Player animatedPlayer, boolean includeAnimated, Collection<ServerPlayer> trackingPlayers, SpellCast.Animation type, PlayerAnimation animation, float speed) {
        var id = getAnimationId(animatedPlayer, animation);
        if (id == null || id.isEmpty()) {
            return;
        }
        var packet = new Packets.SpellAnimation(animatedPlayer.getId(), type, id, speed * animation.speed);
        if (includeAnimated && animatedPlayer instanceof ServerPlayer serverPlayer) {
            sendPacketToPlayer(serverPlayer, packet);
        }
        trackingPlayers.forEach(serverPlayer -> {
            sendPacketToPlayer(serverPlayer, packet);
        });
    }

    private static void sendPacketToPlayer(ServerPlayer serverPlayer, Packets.SpellAnimation packet) {
        try {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getAnimationId(LivingEntity entity, PlayerAnimation animation) {
        if (animation == null) {
            return null;
        }
        return animation.resolve(entity);
    }
}
