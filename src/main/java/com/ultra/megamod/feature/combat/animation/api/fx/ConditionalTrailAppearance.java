package com.ultra.megamod.feature.combat.animation.api.fx;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Wrapper for TrailAppearance that supports conditional appearances based on ItemStack predicates.
 * Conditions are checked in insertion order (LinkedHashMap). The first matching condition's appearance is used.
 * Ported 1:1 from BetterCombat (net.bettercombat.api.fx.ConditionalTrailAppearance).
 */
public record ConditionalTrailAppearance(
        TrailAppearance default_appearance,
        LinkedHashMap<String, TrailAppearance> conditional
) {
    public ConditionalTrailAppearance(TrailAppearance default_appearance) {
        this(default_appearance, new LinkedHashMap<>());
    }

    public ConditionalTrailAppearance() {
        this(TrailAppearance.DEFAULT, new LinkedHashMap<>());
    }

    @Nullable
    public TrailAppearance resolve(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return default_appearance;
        }

        for (Map.Entry<String, TrailAppearance> entry : conditional.entrySet()) {
            String conditionId = entry.getKey();
            if (ItemConditions.test(conditionId, itemStack)) {
                return entry.getValue();
            }
        }

        return default_appearance;
    }

    public ConditionalTrailAppearance merge(@Nullable ConditionalTrailAppearance override) {
        if (override == null) {
            return this;
        }

        TrailAppearance mergedDefault = override.default_appearance != null
                ? override.default_appearance
                : this.default_appearance;

        LinkedHashMap<String, TrailAppearance> mergedConditional = new LinkedHashMap<>(this.conditional);
        mergedConditional.putAll(override.conditional);

        return new ConditionalTrailAppearance(mergedDefault, mergedConditional);
    }
}
