package com.ultra.megamod.feature.citizen.entity.mc.handlers;

import net.minecraft.nbt.CompoundTag;

import java.util.*;

/**
 * Handles citizen happiness tracking for the MC citizen.
 * Ported from MineColonies' ICitizenHappinessHandler concept.
 */
public class CitizenHappinessHandler implements ICitizenHappinessHandler {

    private static final String TAG_MODIFIERS = "HappinessModifiers";
    private static final double BASE_HAPPINESS = 5.0;

    /**
     * Internal modifier record.
     */
    private static class HappinessModifier {
        final String name;
        double value;
        int ticksRemaining; // 0 = permanent

        HappinessModifier(String name, double value, int duration) {
            this.name = name;
            this.value = value;
            this.ticksRemaining = duration;
        }
    }

    private final Map<String, HappinessModifier> modifiers = new LinkedHashMap<>();

    @Override
    public void addModifier(String name, double value, int duration) {
        modifiers.put(name, new HappinessModifier(name, value, duration));
    }

    @Override
    public void resetModifier(String name) {
        modifiers.remove(name);
    }

    @Override
    public double getHappiness() {
        double happiness = BASE_HAPPINESS;
        for (HappinessModifier mod : modifiers.values()) {
            happiness += mod.value;
        }
        return Math.max(0.0, Math.min(10.0, happiness));
    }

    @Override
    public void processDailyHappiness() {
        // Tick down duration-based modifiers
        Iterator<Map.Entry<String, HappinessModifier>> it = modifiers.entrySet().iterator();
        while (it.hasNext()) {
            HappinessModifier mod = it.next().getValue();
            if (mod.ticksRemaining > 0) {
                mod.ticksRemaining -= 24000; // one day
                if (mod.ticksRemaining <= 0) {
                    it.remove();
                }
            }
        }
    }

    @Override
    public void read(CompoundTag compound) {
        modifiers.clear();
        if (compound.contains(TAG_MODIFIERS)) {
            CompoundTag modsTag = compound.getCompoundOrEmpty(TAG_MODIFIERS);
            for (String key : modsTag.keySet()) {
                CompoundTag modTag = modsTag.getCompoundOrEmpty(key);
                double value = modTag.getDoubleOr("Value", 0.0);
                int duration = modTag.getIntOr("Duration", 0);
                modifiers.put(key, new HappinessModifier(key, value, duration));
            }
        }
    }

    @Override
    public void write(CompoundTag compound) {
        CompoundTag modsTag = new CompoundTag();
        for (HappinessModifier mod : modifiers.values()) {
            CompoundTag modTag = new CompoundTag();
            modTag.putDouble("Value", mod.value);
            modTag.putInt("Duration", mod.ticksRemaining);
            modsTag.put(mod.name, modTag);
        }
        compound.put(TAG_MODIFIERS, modsTag);
    }

    @Override
    public List<String> getModifiers() {
        return new ArrayList<>(modifiers.keySet());
    }
}
