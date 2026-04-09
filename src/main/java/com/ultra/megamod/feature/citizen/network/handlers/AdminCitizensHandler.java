package com.ultra.megamod.feature.citizen.network.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.citizen.CitizenConfig;
import com.ultra.megamod.feature.citizen.CitizenManager;
import com.ultra.megamod.feature.citizen.data.*;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import com.ultra.megamod.feature.citizen.data.SiegeManager;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class AdminCitizensHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        if (!action.startsWith("admincitizen_")) return false;
        if (!AdminSystem.isAdmin(player)) return true; // Silently ignore non-admins

        switch (action) {
            case "admincitizen_request" -> handleRequest(player, level, eco);
            case "admincitizen_kill" -> handleKill(player, jsonData, level);
            case "admincitizen_heal" -> handleHeal(player, jsonData, level);
            case "admincitizen_teleport_to" -> handleTeleportTo(player, jsonData, level);
            case "admincitizen_teleport_here" -> handleTeleportHere(player, jsonData, level);
            case "admincitizen_set_owner" -> handleSetOwner(player, jsonData, level);
            case "admincitizen_toggle_ai" -> handleToggleAI(player, jsonData, level);
            case "admincitizen_faction_create" -> handleFactionCreate(player, jsonData, level);
            case "admincitizen_faction_delete" -> handleFactionDelete(player, jsonData, level);
            case "admincitizen_faction_set_relation" -> handleFactionSetRelation(player, jsonData, level);
            case "admincitizen_territory_unclaim" -> handleTerritoryUnclaim(player, jsonData, level);
            case "admincitizen_territory_transfer" -> handleTerritoryTransfer(player, jsonData, level);
            case "admincitizen_siege_start" -> handleSiegeStart(player, jsonData, level);
            case "admincitizen_siege_stop" -> handleSiegeStop(player, jsonData, level);
            case "admincitizen_upkeep_exempt" -> handleUpkeepExempt(player, jsonData, level);
            case "admincitizen_upkeep_toggle" -> handleUpkeepToggle(player, jsonData, level);
            case "admincitizen_config_update" -> handleConfigUpdate(player, jsonData, level);
            // Patrol controls
            case "admincitizen_patrol_toggle" -> handlePatrolToggle(player, level);
            case "admincitizen_patrol_set_interval" -> handlePatrolSetInterval(player, jsonData, level);
            case "admincitizen_patrol_set_chance" -> handlePatrolSetChance(player, jsonData, level);
            case "admincitizen_patrol_force_spawn" -> handlePatrolForceSpawn(player, level);
            case "admincitizen_patrol_force_spawn_type" -> handlePatrolForceSpawnType(player, jsonData, level);
            case "admincitizen_patrol_dismiss" -> handlePatrolDismiss(player, jsonData, level);
            case "admincitizen_patrol_dismiss_all" -> handlePatrolDismissAll(player, level);
            case "admincitizen_clear_unowned" -> handleClearUnowned(player, level);
            // Mass commands
            case "admincitizen_mass_recall" -> handleMassRecall(player, jsonData, level);
            case "admincitizen_mass_dismiss_idle" -> handleMassDismissIdle(player, jsonData, level);
            case "admincitizen_mass_heal" -> handleMassHeal(player, jsonData, level);
            case "admincitizen_mass_feed" -> handleMassFeed(player, jsonData, level);
            case "admincitizen_mass_kill" -> handleMassKill(player, jsonData, level);
            // Siege controls
            case "admincitizen_siege_force_attacker" -> handleSiegeForceAttacker(player, jsonData, level);
            case "admincitizen_siege_force_defender" -> handleSiegeForceDefender(player, jsonData, level);
            case "admincitizen_set_claim_health" -> handleSetClaimHealth(player, jsonData, level);
            case "admincitizen_siege_set_health" -> handleSiegeSetHealth(player, jsonData, level);
            case "admincitizen_siege_start_manual" -> handleSiegeStartManual(player, jsonData, level);
            case "admincitizen_force_transfer_territory" -> handleForceTransferTerritory(player, jsonData, level);
            // Faction bans
            case "admincitizen_faction_ban" -> handleFactionBan(player, jsonData, level);
            case "admincitizen_faction_unban" -> handleFactionUnban(player, jsonData, level);
            // Extended data request for controls tabs
            case "admincitizen_request_controls_data" -> handleRequestControlsData(player, level);
            default -> { return false; }
        }
        return true;
    }

    private static void handleRequest(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        CitizenManager cm = CitizenManager.get(level);
        FactionManager fm = FactionManager.get(level);

        JsonObject data = new JsonObject();
        data.addProperty("totalCitizens", cm.getTotalCitizenCount());
        data.addProperty("factionCount", fm.getFactionCount());

        // Citizens by owner
        JsonArray owners = new JsonArray();
        Map<UUID, String> nameCache = new HashMap<>();
        for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
            nameCache.put(p.getUUID(), p.getGameProfile().name());
        }

        for (var entry : cm.getAllCitizens().entrySet()) {
            JsonObject ownerObj = new JsonObject();
            ownerObj.addProperty("owner", entry.getKey().toString());
            ownerObj.addProperty("ownerName", nameCache.getOrDefault(entry.getKey(), "Offline"));
            ownerObj.addProperty("count", entry.getValue().size());

            int upkeep = cm.getTotalDailyUpkeep(entry.getKey());
            ownerObj.addProperty("dailyUpkeep", upkeep);

            JsonArray citizenArr = new JsonArray();
            for (var record : entry.getValue()) {
                JsonObject c = new JsonObject();
                c.addProperty("entityId", record.entityId().toString());
                c.addProperty("name", record.name());
                c.addProperty("job", record.job().getDisplayName());
                c.addProperty("jobId", record.job().name());
                citizenArr.add(c);
            }
            ownerObj.add("citizens", citizenArr);
            owners.add(ownerObj);
        }
        data.add("owners", owners);

        // Factions
        JsonArray factions = new JsonArray();
        for (FactionData fd : fm.getAllFactions()) {
            JsonObject f = new JsonObject();
            f.addProperty("id", fd.getFactionId());
            f.addProperty("name", fd.getDisplayName());
            f.addProperty("leader", fd.getLeaderUuid().toString());
            f.addProperty("members", fd.getMemberCount());
            factions.add(f);
        }
        data.add("factions", factions);

        // Config values
        JsonObject config = new JsonObject();
        config.addProperty("maxCitizens", CitizenConfig.MAX_CITIZENS_PER_PLAYER);
        config.addProperty("chunkLoading", CitizenConfig.CHUNK_LOADING_ENABLED);
        config.addProperty("upkeepInterval", CitizenConfig.UPKEEP_INTERVAL_TICKS);
        config.addProperty("upkeepFailure", CitizenConfig.UPKEEP_FAILURE_MODE);
        config.addProperty("hungerRate", CitizenConfig.HUNGER_RATE);
        config.addProperty("maxInventory", CitizenConfig.CITIZEN_MAX_INVENTORY);
        config.addProperty("xpMultiplier", CitizenConfig.RECRUIT_XP_MULTIPLIER);
        config.addProperty("globalUpkeep", cm.getUpkeepTracker().isGlobalUpkeepEnabled());
        data.add("config", config);

        // Active sieges
        JsonArray sieges = new JsonArray();
        for (var siege : SiegeManager.get(level).getActiveSieges()) {
            JsonObject s = new JsonObject();
            s.addProperty("attacker", siege.getAttackerFaction());
            s.addProperty("defender", siege.getDefenderFaction());
            s.addProperty("health", siege.getClaimHealth());
            sieges.add(s);
        }
        data.add("sieges", sieges);

        sendResponse(player, "admincitizen_data", data.toString());
    }

    private static void handleKill(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        UUID entityId = UUID.fromString(json.get("entityId").getAsString());
        findCitizenEntity(level, entityId).ifPresent(citizen -> {
            CitizenManager.get(level).unregisterCitizen(citizen.getUUID());
            citizen.kill((ServerLevel) citizen.level());
        });
    }

    private static void handleHeal(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        UUID entityId = UUID.fromString(json.get("entityId").getAsString());
        findCitizenEntity(level, entityId).ifPresent(citizen -> {
            citizen.setHealth(citizen.getMaxHealth());
            citizen.setHunger(CitizenConfig.MAX_HUNGER);
        });
    }

    private static void handleTeleportTo(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        UUID entityId = UUID.fromString(json.get("entityId").getAsString());
        findCitizenEntity(level, entityId).ifPresent(citizen -> {
            player.teleportTo(level, citizen.getX(), citizen.getY(), citizen.getZ(), Set.of(), player.getYRot(), player.getXRot(), false);
        });
    }

    private static void handleTeleportHere(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        UUID entityId = UUID.fromString(json.get("entityId").getAsString());
        findCitizenEntity(level, entityId).ifPresent(citizen -> {
            citizen.teleportTo(player.getX(), player.getY(), player.getZ());
        });
    }

    private static void handleSetOwner(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        UUID entityId = UUID.fromString(json.get("entityId").getAsString());
        UUID newOwner = UUID.fromString(json.get("newOwner").getAsString());
        findCitizenEntity(level, entityId).ifPresent(citizen -> {
            citizen.setOwnerUUID(newOwner);
        });
    }

    private static void handleToggleAI(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        UUID entityId = UUID.fromString(json.get("entityId").getAsString());
        findCitizenEntity(level, entityId).ifPresent(citizen -> {
            citizen.setNoAi(!citizen.isNoAi());
        });
    }

    private static void handleFactionCreate(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String factionId = json.get("id").getAsString();
        String name = json.get("name").getAsString();
        UUID leader = UUID.fromString(json.get("leader").getAsString());
        FactionManager.get(level).createFaction(factionId, name, leader);
    }

    private static void handleFactionDelete(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String factionId = json.get("id").getAsString();
        FactionManager.get(level).deleteFaction(factionId);
        DiplomacyManager.get(level).removeFaction(factionId);
        ClaimManager.get(level).removeFactionClaims(factionId);
        TreatyManager.get(level).removeFaction(factionId);
    }

    private static void handleFactionSetRelation(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String factionA = json.get("factionA").getAsString();
        String factionB = json.get("factionB").getAsString();
        DiplomacyStatus status = DiplomacyStatus.fromString(json.get("status").getAsString());
        DiplomacyManager.get(level).setRelation(factionA, factionB, status);
    }

    private static void handleTerritoryUnclaim(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String factionId = json.get("faction").getAsString();
        int chunkX = json.get("chunkX").getAsInt();
        int chunkZ = json.get("chunkZ").getAsInt();
        ClaimManager.get(level).unclaimChunk(factionId, chunkX, chunkZ);
    }

    private static void handleTerritoryTransfer(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String from = json.get("from").getAsString();
        String to = json.get("to").getAsString();
        ClaimManager.get(level).transferClaims(from, to);
    }

    private static void handleSiegeStart(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String attacker = json.get("attacker").getAsString();
        String defender = json.get("defender").getAsString();
        SiegeManager.get(level).startSiege(attacker, defender, level);
    }

    private static void handleSiegeStop(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String attacker = json.get("attacker").getAsString();
        String defender = json.get("defender").getAsString();
        SiegeManager.get(level).endSiege(attacker, defender);
    }

    private static void handleUpkeepExempt(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        UUID targetPlayer = UUID.fromString(json.get("player").getAsString());
        boolean exempt = json.get("exempt").getAsBoolean();
        CitizenManager.get(level).getUpkeepTracker().setExempt(targetPlayer, exempt);
    }

    private static void handleUpkeepToggle(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        boolean enabled = json.get("enabled").getAsBoolean();
        CitizenManager.get(level).getUpkeepTracker().setGlobalUpkeepEnabled(enabled);
    }

    private static void handleConfigUpdate(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        if (json.has("maxCitizens")) CitizenConfig.MAX_CITIZENS_PER_PLAYER = json.get("maxCitizens").getAsInt();
        if (json.has("chunkLoading")) CitizenConfig.CHUNK_LOADING_ENABLED = json.get("chunkLoading").getAsBoolean();
        if (json.has("upkeepInterval")) CitizenConfig.UPKEEP_INTERVAL_TICKS = json.get("upkeepInterval").getAsInt();
        if (json.has("upkeepFailure")) CitizenConfig.UPKEEP_FAILURE_MODE = json.get("upkeepFailure").getAsString();
        if (json.has("hungerRate")) CitizenConfig.HUNGER_RATE = json.get("hungerRate").getAsFloat();
        if (json.has("maxInventory")) CitizenConfig.CITIZEN_MAX_INVENTORY = json.get("maxInventory").getAsInt();
        if (json.has("xpMultiplier")) CitizenConfig.RECRUIT_XP_MULTIPLIER = json.get("xpMultiplier").getAsFloat();
        CitizenConfigManager.get(level).saveConfig(level);
    }

    // ═══════════════════════════════════════════
    //  PATROL CONTROLS
    // ═══════════════════════════════════════════

    private static void handlePatrolToggle(ServerPlayer player, ServerLevel level) {
        CitizenConfig.PATROL_SPAWN_ENABLED = !CitizenConfig.PATROL_SPAWN_ENABLED;
        CitizenConfigManager.get(level).saveConfig(level);
        handleRequestControlsData(player, level);
    }

    private static void handlePatrolSetInterval(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        int delta = json.has("delta") ? json.get("delta").getAsInt() : 0;
        CitizenConfig.PATROL_SPAWN_INTERVAL = Math.max(1000, CitizenConfig.PATROL_SPAWN_INTERVAL + delta);
        CitizenConfigManager.get(level).saveConfig(level);
        handleRequestControlsData(player, level);
    }

    private static void handlePatrolSetChance(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        float delta = json.has("delta") ? json.get("delta").getAsFloat() : 0;
        CitizenConfig.PATROL_SPAWN_CHANCE = Math.max(0.05f, Math.min(1.0f, CitizenConfig.PATROL_SPAWN_CHANCE + delta));
        CitizenConfigManager.get(level).saveConfig(level);
        handleRequestControlsData(player, level);
    }

    private static void handlePatrolForceSpawn(ServerPlayer player, ServerLevel level) {
        // Patrol system removed during MCEntityCitizen transition
        handleRequestControlsData(player, level);
    }

    private static void handlePatrolForceSpawnType(ServerPlayer player, String jsonData, ServerLevel level) {
        // Patrol system removed during MCEntityCitizen transition
        handleRequestControlsData(player, level);
    }

    private static void handlePatrolDismiss(ServerPlayer player, String jsonData, ServerLevel level) {
        // Patrol system removed during MCEntityCitizen transition
        handleRequestControlsData(player, level);
    }

    private static void handlePatrolDismissAll(ServerPlayer player, ServerLevel level) {
        // Patrol system removed during MCEntityCitizen transition
        handleRequestControlsData(player, level);
    }

    private static void handleClearUnowned(ServerPlayer player, ServerLevel level) {
        int removed = 0;
        List<MCEntityCitizen> toRemove = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof MCEntityCitizen citizen) {
                if (citizen.getOwnerUUID() == null) {
                    toRemove.add(citizen);
                }
            }
        }
        for (MCEntityCitizen citizen : toRemove) {
            citizen.discard();
            removed++;
        }
        // Send back updated data
        handleRequestControlsData(player, level);
    }

    // ═══════════════════════════════════════════
    //  MASS COMMANDS
    // ═══════════════════════════════════════════

    private static UUID resolveTargetPlayer(String playerName, ServerLevel level) {
        if (playerName == null || playerName.isEmpty()) return null;
        for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
            if (p.getGameProfile().name().equalsIgnoreCase(playerName)) {
                return p.getUUID();
            }
        }
        return null;
    }

    private static List<MCEntityCitizen> findCitizensForTarget(ServerLevel level, String playerName) {
        List<MCEntityCitizen> result = new ArrayList<>();
        boolean all = "all".equalsIgnoreCase(playerName);
        UUID targetUuid = all ? null : resolveTargetPlayer(playerName, level);

        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof MCEntityCitizen citizen) {
                UUID ownerUuid = citizen.getOwnerUUID();
                if (all) {
                    if (ownerUuid != null) result.add(citizen);
                } else if (targetUuid != null && targetUuid.equals(ownerUuid)) {
                    result.add(citizen);
                }
            }
        }
        return result;
    }

    private static void handleMassRecall(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String targetName = json.get("player").getAsString();

        if ("all".equalsIgnoreCase(targetName)) {
            // Recall all citizens to their respective owners
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof MCEntityCitizen citizen && citizen.getOwnerUUID() != null) {
                    ServerPlayer owner = level.getServer().getPlayerList().getPlayer(citizen.getOwnerUUID());
                    if (owner != null) {
                        citizen.teleportTo(owner.getX(), owner.getY(), owner.getZ());
                    }
                }
            }
        } else {
            UUID targetUuid = resolveTargetPlayer(targetName, level);
            if (targetUuid != null) {
                ServerPlayer target = level.getServer().getPlayerList().getPlayer(targetUuid);
                if (target != null) {
                    for (MCEntityCitizen citizen : findCitizensForTarget(level, targetName)) {
                        citizen.teleportTo(target.getX(), target.getY(), target.getZ());
                    }
                }
            }
        }
        handleRequestControlsData(player, level);
    }

    private static void handleMassDismissIdle(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String targetName = json.get("player").getAsString();

        List<MCEntityCitizen> citizens = findCitizensForTarget(level, targetName);
        CitizenManager cm = CitizenManager.get(level);
        for (MCEntityCitizen citizen : citizens) {
            if (citizen.isNoAi()) {
                cm.unregisterCitizen(citizen.getUUID());
                citizen.discard();
            }
        }
        handleRequestControlsData(player, level);
    }

    private static void handleMassHeal(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String targetName = json.get("player").getAsString();

        for (MCEntityCitizen citizen : findCitizensForTarget(level, targetName)) {
            citizen.setHealth(citizen.getMaxHealth());
        }
        handleRequestControlsData(player, level);
    }

    private static void handleMassFeed(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String targetName = json.get("player").getAsString();

        for (MCEntityCitizen citizen : findCitizensForTarget(level, targetName)) {
            citizen.setHunger(CitizenConfig.MAX_HUNGER);
        }
        handleRequestControlsData(player, level);
    }

    private static void handleMassKill(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String targetName = json.get("player").getAsString();

        CitizenManager cm = CitizenManager.get(level);
        List<MCEntityCitizen> citizens = findCitizensForTarget(level, targetName);
        for (MCEntityCitizen citizen : citizens) {
            cm.unregisterCitizen(citizen.getUUID());
            citizen.kill((ServerLevel) citizen.level());
        }
        handleRequestControlsData(player, level);
    }

    // ═══════════════════════════════════════════
    //  SIEGE CONTROLS
    // ═══════════════════════════════════════════

    private static void handleSiegeForceAttacker(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String attacker = json.get("attacker").getAsString();
        String defender = json.get("defender").getAsString();
        SiegeManager.get(level).forceWinAttacker(attacker, defender, level);
        handleRequestControlsData(player, level);
    }

    private static void handleSiegeForceDefender(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String attacker = json.get("attacker").getAsString();
        String defender = json.get("defender").getAsString();
        SiegeManager.get(level).forceWinDefender(attacker, defender, level);
        handleRequestControlsData(player, level);
    }

    private static void handleSetClaimHealth(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String attacker = json.get("attacker").getAsString();
        String defender = json.get("defender").getAsString();
        int health = json.get("health").getAsInt();
        SiegeManager.get(level).setSiegeClaimHealth(attacker, defender, health);
        handleRequestControlsData(player, level);
    }

    private static void handleSiegeSetHealth(ServerPlayer player, String jsonData, ServerLevel level) {
        try {
            JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
            String attacker = json.get("attacker").getAsString();
            String defender = json.get("defender").getAsString();
            int health = json.get("health").getAsInt();
            SiegeManager.get(level).setSiegeClaimHealth(attacker, defender, health);
        } catch (Exception e) {
            // Invalid input, ignore
        }
        handleRequestControlsData(player, level);
    }

    private static void handleSiegeStartManual(ServerPlayer player, String jsonData, ServerLevel level) {
        try {
            JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
            String attacker = json.get("attacker").getAsString();
            String defender = json.get("defender").getAsString();
            // Admin bypass: start siege without recruit requirement checks
            SiegeManager siegeManager = SiegeManager.get(level);
            // Check if siege already exists
            if (siegeManager.getSiege(attacker, defender) != null) return;
            // Force start (bypass normal validation)
            siegeManager.startSiege(attacker, defender, level);
        } catch (Exception e) {
            // Invalid input, ignore
        }
        handleRequestControlsData(player, level);
    }

    private static void handleForceTransferTerritory(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String from = json.get("from").getAsString();
        String to = json.get("to").getAsString();
        ClaimManager.get(level).transferClaims(from, to);
        handleRequestControlsData(player, level);
    }

    // ═══════════════════════════════════════════
    //  FACTION BANS
    // ═══════════════════════════════════════════

    private static void handleFactionBan(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String targetName = json.get("player").getAsString();
        UUID targetUuid = resolveTargetPlayer(targetName, level);
        if (targetUuid != null) {
            FactionManager.get(level).banPlayer(targetUuid);
            FactionManager.get(level).saveToDisk(level);
        }
        handleRequestControlsData(player, level);
    }

    private static void handleFactionUnban(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String uuid = json.get("uuid").getAsString();
        FactionManager.get(level).unbanPlayer(UUID.fromString(uuid));
        FactionManager.get(level).saveToDisk(level);
        handleRequestControlsData(player, level);
    }

    // ═══════════════════════════════════════════
    //  CONTROLS DATA REQUEST
    // ═══════════════════════════════════════════

    private static void handleRequestControlsData(ServerPlayer player, ServerLevel level) {
        JsonObject data = new JsonObject();

        // Patrol config
        JsonObject patrol = new JsonObject();
        patrol.addProperty("enabled", CitizenConfig.PATROL_SPAWN_ENABLED);
        patrol.addProperty("interval", CitizenConfig.PATROL_SPAWN_INTERVAL);
        patrol.addProperty("chance", CitizenConfig.PATROL_SPAWN_CHANCE);
        // Count unowned citizens
        int unownedCount = 0;
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof MCEntityCitizen citizen && citizen.getOwnerUUID() == null) {
                unownedCount++;
            }
        }
        patrol.addProperty("unownedCount", unownedCount);

        // Patrol system removed during MCEntityCitizen transition
        JsonArray patrolGroups = new JsonArray();
        patrol.add("groups", patrolGroups);
        patrol.addProperty("trackedPatrolCount", 0);
        patrol.addProperty("totalPatrolMembers", 0);

        data.add("patrol", patrol);

        // Sieges with full detail
        JsonArray sieges = new JsonArray();
        for (var siege : SiegeManager.get(level).getActiveSieges()) {
            JsonObject s = new JsonObject();
            s.addProperty("attacker", siege.getAttackerFaction());
            s.addProperty("defender", siege.getDefenderFaction());
            s.addProperty("health", siege.getClaimHealth());
            s.addProperty("maxHealth", siege.getMaxClaimHealth());
            long elapsedTicks = level.getServer().getTickCount() - siege.getStartTick();
            s.addProperty("elapsedSeconds", elapsedTicks / 20);
            s.addProperty("maxDurationSeconds", CitizenConfig.SIEGE_DURATION_TICKS / 20);
            float progress = siege.getMaxClaimHealth() > 0
                    ? 1.0f - ((float) siege.getClaimHealth() / siege.getMaxClaimHealth()) : 0f;
            s.addProperty("progress", Math.round(progress * 100));
            sieges.add(s);
        }
        data.add("sieges", sieges);

        // Siege config for the UI
        JsonObject siegeConfig = new JsonObject();
        siegeConfig.addProperty("durationTicks", CitizenConfig.SIEGE_DURATION_TICKS);
        siegeConfig.addProperty("healthDefault", CitizenConfig.SIEGE_HEALTH_DEFAULT);
        siegeConfig.addProperty("minRecruits", CitizenConfig.SIEGE_MIN_RECRUITS);
        siegeConfig.addProperty("requiresOwnerOnline", CitizenConfig.SIEGE_REQUIRES_OWNER_ONLINE);
        data.add("siegeConfig", siegeConfig);

        // Factions list (for transfer dropdowns)
        JsonArray factionList = new JsonArray();
        for (FactionData fd : FactionManager.get(level).getAllFactions()) {
            JsonObject f = new JsonObject();
            f.addProperty("id", fd.getFactionId());
            f.addProperty("name", fd.getDisplayName());
            factionList.add(f);
        }
        data.add("factions", factionList);

        // Banned players
        JsonArray banned = new JsonArray();
        Map<UUID, String> nameCache = new HashMap<>();
        for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
            nameCache.put(p.getUUID(), p.getGameProfile().name());
        }
        for (UUID bannedUuid : FactionManager.get(level).getBannedPlayers()) {
            JsonObject b = new JsonObject();
            b.addProperty("uuid", bannedUuid.toString());
            b.addProperty("name", nameCache.getOrDefault(bannedUuid, bannedUuid.toString().substring(0, 8) + "..."));
            banned.add(b);
        }
        data.add("bannedPlayers", banned);

        // Online players list (for mass commands selector)
        JsonArray players = new JsonArray();
        for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
            players.add(p.getGameProfile().name());
        }
        data.add("onlinePlayers", players);

        sendResponse(player, "admincitizen_controls_data", data.toString());
    }

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
}
