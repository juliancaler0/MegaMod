package com.ultra.megamod.feature.citizen.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.*;

/**
 * Tracks all active research effects for a colony. Maintains multiplier
 * stacks, flat modifier stacks, and a set of unlocked features.
 * <p>
 * Multipliers are combined multiplicatively: if two researches give
 * worker_speed 1.1 and 1.15, the final multiplier is 1.1 * 1.15 = 1.265.
 * <p>
 * Modifiers are combined additively: if two researches give +2 and +3
 * to max_citizens, the total modifier is 5.
 * <p>
 * Unlocked features are a simple set of strings checked with
 * {@link #isUnlocked(String)}.
 */
public class ResearchEffectManager {

    // stat -> list of multiplier values (each research adds one entry)
    private final Map<String, List<Double>> multiplierStacks = new HashMap<>();
    // stat -> list of modifier values
    private final Map<String, List<Double>> modifierStacks = new HashMap<>();
    // features unlocked by research
    private final Set<String> unlockedFeatures = new LinkedHashSet<>();

    // Cached computed values (invalidated on change)
    private final Map<String, Double> cachedMultipliers = new HashMap<>();
    private final Map<String, Double> cachedModifiers = new HashMap<>();
    private boolean cacheValid = false;

    // ---- Multiplier operations ----

    /**
     * Applies a multiplier to a stat. Multiple multipliers on the same stat
     * are combined multiplicatively.
     *
     * @param stat       the stat key (e.g., "worker_speed", "block_break_speed")
     * @param multiplier the multiplier value (e.g., 1.15 for +15%)
     */
    public void applyMultiplier(String stat, double multiplier) {
        multiplierStacks.computeIfAbsent(stat, k -> new ArrayList<>()).add(multiplier);
        invalidateCache();
    }

    /**
     * Removes a specific multiplier value from a stat. Removes only the first
     * occurrence matching the given value.
     */
    public void removeMultiplier(String stat, double multiplier) {
        List<Double> stack = multiplierStacks.get(stat);
        if (stack != null) {
            stack.remove(multiplier);
            if (stack.isEmpty()) multiplierStacks.remove(stat);
        }
        invalidateCache();
    }

    /**
     * Returns the combined multiplier for a stat. Defaults to 1.0 if no
     * multipliers are active.
     */
    public double getMultiplier(String stat) {
        ensureCache();
        return cachedMultipliers.getOrDefault(stat, 1.0);
    }

    // ---- Modifier operations ----

    /**
     * Applies a flat modifier to a stat. Multiple modifiers on the same stat
     * are combined additively.
     *
     * @param stat  the stat key (e.g., "max_citizens", "armor_toughness")
     * @param value the modifier value (positive or negative)
     */
    public void applyModifier(String stat, double value) {
        modifierStacks.computeIfAbsent(stat, k -> new ArrayList<>()).add(value);
        invalidateCache();
    }

    /**
     * Removes a specific modifier value from a stat. Removes only the first
     * occurrence matching the given value.
     */
    public void removeModifier(String stat, double value) {
        List<Double> stack = modifierStacks.get(stat);
        if (stack != null) {
            stack.remove(value);
            if (stack.isEmpty()) modifierStacks.remove(stat);
        }
        invalidateCache();
    }

    /**
     * Returns the combined flat modifier for a stat. Defaults to 0.0 if no
     * modifiers are active.
     */
    public double getModifier(String stat) {
        ensureCache();
        return cachedModifiers.getOrDefault(stat, 0.0);
    }

    // ---- Unlock operations ----

    /**
     * Unlocks a feature.
     *
     * @param feature the feature key (e.g., "deep_mining", "plate_armor")
     */
    public void unlock(String feature) {
        unlockedFeatures.add(feature);
    }

    /**
     * Revokes (removes) an unlocked feature.
     */
    public void revoke(String feature) {
        unlockedFeatures.remove(feature);
    }

    /**
     * Returns true if the given feature is unlocked.
     */
    public boolean isUnlocked(String feature) {
        return unlockedFeatures.contains(feature);
    }

    /**
     * Returns a copy of all unlocked features.
     */
    public Set<String> getUnlockedFeatures() {
        return Collections.unmodifiableSet(unlockedFeatures);
    }

    // ---- Recompute from scratch ----

    /**
     * Clears all effects and recomputes them from the colony's completed
     * research. Should be called when research completes, or when loading.
     *
     * @param localTree the colony's research tree
     */
    public void recomputeFromTree(LocalResearchTree localTree) {
        multiplierStacks.clear();
        modifierStacks.clear();
        unlockedFeatures.clear();
        invalidateCache();

        for (LocalResearch local : localTree.getAllProgress()) {
            if (local.getState() != ResearchState.FINISHED) continue;

            GlobalResearch global = GlobalResearchTree.INSTANCE.getResearch(local.getResearchId());
            if (global == null) continue;

            for (IResearchEffect effect : global.getEffects()) {
                effect.apply(this);
            }
        }
    }

    // ---- Cache ----

    private void invalidateCache() {
        cacheValid = false;
        cachedMultipliers.clear();
        cachedModifiers.clear();
    }

    private void ensureCache() {
        if (cacheValid) return;

        // Compute combined multipliers
        for (Map.Entry<String, List<Double>> entry : multiplierStacks.entrySet()) {
            double combined = 1.0;
            for (double v : entry.getValue()) {
                combined *= v;
            }
            cachedMultipliers.put(entry.getKey(), combined);
        }

        // Compute combined modifiers
        for (Map.Entry<String, List<Double>> entry : modifierStacks.entrySet()) {
            double combined = 0.0;
            for (double v : entry.getValue()) {
                combined += v;
            }
            cachedModifiers.put(entry.getKey(), combined);
        }

        cacheValid = true;
    }

    // ---- Persistence ----

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();

        // Multiplier stacks
        CompoundTag multTag = new CompoundTag();
        for (Map.Entry<String, List<Double>> entry : multiplierStacks.entrySet()) {
            ListTag values = new ListTag();
            CompoundTag wrapper = new CompoundTag();
            for (int i = 0; i < entry.getValue().size(); i++) {
                wrapper.putDouble("v" + i, entry.getValue().get(i));
            }
            wrapper.putInt("Count", entry.getValue().size());
            multTag.put(entry.getKey(), wrapper);
        }
        tag.put("Multipliers", (Tag) multTag);

        // Modifier stacks
        CompoundTag modTag = new CompoundTag();
        for (Map.Entry<String, List<Double>> entry : modifierStacks.entrySet()) {
            CompoundTag wrapper = new CompoundTag();
            for (int i = 0; i < entry.getValue().size(); i++) {
                wrapper.putDouble("v" + i, entry.getValue().get(i));
            }
            wrapper.putInt("Count", entry.getValue().size());
            modTag.put(entry.getKey(), wrapper);
        }
        tag.put("Modifiers", (Tag) modTag);

        // Unlocked features
        ListTag unlockList = new ListTag();
        for (String feature : unlockedFeatures) {
            unlockList.add(StringTag.valueOf(feature));
        }
        tag.put("Unlocked", (Tag) unlockList);

        return tag;
    }

    public static ResearchEffectManager fromNbt(CompoundTag tag) {
        ResearchEffectManager manager = new ResearchEffectManager();

        // Multiplier stacks
        CompoundTag multTag = tag.getCompoundOrEmpty("Multipliers");
        for (String key : multTag.keySet()) {
            CompoundTag wrapper = multTag.getCompoundOrEmpty(key);
            int count = wrapper.getIntOr("Count", 0);
            List<Double> values = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                values.add(wrapper.getDoubleOr("v" + i, 1.0));
            }
            if (!values.isEmpty()) {
                manager.multiplierStacks.put(key, values);
            }
        }

        // Modifier stacks
        CompoundTag modTag = tag.getCompoundOrEmpty("Modifiers");
        for (String key : modTag.keySet()) {
            CompoundTag wrapper = modTag.getCompoundOrEmpty(key);
            int count = wrapper.getIntOr("Count", 0);
            List<Double> values = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                values.add(wrapper.getDoubleOr("v" + i, 0.0));
            }
            if (!values.isEmpty()) {
                manager.modifierStacks.put(key, values);
            }
        }

        // Unlocked features
        ListTag unlockList = tag.getListOrEmpty("Unlocked");
        for (int i = 0; i < unlockList.size(); i++) {
            String feature = unlockList.getStringOr(i, "");
            if (!feature.isEmpty()) {
                manager.unlockedFeatures.add(feature);
            }
        }

        return manager;
    }
}
