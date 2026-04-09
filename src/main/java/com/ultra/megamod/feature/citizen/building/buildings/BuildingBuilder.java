package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.*;
import com.ultra.megamod.feature.citizen.data.CitizenJob;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/**
 * Builder's Hut building implementation.
 * Full MegaColonies parity: worker module, crafting module,
 * settings module (task assignment, construction strategy, shears, fill block,
 * recipe mode, pickup priority), and minimum stock tracking.
 *
 * The builder's level gates what level of buildings they can construct.
 */
public class BuildingBuilder extends AbstractBuilding {

    // Setting keys
    public static final String SETTING_TASK_MODE = "taskAssignmentMode";
    public static final String SETTING_CONSTRUCTION_STRATEGY = "constructionStrategy";
    public static final String SETTING_USE_SHEARS = "useShears";
    public static final String SETTING_FILL_BLOCK = "fillBlock";
    public static final String SETTING_RECIPE_MODE = "recipeMode";
    public static final String SETTING_PICKUP_PRIORITY = "pickupPriority";

    // Strategy values
    public static final String STRATEGY_DEFAULT = "default";
    public static final String STRATEGY_HILBERT = "hilbert";
    public static final String STRATEGY_INWARD_CIRCLE = "inward_circle";
    public static final String STRATEGY_RANDOM = "random";

    public static final List<String> STRATEGY_OPTIONS = List.of(
            STRATEGY_DEFAULT, STRATEGY_HILBERT, STRATEGY_INWARD_CIRCLE, STRATEGY_RANDOM
    );

    // Task mode values
    public static final String TASK_MODE_AUTO = "automatic";
    public static final String TASK_MODE_MANUAL = "manual";

    // Recipe mode values
    public static final String RECIPE_MODE_PRIORITY = "priority";
    public static final String RECIPE_MODE_WAREHOUSE_STOCK = "warehouse_stock";

    @Override
    public String getBuildingId() {
        return "builder";
    }

    @Override
    public String getDisplayName() {
        return "Builder's Hut";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    /**
     * Returns the maximum building level this builder can construct.
     * Equal to this building's own level -- a level 3 builder can build
     * structures up to level 3.
     *
     * @return the max buildable level
     */
    public int getMaxBuildableLevel() {
        return getBuildingLevel();
    }

    @Override
    protected void registerModules() {
        // Worker module: 1 builder worker
        addModule(new WorkerBuildingModule(CitizenJob.BUILDER, 1));

        // Crafting module: builders can learn recipes for custom builds
        addModule(new CraftingBuildingModule("builder", 10));

        // Settings module with all builder-specific settings
        addModule(new BuilderSettingsModule());

        // Minimum stock module for tracking required materials
        addModule(new BuilderMinimumStockModule());
    }

    // =====================================================================
    // Inner module: Settings
    // =====================================================================

    /**
     * Settings module for the Builder's Hut.
     * Persists all configurable builder settings to NBT.
     */
    public static class BuilderSettingsModule implements ISettingsModule, IPersistentModule {

        private final StringSetting taskMode = new StringSetting(SETTING_TASK_MODE, TASK_MODE_AUTO,
                List.of(TASK_MODE_AUTO, TASK_MODE_MANUAL));
        private final StringSetting constructionStrategy = new StringSetting(SETTING_CONSTRUCTION_STRATEGY,
                STRATEGY_DEFAULT, STRATEGY_OPTIONS);
        private final BoolSetting useShears = new BoolSetting(SETTING_USE_SHEARS, false);
        private final StringSetting fillBlock = new StringSetting(SETTING_FILL_BLOCK, "minecraft:dirt");
        private final StringSetting recipeMode = new StringSetting(SETTING_RECIPE_MODE, RECIPE_MODE_PRIORITY,
                List.of(RECIPE_MODE_PRIORITY, RECIPE_MODE_WAREHOUSE_STOCK));
        private final IntSetting pickupPriority = new IntSetting(SETTING_PICKUP_PRIORITY, 5, 1, 10);

        private final ISetting<?>[] allSettings = {
                taskMode, constructionStrategy, useShears, fillBlock, recipeMode, pickupPriority
        };

        @Override
        public String getModuleId() {
            return "settings";
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getSetting(String key, T defaultValue) {
            for (ISetting<?> setting : allSettings) {
                if (setting.getKey().equals(key)) {
                    Object val = setting.getValue();
                    if (val != null) {
                        try {
                            return (T) val;
                        } catch (ClassCastException e) {
                            return defaultValue;
                        }
                    }
                    return defaultValue;
                }
            }
            return defaultValue;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> void setSetting(String key, T value) {
            for (ISetting<?> setting : allSettings) {
                if (setting.getKey().equals(key)) {
                    ((ISetting<T>) setting).setValue(value);
                    return;
                }
            }
        }

        @Override
        public java.util.Map<String, Object> getAllSettings() {
            java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
            for (ISetting<?> setting : allSettings) {
                map.put(setting.getKey(), setting.getValue());
            }
            return java.util.Collections.unmodifiableMap(map);
        }

        @Override
        public void onBuildingLoad(net.minecraft.nbt.CompoundTag tag) {
            net.minecraft.nbt.CompoundTag moduleTag = tag.getCompoundOrEmpty(getModuleId());
            for (ISetting<?> setting : allSettings) {
                setting.loadFromNbt(moduleTag);
            }
        }

        @Override
        public void onBuildingSave(net.minecraft.nbt.CompoundTag tag) {
            net.minecraft.nbt.CompoundTag moduleTag = new net.minecraft.nbt.CompoundTag();
            for (ISetting<?> setting : allSettings) {
                setting.saveToNbt(moduleTag);
            }
            tag.put(getModuleId(), moduleTag);
        }

        @Override
        public void onBuildingTick(net.minecraft.world.level.Level level) {
            // Settings don't tick
        }
    }

    // =====================================================================
    // Inner module: MinimumStock
    // =====================================================================

    /**
     * Minimum stock tracking module for the Builder's Hut.
     * Tracks items and their minimum required counts.
     */
    public static class BuilderMinimumStockModule implements IMinimumStockModule, IPersistentModule {

        private final java.util.Map<String, Integer> minimumStocks = new java.util.LinkedHashMap<>();

        @Override
        public String getModuleId() {
            return "minimum_stock";
        }

        @Override
        public java.util.Map<net.minecraft.world.item.ItemStack, Integer> getMinimumStock() {
            java.util.Map<net.minecraft.world.item.ItemStack, Integer> result = new java.util.LinkedHashMap<>();
            for (java.util.Map.Entry<String, Integer> entry : minimumStocks.entrySet()) {
                net.minecraft.resources.Identifier id = net.minecraft.resources.Identifier.tryParse(entry.getKey());
                if (id != null) {
                    net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(id);
                    if (item != null && item != net.minecraft.world.item.Items.AIR) {
                        result.put(new net.minecraft.world.item.ItemStack(item), entry.getValue());
                    }
                }
            }
            return java.util.Collections.unmodifiableMap(result);
        }

        @Override
        public void setMinimumStock(net.minecraft.world.item.ItemStack item, int count) {
            net.minecraft.resources.Identifier id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item.getItem());
            if (id != null) {
                minimumStocks.put(id.toString(), count);
            }
        }

        @Override
        public void removeMinimumStock(net.minecraft.world.item.ItemStack item) {
            net.minecraft.resources.Identifier id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item.getItem());
            if (id != null) {
                minimumStocks.remove(id.toString());
            }
        }

        @Override
        public void onBuildingLoad(net.minecraft.nbt.CompoundTag tag) {
            net.minecraft.nbt.CompoundTag moduleTag = tag.getCompoundOrEmpty(getModuleId());
            minimumStocks.clear();
            for (String key : moduleTag.keySet()) {
                minimumStocks.put(key, moduleTag.getIntOr(key, 0));
            }
        }

        @Override
        public void onBuildingSave(net.minecraft.nbt.CompoundTag tag) {
            net.minecraft.nbt.CompoundTag moduleTag = new net.minecraft.nbt.CompoundTag();
            for (java.util.Map.Entry<String, Integer> entry : minimumStocks.entrySet()) {
                moduleTag.putInt(entry.getKey(), entry.getValue());
            }
            tag.put(getModuleId(), moduleTag);
        }

        @Override
        public void onBuildingTick(net.minecraft.world.level.Level level) {
            // Minimum stock doesn't tick
        }
    }
}
