package com.ultra.megamod.feature.citizen.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a branch (category) of the research tree — e.g., "Combat",
 * "Technology", "Civilian", "Agriculture".
 * <p>
 * Each branch contains an ordered list of research IDs. Branches are
 * loaded from data packs and stored in the {@link GlobalResearchTree}.
 * Each colony has its own progress against each branch via {@link LocalResearchTree}.
 */
public class ResearchBranch {

    /**
     * Base research time, default ~1.5 hours playtime (same as MineColonies).
     * Formula: BASE_RESEARCH_TIME * baseTime * 2^(depth-1)
     */
    public static final int BASE_RESEARCH_TIME = 60 * 60 / 25 / 2; // 144 ticks = ~7.2 seconds per depth-1

    private final String id;
    private String name;
    private int sortOrder;
    private double baseTime;
    private boolean hidden;
    private final List<Identifier> researchIds = new ArrayList<>();

    public ResearchBranch(String id, String name, int sortOrder) {
        this(id, name, sortOrder, 1.0, false);
    }

    public ResearchBranch(String id, String name, int sortOrder, double baseTime, boolean hidden) {
        this.id = id;
        this.name = name;
        this.sortOrder = sortOrder;
        this.baseTime = baseTime;
        this.hidden = hidden;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public double getBaseTime() {
        return baseTime;
    }

    public void setBaseTime(double baseTime) {
        this.baseTime = baseTime;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * Returns the research time in ticks for a given depth/tier on this branch.
     * Matches MineColonies formula: BASE_RESEARCH_TIME * baseTime * 2^(depth-1)
     *
     * @param depth the tier/depth of the research (1-6)
     * @return the base research time in ticks
     */
    public int getBaseTimeForDepth(int depth) {
        return (int) (BASE_RESEARCH_TIME * this.baseTime * Math.pow(2, depth - 1));
    }

    /**
     * Returns the approximate hours of real time for the given depth.
     *
     * @param depth the tier/depth of the research (1-6)
     * @return hours of real time
     */
    public double getHoursTime(int depth) {
        return (getBaseTimeForDepth(depth) * 25.0) / 60 / 60;
    }

    /**
     * Returns the ordered list of research IDs in this branch.
     */
    public List<Identifier> getResearchIds() {
        return Collections.unmodifiableList(researchIds);
    }

    /**
     * Adds a research ID to this branch.
     */
    public void addResearch(Identifier researchId) {
        if (!researchIds.contains(researchId)) {
            researchIds.add(researchId);
        }
    }

    /**
     * Removes a research ID from this branch.
     */
    public void removeResearch(Identifier researchId) {
        researchIds.remove(researchId);
    }

    /**
     * Returns the list of {@link GlobalResearch} objects in this branch,
     * resolved from the global research tree.
     */
    public List<GlobalResearch> getResearches() {
        List<GlobalResearch> result = new ArrayList<>();
        for (Identifier id : researchIds) {
            GlobalResearch r = GlobalResearchTree.INSTANCE.getResearch(id);
            if (r != null) {
                result.add(r);
            }
        }
        return result;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", id);
        tag.putString("Name", name);
        tag.putInt("SortOrder", sortOrder);
        tag.putDouble("BaseTime", baseTime);
        tag.putBoolean("Hidden", hidden);
        ListTag list = new ListTag();
        for (Identifier researchId : researchIds) {
            list.add(StringTag.valueOf(researchId.toString()));
        }
        tag.put("Researches", (Tag) list);
        return tag;
    }

    public static ResearchBranch fromNbt(CompoundTag tag) {
        ResearchBranch branch = new ResearchBranch(
                tag.getStringOr("Id", ""),
                tag.getStringOr("Name", "Unknown"),
                tag.getIntOr("SortOrder", 0),
                tag.getDoubleOr("BaseTime", 1.0),
                tag.getBooleanOr("Hidden", false)
        );
        ListTag list = tag.getListOrEmpty("Researches");
        for (int i = 0; i < list.size(); i++) {
            Identifier id = Identifier.tryParse(list.getStringOr(i, ""));
            if (id != null) {
                branch.researchIds.add(id);
            }
        }
        return branch;
    }
}
