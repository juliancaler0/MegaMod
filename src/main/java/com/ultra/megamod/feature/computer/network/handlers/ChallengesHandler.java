package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/// Stubbed: SkillChallenges system was removed in the Pufferfish migration. The Challenges
/// computer app still exists in the UI but this handler now returns a permanently-empty list.
public class ChallengesHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData,
                                  ServerLevel level, EconomyManager eco) {
        if (!"challenges_request".equals(action)) return false;
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload(
                "challenges_data", "{\"challenges\":[],\"nextWeekMs\":0}", wallet, bank));
        return true;
    }
}
