package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.skills.challenges.SkillChallenges;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Handles computer app requests for the Challenges screen.
 */
public class ChallengesHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData,
                                  ServerLevel level, EconomyManager eco) {
        if (!"challenges_request".equals(action)) return false;

        ServerLevel overworld = player.level().getServer().overworld();
        SkillChallenges.Challenge[] active = SkillChallenges.getActiveChallenges(overworld);
        int[] progress = SkillChallenges.getPlayerProgress(player.getUUID(), overworld);

        StringBuilder sb = new StringBuilder();
        sb.append("{\"challenges\":[");
        for (int i = 0; i < active.length; i++) {
            if (i > 0) sb.append(",");
            SkillChallenges.Challenge c = active[i];
            sb.append("{\"name\":\"").append(c.name()).append("\"");
            sb.append(",\"type\":\"").append(c.type()).append("\"");
            sb.append(",\"target\":").append(c.target());
            sb.append(",\"progress\":").append(progress[i]);
            sb.append(",\"tree\":\"").append(c.tree().getDisplayName()).append("\"");
            sb.append(",\"rewardXp\":").append(SkillChallenges.getRewardXp());
            sb.append(",\"rewardCoins\":").append(SkillChallenges.getRewardCoins());
            sb.append(",\"done\":").append(progress[i] >= c.target());
            sb.append("}");
        }
        sb.append("],\"nextWeekMs\":").append(SkillChallenges.getMillisUntilNextWeek());
        sb.append("}");

        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload("challenges_data", sb.toString(), wallet, bank));
        return true;
    }
}
