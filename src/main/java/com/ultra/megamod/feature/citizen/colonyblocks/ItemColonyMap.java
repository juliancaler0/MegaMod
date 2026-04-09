package com.ultra.megamod.feature.citizen.colonyblocks;

import com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding;
import com.ultra.megamod.feature.citizen.data.ClaimData;
import com.ultra.megamod.feature.citizen.data.ClaimManager;
import com.ultra.megamod.feature.citizen.data.FactionData;
import com.ultra.megamod.feature.citizen.data.FactionManager;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Colony Map item that displays colony layout information in chat,
 * including claimed chunks and building positions.
 */
public class ItemColonyMap extends Item {

    public ItemColonyMap(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        ServerPlayer sp = (ServerPlayer) player;
        FactionManager fm = FactionManager.get(serverLevel);
        FactionData faction = fm.getPlayerFactionData(player.getUUID());
        String factionId = fm.getPlayerFaction(player.getUUID());

        sp.displayClientMessage(Component.literal(
            "\u00A73\u00A7l\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557"), false);
        sp.displayClientMessage(Component.literal(
            "\u00A73\u00A7l\u2551 \u00A7bColony Map \u00A73\u00A7l\u2551"), false);
        sp.displayClientMessage(Component.literal(
            "\u00A73\u00A7l\u2560\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2563"), false);

        if (faction == null || factionId == null) {
            sp.displayClientMessage(Component.literal(
                "\u00A77 You are not in a faction."), false);
        } else {
            sp.displayClientMessage(Component.literal(
                "\u00A77 Faction: \u00A7f" + faction.getDisplayName()), false);

            // Show Town Chest location
            if (faction.hasTownChest()) {
                BlockPos tcp = faction.getTownChestPos();
                sp.displayClientMessage(Component.literal(
                    "\u00A77 Town Chest: \u00A7f(" + tcp.getX() + ", " + tcp.getY() + ", " + tcp.getZ() + ")"), false);
            }

            // Show claimed chunks
            ClaimManager cm = ClaimManager.get(serverLevel);
            ClaimData claimData = cm.getClaim(factionId);
            if (claimData != null && claimData.getChunkCount() > 0) {
                sp.displayClientMessage(Component.literal(
                    "\u00A77 Claimed Chunks: \u00A7f" + claimData.getChunkCount() + "/" + ClaimData.MAX_CHUNKS), false);
                sp.displayClientMessage(Component.literal(""), false);
                sp.displayClientMessage(Component.literal(
                    "\u00A7b Chunk Coordinates:"), false);
                int shown = 0;
                for (long[] chunk : claimData.getClaimedChunks()) {
                    if (shown >= 12) {
                        int remaining = claimData.getChunkCount() - shown;
                        sp.displayClientMessage(Component.literal(
                            "\u00A78  ...and " + remaining + " more"), false);
                        break;
                    }
                    sp.displayClientMessage(Component.literal(
                        "\u00A77  Chunk (" + (int) chunk[0] + ", " + (int) chunk[1] + ")"
                        + " \u00A78-> blocks (" + ((int) chunk[0] * 16) + ", " + ((int) chunk[1] * 16) + ")"), false);
                    shown++;
                }
            } else {
                sp.displayClientMessage(Component.literal(
                    "\u00A77 No claimed chunks."), false);
            }

            // Find buildings within claimed territory
            sp.displayClientMessage(Component.literal(""), false);
            sp.displayClientMessage(Component.literal(
                "\u00A7b Buildings:"), false);
            List<String> buildings = new ArrayList<>();
            if (claimData != null) {
                for (long[] chunk : claimData.getClaimedChunks()) {
                    int cx = (int) chunk[0];
                    int cz = (int) chunk[1];
                    if (!serverLevel.hasChunk(cx, cz)) continue;
                    LevelChunk levelChunk = serverLevel.getChunk(cx, cz);
                    for (Map.Entry<BlockPos, BlockEntity> entry : levelChunk.getBlockEntities().entrySet()) {
                        if (entry.getValue() instanceof TileEntityColonyBuilding tile) {
                            String name = tile.getCustomName().isEmpty() ? tile.getBuildingId() : tile.getCustomName();
                            buildings.add("\u00A77  " + name + " \u00A78Lv." + tile.getBuildingLevel()
                                + " at (" + entry.getKey().getX() + ", " + entry.getKey().getY()
                                + ", " + entry.getKey().getZ() + ")");
                        }
                    }
                    if (buildings.size() >= 20) break;
                }
            }

            if (buildings.isEmpty()) {
                sp.displayClientMessage(Component.literal(
                    "\u00A77  No buildings found in claimed territory."), false);
            } else {
                for (String line : buildings) {
                    sp.displayClientMessage(Component.literal(line), false);
                }
            }
        }

        sp.displayClientMessage(Component.literal(
            "\u00A73\u00A7l\u255A\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255D"), false);

        // Sound feedback
        level.playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                SoundSource.PLAYERS, 0.5f, 1.0f);

        return InteractionResult.SUCCESS;
    }
}
