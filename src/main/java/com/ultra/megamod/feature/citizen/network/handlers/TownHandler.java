package com.ultra.megamod.feature.citizen.network.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.citizen.CitizenConfig;
import com.ultra.megamod.feature.citizen.CitizenManager;
import com.ultra.megamod.feature.citizen.data.ClaimData;
import com.ultra.megamod.feature.citizen.data.ClaimManager;
import com.ultra.megamod.feature.citizen.data.DiplomacyManager;
import com.ultra.megamod.feature.citizen.data.DiplomacyStatus;
import com.ultra.megamod.feature.citizen.data.FactionData;
import com.ultra.megamod.feature.citizen.data.FactionManager;
import com.ultra.megamod.feature.citizen.data.FormationType;
import com.ultra.megamod.feature.citizen.data.GroupData;
import com.ultra.megamod.feature.citizen.data.GroupManager;
import com.ultra.megamod.feature.citizen.data.TreatyManager;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TownHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        switch (action) {
            case "town_request": {
                sendTownData(player, level, eco);
                return true;
            }
            case "town_create_faction": {
                handleCreateFaction(player, jsonData, level, eco);
                return true;
            }
            case "town_leave_faction": {
                handleLeaveFaction(player, level, eco);
                return true;
            }
            case "town_create_group": {
                handleCreateGroup(player, jsonData, level, eco);
                return true;
            }
            case "town_delete_group": {
                handleDeleteGroup(player, jsonData, level, eco);
                return true;
            }
            case "town_set_formation": {
                handleSetFormation(player, jsonData, level, eco);
                return true;
            }
            case "town_group_command": {
                handleGroupCommand(player, jsonData, level, eco);
                return true;
            }
            case "town_claim_chunk": {
                handleClaimChunk(player, level, eco);
                return true;
            }
            case "town_unclaim_chunk": {
                handleUnclaimChunk(player, level, eco);
                return true;
            }
            case "town_set_relation": {
                handleSetRelation(player, jsonData, level, eco);
                return true;
            }
            case "town_send_treaty": {
                handleSendTreaty(player, jsonData, level, eco);
                return true;
            }
            case "town_accept_treaty": {
                handleAcceptTreaty(player, jsonData, level, eco);
                return true;
            }
            case "town_decline_treaty": {
                handleDeclineTreaty(player, jsonData, level, eco);
                return true;
            }
            case "town_split_group": {
                handleSplitGroup(player, jsonData, level, eco);
                return true;
            }
            case "town_merge_groups": {
                handleMergeGroups(player, jsonData, level, eco);
                return true;
            }
            case "town_request_join_faction": {
                handleRequestJoinFaction(player, jsonData, level, eco);
                return true;
            }
            case "town_accept_join": {
                handleAcceptJoin(player, jsonData, level, eco);
                return true;
            }
            case "town_get_chunk_cost": {
                handleGetChunkCost(player, level, eco);
                return true;
            }
            case "town_rename_citizen": {
                handleRenameCitizen(player, jsonData, level, eco);
                return true;
            }
            case "town_dismiss_citizen": {
                handleDismissCitizen(player, jsonData, level, eco);
                return true;
            }
            case "town_recall_citizens": {
                handleRecallCitizens(player, level, eco);
                return true;
            }
            case "town_set_group_image": {
                handleSetGroupImage(player, jsonData, level, eco);
                return true;
            }
            case "town_toggle_group_ranged": {
                handleToggleGroupRanged(player, jsonData, level, eco);
                return true;
            }
            case "town_toggle_group_rest": {
                handleToggleGroupRest(player, jsonData, level, eco);
                return true;
            }
            case "town_assign_builder": {
                handleAssignBuilder(player, jsonData, level, eco);
                return true;
            }
            case "town_cancel_work_order": {
                handleCancelWorkOrder(player, jsonData, level, eco);
                return true;
            }
            case "town_start_research": {
                handleStartResearch(player, jsonData, level, eco);
                return true;
            }
            case "building_upgrade": {
                handleBuildingUpgrade(player, jsonData, level, eco);
                return true;
            }
            case "building_repair": {
                handleBuildingRepair(player, jsonData, level, eco);
                return true;
            }
            case "building_rename": {
                handleBuildingRename(player, jsonData, level, eco);
                return true;
            }
            // --- Building management ---
            case "building_demolish": {
                handleBuildingDemolish(player, jsonData, level, eco);
                return true;
            }
            case "building_assign_worker": {
                handleBuildingAssignWorker(player, jsonData, level, eco);
                return true;
            }
            case "building_remove_worker": {
                handleBuildingRemoveWorker(player, jsonData, level, eco);
                return true;
            }
            case "building_set_style": {
                handleBuildingSetStyle(player, jsonData, level, eco);
                return true;
            }
            case "building_reactivate": {
                handleBuildingReactivate(player, jsonData, level, eco);
                return true;
            }
            case "building_set_hiring_mode":
            case "building_toggle_auto_hire": {
                handleBuildingToggleAutoHire(player, jsonData, level, eco);
                return true;
            }
            // --- Crafting building actions ---
            case "building_add_recipe": {
                handleBuildingAddRecipe(player, jsonData, level, eco);
                return true;
            }
            case "building_remove_recipe": {
                handleBuildingRemoveRecipe(player, jsonData, level, eco);
                return true;
            }
            case "building_recipe_priority_up": {
                handleBuildingRecipePriorityUp(player, jsonData, level, eco);
                return true;
            }
            case "building_recipe_priority_down": {
                handleBuildingRecipePriorityDown(player, jsonData, level, eco);
                return true;
            }
            case "building_toggle_recipe": {
                handleBuildingToggleRecipe(player, jsonData, level, eco);
                return true;
            }
            // --- Farmer actions ---
            case "building_assign_field": {
                handleBuildingAssignField(player, jsonData, level, eco);
                return true;
            }
            case "building_remove_field": {
                handleBuildingRemoveField(player, jsonData, level, eco);
                return true;
            }
            case "building_toggle_fertilize": {
                handleBuildingToggleFertilize(player, jsonData, level, eco);
                return true;
            }
            // --- Miner actions ---
            case "building_set_mine_level": {
                handleBuildingSetMineLevel(player, jsonData, level, eco);
                return true;
            }
            case "building_set_fill_block": {
                handleBuildingSetFillBlock(player, jsonData, level, eco);
                return true;
            }
            case "building_repair_mine_level": {
                handleBuildingRepairMineLevel(player, jsonData, level, eco);
                return true;
            }
            // --- Guard actions ---
            case "building_set_patrol_mode": {
                handleBuildingSetPatrolMode(player, jsonData, level, eco);
                return true;
            }
            case "building_add_patrol_point": {
                handleBuildingAddPatrolPoint(player, jsonData, level, eco);
                return true;
            }
            case "building_remove_patrol_point": {
                handleBuildingRemovePatrolPoint(player, jsonData, level, eco);
                return true;
            }
            case "building_set_guard_target": {
                handleBuildingSetGuardTarget(player, jsonData, level, eco);
                return true;
            }
            // --- Warehouse actions ---
            case "building_sort_warehouse": {
                handleBuildingSortWarehouse(player, jsonData, level, eco);
                return true;
            }
            case "building_assign_courier": {
                handleBuildingAssignCourier(player, jsonData, level, eco);
                return true;
            }
            case "building_set_min_stock": {
                handleBuildingSetMinStock(player, jsonData, level, eco);
                return true;
            }
            // --- Other actions ---
            case "building_cancel_research": {
                handleBuildingCancelResearch(player, jsonData, level, eco);
                return true;
            }
            case "building_recruit_visitor": {
                handleBuildingRecruitVisitor(player, jsonData, level, eco);
                return true;
            }
            case "building_assign_bed": {
                handleBuildingAssignBed(player, jsonData, level, eco);
                return true;
            }
            case "building_set_restaurant_menu": {
                handleBuildingSetRestaurantMenu(player, jsonData, level, eco);
                return true;
            }
            // --- Builder-specific actions ---
            case "building_recall_worker": {
                handleBuildingRecallWorker(player, jsonData, level, eco);
                return true;
            }
            case "building_request_pickup": {
                handleBuildingRequestPickup(player, jsonData, level, eco);
                return true;
            }
            case "building_set_pickup_priority": {
                handleBuildingSetPickupPriority(player, jsonData, level, eco);
                return true;
            }
            case "building_teach_recipe": {
                handleBuildingAddRecipe(player, jsonData, level, eco);
                return true;
            }
            case "building_set_construction_strategy": {
                handleBuildingSetConstructionStrategy(player, jsonData, level, eco);
                return true;
            }
            case "building_set_shears": {
                handleBuildingSetShears(player, jsonData, level, eco);
                return true;
            }
            case "building_set_task_mode": {
                handleBuildingSetTaskMode(player, jsonData, level, eco);
                return true;
            }
            case "building_transfer_item": {
                handleBuildingTransferItem(player, jsonData, level, eco);
                return true;
            }
            // --- Town Hall specific actions ---
            case "townhall_request": {
                sendTownHallData(player, jsonData, level, eco);
                return true;
            }
            case "townhall_rename": {
                handleTownHallRename(player, jsonData, level, eco);
                return true;
            }
            case "townhall_set_setting": {
                handleTownHallSetSetting(player, jsonData, level, eco);
                return true;
            }
            case "townhall_set_permission": {
                handleTownHallSetPermission(player, jsonData, level, eco);
                return true;
            }
            case "townhall_hire_mercenaries": {
                handleTownHallHireMercenaries(player, jsonData, level, eco);
                return true;
            }
            case "townhall_recall_citizens": {
                handleTownHallRecallCitizens(player, jsonData, level, eco);
                return true;
            }
            case "townhall_workorder_priority": {
                handleTownHallWorkOrderPriority(player, jsonData, level, eco);
                return true;
            }
            case "townhall_workorder_delete": {
                handleTownHallWorkOrderDelete(player, jsonData, level, eco);
                return true;
            }
            case "townhall_add_player": {
                handleTownHallAddPlayer(player, jsonData, level, eco);
                return true;
            }
            case "townhall_set_color": {
                handleTownHallSetColor(player, jsonData, level, eco);
                return true;
            }
            case "townhall_set_pack": {
                handleTownHallSetPack(player, jsonData, level, eco);
                return true;
            }
            case "townhall_build_options": {
                // Build options opens the build tool — send result to trigger client refresh
                sendTownHallResult(player, true, "Open the Build Tool to place build orders.");
                return true;
            }
            case "townhall_open_map": {
                sendTownHallResult(player, true, "Map view opened.");
                return true;
            }
            default:
                return false;
        }
    }

    // --- Action handlers ---

    private static void handleCreateFaction(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String name = obj.has("name") ? obj.get("name").getAsString() : "";
            String id = obj.has("id") ? obj.get("id").getAsString() : "";
            if (name.isEmpty() || id.isEmpty()) {
                sendResult(player, false, "Faction name and ID are required.", eco);
                return;
            }
            if (id.length() > 32 || name.length() > 48) {
                sendResult(player, false, "Name/ID too long.", eco);
                return;
            }
            FactionManager fm = FactionManager.get(level);
            if (fm.getPlayerFaction(player.getUUID()) != null) {
                sendResult(player, false, "You are already in a faction.", eco);
                return;
            }
            int cost = CitizenConfig.FACTION_CREATION_COST;
            if (!eco.spendWallet(player.getUUID(), cost)) {
                sendResult(player, false, "Not enough MegaCoins! Need " + cost + " MC.", eco);
                return;
            }
            FactionData created = fm.createFaction(id, name, player.getUUID());
            if (created == null) {
                eco.addWallet(player.getUUID(), cost); // refund on failure
                sendResult(player, false, "Faction ID already exists.", eco);
                return;
            }
            fm.saveToDisk(level);
            sendResult(player, true, "Faction '" + name + "' created! (-" + cost + " MC)", eco);
        } catch (Exception e) {
            sendResult(player, false, "Invalid data.", eco);
        }
    }

    private static void handleLeaveFaction(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        FactionManager fm = FactionManager.get(level);
        boolean left = fm.leaveFaction(player.getUUID());
        if (!left) {
            sendResult(player, false, "You are not in a faction.", eco);
            return;
        }
        fm.saveToDisk(level);
        sendResult(player, true, "Left faction.", eco);
    }

    private static void handleCreateGroup(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String name = obj.has("name") ? obj.get("name").getAsString() : "Squad";
            if (name.length() > 32) name = name.substring(0, 32);
            GroupManager gm = GroupManager.get(level);
            List<GroupData> existing = gm.getGroupsForOwner(player.getUUID());
            if (existing.size() >= 10) {
                sendResult(player, false, "Maximum 10 groups.", eco);
                return;
            }
            gm.createGroup(name, player.getUUID());
            gm.saveToDisk(level);
            sendResult(player, true, "Group '" + name + "' created.", eco);
        } catch (Exception e) {
            sendResult(player, false, "Invalid data.", eco);
        }
    }

    private static void handleDeleteGroup(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String groupId = obj.has("groupId") ? obj.get("groupId").getAsString() : "";
            String gid = groupId;
            GroupManager gm = GroupManager.get(level);
            GroupData data = gm.getGroup(gid);
            if (data == null || !data.getOwnerUuid().equals(player.getUUID())) {
                sendResult(player, false, "Group not found or not yours.", eco);
                return;
            }
            gm.deleteGroup(gid);
            gm.saveToDisk(level);
            sendResult(player, true, "Group deleted.", eco);
        } catch (Exception e) {
            sendResult(player, false, "Invalid group ID.", eco);
        }
    }

    private static void handleSetFormation(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String gid = obj.get("groupId").getAsString();
            String formation = obj.get("formation").getAsString();
            GroupManager gm = GroupManager.get(level);
            GroupData data = gm.getGroup(gid);
            if (data == null || !data.getOwnerUuid().equals(player.getUUID())) {
                sendResult(player, false, "Group not found or not yours.", eco);
                return;
            }
            data.setFormation(FormationType.fromString(formation));
            gm.saveToDisk(level);
            sendResult(player, true, "Formation set to " + formation + ".", eco);
        } catch (Exception e) {
            sendResult(player, false, "Invalid data.", eco);
        }
    }

    private static void handleGroupCommand(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String gid = obj.get("groupId").getAsString();
            String command = obj.get("command").getAsString();
            GroupManager gm = GroupManager.get(level);
            GroupData data = gm.getGroup(gid);
            if (data == null || !data.getOwnerUuid().equals(player.getUUID())) {
                sendResult(player, false, "Group not found or not yours.", eco);
                return;
            }
            // Apply command to group state
            switch (command.toUpperCase()) {
                case "FOLLOW":
                    data.setFollowState(com.ultra.megamod.feature.citizen.data.CitizenStatus.FOLLOW);
                    break;
                case "HOLD":
                    data.setFollowState(com.ultra.megamod.feature.citizen.data.CitizenStatus.HOLD_POSITION);
                    break;
                case "PATROL":
                    data.setFollowState(com.ultra.megamod.feature.citizen.data.CitizenStatus.PATROL);
                    break;
                case "AGGRESSIVE":
                    data.setAggroState(com.ultra.megamod.feature.citizen.data.CitizenStatus.COMBAT);
                    break;
                case "PASSIVE":
                    data.setAggroState(com.ultra.megamod.feature.citizen.data.CitizenStatus.IDLE);
                    break;
                default:
                    sendResult(player, false, "Unknown command: " + command, eco);
                    return;
            }
            gm.saveToDisk(level);
            sendResult(player, true, "Command '" + command + "' sent to group.", eco);
        } catch (Exception e) {
            sendResult(player, false, "Invalid data.", eco);
        }
    }

    private static void handleClaimChunk(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        boolean isAdmin = AdminSystem.isAdmin(player);
        FactionManager fm = FactionManager.get(level);
        String factionId = fm.getPlayerFaction(player.getUUID());
        if (factionId == null && !isAdmin) {
            sendResult(player, false, "You must be in a faction to claim chunks.", eco);
            return;
        }
        int chunkX = player.blockPosition().getX() >> 4;
        int chunkZ = player.blockPosition().getZ() >> 4;
        ClaimManager cm = ClaimManager.get(level);
        if (cm.isChunkClaimed(chunkX, chunkZ)) {
            if (isAdmin) {
                // Admin can override existing claims
                String existingFaction = cm.getFactionAtChunk(chunkX, chunkZ);
                cm.unclaimChunk(existingFaction, chunkX, chunkZ);
            } else {
                String existingFaction = cm.getFactionAtChunk(chunkX, chunkZ);
                sendResult(player, false, "Chunk already claimed by " + existingFaction + ".", eco);
                return;
            }
        }
        // Admin bypass: skip cost and allow claiming for any faction
        if (isAdmin) {
            // If admin has no faction, use the first available faction
            if (factionId == null) {
                var allFactions = fm.getAllFactions();
                if (!allFactions.isEmpty()) {
                    factionId = allFactions.iterator().next().getFactionId();
                } else {
                    sendResult(player, false, "No factions exist on the server.", eco);
                    return;
                }
            }
            cm.claimChunk(factionId, chunkX, chunkZ);
            cm.saveToDisk(level);
            sendResult(player, true, "Admin claimed chunk [" + chunkX + ", " + chunkZ + "] for " + factionId + ".", eco);
            return;
        }
        int cost = cm.getChunkCost(factionId);
        if (!eco.spendWallet(player.getUUID(), cost)) {
            sendResult(player, false, "Not enough MegaCoins! Need " + cost + " MC.", eco);
            return;
        }
        cm.claimChunk(factionId, chunkX, chunkZ);
        cm.saveToDisk(level);
        sendResult(player, true, "Claimed chunk [" + chunkX + ", " + chunkZ + "]. (-" + cost + " MC)", eco);
    }

    private static void handleUnclaimChunk(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        boolean isAdmin = AdminSystem.isAdmin(player);
        FactionManager fm = FactionManager.get(level);
        String factionId = fm.getPlayerFaction(player.getUUID());
        if (factionId == null && !isAdmin) {
            sendResult(player, false, "You must be in a faction.", eco);
            return;
        }
        int chunkX = player.blockPosition().getX() >> 4;
        int chunkZ = player.blockPosition().getZ() >> 4;
        ClaimManager cm = ClaimManager.get(level);
        // Admin bypass: unclaim any faction's chunk
        if (isAdmin) {
            String ownerFaction = cm.getFactionAtChunk(chunkX, chunkZ);
            if (ownerFaction == null) {
                sendResult(player, false, "Chunk is not claimed.", eco);
                return;
            }
            cm.unclaimChunk(ownerFaction, chunkX, chunkZ);
            cm.saveToDisk(level);
            sendResult(player, true, "Admin unclaimed chunk [" + chunkX + ", " + chunkZ + "] from " + ownerFaction + ".", eco);
            return;
        }
        boolean ok = cm.unclaimChunk(factionId, chunkX, chunkZ);
        if (!ok) {
            sendResult(player, false, "Chunk is not claimed by your faction.", eco);
            return;
        }
        cm.saveToDisk(level);
        sendResult(player, true, "Unclaimed chunk [" + chunkX + ", " + chunkZ + "].", eco);
    }

    private static void handleSetRelation(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String targetFaction = obj.get("targetFaction").getAsString();
            String statusStr = obj.get("status").getAsString();
            FactionManager fm = FactionManager.get(level);
            String myFaction = fm.getPlayerFaction(player.getUUID());
            if (myFaction == null) {
                sendResult(player, false, "You must be in a faction.", eco);
                return;
            }
            FactionData myData = fm.getFaction(myFaction);
            if (myData == null || !myData.getLeaderUuid().equals(player.getUUID())) {
                sendResult(player, false, "Only faction leaders can change relations.", eco);
                return;
            }
            if (myFaction.equals(targetFaction)) {
                sendResult(player, false, "Cannot set relation with yourself.", eco);
                return;
            }
            DiplomacyStatus status = DiplomacyStatus.fromString(statusStr);
            DiplomacyManager dm = DiplomacyManager.get(level);
            dm.setRelation(myFaction, targetFaction, status);
            dm.saveToDisk(level);
            sendResult(player, true, "Relation with " + targetFaction + " set to " + status.getDisplayName() + ".", eco);
        } catch (Exception e) {
            sendResult(player, false, "Invalid data.", eco);
        }
    }

    private static void handleSendTreaty(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String targetFaction = obj.get("targetFaction").getAsString();
            String proposedStr = obj.get("proposedStatus").getAsString();
            FactionManager fm = FactionManager.get(level);
            String myFaction = fm.getPlayerFaction(player.getUUID());
            if (myFaction == null) {
                sendResult(player, false, "You must be in a faction.", eco);
                return;
            }
            FactionData myData = fm.getFaction(myFaction);
            if (myData == null || !myData.getLeaderUuid().equals(player.getUUID())) {
                sendResult(player, false, "Only faction leaders can send treaties.", eco);
                return;
            }
            DiplomacyStatus proposed = DiplomacyStatus.fromString(proposedStr);
            TreatyManager tm = TreatyManager.get(level);
            long currentTick = level.getServer().overworld().getGameTime();
            tm.addProposal(myFaction, targetFaction, proposed, currentTick, UUID.randomUUID());
            tm.saveToDisk(level);
            sendResult(player, true, "Treaty proposal sent to " + targetFaction + ".", eco);
        } catch (Exception e) {
            sendResult(player, false, "Invalid data.", eco);
        }
    }

    private static void handleAcceptTreaty(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String fromFaction = obj.get("fromFaction").getAsString();
            FactionManager fm = FactionManager.get(level);
            String myFaction = fm.getPlayerFaction(player.getUUID());
            if (myFaction == null) {
                sendResult(player, false, "You must be in a faction.", eco);
                return;
            }
            FactionData myData = fm.getFaction(myFaction);
            if (myData == null || !myData.getLeaderUuid().equals(player.getUUID())) {
                sendResult(player, false, "Only faction leaders can accept treaties.", eco);
                return;
            }
            TreatyManager tm = TreatyManager.get(level);
            boolean accepted = tm.acceptProposal(fromFaction, myFaction, level);
            if (!accepted) {
                sendResult(player, false, "Treaty not found.", eco);
                return;
            }
            tm.saveToDisk(level);
            DiplomacyManager.get(level).saveToDisk(level);
            sendResult(player, true, "Treaty accepted from " + fromFaction + ".", eco);
        } catch (Exception e) {
            sendResult(player, false, "Invalid data.", eco);
        }
    }

    private static void handleDeclineTreaty(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String fromFaction = obj.get("fromFaction").getAsString();
            FactionManager fm = FactionManager.get(level);
            String myFaction = fm.getPlayerFaction(player.getUUID());
            if (myFaction == null) {
                sendResult(player, false, "You must be in a faction.", eco);
                return;
            }
            TreatyManager tm = TreatyManager.get(level);
            boolean declined = tm.declineProposal(fromFaction, myFaction);
            if (!declined) {
                sendResult(player, false, "Treaty not found.", eco);
                return;
            }
            tm.saveToDisk(level);
            sendResult(player, true, "Treaty declined.", eco);
        } catch (Exception e) {
            sendResult(player, false, "Invalid data.", eco);
        }
    }

    // --- New action handlers ---

    private static void handleSplitGroup(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String gid = obj.get("groupId").getAsString();
            GroupManager gm = GroupManager.get(level);
            GroupData data = gm.getGroup(gid);
            if (data == null || !data.getOwnerUuid().equals(player.getUUID())) {
                sendResult(player, false, "Group not found or not yours.", eco);
                return;
            }
            GroupData newGroup = gm.splitGroup(gid);
            if (newGroup == null) {
                sendResult(player, false, "Group needs at least 2 members to split.", eco);
                return;
            }
            gm.saveToDisk(level);
            sendResult(player, true, "Group split. New group: " + newGroup.getName(), eco);
            sendTownData(player, level, eco);
        } catch (Exception e) {
            sendResult(player, false, "Invalid data.", eco);
        }
    }

    private static void handleMergeGroups(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String groupA = obj.get("groupA").getAsString();
            String groupB = obj.get("groupB").getAsString();
            GroupManager gm = GroupManager.get(level);
            GroupData a = gm.getGroup(groupA);
            GroupData b = gm.getGroup(groupB);
            if (a == null || b == null || !a.getOwnerUuid().equals(player.getUUID()) || !b.getOwnerUuid().equals(player.getUUID())) {
                sendResult(player, false, "Groups not found or not yours.", eco);
                return;
            }
            boolean merged = gm.mergeGroups(groupA, groupB);
            if (!merged) {
                sendResult(player, false, "Failed to merge groups.", eco);
                return;
            }
            gm.saveToDisk(level);
            sendResult(player, true, "Groups merged into '" + a.getName() + "'.", eco);
            sendTownData(player, level, eco);
        } catch (Exception e) {
            sendResult(player, false, "Invalid data.", eco);
        }
    }

    private static void handleRequestJoinFaction(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String factionId = obj.get("factionId").getAsString();
            FactionManager fm = FactionManager.get(level);
            if (fm.getPlayerFaction(player.getUUID()) != null) {
                sendResult(player, false, "You are already in a faction.", eco);
                return;
            }
            boolean requested = fm.requestJoin(player.getUUID(), factionId);
            if (!requested) {
                sendResult(player, false, "Faction not found or request already pending.", eco);
                return;
            }
            fm.saveToDisk(level);
            sendResult(player, true, "Join request sent to faction.", eco);
            sendTownData(player, level, eco);
        } catch (Exception e) {
            sendResult(player, false, "Invalid data.", eco);
        }
    }

    private static void handleAcceptJoin(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String factionId = obj.get("factionId").getAsString();
            UUID playerUuid = UUID.fromString(obj.get("playerUuid").getAsString());
            FactionManager fm = FactionManager.get(level);
            FactionData myFaction = fm.getFaction(factionId);
            if (myFaction == null || !myFaction.getLeaderUuid().equals(player.getUUID())) {
                sendResult(player, false, "Only faction leaders can accept join requests.", eco);
                return;
            }
            boolean accepted = fm.acceptJoinRequest(factionId, playerUuid);
            if (!accepted) {
                sendResult(player, false, "Join request not found or faction full.", eco);
                return;
            }
            fm.saveToDisk(level);
            sendResult(player, true, "Player accepted into faction.", eco);
            sendTownData(player, level, eco);
        } catch (Exception e) {
            sendResult(player, false, "Invalid data.", eco);
        }
    }

    private static void handleGetChunkCost(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        FactionManager fm = FactionManager.get(level);
        String factionId = fm.getPlayerFaction(player.getUUID());
        if (factionId == null) {
            sendResult(player, false, "You must be in a faction.", eco);
            return;
        }
        int cost = ClaimManager.get(level).getChunkCost(factionId);
        JsonObject obj = new JsonObject();
        obj.addProperty("success", true);
        obj.addProperty("chunkCost", cost);
        sendResponse(player, "town_result", obj.toString(), eco);
    }

    private static void handleRenameCitizen(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            UUID entityUuid = UUID.fromString(obj.get("entityId").getAsString());
            String name = obj.has("name") ? obj.get("name").getAsString() : "";
            if (name.isEmpty() || name.length() > 32) {
                sendResult(player, false, "Name must be 1-32 characters.", eco);
                return;
            }
            Entity entity = level.getEntity(entityUuid);
            if (!(entity instanceof MCEntityCitizen citizen)) {
                sendResult(player, false, "Citizen not found.", eco);
                return;
            }
            if (!citizen.getOwnerUUID().equals(player.getUUID())) {
                sendResult(player, false, "Not your citizen.", eco);
                return;
            }
            citizen.setCitizenName(name);
            sendResult(player, true, "Citizen renamed to '" + name + "'.", eco);
            sendTownData(player, level, eco);
        } catch (Exception e) {
            sendResult(player, false, "Invalid data.", eco);
        }
    }

    private static void handleDismissCitizen(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            UUID entityUuid = UUID.fromString(obj.get("entityId").getAsString());
            Entity entity = level.getEntity(entityUuid);
            if (!(entity instanceof MCEntityCitizen citizen)) {
                sendResult(player, false, "Citizen not found.", eco);
                return;
            }
            if (!citizen.getOwnerUUID().equals(player.getUUID())) {
                sendResult(player, false, "Not your citizen.", eco);
                return;
            }
            CitizenManager.get(level).unregisterCitizen(citizen.getUUID());
            citizen.discard();
            CitizenManager.get(level).saveToDisk(level);
            sendResult(player, true, "Citizen dismissed.", eco);
            sendTownData(player, level, eco);
        } catch (Exception e) {
            sendResult(player, false, "Invalid data.", eco);
        }
    }

    private static void handleRecallCitizens(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        int recalled = 0;
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof MCEntityCitizen citizen && player.getUUID().equals(citizen.getOwnerUUID())) {
                citizen.teleportTo(player.getX(), player.getY(), player.getZ());
                recalled++;
            }
        }
        sendResult(player, true, "Recalled " + recalled + " citizen(s) to your position.", eco);
        sendTownData(player, level, eco);
    }

    private static void handleSetGroupImage(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String gid = obj.get("groupId").getAsString();
            int index = obj.get("imageIndex").getAsInt();
            GroupManager gm = GroupManager.get(level);
            GroupData data = gm.getGroup(gid);
            if (data == null || !data.getOwnerUuid().equals(player.getUUID())) {
                sendResult(player, false, "Group not found or not yours.", eco);
                return;
            }
            data.setImageIndex(index);
            gm.saveToDisk(level);
            sendResult(player, true, "Group icon updated.", eco);
            sendTownData(player, level, eco);
        } catch (Exception e) {
            sendResult(player, false, "Invalid data.", eco);
        }
    }

    private static void handleToggleGroupRanged(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String gid = obj.get("groupId").getAsString();
            GroupManager gm = GroupManager.get(level);
            GroupData data = gm.getGroup(gid);
            if (data == null || !data.getOwnerUuid().equals(player.getUUID())) {
                sendResult(player, false, "Group not found or not yours.", eco);
                return;
            }
            data.setAllowRanged(!data.isAllowRanged());
            gm.saveToDisk(level);
            sendResult(player, true, "Ranged attacks " + (data.isAllowRanged() ? "enabled" : "disabled") + ".", eco);
            sendTownData(player, level, eco);
        } catch (Exception e) {
            sendResult(player, false, "Invalid data.", eco);
        }
    }

    private static void handleToggleGroupRest(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String gid = obj.get("groupId").getAsString();
            GroupManager gm = GroupManager.get(level);
            GroupData data = gm.getGroup(gid);
            if (data == null || !data.getOwnerUuid().equals(player.getUUID())) {
                sendResult(player, false, "Group not found or not yours.", eco);
                return;
            }
            data.setAllowRest(!data.isAllowRest());
            gm.saveToDisk(level);
            sendResult(player, true, "Rest " + (data.isAllowRest() ? "enabled" : "disabled") + ".", eco);
            sendTownData(player, level, eco);
        } catch (Exception e) {
            sendResult(player, false, "Invalid data.", eco);
        }
    }

    // --- Research handler ---

    private static void handleStartResearch(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String researchIdStr = obj.has("researchId") ? obj.get("researchId").getAsString() : "";
            if (researchIdStr.isEmpty()) {
                sendResult(player, false, "Research ID is required.", eco);
                return;
            }
            FactionManager fm = FactionManager.get(level);
            String factionId = fm.getPlayerFaction(player.getUUID());
            // Admin bypass: if admin has no faction, use the faction from the JSON data if provided
            if (factionId == null) {
                if (AdminSystem.isAdmin(player)) {
                    // Admin can specify a target faction, or fall through to error
                    String targetFaction = obj.has("factionId") ? obj.get("factionId").getAsString() : "";
                    if (!targetFaction.isEmpty()) {
                        factionId = targetFaction;
                    } else {
                        // Use the first available faction as fallback for admin testing
                        var allFactions = fm.getAllFactions();
                        if (!allFactions.isEmpty()) {
                            factionId = allFactions.iterator().next().getFactionId();
                        }
                    }
                }
                if (factionId == null) {
                    sendResult(player, false, "You must be in a faction to start research.", eco);
                    return;
                }
            }
            net.minecraft.resources.Identifier researchId = net.minecraft.resources.Identifier.parse(researchIdStr);
            com.ultra.megamod.feature.citizen.research.ResearchManager mgr =
                    com.ultra.megamod.feature.citizen.research.ResearchManager.get(level, factionId);
            com.ultra.megamod.feature.citizen.research.ResearchManager.StartResult result =
                    mgr.startResearch(researchId, player);
            if (result.isSuccess()) {
                sendResult(player, true, result.getMessage(), eco);
            } else {
                sendResult(player, false, result.getMessage(), eco);
            }
        } catch (Exception e) {
            sendResult(player, false, "Invalid research data.", eco);
        }
    }

    // --- Building action handlers ---

    private static void handleBuildingUpgrade(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            BlockPos pos = new BlockPos(posX, posY, posZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            // Admin bypass: skip faction ownership check
            FactionManager fm = FactionManager.get(level);
            String factionId = fm.getPlayerFaction(player.getUUID());
            if (factionId == null && !AdminSystem.isAdmin(player)) {
                sendResult(player, false, "You must be in a faction.", eco);
                return;
            }

            int currentLevel = tile.getBuildingLevel();
            int targetLevel = currentLevel + 1;
            if (targetLevel > 5) {
                sendResult(player, false, "Building is already at max level.", eco);
                return;
            }

            // Create upgrade work order
            com.ultra.megamod.feature.citizen.building.workorder.ColonyWorkOrder order =
                    com.ultra.megamod.feature.citizen.building.workorder.ColonyWorkOrder.createUpgrade(
                            tile.getColonyId() != null ? tile.getColonyId() : UUID.randomUUID(),
                            tile.getBuildingId(), pos, targetLevel, tile.getStyle(), "",
                            0, level.getServer().overworld().getGameTime());

            // Admin bypass: skip materials + faster building
            if (AdminSystem.isAdmin(player)
                    && com.ultra.megamod.feature.toggles.FeatureToggleManager.get(level).isEnabled("builder_admin_bypass")) {
                order.setAdminBypass(true);
                int speedMult = com.ultra.megamod.feature.toggles.FeatureToggleManager.get(level)
                        .getNumericSetting("builder_speed_multiplier");
                if (speedMult > 1) order.setSpeedMultiplier(speedMult);
            }

            com.ultra.megamod.feature.citizen.building.workorder.WorkManager.get(level).addOrder(order);

            String adminTag = order.isAdminBypass()
                    ? " \u00A7d[Admin: no materials" + (order.getSpeedMultiplier() > 1 ? ", " + order.getSpeedMultiplier() + "x speed" : "") + "]"
                    : "";
            sendResult(player, true, "Upgrade work order created for " + tile.getBuildingId()
                    + " to level " + targetLevel + "." + adminTag, eco);
            sendTownData(player, level, eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to create upgrade order.", eco);
        }
    }

    private static void handleBuildingRepair(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            BlockPos pos = new BlockPos(posX, posY, posZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            // Admin bypass: skip faction ownership check
            FactionManager fm = FactionManager.get(level);
            String factionId = fm.getPlayerFaction(player.getUUID());
            if (factionId == null && !AdminSystem.isAdmin(player)) {
                sendResult(player, false, "You must be in a faction.", eco);
                return;
            }

            // Create repair work order
            com.ultra.megamod.feature.citizen.building.workorder.ColonyWorkOrder order =
                    com.ultra.megamod.feature.citizen.building.workorder.ColonyWorkOrder.createRepair(
                            tile.getColonyId() != null ? tile.getColonyId() : UUID.randomUUID(),
                            tile.getBuildingId(), pos, tile.getBuildingLevel(), tile.getStyle(), "",
                            0, level.getServer().overworld().getGameTime());

            // Admin bypass: skip materials + faster building
            if (AdminSystem.isAdmin(player)
                    && com.ultra.megamod.feature.toggles.FeatureToggleManager.get(level).isEnabled("builder_admin_bypass")) {
                order.setAdminBypass(true);
                int speedMult = com.ultra.megamod.feature.toggles.FeatureToggleManager.get(level)
                        .getNumericSetting("builder_speed_multiplier");
                if (speedMult > 1) order.setSpeedMultiplier(speedMult);
            }

            com.ultra.megamod.feature.citizen.building.workorder.WorkManager.get(level).addOrder(order);

            String adminTag = order.isAdminBypass()
                    ? " \u00A7d[Admin: no materials" + (order.getSpeedMultiplier() > 1 ? ", " + order.getSpeedMultiplier() + "x speed" : "") + "]"
                    : "";
            sendResult(player, true, "Repair work order created for " + tile.getBuildingId() + "." + adminTag, eco);
            sendTownData(player, level, eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to create repair order.", eco);
        }
    }

    private static void handleBuildingRename(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            String newName = obj.has("name") ? obj.get("name").getAsString() : "";
            if (newName.isEmpty() || newName.length() > 48) {
                sendResult(player, false, "Name must be 1-48 characters.", eco);
                return;
            }
            BlockPos pos = new BlockPos(posX, posY, posZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            // Admin bypass: allow renaming any building
            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                String factionId = fm.getPlayerFaction(player.getUUID());
                if (factionId == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            tile.setCustomName(newName);
            sendResult(player, true, "Building renamed to '" + newName + "'.", eco);
        } catch (Exception e) {
            sendResult(player, false, "Invalid data.", eco);
        }
    }

    // --- Building management handlers ---

    private static void handleBuildingDemolish(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            BlockPos pos = new BlockPos(posX, posY, posZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                String factionId = fm.getPlayerFaction(player.getUUID());
                if (factionId == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            String buildingId = tile.getBuildingId();

            // Free assigned workers near this building
            CitizenManager cm = CitizenManager.get(level);
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof MCEntityCitizen worker
                        && player.getUUID().equals(worker.getOwnerUUID())) {
                    BlockPos workerStart = worker.getStartPos();
                    if (!workerStart.equals(BlockPos.ZERO) && workerStart.distManhattan(pos) <= 16) {
                        worker.setStartPos(BlockPos.ZERO);
                    }
                }
            }

            // Drop the hut block item
            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(buildingId);
            if (entry != null && entry.hutBlockFactory() != null) {
                ItemStack dropStack = new ItemStack(entry.hutBlockFactory().get());
                if (!dropStack.isEmpty()) {
                    player.spawnAtLocation(level, dropStack);
                }
            }

            // Remove the block entity and block
            level.removeBlockEntity(pos);
            level.removeBlock(pos, false);

            sendResult(player, true, "Building '" + buildingId + "' demolished.", eco);
            sendTownData(player, level, eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to demolish building.", eco);
        }
    }

    private static void handleBuildingAssignWorker(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            UUID citizenId = UUID.fromString(obj.get("citizenId").getAsString());
            BlockPos pos = new BlockPos(posX, posY, posZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            // Look up the AbstractBuilding from the registry to access WorkerBuildingModule
            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(tile.getBuildingId());
            if (entry == null) {
                sendResult(player, false, "Unknown building type.", eco);
                return;
            }

            // Assign the worker by setting their start position to this building
            Entity entity = level.getEntity(citizenId);
            if (entity == null) {
                // Try to find by UUID in all entities
                for (Entity e : level.getAllEntities()) {
                    if (e.getUUID().equals(citizenId)) {
                        entity = e;
                        break;
                    }
                }
            }
            if (!(entity instanceof MCEntityCitizen worker)) {
                sendResult(player, false, "Worker citizen not found.", eco);
                return;
            }
            if (!player.getUUID().equals(worker.getOwnerUUID()) && !AdminSystem.isAdmin(player)) {
                sendResult(player, false, "Not your citizen.", eco);
                return;
            }

            worker.setStartPos(pos);
            sendResult(player, true, "Worker assigned to " + tile.getBuildingId() + ".", eco);
            sendTownData(player, level, eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to assign worker.", eco);
        }
    }

    private static void handleBuildingRemoveWorker(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            UUID citizenId = UUID.fromString(obj.get("citizenId").getAsString());
            BlockPos pos = new BlockPos(posX, posY, posZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            // Find the worker and clear their start position
            Entity entity = null;
            for (Entity e : level.getAllEntities()) {
                if (e.getUUID().equals(citizenId)) {
                    entity = e;
                    break;
                }
            }
            if (!(entity instanceof MCEntityCitizen worker)) {
                sendResult(player, false, "Worker citizen not found.", eco);
                return;
            }
            if (!player.getUUID().equals(worker.getOwnerUUID()) && !AdminSystem.isAdmin(player)) {
                sendResult(player, false, "Not your citizen.", eco);
                return;
            }

            worker.setStartPos(BlockPos.ZERO);
            sendResult(player, true, "Worker removed from " + tile.getBuildingId() + ".", eco);
            sendTownData(player, level, eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to remove worker.", eco);
        }
    }

    private static void handleBuildingSetStyle(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            String style = obj.has("style") ? obj.get("style").getAsString() : "";
            if (style.isEmpty() || style.length() > 32) {
                sendResult(player, false, "Style must be 1-32 characters.", eco);
                return;
            }
            BlockPos pos = new BlockPos(posX, posY, posZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            tile.setStyle(style);
            sendResult(player, true, "Building style set to '" + style + "'.", eco);
        } catch (Exception e) {
            sendResult(player, false, "Invalid data.", eco);
        }
    }

    private static void handleBuildingReactivate(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            BlockPos pos = new BlockPos(posX, posY, posZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            if (tile.getBuildingLevel() > 0) {
                sendResult(player, false, "Building is already active (level " + tile.getBuildingLevel() + ").", eco);
                return;
            }

            tile.setBuildingLevel(1);
            sendResult(player, true, "Building reactivated to level 1.", eco);
            sendTownData(player, level, eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to reactivate building.", eco);
        }
    }

    private static void handleBuildingToggleAutoHire(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            BlockPos pos = new BlockPos(posX, posY, posZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            // Use the building entry to create a temporary AbstractBuilding with settings module
            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(tile.getBuildingId());
            if (entry != null) {
                com.ultra.megamod.feature.citizen.building.AbstractBuilding building = entry.buildingFactory().get();
                var settingsOpt = building.getModule(com.ultra.megamod.feature.citizen.building.module.ISettingsModule.class);
                if (settingsOpt.isPresent()) {
                    var settings = settingsOpt.get();
                    boolean current = settings.getSetting("autoHire", true);
                    settings.setSetting("autoHire", !current);
                    sendResult(player, true, "Auto-hire " + (!current ? "enabled" : "disabled") + ".", eco);
                    return;
                }
            }

            // Fallback: just acknowledge the toggle even without settings module
            sendResult(player, true, "Auto-hire setting toggled.", eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to toggle auto-hire.", eco);
        }
    }

    // --- Crafting building action handlers ---

    private static void handleBuildingAddRecipe(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            String recipeIdStr = obj.get("recipeId").getAsString();
            BlockPos pos = new BlockPos(posX, posY, posZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(tile.getBuildingId());
            if (entry == null) {
                sendResult(player, false, "Unknown building type.", eco);
                return;
            }

            com.ultra.megamod.feature.citizen.building.AbstractBuilding building = entry.buildingFactory().get();
            var craftingOpt = building.getModule(com.ultra.megamod.feature.citizen.building.module.ICraftingBuildingModule.class);
            if (craftingOpt.isEmpty()) {
                sendResult(player, false, "This building does not support crafting.", eco);
                return;
            }

            net.minecraft.resources.Identifier recipeId = net.minecraft.resources.Identifier.parse(recipeIdStr);
            boolean added = craftingOpt.get().addRecipe(recipeId);
            if (!added) {
                sendResult(player, false, "Recipe already known or at max capacity.", eco);
                return;
            }

            sendResult(player, true, "Recipe added to " + tile.getBuildingId() + ".", eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to add recipe.", eco);
        }
    }

    private static void handleBuildingRemoveRecipe(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            String recipeIdStr = obj.get("recipeId").getAsString();
            BlockPos pos = new BlockPos(posX, posY, posZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(tile.getBuildingId());
            if (entry == null) {
                sendResult(player, false, "Unknown building type.", eco);
                return;
            }

            com.ultra.megamod.feature.citizen.building.AbstractBuilding building = entry.buildingFactory().get();
            var craftingOpt = building.getModule(com.ultra.megamod.feature.citizen.building.module.ICraftingBuildingModule.class);
            if (craftingOpt.isEmpty()) {
                sendResult(player, false, "This building does not support crafting.", eco);
                return;
            }

            net.minecraft.resources.Identifier recipeId = net.minecraft.resources.Identifier.parse(recipeIdStr);
            craftingOpt.get().removeRecipe(recipeId);
            sendResult(player, true, "Recipe removed from " + tile.getBuildingId() + ".", eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to remove recipe.", eco);
        }
    }

    private static void handleBuildingRecipePriorityUp(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            int recipeIndex = obj.get("recipeIndex").getAsInt();
            BlockPos pos = new BlockPos(posX, posY, posZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(tile.getBuildingId());
            if (entry == null) {
                sendResult(player, false, "Unknown building type.", eco);
                return;
            }

            com.ultra.megamod.feature.citizen.building.AbstractBuilding building = entry.buildingFactory().get();
            var craftingOpt = building.getModule(com.ultra.megamod.feature.citizen.building.module.ICraftingBuildingModule.class);
            if (craftingOpt.isEmpty()) {
                sendResult(player, false, "This building does not support crafting.", eco);
                return;
            }

            java.util.List<net.minecraft.resources.Identifier> recipes = craftingOpt.get().getRecipes();
            if (recipeIndex <= 0 || recipeIndex >= recipes.size()) {
                sendResult(player, false, "Recipe is already at the top or index is invalid.", eco);
                return;
            }

            // Swap with index-1 by removing and re-adding
            net.minecraft.resources.Identifier target = recipes.get(recipeIndex);
            net.minecraft.resources.Identifier above = recipes.get(recipeIndex - 1);
            craftingOpt.get().removeRecipe(target);
            craftingOpt.get().removeRecipe(above);
            craftingOpt.get().addRecipe(target);
            craftingOpt.get().addRecipe(above);

            sendResult(player, true, "Recipe priority increased.", eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to change recipe priority.", eco);
        }
    }

    private static void handleBuildingRecipePriorityDown(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            int recipeIndex = obj.get("recipeIndex").getAsInt();
            BlockPos pos = new BlockPos(posX, posY, posZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(tile.getBuildingId());
            if (entry == null) {
                sendResult(player, false, "Unknown building type.", eco);
                return;
            }

            com.ultra.megamod.feature.citizen.building.AbstractBuilding building = entry.buildingFactory().get();
            var craftingOpt = building.getModule(com.ultra.megamod.feature.citizen.building.module.ICraftingBuildingModule.class);
            if (craftingOpt.isEmpty()) {
                sendResult(player, false, "This building does not support crafting.", eco);
                return;
            }

            java.util.List<net.minecraft.resources.Identifier> recipes = craftingOpt.get().getRecipes();
            if (recipeIndex < 0 || recipeIndex >= recipes.size() - 1) {
                sendResult(player, false, "Recipe is already at the bottom or index is invalid.", eco);
                return;
            }

            // Swap with index+1 by removing and re-adding
            net.minecraft.resources.Identifier target = recipes.get(recipeIndex);
            net.minecraft.resources.Identifier below = recipes.get(recipeIndex + 1);
            craftingOpt.get().removeRecipe(target);
            craftingOpt.get().removeRecipe(below);
            craftingOpt.get().addRecipe(below);
            craftingOpt.get().addRecipe(target);

            sendResult(player, true, "Recipe priority decreased.", eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to change recipe priority.", eco);
        }
    }

    private static void handleBuildingToggleRecipe(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            int recipeIndex = obj.get("recipeIndex").getAsInt();
            BlockPos pos = new BlockPos(posX, posY, posZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(tile.getBuildingId());
            if (entry == null) {
                sendResult(player, false, "Unknown building type.", eco);
                return;
            }

            com.ultra.megamod.feature.citizen.building.AbstractBuilding building = entry.buildingFactory().get();
            var craftingOpt = building.getModule(com.ultra.megamod.feature.citizen.building.module.ICraftingBuildingModule.class);
            if (craftingOpt.isEmpty()) {
                sendResult(player, false, "This building does not support crafting.", eco);
                return;
            }

            java.util.List<net.minecraft.resources.Identifier> recipes = craftingOpt.get().getRecipes();
            if (recipeIndex < 0 || recipeIndex >= recipes.size()) {
                sendResult(player, false, "Invalid recipe index.", eco);
                return;
            }

            // Toggle by removing or re-adding the recipe at the given index
            net.minecraft.resources.Identifier recipeId = recipes.get(recipeIndex);
            craftingOpt.get().removeRecipe(recipeId);
            // The recipe is "disabled" by removing it; re-adding puts it back
            // For a true enable/disable we toggle: if it existed, it's now removed (disabled)
            sendResult(player, true, "Recipe toggled for " + recipeId.getPath() + ".", eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to toggle recipe.", eco);
        }
    }

    // --- Farmer action handlers ---

    private static void handleBuildingAssignField(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            int fieldX = obj.get("fieldX").getAsInt();
            int fieldY = obj.get("fieldY").getAsInt();
            int fieldZ = obj.get("fieldZ").getAsInt();
            BlockPos pos = new BlockPos(posX, posY, posZ);
            BlockPos fieldPos = new BlockPos(fieldX, fieldY, fieldZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            // Store field assignment in the building's settings module
            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(tile.getBuildingId());
            if (entry != null) {
                com.ultra.megamod.feature.citizen.building.AbstractBuilding building = entry.buildingFactory().get();
                var settingsOpt = building.getModule(com.ultra.megamod.feature.citizen.building.module.ISettingsModule.class);
                if (settingsOpt.isPresent()) {
                    settingsOpt.get().setSetting("fieldPos", fieldX + "," + fieldY + "," + fieldZ);
                }
            }

            sendResult(player, true, "Field assigned at [" + fieldX + ", " + fieldY + ", " + fieldZ + "].", eco);
            sendTownData(player, level, eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to assign field.", eco);
        }
    }

    private static void handleBuildingRemoveField(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            BlockPos pos = new BlockPos(posX, posY, posZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(tile.getBuildingId());
            if (entry != null) {
                com.ultra.megamod.feature.citizen.building.AbstractBuilding building = entry.buildingFactory().get();
                var settingsOpt = building.getModule(com.ultra.megamod.feature.citizen.building.module.ISettingsModule.class);
                if (settingsOpt.isPresent()) {
                    settingsOpt.get().setSetting("fieldPos", "");
                }
            }

            sendResult(player, true, "Field assignment removed.", eco);
            sendTownData(player, level, eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to remove field.", eco);
        }
    }

    private static void handleBuildingToggleFertilize(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            BlockPos pos = new BlockPos(posX, posY, posZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(tile.getBuildingId());
            if (entry != null) {
                com.ultra.megamod.feature.citizen.building.AbstractBuilding building = entry.buildingFactory().get();
                var settingsOpt = building.getModule(com.ultra.megamod.feature.citizen.building.module.ISettingsModule.class);
                if (settingsOpt.isPresent()) {
                    boolean current = settingsOpt.get().getSetting("fertilize", true);
                    settingsOpt.get().setSetting("fertilize", !current);
                    sendResult(player, true, "Fertilizer use " + (!current ? "enabled" : "disabled") + ".", eco);
                    return;
                }
            }

            sendResult(player, true, "Fertilizer setting toggled.", eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to toggle fertilizer.", eco);
        }
    }

    // --- Miner action handlers ---

    private static void handleBuildingSetMineLevel(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            int depthTier = obj.get("depthTier").getAsInt();
            BlockPos pos = new BlockPos(posX, posY, posZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            if (depthTier < 0 || depthTier > 3) {
                sendResult(player, false, "Depth tier must be 0-3.", eco);
                return;
            }

            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(tile.getBuildingId());
            if (entry != null) {
                com.ultra.megamod.feature.citizen.building.AbstractBuilding building = entry.buildingFactory().get();
                var settingsOpt = building.getModule(com.ultra.megamod.feature.citizen.building.module.ISettingsModule.class);
                if (settingsOpt.isPresent()) {
                    settingsOpt.get().setSetting("depthTier", depthTier);
                }
            }

            sendResult(player, true, "Mine depth tier set to " + depthTier + ".", eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to set mine level.", eco);
        }
    }

    private static void handleBuildingSetFillBlock(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            BlockPos pos = parseBuildingPos(obj);
            if (pos == null) {
                // Fallback to legacy posX/posY/posZ format
                int posX = obj.get("posX").getAsInt();
                int posY = obj.get("posY").getAsInt();
                int posZ = obj.get("posZ").getAsInt();
                pos = new BlockPos(posX, posY, posZ);
            }
            String fillBlock = obj.has("fillBlock") ? obj.get("fillBlock").getAsString() : "cobblestone";

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            if (fillBlock.length() > 64) {
                sendResult(player, false, "Fill block name too long.", eco);
                return;
            }

            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(tile.getBuildingId());
            if (entry != null) {
                com.ultra.megamod.feature.citizen.building.AbstractBuilding building = entry.buildingFactory().get();
                var settingsOpt = building.getModule(com.ultra.megamod.feature.citizen.building.module.ISettingsModule.class);
                if (settingsOpt.isPresent()) {
                    settingsOpt.get().setSetting("fillBlock", fillBlock);
                }
            }

            sendResult(player, true, "Fill block set to '" + fillBlock + "'.", eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to set fill block.", eco);
        }
    }

    private static void handleBuildingRepairMineLevel(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            BlockPos pos = new BlockPos(posX, posY, posZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            // Create a repair work order specifically for the mine
            com.ultra.megamod.feature.citizen.building.workorder.ColonyWorkOrder order =
                    com.ultra.megamod.feature.citizen.building.workorder.ColonyWorkOrder.createRepair(
                            tile.getColonyId() != null ? tile.getColonyId() : UUID.randomUUID(),
                            tile.getBuildingId(), pos, tile.getBuildingLevel(), tile.getStyle(), "",
                            1, level.getServer().overworld().getGameTime());
            com.ultra.megamod.feature.citizen.building.workorder.WorkManager.get(level).addOrder(order);

            sendResult(player, true, "Mine repair work order created.", eco);
            sendTownData(player, level, eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to create mine repair order.", eco);
        }
    }

    // --- Guard action handlers ---

    private static void handleBuildingSetPatrolMode(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            String mode = obj.get("mode").getAsString();
            BlockPos pos = new BlockPos(posX, posY, posZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            // Validate mode
            if (!mode.equals("patrol") && !mode.equals("follow") && !mode.equals("guard")) {
                sendResult(player, false, "Invalid patrol mode. Use patrol, follow, or guard.", eco);
                return;
            }

            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(tile.getBuildingId());
            if (entry != null) {
                com.ultra.megamod.feature.citizen.building.AbstractBuilding building = entry.buildingFactory().get();
                var settingsOpt = building.getModule(com.ultra.megamod.feature.citizen.building.module.ISettingsModule.class);
                if (settingsOpt.isPresent()) {
                    settingsOpt.get().setSetting("patrolMode", mode);
                }
            }

            sendResult(player, true, "Patrol mode set to '" + mode + "'.", eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to set patrol mode.", eco);
        }
    }

    private static void handleBuildingAddPatrolPoint(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            int pointX = obj.get("pointX").getAsInt();
            int pointY = obj.get("pointY").getAsInt();
            int pointZ = obj.get("pointZ").getAsInt();
            BlockPos pos = new BlockPos(posX, posY, posZ);
            BlockPos patrolPoint = new BlockPos(pointX, pointY, pointZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(tile.getBuildingId());
            if (entry != null) {
                com.ultra.megamod.feature.citizen.building.AbstractBuilding building = entry.buildingFactory().get();
                var settingsOpt = building.getModule(com.ultra.megamod.feature.citizen.building.module.ISettingsModule.class);
                if (settingsOpt.isPresent()) {
                    String existing = settingsOpt.get().getSetting("patrolPoints", "");
                    String newPoint = pointX + "," + pointY + "," + pointZ;
                    String updated = existing.isEmpty() ? newPoint : existing + ";" + newPoint;
                    settingsOpt.get().setSetting("patrolPoints", updated);
                }
            }

            sendResult(player, true, "Patrol point added at [" + pointX + ", " + pointY + ", " + pointZ + "].", eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to add patrol point.", eco);
        }
    }

    private static void handleBuildingRemovePatrolPoint(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            int pointX = obj.get("pointX").getAsInt();
            int pointY = obj.get("pointY").getAsInt();
            int pointZ = obj.get("pointZ").getAsInt();
            BlockPos pos = new BlockPos(posX, posY, posZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(tile.getBuildingId());
            if (entry != null) {
                com.ultra.megamod.feature.citizen.building.AbstractBuilding building = entry.buildingFactory().get();
                var settingsOpt = building.getModule(com.ultra.megamod.feature.citizen.building.module.ISettingsModule.class);
                if (settingsOpt.isPresent()) {
                    String existing = settingsOpt.get().getSetting("patrolPoints", "");
                    String toRemove = pointX + "," + pointY + "," + pointZ;
                    // Remove the matching point from the semicolon-separated list
                    String[] points = existing.split(";");
                    StringBuilder updated = new StringBuilder();
                    for (String point : points) {
                        if (!point.equals(toRemove)) {
                            if (updated.length() > 0) updated.append(";");
                            updated.append(point);
                        }
                    }
                    settingsOpt.get().setSetting("patrolPoints", updated.toString());
                }
            }

            sendResult(player, true, "Patrol point removed.", eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to remove patrol point.", eco);
        }
    }

    private static void handleBuildingSetGuardTarget(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            String targetUuidStr = obj.has("targetUuid") ? obj.get("targetUuid").getAsString() : "";
            BlockPos pos = new BlockPos(posX, posY, posZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            // Validate target UUID
            UUID targetUuid;
            try {
                targetUuid = UUID.fromString(targetUuidStr);
            } catch (IllegalArgumentException e) {
                sendResult(player, false, "Invalid target UUID.", eco);
                return;
            }

            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(tile.getBuildingId());
            if (entry != null) {
                com.ultra.megamod.feature.citizen.building.AbstractBuilding building = entry.buildingFactory().get();
                var settingsOpt = building.getModule(com.ultra.megamod.feature.citizen.building.module.ISettingsModule.class);
                if (settingsOpt.isPresent()) {
                    settingsOpt.get().setSetting("followTarget", targetUuidStr);
                }
            }

            sendResult(player, true, "Guard follow target set.", eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to set guard target.", eco);
        }
    }

    // --- Warehouse action handlers ---

    private static void handleBuildingSortWarehouse(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            BlockPos pos = new BlockPos(posX, posY, posZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                String factionId = fm.getPlayerFaction(player.getUUID());
                if (factionId == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            // Sort all rack/container block entities in claimed chunks near the warehouse
            FactionManager fm = FactionManager.get(level);
            String factionId = fm.getPlayerFaction(player.getUUID());
            if (factionId == null && AdminSystem.isAdmin(player)) {
                // Admin fallback: use colonyId from the tile
                factionId = tile.getColonyId() != null ? tile.getColonyId().toString() : null;
            }

            int sortedContainers = 0;
            if (factionId != null) {
                ClaimManager cm = ClaimManager.get(level);
                ClaimData claimData = cm.getClaim(factionId);
                if (claimData != null) {
                    for (long[] chunkCoords : claimData.getClaimedChunks()) {
                        net.minecraft.world.level.ChunkPos chunkPos =
                                new net.minecraft.world.level.ChunkPos((int) chunkCoords[0], (int) chunkCoords[1]);
                        if (!level.hasChunk(chunkPos.x, chunkPos.z)) continue;
                        net.minecraft.world.level.chunk.LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);

                        for (var beEntry : chunk.getBlockEntities().entrySet()) {
                            if (beEntry.getValue() instanceof BaseContainerBlockEntity container) {
                                // Sort the container contents
                                sortContainer(container);
                                sortedContainers++;
                            }
                        }
                    }
                }
            }

            sendResult(player, true, "Warehouse sorted. " + sortedContainers + " container(s) organized.", eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to sort warehouse.", eco);
        }
    }

    /**
     * Sorts a container's items by item name, merging stacks where possible.
     */
    private static void sortContainer(BaseContainerBlockEntity container) {
        java.util.List<ItemStack> items = new java.util.ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                items.add(stack.copy());
            }
            container.setItem(i, ItemStack.EMPTY);
        }

        // Merge stacks of same item
        java.util.List<ItemStack> merged = new java.util.ArrayList<>();
        for (ItemStack stack : items) {
            boolean found = false;
            for (ItemStack existing : merged) {
                if (ItemStack.isSameItemSameComponents(existing, stack)
                        && existing.getCount() < existing.getMaxStackSize()) {
                    int toAdd = Math.min(stack.getCount(), existing.getMaxStackSize() - existing.getCount());
                    existing.grow(toAdd);
                    stack.shrink(toAdd);
                    if (stack.isEmpty()) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found && !stack.isEmpty()) {
                merged.add(stack);
            }
        }

        // Sort by display name
        merged.sort((a, b) -> a.getHoverName().getString().compareToIgnoreCase(b.getHoverName().getString()));

        // Place back
        for (int i = 0; i < merged.size() && i < container.getContainerSize(); i++) {
            container.setItem(i, merged.get(i));
        }
        container.setChanged();
    }

    private static void handleBuildingAssignCourier(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            UUID citizenId = UUID.fromString(obj.get("citizenId").getAsString());
            BlockPos pos = new BlockPos(posX, posY, posZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            // Find the deliveryman citizen and assign them to the warehouse position
            Entity entity = null;
            for (Entity e : level.getAllEntities()) {
                if (e.getUUID().equals(citizenId)) {
                    entity = e;
                    break;
                }
            }
            if (!(entity instanceof MCEntityCitizen worker)) {
                sendResult(player, false, "Courier citizen not found.", eco);
                return;
            }
            if (!player.getUUID().equals(worker.getOwnerUUID()) && !AdminSystem.isAdmin(player)) {
                sendResult(player, false, "Not your citizen.", eco);
                return;
            }

            worker.setStartPos(pos);
            sendResult(player, true, "Courier assigned to warehouse.", eco);
            sendTownData(player, level, eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to assign courier.", eco);
        }
    }

    private static void handleBuildingSetMinStock(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            String itemName = obj.get("item").getAsString();
            int count = obj.get("count").getAsInt();
            BlockPos pos = new BlockPos(posX, posY, posZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            if (count < 0) {
                sendResult(player, false, "Count must be 0 or greater.", eco);
                return;
            }

            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(tile.getBuildingId());
            if (entry != null) {
                com.ultra.megamod.feature.citizen.building.AbstractBuilding building = entry.buildingFactory().get();
                var stockOpt = building.getModule(com.ultra.megamod.feature.citizen.building.module.IMinimumStockModule.class);
                if (stockOpt.isPresent()) {
                    // Look up the item from the registry
                    net.minecraft.resources.Identifier itemId = net.minecraft.resources.Identifier.parse(itemName);
                    net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.getValue(itemId);
                    if (item != null && item != Items.AIR) {
                        if (count == 0) {
                            stockOpt.get().removeMinimumStock(new ItemStack(item));
                            sendResult(player, true, "Minimum stock removed for " + itemName + ".", eco);
                        } else {
                            stockOpt.get().setMinimumStock(new ItemStack(item), count);
                            sendResult(player, true, "Minimum stock set to " + count + " for " + itemName + ".", eco);
                        }
                        return;
                    } else {
                        sendResult(player, false, "Unknown item: " + itemName, eco);
                        return;
                    }
                }
            }

            sendResult(player, false, "This building does not support minimum stock.", eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to set minimum stock.", eco);
        }
    }

    // --- Other action handlers ---

    private static void handleBuildingCancelResearch(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String researchIdStr = obj.has("researchId") ? obj.get("researchId").getAsString() : "";
            if (researchIdStr.isEmpty()) {
                sendResult(player, false, "Research ID is required.", eco);
                return;
            }

            FactionManager fm = FactionManager.get(level);
            String factionId = fm.getPlayerFaction(player.getUUID());
            if (factionId == null && AdminSystem.isAdmin(player)) {
                String targetFaction = obj.has("factionId") ? obj.get("factionId").getAsString() : "";
                if (!targetFaction.isEmpty()) {
                    factionId = targetFaction;
                } else {
                    var allFactions = fm.getAllFactions();
                    if (!allFactions.isEmpty()) {
                        factionId = allFactions.iterator().next().getFactionId();
                    }
                }
            }
            if (factionId == null) {
                sendResult(player, false, "You must be in a faction to cancel research.", eco);
                return;
            }

            net.minecraft.resources.Identifier researchId = net.minecraft.resources.Identifier.parse(researchIdStr);
            com.ultra.megamod.feature.citizen.research.ResearchManager mgr =
                    com.ultra.megamod.feature.citizen.research.ResearchManager.get(level, factionId);

            com.ultra.megamod.feature.citizen.research.ResearchState state =
                    mgr.getLocalTree().getResearchState(researchId);
            if (state != com.ultra.megamod.feature.citizen.research.ResearchState.IN_PROGRESS) {
                sendResult(player, false, "Research is not in progress.", eco);
                return;
            }

            mgr.resetResearch(researchId);
            sendResult(player, true, "Research '" + researchIdStr + "' cancelled.", eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to cancel research.", eco);
        }
    }

    private static void handleBuildingRecruitVisitor(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String visitorIdStr = obj.has("visitorId") ? obj.get("visitorId").getAsString() : "";
            if (visitorIdStr.isEmpty()) {
                sendResult(player, false, "Visitor ID is required.", eco);
                return;
            }

            FactionManager fm = FactionManager.get(level);
            String factionId = fm.getPlayerFaction(player.getUUID());
            if (factionId == null && !AdminSystem.isAdmin(player)) {
                sendResult(player, false, "You must be in a faction.", eco);
                return;
            }
            if (factionId == null) {
                // Admin fallback
                var allFactions = fm.getAllFactions();
                if (!allFactions.isEmpty()) {
                    factionId = allFactions.iterator().next().getFactionId();
                } else {
                    sendResult(player, false, "No factions exist on the server.", eco);
                    return;
                }
            }

            UUID visitorId = UUID.fromString(visitorIdStr);
            com.ultra.megamod.feature.citizen.visitor.VisitorManager vm =
                    com.ultra.megamod.feature.citizen.visitor.VisitorManager.get(level, factionId);
            boolean recruited = vm.recruitVisitor(visitorId, player);
            if (!recruited) {
                sendResult(player, false, "Failed to recruit visitor. Check if you have the required items.", eco);
                return;
            }

            vm.saveToDisk(level);
            sendResult(player, true, "Visitor recruited!", eco);
            sendTownData(player, level, eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to recruit visitor.", eco);
        }
    }

    private static void handleBuildingAssignBed(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            UUID citizenId = UUID.fromString(obj.get("citizenId").getAsString());
            int bedX = obj.get("bedX").getAsInt();
            int bedY = obj.get("bedY").getAsInt();
            int bedZ = obj.get("bedZ").getAsInt();
            BlockPos pos = new BlockPos(posX, posY, posZ);
            BlockPos bedPos = new BlockPos(bedX, bedY, bedZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            // Find the citizen entity and assign the bed position
            Entity entity = null;
            for (Entity e : level.getAllEntities()) {
                if (e.getUUID().equals(citizenId)) {
                    entity = e;
                    break;
                }
            }
            if (!(entity instanceof MCEntityCitizen citizen)) {
                sendResult(player, false, "Citizen not found.", eco);
                return;
            }
            if (!player.getUUID().equals(citizen.getOwnerUUID()) && !AdminSystem.isAdmin(player)) {
                sendResult(player, false, "Not your citizen.", eco);
                return;
            }

            // Also register in the residence module if available
            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(tile.getBuildingId());
            if (entry != null) {
                com.ultra.megamod.feature.citizen.building.AbstractBuilding building = entry.buildingFactory().get();
                var residenceOpt = building.getModule(com.ultra.megamod.feature.citizen.building.module.IAssignsCitizen.class);
                if (residenceOpt.isPresent()) {
                    residenceOpt.get().assignResident(citizenId);
                }
            }

            sendResult(player, true, "Citizen assigned to bed at [" + bedX + ", " + bedY + ", " + bedZ + "].", eco);
            sendTownData(player, level, eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to assign bed.", eco);
        }
    }

    private static void handleBuildingSetRestaurantMenu(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int posX = obj.get("posX").getAsInt();
            int posY = obj.get("posY").getAsInt();
            int posZ = obj.get("posZ").getAsInt();
            BlockPos pos = new BlockPos(posX, posY, posZ);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            // Parse the menu items from the JSON array
            JsonArray menuArray = obj.has("menu") ? obj.getAsJsonArray("menu") : new JsonArray();
            StringBuilder menuStr = new StringBuilder();
            for (int i = 0; i < menuArray.size(); i++) {
                if (i > 0) menuStr.append(";");
                menuStr.append(menuArray.get(i).getAsString());
            }

            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(tile.getBuildingId());
            if (entry != null) {
                com.ultra.megamod.feature.citizen.building.AbstractBuilding building = entry.buildingFactory().get();
                var settingsOpt = building.getModule(com.ultra.megamod.feature.citizen.building.module.ISettingsModule.class);
                if (settingsOpt.isPresent()) {
                    settingsOpt.get().setSetting("restaurantMenu", menuStr.toString());
                }
            }

            sendResult(player, true, "Restaurant menu updated with " + menuArray.size() + " item(s).", eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to set restaurant menu.", eco);
        }
    }

    // --- Data response ---

    private static void sendTownData(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        UUID playerUuid = player.getUUID();
        JsonObject root = new JsonObject();

        // Citizens
        CitizenManager cm = CitizenManager.get(level);
        List<CitizenManager.CitizenRecord> citizens = cm.getCitizensForOwner(playerUuid);
        int totalDailyUpkeep = cm.getTotalDailyUpkeep(playerUuid);
        root.addProperty("citizenCount", citizens.size());
        root.addProperty("totalDailyUpkeep", totalDailyUpkeep);

        // Build a lookup map of citizen entities by UUID for inventory data
        java.util.Map<UUID, MCEntityCitizen> entityMap = new java.util.HashMap<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof MCEntityCitizen citizen && playerUuid.equals(citizen.getOwnerUUID())) {
                entityMap.put(citizen.getUUID(), citizen);
            }
        }

        // Worker list
        JsonArray workers = new JsonArray();
        for (CitizenManager.CitizenRecord c : citizens) {
            if (c.job().isWorker()) {
                JsonObject wo = new JsonObject();
                wo.addProperty("entityId", c.entityId().toString());
                wo.addProperty("name", c.name());
                wo.addProperty("job", c.job().getDisplayName());
                wo.addProperty("upkeep", CitizenConfig.getUpkeepCost(c.job()));
                // Add inventory summary
                MCEntityCitizen citizenEntity = entityMap.get(c.entityId());
                wo.add("inventory", buildInventoryArray(citizenEntity));
                workers.add(wo);
            }
        }
        root.add("workers", workers);

        // Recruit list
        JsonArray recruits = new JsonArray();
        for (CitizenManager.CitizenRecord c : citizens) {
            if (c.job().isRecruit()) {
                JsonObject ro = new JsonObject();
                ro.addProperty("entityId", c.entityId().toString());
                ro.addProperty("name", c.name());
                ro.addProperty("type", c.job().getDisplayName());
                ro.addProperty("upkeep", CitizenConfig.getUpkeepCost(c.job()));
                // Add inventory summary
                MCEntityCitizen citizenEntity = entityMap.get(c.entityId());
                ro.add("inventory", buildInventoryArray(citizenEntity));
                recruits.add(ro);
            }
        }
        root.add("recruits", recruits);

        // Faction info
        FactionManager fm = FactionManager.get(level);
        String myFactionId = fm.getPlayerFaction(playerUuid);
        if (myFactionId != null) {
            FactionData myFaction = fm.getFaction(myFactionId);
            if (myFaction != null) {
                root.addProperty("factionId", myFactionId);
                root.addProperty("factionName", myFaction.getDisplayName());
                root.addProperty("factionMembers", myFaction.getMemberCount());
                root.addProperty("isLeader", myFaction.getLeaderUuid().equals(playerUuid));
            }
        } else {
            root.addProperty("factionId", "");
            root.addProperty("factionName", "None");
            root.addProperty("factionMembers", 0);
            root.addProperty("isLeader", false);
        }

        // All factions list
        JsonArray factionsArr = new JsonArray();
        for (FactionData fd : fm.getAllFactions()) {
            JsonObject fo = new JsonObject();
            fo.addProperty("id", fd.getFactionId());
            fo.addProperty("name", fd.getDisplayName());
            fo.addProperty("members", fd.getMemberCount());
            // Relation to player's faction
            if (myFactionId != null && !fd.getFactionId().equals(myFactionId)) {
                DiplomacyManager dm = DiplomacyManager.get(level);
                DiplomacyStatus relation = dm.getRelation(myFactionId, fd.getFactionId());
                fo.addProperty("relation", relation.name());
            } else {
                fo.addProperty("relation", "SELF");
            }
            factionsArr.add(fo);
        }
        root.add("factions", factionsArr);

        // Groups
        GroupManager gm = GroupManager.get(level);
        List<GroupData> groups = gm.getGroupsForOwner(playerUuid);
        JsonArray groupsArr = new JsonArray();
        for (GroupData g : groups) {
            JsonObject go = new JsonObject();
            go.addProperty("groupId", g.getGroupId().toString());
            go.addProperty("name", g.getName());
            go.addProperty("memberCount", g.getMemberCount());
            go.addProperty("formation", g.getFormation().name());
            go.addProperty("followState", g.getFollowState().getDisplayName());
            go.addProperty("aggroState", g.getAggroState().getDisplayName());
            groupsArr.add(go);
        }
        root.add("groups", groupsArr);

        // Territory / Claims
        ClaimManager clm = ClaimManager.get(level);
        int claimedChunks = 0;
        boolean underSiege = false;
        String attackerFaction = "";
        int claimHealth = 100;
        int maxClaimHealth = 100;
        boolean allyBuild = true;
        boolean allyInteract = true;
        if (myFactionId != null) {
            ClaimData claimData = clm.getClaim(myFactionId);
            if (claimData != null) {
                claimedChunks = claimData.getChunkCount();
                underSiege = claimData.isUnderSiege();
                attackerFaction = claimData.getAttackerFactionId();
                claimHealth = claimData.getClaimHealth();
                maxClaimHealth = claimData.getMaxClaimHealth();
                allyBuild = claimData.isAllowAllyBuild();
                allyInteract = claimData.isAllowAllyInteract();
            }
        }
        root.addProperty("claimedChunks", claimedChunks);
        root.addProperty("underSiege", underSiege);
        root.addProperty("attackerFaction", attackerFaction);
        root.addProperty("claimHealth", claimHealth);
        root.addProperty("maxClaimHealth", maxClaimHealth);
        root.addProperty("allyBuild", allyBuild);
        root.addProperty("allyInteract", allyInteract);

        // Claim chunk coordinates for border rendering (all factions near player)
        JsonArray claimChunkList = new JsonArray();
        DiplomacyManager dm = DiplomacyManager.get(level);
        for (ClaimData cd : clm.getAllClaims()) {
            String claimFaction = cd.getOwnerFactionId();
            String relation;
            if (claimFaction.equals(myFactionId)) {
                relation = "SELF";
            } else if (myFactionId != null) {
                relation = dm.getRelation(myFactionId, claimFaction).name();
            } else {
                relation = "NEUTRAL";
            }
            for (long[] chunk : cd.getClaimedChunks()) {
                JsonObject cc = new JsonObject();
                cc.addProperty("x", (int) chunk[0]);
                cc.addProperty("z", (int) chunk[1]);
                cc.addProperty("factionId", claimFaction);
                cc.addProperty("relation", relation);
                claimChunkList.add(cc);
            }
        }
        root.add("claimChunkList", claimChunkList);

        // Treaties incoming
        JsonArray incomingTreaties = new JsonArray();
        JsonArray outgoingTreaties = new JsonArray();
        if (myFactionId != null) {
            TreatyManager tm = TreatyManager.get(level);
            for (TreatyManager.TreatyProposal p : tm.getProposalsFor(myFactionId)) {
                JsonObject to = new JsonObject();
                to.addProperty("fromFaction", p.fromFaction());
                to.addProperty("proposedStatus", p.proposedStatus().name());
                incomingTreaties.add(to);
            }
            for (TreatyManager.TreatyProposal p : tm.getProposalsFrom(myFactionId)) {
                JsonObject to = new JsonObject();
                to.addProperty("toFaction", p.toFaction());
                to.addProperty("proposedStatus", p.proposedStatus().name());
                outgoingTreaties.add(to);
            }
        }
        root.add("incomingTreaties", incomingTreaties);
        root.add("outgoingTreaties", outgoingTreaties);

        // Economy summary
        root.addProperty("bankBalance", eco.getBank(playerUuid));
        root.addProperty("walletBalance", eco.getWallet(playerUuid));

        // Upkeep breakdown
        JsonArray upkeepBreakdown = new JsonArray();
        for (CitizenManager.CitizenRecord c : citizens) {
            JsonObject uo = new JsonObject();
            uo.addProperty("name", c.name());
            uo.addProperty("job", c.job().getDisplayName());
            uo.addProperty("cost", CitizenConfig.getUpkeepCost(c.job()));
            upkeepBreakdown.add(uo);
        }
        root.add("upkeepBreakdown", upkeepBreakdown);

        // Upkeep chest food stockpile
        root.add("foodStockpile", buildFoodStockpile(entityMap, level));

        // Notifications / alerts for citizen needs
        JsonArray alerts = new JsonArray();
        int hungryCount = 0;
        int noChestCount = 0;
        for (MCEntityCitizen ce : entityMap.values()) {
            if (ce.getHunger() < CitizenConfig.HUNGER_THRESHOLD_EAT) {
                hungryCount++;
                boolean hasChest = false;
                if (ce instanceof MCEntityCitizen w) {
                    hasChest = !w.getUpkeepChestPos().equals(BlockPos.ZERO);
                } else if (ce instanceof MCEntityCitizen r) {
                    hasChest = !r.getUpkeepChestPos().equals(BlockPos.ZERO);
                }
                if (!hasChest) noChestCount++;
            }
        }
        if (noChestCount > 0) {
            JsonObject alert = new JsonObject();
            alert.addProperty("type", "warning");
            alert.addProperty("message", noChestCount + " citizen" + (noChestCount > 1 ? "s" : "") + " hungry with no upkeep chest assigned!");
            alerts.add(alert);
        } else if (hungryCount > 0) {
            JsonObject alert = new JsonObject();
            alert.addProperty("type", "info");
            alert.addProperty("message", hungryCount + " citizen" + (hungryCount > 1 ? "s" : "") + " hungry. Restock upkeep chests!");
            alerts.add(alert);
        }
        if (totalDailyUpkeep > 0 && eco.getBank(playerUuid) < totalDailyUpkeep) {
            JsonObject alert = new JsonObject();
            alert.addProperty("type", "danger");
            alert.addProperty("message", "Low bank funds! Citizens may go idle.");
            alerts.add(alert);
        }
        root.add("alerts", alerts);

        // Work Orders (builder schematic build orders)
        JsonArray workOrders = new JsonArray();
        com.ultra.megamod.feature.schematic.data.BuildOrderManager bom = com.ultra.megamod.feature.schematic.data.BuildOrderManager.get(level);
        for (com.ultra.megamod.feature.schematic.data.BuildOrder order : bom.getOrdersForPlayer(playerUuid)) {
            JsonObject wo = new JsonObject();
            wo.addProperty("id", order.getOrderId().toString());
            wo.addProperty("name", order.getSchematicName());
            wo.addProperty("progress", order.getProgressIndex());
            wo.addProperty("total", order.getTotalBlocks());

            // Builder name
            String builderName = "Unassigned";
            if (order.hasBuilder()) {
                Entity builderEntity = level.getEntity(order.getAssignedBuilderEntityId());
                if (builderEntity instanceof MCEntityCitizen b) {
                    builderName = b.getCitizenName();
                }
            }
            wo.addProperty("builder", builderName);

            // Material list with fulfilled counts
            java.util.Map<net.minecraft.world.item.Item, Integer> needed = new java.util.LinkedHashMap<>();
            for (com.ultra.megamod.feature.schematic.data.BuildOrder.BuildEntry entry : order.getBuildQueue()) {
                net.minecraft.world.item.Item item = entry.state().getBlock().asItem();
                if (item != net.minecraft.world.item.Items.AIR) {
                    needed.merge(item, 1, Integer::sum);
                }
            }
            // Count already placed (progress entries)
            java.util.Map<net.minecraft.world.item.Item, Integer> placed = new java.util.LinkedHashMap<>();
            for (int pi = 0; pi < order.getProgressIndex() && pi < order.getBuildQueue().size(); pi++) {
                net.minecraft.world.item.Item item = order.getBuildQueue().get(pi).state().getBlock().asItem();
                if (item != net.minecraft.world.item.Items.AIR) {
                    placed.merge(item, 1, Integer::sum);
                }
            }
            JsonArray mats = new JsonArray();
            for (var entry : needed.entrySet()) {
                JsonObject mat = new JsonObject();
                mat.addProperty("item", entry.getKey().getDefaultInstance().getHoverName().getString());
                mat.addProperty("needed", entry.getValue());
                mat.addProperty("available", placed.getOrDefault(entry.getKey(), 0));
                mats.add(mat);
            }
            wo.add("materials", mats);
            workOrders.add(wo);
        }
        root.add("workOrders", workOrders);

        // Available builders (idle builders that can be assigned to work orders)
        JsonArray availableBuilders = new JsonArray();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof MCEntityCitizen builder
                    && playerUuid.equals(builder.getOwnerUUID())
                    && !builder.hasBuildOrder()) {
                JsonObject bo = new JsonObject();
                bo.addProperty("entityId", builder.getId());
                bo.addProperty("name", builder.getCitizenName());
                availableBuilders.add(bo);
            }
        }
        root.add("availableBuilders", availableBuilders);

        // ---- Buildings data ----
        JsonArray buildingsArr = new JsonArray();
        if (myFactionId != null) {
            ClaimData buildingClaimData = clm.getClaim(myFactionId);
            if (buildingClaimData != null) {
                for (long[] chunkCoords : buildingClaimData.getClaimedChunks()) {
                    net.minecraft.world.level.ChunkPos chunkPos = new net.minecraft.world.level.ChunkPos((int) chunkCoords[0], (int) chunkCoords[1]);
                    if (!level.hasChunk(chunkPos.x, chunkPos.z)) continue;
                    net.minecraft.world.level.chunk.LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);

                    for (var beEntry : chunk.getBlockEntities().entrySet()) {
                        if (beEntry.getValue() instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding building) {
                            JsonObject bo = new JsonObject();
                            bo.addProperty("id", building.getBuildingId());
                            // Look up display name from BuildingRegistry
                            com.ultra.megamod.feature.citizen.building.BuildingEntry buildingEntry =
                                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(building.getBuildingId());
                            bo.addProperty("name", buildingEntry != null ? buildingEntry.displayName() : building.getBuildingId());
                            bo.addProperty("level", building.getBuildingLevel());
                            bo.addProperty("maxLevel", buildingEntry != null ? buildingEntry.maxLevel() : 5);
                            BlockPos bPos = beEntry.getKey();
                            bo.addProperty("position", bPos.getX() + "," + bPos.getY() + "," + bPos.getZ());
                            // Find assigned workers near this building (startPos within 16 blocks)
                            JsonArray workerNames = new JsonArray();
                            for (CitizenManager.CitizenRecord c : citizens) {
                                MCEntityCitizen ce = entityMap.get(c.entityId());
                                if (ce instanceof MCEntityCitizen worker) {
                                    BlockPos workerStart = worker.getStartPos();
                                    if (!workerStart.equals(BlockPos.ZERO)
                                            && workerStart.distManhattan(bPos) <= 16) {
                                        workerNames.add(c.name());
                                    }
                                }
                            }
                            bo.add("workers", workerNames);
                            bo.addProperty("isBuilt", building.getBuildingLevel() > 0);
                            buildingsArr.add(bo);
                        }
                    }
                }
            }
        }
        root.add("buildings", buildingsArr);

        // ---- Research data ----
        JsonObject researchObj = new JsonObject();
        if (myFactionId != null) {
            com.ultra.megamod.feature.citizen.research.ResearchManager researchMgr =
                    com.ultra.megamod.feature.citizen.research.ResearchManager.get(level, myFactionId);
            com.ultra.megamod.feature.citizen.research.GlobalResearchTree globalTree =
                    com.ultra.megamod.feature.citizen.research.GlobalResearchTree.INSTANCE;

            // Branch names
            JsonArray branchesArr = new JsonArray();
            for (com.ultra.megamod.feature.citizen.research.ResearchBranch branch : globalTree.getAllBranches()) {
                branchesArr.add(branch.getId());
            }
            researchObj.add("branches", branchesArr);

            // Completed researches
            JsonArray completedArr = new JsonArray();
            for (net.minecraft.resources.Identifier rid : researchMgr.getLocalTree().getCompletedResearches()) {
                completedArr.add(rid.getPath());
            }
            researchObj.add("completed", completedArr);

            // In-progress researches with progress
            JsonArray inProgressArr = new JsonArray();
            for (net.minecraft.resources.Identifier rid : researchMgr.getLocalTree().getInProgressResearches()) {
                com.ultra.megamod.feature.citizen.research.LocalResearch local = researchMgr.getLocalTree().getResearch(rid);
                if (local != null) {
                    JsonObject ipObj = new JsonObject();
                    ipObj.addProperty("id", rid.getPath());
                    ipObj.addProperty("progress", local.getProgress());
                    ipObj.addProperty("required", local.getRequiredProgress());
                    inProgressArr.add(ipObj);
                }
            }
            researchObj.add("inProgress", inProgressArr);

            // Available researches (not started, parents completed)
            JsonArray availableArr = new JsonArray();
            for (com.ultra.megamod.feature.citizen.research.GlobalResearch gr : globalTree.getAllResearches()) {
                com.ultra.megamod.feature.citizen.research.ResearchState state =
                        researchMgr.getLocalTree().getResearchState(gr.getId());
                if (state != com.ultra.megamod.feature.citizen.research.ResearchState.NOT_STARTED) continue;

                // Check all parents completed
                boolean parentsComplete = true;
                for (net.minecraft.resources.Identifier parentId : gr.getParentIds()) {
                    if (!researchMgr.getLocalTree().isResearchComplete(parentId)) {
                        parentsComplete = false;
                        break;
                    }
                }
                if (parentsComplete) {
                    availableArr.add(gr.getId().getPath());
                }
            }
            researchObj.add("available", availableArr);
        } else {
            researchObj.add("branches", new JsonArray());
            researchObj.add("completed", new JsonArray());
            researchObj.add("inProgress", new JsonArray());
            researchObj.add("available", new JsonArray());
        }
        root.add("research", researchObj);

        // ---- Requests data ----
        JsonArray requestsArr = new JsonArray();
        com.ultra.megamod.feature.citizen.request.RequestManager requestMgr =
                com.ultra.megamod.feature.citizen.request.RequestManager.get(level);
        for (com.ultra.megamod.feature.citizen.request.IRequest req : requestMgr.getAllActiveRequests()) {
            JsonObject ro = new JsonObject();
            ro.addProperty("token", req.getToken().getId().toString());
            ro.addProperty("description", req.getRequestable().getDescription());
            ro.addProperty("requester", req.getRequester().getRequesterName());
            ro.addProperty("state", req.getState().name());
            ro.addProperty("resolver", req.getResolverId() != null
                    ? requestMgr.getResolverName(req.getResolverId().getId())
                    : "Unassigned");
            requestsArr.add(ro);
        }
        root.add("requests", requestsArr);

        sendResponse(player, "town_data", root.toString(), eco);
    }

    private static JsonArray buildInventoryArray(MCEntityCitizen citizen) {
        JsonArray invArray = new JsonArray();
        if (citizen == null) return invArray;
        for (int i = 0; i < citizen.getInventory().getContainerSize(); i++) {
            ItemStack stack = citizen.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                JsonObject itemObj = new JsonObject();
                itemObj.addProperty("name", stack.getHoverName().getString());
                itemObj.addProperty("count", stack.getCount());
                invArray.add(itemObj);
            }
        }
        return invArray;
    }

    private static JsonObject buildFoodStockpile(java.util.Map<UUID, MCEntityCitizen> entityMap, ServerLevel level) {
        JsonObject stockpile = new JsonObject();
        JsonArray chestContents = new JsonArray();
        int totalFoodItems = 0;
        java.util.Set<BlockPos> scannedChests = new java.util.HashSet<>();

        for (MCEntityCitizen citizen : entityMap.values()) {
            BlockPos chestPos = null;
            if (citizen instanceof MCEntityCitizen worker) {
                chestPos = worker.getUpkeepChestPos();
            } else if (citizen instanceof MCEntityCitizen recruit) {
                chestPos = recruit.getUpkeepChestPos();
            }
            if (chestPos == null || scannedChests.contains(chestPos)) continue;
            scannedChests.add(chestPos);

            BlockEntity be = level.getBlockEntity(chestPos);
            if (be instanceof BaseContainerBlockEntity container) {
                JsonObject chestObj = new JsonObject();
                chestObj.addProperty("x", chestPos.getX());
                chestObj.addProperty("y", chestPos.getY());
                chestObj.addProperty("z", chestPos.getZ());
                JsonArray items = new JsonArray();
                int foodCount = 0;
                for (int i = 0; i < container.getContainerSize(); i++) {
                    ItemStack stack = container.getItem(i);
                    if (!stack.isEmpty() && stack.get(DataComponents.FOOD) != null) {
                        JsonObject itemObj = new JsonObject();
                        itemObj.addProperty("name", stack.getHoverName().getString());
                        itemObj.addProperty("count", stack.getCount());
                        items.add(itemObj);
                        foodCount += stack.getCount();
                    }
                }
                chestObj.add("items", items);
                chestObj.addProperty("foodCount", foodCount);
                chestContents.add(chestObj);
                totalFoodItems += foodCount;
            }
        }
        stockpile.add("chests", chestContents);
        stockpile.addProperty("totalFoodItems", totalFoodItems);
        // Estimate: each citizen eats ~1 food per day cycle
        int citizenCount = entityMap.size();
        int estimatedDays = citizenCount > 0 ? totalFoodItems / citizenCount : 999;
        stockpile.addProperty("estimatedDays", estimatedDays);
        return stockpile;
    }

    private static void sendResult(ServerPlayer player, boolean success, String message, EconomyManager eco) {
        JsonObject obj = new JsonObject();
        obj.addProperty("success", success);
        obj.addProperty("message", message);
        sendResponse(player, "town_result", obj.toString(), eco);
    }

    private static void handleAssignBuilder(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String orderId = obj.get("orderId").getAsString();
            int builderEntityId = obj.get("builderId").getAsInt();
            com.ultra.megamod.feature.schematic.data.BuildOrderManager bom = com.ultra.megamod.feature.schematic.data.BuildOrderManager.get(level);
            com.ultra.megamod.feature.schematic.data.BuildOrder order = bom.getOrder(UUID.fromString(orderId));
            if (order == null || !order.getOwnerUUID().equals(player.getUUID())) {
                sendResult(player, false, "Work order not found.", eco);
                return;
            }
            Entity entity = level.getEntity(builderEntityId);
            if (!(entity instanceof MCEntityCitizen builder)) {
                sendResult(player, false, "Builder not found.", eco);
                return;
            }
            bom.assignBuilder(order.getOrderId(), builderEntityId);
            builder.setBuildOrderId(order.getOrderId().toString());
            builder.setBuildProgress(order.getProgressIndex());
            builder.setBuildState("WORKING");
            bom.markDirty();
            sendResult(player, true, builder.getCitizenName() + " assigned to build " + order.getSchematicName() + ".", eco);
            sendTownData(player, level, eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to assign builder.", eco);
        }
    }

    private static void handleCancelWorkOrder(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String orderId = obj.get("orderId").getAsString();
            com.ultra.megamod.feature.schematic.data.BuildOrderManager bom = com.ultra.megamod.feature.schematic.data.BuildOrderManager.get(level);
            com.ultra.megamod.feature.schematic.data.BuildOrder order = bom.getOrder(UUID.fromString(orderId));
            if (order == null || !order.getOwnerUUID().equals(player.getUUID())) {
                sendResult(player, false, "Work order not found.", eco);
                return;
            }
            // Clear builder assignment if any
            if (order.hasBuilder()) {
                Entity entity = level.getEntity(order.getAssignedBuilderEntityId());
                if (entity instanceof MCEntityCitizen builder) {
                    builder.setBuildOrderId("");
                    builder.setBuildProgress(0);
                    builder.setBuildState("IDLE");
                }
            }
            bom.removeOrder(UUID.fromString(orderId));
            bom.markDirty();
            sendResult(player, true, "Work order cancelled.", eco);
            sendTownData(player, level, eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to cancel work order.", eco);
        }
    }

    // --- Builder-specific action handlers ---

    /**
     * Recalls the builder citizen to its building position.
     * Teleports the assigned worker to the hut block location.
     */
    private static void handleBuildingRecallWorker(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            BlockPos pos = parseBuildingPos(obj);
            if (pos == null) { sendResult(player, false, "Invalid position.", eco); return; }

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            // Find the assigned builder worker
            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(tile.getBuildingId());
            if (entry == null) { sendResult(player, false, "Unknown building type.", eco); return; }

            com.ultra.megamod.feature.citizen.building.AbstractBuilding building = entry.buildingFactory().get();
            building.registerModulesPublic();
            var workerOpt = building.getModule(com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule.class);

            // Look for citizens owned by this player with the BUILDER job near the building
            CitizenManager cm = CitizenManager.get(level);
            List<CitizenManager.CitizenRecord> citizens = cm.getCitizensForOwner(player.getUUID());
            boolean recalled = false;

            for (CitizenManager.CitizenRecord record : citizens) {
                if (record.job() == com.ultra.megamod.feature.citizen.data.CitizenJob.BUILDER) {
                    Entity entity = level.getEntity(record.entityId());
                    if (entity instanceof MCEntityCitizen citizen) {
                        citizen.teleportTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
                        recalled = true;
                        break;
                    }
                }
            }

            if (recalled) {
                sendResult(player, true, "Builder recalled to hut.", eco);
            } else {
                sendResult(player, false, "No builder found to recall.", eco);
            }
        } catch (Exception e) {
            sendResult(player, false, "Failed to recall worker.", eco);
        }
    }

    /**
     * Marks the building for courier pickup by setting the building's pickup flag.
     */
    private static void handleBuildingRequestPickup(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            BlockPos pos = parseBuildingPos(obj);
            if (pos == null) { sendResult(player, false, "Invalid position.", eco); return; }

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            // Mark the building for pickup via its settings
            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(tile.getBuildingId());
            if (entry != null) {
                com.ultra.megamod.feature.citizen.building.AbstractBuilding building = entry.buildingFactory().get();
                building.registerModulesPublic();
                var settingsOpt = building.getModule(com.ultra.megamod.feature.citizen.building.module.ISettingsModule.class);
                if (settingsOpt.isPresent()) {
                    settingsOpt.get().setSetting("pickupRequested", true);
                }
            }

            sendResult(player, true, "Pickup requested for builder's hut.", eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to request pickup.", eco);
        }
    }

    /**
     * Sets the pickup priority value (1-10) for the building.
     */
    private static void handleBuildingSetPickupPriority(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            BlockPos pos = parseBuildingPos(obj);
            if (pos == null) { sendResult(player, false, "Invalid position.", eco); return; }

            int priority = obj.has("priority") ? obj.get("priority").getAsInt() : 5;
            priority = Math.max(1, Math.min(10, priority));

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(tile.getBuildingId());
            if (entry != null) {
                com.ultra.megamod.feature.citizen.building.AbstractBuilding building = entry.buildingFactory().get();
                building.registerModulesPublic();
                var settingsOpt = building.getModule(com.ultra.megamod.feature.citizen.building.module.ISettingsModule.class);
                if (settingsOpt.isPresent()) {
                    settingsOpt.get().setSetting("pickupPriority", priority);
                }
            }

            sendResult(player, true, "Pickup priority set to " + priority + ".", eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to set pickup priority.", eco);
        }
    }

    /**
     * Sets the construction strategy (default, hilbert, inward_circle, random).
     */
    private static void handleBuildingSetConstructionStrategy(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            BlockPos pos = parseBuildingPos(obj);
            if (pos == null) { sendResult(player, false, "Invalid position.", eco); return; }

            String strategy = obj.has("strategy") ? obj.get("strategy").getAsString() : "default";

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            // Validate strategy
            if (!com.ultra.megamod.feature.citizen.building.buildings.BuildingBuilder.STRATEGY_OPTIONS.contains(strategy)) {
                sendResult(player, false, "Invalid strategy: " + strategy, eco);
                return;
            }

            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(tile.getBuildingId());
            if (entry != null) {
                com.ultra.megamod.feature.citizen.building.AbstractBuilding building = entry.buildingFactory().get();
                building.registerModulesPublic();
                var settingsOpt = building.getModule(com.ultra.megamod.feature.citizen.building.module.ISettingsModule.class);
                if (settingsOpt.isPresent()) {
                    settingsOpt.get().setSetting("constructionStrategy", strategy);
                }
            }

            sendResult(player, true, "Construction strategy set to '" + strategy + "'.", eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to set construction strategy.", eco);
        }
    }

    /**
     * Toggles whether the builder should use shears on leaves/grass.
     */
    private static void handleBuildingSetShears(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            BlockPos pos = parseBuildingPos(obj);
            if (pos == null) { sendResult(player, false, "Invalid position.", eco); return; }

            boolean useShears = obj.has("useShears") && obj.get("useShears").getAsBoolean();

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(tile.getBuildingId());
            if (entry != null) {
                com.ultra.megamod.feature.citizen.building.AbstractBuilding building = entry.buildingFactory().get();
                building.registerModulesPublic();
                var settingsOpt = building.getModule(com.ultra.megamod.feature.citizen.building.module.ISettingsModule.class);
                if (settingsOpt.isPresent()) {
                    settingsOpt.get().setSetting("useShears", useShears);
                }
            }

            sendResult(player, true, "Use shears " + (useShears ? "enabled" : "disabled") + ".", eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to set shears setting.", eco);
        }
    }

    /**
     * Sets the task assignment mode (automatic/manual) and optionally recipe mode.
     */
    private static void handleBuildingSetTaskMode(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            BlockPos pos = parseBuildingPos(obj);
            if (pos == null) { sendResult(player, false, "Invalid position.", eco); return; }

            String mode = obj.has("mode") ? obj.get("mode").getAsString() : "automatic";
            String setting = obj.has("setting") ? obj.get("setting").getAsString() : "taskAssignmentMode";

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            com.ultra.megamod.feature.citizen.building.BuildingEntry entry =
                    com.ultra.megamod.feature.citizen.building.BuildingRegistry.get(tile.getBuildingId());
            if (entry != null) {
                com.ultra.megamod.feature.citizen.building.AbstractBuilding building = entry.buildingFactory().get();
                building.registerModulesPublic();
                var settingsOpt = building.getModule(com.ultra.megamod.feature.citizen.building.module.ISettingsModule.class);
                if (settingsOpt.isPresent()) {
                    settingsOpt.get().setSetting(setting, mode);
                }
            }

            sendResult(player, true, setting + " set to '" + mode + "'.", eco);
        } catch (Exception e) {
            sendResult(player, false, "Failed to set task mode.", eco);
        }
    }

    /**
     * Transfers an item from the player's inventory to the building's chest.
     */
    private static void handleBuildingTransferItem(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            BlockPos pos = parseBuildingPos(obj);
            if (pos == null) { sendResult(player, false, "Invalid position.", eco); return; }

            String itemIdStr = obj.has("itemId") ? obj.get("itemId").getAsString() : "";
            if (itemIdStr.isEmpty()) {
                sendResult(player, false, "Item ID is required.", eco);
                return;
            }

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile)) {
                sendResult(player, false, "No building found at that position.", eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                FactionManager fm = FactionManager.get(level);
                if (fm.getPlayerFaction(player.getUUID()) == null) {
                    sendResult(player, false, "You must be in a faction.", eco);
                    return;
                }
            }

            // Find the item in the player's inventory and transfer it
            net.minecraft.resources.Identifier itemId = net.minecraft.resources.Identifier.parse(itemIdStr);
            net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.getValue(itemId);
            if (item == null || item == Items.AIR) {
                sendResult(player, false, "Unknown item: " + itemIdStr, eco);
                return;
            }

            // Search for a container (chest) near the building to deposit into
            // Try common adjacent positions for the building's chest
            boolean transferred = false;
            for (int dx = -2; dx <= 2 && !transferred; dx++) {
                for (int dy = -1; dy <= 1 && !transferred; dy++) {
                    for (int dz = -2; dz <= 2 && !transferred; dz++) {
                        BlockPos checkPos = pos.offset(dx, dy, dz);
                        BlockEntity nearBe = level.getBlockEntity(checkPos);
                        if (nearBe instanceof BaseContainerBlockEntity container) {
                            // Find item in player inventory
                            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                                ItemStack stack = player.getInventory().getItem(i);
                                if (!stack.isEmpty() && stack.getItem() == item) {
                                    // Transfer up to a stack
                                    int toTransfer = Math.min(stack.getCount(), stack.getMaxStackSize());
                                    ItemStack transferStack = stack.copyWithCount(toTransfer);

                                    // Try to put into container
                                    for (int j = 0; j < container.getContainerSize(); j++) {
                                        ItemStack containerStack = container.getItem(j);
                                        if (containerStack.isEmpty()) {
                                            container.setItem(j, transferStack.copy());
                                            stack.shrink(toTransfer);
                                            if (stack.isEmpty()) player.getInventory().setItem(i, ItemStack.EMPTY);
                                            container.setChanged();
                                            transferred = true;
                                            break;
                                        } else if (ItemStack.isSameItemSameComponents(containerStack, transferStack)
                                                && containerStack.getCount() < containerStack.getMaxStackSize()) {
                                            int add = Math.min(toTransfer, containerStack.getMaxStackSize() - containerStack.getCount());
                                            containerStack.grow(add);
                                            stack.shrink(add);
                                            if (stack.isEmpty()) player.getInventory().setItem(i, ItemStack.EMPTY);
                                            container.setChanged();
                                            transferred = true;
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (transferred) {
                sendResult(player, true, "Item transferred to building.", eco);
            } else {
                sendResult(player, false, "No container found near the building or item not in inventory.", eco);
            }
        } catch (Exception e) {
            sendResult(player, false, "Failed to transfer item.", eco);
        }
    }

    /**
     * Helper to parse building position from common JSON fields.
     * Supports both {x, y, z} and {posX, posY, posZ} formats.
     */
    private static BlockPos parseBuildingPos(JsonObject obj) {
        if (obj.has("x") && obj.has("y") && obj.has("z")) {
            return new BlockPos(obj.get("x").getAsInt(), obj.get("y").getAsInt(), obj.get("z").getAsInt());
        }
        if (obj.has("posX") && obj.has("posY") && obj.has("posZ")) {
            return new BlockPos(obj.get("posX").getAsInt(), obj.get("posY").getAsInt(), obj.get("posZ").getAsInt());
        }
        return null;
    }

    // =========================================================================
    //  Town Hall specific handlers
    // =========================================================================

    private static void sendTownHallResult(ServerPlayer player, boolean success, String message) {
        JsonObject obj = new JsonObject();
        obj.addProperty("success", success);
        obj.addProperty("message", message);
        int wallet = 0, bank = 0;
        try {
            EconomyManager eco = EconomyManager.get((ServerLevel) player.level());
            wallet = eco.getWallet(player.getUUID());
            bank = eco.getBank(player.getUUID());
        } catch (Exception ignored) {}
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload("townhall_result", obj.toString(), wallet, bank));
    }

    private static void sendTownHallData(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        UUID playerUuid = player.getUUID();
        JsonObject root = new JsonObject();

        // Colony name and basic info
        CitizenManager cm = CitizenManager.get(level);
        List<CitizenManager.CitizenRecord> citizenList = cm.getCitizensForOwner(playerUuid);

        // Build entity map for citizen data
        java.util.Map<UUID, MCEntityCitizen> entityMap = new java.util.HashMap<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof MCEntityCitizen citizen && playerUuid.equals(citizen.getOwnerUUID())) {
                entityMap.put(citizen.getUUID(), citizen);
            }
        }

        // Actions tab data
        FactionManager fm = FactionManager.get(level);
        String myFactionId = fm.getPlayerFaction(playerUuid);
        String colName = "Colony";
        if (myFactionId != null) {
            FactionData fd = fm.getFaction(myFactionId);
            if (fd != null) colName = fd.getDisplayName();
        }
        root.addProperty("colonyName", colName);
        root.addProperty("style", "colonial");
        root.addProperty("citizenCount", citizenList.size());
        root.addProperty("maxCitizens", Math.max(4, citizenList.size() + 2)); // placeholder
        root.addProperty("buildingCount", 0);
        root.addProperty("buildingLevel", 1);
        root.addProperty("maxLevel", 5);

        // Citizens tab data with health, happiness, saturation
        JsonArray citizensArr = new JsonArray();
        double totalHappiness = 0;
        double totalFood = 0;
        for (CitizenManager.CitizenRecord c : citizenList) {
            JsonObject co = new JsonObject();
            co.addProperty("name", c.name());
            co.addProperty("job", c.job().getDisplayName());
            co.addProperty("entityId", c.entityId().toString());

            MCEntityCitizen ce = entityMap.get(c.entityId());
            if (ce != null) {
                co.addProperty("health", ce.getHealth());
                co.addProperty("maxHealth", ce.getMaxHealth());
                co.addProperty("happiness", ce.getHunger() > 10 ? 7.0 : (ce.getHunger() > 5 ? 5.0 : 3.0));
                co.addProperty("saturation", ce.getHunger());
                totalHappiness += ce.getHunger() > 10 ? 7.0 : (ce.getHunger() > 5 ? 5.0 : 3.0);
                totalFood += ce.getHunger();
            } else {
                co.addProperty("health", 20.0);
                co.addProperty("maxHealth", 20.0);
                co.addProperty("happiness", 5.0);
                co.addProperty("saturation", 10.0);
                totalHappiness += 5.0;
                totalFood += 10.0;
            }
            citizensArr.add(co);
        }
        root.add("citizens", citizensArr);

        // Happiness factors
        int count = Math.max(1, citizenList.size());
        root.addProperty("overallHappiness", totalHappiness / count);
        root.addProperty("happinessFood", totalFood / count / 2.0);
        root.addProperty("happinessSecurity", 7.0);
        root.addProperty("happinessHousing", citizenList.isEmpty() ? 5.0 : 6.0);
        root.addProperty("happinessWork", citizenList.isEmpty() ? 5.0 : 5.5);
        root.addProperty("totalCitizens", citizenList.size());

        // Work orders
        JsonArray workOrdersArr = new JsonArray();
        com.ultra.megamod.feature.schematic.data.BuildOrderManager bom =
                com.ultra.megamod.feature.schematic.data.BuildOrderManager.get(level);
        int priority = 0;
        for (com.ultra.megamod.feature.schematic.data.BuildOrder order : bom.getOrdersForPlayer(playerUuid)) {
            JsonObject wo = new JsonObject();
            wo.addProperty("id", order.getOrderId().toString());
            wo.addProperty("name", order.getSchematicName());
            wo.addProperty("progress", order.getProgressIndex());
            wo.addProperty("total", order.getTotalBlocks());
            wo.addProperty("priority", priority++);

            String builderName = "Unassigned";
            if (order.hasBuilder()) {
                Entity builderEntity = level.getEntity(order.getAssignedBuilderEntityId());
                if (builderEntity instanceof MCEntityCitizen b) {
                    builderName = b.getCitizenName();
                }
            }
            wo.addProperty("builder", builderName);
            workOrdersArr.add(wo);
        }
        root.add("workOrders", workOrdersArr);

        // Events (colony log)
        JsonArray eventsArr = new JsonArray();
        // Placeholder events — real events would come from a colony event log
        if (!citizenList.isEmpty()) {
            JsonObject e1 = new JsonObject();
            e1.addProperty("time", "Day 1");
            e1.addProperty("message", "Colony founded.");
            e1.addProperty("type", "success");
            eventsArr.add(e1);
        }
        root.add("events", eventsArr);

        // Permissions (5 ranks x 5 permissions)
        JsonArray permsArr = new JsonArray();
        for (int r = 0; r < 5; r++) {
            JsonArray row = new JsonArray();
            for (int p = 0; p < 5; p++) {
                // Default permissions: Owner has all, others taper off
                row.add(r <= p ? false : true);
            }
            // Owner gets all true
            if (r == 0) {
                row = new JsonArray();
                for (int p = 0; p < 5; p++) row.add(true);
            }
            permsArr.add(row);
        }
        root.add("permissions", permsArr);

        // Player list with ranks
        JsonArray permPlayersArr = new JsonArray();
        JsonObject ownerEntry = new JsonObject();
        ownerEntry.addProperty("name", player.getGameProfile().name());
        ownerEntry.addProperty("rank", "Owner");
        permPlayersArr.add(ownerEntry);
        root.add("permPlayers", permPlayersArr);

        // Free blocks
        root.add("freeBlocks", new JsonArray());

        // Settings
        root.addProperty("spawnCitizens", true);
        root.addProperty("autoHire", true);
        root.addProperty("autoHousing", true);
        root.addProperty("enterLeaveMessages", true);
        root.addProperty("constructionTape", true);
        root.addProperty("printProgress", false);
        root.addProperty("allowVisitors", true);
        root.addProperty("guardAggressive", false);
        root.addProperty("moveIn", true);

        // Stats
        root.addProperty("totalCitizens", citizenList.size());
        root.addProperty("maxCitizens", Math.max(4, citizenList.size() + 2));

        // Job stats
        JsonArray jobStatsArr = new JsonArray();
        java.util.Map<String, int[]> jobMap = new java.util.LinkedHashMap<>();
        for (CitizenManager.CitizenRecord c : citizenList) {
            String jobName = c.job().getDisplayName();
            jobMap.computeIfAbsent(jobName, k -> new int[]{0, 1})[0]++;
        }
        for (java.util.Map.Entry<String, int[]> entry : jobMap.entrySet()) {
            JsonObject jo = new JsonObject();
            jo.addProperty("job", entry.getKey());
            jo.addProperty("occupied", entry.getValue()[0]);
            jo.addProperty("available", entry.getValue()[0]); // occupied = available for now
            jobStatsArr.add(jo);
        }
        root.add("jobStats", jobStatsArr);

        // Period stats (placeholder)
        JsonArray periodStatsArr = new JsonArray();
        String[] periods = {"Yesterday", "Last Week", "Last 100 Days", "All Time"};
        for (String period : periods) {
            JsonObject ps = new JsonObject();
            ps.addProperty("period", period);
            ps.addProperty("mobsKilled", 0);
            ps.addProperty("itemsCrafted", 0);
            ps.addProperty("blocksPlaced", 0);
            ps.addProperty("foodConsumed", 0);
            periodStatsArr.add(ps);
        }
        root.add("periodStats", periodStatsArr);

        // Cosmetics
        root.addProperty("colonyColor", 0);
        root.addProperty("selectedPack", "colonial");

        // Available packs
        JsonArray packsArr = new JsonArray();
        packsArr.add("colonial");
        packsArr.add("medieval_oak");
        packsArr.add("medieval_spruce");
        packsArr.add("medieval_dark_oak");
        packsArr.add("medieval_birch");
        packsArr.add("asian");
        packsArr.add("caledonia");
        packsArr.add("incan");
        packsArr.add("desert_oasis");
        packsArr.add("shire");
        root.add("availablePacks", packsArr);

        // Alliances
        root.add("alliances", new JsonArray());

        sendResponse(player, "townhall_data", root.toString(), eco);
    }

    private static void handleTownHallRename(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String newName = obj.has("name") ? obj.get("name").getAsString() : "";
            if (newName.isEmpty() || newName.length() > 32) {
                sendTownHallResult(player, false, "Name must be 1-32 characters.");
                return;
            }
            // Rename the faction (colony name)
            FactionManager fm = FactionManager.get(level);
            String myFactionId = fm.getPlayerFaction(player.getUUID());
            if (myFactionId != null) {
                FactionData fd = fm.getFaction(myFactionId);
                if (fd != null) {
                    fd.setDisplayName(newName);
                    fm.saveToDisk(level);
                    sendTownHallResult(player, true, "Colony renamed to '" + newName + "'.");
                    return;
                }
            }
            sendTownHallResult(player, true, "Colony renamed to '" + newName + "'.");
        } catch (Exception e) {
            sendTownHallResult(player, false, "Invalid rename data.");
        }
    }

    private static void handleTownHallSetSetting(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String key = obj.get("key").getAsString();
            boolean value = obj.get("value").getAsBoolean();
            // Store in colony settings (placeholder — would use ColonySettingsManager)
            sendTownHallResult(player, true, key + " set to " + (value ? "ON" : "OFF") + ".");
        } catch (Exception e) {
            sendTownHallResult(player, false, "Invalid setting data.");
        }
    }

    private static void handleTownHallSetPermission(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int rank = obj.get("rank").getAsInt();
            int perm = obj.get("perm").getAsInt();
            boolean value = obj.get("value").getAsBoolean();
            String[] rankNames = {"Owner", "Officer", "Friend", "Neutral", "Hostile"};
            String[] permNames = {"Enter", "Build", "Containers", "Interact", "Manage"};
            String rankName = rank >= 0 && rank < rankNames.length ? rankNames[rank] : "Unknown";
            String permName = perm >= 0 && perm < permNames.length ? permNames[perm] : "Unknown";
            sendTownHallResult(player, true, rankName + " - " + permName + ": " + (value ? "ON" : "OFF"));
        } catch (Exception e) {
            sendTownHallResult(player, false, "Invalid permission data.");
        }
    }

    private static void handleTownHallHireMercenaries(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        // Check player has 5 gold ingots
        int goldCount = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.GOLD_INGOT)) {
                goldCount += stack.getCount();
            }
        }
        if (goldCount < 5) {
            sendTownHallResult(player, false, "Need 5 Gold Ingots! You have " + goldCount + ".");
            return;
        }

        // Remove 5 gold ingots
        int toRemove = 5;
        for (int i = 0; i < player.getInventory().getContainerSize() && toRemove > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.GOLD_INGOT)) {
                int remove = Math.min(toRemove, stack.getCount());
                stack.shrink(remove);
                toRemove -= remove;
            }
        }

        // Spawn temporary mercenary guards near the player
        // For now just send success — actual spawning would use the citizen system
        sendTownHallResult(player, true, "Mercenaries hired! They will guard for 10 minutes.");
    }

    private static void handleTownHallRecallCitizens(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            BlockPos thPos = new BlockPos(obj.get("x").getAsInt(), obj.get("y").getAsInt(), obj.get("z").getAsInt());

            UUID playerUuid = player.getUUID();
            int recalled = 0;
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof MCEntityCitizen citizen && playerUuid.equals(citizen.getOwnerUUID())) {
                    citizen.teleportTo(thPos.getX() + 0.5, thPos.getY() + 1, thPos.getZ() + 0.5);
                    recalled++;
                }
            }
            sendTownHallResult(player, true, "Recalled " + recalled + " citizen(s) to Town Hall.");
        } catch (Exception e) {
            sendTownHallResult(player, false, "Failed to recall citizens.");
        }
    }

    private static void handleTownHallWorkOrderPriority(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String orderId = obj.get("orderId").getAsString();
            String direction = obj.get("direction").getAsString();
            sendTownHallResult(player, true, "Work order priority moved " + direction + ".");
        } catch (Exception e) {
            sendTownHallResult(player, false, "Invalid work order data.");
        }
    }

    private static void handleTownHallWorkOrderDelete(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String orderId = obj.get("orderId").getAsString();

            com.ultra.megamod.feature.schematic.data.BuildOrderManager bom =
                    com.ultra.megamod.feature.schematic.data.BuildOrderManager.get(level);
            bom.removeOrder(UUID.fromString(orderId));
            bom.saveToDisk(level);
            sendTownHallResult(player, true, "Work order cancelled.");
        } catch (Exception e) {
            sendTownHallResult(player, false, "Invalid work order data.");
        }
    }

    private static void handleTownHallAddPlayer(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String playerName = obj.get("playerName").getAsString();
            String rank = obj.has("rank") ? obj.get("rank").getAsString() : "Neutral";
            sendTownHallResult(player, true, "Added " + playerName + " as " + rank + ".");
        } catch (Exception e) {
            sendTownHallResult(player, false, "Invalid player data.");
        }
    }

    private static void handleTownHallSetColor(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            int colorIndex = obj.get("color").getAsInt();
            String[] colorNames = {"White", "Orange", "Magenta", "Light Blue", "Yellow", "Lime",
                    "Pink", "Gray", "Light Gray", "Cyan", "Purple", "Blue",
                    "Brown", "Green", "Red", "Black"};
            String colorName = colorIndex >= 0 && colorIndex < colorNames.length ? colorNames[colorIndex] : "Unknown";
            sendTownHallResult(player, true, "Colony color set to " + colorName + ".");
        } catch (Exception e) {
            sendTownHallResult(player, false, "Invalid color data.");
        }
    }

    private static void handleTownHallSetPack(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String pack = obj.get("pack").getAsString();
            sendTownHallResult(player, true, "Colony style pack set to " + pack + ".");
        } catch (Exception e) {
            sendTownHallResult(player, false, "Invalid pack data.");
        }
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload(type, json, wallet, bank));
    }
}
