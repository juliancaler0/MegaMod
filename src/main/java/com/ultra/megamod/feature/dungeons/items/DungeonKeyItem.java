/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.component.TooltipDisplay
 *  net.minecraft.world.level.Level
 */
package com.ultra.megamod.feature.dungeons.items;

import com.ultra.megamod.feature.computer.network.handlers.PartyHandler;
import com.ultra.megamod.feature.dungeons.DungeonManager;
import com.ultra.megamod.feature.dungeons.DungeonTier;
import com.ultra.megamod.feature.dungeons.insurance.InsuranceManager;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class DungeonKeyItem
extends Item {
    private final DungeonTier tier;

    public DungeonKeyItem(DungeonTier tier, Item.Properties props) {
        super(props.stacksTo(1));
        this.tier = tier;
    }

    public DungeonTier getTier() {
        return this.tier;
    }

    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        ServerLevel overworld = serverPlayer.level().getServer().overworld();
        DungeonManager manager = DungeonManager.get(overworld);
        if (manager.isPlayerInDungeon(serverPlayer.getUUID())) {
            serverPlayer.sendSystemMessage((Component)Component.literal((String)"You are already in a dungeon!").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        // Check if player already has a pending insurance session
        if (InsuranceManager.hasPendingSession(serverPlayer.getUUID())) {
            serverPlayer.sendSystemMessage(Component.literal("You already have a pending dungeon entry!").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        // Check if any party members are already in a dungeon
        Set<UUID> partyMembers = PartyHandler.getPartyMembers(serverPlayer.getUUID());
        for (UUID memberId : partyMembers) {
            if (memberId.equals(serverPlayer.getUUID())) continue;
            if (manager.isPlayerInDungeon(memberId)) {
                ServerPlayer member = overworld.getServer().getPlayerList().getPlayer(memberId);
                String memberName = member != null ? member.getGameProfile().name() : "A party member";
                serverPlayer.sendSystemMessage(Component.literal(memberName + " is already in a dungeon!").withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }
        }

        // Open insurance screen for key user + all party members
        // Key is NOT consumed yet — it will be consumed when all players are ready
        com.ultra.megamod.MegaMod.LOGGER.info("Dungeon key used by {} — party has {} members (including self)",
                serverPlayer.getGameProfile().name(), partyMembers.size());
        InsuranceManager.createSession(serverPlayer, this.tier, hand, partyMembers);
        // Notify all party members, not just the key holder
        for (UUID memberId : partyMembers) {
            ServerPlayer member = overworld.getServer().getPlayerList().getPlayer(memberId);
            if (member != null) {
                member.sendSystemMessage(Component.literal("Select your insurance and hit Ready!").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }

        return InteractionResult.SUCCESS;
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.literal("Tier: " + this.tier.getDisplayName()).withStyle(this.getTierColor()));
        tooltip.accept(Component.literal("Difficulty: x" + String.format("%.1f", this.tier.getDifficultyMultiplier())).withStyle(ChatFormatting.GRAY));

        tooltip.accept(Component.literal("Rooms: " + this.tier.getMinRooms() + "-" + this.tier.getMaxRooms()
                + " | Max Pieces: " + this.tier.getMaxPieces()).withStyle(ChatFormatting.GRAY));

        int bossChests = this.tier.getBossChestCount();
        int[] scRange = this.tier.getScatteredChestRange();
        String scattered = scRange[0] == scRange[1] ? String.valueOf(scRange[0]) : scRange[0] + "-" + scRange[1];
        tooltip.accept(Component.literal("Boss Chests: " + bossChests + " | Treasure Chests: " + scattered).withStyle(ChatFormatting.GRAY));

        String lootHint = switch (this.tier) {
            case NORMAL -> "Iron gear + Tier 1 weapons & relics";
            case HARD -> "Diamond gear + Tier 2 weapons & relics";
            case NIGHTMARE -> "Diamond gear + Tier 3 weapons & rare relics";
            case INFERNAL -> "Netherite gear + Legendary weapons & relics";
            case MYTHIC -> "Mythic Netherite gear + All legendary weapons & relics";
            case ETERNAL -> "Mythic Netherite+ gear + Nether Stars + Best-in-slot loot";
        };
        tooltip.accept(Component.literal("Loot: " + lootHint).withStyle(ChatFormatting.DARK_AQUA));

        tooltip.accept(Component.empty());
        tooltip.accept(Component.literal("Right-click to enter a dungeon").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.accept(Component.literal("Luck & Loot Fortune affect chest quality!").withStyle(ChatFormatting.BLUE));
        tooltip.accept(Component.literal("Warning: Death means losing your gear!").withStyle(ChatFormatting.RED));
        tooltip.accept(Component.literal("Insure items before entry or carry a Soul Anchor.").withStyle(ChatFormatting.DARK_RED));
    }

    public boolean isFoil(ItemStack stack) {
        return this.tier == DungeonTier.NIGHTMARE || this.tier == DungeonTier.INFERNAL
                || this.tier == DungeonTier.MYTHIC || this.tier == DungeonTier.ETERNAL;
    }

    private ChatFormatting getTierColor() {
        return switch (this.tier) {
            default -> throw new MatchException(null, null);
            case DungeonTier.NORMAL -> ChatFormatting.GREEN;
            case DungeonTier.HARD -> ChatFormatting.YELLOW;
            case DungeonTier.NIGHTMARE -> ChatFormatting.RED;
            case DungeonTier.INFERNAL -> ChatFormatting.DARK_PURPLE;
            case DungeonTier.MYTHIC -> ChatFormatting.LIGHT_PURPLE;
            case DungeonTier.ETERNAL -> ChatFormatting.GOLD;
        };
    }
}

