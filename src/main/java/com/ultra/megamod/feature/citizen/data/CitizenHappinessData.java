package com.ultra.megamod.feature.citizen.data;

import net.minecraft.nbt.CompoundTag;

import java.util.EnumMap;
import java.util.Map;

/**
 * Implements the MegaColonies happiness system with static and time-based modifiers.
 *
 * <h2>Static Modifiers</h2>
 * <ul>
 *   <li><b>School</b> (weight 1.0): requires school level >= 1</li>
 *   <li><b>Mystical Site</b> (weight 1.0): max(1, mysticalSiteMaxLevel / 2.0)</li>
 *   <li><b>Security</b> (weight 4.0): min(guards / (workers * 2/3), 2)</li>
 *   <li><b>Social</b> (weight 2.0): (Total - Unemployed - Homeless - Sick - Hungry) / Total</li>
 *   <li><b>Food</b> (weight 3.0): avg(uniqueFoods/homeLevel, mcFoods/max(1,homeLevel-2)) capped 5</li>
 * </ul>
 *
 * <h2>Time-Based Modifiers</h2>
 * <ul>
 *   <li><b>Homelessness</b> (weight 3.0): degrades over days without a home</li>
 *   <li><b>Unemployment</b> (weight 2.0): degrades over days without a job</li>
 *   <li><b>Health/Disease</b> (weight 2.0): degrades while sick</li>
 *   <li><b>Idle at Job</b> (weight 1.0): degrades when worker is idle</li>
 *   <li><b>Sleep</b> (weight 1.5): progressive degradation without sleep</li>
 * </ul>
 *
 * <h2>Formula</h2>
 * <pre>
 *   totalHappiness = sum(factor * weight) / totalWeight
 *   finalHappiness = totalHappiness * (1 + researchLeveling) * 10
 *   capped at 10.0
 * </pre>
 */
public class CitizenHappinessData {

    // ---- Modifier Types ----

    public enum HappinessModifier {
        // Static modifiers
        SCHOOL("School", 1.0, false),
        MYSTICAL_SITE("Mystical Site", 1.0, false),
        SECURITY("Security", 4.0, false),
        SOCIAL("Social", 2.0, false),
        FOOD("Food", 3.0, false),

        // Time-based modifiers
        HOMELESSNESS("Homelessness", 3.0, true),
        UNEMPLOYMENT("Unemployment", 2.0, true),
        HEALTH("Health", 2.0, true),
        IDLE_AT_JOB("Idle at Job", 1.0, true),
        SLEEP("Sleep", 1.5, true);

        private final String displayName;
        private final double weight;
        private final boolean timeBased;

        HappinessModifier(String displayName, double weight, boolean timeBased) {
            this.displayName = displayName;
            this.weight = weight;
            this.timeBased = timeBased;
        }

        public String getDisplayName() { return displayName; }
        public double getWeight() { return weight; }
        public boolean isTimeBased() { return timeBased; }
    }

    // ---- State ----

    /** Current factor value for each modifier (0.0 to 2.0, where 1.0 is neutral). */
    private final Map<HappinessModifier, Double> factors = new EnumMap<>(HappinessModifier.class);

    /** Days counter for time-based modifier degradation. */
    private final Map<HappinessModifier, Integer> degradationDays = new EnumMap<>(HappinessModifier.class);

    /** Cached final happiness (recalculated when dirty). */
    private double cachedHappiness = 5.0;
    private boolean dirty = true;

    /** Research bonus multiplier (0.0 = no research, 0.1 = 10% bonus, etc.) */
    private double researchLeveling = 0.0;

    public CitizenHappinessData() {
        // Initialize all factors to neutral (1.0)
        for (HappinessModifier mod : HappinessModifier.values()) {
            factors.put(mod, 1.0);
            if (mod.isTimeBased()) {
                degradationDays.put(mod, 0);
            }
        }
    }

    // =====================================================================
    //  Static Modifier Calculation
    // =====================================================================

    /**
     * Updates the School happiness modifier.
     *
     * @param schoolLevel the level of the colony's school building (0 if no school)
     */
    public void updateSchool(int schoolLevel) {
        double factor = schoolLevel >= 1 ? 1.0 : 0.0;
        setFactor(HappinessModifier.SCHOOL, factor);
    }

    /**
     * Updates the Mystical Site happiness modifier.
     *
     * @param mysticalSiteMaxLevel the max level among all mystical sites in the colony
     */
    public void updateMysticalSite(int mysticalSiteMaxLevel) {
        double factor = Math.max(1.0, mysticalSiteMaxLevel / 2.0);
        setFactor(HappinessModifier.MYSTICAL_SITE, factor);
    }

    /**
     * Updates the Security happiness modifier.
     *
     * @param guards  total number of guard/recruit citizens in the colony
     * @param workers total number of worker citizens in the colony
     */
    public void updateSecurity(int guards, int workers) {
        double requiredGuards = workers * (2.0 / 3.0);
        double factor;
        if (requiredGuards <= 0) {
            factor = 2.0; // No workers means no security concern
        } else {
            factor = Math.min((double) guards / requiredGuards, 2.0);
        }
        setFactor(HappinessModifier.SECURITY, factor);
    }

    /**
     * Updates the Social happiness modifier.
     *
     * @param totalCitizens total number of citizens in the colony
     * @param unemployed    number of unemployed citizens
     * @param homeless      number of homeless citizens
     * @param sick          number of sick citizens
     * @param hungry        number of hungry citizens (hunger below threshold)
     */
    public void updateSocial(int totalCitizens, int unemployed, int homeless, int sick, int hungry) {
        if (totalCitizens <= 0) {
            setFactor(HappinessModifier.SOCIAL, 1.0);
            return;
        }
        int satisfied = totalCitizens - unemployed - homeless - sick - hungry;
        double factor = Math.max(0.0, (double) satisfied / totalCitizens);
        setFactor(HappinessModifier.SOCIAL, factor);
    }

    /**
     * Updates the Food happiness modifier based on food diversity.
     *
     * @param uniqueFoods number of unique food types available in the colony
     * @param mcFoods     number of Minecraft-recognized food types (cooked meats, bread, etc.)
     * @param homeLevel   the citizen's home building level (used as divisor)
     */
    public void updateFood(int uniqueFoods, int mcFoods, int homeLevel) {
        double foodDiversity = homeLevel > 0 ? (double) uniqueFoods / homeLevel : uniqueFoods;
        double mcFoodRatio = (double) mcFoods / Math.max(1, homeLevel - 2);
        double avg = (foodDiversity + mcFoodRatio) / 2.0;
        double factor = Math.min(5.0, avg);
        setFactor(HappinessModifier.FOOD, factor);
    }

    // =====================================================================
    //  Time-Based Modifier Updates
    // =====================================================================

    /**
     * Called once per Minecraft day to advance time-based degradation.
     * Each modifier degrades by 0.1 per day if the negative condition persists.
     *
     * @param isHomeless   true if the citizen has no assigned bed/home
     * @param isUnemployed true if the citizen has no assigned job
     * @param isSick       true if the citizen currently has a disease
     * @param isIdleAtJob  true if the citizen's status is IDLE while having a job
     * @param sleptLastNight true if the citizen successfully slept last night
     */
    public void tickDay(boolean isHomeless, boolean isUnemployed, boolean isSick,
                        boolean isIdleAtJob, boolean sleptLastNight) {
        tickTimeBased(HappinessModifier.HOMELESSNESS, isHomeless);
        tickTimeBased(HappinessModifier.UNEMPLOYMENT, isUnemployed);
        tickTimeBased(HappinessModifier.HEALTH, isSick);
        tickTimeBased(HappinessModifier.IDLE_AT_JOB, isIdleAtJob);
        tickTimeBased(HappinessModifier.SLEEP, !sleptLastNight);
    }

    /**
     * Degrades or recovers a time-based modifier.
     * When the negative condition is active: degrades by 0.1 per day (minimum 0.0).
     * When the condition is resolved: recovers by 0.2 per day (maximum 1.0).
     */
    private void tickTimeBased(HappinessModifier modifier, boolean negativeCondition) {
        double current = factors.getOrDefault(modifier, 1.0);
        int days = degradationDays.getOrDefault(modifier, 0);

        if (negativeCondition) {
            days++;
            // Progressive degradation: starts slow, accelerates
            // Day 1-3: -0.1/day, Day 4-7: -0.15/day, Day 8+: -0.2/day
            double degradation;
            if (days <= 3) {
                degradation = 0.1;
            } else if (days <= 7) {
                degradation = 0.15;
            } else {
                degradation = 0.2;
            }
            current = Math.max(0.0, current - degradation);
        } else {
            // Recovery: +0.2 per day when condition is resolved
            days = Math.max(0, days - 1);
            current = Math.min(1.0, current + 0.2);
        }

        degradationDays.put(modifier, days);
        setFactor(modifier, current);
    }

    /**
     * Resets a time-based modifier to neutral (e.g., when citizen gets a home/job).
     */
    public void resetModifier(HappinessModifier modifier) {
        factors.put(modifier, 1.0);
        if (modifier.isTimeBased()) {
            degradationDays.put(modifier, 0);
        }
        dirty = true;
    }

    // =====================================================================
    //  Happiness Calculation
    // =====================================================================

    /**
     * Calculates and returns the final happiness score (0.0 to 10.0).
     *
     * <p>Formula:
     * <pre>
     *   totalHappiness = sum(factor * weight) / totalWeight
     *   finalHappiness = totalHappiness * (1 + researchLeveling) * 10
     *   capped at 10.0
     * </pre>
     */
    public double calculateHappiness() {
        if (!dirty) return cachedHappiness;

        double weightedSum = 0.0;
        double totalWeight = 0.0;

        for (HappinessModifier modifier : HappinessModifier.values()) {
            double factor = factors.getOrDefault(modifier, 1.0);
            double weight = modifier.getWeight();
            weightedSum += factor * weight;
            totalWeight += weight;
        }

        double totalHappiness = totalWeight > 0 ? weightedSum / totalWeight : 1.0;
        double finalHappiness = totalHappiness * (1.0 + researchLeveling) * 10.0;
        cachedHappiness = Math.min(10.0, Math.max(0.0, finalHappiness));
        dirty = false;
        return cachedHappiness;
    }

    /**
     * Returns the current happiness without recalculating.
     */
    public double getHappiness() {
        if (dirty) return calculateHappiness();
        return cachedHappiness;
    }

    /**
     * Returns a breakdown of all modifiers with their current factor and weight.
     * Useful for UI display (e.g., tooltip or colony screen).
     *
     * @return map of modifier -> [factor, weight, contribution]
     */
    public Map<HappinessModifier, double[]> getModifierBreakdown() {
        Map<HappinessModifier, double[]> breakdown = new EnumMap<>(HappinessModifier.class);
        double totalWeight = 0.0;
        for (HappinessModifier mod : HappinessModifier.values()) {
            totalWeight += mod.getWeight();
        }
        for (HappinessModifier mod : HappinessModifier.values()) {
            double factor = factors.getOrDefault(mod, 1.0);
            double weight = mod.getWeight();
            double contribution = totalWeight > 0 ? (factor * weight) / totalWeight : 0.0;
            breakdown.put(mod, new double[]{factor, weight, contribution});
        }
        return breakdown;
    }

    /**
     * Returns a happiness category string based on the current score.
     */
    public String getHappinessCategory() {
        double h = getHappiness();
        if (h >= 8.0) return "Ecstatic";
        if (h >= 6.0) return "Happy";
        if (h >= 4.0) return "Content";
        if (h >= 2.0) return "Unhappy";
        return "Miserable";
    }

    // =====================================================================
    //  Setters
    // =====================================================================

    public void setResearchLeveling(double researchLeveling) {
        this.researchLeveling = Math.max(0.0, researchLeveling);
        dirty = true;
    }

    public double getResearchLeveling() {
        return researchLeveling;
    }

    private void setFactor(HappinessModifier modifier, double factor) {
        factors.put(modifier, factor);
        dirty = true;
    }

    /**
     * Returns the raw factor value for a specific modifier.
     */
    public double getFactor(HappinessModifier modifier) {
        return factors.getOrDefault(modifier, 1.0);
    }

    // =====================================================================
    //  NBT Persistence
    // =====================================================================

    /**
     * Saves happiness data to NBT.
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("researchLeveling", researchLeveling);

        CompoundTag factorsTag = new CompoundTag();
        for (HappinessModifier mod : HappinessModifier.values()) {
            factorsTag.putDouble(mod.name(), factors.getOrDefault(mod, 1.0));
        }
        tag.put("factors", factorsTag);

        CompoundTag daysTag = new CompoundTag();
        for (HappinessModifier mod : HappinessModifier.values()) {
            if (mod.isTimeBased()) {
                daysTag.putInt(mod.name(), degradationDays.getOrDefault(mod, 0));
            }
        }
        tag.put("degradationDays", daysTag);

        return tag;
    }

    /**
     * Loads happiness data from NBT.
     */
    public void load(CompoundTag tag) {
        if (tag == null) return;

        researchLeveling = tag.getDoubleOr("researchLeveling", 0.0);

        CompoundTag factorsTag = tag.getCompoundOrEmpty("factors");
        for (HappinessModifier mod : HappinessModifier.values()) {
            factors.put(mod, factorsTag.getDoubleOr(mod.name(), 1.0));
        }

        CompoundTag daysTag = tag.getCompoundOrEmpty("degradationDays");
        for (HappinessModifier mod : HappinessModifier.values()) {
            if (mod.isTimeBased()) {
                degradationDays.put(mod, daysTag.getIntOr(mod.name(), 0));
            }
        }

        dirty = true;
    }

    @Override
    public String toString() {
        return String.format("CitizenHappinessData{happiness=%.1f, category=%s, research=%.2f}",
                getHappiness(), getHappinessCategory(), researchLeveling);
    }
}
