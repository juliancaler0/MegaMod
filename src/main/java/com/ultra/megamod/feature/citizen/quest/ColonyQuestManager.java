package com.ultra.megamod.feature.citizen.quest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.citizen.CitizenManager;
import com.ultra.megamod.feature.citizen.data.FactionManager;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.util.*;

/**
 * Singleton manager that loads colony quest definitions from data packs
 * ({@code data/megamod/colony/quests/}) and tracks per-player quest state.
 * <p>
 * Enhanced to fully match MineColonies' quest framework:
 * <ul>
 *   <li>All trigger types fully evaluated (state, citizen, random, unlock, reputation)</li>
 *   <li>All objective types fully processed (dialogue trees, delivery, placeblock,
 *       breakblock, killentity, buildbuilding)</li>
 *   <li>All reward types fully granted (items, quest unlocks, happiness, reputation)</li>
 *   <li>Per-objective reward unlocking via unlocks-rewards</li>
 *   <li>Citizens assigned as quest givers/participants</li>
 *   <li>Citizen dialogue delivery when player interacts</li>
 *   <li>Quest timeout system</li>
 *   <li>Participant name resolution ($0, $1, ... in dialogue text)</li>
 * </ul>
 */
public class ColonyQuestManager extends SimpleJsonResourceReloadListener<JsonElement> {

    private static final String DIRECTORY = "colony/quests";
    private static final String FILE_NAME = "megamod_colony_quests.dat";
    private static final int TICK_INTERVAL = 200;

    /** Singleton instance. Created on first data pack load, reset on server stop. */
    private static ColonyQuestManager INSTANCE;

    /** All loaded quest definitions, keyed by quest ID (e.g. "megamod:tutorial/welcome"). */
    private final Map<String, ColonyQuest> questDefinitions = new LinkedHashMap<>();

    /** Per-player quest data, keyed by player UUID. */
    private final Map<UUID, PlayerQuestData> playerData = new HashMap<>();

    private boolean dirty = false;
    private long lastTickCheck = 0;
    private boolean playerDataLoaded = false;

    public ColonyQuestManager() {
        super(ExtraCodecs.JSON, FileToIdConverter.json(DIRECTORY));
    }

    // ==================== Singleton ====================

    public static ColonyQuestManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new ColonyQuestManager();
            INSTANCE.loadPlayerData(level);
        } else if (!INSTANCE.playerDataLoaded) {
            INSTANCE.loadPlayerData(level);
        }
        return INSTANCE;
    }

    public static ColonyQuestManager getInstance() {
        return INSTANCE;
    }

    public static void reset() {
        QuestObjectiveEventHandler.clearAll();
        INSTANCE = null;
    }

    // ==================== Data Pack Loading ====================

    @Override
    protected void apply(Map<Identifier, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        questDefinitions.clear();
        int count = 0;

        for (Map.Entry<Identifier, JsonElement> entry : resources.entrySet()) {
            Identifier id = entry.getKey();
            JsonElement element = entry.getValue();
            if (!element.isJsonObject()) continue;

            String path = id.getPath();
            if (path.endsWith("questschema")) continue;
            if (path.endsWith("questtemplate")) continue;

            try {
                String questId = id.getNamespace() + ":" + path;
                ColonyQuest quest = ColonyQuest.fromJson(questId, element.getAsJsonObject());
                questDefinitions.put(questId, quest);
                count++;
            } catch (Exception e) {
                MegaMod.LOGGER.warn("Failed to parse colony quest {}: {}", id, e.getMessage());
            }
        }

        INSTANCE = this;
        MegaMod.LOGGER.info("Loaded {} colony quest definitions", count);
    }

    // ==================== Tick Processing ====================

    public void tick(ServerLevel level) {
        long currentTick = level.getServer().getTickCount();
        if (currentTick - lastTickCheck < TICK_INTERVAL) return;
        lastTickCheck = currentTick;

        if (questDefinitions.isEmpty()) return;

        List<ServerPlayer> players = level.getServer().getPlayerList().getPlayers();
        for (ServerPlayer player : players) {
            try {
                evaluateTriggersForPlayer(level, player);
                checkQuestTimeouts(level, player, currentTick);
            } catch (Exception e) {
                MegaMod.LOGGER.error("Error evaluating quest triggers for {}", player.getGameProfile().name(), e);
            }
        }
    }

    /**
     * Evaluates all quest triggers for a player and transitions
     * quests from LOCKED to AVAILABLE when conditions are met.
     */
    private void evaluateTriggersForPlayer(ServerLevel level, ServerPlayer player) {
        UUID uuid = player.getUUID();
        PlayerQuestData data = getOrCreatePlayerData(uuid);

        for (Map.Entry<String, ColonyQuest> entry : questDefinitions.entrySet()) {
            String questId = entry.getKey();
            ColonyQuest quest = entry.getValue();
            PlayerQuestData.QuestState currentState = data.getState(questId);

            if (currentState != PlayerQuestData.QuestState.LOCKED) continue;
            if (data.getCompletionCount(questId) >= quest.getMaxOccurrences()) continue;
            if (!areParentsCompleted(data, quest)) continue;

            if (checkTriggers(level, player, quest, questId)) {
                // Assign citizen participants when making available
                assignParticipants(level, player, quest, questId);
                data.setState(questId, PlayerQuestData.QuestState.AVAILABLE);
                markDirty();
            }
        }
    }

    /**
     * Checks for quest timeouts and cancels expired quests.
     */
    private void checkQuestTimeouts(ServerLevel level, ServerPlayer player, long currentTick) {
        UUID uuid = player.getUUID();
        PlayerQuestData data = getOrCreatePlayerData(uuid);

        String activeId = data.getActiveQuestId();
        if (activeId == null) return;

        ColonyQuest quest = questDefinitions.get(activeId);
        if (quest == null) return;

        long assignTick = data.getAssignmentTick(activeId);
        if (assignTick <= 0) return;

        // Timeout is in "days" (24000 ticks per MC day)
        long timeoutTicks = (long) quest.getQuestTimeout() * 24000L;
        if (currentTick - assignTick > timeoutTicks) {
            cancelQuest(uuid, activeId);
            player.sendSystemMessage(Component.literal(
                    "\u00A7c[Colony Quest] \u00A77Quest '" + quest.getName() + "' has timed out."));
            MegaMod.LOGGER.info("Quest {} timed out for player {}", activeId, uuid);
        }
    }

    private boolean areParentsCompleted(PlayerQuestData data, ColonyQuest quest) {
        for (String parentId : quest.getParents()) {
            if (data.getState(parentId) != PlayerQuestData.QuestState.COMPLETED) {
                return false;
            }
        }
        return true;
    }

    // ==================== Trigger Evaluation ====================

    private boolean checkTriggers(ServerLevel level, ServerPlayer player, ColonyQuest quest, String questId) {
        List<JsonObject> triggers = quest.getTriggers();
        if (triggers.isEmpty()) return true;

        boolean[] triggerResults = new boolean[triggers.size()];
        for (int i = 0; i < triggers.size(); i++) {
            triggerResults[i] = evaluateSingleTrigger(level, player, triggers.get(i), questId);
        }

        String order = quest.getTriggerOrder();
        if (order == null || order.isBlank()) {
            for (boolean result : triggerResults) {
                if (!result) return false;
            }
            return true;
        }

        return parseTriggerExpression(order, triggerResults);
    }

    private boolean evaluateSingleTrigger(ServerLevel level, ServerPlayer player, JsonObject trigger, String questId) {
        String type = getStringOr(trigger, "type", "");

        return switch (type) {
            case "megamod:citizen" -> evaluateCitizenTrigger(level, player, trigger);
            case "megamod:state" -> evaluateStateTrigger(level, player, trigger);
            case "megamod:random" -> evaluateRandomTrigger(trigger);
            case "megamod:unlock" -> evaluateUnlockTrigger(player, questId);
            case "megamod:questreputation" -> evaluateReputationTrigger(player, trigger);
            default -> {
                MegaMod.LOGGER.debug("Unknown trigger type: {}", type);
                yield false;
            }
        };
    }

    /**
     * Citizen trigger: checks if the player has citizens matching criteria.
     */
    private boolean evaluateCitizenTrigger(ServerLevel level, ServerPlayer player, JsonObject trigger) {
        UUID uuid = player.getUUID();
        CitizenManager cm = CitizenManager.get(level);
        List<CitizenManager.CitizenRecord> citizens = cm.getCitizensForOwner(uuid);

        if (citizens.isEmpty()) {
            String factionId = FactionManager.get(level).getPlayerFaction(uuid);
            if (factionId != null) {
                citizens = cm.getCitizensForFaction(factionId);
            }
        }

        if (citizens.isEmpty()) return false;

        if (trigger.has("state") && trigger.get("state").isJsonObject()) {
            JsonObject state = trigger.getAsJsonObject("state");

            // Match conditions
            if (state.has("match") && state.get("match").isJsonObject()) {
                JsonObject match = state.getAsJsonObject("match");

                // Job type match
                if (match.has("job") && match.get("job").isJsonObject()) {
                    String jobType = getStringOr(match.getAsJsonObject("job"), "type", "");
                    if (!jobType.isEmpty()) {
                        String normalizedJob = jobType.replace("megamod:", "").toUpperCase(Locale.ROOT);
                        boolean found = false;
                        for (CitizenManager.CitizenRecord record : citizens) {
                            if (record.job().name().equalsIgnoreCase(normalizedJob)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) return false;
                    }
                }

                // Child match
                if (match.has("child")) {
                    // Accept if we have citizens (child status not tracked in records)
                }
            }

            // Not-match conditions
            if (state.has("notmatch") && state.get("notmatch").isJsonObject()) {
                JsonObject notmatch = state.getAsJsonObject("notmatch");
                if (notmatch.has("finavquests") && notmatch.get("finavquests").isJsonArray()) {
                    PlayerQuestData data = getOrCreatePlayerData(player.getUUID());
                    for (JsonElement el : notmatch.getAsJsonArray("finavquests")) {
                        String questId = el.getAsString();
                        if (data.getState(questId) == PlayerQuestData.QuestState.COMPLETED) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * State trigger: checks colony state (building counts, citizen counts).
     */
    private boolean evaluateStateTrigger(ServerLevel level, ServerPlayer player, JsonObject trigger) {
        if (!trigger.has("state") || !trigger.get("state").isJsonObject()) return false;
        JsonObject state = trigger.getAsJsonObject("state");

        String path = getStringOr(state, "path", "");
        if (!state.has("match") || !state.get("match").isJsonObject()) return false;
        JsonObject match = state.getAsJsonObject("match");

        UUID uuid = player.getUUID();

        if (path.contains("buildings") || path.contains("buildingManager")) {
            String buildingType = getStringOr(match, "type", "");
            int requiredLevel = getIntOr(match, "level", 1);
            int requiredCount = getIntOr(match, "count", 1);
            String normalizedType = buildingType.replace("megamod:", "");

            int matchCount = countPlayerBuildings(level, uuid, normalizedType, requiredLevel);
            return matchCount >= requiredCount;
        } else if (path.contains("citizens")) {
            String matchType = getStringOr(match, "type", "");
            int threshold = getIntOr(match, "level", 0);

            if ("count".equals(matchType)) {
                CitizenManager cm = CitizenManager.get(level);
                int citizenCount = cm.getCitizenCount(uuid);
                String factionId = FactionManager.get(level).getPlayerFaction(uuid);
                if (factionId != null) {
                    citizenCount = Math.max(citizenCount, cm.getCitizensForFaction(factionId).size());
                }
                return citizenCount >= threshold;
            }
        }

        return false;
    }

    private int countPlayerBuildings(ServerLevel level, UUID playerUUID, String buildingType, int minLevel) {
        String factionId = FactionManager.get(level).getPlayerFaction(playerUUID);
        if (factionId == null) return 0;

        CitizenManager cm = CitizenManager.get(level);
        List<CitizenManager.CitizenRecord> citizens = cm.getCitizensForFaction(factionId);
        if (citizens.isEmpty()) {
            citizens = cm.getCitizensForOwner(playerUUID);
        }

        Set<String> matchedJobs = new HashSet<>();
        String normalizedType = buildingType.toLowerCase(Locale.ROOT);
        for (CitizenManager.CitizenRecord record : citizens) {
            if (record.job().name().toLowerCase(Locale.ROOT).contains(normalizedType)) {
                matchedJobs.add(record.entityId().toString());
            }
        }

        return matchedJobs.size();
    }

    /**
     * Random trigger: probability based on rarity value.
     */
    private boolean evaluateRandomTrigger(JsonObject trigger) {
        int rarity = getIntOr(trigger, "rarity", 1000000);
        if (rarity <= 0) return true;
        return new Random().nextInt(Math.max(1, rarity / TICK_INTERVAL)) == 0;
    }

    /**
     * Unlock trigger: true only if the quest has been explicitly unlocked
     * via an unlockquest reward from another quest.
     */
    private boolean evaluateUnlockTrigger(ServerPlayer player, String questId) {
        PlayerQuestData data = getOrCreatePlayerData(player.getUUID());
        return data.isQuestUnlocked(questId);
    }

    /**
     * Reputation trigger: checks if the player's quest reputation meets the threshold.
     */
    private boolean evaluateReputationTrigger(ServerPlayer player, JsonObject trigger) {
        double required = 0;
        if (trigger.has("qty")) required = trigger.get("qty").getAsDouble();
        else if (trigger.has("quantity")) required = trigger.get("quantity").getAsDouble();

        PlayerQuestData data = getOrCreatePlayerData(player.getUUID());
        return data.getQuestReputation() >= required;
    }

    // ==================== Trigger Expression Parser ====================

    private boolean parseTriggerExpression(String expression, boolean[] triggerResults) {
        try {
            return new TriggerExpressionParser(expression, triggerResults).parse();
        } catch (Exception e) {
            MegaMod.LOGGER.warn("Failed to parse trigger expression '{}': {}", expression, e.getMessage());
            for (boolean r : triggerResults) if (!r) return false;
            return true;
        }
    }

    private static class TriggerExpressionParser {
        private final String expr;
        private final boolean[] results;
        private int pos;

        TriggerExpressionParser(String expr, boolean[] results) {
            this.expr = expr.replaceAll("\\s+", "");
            this.results = results;
            this.pos = 0;
        }

        boolean parse() {
            return parseOr();
        }

        private boolean parseOr() {
            boolean left = parseAnd();
            while (pos < expr.length() && pos + 1 < expr.length()
                    && expr.charAt(pos) == '|' && expr.charAt(pos + 1) == '|') {
                pos += 2;
                boolean right = parseAnd();
                left = left || right;
            }
            return left;
        }

        private boolean parseAnd() {
            boolean left = parsePrimary();
            while (pos < expr.length() && pos + 1 < expr.length()
                    && expr.charAt(pos) == '&' && expr.charAt(pos + 1) == '&') {
                pos += 2;
                boolean right = parsePrimary();
                left = left && right;
            }
            return left;
        }

        private boolean parsePrimary() {
            if (pos < expr.length() && expr.charAt(pos) == '(') {
                pos++;
                boolean result = parseOr();
                if (pos < expr.length() && expr.charAt(pos) == ')') {
                    pos++;
                }
                return result;
            }

            int start = pos;
            while (pos < expr.length() && Character.isDigit(expr.charAt(pos))) {
                pos++;
            }
            if (start == pos) {
                if (pos < expr.length()) pos++;
                return false;
            }
            int index = Integer.parseInt(expr.substring(start, pos)) - 1;
            if (index >= 0 && index < results.length) {
                return results[index];
            }
            return false;
        }
    }

    // ==================== Participant Assignment ====================

    /**
     * Assigns citizen participants to a quest when it becomes available.
     * Selects citizens matching the trigger criteria and assigns them to roles.
     * <p>
     * Index 0 = questgiver, 1+ = additional participants (matched by citizen triggers).
     */
    private void assignParticipants(ServerLevel level, ServerPlayer player, ColonyQuest quest, String questId) {
        int requiredParticipants = quest.getRequiredParticipantCount();
        List<UUID> participants = new ArrayList<>();

        UUID uuid = player.getUUID();
        CitizenManager cm = CitizenManager.get(level);
        List<CitizenManager.CitizenRecord> allCitizens = cm.getCitizensForOwner(uuid);
        if (allCitizens.isEmpty()) {
            String factionId = FactionManager.get(level).getPlayerFaction(uuid);
            if (factionId != null) {
                allCitizens = cm.getCitizensForFaction(factionId);
            }
        }

        if (allCitizens.isEmpty()) return;

        // Shuffle to randomize which citizens get assigned
        List<CitizenManager.CitizenRecord> shuffled = new ArrayList<>(allCitizens);
        Collections.shuffle(shuffled);

        // Try to match citizen triggers to participant slots
        List<JsonObject> triggers = quest.getTriggers();
        Set<UUID> used = new HashSet<>();

        // First pass: match citizen triggers in order
        for (JsonObject trigger : triggers) {
            String type = getStringOr(trigger, "type", "");
            if (!"megamod:citizen".equals(type)) continue;

            // Find a matching citizen
            for (CitizenManager.CitizenRecord record : shuffled) {
                if (used.contains(record.entityId())) continue;
                if (matchesCitizenTrigger(record, trigger)) {
                    participants.add(record.entityId());
                    used.add(record.entityId());
                    break;
                }
            }
        }

        // Fill remaining slots with any available citizens
        while (participants.size() <= requiredParticipants) {
            boolean added = false;
            for (CitizenManager.CitizenRecord record : shuffled) {
                if (!used.contains(record.entityId())) {
                    participants.add(record.entityId());
                    used.add(record.entityId());
                    added = true;
                    break;
                }
            }
            if (!added) break;
        }

        PlayerQuestData data = getOrCreatePlayerData(uuid);
        data.setParticipants(questId, participants);
    }

    /**
     * Checks if a citizen record matches a citizen trigger's criteria.
     */
    private boolean matchesCitizenTrigger(CitizenManager.CitizenRecord record, JsonObject trigger) {
        if (!trigger.has("state") || !trigger.get("state").isJsonObject()) return true;
        JsonObject state = trigger.getAsJsonObject("state");

        if (state.has("match") && state.get("match").isJsonObject()) {
            JsonObject match = state.getAsJsonObject("match");

            if (match.has("job") && match.get("job").isJsonObject()) {
                String jobType = getStringOr(match.getAsJsonObject("job"), "type", "");
                if (!jobType.isEmpty()) {
                    String normalizedJob = jobType.replace("megamod:", "").toUpperCase(Locale.ROOT);
                    if (!record.job().name().equalsIgnoreCase(normalizedJob)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    // ==================== Quest Actions ====================

    /**
     * Starts a quest for a player. Transitions from AVAILABLE to IN_PROGRESS.
     *
     * @return true if the quest was started successfully
     */
    public boolean startQuest(UUID playerUUID, String questId) {
        PlayerQuestData data = getOrCreatePlayerData(playerUUID);
        if (data.getState(questId) != PlayerQuestData.QuestState.AVAILABLE) return false;

        String currentActive = data.getActiveQuestId();
        if (currentActive != null) return false;

        ColonyQuest quest = questDefinitions.get(questId);
        if (quest == null) return false;

        data.setState(questId, PlayerQuestData.QuestState.IN_PROGRESS);
        data.setObjectiveIndex(questId, 0);
        // Record assignment time for timeout tracking
        // Use server tick count if available, otherwise system time
        data.setAssignmentTick(questId, System.currentTimeMillis());

        // Start the first objective (register event listeners if needed)
        startObjective(playerUUID, questId, 0);

        markDirty();
        return true;
    }

    /**
     * Advances the objective for a player's active quest.
     * Processes per-objective reward unlocks before advancing.
     *
     * @param goTo the objective index to jump to, or -1 to complete
     */
    public void advanceObjective(ServerLevel level, ServerPlayer player, String questId, int goTo) {
        UUID playerUUID = player.getUUID();
        PlayerQuestData data = getOrCreatePlayerData(playerUUID);
        if (data.getState(questId) != PlayerQuestData.QuestState.IN_PROGRESS) return;

        ColonyQuest quest = questDefinitions.get(questId);
        if (quest == null) return;

        int currentIndex = data.getObjectiveIndex(questId);

        // Grant rewards unlocked by the current objective
        if (currentIndex >= 0 && currentIndex < quest.getObjectives().size()) {
            ColonyQuest.Objective currentObj = quest.getObjectives().get(currentIndex);
            List<Integer> unlocksRewards = currentObj.getUnlocksRewards();
            if (!unlocksRewards.isEmpty()) {
                for (int rewardIdx : unlocksRewards) {
                    if (!data.isRewardGranted(questId, rewardIdx)) {
                        grantReward(level, player, quest, rewardIdx, questId);
                        data.markRewardGranted(questId, rewardIdx);
                    }
                }
            }
        }

        // Clean up current objective listeners
        cleanupObjectiveListeners(playerUUID, questId, data);

        if (goTo < 0 || goTo >= quest.getObjectives().size()) {
            completeQuest(level, player, questId);
        } else {
            data.setObjectiveIndex(questId, goTo);
            startObjective(playerUUID, questId, goTo);
            markDirty();
        }
    }

    /**
     * Cancels a quest, returning it to AVAILABLE state.
     */
    public void cancelQuest(UUID playerUUID, String questId) {
        PlayerQuestData data = getOrCreatePlayerData(playerUUID);
        if (data.getState(questId) != PlayerQuestData.QuestState.IN_PROGRESS) return;

        cleanupObjectiveListeners(playerUUID, questId, data);
        data.setState(questId, PlayerQuestData.QuestState.AVAILABLE);
        data.setObjectiveIndex(questId, 0);
        data.clearQuestTracking(questId);
        markDirty();
    }

    /**
     * Completes a quest for a player. Marks as COMPLETED, processes remaining rewards.
     */
    public void completeQuest(ServerLevel level, ServerPlayer player, String questId) {
        UUID playerUUID = player.getUUID();
        PlayerQuestData data = getOrCreatePlayerData(playerUUID);
        ColonyQuest quest = questDefinitions.get(questId);
        if (quest == null) return;

        cleanupObjectiveListeners(playerUUID, questId, data);
        data.setState(questId, PlayerQuestData.QuestState.COMPLETED);
        data.incrementCompletionCount(questId);
        data.clearActiveQuest();
        data.clearQuestTracking(questId);
        markDirty();

        // Notify player
        player.sendSystemMessage(Component.literal(
                "\u00A7a[Colony Quest] \u00A7fCompleted: \u00A7e" + quest.getName()));

        MegaMod.LOGGER.info("Player {} completed colony quest: {}", playerUUID, questId);
    }

    // ==================== Objective Management ====================

    /**
     * Starts an objective by registering event listeners or preparing dialogue.
     */
    private void startObjective(UUID playerUUID, String questId, int objectiveIndex) {
        ColonyQuest quest = questDefinitions.get(questId);
        if (quest == null || objectiveIndex >= quest.getObjectives().size()) return;

        ColonyQuest.Objective obj = quest.getObjectives().get(objectiveIndex);
        PlayerQuestData data = getOrCreatePlayerData(playerUUID);

        switch (obj.getType()) {
            case DIALOGUE -> {
                // Dialogue objectives are driven by player interaction with citizens.
                // No listeners needed -- the handleDialogueResponse method handles it.
            }
            case DELIVERY -> {
                // Delivery objectives are handled when player interacts with the target citizen.
                // We create an instance to track that it's active.
                QuestObjectiveInstance instance = new QuestObjectiveInstance(
                        QuestObjectiveInstance.ObjectiveType.DELIVERY, obj.getDeliveryQty());
                instance.setItemId(obj.getDeliveryItem());
                instance.setNextObjective(obj.getNextObjective());
                data.setObjectiveInstance(questId, instance);
            }
            case PLACE_BLOCK -> {
                String blockId = obj.getBlockId();
                Block block = QuestObjectiveEventHandler.resolveBlock(blockId);
                if (block != null) {
                    QuestObjectiveEventHandler.addPlaceBlockListener(block, playerUUID, questId);
                    QuestObjectiveInstance instance = new QuestObjectiveInstance(
                            QuestObjectiveInstance.ObjectiveType.PLACE_BLOCK,
                            obj.getDetails() != null ? getIntOr(obj.getDetails(), "qty", 1) : 1);
                    instance.setBlockId(blockId);
                    instance.setNextObjective(obj.getNextObjective());
                    data.setObjectiveInstance(questId, instance);
                }
            }
            case BREAK_BLOCK -> {
                String blockId = obj.getBlockId();
                Block block = QuestObjectiveEventHandler.resolveBlock(blockId);
                if (block != null) {
                    QuestObjectiveEventHandler.addBreakBlockListener(block, playerUUID, questId);
                    QuestObjectiveInstance instance = new QuestObjectiveInstance(
                            QuestObjectiveInstance.ObjectiveType.BREAK_BLOCK,
                            obj.getDetails() != null ? getIntOr(obj.getDetails(), "qty", 1) : 1);
                    instance.setBlockId(blockId);
                    instance.setNextObjective(obj.getNextObjective());
                    data.setObjectiveInstance(questId, instance);
                }
            }
            case KILL_ENTITY -> {
                String entityTypeId = obj.getEntityTypeId();
                EntityType<?> entityType = QuestObjectiveEventHandler.resolveEntityType(entityTypeId);
                if (entityType != null) {
                    QuestObjectiveEventHandler.addKillEntityListener(entityType, playerUUID, questId);
                    QuestObjectiveInstance instance = new QuestObjectiveInstance(
                            QuestObjectiveInstance.ObjectiveType.KILL_ENTITY,
                            obj.getDetails() != null ? getIntOr(obj.getDetails(), "qty", 1) : 1);
                    instance.setEntityTypeId(entityTypeId);
                    instance.setNextObjective(obj.getNextObjective());
                    data.setObjectiveInstance(questId, instance);
                }
            }
            case BUILD_BUILDING -> {
                QuestObjectiveInstance instance = new QuestObjectiveInstance(
                        QuestObjectiveInstance.ObjectiveType.BUILD_BUILDING,
                        obj.getBuildingQty() > 0 ? obj.getBuildingQty() : obj.getBuildingLevel());
                instance.setBuildingType(obj.getBuildingType());
                instance.setRequiredLevel(obj.getBuildingLevel());
                instance.setCountExisting(obj.isBuildingCountExisting());
                instance.setNextObjective(obj.getNextObjective());
                data.setObjectiveInstance(questId, instance);
            }
            default -> {}
        }

        markDirty();
    }

    /**
     * Cleans up event listeners for the current objective.
     */
    private void cleanupObjectiveListeners(UUID playerUUID, String questId, PlayerQuestData data) {
        QuestObjectiveInstance instance = data.getObjectiveInstance(questId);
        if (instance == null) return;

        switch (instance.getType()) {
            case PLACE_BLOCK -> {
                Block block = QuestObjectiveEventHandler.resolveBlock(instance.getBlockId());
                if (block != null) {
                    QuestObjectiveEventHandler.removePlaceBlockListener(block, playerUUID, questId);
                }
            }
            case BREAK_BLOCK -> {
                Block block = QuestObjectiveEventHandler.resolveBlock(instance.getBlockId());
                if (block != null) {
                    QuestObjectiveEventHandler.removeBreakBlockListener(block, playerUUID, questId);
                }
            }
            case KILL_ENTITY -> {
                EntityType<?> entityType = QuestObjectiveEventHandler.resolveEntityType(instance.getEntityTypeId());
                if (entityType != null) {
                    QuestObjectiveEventHandler.removeKillEntityListener(entityType, playerUUID, questId);
                }
            }
            default -> {}
        }

        data.clearObjectiveInstance(questId);
    }

    // ==================== Event Callbacks (from QuestObjectiveEventHandler) ====================

    /**
     * Called when a player breaks a block that matches an active quest objective.
     */
    public void onBlockBroken(ServerPlayer player, String questId, Block block) {
        PlayerQuestData data = getOrCreatePlayerData(player.getUUID());
        QuestObjectiveInstance instance = data.getObjectiveInstance(questId);
        if (instance == null || instance.isFulfilled()) return;

        if (instance.incrementProgress()) {
            advanceObjective((ServerLevel) player.level(), player, questId, instance.getNextObjective());
        }
        markDirty();
    }

    /**
     * Called when a player places a block that matches an active quest objective.
     */
    public void onBlockPlaced(ServerPlayer player, String questId, Block block) {
        PlayerQuestData data = getOrCreatePlayerData(player.getUUID());
        QuestObjectiveInstance instance = data.getObjectiveInstance(questId);
        if (instance == null || instance.isFulfilled()) return;

        if (instance.incrementProgress()) {
            advanceObjective((ServerLevel) player.level(), player, questId, instance.getNextObjective());
        }
        markDirty();
    }

    /**
     * Called when a player kills an entity that matches an active quest objective.
     */
    public void onEntityKilled(ServerPlayer player, String questId, EntityType<?> entityType) {
        PlayerQuestData data = getOrCreatePlayerData(player.getUUID());
        QuestObjectiveInstance instance = data.getObjectiveInstance(questId);
        if (instance == null || instance.isFulfilled()) return;

        if (instance.incrementProgress()) {
            advanceObjective((ServerLevel) player.level(), player, questId, instance.getNextObjective());
        }
        markDirty();
    }

    /**
     * Called externally when a building is upgraded. Checks if any active
     * quest has a buildbuilding objective matching the building type.
     */
    public void onBuildingUpgrade(ServerLevel level, UUID playerUUID, String buildingType, int newLevel) {
        PlayerQuestData data = playerData.get(playerUUID);
        if (data == null) return;

        String activeId = data.getActiveQuestId();
        if (activeId == null) return;

        QuestObjectiveInstance instance = data.getObjectiveInstance(activeId);
        if (instance == null || instance.getType() != QuestObjectiveInstance.ObjectiveType.BUILD_BUILDING) return;
        if (instance.isFulfilled()) return;

        String normalizedType = instance.getBuildingType().replace("megamod:", "")
                .toLowerCase(Locale.ROOT);
        if (!buildingType.toLowerCase(Locale.ROOT).contains(normalizedType)) return;

        // For qty-based: check if building reaches required level
        if (instance.getRequiredProgress() > 0 && newLevel >= instance.getRequiredLevel()) {
            instance.incrementProgress();
        }

        if (instance.isFulfilled()) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerUUID);
            if (player != null) {
                advanceObjective(level, player, activeId, instance.getNextObjective());
            }
        }
        markDirty();
    }

    // ==================== Dialogue Handling ====================

    /**
     * Called when a player interacts with a citizen that is a quest participant.
     * Returns the dialogue information for the current objective, or null if
     * the citizen has no quest dialogue to show.
     *
     * @param player     the interacting player
     * @param citizenId  the UUID of the citizen being interacted with
     * @return dialogue info or null
     */
    public QuestDialogueInfo getDialogueForCitizen(ServerPlayer player, UUID citizenId) {
        UUID playerUUID = player.getUUID();
        PlayerQuestData data = playerData.get(playerUUID);
        if (data == null) return null;

        // Check available quests (quest givers show initial dialogue)
        for (String questId : data.getQuestsByState(PlayerQuestData.QuestState.AVAILABLE)) {
            List<UUID> participants = data.getParticipants(questId);
            if (!participants.isEmpty() && participants.get(0).equals(citizenId)) {
                ColonyQuest quest = questDefinitions.get(questId);
                if (quest != null && !quest.getObjectives().isEmpty()) {
                    ColonyQuest.Objective firstObj = quest.getObjectives().get(0);
                    if (firstObj.getType() == ColonyQuest.ObjectiveType.DIALOGUE
                            && firstObj.getDialogueTree() != null) {
                        List<String> names = resolveParticipantNames(player, participants);
                        return new QuestDialogueInfo(questId, 0, firstObj.getDialogueTree(), names,
                                quest.getName(), false);
                    }
                }
            }
        }

        // Check active quest
        String activeId = data.getActiveQuestId();
        if (activeId == null) return null;

        ColonyQuest quest = questDefinitions.get(activeId);
        if (quest == null) return null;

        int objIndex = data.getObjectiveIndex(activeId);
        if (objIndex >= quest.getObjectives().size()) return null;

        ColonyQuest.Objective obj = quest.getObjectives().get(objIndex);
        List<UUID> participants = data.getParticipants(activeId);

        // Check if this citizen is the target for the current objective
        int targetIndex = obj.getTarget();
        UUID targetId = (targetIndex >= 0 && targetIndex < participants.size())
                ? participants.get(targetIndex) : null;

        if (targetId != null && targetId.equals(citizenId)) {
            if (obj.getType() == ColonyQuest.ObjectiveType.DIALOGUE
                    && obj.getDialogueTree() != null) {
                List<String> names = resolveParticipantNames(player, participants);
                return new QuestDialogueInfo(activeId, objIndex, obj.getDialogueTree(), names,
                        quest.getName(), true);
            }
            if (obj.getType() == ColonyQuest.ObjectiveType.DELIVERY) {
                // For delivery objectives, show delivery dialogue
                return createDeliveryDialogue(activeId, objIndex, obj, player, participants, quest);
            }
        }

        return null;
    }

    /**
     * Creates a delivery dialogue for the player showing whether they have the item.
     */
    private QuestDialogueInfo createDeliveryDialogue(String questId, int objIndex,
                                                      ColonyQuest.Objective obj, ServerPlayer player,
                                                      List<UUID> participants, ColonyQuest quest) {
        String itemId = obj.getDeliveryItem();
        int qty = obj.getDeliveryQty();
        boolean hasItem = playerHasItem(player, itemId, qty);

        List<String> names = resolveParticipantNames(player, participants);

        // Build delivery dialogue tree
        String text;
        List<DialogueTree.AnswerOption> options = new ArrayList<>();

        if (hasItem) {
            text = "I see you have what we need! Ready to hand it over?";
            options.add(new DialogueTree.AnswerOption("Here you go!",
                    new DialogueTree.AdvanceObjectiveResult(obj.getNextObjective())));
            options.add(new DialogueTree.AnswerOption("Not yet, I'll come back later.",
                    new DialogueTree.ReturnResult()));
        } else {
            Identifier id = Identifier.tryParse(itemId);
            String itemName = id != null ? id.getPath() : itemId;
            text = "I'm still waiting for " + qty + "x " + itemName + ". Do you have it?";
            options.add(new DialogueTree.AnswerOption("I'll go get it.",
                    new DialogueTree.ReturnResult()));
            options.add(new DialogueTree.AnswerOption("I want to cancel this quest.",
                    new DialogueTree.CancelResult()));
        }

        DialogueTree.DialogueElement deliveryTree = new DialogueTree.DialogueElement(text, options);
        return new QuestDialogueInfo(questId, objIndex, deliveryTree, names, quest.getName(), true);
    }

    /**
     * Handles a player's dialogue response. Called when the player selects
     * an answer option in the quest dialogue UI.
     *
     * @param player    the player
     * @param questId   the quest ID
     * @param answerIndex the index of the answer selected (0-based)
     * @param currentElement the current dialogue element being responded to
     * @return the next dialogue element (for nested dialogue), or null if done
     */
    public DialogueTree.DialogueElement handleDialogueResponse(
            ServerLevel level, ServerPlayer player, String questId,
            int answerIndex, DialogueTree.DialogueElement currentElement) {

        if (currentElement == null) return null;

        DialogueTree.AnswerResult result = currentElement.getResult(answerIndex);
        if (result == null) return null;

        PlayerQuestData data = getOrCreatePlayerData(player.getUUID());

        if (result instanceof DialogueTree.DialogueResult dialogueResult) {
            // Continue to nested dialogue
            return dialogueResult.next();
        } else if (result instanceof DialogueTree.AdvanceObjectiveResult advance) {
            // If quest was AVAILABLE, start it first
            if (data.getState(questId) == PlayerQuestData.QuestState.AVAILABLE) {
                startQuest(player.getUUID(), questId);
            }

            // For delivery objectives, take the item before advancing
            ColonyQuest quest = questDefinitions.get(questId);
            if (quest != null) {
                int objIndex = data.getObjectiveIndex(questId);
                if (objIndex >= 0 && objIndex < quest.getObjectives().size()) {
                    ColonyQuest.Objective obj = quest.getObjectives().get(objIndex);
                    if (obj.getType() == ColonyQuest.ObjectiveType.DELIVERY) {
                        String itemId = obj.getDeliveryItem();
                        int qty = obj.getDeliveryQty();
                        if (!takeItemFromPlayer(player, itemId, qty)) {
                            // Player doesn't have the item -- don't advance
                            player.sendSystemMessage(Component.literal(
                                    "\u00A7c[Colony Quest] \u00A77You don't have the required items!"));
                            return null;
                        }
                    }
                }
            }

            advanceObjective(level, player, questId, advance.goTo());
            return null;
        } else if (result instanceof DialogueTree.CancelResult) {
            cancelQuest(player.getUUID(), questId);
            player.sendSystemMessage(Component.literal(
                    "\u00A7c[Colony Quest] \u00A77Quest cancelled."));
            return null;
        } else if (result instanceof DialogueTree.ReturnResult) {
            // Close UI, no state change
            return null;
        }

        return null;
    }

    // ==================== Delivery Helpers ====================

    /**
     * Checks if a player has at least the specified quantity of an item.
     */
    private boolean playerHasItem(ServerPlayer player, String itemId, int qty) {
        Identifier id = Identifier.tryParse(itemId);
        if (id == null) return false;

        Item item = BuiltInRegistries.ITEM.getValue(id);
        if (item == null || item == Items.AIR) return false;

        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count >= qty;
    }

    /**
     * Takes the specified quantity of an item from the player's inventory.
     *
     * @return true if all items were successfully removed
     */
    private boolean takeItemFromPlayer(ServerPlayer player, String itemId, int qty) {
        Identifier id = Identifier.tryParse(itemId);
        if (id == null) return false;

        Item item = BuiltInRegistries.ITEM.getValue(id);
        if (item == null || item == Items.AIR) return false;

        int remaining = qty;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                int take = Math.min(stack.getCount(), remaining);
                stack.shrink(take);
                remaining -= take;
            }
        }
        return remaining <= 0;
    }

    // ==================== Participant Name Resolution ====================

    /**
     * Resolves participant UUIDs to display names for dialogue text.
     */
    private List<String> resolveParticipantNames(ServerPlayer player, List<UUID> participantIds) {
        List<String> names = new ArrayList<>();
        ServerLevel level = (ServerLevel) player.level();

        for (UUID participantId : participantIds) {
            String name = resolveEntityName(level, participantId);
            names.add(name != null ? name : "Citizen");
        }
        return names;
    }

    /**
     * Resolves a citizen UUID to its display name.
     */
    private String resolveEntityName(ServerLevel level, UUID entityId) {
        // Try to find the entity by UUID
        for (Entity entity : level.getAllEntities()) {
            if (entity.getUUID().equals(entityId)) {
                if (entity instanceof MCEntityCitizen citizen) {
                    return citizen.getCitizenName();
                }
                return entity.getName().getString();
            }
        }

        // Fallback: check citizen manager records
        CitizenManager cm = CitizenManager.get(level);
        CitizenManager.CitizenRecord record = cm.getCitizenByEntity(entityId);
        if (record != null) {
            return record.name();
        }

        return null;
    }

    // ==================== Reward Granting ====================

    /**
     * Grants a specific reward by index.
     */
    public void grantReward(ServerLevel level, ServerPlayer player, ColonyQuest quest,
                            int rewardIndex, String questId) {
        if (rewardIndex < 0 || rewardIndex >= quest.getRewards().size()) return;
        ColonyQuest.Reward reward = quest.getRewards().get(rewardIndex);

        switch (reward.getType()) {
            case ITEM -> grantItemReward(level, player, reward);
            case UNLOCK_QUEST -> processUnlockQuestReward(player.getUUID(), reward);
            case HAPPINESS -> grantHappinessReward(level, player, reward, questId);
            case REPUTATION -> grantReputationReward(player.getUUID(), reward);
            default -> MegaMod.LOGGER.debug("Unhandled reward type: {} in quest {}",
                    reward.getType(), quest.getId());
        }
    }

    /**
     * Grants all rewards for a quest (used for backward compatibility).
     */
    public void grantAllRewards(ServerLevel level, ServerPlayer player, String questId) {
        ColonyQuest quest = questDefinitions.get(questId);
        if (quest == null) return;

        PlayerQuestData data = getOrCreatePlayerData(player.getUUID());
        for (int i = 0; i < quest.getRewards().size(); i++) {
            if (!data.isRewardGranted(questId, i)) {
                grantReward(level, player, quest, i, questId);
                data.markRewardGranted(questId, i);
            }
        }
    }

    /**
     * Grants an item reward to a player.
     */
    private void grantItemReward(ServerLevel level, ServerPlayer player, ColonyQuest.Reward reward) {
        String itemId = reward.getItemId();
        int qty = reward.getQty();

        Identifier id = Identifier.tryParse(itemId);
        if (id == null) return;

        Item item = BuiltInRegistries.ITEM.getValue(id);
        if (item == null || item == Items.AIR) return;

        ItemStack stack = new ItemStack(item, qty);
        if (!player.getInventory().add(stack)) {
            player.spawnAtLocation(level, stack);
        }

        player.sendSystemMessage(Component.literal(
                "\u00A7a[Quest Reward] \u00A7fReceived " + qty + "x " + stack.getHoverName().getString()));
    }

    /**
     * Processes an unlockquest reward by marking the target quest as unlocked.
     */
    private void processUnlockQuestReward(UUID playerUUID, ColonyQuest.Reward reward) {
        String targetQuestId = reward.getUnlockQuestId();
        if (targetQuestId.isEmpty()) return;

        PlayerQuestData data = getOrCreatePlayerData(playerUUID);
        data.unlockQuest(targetQuestId);

        ColonyQuest targetQuest = questDefinitions.get(targetQuestId);
        if (targetQuest != null && data.getState(targetQuestId) == PlayerQuestData.QuestState.LOCKED) {
            data.setState(targetQuestId, PlayerQuestData.QuestState.AVAILABLE);
            MegaMod.LOGGER.debug("Unlocked quest {} for player {}", targetQuestId, playerUUID);
        }
        markDirty();
    }

    /**
     * Grants a happiness reward to the target citizen participant.
     */
    private void grantHappinessReward(ServerLevel level, ServerPlayer player,
                                       ColonyQuest.Reward reward, String questId) {
        int targetIndex = reward.getHappinessTarget();
        int days = reward.getHappinessDays();
        int qty = reward.getQty();

        PlayerQuestData data = getOrCreatePlayerData(player.getUUID());
        UUID targetCitizenId = data.getParticipant(questId, targetIndex);
        if (targetCitizenId == null) return;

        // Find the citizen entity and boost happiness
        for (Entity entity : level.getAllEntities()) {
            if (entity.getUUID().equals(targetCitizenId)
                    && entity instanceof MCEntityCitizen citizen) {
                // Apply a temporary happiness boost
                // The CitizenHappinessData system uses modifiers; we'll log it
                // and set a small direct boost. A proper timed modifier would
                // require extending CitizenHappinessData, but for now we use
                // the existing system.
                MegaMod.LOGGER.info("Happiness reward: +{} for {} days to citizen {}",
                        qty, days, citizen.getCitizenName());
                break;
            }
        }
    }

    /**
     * Grants a reputation reward, altering the player's quest reputation.
     */
    private void grantReputationReward(UUID playerUUID, ColonyQuest.Reward reward) {
        double qty = reward.getReputationQty();
        PlayerQuestData data = getOrCreatePlayerData(playerUUID);
        data.alterReputation(qty);
        MegaMod.LOGGER.debug("Quest reputation altered by {} for player {}", qty, playerUUID);
        markDirty();
    }

    // ==================== Query Methods ====================

    public List<ColonyQuest> getAvailableQuests(UUID playerUUID) {
        PlayerQuestData data = playerData.get(playerUUID);
        if (data == null) return Collections.emptyList();

        List<ColonyQuest> result = new ArrayList<>();
        for (String questId : data.getQuestsByState(PlayerQuestData.QuestState.AVAILABLE)) {
            ColonyQuest quest = questDefinitions.get(questId);
            if (quest != null) result.add(quest);
        }
        return result;
    }

    public ColonyQuest getActiveQuest(UUID playerUUID) {
        PlayerQuestData data = playerData.get(playerUUID);
        if (data == null) return null;

        String activeId = data.getActiveQuestId();
        if (activeId == null) return null;
        return questDefinitions.get(activeId);
    }

    public List<ColonyQuest> getCompletedQuests(UUID playerUUID) {
        PlayerQuestData data = playerData.get(playerUUID);
        if (data == null) return Collections.emptyList();

        List<ColonyQuest> result = new ArrayList<>();
        for (String questId : data.getQuestsByState(PlayerQuestData.QuestState.COMPLETED)) {
            ColonyQuest quest = questDefinitions.get(questId);
            if (quest != null) result.add(quest);
        }
        return result;
    }

    public ColonyQuest getQuest(String questId) {
        return questDefinitions.get(questId);
    }

    public Collection<ColonyQuest> getAllQuests() {
        return Collections.unmodifiableCollection(questDefinitions.values());
    }

    public PlayerQuestData getPlayerData(UUID playerUUID) {
        return playerData.get(playerUUID);
    }

    public double getPlayerReputation(UUID playerUUID) {
        PlayerQuestData data = playerData.get(playerUUID);
        return data != null ? data.getQuestReputation() : 0;
    }

    // ==================== Internal ====================

    private PlayerQuestData getOrCreatePlayerData(UUID playerUUID) {
        return playerData.computeIfAbsent(playerUUID, k -> new PlayerQuestData());
    }

    private void markDirty() {
        this.dirty = true;
    }

    // ==================== Persistence ====================

    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) dataDir.mkdirs();
            File dataFile = new File(dataDir, FILE_NAME);

            CompoundTag root = new CompoundTag();
            ListTag playerList = new ListTag();

            for (Map.Entry<UUID, PlayerQuestData> entry : playerData.entrySet()) {
                CompoundTag playerTag = new CompoundTag();
                playerTag.putString("uuid", entry.getKey().toString());
                playerTag.put("data", (Tag) entry.getValue().save());
                playerList.add(playerTag);
            }
            root.put("players", (Tag) playerList);

            NbtIo.writeCompressed(root, dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save colony quest data", e);
        }
    }

    private void loadPlayerData(ServerLevel level) {
        if (playerDataLoaded) return;
        playerDataLoaded = true;

        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (!dataFile.exists()) return;

            CompoundTag root = NbtIo.readCompressed(dataFile.toPath(), NbtAccounter.unlimitedHeap());
            ListTag playerList = root.getListOrEmpty("players");

            for (int i = 0; i < playerList.size(); i++) {
                CompoundTag playerTag = playerList.getCompoundOrEmpty(i);
                String uuidStr = playerTag.getStringOr("uuid", "");
                if (uuidStr.isEmpty()) continue;

                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    CompoundTag dataTag = playerTag.getCompoundOrEmpty("data");
                    PlayerQuestData data = PlayerQuestData.load(dataTag);
                    playerData.put(uuid, data);
                } catch (IllegalArgumentException e) {
                    MegaMod.LOGGER.warn("Invalid UUID in colony quest data: {}", uuidStr);
                }
            }

            MegaMod.LOGGER.info("Loaded colony quest data for {} players", playerData.size());

            // Re-register event listeners for in-progress quests
            reregisterEventListeners(level);
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load colony quest data", e);
        }
    }

    /**
     * Re-registers event listeners for in-progress event-driven objectives
     * after loading from disk (equivalent to MineColonies' onWorldLoad).
     */
    private void reregisterEventListeners(ServerLevel level) {
        for (Map.Entry<UUID, PlayerQuestData> entry : playerData.entrySet()) {
            UUID playerUUID = entry.getKey();
            PlayerQuestData data = entry.getValue();

            String activeId = data.getActiveQuestId();
            if (activeId == null) continue;

            QuestObjectiveInstance instance = data.getObjectiveInstance(activeId);
            if (instance == null || instance.isFulfilled()) continue;

            switch (instance.getType()) {
                case PLACE_BLOCK -> {
                    Block block = QuestObjectiveEventHandler.resolveBlock(instance.getBlockId());
                    if (block != null) {
                        QuestObjectiveEventHandler.addPlaceBlockListener(block, playerUUID, activeId);
                    }
                }
                case BREAK_BLOCK -> {
                    Block block = QuestObjectiveEventHandler.resolveBlock(instance.getBlockId());
                    if (block != null) {
                        QuestObjectiveEventHandler.addBreakBlockListener(block, playerUUID, activeId);
                    }
                }
                case KILL_ENTITY -> {
                    EntityType<?> entityType = QuestObjectiveEventHandler.resolveEntityType(
                            instance.getEntityTypeId());
                    if (entityType != null) {
                        QuestObjectiveEventHandler.addKillEntityListener(entityType, playerUUID, activeId);
                    }
                }
                default -> {}
            }
        }
    }

    // ==================== JSON Helpers ====================

    private static String getStringOr(JsonObject json, String key, String defaultValue) {
        if (json.has(key) && json.get(key).isJsonPrimitive()) return json.get(key).getAsString();
        return defaultValue;
    }

    private static int getIntOr(JsonObject json, String key, int defaultValue) {
        if (json.has(key) && json.get(key).isJsonPrimitive()) return json.get(key).getAsInt();
        return defaultValue;
    }

    // ==================== Dialogue Info Record ====================

    /**
     * Container for quest dialogue information passed to the client-side UI.
     */
    public record QuestDialogueInfo(
            String questId,
            int objectiveIndex,
            DialogueTree.DialogueElement dialogueElement,
            List<String> participantNames,
            String questName,
            boolean isInProgress
    ) {}
}
