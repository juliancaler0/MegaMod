package com.ultra.megamod.feature.citizen.quest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data class representing a loaded colony quest definition.
 * Parsed from JSON files in {@code data/megamod/colony/quests/}.
 * <p>
 * Enhanced to fully match MineColonies' quest format with:
 * <ul>
 *   <li>Parsed dialogue trees (text/options/result) instead of raw JSON</li>
 *   <li>Typed objectives (dialogue, delivery, placeblock, breakblock,
 *       killentity, buildbuilding)</li>
 *   <li>Typed rewards (item, unlockquest, happiness, reputation)</li>
 *   <li>Per-objective reward unlocking via "unlocks-rewards" arrays</li>
 *   <li>Quest timeout in days</li>
 *   <li>Citizen participant targets ($0 = questgiver, $1+ = participants)</li>
 * </ul>
 */
public class ColonyQuest {

    private final String id;
    private final String name;
    private final int maxOccurrences;
    private final int questTimeout;
    private final List<String> parents;
    private final List<JsonObject> triggers;
    private final String triggerOrder;
    private final List<Objective> objectives;
    private final List<Reward> rewards;

    private ColonyQuest(String id, String name, int maxOccurrences, int questTimeout,
                        List<String> parents, List<JsonObject> triggers, String triggerOrder,
                        List<Objective> objectives, List<Reward> rewards) {
        this.id = id;
        this.name = name;
        this.maxOccurrences = maxOccurrences;
        this.questTimeout = questTimeout;
        this.parents = Collections.unmodifiableList(parents);
        this.triggers = Collections.unmodifiableList(triggers);
        this.triggerOrder = triggerOrder;
        this.objectives = Collections.unmodifiableList(objectives);
        this.rewards = Collections.unmodifiableList(rewards);
    }

    // ========================= Objective Types =========================

    /**
     * Represents a single objective in the quest. Each objective has a type,
     * an optional dialogue tree, optional details for event-driven tracking,
     * the target citizen index (0=questgiver, 1+=participants), and
     * which rewards it unlocks upon advancing past it.
     */
    public static class Objective {
        private final ObjectiveType type;
        private final int target;
        private final DialogueTree.DialogueElement dialogueTree;
        private final JsonObject details;
        private final List<Integer> unlocksRewards;
        private final JsonObject rawJson;

        public Objective(ObjectiveType type, int target,
                         DialogueTree.DialogueElement dialogueTree,
                         JsonObject details, List<Integer> unlocksRewards, JsonObject rawJson) {
            this.type = type;
            this.target = target;
            this.dialogueTree = dialogueTree;
            this.details = details;
            this.unlocksRewards = unlocksRewards != null
                    ? Collections.unmodifiableList(unlocksRewards) : Collections.emptyList();
            this.rawJson = rawJson;
        }

        public ObjectiveType getType() { return type; }
        public int getTarget() { return target; }
        public DialogueTree.DialogueElement getDialogueTree() { return dialogueTree; }
        public JsonObject getDetails() { return details; }
        public List<Integer> getUnlocksRewards() { return unlocksRewards; }
        public JsonObject getRawJson() { return rawJson; }

        /**
         * For delivery objectives: gets the item ID from details.
         */
        public String getDeliveryItem() {
            return details != null ? getStringOr(details, "item", "") : "";
        }

        /**
         * For delivery objectives: gets the quantity from details.
         */
        public int getDeliveryQty() {
            return details != null ? getIntOr(details, "qty", 1) : 1;
        }

        /**
         * For event-driven objectives: gets the next objective to advance to.
         * -1 means complete the quest.
         */
        public int getNextObjective() {
            return details != null ? getIntOr(details, "next-objective", -1) : -1;
        }

        /**
         * For block objectives: gets the block ID.
         */
        public String getBlockId() {
            return details != null ? getStringOr(details, "block", "") : "";
        }

        /**
         * For entity objectives: gets the entity type ID.
         */
        public String getEntityTypeId() {
            return details != null ? getStringOr(details, "entity-type", "") : "";
        }

        /**
         * For building objectives: gets the building type.
         */
        public String getBuildingType() {
            return details != null ? getStringOr(details, "type", "") : "";
        }

        /**
         * For building objectives: gets the required level.
         */
        public int getBuildingLevel() {
            return details != null ? getIntOr(details, "lvl", 1) : 1;
        }

        /**
         * For building objectives: gets the quantity of buildings needed.
         */
        public int getBuildingQty() {
            return details != null ? getIntOr(details, "qty", 1) : 1;
        }

        /**
         * For building objectives: whether to count existing buildings.
         */
        public boolean isBuildingCountExisting() {
            return details != null && details.has("count-existing")
                    && details.get("count-existing").getAsBoolean();
        }

        /**
         * For delivery objectives: gets the nbt-mode (e.g. "any" to ignore NBT).
         */
        public String getNbtMode() {
            return details != null ? getStringOr(details, "nbt-mode", "") : "";
        }
    }

    /**
     * Enumeration of all supported objective types.
     */
    public enum ObjectiveType {
        DIALOGUE,
        DELIVERY,
        PLACE_BLOCK,
        BREAK_BLOCK,
        KILL_ENTITY,
        BUILD_BUILDING,
        UNKNOWN
    }

    // ========================= Reward Types =========================

    /**
     * Represents a single quest reward.
     */
    public static class Reward {
        private final RewardType type;
        private final JsonObject details;

        public Reward(RewardType type, JsonObject details) {
            this.type = type;
            this.details = details;
        }

        public RewardType getType() { return type; }
        public JsonObject getDetails() { return details; }

        // Convenience accessors
        public String getItemId() {
            return details != null ? getStringOr(details, "item", "minecraft:air") : "minecraft:air";
        }
        public int getQty() {
            return details != null ? getIntOr(details, "qty", 1) : 1;
        }
        public String getUnlockQuestId() {
            return details != null ? getStringOr(details, "id", "") : "";
        }
        public int getHappinessTarget() {
            return details != null ? getIntOr(details, "target", 0) : 0;
        }
        public int getHappinessDays() {
            return details != null ? getIntOr(details, "days", 1) : 1;
        }
        public double getReputationQty() {
            if (details == null) return 0;
            return details.has("qty") ? details.get("qty").getAsDouble() : 0;
        }
    }

    /**
     * Enumeration of all supported reward types.
     */
    public enum RewardType {
        ITEM,
        UNLOCK_QUEST,
        HAPPINESS,
        REPUTATION,
        UNKNOWN
    }

    // ========================= Parsing =========================

    /**
     * Parse a quest from JSON.
     *
     * @param id   the quest identifier, e.g. "megamod:tutorial/welcome"
     * @param json the parsed JSON object
     * @return a ColonyQuest instance
     */
    public static ColonyQuest fromJson(String id, JsonObject json) {
        String name = getStringOr(json, "name", "Unnamed Quest");
        int maxOccurrences = getIntOr(json, "max-occurrences", 1);
        int questTimeout = getIntOr(json, "timeout", 10);
        String triggerOrder = getStringOr(json, "triggerOrder", "");

        // Parents
        List<String> parents = new ArrayList<>();
        if (json.has("parents") && json.get("parents").isJsonArray()) {
            for (JsonElement el : json.getAsJsonArray("parents")) {
                if (el.isJsonPrimitive()) {
                    parents.add(el.getAsString());
                }
            }
        }

        // Triggers (stored as raw JSON for evaluation)
        List<JsonObject> triggers = new ArrayList<>();
        if (json.has("triggers") && json.get("triggers").isJsonArray()) {
            for (JsonElement el : json.getAsJsonArray("triggers")) {
                if (el.isJsonObject()) {
                    triggers.add(el.getAsJsonObject());
                }
            }
        }

        // Objectives - parsed into typed objects
        List<Objective> objectives = new ArrayList<>();
        if (json.has("objectives") && json.get("objectives").isJsonArray()) {
            for (JsonElement el : json.getAsJsonArray("objectives")) {
                if (el.isJsonObject()) {
                    objectives.add(parseObjective(el.getAsJsonObject()));
                }
            }
        }

        // Rewards - parsed into typed objects
        List<Reward> rewards = new ArrayList<>();
        if (json.has("rewards") && json.get("rewards").isJsonArray()) {
            for (JsonElement el : json.getAsJsonArray("rewards")) {
                if (el.isJsonObject()) {
                    rewards.add(parseReward(el.getAsJsonObject()));
                }
            }
        }

        return new ColonyQuest(id, name, maxOccurrences, questTimeout,
                parents, triggers, triggerOrder, objectives, rewards);
    }

    /**
     * Parses a single objective from JSON.
     */
    private static Objective parseObjective(JsonObject json) {
        String typeStr = getStringOr(json, "type", "");
        ObjectiveType type = parseObjectiveType(typeStr);
        int target = getIntOr(json, "target", 0);

        // Parse unlocks-rewards array
        List<Integer> unlocksRewards = new ArrayList<>();
        if (json.has("unlocks-rewards") && json.get("unlocks-rewards").isJsonArray()) {
            JsonArray arr = json.getAsJsonArray("unlocks-rewards");
            for (int i = 0; i < arr.size(); i++) {
                unlocksRewards.add(arr.get(i).getAsInt());
            }
        }

        // Parse dialogue tree for dialogue-type objectives
        DialogueTree.DialogueElement dialogueTree = null;
        if (json.has("text") && json.has("options")) {
            dialogueTree = DialogueTree.parse(json);
        }

        // For non-dialogue objectives, details are in a "details" sub-object
        // For delivery objectives, target is inside details
        JsonObject details = json.has("details") ? json.getAsJsonObject("details") : null;
        if (details != null && details.has("target")) {
            target = details.get("target").getAsInt();
        }

        return new Objective(type, target, dialogueTree, details, unlocksRewards, json);
    }

    /**
     * Maps a type string to an ObjectiveType enum.
     */
    private static ObjectiveType parseObjectiveType(String type) {
        return switch (type) {
            case "megamod:dialogue" -> ObjectiveType.DIALOGUE;
            case "megamod:delivery" -> ObjectiveType.DELIVERY;
            case "megamod:placeblock" -> ObjectiveType.PLACE_BLOCK;
            case "megamod:breakblock" -> ObjectiveType.BREAK_BLOCK;
            case "megamod:killentity" -> ObjectiveType.KILL_ENTITY;
            case "megamod:buildbuilding" -> ObjectiveType.BUILD_BUILDING;
            default -> ObjectiveType.UNKNOWN;
        };
    }

    /**
     * Parses a single reward from JSON.
     */
    private static Reward parseReward(JsonObject json) {
        String typeStr = getStringOr(json, "type", "");
        RewardType type = switch (typeStr) {
            case "megamod:item" -> RewardType.ITEM;
            case "megamod:unlockquest" -> RewardType.UNLOCK_QUEST;
            case "megamod:happiness" -> RewardType.HAPPINESS;
            case "megamod:questreputation", "megamod:reputation" -> RewardType.REPUTATION;
            default -> RewardType.UNKNOWN;
        };

        JsonObject details = json.has("details") ? json.getAsJsonObject("details") : new JsonObject();
        return new Reward(type, details);
    }

    // ==================== Getters ====================

    public String getId() { return id; }
    public String getName() { return name; }
    public int getMaxOccurrences() { return maxOccurrences; }
    public int getQuestTimeout() { return questTimeout; }
    public List<String> getParents() { return parents; }
    public List<JsonObject> getTriggers() { return triggers; }
    public String getTriggerOrder() { return triggerOrder; }
    public List<Objective> getObjectives() { return objectives; }
    public List<Reward> getRewards() { return rewards; }

    /**
     * Returns a short description of the current objective at the given index.
     */
    public String getObjectiveDescription(int index) {
        if (index < 0 || index >= objectives.size()) return "Completed";
        Objective obj = objectives.get(index);

        return switch (obj.type) {
            case DIALOGUE -> {
                if (obj.dialogueTree != null) {
                    String text = obj.dialogueTree.getText();
                    yield text.length() > 60 ? text.substring(0, 57) + "..." : text;
                }
                yield "Talk to citizen";
            }
            case DELIVERY -> "Deliver " + obj.getDeliveryQty() + "x " + obj.getDeliveryItem();
            case PLACE_BLOCK -> "Place " + obj.getBlockId();
            case BREAK_BLOCK -> "Break " + obj.getBlockId();
            case KILL_ENTITY -> "Kill " + obj.getEntityTypeId();
            case BUILD_BUILDING -> "Build " + obj.getBuildingType() + " to level " + obj.getBuildingLevel();
            case UNKNOWN -> "Unknown objective";
        };
    }

    /**
     * Returns the number of citizen participant slots this quest requires.
     * Scans all objectives for the highest target index.
     * target 0 = quest giver, 1+ = participants.
     */
    public int getRequiredParticipantCount() {
        int maxTarget = 0;
        for (Objective obj : objectives) {
            if (obj.target > maxTarget) {
                maxTarget = obj.target;
            }
        }
        return maxTarget; // Number of participants (excluding questgiver at 0)
    }

    // ---- JSON helpers ----

    static String getStringOr(JsonObject json, String key, String defaultValue) {
        if (json.has(key) && json.get(key).isJsonPrimitive()) return json.get(key).getAsString();
        return defaultValue;
    }

    static int getIntOr(JsonObject json, String key, int defaultValue) {
        if (json.has(key) && json.get(key).isJsonPrimitive()) return json.get(key).getAsInt();
        return defaultValue;
    }

    @Override
    public String toString() {
        return "ColonyQuest{id='" + id + "', name='" + name + "'}";
    }
}
