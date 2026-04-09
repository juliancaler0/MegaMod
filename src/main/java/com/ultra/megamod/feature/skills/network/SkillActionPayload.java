/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.neoforge.network.handling.IPayloadContext
 */
package com.ultra.megamod.feature.skills.network;

import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.skills.SkillAttributeApplier;
import com.ultra.megamod.feature.skills.SkillBranch;
import com.ultra.megamod.feature.skills.SkillEvents;
import com.ultra.megamod.feature.skills.SkillManager;
import com.ultra.megamod.feature.skills.SkillNode;
import com.ultra.megamod.feature.skills.SkillTreeDefinitions;
import com.ultra.megamod.feature.skills.SkillTreeType;
import com.ultra.megamod.feature.skills.integration.SkillsEconomyIntegration;
import com.ultra.megamod.feature.skills.prestige.PrestigeManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SkillActionPayload(String action, String nodeId) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<SkillActionPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath((String)"megamod", (String)"skill_action"));
    public static final StreamCodec<FriendlyByteBuf, SkillActionPayload> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, SkillActionPayload>(){

        public SkillActionPayload decode(FriendlyByteBuf buf) {
            String action = buf.readUtf();
            String nodeId = buf.readUtf();
            return new SkillActionPayload(action, nodeId);
        }

        public void encode(FriendlyByteBuf buf, SkillActionPayload payload) {
            buf.writeUtf(payload.action());
            buf.writeUtf(payload.nodeId());
        }
    };

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(SkillActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player patt0$temp = context.player();
            if (!(patt0$temp instanceof ServerPlayer)) {
                return;
            }
            ServerPlayer player = (ServerPlayer)patt0$temp;
            ServerLevel level = player.level();
            SkillManager manager = SkillManager.get(level);
            switch (payload.action()) {
                case "unlock": {
                    boolean isAdmin = com.ultra.megamod.feature.computer.admin.AdminSystem.isAdmin(player);
                    SkillNode node = SkillTreeDefinitions.getNodeById(payload.nodeId());
                    if (node != null && !manager.canUnlockInBranch(player.getUUID(), node, isAdmin)) {
                        player.sendSystemMessage((Component)Component.literal((String)("Branch locked! You've already specialized in 2 branches in " + node.branch().getTreeType().getDisplayName() + ". Respec to change your specialization.")).withStyle(ChatFormatting.RED));
                        break;
                    }
                    boolean success = manager.unlockNode(player.getUUID(), payload.nodeId(), isAdmin);
                    if (success) {
                        SkillAttributeApplier.recalculate(player);
                        com.ultra.megamod.feature.recipes.RecipeUnlocker.onSkillUnlocked(player, payload.nodeId());
                        player.sendSystemMessage((Component)Component.literal((String)"Skill node unlocked!").withStyle(ChatFormatting.GREEN));
                        // Sound + particle on unlock
                        if (node != null && node.tier() == 4) {
                            level.playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);
                            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1, player.getZ(), 50, 0.5, 0.5, 0.5, 0.5);
                        } else {
                            level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7f, 1.2f);
                            level.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1, player.getZ(), 30, 0.5, 0.5, 0.5, 0.5);
                        }
                        break;
                    }
                    player.sendSystemMessage((Component)Component.literal((String)"Cannot unlock that node. Check prerequisites and available points.").withStyle(ChatFormatting.RED));
                    break;
                }
                case "respec": {
                    if (!SkillsEconomyIntegration.attemptRespec(player)) {
                        player.sendSystemMessage((Component)Component.literal((String)"Not enough MegaCoins! Respec costs 50 MC.").withStyle(ChatFormatting.RED));
                        break;
                    }
                    SkillAttributeApplier.removeAll(player);
                    SkillAttributeApplier.recalculate(player);
                    player.sendSystemMessage((Component)Component.literal((String)"Skills reset! All points refunded. (50 MC deducted)").withStyle(ChatFormatting.YELLOW));
                    break;
                }
                case "respec_branch": {
                    String branchName = payload.nodeId();
                    SkillBranch branch = null;
                    for (SkillBranch b : SkillBranch.values()) {
                        if (b.name().equalsIgnoreCase(branchName)) {
                            branch = b;
                            break;
                        }
                    }
                    if (branch == null) {
                        player.sendSystemMessage((Component)Component.literal((String)"Invalid branch name.").withStyle(ChatFormatting.RED));
                        break;
                    }
                    ServerLevel overworld = player.level().getServer().overworld();
                    EconomyManager eco = EconomyManager.get(overworld);
                    if (!eco.spendWallet(player.getUUID(), 10)) {
                        player.sendSystemMessage((Component)Component.literal((String)"Not enough MegaCoins! Branch respec costs 10 MC.").withStyle(ChatFormatting.RED));
                        break;
                    }
                    int refunded = manager.respecBranch(player.getUUID(), branch);
                    if (refunded == 0) {
                        // Refund the 10 MC if nothing was actually reset
                        eco.addWallet(player.getUUID(), 10);
                        player.sendSystemMessage((Component)Component.literal((String)"No nodes unlocked in that branch.").withStyle(ChatFormatting.YELLOW));
                        break;
                    }
                    SkillAttributeApplier.removeAll(player);
                    SkillAttributeApplier.recalculate(player);
                    player.sendSystemMessage((Component)Component.literal((String)(branch.getDisplayName() + " branch reset! " + refunded + " points refunded. (10 MC deducted)")).withStyle(ChatFormatting.YELLOW));
                    break;
                }
                case "prestige": {
                    String treeName = payload.nodeId();
                    SkillTreeType tree = null;
                    for (SkillTreeType t : SkillTreeType.values()) {
                        if (t.name().equalsIgnoreCase(treeName)) {
                            tree = t;
                            break;
                        }
                    }
                    if (tree == null) {
                        player.sendSystemMessage((Component)Component.literal((String)"Invalid tree name.").withStyle(ChatFormatting.RED));
                        break;
                    }
                    int currentLevel = manager.getLevel(player.getUUID(), tree);
                    if (currentLevel < 50) {
                        player.sendSystemMessage((Component)Component.literal((String)"You must be level 50 in this tree to prestige!").withStyle(ChatFormatting.RED));
                        break;
                    }
                    ServerLevel overworld2 = player.level().getServer().overworld();
                    PrestigeManager prestige = PrestigeManager.get(overworld2);
                    int currentPrestige = prestige.getPrestigeLevel(player.getUUID(), tree);
                    if (currentPrestige >= 5) {
                        player.sendSystemMessage((Component)Component.literal((String)"Maximum prestige (5) already reached for this tree!").withStyle(ChatFormatting.RED));
                        break;
                    }
                    // Prestige cost: 100 * (currentPrestige + 1) MC
                    int prestigeCost = 100 * (currentPrestige + 1);
                    EconomyManager eco = EconomyManager.get(overworld2);
                    if (!eco.spendWallet(player.getUUID(), prestigeCost)) {
                        player.sendSystemMessage((Component)Component.literal((String)("Not enough MegaCoins! Prestige costs " + prestigeCost + " MC.")).withStyle(ChatFormatting.RED));
                        break;
                    }
                    prestige.prestige(player.getUUID(), tree);
                    manager.resetTree(player.getUUID(), tree);
                    SkillAttributeApplier.removeAll(player);
                    SkillAttributeApplier.recalculate(player);
                    // Apply class-specific prestige bonuses
                    com.ultra.megamod.feature.skills.prestige.PrestigeClassBonusHandler.onPrestige(player, tree);
                    int newPrestige = prestige.getPrestigeLevel(player.getUUID(), tree);
                    double bonus = prestige.getPrestigeBonus(player.getUUID(), tree) * 100;
                    // Award Mastery Marks
                    com.ultra.megamod.feature.prestige.MasteryMarkManager.get(overworld2).awardMilestone(
                        player, "skill_prestige_" + tree.name() + "_" + newPrestige, 25,
                        "Prestige " + newPrestige + " in " + tree.getDisplayName());
                    // Prestige reward: rolled item, high tier since players must hit lvl 50 to prestige
                    com.ultra.megamod.feature.dungeons.DungeonTier rewardTier = switch (newPrestige) {
                        case 1 -> com.ultra.megamod.feature.dungeons.DungeonTier.NIGHTMARE;
                        case 2 -> com.ultra.megamod.feature.dungeons.DungeonTier.INFERNAL;
                        case 3 -> com.ultra.megamod.feature.dungeons.DungeonTier.MYTHIC;
                        default -> com.ultra.megamod.feature.dungeons.DungeonTier.ETERNAL;
                    };
                    net.minecraft.world.item.ItemStack prestigeItem = com.ultra.megamod.feature.loot.WorldLootIntegration.generatePrestigeReward(
                            rewardTier, net.minecraft.util.RandomSource.create(), tree.getDisplayName());
                    if (!player.getInventory().add(prestigeItem)) {
                        player.spawnAtLocation((ServerLevel) player.level(), prestigeItem);
                    }
                    player.sendSystemMessage(Component.literal(
                            "\u00A76\u00A7l\u2605 \u00A7ePrestige reward: " + prestigeItem.getHoverName().getString() + "!").withStyle(ChatFormatting.GOLD));
                    player.sendSystemMessage((Component)Component.literal((String)("Prestige! " + tree.getDisplayName() + " reset to level 0 with +" + (int) bonus + "% permanent bonus (Prestige " + newPrestige + ") [-" + prestigeCost + " MC]")).withStyle(ChatFormatting.GOLD));
                    level.playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 0.8f);
                    level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1, player.getZ(), 80, 0.8, 1.0, 0.8, 0.6);
                    break;
                }
            }
            SkillEvents.syncToClient(player);
        });
    }
}

