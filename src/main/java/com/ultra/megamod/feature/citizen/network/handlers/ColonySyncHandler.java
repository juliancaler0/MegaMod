package com.ultra.megamod.feature.citizen.network.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.citizen.CitizenManager;
import com.ultra.megamod.feature.citizen.data.*;
import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.BuildingRegistry;
import com.ultra.megamod.feature.citizen.building.workorder.ColonyWorkOrder;
import com.ultra.megamod.feature.citizen.building.workorder.WorkManager;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import com.ultra.megamod.feature.citizen.network.*;
import com.ultra.megamod.feature.citizen.research.LocalResearchTree;
import com.ultra.megamod.feature.citizen.research.ResearchManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

/**
 * Server-side handler for colony sync network payloads.
 * <p>
 * Handles incoming client-to-server requests (colony actions, building actions,
 * work order management, research requests) and sends back appropriate
 * server-to-client sync payloads.
 * <p>
 * NOTE: Many building/citizen detail APIs are pending implementation.
 * Methods that depend on unimplemented APIs are stubbed with TODO markers.
 */
public class ColonySyncHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ColonySyncHandler.class);

    // ======================== Colony View Sync ========================

    /**
     * Builds and sends a full colony view to the requesting player.
     * Called when a player opens the Town Hall or subscribes to colony updates.
     */
    public static void sendColonyView(ServerPlayer player, String colonyId, boolean isFullSync) {
        ServerLevel level = (ServerLevel) player.level();
        FactionManager fm = FactionManager.get(level);
        FactionData faction = fm.getFaction(colonyId);
        if (faction == null) return;

        CitizenManager cm = CitizenManager.get(level);
        ColonyStatisticsManager stats = ColonyStatisticsManager.get(level, colonyId);

        JsonObject json = new JsonObject();
        json.addProperty("name", faction.getName());
        json.addProperty("ownerId", faction.getOwnerId().toString());
        json.addProperty("style", faction.getStyle() != null ? faction.getStyle() : "colonial");

        // Citizens list
        JsonArray citizensArray = new JsonArray();
        List<CitizenManager.CitizenRecord> citizens = cm.getCitizensForFaction(colonyId);
        if (citizens != null) {
            for (CitizenManager.CitizenRecord record : citizens) {
                JsonObject citizenJson = new JsonObject();
                citizenJson.addProperty("entityId", record.entityId().toString());
                citizenJson.addProperty("name", record.name());
                citizenJson.addProperty("job", record.job().name());
                citizenJson.addProperty("jobDisplayName", record.job().getDisplayName());
                citizensArray.add(citizenJson);
            }
        }
        json.add("citizens", citizensArray);

        // Buildings list - TODO: BuildingRegistry instance API pending
        json.add("buildings", new JsonArray());

        // Happiness - from stats
        json.addProperty("happiness", stats.getAverage(ColonyStatisticsManager.CITIZEN_HAPPINESS_TOTAL));

        // Citizen count
        json.addProperty("citizenCount", citizens != null ? citizens.size() : 0);

        ColonyViewPayload payload = new ColonyViewPayload(colonyId, isFullSync, json.toString());
        PacketDistributor.sendToPlayer(player, payload);
    }

    // ======================== Building View Sync ========================

    /**
     * Builds and sends building-specific view data to the requesting player.
     * TODO: Building detail APIs pending implementation.
     */
    public static void sendBuildingView(ServerPlayer player, String colonyId, long buildingPosLong) {
        // Building instance lookup API not yet available
        JsonObject json = new JsonObject();
        json.addProperty("buildingId", "unknown");
        json.addProperty("displayName", "Building");
        json.addProperty("level", 0);
        json.addProperty("position", buildingPosLong);
        json.addProperty("isBuilt", false);

        BuildingViewPayload payload = new BuildingViewPayload(colonyId, buildingPosLong, json.toString());
        PacketDistributor.sendToPlayer(player, payload);
    }

    // ======================== Colony Action Handler ========================

    /**
     * Handles colony-level actions from the client.
     */
    public static void handleColonyAction(ServerPlayer player, String colonyId, String action, String jsonData) {
        ServerLevel level = (ServerLevel) player.level();
        FactionManager fm = FactionManager.get(level);

        // Verify player has permission for this colony
        String playerFaction = fm.getPlayerFaction(player.getUUID());
        if (playerFaction == null || !playerFaction.equals(colonyId)) return;

        switch (action) {
            case "rename" -> {
                JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
                String newName = json.get("name").getAsString();
                if (newName.length() > 64) newName = newName.substring(0, 64);
                FactionData faction = fm.getFaction(colonyId);
                if (faction != null && faction.getOwnerId().equals(player.getUUID())) {
                    faction.setName(newName);
                    fm.markDirty();
                }
            }
            case "toggle_setting" -> {
                // TODO: FactionData.setSetting not yet implemented
                LOGGER.debug("Colony toggle_setting action pending implementation");
            }
            case "recall_citizens" -> {
                // Recall all citizens belonging to the colony's owner
                CitizenManager cm = CitizenManager.get(level);
                List<CitizenManager.CitizenRecord> citizens = cm.getCitizensForFaction(colonyId);
                if (citizens != null) {
                    BlockPos recallPos = player.blockPosition();
                    for (CitizenManager.CitizenRecord record : citizens) {
                        // Find the entity in the world
                        for (Entity e : level.getEntities().getAll()) {
                            if (e.getUUID().equals(record.entityId()) && e instanceof MCEntityCitizen citizen) {
                                citizen.teleportTo(recallPos.getX() + 0.5, (double) recallPos.getY(), recallPos.getZ() + 0.5);
                                break;
                            }
                        }
                    }
                }
            }
            case "set_style" -> {
                JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
                String style = json.get("style").getAsString();
                FactionData faction = fm.getFaction(colonyId);
                if (faction != null && faction.getOwnerId().equals(player.getUUID())) {
                    faction.setStyle(style);
                    fm.markDirty();
                }
            }
            default -> {}
        }

        // After action, send updated colony view back to player
        sendColonyView(player, colonyId, false);
    }

    // ======================== Building Action Handler ========================

    /**
     * Handles building-level actions from the client.
     * TODO: Most building actions require BuildingRegistry instance API.
     */
    public static void handleBuildingAction(ServerPlayer player, String colonyId, long buildingPosLong, String action, String jsonData) {
        ServerLevel level = (ServerLevel) player.level();
        FactionManager fm = FactionManager.get(level);

        // Verify player has permission
        String playerFaction = fm.getPlayerFaction(player.getUUID());
        if (playerFaction == null || !playerFaction.equals(colonyId)) return;

        LOGGER.debug("Building action '{}' received but building instance API pending", action);

        // After action, send updated building view back to player
        sendBuildingView(player, colonyId, buildingPosLong);
    }

    // ======================== Citizen Sync ========================

    /**
     * Sends detailed citizen data to the player.
     */
    public static void sendCitizenSync(ServerPlayer player, String colonyId, String citizenIdStr) {
        ServerLevel level = (ServerLevel) player.level();
        UUID citizenUUID = UUID.fromString(citizenIdStr);

        // Look up citizen entity
        CitizenManager cm = CitizenManager.get(level);
        CitizenManager.CitizenRecord record = cm.getCitizenByEntity(citizenUUID);

        JsonObject json = new JsonObject();
        if (record != null) {
            json.addProperty("name", record.name());
            json.addProperty("job", record.job().name());
            json.addProperty("jobDisplayName", record.job().getDisplayName());

            // Try to find the live entity for health/position data
            MCEntityCitizen citizen = null;
            for (Entity e : level.getEntities().getAll()) {
                if (e.getUUID().equals(citizenUUID) && e instanceof MCEntityCitizen ace) {
                    citizen = ace;
                    break;
                }
            }
            if (citizen != null) {
                json.addProperty("health", citizen.getHealth());
                json.addProperty("maxHealth", citizen.getMaxHealth());

                JsonObject posJson = new JsonObject();
                posJson.addProperty("x", citizen.getX());
                posJson.addProperty("y", citizen.getY());
                posJson.addProperty("z", citizen.getZ());
                json.add("position", posJson);
            }
        }

        CitizenSyncPayload payload = new CitizenSyncPayload(colonyId, citizenIdStr, json.toString());
        PacketDistributor.sendToPlayer(player, payload);
    }

    // ======================== Work Order Handler ========================

    /**
     * Handles work order actions from the client.
     */
    public static void handleWorkOrderAction(ServerPlayer player, String colonyId, String action, String jsonData) {
        ServerLevel level = (ServerLevel) player.level();
        FactionManager fm = FactionManager.get(level);

        String playerFaction = fm.getPlayerFaction(player.getUUID());
        if (playerFaction == null || !playerFaction.equals(colonyId)) return;

        WorkManager wm = WorkManager.get(level);

        switch (action) {
            case "cancel" -> {
                JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
                UUID orderId = UUID.fromString(json.get("orderId").getAsString());
                wm.removeOrder(orderId);
                wm.saveToDisk(level);
            }
            case "change_priority" -> {
                JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
                UUID orderId = UUID.fromString(json.get("orderId").getAsString());
                int priority = json.get("priority").getAsInt();
                ColonyWorkOrder order = wm.getOrder(orderId);
                if (order != null) {
                    order.setPriority(priority);
                    wm.saveToDisk(level);
                }
            }
            case "request" -> {
                // Client requesting full work order sync
                sendWorkOrderSync(player, colonyId);
                return;
            }
            default -> {}
        }

        // After any mutation, send updated work orders
        sendWorkOrderSync(player, colonyId);
    }

    /**
     * Sends full work order list to the player.
     */
    public static void sendWorkOrderSync(ServerPlayer player, String colonyId) {
        ServerLevel level = (ServerLevel) player.level();
        WorkManager wm = WorkManager.get(level);
        List<ColonyWorkOrder> orders = wm.getOrdersForColony(UUID.fromString(colonyId));

        JsonObject json = new JsonObject();
        JsonArray ordersArray = new JsonArray();
        for (ColonyWorkOrder order : orders) {
            JsonObject oJson = new JsonObject();
            oJson.addProperty("orderId", order.getOrderId().toString());
            oJson.addProperty("buildingId", order.getBuildingId());
            oJson.addProperty("type", order.getType().name());
            oJson.addProperty("targetLevel", order.getTargetLevel());
            oJson.addProperty("priority", order.getPriority());
            oJson.addProperty("claimed", order.isClaimed());
            oJson.addProperty("position", order.getPosition().asLong());
            if (order.getAssignedBuilderId() != null) {
                oJson.addProperty("assignedBuilderId", order.getAssignedBuilderId().toString());
            }
            ordersArray.add(oJson);
        }
        json.add("orders", ordersArray);

        WorkOrderSyncPayload payload = new WorkOrderSyncPayload(colonyId, "sync", json.toString());
        PacketDistributor.sendToPlayer(player, payload);
    }

    // ======================== Research Handler ========================

    /**
     * Handles research actions from the client.
     */
    public static void handleResearchAction(ServerPlayer player, String colonyId, String action, String jsonData) {
        ServerLevel level = (ServerLevel) player.level();
        FactionManager fm = FactionManager.get(level);

        String playerFaction = fm.getPlayerFaction(player.getUUID());
        if (playerFaction == null || !playerFaction.equals(colonyId)) return;

        ResearchManager rm = ResearchManager.get(level, colonyId);

        switch (action) {
            case "start" -> {
                JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
                String researchId = json.get("researchId").getAsString();
                net.minecraft.resources.Identifier resId = net.minecraft.resources.Identifier.tryParse(researchId);
                if (resId != null) {
                    rm.startResearch(resId, player);
                }
            }
            case "cancel", "reset" -> {
                JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
                String researchId = json.get("researchId").getAsString();
                net.minecraft.resources.Identifier resId = net.minecraft.resources.Identifier.tryParse(researchId);
                if (resId != null) {
                    rm.resetResearch(resId);
                }
            }
            case "request" -> {
                // Client requesting full research tree sync
                sendResearchSync(player, colonyId);
                return;
            }
            default -> {}
        }

        // After any mutation, send updated research state
        sendResearchSync(player, colonyId);
    }

    /**
     * Sends full research tree state to the player.
     * TODO: Research tree serialization to JSON pending implementation.
     */
    public static void sendResearchSync(ServerPlayer player, String colonyId) {
        JsonObject json = new JsonObject();
        json.add("branches", new JsonObject());

        ResearchSyncPayload payload = new ResearchSyncPayload(colonyId, "sync", json.toString());
        PacketDistributor.sendToPlayer(player, payload);
    }

    // ======================== Helpers ========================

    private static BlockPos parseBlockPos(JsonObject json, String key) {
        if (!json.has(key)) return BlockPos.ZERO;
        String posStr = json.get(key).getAsString();
        String[] parts = posStr.split(",");
        if (parts.length != 3) return BlockPos.ZERO;
        try {
            return new BlockPos(Integer.parseInt(parts[0].trim()),
                Integer.parseInt(parts[1].trim()),
                Integer.parseInt(parts[2].trim()));
        } catch (NumberFormatException e) {
            return BlockPos.ZERO;
        }
    }
}
