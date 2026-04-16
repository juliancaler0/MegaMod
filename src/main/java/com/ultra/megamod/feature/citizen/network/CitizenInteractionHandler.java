package com.ultra.megamod.feature.citizen.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.citizen.CitizenConfig;
import com.ultra.megamod.feature.citizen.CitizenEvents;
import com.ultra.megamod.feature.citizen.CitizenManager;
import com.ultra.megamod.feature.citizen.CitizenRegistry;
import com.ultra.megamod.feature.citizen.data.*;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import java.util.List;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.neoforged.neoforge.network.PacketDistributor;

public class CitizenInteractionHandler {

    public static void handleAction(ServerPlayer player, int entityId, String action, String jsonData) {
        ServerLevel level = (ServerLevel) player.level();
        EconomyManager eco = EconomyManager.get(level);

        switch (action) {
            case "hire" -> handleHire(player, entityId, jsonData, level, eco);
            case "dismiss" -> handleDismiss(player, entityId, level, eco);
            case "set_bed" -> handleSetPosition(player, entityId, "bed", jsonData, level);
            case "set_chest" -> handleSetPosition(player, entityId, "chest", jsonData, level);
            case "set_work" -> handleSetPosition(player, entityId, "work", jsonData, level);
            case "set_follow" -> handleSetStatus(player, entityId, CitizenStatus.FOLLOW, level);
            case "set_stay" -> handleSetStatus(player, entityId, CitizenStatus.HOLD_POSITION, level);
            case "set_work_mode" -> handleSetStatus(player, entityId, CitizenStatus.WORK, level);
            case "set_group" -> handleSetGroup(player, entityId, jsonData, level);
            case "set_aggro" -> handleSetAggroState(player, entityId, jsonData, level);
            case "rename" -> handleRename(player, entityId, jsonData, level);
            case "open_inventory" -> handleOpenInventory(player, entityId, level);
            case "set_upkeep_chest" -> handleSetUpkeepChest(player, entityId, jsonData, level);
            case "set_town_chest" -> handleSetTownChest(player, entityId, jsonData, level);
            case "set_worker_setting" -> handleSetWorkerSetting(player, entityId, jsonData, level);
            case "merchant_shop_request" -> handleMerchantShopRequest(player, entityId, level);
            case "merchant_buy" -> handleMerchantBuy(player, entityId, jsonData, level, eco);
            case "hire_unowned" -> handleHireUnowned(player, entityId, jsonData, level, eco);
            case "herald_get_quests" -> handleHeraldGetQuests(player, entityId, level);
            case "herald_accept_quest" -> handleHeraldAcceptQuest(player, entityId, jsonData, level, eco);
            case "view_work_area" -> handleViewWorkArea(player, entityId, level);
            case "build_pen_preview" -> handleBuildPenPreview(player, entityId, level);
            case "build_pen_confirm" -> handleBuildPenConfirm(player, entityId, level);
            case "remove_pen" -> handleRemovePen(player, entityId, level);
            case "rebuild_pen" -> handleRebuildPen(player, entityId, jsonData, level);
            case "cancel_build" -> handleCancelBuild(player, entityId, level);
            case "quest_dialogue_response" -> handleQuestDialogueResponse(player, entityId, jsonData, level);
            case "quest_get_dialogue" -> handleQuestGetDialogue(player, entityId, level);
            default -> {}
        }
    }

    private static void handleHire(ServerPlayer player, int entityId, String jsonData, ServerLevel level, EconomyManager eco) {
        Entity target = level.getEntity(entityId);
        if (!(target instanceof Villager villager)) return;

        // Require faction membership to hire citizens
        FactionManager fm = FactionManager.get(level);
        String factionId = fm.getPlayerFaction(player.getUUID());
        if (factionId == null) {
            sendResult(player, false, "You must be in a faction to hire citizens! Place a Town Chest to create one.");
            return;
        }

        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String jobName = json.get("job").getAsString();
        String citizenName = json.has("name") ? json.get("name").getAsString() : "Citizen";
        CitizenJob job = CitizenJob.fromString(jobName);

        // Check max citizens (arcane tree summoner branch adds bonus capacity)
        CitizenManager cm = CitizenManager.get(level);
        int maxCitizens = CitizenConfig.MAX_CITIZENS_PER_PLAYER
                + 0 /* TODO: Reconnect with Pufferfish Skills API (was CitizenSkillBonuses.getMaxCitizenBonus) */;
        if (cm.getCitizenCount(player.getUUID()) >= maxCitizens) {
            sendResult(player, false, "Maximum citizens reached!");
            return;
        }

        // Check merchant limit (max 3 per faction)
        if (job == CitizenJob.MERCHANT) {
            long merchantCount = level.getEntitiesOfClass(MCEntityCitizen.class,
                    player.getBoundingBox().inflate(1000),
                    m -> m.getCitizenJobHandler().getColonyJob() == CitizenJob.MERCHANT).size();
            if (merchantCount >= 3) {
                sendResult(player, false, "Maximum 3 merchants allowed!");
                return;
            }
        }

        // Check cost (arcane tree summoner branch reduces hire cost)
        int baseCost = CitizenConfig.getHireCost(job);
        double hireMult = 1.0 /* TODO: Reconnect with Pufferfish Skills API (was CitizenSkillBonuses.getHireCostMultiplier) */;
        int cost = Math.max(1, (int) (baseCost * hireMult));
        if (!eco.spendWallet(player.getUUID(), cost)) {
            sendResult(player, false, "Not enough MegaCoins! Need " + cost + " MC.");
            return;
        }

        // Spawn citizen entity at villager's position, then remove villager
        BlockPos pos = villager.blockPosition();
        float yRot = villager.getYRot();

        // Create the actual entity via registry lookup
        net.minecraft.world.entity.EntityType<?> entityType = CitizenRegistry.getEntityTypeForJob(job);
        if (entityType == null) {
            sendResult(player, false, "Unknown citizen type!");
            eco.addWallet(player.getUUID(), cost); // refund
            return;
        }

        MCEntityCitizen citizen = (MCEntityCitizen) entityType.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (citizen == null) {
            sendResult(player, false, "Failed to create citizen!");
            eco.addWallet(player.getUUID(), cost); // refund
            return;
        }

        // Transfer position from villager, set owner, name, job
        citizen.setPos(pos.getX() + 0.5, (double) pos.getY(), pos.getZ() + 0.5);
        citizen.setYRot(yRot);
        citizen.setXRot(0.0f);
        citizen.setOwnerUUID(player.getUUID());

        // Randomize gender and appearance
        boolean isFemale = level.random.nextBoolean();
        citizen.setFemale(isFemale);
        citizen.setTextureId(level.random.nextInt(3));
        String[] suffixes = {"_a", "_b", "_d", "_w"};
        citizen.setTextureSuffix(suffixes[level.random.nextInt(suffixes.length)]);

        // Generate gender-appropriate name if none provided
        if (citizenName.equals("Citizen")) {
            if (villager.hasCustomName()) {
                citizenName = villager.getCustomName().getString();
            } else {
                var nameListener = com.ultra.megamod.feature.citizen.data.listener.CitizenNameListener.INSTANCE;
                citizenName = nameListener.getRandomFullName("default", isFemale);
            }
        }
        citizen.setCitizenName(citizenName);
        citizen.setCitizenJob(job);
        citizen.setHunger(CitizenConfig.MAX_HUNGER);

        // Add to world and remove villager
        level.addFreshEntity(citizen);
        villager.discard();

        // Register in citizen manager (linked to player and faction)
        cm.registerCitizen(citizen.getUUID(), player.getUUID(), factionId, job, citizen.getCitizenName(), level.getServer().getTickCount());

        // Discovery hook
        CitizenEvents.onCitizenHired(level, player, job);

        eco.addAuditEntry(player.getGameProfile().name(), "hire_citizen", cost,
            "Hired " + job.getDisplayName() + " '" + citizenName + "'");
        sendResult(player, true, "Hired " + job.getDisplayName() + " for " + cost + " MC!");
    }

    private static void handleDismiss(ServerPlayer player, int entityId, ServerLevel level, EconomyManager eco) {
        Entity entity = level.getEntity(entityId);
        if (!(entity instanceof MCEntityCitizen citizen)) return;
        if (!citizen.getOwnerUUID().equals(player.getUUID())) return;

        String name = citizen.getCitizenName();
        String jobName = citizen.getCitizenJob().getDisplayName();
        CitizenManager.get(level).unregisterCitizen(citizen.getUUID());
        citizen.discard();
        sendChat(player, "\u00A7c\u00A7l\u2716 \u00A76" + name + " \u00A77(" + jobName + ") \u00A7chas been dismissed.");
    }

    private static void handleSetPosition(ServerPlayer player, int entityId, String type, String jsonData, ServerLevel level) {
        Entity entity = level.getEntity(entityId);
        if (!(entity instanceof MCEntityCitizen citizen)) return;

        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        BlockPos pos = new BlockPos(json.get("x").getAsInt(), json.get("y").getAsInt(), json.get("z").getAsInt());

        String citizenName = citizen.getCitizenName();
        String jobName = citizen.getCitizenJobHandler().getColonyJob() != null
                ? citizen.getCitizenJobHandler().getColonyJob().getDisplayName() : "Citizen";

        switch (type) {
            case "bed" -> {
                // TODO: set bed via MCEntityCitizen sleep handler
                sendChat(player, "\u00A7a\u00A7l\u2714 \u00A76" + citizenName + " \u00A77(" + jobName + ") \u00A7abed set to \u00A7f(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")");
            }
            case "chest" -> {
                // TODO: set chest via MCEntityCitizen handler
                sendChat(player, "\u00A7a\u00A7l\u2714 \u00A76" + citizenName + " \u00A77(" + jobName + ") \u00A7achest set to \u00A7f(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")");
            }
            case "work" -> {
                // TODO: set work area via MCEntityCitizen job handler
                sendChat(player, "\u00A7a\u00A7l\u2714 \u00A76" + citizenName + " \u00A77(" + jobName + ") \u00A7awork area set to \u00A7f(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")");
            }
        }
    }

    private static void handleSetStatus(ServerPlayer player, int entityId, CitizenStatus status, ServerLevel level) {
        Entity entity = level.getEntity(entityId);
        if (entity instanceof MCEntityCitizen citizen) {
            String name = citizen.getCitizenName();
            // TODO: set status via MCEntityCitizen job handler
            String statusMsg = switch (status) {
                case FOLLOW -> "\u00A7b\u00A7l\u2714 \u00A76" + name + " \u00A7bis now following you.";
                case HOLD_POSITION -> "\u00A7e\u00A7l\u2714 \u00A76" + name + " \u00A7eis holding position.";
                case WORK -> "\u00A7a\u00A7l\u2714 \u00A76" + name + " \u00A7ais heading to work.";
                default -> "\u00A77\u00A76" + name + " \u00A77status changed.";
            };
            sendChat(player, statusMsg);
        }
    }

    private static void handleSetGroup(ServerPlayer player, int entityId, String jsonData, ServerLevel level) {
        Entity entity = level.getEntity(entityId);
        if (!(entity instanceof MCEntityCitizen citizen)) return;

        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String groupIdStr = json.get("groupId").getAsString();
        // TODO: group assignment via MCEntityCitizen handler
        sendChat(player, "\u00A7a\u00A7l\u2714 \u00A76" + citizen.getCitizenName() + " \u00A7agroup updated.");
    }

    private static void handleSetAggroState(ServerPlayer player, int entityId, String jsonData, ServerLevel level) {
        Entity entity = level.getEntity(entityId);
        if (!(entity instanceof MCEntityCitizen citizen)) return;

        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        CitizenStatus state = CitizenStatus.fromString(json.get("state").getAsString());
        // TODO: aggro state via MCEntityCitizen handler
        sendChat(player, "\u00A7e\u00A76" + citizen.getCitizenName() + " \u00A7eaggro set to \u00A7f" + state.name().toLowerCase() + "\u00A7e.");
    }

    private static void handleRename(ServerPlayer player, int entityId, String jsonData, ServerLevel level) {
        Entity entity = level.getEntity(entityId);
        if (!(entity instanceof MCEntityCitizen citizen)) return;
        if (!citizen.getOwnerUUID().equals(player.getUUID())) return;

        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String oldName = citizen.getCitizenName();
        String name = json.get("name").getAsString();
        if (name.length() > 32) name = name.substring(0, 32);
        citizen.setCitizenName(name);
        sendChat(player, "\u00A7a\u00A7l\u2714 \u00A77Citizen renamed from \u00A76" + oldName + " \u00A77to \u00A76" + name + "\u00A77.");
    }

    private static void handleOpenInventory(ServerPlayer player, int entityId, ServerLevel level) {
        // TODO: open MCEntityCitizen inventory via handler
        Entity entity = level.getEntity(entityId);
        if (entity instanceof MCEntityCitizen citizen) {
            sendChat(player, "\u00A7eCitizen inventory system being ported to MCEntityCitizen.");
        }
    }

    private static void handleSetUpkeepChest(ServerPlayer player, int entityId, String jsonData, ServerLevel level) {
        Entity entity = level.getEntity(entityId);
        if (!(entity instanceof MCEntityCitizen citizen)) return;

        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        BlockPos pos = new BlockPos(json.get("x").getAsInt(), json.get("y").getAsInt(), json.get("z").getAsInt());

        String name = citizen.getCitizenName();
        // TODO: set upkeep chest via MCEntityCitizen handler
        sendChat(player, "\u00A7a\u00A7l\u2714 \u00A76" + name + " \u00A7aupkeep chest set to \u00A7f(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")");
    }

    private static void handleSetTownChest(ServerPlayer player, int entityId, String jsonData, ServerLevel level) {
        Entity entity = level.getEntity(entityId);
        if (!(entity instanceof MCEntityCitizen citizen)) return;

        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        BlockPos pos = new BlockPos(json.get("x").getAsInt(), json.get("y").getAsInt(), json.get("z").getAsInt());

        String name = citizen.getCitizenName();
        // TODO: set town chest via MCEntityCitizen handler
        sendChat(player, "\u00A7a\u00A7l\u2714 \u00A76" + name + " \u00A7aTown Chest set to \u00A7f(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")");
    }

    private static void handleSetWorkerSetting(ServerPlayer player, int entityId, String jsonData, ServerLevel level) {
        Entity entity = level.getEntity(entityId);
        if (!(entity instanceof MCEntityCitizen citizen)) return;

        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String setting = json.get("setting").getAsString();
        int value = json.get("value").getAsInt();
        String name = citizen.getCitizenName();

        // TODO: worker settings via MCEntityCitizen job handler
        sendChat(player, "\u00A7a\u00A7l\u2714 \u00A76" + name + " \u00A7asetting '" + setting + "' updated.");
    }

    private static void handleHireUnowned(ServerPlayer player, int entityId, String jsonData, ServerLevel level, EconomyManager eco) {
        Entity entity = level.getEntity(entityId);
        if (!(entity instanceof MCEntityCitizen citizen)) return;

        // Require faction membership
        FactionManager fm = FactionManager.get(level);
        String factionId = fm.getPlayerFaction(player.getUUID());
        if (factionId == null) {
            sendResult(player, false, "You must be in a faction to hire citizens! Place a Town Chest to create one.");
            return;
        }

        // Must be unowned
        if (citizen.getOwnerUUID() != null) {
            sendResult(player, false, "This citizen already has an owner!");
            return;
        }

        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String citizenName = json.has("name") ? json.get("name").getAsString() : citizen.getCitizenName();
        // Allow job override from hire screen selection
        CitizenJob requestedJob = json.has("job") ? CitizenJob.fromString(json.get("job").getAsString()) : null;
        CitizenJob currentJob = citizen.getCitizenJob();
        boolean needsJobChange = requestedJob != null && requestedJob != currentJob;
        CitizenJob finalJob = needsJobChange ? requestedJob : currentJob;

        // Check max citizens (arcane tree summoner branch adds bonus capacity)
        CitizenManager cm = CitizenManager.get(level);
        int maxCitizens = CitizenConfig.MAX_CITIZENS_PER_PLAYER
                + 0 /* TODO: Reconnect with Pufferfish Skills API (was CitizenSkillBonuses.getMaxCitizenBonus) */;
        if (cm.getCitizenCount(player.getUUID()) >= maxCitizens) {
            sendResult(player, false, "Maximum citizens reached!");
            return;
        }

        // Check cost (half price for hiring existing unowned, arcane tree reduces further)
        int baseCost = CitizenConfig.getHireCost(finalJob) / 2;
        double hireMult = 1.0 /* TODO: Reconnect with Pufferfish Skills API (was CitizenSkillBonuses.getHireCostMultiplier) */;
        int cost = Math.max(1, (int) (baseCost * hireMult));
        if (cost > 0 && !eco.spendWallet(player.getUUID(), cost)) {
            sendResult(player, false, "Not enough MegaCoins! Need " + cost + " MC.");
            return;
        }

        // If the requested job is different, we need to replace the entity with the correct type
        if (needsJobChange) {
            net.minecraft.world.entity.EntityType<?> entityType = CitizenRegistry.getEntityTypeForJob(finalJob);
            if (entityType == null) {
                eco.addWallet(player.getUUID(), cost); // refund
                sendResult(player, false, "Unknown citizen type!");
                return;
            }
            MCEntityCitizen newCitizen = (MCEntityCitizen) entityType.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            if (newCitizen == null) {
                eco.addWallet(player.getUUID(), cost); // refund
                sendResult(player, false, "Failed to create citizen!");
                return;
            }
            BlockPos pos = citizen.blockPosition();
            newCitizen.setPos(pos.getX() + 0.5, (double) pos.getY(), pos.getZ() + 0.5);
            newCitizen.setYRot(citizen.getYRot());
            newCitizen.setXRot(0.0f);
            newCitizen.setOwnerUUID(player.getUUID());
            // Preserve original citizen's appearance when changing jobs
            newCitizen.setFemale(citizen.isFemale());
            newCitizen.setTextureId(citizen.getTextureId());
            newCitizen.setTextureSuffix(citizen.getTextureSuffix());
            newCitizen.setCitizenName(citizenName.equals("Citizen") ? citizen.getCitizenName() : citizenName);
            newCitizen.setCitizenJob(finalJob);
            newCitizen.setHunger(CitizenConfig.MAX_HUNGER);
            level.addFreshEntity(newCitizen);
            citizen.discard();
            cm.registerCitizen(newCitizen.getUUID(), player.getUUID(), factionId, finalJob, newCitizen.getCitizenName(), level.getServer().getTickCount());
            CitizenEvents.onCitizenHired(level, player, finalJob);
        } else {
            // Same job - just set owner on existing entity
            citizen.setOwnerUUID(player.getUUID());
            if (!citizenName.equals("Citizen") && !citizenName.equals(citizen.getCitizenName())) {
                citizen.setCitizenName(citizenName);
            }
            cm.registerCitizen(citizen.getUUID(), player.getUUID(), factionId, finalJob, citizen.getCitizenName(), level.getServer().getTickCount());
            CitizenEvents.onCitizenHired(level, player, finalJob);
        }

        eco.addAuditEntry(player.getGameProfile().name(), "hire_unowned_citizen", cost,
            "Hired unowned " + finalJob.getDisplayName() + " '" + citizenName + "'");
        sendResult(player, true, "Hired " + finalJob.getDisplayName() + " for " + cost + " MC!");
    }

    // ---- Herald Quest Handling (Herald entity removed; quest board still works) ----

    private static void handleHeraldGetQuests(ServerPlayer player, int entityId, ServerLevel level) {
        // Herald entity removed - only Quest Board (entityId==-1) is supported now
        if (entityId != -1) return;

        long tick = level.getServer().getTickCount();
        var boardQuests = com.ultra.megamod.feature.furniture.QuestBoardManager.getQuests(tick);

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < boardQuests.size(); i++) {
            var bq = boardQuests.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"id\":\"").append(bq.id()).append("\"");
            sb.append(",\"title\":\"").append(bq.title()).append("\"");
            sb.append(",\"difficulty\":\"").append(bq.difficulty()).append("\"");
            sb.append(",\"dungeonName\":\"").append(bq.dungeonName()).append("\"");
            sb.append(",\"coinReward\":").append(bq.coinReward());
            sb.append(",\"keyItemId\":\"").append(bq.keyItemId()).append("\"");
            sb.append(",\"diffLevel\":").append(bq.difficultyLevel());
            sb.append("}");
        }
        sb.append("]");
        PacketDistributor.sendToPlayer(player, (CustomPacketPayload) new CitizenDataPayload("herald_quests", sb.toString()));
    }

    private static void handleHeraldAcceptQuest(ServerPlayer player, int entityId, String jsonData, ServerLevel level, EconomyManager eco) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String questId = json.get("questId").getAsString();

        // Herald entity removed - only Quest Board (entityId==-1) is supported now
        if (entityId != -1) return;

        long tick = level.getServer().getTickCount();
        var boardQuests = com.ultra.megamod.feature.furniture.QuestBoardManager.getQuests(tick);

        com.ultra.megamod.feature.furniture.QuestData quest = null;
        for (var bq : boardQuests) {
            if (bq.id().equals(questId)) {
                quest = bq;
                break;
            }
        }

        if (quest == null) {
            sendChat(player, "\u00A7cQuest no longer available.");
            return;
        }

        // Give dungeon key item
        net.minecraft.world.item.Item keyItem = switch (quest.keyItemId()) {
            case "dungeon_key_normal" -> com.ultra.megamod.feature.dungeons.DungeonRegistry.DUNGEON_KEY_NORMAL.get();
            case "dungeon_key_hard" -> com.ultra.megamod.feature.dungeons.DungeonRegistry.DUNGEON_KEY_HARD.get();
            case "dungeon_key_nightmare" -> com.ultra.megamod.feature.dungeons.DungeonRegistry.DUNGEON_KEY_NIGHTMARE.get();
            case "dungeon_key_infernal" -> com.ultra.megamod.feature.dungeons.DungeonRegistry.DUNGEON_KEY_INFERNAL.get();
            default -> com.ultra.megamod.feature.dungeons.DungeonRegistry.DUNGEON_KEY_NORMAL.get();
        };

        net.minecraft.world.item.ItemStack keyStack = new net.minecraft.world.item.ItemStack(keyItem);
        if (!player.getInventory().add(keyStack)) {
            player.spawnAtLocation(level, keyStack);
        }

        com.ultra.megamod.feature.furniture.QuestBoardManager.removeQuest(questId);

        sendChat(player, "\u00A7a\u00A7l\u2605 \u00A7eQuest accepted! \u00A77Received \u00A7b" + quest.difficulty()
            + " Dungeon Key\u00A77. Complete the dungeon to earn \u00A7a" + quest.coinReward() + " MC\u00A77!");

        handleHeraldGetQuests(player, entityId, level);
    }

    private static void handleMerchantShopRequest(ServerPlayer player, int entityId, ServerLevel level) {
        // TODO: merchant shop via MCEntityCitizen job handler
        sendChat(player, "\u00A7eMerchant shop being ported to MCEntityCitizen.");
    }

    private static void handleMerchantBuy(ServerPlayer player, int entityId, String jsonData, ServerLevel level, EconomyManager eco) {
        // TODO: merchant buy via MCEntityCitizen job handler
        sendChat(player, "\u00A7eMerchant buy being ported to MCEntityCitizen.");
    }

    private static void sendResult(ServerPlayer player, boolean success, String message) {
        String json = "{\"success\":" + success + ",\"message\":\"" + message.replace("\"", "\\\"") + "\"}";
        PacketDistributor.sendToPlayer(player, (CustomPacketPayload) new CitizenDataPayload("citizen_result", json));
    }

    private static void sendChat(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal(message));
    }

    private static void handleViewWorkArea(ServerPlayer player, int entityId, ServerLevel level) {
        Entity entity = level.getEntity(entityId);
        if (!(entity instanceof MCEntityCitizen citizen)) return;

        // TODO: work area view via MCEntityCitizen handler
        sendChat(player, "\u00A7eWork area view being ported to MCEntityCitizen.");
    }

    /**
     * Sends particle effects along the border of the citizen's work area.
     */
    private static void showWorkAreaBorder(ServerPlayer player, MCEntityCitizen citizen, BlockPos center, ServerLevel level) {
        int radius = 8; // Default radius for MCEntityCitizen

        double y = center.getY() + 1.0;
        int minX = center.getX() - radius;
        int maxX = center.getX() + radius;
        int minZ = center.getZ() - radius;
        int maxZ = center.getZ() + radius;

        // Draw 4 edges with particles (every 2 blocks for performance)
        for (int x = minX; x <= maxX; x += 2) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                x + 0.5, y, minZ + 0.5, 1, 0, 0.2, 0, 0);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                x + 0.5, y, maxZ + 0.5, 1, 0, 0.2, 0, 0);
        }
        for (int z = minZ; z <= maxZ; z += 2) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                minX + 0.5, y, z + 0.5, 1, 0, 0.2, 0, 0);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                maxX + 0.5, y, z + 0.5, 1, 0, 0.2, 0, 0);
        }

        // Corner pillars (4 blocks tall for visibility)
        for (int dy = 0; dy < 4; dy++) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.COMPOSTER,
                minX + 0.5, y + dy, minZ + 0.5, 2, 0.1, 0.1, 0.1, 0);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.COMPOSTER,
                maxX + 0.5, y + dy, minZ + 0.5, 2, 0.1, 0.1, 0.1, 0);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.COMPOSTER,
                minX + 0.5, y + dy, maxZ + 0.5, 2, 0.1, 0.1, 0.1, 0);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.COMPOSTER,
                maxX + 0.5, y + dy, maxZ + 0.5, 2, 0.1, 0.1, 0.1, 0);
        }
    }

    // ---- Pen Handlers (removed during MCEntityCitizen transition) ----

    private static void handleBuildPenPreview(ServerPlayer player, int entityId, ServerLevel level) {
        // TODO: pen system via MCEntityCitizen handler
        sendChat(player, "\u00A7ePen system being ported to MCEntityCitizen.");
    }

    private static void handleBuildPenConfirm(ServerPlayer player, int entityId, ServerLevel level) {
        // TODO: pen system via MCEntityCitizen handler
    }

    private static void handleRemovePen(ServerPlayer player, int entityId, ServerLevel level) {
        // TODO: pen system via MCEntityCitizen handler
    }

    private static void handleRebuildPen(ServerPlayer player, int entityId, String jsonData, ServerLevel level) {
        // TODO: pen system via MCEntityCitizen handler
    }

    /** Find terrain Y at a given XZ by searching down/up from baseY. */
    private static double getTerrainY(ServerLevel level, int x, int z, int baseY) {
        for (int dy = 0; dy >= -3; dy--) {
            BlockPos below = new BlockPos(x, baseY + dy - 1, z);
            if (level.getBlockState(below).isSolid()) return baseY + dy;
        }
        for (int dy = 1; dy <= 3; dy++) {
            BlockPos below = new BlockPos(x, baseY + dy - 1, z);
            if (level.getBlockState(below).isSolid()) return baseY + dy;
        }
        return baseY;
    }

    private static void handleCancelBuild(ServerPlayer player, int entityId, ServerLevel level) {
        // TODO: cancel build via MCEntityCitizen job handler
        Entity entity = level.getEntity(entityId);
        if (entity instanceof MCEntityCitizen citizen) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "\u00A7e" + citizen.getCitizenName() + "'s build order has been cancelled."));
        }
    }

    // ==================== Quest Dialogue Handlers ====================

    /**
     * Handles a quest dialogue response from the player.
     * JSON data format: {"questId":"...", "answerIndex":0}
     */
    private static void handleQuestDialogueResponse(ServerPlayer player, int entityId,
                                                     String jsonData, ServerLevel level) {
        try {
            JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
            String questId = json.get("questId").getAsString();
            int answerIndex = json.get("answerIndex").getAsInt();

            com.ultra.megamod.feature.citizen.quest.ColonyQuestManager questManager =
                    com.ultra.megamod.feature.citizen.quest.ColonyQuestManager.get(level);

            // Get current dialogue element for this quest
            Entity entity = level.getEntity(entityId);
            if (entity == null) return;

            var dialogueInfo = questManager.getDialogueForCitizen(player, entity.getUUID());
            if (dialogueInfo == null) return;

            // Process the response
            var nextElement = questManager.handleDialogueResponse(
                    level, player, questId, answerIndex, dialogueInfo.dialogueElement());

            // Send the result back to the client
            if (nextElement != null) {
                // More dialogue to show - send updated dialogue data
                JsonObject response = new JsonObject();
                response.addProperty("action", "quest_dialogue_update");
                response.addProperty("questId", questId);
                response.addProperty("questName", dialogueInfo.questName());

                // Resolve participant names in text
                String resolvedText = com.ultra.megamod.feature.citizen.quest.DialogueTree
                        .resolveParticipants(nextElement.getText(), dialogueInfo.participantNames());
                response.addProperty("text", resolvedText);

                com.google.gson.JsonArray options = new com.google.gson.JsonArray();
                for (var opt : nextElement.getOptions()) {
                    String resolvedAnswer = com.ultra.megamod.feature.citizen.quest.DialogueTree
                            .resolveParticipants(opt.text(), dialogueInfo.participantNames());
                    options.add(resolvedAnswer);
                }
                response.add("options", options);

                PacketDistributor.sendToPlayer(player,
                        (CustomPacketPayload) new CitizenSyncPayload("quest_dialogue", "", response.toString()));
            } else {
                // Dialogue complete
                JsonObject response = new JsonObject();
                response.addProperty("action", "quest_dialogue_close");
                response.addProperty("questId", questId);
                PacketDistributor.sendToPlayer(player,
                        (CustomPacketPayload) new CitizenSyncPayload("quest_dialogue", "", response.toString()));
            }
        } catch (Exception e) {
            com.ultra.megamod.MegaMod.LOGGER.error("Error handling quest dialogue response", e);
        }
    }

    /**
     * Handles a request for quest dialogue from a citizen.
     * Checks if the citizen has quest dialogue to deliver.
     */
    private static void handleQuestGetDialogue(ServerPlayer player, int entityId, ServerLevel level) {
        Entity entity = level.getEntity(entityId);
        if (entity == null) return;

        com.ultra.megamod.feature.citizen.quest.ColonyQuestManager questManager =
                com.ultra.megamod.feature.citizen.quest.ColonyQuestManager.get(level);

        var dialogueInfo = questManager.getDialogueForCitizen(player, entity.getUUID());
        if (dialogueInfo == null) {
            // No quest dialogue for this citizen
            JsonObject response = new JsonObject();
            response.addProperty("action", "quest_dialogue_none");
            PacketDistributor.sendToPlayer(player,
                    (CustomPacketPayload) new CitizenSyncPayload("quest_dialogue", "", response.toString()));
            return;
        }

        // Send dialogue to client
        JsonObject response = new JsonObject();
        response.addProperty("action", "quest_dialogue_start");
        response.addProperty("questId", dialogueInfo.questId());
        response.addProperty("questName", dialogueInfo.questName());
        response.addProperty("objectiveIndex", dialogueInfo.objectiveIndex());
        response.addProperty("isInProgress", dialogueInfo.isInProgress());
        response.addProperty("citizenId", entity.getUUID().toString());

        // Resolve participant names in text
        String resolvedText = com.ultra.megamod.feature.citizen.quest.DialogueTree
                .resolveParticipants(dialogueInfo.dialogueElement().getText(),
                        dialogueInfo.participantNames());
        response.addProperty("text", resolvedText);

        com.google.gson.JsonArray options = new com.google.gson.JsonArray();
        for (var opt : dialogueInfo.dialogueElement().getOptions()) {
            String resolvedAnswer = com.ultra.megamod.feature.citizen.quest.DialogueTree
                    .resolveParticipants(opt.text(), dialogueInfo.participantNames());
            options.add(resolvedAnswer);
        }
        response.add("options", options);

        PacketDistributor.sendToPlayer(player,
                (CustomPacketPayload) new CitizenSyncPayload("quest_dialogue", "", response.toString()));
    }
}
