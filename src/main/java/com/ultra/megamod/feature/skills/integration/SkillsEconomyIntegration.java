package com.ultra.megamod.feature.skills.integration;

import com.ultra.megamod.feature.attributes.AttributeHelper;
import com.ultra.megamod.feature.attributes.MegaModAttributes;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.skills.SkillBranch;
import com.ultra.megamod.feature.skills.SkillManager;
import com.ultra.megamod.feature.skills.SkillTreeType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public final class SkillsEconomyIntegration {
    private static final int FULL_RESPEC_BASE = 100;
    private static final int FULL_RESPEC_PER_LEVEL = 25;
    private static final int BRANCH_RESPEC_BASE = 50;
    private static final int BRANCH_RESPEC_PER_TIER = 50;

    private SkillsEconomyIntegration() {
    }

    public static int applyMegacoinBonus(ServerPlayer player, int baseReward) {
        if (baseReward <= 0) {
            return 0;
        }
        double bonus = AttributeHelper.getValue((LivingEntity)player, MegaModAttributes.MEGACOIN_BONUS);
        int finalReward = (int)((double)baseReward * (1.0 + bonus / 100.0));
        return Math.max(1, finalReward);
    }

    public static int applyShopDiscount(ServerPlayer player, int basePrice) {
        if (basePrice <= 0) {
            return 0;
        }
        double discount = AttributeHelper.getValue((LivingEntity)player, MegaModAttributes.SHOP_DISCOUNT);
        int finalPrice = (int)((double)basePrice * (1.0 - discount / 100.0));
        return Math.max(1, finalPrice);
    }

    public static int applySellBonus(ServerPlayer player, int basePrice) {
        if (basePrice <= 0) {
            return 0;
        }
        double bonus = AttributeHelper.getValue((LivingEntity)player, MegaModAttributes.SELL_BONUS);
        int finalPrice = (int)((double)basePrice * (1.0 + bonus / 100.0));
        return Math.max(1, finalPrice);
    }

    /**
     * Full tree respec. Cost: 100 MC base + 25 MC per skill level invested in the tree.
     * First respec per tree is free.
     */
    public static boolean attemptRespec(ServerPlayer player, SkillTreeType tree) {
        ServerLevel overworld = player.level().getServer().overworld();
        SkillManager manager = SkillManager.get(overworld);

        int cost = getFullRespecCost(player, tree);
        if (cost > 0) {
            EconomyManager eco = EconomyManager.get(overworld);
            if (!eco.spendWallet(player.getUUID(), cost)) {
                return false;
            }
        }
        manager.respec(player.getUUID());
        return true;
    }

    /**
     * Legacy overload — respecs all trees, uses sum of costs.
     */
    public static boolean attemptRespec(ServerPlayer player) {
        ServerLevel overworld = player.level().getServer().overworld();
        SkillManager manager = SkillManager.get(overworld);

        int totalCost = 0;
        for (SkillTreeType tree : SkillTreeType.values()) {
            totalCost += getFullRespecCost(player, tree);
        }
        if (totalCost > 0) {
            EconomyManager eco = EconomyManager.get(overworld);
            if (!eco.spendWallet(player.getUUID(), totalCost)) {
                return false;
            }
        }
        manager.respec(player.getUUID());
        return true;
    }

    /**
     * Branch respec. Cost: 50 MC base + 50 MC per tier of the branch.
     * First respec per tree is free.
     */
    public static boolean attemptBranchRespec(ServerPlayer player, SkillBranch branch) {
        ServerLevel overworld = player.level().getServer().overworld();
        SkillManager manager = SkillManager.get(overworld);
        SkillTreeType tree = branch.getTreeType();

        int cost = getBranchRespecCost(player, branch);
        if (cost > 0) {
            EconomyManager eco = EconomyManager.get(overworld);
            if (!eco.spendWallet(player.getUUID(), cost)) {
                return false;
            }
        }
        manager.respecBranch(player.getUUID(), branch);
        return true;
    }

    public static int getFullRespecCost(ServerPlayer player, SkillTreeType tree) {
        ServerLevel overworld = player.level().getServer().overworld();
        SkillManager manager = SkillManager.get(overworld);
        // First respec per tree is free
        if (manager.getRespecCount(player.getUUID(), tree) == 0) {
            return 0;
        }
        int investment = manager.getTotalInvestment(player.getUUID(), tree);
        return FULL_RESPEC_BASE + (FULL_RESPEC_PER_LEVEL * investment);
    }

    public static int getBranchRespecCost(ServerPlayer player, SkillBranch branch) {
        ServerLevel overworld = player.level().getServer().overworld();
        SkillManager manager = SkillManager.get(overworld);
        SkillTreeType tree = branch.getTreeType();
        // First respec per tree is free
        if (manager.getRespecCount(player.getUUID(), tree) == 0) {
            return 0;
        }
        int highestTier = manager.getHighestTierInBranch(player.getUUID(), branch);
        return BRANCH_RESPEC_BASE + (BRANCH_RESPEC_PER_TIER * highestTier);
    }

    /**
     * @deprecated Use getFullRespecCost or getBranchRespecCost instead.
     */
    @Deprecated
    public static int getRespecCost() {
        return FULL_RESPEC_BASE;
    }
}
