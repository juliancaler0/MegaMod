package com.ultra.megamod.feature.schematic.network;

import com.mojang.logging.LogUtils;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.schematic.data.*;
import com.ultra.megamod.feature.schematic.placement.SchematicPlacement;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import org.slf4j.Logger;

import java.util.UUID;

/**
 * Server-side handler for schematic network payloads.
 * Admin-only: schematics are instantly placed when accepted.
 */
public class SchematicServerHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void handlePlacement(ServerPlayer player, SchematicPlacementPayload payload) {
        if (!AdminSystem.isAdmin(player)) {
            player.sendSystemMessage(Component.literal("\u00A7cSchematics are admin-only."));
            return;
        }

        // Parse the schematic from the uploaded bytes
        SchematicData schematic = SchematicLoader.loadFromBytes(payload.schematicData(), payload.fileName());
        if (schematic == null) {
            player.sendSystemMessage(Component.literal("\u00A7cFailed to load schematic: " + payload.fileName()));
            return;
        }

        // Create placement
        Rotation rotation = SchematicPlacement.rotationFromIndex(payload.rotation());
        Mirror mirror = SchematicPlacement.mirrorFromIndex(payload.mirror());
        SchematicPlacement placement = new SchematicPlacement(schematic, payload.origin());
        placement.setRotation(rotation);
        placement.setMirror(mirror);
        placement.setLocked(true);

        // Create build order, then instantly place all blocks
        ServerLevel level = (ServerLevel) player.level();
        BuildOrderManager manager = BuildOrderManager.get(level);
        BuildOrder order = manager.createOrder(player.getUUID(), schematic, placement);

        if (order != null) {
            // Instantly place all blocks in the build queue
            int placed = 0;
            for (BuildOrder.BuildEntry entry : order.getBuildQueue()) {
                try {
                    level.setBlock(entry.worldPos(), entry.state(), 3);
                    placed++;
                } catch (Exception e) {
                    LOGGER.warn("Failed to place block at {}: {}", entry.worldPos(), e.getMessage());
                }
            }
            order.setProgressIndex(order.getTotalBlocks());

            // Remove the completed order (no need to persist it)
            manager.removeOrder(order.getOrderId());
            manager.saveToDisk(level);

            player.sendSystemMessage(Component.literal(
                    "\u00A7a\u00A7lSchematic placed! \u00A7f" + schematic.getName()
                            + "\u00A7a — " + placed + "/" + order.getTotalBlocks() + " blocks placed instantly."));
            LOGGER.info("Admin {} instantly placed schematic '{}' ({} blocks)",
                    player.getGameProfile().name(), schematic.getName(), placed);
        } else {
            player.sendSystemMessage(Component.literal("\u00A7cFailed to create build order."));
        }
    }

    public static void handleBuildOrder(ServerPlayer player, BuildOrderPayload payload) {
        ServerLevel level = (ServerLevel) player.level();
        BuildOrderManager manager = BuildOrderManager.get(level);

        switch (payload.action()) {
            case "cancel" -> {
                try {
                    UUID orderId = UUID.fromString(payload.orderId());
                    BuildOrder order = manager.getOrder(orderId);
                    if (order != null && (order.getOwnerUUID().equals(player.getUUID()) || AdminSystem.isAdmin(player))) {
                        manager.removeOrder(orderId);
                        player.sendSystemMessage(Component.literal("\u00A7eBuild order cancelled."));
                    }
                } catch (Exception e) {
                    LOGGER.warn("Failed to cancel build order: {}", e.getMessage());
                }
            }
        }
    }
}
