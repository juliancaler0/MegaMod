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
 * Definition (template) of a single research. This is the global,
 * data-driven definition loaded from JSON data packs. Each colony tracks
 * its own progress against these templates via {@link LocalResearch}.
 * <p>
 * Fields:
 * <ul>
 *   <li><b>id</b> — Unique identifier (e.g., "megamod:combat/sharper_swords")</li>
 *   <li><b>name</b> — Display name shown in the research UI</li>
 *   <li><b>description</b> — Tooltip/description text</li>
 *   <li><b>branch</b> — The branch this research belongs to</li>
 *   <li><b>tier</b> — Tier level (1-6), determines position in the tree</li>
 *   <li><b>costs</b> — Resources required to start the research</li>
 *   <li><b>requirements</b> — Prerequisites (buildings, prior research)</li>
 *   <li><b>effects</b> — Bonuses applied when research completes</li>
 *   <li><b>parentIds</b> — Parent research IDs in the tree graph</li>
 *   <li><b>hidden</b> — If true, not visible until requirements are nearly met</li>
 *   <li><b>autoStart</b> — If true, starts automatically when requirements are met</li>
 * </ul>
 */
public class GlobalResearch {

    private final Identifier id;
    private String name;
    private String description;
    private String branchId;
    private int tier;
    private final List<IResearchCost> costs = new ArrayList<>();
    private final List<IResearchRequirement> requirements = new ArrayList<>();
    private final List<IResearchEffect> effects = new ArrayList<>();
    private final List<Identifier> parentIds = new ArrayList<>();
    private final List<Identifier> blocking = new ArrayList<>();
    private final List<Identifier> childIds = new ArrayList<>();
    private boolean hidden;
    private boolean autoStart;
    private boolean exclusive;
    private boolean instant;
    private boolean immutable;
    private boolean onlyChild;
    private int researchTime; // ticks required to complete
    private int sortOrder;

    public GlobalResearch(Identifier id, String name, String description, String branchId, int tier) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.branchId = branchId;
        this.tier = tier;
        this.hidden = false;
        this.autoStart = false;
        this.exclusive = false;
        this.instant = false;
        this.immutable = false;
        this.onlyChild = false;
        this.researchTime = 6000; // default: 5 minutes at 20 TPS
        this.sortOrder = 1000;
    }

    // ---- Getters ----

    public Identifier getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getBranchId() { return branchId; }
    public int getTier() { return tier; }
    public List<IResearchCost> getCosts() { return Collections.unmodifiableList(costs); }
    public List<IResearchRequirement> getRequirements() { return Collections.unmodifiableList(requirements); }
    public List<IResearchEffect> getEffects() { return Collections.unmodifiableList(effects); }
    public List<Identifier> getParentIds() { return Collections.unmodifiableList(parentIds); }
    public List<Identifier> getBlocking() { return Collections.unmodifiableList(blocking); }
    public List<Identifier> getChildIds() { return Collections.unmodifiableList(childIds); }
    public boolean isHidden() { return hidden; }
    public boolean isAutoStart() { return autoStart; }
    public boolean isExclusive() { return exclusive; }
    public boolean isInstant() { return instant; }
    public boolean isImmutable() { return immutable; }
    public boolean hasOnlyChild() { return onlyChild; }
    public int getResearchTime() { return researchTime; }
    public int getSortOrder() { return sortOrder; }

    // ---- Setters (used during data pack loading) ----

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setBranchId(String branchId) { this.branchId = branchId; }
    public void setTier(int tier) { this.tier = tier; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }
    public void setAutoStart(boolean autoStart) { this.autoStart = autoStart; }
    public void setExclusive(boolean exclusive) { this.exclusive = exclusive; }
    public void setInstant(boolean instant) { this.instant = instant; }
    public void setImmutable(boolean immutable) { this.immutable = immutable; }
    public void setOnlyChild(boolean onlyChild) { this.onlyChild = onlyChild; }
    public void setResearchTime(int researchTime) { this.researchTime = Math.max(1, researchTime); }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public void addCost(IResearchCost cost) { costs.add(cost); }
    public void addRequirement(IResearchRequirement req) { requirements.add(req); }
    public void addEffect(IResearchEffect effect) { effects.add(effect); }
    public void addParentId(Identifier parentId) { parentIds.add(parentId); }
    public void addBlocking(Identifier blockedId) { blocking.add(blockedId); }
    public void addChildId(Identifier childId) { if (!childIds.contains(childId)) childIds.add(childId); }

    /**
     * Checks if any child of this research has already been started or completed
     * in the given local tree. Used with onlyChild to enforce exclusive child selection.
     */
    public boolean hasResearchedChild(LocalResearchTree localTree) {
        for (Identifier childId : childIds) {
            ResearchState childState = localTree.getResearchState(childId);
            if (childState == ResearchState.IN_PROGRESS || childState == ResearchState.FINISHED) {
                return true;
            }
        }
        return false;
    }

    // ---- Persistence ----

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", id.toString());
        tag.putString("Name", name);
        tag.putString("Description", description);
        tag.putString("BranchId", branchId);
        tag.putInt("Tier", tier);
        tag.putBoolean("Hidden", hidden);
        tag.putBoolean("AutoStart", autoStart);
        tag.putBoolean("Exclusive", exclusive);
        tag.putBoolean("Instant", instant);
        tag.putBoolean("Immutable", immutable);
        tag.putBoolean("OnlyChild", onlyChild);
        tag.putInt("ResearchTime", researchTime);
        tag.putInt("SortOrder", sortOrder);

        // Costs
        ListTag costList = new ListTag();
        for (IResearchCost cost : costs) {
            costList.add(cost.toNbt());
        }
        tag.put("Costs", (Tag) costList);

        // Requirements
        ListTag reqList = new ListTag();
        for (IResearchRequirement req : requirements) {
            reqList.add(req.toNbt());
        }
        tag.put("Requirements", (Tag) reqList);

        // Effects
        ListTag effectList = new ListTag();
        for (IResearchEffect effect : effects) {
            effectList.add(effect.toNbt());
        }
        tag.put("Effects", (Tag) effectList);

        // Parents
        ListTag parentList = new ListTag();
        for (Identifier parentId : parentIds) {
            parentList.add(StringTag.valueOf(parentId.toString()));
        }
        tag.put("Parents", (Tag) parentList);

        // Blocking
        ListTag blockingList = new ListTag();
        for (Identifier blockedId : blocking) {
            blockingList.add(StringTag.valueOf(blockedId.toString()));
        }
        tag.put("Blocking", (Tag) blockingList);

        // Children
        ListTag childList = new ListTag();
        for (Identifier childId : childIds) {
            childList.add(StringTag.valueOf(childId.toString()));
        }
        tag.put("Children", (Tag) childList);

        return tag;
    }

    public static GlobalResearch fromNbt(CompoundTag tag) {
        Identifier id = Identifier.tryParse(tag.getStringOr("Id", "megamod:unknown"));
        if (id == null) id = Identifier.fromNamespaceAndPath("megamod", "unknown");

        GlobalResearch research = new GlobalResearch(
                id,
                tag.getStringOr("Name", "Unknown"),
                tag.getStringOr("Description", ""),
                tag.getStringOr("BranchId", ""),
                tag.getIntOr("Tier", 1)
        );
        research.hidden = tag.getBooleanOr("Hidden", false);
        research.autoStart = tag.getBooleanOr("AutoStart", false);
        research.exclusive = tag.getBooleanOr("Exclusive", false);
        research.instant = tag.getBooleanOr("Instant", false);
        research.immutable = tag.getBooleanOr("Immutable", false);
        research.onlyChild = tag.getBooleanOr("OnlyChild", false);
        research.researchTime = tag.getIntOr("ResearchTime", 6000);
        research.sortOrder = tag.getIntOr("SortOrder", 1000);

        // Costs
        ListTag costList = tag.getListOrEmpty("Costs");
        for (int i = 0; i < costList.size(); i++) {
            CompoundTag costTag = costList.getCompoundOrEmpty(i);
            IResearchCost cost = IResearchCost.fromNbt(costTag);
            if (cost != null) research.costs.add(cost);
        }

        // Requirements
        ListTag reqList = tag.getListOrEmpty("Requirements");
        for (int i = 0; i < reqList.size(); i++) {
            CompoundTag reqTag = reqList.getCompoundOrEmpty(i);
            IResearchRequirement req = IResearchRequirement.fromNbt(reqTag);
            if (req != null) research.requirements.add(req);
        }

        // Effects
        ListTag effectList = tag.getListOrEmpty("Effects");
        for (int i = 0; i < effectList.size(); i++) {
            CompoundTag effectTag = effectList.getCompoundOrEmpty(i);
            IResearchEffect effect = IResearchEffect.fromNbt(effectTag);
            if (effect != null) research.effects.add(effect);
        }

        // Parents
        ListTag parentList = tag.getListOrEmpty("Parents");
        for (int i = 0; i < parentList.size(); i++) {
            Identifier parentId = Identifier.tryParse(parentList.getStringOr(i, ""));
            if (parentId != null) research.parentIds.add(parentId);
        }

        // Blocking
        ListTag blockingList = tag.getListOrEmpty("Blocking");
        for (int i = 0; i < blockingList.size(); i++) {
            Identifier blockedId = Identifier.tryParse(blockingList.getStringOr(i, ""));
            if (blockedId != null) research.blocking.add(blockedId);
        }

        // Children
        ListTag childList = tag.getListOrEmpty("Children");
        for (int i = 0; i < childList.size(); i++) {
            Identifier childId = Identifier.tryParse(childList.getStringOr(i, ""));
            if (childId != null) research.childIds.add(childId);
        }

        return research;
    }
}
