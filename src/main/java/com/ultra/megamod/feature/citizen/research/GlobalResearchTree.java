package com.ultra.megamod.feature.citizen.research;

import com.ultra.megamod.MegaMod;
import net.minecraft.resources.Identifier;

import java.util.*;

/**
 * Singleton registry holding all available research definitions and branches.
 * Populated by {@link ResearchDataListener} from JSON data packs at
 * {@code data/megamod/research/}.
 * <p>
 * This is global (shared across all colonies). Each colony has its own
 * {@link LocalResearchTree} tracking progress against these definitions.
 */
public class GlobalResearchTree {

    public static final GlobalResearchTree INSTANCE = new GlobalResearchTree();

    private final Map<Identifier, GlobalResearch> researches = new LinkedHashMap<>();
    private final Map<String, ResearchBranch> branches = new LinkedHashMap<>();
    private final Set<Identifier> autostartResearchIds = new LinkedHashSet<>();

    private GlobalResearchTree() {}

    // ---- Query ----

    /**
     * Returns the research definition for the given ID, or null if not found.
     */
    public GlobalResearch getResearch(Identifier id) {
        return researches.get(id);
    }

    /**
     * Returns the branch with the given ID, or null if not found.
     */
    public ResearchBranch getBranch(String branchId) {
        return branches.get(branchId);
    }

    /**
     * Returns all branches sorted by their sort order.
     */
    public List<ResearchBranch> getAllBranches() {
        List<ResearchBranch> sorted = new ArrayList<>(branches.values());
        sorted.sort(Comparator.comparingInt(ResearchBranch::getSortOrder));
        return sorted;
    }

    /**
     * Returns all registered research definitions.
     */
    public Collection<GlobalResearch> getAllResearches() {
        return Collections.unmodifiableCollection(researches.values());
    }

    /**
     * Returns all research IDs.
     */
    public Set<Identifier> getAllResearchIds() {
        return Collections.unmodifiableSet(researches.keySet());
    }

    /**
     * Returns all researches belonging to the given branch, sorted by tier.
     */
    public List<GlobalResearch> getResearchesForBranch(String branchId) {
        List<GlobalResearch> result = new ArrayList<>();
        for (GlobalResearch r : researches.values()) {
            if (branchId.equals(r.getBranchId())) {
                result.add(r);
            }
        }
        result.sort(Comparator.comparingInt(GlobalResearch::getTier));
        return result;
    }

    /**
     * Returns all child researches that have the given research as a parent.
     */
    public List<GlobalResearch> getChildren(Identifier parentId) {
        List<GlobalResearch> children = new ArrayList<>();
        for (GlobalResearch r : researches.values()) {
            if (r.getParentIds().contains(parentId)) {
                children.add(r);
            }
        }
        return children;
    }

    // ---- Registration (called by ResearchDataListener) ----

    /**
     * Registers or replaces a research definition.
     */
    public void addResearch(GlobalResearch research) {
        researches.put(research.getId(), research);

        // Auto-add to branch if it exists
        ResearchBranch branch = branches.get(research.getBranchId());
        if (branch != null) {
            branch.addResearch(research.getId());
        }
    }

    /**
     * Registers or replaces a research branch.
     */
    public void addBranch(ResearchBranch branch) {
        branches.put(branch.getId(), branch);
    }

    /**
     * Clears all researches and branches. Called before reloading data packs.
     */
    public void clear() {
        researches.clear();
        branches.clear();
        autostartResearchIds.clear();
    }

    /**
     * Returns the total number of registered researches.
     */
    public int size() {
        return researches.size();
    }

    /**
     * Returns all researches marked as autostart.
     */
    public Set<Identifier> getAutostartResearchIds() {
        return Collections.unmodifiableSet(autostartResearchIds);
    }

    /**
     * Returns all autostart GlobalResearch objects.
     */
    public List<GlobalResearch> getAutostartResearches() {
        List<GlobalResearch> result = new ArrayList<>();
        for (Identifier id : autostartResearchIds) {
            GlobalResearch r = researches.get(id);
            if (r != null) result.add(r);
        }
        return result;
    }

    /**
     * Checks if a given branch already has a tier-6 research completed
     * or in-progress in the given local tree.
     *
     * @param branchId the branch to check
     * @param localTree the colony's local research tree
     * @return true if a tier-6 research has been started or completed
     */
    public boolean branchHasMaxTierResearch(String branchId, LocalResearchTree localTree) {
        for (GlobalResearch r : getResearchesForBranch(branchId)) {
            if (r.getTier() >= 6) {
                ResearchState state = localTree.getResearchState(r.getId());
                if (state == ResearchState.IN_PROGRESS || state == ResearchState.FINISHED) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Validates the research tree for integrity issues. Builds parent-child
     * relationships, populates the autostart list, and logs warnings for
     * any dangling parent references or missing branch assignments.
     */
    public void validate() {
        autostartResearchIds.clear();

        for (GlobalResearch r : researches.values()) {
            // Check parent references and build child relationships
            for (Identifier parentId : r.getParentIds()) {
                GlobalResearch parent = researches.get(parentId);
                if (parent == null) {
                    MegaMod.LOGGER.warn("Research {} references unknown parent: {}",
                            r.getId(), parentId);
                } else {
                    parent.addChildId(r.getId());
                }
            }

            // Check branch assignment
            if (!r.getBranchId().isEmpty() && !branches.containsKey(r.getBranchId())) {
                MegaMod.LOGGER.warn("Research {} references unknown branch: {}",
                        r.getId(), r.getBranchId());
            }

            // Track autostart research
            if (r.isAutoStart()) {
                autostartResearchIds.add(r.getId());
            }
        }

        // Validate depth consistency (child must be exactly parent depth + 1)
        for (GlobalResearch r : researches.values()) {
            for (Identifier parentId : r.getParentIds()) {
                GlobalResearch parent = researches.get(parentId);
                if (parent != null) {
                    int depthDiff = r.getTier() - parent.getTier();
                    if (depthDiff != 1) {
                        MegaMod.LOGGER.warn("Research {} (tier {}) has parent {} (tier {}), " +
                                "expected tier difference of 1 but got {}",
                                r.getId(), r.getTier(), parentId, parent.getTier(), depthDiff);
                    }
                }
            }
        }
    }
}
