package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.museum.MuseumData;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Set;

public class MusicPlayerHandler {

    private static final String[] DISC_IDS = {
        "minecraft:music_disc_13",
        "minecraft:music_disc_cat",
        "minecraft:music_disc_blocks",
        "minecraft:music_disc_chirp",
        "minecraft:music_disc_far",
        "minecraft:music_disc_mall",
        "minecraft:music_disc_mellohi",
        "minecraft:music_disc_stal",
        "minecraft:music_disc_strad",
        "minecraft:music_disc_ward",
        "minecraft:music_disc_11",
        "minecraft:music_disc_wait",
        "minecraft:music_disc_otherside",
        "minecraft:music_disc_pigstep",
        "minecraft:music_disc_5",
        "minecraft:music_disc_relic"
    };

    // Admin-exclusive custom discs
    private static final String[] ADMIN_DISC_IDS = {
        "megamod:house_money"
    };

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        if (!"music_request_discs".equals(action)) {
            return false;
        }

        MuseumData data = MuseumData.get(level);
        Set<String> donatedItems = data.getDonatedItems(player.getUUID());
        boolean isAdmin = AdminSystem.isAdmin(player);

        StringBuilder sb = new StringBuilder("{\"unlockedDiscs\":[");
        boolean first = true;
        for (String disc : DISC_IDS) {
            if (donatedItems.contains(disc)) {
                if (!first) sb.append(",");
                sb.append("\"").append(disc).append("\"");
                first = false;
            }
        }
        // Admin-only custom discs
        if (isAdmin) {
            for (String disc : ADMIN_DISC_IDS) {
                if (!first) sb.append(",");
                sb.append("\"").append(disc).append("\"");
                first = false;
            }
        }
        sb.append("]}");

        sendResponse(player, "music_disc_data", sb.toString(), eco);
        return true;
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer((ServerPlayer) player, (CustomPacketPayload) new ComputerDataPayload(type, json, wallet, bank), (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }
}
