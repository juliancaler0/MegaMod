package com.ultra.megamod.feature.citizen.building;

import com.ultra.megamod.feature.citizen.building.module.IBuildingModule;
import com.ultra.megamod.feature.citizen.building.module.IPersistentModule;
import com.ultra.megamod.feature.citizen.building.module.ITickingModule;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Base class for all colony buildings.
 * Each building has a position, level, style, and a composable list of modules
 * that define its behavior (jobs, crafting, housing, etc.).
 * Concrete subclasses (e.g., BuildingResidence, BuildingBaker) override
 * {@link #getBuildingId()} and {@link #getDisplayName()} and add their modules.
 */
public abstract class AbstractBuilding implements ISchematicProvider {

    protected BlockPos position = BlockPos.ZERO;
    protected int level = 0;
    protected String style = "colonial";
    protected String customName = "";
    protected boolean isBuilt = false;
    protected boolean needsRepair = false;
    protected UUID colonyId;
    protected boolean dirty = false;

    private final List<IBuildingModule> modules = new ArrayList<>();

    // ==================== Abstract Methods ====================

    /**
     * Returns the unique building type ID (e.g., "residence", "baker", "miner").
     *
     * @return the building ID
     */
    public abstract String getBuildingId();

    /**
     * Returns the human-readable display name for this building.
     *
     * @return the display name
     */
    public abstract String getDisplayName();

    /**
     * Called during construction to register this building's modules.
     * Subclasses override this to add their specific modules
     * (e.g., WorkerBuildingModule, ResidenceBuildingModule).
     * <p>
     * This method is intended to be called once after the building is created
     * or loaded, to populate the module list before ticking begins.
     */
    protected void registerModules() {
        // Default: no modules. Subclasses override to add their modules.
    }

    /**
     * Public entry point to initialize modules on a building instance.
     * Call this after constructing a building via its factory to ensure
     * all modules are registered before querying them.
     */
    public void registerModulesPublic() {
        if (getAllModules().isEmpty()) {
            registerModules();
        }
    }

    // ==================== ISchematicProvider ====================

    @Override
    public String getSchematicName() {
        return getBuildingId();
    }

    @Override
    public int getBuildingLevel() {
        return level;
    }

    @Override
    public void setBuildingLevel(int level) {
        this.level = level;
        markDirty();
    }

    @Override
    public String getStyle() {
        return style;
    }

    @Override
    public BlockPos getPosition() {
        return position;
    }

    // ==================== Module Management ====================

    /**
     * Adds a module to this building. Modules define building behavior
     * (jobs, crafting, housing, ticking logic, etc.).
     *
     * @param module the module to add
     */
    public void addModule(IBuildingModule module) {
        modules.add(module);
    }

    /**
     * Finds the first module of the given type.
     *
     * @param type the module class to search for
     * @param <T>  the module type
     * @return an Optional containing the module, or empty if not found
     */
    public <T extends IBuildingModule> Optional<T> getModule(Class<T> type) {
        for (IBuildingModule module : modules) {
            if (type.isInstance(module)) {
                return Optional.of(type.cast(module));
            }
        }
        return Optional.empty();
    }

    /**
     * Finds all modules of the given type.
     *
     * @param type the module class to search for
     * @param <T>  the module type
     * @return a list of matching modules (may be empty)
     */
    public <T extends IBuildingModule> List<T> getModules(Class<T> type) {
        List<T> result = new ArrayList<>();
        for (IBuildingModule module : modules) {
            if (type.isInstance(module)) {
                result.add(type.cast(module));
            }
        }
        return result;
    }

    /**
     * Returns all modules attached to this building.
     *
     * @return unmodifiable view of modules
     */
    public List<IBuildingModule> getAllModules() {
        return List.copyOf(modules);
    }

    // ==================== Lifecycle ====================

    /**
     * Called each game tick while the building is active.
     * Delegates to all {@link ITickingModule} modules.
     *
     * @param level the server level
     */
    public void onTick(Level level) {
        for (IBuildingModule module : modules) {
            if (module instanceof ITickingModule ticking) {
                ticking.tick(level);
            }
        }
    }

    // ==================== Building State ====================

    public boolean canUpgrade() {
        return level < getMaxLevel();
    }

    public int getMaxLevel() {
        return 5;
    }

    public void upgrade() {
        if (canUpgrade()) {
            level++;
            markDirty();
        }
    }

    public boolean isBuilt() {
        return isBuilt;
    }

    public void setBuilt(boolean built) {
        this.isBuilt = built;
        markDirty();
    }

    public boolean needsRepair() {
        return needsRepair;
    }

    public void setNeedsRepair(boolean needsRepair) {
        this.needsRepair = needsRepair;
        markDirty();
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
        markDirty();
    }

    public void setStyle(String style) {
        this.style = style;
        markDirty();
    }

    public void setPosition(BlockPos position) {
        this.position = position;
    }

    public UUID getColonyId() {
        return colonyId;
    }

    public void setColonyId(UUID colonyId) {
        this.colonyId = colonyId;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void clearDirty() {
        this.dirty = false;
    }

    // ==================== Persistence ====================

    /**
     * Saves this building's state to a CompoundTag.
     * Modules implementing {@link IPersistentModule} have their data saved
     * under a sub-tag keyed by their module ID.
     *
     * @return the saved tag
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("BuildingId", getBuildingId());
        tag.putInt("PosX", position.getX());
        tag.putInt("PosY", position.getY());
        tag.putInt("PosZ", position.getZ());
        tag.putInt("Level", level);
        tag.putString("Style", style);
        tag.putString("CustomName", customName);
        tag.putBoolean("IsBuilt", isBuilt);
        tag.putBoolean("NeedsRepair", needsRepair);
        if (colonyId != null) {
            tag.putString("ColonyId", colonyId.toString());
        }

        // Save persistent modules
        CompoundTag modulesTag = new CompoundTag();
        for (IBuildingModule module : modules) {
            if (module instanceof IPersistentModule) {
                CompoundTag moduleTag = new CompoundTag();
                module.onBuildingSave(moduleTag);
                modulesTag.put(module.getModuleId(), moduleTag);
            }
        }
        tag.put("Modules", modulesTag);

        return tag;
    }

    /**
     * Loads this building's state from a CompoundTag.
     * Persistent modules are loaded from sub-tags keyed by their module ID.
     *
     * @param tag the tag to load from
     */
    public void load(CompoundTag tag) {
        position = new BlockPos(
                tag.getIntOr("PosX", 0),
                tag.getIntOr("PosY", 0),
                tag.getIntOr("PosZ", 0)
        );
        level = tag.getIntOr("Level", 0);
        style = tag.getStringOr("Style", "colonial");
        customName = tag.getStringOr("CustomName", "");
        isBuilt = tag.getBooleanOr("IsBuilt", false);
        needsRepair = tag.getBooleanOr("NeedsRepair", false);

        String colonyIdStr = tag.getStringOr("ColonyId", "");
        if (!colonyIdStr.isEmpty()) {
            try {
                colonyId = UUID.fromString(colonyIdStr);
            } catch (IllegalArgumentException ignored) {
                colonyId = null;
            }
        }

        // Load persistent modules
        CompoundTag modulesTag = tag.getCompoundOrEmpty("Modules");
        for (IBuildingModule module : modules) {
            if (module instanceof IPersistentModule) {
                CompoundTag moduleTag = modulesTag.getCompoundOrEmpty(module.getModuleId());
                module.onBuildingLoad(moduleTag);
            }
        }
    }
}
