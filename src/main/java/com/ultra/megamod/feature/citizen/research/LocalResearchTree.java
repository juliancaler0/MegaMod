package com.ultra.megamod.feature.citizen.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;

import java.util.*;

/**
 * A colony's research progress tracker. Stores a {@link LocalResearch}
 * instance for each research the colony has started or completed.
 * <p>
 * This is the mutable, per-colony tree. The immutable global definitions
 * live in {@link GlobalResearchTree}.
 */
public class LocalResearchTree {

    private final Map<Identifier, LocalResearch> researchProgress = new LinkedHashMap<>();

    // ---- Query ----

    /**
     * Returns the local progress record for a research, or null if the
     * colony has never interacted with it.
     */
    public LocalResearch getResearch(Identifier researchId) {
        return researchProgress.get(researchId);
    }

    /**
     * Returns the state of a research in this colony. Returns NOT_STARTED
     * if the colony has no record of the research.
     */
    public ResearchState getResearchState(Identifier researchId) {
        LocalResearch local = researchProgress.get(researchId);
        return local != null ? local.getState() : ResearchState.NOT_STARTED;
    }

    /**
     * Returns true if the given research has been completed.
     */
    public boolean isResearchComplete(Identifier researchId) {
        return getResearchState(researchId) == ResearchState.FINISHED;
    }

    /**
     * Returns true if the given research is currently in progress.
     */
    public boolean isResearchInProgress(Identifier researchId) {
        return getResearchState(researchId) == ResearchState.IN_PROGRESS;
    }

    /**
     * Returns the IDs of all completed researches.
     */
    public List<Identifier> getCompletedResearches() {
        List<Identifier> completed = new ArrayList<>();
        for (Map.Entry<Identifier, LocalResearch> entry : researchProgress.entrySet()) {
            if (entry.getValue().getState() == ResearchState.FINISHED) {
                completed.add(entry.getKey());
            }
        }
        return completed;
    }

    /**
     * Returns the IDs of all researches currently in progress.
     */
    public List<Identifier> getInProgressResearches() {
        List<Identifier> inProgress = new ArrayList<>();
        for (Map.Entry<Identifier, LocalResearch> entry : researchProgress.entrySet()) {
            if (entry.getValue().getState() == ResearchState.IN_PROGRESS) {
                inProgress.add(entry.getKey());
            }
        }
        return inProgress;
    }

    /**
     * Returns the total number of completed researches.
     */
    public int getCompletedCount() {
        int count = 0;
        for (LocalResearch r : researchProgress.values()) {
            if (r.getState() == ResearchState.FINISHED) count++;
        }
        return count;
    }

    /**
     * Returns true if the given branch already has a tier-6 (max depth)
     * research completed or in progress in this colony. Used for the
     * MineColonies column-6 mutual exclusivity rule.
     *
     * @param branchId the branch to check
     * @return true if a tier-6 research is already locked in
     */
    public boolean branchFinishedHighestLevel(String branchId) {
        for (Map.Entry<Identifier, LocalResearch> entry : researchProgress.entrySet()) {
            LocalResearch local = entry.getValue();
            if (local.getState() == ResearchState.FINISHED || local.getState() == ResearchState.IN_PROGRESS) {
                GlobalResearch global = GlobalResearchTree.INSTANCE.getResearch(entry.getKey());
                if (global != null && branchId.equals(global.getBranchId()) && global.getTier() >= 6) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns all local research records.
     */
    public Collection<LocalResearch> getAllProgress() {
        return Collections.unmodifiableCollection(researchProgress.values());
    }

    // ---- Mutations ----

    /**
     * Starts a research. Creates a new {@link LocalResearch} record if one
     * does not exist, and transitions it to IN_PROGRESS.
     *
     * @param researchId       the research to start
     * @param requiredProgress the total progress ticks needed (from GlobalResearch)
     */
    public void startResearch(Identifier researchId, int requiredProgress) {
        LocalResearch local = researchProgress.get(researchId);
        if (local == null) {
            local = new LocalResearch(researchId, requiredProgress);
            researchProgress.put(researchId, local);
        }
        local.start();
    }

    /**
     * Adds progress to an in-progress research.
     *
     * @param researchId the research to progress
     * @param amount     the progress ticks to add
     * @return true if the research just completed as a result
     */
    public boolean progressResearch(Identifier researchId, int amount) {
        LocalResearch local = researchProgress.get(researchId);
        if (local == null) return false;
        return local.addProgress(amount);
    }

    /**
     * Force-completes a research immediately, regardless of progress.
     */
    public void completeResearch(Identifier researchId) {
        LocalResearch local = researchProgress.get(researchId);
        if (local == null) {
            // Create the record and immediately complete it
            GlobalResearch global = GlobalResearchTree.INSTANCE.getResearch(researchId);
            int time = global != null ? global.getResearchTime() : 1;
            local = new LocalResearch(researchId, time);
            researchProgress.put(researchId, local);
        }
        local.complete();
    }

    /**
     * Resets a research back to NOT_STARTED, clearing its progress.
     */
    public void resetResearch(Identifier researchId) {
        LocalResearch local = researchProgress.get(researchId);
        if (local != null) {
            local.reset();
        }
    }

    // ---- Persistence ----

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (LocalResearch local : researchProgress.values()) {
            list.add(local.toNbt());
        }
        tag.put("Researches", (Tag) list);
        return tag;
    }

    public static LocalResearchTree fromNbt(CompoundTag tag) {
        LocalResearchTree tree = new LocalResearchTree();
        ListTag list = tag.getListOrEmpty("Researches");
        for (int i = 0; i < list.size(); i++) {
            CompoundTag researchTag = list.getCompoundOrEmpty(i);
            LocalResearch local = LocalResearch.fromNbt(researchTag);
            tree.researchProgress.put(local.getResearchId(), local);
        }
        return tree;
    }
}
