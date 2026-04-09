package com.ultra.megamod.feature.citizen.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

/**
 * Colony-specific progress for a single research. Tracks the current state,
 * how many progress ticks have been accumulated, and the total required.
 * <p>
 * This is the mutable, per-colony counterpart to the immutable
 * {@link GlobalResearch} template.
 */
public class LocalResearch {

    private final Identifier researchId;
    private ResearchState state;
    private int progress;
    private int requiredProgress;

    public LocalResearch(Identifier researchId, int requiredProgress) {
        this.researchId = researchId;
        this.state = ResearchState.NOT_STARTED;
        this.progress = 0;
        this.requiredProgress = Math.max(1, requiredProgress);
    }

    // ---- Getters ----

    public Identifier getResearchId() { return researchId; }
    public ResearchState getState() { return state; }
    public int getProgress() { return progress; }
    public int getRequiredProgress() { return requiredProgress; }

    /**
     * Returns a value between 0.0 and 1.0 representing completion percentage.
     */
    public float getProgressFraction() {
        if (requiredProgress <= 0) return 1.0f;
        return Math.min(1.0f, (float) progress / requiredProgress);
    }

    // ---- State transitions ----

    /**
     * Starts this research. Sets state to IN_PROGRESS and resets progress to 0.
     */
    public void start() {
        this.state = ResearchState.IN_PROGRESS;
        this.progress = 0;
    }

    /**
     * Adds progress ticks. If progress reaches or exceeds requiredProgress,
     * the state transitions to FINISHED.
     *
     * @param amount the number of progress ticks to add
     * @return true if the research just completed as a result of this call
     */
    public boolean addProgress(int amount) {
        if (state != ResearchState.IN_PROGRESS) return false;
        this.progress += amount;
        if (progress >= requiredProgress) {
            progress = requiredProgress;
            state = ResearchState.FINISHED;
            return true;
        }
        return false;
    }

    /**
     * Force-completes this research immediately.
     */
    public void complete() {
        this.progress = requiredProgress;
        this.state = ResearchState.FINISHED;
    }

    /**
     * Resets this research back to NOT_STARTED with zero progress.
     */
    public void reset() {
        this.state = ResearchState.NOT_STARTED;
        this.progress = 0;
    }

    // ---- Persistence ----

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("ResearchId", researchId.toString());
        tag.putString("State", state.name());
        tag.putInt("Progress", progress);
        tag.putInt("RequiredProgress", requiredProgress);
        return tag;
    }

    public static LocalResearch fromNbt(CompoundTag tag) {
        Identifier id = Identifier.tryParse(tag.getStringOr("ResearchId", "megamod:unknown"));
        if (id == null) id = Identifier.fromNamespaceAndPath("megamod", "unknown");
        int required = tag.getIntOr("RequiredProgress", 6000);
        LocalResearch research = new LocalResearch(id, required);
        research.state = ResearchState.fromString(tag.getStringOr("State", "NOT_STARTED"));
        research.progress = tag.getIntOr("Progress", 0);
        return research;
    }
}
