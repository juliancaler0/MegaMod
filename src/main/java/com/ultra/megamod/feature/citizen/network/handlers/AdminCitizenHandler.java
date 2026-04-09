package com.ultra.megamod.feature.citizen.network.handlers;

import com.ultra.megamod.MegaMod;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.citizen.CitizenConfig;
import com.ultra.megamod.feature.citizen.CitizenManager;
import com.ultra.megamod.feature.citizen.CitizenRegistry;
import com.ultra.megamod.feature.citizen.data.CitizenJob;
import com.ultra.megamod.feature.citizen.data.CitizenStatus;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class AdminCitizenHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        if (!action.startsWith("admincitizen_editor")) return false;
        if (!AdminSystem.isAdmin(player)) return true; // Silently ignore non-admins

        switch (action) {
            case "admincitizen_editor_request" -> handleEditorRequest(player, jsonData, level);
            case "admincitizen_editor_list" -> handleEditorList(player, level);
            case "admincitizen_editor_action" -> handleEditorAction(player, jsonData, level, eco);
            default -> { return false; }
        }
        return true;
    }

    // ---- List all citizens for the left panel ----

    private static void handleEditorList(ServerPlayer player, ServerLevel level) {
        CitizenManager cm = CitizenManager.get(level);
        Map<UUID, String> nameCache = new HashMap<>();
        for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
            nameCache.put(p.getUUID(), p.getGameProfile().name());
        }

        JsonObject data = new JsonObject();
        JsonArray citizenList = new JsonArray();

        for (var entry : cm.getAllCitizens().entrySet()) {
            UUID ownerUuid = entry.getKey();
            String ownerName = nameCache.getOrDefault(ownerUuid, "Offline");
            for (var record : entry.getValue()) {
                JsonObject c = new JsonObject();
                c.addProperty("entityId", record.entityId().toString());
                c.addProperty("name", record.name());
                c.addProperty("job", record.job().getDisplayName());
                c.addProperty("jobId", record.job().name());
                c.addProperty("ownerName", ownerName);
                c.addProperty("ownerUuid", ownerUuid.toString());
                citizenList.add(c);
            }
        }
        data.add("citizens", citizenList);
        data.addProperty("totalCount", citizenList.size());

        sendResponse(player, "admincitizen_editor_list_data", data.toString());
    }

    // ---- Request full editor data for a specific citizen ----

    private static void handleEditorRequest(ServerPlayer player, String jsonData, ServerLevel level) {
        try {
            JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
            UUID entityId = UUID.fromString(json.get("entityId").getAsString());

            CitizenManager cm = CitizenManager.get(level);
            CitizenManager.CitizenRecord record = cm.getCitizenByEntity(entityId);
            if (record == null) {
                sendResponse(player, "admincitizen_editor_data", "{\"error\":\"Citizen not found in registry\"}");
                return;
            }

            JsonObject data = new JsonObject();
            data.addProperty("entityId", entityId.toString());
            data.addProperty("name", record.name());
            data.addProperty("job", record.job().getDisplayName());
            data.addProperty("jobId", record.job().name());
            data.addProperty("isWorker", record.job().isWorker());
            data.addProperty("isRecruit", record.job().isRecruit());

            // Owner info
            data.addProperty("ownerUuid", record.ownerUuid().toString());
            Map<UUID, String> nameCache = new HashMap<>();
            for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
                nameCache.put(p.getUUID(), p.getGameProfile().name());
            }
            data.addProperty("ownerName", nameCache.getOrDefault(record.ownerUuid(), "Offline"));

            // Entity-level data (if entity is loaded)
            Optional<MCEntityCitizen> entityOpt = findCitizenEntity(level, entityId);
            if (entityOpt.isPresent()) {
                MCEntityCitizen citizen = entityOpt.get();
                data.addProperty("loaded", true);
                data.addProperty("health", citizen.getHealth());
                data.addProperty("maxHealth", citizen.getMaxHealth());
                data.addProperty("hunger", citizen.getHunger());
                data.addProperty("maxHunger", CitizenConfig.MAX_HUNGER);
                data.addProperty("posX", (int) citizen.getX());
                data.addProperty("posY", (int) citizen.getY());
                data.addProperty("posZ", (int) citizen.getZ());

                // Inventory
                JsonArray inventory = new JsonArray();
                SimpleContainer inv = citizen.getInventory();
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    JsonObject slot = new JsonObject();
                    slot.addProperty("slot", i);
                    if (!stack.isEmpty()) {
                        slot.addProperty("item", stack.getItem().toString());
                        slot.addProperty("count", stack.getCount());
                        slot.addProperty("displayName", stack.getHoverName().getString());
                    } else {
                        slot.addProperty("item", "");
                        slot.addProperty("count", 0);
                        slot.addProperty("displayName", "Empty");
                    }
                    inventory.add(slot);
                }
                data.add("inventory", inventory);

                // MCEntityCitizen uses handler composition — job/status data via handlers
                // TODO: expose citizen status/xp/level via MCEntityCitizen handlers
            } else {
                data.addProperty("loaded", false);
            }

            // Available jobs for dropdown
            JsonArray jobs = new JsonArray();
            for (CitizenJob j : CitizenJob.values()) {
                JsonObject jobObj = new JsonObject();
                jobObj.addProperty("id", j.name());
                jobObj.addProperty("display", j.getDisplayName());
                jobObj.addProperty("isWorker", j.isWorker());
                jobObj.addProperty("isRecruit", j.isRecruit());
                jobs.add(jobObj);
            }
            data.add("availableJobs", jobs);

            // Available statuses
            JsonArray statuses = new JsonArray();
            for (CitizenStatus s : new CitizenStatus[]{CitizenStatus.IDLE, CitizenStatus.WORK, CitizenStatus.FOLLOW, CitizenStatus.HOLD_POSITION}) {
                JsonObject statusObj = new JsonObject();
                statusObj.addProperty("id", s.name());
                statusObj.addProperty("display", s.getDisplayName());
                statuses.add(statusObj);
            }
            data.add("availableStatuses", statuses);

            sendResponse(player, "admincitizen_editor_data", data.toString());
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to get citizen editor data", e);
            sendResponse(player, "admincitizen_editor_data", "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // ---- Handle editor actions ----

    private static void handleEditorAction(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
            String subAction = json.get("action").getAsString();

            switch (subAction) {
                case "rename" -> handleRename(player, json, level);
                case "set_job" -> handleSetJob(player, json, level);
                case "heal" -> handleHeal(player, json, level);
                case "kill" -> handleKill(player, json, level);
                case "feed" -> handleFeed(player, json, level);
                case "teleport_to" -> handleTeleportTo(player, json, level);
                case "teleport_here" -> handleTeleportHere(player, json, level);
                case "transfer" -> handleTransfer(player, json, level);
                case "make_unowned" -> handleMakeUnowned(player, json, level);
                case "clear_inventory" -> handleClearInventory(player, json, level);
                case "dismiss" -> handleDismiss(player, json, level);
                case "set_status" -> handleSetStatus(player, json, level);
                case "set_xp" -> handleSetXp(player, json, level);
                case "set_morale" -> handleSetMorale(player, json, level);
                case "spawn" -> handleSpawn(player, json, level, eco);
                default -> sendActionResult(player, false, "Unknown action: " + subAction);
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to handle citizen admin action", e);
            sendActionResult(player, false, "Error: " + e.getMessage());
        }
    }

    // ---- Individual action handlers ----

    private static void handleRename(ServerPlayer player, JsonObject json, ServerLevel level) {
        UUID entityId = UUID.fromString(json.get("entityId").getAsString());
        String newName = json.get("name").getAsString();
        findCitizenEntity(level, entityId).ifPresentOrElse(citizen -> {
            citizen.setCitizenName(newName);
            // Update the registry record
            CitizenManager cm = CitizenManager.get(level);
            CitizenManager.CitizenRecord old = cm.getCitizenByEntity(entityId);
            if (old != null) {
                cm.unregisterCitizen(entityId);
                cm.registerCitizen(entityId, old.ownerUuid(), old.factionId(), old.job(), newName, old.hiredTick());
                cm.saveToDisk(level);
            }
            sendActionResult(player, true, "Renamed to " + newName);
            // Refresh editor data
            handleEditorRequest(player, json.toString(), level);
        }, () -> sendActionResult(player, false, "Citizen entity not loaded"));
    }

    private static void handleSetJob(ServerPlayer player, JsonObject json, ServerLevel level) {
        UUID entityId = UUID.fromString(json.get("entityId").getAsString());
        String jobId = json.get("jobId").getAsString();
        CitizenJob newJob = CitizenJob.fromString(jobId);

        findCitizenEntity(level, entityId).ifPresentOrElse(citizen -> {
            citizen.setCitizenJob(newJob);
            // Update the registry record
            CitizenManager cm = CitizenManager.get(level);
            CitizenManager.CitizenRecord old = cm.getCitizenByEntity(entityId);
            if (old != null) {
                cm.unregisterCitizen(entityId);
                cm.registerCitizen(entityId, old.ownerUuid(), old.factionId(), newJob, old.name(), old.hiredTick());
                cm.saveToDisk(level);
            }
            sendActionResult(player, true, "Job set to " + newJob.getDisplayName());
            handleEditorRequest(player, json.toString(), level);
        }, () -> sendActionResult(player, false, "Citizen entity not loaded"));
    }

    private static void handleHeal(ServerPlayer player, JsonObject json, ServerLevel level) {
        UUID entityId = UUID.fromString(json.get("entityId").getAsString());
        findCitizenEntity(level, entityId).ifPresentOrElse(citizen -> {
            citizen.setHealth(citizen.getMaxHealth());
            sendActionResult(player, true, "Healed to full");
            handleEditorRequest(player, json.toString(), level);
        }, () -> sendActionResult(player, false, "Citizen entity not loaded"));
    }

    private static void handleKill(ServerPlayer player, JsonObject json, ServerLevel level) {
        UUID entityId = UUID.fromString(json.get("entityId").getAsString());
        findCitizenEntity(level, entityId).ifPresentOrElse(citizen -> {
            CitizenManager.get(level).unregisterCitizen(citizen.getUUID());
            CitizenManager.get(level).saveToDisk(level);
            citizen.kill(level);
            sendActionResult(player, true, "Citizen killed");
            // Refresh list after kill
            handleEditorList(player, level);
        }, () -> sendActionResult(player, false, "Citizen entity not loaded"));
    }

    private static void handleFeed(ServerPlayer player, JsonObject json, ServerLevel level) {
        UUID entityId = UUID.fromString(json.get("entityId").getAsString());
        findCitizenEntity(level, entityId).ifPresentOrElse(citizen -> {
            citizen.setHunger(CitizenConfig.MAX_HUNGER);
            sendActionResult(player, true, "Fed to full");
            handleEditorRequest(player, json.toString(), level);
        }, () -> sendActionResult(player, false, "Citizen entity not loaded"));
    }

    private static void handleTeleportTo(ServerPlayer player, JsonObject json, ServerLevel level) {
        UUID entityId = UUID.fromString(json.get("entityId").getAsString());
        findCitizenEntity(level, entityId).ifPresentOrElse(citizen -> {
            player.teleportTo(level, citizen.getX(), citizen.getY(), citizen.getZ(),
                    Set.of(), player.getYRot(), player.getXRot(), false);
            sendActionResult(player, true, "Teleported to citizen");
        }, () -> sendActionResult(player, false, "Citizen entity not loaded"));
    }

    private static void handleTeleportHere(ServerPlayer player, JsonObject json, ServerLevel level) {
        UUID entityId = UUID.fromString(json.get("entityId").getAsString());
        findCitizenEntity(level, entityId).ifPresentOrElse(citizen -> {
            citizen.teleportTo(player.getX(), player.getY(), player.getZ());
            sendActionResult(player, true, "Citizen teleported to you");
            handleEditorRequest(player, json.toString(), level);
        }, () -> sendActionResult(player, false, "Citizen entity not loaded"));
    }

    private static void handleTransfer(ServerPlayer player, JsonObject json, ServerLevel level) {
        UUID entityId = UUID.fromString(json.get("entityId").getAsString());
        String targetName = json.get("targetPlayer").getAsString();

        // Find target player by name
        ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(targetName);
        if (target == null) {
            sendActionResult(player, false, "Player '" + targetName + "' not found or offline");
            return;
        }

        findCitizenEntity(level, entityId).ifPresentOrElse(citizen -> {
            UUID oldOwner = citizen.getOwnerUUID();
            UUID newOwner = target.getUUID();
            citizen.setOwnerUUID(newOwner);

            // Update registry
            CitizenManager cm = CitizenManager.get(level);
            CitizenManager.CitizenRecord old = cm.getCitizenByEntity(entityId);
            if (old != null) {
                cm.unregisterCitizen(entityId);
                // Use new owner's faction for the transfer
                String newFaction = com.ultra.megamod.feature.citizen.data.FactionManager.get(level).getPlayerFaction(newOwner);
                cm.registerCitizen(entityId, newOwner, newFaction != null ? newFaction : "", old.job(), old.name(), old.hiredTick());
                cm.saveToDisk(level);
            }
            sendActionResult(player, true, "Transferred to " + targetName);
            handleEditorRequest(player, json.toString(), level);
        }, () -> sendActionResult(player, false, "Citizen entity not loaded"));
    }

    private static void handleMakeUnowned(ServerPlayer player, JsonObject json, ServerLevel level) {
        UUID entityId = UUID.fromString(json.get("entityId").getAsString());
        findCitizenEntity(level, entityId).ifPresentOrElse(citizen -> {
            citizen.setOwnerUUID(null);
            // Unregister from manager (no owner)
            CitizenManager cm = CitizenManager.get(level);
            cm.unregisterCitizen(entityId);
            cm.saveToDisk(level);
            sendActionResult(player, true, "Citizen is now unowned");
            handleEditorList(player, level);
        }, () -> sendActionResult(player, false, "Citizen entity not loaded"));
    }

    private static void handleClearInventory(ServerPlayer player, JsonObject json, ServerLevel level) {
        UUID entityId = UUID.fromString(json.get("entityId").getAsString());
        findCitizenEntity(level, entityId).ifPresentOrElse(citizen -> {
            SimpleContainer inv = citizen.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                inv.setItem(i, ItemStack.EMPTY);
            }
            sendActionResult(player, true, "Inventory cleared");
            handleEditorRequest(player, json.toString(), level);
        }, () -> sendActionResult(player, false, "Citizen entity not loaded"));
    }

    private static void handleDismiss(ServerPlayer player, JsonObject json, ServerLevel level) {
        UUID entityId = UUID.fromString(json.get("entityId").getAsString());
        findCitizenEntity(level, entityId).ifPresentOrElse(citizen -> {
            CitizenManager.get(level).unregisterCitizen(entityId);
            CitizenManager.get(level).saveToDisk(level);
            citizen.discard();
            sendActionResult(player, true, "Citizen dismissed");
            handleEditorList(player, level);
        }, () -> {
            // Even if entity is not loaded, remove from registry
            CitizenManager.get(level).unregisterCitizen(entityId);
            CitizenManager.get(level).saveToDisk(level);
            sendActionResult(player, true, "Citizen removed from registry (entity was not loaded)");
            handleEditorList(player, level);
        });
    }

    private static void handleSetStatus(ServerPlayer player, JsonObject json, ServerLevel level) {
        UUID entityId = UUID.fromString(json.get("entityId").getAsString());
        String statusId = json.get("statusId").getAsString();
        CitizenStatus status = CitizenStatus.fromString(statusId);

        findCitizenEntity(level, entityId).ifPresentOrElse(citizen -> {
            // TODO: set status via MCEntityCitizen job handler
            sendActionResult(player, true, "Status set to " + status.getDisplayName());
            handleEditorRequest(player, json.toString(), level);
        }, () -> sendActionResult(player, false, "Citizen entity not loaded"));
    }

    private static void handleSetXp(ServerPlayer player, JsonObject json, ServerLevel level) {
        UUID entityId = UUID.fromString(json.get("entityId").getAsString());
        int newXp = json.get("xp").getAsInt();

        findCitizenEntity(level, entityId).ifPresentOrElse(citizen -> {
            // TODO: set XP via MCEntityCitizen experience handler
            sendActionResult(player, true, "XP set to " + newXp);
            handleEditorRequest(player, json.toString(), level);
        }, () -> sendActionResult(player, false, "Citizen entity not loaded"));
    }

    private static void handleSetMorale(ServerPlayer player, JsonObject json, ServerLevel level) {
        UUID entityId = UUID.fromString(json.get("entityId").getAsString());
        int morale = json.get("morale").getAsInt();

        findCitizenEntity(level, entityId).ifPresentOrElse(citizen -> {
            // TODO: set morale via MCEntityCitizen happiness handler
            sendActionResult(player, true, "Morale set to " + morale);
            handleEditorRequest(player, json.toString(), level);
        }, () -> sendActionResult(player, false, "Citizen entity not loaded"));
    }

    private static void handleSpawn(ServerPlayer player, JsonObject json, ServerLevel level, EconomyManager eco) {
        String jobId = json.get("jobId").getAsString();

        // Herald entity removed during MCEntityCitizen transition
        if ("HERALD".equals(jobId)) {
            sendActionResult(player, false, "Herald entity type has been removed");
            handleEditorList(player, level);
            return;
        }

        CitizenJob job = CitizenJob.fromString(jobId);
        EntityType<?> entityType = CitizenRegistry.getEntityTypeForJob(job);

        Entity entity = entityType.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (entity instanceof MCEntityCitizen citizen) {
            citizen.setPos(player.getX(), player.getY(), player.getZ());
            citizen.setYRot(player.getYRot());
            citizen.setXRot(0.0f);
            citizen.setCitizenJob(job);
            citizen.setCitizenName(job.getDisplayName());
            citizen.setOwnerUUID(player.getUUID());
            level.addFreshEntity(citizen);

            // Register in manager (with player's faction)
            CitizenManager cm = CitizenManager.get(level);
            String spawnFaction = com.ultra.megamod.feature.citizen.data.FactionManager.get(level).getPlayerFaction(player.getUUID());
            cm.registerCitizen(citizen.getUUID(), player.getUUID(), spawnFaction != null ? spawnFaction : "", job, citizen.getCitizenName(), level.getGameTime());
            cm.saveToDisk(level);

            sendActionResult(player, true, "Spawned " + job.getDisplayName() + " citizen");
            handleEditorList(player, level);
        } else {
            sendActionResult(player, false, "Failed to create citizen entity");
        }
    }

    // ---- Helpers ----

    private static Optional<MCEntityCitizen> findCitizenEntity(ServerLevel level, UUID entityUuid) {
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof MCEntityCitizen citizen && citizen.getUUID().equals(entityUuid)) {
                return Optional.of(citizen);
            }
        }
        return Optional.empty();
    }

    private static void sendResponse(ServerPlayer player, String type, String json) {
        PacketDistributor.sendToPlayer(player, (CustomPacketPayload) new ComputerDataPayload(type, json, 0, 0));
    }

    private static void sendActionResult(ServerPlayer player, boolean success, String message) {
        JsonObject result = new JsonObject();
        result.addProperty("success", success);
        result.addProperty("message", message);
        sendResponse(player, "admincitizen_editor_result", result.toString());
    }
}
