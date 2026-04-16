package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Handles computer app requests for the Challenges screen.
 * TODO: Reconnect with Pufferfish Skills API (was SkillChallenges-based)
 */
public class ChallengesHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData,
                                  ServerLevel level, EconomyManager eco) {
        if (!"challenges_request".equals(action)) return false;

        // Old SkillChallenges system removed — return empty data
        StringBuilder sb = new StringBuilder();
        sb.append("{\"challenges\":[],\"nextWeekMs\":0}");

        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload("challenges_data", sb.toString(), wallet, bank));
        return true;
    }
}
