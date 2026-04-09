/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.LivingEntity
 */
package com.ultra.megamod.feature.skills.integration;

import com.ultra.megamod.feature.attributes.AttributeHelper;
import com.ultra.megamod.feature.attributes.MegaModAttributes;
import com.ultra.megamod.feature.skills.SkillManager;
import com.ultra.megamod.feature.skills.SkillTreeType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public final class SkillsRelicIntegration {
    private static final int ARCANE_XP_PER_CAST = 5;

    private SkillsRelicIntegration() {
    }

    public static double[] applyAbilityPower(ServerPlayer player, double[] statValues) {
        double abilityPower = AttributeHelper.getValue((LivingEntity)player, MegaModAttributes.ABILITY_POWER);
        if (abilityPower <= 0.0 || statValues == null || statValues.length == 0) {
            return statValues;
        }
        double multiplier = 1.0 + abilityPower / 100.0;
        double[] boosted = new double[statValues.length];
        for (int i = 0; i < statValues.length; ++i) {
            boosted[i] = statValues[i] * multiplier;
        }
        return boosted;
    }

    public static int applyCooldownReduction(ServerPlayer player, int baseCooldown) {
        double cdr = AttributeHelper.getValue((LivingEntity)player, MegaModAttributes.COOLDOWN_REDUCTION);
        if (cdr <= 0.0) {
            return baseCooldown;
        }
        int reduced = (int)((double)baseCooldown * (1.0 - cdr / 100.0));
        return Math.max(1, reduced);
    }

    public static void grantArcaneCastXp(ServerPlayer player) {
        ServerLevel overworld = player.level().getServer().overworld();
        SkillManager manager = SkillManager.get(overworld);
        if (!manager.checkAntiAbuse(player.getUUID(), "arcane_cast", 5)) {
            return;
        }
        int levelsGained = manager.addXp(player.getUUID(), SkillTreeType.ARCANE, 5);
        if (levelsGained > 0) {
            player.sendSystemMessage((Component)Component.literal((String)("Arcane skill leveled up! Now level " + manager.getLevel(player.getUUID(), SkillTreeType.ARCANE) + " (+1 skill point)")).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }
}

