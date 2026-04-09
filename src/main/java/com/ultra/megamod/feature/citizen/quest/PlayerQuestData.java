package com.ultra.megamod.feature.citizen.quest;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.*;

/**
 * Per-player quest state. Tracks which quests are locked, available,
 * in progress, or completed, plus the current objective index,
 * completion counts, quest participants, quest reputation, unlocked
 * quests, and per-objective progress data.
 * <p>
 * Enhanced to match MineColonies' full quest tracking capabilities:
 * <ul>
 *   <li>Quest participants (citizen UUIDs assigned to quest roles)</li>
 *   <li>Quest reputation (accumulated from quest rewards)</li>
 *   <li>Explicitly unlocked quests (via unlock trigger/reward)</li>
 *   <li>Per-quest objective instance data (for event-driven objectives)</li>
 *   <li>Assignment day tracking (for quest timeouts)</li>
 *   <li>Granted reward tracking (which reward indices have been given)</li>
 * </ul>
 */
public class PlayerQuestData {

    public enum QuestState {
        LOCKED,
        AVAILABLE,
        IN_PROGRESS,
        COMPLETED
    }

    /** Quest ID -> current state */
    private final Map<String, QuestState> questStates = new LinkedHashMap<>();
    /** Quest ID -> current objective index (only meaningful when IN_PROGRESS) */
    private final Map<String, Integer> objectiveIndices = new HashMap<>();
    /** Quest ID -> number of times completed (for max-occurrences) */
    private final Map<String, Integer> completionCounts = new HashMap<>();
    /** The quest ID currently in progress (null if none) */
    private String activeQuestId = null;

    // ---- New fields for full MineColonies parity ----

    /** Quest ID -> list of citizen entity UUIDs participating.
     *  Index 0 = quest giver, 1+ = additional participants.
     *  These map to $0, $1, $2... in dialogue text. */
    private final Map<String, List<UUID>> questParticipants = new HashMap<>();

    /** Accumulated quest reputation for this player's colony. */
    private double questReputation = 0;

    /** Explicitly unlocked quest IDs (via unlock triggers/rewards). */
    private final Set<String> unlockedQuests = new HashSet<>();

    /** Quest ID -> current objective instance (for event-driven progress tracking). */
    private final Map<String, QuestObjectiveInstance> objectiveInstances = new HashMap<>();

    /** Quest ID -> game tick when the quest was assigned/started. */
    private final Map<String, Long> questAssignmentTicks = new HashMap<>();

    /** Quest ID -> set of reward indices already granted. */
    private final Map<String, Set<Integer>> grantedRewards = new HashMap<>();

    // ==================== State Access ====================

    public QuestState getState(String questId) {
        return questStates.getOrDefault(questId, QuestState.LOCKED);
    }

    public void setState(String questId, QuestState state) {
        questStates.put(questId, state);
        if (state == QuestState.IN_PROGRESS) {
            activeQuestId = questId;
        } else if (questId.equals(activeQuestId) && state != QuestState.IN_PROGRESS) {
            activeQuestId = null;
        }
    }

    public int getObjectiveIndex(String questId) {
        return objectiveIndices.getOrDefault(questId, 0);
    }

    public void setObjectiveIndex(String questId, int index) {
        objectiveIndices.put(questId, index);
    }

    public int getCompletionCount(String questId) {
        return completionCounts.getOrDefault(questId, 0);
    }

    public void incrementCompletionCount(String questId) {
        completionCounts.merge(questId, 1, Integer::sum);
    }

    public String getActiveQuestId() {
        return activeQuestId;
    }

    public void clearActiveQuest() {
        activeQuestId = null;
    }

    /**
     * Returns all quest IDs with the given state.
     */
    public List<String> getQuestsByState(QuestState state) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, QuestState> entry : questStates.entrySet()) {
            if (entry.getValue() == state) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    // ==================== Participant Access ====================

    /**
     * Sets the participant list for a quest.
     *
     * @param questId the quest ID
     * @param participants ordered list of citizen UUIDs
     *                     (index 0 = questgiver, 1+ = participants)
     */
    public void setParticipants(String questId, List<UUID> participants) {
        questParticipants.put(questId, new ArrayList<>(participants));
    }

    /**
     * Returns the participant list for a quest, or empty list.
     */
    public List<UUID> getParticipants(String questId) {
        return questParticipants.getOrDefault(questId, Collections.emptyList());
    }

    /**
     * Returns the participant UUID at a specific index for a quest.
     *
     * @param questId the quest ID
     * @param index 0 = questgiver, 1+ = participants
     * @return the UUID or null if not set
     */
    public UUID getParticipant(String questId, int index) {
        List<UUID> parts = questParticipants.get(questId);
        if (parts == null || index < 0 || index >= parts.size()) return null;
        return parts.get(index);
    }

    // ==================== Reputation ====================

    public double getQuestReputation() {
        return questReputation;
    }

    public void alterReputation(double delta) {
        questReputation += delta;
    }

    // ==================== Unlock Tracking ====================

    public void unlockQuest(String questId) {
        unlockedQuests.add(questId);
    }

    public boolean isQuestUnlocked(String questId) {
        return unlockedQuests.contains(questId);
    }

    // ==================== Objective Instance ====================

    public void setObjectiveInstance(String questId, QuestObjectiveInstance instance) {
        objectiveInstances.put(questId, instance);
    }

    public QuestObjectiveInstance getObjectiveInstance(String questId) {
        return objectiveInstances.get(questId);
    }

    public void clearObjectiveInstance(String questId) {
        objectiveInstances.remove(questId);
    }

    // ==================== Assignment Tick ====================

    public void setAssignmentTick(String questId, long tick) {
        questAssignmentTicks.put(questId, tick);
    }

    public long getAssignmentTick(String questId) {
        return questAssignmentTicks.getOrDefault(questId, 0L);
    }

    // ==================== Granted Rewards ====================

    /**
     * Marks a reward index as granted for a quest.
     */
    public void markRewardGranted(String questId, int rewardIndex) {
        grantedRewards.computeIfAbsent(questId, k -> new HashSet<>()).add(rewardIndex);
    }

    /**
     * Checks if a reward index has already been granted for a quest.
     */
    public boolean isRewardGranted(String questId, int rewardIndex) {
        Set<Integer> granted = grantedRewards.get(questId);
        return granted != null && granted.contains(rewardIndex);
    }

    /**
     * Returns the set of granted reward indices for a quest.
     */
    public Set<Integer> getGrantedRewards(String questId) {
        return grantedRewards.getOrDefault(questId, Collections.emptySet());
    }

    /**
     * Clears all tracking data for a quest (on completion or cancellation cleanup).
     */
    public void clearQuestTracking(String questId) {
        objectiveInstances.remove(questId);
        questAssignmentTicks.remove(questId);
        grantedRewards.remove(questId);
        // Don't clear participants -- they may be needed for dialogue reference
    }

    // ==================== NBT Persistence ====================

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        // States
        CompoundTag statesTag = new CompoundTag();
        for (Map.Entry<String, QuestState> entry : questStates.entrySet()) {
            statesTag.putString(entry.getKey(), entry.getValue().name());
        }
        tag.put("states", (Tag) statesTag);

        // Objective indices
        CompoundTag objTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : objectiveIndices.entrySet()) {
            objTag.putInt(entry.getKey(), entry.getValue());
        }
        tag.put("objectives", (Tag) objTag);

        // Completion counts
        CompoundTag countsTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : completionCounts.entrySet()) {
            countsTag.putInt(entry.getKey(), entry.getValue());
        }
        tag.put("counts", (Tag) countsTag);

        // Active quest
        if (activeQuestId != null) {
            tag.putString("active", activeQuestId);
        }

        // Participants
        CompoundTag participantsTag = new CompoundTag();
        for (Map.Entry<String, List<UUID>> entry : questParticipants.entrySet()) {
            ListTag uuidList = new ListTag();
            for (UUID uuid : entry.getValue()) {
                CompoundTag uuidTag = new CompoundTag();
                uuidTag.putString("id", uuid.toString());
                uuidList.add(uuidTag);
            }
            participantsTag.put(entry.getKey(), (Tag) uuidList);
        }
        tag.put("participants", (Tag) participantsTag);

        // Reputation
        tag.putDouble("reputation", questReputation);

        // Unlocked quests
        ListTag unlockedList = new ListTag();
        for (String unlocked : unlockedQuests) {
            CompoundTag unlockedTag = new CompoundTag();
            unlockedTag.putString("id", unlocked);
            unlockedList.add(unlockedTag);
        }
        tag.put("unlocked", (Tag) unlockedList);

        // Objective instances
        CompoundTag instancesTag = new CompoundTag();
        for (Map.Entry<String, QuestObjectiveInstance> entry : objectiveInstances.entrySet()) {
            instancesTag.put(entry.getKey(), (Tag) entry.getValue().save());
        }
        tag.put("objInstances", (Tag) instancesTag);

        // Assignment ticks
        CompoundTag ticksTag = new CompoundTag();
        for (Map.Entry<String, Long> entry : questAssignmentTicks.entrySet()) {
            ticksTag.putLong(entry.getKey(), entry.getValue());
        }
        tag.put("assignTicks", (Tag) ticksTag);

        // Granted rewards
        CompoundTag rewardsTag = new CompoundTag();
        for (Map.Entry<String, Set<Integer>> entry : grantedRewards.entrySet()) {
            ListTag indices = new ListTag();
            for (int idx : entry.getValue()) {
                CompoundTag idxTag = new CompoundTag();
                idxTag.putInt("i", idx);
                indices.add(idxTag);
            }
            rewardsTag.put(entry.getKey(), (Tag) indices);
        }
        tag.put("grantedRewards", (Tag) rewardsTag);

        return tag;
    }

    public static PlayerQuestData load(CompoundTag tag) {
        PlayerQuestData data = new PlayerQuestData();

        // States
        CompoundTag statesTag = tag.getCompoundOrEmpty("states");
        for (String key : statesTag.keySet()) {
            try {
                QuestState state = QuestState.valueOf(statesTag.getStringOr(key, "LOCKED"));
                data.questStates.put(key, state);
            } catch (IllegalArgumentException ignored) {
                data.questStates.put(key, QuestState.LOCKED);
            }
        }

        // Objective indices
        CompoundTag objTag = tag.getCompoundOrEmpty("objectives");
        for (String key : objTag.keySet()) {
            data.objectiveIndices.put(key, objTag.getIntOr(key, 0));
        }

        // Completion counts
        CompoundTag countsTag = tag.getCompoundOrEmpty("counts");
        for (String key : countsTag.keySet()) {
            data.completionCounts.put(key, countsTag.getIntOr(key, 0));
        }

        // Active quest
        String active = tag.getStringOr("active", "");
        if (!active.isEmpty()) {
            data.activeQuestId = active;
        }

        // Participants
        CompoundTag participantsTag = tag.getCompoundOrEmpty("participants");
        for (String questKey : participantsTag.keySet()) {
            ListTag uuidList = participantsTag.getListOrEmpty(questKey);
            List<UUID> uuids = new ArrayList<>();
            for (int i = 0; i < uuidList.size(); i++) {
                CompoundTag uuidTag = uuidList.getCompoundOrEmpty(i);
                String idStr = uuidTag.getStringOr("id", "");
                if (!idStr.isEmpty()) {
                    try {
                        uuids.add(UUID.fromString(idStr));
                    } catch (IllegalArgumentException ignored) {}
                }
            }
            data.questParticipants.put(questKey, uuids);
        }

        // Reputation
        data.questReputation = tag.getDoubleOr("reputation", 0.0);

        // Unlocked quests
        ListTag unlockedList = tag.getListOrEmpty("unlocked");
        for (int i = 0; i < unlockedList.size(); i++) {
            CompoundTag unlockedTag = unlockedList.getCompoundOrEmpty(i);
            String id = unlockedTag.getStringOr("id", "");
            if (!id.isEmpty()) {
                data.unlockedQuests.add(id);
            }
        }

        // Objective instances
        CompoundTag instancesTag = tag.getCompoundOrEmpty("objInstances");
        for (String key : instancesTag.keySet()) {
            CompoundTag instTag = instancesTag.getCompoundOrEmpty(key);
            data.objectiveInstances.put(key, QuestObjectiveInstance.load(instTag));
        }

        // Assignment ticks
        CompoundTag ticksTag = tag.getCompoundOrEmpty("assignTicks");
        for (String key : ticksTag.keySet()) {
            data.questAssignmentTicks.put(key, ticksTag.getLongOr(key, 0L));
        }

        // Granted rewards
        CompoundTag rewardsTag = tag.getCompoundOrEmpty("grantedRewards");
        for (String key : rewardsTag.keySet()) {
            ListTag indices = rewardsTag.getListOrEmpty(key);
            Set<Integer> idxSet = new HashSet<>();
            for (int i = 0; i < indices.size(); i++) {
                CompoundTag idxTag = indices.getCompoundOrEmpty(i);
                idxSet.add(idxTag.getIntOr("i", 0));
            }
            data.grantedRewards.put(key, idxSet);
        }

        return data;
    }
}
