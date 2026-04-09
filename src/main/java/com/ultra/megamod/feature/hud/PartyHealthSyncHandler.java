package com.ultra.megamod.feature.hud;

import com.ultra.megamod.feature.computer.network.handlers.PartyHandler;
import com.ultra.megamod.feature.computer.network.handlers.SettingsHandler;
import com.ultra.megamod.feature.hud.network.PartyHealthPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Server-side: syncs party member health data to clients every 10 ticks.
 */
@EventBusSubscriber(modid = "megamod")
public class PartyHealthSyncHandler {

    // Track which players were in a party last tick so we only send clear once
    private static final Set<UUID> wasInParty = new HashSet<>();

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 10 != 0) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            UUID uuid = player.getUUID();
            if (!PartyHandler.isInParty(uuid)) {
                // Send clear only once when player leaves party
                if (wasInParty.remove(uuid)) {
                    PacketDistributor.sendToPlayer(player, new PartyHealthPayload(List.of()));
                }
                continue;
            }
            wasInParty.add(uuid);
            if (!SettingsHandler.isEnabled(uuid, "hud_party_health")) continue;

            Set<UUID> memberUuids = PartyHandler.getPartyMembers(uuid);
            List<PartyHealthPayload.MemberInfo> members = new ArrayList<>();

            for (UUID memberUuid : memberUuids) {
                if (memberUuid.equals(uuid)) continue; // Skip self
                ServerPlayer member = event.getServer().getPlayerList().getPlayer(memberUuid);
                if (member != null) {
                    members.add(new PartyHealthPayload.MemberInfo(
                        member.getGameProfile().name(),
                        member.getHealth(),
                        member.getMaxHealth(),
                        true
                    ));
                } else {
                    String name = PartyHandler.getCachedName(memberUuid);
                    if (name == null) name = "???";
                    members.add(new PartyHealthPayload.MemberInfo(name, 0, 20, false));
                }
            }

            PacketDistributor.sendToPlayer(player, new PartyHealthPayload(members));
        }
    }
}
