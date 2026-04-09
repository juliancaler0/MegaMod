package com.ultra.megamod.feature.citizen.client;

import com.google.gson.JsonObject;
import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import com.ultra.megamod.feature.citizen.network.CitizenActionPayload;
import com.ultra.megamod.feature.citizen.blockui.BOScreen;
import com.ultra.megamod.feature.citizen.blockui.views.BOWindow;
import com.ultra.megamod.feature.citizen.screen.citizen.MainWindowCitizen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class CitizenClientEvents {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getEntity().level().isClientSide()) return;
        if (!CitizenAssignmentMode.isActive()) return;

        var type = CitizenAssignmentMode.getType();
        int entityId = CitizenAssignmentMode.getEntityId();
        BlockPos pos = event.getPos();
        BlockState state = event.getEntity().level().getBlockState(pos);
        Block block = state.getBlock();

        switch (type) {
            case BED -> {
                if (!(block instanceof BedBlock)) {
                    Minecraft.getInstance().player.displayClientMessage(
                        Component.literal("\u00A7cThat's not a bed! Right-click a bed to assign it."), true);
                    event.setCanceled(true);
                    return;
                }
            }
            case CHEST, UPKEEP_CHEST -> {
                if (!isContainer(block)) {
                    Minecraft.getInstance().player.displayClientMessage(
                        Component.literal("\u00A7cThat's not a container! Right-click a chest, barrel, or shulker box."), true);
                    event.setCanceled(true);
                    return;
                }
            }
            case TOWN_CHEST -> {
                if (!(block instanceof com.ultra.megamod.feature.citizen.block.TownChestBlock)) {
                    Minecraft.getInstance().player.displayClientMessage(
                        Component.literal("\u00A7cThat's not a Town Chest! Right-click your Town Chest block."), true);
                    event.setCanceled(true);
                    return;
                }
            }
            case WORK -> {
                // Any solid block works for work area
            }
        }

        // Send position to server
        String action = switch (type) {
            case BED -> "set_bed";
            case CHEST -> "set_chest";
            case WORK -> "set_work";
            case UPKEEP_CHEST -> "set_upkeep_chest";
            case TOWN_CHEST -> "set_town_chest";
        };

        JsonObject json = new JsonObject();
        json.addProperty("x", pos.getX());
        json.addProperty("y", pos.getY());
        json.addProperty("z", pos.getZ());
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new CitizenActionPayload(entityId, action, json.toString()));

        CitizenAssignmentMode.clear();
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!event.getEntity().level().isClientSide()) return;

        // If in assignment mode and clicking an entity, cancel assignment
        if (CitizenAssignmentMode.isActive()) {
            CitizenAssignmentMode.cancel();
            // Don't cancel the event — let it fall through to normal interaction
        }

        // Right-click MC citizen -> open citizen info screen
        if (event.getTarget() instanceof MCEntityCitizen mcCitizen) {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                new MainWindowCitizen(mcCitizen).open();
                event.setCanceled(true);
            }
            return;
        }
    }

    /**
     * Sends a request to the server to check if a citizen has quest dialogue.
     * The server will respond with dialogue data if available.
     */
    private static void sendQuestDialogueRequest(int entityId) {
        ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new CitizenActionPayload(entityId, "quest_get_dialogue", "{}"));
    }

    private static boolean isContainer(Block block) {
        return block instanceof ChestBlock
            || block instanceof BarrelBlock
            || block instanceof ShulkerBoxBlock
            || block instanceof net.minecraft.world.level.block.TrappedChestBlock
            || block instanceof com.ultra.megamod.feature.citizen.block.TownChestBlock;
    }
}
