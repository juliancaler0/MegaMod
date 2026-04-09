package com.ultra.megamod.feature.citizen.research;

import com.ultra.megamod.MegaMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

import com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding;
import com.ultra.megamod.feature.citizen.data.ClaimData;
import com.ultra.megamod.feature.citizen.data.ClaimManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Per-colony research management. Links a {@link LocalResearchTree}
 * (colony progress) with a {@link ResearchEffectManager} (active bonuses),
 * and handles the full research lifecycle: validation, cost deduction,
 * progress ticking, and effect application.
 * <p>
 * Persists to {@code world/data/megamod_research.dat}. In a multi-colony
 * setup, each colony's data is stored under its faction ID key.
 * <p>
 * Building level queries are delegated to a pluggable function that can
 * be wired to the colony's actual building registry when the University
 * building is integrated.
 */
public class ResearchManager {

    private static final String FILE_NAME = "megamod_research.dat";

    // factionId -> ResearchManager
    private static final Map<String, ResearchManager> INSTANCES = new HashMap<>();
    private static boolean loaded = false;

    private final String factionId;
    private final LocalResearchTree localTree;
    private final ResearchEffectManager effectManager;
    private boolean dirty = false;
    private long lastSaveTimestamp; // game time ticks when last saved

    /**
     * Pluggable function to query building levels for the colony.
     * Maps building ID -> current level. Defaults to always returning 0
     * until wired into the building system.
     */
    private Function<String, Integer> buildingLevelProvider = buildingId -> 0;

    public ResearchManager(String factionId) {
        this.factionId = factionId;
        this.localTree = new LocalResearchTree();
        this.effectManager = new ResearchEffectManager();
        this.lastSaveTimestamp = 0;
    }

    private ResearchManager(String factionId, LocalResearchTree localTree,
                            ResearchEffectManager effectManager, long lastSaveTimestamp) {
        this.factionId = factionId;
        this.localTree = localTree;
        this.effectManager = effectManager;
        this.lastSaveTimestamp = lastSaveTimestamp;
    }

    // ---- Static access ----

    /**
     * Returns the research manager for the given faction, loading from
     * disk if necessary. Automatically wires the building level provider
     * to query actual building levels from placed colony buildings in
     * the faction's claimed chunks. Also processes offline research if
     * the university is level 3+.
     */
    public static ResearchManager get(ServerLevel level, String factionId) {
        if (!loaded) {
            loadAllFromDisk(level);
            loaded = true;
            // Process offline research for all loaded managers
            long currentTime = level.getGameTime();
            for (ResearchManager mgr : INSTANCES.values()) {
                if (mgr.lastSaveTimestamp > 0 && currentTime > mgr.lastSaveTimestamp) {
                    long elapsed = currentTime - mgr.lastSaveTimestamp;
                    mgr.processOfflineResearch(level, elapsed);
                }
            }
        }
        ResearchManager manager = INSTANCES.computeIfAbsent(factionId, ResearchManager::new);

        // Wire the building level provider to scan the faction's claimed chunks
        // for TileEntityColonyBuilding instances matching the requested building type.
        // Returns the SUM of all building levels of that type across the colony,
        // so requirements like "Residences totaling 3 levels" work correctly.
        manager.setBuildingLevelProvider(buildingId -> {
            ClaimManager claimMgr = ClaimManager.get(level);
            ClaimData claimData = claimMgr.getClaim(factionId);
            if (claimData == null) return 0;

            int totalLevels = 0;
            for (long[] chunkCoords : claimData.getClaimedChunks()) {
                ChunkPos chunkPos = new ChunkPos((int) chunkCoords[0], (int) chunkCoords[1]);
                if (!level.hasChunk(chunkPos.x, chunkPos.z)) continue;
                LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);

                for (var entry : chunk.getBlockEntities().entrySet()) {
                    if (entry.getValue() instanceof TileEntityColonyBuilding building) {
                        if (buildingId.equals(building.getBuildingId())) {
                            totalLevels += building.getBuildingLevel();
                        }
                    }
                }
            }
            return totalLevels;
        });

        return manager;
    }

    /**
     * Resets all cached instances. Called on server shutdown.
     */
    public static void reset() {
        INSTANCES.clear();
        loaded = false;
    }

    /**
     * Saves all dirty faction research managers to disk.
     */
    public static void saveAll(ServerLevel level) {
        boolean anyDirty = false;
        for (ResearchManager mgr : INSTANCES.values()) {
            if (mgr.dirty) {
                anyDirty = true;
                break;
            }
        }
        if (!anyDirty) return;

        long currentTime = level.getGameTime();

        try {
            Path dir = level.getServer().getWorldPath(LevelResource.ROOT).resolve("data");
            dir.toFile().mkdirs();

            CompoundTag root = new CompoundTag();
            for (Map.Entry<String, ResearchManager> entry : INSTANCES.entrySet()) {
                entry.getValue().lastSaveTimestamp = currentTime;
                root.put(entry.getKey(), (Tag) entry.getValue().toNbt());
            }

            NbtIo.writeCompressed(root, dir.resolve(FILE_NAME));

            for (ResearchManager mgr : INSTANCES.values()) {
                mgr.dirty = false;
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save research data", e);
        }
    }

    private static void loadAllFromDisk(ServerLevel level) {
        try {
            Path file = level.getServer().getWorldPath(LevelResource.ROOT)
                    .resolve("data").resolve(FILE_NAME);
            if (!file.toFile().exists()) return;

            CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
            for (String factionId : root.keySet()) {
                CompoundTag factionTag = root.getCompoundOrEmpty(factionId);
                ResearchManager mgr = ResearchManager.fromNbt(factionId, factionTag);
                INSTANCES.put(factionId, mgr);
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load research data", e);
        }
    }

    // ---- Accessors ----

    public String getFactionId() { return factionId; }
    public LocalResearchTree getLocalTree() { return localTree; }
    public ResearchEffectManager getEffectManager() { return effectManager; }

    /**
     * Sets the function used to query building levels for requirement checks.
     * This should be wired to the colony's building system.
     */
    public void setBuildingLevelProvider(Function<String, Integer> provider) {
        this.buildingLevelProvider = provider;
    }

    /**
     * Returns the current level of the specified building in this colony.
     * Used by {@link BuildingResearchRequirement}.
     */
    public int getBuildingLevel(String buildingId) {
        return buildingLevelProvider.apply(buildingId);
    }

    // ---- Research lifecycle ----

    /**
     * Attempts to start a research. Validates all requirements and costs,
     * deducts costs from the player, and begins research progress.
     * <p>
     * Validation checks (in order):
     * <ol>
     *   <li>Research exists in global tree</li>
     *   <li>Not already completed or in progress</li>
     *   <li>Not blocked by any completed research's blocking list</li>
     *   <li>Column 6 / exclusive mutual exclusivity</li>
     *   <li>Parent's onlyChild check (only one child can be researched)</li>
     *   <li>Requirements (building levels, prior research)</li>
     *   <li>Parent research completed</li>
     *   <li>Costs affordable</li>
     * </ol>
     *
     * @param researchId the research to start
     * @param player     the player initiating the research
     * @return a result indicating success or the reason for failure
     */
    public StartResult startResearch(Identifier researchId, Player player) {
        GlobalResearch global = GlobalResearchTree.INSTANCE.getResearch(researchId);
        if (global == null) {
            return StartResult.failure("Unknown research: " + researchId);
        }

        // Check not already completed or in progress
        ResearchState currentState = localTree.getResearchState(researchId);
        if (currentState == ResearchState.FINISHED) {
            return StartResult.failure("Research already completed.");
        }
        if (currentState == ResearchState.IN_PROGRESS) {
            return StartResult.failure("Research already in progress.");
        }

        // Check if this research is blocked by any completed research's blocking list
        for (GlobalResearch other : GlobalResearchTree.INSTANCE.getAllResearches()) {
            if (localTree.isResearchComplete(other.getId())) {
                if (other.getBlocking().contains(researchId)) {
                    return StartResult.failure("Blocked by completed research: " + other.getName());
                }
            }
        }

        // Check column 6 / exclusive mutual exclusivity: only one tier-6 research per branch
        if (global.getTier() >= 6) {
            String branch = global.getBranchId();
            if (GlobalResearchTree.INSTANCE.branchHasMaxTierResearch(branch, localTree)) {
                return StartResult.failure("A tier-6 research in this branch is already completed.");
            }
        }

        // Check parent's onlyChild flag: if parent has onlyChild, only one child can be researched
        for (Identifier parentId : global.getParentIds()) {
            GlobalResearch parent = GlobalResearchTree.INSTANCE.getResearch(parentId);
            if (parent != null && parent.hasOnlyChild() && parent.hasResearchedChild(localTree)) {
                return StartResult.failure("Parent research '" + parent.getName() + "' only allows one child research.");
            }
        }

        // Check requirements
        for (IResearchRequirement req : global.getRequirements()) {
            if (!req.isFulfilled(this)) {
                return StartResult.failure("Requirement not met: " +
                        req.getDisplayText().getString());
            }
        }

        // Check parent research completed
        for (Identifier parentId : global.getParentIds()) {
            if (!localTree.isResearchComplete(parentId)) {
                GlobalResearch parent = GlobalResearchTree.INSTANCE.getResearch(parentId);
                String parentName = parent != null ? parent.getName() : parentId.getPath();
                return StartResult.failure("Prerequisite research not completed: " + parentName);
            }
        }

        // Check costs
        for (IResearchCost cost : global.getCosts()) {
            if (!cost.canAfford(player)) {
                return StartResult.failure("Cannot afford: " +
                        cost.getDisplayText().getString());
            }
        }

        // Deduct costs
        for (IResearchCost cost : global.getCosts()) {
            cost.deduct(player);
        }

        // Start the research
        localTree.startResearch(researchId, global.getResearchTime());

        // If instant, complete it immediately
        if (global.isInstant()) {
            completeResearch(researchId);
        }

        markDirty();

        return StartResult.success();
    }

    /**
     * Ticks all in-progress researches. Called each server tick from the
     * colony/faction tick handler.
     * <p>
     * Progress rate can be modified by the university building level
     * in the future. Currently, 1 tick of progress per server tick.
     *
     * @param level the server level
     */
    public void tickResearch(ServerLevel level) {
        List<Identifier> justCompleted = new ArrayList<>();

        for (Identifier researchId : localTree.getInProgressResearches()) {
            // Base progress: 1 tick per server tick
            // Can be scaled by university level later
            int progressAmount = 1;

            boolean completed = localTree.progressResearch(researchId, progressAmount);
            if (completed) {
                justCompleted.add(researchId);
            }
        }

        // Apply effects for newly completed research
        for (Identifier researchId : justCompleted) {
            completeResearch(researchId);
        }

        if (!justCompleted.isEmpty()) {
            markDirty();
        }
    }

    /**
     * Completes a research and applies its effects. Called automatically
     * when progress finishes, or can be called manually for admin/debug.
     */
    public void completeResearch(Identifier researchId) {
        localTree.completeResearch(researchId);

        GlobalResearch global = GlobalResearchTree.INSTANCE.getResearch(researchId);
        if (global != null) {
            for (IResearchEffect effect : global.getEffects()) {
                effect.apply(effectManager);
            }
        }

        markDirty();
    }

    /**
     * Resets a research, removing its effects if it was completed.
     * For in-progress research, this cancels it.
     * For completed research, this undoes it (checking immutable and children).
     *
     * @param researchId the research to reset
     * @return a result indicating success or the reason for failure
     */
    public StartResult resetResearch(Identifier researchId) {
        ResearchState oldState = localTree.getResearchState(researchId);

        if (oldState == ResearchState.IN_PROGRESS) {
            // Cancelling in-progress research is always allowed
            localTree.resetResearch(researchId);
            markDirty();
            return StartResult.success();
        }

        if (oldState == ResearchState.FINISHED) {
            // Undoing completed research has additional checks
            return undoResearch(researchId);
        }

        return StartResult.failure("Research is not started or completed.");
    }

    /**
     * Undoes a completed research: resets it to NOT_STARTED and removes its
     * effects, but does NOT refund costs. This allows the player to choose
     * a different path (especially useful for exclusive column 6 researches).
     * <p>
     * Cannot undo immutable research, or research that has children already
     * started/completed.
     *
     * @param researchId the research to undo
     * @return a result indicating success or the reason for failure
     */
    public StartResult undoResearch(Identifier researchId) {
        ResearchState state = localTree.getResearchState(researchId);
        if (state != ResearchState.FINISHED) {
            return StartResult.failure("Can only undo completed research.");
        }

        GlobalResearch global = GlobalResearchTree.INSTANCE.getResearch(researchId);
        if (global == null) {
            return StartResult.failure("Unknown research: " + researchId);
        }

        // Check immutable flag
        if (global.isImmutable()) {
            return StartResult.failure("This research cannot be undone.");
        }

        // Check if any children have been started/completed
        for (Identifier childId : global.getChildIds()) {
            ResearchState childState = localTree.getResearchState(childId);
            if (childState == ResearchState.IN_PROGRESS || childState == ResearchState.FINISHED) {
                return StartResult.failure("Cannot undo: child research '" + childId.getPath() + "' is already started.");
            }
        }

        // Reset the research to NOT_STARTED
        localTree.resetResearch(researchId);

        // Full recompute of effects from scratch (matching MineColonies approach)
        // This is safer than individual remove because effects may cross branches
        recomputeEffects();

        markDirty();
        return StartResult.success();
    }

    /**
     * Recomputes all effects from scratch based on current completed research.
     * Useful after loading or if effects get out of sync.
     */
    public void recomputeEffects() {
        effectManager.recomputeFromTree(localTree);
    }

    /**
     * Checks all autostart researches and starts any whose requirements
     * are met. Autostart researches with no item cost begin automatically;
     * those with costs are just flagged but not started (the player must
     * interact to pay costs).
     * <p>
     * Matches MineColonies behavior: called periodically (e.g., on building
     * upgrade, on research completion, on colony tick).
     *
     * @param level the server level
     */
    public void checkAutoStartResearch(ServerLevel level) {
        List<GlobalResearch> autostarts = GlobalResearchTree.INSTANCE.getAutostartResearches();

        for (GlobalResearch research : autostarts) {
            Identifier researchId = research.getId();

            // Skip already completed or in-progress research
            ResearchState state = localTree.getResearchState(researchId);
            if (state == ResearchState.FINISHED || state == ResearchState.IN_PROGRESS) {
                continue;
            }

            // Check requirements
            boolean requirementsMet = true;
            for (IResearchRequirement req : research.getRequirements()) {
                if (!req.isFulfilled(this)) {
                    requirementsMet = false;
                    break;
                }
            }
            if (!requirementsMet) continue;

            // Check parent research completed
            boolean parentsMet = true;
            for (Identifier parentId : research.getParentIds()) {
                if (!localTree.isResearchComplete(parentId)) {
                    parentsMet = false;
                    break;
                }
            }
            if (!parentsMet) continue;

            // Check onlyChild and blocking
            boolean blocked = false;
            for (Identifier parentId : research.getParentIds()) {
                GlobalResearch parent = GlobalResearchTree.INSTANCE.getResearch(parentId);
                if (parent != null && parent.hasOnlyChild() && parent.hasResearchedChild(localTree)) {
                    blocked = true;
                    break;
                }
            }
            if (blocked) continue;

            // Check tier-6 exclusivity
            if (research.getTier() >= 6 &&
                    GlobalResearchTree.INSTANCE.branchHasMaxTierResearch(research.getBranchId(), localTree)) {
                continue;
            }

            // If no costs, start automatically
            if (research.getCosts().isEmpty()) {
                localTree.startResearch(researchId, research.getResearchTime());
                if (research.isInstant()) {
                    completeResearch(researchId);
                }
                markDirty();
                MegaMod.LOGGER.info("Auto-started research: {}", researchId);
            }
            // If has costs, just log that it's available (UI should show notification)
        }
    }

    /**
     * Processes offline research progress. When a colony's university building
     * is level 3 or higher, research continues to progress while the player
     * is offline.
     * <p>
     * Called when the colony/faction loads (e.g., player logs in, chunk loads).
     * Calculates elapsed ticks since last save and applies them to in-progress
     * research.
     *
     * @param level      the server level
     * @param ticksElapsed the number of ticks since the last save/unload
     */
    public void processOfflineResearch(ServerLevel level, long ticksElapsed) {
        // Only process if university is level 3+
        int universityLevel = getBuildingLevel("university");
        if (universityLevel < 3) return;

        if (ticksElapsed <= 0) return;

        List<Identifier> justCompleted = new ArrayList<>();

        for (Identifier researchId : localTree.getInProgressResearches()) {
            boolean completed = localTree.progressResearch(researchId, (int) Math.min(ticksElapsed, Integer.MAX_VALUE));
            if (completed) {
                justCompleted.add(researchId);
            }
        }

        for (Identifier researchId : justCompleted) {
            completeResearch(researchId);
        }

        if (!justCompleted.isEmpty()) {
            markDirty();
            MegaMod.LOGGER.info("Processed {} offline research completion(s) for faction {}",
                    justCompleted.size(), factionId);
        }
    }

    // ---- Convenience queries ----

    /**
     * Returns true if the given research can be started right now.
     */
    public boolean canStartResearch(Identifier researchId, Player player) {
        GlobalResearch global = GlobalResearchTree.INSTANCE.getResearch(researchId);
        if (global == null) return false;

        ResearchState state = localTree.getResearchState(researchId);
        if (state != ResearchState.NOT_STARTED) return false;

        // Check blocking
        for (GlobalResearch other : GlobalResearchTree.INSTANCE.getAllResearches()) {
            if (localTree.isResearchComplete(other.getId())) {
                if (other.getBlocking().contains(researchId)) return false;
            }
        }

        // Check column 6 / tier-6 exclusivity
        if (global.getTier() >= 6) {
            if (GlobalResearchTree.INSTANCE.branchHasMaxTierResearch(global.getBranchId(), localTree)) {
                return false;
            }
        }

        // Check parent's onlyChild flag
        for (Identifier parentId : global.getParentIds()) {
            GlobalResearch parent = GlobalResearchTree.INSTANCE.getResearch(parentId);
            if (parent != null && parent.hasOnlyChild() && parent.hasResearchedChild(localTree)) {
                return false;
            }
        }

        for (IResearchRequirement req : global.getRequirements()) {
            if (!req.isFulfilled(this)) return false;
        }
        for (Identifier parentId : global.getParentIds()) {
            if (!localTree.isResearchComplete(parentId)) return false;
        }
        if (player != null) {
            for (IResearchCost cost : global.getCosts()) {
                if (!cost.canAfford(player)) return false;
            }
        }

        return true;
    }

    /**
     * Returns the number of researches currently in progress.
     */
    public int getActiveResearchCount() {
        return localTree.getInProgressResearches().size();
    }

    /**
     * Returns true if the given research should be visible in the UI.
     * Hidden researches are only visible when all their requirements are met.
     * Matches MineColonies' canDisplay logic.
     *
     * @param researchId the research to check
     * @param universityLevel the current university building level
     * @return true if the research should be shown
     */
    public boolean canDisplayResearch(Identifier researchId, int universityLevel) {
        GlobalResearch global = GlobalResearchTree.INSTANCE.getResearch(researchId);
        if (global == null) return false;

        // Already started or completed -- always show
        ResearchState state = localTree.getResearchState(researchId);
        if (state != ResearchState.NOT_STARTED) return true;

        // University level must be >= research tier for it to display
        if (universityLevel < global.getTier()) return false;

        // If hidden, only show when requirements are met
        if (global.isHidden()) {
            for (IResearchRequirement req : global.getRequirements()) {
                if (!req.isFulfilled(this)) return false;
            }
            for (Identifier parentId : global.getParentIds()) {
                if (!localTree.isResearchComplete(parentId)) return false;
            }
        }

        return true;
    }

    private void markDirty() {
        this.dirty = true;
    }

    // ---- Persistence ----

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.put("LocalTree", (Tag) localTree.toNbt());
        tag.put("Effects", (Tag) effectManager.toNbt());
        tag.putLong("LastSaveTimestamp", lastSaveTimestamp);
        return tag;
    }

    public static ResearchManager fromNbt(String factionId, CompoundTag tag) {
        LocalResearchTree tree = LocalResearchTree.fromNbt(tag.getCompoundOrEmpty("LocalTree"));
        ResearchEffectManager effects = ResearchEffectManager.fromNbt(tag.getCompoundOrEmpty("Effects"));
        long timestamp = tag.getLongOr("LastSaveTimestamp", 0L);
        return new ResearchManager(factionId, tree, effects, timestamp);
    }

    // ---- Result type ----

    /**
     * Result of attempting to start a research.
     */
    public static class StartResult {
        private final boolean success;
        private final String message;

        private StartResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }

        public Component getMessageComponent() {
            return Component.literal(message);
        }

        public static StartResult success() {
            return new StartResult(true, "Research started!");
        }

        public static StartResult failure(String reason) {
            return new StartResult(false, reason);
        }
    }
}
