package com.ultra.megamod.lib.combatroll.client;

import com.google.gson.Gson;
import com.ultra.megamod.lib.combatroll.CombatRollMod;
import com.ultra.megamod.lib.combatroll.config.ServerConfig;
import com.ultra.megamod.lib.combatroll.internals.RollingEntity;
import com.ultra.megamod.lib.combatroll.network.Packets;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ClientNetwork {
    public static void handleRollAnimation(Packets.RollAnimation packet) {
        var client = Minecraft.getInstance();
        client.execute(() -> {
            var entity = client.level.getEntity(packet.playerId());
            if (entity instanceof Player player) {
                RollEffect.playVisuals(packet.visuals(), player, packet.velocity());
            }
        });
    }

    public static void handleConfigSync(Packets.ConfigSync packet) {
        var client = Minecraft.getInstance();
        var rollingPlayer = ((RollingEntity)client.player);
        if (rollingPlayer != null) {
            rollingPlayer.getRollManager().isEnabled = true;
        }
        var gson = new Gson();
        var config = gson.fromJson(packet.json(), ServerConfig.class);
        CombatRollMod.config = config;
    }
}
