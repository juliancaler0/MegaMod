package com.ultra.megamod.feature.quests;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.quests.QuestDefinitions.QuestDef;
import com.ultra.megamod.feature.quests.QuestDefinitions.QuestReward;
import com.ultra.megamod.feature.quests.QuestDefinitions.QuestRewardType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Grants quest rewards to a player when they claim a completed quest.
 */
public class QuestRewardGranter {

    public static void grantRewards(ServerPlayer player, QuestDef def, ServerLevel level, EconomyManager eco) {
        for (QuestReward reward : def.rewards()) {
            try {
                grantSingleReward(player, reward, level, eco);
            } catch (Exception e) {
                MegaMod.LOGGER.error("Failed to grant quest reward {} for quest {}: {}",
                    reward.type(), def.id(), e.getMessage());
            }
        }
    }

    private static void grantSingleReward(ServerPlayer player, QuestReward reward, ServerLevel level, EconomyManager eco) {
        switch (reward.type()) {
            case COINS -> {
                eco.addWallet(player.getUUID(), reward.amount());
            }
            case SKILL_XP -> {
                // targetId may be a full category identifier (skill_tree_rpgs:class_skills) or a
                // legacy enum name ("COMBAT" etc.) — categoryFor handles both.
                var catId = com.ultra.megamod.feature.skills.adminbridge.SkillAdminBridge
                        .categoryFor(reward.targetId().isEmpty() ? "CLASS" : reward.targetId());
                com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI.getCategory(catId)
                        .flatMap(cat -> cat.getExperience())
                        .ifPresent(exp -> exp.addTotal(player, reward.amount()));
            }
            case ITEM -> {
                if (!reward.targetId().isEmpty()) {
                    Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(reward.targetId()));
                    if (item != null) {
                        ItemStack stack = new ItemStack(item, reward.amount());
                        if (!player.getInventory().add(stack)) {
                            player.spawnAtLocation((ServerLevel) player.level(), stack);
                        }
                    }
                }
            }
        }
    }
}
