package com.ultra.megamod.feature.citizen.colonyblocks;

import com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding;
// HeraldQuestTracker removed during MCEntityCitizen transition
import com.ultra.megamod.feature.citizen.quest.ColonyQuest;
import com.ultra.megamod.feature.citizen.quest.ColonyQuestManager;
import com.ultra.megamod.feature.citizen.quest.PlayerQuestData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

/**
 * Quest Log item. Shows active quests in chat.
 * - Shift+right-click on a hut block: shows quests for that colony
 * - Right-click in air: shows all active quests for the player
 */
public class ItemQuestLog extends Item {

    public ItemQuestLog(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        // Only handle shift+click on hut blocks
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS; // Fall through to use()
        }

        ServerLevel serverLevel = (ServerLevel) level;
        ServerPlayer sp = (ServerPlayer) player;
        BlockPos clickedPos = context.getClickedPos();
        BlockEntity be = level.getBlockEntity(clickedPos);

        if (be instanceof TileEntityColonyBuilding tile) {
            sp.displayClientMessage(Component.literal(
                "\u00A7d\u00A7l\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557"), false);
            String name = tile.getCustomName().isEmpty() ? tile.getBuildingId() : tile.getCustomName();
            sp.displayClientMessage(Component.literal(
                "\u00A7d\u00A7l\u2551 \u00A75Quest Log - " + name + " \u00A7d\u00A7l\u2551"), false);
            sp.displayClientMessage(Component.literal(
                "\u00A7d\u00A7l\u2560\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2563"), false);
            sp.displayClientMessage(Component.literal(
                "\u00A77 Building: \u00A7f" + tile.getBuildingId() + " \u00A77Lv." + tile.getBuildingLevel()), false);

            // Show quests for the colony owner
            if (tile.getColonyId() != null) {
                showQuestsForPlayer(serverLevel, sp, tile.getColonyId());
            } else {
                sp.displayClientMessage(Component.literal(
                    "\u00A77 No colony owner set for this building."), false);
            }

            sp.displayClientMessage(Component.literal(
                "\u00A7d\u00A7l\u255A\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255D"), false);
        } else {
            sp.displayClientMessage(Component.literal(
                "\u00A7c\u00A7l\u2716 \u00A76That block is not a colony building."), false);
        }

        level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS,
                1.0f, 1.0f);

        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        ServerPlayer sp = (ServerPlayer) player;

        sp.displayClientMessage(Component.literal(
            "\u00A7d\u00A7l\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557"), false);
        sp.displayClientMessage(Component.literal(
            "\u00A7d\u00A7l\u2551 \u00A75Quest Log \u00A7d\u00A7l\u2551"), false);
        sp.displayClientMessage(Component.literal(
            "\u00A7d\u00A7l\u2560\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2563"), false);

        showQuestsForPlayer(serverLevel, sp, player.getUUID());

        sp.displayClientMessage(Component.literal(
            "\u00A7d\u00A7l\u255A\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255D"), false);

        level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS,
                1.0f, 1.0f);

        return InteractionResult.SUCCESS;
    }

    private void showQuestsForPlayer(ServerLevel serverLevel, ServerPlayer displayTo, java.util.UUID targetUUID) {
        boolean hasAny = false;

        // --- Colony Quests ---
        try {
            ColonyQuestManager cqm = ColonyQuestManager.get(serverLevel);
            PlayerQuestData pqd = cqm.getPlayerData(targetUUID);

            // Active colony quest
            ColonyQuest activeColony = cqm.getActiveQuest(targetUUID);
            if (activeColony != null && pqd != null) {
                hasAny = true;
                int objIdx = pqd.getObjectiveIndex(activeColony.getId());
                String objDesc = activeColony.getObjectiveDescription(objIdx);
                displayTo.displayClientMessage(Component.literal(
                    "\u00A7a \u25B6 \u00A7f" + activeColony.getName()
                    + " \u00A77[In Progress]"), false);
                displayTo.displayClientMessage(Component.literal(
                    "\u00A77   Objective: \u00A7e" + objDesc), false);
            }

            // Available colony quests
            List<ColonyQuest> available = cqm.getAvailableQuests(targetUUID);
            if (!available.isEmpty()) {
                hasAny = true;
                displayTo.displayClientMessage(Component.literal(
                    "\u00A76 -- Available Colony Quests --"), false);
                for (ColonyQuest quest : available) {
                    displayTo.displayClientMessage(Component.literal(
                        "\u00A7e \u2022 \u00A7f" + quest.getName()
                        + " \u00A77[Available]"), false);
                }
            }
        } catch (Exception e) {
            // Colony quest system may not be loaded yet
        }

        // Herald quest system removed during MCEntityCitizen transition

        if (!hasAny) {
            displayTo.displayClientMessage(Component.literal(
                "\u00A77 No active quests."), false);
        }
    }
}
